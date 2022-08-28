package graphics;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class GraphicTools {


    public static Group drawHeatmap(double min, double max, double width, double height){
        Group heatmapScaleGroup = new Group();

        Stop[] stops = new Stop[] { new Stop(0, Color.hsb(Color.GREEN.getHue(), 1.0, 1.0)),
                new Stop(0.5,Color.hsb(Color.YELLOW.getHue(), 1.0, 1.0)),
                new Stop(1,Color.hsb(Color.RED.getHue(), 1.0, 1.0))};
        LinearGradient lg1 = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
        Rectangle r1 = new Rectangle();
        r1.setFill(lg1);
        r1.setHeight(height);
        r1.setWidth(width);
        //r1.setX(container.getWidth()-250);
        heatmapScaleGroup.getChildren().add(r1);

        NumberFormat formatter = new DecimalFormat("#0.00");

        javafx.scene.control.Label minLabel = new javafx.scene.control.Label(formatter.format(min));
        minLabel.setLayoutX(10);
        minLabel.setLayoutY(height+10);
        javafx.scene.control.Label maxLabel = new javafx.scene.control.Label(formatter.format(max));
        maxLabel.setLayoutX(width-maxLabel.getLayoutBounds().getWidth());
        maxLabel.setLayoutY(height+10);
        heatmapScaleGroup.getChildren().add(minLabel);
        heatmapScaleGroup.getChildren().add(maxLabel);

        return heatmapScaleGroup;
    }
}
