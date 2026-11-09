package pl.databucket.client;

/**
 * Sort orders for {@link Query#sort(String)} and {@link Reserve#sort(String)}: a field or a property.
 * <pre>{@code
 * Sort.asc("$.name"), Sort.desc(Field.ID), Sort.RANDOM
 * }</pre>
 */
public final class Sort {

    /** Random order, e.g. to reserve a random row. */
    public static final String RANDOM = "random";

    private Sort() {
    }

    public static String asc(String fieldOrProperty) {
        return "asc(" + fieldOrProperty + ")";
    }

    public static String desc(String fieldOrProperty) {
        return "desc(" + fieldOrProperty + ")";
    }

    public static String asc(Field field) {
        return asc(field.apiName());
    }

    public static String desc(Field field) {
        return desc(field.apiName());
    }
}
