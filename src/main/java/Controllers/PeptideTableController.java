package Controllers;

import Cds.PSM;
import Cds.PTM;
import Cds.Peptide;
import com.jfoenix.controls.JFXComboBox;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Pair;
import netscape.javascript.JSObject;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
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

import static org.dizitart.no2.filters.Filters.*;

public class PeptideTableController implements Initializable {

    @FXML
    public HBox chartsBox;
    @FXML
    public GridPane peptideDetailsGrid;
    @FXML
    public TableView<MSRun> peptideSampleTable;
    @FXML
    public TableView<PSM> psmTable;
    @FXML
    public TableColumn<PSM, Double> psmProbabilityColumn;
    @FXML
    public TableColumn<PSM, String> psmFileColumn;
    @FXML
    public TableColumn<PSM, Integer> psmIndexColumn;
    @FXML
    public TableView<PTM> suggestedPTMFilterTable;
    @FXML
    public TableView<PTM> ptmFilterTable;
    @FXML
    public TableColumn<PTM, String> suggestedPTMNameColumn;
    @FXML
    public TableColumn<PTM, Double> suggestedPTMMassShiftColumn;
    @FXML
    public TableColumn<PTM, String> PTMNameFilterColumn;
    @FXML
    public TableColumn<PTM, Double> PTMMassShiftFilterColumn;
    @FXML
    public TableColumn<Peptide, Integer> nbGenesColumn;
    @FXML
    public WebView specWebview;
    @FXML
    public AnchorPane spectrumViewer;
    @FXML
    public BarChart intensitiesChart;
    @FXML
    private TableColumn<MSRun, String> peptideSampleTableSampleColumn;
    @FXML
    private TableColumn<MSRun, Double> peptideSampleTableProbabilityColumn;
    @FXML
    public TableView<MassSpecModificationSample> modificationsTable;
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
    private Nitrite db;
    private ArrayList<Peptide> allPeptides;
    private Peptide selectedPeptide;
    private Peptide peptideToFind;
    private MSRun selectedRun;



