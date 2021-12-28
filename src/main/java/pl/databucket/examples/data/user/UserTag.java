package pl.databucket.examples.data.user;

public enum UserTag {
    GOOD(1),
    TRASH(2),
    DELETED(3),
    SCHEDULED(4),
    ANALYSIS(5);

    private final int tagId;

    UserTag(final int newValue) {
        tagId = newValue;
    }

    public int id() {
        return tagId;
    }

    public static UserTag valueOf(int tagId) {
        for (UserTag tag : UserTag.values()) {
            if (tag.id() == tagId) {
                return tag;
            }
        }
        return null;
    }
}
