package pl.databucket.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

@Getter
@Setter
public class RequestResponse {

    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private MultivaluedMap<String, String> requestHeaders;
    private String requestUrl;
    private String requestBody;
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

    public String getPrettyRequestHeaders() {
        return getPrettyHeaders(requestHeaders);
    }

    public String getPrettyResponseHeaders() {
        return getPrettyHeaders(responseHeaders);
    }

    private String getPrettyHeaders(MultivaluedMap<String, String> headers) {
        StringBuilder sb = new StringBuilder();
        headers.forEach((key, value) -> {
            String v = value.toString();
            sb.append(key).append(": ").append(v.substring(1, v.length() - 1)).append("\n");
        });
        return sb.toString();
    }

    public String getPrettyRequestBody() {
        String contentType = requestHeaders.getFirst("Content-Type");
        return getPrettyBody(contentType, requestBody);
    }

    public String getPrettyResponseBody() {
        String contentType = responseHeaders.getFirst("Content-Type");
        return getPrettyBody(contentType, responseBody);
    }

    private String getPrettyBody(String type, String content) {
        if (type.contains("json")) {
            JsonElement je = JsonParser.parseString(content);
            return gson.toJson(je);
        } else if (type.contains("xml")) {
            Source xmlInput = new StreamSource(new StringReader(content));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 2);
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            try {
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.transform(xmlInput, xmlOutput);
                return xmlOutput.getWriter().toString();
            } catch (TransformerException e) {
                e.printStackTrace();
            }
        }
        return content;
    }

}
