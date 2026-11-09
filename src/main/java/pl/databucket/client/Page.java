package pl.databucket.client;

import java.util.List;

/**
 * One page of data rows matching the rules.
 */
public final class Page<T> {

    private final List<DataRow<T>> rows;
    private final int page;
    private final int limit;
    private final long total;
    private final int totalPages;

    Page(List<DataRow<T>> rows, int page, int limit, long total, int totalPages) {
        this.rows = rows;
        this.page = page;
        this.limit = limit;
        this.total = total;
        this.totalPages = totalPages;
    }

    /** The rows of this page (empty when nothing matches). */
    public List<DataRow<T>> getRows() {
        return rows;
    }

    /** Page number, starting from 1. */
    public int getPage() {
        return page;
    }

    public int getLimit() {
        return limit;
    }

    /** Number of all rows matching the rules. */
    public long getTotal() {
        return total;
    }

    public int getTotalPages() {
        return totalPages;
    }

    @Override
    public String toString() {
        return "Page{page=" + page + "/" + totalPages + ", total=" + total + ", rows=" + rows.size() + "}";
    }
}
