package pl.databucket.client;

import java.time.Instant;

/**
 * One change of a data row (bucket with history enabled). Only the items that changed are set.
 *
 * @param <T> type of the properties
 */
public final class HistoryEntry<T> {

    private final long id;
    private final long dataId;
    private final Long tagId;
    private final Boolean reserved;
    private final boolean propertiesChanged;
    private final T properties;
    private final Instant modifiedAt;
    private final String modifiedBy;

    HistoryEntry(long id, long dataId, Long tagId, Boolean reserved, boolean propertiesChanged, T properties,
                 Instant modifiedAt, String modifiedBy) {
        this.id = id;
        this.dataId = dataId;
        this.tagId = tagId;
        this.reserved = reserved;
        this.propertiesChanged = propertiesChanged;
        this.properties = properties;
        this.modifiedAt = modifiedAt;
        this.modifiedBy = modifiedBy;
    }

    /** Id of the history entry (use it with {@link Bucket#historyProperties(long, long...)}). */
    public long getId() {
        return id;
    }

    /** Id of the data row. */
    public long getDataId() {
        return dataId;
    }

    /** The new tag, or null when the tag did not change. */
    public Long getTagId() {
        return tagId;
    }

    /** The new reservation state, or null when it did not change. */
    public Boolean getReserved() {
        return reserved;
    }

    /** Whether the properties changed. */
    public boolean isPropertiesChanged() {
        return propertiesChanged;
    }

    /**
     * The properties of this version: only from {@link HistorySearch#withProperties()} and when they changed;
     * otherwise null (see {@link Bucket#historyProperties(long, long...)}).
     */
    public T getProperties() {
        return properties;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    @Override
    public String toString() {
        return "HistoryEntry{id=" + id + ", dataId=" + dataId + ", tagId=" + tagId + ", reserved=" + reserved
                + ", propertiesChanged=" + propertiesChanged + ", modifiedAt=" + modifiedAt + ", modifiedBy=" + modifiedBy + "}";
    }
}
