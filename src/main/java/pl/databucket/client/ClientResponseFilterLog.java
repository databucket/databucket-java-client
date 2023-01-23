package pl.databucket.client;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ClientResponseFilterLog implements ClientResponseFilter {
    @Override
    public void filter(ClientRequestContext clientRequestContext, ClientResponseContext clientResponseContext) throws IOException {
        System.out.println(clientRequestContext.getMethod() + " " + clientRequestContext.getUri());
        if (clientRequestContext.getEntity() == null) {
            System.out.println("Request body: empty");
        } else
            System.out.println("Request body:\n" + Mapper.objectMapper.writeValueAsString(clientRequestContext.getEntity()) + "\n");

        if (clientResponseContext.hasEntity()) {
            String body = new BufferedReader(
                    new InputStreamReader(clientResponseContext.getEntityStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            System.out.println("Response body:\n" +  body + "\n");

            clientResponseContext.setEntityStream(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        }
    }

}