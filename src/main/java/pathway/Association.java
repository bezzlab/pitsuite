package pathway;

import java.util.ArrayList;

public class Association extends Element{

    private String label;
    private ArrayList<Port> ports = new ArrayList<>(2);


    public Association(double x, double y, double width, double height, String id) {
        super(x, y, width, height, id);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setPorts(ArrayList<Port> ports) {
        this.ports = ports;
    }
}
