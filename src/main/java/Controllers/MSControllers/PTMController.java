package Controllers.MSControllers;

import Cds.PSM;
import Cds.PTM;
import Singletons.Config;
import Singletons.Database;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartRandomPlacementStrategy;
import graphics.AnchorFitter;
import graphics.ConfidentBarChart;
import graphics.GraphicTools;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebView;
import javafx.util.Pair;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteCollection;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import pathway.Element;
import pathway.Gene;
import pathway.alerts.DgeAlert;
import pathway.alerts.MutationAlert;
import pathway.alerts.PTMAlert;
import pathway.alerts.SplicingAlert;
import utilities.Kinase;
import utilities.MSRun;

import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.DoubleBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Controllers.PathwayController.instance;
import static Controllers.PathwayController.toHexString;
import static org.dizitart.no2.filters.Filters.and;
import static org.dizitart.no2.filters.Filters.eq;

public class PTMController implements Initializable {

    @FXML
    public ListView<PSM> psmListView;
    @FXML
    public HBox intensitiesPane;
    @FXML
    public SpectrumViewerController spectrumViewerController;
    @FXML
    public PAGController pagController;
    @FXML
    private ComboBox<String> runCombobox;
    @FXML
    private ComboBox<String> comparisonCombobox;
    @FXML
    private TabPane detailsTabpane;
    @FXML
    private Tab intensityTab;
    @FXML
    private Tab psmTab;
    @FXML
    private GridPane grid;
    @FXML
    private TableView<PTM> ptmTable;
    @FXML
    private TableColumn<PTM, String> ptmGeneColumn;
    @FXML
    private TableColumn<PTM, Integer> posColumn;
    @FXML
    private TableColumn<PTM, String> residueColumn;
    @FXML
    private TableColumn<PTM, Double> ptmLog2fcColumn;
    @FXML
    private TableColumn<PTM, Double> ptmPvalColumn;
    @FXML
    private VBox expressionPane;

    private String ptm;
    private Graph<String, String>  g;
    private AnchorPane graphPane;
    private SmartGraphPanel<String, String> graphView;
    private Pair<Double, Double> minMaxLog2fc;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ptmGeneColumn.setCellValueFactory( new PropertyValueFactory<>("gene"));
        posColumn.setCellValueFactory( new PropertyValueFactory<>("pos"));
        residueColumn.setCellValueFactory( new PropertyValueFactory<>("residue"));
        ptmLog2fcColumn.setCellValueFactory( new PropertyValueFactory<>("Log2fc"));
        ptmPvalColumn.setCellValueFactory( new PropertyValueFactory<>("pval"));

        ptmGeneColumn.prefWidthProperty().bind(ptmTable.widthProperty().divide(5));
        posColumn.prefWidthProperty().bind(ptmTable.widthProperty().divide(5));
        residueColumn.prefWidthProperty().bind(ptmTable.widthProperty().divide(5));
        ptmLog2fcColumn.prefWidthProperty().bind(ptmTable.widthProperty().divide(5));
        ptmPvalColumn.prefWidthProperty().bind(ptmTable.widthProperty().divide(5));


