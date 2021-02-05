package Controllers;


import Cds.Peptide;
import FileReading.AllGenesReader;
import TablesModels.BamFile;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.dizitart.no2.Nitrite;
import org.json.JSONObject;
import utilities.BioFile;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class BrowserController implements Initializable {

    //main tabs
    @FXML
    private TabPane browsersTabTabPane;
    @FXML
    private Tab genomeBrowserTab;
    @FXML
    private Tab geneBrowserTab;

    // panes in tabs
    @FXML
    private GenomeBrowserController genomeBrowserController;
    @FXML
    private GeneBrowserController geneBrowserController;


    private String databaseProjectName;
    private ResultsController parentController;
    private HostServices hostServices;




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


    }

    /*
     * Used to set the parent, from the FXML Document Controller,
     * So that when data is loaded, it can handle the first view of the tab
     */
    public void setParentControler(ResultsController parent, JSONObject settings, HostServices hostServices, String databaseName,
                                    AllGenesReader allGenesReader) {
        parentController = parent;
        databaseProjectName = databaseName;
        this.hostServices = hostServices;
        setTabsParents(parent, settings, databaseProjectName, allGenesReader);
    }

    private void setTabsParents(ResultsController parent, JSONObject settings, String databaseProjectName, AllGenesReader keggReader){
        genomeBrowserController.setParentControler(parent);
        geneBrowserController.setParentControler(parent, settings, hostServices, databaseProjectName, keggReader);
    }

    public void geneBrowserDisplayGeneFromId(String gene, int position){
        browsersTabTabPane.getSelectionModel().select(geneBrowserTab);
        geneBrowserController.showGeneAtPosition(gene, position);
    }
    public void geneBrowserDisplayGeneFromId(String gene, int start, int end){
        browsersTabTabPane.getSelectionModel().select(geneBrowserTab);
        geneBrowserController.showGeneAtPosition(gene, start, end);
    }

    public GeneBrowserController getGeneBrowserController() {
        return geneBrowserController;
    }

    public void updateSettings(JSONObject settings){
        geneBrowserController.updateSettings(settings);
    }

    public void showGeneBrowser(String gene){
        browsersTabTabPane.getSelectionModel().select(1);
        geneBrowserController.showGeneBrowser(gene);
    }

    public void showGeneBrowser(String gene, Peptide peptide){
        browsersTabTabPane.getSelectionModel().select(1);
        geneBrowserController.showGeneBrowser(gene, peptide);
    }

    public void resize(){
        geneBrowserController.resize();
    }

    public void onTrackFilesUpdated(){
        geneBrowserController.onTrackFilesUpdated();
    }
}



















