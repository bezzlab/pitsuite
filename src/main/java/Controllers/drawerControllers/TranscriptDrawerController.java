package Controllers.drawerControllers;

import Cds.Exon;
import Cds.Transcript;
import Cds.TranscriptCondSample;
import Controllers.Controller;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Pair;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class TranscriptDrawerController extends Controller {


    @FXML
    public Hyperlink transcriptIdLink;
    @FXML
    private TableView<TranscriptCondSample> transcriptTable;
    @FXML
    private TableColumn<TranscriptCondSample, String> conditionColumn;
    @FXML
    private TableColumn<TranscriptCondSample, String> sampleColumn;
    @FXML
    private TableColumn<TranscriptCondSample, Double> tpmColumn;
    @FXML
    private BarChart<String, Double> tpmChart;
    @FXML
    private TableView<Exon> exonTable;
    @FXML
    private TableColumn<Exon, Double> exonStartColumn;
    @FXML
    private TableColumn<Exon, Double> exonEndColumn;

    private HostServices hostServices;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        conditionColumn.setCellValueFactory( new PropertyValueFactory<>("condition"));
        sampleColumn.setCellValueFactory( new PropertyValueFactory<>("sample"));
        tpmColumn.setCellValueFactory( new PropertyValueFactory<>("tpm"));

        conditionColumn.prefWidthProperty().bind(transcriptTable.widthProperty().divide(3));
        sampleColumn.prefWidthProperty().bind(transcriptTable.widthProperty().divide(3));
        tpmColumn.prefWidthProperty().bind(transcriptTable.widthProperty().divide(3));

        exonStartColumn.setCellValueFactory( new PropertyValueFactory<>("start"));
        exonEndColumn.setCellValueFactory( new PropertyValueFactory<>("end"));

        exonStartColumn.prefWidthProperty().bind(exonTable.widthProperty().divide(2));
        exonEndColumn.prefWidthProperty().bind(exonTable.widthProperty().divide(2));

        tpmChart.setLegendVisible(false);
        

    }


    public void show(Transcript transcript){

        transcriptTable.getItems().clear();
        exonTable.getItems().clear();
        tpmChart.getData().clear();

        transcriptIdLink.setText(transcript.getTranscriptId());


        ArrayList<XYChart.Series> allSeries = new ArrayList<>();

        HashMap<String, HashMap<String, Double>> tpms =  transcript.getTpms();
        HashMap<String, Integer> conditionsIndex = new HashMap<>();


        for(Map.Entry<String, HashMap<String, Double>> conditionEntry: tpms.entrySet()) {

            for(Map.Entry<String, Double> sampleEntry: conditionEntry.getValue().entrySet()) {


                TranscriptCondSample transcriptCondSample = new TranscriptCondSample(conditionEntry.getKey(), sampleEntry.getKey(), sampleEntry.getValue());
                transcriptTable.getItems().add(transcriptCondSample);


                if (!conditionsIndex.containsKey(sampleEntry.getKey())) {
                    conditionsIndex.put(sampleEntry.getKey(), 0);
                }

                int conditionIndex = conditionsIndex.get(sampleEntry.getKey());
                if (conditionIndex > allSeries.size() - 1) {
                    allSeries.add(new XYChart.Series());
                }
                allSeries.get(conditionsIndex.get(sampleEntry.getKey())).getData().add(new XYChart.Data(sampleEntry.getKey(), sampleEntry.getValue()));

                conditionsIndex.replace(sampleEntry.getKey(), conditionsIndex.get(sampleEntry.getKey()) + 1);
            }

        }
        for(XYChart.Series series: allSeries){
            tpmChart.getData().add(series);
        }



        for(Exon exon: transcript.getExons()){
            exonTable.getItems().add(exon);
        }
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
        //transcriptIdLink.setOnAction(t -> hostServices.showDocument(hyperlink.getText()));
    }
}
