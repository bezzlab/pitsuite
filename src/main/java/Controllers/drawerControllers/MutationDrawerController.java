package Controllers.drawerControllers;

import Cds.VariationCondSample;
import Controllers.Controller;
import Singletons.Config;
import TablesModels.Variation;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.json.JSONObject;

import java.net.URL;
import java.util.*;

public class MutationDrawerController extends Controller {

    @FXML
    public Label refPosLabel;
    @FXML
    public Label refLabel;
    @FXML
    public Label altLabel;
    @FXML
    private VBox container;
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

        if(Config.hasPatients()){
            container.getChildren().clear();

            HashMap<String, ArrayList<String>> patients = Config.getPatientsGroups();

            CategoryAxis xRnaAxis = new CategoryAxis();
            NumberAxis yRnaAxis = new NumberAxis();
            StackedBarChart<String, Number> sbcRna =
                    new StackedBarChart<>(xRnaAxis, yRnaAxis);
            XYChart.Series<String, Number> rnaFoundSeries =
                    new XYChart.Series<>();
            XYChart.Series<String, Number> rnaNotFoundSeries =
                    new XYChart.Series<>();

            for(Map.Entry<String, ArrayList<String>> entry: patients.entrySet()){
                HashSet<String> conditionPatients = new HashSet<>(entry.getValue());
                conditionPatients.retainAll(variation.getConditions().keySet());
                rnaFoundSeries.getData().add(new XYChart.Data<>(entry.getKey(), conditionPatients.size()));
                rnaNotFoundSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue().size()-conditionPatients.size()));

            }

            xRnaAxis.setLabel("Condition");
            xRnaAxis.setCategories(FXCollections.observableArrayList(patients.keySet()));
            yRnaAxis.setLabel("Samples");
            rnaFoundSeries.setName("Found");
            rnaNotFoundSeries.setName("Not found");

            sbcRna.getData().addAll(rnaFoundSeries, rnaNotFoundSeries);
            sbcRna.setTitle("Patients with mutation at RNA level");
            container.getChildren().add(sbcRna);





            CategoryAxis xProteinAxis = new CategoryAxis();
            NumberAxis yProteinAxis = new NumberAxis();
            StackedBarChart<String, Number> sbcProtein =
                    new StackedBarChart<>(xProteinAxis, yProteinAxis);
            XYChart.Series<String, Number> proteinFoundSeries =
                    new XYChart.Series<>();
            XYChart.Series<String, Number> proteinNotFoundSeries =
                    new XYChart.Series<>();

            ArrayList<String> peptideRuns = new ArrayList<>();
            for(Object o: variation.getPeptides()){
                org.json.simple.JSONObject pep  = (org.json.simple.JSONObject) o;
                peptideRuns.add(Config.getRunOrLabelCondition((String) pep.get("run")));
            }

            for(Map.Entry<String, ArrayList<String>> entry: patients.entrySet()){
                HashSet<String> conditionPatients = new HashSet<>(entry.getValue());
                conditionPatients.retainAll(new HashSet<>(peptideRuns));
                proteinFoundSeries.getData().add(new XYChart.Data<>(entry.getKey(), conditionPatients.size()));
                proteinNotFoundSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue().size()-conditionPatients.size()));

            }

            xProteinAxis.setLabel("Condition");
            xProteinAxis.setCategories(FXCollections.observableArrayList(patients.keySet()));
            yProteinAxis.setLabel("Samples");
            proteinFoundSeries.setName("Found");
            proteinNotFoundSeries.setName("Not found");
            sbcProtein.setTitle("Patients with mutation at protein level");

            sbcProtein.getData().addAll(proteinFoundSeries, proteinNotFoundSeries);
            container.getChildren().add(sbcProtein);

        }else {

            variationTable.getItems().clear();
            qualChart.getData().clear();

            refPosLabel.setText("Position in refence genome:" + variation.getRefPos());
            refLabel.setText("Reference: " + variation.getRef());
            altLabel.setText("Alternative: " + variation.getAlt());

            ArrayList<XYChart.Series> allSeries = new ArrayList<>();

            Map<String, Map<String, Map<String, Object>>> conditions = variation.getConditions();

            int i;
            for (Map.Entry<String, Map<String, Map<String, Object>>> condition : conditions.entrySet()) {

                i = 0;
                for (Map.Entry<String, Map<String, Object>> sample : condition.getValue().entrySet()) {
                    VariationCondSample variationCondSample = new VariationCondSample(condition.getKey(),
                            sample.getKey(), variation.getRefPos(), (double) sample.getValue().get("qual"), variation.getRef(),
                            variation.getAlt(), (double) sample.getValue().get("AF"));
                    variationTable.getItems().add(variationCondSample);


                    if (i > allSeries.size() - 1) {
                        allSeries.add(new XYChart.Series());
                    }
                    allSeries.get(i).getData().add(new XYChart.Data(condition.getKey(), sample.getValue().get("qual")));

                    i++;
                }
            }
            for (XYChart.Series series : allSeries) {
                qualChart.getData().add(series);
            }
        }
    }
}
