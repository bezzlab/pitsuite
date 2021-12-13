package Controllers.MSControllers;

import Cds.PTM;
import Controllers.PathwaySideController;
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

    private static MSController instance;
    private HashMap<String, PTMController> ptmControllers = new HashMap<>();




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        instance = this;
        peptideTableController.load();

        for(Map.Entry<String, ArrayList<String>> entry: Config.getAllPTMSearched().entrySet()){
            Tab tab = new Tab();
            AnchorPane anchorPane = new AnchorPane();
            FXMLLoader fxmlLoader1 = new FXMLLoader(SettingsController.class.getResource("/ptm.fxml"));
            try {
                Parent root = fxmlLoader1.load();
                PTMController ptmController = fxmlLoader1.getController();
                ptmController.loadPtm(entry.getKey(), entry.getValue());
                ptmControllers.put(entry.getKey(), ptmController);
                AnchorFitter.fitAnchor(root);
                anchorPane.getChildren().add(root);
            } catch(Exception e){
                e.printStackTrace();
            }


            tab.setContent(anchorPane);
            tab.setText(entry.getKey());
            ptmTabpane.getTabs().add(tab);

            if(entry.getKey().equals("Phospho (STY)") && Config.getSpecies()!=null && Config.getSpecies().equalsIgnoreCase("HOMO SAPIENS")){
                try {
                    Tab kinaseTab = new Tab();
                    kinaseTab.setText("Kinase activity");
                    FXMLLoader fxmlLoader2 = new FXMLLoader(SettingsController.class.getResource("/kinase.fxml"));
                    AnchorPane anchorPane2 = new AnchorPane();
                    Parent root2 = fxmlLoader2.load();
                    AnchorFitter.fitAnchor(root2);
                    anchorPane2.getChildren().add(root2);
                    kinaseTab.setContent(anchorPane2);
                    KinaseController kinaseController = fxmlLoader2.getController();
                    kinaseController.loadKinases(entry.getValue());
                    ptmTabpane.getTabs().add(kinaseTab);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

        }
    }
    public static MSController getInstance() {
        return instance;
    }

    public void selectTab(String tabName) {
        for(Tab tab: ptmTabpane.getTabs()){
            if(tab.getText().equals(tabName)){
                ptmTabpane.getSelectionModel().select(tab);
                break;
            }
        }
    }

    public PTMController getPTMController(String ptm){
        return ptmControllers.get(ptm);
    }
}
