package pathway;

import Controllers.PathwayController;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import org.w3c.dom.Text;
import pathway.alerts.Alert;
import pathway.alerts.DgeAlert;

import java.util.ArrayList;

public class Element {

    private double x;
    private double y;
    private double width;
    private double height;
    private String id;
    protected String type;
    protected String label;

    private Group nodeGroups = new Group();
    private Group colorRectangles = new Group();
    private Label nodeLabel;
    private Shape node;
    private ArrayList<Entity> entities = new ArrayList<>();
    private ArrayList<Alert> alerts = new ArrayList<>();

    public Element(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        nodeGroups.getChildren().add(colorRectangles);
    }

    public Element(double x, double y, double width, double height, String id) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.id = id;
        nodeGroups.getChildren().add(colorRectangles);

    }

    public Element(double x, double y, double width, double height, String id, String type, String label) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.id = id;
        this.type=type;
        this.label=label;
        nodeGroups.getChildren().add(colorRectangles);
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

    public void setNode(Shape node) {
        this.node = node;
        nodeGroups.getChildren().add(node);
    }

    public Node getNode() {
        return node;
    }

    public void addEntity(Entity entity){
        entities.add(entity);
    }

    public ArrayList<Entity> getEntities() {
        return entities;
    }

    public Label getNodeLabel() {
        return nodeLabel;
    }

    public void setNodeLabel(Label nodeLabel) {
        this.nodeLabel = nodeLabel;
        nodeGroups.getChildren().add(nodeLabel);
    }

    public Group getNodeGroups() {
        return nodeGroups;
    }

    public Group getColorRectangles() {
        return colorRectangles;
    }

    public void addColorRectangle(Rectangle r){
        colorRectangles.getChildren().add(r);
    }

    public void setAlert(Alert alert, PathwayController pathwayController) {

        if(alert.getClass().equals(DgeAlert.class)){
            if(alerts.stream().filter(e->e.getClass().equals(DgeAlert.class)).findFirst().isEmpty()){
                Label t = new Label("G");
                t.setFont(Font.font(8));
                t.setLayoutX(pathwayController.scaleCoordinates(x, "x"));
                t.setLayoutY(pathwayController.scaleCoordinates(y, "y")-10);
                nodeGroups.getChildren().add(t);
            }
        }
        alerts.add(alert);

    }
    
}
