package pl.databucket.examples.data.user;

import pl.databucket.client.Data;

public class SampleUser extends Data {

    // User properties
    public static final String EYE_COLOR = "$.eyeColor";
    public static final String EMAIL = "$.contact.email";
    public static final String NUMBER = "$.number";

    public SampleUser() {
        super(new Data());
    }

    public SampleUser(Data data) {
        super(data);
    }

    public SampleUserTag getTag() {
        return SampleUserTag.valueOf(getTagId());
    }

    public void setTag(SampleUserTag tag) {
        setTagId(tag.id());
    }

    public SampleUserEyeColor getEyeColor() {
        return SampleUserEyeColor.valueOf((String) getProperty(EYE_COLOR));
    }

    public void setEyeColor(SampleUserEyeColor eyeColor) {
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
