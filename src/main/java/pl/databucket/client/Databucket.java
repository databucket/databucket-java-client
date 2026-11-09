package pl.databucket.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Client for one Databucket project, signed in as one user (typically a ROBOT account).
 * <pre>{@code
 * Databucket databucket = Databucket.builder("https://databucket.example.com")
 *         .password("tests-robot", System.getenv("DATABUCKET_PASSWORD"))
 *         .project("Acme Shop")
 *         .build();
 *
 * Bucket<User> users = databucket.bucket("dev-users", User.class);
 * Optional<DataRow<User>> user = users.reserve(Rules.where(Rule.property("$.status").eq("active"))).one();
 * }</pre>
 * The client signs in on the first call and again when the token expires. It is thread-safe.
 * All data calls use API version 2 (header {@value #API_VERSION_HEADER}: 2).
 */
public final class Databucket {

    public static final String API_VERSION_HEADER = "Databucket-Api-Version";
    static final String API_VERSION = "2";
    static final String USER_AGENT = "databucket-java-client/2";

    private static final System.Logger LOG = System.getLogger(Databucket.class.getName());

    private final String baseUrl;
    private final HttpClient http;
    private final Duration timeout;
    private final ObjectMapper mapper;
    private final SignIn signIn;
    private final Integer projectId;
    private final String projectName;

    private volatile String token;
    private volatile Project project;

    private Databucket(Builder builder) {
        this.baseUrl = builder.url.endsWith("/") ? builder.url.substring(0, builder.url.length() - 1) : builder.url;
        this.http = builder.httpClient != null ? builder.httpClient
                : HttpClient.newBuilder().connectTimeout(builder.timeout).followRedirects(HttpClient.Redirect.NORMAL).build();
        this.timeout = builder.timeout;
        this.mapper = builder.objectMapper != null ? builder.objectMapper : defaultMapper();
        this.signIn = builder.signIn;
        this.projectId = builder.projectId;
        this.projectName = builder.projectName;
        this.token = builder.token;
    }

    /** @param url the address of Databucket, e.g. {@code https://databucket.example.com} or {@code http://localhost:8080} */
    public static Builder builder(String url) {
        return new Builder(url);
    }

    static ObjectMapper defaultMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /** A bucket whose properties are read as maps. */
    public Bucket<Map<String, Object>> bucket(String name) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        Class<Map<String, Object>> type = (Class) Map.class;
        return new Bucket<>(this, name, type);
    }

    /** A bucket whose properties are read as the given class (Jackson mapping; unknown properties are ignored). */
    public <T> Bucket<T> bucket(String name, Class<T> propertiesType) {
        return new Bucket<>(this, name, propertiesType);
    }

    /** The project the client works in (signs in if not done yet); null when only a token was given. */
    public Project project() {
        token();
        return project;
    }

    /** The current access token (signs in if needed). */
    public String token() {
        String current = token;
        if (current != null)
            return current;
        synchronized (this) {
            if (token == null)
                signIn();
            return token;
        }
    }

    ObjectMapper mapper() {
        return mapper;
    }

    // ---------------------------------------------------------------- sign-in

    private void signIn() {
        if (signIn == null)
            throw new IllegalStateException("No credentials: use password(...), ssoToken(...) or token(...) in the builder");

        Integer id = projectId;
        JsonNode response = signIn.call(this, id);
        if (id == null && response.path("token").isMissingNode() && response.has("projects")) {
            // the user has several projects: pick one by name
            id = pickProject(response.get("projects"));
            response = signIn.call(this, id);
        }
        if (projectName != null && id == null && response.has("project")
                && !projectName.equals(response.get("project").path("name").asText()))
            throw new DatabucketException(0, "The user is not assigned to the project '" + projectName + "'", null);

        String newToken = response.path("token").asText(null);
        if (newToken == null || response.path("changePassword").asBoolean(false))
            throw new DatabucketException(0, "Sign-in gave no token for a project"
                    + (response.path("changePassword").asBoolean(false) ? ": the user has to change the password first" : ""),
                    response.toString());

        JsonNode p = response.path("project");
        this.project = p.isMissingNode() ? null : new Project(p.path("id").asInt(), p.path("name").asText(null));
        this.token = newToken;
        LOG.log(System.Logger.Level.DEBUG, "Signed in to project {0}", project);
    }

    private Integer pickProject(JsonNode projects) {
        List<String> names = new ArrayList<>();
        for (JsonNode p : projects) {
            String name = p.path("name").asText();
            if (name.equals(projectName))
                return p.path("id").asInt();
            names.add(name);
        }
        throw new DatabucketException(0, (projectName == null
                ? "The user has several projects, choose one with project(...): "
                : "The user is not assigned to the project '" + projectName + "'; projects: ") + names, null);
    }

    JsonNode signInCall(String path, Map<String, Object> body) {
        HttpRequest request = request(path).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json(body))).build();
        HttpResponse<String> response = send(request);
        return handle(request, response);
    }

    // ---------------------------------------------------------------- HTTP

    JsonNode call(String method, String path, Object body) {
        return call(method, path, body, true);
    }

    private JsonNode call(String method, String path, Object body, boolean retry) {
        String currentToken = token();
        HttpRequest.Builder builder = request(path)
                .header("Authorization", "Bearer " + currentToken)
                .header(API_VERSION_HEADER, API_VERSION);
        if (body != null)
            builder.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(json(body)));
        else
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        HttpRequest request = builder.build();
        HttpResponse<String> response = send(request);

        if (response.statusCode() == 401 && retry && signIn != null) {
            // the token expired: sign in again once
            synchronized (this) {
                if (Objects.equals(token, currentToken))
                    token = null;
            }
            return call(method, path, body, false);
        }
        return handle(request, response);
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(timeout)
                .header("Accept", "application/json")
                .header("User-Agent", USER_AGENT);
    }

    private HttpResponse<String> send(HttpRequest request) {
        long start = System.nanoTime();
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            LOG.log(System.Logger.Level.DEBUG, "{0} {1} -> {2} ({3} ms)", request.method(), request.uri(),
                    response.statusCode(), (System.nanoTime() - start) / 1_000_000);
            return response;
        } catch (IOException e) {
            throw new DatabucketException(request.method() + " " + request.uri() + " failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DatabucketException(request.method() + " " + request.uri() + " interrupted", e);
        }
    }

    private JsonNode handle(HttpRequest request, HttpResponse<String> response) {
        String body = response.body();
        JsonNode json = parse(body);
        if (response.statusCode() >= 400) {
            String message = json.path("message").asText(null);
            if (message == null)
                message = json.path("error").asText(body == null || body.isBlank() ? "no response body" : body);
            throw new DatabucketException(response.statusCode(),
                    message + " (" + request.method() + " " + request.uri().getPath() + ")", body);
        }
        return json;
    }

    private JsonNode parse(String body) {
        if (body == null || body.isBlank())
            return NullNode.getInstance();
        try {
            return mapper.readTree(body);
        } catch (JsonProcessingException e) {
            return NullNode.getInstance();
        }
    }

    String json(Object body) {
        try {
            return mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Can not convert the request to JSON: " + e.getOriginalMessage(), e);
        }
    }

    // ---------------------------------------------------------------- types

    /** The project the client is signed in to. */
    public static final class Project {
        private final int id;
        private final String name;

        Project(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name + " (" + id + ")";
        }
    }

    @FunctionalInterface
    private interface SignIn {
        JsonNode call(Databucket databucket, Integer projectId);
    }

    public static final class Builder {

        private final String url;
        private SignIn signIn;
        private String token;
        private Integer projectId;
        private String projectName;
        private Duration timeout = Duration.ofSeconds(30);
        private HttpClient httpClient;
        private ObjectMapper objectMapper;

        private Builder(String url) {
            if (url == null || url.isBlank())
                throw new IllegalArgumentException("The Databucket url is empty");
            this.url = url;
        }

        /** Sign in with a username and password (POST /api/public/signin). */
        public Builder password(String username, String password) {
            this.signIn = (db, projectId) -> {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("username", username);
                body.put("password", password);
                if (projectId != null)
                    body.put("projectId", projectId);
                return db.signInCall("/api/public/signin", body);
            };
            return this;
        }

        /**
         * Sign in with an access token of the identity provider (OAuth2 client credentials of a ROBOT account),
         * exchanged for a Databucket token (POST /api/public/sso/token-exchange). The supplier is called on every
         * sign-in, so it can fetch a fresh token.
         */
        public Builder ssoToken(Supplier<String> identityProviderToken) {
            this.signIn = (db, projectId) -> {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("token", identityProviderToken.get());
                if (projectId != null)
                    body.put("projectId", projectId);
                return db.signInCall("/api/public/sso/token-exchange", body);
            };
            return this;
        }

        /** Use a Databucket token you already have (no sign-in; it is not renewed). */
        public Builder token(String token) {
            this.token = token;
            return this;
        }

        /** The project by id. */
        public Builder project(int projectId) {
            this.projectId = projectId;
            this.projectName = null;
            return this;
        }

        /** The project by name (needed only when the user has several projects). */
        public Builder project(String projectName) {
            this.projectName = projectName;
            this.projectId = null;
            return this;
        }

        /** Timeout of the connection and of each request (default 30 s). */
        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /** Your own HTTP client, e.g. with a proxy: {@code HttpClient.newBuilder().proxy(ProxySelector.of(...)).build()}. */
        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        /** Your own Jackson mapper for the properties (it should ignore unknown properties and handle java.time). */
        public Builder objectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        public Databucket build() {
            if (signIn == null && token == null)
                throw new IllegalStateException("No credentials: use password(...), ssoToken(...) or token(...)");
            return new Databucket(this);
        }
    }
}
