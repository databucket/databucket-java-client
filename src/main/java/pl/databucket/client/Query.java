package pl.databucket.client;

import java.util.List;
import java.util.Optional;

/**
 * A search for data rows; create it with {@link Bucket#find(Rules)}.
 * <pre>{@code
 * Page<User> page = users.find(rules).sort(Sort.desc(Field.ID)).limit(20).page(2).fetch();
 * List<DataRow<User>> rows = users.find(rules).limit(100).list();
 * Optional<DataRow<User>> row = users.find(rules).first();
 * long count = users.find(rules).count();
 * }</pre>
 */
public final class Query<T> {

    private final Bucket<T> bucket;
    private final Rules rules;
    private int page = 1;
    private int limit = 1;
    private String sort = "id";

    Query(Bucket<T> bucket, Rules rules) {
        this.bucket = bucket;
        this.rules = rules;
    }

    /** Page number, starting from 1 (default 1). */
    public Query<T> page(int page) {
        if (page < 1)
            throw new IllegalArgumentException("The page starts from 1");
        this.page = page;
        return this;
    }

    /** Rows per page (default 1). */
    public Query<T> limit(int limit) {
        if (limit < 1)
            throw new IllegalArgumentException("The limit must be at least 1; use count() to only count rows");
        this.limit = limit;
        return this;
    }

    /** Sort order, see {@link Sort} (default: by id). */
    public Query<T> sort(String sort) {
        this.sort = sort;
        return this;
    }

    public Page<T> fetch() {
        return bucket.fetch(rules, page, limit, sort);
    }

    /** The rows of the page. */
    public List<DataRow<T>> list() {
        return fetch().getRows();
    }

    /** The first matching row. */
    public Optional<DataRow<T>> first() {
        List<DataRow<T>> rows = bucket.fetch(rules, 1, 1, sort).getRows();
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /** Number of matching rows (no rows are transferred). */
    public long count() {
        return bucket.count(rules);
    }
}
