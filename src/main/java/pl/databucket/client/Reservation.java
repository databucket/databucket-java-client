package pl.databucket.client;

import java.util.List;

/**
 * The result of a reservation: the rows reserved by this call and how many free rows still match the rules.
 */
public final class Reservation<T> {

    private final List<DataRow<T>> rows;
    private final long available;

    Reservation(List<DataRow<T>> rows, long available) {
        this.rows = rows;
        this.available = available;
    }

    /** The reserved rows (empty when nothing was free). */
    public List<DataRow<T>> getRows() {
        return rows;
    }

    /** Free rows still matching the rules after this reservation. */
    public long getAvailable() {
        return available;
    }

    @Override
    public String toString() {
        return "Reservation{reserved=" + rows.size() + ", available=" + available + "}";
    }
}
