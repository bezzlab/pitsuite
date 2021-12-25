package Controllers.config_generation;

public class Sample {
    String name;
    String condition;
    String left;
    String right;
    String single;

    public Sample(String name, String condition, String left, String right) {
        this.name = name;
        this.condition = condition;
        this.left = left;
        this.right = right;
    }

    public Sample(String name, String condition, String single) {
        this.name = name;
        this.condition = condition;
        this.single = single;
    }

    public String getName() {
        return name;
    }

    public String getCondition() {
        return condition;
    }

    public String getLeft() {
        return left;
    }

    public String getRight() {
        return right;
    }

    public String getSingle() {
        return single;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public void setRight(String right) {
        this.right = right;
    }

    public void setSingle(String single) {
        this.single = single;
    }
}
