package pl.databucket.examples.base;

import pl.databucket.client.PropertyEnum;

public enum SampleUserEyeColor implements PropertyEnum {
    BLUE("blue"),
    BLACK("black"),
    GREEN("green"),
    GREY("grey");

    private final String colorName;

    SampleUserEyeColor(final String newValue) {
        colorName = newValue;
    }

    @Override
    public String getValue() {
        return colorName;
    }
}
