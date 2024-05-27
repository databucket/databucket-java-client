package pl.databucket.examples;

import pl.databucket.client.*;
import pl.databucket.examples.approach1.user.XBucketUsers;
import pl.databucket.examples.approach1.user.XUser;
import pl.databucket.examples.base.SampleUserEyeColor;
import pl.databucket.examples.base.SampleUserRules;

public class ExampleApproach3 {

    // This should be initiated once
    Databucket databucket = new Databucket(Config.SERVER_URL, Config.USER_NAME, Config.PASSWORD, Config.PROJECT_ID, Config.DEBUG_LOG);
    Bucket2 bucketUsers = new Bucket2(databucket, "int-users");

    public static void main(String[] args) {
        ExampleApproach3 userExamples = new ExampleApproach3();
        userExamples.run();
    }

    private void run() {
        Rules rules = new Rules();
        rules.addRule(SampleUserRules.goodUser());
        bucketUsers.getData(rules, true);
    }

}
