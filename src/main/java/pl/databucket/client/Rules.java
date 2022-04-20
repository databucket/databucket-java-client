package pl.databucket.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rules {

    LogicalOperator logicalOperator = LogicalOperator.and;
    List<Object> rules = new ArrayList<>();

    public Rules() {
    }

    public Rules(List<Rule> rules) {
        this.rules.addAll(rules);
    }

    public Rules(Rule rule) {
        this.rules.add(rule);
    }

    public Rules(Object leftObject, Operator operator, Object rightObject) {
        this.rules.add(new Rule(leftObject, operator, rightObject));
    }

    public Rules(LogicalOperator logicalOperator) {
        this.logicalOperator = logicalOperator;
    }

    public void addRule(Object leftObject, Operator operator, Object rightObject) {
        this.rules.add(new Rule(leftObject, operator, rightObject));
    }

    public void addRule(Rule rule) {
        this.rules.add(rule);
    }

    public void addRules(List<Rule> rules) {
        this.rules.addAll(rules);
    }

    public void addNestedRules(Rules rules) {
        this.rules.add(rules);
    }

    public List<Object> getOwnRules() {
        return this.rules;
    }

    public LogicalOperator getOwnOperator() {
        return this.logicalOperator;
    }

    public List<Object> toNativeObject() {
        List<Object> list = new ArrayList<>();
        if (getOwnRules().size() > 0) {
            if (getOwnOperator() == LogicalOperator.and) {
                getOwnRules().forEach(item -> {
                    if (item instanceof Rules)
                        list.add(rulesToMap((Rules) item));
                    else if (item instanceof Rule)
                        list.add(ruleToList((Rule) item));
                });
            } else
                list.add(rulesToMap(this));
        }
        return list;
    }

    private Map<String, Object> rulesToMap(Rules rules) {
        List<Object> list = new ArrayList<>();
        rules.getOwnRules().forEach(item -> {
            if (item instanceof Rules)
                list.add(rulesToMap((Rules) item));
            else if (item instanceof Rule)
                list.add(ruleToList((Rule) item));
        });

        Map<String, Object> map = new HashMap<>();
        map.put(rules.getOwnOperator().toString(), list);
        return map;
    }

    private List<Object> ruleToList(Rule rule) {
        List<Object> list = new ArrayList<>();

        // leftObject
        if (rule.getLeftObject() instanceof PropertyEnum) {
            Object enumValue = ((PropertyEnum) rule.getLeftObject()).getValue();
            list.add(enumValue);
        } else
            list.add(rule.getLeftObject());

        // operator
        list.add(rule.getOperator().toString());

        // rightObject
        if (rule.getRightObject() instanceof PropertyEnum) {
            Object enumValue = ((PropertyEnum) rule.getRightObject()).getValue();
            list.add(enumValue);
        } else
            list.add(rule.getRightObject());

        return list;
    }
}
