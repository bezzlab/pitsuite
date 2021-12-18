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
}
