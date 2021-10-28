package Controllers;

import Cds.*;
import Singletons.Database;
import TablesModels.PeptideSampleModel;
import com.jfoenix.controls.JFXComboBox;
import graphics.AnchorFitter;
import graphics.ConfidentBarChart;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Pair;
import netscape.javascript.JSObject;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.filters.Filters;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import Singletons.Config;
import utilities.MSRun;
import utilities.MassSpecModificationSample;
import utilities.MassSpecSample;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.dizitart.no2.filters.Filters.*;

public class PeptideTableController implements Initializable {

    @FXML
    private AnchorPane intensitiesChartContainer;
    @FXML
    private HBox chartsBox;
    @FXML
    private GridPane peptideDetailsGrid;
    @FXML
    private TableView<PeptideSampleModel> peptideSampleTable;
    @FXML
    private TableView<PSM> psmTable;
    @FXML
    private TableColumn<PSM, Double> psmProbabilityColumn;
    @FXML
    private TableColumn<PSM, String> psmFileColumn;
    @FXML
    private TableColumn<PSM, Integer> psmIndexColumn;
    @FXML
    private TableView<PTM> suggestedPTMFilterTable;
    @FXML
    private TableView<PTM> ptmFilterTable;
    @FXML
    private TableColumn<PTM, String> suggestedPTMNameColumn;
    @FXML
    private TableColumn<PTM, Double> suggestedPTMMassShiftColumn;
    @FXML
    private TableColumn<PTM, String> PTMNameFilterColumn;
    @FXML
    private TableColumn<PTM, Double> PTMMassShiftFilterColumn;
    @FXML
    private TableColumn<Peptide, Integer> nbGenesColumn;
    @FXML
    private TableColumn<Peptide, Double> foldChangeColumn;
    @FXML
    private WebView specWebview;
    @FXML
    private AnchorPane spectrumViewer;

    @FXML
    private ComboBox<String> condACombobox;
    @FXML
    private ComboBox<String> condBCombobox;

    @FXML
    private TableColumn<PeptideSampleModel, String> peptideSampleTableSampleColumn;
    @FXML
    private TableColumn<PeptideSampleModel, Double> peptideSampleTableProbabilityColumn;
    @FXML
    private TableView<MassSpecModificationSample> modificationsTable;
    @FXML
    private TableView<Peptide> peptideTable;
    @FXML
    private TableColumn<Peptide, String> peptideColumn;
    @FXML
    private JFXComboBox<String> runCombobox;
    @FXML
    private WebView webview;
    @FXML
    private SpectrumViewerController spectrumViewerController;

    private ResultsController parentController;
    private ArrayList<Peptide> allPeptides;
    private Peptide selectedPeptide;
    private Peptide peptideToFind;
    private MSRun selectedRun;

    public TextField geneSymbolSearchBox;
    public TextField geneNumberSearchBox;
    public TextField sequenceSearchBox;
    public TextField foldChangeSearchBox;
    List<Peptide> resultMOD = new ArrayList<Peptide>();

    @FXML
    private Label numberOfGenesInPeptideTableLabel;

    private static PeptideTableController instance;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        instance = this;

        peptideColumn.setCellValueFactory( new PropertyValueFactory<>("sequence"));
        peptideColumn.prefWidthProperty().bind(peptideTable.widthProperty().multiply(0.6));
        nbGenesColumn.setCellValueFactory(new PropertyValueFactory<>("nbGenes"));
        nbGenesColumn.prefWidthProperty().bind(peptideTable.widthProperty().multiply(0.15));
        foldChangeColumn.setCellValueFactory(new PropertyValueFactory<>("foldChange"));
        foldChangeColumn.prefWidthProperty().bind(peptideTable.widthProperty().multiply(0.25));

        peptideSampleTableSampleColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getSample()));
        peptideSampleTableProbabilityColumn.setCellValueFactory(cellData ->
                new ReadOnlyDoubleWrapper(cellData.getValue().getProbability()).asObject());

        peptideSampleTableSampleColumn.prefWidthProperty().bind(peptideSampleTable.widthProperty().divide(2));
        peptideSampleTableProbabilityColumn.prefWidthProperty().bind(peptideSampleTable.widthProperty().divide(2));


        psmProbabilityColumn.setCellValueFactory(new PropertyValueFactory<>("prob"));
        psmIndexColumn.setCellValueFactory(new PropertyValueFactory<>("specIndex"));
        psmFileColumn.setCellValueFactory(new PropertyValueFactory<>("file"));

        psmProbabilityColumn.prefWidthProperty().bind(psmTable.widthProperty().multiply(0.2));
        psmIndexColumn.prefWidthProperty().bind(psmTable.widthProperty().multiply(0.2));
        psmFileColumn.prefWidthProperty().bind(psmTable.widthProperty().multiply(0.6));

        peptideSampleTableProbabilityColumn.setCellFactory(tc -> new TableCell<>() {

            @Override
            protected void updateItem(Double probability, boolean empty) {
                super.updateItem(probability, empty);
                if (empty) {
                    setText(null);
                } else {
                    DecimalFormat df = new DecimalFormat("0.00##");
                    setText(df.format(probability));
                }
            }
        });

        peptideSampleTable.setRowFactory(tv -> {
            TableRow<PeptideSampleModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!(row.isEmpty())) {
                    if (event.getButton().equals(MouseButton.PRIMARY)){
                        Peptide peptide = peptideTable.getSelectionModel().getSelectedItem();

                        drawIntensitiesChart(selectedPeptide.getIntensities(row.getItem().getSample()));

                        selectPeptideRun(peptide, row.getItem().getSample());

                    }

                }
            });
            return row;
        });



        peptideTable.setRowFactory(tv -> {
            TableRow<Peptide> row = new TableRow<>();
            row.setOnMouseClicked(event -> {

                modificationsTable.getItems().clear();
                psmTable.getItems().clear();
                spectrumViewerController.clear();

                if (!(row.isEmpty())) {
                    if (event.getButton().equals(MouseButton.PRIMARY)){
                        Peptide peptide = peptideTable.getSelectionModel().getSelectedItem();

                        if(!Config.isCombinedRun(runCombobox.getSelectionModel().getSelectedItem())){
                            drawIntensitiesChart(selectedRun.getIntensities(peptide.getSequence()));
                        }

                        selectPeptide(peptide);

                    }

                }
            });
            return row;
        });


        modificationsTable.setRowFactory(tv -> {
            TableRow<MassSpecModificationSample> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!(row.isEmpty())) {
                    if (event.getButton().equals(MouseButton.PRIMARY)){
                        MassSpecModificationSample rowData = modificationsTable.getSelectionModel().getSelectedItem();

                        psmTable.getItems().clear();
                        for(PSM psm: rowData.getPsms()){
                            psmTable.getItems().add(psm);
                        }

                        drawIntensitiesChart(selectedRun.getIntensities(peptideTable.getSelectionModel()
                                .getSelectedItem().getSequence(), rowData.getPtms()));

                    }

                }
            });
            return row;
        });


        psmTable.setRowFactory(tv -> {
            TableRow<PSM> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!(row.isEmpty())) {
                    if (event.getButton().equals(MouseButton.PRIMARY)){

                        drawIntensitiesChart(row.getItem().getIntensities());

                        spectrumViewerController.setConfig(parentController.getConfig(), specWebview);
                        spectrumViewerController.select(psmTable.getSelectionModel().getSelectedItem()
                                , selectedRun, selectedPeptide.getSequence());
                    }

                }
            });
            return row;
        });


        //Filters

        suggestedPTMFilterTable.setRowFactory( tv -> {
            TableRow<PTM> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    PTM rowData = row.getItem();
                    suggestedPTMFilterTable.getItems().remove(rowData);
                    ptmFilterTable.getItems().add(rowData);
                }
            });
            return row ;
        });

        ptmFilterTable.setRowFactory( tv -> {
            TableRow<PTM> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    PTM rowData = row.getItem();
                    ptmFilterTable.getItems().remove(rowData);
                    suggestedPTMFilterTable.getItems().add(rowData);
                }
            });
            return row ;
        });

        suggestedPTMNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        suggestedPTMMassShiftColumn.setCellValueFactory(new PropertyValueFactory<>("massShift"));

        PTMNameFilterColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        PTMMassShiftFilterColumn.setCellValueFactory(new PropertyValueFactory<>("massShift"));
        populateSuggestedPtms();
        numberOfGenesInPeptideTableLabel.setText(peptideTable.getItems().size() + " ");
    }

    public static PeptideTableController getInstance(){return instance;}


    public void setParentController(ResultsController parentController){
        this.parentController = parentController;
        spectrumViewerController.setConfig(parentController.getConfig(), specWebview);


        for ( String run : Config.getRuns()){
            runCombobox.getItems().add(run);
        }

        runCombobox.getSelectionModel().select(0);
        loadRun();

    }

    @FXML
    public void loadRun(){
        loadRun(null);
    }


    @FXML
    public void loadRun(String peptideSeq){

        System.out.println(runCombobox.getSelectionModel().getSelectedItem()+", "+peptideSeq);

        selectedRun = new MSRun(runCombobox.getSelectionModel().getSelectedItem(), Config.getOutputPath());


        new Thread(() -> {

            HashSet<String> conditions = new HashSet<>();
            for ( String subrun : Config.getSubRuns(selectedRun.getName())){
                for(String sample: Config.getRunSamples(subrun))
                    conditions.add(sample.split("/")[0]);

            }

            Iterator<String> condIterator = conditions.iterator();

            condACombobox.getItems().addAll(conditions);
            condBCombobox.getItems().addAll(conditions);




            if(conditions.size()>2) {
                Platform.runLater(() -> {
                    condACombobox.getSelectionModel().select(0);
                    condBCombobox.getSelectionModel().select(1);
                });

                selectedRun.load(Database.getDb(), Config.getOutputPath(), runCombobox.getSelectionModel().getSelectedItem(),
                        this, peptideToFind, condIterator.next(), condIterator.next());
            }else
                selectedRun.load(Database.getDb(), Config.getOutputPath(), runCombobox.getSelectionModel().getSelectedItem(),
                        this, peptideToFind, null, null);


            Platform.runLater(() -> {
                peptideTable.getItems().clear();
                peptideTable.getItems().addAll(selectedRun.getAllPeptides());
                allPeptides = new ArrayList<>(selectedRun.getAllPeptides());



                if(peptideSeq!=null){
                    int i = 0;

                    for(Peptide peptide: selectedRun.getAllPeptides()){


                        if(peptide.getSequence().equals(peptideSeq)){
                            int finalI = i;


                                peptideTable.requestFocus();
                                peptideTable.getSelectionModel().select(finalI);
                                peptideTable.getFocusModel().focus(finalI);
                                peptideTable.scrollTo(finalI);
                                selectPeptide(peptide);

        }).start();
    }

    public void findPeptideInTable(String peptideSeq, String run){

        if(selectedRun.getName().equals(run)){
            int i = 0;
            for(Peptide peptide: peptideTable.getItems()){
                if(peptide.getSequence().equals(peptideSeq)){
                    int finalI = i;
                    Platform.runLater(() -> {
                        peptideTable.requestFocus();
                        peptideTable.getSelectionModel().select(finalI);
                        peptideTable.getFocusModel().focus(finalI);
                        peptideTable.scrollTo(finalI);
                        selectPeptide(peptide);
                    });

                            break;
                        }
                        i++;
                    }
                }
            });


        }).start();



    }

    public void findPeptideInTable(String peptideSeq, String run){


        runCombobox.getSelectionModel().select(run);

        loadRun(peptideSeq);


    }

    public void selectPeptide(Peptide peptide){

        peptideToFind=null;
        showMap(peptide.getSequence());

        if(peptideSampleTable.getColumns().size()==2){
            if(!parentController.getConfig().hasQuantification(runCombobox.getSelectionModel().getSelectedItem())){
                TableColumn<MassSpecSample, Integer> spectralCountColumn = new TableColumn<>("Spectral count");
                spectralCountColumn.setCellValueFactory(cellData ->
                        new ReadOnlyIntegerWrapper(cellData.getValue().getSpectralCount()).asObject());
                //peptideSampleTable.getColumns().add(spectralCountColumn);
                GridPane.setRowSpan(spectrumViewer, 3);
                GridPane.setRowSpan(intensitiesChartContainer, 0);

            }else{


                GridPane.setRowSpan(spectrumViewer, 2);
                GridPane.setRowSpan(intensitiesChartContainer, 1);

            }
        }

        peptideSampleTable.getItems().clear();
        modificationsTable.getItems().clear();
        psmTable.getItems().clear();
        spectrumViewerController.clear();
        chartsBox.getChildren().clear();


        for(String run: peptide.getRuns()){
            peptideSampleTable.getItems().add(new PeptideSampleModel(run, peptide.getProbability()));
        }

        selectedPeptide = peptide;

    }


    public void selectPeptideRun(Peptide peptide, String run){

        HashMap<HashSet<PTM>, MassSpecModificationSample> ptmSamples = new HashMap<>();

        for (PSM psm: peptide.getPsms(run)){
            if (ptmSamples.containsKey(psm.getModifications())) {
                ptmSamples.get(psm.getModifications()).addPSM(psm);
            }else{
                MassSpecModificationSample modificationsSample = new MassSpecModificationSample(psm.getModifications());
                modificationsSample.addPSM(psm);
                ptmSamples.put(psm.getModifications(), modificationsSample);
            }
        }

        modificationsTable.getItems().clear();

        modificationsTable.getColumns().clear();

        TableColumn<MassSpecModificationSample, String> modificationsColumn = new TableColumn<>("Modifications");
        modificationsColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().ptmsToString()));
        modificationsTable.getColumns().add(modificationsColumn);

        TableColumn<MassSpecModificationSample, String> ptmProbabilityColumn = new TableColumn<>("Probability");
        ptmProbabilityColumn.setCellValueFactory(new PropertyValueFactory<>("probability"));
        modificationsTable.getColumns().add(ptmProbabilityColumn);

        Set<String> runSamples = selectedRun.getChannels();

        modificationsColumn.prefWidthProperty().bind(modificationsTable.widthProperty().multiply(0.8));
        ptmProbabilityColumn.prefWidthProperty().bind(modificationsTable.widthProperty().multiply(0.2));


        modificationsTable.getItems().addAll(ptmSamples.values());

    }

    public void showMap(String peptide){


        webview.setPrefWidth(peptideTable.getWidth()*0.8);


        WebEngine webEngine = webview.getEngine();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String command = getNodes(peptide);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if(webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED){

                            webEngine.executeScript(getNodes(peptide));
                        }else{
                            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                                if (newState == Worker.State.SUCCEEDED) {
                                    try {

                                        webEngine.executeScript(new String(Files.readAllBytes(Paths.get(getClass().getResource("/javascript/vis.js").toURI()))));
                                        webEngine.executeScript(new String(Files.readAllBytes(Paths.get(getClass().getResource("/javascript/myNetwork.js").toURI()))));

                                        JSObject jsobj = (JSObject) webEngine.executeScript("window");

                                        jsobj.setMember("java", new SelectNodeBridge());


                                        webEngine.executeScript(command);
                                    } catch (IOException | URISyntaxException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                            webview.getEngine().load(getClass().getResource("/html/mynetwork.html").toString());
                        }
                    }

                });
            }
        }).start();


    }



    private String getNodes(String peptide){




        ArrayList<String> nodes = new ArrayList<>();
        ArrayList<Pair<Integer, Integer>> links = new ArrayList<>();

        ArrayList<String> nodesJson = new ArrayList<>();
        ArrayList<String> linksJson = new ArrayList<>();

        ArrayList<String> genes = new ArrayList<>();


        nodes.add(peptide);
        nodesJson.add("{id:" + (nodes.size() - 1) + ", label: \"" +
                nodes.get(nodes.size() - 1) + "\", shape: \"ellipse\", color: \"#BF9D7A\"}");


        getPeptideGenes(nodesJson,  nodes,  links, genes,  peptide, true);



        NitriteCollection collection = Database.getDb().getCollection("genePeptides");

        Document doc = collection.find(and(eq("run", runCombobox.getSelectionModel().getSelectedItem()),
                in("gene", genes.toArray(new Object[0])))).firstOrDefault();


        JSONObject peptides = doc.get("peptides", JSONObject.class);

        String gene = (String) doc.get("gene");

        if(!gene.equals("unknown")){
            for (String peptideSequence : (Iterable<String>) peptides.keySet()) {
                JSONObject peptideObj = (JSONObject) peptides.get(peptideSequence);



                if (!nodes.contains(peptideSequence)) {
                    nodes.add(peptideSequence);
                    nodesJson.add("{id:" + (nodes.size() - 1) + ", label: \"" +
                            nodes.get(nodes.size() - 1) + "\", shape: \"ellipse\", size: 12, color: \"#80ADD7\"}");
                    getPeptideGenes(nodesJson,  nodes,  links, genes,  peptideSequence, false);

                }

                Pair<Integer, Integer> pepPair = new Pair<>(nodes.indexOf(peptideSequence), nodes.indexOf(gene));
                if (!links.contains(pepPair)) {
                    links.add(pepPair);
                }


            }
        }

        StringBuilder nodesStr = new StringBuilder("[");
        StringBuilder linksStr = new StringBuilder("[");

        for (int i = 0; i < nodesJson.size(); i++) {


            if(i<nodes.size()-1){
                nodesStr.append(nodesJson.get(i)).append(",");
            }else{
                nodesStr.append(nodesJson.get(i)).append("]");
            }
        }


        for (int i = 0; i < links.size(); i++) {

            Pair<Integer, Integer> pair = links.get(i);

            if(i<links.size()-1){
                linksStr.append("{from:").append(pair.getKey()).append(", to:  ").append(pair.getValue()).append("},");
            }else{
                linksStr.append("{from:").append(pair.getKey()).append(", to: ").append(pair.getValue()).append("}]");
            }

        }

        System.out.println("genNetwork(" + nodesStr.toString() + ", " + linksStr.toString() + "," + webview.getHeight() + ")");
        return "genNetwork(" + nodesStr.toString() + ", " + linksStr.toString() + "," + webview.getHeight() + ")";

    }

    private void getPeptideGenes(ArrayList<String> nodesJson, ArrayList<String> nodes, ArrayList<Pair<Integer, Integer>> links,
                                 ArrayList<String> genesFound, String peptide, boolean addTranscripts){
        NitriteCollection collection = Database.getDb().getCollection("peptideMap");
        Document doc = collection.find(and(eq("run", runCombobox.getSelectionModel().getSelectedItem()), eq("peptide", peptide)))
                .firstOrDefault();

        JSONObject genesTranscripts = doc.get("transcripts", JSONObject.class);
        for (String gene : (Iterable<String>) genesTranscripts.keySet()) {
            JSONArray transcripts = (JSONArray) genesTranscripts.get(gene);


            if (!nodes.contains(gene)) {
                nodes.add(gene);
                genesFound.add(gene);
                nodesJson.add("{id:" + (nodes.size() - 1) + ", label: \"" +
                        nodes.get(nodes.size() - 1) + "\", shape: \"triangle\", color: \"#D4DCA9\"}");
            }

            Pair<Integer, Integer> pepPair = new Pair<>(nodes.indexOf(peptide), nodes.indexOf(gene));
            if (!links.contains(pepPair)) {
                links.add(pepPair);
            }

            if(addTranscripts){
                for (Object o : transcripts) {
                    String transcript = (String) o;
                    if (!nodes.contains(transcript)) {
                        nodes.add(transcript);
                        nodesJson.add("{id:" + (nodes.size() - 1) + ", label: \"" +
                                nodes.get(nodes.size() - 1) + "\", shape: \"hexagon\", color:\"#0ABDA0\"}");
                    }

                    Pair<Integer, Integer> pair = new Pair<>(nodes.indexOf(gene), nodes.indexOf(transcript));
                    if (!links.contains(pair)) {
                        links.add(pair);
                    }
                }
            }


        }

    }

    public class SelectNodeBridge {
        public void callbackFromJavaScript(String msg) {
            chartsBox.getChildren().clear();

            org.json.JSONObject obj = new org.json.JSONObject(msg);

            switch (obj.getString("shape")) {
                case "ellipse":
                    drawPeptideChart(obj.getString("label"));
                    break;
                case "triangle":
                    drawSelectedGeneReadCount(obj.getString("label"));
                    drawSelectedGeneProteinQuant(obj.getString("label"));
                    break;
                case "hexagon":
                    drawTranscriptAbundance(obj.getString("label"));
                    break;
            }
        }

//        public void browseGene(String msg){
//            org.json.JSONObject obj = new org.json.JSONObject(msg);
//            if(obj.getString("shape").equals("triangle")){
//                parentController.showGeneBrowser(obj.getString("label"), selectedPeptide);
//            }
//        }

    }


    private void drawPeptideChart(String peptide){




        if(parentController.getConfig().hasQuantification(runCombobox.getSelectionModel().getSelectedItem())){
            NitriteCollection collection = Database.getDb().getCollection("peptideQuant");
            Document doc = collection.find(Filters.eq("peptide", peptide)).firstOrDefault();

            final CategoryAxis xAxisbarChart = new CategoryAxis();
            final NumberAxis yAxisbarChart = new NumberAxis();
            yAxisbarChart.setLabel("Protein abundance");
            BarChart<String, Number> barChart =
                    new BarChart<>(xAxisbarChart, yAxisbarChart);
            barChart.setTitle("Differential protein abundance");


            ArrayList<XYChart.Series> allSeries = new ArrayList<>();

            org.json.JSONObject res = (org.json.JSONObject) doc.get("abundance");



            ConfidentBarChart confidentBarChart = new ConfidentBarChart();

            for (String conditionKey : res.keySet()) {

                ArrayList<Double> intensities = new ArrayList<>();


                org.json.JSONObject condition = res.getJSONObject(conditionKey);
                int i = 0;
                for (String sampleKey : condition.keySet()) {
                    if (i + 1 > intensities.size()) {
                        allSeries.add(new XYChart.Series());
                    }

                    intensities.add(condition.getDouble(sampleKey));
                    allSeries.get(i).getData().add(new XYChart.Data(conditionKey, condition.getInt(sampleKey)));
                    i++;
                }
                confidentBarChart.addSeries(conditionKey, intensities);
            }

            final MenuItem saveImageItem = new MenuItem("Save plot");
            PlotSaver plotSaver = new PlotSaver("barchart");
            saveImageItem.setOnAction(event -> {
                plotSaver.setBarchartData(allSeries, (Stage) confidentBarChart.getScene().getWindow());
            });
            final MenuItem saveDataItem = new MenuItem("Save data");
            saveDataItem.setOnAction(event -> {
                plotSaver.saveBarchartData(allSeries, "Condition", "Intensity",
                        (Stage) confidentBarChart.getScene().getWindow());
            });

            final ContextMenu menu = new ContextMenu(
                    saveImageItem,
                    saveDataItem
            );

            confidentBarChart.setOnMouseClicked(event -> {
                if (MouseButton.SECONDARY.equals(event.getButton())) {
                    menu.show(confidentBarChart.getScene().getWindow(), event.getScreenX(), event.getScreenY());
                }
            });

            confidentBarChart.draw();
            HBox.setHgrow(barChart, Priority.ALWAYS);
            chartsBox.getChildren().add(confidentBarChart);
        }




    }

    private void drawSelectedGeneReadCount(String gene){

        NitriteCollection collection = Database.getDb().getCollection("readCounts");
        Cursor documents = collection.find(Filters.eq("gene", gene));

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Normalised read counts");
        BarChart<String,Number> bc =
                new BarChart<>(xAxis, yAxis);
        bc.setTitle("Differential gene expression");
//        bc.setStyle("-fx-font-size: " + fontSize + "px;");

        ArrayList<XYChart.Series> allSeries = new ArrayList<>();

        for(Document result: documents){
            org.json.JSONObject res = new org.json.JSONObject(result).getJSONObject("counts");
            for (String conditionKey : res.keySet()) {

                org.json.JSONObject condition = res.getJSONObject(conditionKey);
                int i = 0;
                for (String sampleKey : condition.keySet()) {
                    if (i + 1 > allSeries.size()) {
                        allSeries.add(new XYChart.Series());
                    }
                    allSeries.get(i).getData().add(new XYChart.Data(conditionKey, condition.getInt(sampleKey)));
                    i++;
                }
            }
        }
        for(XYChart.Series series: allSeries){
            bc.getData().add(series);
        }
        HBox.setHgrow(bc, Priority.ALWAYS);
        chartsBox.getChildren().add(bc);


    }


    private void drawSelectedGeneProteinQuant(String gene){

        NitriteCollection collection = Database.getDb().getCollection("proteinQuant");
        Cursor documents = collection.find(Filters.eq("gene", gene));

        final CategoryAxis xAxisbarChart = new CategoryAxis();
        final NumberAxis yAxisbarChart = new NumberAxis();
        yAxisbarChart.setLabel("Protein abundance");
        BarChart<String,Number> barChart =
                new BarChart<>(xAxisbarChart,yAxisbarChart);
        barChart.setTitle("Differential protein abundance");


        final CategoryAxis xAxisLineChart = new CategoryAxis();
        final NumberAxis yAxisLineChart = new NumberAxis();
        yAxisLineChart.setLabel("Log2 PSM intensity");
        LineChart<String,Number> lineChart =
                new LineChart<>(xAxisLineChart,yAxisLineChart);
        lineChart.setTitle("Differential peptide intensity");




        ArrayList<XYChart.Series> allSeriesAbundance = new ArrayList<>();
        ArrayList<XYChart.Series> allPeptidesSeries = new ArrayList<>();


        for(Document result: documents){
            org.json.JSONObject res = new org.json.JSONObject(result).getJSONObject("abundance");
            for (String conditionKey : res.keySet()) {
                org.json.JSONObject condition = res.getJSONObject(conditionKey);
                int i = 0;
                for (String sampleKey : condition.keySet()) {
                    if (i + 1 > allSeriesAbundance.size()) {
                        allSeriesAbundance.add(new XYChart.Series());
                    }
                    allSeriesAbundance.get(i).getData().add(new XYChart.Data(conditionKey, condition.getDouble(sampleKey)));
                    i++;
                }
            }



            res = new org.json.JSONObject(result).getJSONObject("peptides");
            for (String PSM : res.keySet()) {

                XYChart.Series peptideSeries = new XYChart.Series();
                peptideSeries.setName(PSM);
                allPeptidesSeries.add(peptideSeries);

                org.json.JSONObject peptideObj = res.getJSONObject(PSM);

                for (String conditionKey : peptideObj.keySet()) {
                    org.json.JSONObject condition = peptideObj.getJSONObject(conditionKey);
                    for (String sampleKey : condition.keySet()) {
                        peptideSeries.getData().add(new XYChart.Data(conditionKey+" "+sampleKey, Math.log10(condition.getInt(sampleKey))/Math.log10(2)));
                    }
                }
            }

        }
        for(XYChart.Series series: allSeriesAbundance){
            barChart.getData().add(series);
        }
        HBox.setHgrow(barChart, Priority.ALWAYS);
        chartsBox.getChildren().add(barChart);


        for(XYChart.Series series: allPeptidesSeries){
            lineChart.getData().add(series);
        }
        HBox.setHgrow(lineChart, Priority.ALWAYS);
        chartsBox.getChildren().add(lineChart);


    }

    private void drawTranscriptAbundance(String transcriptID){

        final CategoryAxis xAxisbarChart = new CategoryAxis();
        final NumberAxis yAxisbarChart = new NumberAxis();
        yAxisbarChart.setLabel("TPM");
        BarChart<String,Number> barChart =
                new BarChart<>(xAxisbarChart,yAxisbarChart);
        barChart.setTitle("Transcript Abundance");


        NitriteCollection collection = Database.getDb().getCollection("allTranscripts");
        Document doc = collection.find(Filters.eq("transcriptID", transcriptID)).firstOrDefault();

        HashMap<String, HashMap<String, Double>> tpm = (HashMap<String, HashMap<String, Double>>) doc.get("TPM");

        ArrayList<XYChart.Series> allSeriesAbundance = new ArrayList<>();


        for(Map.Entry<String, HashMap<String, Double>> condition: tpm.entrySet()){

            int i = 0;
            for(Map.Entry<String, Double> sample: condition.getValue().entrySet()){
                if (i + 1 > allSeriesAbundance.size()) {
                    allSeriesAbundance.add(new XYChart.Series());
                }
                allSeriesAbundance.get(i).getData().add(new XYChart.Data(condition.getKey(), sample.getValue()));
                i++;
            }
        }

        for(XYChart.Series series: allSeriesAbundance){
            barChart.getData().add(series);
        }
        HBox.setHgrow(barChart, Priority.ALWAYS);
        chartsBox.getChildren().add(barChart);

    }

    private void populateSuggestedPtms(){
        suggestedPTMFilterTable.getItems().add(new MaxQuantPTM( "M","Oxidation (M)"));
        suggestedPTMFilterTable.getItems().add(new PTM("Protein N-term","Acetyl (Protein N-term)"));

    }

    public void filter() {
        Iterator<Peptide> it = allPeptides.iterator();
        peptideTable.getItems().clear();
        //populate table with allPeptides if no mod filters chosen
        if (ptmFilterTable.getItems().isEmpty()) {
            Iterator<Peptide> it1 = allPeptides.iterator();
            peptideTable.getItems().clear();
            while (it1.hasNext()) {
                Peptide peptide = it1.next();
                peptideTable.getItems().add(peptide);
            }
        }
        //filter the table if mods are chosen
        while (it.hasNext()) {
            Peptide peptide = it.next();
            for (PTM ptm : ptmFilterTable.getItems()) {
                if (peptide.contains(ptm)) {
                    peptideTable.getItems().add(peptide);
                    resultMOD.add(peptide);
                    break;
                }
            }
        }
        numberOfGenesInPeptideTableLabel.setText(peptideTable.getItems().size() + " ");
    }

    @FXML
    public void resetModFilter() {
        Iterator<Peptide> it1 = allPeptides.iterator();
        peptideTable.getItems().clear();
        while (it1.hasNext()) {
            Peptide peptide = it1.next();
            peptideTable.getItems().add(peptide);
        }
        numberOfGenesInPeptideTableLabel.setText(peptideTable.getItems().size() + " ");
        ptmFilterTable.getItems().clear();
        suggestedPTMFilterTable.getItems().clear();
        populateSuggestedPtms();
    }

    public void resetSecondFilter() {
        geneSymbolSearchBox.clear();
        geneNumberSearchBox.clear();
        sequenceSearchBox.clear();
        foldChangeSearchBox.clear();

        Iterator<Peptide> it1 = allPeptides.iterator();
        peptideTable.getItems().clear();
        while (it1.hasNext()) {
            Peptide peptide = it1.next();
            peptideTable.getItems().add(peptide);
        }
        numberOfGenesInPeptideTableLabel.setText(peptideTable.getItems().size() + " ");
    }

    public void secondFilter() {

        Iterator<Peptide> it2 = allPeptides.iterator();
        peptideTable.getItems().clear();
        while (it2.hasNext()) {
            Peptide peptide = it2.next();
            peptideTable.getItems().add(peptide);
        }

        String geneSymbolFilter="";
        Integer numberOfGenesFilter = 0;
        String sequenceFilter="";
        double foldChangeFilter=0;

        if(geneSymbolSearchBox.getText().length()>0) {
            geneSymbolFilter = geneSymbolSearchBox.getText().strip().toUpperCase();
        }
        if (geneNumberSearchBox.getText().length() > 0) {
            numberOfGenesFilter = Integer.parseInt(geneNumberSearchBox.getText());
        }
        if(sequenceSearchBox.getText().length()>0) {
            sequenceFilter = sequenceSearchBox.getText().strip().toUpperCase();
        }
        if(foldChangeSearchBox.getText().length()>0) {
            foldChangeFilter = Double.parseDouble(foldChangeSearchBox.getText());
        }

        String finalGSFilter = geneSymbolFilter;
        Integer finalGNFilter = numberOfGenesFilter;
        String finalSEQfilter = sequenceFilter;
        Double finalFCfilter = foldChangeFilter;
        Predicate<Peptide> isGeneSymbol = e -> e.getGenes().contains(finalGSFilter);
        Predicate<Peptide> isGeneNumber = e -> e.getGenes().size() == finalGNFilter;
        Predicate<Peptide> isSequenceEqual = e -> e.getSequence().contains(finalSEQfilter);
        Predicate<Peptide> isFCbiggerThan = e -> e.getFoldChange() > (finalFCfilter);

        List<Peptide> resultGS = new ArrayList<Peptide>();
        List<Peptide> resultGN = new ArrayList<Peptide>();
        List<Peptide> resultSEQ = new ArrayList<Peptide>();

        //IF TABLE WAS FILTERED FOR MODS
        //GENE SYMBOL FILTER
        if(ptmFilterTable.getItems().size()>0) {
            //GENE SYMBOL
            if (geneSymbolFilter.length() > 0) {
                peptideTable.getItems().clear();
                List<Peptide> result = resultMOD.stream()
                        .filter(isGeneSymbol)
                        .collect(Collectors.toList());
                Iterator<Peptide> it = result.iterator();
                while (it.hasNext()) {
                    Peptide peptide = it.next();
                    peptideTable.getItems().add(peptide);
                    resultGS.add(peptide);
                }
            }

            //GENE NUMBER
            if (geneNumberSearchBox.getText().length() > 0) {
                peptideTable.getItems().clear();
                List<Peptide> result2;
                if (geneSymbolSearchBox.getText().length()>0) {
                    result2 = resultGS.stream()
                            .filter(isGeneNumber)
                            .collect(Collectors.toList());
                    Iterator<Peptide> it = result2.iterator();
                    while (it.hasNext()) {
                        Peptide peptide = it.next();
                        peptideTable.getItems().add(peptide);
                        resultGN.add(peptide);
                    }
                } else {
                    result2 = resultMOD.stream()
                            .filter(isGeneNumber)
                            .collect(Collectors.toList());
                    Iterator<Peptide> it = result2.iterator();
                    while (it.hasNext()) {
                        Peptide peptide = it.next();
                        peptideTable.getItems().add(peptide);
                        resultGN.add(peptide);
                    }
                }

            }

            //PEPTIDE SEQUENCE
            if (sequenceSearchBox.getText().length()>0) {
                if (geneNumberSearchBox.getText().length()>0) {
                    peptideTable.getItems().clear();
                    List<Peptide> result2 = resultGN.stream()
                            .filter(isSequenceEqual)
                            .collect(Collectors.toList());
                    Iterator<Peptide> it = result2.iterator();
                    while (it.hasNext()) {
                        Peptide peptide = it.next();
                        peptideTable.getItems().add(peptide);
                        resultSEQ.add(peptide);
                    }

                } else if (geneSymbolSearchBox.getText().length()>0) {
                    peptideTable.getItems().clear();
                    List<Peptide> result2 = resultGS.stream()
                            .filter(isSequenceEqual)
                            .collect(Collectors.toList());
                    Iterator<Peptide> it = result2.iterator();
                    while (it.hasNext()) {
                        Peptide peptide = it.next();
                        peptideTable.getItems().add(peptide);
                        resultSEQ.add(peptide);
                    }

                } else {
                    peptideTable.getItems().clear();
                    List<Peptide> result2 = resultMOD.stream()
                            .filter(isSequenceEqual)
                            .collect(Collectors.toList());
                    Iterator<Peptide> it = result2.iterator();
                    while (it.hasNext()) {
                        Peptide peptide = it.next();
                        peptideTable.getItems().add(peptide);
                        resultSEQ.add(peptide);
                    }

                }
            }
            //FOLD CHANGE FILTER
            if (foldChangeSearchBox.getText().length()>0) {
                if (sequenceSearchBox.getText().length()>0) {
                    peptideTable.getItems().clear();
                    List<Peptide> resultFold = resultSEQ.stream()
                            .filter(isFCbiggerThan)
                            .collect(Collectors.toList());
                    Iterator<Peptide> it = resultFold.iterator();
                    while (it.hasNext()) {
                        Peptide peptide = it.next();
                        peptideTable.getItems().add(peptide);
                    }
                } else if (geneNumberSearchBox.getText().length()>0) {
                    peptideTable.getItems().clear();
                    List<Peptide> resultFold = resultGN.stream()
                            .filter(isFCbiggerThan)
                            .collect(Collectors.toList());
                    Iterator<Peptide> it = resultFold.iterator();
                    while (it.hasNext()) {
                        Peptide peptide = it.next();
                        peptideTable.getItems().add(peptide);
                    }
                } else if (geneSymbolSearchBox.getText().length()>0) {
                    peptideTable.getItems().clear();
                    List<Peptide> resultFold = resultGS.stream()
                            .filter(isFCbiggerThan)
                            .collect(Collectors.toList());
                    Iterator<Peptide> it = resultFold.iterator();
                    while (it.hasNext()) {
                        Peptide peptide = it.next();
                        peptideTable.getItems().add(peptide);
                    }
                } else {
                    peptideTable.getItems().clear();
                    List<Peptide> resultFold = resultMOD.stream()
                            .filter(isFCbiggerThan)
                            .collect(Collectors.toList());
                    Iterator<Peptide> it = resultFold.iterator();
                    while (it.hasNext()) {
                        Peptide peptide = it.next();
                        peptideTable.getItems().add(peptide);
                    }

                }
            }
        }

        //IF TABLE NOT FILTERED FOR MODS
        else {
            //GENE SYMBOL FILTER
            if (geneSymbolSearchBox.getText().length()>0) {
                peptideTable.getItems().clear();
                List<Peptide> result = allPeptides.stream()
                        .filter(isGeneSymbol)
                        .collect(Collectors.toList());
                Iterator<Peptide> it = result.iterator();
                while (it.hasNext()) {
                    Peptide peptide = it.next();
                    resultGS.add(peptide);
                    peptideTable.getItems().add(peptide);
                }

            }
            //GENE NUMBER FILTER
            if (geneNumberSearchBox.getText().length() > 0) {
                peptideTable.getItems().clear();
                List<Peptide> result2;
                if (geneSymbolSearchBox.getText().length()>0) {
                    result2 = resultGS.stream()
                            .filter(isGeneNumber)
                            .collect(Collectors.toList());
                    Iterator<Peptide> it = result2.iterator();
                    while (it.hasNext()) {
                        Peptide peptide = it.next();
                        peptideTable.getItems().add(peptide);
                        resultGN.add(peptide);
                    }
                } else {
                    result2 = allPeptides.stream()
                            .filter(isGeneNumber)
                            .collect(Collectors.toList());
                    Iterator<Peptide> it = result2.iterator();
                    while (it.hasNext()) {
                        Peptide peptide = it.next();
                        peptideTable.getItems().add(peptide);
                        resultGN.add(peptide);
                    }
                }

            }
            //PEPTIDE SEQUENCE FILTER
            if (sequenceSearchBox.getText().length()>0) {
                if (geneNumberSearchBox.getText().length() > 0) {
                    peptideTable.getItems().clear();
                    List<Peptide> result2 = resultGN.stream()
                            .filter(isSequenceEqual)
                            .collect(Collectors.toList());
                    Iterator<Peptide> it = result2.iterator();
                    while (it.hasNext()) {
                        Peptide peptide = it.next();
                        peptideTable.getItems().add(peptide);
                        resultSEQ.add(peptide);
                    }

                } else if (geneSymbolSearchBox.getText().length()>0) {
                    peptideTable.getItems().clear();
                    List<Peptide> result2 = resultGS.stream()
                            .filter(isSequenceEqual)
                            .collect(Collectors.toList());
                    Iterator<Peptide> it = result2.iterator();
                    while (it.hasNext()) {
                        Peptide peptide = it.next();
                        peptideTable.getItems().add(peptide);
                        resultSEQ.add(peptide);
                    }

                } else {
                    peptideTable.getItems().clear();
                    List<Peptide> result2 = allPeptides.stream()
                            .filter(isSequenceEqual)
                            .collect(Collectors.toList());
                    Iterator<Peptide> it = result2.iterator();
                    while (it.hasNext()) {
                        Peptide peptide = it.next();
                        peptideTable.getItems().add(peptide);
                        resultSEQ.add(peptide);
                    }

                }
            }
            //FOLD CHANGE FILTER
            if (foldChangeSearchBox.getText().length()>0) {
                if (sequenceSearchBox.getText().length()>0) {
                    peptideTable.getItems().clear();
                    List<Peptide> resultFold = resultSEQ.stream()
                            .filter(isFCbiggerThan)
                            .collect(Collectors.toList());
                    Iterator<Peptide> it = resultFold.iterator();
                    while (it.hasNext()) {
                        Peptide peptide = it.next();
                        peptideTable.getItems().add(peptide);
                    }
                } else if (geneNumberSearchBox.getText().length() > 0) {
                    peptideTable.getItems().clear();
                    List<Peptide> resultFold = resultGN.stream()
                            .filter(isFCbiggerThan)
                            .collect(Collectors.toList());
                    Iterator<Peptide> it = resultFold.iterator();
                    while (it.hasNext()) {
                        Peptide peptide = it.next();
                        peptideTable.getItems().add(peptide);
                    }
                } else if (geneSymbolSearchBox.getText().length() > 0) {
                    peptideTable.getItems().clear();
                    List<Peptide> resultFold = resultGS.stream()
                            .filter(isFCbiggerThan)
                            .collect(Collectors.toList());
                    Iterator<Peptide> it = resultFold.iterator();
                    while (it.hasNext()) {
                        Peptide peptide = it.next();
                        peptideTable.getItems().add(peptide);
                    }
                } else {
                    peptideTable.getItems().clear();
                    List<Peptide> resultFold = allPeptides.stream()
                            .filter(isFCbiggerThan)
                            .collect(Collectors.toList());
                    Iterator<Peptide> it = resultFold.iterator();
                    while (it.hasNext()) {
                        Peptide peptide = it.next();
                        peptideTable.getItems().add(peptide);
                    }

                }
            }
        }
        numberOfGenesInPeptideTableLabel.setText(peptideTable.getItems().size() + " ");
    }




    public void showPeptide(Peptide peptide){
        String run = peptide.getRunName();

        if(runCombobox.getSelectionModel().getSelectedItem()==null || !runCombobox.getSelectionModel().getSelectedItem().equals("run")){
            runCombobox.getSelectionModel().select(run);
            peptideToFind = peptide;
            loadRun();
        }
        else{
            for(Peptide tablePeptide: peptideTable.getItems()){
                if(tablePeptide.getSequence().equals(peptide.getSequence())){
                    selectPeptide(tablePeptide);
                    return;
                }
            }
        }
    }

    public void drawIntensitiesChart(HashMap<String, Double> intensities){

        intensitiesChartContainer.getChildren().clear();

        HashMap<String, ArrayList<Double>> conditionIntensities = new HashMap<>();
        for(Map.Entry<String, Double> entry: intensities.entrySet()){
            String condition = entry.getKey().split("/")[0];
            if(!conditionIntensities.containsKey(condition))
                conditionIntensities.put(condition, new ArrayList<>());
            conditionIntensities.get(condition).add(entry.getValue());
        }
        ArrayList<XYChart.Series> allSeries = new ArrayList<>();

        ConfidentBarChart confidentBarChart = new ConfidentBarChart();

        for (Map.Entry<String, ArrayList<Double>> entry : conditionIntensities.entrySet()) {



            int i = 0;
            for (Double intensity : entry.getValue()) {
                if (i + 1 > allSeries.size()) {
                    allSeries.add(new XYChart.Series());
                }

                allSeries.get(i).getData().add(new XYChart.Data(entry.getKey(), intensity));
                i++;
            }
            confidentBarChart.addSeries(entry.getKey(), entry.getValue());
        }

        final MenuItem saveImageItem = new MenuItem("Save plot");
        PlotSaver plotSaver = new PlotSaver("barchart");
        saveImageItem.setOnAction(event -> {
            plotSaver.setBarchartData(allSeries, (Stage) confidentBarChart.getScene().getWindow());
        });
        final MenuItem saveDataItem = new MenuItem("Save data");
        saveDataItem.setOnAction(event -> {
            plotSaver.saveBarchartData(allSeries, "Condition", "Intensity",
                    (Stage) confidentBarChart.getScene().getWindow());
        });

        final ContextMenu menu = new ContextMenu(
                saveImageItem,
                saveDataItem
        );

        confidentBarChart.setOnMouseClicked(event -> {
            if (MouseButton.SECONDARY.equals(event.getButton())) {
                menu.show(confidentBarChart.getScene().getWindow(), event.getScreenX(), event.getScreenY());
            }
        });

        confidentBarChart.draw();
        confidentBarChart.setYLegend("Intensity");
        AnchorFitter.fitAnchor(confidentBarChart);
        intensitiesChartContainer.getChildren().add(confidentBarChart);
    }



}
