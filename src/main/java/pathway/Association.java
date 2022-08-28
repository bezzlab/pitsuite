package pathway;

public class Association extends Element{

    Arc arc;

    public Association(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    public Association(double x, double y, double width, double height, String id) {
        super(x, y, width, height, id);
    }

    public Association(double x, double y, double width, double height, String id, String type, String label) {
        super(x, y, width, height, id, type, label);
    }

    public Arc getArc() {
        return arc;
    }

    public void setArc(Arc arc) {
        this.arc = arc;
    }
}
