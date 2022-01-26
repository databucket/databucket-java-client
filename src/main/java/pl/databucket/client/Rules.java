package pl.databucket.client;

import java.util.ArrayList;
import java.util.List;

public class Rules {

    LogicalOperator logicalOperator = LogicalOperator.and;
    List<Object> rules = new ArrayList<>();

    public Rules() {
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

    public void addSubRules(Rules rules) {
        this.rules.add(rules);
    }

    public List<Object> getMatchRules() {
        return this.rules;
    }

    public String toJsonString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\"rules\":");
        if (rules.size() > 0) {
            stringBuilder.append("[");
            if (logicalOperator == LogicalOperator.and) {
                for (int i = 0; i < rules.size(); i++) {
                    Object item = rules.get(i);
                    if (item instanceof Rules)
                        stringBuilder.append(getMatchRulesStr((Rules) item));
                    else if (item instanceof Rule)
                        stringBuilder.append(getMatchRuleStr((Rule) item));

                    if (i < rules.size() - 1)
                        stringBuilder.append(",");
                }
            } else
                stringBuilder.append(getMatchRulesStr(this));
            stringBuilder.append("]");
        } else
            stringBuilder.append("null");

        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    private String getMatchRulesStr(Rules matchRules) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{").append("\"").append(matchRules.logicalOperator.toString()).append("\": [");
        for (int i = 0; i < matchRules.getMatchRules().size(); i++) {
            Object item = matchRules.getMatchRules().get(i);
            if (item instanceof Rules)
                stringBuilder.append(getMatchRulesStr((Rules) item));
            else if (item instanceof Rule)
                stringBuilder.append(getMatchRuleStr((Rule) item));

            if (i < matchRules.getMatchRules().size() - 1)
                stringBuilder.append(",");
        }
        stringBuilder.append("]}");
        return stringBuilder.toString();
    }

    private String getMatchRuleStr(Rule rule) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("[");

        // leftObject
        if (rule.getLeftObject() instanceof String)
            stringBuilder.append("\"").append(rule.getLeftObject()).append("\", ");
        else if (rule.getLeftObject() instanceof PropertyEnum) {
            Object enumValue = ((PropertyEnum) rule.getLeftObject()).getValue();
            if (enumValue instanceof String)
                stringBuilder.append("\"").append(enumValue).append("\"");
            else
                stringBuilder.append(enumValue);
        } else
            stringBuilder.append(rule.getLeftObject()).append(", ");

        // operator
        stringBuilder.append("\"").append(rule.getOperator().toString()).append("\", ");

        // rightObject
        if (rule.getRightObject() instanceof String)
            stringBuilder.append("\"").append(rule.getRightObject()).append("\"");
        else if (rule.getRightObject() instanceof PropertyEnum) {
            Object enumValue = ((PropertyEnum) rule.getRightObject()).getValue();
            if (enumValue instanceof String)
                stringBuilder.append("\"").append(enumValue).append("\"");
            else
                stringBuilder.append(enumValue);
        } else
            stringBuilder.append(rule.getRightObject());

        stringBuilder.append("]");

        return stringBuilder.toString();
    }
}
