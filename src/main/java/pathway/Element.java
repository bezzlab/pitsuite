package pathway;

public class Element {

    private double x;
    private double y;
    private double width;
    private double height;
    private String id;
    protected String type;
    private String label;

    public Element(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Element(double x, double y, double width, double height, String id) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.id = id;

    }

    public Element(double x, double y, double width, double height, String id, String type, String label) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.id = id;
        this.type=type;
        this.label=label;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }
}
