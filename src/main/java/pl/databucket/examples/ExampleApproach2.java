package pl.databucket.examples;

import pl.databucket.client.*;
import pl.databucket.examples.base.SampleUserEyeColor;
import pl.databucket.examples.approach2.user.YBucketUsers;
import pl.databucket.examples.approach2.user.YUserContact;
import pl.databucket.examples.approach2.user.YUserProperties;
import pl.databucket.examples.approach2.user.YUser;
import pl.databucket.examples.base.SampleUserTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExampleApproach2 {

    // This should be initiated once
    Databucket databucket = new Databucket(Config.SERVER_URL, Config.USER_NAME, Config.PASSWORD, Config.PROJECT_ID, Config.DEBUG_LOG);
    YBucketUsers bucketUsers = new YBucketUsers(databucket, "int-users");

    public static void main(String[] args) {
        ExampleApproach2 userExamples = new ExampleApproach2();
        userExamples.run();
    }

    private void run() {
        // insert a new data.user
        YUser user = bucketUsers.insertUser(YUser.builder()
                .tagId(SampleUserTag.GOOD.id())
                .reserved(false)
                .properties(YUserProperties.builder()
                        .eyeColor(SampleUserEyeColor.GREEN.getValue())
                        .number(101)
                        .contact(YUserContact.builder()
                                .email("test@test.io")
                                .phone("111222333")
                                .build())
                        .build())
                .build());
        System.out.println("userId: " + user.getId());


         //insert a new multi user
//        YUser user1 = YUser.builder()
//                .tagId(SampleUserTag.GOOD.id())
//                .reserved(true)
//                .properties(YUserProperties.builder()
//                        .eyeColor(SampleUserEyeColor.GREEN.getValue())
//                        .number(101)
//                        .contact(YUserContact.builder()
//                                .email("test@test.io")
//                                .phone("111222333")
//                                .build())
//                        .build())
//                .build();
//
//        YUser user2 = YUser.builder()
//                .tagId(SampleUserTag.ANALYSIS.id())
//                .reserved(false)
//                .properties(YUserProperties.builder()
//                        .eyeColor(SampleUserEyeColor.BLUE.getValue())
//                        .number(101)
//                        .contact(YUserContact.builder()
//                                .email("test@test.io")
//                                .phone("111222333")
//                                .build())
//                        .build())
//                .build();
//
//        List<YUser> userList = new ArrayList<>();
//        userList.add(user1);
//        userList.add(user2);
//
//        bucketUsers.insertMultiUser(userList);

        // modify data.user
        user = bucketUsers.updateUser(user.toBuilder()
                .tagId(SampleUserTag.TRASH.id())
                        .properties(YUserProperties.builder()
                                .eyeColor(SampleUserEyeColor.GREY.getValue())
                                .build())
                .reserved(false)
                .build());

        // rules def
        Rules rules = new Rules();
        rules.addRule("$.email", Operator.like, "%email%");
        rules.addRule("id", Operator.graterEqual, 0);

        List<CustomDataDef> customDataDefList = new ArrayList<>();
        customDataDefList.add(new CustomDataDef("id", "id"));
        customDataDefList.add(new CustomDataDef("name", "$.name"));

        // get data.user by rules
        Map<String, Object> map = bucketUsers.getUser(rules, customDataDefList);

//        // reserve data.user by rules
//        YUser user = bucketUsers.reserveUser(rules);
//        System.out.println(user != null);
    }

}
