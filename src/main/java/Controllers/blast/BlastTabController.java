package Controllers.blast;

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
import java.util.Optional;
import java.util.ResourceBundle;

public class BlastTabController implements Initializable {

    @FXML
    AnchorPane container;

    private BlastPaneController rnaBlastController;
    private BlastPaneController proteinBlastController;

    private static BlastTabController instance;
    private TabPane tabPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        instance = this;

        if(Config.hasProteinBlast() && Config.hasRnaBlast()){
            tabPane = new TabPane();
            AnchorFitter.fitAnchor(tabPane);


            Tab rnaTab = new Tab();
            rnaTab.setClosable(false);
            rnaTab.setText("RNA");
            rnaTab.setContent(loadBlastPane("rna"));
            Tab proteinTab = new Tab();
            proteinTab.setClosable(false);
            proteinTab.setText("Protein");
            proteinTab.setContent(loadBlastPane("protein"));
            tabPane.getTabs().add(rnaTab);
            tabPane.getTabs().add(proteinTab);

            AnchorFitter.fitAnchor(proteinTab.getContent());
            AnchorFitter.fitAnchor(rnaTab.getContent());

            container.getChildren().add(tabPane);
        }else if(Config.hasProteinBlast()){
            container.getChildren().add(loadBlastPane("protein"));
        }else if(Config.hasRnaBlast()){
            container.getChildren().add(loadBlastPane("rna"));
        }

    }


    public Parent loadBlastPane(String rnaOrProtein){
        FXMLLoader fxmlLoader = new FXMLLoader(SettingsController.class.getResource("/blastPane" + ".fxml"));
        try {
            Parent root = fxmlLoader.load();

            if(rnaOrProtein.equals("rna")){
                rnaBlastController = fxmlLoader.getController();
                rnaBlastController.setType(rnaOrProtein);
            }else{
                proteinBlastController = fxmlLoader.getController();
                proteinBlastController.setType(rnaOrProtein);
            }


            return root;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public BlastPaneController getRnaBlastController() {
        return rnaBlastController;
    }

    public BlastPaneController getProteinBlastController() {
        return proteinBlastController;
    }

    public static BlastTabController getInstance() {
        return instance;
    }

    public void selectTab(String tabName){
        if(tabPane!=null){
            if(tabName.equals("rna")){
                tabPane.getSelectionModel().select(0);
            }else{
                tabPane.getSelectionModel().select(1);
            }
        }
    }

}










