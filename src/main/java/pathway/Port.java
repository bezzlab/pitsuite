package pathway;

public class Port {

    private String id;
    private double x;
    private double y;

    public Port(double x, double y, String id) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public String getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
