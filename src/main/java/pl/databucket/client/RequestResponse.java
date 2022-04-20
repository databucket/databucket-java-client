package pl.databucket.client;

import lombok.Getter;
import lombok.Setter;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Map;

@Getter
@Setter
public class RequestResponse {

    private Map<String, Object> requestHeaders;
    private String requestUrl;
    private Object requestBody;
    private String requestMethod;

    private int responseStatus;
    private long responseDuration;
    private MultivaluedMap<String, String> responseHeaders;
    private String responseBody;
    private boolean responseCorrect;

    public String getErrorMessage() {
        return "Response status: " + responseStatus + "\n\n" + responseBody;
    }

}
