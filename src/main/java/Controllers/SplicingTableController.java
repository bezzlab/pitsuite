package Controllers;

import FileReading.AllGenesReader;
import Singletons.Config;
import Singletons.Database;
import TablesModels.SplicingEventsTableModel;
import TablesModels.Variation;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import exonSplicingEvent.SplicingEvent;
import graphics.ConfidentBarChart;
import graphics.DoughnutChart;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.dizitart.no2.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static javafx.scene.paint.Color.*;
import static org.dizitart.no2.filters.Filters.*;

public class SplicingTableController extends Controller {


    @FXML
    private GridPane rightGrid;
    @FXML
    private GridPane representationChartsBox;
    @FXML
    private ComboBox<String> selectedRunRepresentation;
    @FXML
    private Spinner minRatioSpinner;
    @FXML
    private JFXComboBox<String> comparisonSplicingCombobox;

    // table filters
    @FXML
    private JFXTextField geneFilterSplicingTextField;
    @FXML
    private Spinner<Double> pValFilterSplicingSpinner;
    @FXML
    private JFXTextField eventTypeFilterSplicingTextField;
    @FXML
    private CheckBox peptideEvidenceCheckbox;
    @FXML
    private  Button filterSplicingButton;
//    @FXML
//    private JFXCheckBox domainsOutFilterSplicingCheckBox;

    // table
    @FXML
    private TableView<SplicingEventsTableModel> splicingEventsTableView;
    @FXML
    private TableColumn<SplicingEventsTableModel, String> geneSymbolSplicingTableColumn;
    @FXML
    private TableColumn<SplicingEventsTableModel, String> strandSplicingTableColumn;
    @FXML
    private TableColumn<SplicingEventsTableModel, String> eventTypeSplicingTableColumn;
    @FXML
    private TableColumn<SplicingEventsTableModel, Integer> exonStartSplicingTableColumn;
    @FXML
    private TableColumn<SplicingEventsTableModel, Integer> exonEndSplicingTableColumn;
    @FXML
    private TableColumn<SplicingEventsTableModel, Double> dPsiSplicingTableColumn;
    @FXML
    private TableColumn<SplicingEventsTableModel, Double> pValSplicingTableColumn;
    @FXML
    private TableColumn<SplicingEventsTableModel, Boolean> peptideEvidenceColumn;
    @FXML
    private TableColumn<SplicingEventsTableModel, Double> geneRatioColumn;

    // labels below the table
    @FXML
    private Label numberOfGenesInTableLabel;
    @FXML
    private Label numberOfSplicingEventssInTableLabel;

    // events types barchart


    // event description key
    @FXML
    private Label eventTypeLabel;
    @FXML
    private Button eventTypeAboutButton;
    @FXML Label pvalLabel;
    @FXML Label typeLabel;

    // domains
    @FXML
    private TextArea domainsInTextArea;
    @FXML
    private TextArea domainsOutTextArea;

    // exon specific charts
    @FXML
    private BarChart<String, Number> tpmWithBarChart;
    @FXML
    private Pane psiBarChartContainer;

    @FXML
    private Pane exonRepresentationPane;
    @FXML
    private Pane arrowRepresentationPane;
    @FXML
    private GridPane mainGrid;
    @FXML
    private KeggController keggController;
    @FXML
    private GoTermsController goTermsController;

    @FXML
    private GSEAController gseaController;


    // parent controller
    ResultsController parentController;
    private ConfidentBarChart psiChart;

    // fold change list to Table
    private HashMap<String, SplicingEvent> exonSplicingMap; // map key = event key
    private DoughnutChart eventsTypeDoughnuts;
    private String selectedEvent;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        pvalLabel.setMinWidth(Region.USE_PREF_SIZE);
        typeLabel.setMinWidth(Region.USE_PREF_SIZE);

        //minRatioSpinner.getValueFactory().setValue(30);


        // table : reflection for the getters
        geneSymbolSplicingTableColumn.setCellValueFactory( new PropertyValueFactory<>("geneSymbol"));
        strandSplicingTableColumn.setCellValueFactory( new PropertyValueFactory<>("strand"));
        eventTypeSplicingTableColumn.setCellValueFactory( new PropertyValueFactory<>("eventType"));
        exonStartSplicingTableColumn.setCellValueFactory( new PropertyValueFactory<>("exonStart"));
        exonEndSplicingTableColumn.setCellValueFactory( new PropertyValueFactory<>("exonEnd"));
        dPsiSplicingTableColumn.setCellValueFactory( new PropertyValueFactory<>("deltaPsi"));
        pValSplicingTableColumn.setCellValueFactory( new PropertyValueFactory<>("pVal"));
        geneRatioColumn.setCellValueFactory( new PropertyValueFactory<>("geneRatioDiff"));
        peptideEvidenceColumn.setCellValueFactory( new PropertyValueFactory<>("hasPeptideEvidence"));

        geneSymbolSplicingTableColumn.prefWidthProperty().bind(splicingEventsTableView.widthProperty().divide(9));
        strandSplicingTableColumn.prefWidthProperty().bind(splicingEventsTableView.widthProperty().divide(9));
        eventTypeSplicingTableColumn.prefWidthProperty().bind(splicingEventsTableView.widthProperty().divide(9));
        exonStartSplicingTableColumn.prefWidthProperty().bind(splicingEventsTableView.widthProperty().divide(9));
        exonEndSplicingTableColumn.prefWidthProperty().bind(splicingEventsTableView.widthProperty().divide(9));
        dPsiSplicingTableColumn.prefWidthProperty().bind(splicingEventsTableView.widthProperty().divide(9));
        pValSplicingTableColumn.prefWidthProperty().bind(splicingEventsTableView.widthProperty().divide(9));
        geneRatioColumn.prefWidthProperty().bind(splicingEventsTableView.widthProperty().divide(9));
        pValSplicingTableColumn.prefWidthProperty().bind(splicingEventsTableView.widthProperty().divide(9));





