package pl.databucket.examples.data.user;

import pl.databucket.client.PropertyEnum;

public enum SampleUserNumber implements PropertyEnum {
    ONE(1),
    TWO(2),
    THREE(3);

    private final int number;

    SampleUserNumber(final int newValue) {
        number = newValue;
    }

    @Override
    public Object getValue() {
        return number;
    }
}
