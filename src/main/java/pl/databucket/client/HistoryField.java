package pl.databucket.client;

/**
 * Fields of a history entry, for {@link HistorySearch#where(Rule...)} and sorting the history.
 */
public enum HistoryField {
    /** Id of the history entry. */
    ID("id"),
    /** Id of the data row. */
    DATA_ID("dataId"),
    TAG_ID("tagId"),
    RESERVED("reserved"),
    MODIFIED_BY("modifiedBy"),
    MODIFIED_AT("modifiedAt");

    private final String name;

    HistoryField(String name) {
        this.name = name;
    }

    /** The name used by the API. */
    public String apiName() {
        return name;
    }
}