        splicingEventsTableView.setRowFactory(tv -> {
            TableRow<SplicingEventsTableModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!(row.isEmpty())) {
                    if (event.getButton().equals(MouseButton.PRIMARY)){

                        if ( event.getClickCount() == 1 ) {

                            SplicingEventsTableModel tmpSplEvent = splicingEventsTableView.getSelectionModel().getSelectedItem();
                            displayExonInfo(tmpSplEvent.getEventKey(), tmpSplEvent.getEventType(), tmpSplEvent.getStrand());
                            goTermsController.setGoTermsGeneTable(tmpSplEvent.getGeneSymbol());


                        } else if ( event.getClickCount() == 2 ) {
                            parentController.showBrowserFromTranscId(row.getItem().getGeneSymbol(), row.getItem().getExonStart(),
                                    row.getItem().getExonEnd());
                        }
                    }
                }
            });
            return row;
        });

        // p-value spinner

        StringConverter<Double> doubleConverter = new StringConverter<Double>() {
            DecimalFormat df = new DecimalFormat("#.###");
            @Override
            public String toString(Double object) {
                if (object == null) {return "";}
                return df.format(object);}
            @Override
            public Double fromString(String string) {
                try {
                    if (string == null) {return null;}
                    string = string.trim();
                    if (string.length() < 1) {return null;}
                    return df.parse(string).doubleValue();
                } catch (ParseException ex) {throw new RuntimeException(ex);}
            }
        };

        SpinnerValueFactory<Double> pValValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1, 0.05, 0.001);
        pValValueFactory.setConverter(doubleConverter);
        pValFilterSplicingSpinner.setValueFactory(pValValueFactory);
        pValFilterSplicingSpinner.setEditable(true);

        ChangeListener runRepresentationListener =  new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                SplicingEventsTableModel se = splicingEventsTableView.getSelectionModel().getSelectedItem();
                if(se.getEventKey().equals(selectedEvent)){
                    drawSplicingEventRepresentation(se.getEventKey(), se.getStrand(), se.getEventType());
                }

            }
        };
        selectedRunRepresentation.valueProperty().addListener(runRepresentationListener);



    }


    /*
     * Used to set the parent, from the FXML Document Controller,
     * So that when data is loaded, it can handle the first view of the tab
     */
    public void setParentControler(ResultsController parent, AllGenesReader allGenesReader){
        parentController = parent;
        
        for(String collection: Database.getDb().listCollectionNames()){
            if(collection.contains("SplicingDPSI")){
                comparisonSplicingCombobox.getItems().add(collection.replace("SplicingDPSI_", ""));
            }
        }

        if(allGenesReader.getGenesLoadedProperty().get()){
            Platform.runLater(() -> {
                keggController.setParentController(this, allGenesReader);
                goTermsController.setParentController(this, allGenesReader, Database.getDb());
                gseaController.setParentController(this);
            });
        }else{
            allGenesReader.getGenesLoadedProperty().addListener((observableValue, aBoolean, t1) -> {
                if (allGenesReader.getGenesLoadedProperty().get()) {
                    Platform.runLater(() -> {
                        keggController.setParentController(this, allGenesReader);
                        goTermsController.setParentController(this, allGenesReader, Database.getDb());
                        gseaController.setParentController(this);
                    });
                }
            });
        }

        setTableContentAndListeners();
    }



    /**
     * set the listeners and the table when first loaded
     */
    private void setTableContentAndListeners(){

        comparisonSplicingCombobox.valueProperty().addListener((observableValue, o, t1) -> {
            filterSplicingTable();
        });

        filterSplicingButton.setOnAction(actionEvent -> {
            filterSplicingTable();
        });

        // display the table
        comparisonSplicingCombobox.getSelectionModel().select(0);


    }


    /**
     * filters the table
     */
    public void filterSplicingTable(){


        AtomicInteger seEvents = new AtomicInteger(0);
        AtomicInteger mxEvents = new AtomicInteger(0);
        AtomicInteger a5Events = new AtomicInteger(0);
        AtomicInteger a3Events = new AtomicInteger(0);
        AtomicInteger riEvents = new AtomicInteger(0);
        AtomicInteger afEvents = new AtomicInteger(0);
        AtomicInteger alEvents = new AtomicInteger(0);
        ArrayList<String> genesList = new ArrayList<>();
        ArrayList<SplicingEventsTableModel> events = new ArrayList();


        ArrayList<String> genesWithGoFilterList = (!goTermsController.isGoLoaded())?null:goTermsController.genesWithGoTermsForFilter();


        new Thread(() -> {
            String comparison = comparisonSplicingCombobox.getValue() ;
            double pvalThreshold = pValFilterSplicingSpinner.getValue();
            String eventTypesFilterString = eventTypeFilterSplicingTextField.getText().strip().replaceAll(" ","").toUpperCase();
            String[] eventTypesFilterArray = eventTypesFilterString.split(",");


            String geneSymbolFilter = geneFilterSplicingTextField.getText().toUpperCase().trim();


            // get the info for the table
            NitriteCollection splicingDPsiColl = Database.getDb().getCollection("SplicingDPSI_"+comparisonSplicingCombobox.getValue());

            // filters
            List<Filter> filters = new ArrayList<>();

            if(Config.haveReplicates(comparisonSplicingCombobox.getValue().split("vs")))
                filters.add(lte("pval", pvalThreshold));

            if (geneSymbolFilter.length() > 0 ) { // TODO: change this to check if in list
                filters.add(eq("geneName", geneSymbolFilter));
            }
            if (eventTypesFilterString.length() > 0 ){
                filters.add(in("eventType", eventTypesFilterArray));
            }
            if (peptideEvidenceCheckbox.isSelected()) {
                filters.add(eq("pepEvidence", true));
            }
            if (genesWithGoFilterList != null){
                if(genesWithGoFilterList.size() > 0){
                    if (genesWithGoFilterList.size() == 1) {
                        filters.add(eq("geneName", genesWithGoFilterList.get(0)));
                    } else {
                        filters.add(in("geneName", genesWithGoFilterList.toArray()));
                    }
                }
            }


            Cursor splicingCursor = splicingDPsiColl.find(and(filters.toArray(new Filter[]{})));
            boolean updateTableBool = true;

            if (splicingCursor.size() == 0) {
                updateTableBool = false;
            } else {


                for (Document tmpDoc : splicingCursor) {

                    String tmpGeneName = (String) tmpDoc.get("geneName");

                    // since loading is on thread so may be null when first loaded. Depend on allGeneReader
                    if (keggController.isKeggLoaded()  ){
                        if (!keggController.isInKegg(tmpGeneName)){
                            continue;
                        }
                    }


                    String tmpEventKey = (String) tmpDoc.get("event");
                    Double tmpDeltaPsi = Double.valueOf(tmpDoc.get("deltaPsi").toString());
                    Double tmpPval = Double.valueOf(tmpDoc.get("pval").toString());
                    String tmpEventType = (String) tmpDoc.get("eventType");
                    boolean tmpPepEvidence = (boolean) tmpDoc.get("pepEvidence");
                    Double geneRatioDiff = (Double) tmpDoc.get("geneRatioDiff");


                    events.add(new SplicingEventsTableModel(tmpEventKey, tmpGeneName, tmpEventType, tmpDeltaPsi, tmpPval,
                            tmpPepEvidence, geneRatioDiff));

                    switch (tmpEventType) {
                        case "SE":
                            seEvents.getAndIncrement();
                            break;
                        case "MX":
                            mxEvents.getAndIncrement();
                            break;
                        case "A5":
                            a5Events.getAndIncrement();
                            break;
                        case "A3":
                            a3Events.getAndIncrement();
                            break;
                        case "RI":
                            riEvents.getAndIncrement();
                            break;
                        case "AF":
                            afEvents.getAndIncrement();
                            break;
                        case "AL":
                            alEvents.getAndIncrement();
                            break;
                        default:
                            System.out.println("Splicing Table controller L577. Problem with the event type: " + tmpEventType);
                            break;
                    }

                    if (!genesList.contains(tmpGeneName)) {
                        genesList.add(tmpGeneName);
                    }


                }
            }
            boolean finalUpdateTableBool = updateTableBool;
            Platform.runLater(() -> {
                if(finalUpdateTableBool) {
                    splicingEventsTableView.getItems().clear();
                    splicingEventsTableView.getItems().addAll(events);

                    numberOfSplicingEventssInTableLabel.setText( Integer.toString(splicingEventsTableView.getItems().size()) );
                    numberOfGenesInTableLabel.setText( Integer.toString(genesList.size()) );

                    drawEventTypeChart(seEvents.get(), mxEvents.get(), a5Events.get(), a3Events.get(), riEvents.get(), afEvents.get(), alEvents.get(),
                            splicingEventsTableView.getItems().size());
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Info");
                    alert.setHeaderText(null);
                    alert.setContentText("No gene found with selected search criteria. Table won't update.");

                    alert.showAndWait();
                }

            });

        }).start();




    }

    /**
     * draws types chart
     */
    private void drawEventTypeChart(int seEvents, int mxEvents, int a5Events, int a3Events, int riEvents, int afEvents, int alEvents, int total ){




        ObservableList<DoughnutChart.Data> pieChartData =
                FXCollections.observableArrayList();

        if(seEvents>0){
            pieChartData.add(new DoughnutChart.Data("SE: Exon skipping", seEvents ));
        }
        if(mxEvents>0){
            pieChartData.add(new DoughnutChart.Data("MX: Mut. exclusive exons", mxEvents ));
        }
        if(a5Events>0){
            pieChartData.add(new DoughnutChart.Data("A5: Alt. 5' splice-site", a5Events ));
        }
        if(a3Events>0){
            pieChartData.add(new DoughnutChart.Data("A3: Alt. 3' splice-site", a3Events ));
        }
        if(riEvents>0){
            pieChartData.add(new DoughnutChart.Data("RI: Intron retention", riEvents ));
        }
        if(afEvents>0){
            pieChartData.add(new DoughnutChart.Data("AF: Alt. first exon", afEvents ));
        }
        if(alEvents>0){
            pieChartData.add(new DoughnutChart.Data("AL: Alt. last exon", alEvents ));
        }

        pieChartData.forEach(data ->
                data.nameProperty().bind(
                        Bindings.concat(
                                 data.getName() ," (" ,
                                 data.pieValueProperty().intValue() ,
                                 ")"
                        )
                )
        );


        if(eventsTypeDoughnuts!=null){
            mainGrid.getChildren().remove(eventsTypeDoughnuts);
        }
        eventsTypeDoughnuts = new DoughnutChart(pieChartData);
        eventsTypeDoughnuts.setLegendVisible(false);
        mainGrid.add(eventsTypeDoughnuts, 0, 5);
        GridPane.setRowSpan(eventsTypeDoughnuts, 4);
    }

    private void displayExonInfo (String splicingEventKey, String eventType, String strand){

        // clear charts
        //tpmWithBarChart.getData().clear();


        // display the key
        setEventTypeLabel(eventType);

//        SplicingEvent splicingEvent = exonSplicingMap.get(splicingEventKey); // TODO: remove the map
        // open database
        NitriteCollection splicingEventsCollection = Database.getDb().getCollection("SplicingEvents_"+comparisonSplicingCombobox.getValue());
        NitriteCollection splicingPsiCollection = Database.getDb().getCollection("SplicingPsi");

        // get domains
        Cursor splicingEventCursor = splicingEventsCollection.find(eq("event", splicingEventKey));
        Cursor splicingPsiCursor = splicingPsiCollection.find(eq("event", splicingEventKey));

        // get splicing info
        Document splicingEventInfoDoc = splicingEventCursor.firstOrDefault();
        Document splicingPsiDoc = splicingPsiCursor.firstOrDefault();


        // domains info
//        String[] domainsInArray = (String[]) splicingEventInfoDoc.get("domains_in");
//        String[] domainsOutArray = (String[]) splicingEventInfoDoc.get("domains_out");
//        String domainsIn = Arrays.toString(domainsInArray).replace("[", "").replace("]", "");
//        String domainsOut = Arrays.toString(domainsOutArray).replace("[", "").replace("]", "");
//
//        domainsInTextArea.setText(domainsIn);
//        domainsOutTextArea.setText(domainsOut);


        HashMap<String, ArrayList<Double>> psiValues = new HashMap<>();

        for (KeyValuePair keyValuePair: splicingPsiDoc){
            if(!keyValuePair.getKey().equals("_id") && !keyValuePair.getKey().equals("_modified")
                    && !keyValuePair.getKey().equals("_revision")){
                if(!keyValuePair.getKey().equals("event")){
                    String condition = keyValuePair.getKey().split("/")[0];
                    if(!psiValues.containsKey(condition)){
                        psiValues.put(condition, new ArrayList<>());
                    }
                    psiValues.get(condition).add(Double.valueOf(String.valueOf(keyValuePair.getValue())));
                }
            }


        }

        if(psiChart!=null){
            rightGrid.getChildren().remove(psiChart);
        }

        psiChart = new ConfidentBarChart();
        psiChart.addAll(psiValues);
        psiChart.setMin(0.);
        psiChart.setMax(1.);

        rightGrid.add(psiChart, 2, 2);

        // Barcharts
        //tpmWithBarChart.getData().add(samplCondTpmBarChartSeries);


        psiChart.draw();




        // draw the representation
        drawSplicingEventRepresentation(splicingEventKey, strand, eventType);

        selectedEvent = splicingEventKey;

    }

    /**
     * Set Event type Label
     * @param eventType  type of event, abreviation.
     */
    private void setEventTypeLabel(String eventType){

        String typeLabel = "";
        if(eventType.equals("SE")){
            typeLabel = "Skipping Exon";
        } else if(eventType.equals("MX")){
            typeLabel = "Mutually Exclusive Exons";
        } else if(eventType.equals("A5")){
            typeLabel = "Alternative 5' Splice-site";
        } else if(eventType.equals("A3")){
            typeLabel = "Alternative 3' Splice site";
        } else if(eventType.equals("RI")){
            typeLabel = "Retained Intron";
        } else if(eventType.equals("AF")){
            typeLabel = "Alternative First Exon";
        } else if(eventType.equals("AL")){
            typeLabel = "Alternative Last Exon";
        }

        eventTypeLabel.setText("Event type: "+typeLabel);
    }

    private void drawSplicingEventRepresentation(String spliceEventKey, String strand, String eventType){
        // clear representation hbox
        exonRepresentationPane.getChildren().clear();
        Pane exonRepresentationPane = new Pane();

        // parse splice event key to extract values (https://github.com/comprna/SUPPA)
        String[] spliceEventKeySplitted = spliceEventKey.split(";",1);

        List<String> spliceEventKeySplittedList = Arrays.asList(spliceEventKey.split(";"));
        String restKey = spliceEventKeySplittedList.get(1);
        List<String> keyElementsColonSplit = Arrays.asList(restKey.split(":"));
//        String strand = keyElementsColonSplit.get(keyElementsColonSplit.size() - 1);
//        String eventType = keyElementsColonSplit.get(0);

        int currentExonStart = 0;
        int currentExonEnd = 0;
        int lastExonStart = 0;
        int firstExonEnd = 0;

        // if forward strand
        if (strand.equals("+")) {
            drawArrow(strand); // draw arrow
            if (eventType.equals("SE")) {
                // eg. "SE:chr1:960800-961293:961552-961629:+"
                //      0  1    2             3
                List<String> firstPart = Arrays.asList(keyElementsColonSplit.get(2).split("-"));
                firstExonEnd = Integer.parseInt(firstPart.get(0));
                currentExonStart = Integer.parseInt(firstPart.get(1));

                List<String> secondPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                currentExonEnd = Integer.parseInt(secondPartSplit.get(0));
                lastExonStart = Integer.parseInt(secondPartSplit.get(1));

                drawSEPosNeg(spliceEventKey, firstExonEnd, currentExonStart, currentExonEnd, lastExonStart);

            } else if (eventType.equals("MX")) {
                // eg. "MX:chr1:1315618-1319296:1319524-1324581:1315618-1320996:1321093-1324581:-";
                //      0  1    2       1S      3  0E          4               5
                List<String> firstPartSplit = Arrays.asList(keyElementsColonSplit.get(2).split("-"));
                firstExonEnd = Integer.parseInt(firstPartSplit.get(0));
                currentExonStart = Integer.parseInt(firstPartSplit.get(1));
                List<String> secondPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                currentExonEnd = Integer.parseInt(secondPartSplit.get(0));
                lastExonStart = Integer.parseInt(secondPartSplit.get(1));
                List<String> thirdPartSplit = Arrays.asList(keyElementsColonSplit.get(4).split("-"));
                int nextExonStart = Integer.parseInt(thirdPartSplit.get(1));
                List<String> fourthPartSplit = Arrays.asList(keyElementsColonSplit.get(5).split("-"));
                int nextExonEnd = Integer.parseInt(fourthPartSplit.get(0));

                drawMXPosNeg(firstExonEnd, currentExonStart, currentExonEnd, nextExonStart, nextExonEnd, lastExonStart);

            } else  if (eventType.equals("A5")){
                // eg. "A5:chr1:964180-964349:964167-964349:+";
                //      0  1    2  0E          3 0S
                List<String> firstPartSplit = Arrays.asList(keyElementsColonSplit.get(2).split("-"));
                currentExonEnd = Integer.parseInt(firstPartSplit.get(0));
                lastExonStart = Integer.parseInt(firstPartSplit.get(1));
                List<String> secondPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                firstExonEnd = Integer.parseInt(secondPartSplit.get(0));

                drawA5PosA3Neg(firstExonEnd, currentExonEnd, lastExonStart);
            } else  if (eventType.equals("A3")){
                // eg. "A3:chr1:962917-963032:962917-963109:+";
                //      0  1    2         1S  3        1E
                List<String> fistPartSplit = Arrays.asList(keyElementsColonSplit.get(2).split("-"));
                firstExonEnd = Integer.parseInt(fistPartSplit.get(0));
                currentExonStart = Integer.parseInt(fistPartSplit.get(1));
                List<String> secondPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                lastExonStart = Integer.parseInt(secondPartSplit.get(1));

                drawA3PosA5Neg(firstExonEnd, currentExonStart, lastExonStart);
            }  else  if (eventType.equals("RI")) {
                // eg. "RI:chr1:961449:961552-961629:961750:+";
                //      0  1    2      3  0S    1E   4
                int firstExonStart = Integer.parseInt(keyElementsColonSplit.get(2));
                List<String> secondPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                currentExonStart = Integer.parseInt(secondPartSplit.get(0));
                currentExonEnd = Integer.parseInt(secondPartSplit.get(1));
                int lastExonEnd = Integer.parseInt(keyElementsColonSplit.get(4));

                drawRIPosNeg(spliceEventKey, firstExonStart, currentExonStart, currentExonEnd, lastExonEnd);
            } else  if (eventType.equals("AF")) {
                // eg. "AF:chr1:1623122:1623482-1623774:1623581:1623699-1623774:+";
                //      0  1    2  S    3 0E            4       5
                currentExonStart = Integer.parseInt(keyElementsColonSplit.get(2));
                List<String> secondPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                currentExonEnd = Integer.parseInt(secondPartSplit.get(0));
                lastExonStart =  Integer.parseInt(secondPartSplit.get(1));
                int nextExonStart = Integer.parseInt(keyElementsColonSplit.get(4));
                List<String> fourthPartSplit = Arrays.asList(keyElementsColonSplit.get(5).split("-"));
                int nextExonEnd =  Integer.parseInt(fourthPartSplit.get(0));

                drawAFPosALNeg(spliceEventKey, currentExonStart, currentExonEnd, nextExonStart, nextExonEnd, lastExonStart);
            } else  if (eventType.equals("AL")) {
                // eg. "AL:chr1:1060393-1061020:1061117:1060393-1065830:1066274:+";
                //      0  1    2               3       4         1S     5 E
                List<String> fistPartSplit = Arrays.asList(keyElementsColonSplit.get(2).split("-"));
                firstExonEnd = Integer.parseInt(fistPartSplit.get(0));
                int prevExonStart = Integer.parseInt(fistPartSplit.get(1));
                int prevExonEnd = Integer.parseInt(keyElementsColonSplit.get(3));
                List<String> thidPartSplit = Arrays.asList(keyElementsColonSplit.get(4).split("-"));
                currentExonStart = Integer.parseInt(thidPartSplit.get(1));
                currentExonEnd = Integer.parseInt(keyElementsColonSplit.get(5));

                drawALPosAFNeg(spliceEventKey, firstExonEnd, prevExonStart, prevExonEnd, currentExonStart, currentExonEnd);
            }

        } else { // reverse strand
            drawArrow(strand); // draw arrow
            if (eventType.equals("SE")) {
                // eg. "SE:chr1:1315618-1320996:1321093-1324581:-";
                //      0  1    2              3
                List<String> firstPart = Arrays.asList(keyElementsColonSplit.get(2).split("-"));
                firstExonEnd = Integer.parseInt(firstPart.get(0));
                currentExonStart = Integer.parseInt(firstPart.get(1));
                List<String> secondPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                currentExonEnd = Integer.parseInt(secondPartSplit.get(0));
                lastExonStart = Integer.parseInt(secondPartSplit.get(1));

                drawSEPosNeg(spliceEventKey, firstExonEnd, currentExonStart, currentExonEnd, lastExonStart);
            } else if (eventType.equals("MX")) {
                // eg. "MX:chr1:183565779-183566920:183566988-183569142:183565779-183567204:183567345-183569142:-";
                //      0  1    2                   3                   4                   5
                List<String> firstPartSplit = Arrays.asList(keyElementsColonSplit.get(2).split("-"));
                firstExonEnd = Integer.parseInt(firstPartSplit.get(0));
                currentExonStart = Integer.parseInt(firstPartSplit.get(1));
                List<String> secondPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                currentExonEnd = Integer.parseInt(secondPartSplit.get(0));
                lastExonStart = Integer.parseInt(secondPartSplit.get(1));
                List<String> thirdPartSplit = Arrays.asList(keyElementsColonSplit.get(4).split("-"));
                int nextExonStart = Integer.parseInt(thirdPartSplit.get(1));
                List<String> fourthPartSplit = Arrays.asList(keyElementsColonSplit.get(5).split("-"));
                int nextExonEnd = Integer.parseInt(fourthPartSplit.get(0));

                drawMXPosNeg(firstExonEnd, currentExonStart, currentExonEnd, nextExonStart, nextExonEnd, lastExonStart );

            } else  if (eventType.equals("A5")){
                // eg. "A5:chr2:6915967-6916816:6915967-6916850:-";
                //      0  1    2       1S      3        1E
                List<String> firstPartSplit = Arrays.asList(keyElementsColonSplit.get(2).split("-"));
                firstExonEnd = Integer.parseInt(firstPartSplit.get(0));
                currentExonStart = Integer.parseInt(firstPartSplit.get(1));
                List<String> secondPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                lastExonStart = Integer.parseInt(secondPartSplit.get(1));

                drawA3PosA5Neg(firstExonEnd, currentExonStart, lastExonStart);
            } else  if (eventType.equals("A3")){
                // eg. "A3:chr1:42659370-42659522:42659251-42659522:-";
                //      0  1    2  0E             3 0S
                List<String> fistPartSplit = Arrays.asList(keyElementsColonSplit.get(2).split("-"));
                currentExonEnd = Integer.parseInt(fistPartSplit.get(0));
                lastExonStart = Integer.parseInt(fistPartSplit.get(1));
                List<String> secondPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                firstExonEnd = Integer.parseInt(secondPartSplit.get(0));

                drawA5PosA3Neg(firstExonEnd, currentExonEnd, lastExonStart);
            }  else  if (eventType.equals("RI")) {
                // eg. "RI:chr1:42658443:42658512-42658844:42658908:-";
                //      0  1    2        3  0S    1E       4
                int firstExonStart = Integer.parseInt(keyElementsColonSplit.get(2));
                List<String> secondPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                currentExonStart = Integer.parseInt(secondPartSplit.get(0));
                firstExonEnd = Integer.parseInt(secondPartSplit.get(0));
                currentExonEnd = Integer.parseInt(secondPartSplit.get(1));
                lastExonStart = Integer.parseInt(secondPartSplit.get(1));
                int lastExonEnd = Integer.parseInt(keyElementsColonSplit.get(4));

                drawRIPosNeg(spliceEventKey, firstExonStart, currentExonStart, currentExonEnd, lastExonEnd);
            } else  if (eventType.equals("AF")) {
                // eg. "AF:chr2:9420100-9422479:9423228:9420100-9423373:9423480:-";
                //      0  1    2               3       4           1S  5  E
                List<String> firstPartSplit = Arrays.asList(keyElementsColonSplit.get(2).split("-"));
                firstExonEnd = Integer.parseInt(firstPartSplit.get(0));
                int prevExonStart =  Integer.parseInt(firstPartSplit.get(1));
                int prevExonEnd = Integer.parseInt(keyElementsColonSplit.get(3));
                List<String> thirdPartSplit = Arrays.asList(keyElementsColonSplit.get(4).split("-"));
                currentExonStart =  Integer.parseInt(thirdPartSplit.get(1));
                currentExonEnd =  Integer.parseInt(keyElementsColonSplit.get(5));

                drawALPosAFNeg(spliceEventKey, firstExonEnd, prevExonStart, prevExonEnd, currentExonStart, currentExonEnd);
            } else  if (eventType.equals("AL")) {
                // eg. "AL:chr2:9405684:9406905-9408113:9407572:9407598-9408113:-";
                //      0  1    2  S    3  0E           4       5
                currentExonStart =  Integer.parseInt(keyElementsColonSplit.get(2));
                List<String> secondPartSplit = Arrays.asList(keyElementsColonSplit.get(3).split("-"));
                currentExonEnd = Integer.parseInt(secondPartSplit.get(0));
                lastExonStart = Integer.parseInt(secondPartSplit.get(1));
                int nextExonStart =  Integer.parseInt(keyElementsColonSplit.get(4));
                List<String> fourthPartSplit = Arrays.asList(keyElementsColonSplit.get(5).split("-"));
                int nextExonEnd = Integer.parseInt(fourthPartSplit.get(0));

                drawAFPosALNeg(spliceEventKey, currentExonStart, currentExonEnd, nextExonStart, nextExonEnd, lastExonStart);
            }

        }



    }

    private void addPeptideToRepresentation(String splicingEventKey, Group group, double xstart, double xend,
                                            double leftExonXstart, double leftExonXend, double rightExonXstart, double rightExonXend){

        representationChartsBox.getChildren().clear();
        NitriteCollection splicingEventsCollection = Database.getDb().getCollection("eventPeptides");

        // get domains
        Document doc = splicingEventsCollection.find(eq("event", splicingEventKey)).firstOrDefault();


        if(doc!=null){


            if(selectedEvent==null || !selectedEvent.equals(splicingEventKey)){
                selectedRunRepresentation.getItems().clear();
                HashSet<String> runs = new HashSet<>();
                runs.addAll(doc.get("peptidesIn", JSONObject.class).keySet());
                runs.addAll(doc.get("peptidesOut", JSONObject.class).keySet());
                selectedRunRepresentation.getItems().addAll(runs);
                selectedRunRepresentation.getSelectionModel().select(0);

            }

            if(doc.get("peptidesIn", JSONObject.class).containsKey(selectedRunRepresentation.getSelectionModel().getSelectedItem())) {
                for (Object o : (JSONArray) doc.get("peptidesIn", JSONObject.class)
                        .get(selectedRunRepresentation.getSelectionModel().getSelectedItem())) {
                    JSONObject peptideObj = (JSONObject) o;
                    String peptideSeq = (String) peptideObj.get("sequence");
                    int startInExon = Math.toIntExact((Long) peptideObj.get("startInExon"));
                    int exonLength = Math.toIntExact((Long) doc.get("exonLength"));

                    Rectangle rec = new Rectangle();
                    rec.setStroke(Color.BLACK);
                    rec.setStrokeWidth(3);
                    rec.setWidth((xend - xstart) * 0.6);
                    rec.setY(30);
                    rec.setHeight(20);
                    rec.setFill(RED);
                    if (startInExon < 0) {
                        rec.setX((leftExonXstart + leftExonXend) / 2);
                        int lengthInExon = peptideSeq.length() * 3 + startInExon;
                        double lengthInExonRatio = (double) lengthInExon / exonLength;
                        if (lengthInExonRatio < 1) {
                            rec.setWidth(xstart + (xend - xstart) * lengthInExonRatio);
                        } else {
                            rec.setWidth((rightExonXstart + rightExonXend) / 2 - (leftExonXstart + leftExonXend) / 2);
                        }
                    } else {
                        rec.setX(xstart + (xend - xstart) * ((double) startInExon / exonLength));

                        int lengthInExon = peptideSeq.length() * 3;
                        if (startInExon + lengthInExon < exonLength) {
                            rec.setWidth((xend - xstart) * ((double) lengthInExon / exonLength));
                        } else {
                            rec.setWidth((rightExonXstart + rightExonXend) / 2 - (xstart+ (xend - xstart) * ((double) startInExon / exonLength)));
                        }
                    }


                    group.getChildren().add(rec);
                }
            }

            if(doc.get("peptidesOut", JSONObject.class).containsKey(selectedRunRepresentation.getSelectionModel().getSelectedItem())) {
                for (Object o : (JSONArray) doc.get("peptidesIn", JSONObject.class)
                        .get(selectedRunRepresentation.getSelectionModel().getSelectedItem())) {

                    Rectangle rec = new Rectangle();
                    rec.setStroke(Color.BLACK);
                    rec.setStrokeWidth(3);
                    rec.setWidth((xend - xstart) * 0.6);
                    rec.setY(100);
                    rec.setHeight(20);
                    rec.setFill(RED);

                    rec.setX((leftExonXstart + leftExonXend) / 2);
                    rec.setWidth((rightExonXstart + rightExonXend) / 2 - (leftExonXstart + leftExonXend) / 2);


                    group.getChildren().add(rec);
                }
            }



            if(doc.containsKey("proteinCorrectedRatios") &&
                    doc.get("proteinCorrectedRatios", JSONObject.class).containsKey(selectedRunRepresentation.getSelectionModel().getSelectedItem())){

                final CategoryAxis xAxis = new CategoryAxis();
                final NumberAxis yAxis = new NumberAxis();

                BarChart<String,Number> bc =
                        new BarChart<>(xAxis, yAxis);
                bc.setTitle("Differential protein splicing");
                bc.setLegendVisible(false);

                XYChart.Series proteinCorrectedRatiosSeries = new XYChart.Series<>();

                JSONObject intensities = (JSONObject) doc.get("proteinCorrectedRatios", JSONObject.class)
                        .get(selectedRunRepresentation.getSelectionModel().getSelectedItem());

                for(Object channel: intensities.keySet()){
                    if(intensities.get(channel)!=null){
                        proteinCorrectedRatiosSeries.getData().add(new XYChart.Data(channel, intensities.get(channel)));
                    }

                }
                
                bc.getData().add(proteinCorrectedRatiosSeries);
                GridPane.setColumnIndex(bc, 0);
                representationChartsBox.getChildren().add(bc);





                if(doc.containsKey("proteinPeptidesRatios") &&
                        doc.get("proteinPeptidesRatios", JSONObject.class).containsKey(selectedRunRepresentation.getSelectionModel().getSelectedItem())){

                    ConfidentBarChart proteinPeptidesRatiosChart = new ConfidentBarChart();
                    JSONArray peptidesRatios = (JSONArray) doc.get("proteinPeptidesRatios", JSONObject.class)
                            .get(selectedRunRepresentation.getSelectionModel().getSelectedItem());

                    HashMap<String, ArrayList<Double>> channels = new HashMap<>();

                    for(Object o: peptidesRatios){
                        JSONObject peptideChannels = ((JSONObject) o);

                        for(Object c: peptideChannels.keySet()){
                            String channel = (String) c;
                            if(!channels.containsKey(channel)){
                                channels.put(channel, new ArrayList<>());
                            }
                            channels.get(channel).add((double) peptideChannels.get(channel));

                        }
                    }

                    proteinPeptidesRatiosChart.addAll(channels);
                    proteinPeptidesRatiosChart.draw();
                    GridPane.setColumnIndex(proteinPeptidesRatiosChart, 1);
                    representationChartsBox.getChildren().add(proteinPeptidesRatiosChart);

                }

                if(doc.containsKey("eventPeptidesRatios") &&
                        doc.get("eventPeptidesRatios", JSONObject.class).containsKey(selectedRunRepresentation.getSelectionModel().getSelectedItem())){

                    ConfidentBarChart eventPeptidesRatiosChart = new ConfidentBarChart();
                    JSONArray peptidesRatios = (JSONArray) doc.get("eventPeptidesRatios", JSONObject.class)
                            .get(selectedRunRepresentation.getSelectionModel().getSelectedItem());

                    HashMap<String, ArrayList<Double>> channels = new HashMap<>();

                    for(Object o: peptidesRatios){
                        JSONObject peptideChannels = ((JSONObject) o);

                        for(Object c: peptideChannels.keySet()){
                            String channel = (String) c;
                            if(!channels.containsKey(channel)){
                                channels.put(channel, new ArrayList<>());
                            }
                            channels.get(channel).add((double) peptideChannels.get(channel));

                        }
                    }

                    eventPeptidesRatiosChart.addAll(channels);
                    eventPeptidesRatiosChart.draw();
                    GridPane.setColumnIndex(eventPeptidesRatiosChart, 2);
                    representationChartsBox.getChildren().add(eventPeptidesRatiosChart);

                }



            }

        }

    }


    private void drawArrow(String strand){

        arrowRepresentationPane.getChildren().clear();
        Group group = new Group();

        double prefW = arrowRepresentationPane.getWidth();
        double prefH = arrowRepresentationPane.getHeight();


        if (strand.equals("+")){
            Path arrowHead = new Path(new MoveTo(perctToVal(60,prefW), perctToVal(30, prefH)),
                    new LineTo((perctToVal(60,prefW) + 30 ), perctToVal(50, prefH)),
                    new LineTo(perctToVal(60,prefW),perctToVal(70, prefH)),
                    new ClosePath());
            arrowHead.setFill(GRAY);
            arrowHead.setStroke(GRAY);
            group.getChildren().add(arrowHead);


            Path arrowLine = new Path(new MoveTo(perctToVal(30,prefW), perctToVal(50, prefH)),
                    new LineTo((perctToVal(60,prefW)  ), perctToVal(50, prefH))
            );
            arrowLine.setFill(GRAY);
            arrowLine.setStroke(GRAY);
            arrowLine.prefHeight(5);
            group.getChildren().add(arrowLine);



        } else { // negative strand
            Path arrowHead = new Path(new MoveTo(perctToVal(30,prefW), perctToVal(30, prefH)),
                    new LineTo((perctToVal(30,prefW) - 30 ), perctToVal(50, prefH)),
                    new LineTo(perctToVal(30,prefW),perctToVal(70, prefH)),
                    new ClosePath());
            arrowHead.setFill(GRAY);
            arrowHead.setStroke(GRAY);
            group.getChildren().add(arrowHead);


            Path arrowLine = new Path(new MoveTo(perctToVal(60,prefW), perctToVal(50, prefH)),
                    new LineTo((perctToVal(30,prefW)  ), perctToVal(50, prefH))
            );
            arrowLine.setFill(GRAY);
            arrowLine.setStroke(GRAY);
            arrowLine.prefHeight(5);
            group.getChildren().add(arrowLine);


        }

        arrowRepresentationPane.getChildren().add(group);
    }


    private void drawSEPosNeg(String eventID, int firstExonEnd, int currentExonStart, int currentExonEnd, int lastExonStart){

        exonRepresentationPane.getChildren().clear();
        Group group = new Group();

        double prefW = exonRepresentationPane.getWidth();
        double prefH = exonRepresentationPane.getHeight();
        Text text = new Text("I");
        int fontSize = (int) Math.round(perctToVal(10, prefH));
        text.setFont(Font.font("monospace", fontSize));
        double fontHeight  = text.getLayoutBounds().getHeight();
        double textWidth;
        int yTop = 40;
        int yBottom = 60;
        int yMax = yTop -  20;
        int yMin = yBottom + 20;
        double yValTop = perctToVal(yTop, prefH);
        double yValBottom = perctToVal(yBottom,prefH);
        double yValMin = perctToVal(yMin,prefH);
        double yValMax = perctToVal(yMax,prefH);
        double textTopYVal = perctToVal(yTop ,prefH) - (fontHeight * 0.5);
        double textBottomYVal = perctToVal(yBottom ,prefH) + (fontHeight * 1.5);


        Path firstExon = new Path(new MoveTo(0, yValTop),
                new LineTo(perctToVal(20,prefW), yValTop ), // top right
                new LineTo(perctToVal(20,prefW), yValBottom), // bottom right
                new LineTo(0, yValBottom));

        firstExon.setFill(WHITE);
        group.getChildren().add(firstExon);

        Path currExon = new Path(new MoveTo(perctToVal(35,prefW),yValTop), //  left top
                new LineTo(perctToVal(65,prefW),yValTop), // right top
                new LineTo(perctToVal(65,prefW),yValBottom),
                new LineTo(perctToVal(35,prefW),yValBottom),
                new ClosePath());
        currExon.setFill(BLACK);

        group.getChildren().add(currExon);

        Path lastExonOpenRect = new Path(new MoveTo(perctToVal(100,prefW),yValTop),
                new LineTo(perctToVal(80,prefW),yValTop),
                new LineTo(perctToVal(80,prefW),yValBottom), // left bottom
                new LineTo(perctToVal(100,prefW),yValBottom));
        lastExonOpenRect.setFill(WHITE);

        group.getChildren().add(lastExonOpenRect);


        Path union1Top = new Path(new MoveTo(perctToVal(20,prefW), yValTop),
                new LineTo(perctToVal((20 + (35-20)/2),prefW), yValMax),
                new LineTo(perctToVal(35,prefW), yValTop)); // end

        group.getChildren().add(union1Top);

        Path union2Top = new Path(new MoveTo(perctToVal(65,prefW),yValTop),
                new LineTo(perctToVal((65 + (80-65)/2),prefW),yValMax),
                new LineTo(perctToVal(80,prefW),yValTop)); // end
        group.getChildren().add(union2Top);

        Path union1Bottom = new Path(new MoveTo(perctToVal(20,prefW),yValBottom),
                new LineTo(perctToVal((20 + (80-20)/2),prefW),yValMin),
                new LineTo(perctToVal(80,prefW),yValBottom));

        group.getChildren().add(union1Bottom);


        text = new Text(Integer.toString(firstExonEnd));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(20,prefW) - (textWidth / 2.0));
        text.setY(textBottomYVal);
        group.getChildren().add(text);

        text = new Text(Integer.toString(currentExonStart));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(35,prefW) - (textWidth / 2.0));
        text.setY(textTopYVal );
        group.getChildren().add(text);

        text = new Text(Integer.toString(currentExonEnd));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(65,prefW) - (textWidth / 2.0));
        text.setY(textTopYVal );
        group.getChildren().add(text);

        text = new Text(Integer.toString(lastExonStart));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(80,prefW) - (textWidth / 2.0));
        text.setY(textBottomYVal );
        group.getChildren().add(text);

        addPeptideToRepresentation(eventID, group, perctToVal(35,prefW), perctToVal(65,prefW), 0, perctToVal(20,prefW),
                perctToVal(80,prefW), perctToVal(100,prefW));


        exonRepresentationPane.getChildren().add(group);
    }


    private void drawMXPosNeg(int firstExonEnd, int currentExonStart, int currentExonEnd, int nextExonStart, int nextExonEnd,  int lastExonStart){

        exonRepresentationPane.getChildren().clear();
        Group group = new Group();

        double prefW = exonRepresentationPane.getWidth();
        double prefH = exonRepresentationPane.getHeight();
        Text text = new Text("I");
        int fontSize = (int) Math.round(perctToVal(10, prefH));
        text.setFont(Font.font("monospace", fontSize));
        double fontHeight  = text.getLayoutBounds().getHeight();
        double textWidth;
        int yTop = 30;
        int yBottom = 60;
        int yMax = yTop -  20;
        int yMin = yBottom + 20;
        double yValTop = perctToVal(yTop, prefH);
        double yValBottom = perctToVal(yBottom,prefH);
        double yValMin = perctToVal(yMin,prefH);
        double yValMax = perctToVal(yMax,prefH);
        double textTopYVal = perctToVal(yTop ,prefH) - (fontHeight * 0.5);
        double textBottomYVal = perctToVal(yBottom ,prefH) + (fontHeight * 1.5);


        Path firstExon = new Path(new MoveTo(0, yValTop),
                new LineTo(perctToVal(10,prefW), yValTop), // top right
                new LineTo(perctToVal(10,prefW), yValBottom), // bottom right
                new LineTo(0,yValBottom));

        firstExon.setFill(WHITE);
        group.getChildren().add(firstExon);

        text = new Text(Integer.toString(firstExonEnd));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(10,prefW) - (textWidth / 2.0));
        text.setY(perctToVal(yBottom ,prefH) + (fontHeight * 1.5) );
        group.getChildren().add(text);


        Path currExon = new Path(new MoveTo(perctToVal(30,prefW),yValTop), //  left top
                new LineTo(perctToVal(55,prefW),yValTop), // right top
                new LineTo(perctToVal(55,prefW),yValBottom),
                new LineTo(perctToVal(30,prefW),yValBottom),
                new ClosePath());
        currExon.setFill(BLACK);

        group.getChildren().add(currExon);


        Path nextExon = new Path(new MoveTo(perctToVal(60,prefW),yValTop), //  left top
                new LineTo(perctToVal(80,prefW),yValTop), // right top
                new LineTo(perctToVal(80,prefW),yValBottom),
                new LineTo(perctToVal(60,prefW),yValBottom),
                new ClosePath());
        nextExon.setFill(LIGHTGRAY);

        group.getChildren().add(nextExon);


        Path lastExonOpenRect = new Path(new MoveTo(perctToVal(100,prefW),yValTop),
                new LineTo(perctToVal(90,prefW),yValTop),
                new LineTo(perctToVal(90,prefW),yValBottom), // left bottom
                new LineTo(perctToVal(100,prefW),yValBottom));
        lastExonOpenRect.setFill(WHITE);

        group.getChildren().add(lastExonOpenRect);



        Path union1Top = new Path(new MoveTo(perctToVal(10,prefW), yValTop),
                new LineTo(perctToVal(20,prefW), yValMax),
                new LineTo(perctToVal(30,prefW), yValTop)); // end

        group.getChildren().add(union1Top);


        Path union1Bottom = new Path(new MoveTo(perctToVal(10,prefW),yValBottom),
                new LineTo(perctToVal((10 + (60-10)/2),prefW),yValMin),
                new LineTo(perctToVal(60,prefW),yValBottom));

        group.getChildren().add(union1Bottom);


        Path union2Bottom = new Path(new MoveTo(perctToVal(80,prefW),yValBottom),
                new LineTo(perctToVal((80 + (90-80)/2),prefW),yValMin),
                new LineTo(perctToVal(90,prefW),yValBottom));

        group.getChildren().add(union2Bottom);

        Path union2Top = new Path(new MoveTo(perctToVal(55,prefW), yValTop),
                new LineTo(perctToVal((55 + (90 - 55 )/2),prefW), yValMax),
                new LineTo(perctToVal(90,prefW), yValTop)); // end

        group.getChildren().add(union2Top);

        text = new Text(Integer.toString(nextExonStart));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(60,prefW) - (textWidth / 2.0));
        text.setY(textBottomYVal );
        group.getChildren().add(text);


        text = new Text(Integer.toString(currentExonStart));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(30,prefW) - (textWidth / 2.0));
        text.setY(textTopYVal);
        group.getChildren().add(text);

        text = new Text(Integer.toString(currentExonEnd));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(55,prefW) - (textWidth / 2.0));
        text.setY(textTopYVal );
        group.getChildren().add(text);


        text = new Text(Integer.toString(nextExonEnd));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(80,prefW) - (textWidth / 2.0));
        text.setY(textBottomYVal );
        group.getChildren().add(text);

        text = new Text(Integer.toString(lastExonStart));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(90,prefW) - (textWidth / 2.0));
        text.setY(textBottomYVal );
        group.getChildren().add(text);

        // add the representation
        exonRepresentationPane.getChildren().add(group);
    }


    private void drawA5PosA3Neg(int firstExonEnd, int currentExonEnd, int lastExonStart){

        exonRepresentationPane.getChildren().clear();
        Group group = new Group();

        double prefW = exonRepresentationPane.getWidth();
        double prefH = exonRepresentationPane.getHeight();
        Text text = new Text("I");
        int fontSize = (int) Math.round(perctToVal(10, prefH));
        text.setFont(Font.font("monospace", fontSize));
        double fontHeight  = text.getLayoutBounds().getHeight();
        double textWidth;
        int yTop = 30;
        int yBottom = 60;
        int yMax = yTop -  20;
        int yMin = yBottom + 20;
        double yValTop = perctToVal(yTop, prefH);
        double yValBottom = perctToVal(yBottom,prefH);
        double yValMin = perctToVal(yMin,prefH);
        double yValMax = perctToVal(yMax,prefH);
        double textTopYVal = perctToVal(yTop ,prefH) - (fontHeight * 0.5);
        double textBottomYVal = perctToVal(yBottom ,prefH) + (fontHeight * 1.5);

        Path firstExon = new Path(new MoveTo(0, yValTop),
                new LineTo(perctToVal(20,prefW), yValTop), // top right
                new LineTo(perctToVal(20,prefW), yValBottom), // bottom right
                new LineTo(0,yValBottom));

        firstExon.setFill(LIGHTGRAY);
        group.getChildren().add(firstExon);


        Path currExon = new Path(new MoveTo(perctToVal(20,prefW),yValTop), //  left top
                new LineTo(perctToVal(60,prefW),yValTop), // right top
                new LineTo(perctToVal(60,prefW),yValBottom),
                new LineTo(perctToVal(20,prefW),yValBottom),
                new ClosePath());
        currExon.setFill(BLACK);

        group.getChildren().add(currExon);


        Path lastExonOpenRect = new Path(new MoveTo(perctToVal(100,prefW),yValTop),
                new LineTo(perctToVal(90,prefW),yValTop),
                new LineTo(perctToVal(90,prefW),yValBottom), // left bottom
                new LineTo(perctToVal(100,prefW),yValBottom));
        lastExonOpenRect.setFill(WHITE);

        group.getChildren().add(lastExonOpenRect);


        Path union1Top = new Path(new MoveTo(perctToVal(60,prefW), yValTop),
                new LineTo(perctToVal((60 + (90-60)/2),prefW), yValMax),
                new LineTo(perctToVal(90,prefW), yValTop)); // end

        group.getChildren().add(union1Top);


        Path union1Bottom = new Path(new MoveTo(perctToVal(20,prefW),yValBottom),
                new LineTo(perctToVal((20 + (90-20)/2),prefW),yValMin),
                new LineTo(perctToVal(90,prefW),yValBottom));

        group.getChildren().add(union1Bottom);


        text = new Text(Integer.toString(firstExonEnd));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(20,prefW) - (textWidth / 2.0));
        text.setY(textBottomYVal);
        group.getChildren().add(text);

        text = new Text(Integer.toString(currentExonEnd));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(60,prefW) - (textWidth / 2.0));
        text.setY(textTopYVal );
        group.getChildren().add(text);

        text = new Text(Integer.toString(lastExonStart));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(90,prefW) - (textWidth / 2.0));
        text.setY(textBottomYVal );
        group.getChildren().add(text);

        // add the representation
        exonRepresentationPane.getChildren().add(group);
    }


    private void drawA3PosA5Neg(int firstExonEnd, int currentExonStart, int lastExonStart){

        exonRepresentationPane.getChildren().clear();
        Group group = new Group();

        double prefW = exonRepresentationPane.getWidth();
        double prefH = exonRepresentationPane.getHeight();
        Text text = new Text("I");
        int fontSize = (int) Math.round(perctToVal(10, prefH));
        text.setFont(Font.font("monospace", fontSize));
        double fontHeight  = text.getLayoutBounds().getHeight();
        double textWidth;
        int yTop = 30;
        int yBottom = 60;
        int yMax = yTop -  20;
        int yMin = yBottom + 20;
        double yValTop = perctToVal(yTop, prefH);
        double yValBottom = perctToVal(yBottom,prefH);
        double yValMin = perctToVal(yMin,prefH);
        double yValMax = perctToVal(yMax,prefH);
        double textTopYVal = perctToVal(yTop ,prefH) - (fontHeight * 0.5);
        double textBottomYVal = perctToVal(yBottom ,prefH) + (fontHeight * 1.5);

        // exons
        Path firstExon = new Path(new MoveTo(0, yValTop),
                new LineTo(perctToVal(10,prefW), yValTop), // top right
                new LineTo(perctToVal(10,prefW), yValBottom), // bottom right
                new LineTo(0,yValBottom));

        firstExon.setFill(WHITE);
        group.getChildren().add(firstExon);

        Path currExon = new Path(new MoveTo(perctToVal(40,prefW),yValTop), //  left top
                new LineTo(perctToVal(80,prefW),yValTop), // right top
                new LineTo(perctToVal(80,prefW),yValBottom),
                new LineTo(perctToVal(40,prefW),yValBottom),
                new ClosePath());
        currExon.setFill(BLACK);

        group.getChildren().add(currExon);


        Path lastExonOpenRect = new Path(new MoveTo(perctToVal(100,prefW),yValTop),
                new LineTo(perctToVal(80,prefW),yValTop),
                new LineTo(perctToVal(80,prefW),yValBottom), // left bottom
                new LineTo(perctToVal(100,prefW),yValBottom));
        lastExonOpenRect.setFill(LIGHTGRAY);

        group.getChildren().add(lastExonOpenRect);


        // texts

        text = new Text(Integer.toString(firstExonEnd));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(10,prefW) - (textWidth / 2.0));
        text.setY(textBottomYVal);
        group.getChildren().add(text);


        text = new Text(Integer.toString(currentExonStart));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(40,prefW) - (textWidth / 2.0));
        text.setY(textTopYVal );
        group.getChildren().add(text);


        text = new Text(Integer.toString(lastExonStart));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(80,prefW) - (textWidth / 2.0));
        text.setY(textBottomYVal );
        group.getChildren().add(text);


        // unions
        Path union1Top = new Path(new MoveTo(perctToVal(10,prefW), yValTop),
                new LineTo(perctToVal((10 + (40-10)/2),prefW), yValMax),
                new LineTo(perctToVal(40,prefW), yValTop)); // end

        group.getChildren().add(union1Top);

        Path union1Bottom = new Path(new MoveTo(perctToVal(10,prefW),yValBottom),
                new LineTo(perctToVal((10 + (80-10)/2),prefW),yValMin),
                new LineTo(perctToVal(80,prefW),yValBottom));

        group.getChildren().add(union1Bottom);




        // add the representation
        exonRepresentationPane.getChildren().add(group);
    }


    private void drawRIPosNeg(String eventID, int firstExonStart, int currentExonStart, int currentExonEnd, int lastExonEnd){

        exonRepresentationPane.getChildren().clear();
        Group group = new Group();

        double prefW = exonRepresentationPane.getWidth();
        double prefH = exonRepresentationPane.getHeight();
        Text text = new Text("I");
        int fontSize = (int) Math.round(perctToVal(10, prefH));
        text.setFont(Font.font("monospace", fontSize));
        double fontHeight  = text.getLayoutBounds().getHeight();
        double textWidth;
        int yTop = 30;
        int yBottom = 60;
        int yMax = yTop -  20;
        int yMin = yBottom + 20;
        double yValTop = perctToVal(yTop, prefH);
        double yValBottom = perctToVal(yBottom,prefH);
        double yValMin = perctToVal(yMin,prefH);
        double yValMax = perctToVal(yMax,prefH);
        double textTopYVal = perctToVal(yTop ,prefH) - (fontHeight * 0.5);
        double textBottomYVal = perctToVal(yBottom ,prefH) + (fontHeight * 1.5);

        // exons
        Path firstExon = new Path(new MoveTo(0, yValTop),
                new LineTo(perctToVal(100,prefW), yValTop), // top right
                new LineTo(perctToVal(100,prefW), yValBottom), // bottom right
                new LineTo(0,yValBottom),
                new ClosePath());

        firstExon.setFill(WHITE);
        group.getChildren().add(firstExon);

        Path currExon = new Path(new MoveTo(perctToVal(20,prefW),yValTop), //  left top
                new LineTo(perctToVal(80,prefW),yValTop), // right top
                new LineTo(perctToVal(80,prefW),yValBottom),
                new LineTo(perctToVal(20,prefW),yValBottom),
                new ClosePath());
        currExon.setFill(BLACK);

        group.getChildren().add(currExon);


        // texts

        text = new Text(Integer.toString(firstExonStart));
        text.setFont(Font.font("monospace", fontSize));
        text.setX(0);
        text.setY(textTopYVal);
        group.getChildren().add(text);


        text = new Text(Integer.toString(currentExonStart));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(20,prefW) - (textWidth / 2.0));
        text.setY(textTopYVal );
        group.getChildren().add(text);


        text = new Text(Integer.toString(currentExonEnd));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(80,prefW) - (textWidth / 2.0));
        text.setY(textTopYVal );
        group.getChildren().add(text);


        text = new Text(Integer.toString(lastExonEnd));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(100,prefW) - (textWidth ));
        text.setY(textTopYVal );
        group.getChildren().add(text);


        // unions
        Path union1Bottom = new Path(new MoveTo(perctToVal(20,prefW),yValBottom),
                new LineTo(perctToVal((20 + (80-20)/2),prefW),yValMin),
                new LineTo(perctToVal(80,prefW),yValBottom));

        group.getChildren().add(union1Bottom);




        // add the representation
        exonRepresentationPane.getChildren().add(group);

        addPeptideToRepresentation(eventID, group, perctToVal(35,prefW), perctToVal(65,prefW), 0, perctToVal(20,prefW),
                perctToVal(80,prefW), perctToVal(100,prefW));
    }


    private void drawAFPosALNeg(String eventID, int currentExonStart, int currentExonEnd, int nextExonStart, int nextExonEnd, int lastExonStart){

        exonRepresentationPane.getChildren().clear();
        Group group = new Group();

        double prefW = exonRepresentationPane.getWidth();
        double prefH = exonRepresentationPane.getHeight();
        Text text = new Text("I");
        int fontSize = (int) Math.round(perctToVal(10, prefH));
        text.setFont(Font.font("monospace", fontSize));
        double fontHeight  = text.getLayoutBounds().getHeight();
        double textWidth;
        int yTop = 40;
        int yBottom = 60;
        int yMax = yTop -  20;
        int yMin = yBottom + 20;
        double yValTop = perctToVal(yTop, prefH);
        double yValBottom = perctToVal(yBottom,prefH);
        double yValMin = perctToVal(yMin,prefH);
        double yValMax = perctToVal(yMax,prefH);
        double textTopYVal = perctToVal(yTop ,prefH) - (fontHeight * 0.5);
        double textBottomYVal = perctToVal(yBottom ,prefH) + (fontHeight * 1.5);

        // exons

        Path currExon = new Path(new MoveTo(perctToVal(0,prefW),yValTop), //  left top
                new LineTo(perctToVal(20,prefW),yValTop), // right top
                new LineTo(perctToVal(20,prefW),yValBottom),
                new LineTo(perctToVal(0,prefW),yValBottom),
                new ClosePath());
        currExon.setFill(BLACK);

        group.getChildren().add(currExon);

        Path nextExon = new Path(new MoveTo(perctToVal(35,prefW), yValTop),
                new LineTo(perctToVal(65,prefW), yValTop), // top right
                new LineTo(perctToVal(65,prefW), yValBottom), // bottom right
                new LineTo(perctToVal(35,prefW),yValBottom),
                new ClosePath());
//
        nextExon.setFill(LIGHTGRAY);
        group.getChildren().add(nextExon);


        Path lastExon = new Path(new MoveTo(perctToVal(100,prefW),yValTop),
                new LineTo(perctToVal(80,prefW),yValTop),
                new LineTo(perctToVal(80,prefW),yValBottom), // left bottom
                new LineTo(perctToVal(100,prefW),yValBottom));
        lastExon.setFill(WHITE);

        group.getChildren().add(lastExon);


        // texts


        text = new Text(Integer.toString(currentExonStart));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(0);
        text.setY(textTopYVal );
        group.getChildren().add(text);

        text = new Text(Integer.toString(currentExonEnd));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(35,prefW) - (textWidth / 2.0));
        text.setY(textTopYVal );
        group.getChildren().add(text);


        text = new Text(Integer.toString(nextExonStart));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(65,prefW) - (textWidth / 2.0));
        text.setY(textBottomYVal);
        group.getChildren().add(text);

        text = new Text(Integer.toString(nextExonEnd));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(80,prefW) - (textWidth / 2.0));
        text.setY(textBottomYVal);
        group.getChildren().add(text);



        // unions
        Path union1Top = new Path(new MoveTo(perctToVal(20,prefW), yValTop),
                new LineTo(perctToVal((20 + (80-20)/2),prefW), yValMax),
                new LineTo(perctToVal(80,prefW), yValTop)); // end

        group.getChildren().add(union1Top);

        Path union1Bottom = new Path(new MoveTo(perctToVal(65,prefW),yValBottom),
                new LineTo(perctToVal((65 + (80-65)/2),prefW),yValMin),
                new LineTo(perctToVal(80,prefW),yValBottom));

        group.getChildren().add(union1Bottom);


        // add the representation
        exonRepresentationPane.getChildren().add(group);

        addPeptideToRepresentation(eventID, group, perctToVal(35,prefW), perctToVal(65,prefW), 0, perctToVal(20,prefW),
                perctToVal(80,prefW), perctToVal(100,prefW));
    }



    private void drawALPosAFNeg(String eventID, int firstExonEnd, int prevExonStart, int prevExonEnd, int currentExonStart, int currentExonEnd){

        exonRepresentationPane.getChildren().clear();
        Group group = new Group();

        double prefW = exonRepresentationPane.getWidth();
        double prefH = exonRepresentationPane.getHeight();
        Text text = new Text("I");
        int fontSize = (int) Math.round(perctToVal(10, prefH));
        text.setFont(Font.font("monospace", fontSize));
        double fontHeight  = text.getLayoutBounds().getHeight();
        double textWidth;
        int yTop = 30;
        int yBottom = 60;
        int yMax = yTop -  20;
        int yMin = yBottom + 20;
        double yValTop = perctToVal(yTop, prefH);
        double yValBottom = perctToVal(yBottom,prefH);
        double yValMin = perctToVal(yMin,prefH);
        double yValMax = perctToVal(yMax,prefH);
        double textTopYVal = perctToVal(yTop ,prefH) - (fontHeight * 0.5);
        double textBottomYVal = perctToVal(yBottom ,prefH) + (fontHeight * 1.5);

        // exons

        Path firstExon = new Path(new MoveTo(perctToVal(0,prefW),yValTop), //  left top
                new LineTo(perctToVal(10,prefW),yValTop), // right top
                new LineTo(perctToVal(10,prefW),yValBottom),
                new LineTo(perctToVal(0,prefW),yValBottom));
        firstExon.setFill(WHITE);

        group.getChildren().add(firstExon);


        Path prevExon = new Path(new MoveTo(perctToVal(20,prefW),yValTop), //  left top
                new LineTo(perctToVal(50,prefW),yValTop), // right top
                new LineTo(perctToVal(50,prefW),yValBottom),
                new LineTo(perctToVal(20,prefW),yValBottom),
                new ClosePath());
        prevExon.setFill(LIGHTGRAY);

        group.getChildren().add(prevExon);

        Path currExon = new Path(new MoveTo(perctToVal(60,prefW),yValTop), //  left top
                new LineTo(perctToVal(100,prefW),yValTop), // right top
                new LineTo(perctToVal(100,prefW),yValBottom),
                new LineTo(perctToVal(60,prefW),yValBottom),
                new ClosePath());
        currExon.setFill(BLACK);
        group.getChildren().add(currExon);


        // texts

        text = new Text(Integer.toString(firstExonEnd));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(10,prefW) - (textWidth / 2.0));
        text.setY(textTopYVal );
        group.getChildren().add(text);

        text = new Text(Integer.toString(prevExonStart));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(20,prefW) - (textWidth / 2.0));
        text.setY(textBottomYVal );
        group.getChildren().add(text);

        text = new Text(Integer.toString(prevExonEnd));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(50,prefW) - (textWidth / 2.0));
        text.setY(textBottomYVal);
        group.getChildren().add(text);

        text = new Text(Integer.toString(currentExonStart));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(60,prefW) - (textWidth / 2.0));
        text.setY(textTopYVal );
        group.getChildren().add(text);

        text = new Text(Integer.toString(currentExonEnd));
        text.setFont(Font.font("monospace", fontSize));
        textWidth = text.getLayoutBounds().getWidth();
        text.setX(perctToVal(100,prefW) - (textWidth));
        text.setY(textTopYVal );
        group.getChildren().add(text);


        // unions
        Path union1Top = new Path(new MoveTo(perctToVal(10,prefW), yValTop),
                new LineTo(perctToVal((10 + (60-10)/2),prefW), yValMax),
                new LineTo(perctToVal(60,prefW), yValTop)); // end

        group.getChildren().add(union1Top);

        Path union1Bottom = new Path(new MoveTo(perctToVal(10,prefW),yValBottom),
                new LineTo(perctToVal((10 + (20-10)/2),prefW),yValMin),
                new LineTo(perctToVal(20,prefW),yValBottom));

        group.getChildren().add(union1Bottom);


        // add the representation
        exonRepresentationPane.getChildren().add(group);

        addPeptideToRepresentation(eventID, group, perctToVal(35,prefW), perctToVal(65,prefW), 0, perctToVal(20,prefW),
                perctToVal(80,prefW), perctToVal(100,prefW));
    }


    private Double perctToVal(int value, double prefValue){
        return ( ((double) value / 100.0)  * prefValue );
    }

    public void resize(){
        if(splicingEventsTableView.getSelectionModel().getSelectedItem()!=null){
            SplicingEventsTableModel se = splicingEventsTableView.getSelectionModel().getSelectedItem();
            drawSplicingEventRepresentation(se.getEventKey(), se.getStrand(), se.getEventType());
        }
    }

    public ObservableList<SplicingEventsTableModel> getTableData(){
        return splicingEventsTableView.getItems();
    }

    public Collection<SplicingEventsTableModel> getDataTableBiggestGeneDpsi(){
        HashMap<String, SplicingEventsTableModel> genes = new HashMap<>();
        for(SplicingEventsTableModel event: splicingEventsTableView.getItems()){
            if(!genes.containsKey(event.getGeneSymbol())){
                genes.put(event.getGeneSymbol(), event);
            }else{
                if(genes.get(event.getGeneSymbol()).getDeltaPsi()<event.getDeltaPsi()){
                    genes.replace(event.getGeneSymbol(), event);
                }
            }
        }
        return genes.values();
    }

    @Override
    public String getSelectedComparison(){
        return comparisonSplicingCombobox.getValue();
    }

}
