package pl.databucket.client;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class RequestResponse {

    private String requestUrl;
    private String requestMethod;
    private MultivaluedMap<String, String> requestHeaders;
    private String requestBody;

    private Integer responseStatus;
    private Long responseDuration;
    private MultivaluedMap<String, Object> responseHeaders;
    private String responseBody;
    private boolean responseCorrect;
    private Exception exception;

}
