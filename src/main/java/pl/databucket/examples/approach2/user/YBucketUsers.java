package pl.databucket.examples.approach2.user;

import com.fasterxml.jackson.core.type.TypeReference;
import pl.databucket.client.*;
import pl.databucket.client.CustomDataDef;

import java.util.List;
import java.util.Map;

public class YBucketUsers extends BaseBucket {

    public YBucketUsers(Databucket databucket, String bucketName) {
        super(databucket, bucketName);
    }

    public YUser insertUser(YUser user) {
        try {
            RequestResponse rr = insertData(user);
            if (rr.isResponseCorrect())
                return Mapper.objectMapper.readValue(rr.getResponseBody(), YUser.class);
            else
                throw new RuntimeException(rr.getException());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void insertMultiUser(List<YUser> userList) {
        insertMultiData(userList);
    }

    public YUser getUser(Long dataId) {
        try {
            RequestResponse rr = getData(dataId);
            if (rr.isResponseCorrect())
                return Mapper.objectMapper.readValue(rr.getResponseBody(), YUser.class);
            else
                throw new RuntimeException(rr.getException());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public YUser getUser(Rules rules) {
        try {
            RequestResponse rr = getData(rules);
            if (rr.isResponseCorrect()) {
                MultiDataResponse multiDataResponse = Mapper.objectMapper.readValue(rr.getResponseBody(), MultiDataResponse.class);
                if (multiDataResponse.getData() != null) {
                    List<YUser> listOfUsers = Mapper.objectMapper.convertValue(multiDataResponse.getData(), new TypeReference<List<YUser>>() {
                    });
                    return listOfUsers.get(0);
                }
                return null;
            } else
                throw new RuntimeException(rr.getException());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, Object> getUser(Rules rules, List<CustomDataDef> columns) {
        try {
            RequestResponse rr = getData(rules, columns);
            if (rr.isResponseCorrect()) {
                MultiDataResponse multiDataResponse = Mapper.objectMapper.readValue(rr.getResponseBody(), MultiDataResponse.class);
                if (multiDataResponse.getCustomData().size() > 0)
                    return multiDataResponse.getCustomData().get(0);
                else
                    return null;
            } else
                throw new RuntimeException(rr.getException());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public YUser reserveUser(Rules rules) {
        try {
            RequestResponse rr = reserveData(rules, true);
            if (rr.isResponseCorrect()) {
                MultiDataResponse multiDataResponse = Mapper.objectMapper.readValue(rr.getResponseBody(), MultiDataResponse.class);
                if (multiDataResponse.getData() != null) {
                    List<YUser> listOfUsers = Mapper.objectMapper.convertValue(multiDataResponse.getData(), new TypeReference<List<YUser>>() {
                    });
                    return listOfUsers.get(0);
                }
                return null;
            } else
                throw new RuntimeException(rr.getException());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public YUser updateUser(YUser user) {
        try {
            RequestResponse rr = updateData(user);
            if (rr.isResponseCorrect()) {
                return Mapper.objectMapper.readValue(rr.getRequestBody(), YUser.class);
            } else
                throw new RuntimeException(rr.getException());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
