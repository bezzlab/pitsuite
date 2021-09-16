package Controllers;

import Cds.PTM;
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
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebView;
import javafx.util.Pair;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import pathway.Element;
import pathway.Gene;
import pathway.alerts.DgeAlert;
import pathway.alerts.MutationAlert;
import pathway.alerts.PTMAlert;
import pathway.alerts.SplicingAlert;
import utilities.Kinase;

import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Controllers.PathwayController.toHexString;

public class PhosphoController implements Initializable {

    @FXML
    private AnchorPane dgePane;
    @FXML
    private Pane graphPane;
    @FXML
    private Pane heatmapPane;
    @FXML
    private TableColumn<Kinase, String> kinaseColumn;
    @FXML
    private TableColumn<Kinase, Double> kinaseLog2fcColumn;
    @FXML
    private TableColumn<Kinase, Double> kinasePvalColumn;
    @FXML
    private WebView kinaseWebview;
    @FXML
    private Accordion kinaseAccordion;
    @FXML
    private TableView<Kinase> kinaseTable;
    @FXML
    private Accordion accordion;
    @FXML
    private AnchorPane intensityPane;
    @FXML
    private TableView<PTM> phosphoTable;
    @FXML
    private TableColumn<PTM, String> phosphoGeneColumn;
    @FXML
    private TableColumn<PTM, Integer> posColumn;
    @FXML
    private TableColumn<PTM, String> residueColumn;
    @FXML
    private TableColumn<PTM, Double> phosphoLog2fcColumn;
    @FXML
    private TableColumn<PTM, Double> phosphoPvalColumn;

    private SmartGraphPanel<String, String> graphView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        phosphoGeneColumn.setCellValueFactory( new PropertyValueFactory<>("gene"));
        posColumn.setCellValueFactory( new PropertyValueFactory<>("pos"));
        residueColumn.setCellValueFactory( new PropertyValueFactory<>("residue"));
        phosphoLog2fcColumn.setCellValueFactory( new PropertyValueFactory<>("Log2fc"));
        phosphoPvalColumn.setCellValueFactory( new PropertyValueFactory<>("pval"));

        phosphoGeneColumn.prefWidthProperty().bind(phosphoTable.widthProperty().divide(5));
        posColumn.prefWidthProperty().bind(phosphoTable.widthProperty().divide(5));
        residueColumn.prefWidthProperty().bind(phosphoTable.widthProperty().divide(5));
        phosphoLog2fcColumn.prefWidthProperty().bind(phosphoTable.widthProperty().divide(5));
        phosphoPvalColumn.prefWidthProperty().bind(phosphoTable.widthProperty().divide(5));

        kinaseColumn.setCellValueFactory( new PropertyValueFactory<>("name"));
        kinaseLog2fcColumn.setCellValueFactory( new PropertyValueFactory<>("Log2fc"));
        kinasePvalColumn.setCellValueFactory( new PropertyValueFactory<>("pval"));

        kinaseColumn.prefWidthProperty().bind(kinaseTable.widthProperty().divide(3));
        kinaseLog2fcColumn.prefWidthProperty().bind(kinaseTable.widthProperty().divide(3));
        kinasePvalColumn.prefWidthProperty().bind(kinaseTable.widthProperty().divide(3));

