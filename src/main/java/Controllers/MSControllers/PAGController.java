package Controllers.MSControllers;

import Controllers.PlotSaver;
import Singletons.Config;
import Singletons.Database;
import graphics.ConfidentBarChart;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
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
import utilities.MSRun;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static org.dizitart.no2.filters.Filters.*;
import static org.dizitart.no2.filters.Filters.eq;

public class PAGController implements Initializable {

    @FXML
    private WebView webview;
    @FXML
    private HBox chartsBox;

    private String run;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void showMap(String peptide, String run){

        this.run = run;
        //webview.setPrefWidth(peptideTable.getWidth()*0.8);


        WebEngine webEngine = webview.getEngine();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String command = getNodes(peptide, run);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if(webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED){

                            webEngine.executeScript(getNodes(peptide, run));
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

    private String getNodes(String peptide, String run){


        ArrayList<String> nodes = new ArrayList<>();
        ArrayList<Pair<Integer, Integer>> links = new ArrayList<>();

        ArrayList<String> nodesJson = new ArrayList<>();
        ArrayList<String> linksJson = new ArrayList<>();

        ArrayList<String> genes = new ArrayList<>();


        nodes.add(peptide);
        nodesJson.add("{id:" + (nodes.size() - 1) + ", label: \"" +
                nodes.get(nodes.size() - 1) + "\", shape: \"ellipse\", color: \"#BF9D7A\"}");


        getPeptideGenes(nodesJson,  nodes,  links, genes,  peptide, true, run);



        NitriteCollection collection = Database.getDb().getCollection("genePeptides");

        Document doc = collection.find(and(eq("run", run),
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
                    getPeptideGenes(nodesJson,  nodes,  links, genes,  peptideSequence, false, run);

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


        return "genNetwork(" + nodesStr + ", " + linksStr + "," + webview.getHeight() + ")";

    }

    private void getPeptideGenes(ArrayList<String> nodesJson, ArrayList<String> nodes, ArrayList<Pair<Integer, Integer>> links,
                                 ArrayList<String> genesFound, String peptide, boolean addTranscripts, String run){
        NitriteCollection collection = Database.getDb().getCollection("peptideMap");
        Document doc = collection.find(and(eq("run", run), eq("peptide", peptide))).firstOrDefault();

        JSONObject genesTranscripts = doc.get("transcripts", JSONObject.class);
        for (String gene : (Iterable<String>) genesTranscripts.keySet()) {
            JSONArray transcripts = (JSONArray) genesTranscripts.get(gene);


            if (!nodes.contains(gene)) {
                nodes.add(gene);
                System.out.println(gene);
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
                    drawPeptideChart(obj.getString("label"), run);
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

    private void drawPeptideChart(String peptide, String run){

        if(Config.hasQuantification(run)){
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



}
