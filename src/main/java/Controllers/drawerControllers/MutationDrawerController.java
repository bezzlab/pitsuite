package Controllers.drawerControllers;

import Cds.VariationCondSample;
import Controllers.Controller;
import TablesModels.Variation;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;

public class MutationDrawerController extends Controller {

    @FXML
    public Label refPosLabel;
    @FXML
    public Label refLabel;
    @FXML
    public Label altLabel;
    @FXML
    private TableView<VariationCondSample> variationTable;
    @FXML
    private TableColumn<VariationCondSample, String> conditionColumn;
    @FXML
    private TableColumn<VariationCondSample, String> sampleColumn;
    @FXML
    private TableColumn<VariationCondSample, Double> qualColumn;
    @FXML
    private TableColumn<VariationCondSample, Double> afColumn;
    @FXML
    private BarChart<String, Double> qualChart;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        conditionColumn.setCellValueFactory( new PropertyValueFactory<>("condition"));
        sampleColumn.setCellValueFactory( new PropertyValueFactory<>("sample"));
        qualColumn.setCellValueFactory( new PropertyValueFactory<>("qual"));
        afColumn.setCellValueFactory( new PropertyValueFactory<>("af"));
    }


    public void show(Variation variation){

        variationTable.getItems().clear();
        qualChart.getData().clear();

        refPosLabel.setText("Position in refence genome:" + variation.getRefPos());
        refLabel.setText("Reference: " + variation.getRef());
        altLabel.setText("Alternative: " + variation.getAlt());

        ArrayList<XYChart.Series> allSeries = new ArrayList<>();

        Map<String, Map< String, Map<String, Double>>> conditions = variation.getConditions();

        int i;
        for(Map.Entry<String, Map< String, Map<String, Double>>> condition: conditions.entrySet()){

            i = 0;
            for(Map.Entry<String, Map<String, Double>> sample: condition.getValue().entrySet()){
                VariationCondSample variationCondSample = new VariationCondSample(condition.getKey(),
                        sample.getKey(), variation.getRefPos(), sample.getValue().get("qual"), variation.getRef(),
                        variation.getAlt(), sample.getValue().get("AF"));
                variationTable.getItems().add(variationCondSample);


                if(i>allSeries.size()-1){
                    allSeries.add(new XYChart.Series());
                }
                allSeries.get(i).getData().add(new XYChart.Data(condition.getKey(), sample.getValue().get("qual")));

                i++;
            }
        }
        for(XYChart.Series series: allSeries){
         qualChart.getData().add(series);
        }
    }
}
