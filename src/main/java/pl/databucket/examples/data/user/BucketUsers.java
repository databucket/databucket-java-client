package pl.databucket.examples.data.user;

import pl.databucket.client.*;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class BucketUsers extends Bucket {

    public BucketUsers(Databucket databucket, String bucketName) {
        super(databucket, bucketName);
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
            throw new RuntimeException(rr.getErrorMessage());
    }

    public User getUser(Rules rules, List<String> fields) {
        RequestResponse rr = getData(rules, fields);
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
            throw new RuntimeException(rr.getErrorMessage());
    }

    public User reserveUser(Rules rules) {
        RequestResponse rr = reserveData(rules, true);
        if (rr.isResponseCorrect()) {
            Data data = strToData(rr.getResponseBody());
            return new User(data);
        } else
            throw new RuntimeException(rr.getErrorMessage());
    }

    public User insertUser(User user) {
        RequestResponse rr = insertData(user);
        if (rr.isResponseCorrect()) {
            Data data = strToData(rr.getResponseBody());
            return new User(data);
        } else
            throw new RuntimeException(rr.getErrorMessage());
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
            throw new RuntimeException(rr.getErrorMessage());
    }
}
