package pl.databucket.client;

import java.time.Instant;

/**
 * One change of a data row (bucket with history enabled). Only the items that changed are set.
 */
public final class HistoryEntry {

    private final long id;
    private final Long tagId;
    private final Boolean reserved;
    private final boolean propertiesChanged;
    private final Instant modifiedAt;
    private final String modifiedBy;

    HistoryEntry(long id, Long tagId, Boolean reserved, boolean propertiesChanged, Instant modifiedAt, String modifiedBy) {
        this.id = id;
        this.tagId = tagId;
        this.reserved = reserved;
        this.propertiesChanged = propertiesChanged;
        this.modifiedAt = modifiedAt;
        this.modifiedBy = modifiedBy;
    }

    /** Id of the history entry (use it with {@link Bucket#historyProperties(long, long...)}). */
    public long getId() {
        return id;
    }

    /** The new tag, or null when the tag did not change. */
    public Long getTagId() {
        return tagId;
    }

    /** The new reservation state, or null when it did not change. */
    public Boolean getReserved() {
        return reserved;
    }

    /** Whether the properties changed (get them with {@link Bucket#historyProperties(long, long...)}). */
    public boolean isPropertiesChanged() {
        return propertiesChanged;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    @Override
    public String toString() {
        return "HistoryEntry{id=" + id + ", tagId=" + tagId + ", reserved=" + reserved
                + ", propertiesChanged=" + propertiesChanged + ", modifiedAt=" + modifiedAt + ", modifiedBy=" + modifiedBy + "}";
    }
}
