package pl.databucket.client;

import java.util.List;
import java.util.Optional;

/**
 * A reservation of free data rows; create it with {@link Bucket#reserve(Rules)}. Rows taken at the same moment by
 * another client are skipped, so parallel tests never get the same row.
 * <pre>{@code
 * Optional<DataRow<User>> user = users.reserve(rules).one();
 * Reservation<User> r = users.reserve(rules).limit(3).sort(Sort.RANDOM).fetch();
 * Reservation<User> quick = users.reserve(rules).limit(3).withoutCount().fetch();
 * long free = users.reserve(rules).available();
 * }</pre>
 * Counting the free rows that still match ({@link Reservation#getAvailable()}) reads all of them: with many matching
 * rows it is most of the time of a reservation. {@link #one()} never counts, {@link #withoutCount()} skips it for
 * {@link #fetch()} (Databucket 3.16.3+; older servers always count).
 */
public final class Reserve<T> {

    private final Bucket<T> bucket;
    private final Rules rules;
    private int limit = 1;
    private String sort = "id";
    private String forUser;
    private boolean count = true;

    Reserve(Bucket<T> bucket, Rules rules) {
        this.bucket = bucket;
        this.rules = rules;
    }

    /** Number of rows to reserve (default 1). */
    public Reserve<T> limit(int limit) {
        if (limit < 1)
            throw new IllegalArgumentException("The limit must be at least 1; use available() to only count free rows");
        this.limit = limit;
        return this;
    }

    /** Which free rows are taken first, see {@link Sort} (default: by id). */
    public Reserve<T> sort(String sort) {
        this.sort = sort;
        return this;
    }

    /** Reserve for another user (ADMIN only; ignored for other roles). */
    public Reserve<T> forUser(String username) {
        this.forUser = username;
        return this;
    }

    /**
     * Reserve without counting the free rows still matching ({@link Reservation#getAvailable()} is then -1): faster
     * when many rows match.
     */
    public Reserve<T> withoutCount() {
        this.count = false;
        return this;
    }

    public Reservation<T> fetch() {
        return bucket.reserve(rules, limit, sort, forUser, count);
    }

    /** Reserves one row; empty when no free row matches. Doesn't count the free rows (not needed here). */
    public Optional<DataRow<T>> one() {
        List<DataRow<T>> rows = bucket.reserve(rules, 1, sort, forUser, false).getRows();
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /** Number of free rows matching the rules; nothing is reserved. */
    public long available() {
        return bucket.reserve(rules, 0, sort, forUser, true).getAvailable();
    }
}
