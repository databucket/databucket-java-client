package pl.databucket.client;

/**
 * Fields of a data row that can be used in rules (besides properties such as {@code $.address.city}).
 */
public enum Field {
    ID("id"),
    TAG_ID("tagId"),
    OWNER("owner"),
    RESERVED("reserved"),
    CREATED_BY("createdBy"),
    CREATED_AT("createdAt"),
    MODIFIED_BY("modifiedBy"),
    MODIFIED_AT("modifiedAt");

    private final String name;

    Field(String name) {
        this.name = name;
    }

    /** The name used by the API. */
    public String apiName() {
        return name;
    }
}
