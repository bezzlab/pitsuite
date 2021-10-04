package Controllers;

import FileReading.AllGenesReader;
import Singletons.Config;
import Singletons.Database;
import TablesModels.Variation;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import export.ProVcf;
import graphics.DoughnutChart;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.Pair;
import org.controlsfx.control.RangeSlider;
import org.controlsfx.control.textfield.TextFields;
import org.dizitart.no2.*;
import org.dizitart.no2.filters.Filters;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import pitguiv2.Settings;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.*;

import static org.dizitart.no2.filters.Filters.*;


public class MutationsTableController implements Initializable {

    @FXML
    public JFXCheckBox cdsCheckbox;
    @FXML
    public JFXCheckBox nonSilentCheckbox;
    @FXML
    private TableView <Variation> variantsTable;
    @FXML
    private TableColumn  transcIdVarTableColumn; // without the class here to be able to add the font formating
    @FXML
    private TableColumn <Variation, Integer> positionVarTableColumn;
    @FXML
    private TableColumn <Variation, String> typeVarTableColumn;
    @FXML
    private TableColumn <Variation, String> refVarTableColumn;
    @FXML
    private TableColumn <Variation, String> altVarTableColumn;
    @FXML
    private TableColumn <Variation, Boolean> inCDSColumn;
    @FXML
    private TableColumn <Variation, Boolean> silentColumn;
    @FXML
    private TableColumn <Variation, Boolean> peptideEvidenceColumn;
    @FXML
    private TableColumn<Variation, String> proteinRefTableColumn;
    @FXML
    private TableColumn<Variation, String> proteinAltTableColumn;
    @FXML
    private TableColumn<Variation, String> pfamColumn;


    @FXML
    private Button filterTableButton;

    @FXML
    private Label numberOfElementsInTableLabel;
    // table filters
    @FXML
    private JFXTextField geneIdTableFilterTextFiled;
    @FXML
    private JFXCheckBox insertionTableFilterCheckbox;
    @FXML
    private JFXCheckBox deletionTableFilterCheckbox;
    @FXML
    private JFXCheckBox snpTableFilterCheckbox;
    @FXML
    private JFXCheckBox otherTableFilterCheckbox;
    @FXML
    private CheckBox peptideEvidenceCheckbox;



    // graphs
    @FXML
    private DoughnutChart mutationsPieChart;
    @FXML
    private BarChart<String, Number> qualBarChart;
    @FXML
    private BarChart<String, Number> afBarChart;
    @FXML
    VBox upsetSampleSliderBox;
    @FXML
    GridPane mainGrid;

    private String databaseProjectName;

    private AllGenesReader allGenesReader;



    private ResultsController parentController;
    private ArrayList<CheckBox> conditionFilterCheckboxes;
    private ArrayList<RangeSlider> minSamplesConditionFilterSliders;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // to generate the getters for the column values
        transcIdVarTableColumn.setCellValueFactory(new PropertyValueFactory<Variation, String>("gene")); // added the class here to be able to add the font formating below
        typeVarTableColumn.setCellValueFactory(new PropertyValueFactory<>("type")); // added the class here to be able to add the font formating below
        positionVarTableColumn.setCellValueFactory(new PropertyValueFactory<>("refPos"));
        refVarTableColumn.setCellValueFactory(new PropertyValueFactory<>("ref"));
        altVarTableColumn.setCellValueFactory(new PropertyValueFactory<>("alt"));
        peptideEvidenceColumn.setCellValueFactory(new PropertyValueFactory<>("hasPeptideEvidence"));
        inCDSColumn.setCellValueFactory(new PropertyValueFactory<>("inCDS"));
        silentColumn.setCellValueFactory(new PropertyValueFactory<>("silent"));
        proteinRefTableColumn.setCellValueFactory(new PropertyValueFactory<>("refAA"));
        proteinAltTableColumn.setCellValueFactory(new PropertyValueFactory<>("altAA"));
        pfamColumn.setCellValueFactory(new PropertyValueFactory<>("pfamStr"));
        int nCols = variantsTable.getColumns().size();
        transcIdVarTableColumn.prefWidthProperty().bind(variantsTable.widthProperty().divide(nCols));
        positionVarTableColumn.prefWidthProperty().bind(variantsTable.widthProperty().divide(nCols));
        typeVarTableColumn.prefWidthProperty().bind(variantsTable.widthProperty().divide(nCols));
        refVarTableColumn.prefWidthProperty().bind(variantsTable.widthProperty().divide(nCols));
        altVarTableColumn.prefWidthProperty().bind(variantsTable.widthProperty().divide(nCols));
        peptideEvidenceColumn.prefWidthProperty().bind(variantsTable.widthProperty().divide(nCols));
        inCDSColumn.prefWidthProperty().bind(variantsTable.widthProperty().divide(nCols));
        silentColumn.prefWidthProperty().bind(variantsTable.widthProperty().divide(nCols));
        pfamColumn.prefWidthProperty().bind(variantsTable.widthProperty().divide(nCols));

