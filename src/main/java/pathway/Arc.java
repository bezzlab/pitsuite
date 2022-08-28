package pathway;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.ListIterator;

public class Arc {

    private String source;
    private String target;
    private String type;
    private String id;
    private Element glyph;


    ArrayList<Pair<Double, Double>> points = new ArrayList<>(3);

    public Arc(String source, String target, String type, String id) {
        this.source = source;
        this.target = target;
        this.type = type;
        this.id = id;
    }

    public Arc(String source, String target, String type) {
        this.source = source;
        this.target = target;
        this.type = type;
    }

    public void setPoints(ArrayList<Pair<Double, Double>> points) {

        Pair<Double, Double> lastPoints = points.get(points.size()-1);
        points.removeIf(point -> (point.getKey().equals(lastPoints.getKey()) && point.getValue().equals(lastPoints.getValue())) && point!=lastPoints);


        this.points = points;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public ArrayList<Pair<Double, Double>> getPoints() {
        return points;
    }

    public void setGlyph(Element glyph) {
        this.glyph = glyph;
    }

}
