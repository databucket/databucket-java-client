package pl.databucket.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;

public class Databucket {

    private final String serviceUrl;
    private Client client;
    private MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();

    public Databucket(String serviceUrl, boolean logs) {
        this.serviceUrl = serviceUrl;
        client = ClientBuilder.newClient();
        if (logs)
            client.register(new ClientResponseFilterLog());
        addBaseHeaders();
    }

    public Databucket(String serviceUrl, boolean logs, String proxyUri) {
        this.serviceUrl = serviceUrl;

        ClientConfig config = new ClientConfig();
        config.property(ClientProperties.PROXY_URI, proxyUri);

        client = ClientBuilder.newClient(config)
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);

        if (logs)
            client.register(new ClientResponseFilterLog());

        addBaseHeaders();
    }

    public Databucket(String serviceUrl, String username, String password, Integer projectId, boolean logs) {
        this.serviceUrl = serviceUrl;
        client = ClientBuilder.newClient();
        if (logs)
            client.register(new ClientResponseFilterLog());
        addBaseHeaders();
        authenticate(SignInRequestDTO.builder()
                .username(username)
                .password(password)
                .projectId(projectId)
                .build());
    }

    public Databucket(String serviceUrl, String username, String password, Integer projectId, boolean logs, String proxyUri) {
        this.serviceUrl = serviceUrl;

        ClientConfig config = new ClientConfig();
        config.property(ClientProperties.PROXY_URI, proxyUri);

        client = ClientBuilder.newClient(config)
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);

        if (logs)
            client.register(new ClientResponseFilterLog());

        addBaseHeaders();
        authenticate(SignInRequestDTO.builder()
                .username(username)
                .password(password)
                .projectId(projectId)
                .build());
    }

    public Header[] getHeaders2() {
        List<Header> headerList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String value = entry.getValue().toString();
            headerList.add(new BasicHeader(entry.getKey(), value.substring(1, value.length() - 1)));
        }
        return headerList.toArray(new Header[headers.size()]);
    }

    public void setClient(Client newClient) {
        client = newClient;
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

    public void setHeaders(MultivaluedMap<String, String> newHeaders) {
        headers = newHeaders;
    }
    public MultivaluedMap<String, String> getHeaders() {
        return headers;
    }

    @SuppressWarnings("unchecked")
    public void authenticate(SignInRequestDTO signInRequest) {
        WebTarget webTarget = client.target(buildUrl("/api/public/signin"));
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
        for (Map.Entry<String, List<String>> entry : getHeaders().entrySet()) {
            String value = entry.getValue().toString();
            builder = builder.header(entry.getKey(), value.substring(1, value.length() - 1));
        }

        Response response = builder.post(Entity.json(signInRequest));

        if (response.getStatus() == 200) {
            SignInResponseDTO signInResponse = response.readEntity(SignInResponseDTO.class);
            headers.putSingle("Authorization", "Bearer " + signInResponse.getToken());
        } else
            throw new RuntimeException("Response status: " + response.getStatus() + "\n\n" + response.readEntity(String.class));
    }
}
