package FileReading;

import Controllers.AddBamController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.util.Pair;

import java.io.IOException;
import java.util.HashMap;

public class AddBamFileDialog extends Dialog {

    AddBamController controller;

    public AddBamFileDialog(HashMap<Pair<String, String>, String> pathsMap) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addBam.fxml"));
            Parent root = loader.load();
            controller = loader.<AddBamController>getController();

            getDialogPane().setContent(root);
            ButtonType bt = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);

            getDialogPane().getButtonTypes().add(bt);
            getDialogPane().lookupButton(bt).setVisible(false);

            controller.displayTable(pathsMap);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<Pair<String, String>, String> getCondSampleBamPathMap(){return controller.getCondSampleBamPathMap();}

}
