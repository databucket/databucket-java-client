package pl.databucket.client;

import java.util.List;

/**
 * One page of history entries.
 */
public final class HistoryPage<T> {

    private final List<HistoryEntry<T>> entries;
    private final int page;
    private final int limit;
    private final long total;
    private final int totalPages;

    HistoryPage(List<HistoryEntry<T>> entries, int page, int limit, long total, int totalPages) {
        this.entries = entries;
        this.page = page;
        this.limit = limit;
        this.total = total;
        this.totalPages = totalPages;
    }

    public List<HistoryEntry<T>> getEntries() {
        return entries;
    }

    public int getPage() {
        return page;
    }

    public int getLimit() {
        return limit;
    }

    /** Number of all matching history entries. */
    public long getTotal() {
        return total;
    }

    public int getTotalPages() {
        return totalPages;
    }

    @Override
    public String toString() {
        return "HistoryPage{page=" + page + "/" + totalPages + ", total=" + total + ", entries=" + entries.size() + "}";
    }
}
