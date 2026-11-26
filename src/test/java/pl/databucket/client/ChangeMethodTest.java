package pl.databucket.client;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Data rows are changed with PATCH; a server before 3.26.0 answers 405 and gets PUT from then on. */
class ChangeMethodTest {

    private HttpServer server;
    private final List<String> calls = new CopyOnWriteArrayList<>();
    private volatile boolean patchKnown;
    private volatile int patchStatus = 200;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/bucket/orders", exchange -> {
            String method = exchange.getRequestMethod();
            calls.add(method + " " + exchange.getRequestURI().getPath());
            exchange.getRequestBody().readAllBytes();
            int status = "PATCH".equals(method) ? (patchKnown ? patchStatus : 405) : 200;
            byte[] body = (status == 200 ? "{\"message\": \"Modified 2 data row(s)\"}"
                    : "{\"message\": \"Request method 'PATCH' is not supported\"}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private Bucket<?> orders() {
        return Databucket.builder("http://127.0.0.1:" + server.getAddress().getPort()).token("t").build().bucket("orders");
    }

    @Test
    void changes_with_patch() {
        patchKnown = true;
        Bucket<?> orders = orders();

        assertEquals(2, orders.update(Change.create().set("$.status", "PAID"), 1L, 2L));
        assertEquals(2, orders.update(Rules.where(Rule.field(Field.ID).gt(0)), Change.create().set("$.status", "PAID")));

        assertEquals(List.of("PATCH /api/bucket/orders/1,2", "PATCH /api/bucket/orders"), calls);
    }

    @Test
    void falls_back_to_put_on_an_older_server_and_stays_with_it() {
        Bucket<?> orders = orders();

        assertEquals(2, orders.update(Change.create().set("$.status", "PAID"), 1L, 2L));
        assertEquals(2, orders.update(Change.create().set("$.status", "SHIPPED"), 1L, 2L));

        assertEquals(List.of("PATCH /api/bucket/orders/1,2", "PUT /api/bucket/orders/1,2", "PUT /api/bucket/orders/1,2"),
                calls);
    }

    @Test
    void other_errors_of_patch_are_not_retried() {
        patchKnown = true;
        patchStatus = 406;

        DatabucketException e = assertThrows(DatabucketException.class,
                () -> orders().update(Change.create().set("$.status", "PAID"), 1L));

        assertEquals(406, e.getStatus());
        assertEquals(List.of("PATCH /api/bucket/orders/1"), calls);
    }
}
