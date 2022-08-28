package Controllers.MSControllers;

import Cds.PTM;
import Controllers.DgeTableController;
import Singletons.Config;
import Singletons.Database;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartRandomPlacementStrategy;
import graphics.AnchorFitter;
import graphics.ConfidentBarChart;
import graphics.GraphicTools;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.dizitart.no2.filters.Filters.and;
import static org.dizitart.no2.filters.Filters.eq;

public class KinaseController implements Initializable {

    @FXML
    private ComboBox<String> runCombobox;
    @FXML
    private ComboBox<String> comparisonCombobox;
    @FXML
    private VBox expressionBox;
    @FXML
    private Pane graphPane;
    @FXML
    private TableColumn<Kinase, String> kinaseColumn;
    @FXML
    private TableColumn<Kinase, Double> kinaseLog2fcColumn;
    @FXML
    private TableColumn<Kinase, Double> kinasePvalColumn;

    @FXML
    private TableView<Kinase> kinaseTable;

    private static KinaseController instance;

    private Pair<Double, Double> minMaxLog2fc;


    private SmartGraphPanel<String, String> graphView;
    private Graph<String, String> g;
    private String ptm;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        instance = this;

        kinaseColumn.setCellValueFactory( new PropertyValueFactory<>("name"));
        kinaseLog2fcColumn.setCellValueFactory( new PropertyValueFactory<>("Log2fc"));
        kinasePvalColumn.setCellValueFactory( new PropertyValueFactory<>("pval"));

        kinaseColumn.prefWidthProperty().bind(kinaseTable.widthProperty().divide(3));
        kinaseLog2fcColumn.prefWidthProperty().bind(kinaseTable.widthProperty().divide(3));
        kinasePvalColumn.prefWidthProperty().bind(kinaseTable.widthProperty().divide(3));







        g = new GraphEdgeList<>();
        SmartPlacementStrategy strategy = new SmartRandomPlacementStrategy();
        graphView = new SmartGraphPanel<>(g, strategy);

        graphView.setAutomaticLayout(true);
        graphPane.getChildren().clear();
        graphPane.getChildren().add(graphView);
        AnchorFitter.fitAnchor(graphView);

        Platform.runLater(()->graphView.init());

