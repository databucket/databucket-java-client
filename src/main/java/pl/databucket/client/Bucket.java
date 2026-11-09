package pl.databucket.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * The data of one bucket. Get it with {@link Databucket#bucket(String, Class)}.
 *
 * @param <T> type of the properties
 */
public final class Bucket<T> {

    private static final Pattern NUMBER = Pattern.compile("\\d+");

    private final Databucket databucket;
    private final String name;
    private final Class<T> type;
    private final String path;

    Bucket(Databucket databucket, String name, Class<T> type) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("The bucket name is empty");
        this.databucket = databucket;
        this.name = name;
        this.type = type;
        this.path = "/api/bucket/" + URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");
    }

    public String getName() {
        return name;
    }

    // ---------------------------------------------------------------- insert

    /** Inserts a row and returns it with its id. */
    public DataRow<T> insert(T properties) {
        return insert(NewData.of(properties));
    }

    /** Inserts a row (with a tag or reserved) and returns it with its id. */
    public DataRow<T> insert(NewData<T> data) {
        return row(databucket.call("POST", path, newDataBody(data)));
    }

    /** Inserts many rows in one call (the rows are not returned; find them by rules). */
    public void insertAll(Collection<T> properties) {
        insertAllData(properties.stream().map(NewData::of).collect(Collectors.toList()));
    }

    /** Inserts many rows in one call (the rows are not returned; find them by rules). */
    public void insertAllData(Collection<NewData<T>> data) {
        if (data.isEmpty())
            return;
        List<Map<String, Object>> body = data.stream().map(this::newDataBody).collect(Collectors.toList());
        databucket.call("POST", path + "/multi", body);
    }

    private Map<String, Object> newDataBody(NewData<T> data) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (data.getTagId() != null)
            body.put("tagId", data.getTagId());
        if (data.getReserved() != null)
            body.put("reserved", data.getReserved());
        body.put("properties", data.getProperties());
        return body;
    }

    // ---------------------------------------------------------------- read

    /** The row with the given id; empty when it doesn't exist (or is not visible to the user). */
    public Optional<DataRow<T>> get(long id) {
        List<DataRow<T>> rows = get(Collections.singletonList(id));
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /** The rows with the given ids; missing ids are skipped. */
    public List<DataRow<T>> get(Collection<Long> ids) {
        if (ids.isEmpty())
            return Collections.emptyList();
        // one existing row comes as an object, otherwise a list
        JsonNode json = databucket.call("GET", path + "/" + ids(ids), null);
        return json.isArray() ? rows(json) : Collections.singletonList(row(json));
    }

    /** A search for rows matching the rules; see {@link Query}. */
    public Query<T> find(Rules rules) {
        return new Query<>(this, rules);
    }

    /** Number of rows matching the rules. */
    public long count(Rules rules) {
        return databucket.call("POST", path + "/get?limit=0", rules.toBody()).path("total").asLong();
    }

    Page<T> fetch(Rules rules, int page, int limit, String sort) {
        JsonNode json = databucket.call("POST", path + "/get?page=" + page + "&limit=" + limit + "&sort=" + encode(sort),
                rules.toBody());
        return new Page<>(rows(json.path("data")), json.path("page").asInt(page), json.path("limit").asInt(limit),
                json.path("total").asLong(), json.path("totalPages").asInt());
    }

    // ---------------------------------------------------------------- reserve

    /** A reservation of free rows matching the rules; see {@link Reserve}. */
    public Reserve<T> reserve(Rules rules) {
        return new Reserve<>(this, rules);
    }

    Reservation<T> reserve(Rules rules, int limit, String sort, String forUser) {
        Map<String, Object> body = rules.toBody();
        if (forUser != null)
            body.put("targetOwnerUsername", forUser);
        JsonNode json = databucket.call("POST", path + "/reserve?limit=" + limit + "&sort=" + encode(sort), body);
        return new Reservation<>(rows(json.path("data")), json.path("available").asLong());
    }

    /** Releases the rows (reserved = false); returns the number of changed rows. */
    public int release(long... ids) {
        return update(Change.create().reserved(false), ids);
    }

    // ---------------------------------------------------------------- modify and remove

    /** Applies the change to the rows with the given ids; returns the number of changed rows. */
    public int update(Change change, long... ids) {
        return update(change, LongStream.of(ids).boxed().collect(Collectors.toList()));
    }

    /** Applies the change to the rows with the given ids; returns the number of changed rows. */
    public int update(Change change, Collection<Long> ids) {
        if (ids.isEmpty())
            return 0;
        return count(databucket.call("PUT", path + "/" + ids(ids), change.toBody()));
    }

    /** Applies the change to all rows matching the rules (at least one rule); returns the number of changed rows. */
    public int update(Rules rules, Change change) {
        requireRules(rules, "modify");
        Map<String, Object> body = change.toBody();
        body.putAll(rules.toBody());
        return count(databucket.call("PUT", path, body));
    }

    /** Removes the rows with the given ids together with their history; returns the number of removed rows. */
    public int delete(long... ids) {
        if (ids.length == 0)
            return 0;
        return count(databucket.call("DELETE", path + "/" + ids(LongStream.of(ids).boxed().collect(Collectors.toList())), null));
    }

    /** Removes all rows matching the rules (at least one rule) together with their history. */
    public int delete(Rules rules) {
        requireRules(rules, "remove");
        return count(databucket.call("DELETE", path, rules.toBody()));
    }

    // ---------------------------------------------------------------- history (MEMBER or higher)

    /** The changes of a row, oldest first (bucket with history enabled). */
    public List<HistoryEntry> history(long id) {
        JsonNode json = databucket.call("GET", path + "/" + id + "/history", null);
        List<HistoryEntry> entries = new ArrayList<>();
        for (JsonNode e : json)
            entries.add(new HistoryEntry(e.path("id").asLong(), longOrNull(e.get("tag_id")),
                    e.hasNonNull("reserved") ? e.get("reserved").asBoolean() : null,
                    e.path("properties").asBoolean(false), instant(e.get("modified_at")), e.path("modified_by").asText(null)));
        return entries;
    }

    /** The properties of a row as they were in the given history entries (by history entry id). */
    public Map<Long, T> historyProperties(long id, long... historyIds) {
        Map<Long, T> result = new LinkedHashMap<>();
        if (historyIds.length == 0)
            return result;
        JsonNode json = databucket.call("GET", path + "/" + id + "/history/"
                + ids(LongStream.of(historyIds).boxed().collect(Collectors.toList())), null);
        for (JsonNode e : json)
            result.put(e.path("id").asLong(), properties(e.get("properties")));
        return result;
    }

    /** Removes the history of a row (the row itself stays); returns the number of removed history entries. */
    public int clearHistory(long id) {
        return count(databucket.call("DELETE", path + "/" + id + "/history/clear", null));
    }

    /** Removes the history of all rows matching the rules (at least one rule); returns the number of rows. */
    public int clearHistory(Rules rules) {
        requireRules(rules, "clear the history of");
        return count(databucket.call("DELETE", path + "/history/clear", rules.toBody()));
    }

    // ---------------------------------------------------------------- mapping

    private List<DataRow<T>> rows(JsonNode array) {
        if (array == null || !array.isArray())
            return Collections.emptyList();
        List<DataRow<T>> rows = new ArrayList<>();
        for (JsonNode item : array)
            rows.add(row(item));
        return rows;
    }

    private DataRow<T> row(JsonNode json) {
        return new DataRow<>(json.path("id").asLong(), longOrNull(json.get("tagId")), json.path("reserved").asBoolean(),
                json.path("owner").asText(null), properties(json.get("properties")),
                json.path("createdBy").asText(null), instant(json.get("createdAt")),
                json.path("modifiedBy").asText(null), instant(json.get("modifiedAt")));
    }

    private T properties(JsonNode json) {
        if (json == null || json.isNull())
            return null;
        try {
            if (json.isTextual()) // stored as text by some endpoints
                json = databucket.mapper().readTree(json.asText());
            return databucket.mapper().treeToValue(json, type);
        } catch (JsonProcessingException e) {
            throw new DatabucketException(0, "Can not read the properties as " + type.getName() + ": " + e.getOriginalMessage(), json.toString());
        }
    }

    private static Long longOrNull(JsonNode json) {
        return json == null || json.isNull() ? null : json.asLong();
    }

    private static Instant instant(JsonNode json) {
        if (json == null || json.isNull())
            return null;
        if (json.isNumber())
            return Instant.ofEpochMilli(json.asLong());
        return Instant.parse(json.asText());
    }

    /** The number in a message such as "Modified 3 data row(s)". */
    private static int count(JsonNode json) {
        Matcher matcher = NUMBER.matcher(json.path("message").asText(""));
        return matcher.find() ? Integer.parseInt(matcher.group()) : 0;
    }

    private static String ids(Collection<Long> ids) {
        return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void requireRules(Rules rules, String action) {
        if (rules == null || rules.isEmpty())
            throw new IllegalArgumentException("Can not " + action + " data without rules (bucket " + name + ")");
    }

    @Override
    public String toString() {
        return "Bucket{" + name + "}";
    }
}
