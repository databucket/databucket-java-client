package pl.databucket.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Changes to apply to data rows; items left out stay unchanged.
 * <pre>{@code
 * Change.create().set("$.status", "used").remove("$.tmp").tagId(4L)
 * Change.create().reserved(false)       // release
 * Change.create().properties(newUser)   // replace all properties
 * }</pre>
 */
public final class Change {

    private Long tagId;
    private Boolean reserved;
    private Object properties;
    private final Map<String, Object> propertiesToSet = new LinkedHashMap<>();
    private final List<String> propertiesToRemove = new ArrayList<>();

    private Change() {
    }

    public static Change create() {
        return new Change();
    }

    public Change tagId(Long tagId) {
        this.tagId = tagId;
        return this;
    }

    /**
     * true reserves the rows for the signed-in user (only rows that are not reserved yet are changed),
     * false releases them.
     */
    public Change reserved(boolean reserved) {
        this.reserved = reserved;
        return this;
    }

    /** Replaces all properties. */
    public Change properties(Object properties) {
        this.properties = properties;
        return this;
    }

    /** Sets one property, e.g. {@code set("$.address.city", "Paris")} (the {@code $.} prefix may be left out). */
    public Change set(String path, Object value) {
        propertiesToSet.put(path(path), value);
        return this;
    }

    /** Removes one property, e.g. {@code remove("$.tmp")}. */
    public Change remove(String path) {
        propertiesToRemove.add(path(path));
        return this;
    }

    private static String path(String path) {
        if (path == null || path.isBlank())
            throw new IllegalArgumentException("The property path is empty");
        return path.startsWith("$") ? path : "$." + path;
    }

    Map<String, Object> toBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        if (tagId != null)
            body.put("tagId", tagId);
        if (reserved != null)
            body.put("reserved", reserved);
        if (properties != null)
            body.put("properties", properties);
        if (!propertiesToSet.isEmpty())
            body.put("propertiesToSet", propertiesToSet);
        if (!propertiesToRemove.isEmpty())
            body.put("propertiesToRemove", propertiesToRemove);
        return body;
    }
}
