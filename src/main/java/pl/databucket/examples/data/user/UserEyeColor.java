package pl.databucket.examples.data.user;

public enum UserEyeColor {
    BLUE("blue"),
    BLACK("black"),
    GREEN("green"),
    GREY("grey");

    private final String colorName;

    UserEyeColor(final String newValue) {
        colorName = newValue;
    }
}