        kinaseTable.setRowFactory(tv -> {
            TableRow<Kinase> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!(row.isEmpty())) {
                    if (event.getButton().equals(MouseButton.PRIMARY)){
                        if ( event.getClickCount() == 1 ) {
                            showKinaseTargets(row.getItem());
                            //showExpression(row.getItem());
                        }
                    }
                }
            });
            return row;
        });


    }



    public void loadKinases(ArrayList<String> runs){

        runCombobox.getItems().addAll(runs);
        runCombobox.getSelectionModel().select(0);
        comparisonCombobox.getItems().addAll(Config.getComparisons(new ArrayList<>(Config.getRunConditions(runs.get(0)))));
        comparisonCombobox.getSelectionModel().select(0);

        fillTable(runCombobox.getSelectionModel().getSelectedItem(), comparisonCombobox.getSelectionModel().getSelectedItem().replace(" ", ""));

        runCombobox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            fillTable(runCombobox.getSelectionModel().getSelectedItem(), comparisonCombobox.getSelectionModel().getSelectedItem().replace(" ", ""));
            expressionBox.getChildren().clear();
            clearGraph();
        });

        comparisonCombobox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            fillTable(runCombobox.getSelectionModel().getSelectedItem(), comparisonCombobox.getSelectionModel().getSelectedItem().replace(" ", ""));
            expressionBox.getChildren().clear();


        });



    }

    public void fillTable(String run, String comparison){
        kinaseTable.getItems().clear();

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        Cursor cursor = Database.getDb().getCollection("kinaseActivity").find(and(eq("run", run), eq("comparison", comparison)));
        for(Document doc: cursor){

            org.json.simple.JSONObject targets = (org.json.simple.JSONObject) doc.get("targets");
            HashMap<String, Double> targetsMap = new HashMap<>();
            for(Object ptm: targets.keySet()){
                targetsMap.put((String) ptm, (double) targets.get(ptm));
            }

            double log2fc = (double) doc.get("log2fc");
            if(log2fc<min)
                min = log2fc;
            if(log2fc>max)
                max = log2fc;


            kinaseTable.getItems().add(new Kinase((String) doc.get("id"), log2fc, (double) doc.get("pval"), targetsMap));
        }

        minMaxLog2fc = new Pair<>(min, max);
    }

    public void clearGraph(){
        for(Vertex v: g.vertices()){
            g.removeVertex(v);
        }
    }

    public void showExpression(Kinase kinase){


        expressionBox.getChildren().clear();

        GridPane gridPane = new GridPane();
        RowConstraints r1 = new RowConstraints();
        r1.setPercentHeight(50);
        RowConstraints r2 = new RowConstraints();
        r2.setPercentHeight(50);
        gridPane.getRowConstraints().add(r1);
        gridPane.getRowConstraints().add(r2);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(100);
        gridPane.getColumnConstraints().add(c1);

        AnchorPane rnaPane = new AnchorPane();
        DgeTableController.drawSelectedGeneReadCount(kinase.getName(), rnaPane, 14);


        HBox proteinPane = new HBox();
        DgeTableController.drawSelectedGeneProteinQuant(kinase.getName(), proteinPane, 14, "1", false);

        gridPane.add(rnaPane,0, 0);
        gridPane.add(proteinPane, 0, 1);
        expressionBox.getChildren().add(gridPane);
        VBox.setVgrow(gridPane, Priority.ALWAYS);
        if(proteinPane.getChildren().size()==0){
            r1.setPercentHeight(100);
            r2.setPercentHeight(0);
        }


    }


    public void showKinaseTargets(Kinase kinase) {


        HashSet<String> nodes = new HashSet<>();
        HashSet<String> ptms = new HashSet<>();

        clearGraph();
        g.insertVertex(kinase.getName());
        nodes.add(kinase.getName());

        TreeMap<Double, String> targetsOrdered = new TreeMap<>();
        for (Map.Entry<String, Double> target : kinase.getTargets().entrySet()) {
            targetsOrdered.put(Math.abs(target.getValue()), target.getKey());
        }

        for (Map.Entry<Double, String> target : targetsOrdered.descendingMap().entrySet()) {
            g.insertVertex(target.getValue());
            nodes.add(target.getValue());
            g.insertEdge(kinase.getName(), target.getValue(), kinase.getName() + "->" + target.getValue());
            ptms.add(target.getValue());

        }


        graphView.update();
        Pair<Double, Double> ptmMinMaxFc = MSController.getInstance().getPTMController("Phospho (STY)").getMinMaxLog2fc();
        generateGraph(g, targetsOrdered, ptmMinMaxFc.getKey(), ptmMinMaxFc.getValue(),  nodes,  ptms);


    }

    public void generateGraph(Graph<String, String> g, TreeMap<Double, String> targetsOrdered, double min, double max, HashSet<String> nodes, HashSet<String> ptms){


        addNewNodesListeners(min, max, targetsOrdered, nodes, g, ptms);


        new Thread(() -> {
            try {
                Thread.sleep(100);

                HashMap<String, Double> ptmsLog2fc = MSController.getInstance().getPTMController("Phospho (STY)").getPTMLog2Fc(ptms);

                for (Map.Entry<String, Double> target : ptmsLog2fc.entrySet()) {

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

            Optional<Kinase> newKinase= kinaseTable.getItems().stream().filter(e-> e.getName().equals(graphVertex.getUnderlyingVertex().element())).findFirst();
            if(newKinase.isPresent()){


                for (Map.Entry<String, Double> target : newKinase.get().getTargets().entrySet()) {
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
                                double hue = Color.GREEN.getHue() + (Color.RED.getHue() - Color.GREEN.getHue()) * (target.getKey() - min) / (max - min);
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


//                Optional<PTM> ptm = phosphoTable.getItems().stream().filter(e -> e.getId().equals(graphVertex.getUnderlyingVertex().element())).findFirst();
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
            }
        });

    }

    public static KinaseController getInstance() {
        return instance;
    }

    public String getPtm() {
        return ptm;
    }

    public void setPtm(String ptm) {
        this.ptm = ptm;
    }


    public void selectKinase(String kinase, String run, String comparison) {
        if(!run.equals(runCombobox.getSelectionModel().getSelectedItem())){
            runCombobox.getSelectionModel().select(run);
        }
        if(!comparison.equals(comparisonCombobox.getSelectionModel().getSelectedItem())){
            comparisonCombobox.getSelectionModel().select(run);
        }
        fillTable(run, comparison);
        for(Kinase tableKinase: kinaseTable.getItems()){
            if(tableKinase.getName().equals(kinase)){
                kinaseTable.getSelectionModel().select(tableKinase);
                kinaseTable.scrollTo(tableKinase);
                break;
            }
        }
    }

    public Pair<Double, Double> getMinMaxLog2fc() {
        return minMaxLog2fc;
    }

    public HashMap<String, Double> getKinasesLog2Fc(HashSet<String> kinases){
        HashMap<String, Double> kinasesLog2fc = new HashMap<>();
        for(Kinase kinase: kinaseTable.getItems()){
            if(kinases.contains(kinase.getName())){
                kinasesLog2fc.put(kinase.getName(), kinase.getLog2fc());
            }
        }
        return kinasesLog2fc;
    }
}
