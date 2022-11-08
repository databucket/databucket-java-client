package pl.databucket.examples.base;

public enum SampleUserTag {
    GOOD(1),
    TRASH(2),
    DELETED(3),
    SCHEDULED(4),
    ANALYSIS(5);

    private final int tagId;

    SampleUserTag(final int newValue) {
        tagId = newValue;
    }

    public int id() {
        return tagId;
    }

    public static SampleUserTag valueOf(int tagId) {
        for (SampleUserTag tag : SampleUserTag.values()) {
            if (tag.id() == tagId) {
                return tag;
            }
        }
        return null;
    }
}
