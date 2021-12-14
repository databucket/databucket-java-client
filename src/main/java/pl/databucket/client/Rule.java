package pl.databucket.client;

public class Rule {

    private Object leftObject;
    private Operator operator;
    private Object rightObject;

    public Rule(Object leftObject, Operator operator, Object rightObject) {
        this.leftObject = leftObject;
        this.operator = operator;
        this.rightObject = rightObject;
    }

    public Object getLeftObject() {
        return leftObject;
    }

    public Operator getOperator() {
        return operator;
    }

    public Object getRightObject() {
        return rightObject;
    }

}
