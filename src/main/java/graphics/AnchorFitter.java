package graphics;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

public class AnchorFitter {

    public static void fitAnchor(Node node){
        AnchorPane.setBottomAnchor(node, 0.);
        AnchorPane.setTopAnchor(node, 0.);
        AnchorPane.setLeftAnchor(node, 0.);
        AnchorPane.setRightAnchor(node, 0.);
    }
}
