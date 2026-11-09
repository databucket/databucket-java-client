package pl.databucket.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A search of the history of a bucket; create it with {@link Bucket#searchHistory(Rules)} (Databucket 3.14.0+).
 * <pre>{@code
 * // changes made by the nightly robot since yesterday, on the rows that are now in GB, newest first
 * HistoryPage<User> page = users.searchHistory(Rules.where(Rule.property("$.address.country").eq("GB")))
 *         .where(Rule.field(HistoryField.MODIFIED_BY).eq("nightly-robot"),
 *                Rule.field(HistoryField.MODIFIED_AT).ge(Instant.now().minus(1, ChronoUnit.DAYS)))
 *         .sort(Sort.desc(HistoryField.MODIFIED_AT))
 *         .limit(50)
 *         .fetch();
 * }</pre>
 */
public final class HistorySearch<T> {

    private final Bucket<T> bucket;
    private final Rules rows;
    private final List<Rule> entries = new ArrayList<>();
    private int page = 1;
    private int limit = 20;
    private String sort = "id";
    private boolean properties;

    HistorySearch(Bucket<T> bucket, Rules rows) {
        this.bucket = bucket;
        this.rows = rows;
    }

    /**
     * Which history entries (all must match): rules on {@link HistoryField}s, e.g.
     * {@code Rule.field(HistoryField.MODIFIED_BY).eq("john")}. The properties of the versions can't be searched.
     */
    public HistorySearch<T> where(Rule... rules) {
        entries.addAll(Arrays.asList(rules));
        return this;
    }

    /** Page number, starting from 1 (default 1). */
    public HistorySearch<T> page(int page) {
        if (page < 1)
            throw new IllegalArgumentException("The page starts from 1");
        this.page = page;
        return this;
    }

    /** Entries per page, 1..1000 (default 20). */
    public HistorySearch<T> limit(int limit) {
        if (limit < 1 || limit > 1000)
            throw new IllegalArgumentException("The limit is 1..1000; use count() to only count entries");
        this.limit = limit;
        return this;
    }

    /** Sort order by a history field, e.g. {@code Sort.desc(HistoryField.MODIFIED_AT)} (default: by id). */
    public HistorySearch<T> sort(String sort) {
        this.sort = sort;
        return this;
    }

    /** Also the properties of each version (when they changed). */
    public HistorySearch<T> withProperties() {
        this.properties = true;
        return this;
    }

    public HistoryPage<T> fetch() {
        return bucket.searchHistory(rows, Rules.where(entries), page, limit, sort, properties);
    }

    /** The entries of the page. */
    public List<HistoryEntry<T>> list() {
        return fetch().getEntries();
    }

    /** Number of matching history entries. */
    public long count() {
        return bucket.searchHistory(rows, Rules.where(entries), 1, 0, sort, false).getTotal();
    }
}