        phosphoTable.setRowFactory(tv -> {
            TableRow<PTM> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!(row.isEmpty())) {
                    if (event.getButton().equals(MouseButton.PRIMARY)){
                        if ( event.getClickCount() == 1 )
                            showIntensity(phosphoTable.getSelectionModel().getSelectedItem());
                    }
                }
            });
            return row;
        });

        kinaseTable.setRowFactory(tv -> {
            TableRow<Kinase> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!(row.isEmpty())) {
                    if (event.getButton().equals(MouseButton.PRIMARY)){
                        if ( event.getClickCount() == 1 )
                            showKinaseTargets(row.getItem());
                    }
                }
            });
            return row;
        });
        phosphoTable.setRowFactory(tv -> {
            TableRow<PTM> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!(row.isEmpty())) {
                    if (event.getButton().equals(MouseButton.PRIMARY)){
                        if ( event.getClickCount() == 1 ) {
                            showPhosphositesKinases(row.getItem());
                            showGeneData(row.getItem().getGene());
                        }
                    }
                }
            });
            return row;
        });
        loadPhosphosites();
        loadKinases();



    }


    public void loadPhosphosites(){
        Cursor ptmCursor = Database.getDb().getCollection("ptm").find();
        Pattern pattern = Pattern.compile("\\(([A-Z])(\\d+)\\)");

        for(Document doc: ptmCursor){
            JSONObject json = new JSONObject(doc);

            Matcher matcher = pattern.matcher(json.getString("id"));
            if (matcher.find()) {
                PTM ptm = new PTM(matcher.group(1), Integer.parseInt(matcher.group(2)), json.getString("gene"), json.getDouble("log2fc"), json.has("pval") ? json.getDouble("pval") : Double.NaN, json.getJSONObject("samples"),
                        json.getString("type"));
                if(json.has("kinases")){
                    for(Object o: json.getJSONArray("kinases")){
                        ptm.addKinase((String) o);
                    }
                }
                phosphoTable.getItems().add(ptm);
            }
        }
    }

    public void loadKinases(){
        JSONParser parser = new JSONParser();
        try {
            org.json.simple.JSONObject jsonKinases = (org.json.simple.JSONObject) parser.parse(new FileReader("/home/esteban/Documents/tests/KSEA/kinaseActivity.json"));
            for(Object kinaseName: jsonKinases.keySet()){
                org.json.simple. JSONObject kinaseObj = (org.json.simple.JSONObject) jsonKinases.get(kinaseName);
                HashMap<String, Double> targets = new HashMap<>();
                org.json.simple.JSONObject targetsJson = (org.json.simple.JSONObject) kinaseObj.get("targets");
                for (Object target: targetsJson.keySet()){
                    targets.put((String) target, (double) targetsJson.get( target));
                }
                kinaseTable.getItems().add(new Kinase((String) kinaseName, (double) kinaseObj.get("log2fc"), (double) kinaseObj.get("pval"), targets));
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
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
        chart.draw();
        intensityPane.getChildren().clear();
        intensityPane.getChildren().add(chart);
        AnchorFitter.fitAnchor(chart);

        accordion.setExpandedPane(accordion.getPanes().get(0));

    }

    public void showPhosphositesKinases(PTM ptm){
        Graph<String, String> g = new GraphEdgeList<>();
        HashSet<String> nodes = new HashSet<>();
        HashSet<String> kinases = new HashSet<>();

        g.insertVertex(ptm.getId());
        nodes.add(ptm.getId());

        TreeMap<Double, String> targetsOrdered = new TreeMap<>();
        targetsOrdered.put(ptm.getLog2fc(), ptm.getId());
        final double[] min = {Double.POSITIVE_INFINITY};
        final double[] max = { Double.NEGATIVE_INFINITY };

        min[0] = ptm.getLog2fc();
        max[0] = ptm.getLog2fc();

        if(ptm.getKinases()!=null) {
            for (String kinase : ptm.getKinases()) {
                g.insertVertex(kinase);
                nodes.add(kinase);
                g.insertEdge(kinase, ptm.getId(), kinase + "->" + ptm.getId());

            }
        }

        generateGraph(g, targetsOrdered, min, max,  nodes,  kinases);
    }

    public void generateGraph(Graph<String, String> g, TreeMap<Double, String> targetsOrdered, double[] min, double[] max, HashSet<String> nodes, HashSet<String> kinases){
        SmartPlacementStrategy strategy = new SmartRandomPlacementStrategy();
        graphView = new SmartGraphPanel<>(g, strategy);


        for (Map.Entry<Double, String> target : targetsOrdered.descendingMap().entrySet()) {
            double hue = Color.GREEN.getHue() + (Color.RED.getHue() - Color.GREEN.getHue()) * (target.getKey() - min[0]) / (max[0] - min[0]);
            Color color = Color.hsb(hue, 1.0, 1.0);
            graphView.getStylableVertex(target.getValue()).setStyle("-fx-fill: \""+color+"\"; -fx-stroke: brown;");
        }


        graphView.setAutomaticLayout(true);

        graphPane.getChildren().clear();
        graphPane.getChildren().add(graphView);
        AnchorFitter.fitAnchor(graphView);


        addNewNodesListeners(min, max, targetsOrdered, nodes, g, kinases);


        new Thread(() -> {
            try {
                Thread.sleep(100);
                Platform.runLater(graphView::init);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void showKinaseTargets(Kinase kinase) {

        Graph<String, String> g = new GraphEdgeList<>();
        HashSet<String> nodes = new HashSet<>();
        HashSet<String> kinases = new HashSet<>();

        g.insertVertex(kinase.getName());
        kinases.add(kinase.getName());
        nodes.add(kinase.getName());

        TreeMap<Double, String> targetsOrdered = new TreeMap<>();
        final double[] min = {Double.POSITIVE_INFINITY};
        final double[] max = { Double.NEGATIVE_INFINITY };
        for (Map.Entry<String, Double> target : kinase.getTargets().entrySet()) {
            if (target.getValue() < min[0])
                min[0] = target.getValue();
            if (target.getValue() > max[0])
                max[0] = target.getValue();
            targetsOrdered.put(Math.abs(target.getValue()), target.getKey());
        }

        for (Map.Entry<Double, String> target : targetsOrdered.descendingMap().entrySet()) {
            g.insertVertex(target.getValue());
            nodes.add(target.getValue());
            g.insertEdge(kinase.getName(), target.getValue(), kinase.getName() + "->" + target.getValue());

        }

        generateGraph(g, targetsOrdered, min, max,  nodes,  kinases);


    }


    public void addNewNodesListeners(double[] min, double[] max, TreeMap<Double, String> targetsOrdered, HashSet<String> nodes, Graph<String, String> g, HashSet<String> kinases){
        graphView.setVertexDoubleClickAction(graphVertex -> {

            Optional<Kinase> newKinase= kinaseTable.getItems().stream().filter(e-> e.getName().equals(graphVertex.getUnderlyingVertex().element())).findFirst();
            if(newKinase.isPresent()){


                for (Map.Entry<String, Double> target : newKinase.get().getTargets().entrySet()) {
                    if (target.getValue() < min[0])
                        min[0] = target.getValue();
                    if (target.getValue() > max[0])
                        max[0] = target.getValue();
                    targetsOrdered.put(Math.abs(target.getValue()), target.getKey());
                }



                for (Map.Entry<String, Double> target : newKinase.get().getTargets().entrySet()) {
                    if(!nodes.contains(target.getKey())) {
                        g.insertVertex(target.getKey());
                        nodes.add(target.getKey());
                        g.insertEdge(newKinase.get().getName(), target.getKey(), newKinase.get().getName() + "->" + target.getKey());
                    }
                }

                graphView.update();

                new Thread(()->{
                    try{
                        Thread.sleep(100);
                        Platform.runLater(()->{
                            for (Map.Entry<Double, String> target : targetsOrdered.descendingMap().entrySet()) {
                                double hue = Color.GREEN.getHue() + (Color.RED.getHue() - Color.GREEN.getHue()) * (target.getKey() - min[0]) / (max[0] - min[0]);
                                Color color = Color.hsb(hue, 1.0, 1.0);
                                graphView.getStylableVertex(target.getValue()).setStyle("-fx-fill: \""+color+"\"; -fx-stroke: brown;");
                            }

                            graphView.update();
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }).start();




            }else {


                Optional<PTM> ptm = phosphoTable.getItems().stream().filter(e -> e.getId().equals(graphVertex.getUnderlyingVertex().element())).findFirst();
                if (ptm.isPresent()) {
                    for (String ptmKinase : ptm.get().getKinases()) {
                        if (!nodes.contains(ptmKinase)) {
                            g.insertVertex(ptmKinase);
                            kinases.add(ptmKinase);
                            nodes.add(ptmKinase);

                            //graphView.getStylableVertex(ptmKinase).setStyle("-fx-fill: blue");
                            g.insertEdge(ptmKinase, ptm.get().getId(), ptmKinase + "->" + ptm.get().getId());
                        }

                        graphView.update();
                    }
                }
            }
        });

    }

    public void showGeneData(String gene){
        Element element = new Element("macromolecule");
        element.getEntities().add(new Gene(gene));

        DgeAlert.setAlerts(element, null);
        SplicingAlert.setAlerts(element, null);
        MutationAlert.setAlerts(element, null);
        PTMAlert.setAlerts(element, null);

        pathway.alerts.Alert.populateGenes(dgePane, element, DgeAlert.class);
//        pathway.alerts.Alert.populateGenes(splicingPane, element, SplicingAlert.class);
//        pathway.alerts.Alert.populateGenes(mutationPane, element, MutationAlert.class);
//        pathway.alerts.Alert.populateGenes(PTMPane, element, PTMAlert.class);
    }


}