        ptmTable.setRowFactory(tv -> {
            TableRow<PTM> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!(row.isEmpty())) {
                    if (event.getButton().equals(MouseButton.PRIMARY)){
                        if ( event.getClickCount() == 1 ) {
                            intensitiesPane.getChildren().clear();
                            showIntensity(ptmTable.getSelectionModel().getSelectedItem());
                            showPSM(ptmTable.getSelectionModel().getSelectedItem().getId(), "phospho");
                            if(ptm.equals("Phospho (STY)") && Config.getSpecies()!=null && Config.getSpecies().equalsIgnoreCase("HOMO SAPIENS"))
                                showPhosphositesKinases(row.getItem(), g, graphView);
                        }
                    }
                }
            });
            return row;
        });

        psmListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(PSM item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? null : item.getModifiedSequence());
            }
        });


        psmListView.setOnMouseClicked(event -> {
            showPeptideIntensity(psmListView.getSelectionModel().getSelectedItem().getIntensities());
            PSM psm = psmListView.getSelectionModel().getSelectedItem();
            spectrumViewerController.select(psm, new MSRun(psm.getRun()), psm.getSequence());
        });

    }


    public void loadPtm(String ptmName, ArrayList<String> runs) {
        this.ptm = ptmName;
        runCombobox.getItems().addAll(runs);
        runCombobox.getSelectionModel().select(0);
        comparisonCombobox.getItems().addAll(Config.getComparisons(new ArrayList<>(Config.getRunConditions(runs.get(0)))));
        comparisonCombobox.getSelectionModel().select(0);

        if (ptm.equals("Phospho (STY)") && Config.getSpecies() != null && Config.getSpecies().equalsIgnoreCase("HOMO SAPIENS")) {
            Tab kinasesTab = new Tab();
            graphPane = new AnchorPane();
            kinasesTab.setContent(graphPane);
            kinasesTab.setText("Kinases");
            detailsTabpane.getTabs().add(kinasesTab);
            g = new GraphEdgeList<>();
            SmartPlacementStrategy strategy = new SmartRandomPlacementStrategy();
            graphView = new SmartGraphPanel<>(g, strategy);

            graphView.setAutomaticLayout(true);
            graphPane.getChildren().clear();
            graphPane.getChildren().add(graphView);
            AnchorFitter.fitAnchor(graphView);
            Platform.runLater(()-> graphView.init());

            graphView.setVertexDoubleClickAction(graphVertex -> {
                KinaseController.getInstance().selectKinase(graphVertex.getUnderlyingVertex().element(), runCombobox.getSelectionModel().getSelectedItem(), comparisonCombobox.getSelectionModel().getSelectedItem());
                MSController.getInstance().selectTab("Kinase activity");
            });
        }

        runCombobox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            fillTable();
            intensitiesPane.getChildren().clear();
        });

        comparisonCombobox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            fillTable();
            intensitiesPane.getChildren().clear();

        });

        fillTable();
    }

    public void fillTable(){


        ptmTable.getItems().clear();
        Cursor ptmCursor = Database.getDb().getCollection("ptm").find(and(eq("type", ptm), eq("comparison", comparisonCombobox.getSelectionModel().getSelectedItem().replace(" ", "")),
                eq("run", runCombobox.getSelectionModel().getSelectedItem())));
        Pattern pattern = Pattern.compile("\\(([A-Z])(\\d+)\\)");

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for(Document doc: ptmCursor){
            JSONObject json = new JSONObject(doc);

            Matcher matcher = pattern.matcher(json.getString("id"));
            if (matcher.find()) {
                PTM ptm = new PTM(matcher.group(1), Integer.parseInt(matcher.group(2)), json.getString("gene"), json.getDouble("log2fc"), json.has("pval") ? json.getDouble("pval") : Double.NaN, json.getJSONObject("samples"),
                        json.getString("type"));

                if(ptm.getLog2fc()<min)
                    min = ptm.getLog2fc();
                if(ptm.getLog2fc()>max)
                    max = ptm.getLog2fc();


                if(json.has("kinases")){
                    for(Object o: json.getJSONArray("kinases")){
                        ptm.addKinase((String) o);
                    }
                }
                ptmTable.getItems().add(ptm);
            }
        }
        minMaxLog2fc = new Pair<>(min, max);
    }



    public void showIntensity(PTM ptm){

        JSONObject intensities = ptm.getIntensities();
        HashMap<String, ArrayList<Double>> intensitiesHashMap = new HashMap<>();
        for (String sample : intensities.keySet()) {
            String[] sampleSplit = sample.split("/");
            if (!intensitiesHashMap.containsKey(sampleSplit[0]))
                intensitiesHashMap.put(sampleSplit[0], new ArrayList<>());
            intensitiesHashMap.get(sampleSplit[0]).add(intensities.getDouble(sample));
        }

        ConfidentBarChart chart = new ConfidentBarChart();
        chart.addAll(intensitiesHashMap);
        chart.setTitle("PTM intensity");
        chart.draw();
        VBox.setVgrow(chart, Priority.ALWAYS);
        expressionPane.getChildren().clear();
        expressionPane.getChildren().add(chart);

        drawNormalisedIntensity(ptm, "1");


    }

    public void showPhosphositesKinases(PTM ptm, Graph<String, String> g, SmartGraphPanel<String, String> graphView){

        HashSet<String> nodes = new HashSet<>();
        HashSet<String> kinases = new HashSet<>();

        for(Vertex v: g.vertices()){
            g.removeVertex(v);
        }

        g.insertVertex(ptm.getId());
        nodes.add(ptm.getId());

        TreeMap<Double, String> targetsOrdered = new TreeMap<>();
        targetsOrdered.put(ptm.getLog2fc(), ptm.getId());

        if(ptm.getKinases()!=null) {
            for (String kinase : ptm.getKinases()) {
                g.insertVertex(kinase);
                nodes.add(kinase);
                kinases.add(kinase);
                g.insertEdge(kinase, ptm.getId(), kinase + "->" + ptm.getId());
            }
        }

        graphView.update();
        Pair<Double, Double> kinaseMinMaxFc = KinaseController.getInstance().getMinMaxLog2fc();
        generateGraph(g, targetsOrdered, kinaseMinMaxFc.getKey(), kinaseMinMaxFc.getValue(),  nodes,  kinases);
    }


    public void generateGraph(Graph<String, String> g, TreeMap<Double, String> targetsOrdered, double min, double max, HashSet<String> nodes, HashSet<String> kinases){

        addNewNodesListeners(min, max, targetsOrdered, nodes, g, kinases);

        new Thread(() -> {
            try {
                Thread.sleep(100);
                HashMap<String, Double> kinasesLog2fc = KinaseController.getInstance().getKinasesLog2Fc(kinases);

                for (Map.Entry<String, Double> target : kinasesLog2fc.entrySet()) {
                    double hue = Color.GREEN.getHue() + (Color.RED.getHue() - Color.GREEN.getHue()) * (target.getValue() - min) / (max - min);
                    Color color = Color.hsb(hue, 1.0, 1.0);
                    graphView.getStylableVertex(target.getKey()).setStyle("-fx-fill: \""+color+"\"; -fx-stroke: brown;");
                }

                Group heatmap = GraphicTools.drawHeatmap(min, max, 200, 50);
                Platform.runLater(()->{
                   if(graphPane.getChildren().size()==2)
                       graphPane.getChildren().remove(1);
                    graphPane.getChildren().add(heatmap);
                });


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }


    public void addNewNodesListeners(double min, double max, TreeMap<Double, String> targetsOrdered, HashSet<String> nodes, Graph<String, String> g, HashSet<String> kinases){
        graphView.setVertexDoubleClickAction(graphVertex -> {

//            Optional<Kinase> newKinase= kinaseTable.getItems().stream().filter(e-> e.getName().equals(graphVertex.getUnderlyingVertex().element())).findFirst();
//            if(newKinase.isPresent()){
//
//
//                for (Map.Entry<String, Double> target : newKinase.get().getTargets().entrySet()) {
//                    if (target.getValue() < min[0])
//                        min[0] = target.getValue();
//                    if (target.getValue() > max[0])
//                        max[0] = target.getValue();
//                    targetsOrdered.put(Math.abs(target.getValue()), target.getKey());
//                }
//
//
//
//                for (Map.Entry<String, Double> target : newKinase.get().getTargets().entrySet()) {
//                    if(!nodes.contains(target.getKey())) {
//                        g.insertVertex(target.getKey());
//                        nodes.add(target.getKey());
//                        g.insertEdge(newKinase.get().getName(), target.getKey(), newKinase.get().getName() + "->" + target.getKey());
//                    }
//                }
//
//                graphView.update();
//
//                new Thread(()->{
//                    try{
//                        Thread.sleep(100);
//                        Platform.runLater(()->{
//                            for (Map.Entry<Double, String> target : targetsOrdered.descendingMap().entrySet()) {
//                                double hue = Color.GREEN.getHue() + (Color.RED.getHue() - Color.GREEN.getHue()) * (target.getKey() - min[0]) / (max[0] - min[0]);
//                                Color color = Color.hsb(hue, 1.0, 1.0);
//                                graphView.getStylableVertex(target.getValue()).setStyle("-fx-fill: \""+color+"\"; -fx-stroke: brown;");
//                            }
//
//                            graphView.update();
//                        });
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }).start();
//
//
//
//
//            }else {
//
//
//                Optional<PTM> ptm = ptmTable.getItems().stream().filter(e -> e.getId().equals(graphVertex.getUnderlyingVertex().element())).findFirst();
//                if (ptm.isPresent()) {
//                    for (String ptmKinase : ptm.get().getKinases()) {
//                        if (!nodes.contains(ptmKinase)) {
//                            g.insertVertex(ptmKinase);
//                            kinases.add(ptmKinase);
//                            nodes.add(ptmKinase);
//
//                            //graphView.getStylableVertex(ptmKinase).setStyle("-fx-fill: blue");
//                            g.insertEdge(ptmKinase, ptm.get().getId(), ptmKinase + "->" + ptm.get().getId());
//                        }
//
//                        graphView.update();
//                    }
//                }
//            }
        });

    }

    public String getPtm() {
        return ptm;
    }

    public void setPtm(String ptm) {
        this.ptm = ptm;
    }

    public void drawNormalisedIntensity(PTM ptm, String normalisationRun){
        NitriteCollection collection = Database.getDb().getCollection("genePeptides");
        Document result = collection.find(and(eq("gene", ptm.getGene()), eq("run", Config.getParentRun(normalisationRun)))).firstOrDefault();
        if(result!=null) {

            if (Config.getRunType(normalisationRun).equals("TMT")) {
                HashMap<String, ArrayList<Double>> intensities = new HashMap<>();
                org.json.simple.JSONObject intensitiesJson = (org.json.simple.JSONObject) result.get("intensity");
                for (Object run : intensitiesJson.keySet()) {
                    org.json.simple.JSONObject runIntensities = (org.json.simple.JSONObject) intensitiesJson.get(run);
                    for (Object sample : runIntensities.keySet()) {
                        double intensity = (double) runIntensities.get(sample);
                        if (!intensities.containsKey((String) sample)) {
                            intensities.put((String) sample, new ArrayList<>());
                        }
                        intensities.get((String) sample).add(intensity);
                    }
                }
                HashMap<String, Double> intensitiesRatios = new HashMap<>();
                for (Map.Entry<String, ArrayList<Double>> entry : intensities.entrySet()) {
                    Median median = new Median();
                    intensitiesRatios.put(entry.getKey(), median.evaluate(entry.getValue().stream().mapToDouble(Double::doubleValue).toArray()));
                }
                double max = Collections.max(intensitiesRatios.values());
                for (Map.Entry<String, Double> entry : intensitiesRatios.entrySet()) {
                    intensitiesRatios.replace(entry.getKey(), entry.getValue() / max);
                }

                JSONObject ptmIntensities = ptm.getIntensities();
                HashMap<String, ArrayList<Double>> ptmIntensitiesNormalised = new HashMap<>();
                for (String sample : ptmIntensities.keySet()) {
                    String condition = sample.split("/")[0];
                    if (!ptmIntensitiesNormalised.containsKey(condition))
                        ptmIntensitiesNormalised.put(condition, new ArrayList<>());
                    ptmIntensitiesNormalised.get(condition).add(ptmIntensities.getDouble(sample) / intensitiesRatios.get(sample));
                }

                ConfidentBarChart chart = new ConfidentBarChart();
                chart.addAll(ptmIntensitiesNormalised);
                chart.setTitle("PTM intensity normalised for differencial protein expression");
                chart.draw();
                VBox.setVgrow(chart, Priority.ALWAYS);
                expressionPane.getChildren().add(chart);
            }
        }

    }

    public void showPSM(String id, String run){
        psmListView.getItems().clear();
        Document doc = Database.getDb().getCollection("ptm").find(and(eq("id", id), eq("type", ptm), eq("run", run))).firstOrDefault();
        JSONArray peptides = (JSONArray) doc.get("peptides");
        for(Object o: peptides){
            org.json.simple.JSONObject peptideObj = (org.json.simple.JSONObject) o;

            long specIndex = (long) peptideObj.get("specIndex");
            String file = (String) peptideObj.get("file");

            PSM psm = new PSM((String) peptideObj.get("sequence"), run, specIndex, file);
            System.out.println(psm.getSequence());
            Document peptideDoc = Database.getDb().getCollection("peptideMap").find(and(eq("peptide", psm.getSequence()), eq("run", run))).firstOrDefault();
            for(Object psmO: (JSONArray) peptideDoc.get("psms")){
                org.json.simple.JSONObject psm2 = (org.json.simple.JSONObject) psmO;
                long specIndex2 = (long) psm2.get("specIndex");
                String file2 = (String) psm2.get("file");
                if(specIndex==specIndex2 && file.equals(file2)){
                    org.json.simple.JSONObject intensities = (org.json.simple.JSONObject) psm2.get("intensity");

                    HashMap<String, Double> intensitiesMap = new HashMap<>();
                    for(Object sample: intensities.keySet()){
                        intensitiesMap.put((String) sample, (double) intensities.get(sample));
                    }
                    psm.setIntensities(intensitiesMap);
                    psmListView.getItems().add(psm);
                    break;
                }
            }
        }
    }

    public void showPeptideIntensity(HashMap<String, Double> intensities){
        intensitiesPane.getChildren().clear();
        PeptideTableController.drawIntensitiesChart(intensities, intensitiesPane);
    }

    public HashMap<String, Double> getPTMLog2Fc(HashSet<String> ptms){
        HashMap<String, Double> ptmsLog2fc = new HashMap<>();
        for(PTM ptm: ptmTable.getItems()){
            if(ptms.contains(ptm.getId())){
                ptmsLog2fc.put(ptm.getId(), ptm.getLog2fc());
            }
        }
        return ptmsLog2fc;
    }


    public Pair<Double, Double> getMinMaxLog2fc() {
        return minMaxLog2fc;
    }


}
