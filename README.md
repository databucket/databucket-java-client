# databucket-java-client

[![Maven Central](https://img.shields.io/maven-central/v/pl.databucket/databucket-client)](https://central.sonatype.com/artifact/pl.databucket/databucket-client)

Java client for [Databucket](https://github.com/databucket/databucket-server): find, reserve, change and remove test
data from your automated tests.

- Java 11+, one dependency (Jackson), the JDK `HttpClient`
- API version 2 of Databucket (3.13.0 or newer; history search and history for ROBOT accounts: 3.14.0)
- Sign-in with a password or with a token of your identity provider (SSO robot accounts, Databucket 3.10.0+);
  the token is renewed when it expires
- Thread-safe; parallel tests never reserve the same row

```xml
<dependency>
  <groupId>pl.databucket</groupId>
  <artifactId>databucket-client</artifactId>
  <version>2.0.0</version>
</dependency>
```

Version 1.x (Java 8, API version 1) works with older Databucket servers; its code is on the `main` branch history
before 2.0.0.

## Quick start

```java
Databucket databucket = Databucket.builder("https://databucket.example.com")
        .password("tests-robot", System.getenv("DATABUCKET_PASSWORD"))
        .project("Acme Shop")                 // by name or id; needed when the user has several projects
        .build();

// properties as your own class (unknown properties are ignored) ...
Bucket<User> users = databucket.bucket("dev-users", User.class);
// ... or as a map
Bucket<Map<String, Object>> config = databucket.bucket("config");

// reserve one free active user for this test
DataRow<User> user = users.reserve(Rules.where(
                Rule.property("$.account.status").eq("active"),
                Rule.property("$.address.country").in("GB", "PL")))
        .one()
        .orElseThrow();

// ... the test ...

users.update(Change.create().set("$.account.status", "suspended"), user.getId());
users.release(user.getId());
```

## Rules

Rules are written in Java and sent as JsonLogic, so values are never mistaken for fields or properties.

```java
Rule.property("$.address.city").eq("Paris")         // also: ne, gt, ge, lt, le
Rule.property("$.age").between(18, 65)
Rule.tagId().in(1, 2)                                // also: notIn
Rule.property("$.email").like("%@example.com")       // also: notLike, matches, matchesIgnoreCase, notMatches
Rule.owner().isNull()                                // also: isNotNull
Rule.property("$.phone").isEmpty()                   // null, missing or ""; also: isNotEmpty
Rule.property("$.tmp").exists()                      // also: notExists
Rule.property("$.orders").length().gt(2)             // items of an array
Rule.field(Field.CREATED_BY).eq(Rule.CURRENT_USER)   // the signed-in user
Rule.or(rule1, rule2), Rule.and(...), Rule.not(rule)

Rules.where(rule1, rule2)   // all must match
Rules.ALL                   // every row (not accepted for update / delete)
Rules.fromRules(list)       // the API "rules" format, e.g. copied from the "API request" tab of the rich filter
Rules.fromLogic(map)        // JsonLogic
```

Fields: `id`, `tagId`, `owner`, `reserved`, `createdBy`, `createdAt`, `modifiedBy`, `modifiedAt` (`Field`).
Dates are compared as ISO text with `Z`, e.g. `Instant.parse("2026-01-01T00:00:00Z")`.

## Operations

| Call | API |
|---|---|
| `insert(properties)`, `insert(NewData.of(p).tagId(3L).reserved(true))` | `POST /api/bucket/{bucket}` |
| `insertAll(list)`, `insertAllData(list)` | `POST /api/bucket/{bucket}/multi` |
| `get(id)`, `get(ids)` | `GET /api/bucket/{bucket}/{ids}` |
| `find(rules).sort(Sort.desc("$.name")).limit(20).page(2).fetch()` / `.list()` / `.first()` / `.count()`, `count(rules)` | `POST /api/bucket/{bucket}/get` |
| `reserve(rules).one()`, `.limit(3).sort(Sort.RANDOM).fetch()`, `.available()`, `.forUser("john")` (ADMIN) | `POST /api/bucket/{bucket}/reserve` |
| `update(change, ids...)`, `update(rules, change)`, `release(ids...)` | `PUT /api/bucket/{bucket}[/{ids}]` |
| `delete(ids...)`, `delete(rules)` | `DELETE /api/bucket/{bucket}[/{ids}]` |
| `history(id)`, `historyProperties(id, historyIds...)`, `clearHistory(id)`, `clearHistory(rules)` | `/api/bucket/{bucket}/{id}/history...` |
| `searchHistory(rows).where(...).sort(...).limit(50).withProperties().fetch()` / `.list()` / `.count()` | `POST /api/bucket/{bucket}/history/get` |

## History

```java
// changes made by the nightly robot since yesterday, on the rows that are now in GB, newest first
HistoryPage<User> page = users.searchHistory(Rules.where(Rule.property("$.address.country").eq("GB")))
        .where(Rule.field(HistoryField.MODIFIED_BY).eq("nightly-robot"),
               Rule.field(HistoryField.MODIFIED_AT).ge(Instant.now().minus(1, ChronoUnit.DAYS)))
        .sort(Sort.desc(HistoryField.MODIFIED_AT))
        .limit(50)
        .withProperties()                 // the properties of each version
        .fetch();
```

The rows are chosen by their current state (`Rules`), the entries by the fields of the history (`HistoryField`:
`ID`, `DATA_ID`, `TAG_ID`, `RESERVED`, `MODIFIED_BY`, `MODIFIED_AT`); the properties of past versions can't be searched.

Errors come as `DatabucketException` with the HTTP status and the server's message.

## Sign-in

```java
.password("tests-robot", password)       // POST /api/public/signin
.ssoToken(() -> fetchTokenFromIdp())     // POST /api/public/sso/token-exchange (client credentials of a ROBOT)
.token(databucketToken)                  // a token you already have (not renewed)
.project(15) / .project("Acme Shop")
.timeout(Duration.ofSeconds(10))
.httpClient(HttpClient.newBuilder().proxy(ProxySelector.of(new InetSocketAddress("proxy", 8080))).build())
```

Requests are logged at DEBUG level with `System.Logger` (`pl.databucket.client.Databucket`).

## Build

```
./mvnw verify            # unit and integration tests
./mvnw test              # unit tests only
./mvnw verify -DskipITs  # without integration tests
```

The integration tests (`*IT`) need Databucket with the DEMO dataset, e.g. the docker compose of databucket-server
(http://databucket.localhost, project "Acme Shop", users `demo-robot` and `demo-member`); the history tests need
Databucket 3.14.0+. Change it with
`-Ddatabucket.url=...`, `-Ddatabucket.project=...`, `-Ddatabucket.bucket=...` (or `DATABUCKET_URL`, ...).
They work only on rows they create and remove them at the end.

Release to Maven Central: `./mvnw -Prelease -DskipITs deploy` (Central Portal token as server `central` in
`~/.m2/settings.xml`, GPG key).
