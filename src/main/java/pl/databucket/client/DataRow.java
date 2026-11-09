package pl.databucket.client;

import java.time.Instant;

/**
 * One data row of a bucket.
 *
 * @param <T> type of the properties (e.g. {@code Map<String, Object>}, {@code JsonNode} or your own class)
 */
public final class DataRow<T> {

    private final long id;
    private final Long tagId;
    private final boolean reserved;
    private final String owner;
    private final T properties;
    private final String createdBy;
    private final Instant createdAt;
    private final String modifiedBy;
    private final Instant modifiedAt;

    DataRow(long id, Long tagId, boolean reserved, String owner, T properties,
            String createdBy, Instant createdAt, String modifiedBy, Instant modifiedAt) {
        this.id = id;
        this.tagId = tagId;
        this.reserved = reserved;
        this.owner = owner;
        this.properties = properties;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.modifiedBy = modifiedBy;
        this.modifiedAt = modifiedAt;
    }

    public long getId() {
        return id;
    }

    /** Tag id, or null when the row has no tag. */
    public Long getTagId() {
        return tagId;
    }

    public boolean isReserved() {
        return reserved;
    }

    /** User who reserved the row, or null. */
    public String getOwner() {
        return owner;
    }

    public T getProperties() {
        return properties;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    @Override
    public String toString() {
        return "DataRow{id=" + id + ", tagId=" + tagId + ", reserved=" + reserved + ", owner=" + owner
                + ", properties=" + properties + "}";
    }
}
