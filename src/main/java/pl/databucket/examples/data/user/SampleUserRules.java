package pl.databucket.examples.data.user;

import pl.databucket.client.Field;
import pl.databucket.client.Operator;
import pl.databucket.client.Rule;

public class SampleUserRules {

    public static Rule goodUser() {
        return new Rule(Field.TAG_ID, Operator.equal, SampleUserTag.GOOD.id());
    }

}
