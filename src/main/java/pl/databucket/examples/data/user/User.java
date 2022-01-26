package pl.databucket.examples.data.user;

import pl.databucket.client.Data;

public class User extends Data {

    // User properties
    public static final String EYE_COLOR = "$.eyeColor";
    public static final String EMAIL = "$.contact.email";
    public static final String NUMBER = "$.number";

    public User() {
        super(new Data());
    }

    public User(Data data) {
        super(data);
    }

    public UserTag getTag() {
        return UserTag.valueOf(getTagId());
    }

    public void setTag(UserTag tag) {
        setTagId(tag.id());
    }

    public UserEyeColor getEyeColor() {
        return UserEyeColor.valueOf((String) getProperty(EYE_COLOR));
    }

    public void setEyeColor(UserEyeColor eyeColor) {
        setProperty(EYE_COLOR, eyeColor.name());
    }

    public String getEmail() {
        return (String) getProperty(EMAIL);
    }

    public void setEmail(String email) {
        setProperty(EMAIL, email);
    }

    public Integer getNumber() {
        return (Integer) getProperty(NUMBER);
    }

    public void setNumber(Integer number) {
        setProperty(NUMBER, number);
    }

}
