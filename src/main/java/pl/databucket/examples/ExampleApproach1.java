package pl.databucket.examples;

import pl.databucket.client.*;
import pl.databucket.examples.approach1.user.*;
import pl.databucket.examples.base.SampleUserEyeColor;
import pl.databucket.examples.base.SampleUserRules;
import pl.databucket.examples.base.SampleUserTag;

import java.util.ArrayList;
import java.util.List;

public class ExampleApproach1 {

    // This should be initiated once
    Databucket databucket = new Databucket(Config.SERVER_URL, Config.USER_NAME, Config.PASSWORD, Config.PROJECT_ID, Config.DEBUG_LOG);
    XBucketUsers bucketUsers = new XBucketUsers(databucket, "int-users");

    public static void main(String[] args) {
        ExampleApproach1 userExamples = new ExampleApproach1();
        userExamples.run();
    }

    private void run() {
        // insert a new data.user
//        XUser user = new XUser();
//        user.setTag(SampleUserTag.GOOD);
//        user.setReserved(true);
//        user.setEyeColor(SampleUserEyeColor.GREEN);
//        user.setEmail("jakismail@test.io");
//        user = bucketUsers.insertUser(user);
//        System.out.println("userId: " + user.getId());

        // insert a new multi data.user
//        XUser user1 = new XUser();
//        user1.setReserved(true);
//        user1.setTag(SampleUserTag.GOOD);
//        user1.setEyeColor(SampleUserEyeColor.BLUE);
//        user1.setEmail("samplemail@test.io");
//
//        XUser user2 = new XUser();
//        user2.setReserved(false);
//        user2.setTag(SampleUserTag.ANALYSIS);
//        user2.setEyeColor(SampleUserEyeColor.BLUE);
//        user2.setEmail("samplemail@test.io");
//
//        List<XUser> userList = new ArrayList<>();
//        userList.add(user1);
//        userList.add(user2);
//        bucketUsers.insertMultiUser(userList);

        // modify data.user
//        XUser user = new XUser();
//        user.setTag(SampleUserTag.TRASH);
//        user.setReserved(false);
//        user = bucketUsers.updateUser(user);

        // rules def
//        Rules nestedRules = new Rules(LogicalOperator.or);
//        nestedRules.addRule(XUser.EMAIL, Operator.like, "%email%");
//        nestedRules.addRule("id", Operator.graterEqual, 0);
//
//        Rules rules = new Rules();
//        rules.addRule(SampleUserRules.goodUser());
//        rules.addRule(XUser.EYE_COLOR, Operator.equal, SampleUserEyeColor.BLUE);
//        rules.addRule(XUser.NUMBER, Operator.notEqual, XUserNumber.ONE);
//        rules.addNestedRules(nestedRules);
//
//        List<String> fields = new ArrayList<>();
//        fields.add(Field.ID);
//        fields.add(XUser.EMAIL);
//        fields.add(XUser.EYE_COLOR);
//
//        // get data.user by rules
//        XUser user = bucketUsers.getUser(rules, fields);
//
//        // reserve data.user by rules
//        user = bucketUsers.reserveUser(rules);

    }

}
