package Controllers.drawerControllers;

import Cds.CDS;
import Cds.Pfam;
import Cds.Transcript;
import Controllers.Controller;
import TablesModels.Variation;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import pitguiv2.App;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class DrawerController extends Controller {

    @FXML
    public AnchorPane mainPane;

    private MutationDrawerController variationController;
    private Parent variationRoot;
    private TranscriptDrawerController transcriptController;
    private Parent transcriptRoot;
    private PfamDrawerController pfamController;
    private Parent pfamRoot;
    private Parent peptideRoot;
    private PeptideDrawerController peptideController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        FXMLLoader variationFXML = new FXMLLoader(App.class.getResource("/drawerControllers/mutationDrawer.fxml"));
        FXMLLoader transcriptFXML = new FXMLLoader(App.class.getResource("/drawerControllers/transcriptDrawer.fxml"));
        FXMLLoader pfamFXML = new FXMLLoader(App.class.getResource("/drawerControllers/pfamDrawer.fxml"));
        FXMLLoader peptideFXML = new FXMLLoader(App.class.getResource("/drawerControllers/peptideDrawer.fxml"));

        try {
            variationRoot = variationFXML.load();
            variationController = variationFXML.getController();

            transcriptRoot = transcriptFXML.load();
            transcriptController = transcriptFXML.getController();

            pfamRoot = pfamFXML.load();
            pfamController = pfamFXML.getController();

            peptideRoot = peptideFXML.load();
            peptideController = peptideFXML.getController();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public void showVariation(Variation variation){
        mainPane.getChildren().clear();
        mainPane.getChildren().add(variationRoot);
        variationController.show(variation);
    }

    public void showtranscript(Transcript transcript){
        mainPane.getChildren().clear();
        mainPane.getChildren().add(transcriptRoot);
        transcriptController.show(transcript);
    }

    public void showPfam(Pfam pfam){
        mainPane.getChildren().clear();
        mainPane.getChildren().add(pfamRoot);
        pfamController.show(pfam);
    }

    public void showPeptides(String pepSeq, HashMap<String, CDS> cdss){
        mainPane.getChildren().clear();
        mainPane.getChildren().add(peptideRoot);
        peptideController.show(pepSeq, cdss);
    }


    public void setHostServices(HostServices hostServices) {
        transcriptController.setHostServices(hostServices);
        peptideController.setHostServices(hostServices);
    }
}
