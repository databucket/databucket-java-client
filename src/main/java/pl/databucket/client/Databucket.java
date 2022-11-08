package pl.databucket.client;

import java.net.Proxy;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.client.urlconnection.URLConnectionClientHandler;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class Databucket {

    private final String serviceUrl;
    private final Client client;
    private final MultivaluedMap<String, String> headers = new MultivaluedMapImpl();

    public Databucket(String serviceUrl, boolean logs) {
        this.serviceUrl = serviceUrl;
        client = Client.create();
        if (logs)
            client.addFilter(new LoggingFilter(System.out));
        addBaseHeaders();
    }

    public Databucket(String serviceUrl, boolean logs, Proxy proxy) {
        this.serviceUrl = serviceUrl;

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.initializeProxy(proxy);
        URLConnectionClientHandler clientHandler = new URLConnectionClientHandler(connectionFactory);
        client = new Client(clientHandler);
        if (logs)
            client.addFilter(new LoggingFilter(System.out));
        addBaseHeaders();
    }

    public Databucket(String serviceUrl, String username, String password, Integer projectId, boolean logs) {
        this.serviceUrl = serviceUrl;
        client = Client.create();
        if (logs)
            client.addFilter(new LoggingFilter(System.out));
        addBaseHeaders();
        authenticate(SignInRequestDTO.builder()
                .username(username)
                .password(password)
                .projectId(projectId)
                .build());
    }

    public Databucket(String serviceUrl, String username, String password, Integer projectId, boolean logs, Proxy proxy) {
        this.serviceUrl = serviceUrl;

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.initializeProxy(proxy);
        URLConnectionClientHandler clientHandler = new URLConnectionClientHandler(connectionFactory);
        client = new Client(clientHandler);
        if (logs)
            client.addFilter(new LoggingFilter(System.out));
        addBaseHeaders();
        authenticate(SignInRequestDTO.builder()
                .username(username)
                .password(password)
                .projectId(projectId)
                .build());
    }

    public Client getClient() {
        return client;
    }

    public String buildUrl(String resource) {
        return serviceUrl + resource;
    }

    private void addBaseHeaders() {
        headers.putSingle("User-Agent", "api-client");
        headers.putSingle("Content-Type", "application/json");
    }

    public MultivaluedMap<String, String> getHeaders() {
        return headers;
    }

    @SuppressWarnings("unchecked")
    public void authenticate(SignInRequestDTO signInRequest) {
        try {
            WebResource webResource = client.resource(buildUrl("/api/public/signin"));
            Builder builder = webResource.getRequestBuilder();
            for (Map.Entry<String, List<String>> entry : getHeaders().entrySet()) {
                String value = entry.getValue().toString();
                builder = builder.header(entry.getKey(), value.substring(1, value.length() - 1));
            }

            ClientResponse response = builder.post(ClientResponse.class, Mapper.objectMapper.writeValueAsString(signInRequest));
            String responseBody = response.getEntity(String.class);

            if (response.getStatus() == 200) {
                SignInResponseDTO signInResponse = Mapper.objectMapper.readValue(responseBody, SignInResponseDTO.class);
                headers.putSingle("Authorization", "Bearer " + signInResponse.getToken());
            } else
                throw new RuntimeException("Response status: " + response.getStatus() + "\n\n" + responseBody);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
