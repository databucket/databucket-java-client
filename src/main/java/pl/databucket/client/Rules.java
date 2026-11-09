package pl.databucket.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Which data rows a call works on.
 * <pre>{@code
 * Rules.where(Rule.property("$.status").eq("active"), Rule.reserved().eq(false))  // all must match
 * Rules.ALL                                                                      // every row
 * Rules.fromRules(List.of(List.of("$.status", "==", "active")))                  // the API "rules" format
 * Rules.fromLogic(jsonLogic)                                                     // JsonLogic, e.g. from the rich filter
 * }</pre>
 */
public final class Rules {

    /** No conditions: every row (not accepted when modifying or removing data). */
    public static final Rules ALL = new Rules(null, null);

    private final Object logic;
    private final List<?> rules;

    private Rules(Object logic, List<?> rules) {
        this.logic = logic;
        this.rules = rules;
    }

    /** Rows matching all the rules. */
    public static Rules where(Rule... rules) {
        return where(Arrays.asList(rules));
    }

    /** Rows matching all the rules. */
    public static Rules where(Collection<Rule> rules) {
        if (rules.isEmpty())
            return ALL;
        if (rules.size() == 1)
            return new Rules(rules.iterator().next().toLogic(), null);
        return new Rules(Rule.and(rules).toLogic(), null);
    }

    /**
     * Rules in the API "rules" format, e.g. copied from the "API request" tab of the rich filter:
     * {@code [["$.city", "==", "Paris"], {"or": [["tagId", "in", [1, 2]], ["$.age", ">", 30]]}]}.
     * <p>
     * Note: in this format a value that starts with {@code $} or equals a field name (e.g. "owner") is read as
     * a property or field, not as text. {@link #where(Rule...)} has no such ambiguity.
     */
    public static Rules fromRules(List<?> rules) {
        return new Rules(null, new ArrayList<>(rules));
    }

    /** A JsonLogic expression, e.g. {@code {"==": [{"var": "$.city"}, "Paris"]}}. */
    public static Rules fromLogic(Map<String, ?> logic) {
        return new Rules(logic, null);
    }

    boolean isEmpty() {
        return logic == null && (rules == null || rules.isEmpty());
    }

    /** The request body items: {@code logic} or {@code rules}. */
    Map<String, Object> toBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        if (logic != null)
            body.put("logic", logic);
        else if (rules != null)
            body.put("rules", rules);
        return body;
    }

    @Override
    public String toString() {
        return isEmpty() ? "ALL" : toBody().toString();
    }
}
