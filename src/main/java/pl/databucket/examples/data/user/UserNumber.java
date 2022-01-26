package pl.databucket.examples.data.user;

import pl.databucket.client.PropertyEnum;

public enum UserNumber implements PropertyEnum {
    ONE(1),
    TWO(2),
    THREE(3);

    private final int number;

    UserNumber(final int newValue) {
        number = newValue;
    }

    @Override
    public Object getValue() {
        return number;
    }
}
