package pl.databucket.client;

/**
 * A data row to insert: properties plus, optionally, a tag and the reservation.
 * <pre>{@code
 * bucket.insert(NewData.of(user).tagId(3).reserved(true));
 * }</pre>
 */
public final class NewData<T> {

    private final T properties;
    private Long tagId;
    private Boolean reserved;

    private NewData(T properties) {
        this.properties = properties;
    }

    public static <T> NewData<T> of(T properties) {
        return new NewData<>(properties);
    }

    public NewData<T> tagId(Long tagId) {
        this.tagId = tagId;
        return this;
    }

    /** true reserves the row for the signed-in user. */
    public NewData<T> reserved(boolean reserved) {
        this.reserved = reserved;
        return this;
    }

    T getProperties() {
        return properties;
    }

    Long getTagId() {
        return tagId;
    }

    Boolean getReserved() {
        return reserved;
    }
}
