package pl.databucket.examples.data.user;

import pl.databucket.client.PropertyEnum;

public enum UserEyeColor implements PropertyEnum {
    BLUE("blue"),
    BLACK("black"),
    GREEN("green"),
    GREY("grey");

    private final String colorName;

    UserEyeColor(final String newValue) {
        colorName = newValue;
    }

    @Override
    public Object getValue() {
        return colorName;
    }
}
