package Controllers.drawerControllers;

import Cds.CDS;
import Cds.PSM;
import Cds.Peptide;
import Controllers.Controller;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

public class PeptideDrawerController extends Controller {

    @FXML
    public Label pepSeqLabel;
    @FXML
    public TableView<PSM> peptidesTable;
    @FXML
    public TableColumn<PSM, String> runColumn;
    @FXML
    public TableColumn<PSM, Double> probColumn;
    @FXML
    public TableColumn<PSM, String> modifColumn;

    private HostServices hostServices;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        runColumn.setCellValueFactory( new PropertyValueFactory<>("run"));
        probColumn.setCellValueFactory( new PropertyValueFactory<>("prob"));
        modifColumn.setCellValueFactory( new PropertyValueFactory<>("modifications"));
    }

    public void show(String pepSeq, HashMap<String, CDS> cdss){
        pepSeqLabel.setText(pepSeq);
        peptidesTable.getItems().clear();

        ArrayList<PSM> psms = new ArrayList<>();

        for (CDS tmpCds : cdss.values()){
            if (tmpCds.getPeptides() != null) {
                for (Peptide tmpPep: tmpCds.getPeptides()){

                    if (tmpPep.getSequence().equals(pepSeq)){

                        psms.addAll(tmpPep.getPsms());

                        break;
                    }

                }
            }

        }

        peptidesTable.getItems().addAll(psms);

    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
        //transcriptIdLink.setOnAction(t -> hostServices.showDocument(hyperlink.getText()));
    }

}
