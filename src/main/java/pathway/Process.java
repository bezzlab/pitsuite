package pathway;

import java.util.ArrayList;

public class Process extends Element{

    private String label;
    private ArrayList<Port> ports = new ArrayList<>(2);


    public Process(double x, double y, double width, double height, String id, String type) {
        super(x, y, width, height, id);
        this.type = type;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setPorts(ArrayList<Port> ports) {
        this.ports = ports;
    }

    public ArrayList<Port> getPorts() {
        return ports;
    }

}
