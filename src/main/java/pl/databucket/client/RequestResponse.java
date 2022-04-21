package pl.databucket.client;

import lombok.Getter;
import lombok.Setter;

import javax.ws.rs.core.MultivaluedMap;

@Getter
@Setter
public class RequestResponse {

    private MultivaluedMap<String, String> requestHeaders;
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

    public String getResponseDurationStr() {
        long min = (responseDuration / 1000) / 60;
        long sec = (responseDuration / 1000) % 60;
        long ms = responseDuration % 1000;

        String result = "";
        if (min > 0)
            result += min + " min ";
        if (sec > 0)
            result += sec + " sec ";
        if (ms > 0)
            result += ms + " ms";

        return result;
    }

}
