package Controllers.MSControllers;

import Controllers.SettingsController;
import Singletons.Config;
import graphics.AnchorFitter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MSController implements Initializable {


    @FXML
    private TabPane ptmTabpane;
    @FXML
    private PeptideTableController peptideTableController;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        peptideTableController.load();

        for(Map.Entry<String, ArrayList<String>> entry: Config.getAllPTMSearched().entrySet()){
            Tab tab = new Tab();
            AnchorPane anchorPane = new AnchorPane();
            FXMLLoader fxmlLoader1 = new FXMLLoader(SettingsController.class.getResource("/ptm.fxml"));
            try {
                Parent root = fxmlLoader1.load();
                PTMController ptmController = fxmlLoader1.getController();
                ptmController.loadPtm(entry.getKey(), entry.getValue());
                AnchorFitter.fitAnchor(root);
                anchorPane.getChildren().add(root);
            } catch(Exception e){
                e.printStackTrace();
            }


            tab.setContent(anchorPane);
            tab.setText(entry.getKey());
            ptmTabpane.getTabs().add(tab);

        }
    }
}
