package pl.databucket.examples.approach1.user;

import pl.databucket.client.PropertyEnum;

public enum XUserNumber implements PropertyEnum {
    ONE(1),
    TWO(2),
    THREE(3);

    private final int number;

    XUserNumber(final int newValue) {
        number = newValue;
    }

    @Override
    public Object getValue() {
        return number;
    }
}
