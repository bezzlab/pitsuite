package pathway;

public class Compartment extends Element{

    private Label label;

    public Compartment(double x, double y, double width, double height, String id) {
        super(x, y, width, height, id);
    }

    public void setLabel(Label label){ this.label = label; }


    public Label getLabelObj() {
        return label;
    }
}
