package pl.databucket.examples.data.user;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import pl.databucket.client.*;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class BucketUsers extends Bucket {

    private final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    public BucketUsers(Databucket databucket, String bucketName) {
        super(databucket, bucketName);
    }

    public String getErrorMessage(RequestResponse rr) {
        return "Response status: " + rr.getResponseStatus() + "\n\n" + rr.getResponseBody();
    }


    public User getUser(Rules rules) {
        RequestResponse rr = getData(rules);
        if (rr.isResponseCorrect()) {
            Map<String, Object> json = strToJson(rr.getResponseBody());
            if (json.containsKey("data")) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) json.get("data");
                if (dataList.size() > 0) {
                    Data data = jsonToData(dataList.get(0));
                    return new User(data);
                } else
                    return null;
            } else
                return null;
        } else
            throw new RuntimeException(getErrorMessage(rr));
    }

    public User getUser(Rules rules, List<String> fields) {
        RequestResponse rr = getData(rules, fields);
        System.out.println("Request headers: \n" + getPrettyHeaders(rr.getRequestHeaders()));
        System.out.println("Request body: \n" + getPrettyBody(rr.getRequestBody()));
        System.out.println("Response headers: \n" + getPrettyHeaders(rr.getResponseHeaders()));
        System.out.println("Response body: \n" + getPrettyBody(rr.getResponseBody()));
        if (rr.isResponseCorrect()) {
            Map<String, Object> json = strToJson(rr.getResponseBody());
            if (json.containsKey("customData")) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) json.get("customData");
                if (dataList.size() > 0) {
                    Data data = customJsonToData(dataList.get(0));
                    return new User(data);
                } else
                    return null;
            } else
                return null;
        } else
            throw new RuntimeException(getErrorMessage(rr));
    }

    public User reserveUser(Rules rules) {
        RequestResponse rr = reserveData(rules, true);
        if (rr.isResponseCorrect()) {
            Data data = strToData(rr.getResponseBody());
            return new User(data);
        } else
            throw new RuntimeException(getErrorMessage(rr));
    }

    public User insertUser(User user) {
        RequestResponse rr = insertData(user);
        if (rr.isResponseCorrect()) {
            Data data = strToData(rr.getResponseBody());
            return new User(data);
        } else
            throw new RuntimeException(getErrorMessage(rr));
    }

    public void insertMultiUser(List<User> userList) {
        insertMultiData(userList);
    }

    public User updateUser(User user) {
        RequestResponse rr = updateData(user);
        if (rr.isResponseCorrect()) {
            Data data = strToData(rr.getResponseBody());
            return new User(data);
        } else
            throw new RuntimeException(getErrorMessage(rr));
    }

    public String getResponseDurationStr(long duration) {
        long min = (duration / 1000) / 60;
        long sec = (duration / 1000) % 60;
        long ms = duration % 1000;

        String result = "";
        if (min > 0)
            result += min + " min ";
        if (sec > 0)
            result += sec + " sec ";
        if (ms > 0)
            result += ms + " ms";

        return result;
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
