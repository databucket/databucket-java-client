package pl.databucket.examples;

import pl.databucket.client.*;
import pl.databucket.examples.config.SampleServerConfig;
import pl.databucket.examples.data.user.*;

import java.util.ArrayList;
import java.util.List;

public class Example {

    // This should be initiated once
    Databucket databucket = new Databucket(SampleServerConfig.SERVER_URL, SampleServerConfig.USER_NAME, SampleServerConfig.PASSWORD, SampleServerConfig.PROJECT_ID, SampleServerConfig.DEBUG_LOG);
    SampleBucketUsers bucketUsers = new SampleBucketUsers(databucket, "int-users");

    public static void main(String[] args) {
        Example userExamples = new Example();
        userExamples.run();
    }

    private void run() {
        // insert a new data.user
//        User user = new User();
//        user.setTag(UserTag.GOOD);
//        user.setReserved(true);
//        user.setEyeColor(UserEyeColor.BLUE);
//        user.setEmail("jakismail@test.io");
//        user = bucketUsers.insertUser(user);
//        System.out.println("userId: " + user.getId());

        // insert a new multi data.user
//        User user1 = new User();
//        user1.setReserved(true);
//        user1.setTag(UserTag.GOOD);
//        user1.setEyeColor(UserEyeColor.BLUE);
//        user1.setEmail("samplemail@test.io");
//
//        User user2 = new User();
//        user2.setReserved(false);
//        user2.setTag(UserTag.ANALYSIS);
//        user2.setEyeColor(UserEyeColor.BLUE);
//        user2.setEmail("samplemail@test.io");
//
//        List<User> userList = new ArrayList<>();
//        userList.add(user1);
//        userList.add(user2);
//
//        bucketUsers.insertMultiUser(userList);

        // modify data.user
//        user.setTag(UserTag.TRASH);
//        user.setReserved(false);
//        user = bucketUsers.updateUser(user);

        // rules def
        Rules nestedRules = new Rules(LogicalOperator.or);
        nestedRules.addRule(SampleUser.EMAIL, Operator.like, "%email%");
        nestedRules.addRule("id", Operator.graterEqual, 0);

        Rules rules = new Rules();
        rules.addRule(SampleUserRules.goodUser());
        rules.addRule(SampleUser.EYE_COLOR, Operator.equal, SampleUserEyeColor.BLUE);
        rules.addRule(SampleUser.NUMBER, Operator.notEqual, SampleUserNumber.ONE);
        rules.addNestedRules(nestedRules);

        List<String> fields = new ArrayList<>();
        fields.add(Field.ID);
        fields.add(SampleUser.EMAIL);
        fields.add(SampleUser.EYE_COLOR);

        // get data.user by rules
        SampleUser user = bucketUsers.getUser(rules, fields);

        // reserve data.user by rules
//        user = bucketUsers.reserveUser(rules);

    }

}
