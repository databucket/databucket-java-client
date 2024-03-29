package pl.databucket.examples.approach1.user;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import jakarta.ws.rs.core.MultivaluedMap;
import pl.databucket.client.*;
import pl.databucket.client.Data;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class XBucketUsers extends Bucket {

    private final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    public XBucketUsers(Databucket databucket, String bucketName) {
        super(databucket, bucketName);
    }

    public String getErrorMessage(RequestResponse rr) {
        return "Response status: " + rr.getResponseStatus() + "\n\n" + rr.getResponseBody();
    }

    public XUser getUser(Rules rules) {
        RequestResponse rr = getData(rules);
        if (rr.isResponseCorrect()) {
            Map<String, Object> json = strToJson(rr.getResponseBody());
            if (json.containsKey("data")) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) json.get("data");
                if (dataList.size() > 0) {
                    Data data = jsonToData(dataList.get(0));
                    return new XUser(data);
                } else
                    return null;
            } else
                return null;
        } else
            throw new RuntimeException(getErrorMessage(rr));
    }

    public XUser getUser(Rules rules, List<String> fields) {
        RequestResponse rr = getData(rules, fields);
        if (rr.isResponseCorrect()) {
            Map<String, Object> json = strToJson(rr.getResponseBody());
            if (json.containsKey("customData")) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) json.get("customData");
                if (dataList.size() > 0) {
                    Data data = customJsonToData(dataList.get(0));
                    return new XUser(data);
                } else
                    return null;
            } else
                return null;
        } else
            throw new RuntimeException(getErrorMessage(rr));
    }

    public XUser reserveUser(Rules rules) {
        RequestResponse rr = reserveData(rules, true);
        if (rr.isResponseCorrect()) {
            Data data = strToData(rr.getResponseBody());
            return new XUser(data);
        } else
            throw new RuntimeException(getErrorMessage(rr));
    }

    public XUser insertUser(XUser user) {
        RequestResponse rr = insertData(user);
        if (rr.isResponseCorrect()) {
            Data data = strToData(rr.getResponseBody());
            return new XUser(data);
        } else
            throw new RuntimeException(getErrorMessage(rr));
    }

    public void insertMultiUser(List<XUser> userList) {
        insertMultiData(userList);
    }

    public XUser updateUser(XUser user) {
        RequestResponse rr = updateData(user);
        if (rr.isResponseCorrect()) {
            Data data = strToData(rr.getResponseBody());
            return new XUser(data);
        } else
            throw new RuntimeException(getErrorMessage(rr));
    }

    private String getPrettyHeaders(MultivaluedMap<String, String> headers) {
        StringBuilder sb = new StringBuilder();
        headers.forEach((key, value) -> {
            String v = value.toString();
            sb.append(key).append(": ").append(v, 1, v.length() - 1).append("\n");
        });
        return sb.toString();
    }

    private String getPrettyBody(String content) {
        JsonElement je = JsonParser.parseString(content);
        return gson.toJson(je);
    }
}
