package pathway;

public class Label extends Element{

    private String label;

    public Label(double x, double y, double width, double height,  String label) {
        super(x, y, width, height);
        this.label = label;
    }

    public String getLabel(){
        return label;
    }
}