        insertionTableFilterCheckbox.setSelected(true);
        deletionTableFilterCheckbox.setSelected(true);
        snpTableFilterCheckbox.setSelected(true);
        otherTableFilterCheckbox.setSelected(true);


        //  add actions to table
        variantsTable.setRowFactory(tv -> {
            TableRow<Variation> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!(row.isEmpty())) {
                    if (event.getButton().equals(MouseButton.PRIMARY)){
                        Variation tableRow = row.getItem();
                        if ( event.getClickCount() == 1 ) {

                            displayTranscMutInfo(tableRow);
                        } else if ( event.getClickCount() == 2 ) {

                            boolean foundCondition=false;
                            if(tableRow.isInCDS() && !tableRow.isSilent()){
                                for(Map.Entry<String, Map<String,  Map<String, Object>>> o: tableRow.getConditions().entrySet()){
                                    for(Map.Entry<String,  Map<String, Object>> o2: o.getValue().entrySet()){
                                        if(o2.getValue().containsKey("silent")){
                                            foundCondition = true;
                                            parentController.showBrowserFromTranscId(row.getItem().getGene(), row.getItem().getRefPos(),
                                                    o.getKey());
                                            System.out.println("FOUND");
                                            break;
                                        }
                                    }
                                    if(foundCondition)
                                        break;
                                }
                            }

                            if(!foundCondition)
                                parentController.showBrowserFromTranscId(row.getItem().getGene(), row.getItem().getRefPos());
                        }
                    }

                }
            });
            return row;
        });


        // To underline the ids
        transcIdVarTableColumn.setCellFactory(new Callback<TableColumn, TableCell>() {
            public TableCell call(TableColumn param) {
                return new TableCell<TableColumn, String>() {

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!isEmpty()) {
                            this.setUnderline(true);
                            if (item.startsWith("ENST")) {
                                this.setTextFill(Color.rgb(52, 85, 235));
                            } else {
                                this.setTextFill(Color.rgb(145, 160, 227));
                            }
                            setText(item);
                        } else {
                            setText(item);
                        }
                    }
                };
            }
        });


        filterTableButton.setOnAction(actionEvent -> filterTable());

    }


    /*
     * Used to set the parent, from the FXML Document Controller,
     * So that when data is loaded, it can handle the first view of the tab
     */
    public void setParentControler(ResultsController parent, String databaseName,  AllGenesReader allGenesReader){
        parentController = parent;
        this.allGenesReader = allGenesReader;
        databaseProjectName = databaseName;


        if(allGenesReader.getGenesLoadedProperty().get()){
            TextFields.bindAutoCompletion(geneIdTableFilterTextFiled, allGenesReader.getAllGeneNames());
        }else{
            allGenesReader.getGenesLoadedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if(newValue){
                        TextFields.bindAutoCompletion(geneIdTableFilterTextFiled, allGenesReader.getAllGeneNames());
                        allGenesReader.getGenesLoadedProperty().removeListener(this);
                    }

                }
            });
        }


        // get mutations from the database and add it to the table
        drawSampleFilterSliders();
        filterTable();

    }


    /**
     * Either sends a new query to the Database.getDb() or filters the table with a for loop
     * @param type type of function
     */

    /**
     * Uses the list of variations and filters the table
     * also displays the number of elements on the bottom
     */
    private void filterTable(){


        boolean insSelected = insertionTableFilterCheckbox.isSelected();
        boolean delSelected = deletionTableFilterCheckbox.isSelected();
        boolean snpSelected = snpTableFilterCheckbox.isSelected();
        boolean otherSelected = otherTableFilterCheckbox.isSelected();


        if (!(insSelected || delSelected || snpSelected || otherSelected)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Info");
            alert.setHeaderText(null);
            alert.setContentText("Please select at least 1 type of Mutation: Insertion (Ins), Deletion (Del), Single-Nucleotide polymorphism (SNP) or other.");

            alert.showAndWait();
        } else {
            new Thread(() -> {

                List<Filter> filters = new ArrayList<>(4);
                if(geneIdTableFilterTextFiled.getText().length()>0){
                    filters.add(eq("gene", geneIdTableFilterTextFiled.getText().toUpperCase().strip()));
                }
                if(cdsCheckbox.isSelected()){
                    filters.add(eq("inCDS", true));
                }
                if(nonSilentCheckbox.isSelected()){
                    filters.add(eq("silent", false));
                }
                if(peptideEvidenceCheckbox.isSelected()){
                    filters.add(eq("hasPeptideEvidence", true));
                }

                ArrayList<Variation> mutations = new ArrayList<>();


                long startTime = System.currentTimeMillis();
                Cursor mutationsCursor = Database.getDb().getCollection("mutations").find(and(filters.toArray(new Filter[]{})));
                long estimatedTime = System.currentTimeMillis() - startTime;
                System.out.println(estimatedTime);





                int insCount = 0;
                int delCount = 0;
                int snpCount = 0;
                int otherCount = 0;


                for (Document mutationDoc : mutationsCursor){
                    String gene = (String) mutationDoc.get("gene");
                    if(!gene.equals("unknown")){
                        Long varPos = (Long) mutationDoc.get("refPos");
                        String varAlt = (String) mutationDoc.get("alt");
                        String varRef = (String) mutationDoc.get("ref");
                        String chr = (String) mutationDoc.get("chr");
                        boolean hasPeptideEvidence = (boolean) mutationDoc.get("hasPeptideEvidence");
                        Map<String, Map< String, Map<String, Object>>> conditions =
                                (Map<String, Map<String, Map<String, Object>>>) mutationDoc.get("condition");

                        int varAltLength = varAlt.length();
                        int varRefLength = varRef.length();

                        if (!insSelected ) {
                            if ((varRefLength == 0 &&  varAltLength > 0)){
                                continue;
                            }
                        }

                        // filter by deletion
                        if (!delSelected ) {
                            if ((varRefLength > 0 &&  varAltLength == 0)){
                                continue;
                            }
                        }

                        // filter by Snp
                        if (!snpSelected ) {
                            if ((varRefLength == 1 &&  varAltLength == 1)){
                                continue;
                            }
                        }


                        if (!otherSelected) { // other
                            if ((varRefLength > 1 &&  varAltLength == 1) ||
                                    (varRefLength == 1 &&  varAltLength > 1) ||
                                    (varRefLength > 1 &&  varAltLength > 1)
                            ){
                                continue;
                            }
                        }

                        boolean exclude = false;
                        int checkboxCount = 0;
                        if(Config.hasPatients()){


                            for (CheckBox checkBox : conditionFilterCheckboxes) {
                                if (checkBox.isSelected()) {

                                    HashSet<String> intersection = new HashSet<>(conditions.keySet());
                                    intersection.retainAll(Config.getPatientsGroups().get(checkBox.getText()));
                                    if(intersection.size()<minSamplesConditionFilterSliders.get(checkboxCount).getLowValue() ||
                                            intersection.size()>minSamplesConditionFilterSliders.get(checkboxCount).getHighValue()){

                                        exclude=true;
                                        break;
                                    }
                                }
                                checkboxCount++;
                            }

                        }else {
                            for (CheckBox checkBox : conditionFilterCheckboxes) {
                                if (checkBox.isSelected()) {

                                    if ((!conditions.containsKey(checkBox.getText()) && minSamplesConditionFilterSliders.get(checkboxCount).getLowValue() > 0) ||
                                            (conditions.containsKey(checkBox.getText()) &&
                                                    conditions.get(checkBox.getText()).size() < minSamplesConditionFilterSliders.get(checkboxCount).getLowValue()) ||
                                            (conditions.containsKey(checkBox.getText()) &&
                                                    conditions.get(checkBox.getText()).size() > minSamplesConditionFilterSliders.get(checkboxCount).getHighValue())) {
                                        exclude=true;
                                        break;
                                    }
                                }
                                checkboxCount++;
                            }
                        }
                        if(exclude)
                            continue;

                        String type;
                        // count
                        if ((varRefLength == 0 &&  varAltLength > 0)){ // insertion
                            insCount ++;
                            type = "INS";
                        } else if ((varRefLength > 0 &&  varAltLength == 0)){ //  deletion
                            delCount ++;
                            type = "DEL";
                        } else if ((varRefLength == 1 &&  varAltLength == 1)){ //  Snp
                            snpCount ++;
                            type = "SNP";
                        } else  { // other
                            type = "Other";
                            otherCount ++;
                        }

                        Variation tmpVarBasicInfo;
                        if(mutationDoc.containsKey("pfam"))
                            tmpVarBasicInfo = new Variation(gene, chr, varPos.intValue(), varRef, varAlt, hasPeptideEvidence, conditions,
                                (JSONObject) mutationDoc.get("transcripts"), (boolean) mutationDoc.get("inCDS"), (boolean) mutationDoc.get("silent"), type, mutationDoc.get("pfam", JSONArray.class));
                        else
                            tmpVarBasicInfo = new Variation(gene, chr, varPos.intValue(), varRef, varAlt, hasPeptideEvidence, conditions,
                                    (JSONObject) mutationDoc.get("transcripts"), (boolean) mutationDoc.get("inCDS"), (boolean) mutationDoc.get("silent"), type);

                        mutations.add(tmpVarBasicInfo);
                    }
                }





                Platform.runLater(() -> {
                    variantsTable.getItems().clear();
                    variantsTable.getItems().addAll(mutations);
                    //ProVcf.generate("effefe", variantsTable.getItems(), Database.getDb());
                    numberOfElementsInTableLabel.setText(mutations.size() + " ");

                    // set label that's below the table
    //            String percentage =  Integer.toString((int)((double)variationsClone.size() / (double) variations.size() * 100));
    //            String labelString = variationsClone.size() + "/" +  variations.size() + " (" + percentage + "%) ";
    //            numberOfElementsInTableLabel.setText(labelString);

                });

                Platform.runLater(() -> {
                    drawUpsetPlot(mutations);
                });


                // generate hashmap for the graph
                HashMap<String, Integer> mutTypeCountMap = new HashMap<>();

                mutTypeCountMap.put("Ins", insCount);
                mutTypeCountMap.put("Del", delCount);
                mutTypeCountMap.put("SNP", snpCount);
                mutTypeCountMap.put("Other", otherCount);


                // draw graph
                Platform.runLater(() -> {
                    if (mutations.size() > 0 ){
                        drawGraphs(mutTypeCountMap);
                    } else if(mutationsPieChart!=null){
                        mutationsPieChart.getData().clear();
                    }
                });

            }).start();
        }

    }


    /**
     * draw graphs on bottom of the table
     * @param mutTypeCountMap
     */
    public void drawGraphs(HashMap<String, Integer> mutTypeCountMap ){

        ObservableList<DoughnutChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<String, Integer> mutTypeEntry: mutTypeCountMap.entrySet()){
            if(mutTypeEntry.getValue()>0){
                pieChartData.add(new DoughnutChart.Data(mutTypeEntry.getKey(), mutTypeEntry.getValue()));
            }

        }
        pieChartData.forEach(data ->
                data.nameProperty().bind(
                        Bindings.concat(
                                data.pieValueProperty().intValue(), " ", data.getName()
                        )
                )
        );

        //mutationsPieChart.getData().clear();
        if(mutationsPieChart!=null){
            mainGrid.getChildren().remove(mutationsPieChart);
        }
        mutationsPieChart = new DoughnutChart(pieChartData);
        mutationsPieChart.setLegendVisible(false);
        GridPane.setRowSpan(mutationsPieChart, 2);
        mainGrid.add(mutationsPieChart, 2, 0);


        //drawUpsetPlot();


        //mutationsPieChart.setData(pieChartData);


    }


    private void  displayTranscMutInfo(Variation tableMutationBasicInfo){
        ArrayList<Pair<String, Double>> qualArray = new ArrayList<>();
        ArrayList<Pair<String, Double>> afArray = new ArrayList<>();

        for(Map.Entry<String, Map<String, Map<String, Object>>> condition: tableMutationBasicInfo.getConditions().entrySet()){
            for(Map.Entry<String, Map<String, Object>> sample: condition.getValue().entrySet()){
                qualArray.add(new Pair<String, Double>(condition.getKey()+" "+sample.getKey(), (double) sample.getValue().get("qual")));
                afArray.add(new Pair<>(condition.getKey() + " " + sample.getKey(), (double) sample.getValue().get("AF")));
            }
        }

        // sort the lists by condSample String
        qualArray.sort(Comparator.comparing(s -> s.getKey()));
        afArray.sort(Comparator.comparing(s -> s.getKey()));


        // qual barchart
        qualBarChart.getXAxis().setLabel("Sample");
        qualBarChart.getYAxis().setLabel("Qual");

        XYChart.Series qualBarChartSeries = new XYChart.Series();
        for (Pair<String, Double> condSamplQualPair: qualArray ){
            qualBarChartSeries.getData().add(new XYChart.Data(condSamplQualPair.getKey(), condSamplQualPair.getValue()));
        }

        qualBarChart.getData().clear();
        qualBarChart.getData().add(qualBarChartSeries);
        qualBarChart.setLegendVisible(false);

        // af barchart
        afBarChart.getXAxis().setLabel("Sample");
        afBarChart.getYAxis().setLabel("Allele Frequency (AF)");
        afBarChart.setLegendVisible(false);

        NumberAxis yAxis = (NumberAxis) afBarChart.getYAxis();
        yAxis.setAutoRanging(false);
        yAxis.setUpperBound(1);


        XYChart.Series afBarChartSeries = new XYChart.Series();
        for (Pair<String, Double> condSamplAfPair: afArray ){
            afBarChartSeries.getData().add(new XYChart.Data(condSamplAfPair.getKey(), condSamplAfPair.getValue()));
        }

        afBarChart.getData().clear();
        afBarChart.getData().add(afBarChartSeries);


    }

    private void drawSampleFilterSliders(){
        Set<String> conditions = Config.getConditions();

        conditionFilterCheckboxes = new ArrayList<>();
        minSamplesConditionFilterSliders = new ArrayList<>();

        VBox container = new VBox();

        // add chechboxes for the conditions

        HashMap<String, ArrayList<String>> patients = Config.getPatientsGroups();
        if(patients!=null){
            for (Map.Entry<String, ArrayList<String>> entry : patients.entrySet()) {

                JFXCheckBox conditionEnabledCheckbox = new JFXCheckBox();
                conditionEnabledCheckbox.setText(entry.getKey());
                conditionEnabledCheckbox.setSelected(true);

//            conditionEnabledCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> filterTable());


                conditionFilterCheckboxes.add(conditionEnabledCheckbox);

                int nbSamples = entry.getValue().size();
                RangeSlider slider = new RangeSlider();
                slider.setMax(nbSamples);
                slider.setLowValue(0);
                slider.setHighValue(nbSamples);
                slider.setMajorTickUnit(1);
                slider.setShowTickLabels(true);
                slider.setShowTickMarks(true);
                slider.setSnapToTicks(true);


                slider.lowValueProperty().addListener((obs, oldval, newVal) -> slider.setLowValue(Math.round(newVal.doubleValue())));
                slider.highValueProperty().addListener((obs, oldval, newVal) -> slider.setHighValue(Math.round(newVal.doubleValue())));
                conditionEnabledCheckbox.selectedProperty().addListener((obs, oldval, newVal) -> {
                    slider.setDisable(!newVal);
                });

                minSamplesConditionFilterSliders.add(slider);

                container.getChildren().add(conditionEnabledCheckbox);
                container.getChildren().add(slider);


            }
        }else {


            for (String condition : conditions) {

                JFXCheckBox conditionEnabledCheckbox = new JFXCheckBox();
                conditionEnabledCheckbox.setText(condition);
                conditionEnabledCheckbox.setSelected(true);

//            conditionEnabledCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> filterTable());


                conditionFilterCheckboxes.add(conditionEnabledCheckbox);

                int nbSamples = Config.getSamplesInCondition(condition).size();
                RangeSlider slider = new RangeSlider();
                slider.setMax(nbSamples);
                slider.setLowValue(0);
                slider.setHighValue(nbSamples);
                slider.setMajorTickUnit(1);
                slider.setShowTickLabels(true);
                slider.setShowTickMarks(true);
                slider.setSnapToTicks(true);


                slider.lowValueProperty().addListener((obs, oldval, newVal) -> slider.setLowValue(Math.round(newVal.doubleValue())));
                slider.highValueProperty().addListener((obs, oldval, newVal) -> slider.setHighValue(Math.round(newVal.doubleValue())));
                conditionEnabledCheckbox.selectedProperty().addListener((obs, oldval, newVal) -> {
                    slider.setDisable(!newVal);
                });

                minSamplesConditionFilterSliders.add(slider);

                container.getChildren().add(conditionEnabledCheckbox);
                container.getChildren().add(slider);


            }
        }
        upsetSampleSliderBox.getChildren().add(container);

    }

    private void prepareUpsetPlot(ArrayList<Variation> rows){

        StringBuilder sb = new StringBuilder(",");
        Set<String> conditions = parentController.getConfig().getConditions();
        for (Iterator<String> it = conditions.iterator(); it.hasNext(); ) {
            String condition = it.next();
            sb.append(condition);
            if(it.hasNext()){
                sb.append(",");
            }
        }
        sb.append("\n");


        for(Variation row: rows){
            sb.append(row.getChr()).append("_").append(row.getRefPos()).append("_").append(row.getRef()).append("_")
                    .append(row.getAlt()).append(",");
            for (Iterator<String> it = conditions.iterator(); it.hasNext(); ) {
                String condition = it.next();
                int nbSamples;
                if(row.getConditions().containsKey(condition)){
                    nbSamples = row.getConditions().get(condition).size();
                }else{
                    nbSamples = 0;
                }


                sb.append(nbSamples>0?"1":"0");

                if(it.hasNext()){
                    sb.append(",");
                }
            }
            sb.append("\n");
        }
        try {

            PrintWriter out = new PrintWriter("Rscripts/variantsTable.csv");
            out.println(sb);
            out.close();


            VBox lastNode = (VBox) upsetSampleSliderBox.getChildren().get(0);
            double height = upsetSampleSliderBox.getHeight() - lastNode.getHeight();

            String ptr;
            ptr = Settings.getInstance().getPathToR();

            ProcessBuilder pb = new ProcessBuilder(ptr, "Rscripts/upset.R", "Rscripts/variantsTable.csv",
                    "plots/upset.jpeg", String.valueOf(upsetSampleSliderBox.getWidth()), String.valueOf(height - 10));
            pb.redirectErrorStream(true);
            Process process = pb.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawUpsetPlot(ArrayList<Variation> rows){
        prepareUpsetPlot(rows);
        Platform.runLater(() -> {
            ImageView upsetViewer = new ImageView();
            upsetViewer.setImage(new Image("file:plots/upset.jpeg"));

            if(upsetSampleSliderBox.getChildren().get(upsetSampleSliderBox.getChildren().size()-1).getClass()==ImageView.class){
                upsetSampleSliderBox.getChildren().remove(upsetSampleSliderBox.getChildren().size()-1);
            }

            upsetSampleSliderBox.getChildren().add(upsetViewer);
        });


    }

    public void resize(){

    }

    public ObservableList<Variation> getSelectedMutations(){
        return variantsTable.getItems();
    }

}