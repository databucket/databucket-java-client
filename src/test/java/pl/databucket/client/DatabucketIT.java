package pl.databucket.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs against a real Databucket with the DEMO dataset (docker compose of databucket-server, project "Acme Shop").
 * Every row it creates carries {@code $.clientIt = <run id>} and is removed at the end.
 * <p>
 * Settings (system property / environment variable): databucket.url / DATABUCKET_URL (default http://databucket.localhost),
 * databucket.project / DATABUCKET_PROJECT (default "Acme Shop"), databucket.bucket / DATABUCKET_BUCKET (default
 * dev-scheduler, a bucket with history). Users demo-robot (ROBOT) and demo-member (MEMBER), password "test".
 */
class DatabucketIT {

    static final String URL = setting("databucket.url", "DATABUCKET_URL", "http://databucket.localhost");
    static final String PROJECT = setting("databucket.project", "DATABUCKET_PROJECT", "Acme Shop");
    static final String BUCKET = setting("databucket.bucket", "DATABUCKET_BUCKET", "dev-scheduler");
    static final String RUN = UUID.randomUUID().toString();

    static Databucket robot;
    static Databucket member;
    static Bucket<Item> items;
    static Bucket<Map<String, Object>> raw;

    /** Properties of the test rows (null items are left out, so exists() / notExists() can be checked). */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Item {
        public String clientIt = RUN;
        public String name;
        public String status;
        public Integer count;
        public List<String> labels;
        public String tmp;

        public Item() {
        }

        Item(String name, String status, int count, List<String> labels) {
            this.name = name;
            this.status = status;
            this.count = count;
            this.labels = labels;
        }
    }

    static String setting(String property, String env, String defaultValue) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank())
            value = System.getenv(env);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    static Rule mine() {
        return Rule.property("$.clientIt").eq(RUN);
    }

    static Rules mine(Rule... more) {
        List<Rule> rules = new ArrayList<>();
        rules.add(mine());
        Collections.addAll(rules, more);
        return Rules.where(rules);
    }

    @BeforeAll
    static void setUp() {
        robot = Databucket.builder(URL).password("demo-robot", "test").project(PROJECT).build();
        member = Databucket.builder(URL).password("demo-member", "test").project(PROJECT).build();
        items = robot.bucket(BUCKET, Item.class);
        raw = robot.bucket(BUCKET);
    }

    @AfterAll
    static void cleanUp() {
        if (items != null)
            items.delete(Rules.where(mine()));
    }

    // ---------------------------------------------------------------- sign-in

    @Test
    void signsInToTheProjectByName() {
        assertEquals(PROJECT, robot.project().getName());
    }

    @Test
    void signsInToTheProjectById() {
        int id = robot.project().getId();
        Databucket byId = Databucket.builder(URL).password("demo-robot", "test").project(id).build();
        assertEquals(PROJECT, byId.project().getName());
    }

    @Test
    void severalProjectsWithoutChoiceFail() {
        Databucket noProject = Databucket.builder(URL).password("demo-robot", "test").build();
        DatabucketException e = assertThrows(DatabucketException.class, noProject::token);
        assertTrue(e.getMessage().contains("several projects"), e.getMessage());
    }

    @Test
    void wrongPasswordFails() {
        Databucket wrong = Databucket.builder(URL).password("demo-robot", "wrong").project(PROJECT).build();
        DatabucketException e = assertThrows(DatabucketException.class, wrong::token);
        assertEquals(401, e.getStatus());
    }

    @Test
    void unknownProjectFails() {
        Databucket wrong = Databucket.builder(URL).password("demo-robot", "test").project("No such project").build();
        assertThrows(DatabucketException.class, wrong::token);
    }

    @Test
    void invalidTokenFails() {
        Databucket wrong = Databucket.builder(URL).token("not-a-token").build();
        DatabucketException e = assertThrows(DatabucketException.class, () -> wrong.bucket(BUCKET).count(Rules.ALL));
        assertEquals(401, e.getStatus());
    }

    @Test
    void expiredTokenIsRenewed() {
        Databucket client = Databucket.builder(URL).password("demo-robot", "test").project(PROJECT).build();
        client.token();
        // simulate an expired token
        setToken(client, "expired");
        assertTrue(client.bucket(BUCKET).count(Rules.ALL) >= 0);
        assertFalse("expired".equals(client.token()));
    }

    static void setToken(Databucket client, String token) {
        try {
            java.lang.reflect.Field field = Databucket.class.getDeclaredField("token");
            field.setAccessible(true);
            field.set(client, token);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void unknownBucketIs404() {
        DatabucketException e = assertThrows(DatabucketException.class, () -> robot.bucket("no-such-bucket").count(Rules.ALL));
        assertEquals(404, e.getStatus());
    }

    // ---------------------------------------------------------------- insert and read

    @Test
    void insertGetAndFind() {
        DataRow<Item> row = items.insert(new Item("insert-get", "new", 1, List.of("a")));
        assertTrue(row.getId() > 0);
        assertEquals("insert-get", row.getProperties().name);
        assertFalse(row.isReserved());
        assertEquals("demo-robot", row.getCreatedBy());
        assertNotNull(row.getCreatedAt());

        DataRow<Item> read = items.get(row.getId()).orElseThrow();
        assertEquals(RUN, read.getProperties().clientIt);
        assertEquals(List.of("a"), read.getProperties().labels);

        assertTrue(items.get(Long.MAX_VALUE / 2).isEmpty());
        assertEquals(1, items.get(List.of(row.getId(), Long.MAX_VALUE / 2)).size());

        Optional<DataRow<Item>> found = items.find(mine(Rule.property("$.name").eq("insert-get"))).first();
        assertEquals(row.getId(), found.orElseThrow().getId());
    }

    @Test
    void insertReservedWithTag() {
        long tagId = anyTagId();
        DataRow<Item> row = items.insert(NewData.of(new Item("reserved-insert", "new", 1, List.of())).tagId(tagId).reserved(true));
        assertTrue(row.isReserved());
        assertEquals("demo-robot", row.getOwner());
        assertEquals(tagId, row.getTagId());
    }

    @Test
    void insertAllAndPaging() {
        String batch = "batch-" + UUID.randomUUID();
        List<Item> list = IntStream.rangeClosed(1, 7)
                .mapToObj(i -> new Item(batch, "paged", i, List.of()))
                .collect(Collectors.toList());
        items.insertAll(list);

        Rules rules = mine(Rule.property("$.name").eq(batch));
        assertEquals(7, items.count(rules));
        assertEquals(7, items.find(rules).count());

        Page<Item> page2 = items.find(rules).sort(Sort.desc("$.count")).limit(3).page(2).fetch();
        assertEquals(7, page2.getTotal());
        assertEquals(3, page2.getTotalPages());
        assertEquals(List.of(4, 3, 2), page2.getRows().stream().map(r -> r.getProperties().count).collect(Collectors.toList()));

        Page<Item> page3 = items.find(rules).sort(Sort.desc("$.count")).limit(3).page(3).fetch();
        assertEquals(1, page3.getRows().size());

        Page<Item> beyond = items.find(rules).limit(3).page(9).fetch();
        assertTrue(beyond.getRows().isEmpty());
    }

    @Test
    void mapProperties() {
        DataRow<Item> row = items.insert(new Item("as-map", "new", 5, List.of("x", "y")));
        Map<String, Object> props = raw.get(row.getId()).orElseThrow().getProperties();
        assertEquals("as-map", props.get("name"));
        assertEquals(5, props.get("count"));
    }

    // ---------------------------------------------------------------- rules (API v2)

    @Test
    void rules() {
        String group = "rules-" + UUID.randomUUID();
        items.insert(new Item(group, "active", 10, List.of("a", "b", "c")));
        items.insert(new Item(group, "active", 20, List.of()));
        Item noStatus = new Item(group, null, 30, null);
        noStatus.tmp = "x";
        items.insert(noStatus);
        Rule g = Rule.property("$.name").eq(group);

        assertEquals(2, items.count(mine(g, Rule.property("$.status").eq("active"))));
        assertEquals(1, items.count(mine(g, Rule.property("$.count").gt(15), Rule.property("$.count").lt(25))));
        assertEquals(2, items.count(mine(g, Rule.property("$.count").between(10, 20))));
        assertEquals(2, items.count(mine(g, Rule.property("$.count").in(10, 30))));
        assertEquals(1, items.count(mine(g, Rule.property("$.count").notIn(10, 30))));
        assertEquals(3, items.count(mine(g, Rule.property("$.name").like("rules-%"))));
        assertEquals(3, items.count(mine(g, Rule.property("$.name").matchesIgnoreCase("^RULES-"))));
        assertEquals(1, items.count(mine(g, Rule.property("$.status").isNull())));
        assertEquals(1, items.count(mine(g, Rule.property("$.status").isEmpty())));
        assertEquals(2, items.count(mine(g, Rule.property("$.status").isNotEmpty())));
        assertEquals(1, items.count(mine(g, Rule.property("$.tmp").exists())));
        assertEquals(2, items.count(mine(g, Rule.property("$.tmp").notExists())));
        assertEquals(1, items.count(mine(g, Rule.property("$.labels").length().gt(2))));
        assertEquals(2, items.count(mine(g, Rule.or(Rule.property("$.count").eq(10), Rule.property("$.count").eq(30)))));
        assertEquals(1, items.count(mine(g, Rule.not(Rule.property("$.count").le(20)))));
        assertEquals(3, items.count(mine(g, Rule.field(Field.CREATED_BY).eq(Rule.CURRENT_USER))));
        assertEquals(3, items.count(mine(g, Rule.owner().isNull())));
    }

    @Test
    void rulesInTheApiFormat() {
        String group = "format-" + UUID.randomUUID();
        items.insert(new Item(group, "active", 1, List.of()));
        items.insert(new Item(group, "done", 2, List.of()));
        Rules rules = Rules.fromRules(List.of(
                List.of("$.clientIt", "==", RUN),
                List.of("$.name", "==", group),
                Map.of("or", List.of(List.of("$.status", "==", "done"), List.of("$.count", ">", 5)))));
        assertEquals(1, items.count(rules));
    }

    @Test
    void sqlTextRulesAreRefusedInVersion2() {
        DatabucketException e = assertThrows(DatabucketException.class,
                () -> items.count(Rules.fromRules(List.of("$.count > 1"))));
        assertEquals(400, e.getStatus());
    }

    @Test
    void unknownFieldIs400() {
        DatabucketException e = assertThrows(DatabucketException.class,
                () -> items.count(Rules.fromRules(List.of(List.of("noSuchField", "==", 1)))));
        assertEquals(400, e.getStatus());
    }

    // ---------------------------------------------------------------- reserve

    @Test
    void reserveAndRelease() {
        String group = "reserve-" + UUID.randomUUID();
        for (int i = 0; i < 5; i++)
            items.insert(new Item(group, "free", i, List.of()));
        Rules free = mine(Rule.property("$.name").eq(group));

        assertEquals(5, items.reserve(free).available());

        DataRow<Item> one = items.reserve(free).one().orElseThrow();
        assertTrue(one.isReserved());
        assertEquals("demo-robot", one.getOwner());

        Reservation<Item> two = items.reserve(free).limit(2).sort(Sort.RANDOM).fetch();
        assertEquals(2, two.getRows().size());
        assertEquals(2, two.getAvailable());

        Reservation<Item> rest = items.reserve(free).limit(10).fetch();
        assertEquals(2, rest.getRows().size());
        assertEquals(0, rest.getAvailable());
        assertTrue(items.reserve(free).one().isEmpty());

        assertEquals(1, items.release(one.getId()));
        DataRow<Item> released = items.get(one.getId()).orElseThrow();
        assertFalse(released.isReserved());
        assertNull(released.getOwner());
        assertEquals(1, items.reserve(free).available());
    }

    @Test
    void parallelReservationsNeverShareARow() throws Exception {
        String group = "parallel-" + UUID.randomUUID();
        List<Item> list = IntStream.range(0, 20).mapToObj(i -> new Item(group, "free", i, List.of())).collect(Collectors.toList());
        items.insertAll(list);
        Rules free = mine(Rule.property("$.name").eq(group));

        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<Optional<DataRow<Item>>>> futures = new ArrayList<>();
            for (int i = 0; i < 24; i++)
                futures.add(pool.submit(() -> items.reserve(free).one()));
            Set<Long> ids = new HashSet<>();
            int empty = 0;
            for (Future<Optional<DataRow<Item>>> f : futures) {
                Optional<DataRow<Item>> row = f.get();
                if (row.isPresent())
                    assertTrue(ids.add(row.get().getId()), "row reserved twice: " + row.get().getId());
                else
                    empty++;
            }
            assertEquals(20, ids.size());
            assertEquals(4, empty);
        } finally {
            pool.shutdown();
        }
    }

    // ---------------------------------------------------------------- modify and remove

    @Test
    void updateByIdsAndRules() {
        DataRow<Item> row = items.insert(new Item("update", "new", 1, List.of()));
        Item tmp = new Item("update", "new", 1, List.of());
        tmp.tmp = "remove me";
        DataRow<Item> row2 = items.insert(tmp);

        assertEquals(2, items.update(Change.create().set("$.status", "used").remove("$.tmp"), row.getId(), row2.getId()));
        DataRow<Item> changed = items.get(row2.getId()).orElseThrow();
        assertEquals("used", changed.getProperties().status);
        assertNull(changed.getProperties().tmp);

        long tagId = anyTagId();
        int n = items.update(mine(Rule.property("$.name").eq("update")), Change.create().tagId(tagId).set("$.count", 9));
        assertEquals(2, n);
        assertEquals(tagId, items.get(row.getId()).orElseThrow().getTagId());
        assertEquals(9, items.get(row.getId()).orElseThrow().getProperties().count);

        Item replaced = new Item("update-replaced", "x", 0, List.of());
        items.update(Change.create().properties(replaced), row.getId());
        assertEquals("update-replaced", items.get(row.getId()).orElseThrow().getProperties().name);
    }

    @Test
    void updateAndDeleteNeedRules() {
        assertThrows(IllegalArgumentException.class, () -> items.update(Rules.ALL, Change.create().set("$.a", 1)));
        assertThrows(IllegalArgumentException.class, () -> items.delete(Rules.ALL));
    }

    @Test
    void deleteByIdsAndRules() {
        DataRow<Item> a = items.insert(new Item("delete", "x", 1, List.of()));
        DataRow<Item> b = items.insert(new Item("delete", "x", 2, List.of()));
        items.insert(new Item("delete", "x", 3, List.of()));
        assertEquals(1, items.delete(a.getId()));
        assertTrue(items.get(a.getId()).isEmpty());
        assertEquals(0, items.delete(a.getId()));
        assertEquals(2, items.delete(mine(Rule.property("$.name").eq("delete"))));
        assertTrue(items.get(b.getId()).isEmpty());
    }

    // ---------------------------------------------------------------- history

    @Test
    void historyForMember() {
        DataRow<Item> row = items.insert(new Item("history", "new", 1, List.of()));
        items.update(Change.create().set("$.status", "used"), row.getId());
        items.reserve(mine(Rule.id().eq(row.getId()))).one().orElseThrow();

        Bucket<Item> asMember = member.bucket(BUCKET, Item.class);
        List<HistoryEntry> history = asMember.history(row.getId());
        assertEquals(3, history.size(), history.toString());
        assertTrue(history.get(0).isPropertiesChanged());
        assertTrue(history.get(1).isPropertiesChanged());
        assertEquals(Boolean.TRUE, history.get(2).getReserved());
        assertEquals("demo-robot", history.get(2).getModifiedBy());

        Map<Long, Item> versions = asMember.historyProperties(row.getId(), history.get(0).getId(), history.get(1).getId());
        assertEquals("new", versions.get(history.get(0).getId()).status);
        assertEquals("used", versions.get(history.get(1).getId()).status);

        assertTrue(asMember.clearHistory(row.getId()) >= 2);
        assertTrue(asMember.history(row.getId()).size() <= 1);
    }

    @Test
    void historyIsNotAvailableToRobots() {
        // server: DataHistoryController allows MEMBER and ADMIN only
        DataRow<Item> row = items.insert(new Item("history-robot", "new", 1, List.of()));
        DatabucketException e = assertThrows(DatabucketException.class, () -> items.history(row.getId()));
        assertEquals(403, e.getStatus());
    }

    // ---------------------------------------------------------------- helpers

    static long anyTagId() {
        // tags of the DEMO dataset are used by the rows of every bucket; take one from the data
        return robot.bucket("dev-users").find(Rules.where(Rule.tagId().isNotNull())).first().orElseThrow().getTagId();
    }
}
