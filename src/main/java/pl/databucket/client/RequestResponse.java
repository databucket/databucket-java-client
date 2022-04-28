package pl.databucket.client;

import lombok.Getter;
import lombok.Setter;
import javax.ws.rs.core.MultivaluedMap;

@Getter
@Setter
public class RequestResponse {

    private String requestUrl;
    private String requestMethod;
    private MultivaluedMap<String, String> requestHeaders;
    private String requestBody;

    private Integer responseStatus;
    private Long responseDuration;
    private MultivaluedMap<String, String> responseHeaders;
    private String responseBody;
    private boolean responseCorrect;
    private Exception exception;
}
