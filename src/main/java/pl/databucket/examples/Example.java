package pl.databucket.examples;

import pl.databucket.client.*;
import pl.databucket.examples.config.ServerConfig;
import pl.databucket.examples.data.user.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Example {

    // This should be initiated once
    Databucket databucket = new Databucket(ServerConfig.SERVER_URL, ServerConfig.USER_NAME, ServerConfig.PASSWORD, ServerConfig.PROJECT_ID, ServerConfig.DEBUG_LOG);
    BucketUsers bucketUsers = new BucketUsers(databucket, "int-users");

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
//        Rules nestedRules = new Rules(LogicalOperator.or);
//        nestedRules.addRule(User.EMAIL, Operator.like, "%email%");
//        nestedRules.addRule("id", Operator.graterEqual, 0);

        Rules rules = new Rules();
        rules.addRule(UserRules.goodUser());
//        rules.addRule(User.EYE_COLOR, Operator.equal, UserEyeColor.BLUE);
//        rules.addRule(User.NUMBER, Operator.notEqual, UserNumber.ONE);
//        rules.addNestedRules(nestedRules);

        List<String> fields = new ArrayList<>();
        fields.add(Field.ID);
        fields.add(User.EMAIL);
        fields.add(User.EYE_COLOR);

        // get data.user by rules
        User user = bucketUsers.getUser(rules, fields);
        System.out.println(user.getEmail());

        // reserve data.user by rules
//        user = bucketUsers.reserveUser(rules);

    }

}
