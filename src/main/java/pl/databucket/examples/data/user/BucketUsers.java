package pl.databucket.examples.data.user;

import pl.databucket.client.Bucket;
import pl.databucket.client.Data;
import pl.databucket.client.Databucket;
import pl.databucket.client.Rules;

import java.util.List;

public class BucketUsers extends Bucket {

    public BucketUsers(Databucket databucket, String bucketName) {
        super(databucket, bucketName);
    }

    public User getUser(Rules rules) {
        Data data = getData(rules);
        if (data != null)
            return new User(data);
        else
            return null;
    }

    public User reserveUser(Rules rules) {
        Data data = reserveData(rules, true);
        if (data != null)
            return new User(data);
        else
            return null;
    }

    public User insertUser(User user) {
        Data data = insertData(user);
        return new User(data);
    }

    public void insertMultiUser(List<User> userList) {
        insertMultiData(userList);
    }

    public User updateUser(User user) {
        Data data = updateData(user);
        return new User(data);
    }
}