    @Override
    public void initialize(URL location, ResourceBundle resources) {

        peptideColumn.setCellValueFactory( new PropertyValueFactory<>("sequence"));
        peptideColumn.prefWidthProperty().bind(peptideTable.widthProperty().multiply(0.7));
        nbGenesColumn.setCellValueFactory(new PropertyValueFactory<>("nbGenes"));
        nbGenesColumn.prefWidthProperty().bind(peptideTable.widthProperty().multiply(0.3));

        peptideSampleTableSampleColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getName()));
        peptideSampleTableProbabilityColumn.setCellValueFactory(cellData ->
                new ReadOnlyDoubleWrapper(cellData.getValue().getPeptideProbability(selectedPeptide.getSequence())).asObject());

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
            TableRow<MSRun> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!(row.isEmpty())) {
                    if (event.getButton().equals(MouseButton.PRIMARY)){
                        Peptide peptide = peptideTable.getSelectionModel().getSelectedItem();
                        MSRun run = row.getItem();

                       HashMap<String, Double> intensities = run.getIntensities(peptide.getSequence());
                        XYChart.Series series =  new XYChart.Series();
                        for(Map.Entry<String, Double> entry: intensities.entrySet()){
                            series.getData().add(new XYChart.Data(entry.getKey(), entry.getValue()));
                        }


                        intensitiesChart.getData().clear();
                        intensitiesChart.getData().add(series);

                        selectPeptideRun(row.getItem().getPeptide(selectedPeptide.getSequence()));

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

                        HashMap<String, Double> intensities = selectedRun.getIntensities(peptide.getSequence());
                        XYChart.Series series =  new XYChart.Series();
                        for(Map.Entry<String, Double> entry: intensities.entrySet()){
                            series.getData().add(new XYChart.Data(entry.getKey(), entry.getValue()));
                        }


                        intensitiesChart.getData().clear();
                        intensitiesChart.getData().add(series);

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



                        HashMap<String, Double> intensities = row.getItem().getIntensities();
                        XYChart.Series series =  new XYChart.Series();
                        for(Map.Entry<String, Double> entry: intensities.entrySet()){
                            series.getData().add(new XYChart.Data(entry.getKey(), entry.getValue()));
                        }


                        intensitiesChart.getData().clear();
                        intensitiesChart.getData().add(series);



                        spectrumViewerController.setConfig(parentController.getConfig(), specWebview);
                        spectrumViewerController.select(psmTable.getSelectionModel().getSelectedItem(), peptideSampleTable.getSelectionModel().getSelectedItem(), selectedPeptide.getSequence());
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


    }


    public void setParentController(ResultsController parentController, Nitrite db){
        this.parentController = parentController;
        this.db = db;
        spectrumViewerController.setConfig(parentController.getConfig(), specWebview);


        for ( String run : Config.getRuns()){
            runCombobox.getItems().add(run);
        }

        runCombobox.getSelectionModel().select(0);
        loadRun();

    }


    @FXML
    public void loadRun(){

        selectedRun = new MSRun(runCombobox.getSelectionModel().getSelectedItem(), Config.getOutputPath());



        new Thread(() -> {

            selectedRun.load(db, Config.getOutputPath(), runCombobox.getSelectionModel().getSelectedItem(),
                    this, peptideToFind, parentController.getConfig());


            peptideTable.getItems().clear();
            peptideTable.getItems().addAll(selectedRun.getAllPeptides());


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
                GridPane.setRowSpan(intensitiesChart, 0);

            }else{


                GridPane.setRowSpan(spectrumViewer, 2);
                GridPane.setRowSpan(intensitiesChart, 1);

            }
        }

        peptideSampleTable.getItems().clear();
        modificationsTable.getItems().clear();
        psmTable.getItems().clear();
        spectrumViewerController.clear();
        chartsBox.getChildren().clear();


        for(MSRun run: selectedRun.getRuns()){
            if(run.getPeptide(peptide.getSequence())!=null && run.getPeptide(peptide.getSequence()).hasPSM()){
                peptideSampleTable.getItems().add(run);
            }

        }

        selectedPeptide = peptide;

    }


    public void selectPeptideRun(Peptide peptide){

        HashMap<HashSet<PTM>, MassSpecModificationSample> ptmSamples = new HashMap<>();

        for (PSM psm: peptide.getPsms()){
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

                                        //System.out.println(getNodes(peptide));
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

        System.out.println("getNodes");


        ArrayList<String> nodes = new ArrayList<>();
        ArrayList<Pair<Integer, Integer>> links = new ArrayList<>();

        ArrayList<String> nodesJson = new ArrayList<>();
        ArrayList<String> linksJson = new ArrayList<>();

        ArrayList<String> genes = new ArrayList<>();


        nodes.add(peptide);
        nodesJson.add("{id:" + (nodes.size() - 1) + ", label: \"" +
                nodes.get(nodes.size() - 1) + "\", shape: \"ellipse\", color: \"#BF9D7A\"}");

        System.out.println("x");
        getPeptideGenes(nodesJson,  nodes,  links, genes,  peptide, true);


        System.out.println("a");
        NitriteCollection collection = db.getCollection("genePeptides");
        System.out.println("b");
        Document doc = collection.find(and(eq("run", runCombobox.getSelectionModel().getSelectedItem()),
                in("gene", genes.toArray(new Object[0])))).firstOrDefault();
        System.out.println("c");

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
        NitriteCollection collection = db.getCollection("peptideMap");
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

        public void browseGene(String msg){
            org.json.JSONObject obj = new org.json.JSONObject(msg);
            if(obj.getString("shape").equals("triangle")){
                parentController.showGeneBrowser(obj.getString("label"), selectedPeptide);
            }
        }

    }


    private void drawPeptideChart(String peptide){




        if(parentController.getConfig().hasQuantification(runCombobox.getSelectionModel().getSelectedItem())){
            NitriteCollection collection = db.getCollection("peptideQuant");
            Document doc = collection.find(Filters.eq("peptide", peptide)).firstOrDefault();

            final CategoryAxis xAxisbarChart = new CategoryAxis();
            final NumberAxis yAxisbarChart = new NumberAxis();
            yAxisbarChart.setLabel("Protein abundance");
            BarChart<String, Number> barChart =
                    new BarChart<>(xAxisbarChart, yAxisbarChart);
            barChart.setTitle("Differential protein abundance");
            ArrayList<XYChart.Series> allSeriesAbundance = new ArrayList<>();

            org.json.JSONObject res = (org.json.JSONObject) doc.get("abundance");
            System.out.println(res);
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

            for (XYChart.Series series : allSeriesAbundance) {
                barChart.getData().add(series);
            }
            HBox.setHgrow(barChart, Priority.ALWAYS);
            chartsBox.getChildren().add(barChart);
        }else{

        }




    }

    private void drawSelectedGeneReadCount(String gene){

        NitriteCollection collection = db.getCollection("readCounts");
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

        NitriteCollection collection = db.getCollection("proteinQuant");
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


        NitriteCollection collection = db.getCollection("allTranscripts");
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
        suggestedPTMFilterTable.getItems().add(new PTM("S", 79.96633, true));
        suggestedPTMFilterTable.getItems().add(new PTM("T", 79.96633, true));
        suggestedPTMFilterTable.getItems().add(new PTM("Y", 79.96633, true));
        suggestedPTMFilterTable.getItems().add(new PTM("M", 15.9949, true));
    }

    @FXML
    public void filter(){
        Iterator<Peptide> it = allPeptides.iterator();
        peptideTable.getItems().clear();
        while (it.hasNext()){
            Peptide peptide = it.next();
            for(PTM ptm: ptmFilterTable.getItems()){
                if(peptide.contains(ptm)){
                    peptideTable.getItems().add(peptide);
                    break;
                }
            }

        }
    }

    public void showPeptide(Peptide peptide){
        String run = peptide.getPsms().get(0).getRun();


        if(runCombobox.getSelectionModel().getSelectedItem()==null || !runCombobox.getSelectionModel().getSelectedItem().equals("run")){
            runCombobox.getSelectionModel().select(run);
            peptideToFind = peptide;
            loadRun();
        }else{
            for(Peptide tablePeptide: peptideTable.getItems()){
                if(tablePeptide.getSequence().equals(peptide.getSequence())){
                    selectPeptide(tablePeptide);
                    return;
                }
            }
        }

    }



}
