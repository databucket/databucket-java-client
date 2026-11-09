package pl.databucket.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A condition on data rows, built in Java and sent to Databucket as JsonLogic (API version 2).
 * <pre>{@code
 * Rule.property("$.address.city").eq("Paris")
 * Rule.tagId().in(1, 2)
 * Rule.or(Rule.owner().isNull(), Rule.owner().eq(Rule.CURRENT_USER))
 * Rule.property("$.orders").length().gt(2)
 * }</pre>
 */
public final class Rule {

    /** As a value: the signed-in user, e.g. {@code Rule.owner().eq(Rule.CURRENT_USER)}. */
    public static final String CURRENT_USER = "@currentUser";

    private final Object logic;

    private Rule(Object logic) {
        this.logic = logic;
    }

    /** The JsonLogic expression of this rule. */
    public Object toLogic() {
        return logic;
    }

    /** A property of the data, e.g. {@code $.address.city} (the {@code $.} prefix may be left out). */
    public static Operand property(String path) {
        if (path == null || path.isBlank())
            throw new IllegalArgumentException("The property path is empty");
        return new Operand(path.startsWith("$") ? path : "$." + path, true);
    }

    public static Operand field(Field field) {
        return new Operand(field.apiName(), false);
    }

    public static Operand id() {
        return field(Field.ID);
    }

    public static Operand tagId() {
        return field(Field.TAG_ID);
    }

    public static Operand owner() {
        return field(Field.OWNER);
    }

    public static Operand reserved() {
        return field(Field.RESERVED);
    }

    public static Rule and(Rule... rules) {
        return group("and", Arrays.asList(rules));
    }

    public static Rule and(Collection<Rule> rules) {
        return group("and", rules);
    }

    public static Rule or(Rule... rules) {
        return group("or", Arrays.asList(rules));
    }

    public static Rule or(Collection<Rule> rules) {
        return group("or", rules);
    }

    public static Rule not(Rule rule) {
        return new Rule(Collections.singletonMap("!", rule.logic));
    }

    /** Any JsonLogic expression, e.g. copied from the rich filter. */
    public static Rule logic(Map<String, ?> jsonLogic) {
        return new Rule(jsonLogic);
    }

    private static Rule group(String operator, Collection<Rule> rules) {
        List<Object> items = new ArrayList<>();
        for (Rule rule : rules)
            items.add(rule.logic);
        return new Rule(Collections.singletonMap(operator, items));
    }

    @Override
    public String toString() {
        return String.valueOf(logic);
    }

    /** The left side of a rule: a field, a property or a property function. */
    public static final class Operand {

        private final String name;
        private final boolean property;

        private Operand(String name, boolean property) {
            this.name = name;
            this.property = property;
        }

        private Map<String, Object> var() {
            return Collections.singletonMap("var", name);
        }

        private Rule op(String operator, Object... args) {
            List<Object> list = new ArrayList<>();
            list.add(var());
            list.addAll(Arrays.asList(args));
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(operator, list);
            return new Rule(map);
        }

        public Rule eq(Object value) {
            return op("==", value);
        }

        public Rule ne(Object value) {
            return op("!=", value);
        }

        public Rule gt(Object value) {
            return op(">", value);
        }

        public Rule ge(Object value) {
            return op(">=", value);
        }

        public Rule lt(Object value) {
            return op("<", value);
        }

        public Rule le(Object value) {
            return op("<=", value);
        }

        /** from &lt;= value &lt;= to */
        public Rule between(Object from, Object to) {
            List<Object> list = new ArrayList<>();
            list.add(from);
            list.add(var());
            list.add(to);
            return new Rule(Collections.singletonMap("<=", list));
        }

        public Rule in(Object... values) {
            return op("in", Arrays.asList(values));
        }

        public Rule in(Collection<?> values) {
            return op("in", new ArrayList<>(values));
        }

        public Rule notIn(Object... values) {
            return op("!in", Arrays.asList(values));
        }

        public Rule notIn(Collection<?> values) {
            return op("!in", new ArrayList<>(values));
        }

        /** SQL LIKE pattern: {@code %} any text, {@code _} one character. */
        public Rule like(String pattern) {
            return op("like", pattern);
        }

        public Rule notLike(String pattern) {
            return op("!like", pattern);
        }

        /** POSIX regular expression, case sensitive. */
        public Rule matches(String regex) {
            return op("~", regex);
        }

        /** POSIX regular expression, case insensitive. */
        public Rule matchesIgnoreCase(String regex) {
            return op("~*", regex);
        }

        public Rule notMatches(String regex) {
            return op("!~", regex);
        }

        public Rule isNull() {
            return op("is", (Object) null);
        }

        public Rule isNotNull() {
            return op("is not", (Object) null);
        }

        /** No value: null, missing or (for a property) an empty text. */
        public Rule isEmpty() {
            return new Rule(Collections.singletonMap("!", var()));
        }

        public Rule isNotEmpty() {
            return new Rule(Collections.singletonMap("!!", var()));
        }

        /** The property is present in the data (also when its value is null). */
        public Rule exists() {
            return function("exists()").eq(true);
        }

        public Rule notExists() {
            return function("notExists()").eq(true);
        }

        /** Number of items of an array property (0 when it is not an array). */
        public Operand length() {
            return function("length()");
        }

        private Operand function(String function) {
            if (!property)
                throw new IllegalStateException(function + " works only on a property, not on the field " + name);
            return new Operand(name + "." + function, false);
        }
    }
}
