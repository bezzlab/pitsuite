package Controllers;

import Cds.*;
import Controllers.drawerControllers.DrawerController;
import FileReading.AllGenesReader;
import FileReading.FastaIndex;
import Gene.Gene;
import Singletons.ControllersBasket;
import Singletons.Database;
import TablesModels.BamFile;
import TablesModels.Variation;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.PauseTransition;
import javafx.application.HostServices;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.IndexedCheckModel;
import org.controlsfx.control.RangeSlider;
import org.controlsfx.control.textfield.TextFields;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.filters.Filters;
import org.json.simple.JSONObject;
import pitguiv2.App;
import Singletons.Config;
import utilities.BioFile;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Comparator.comparing;

public class GeneBrowserController implements Initializable {


    @FXML
    private Label currentPositionPane;
    @FXML
    private TabPane infoTitleTabs;
    @FXML
    private CheckComboBox<String> transcriptsComboBox;
    @FXML
    private JFXTextField findSeqField;
    @FXML
    private Button findSeqButton;
    @FXML
    private Label seqSearchMatchIndexLabel;
    //main tabs
    @FXML
    private TabPane browsersTabTabPane;
    @FXML
    private Tab genomeBrowserTab;
    @FXML
    private Tab geneBrowserTab;

    //textfields
    @FXML
    private TextField geneIdTextField;
    @FXML
    private Button queryGeneDataBaseButton;


    // sequence viewer controls

    @FXML
    private Button geneBrowserMoveLeftButton;
    @FXML
    private Label currentCoordinateTextField;
    @FXML
    Label qualLabel;
    @FXML
    Label sortTPMLabel;
    @FXML
    Label tpmLabel;
    @FXML
    private Button geneBrowserMoveRightButton;

    @FXML
    private AnchorPane browserAnchorPane;
    @FXML
    private VBox geneExonsSeqsVBox;
    @FXML
    private Pane verticalLineGeneBrowserPane;

    @FXML
    private Pane geneHBoxSlider;

    private RangeSlider geneSlider;


    // exons
    @FXML
    private Button zoomFullSeqExons;
    @FXML
    private Button zoomInExonButton;
    @FXML
    private Button zoomOutExonButton;


    @FXML
    private JFXComboBox<String> conditionsGeneBrowserCombobox;
    @FXML
    private JFXTextField minTpmFilterGeneBrowserTextField;
    @FXML
    private JFXTextField minQualFilterGeneBrowserTextField;

    // menu items
    @FXML
    private MenuItem displayCdsGeneBrowserMenuItem;
    @FXML
    private MenuItem showHideDepthGeneBrowserMenuItem;
    @FXML
    private MenuItem cdsCentricViewMenuItem;
    @FXML
    private MenuItem transcriptCentricViewMenuItem;
    @FXML
    private MenuItem tpmChartMenuItem;

    @FXML
    private VBox transcriptionVBox;
    @FXML
    private VBox cdsVBox;

    // tabs
    @FXML
    private TabPane bottomTabPane;
    @FXML
    private Tab mutationsTab;


    // the table itself
    @FXML
    private TableView<Variation> varTableView;
    @FXML
    private TableColumn<Variation, String> geneVarTableColumn;
    @FXML
    private TableColumn<Variation, String> coordVarTableColumn;
    @FXML
    private TableColumn<Variation, String> refVarTableColumn;
    @FXML
    private TableColumn<Variation, String> altVarTableColumn;
    @FXML
    private TableColumn<Variation, Boolean> synonymousVarTableColumn;
    @FXML
    private TableColumn<Variation, Boolean> inCdsVarTableColumn;
    @FXML
    private TableColumn<Variation, Boolean> peptEvidenceVarTableColumn;




    @FXML
    private VBox rnaAASeqsCdsTabVBox;

    @FXML
    private VBox cdsRectanglesVBox;
    @FXML
    private BamController bamPaneController;
    @FXML
    private BedController bedPaneController;





    @FXML
    private TitledPane extraInfoTitledPane;
    @FXML
    private ScrollPane geneBrowserScrollPane;
    @FXML
    public GridPane browserGrid;
    @FXML
    private Pane selectionZoomRectanglePane;

    @FXML
    private MutatedCdsController mutatedCdsController;

    @FXML
    private JFXDrawer drawer;






    // elements in browser
    private boolean isAGeneDisplayed;
    private boolean showCdsInGeneBrowserBool;
    private boolean showDepthInGeneBrowserBool;

    private String selectedGene = null;


    private ResultsController parentController;
    private String databaseProjectName;

    private boolean activateListenerConditionsGeneBrowserCombobox;

    private HashMap<String, String> transcFormatedSequencesMap;
    private HashMap<String, Transcript> transcriptHashMap;
    private ArrayList<CdsCondSample> cdsCondSamplePerTranscCondSample;

    private LinkedList<Variation> variations;

    private String viewType;

    // private

    private double representationWidthFinal;
    private double representationHeightFinal;


    // minimum and maximum coordinates
    private int geneMinimumCoordinate;
    private int geneMaximumCoordinate;
    private int geneViewerMinimumCoordinate;
    private int geneViewerMaximumCoordinate;

    private double maxTranscriptIdWidth;

    private HashMap<String, CDS> cdss;



    // list of conditions
    private ArrayList<String> conditions;

    // for depth graphs

    private ArrayList<String> transcriptsIdsOfGene;

    private boolean isSelectedZoomRegion;
    private double selectingRegionStart;

    private int fontSize;

    private DrawerController drawerController;
    private HostServices hostServices;

    private ArrayList<Transcript> displayedTranscripts;

    private String chr;
    private AllGenesReader allGenesReader;
    private FastaIndex fastaIndex;

    private String sequenceSearched;
    private ArrayList<Integer> sequenceSearchestarts;
    private int currentSequenceSearchMatchIndex;
    
    private String previousGene;
    private int previousStart;
    private int previousEnd;




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        isAGeneDisplayed = false;
        queryGeneDataBaseButton.setDisable(true);
        showCdsInGeneBrowserBool = true;
        showDepthInGeneBrowserBool = true;

        currentCoordinateTextField.setMinWidth(Region.USE_PREF_SIZE);
        qualLabel.setMinWidth(Region.USE_PREF_SIZE);
        tpmLabel.setMinWidth(Region.USE_PREF_SIZE);
        sortTPMLabel.setMinWidth(Region.USE_PREF_SIZE);

        geneBrowserMoveLeftButton.setDisable(true);
        geneBrowserMoveRightButton.setDisable(true);


        Text t = new Text("1");
        t.setFont(Font.font(16));


        browserGrid.heightProperty().addListener((observable, oldValue, newValue) -> {
            if(extraInfoTitledPane.isExpanded()){
                extraInfoTitledPane.setPrefHeight(browserGrid.getHeight());
            }else{
                geneBrowserScrollPane.setPrefHeight(browserGrid.getHeight()-40);

            }
        });

        //geneExonsSeqsVBox.setStyle("-fx-background-color: #25282D");



        FXMLLoader drawerFXML = new FXMLLoader(App.class.getResource("/drawerControllers/drawer.fxml"));

        try {
            Parent root = drawerFXML.load();
            drawerController = drawerFXML.getController();
            drawerController.setHostServices(hostServices);
            drawer.setSidePane(root);
            drawer.close();
            mutatedCdsController.setDrawer(drawer, drawerController);
        } catch (IOException e) {
            e.printStackTrace();
        }





        // behavior of the scroll pane - titled pane of extra info
        extraInfoTitledPane.expandedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (extraInfoTitledPane.isExpanded()) {

                browserGrid.getRowConstraints().remove(0);
                browserGrid.getRowConstraints().remove(0);
                RowConstraints rowConstraints1 = new RowConstraints();
                rowConstraints1.setPercentHeight(70);
                browserGrid.getRowConstraints().add(rowConstraints1);

                RowConstraints rowConstraints2 = new RowConstraints();
                rowConstraints2.setPercentHeight(30);
                browserGrid.getRowConstraints().add(rowConstraints2);


                extraInfoTitledPane.setPrefHeight(browserGrid.getHeight()*0.3);

            } else {

//                browserGrid.getRowConstraints().remove(0);
//                browserGrid.getRowConstraints().remove(0);
//                geneBrowserScrollPane.setPrefHeight(browserGrid.getHeight()-40);
//                extraInfoTitledPane.setPrefHeight(40);


                browserGrid.getRowConstraints().remove(0);
                browserGrid.getRowConstraints().remove(0);
                RowConstraints rowConstraints1 = new RowConstraints();
                rowConstraints1.setPercentHeight(100*(1-extraInfoTitledPane.getChildrenUnmodifiable()
                        .get(1).getLayoutBounds().getHeight()/browserGrid.getHeight()));
                browserGrid.getRowConstraints().add(rowConstraints1);

                RowConstraints rowConstraints2 = new RowConstraints();
                rowConstraints2.setPercentHeight(100*extraInfoTitledPane.getChildrenUnmodifiable()
                        .get(1).getLayoutBounds().getHeight()/browserGrid.getHeight());
                browserGrid.getRowConstraints().add(rowConstraints2);

                extraInfoTitledPane.setPrefHeight(extraInfoTitledPane.getChildrenUnmodifiable()
                        .get(1).getLayoutBounds().getHeight());
            }
        });

        varTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);// to remove extra empty column
        //  to autommatically generate the get methods
        geneVarTableColumn.setCellValueFactory(new PropertyValueFactory<>("gene"));
        coordVarTableColumn.setCellValueFactory(new PropertyValueFactory<>("refPos"));
        refVarTableColumn.setCellValueFactory(new PropertyValueFactory<>("ref"));
        altVarTableColumn.setCellValueFactory(new PropertyValueFactory<>("alt"));
        synonymousVarTableColumn.setCellValueFactory(new PropertyValueFactory<>("silent"));
        inCdsVarTableColumn.setCellValueFactory(new PropertyValueFactory<>("inCDS"));
        peptEvidenceVarTableColumn.setCellValueFactory(new PropertyValueFactory<>("hasPeptideEvidence"));


        varTableView.setRowFactory(tv -> {
            TableRow<Variation> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!(row.isEmpty()) ) {
                    if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                        Variation varItem =  row.getItem();
                        int varCoord = varItem.getRefPos();
                        geneSlider.setLowValue( varCoord - 40);
                        geneSlider.setHighValue( varCoord + 40);
                    }
                }
            });
            return row;
        });


        // default value
        minTpmFilterGeneBrowserTextField.setText("0");
        minQualFilterGeneBrowserTextField.setText("0");

        // textfield only doubles are accepted
        minTpmFilterGeneBrowserTextField.textProperty().addListener((observableValue, s, t1) -> {
            String value = minTpmFilterGeneBrowserTextField.getText();
            try {
                Double.parseDouble(value);
                if (Double.parseDouble(value) < 0) {
                    minTpmFilterGeneBrowserTextField.setText(Double.toString(1.0));
                }
            } catch (Exception e) {
                minTpmFilterGeneBrowserTextField.setText(Double.toString(1.0));
            }
        });

        // textfield only doubles are accepted
        minQualFilterGeneBrowserTextField.textProperty().addListener((observableValue, s, t1) -> {
            String value = minQualFilterGeneBrowserTextField.getText();
            try {
                Double.parseDouble(value);
                if (Double.parseDouble(value) < 0) {
                    minQualFilterGeneBrowserTextField.setText(Double.toString(1.0));
                }
            } catch (Exception e) {
                minQualFilterGeneBrowserTextField.setText(Double.toString(1.0));
            }
        });




        activateListenerConditionsGeneBrowserCombobox = false;
        conditionsGeneBrowserCombobox.valueProperty().addListener((observableValue, s, t1) -> {
            if (activateListenerConditionsGeneBrowserCombobox) {
                transcriptOrCdsCentricView();
            }
        });


        geneExonsSeqsVBox.heightProperty().addListener((observableValue, oldValue, newValue) -> {
            verticalLineGeneBrowserPane.setPrefHeight(newValue.doubleValue());
            selectionZoomRectanglePane.setPrefHeight(newValue.doubleValue());
        });





        drawer.setOnDrawerClosed(event -> drawer.setDisable(true));
        drawer.setOnDrawerOpened(event -> drawer.setDisable(false));



        // vertical bar to display x position
        geneBrowserScrollPane.setOnMouseMoved(mouseEvent -> {
            double xValue = mouseEvent.getX();
            if (xValue < representationWidthFinal) {
                verticalLineGeneBrowserPane.getChildren().clear();
                Line line = new Line(xValue, 0, xValue, geneExonsSeqsVBox.getHeight());

                line.setStroke(Color.GRAY);
                verticalLineGeneBrowserPane.getChildren().add(line);// verticalBarGeneBrowserVBox
            }
            if(isSelectedZoomRegion){
                selectionZoomRectanglePane.getChildren().clear();

                double start, end;
                if(xValue<selectingRegionStart){
                    start = xValue;
                    end = selectingRegionStart;
                }else{
                    start = selectingRegionStart;
                    end = xValue;
                }
                Rectangle rec = new Rectangle();
                rec.setWidth(end-start);
                rec.setHeight(geneExonsSeqsVBox.getHeight());
                rec.setX(start);
                rec.setY(0);
                rec.setFill(new Color(0.418, 0.7344, 0.7773, 0.4));
                selectionZoomRectanglePane.getChildren().add(rec);
            }

            double position = xValue/representationWidthFinal * (geneSlider.getHighValue()-geneSlider.getLowValue())
                    + geneSlider.getLowValue();

            currentPositionPane.setText("Position: "+NumberFormat.getIntegerInstance().format((int) position));
        });


        geneBrowserScrollPane.addEventFilter(MouseEvent.MOUSE_CLICKED, mouseEvent -> {

            //geneBrowserScrollPane.setOnMousePressed(mouseEvent -> {
            if(mouseEvent.getTarget().getClass()!=Rectangle.class && mouseEvent.getTarget().getClass()!=Text.class &&
                    !(mouseEvent.getTarget().getClass()==Pane.class && ((Pane) mouseEvent.getTarget()).getId()!=null &&
                            ((Pane) mouseEvent.getTarget()).getId().equals("exonPane")) &&
                    mouseEvent.getX()<representationWidthFinal){
                if(!isSelectedZoomRegion){
                    selectingRegionStart = mouseEvent.getX();
                }
                if(isSelectedZoomRegion){

                    double start, end;
                    if(mouseEvent.getX()<selectingRegionStart){
                        start = mouseEvent.getX();
                        end = selectingRegionStart;
                    }else{
                        start = selectingRegionStart;
                        end = mouseEvent.getX();
                    }
                    double basesPerPixel = (geneSlider.getHighValue()-geneSlider.getLowValue())/representationWidthFinal;
                    double previousStart = geneSlider.getLowValue();;
                    geneSlider.setLowValue(previousStart+Math.round(start*basesPerPixel));
                    geneSlider.setHighValue(previousStart+Math.round(end*basesPerPixel));
                    selectionZoomRectanglePane.getChildren().clear();
                    transcriptOrCdsCentricView();
                }
                isSelectedZoomRegion = !isSelectedZoomRegion;
            }


        });

        transcriptsComboBox.setPrefWidth(new Text("Include STRG transcripts").getLayoutBounds().getWidth()*1.2);




    }

    /*
     * Used to set the parent, from the FXML Document Controller,
     * So that when data is loaded, it can handle the first view of the tab
     */
    public void setParentControler(ResultsController parent, org.json.JSONObject settings, HostServices hostServices,
                                   String databaseName,  AllGenesReader allGenesReader) {
        parentController = parent;
        databaseProjectName = databaseName;
        this.hostServices = hostServices;

        this.allGenesReader = allGenesReader;
        displayCdsGeneBrowserMenuItem.setDisable(true);
        showHideDepthGeneBrowserMenuItem.setDisable(true);
        cdsCentricViewMenuItem.setDisable(true);
        transcriptCentricViewMenuItem.setDisable(true);


        fastaIndex = new FastaIndex(Config.getFastaPath());

        mutatedCdsController.setResultsController(parentController);

        viewType = "transcripts";

        fontSize = settings.getJSONObject("Fonts").getJSONObject("browser").getInt("size");

        if(allGenesReader.getGenesLoadedProperty().get()){
            TextFields.bindAutoCompletion(geneIdTextField, allGenesReader.getAllGeneNames());
        }else{
            allGenesReader.getGenesLoadedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if(newValue){
                        TextFields.bindAutoCompletion(geneIdTextField, allGenesReader.getAllGeneNames());
                        allGenesReader.getGenesLoadedProperty().removeListener(this);
                    }

                }
            });
        }



        geneIdTextField.textProperty().addListener((observableValue, s, t1) -> {
            String geneText = geneIdTextField.getText().trim();
            if (geneText.length() > 0) {
                queryGeneDataBaseButton.setDisable(!allGenesReader.getAllGeneNames().contains(geneText));
            }
        });

        queryGeneDataBaseButton.setOnAction(actionEvent -> {
            selectedGene = geneIdTextField.getText().trim();
            displaygeneFromGeneSymbol(selectedGene, true);
        });


        displayCdsGeneBrowserMenuItem.setOnAction(actionEvent -> {
            showCdsInGeneBrowserBool = !showCdsInGeneBrowserBool;
            if (isAGeneDisplayed) {
                transcriptOrCdsCentricView();
            }
        });

        showHideDepthGeneBrowserMenuItem.setOnAction(actionEvent -> {
            showDepthInGeneBrowserBool = !showDepthInGeneBrowserBool;
            if (isAGeneDisplayed) {
                transcriptOrCdsCentricView();
            }
        });

        cdsCentricViewMenuItem.setOnAction(actionEvent -> {
            viewType = "cds";
            transcriptOrCdsCentricView();

        });

        transcriptCentricViewMenuItem.setOnAction(actionEvent -> {
            viewType = "transcripts";
            transcriptOrCdsCentricView();
        });

    }


    public void displaygeneFromGeneSymbol(String geneSymbolToQuery, boolean display) {


//        // TODO. check that some may be duplicated,  eg: SNORD38B
        displayGeneFromId(geneSymbolToQuery, -1, -1, display);


    }

    /**
     * Displays all the transcripts of a gene. Sometimes can be called from other tabs.
     *
     * @param geneId  gene symbol
     */
    public void displayGeneFromId(String geneId, int start, int end, boolean display) {
        isAGeneDisplayed = false;
        displayCdsGeneBrowserMenuItem.setDisable(false);
        showHideDepthGeneBrowserMenuItem.setDisable(false);
        cdsCentricViewMenuItem.setDisable(false);
        transcriptCentricViewMenuItem.setDisable(false);

        mutatedCdsController.reset();


        // this is where all the information of the transcripts for a particular gene Will be saved
        transcriptHashMap = new HashMap<>();
        cdsCondSamplePerTranscCondSample = new ArrayList<>();


        // enable controls
        boolean disabled = false;

        geneBrowserMoveLeftButton.setDisable(disabled);
        geneBrowserMoveRightButton.setDisable(disabled);

        // sequences Strins must be formated to add white spaces where no sequence,
        //  and also characters that represent introns (eg. "-" )
        //  so that sequences can be aligned.
        transcFormatedSequencesMap = new HashMap<>();


        Gene gene = allGenesReader.getGene(geneIdTextField.getText());

        cdss = new HashMap<>();


        geneMinimumCoordinate = gene.getStartCoordinate();
        geneMaximumCoordinate = gene.getEndCoordinate();

        geneViewerMinimumCoordinate = 2147483647;
        geneViewerMaximumCoordinate = 0;

        findSeqButton.setDisable(false);
        findSeqField.setDisable(false);

        transcriptsComboBox.getItems().clear();
        transcriptsComboBox.getItems().add("Include USTRG transcripts");

        NitriteCollection transcCollection = Database.getDb().getCollection("allTranscripts");


        Cursor transcriptsQueryResult = transcCollection.find(Filters.eq("gene", geneIdTextField.getText()));
        for (Document tpmDocResult : transcriptsQueryResult) {
            Transcript transcript = new Transcript(tpmDocResult, cdss);
            chr = transcript.getChr();
            transcriptHashMap.put(transcript.getTranscriptId(), transcript);

            int transcStart = transcript.getStartGenomCoord();
            int transcEnd = transcript.getEndGenomCoord()+1;

            // since some MSTRG transcripts have start-end coords that are above or below gene coordinates, need to obtain the max and min to display
            geneViewerMinimumCoordinate = Math.min(geneViewerMinimumCoordinate, transcStart);
            geneViewerMaximumCoordinate = Math.max(geneViewerMaximumCoordinate, transcEnd);

            transcriptsComboBox.getItems().add(transcript.getTranscriptId());

        }



        ListChangeListener<String> changeListener = new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {

                c.next();
                if((c.getAddedSubList().size()>0 &&  c.getAddedSubList().get(0).equals("Include USTRG transcripts")) ||
                        (c.getRemoved().size()>0 &&  c.getRemoved().get(0).equals("Include USTRG transcripts"))) {

                    transcriptsComboBox.getCheckModel().getCheckedItems().removeListener(this);
                    for (int i = 0; i < transcriptsComboBox.getItems().size(); i++) {
                        if (transcriptsComboBox.getItems().get(i).startsWith("USTRG")) {
                            if (transcriptsComboBox.getCheckModel().isChecked("Include USTRG transcripts")) {
                                transcriptsComboBox.getCheckModel().check(i);
                            } else {
                                transcriptsComboBox.getCheckModel().clearCheck(i);
                            }
                        }
                    }
                    transcriptsComboBox.getCheckModel().getCheckedItems().addListener(this);
                }
                transcriptOrCdsCentricView();
            }
        };


        transcriptsComboBox.checkModelProperty().get().checkAll();

        transcriptsComboBox.getCheckModel().getCheckedItems().addListener(changeListener);





        getMaxTranscriptIdWidth();

        Thread bamThread = null;

        if(previousGene==null || !previousGene.equals(geneId) || previousStart!=start || previousEnd!=end) {
            bamThread = new Thread(() -> bamPaneController.showGene(chr, geneViewerMinimumCoordinate, geneViewerMaximumCoordinate,
                    representationWidthFinal, representationHeightFinal, Database.getDb()));
            bamThread.start();

            Thread bedThread = new Thread(() -> bedPaneController.showGene(chr, geneViewerMinimumCoordinate, geneViewerMaximumCoordinate,
                    representationWidthFinal, representationHeightFinal, Database.getDb()));
            bedThread.start();
        }

        NitriteCollection mutationsCollection = Database.getDb().getCollection("mutations");


        variations = new LinkedList<>();

        Cursor mutationsResults = mutationsCollection.find(Filters.eq("gene", geneIdTextField.getText()));
        for (Document tmpDoc : mutationsResults) {



            Variation variation = new Variation(tmpDoc);

            for(String transcriptId: variation.getTranscriptIds()){
                transcriptHashMap.get(transcriptId).addVariation(variation);
            }
            variations.add(variation);


        }

//        // format sequences strings. Add spaces and so.
        for (Map.Entry<String, Transcript> transcMapEntry : transcriptHashMap.entrySet()) {
            String transcId = transcMapEntry.getKey();
            Transcript tmpTranscript = transcMapEntry.getValue();
            transcFormatedSequencesMap.put(transcId, formatSequence(transcId, tmpTranscript, geneViewerMinimumCoordinate,
                    geneViewerMaximumCoordinate));
        }
//
//
        // clear geneExonsSeqsVBox
        geneExonsSeqsVBox.getChildren().remove(2, geneExonsSeqsVBox.getChildren().size());

        // add Slider to the transcripts viewer
        geneSlider = new RangeSlider();
        geneSlider.setId("browserSlider");

        // set view position when first started the browser
        int genomPos;
        int lowValue;
        int highValue;
        if (start > -1) {
//            Transcript tmpTransc = transcriptHashMap.get(geneOrTransId);
//
//            genomPos = tmpTransc.genomCoordFromSeqPos(seqPosOrCoord);
            if(start==end){
                lowValue = start - 30;
                highValue = start + 30;
            }else{
                lowValue = start;
                highValue = end;
            }

        } else {
            lowValue = geneViewerMinimumCoordinate;
            highValue = geneViewerMaximumCoordinate;
        }


        geneSlider.prefWidthProperty().bind(geneHBoxSlider.widthProperty().subtract(currentPositionPane.widthProperty())
        .multiply(0.8));


        geneSlider.setMin(geneViewerMinimumCoordinate);
        geneSlider.setMax(geneViewerMaximumCoordinate);

        geneSlider.setHighValue(highValue);
        geneSlider.setLowValue(lowValue);



        geneSlider.setShowTickLabels(true);
        geneSlider.setShowTickMarks(true);
        geneSlider.layoutXProperty().bind(currentPositionPane.widthProperty().add(20));


        if(geneHBoxSlider.getChildren().size()>1)
            geneHBoxSlider.getChildren().remove(1);


        geneHBoxSlider.getChildren().add(geneSlider);

        // listeners for the slider
        geneSlider.setMajorTickUnit((double)(geneViewerMaximumCoordinate - geneViewerMinimumCoordinate) / 5);
        geneSlider.setMinorTickCount(5);

        geneSlider.setOnScroll(event -> {
            if (geneSlider.getHighValue() - geneSlider.getLowValue() >= 0) {
                if (event.getDeltaY() < 0) {
                    geneSlider.setLowValue(geneSlider.getLowValue() - getZoomValue("out"));
                    geneSlider.setHighValue(geneSlider.getHighValue() + getZoomValue("out"));
                } else if (event.getDeltaY() > 0) {
                    geneSlider.setLowValue(geneSlider.getLowValue() + getZoomValue("in"));
                    geneSlider.setHighValue(geneSlider.getHighValue() - getZoomValue("in"));
                }
                transcriptOrCdsCentricView();
            }
        });



        geneSlider.setOnKeyPressed(event -> {

            switch (event.getCode()) {
                case LEFT:
                    geneSlider.setLowValue(geneSlider.getLowValue() - getZoomValue(""));
                    geneSlider.setHighValue(geneSlider.getHighValue() - getZoomValue(""));
                    transcriptOrCdsCentricView();
                    break;
                case RIGHT:
                    geneSlider.setLowValue(geneSlider.getLowValue() + getZoomValue(""));
                    geneSlider.setHighValue(geneSlider.getHighValue() + getZoomValue(""));
                    transcriptOrCdsCentricView();
                    break;
                case CONTROL:
                    geneSlider.setOnScroll(event2 -> {
                        if (event2.getDeltaY() > 0) {
                            geneSlider.setLowValue(geneSlider.getLowValue() + getZoomValue(""));
                            geneSlider.setHighValue(geneSlider.getHighValue() + getZoomValue(""));
                            transcriptOrCdsCentricView();
                        } else if (event2.getDeltaY() < 0) {
                            geneSlider.setLowValue(geneSlider.getLowValue() - getZoomValue(""));
                            geneSlider.setHighValue(geneSlider.getHighValue() - getZoomValue(""));
                            transcriptOrCdsCentricView();
                        }
                    });
                    break;
            }
        });

        geneSlider.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.CONTROL) {
                geneSlider.setOnScroll(event2 -> {
                    if (event2.getDeltaY() < 0) {
                        geneSlider.setLowValue(geneSlider.getLowValue() - getZoomValue("out"));
                        geneSlider.setHighValue(geneSlider.getHighValue() + getZoomValue("out"));
                        transcriptOrCdsCentricView();
                    } else if (event2.getDeltaY() > 0) {
                        geneSlider.setLowValue(geneSlider.getLowValue() + getZoomValue("in"));
                        geneSlider.setHighValue(geneSlider.getHighValue() - getZoomValue("in"));
                        transcriptOrCdsCentricView();
                    }
                });
            }
        });


        // add listener to the controls

        geneBrowserMoveLeftButton.setOnAction(event -> {
            geneSlider.setLowValue(geneSlider.getLowValue() - getZoomValue(""));
            geneSlider.setHighValue(geneSlider.getHighValue() - getZoomValue(""));
            transcriptOrCdsCentricView();

        });
        geneBrowserMoveRightButton.setOnAction(event -> {
            geneSlider.setLowValue(geneSlider.getLowValue() + getZoomValue(""));
            geneSlider.setHighValue(geneSlider.getHighValue() + getZoomValue(""));
            transcriptOrCdsCentricView();
        });


        zoomFullSeqExons.setOnAction(event -> {
            geneSlider.setLowValue(geneSlider.getMin());
            geneSlider.setHighValue(geneSlider.getMax());
            transcriptOrCdsCentricView();
        });

        zoomInExonButton.setOnAction(event -> {
            geneSlider.setLowValue(geneSlider.getLowValue() + getZoomValue("in"));
            geneSlider.setHighValue(geneSlider.getHighValue() - getZoomValue("in"));
            transcriptOrCdsCentricView();
        });

        zoomOutExonButton.setOnAction(event -> {
                    geneSlider.setLowValue(geneSlider.getLowValue() - getZoomValue("out"));
                    geneSlider.setHighValue(geneSlider.getHighValue() + getZoomValue("out"));
                    transcriptOrCdsCentricView();
                }
        );


        // tpm filtering
        minTpmFilterGeneBrowserTextField.textProperty().addListener((observableValue, s, t1) ->
                transcriptOrCdsCentricView()
        );

        // qual filtering
        minQualFilterGeneBrowserTextField.textProperty().addListener((observableValue, s, t1) ->
                transcriptOrCdsCentricView()
        );



        try {
            if(bamThread!=null)
                bamThread.join();
            // set combobox options and listeners
            setConditionsCombobox(); // exons are drawn by this one. Uses transcriptHashMap
        } catch (InterruptedException e) {
            e.printStackTrace();
        }




        //TODO: modify this, it's just a temporal thing
        tpmChartMenuItem.setOnAction(actionEvent -> {

            Dialog dialog = new Dialog();
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            Node closeButton = dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
            closeButton.managedProperty().bind(closeButton.visibleProperty());
            closeButton.setVisible(false);


            BarChart<Number, String> bc = getAvgTPMBarChartByCond(transcriptHashMap.keySet());

            bc.setMinHeight(750);
            bc.setMinWidth(700);
            bc.legendSideProperty().setValue(Side.RIGHT);

            HBox bcHBox = new HBox();
            bcHBox.setMinWidth(750);
            bcHBox.setMinHeight(760);


            bcHBox.getChildren().add(bc);

            dialog.getDialogPane().setMinWidth(800);
            dialog.getDialogPane().setMinHeight(800);

            dialog.getDialogPane().getChildren().add(bcHBox);

            dialog.showAndWait();
        });

        previousGene=geneId;
        previousStart=start;
        previousEnd=end;
    }


    private void transcriptOrCdsCentricView() {

        currentCoordinateTextField.setText(NumberFormat.getNumberInstance(Locale.US).format(geneSlider.getLowValue())+":"+ NumberFormat.getNumberInstance(Locale.US).format(geneSlider.getHighValue()));
        filterTranscripts();

        if (showDepthInGeneBrowserBool && ((int) geneSlider.getLowValue()!=previousStart || (int) geneSlider.getHighValue()!=previousEnd)) {
            bamPaneController.show((int) geneSlider.getLowValue(), (int) geneSlider.getHighValue());
            bedPaneController.show((int) geneSlider.getLowValue(), (int) geneSlider.getHighValue());
        }
        previousStart = (int) geneSlider.getLowValue();
        previousEnd = (int) geneSlider.getHighValue();

        if (viewType.equals("transcripts")) {
            displayTranscriptCentricViewExonsOrSeq();
        } else if (viewType.equals("cds")) {
            //displayCdsCentricView();
        }




    }

    /**
     * Displays rectangles representing introns-exons, or the letter sequence,
     * depending on the distance between high and low value of the geneSlider.
     * Displays the items according to average TPM Condition values.
     * uses transcriptHashMap Map of transcripts: Key is transcript Id, Value is Transcript Object.
     * Passed to the functions that actually
     * draw the exons or the sequences.
     */
    private void displayTranscriptCentricViewExonsOrSeq() {
        isAGeneDisplayed = true;
        geneExonsSeqsVBox.getChildren().remove(2, geneExonsSeqsVBox.getChildren().size());

        drawTranscOrSequences();


    }


    private void filterTranscripts(){

        displayedTranscripts = new ArrayList<>();

        double tpmThreshold = Double.parseDouble(minTpmFilterGeneBrowserTextField.getText());

        String conditionToSortBy = conditionsGeneBrowserCombobox.getValue();

        // sort transcID by condition average TPM
        if (!conditionToSortBy.equals("")) {
            ArrayList<Pair<Transcript, Double>> transcIDconditionAverageTPM = new ArrayList<>();

            for (Map.Entry<String, Transcript> transcMapEntry : transcriptHashMap.entrySet()) {

                Double avgTPM = transcMapEntry.getValue().getAverageTPMByCondition(conditionToSortBy);

                if (!avgTPM.equals(Double.NaN) && avgTPM >= tpmThreshold) { // filter by average TPM
                    Pair<Transcript, Double> transcAvgTPMPair = new Pair(transcMapEntry.getValue(), avgTPM);
                    transcIDconditionAverageTPM.add(transcAvgTPMPair);

                }
            }

            // sort by average tpm
            final Comparator<Pair<Transcript, Double>> avgTPMComparator = comparing(Pair::getValue);
            transcIDconditionAverageTPM.sort(avgTPMComparator);
            Collections.reverse(transcIDconditionAverageTPM);

            // get transcript ids sorted by average tpm



            for (Pair<Transcript, Double> transcIDAvgPair : transcIDconditionAverageTPM) {
                if(transcriptsComboBox.getCheckModel().isChecked(transcIDAvgPair.getKey().getTranscriptId())){
                    displayedTranscripts.add(transcIDAvgPair.getKey());
                }
            }
        }

        bamPaneController.setDisplayedTranscripts(displayedTranscripts);

    }

    // function that displays t
    private void drawTranscOrSequences(){
        if (geneSlider.getHighValue() - geneSlider.getLowValue() <= 170) {
            drawAlignedSequencesVBox();
        } else {
            drawExonsVBox();
        }
    }


//    private void displayCdsCentricView() {
//        isAGeneDisplayed = true;
//        geneExonsSeqsVBox.getChildren().remove(1, geneExonsSeqsVBox.getChildren().size());
//
//        double rectanglesAreaWidth = representationWidthFinal;
//
//        int startGenomCoord = (int) geneSlider.getLowValue();
//        int endGenomCoord = (int) geneSlider.getHighValue();
//
//        // draw depth
//        if (showDepthInGeneBrowserBool) {
//            bamPaneController.show((int) geneSlider.getLowValue(), (int) geneSlider.getHighValue());
//        }
//
//        // tpm threshold, if tpm < tpmThreshold, the seq is not shown
//        double tpmThreshold = Double.parseDouble(minTpmFilterGeneBrowserTextField.getText());
//        String conditionToSortBy = conditionsGeneBrowserCombobox.getValue();
//
//
//        // identify unique cds, use: sequence, Start genom coord and End genom coords
//        HashMap<Pair<String, Pair<Integer, Integer>>, ArrayList<String>> seqStartEndCoordTranscIdsMap = new HashMap<>();
//        HashMap<Pair<String, Pair<Integer, Integer>>, Double> seqStartEndCoordSumTpmMap = new HashMap<>();
//        HashMap<Pair<String, Pair<Integer, Integer>>, ArrayList<Pair<String, Pair<Integer, Integer>>>> cdsSeqStartEndCoordPfamDescStartEndCoords = new HashMap<>();
//        HashMap<Pair<String, Pair<Integer, Integer>>, ArrayList<Pair<String, Pair<Integer, Integer>>>> cdsSeqStartEndCoordPeptidesSeqCoordsMap = new HashMap<>();
//        HashMap<Pair<String, Pair<Integer, Integer>>, ArrayList<String>> cdsSeqStartEndCoorddistinctPeptidesMap = new HashMap<>();
//
//
//        for (CdsCondSample tmpCdsCondSample : cdsCondSamplePerTranscCondSample) {
//            String cdsTranscId = tmpCdsCondSample.getTranscID();
//            int cdsStartPos = tmpCdsCondSample.getStart();
//            int cdsEndPos = tmpCdsCondSample.getEnd();
//            String cdsSeq = tmpCdsCondSample.getSequence();
//
//            // filter by condition
//            if (!tmpCdsCondSample.getCondition().equals(conditionToSortBy)){
//                continue;
//            }
//
//            // filter by avgTPM
//            Double avgTPM = transcriptHashMap.get(cdsTranscId).getAverageTPMByCondition(conditionToSortBy);
//            if (avgTPM.equals(Double.NaN)) { // could be NaN, since it may not be present in some condition
//                avgTPM = 0.0;
//            }
//            if (avgTPM < tpmThreshold) { // filter by average TPM
//                continue;
//            }
//
//            int cdsStartGenomCoord = transcriptHashMap.get(cdsTranscId).genomCoordFromSeqPos(cdsStartPos) - 1; // - 1 cause  start is not inclusive
//            int cdsEndGenomCoord = transcriptHashMap.get(cdsTranscId).genomCoordFromSeqPos(cdsEndPos);
//            double cdsTranscTpmCumSumByCond = transcriptHashMap.get(cdsTranscId).getCumSumTPMByCondition(conditionToSortBy);
//
//            Pair<String, Pair<Integer, Integer>> seqStartEndCoodsPair = new Pair<>(cdsSeq, new Pair<>(cdsStartGenomCoord, cdsEndGenomCoord));
//
//            if (!seqStartEndCoordTranscIdsMap.keySet().contains(seqStartEndCoodsPair)) { // new
//                seqStartEndCoordTranscIdsMap.put(seqStartEndCoodsPair, new ArrayList<String>());
//                seqStartEndCoordTranscIdsMap.get(seqStartEndCoodsPair).add(cdsTranscId);
//
//                seqStartEndCoordSumTpmMap.put(seqStartEndCoodsPair, cdsTranscTpmCumSumByCond); //cumsum to sort
//
//                // get pfams
//                if (tmpCdsCondSample.hasPfam()) {
//
//                    ArrayList<Pair<String, Pair<Integer, Integer>>> pfamsDescStartEndGenomCoords = new ArrayList<>();
//
//                    for (Pfam tmpPfam : tmpCdsCondSample.getPfams()) {
//                        int pfamAaStart = tmpPfam.getAaStart();
//                        int pfamAaEnd = tmpPfam.getAaEnd();
//                        String pfamDesc = tmpPfam.getDesc();
//
//
//                        // get rna seq position
//                        int pfamRnaStart = -999;
//                        int pfamRnaEnd = -999;
//
//                        if (tmpCdsCondSample.getStrand().equals("+")){
//                            pfamRnaStart = tmpCdsCondSample.getStart() + ((pfamAaStart - 1) * 3) -1; // -1 since are 1 indexed
//                            pfamRnaEnd = tmpCdsCondSample.getStart() + ((pfamAaEnd - 1) * 3) + 2; // -1 since are 1 indexed
//                        } else {
//                            pfamRnaEnd = tmpCdsCondSample.getEnd() - ((pfamAaStart - 1) * 3) ; // -1 since are 1 indexed
//                            pfamRnaStart =  tmpCdsCondSample.getEnd() - ((pfamAaEnd - 1) * 3) - 3; // -1 since are 1 indexed
//                        }
//
//
//                        int pfamStartCoord = transcriptHashMap.get(cdsTranscId).genomCoordFromSeqPos(pfamRnaStart);
//                        int pfamEndCoord = transcriptHashMap.get(cdsTranscId).genomCoordFromSeqPos(pfamRnaEnd);
//
//                        Pair<String, Pair<Integer, Integer>> pfamDescStartEndCoordsPair = new Pair<>(pfamDesc, new Pair<>(pfamStartCoord, pfamEndCoord));
//
//                        pfamsDescStartEndGenomCoords.add(pfamDescStartEndCoordsPair);
//                    }
//
//
//                    cdsSeqStartEndCoordPfamDescStartEndCoords.put(seqStartEndCoodsPair, pfamsDescStartEndGenomCoords);
//                }
//
//            } else { // existing cds
//                if (!seqStartEndCoordTranscIdsMap.get(seqStartEndCoodsPair).contains(cdsTranscId)) {
//                    seqStartEndCoordTranscIdsMap.get(seqStartEndCoodsPair).add(cdsTranscId);
//                    double cumTpmSum = seqStartEndCoordSumTpmMap.get(seqStartEndCoodsPair) + cdsTranscTpmCumSumByCond;
//                    seqStartEndCoordSumTpmMap.put(seqStartEndCoodsPair, cumTpmSum);  //cumsum to sort
//                }
//            }
//
//
//            // peptides
//            if (tmpCdsCondSample.hasPeptides()){
//
//                if (!cdsSeqStartEndCoordPeptidesSeqCoordsMap.containsKey(seqStartEndCoodsPair)){
//                    cdsSeqStartEndCoordPeptidesSeqCoordsMap.put(seqStartEndCoodsPair, new ArrayList<>());
//                    cdsSeqStartEndCoorddistinctPeptidesMap.put(seqStartEndCoodsPair, new ArrayList<String>());
//                }
//
//                for (Peptide tpmPeptide : tmpCdsCondSample.getPeptides()){
//                    String pepSeq = tpmPeptide.getSequence();
//
//                    if (!cdsSeqStartEndCoorddistinctPeptidesMap.get(seqStartEndCoodsPair).contains(pepSeq)){
//                        cdsSeqStartEndCoorddistinctPeptidesMap.get(seqStartEndCoodsPair).add(pepSeq); // prevent replicates
//                        getPepPos(cdsSeq, pepSeq, tmpCdsCondSample,seqStartEndCoodsPair, cdsSeqStartEndCoordPeptidesSeqCoordsMap );
//
//                    }
//                }
//            }
//        }
//
//        // sort (descending) cds by cumsumTpm
//        Comparator<Map.Entry<Pair<String, Pair<Integer, Integer>>, Double>> valueComparator = Comparator.comparing(Map.Entry::getValue);
//        Map<Pair<String, Pair<Integer, Integer>>, Double> sortedseqStartEndCoordSumTpmMap =
//                seqStartEndCoordSumTpmMap.entrySet().stream().
//                        sorted(valueComparator.reversed()).
//                        collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
//                                (e1, e2) -> e1, LinkedHashMap::new));
//
//
//        for (Map.Entry<Pair<String, Pair<Integer, Integer>>, Double> startEndTranscIdEntry : sortedseqStartEndCoordSumTpmMap.entrySet()) {
//            Pair<String, Pair<Integer, Integer>> seqStartEndPair = startEndTranscIdEntry.getKey();
//            Pair<Integer, Integer> cdsStartEndPair = seqStartEndPair.getValue();
//            ArrayList<String> cdsTranscIdList = seqStartEndCoordTranscIdsMap.get(seqStartEndPair);
//            ArrayList<String> transcriptsIdsAboveThresholdArray = new ArrayList<>();
//
//
//            // draw transcripts
//            // sort transcID by condition average TPM
//            if (!conditionToSortBy.equals("")) {
//                ArrayList<Pair<String, Double>> cdsTranscIDconditionAverageTPM = new ArrayList<>();
//
//                for (String cdsTranscId : cdsTranscIdList) {
//                    Double avgTPM = transcriptHashMap.get(cdsTranscId).getAverageTPMByCondition(conditionToSortBy);
//                    if (avgTPM.equals(Double.NaN)) { // could be NaN, since it may not be present in some condition
//                        avgTPM = 0.0;
//                    }
//                    if (avgTPM >= tpmThreshold) { // filter by average TPM
//                        Pair<String, Double> transcAvgTPMPair = new Pair(cdsTranscId, avgTPM);
//                        cdsTranscIDconditionAverageTPM.add(transcAvgTPMPair);
//                    }
//                }
//
//                // sort by average tpm
//                cdsTranscIDconditionAverageTPM.sort(Comparator.comparing(p -> p.getValue()));
//                Collections.reverse(cdsTranscIDconditionAverageTPM);
//
//                // get transcript ids sorted by average tpm
//                for (Pair<String, Double> transcIDAvgPair : cdsTranscIDconditionAverageTPM) {
//                    transcriptsIdsAboveThresholdArray.add(transcIDAvgPair.getKey());
//                }
//
//                // draw either exons or sequences, depending on the distance
//                drawTranscOrSequences();
//
//                // draw cds
//                HBox cdsHBox = new HBox();
//                HBox pepHBox = new HBox();
//
//                Pane cdsPane = new Pane();
//                Group cdsGroup = new Group();
//                Pane pepPane = new Pane();
//                Group pepGroup = new Group();
//
//                Rectangle cdsRectangle = new Rectangle();
//                int height = 10;
//                cdsHBox.setPrefHeight(height);
//                pepHBox.setPrefHeight(height);
//                cdsRectangle.setHeight(height);
//
//                // tooltip for cds
//                Tooltip cdsToolTip = new Tooltip("CDS");
//                cdsToolTip.setShowDelay(Duration.millis(500));
//                cdsToolTip.setFont(Font.font(fontSize));
//                cdsToolTip.setShowDuration(Duration.seconds(4));
//                Tooltip.install(cdsRectangle, cdsToolTip);
//
//                // on click action
//                cdsRectangle.setOnMouseClicked(mouseEvent -> {
//                    if (mouseEvent.getClickCount() == 1 && mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
//                        String rectCdsSeq = seqStartEndPair.getKey();
//                        displayCdsTab(rectCdsSeq);
//                    }
//                });
//
//                //  color depending on having peptides
//                if (cdsSeqStartEndCoordPeptidesSeqCoordsMap.get(seqStartEndPair) != null) {
//                    cdsRectangle.setFill(Color.rgb(217, 33, 122));
//                    cdsRectangle.setStroke(Color.WHITE);
//                } else {
//                    cdsRectangle.setFill(Color.rgb(217, 33, 122, 0.5));
//                    cdsRectangle.setStroke(Color.WHITE);
//                }
//
//                // min-max coords in view
//                int cdsStartCoordInView = Math.max(startGenomCoord, cdsStartEndPair.getKey());
//                int cdsEndCoordInView = Math.min(endGenomCoord, cdsStartEndPair.getValue());
//
//                double totalSize = endGenomCoord - startGenomCoord;
//
//                cdsRectangle.setX(rectanglesAreaWidth * (getProportion(cdsStartCoordInView, startGenomCoord, totalSize)));
//                cdsRectangle.setWidth(rectanglesAreaWidth * (getProportion(cdsEndCoordInView, cdsStartCoordInView, totalSize)));
//                cdsGroup.getChildren().add(cdsRectangle);
//
//
//                // if has pfams, add pfam rectangles
//                if (cdsSeqStartEndCoordPfamDescStartEndCoords.get(seqStartEndPair) != null) {
//                    ArrayList<Pair<String, Pair<Integer, Integer>>> pfamDescStartEndCoordsPairArray = cdsSeqStartEndCoordPfamDescStartEndCoords.get(seqStartEndPair);
//
//                    for (Pair<String, Pair<Integer, Integer>> pfamDescStartEndCoordsPair : pfamDescStartEndCoordsPairArray) {
//                        Rectangle pfamRectangle = new Rectangle();
//                        pfamRectangle.setHeight(height);
//
//                        pfamRectangle.setFill(Color.rgb(153, 255, 153));
//                        pfamRectangle.setStroke(Color.WHITE);
//
//                        String pfamDesc = pfamDescStartEndCoordsPair.getKey();
//                        int pfamStartCoord = pfamDescStartEndCoordsPair.getValue().getKey();
//                        int pfamEndCoord = pfamDescStartEndCoordsPair.getValue().getValue();
//
//
//                        int pfamStartCoordInView = Math.max(startGenomCoord, pfamStartCoord);
//                        int pfamEndCoordInView = Math.min(endGenomCoord, pfamEndCoord);
//
//                        double totalSizeInView = endGenomCoord - startGenomCoord;
//
//                        // Tooltip for pfam domain
//                        Tooltip pfamToolTip = new Tooltip(pfamDesc);
//                        pfamToolTip.setShowDelay(Duration.ZERO);
//                        Tooltip.install(pfamRectangle, pfamToolTip);
//                        pfamToolTip.setShowDuration(Duration.seconds(4));
//
//
//                        pfamRectangle.setX(rectanglesAreaWidth * (getProportion(pfamStartCoordInView, startGenomCoord, totalSizeInView)));
//                        pfamRectangle.setWidth(rectanglesAreaWidth * (getProportion(pfamEndCoordInView, pfamStartCoordInView, totalSizeInView)));
//                        cdsGroup.getChildren().add(pfamRectangle);
//                    }
//                }
//
//                // draw peptides: get rectangles
//                if (cdsSeqStartEndCoordPeptidesSeqCoordsMap.get(seqStartEndPair) != null ) {
//                    ArrayList<Pair<String, Pair<Integer, Integer>>> pepSeqStartEndCoordsArray = cdsSeqStartEndCoordPeptidesSeqCoordsMap.get(seqStartEndPair);
//                    pepGroup = getPepGroup(height, startGenomCoord, endGenomCoord, rectanglesAreaWidth, pepSeqStartEndCoordsArray);
//                }
//
//                cdsPane.getChildren().add(cdsGroup);
//                cdsHBox.getChildren().add(cdsPane);
//                geneExonsSeqsVBox.getChildren().add(cdsHBox);
//
//                // add peptides
//                if (pepGroup.getChildren().size() > 0 ) {
//                    pepPane.getChildren().add(pepGroup);
//                    pepHBox.getChildren().add(pepPane);
//                    geneExonsSeqsVBox.getChildren().add(pepHBox);
//                }
//
//            }
//        }
//
//
//        // include the transcripts that don't have CDS
//        ArrayList<String> transcWithNoCds = new ArrayList<>();
//        ArrayList<Pair<String, Double>> transcWithNoCdsTranscIDconditionAverageTPM = new ArrayList<>();
//        for (Map.Entry<String, Transcript> transcriptEntry : transcriptHashMap.entrySet()) {
//            String transcId = transcriptEntry.getKey();
//            Transcript tmpTransc = transcriptEntry.getValue();
//            if (!tmpTransc.getHasCds()) { // if doesn't have cds
//                Double avgTPM = transcriptHashMap.get(transcId).getAverageTPMByCondition(conditionToSortBy);
//                if (avgTPM.equals(Double.NaN)) { // could be NaN, since it may not be present in some condition
//                    avgTPM = 0.0;
//                }
//                if (avgTPM >= tpmThreshold) { // filter by average TPM
//                    Pair<String, Double> transcAvgTPMPair = new Pair(transcId, avgTPM);
//                    transcWithNoCdsTranscIDconditionAverageTPM.add(transcAvgTPMPair);
//                }
//            }
//        }
//        // sort by tpm
//        transcWithNoCdsTranscIDconditionAverageTPM.sort(Comparator.comparing(p -> p.getValue()));
//        Collections.reverse(transcWithNoCdsTranscIDconditionAverageTPM);
//        // get transcIds
//        for (Pair<String, Double> avgTpmPair : transcWithNoCdsTranscIDconditionAverageTPM) {
//            transcWithNoCds.add(avgTpmPair.getKey());
//        }
//
//        // draw either exons or sequences, depending on the distance
//        drawTranscOrSequences();
//
//
//    }


    private Group getPepGroup (CDS cds, Transcript transcript, double height, int startGenomCoord, int endGenomCoord,
                               double rectanglesAreaWidth){


        Group pepGroup = new Group();


        HashMap<String, HashSet<String>> peptideSeqsRuns = new HashMap<>();

        for (Peptide peptide : cds.getPeptides()) {


            String pepSeq = peptide.getSequence();
            if (!peptideSeqsRuns.containsKey(pepSeq)) {

                peptideSeqsRuns.put(pepSeq, new HashSet<>());

                Pair<Integer, Integer> peptideGenomicPos = getPepPos(pepSeq, cds, transcript);

                if (peptideGenomicPos != null) {


                    for (int i = 0; i < transcript.getExons().size(); i++) {

                        Exon exon = transcript.getExons().get(i);

                        int peptideRectangleStart = 0, peptideRectangleEnd = 0;

                        if ((exon.getStart() >= startGenomCoord && exon.getStart() <= endGenomCoord) ||
                                (exon.getEnd() >= startGenomCoord && exon.getEnd() <= endGenomCoord)) {

                            if(exon.getStart() <= peptideGenomicPos.getKey() && exon.getEnd() >= peptideGenomicPos.getValue()) {
                                peptideRectangleStart = Math.max(peptideGenomicPos.getKey(), startGenomCoord) - 1;
                                peptideRectangleEnd = Math.min(peptideGenomicPos.getValue() + 1, endGenomCoord);
                            }else if (exon.getStart() <= peptideGenomicPos.getKey() && exon.getEnd() <= peptideGenomicPos.getValue()) {
                                peptideRectangleStart = Math.max(peptideGenomicPos.getKey(), startGenomCoord) - 1;
                                peptideRectangleEnd = Math.min(exon.getEnd() + 1, endGenomCoord);
                            } else if (exon.getStart() <= peptideGenomicPos.getValue() && exon.getEnd() >= peptideGenomicPos.getValue()) {
                                peptideRectangleStart = Math.max(exon.getStart(), startGenomCoord) - 1;
                                peptideRectangleEnd = Math.min(peptideGenomicPos.getValue(), endGenomCoord);
                            } else if (exon.getStart() >= peptideGenomicPos.getKey() && exon.getEnd() <= peptideGenomicPos.getValue()) {
                                peptideRectangleStart = Math.max(exon.getStart() + 2, startGenomCoord) - 1;
                                peptideRectangleEnd = Math.min(exon.getEnd() + 1, endGenomCoord);
                            }

                            if (peptideRectangleStart != peptideRectangleEnd) {


                                Rectangle pepRect = new Rectangle();
                                pepRect.setHeight(height);

                                pepRect.setOnMouseClicked(mouseEvent -> {
                                    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                                        if (mouseEvent.getClickCount() == 2) {

                                            if (peptideSeqsRuns.get(pepSeq).size() > 1) {
                                                ChoiceDialog d = new ChoiceDialog(peptideSeqsRuns.get(pepSeq).iterator().next(),
                                                        peptideSeqsRuns.get(pepSeq).toArray());


                                                d.showAndWait();
                                                ControllersBasket.getResultsController().moveToTab(5);
                                                ControllersBasket.getPeptideTableController()
                                                        .findPeptideInTable(pepSeq, (String) d.getSelectedItem());
                                            } else {
                                                ControllersBasket.getResultsController().moveToTab(5);
                                                ControllersBasket.getPeptideTableController()
                                                        .findPeptideInTable(pepSeq, peptideSeqsRuns.get(pepSeq).iterator().next());

                                            }
                                        }
                                    }
                                });

                                // tooltip for peptides
                                Tooltip pepToolTip = new Tooltip("Peptide");
                                pepToolTip.setShowDelay(Duration.millis(500));
                                pepToolTip.setFont(Font.font(fontSize));
                                pepToolTip.setShowDuration(Duration.seconds(4));
                                Tooltip.install(pepRect, pepToolTip);

                                pepRect.setFill(Color.rgb(193, 154, 0, 1));
                                pepRect.setStroke(Color.rgb(193, 154, 0));
                                pepRect.setStrokeWidth(2);


                                int pepStartCoordInView = Math.max(startGenomCoord, peptideRectangleStart+1);
                                int pepEndCoordInView = Math.min(endGenomCoord, peptideRectangleEnd);

                                double totalSizeInView = endGenomCoord - startGenomCoord;

                                pepRect.setX(rectanglesAreaWidth * (getProportion(pepStartCoordInView, startGenomCoord, totalSizeInView)));
                                pepRect.setWidth(rectanglesAreaWidth * (getProportion(pepEndCoordInView, pepStartCoordInView, totalSizeInView)));

                                if (pepRect.getWidth() > 0) {
                                    pepGroup.getChildren().add(pepRect);


                                    for (PSM psm : peptide.getPsms()) {
                                        HashSet<PTM> ptms = psm.getModifications();


                                        for (PTM ptm : ptms) {

                                            Tooltip tooltipTranscId = new Tooltip(ptm.getName());
                                            tooltipTranscId.setShowDelay(Duration.ONE);
                                            tooltipTranscId.setFont(Font.font("monospace", fontSize));


                                            if (ptm.getShape().equals("triangle")) {

                                                Polygon polygon = new Polygon();
                                                if (cds.getStrand().equals("+")) {
                                                    double X = rectanglesAreaWidth * (getProportion(peptideRectangleStart + 3 * (ptm.getPos() - 1) + 1.5,
                                                            startGenomCoord, totalSizeInView));

                                                    polygon.getPoints().addAll(X + 10, height,
                                                            X, height + 20.0,
                                                            X + 20, height + 20.0);


                                                } else {
                                                    double X = rectanglesAreaWidth * (getProportion(peptideRectangleStart + 3 * (peptide.getSequence().length() -
                                                                    ptm.getPos() - 1) + 0.75,
                                                            startGenomCoord, totalSizeInView));

                                                    polygon.getPoints().addAll(X + 10, height,
                                                            X, height + 20.0,
                                                            X + 20, height + 20.0);
                                                }
                                                polygon.setFill(ptm.getColor());

                                                pepGroup.getChildren().add(polygon);
                                                Tooltip.install(polygon, tooltipTranscId);


                                            } else if (ptm.getShape().equals("square")) {
                                                Rectangle rect = new Rectangle();
                                                if (cds.getStrand().equals("+")) {
                                                    rect.setX(rectanglesAreaWidth * (getProportion(peptideRectangleStart + 3 * (ptm.getPos() - 1) + 0.75,
                                                            startGenomCoord, totalSizeInView)));


                                                } else {
                                                    rect.setX(rectanglesAreaWidth * (getProportion(pepStartCoordInView
                                                                    + 3 * (peptide.getSequence().length() - ptm.getPos()) - 1,
                                                            startGenomCoord, totalSizeInView)));

                                                }

                                                rect.setWidth(20);
                                                rect.setHeight(20);
                                                rect.setHeight(height);
                                                rect.setFill(ptm.getColor());

                                                pepGroup.getChildren().add(rect);
                                                Tooltip.install(rect, tooltipTranscId);
                                            }

                                        }
                                    }
                                }
                            }
                        }

                        if(exon.getStart()<=peptideGenomicPos.getKey() && exon.getEnd()>=peptideGenomicPos.getKey()
                                && exon.getEnd()+1<peptideGenomicPos.getValue()){
                            Exon nextExon = transcript.getExons().get(i+1);
                            if(nextExon.getStart()<=peptideGenomicPos.getValue() && nextExon.getEnd()>=peptideGenomicPos.getValue()){
                                peptideRectangleStart = exon.getEnd();
                                peptideRectangleEnd = peptideGenomicPos.getValue();
                            }else{
                                peptideRectangleStart = exon.getEnd();
                                peptideRectangleEnd = nextExon.getEnd();
                            }

                            Rectangle pepRect = new Rectangle();
                            pepRect.setFill(Color.rgb(193, 154, 0, 0.4));
                            pepRect.setHeight(height/8);
                            pepRect.setY(height/2-pepRect.getHeight()/2);
                            int pepStartCoordInView = Math.max(startGenomCoord, peptideRectangleStart);
                            int pepEndCoordInView = Math.min(endGenomCoord, peptideRectangleEnd);

                            double totalSizeInView = endGenomCoord - startGenomCoord;
                            pepRect.setX(rectanglesAreaWidth * (getProportion(pepStartCoordInView, startGenomCoord, totalSizeInView)));
                            pepRect.setWidth(rectanglesAreaWidth * (getProportion(pepEndCoordInView, pepStartCoordInView, totalSizeInView)));

                            if (pepRect.getWidth() > 0) {
                                pepGroup.getChildren().add(pepRect);
                            }

                        }
                    }

                }
            }
            peptideSeqsRuns.get(pepSeq).add(Config.getMainRun(peptide.getRunName()));


        }

        return pepGroup;
    }

    /**
     * To smoothly handle the amount of increase-decrease
     *
     * @param typeOfZoom a String, "in" to zoom-in (nearer) or "out" to zoom-out
     * @return an integer with the amount of zoom.
     */
    private int getZoomValue(String typeOfZoom) {
        int distance = (int) ((int) geneSlider.getHighValue() - geneSlider.getLowValue());

        if (distance <= 30) {
            if (typeOfZoom.equals("in")) {
                return 0;
            } else {
                return 10;
            }

        } else if (distance <= 300) {
            return 10;
        } else if (distance <= 500) {
            return 100;
        } else if (distance <= 3000) {
            return 200;
        } else if (distance <= 10000) {
            return 1000;
        } else {
            return 2000;
        }
    }


    /**
     * Draws the intron-exons rectangles.
     *                       Calls drawExonsPane() to Gets Panes containing the rectangles representing the introns-exons.
     */
    private void drawExonsVBox() {

        for (Transcript transcript : displayedTranscripts) {

            int startPosition = (int) geneSlider.getLowValue();
            int endPosition = (int) geneSlider.getHighValue();
            HBox box = drawExonsHBox(transcript.getTranscriptId(), transcript, startPosition, endPosition); // exons

            if(box!=null){
                box.setOnMouseClicked(event -> {
                    drawerController.showtranscript(transcript);
                    event.consume();
                    drawer.open();
                    drawer.toFront();
                });


                if (showCdsInGeneBrowserBool && transcript.getHasCds() && viewType.equals("transcripts")) {
                    drawCdsForSpecificTranscript(transcript, box);
                }
            }

        }

    }


    private HBox drawExonsHBox(String transcriptId, Transcript transcript,
                               int startGenomPosition, int endGenomPosition) {




        if((transcript.getStartGenomCoord()>=startGenomPosition && transcript.getStartGenomCoord()<=endGenomPosition) ||
                (transcript.getEndGenomCoord()>=startGenomPosition && transcript.getEndGenomCoord()<=endGenomPosition) ||
                (startGenomPosition>=transcript.getStartGenomCoord()&& endGenomPosition<=transcript.getEndGenomCoord())) {


            HBox exonsHBox = new HBox();
            Pane exonPane = new Pane();
            exonPane.setId("exonPane");
            Group group = new Group();

            Text transcIdText = new Text(transcriptId);
            transcIdText.setFont(Font.font("monospace", fontSize));


            double rectanglesMaxWidth = representationWidthFinal;
            double exonHeight = transcIdText.getLayoutBounds().getHeight();


            // color depends on transcript has coding sequences (cds)
            Color exonsColour = Color.rgb(0, 0, 0);
            Color intronsColour = Color.rgb(0, 0, 0);

            if (transcript.getStartGenomCoord() < geneMinimumCoordinate || transcript.getEndGenomCoord() > geneMaximumCoordinate) {
                if (transcript.getHasCds()) {
                    exonsColour = Color.rgb(50, 168, 82);
                    intronsColour = Color.rgb(50, 168, 82, 0.5);
                } else {
                    exonsColour = Color.rgb(168, 50, 121, 1);
                    intronsColour = Color.rgb(168, 50, 121, 0.5);
                }
            } else {
                if (transcript.getHasCds()) {
                    exonsColour = Color.rgb(0, 102, 255, 1);
                    intronsColour = Color.rgb(0, 102, 255, 0.5);
                } else {
                    exonsColour = Color.rgb(255, 26, 26, 1);
                    intronsColour = Color.rgb(255, 26, 26, 0.5);
                }

            }


            // add tooltip: display transcId when hover
            String selectedCondition = conditionsGeneBrowserCombobox.getValue();
            Double avgTPM = transcriptHashMap.get(transcriptId).getAverageTPMByCondition(selectedCondition);
            String tooltipTranscIdString = "Average TPM (" + selectedCondition + "): " + avgTPM;
            Tooltip tooltipTranscId = new Tooltip(tooltipTranscIdString);
            tooltipTranscId.setShowDelay(Duration.ONE);
            tooltipTranscId.setFont(Font.font("monospace", fontSize));
            Tooltip.install(exonPane, tooltipTranscId);


            // default colour
            exonsHBox.setStyle("-fx-background-color:  transparent;");
            // add listeners
            //   hover listener
            exonsHBox.hoverProperty().addListener((observableValue, aBoolean, t1) -> {
                if (exonsHBox.isHover()) {
                    exonsHBox.setStyle("-fx-background-color: #ffffb3;");
                } else {
                    exonsHBox.setStyle("-fx-background-color: transparent;");
                }
            });


            double seqTotalSize = (double) endGenomPosition - startGenomPosition;


            // get exons list and sort it by start
            ArrayList<Exon> exonsList = transcript.getExons();


            // introns-exons representation

            int tmpLastPos = 0;
            int tmpExonEndPosition;
            int tmpExonStartPosition;

            // start exons
            for (int i = 0; i < exonsList.size(); i++) {
                Exon exon = exonsList.get(i);

                // Rectangle for the exons
                Rectangle exonRectangle = new Rectangle();
                exonRectangle.setFill(exonsColour);
                exonRectangle.setStroke(Color.TRANSPARENT);
                exonRectangle.setHeight(exonHeight);

                //Rectangle for the introns
                Rectangle intronRectangle = new Rectangle();
                intronRectangle.setFill(Color.TRANSPARENT);
                intronRectangle.setStroke(intronsColour);
                intronRectangle.setHeight(1);


                exonRectangle.setOnMouseClicked(mouseEvent -> {
                    if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 1) {
                        geneSlider.setLowValue(exon.getStart() - 15);
                        geneSlider.setHighValue(exon.getEnd() + 15);
                        displayExonSplicingInformation("chr1", 1111, 2222);
                    }
                });


                if (exon.getEnd() > startGenomPosition) {
                    if (i == 0) {
                        if (exon.getStart() <= startGenomPosition) {
                            tmpLastPos = startGenomPosition;
                        } else if (exon.getStart() > startGenomPosition && exon.getStart() < endGenomPosition) {
                            tmpLastPos = startGenomPosition;
                        } else if (exon.getStart() >= endGenomPosition) {
                            tmpLastPos = endGenomPosition;
                        }

                    } else {
                        if (exon.getStart() <= startGenomPosition) {
                            tmpLastPos = startGenomPosition;
                        } else if (exonsList.get(i - 1).getEnd() <= startGenomPosition) {
                            tmpLastPos = startGenomPosition;
                        } else if (exonsList.get(i - 1).getEnd() > startGenomPosition && exonsList.get(i - 1).getEnd() < endGenomPosition) {
                            tmpLastPos = exonsList.get(i - 1).getEnd();
                        } else if (exonsList.get(i - 1).getEnd() >= endGenomPosition) {
                            tmpLastPos = endGenomPosition;
                        }
                    }

                    if (exon.getStart() >= endGenomPosition) {
                        tmpExonStartPosition = endGenomPosition;
                    } else tmpExonStartPosition = Math.max(exon.getStart(), startGenomPosition);

                    tmpExonEndPosition = Math.min(exon.getEnd()+1, endGenomPosition);


                    if (i == 0) {

                        exonRectangle.setX(rectanglesMaxWidth * (getProportion(tmpExonStartPosition, tmpLastPos, seqTotalSize)));
                    } else {
                        //intron
                        intronRectangle.setY(exonHeight / 2.0);
                        intronRectangle.setX(rectanglesMaxWidth * (getProportion(tmpLastPos, startGenomPosition, seqTotalSize)));
                        intronRectangle.setWidth(rectanglesMaxWidth * (getProportion(tmpExonStartPosition, tmpLastPos, seqTotalSize)));
                        group.getChildren().add(intronRectangle);
                        // exon
                        exonRectangle.setX(rectanglesMaxWidth * (getProportion(tmpExonStartPosition, startGenomPosition, seqTotalSize)));
                    }
                    exonRectangle.setWidth(rectanglesMaxWidth * (getProportion(tmpExonEndPosition, tmpExonStartPosition, seqTotalSize)));
                    group.getChildren().add(exonRectangle);
                }
            }


            // mutations detected in cond
            double qualThreshold = Double.parseDouble(minQualFilterGeneBrowserTextField.getText());
            String conditionToSortBy = conditionsGeneBrowserCombobox.getValue();

            //   cond        sample       af/qual  value
            Map<String, Map<String, Map<String, Double>>> tmpConditions;

            // mutations: represented as black bars
            for (Variation variation : transcript.getVariations()) {
                int varGenomCoord = variation.getRefPos();

                tmpConditions = variation.getConditions();
                if (!tmpConditions.keySet().contains(conditionToSortBy)) { // filter by condition
                    continue;
                }

                boolean qualGteThreshold = false;
                Map<String, Map<String, Double>> sampleVariationMap = tmpConditions.get(conditionToSortBy);
                for (Map<String, Double> sampleVariation : sampleVariationMap.values()) {
                    if (sampleVariation.get("qual") >= qualThreshold) {
                        qualGteThreshold = true;
                    }
                }

                if (!qualGteThreshold) {
                    continue;
                }

                if (varGenomCoord >= startGenomPosition && varGenomCoord <= endGenomPosition) {


                    Rectangle variationRectangle = new Rectangle();
                    variationRectangle.setFill(Color.BLACK);
                    variationRectangle.setStroke(Color.TRANSPARENT);
                    variationRectangle.setHeight(exonHeight);
                    variationRectangle.setWidth(2);
                    variationRectangle.setX(rectanglesMaxWidth * (getProportion(varGenomCoord, startGenomPosition, seqTotalSize)));

                    // add click behavior
                    variationRectangle.setOnMouseClicked(mouseEvent -> {
                        if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 1) {
                            // to filter table //TODO:!!
//                        transcIDVarTableTextField.setText(transcriptId);
//                        seqPosVarTableTextField.setText(String.valueOf(variation.getPositionInTranscript(transcript.getTranscriptId())));
                        }
                    });


                    group.getChildren().add(variationRectangle);

                }
            }


            // rectangle that signals the limit of the genes area
            Rectangle limitRectangle = new Rectangle();
            limitRectangle.setFill(Color.rgb(240, 240, 240));
            limitRectangle.setX(rectanglesMaxWidth + 1);
            limitRectangle.setWidth(geneExonsSeqsVBox.getWidth() - (rectanglesMaxWidth + 1));
            limitRectangle.setHeight(exonPane.getPrefHeight());
            group.getChildren().add(limitRectangle);

            // add transcript ID


            transcIdText.setX(rectanglesMaxWidth + 5);
            transcIdText.setY(0);


            exonPane.getChildren().add(group);
            //exonPane.setPadding(new Insets( (int) Math.round(representationHeightFinal*0.01), 0, 0, 0));
            exonsHBox.getChildren().add(exonPane);

            exonsHBox.getChildren().add(transcIdText);

            VBox.setMargin(exonsHBox, new Insets((int) Math.round(representationHeightFinal * 0.02), 0, 0, 0));
            geneExonsSeqsVBox.getChildren().add(exonsHBox);

            return exonsHBox;
        }
        return null;

    }



    private void drawAlignedSequencesVBox() {


        geneExonsSeqsVBox.getChildren().add(drawRefDNA());
        for ( Transcript transcript : displayedTranscripts) {

            String formatedSeq = transcFormatedSequencesMap.get(transcript.getTranscriptId());

            String strand = transcript.getStrand();

            Pane seqHBox = getSequencesHBox(transcript.getTranscriptId(), transcript, strand, formatedSeq);
            if(seqHBox!=null){
                // draw sequences
                geneExonsSeqsVBox.getChildren().add(seqHBox);

                // draw cds
                if (showCdsInGeneBrowserBool && transcript.getHasCds() && viewType.equals("transcripts")) {
                    drawCdsForSpecificTranscript(transcript, seqHBox);
                }
            }

        }

    }


    /**
     * Gets a proportion, used instead of having to write the math operation each time.
     *
     * @param end       end position or coordinate.
     * @param start     start position or coordinate.
     * @param totalSize total size of the window or sequence
     * @return a double that is the proportion.
     */
    private double getProportion(double end, double start, double totalSize) {

        if (totalSize > 0) {
            return (end - start) / (totalSize);
        }
        return 0;
    }


    /**
     * Formats a String, that is, divides the RNA sequence
     * in introns ("-") and exons (according letter) and, if needed
     * adds spaces at the beginning and at the end, so that sequences
     * are aligned.
     *
     */
    private String formatSequence(String transcriptId, Transcript transcript, int minimumCoordinate, int maximumCoordinate) {
        String formatedSeqString = "";
        String transcSeq = transcript.getSequence();

        // get exons list and sort it by start
        ArrayList<Exon> exonsList = transcript.getExons();

        String blanckCharacter = " ";
        String intronSpace = "-";

        int distance = 0;
        int currentSeqPosition = 0;

        for (int i = 0; i < exonsList.size(); i++) {
            Exon exon = exonsList.get(i); // get Exon

            if (i == 0) {
                // add additional spaces to the sequence at the beginning
                if (exon.getStart() != minimumCoordinate) {
                    distance = exon.getStart() - minimumCoordinate; // coordinates are end inclusive

                    formatedSeqString += blanckCharacter.repeat(distance);
                }


                // since it's the first exon substring(0,...)
                formatedSeqString += transcSeq.substring(0, exon.getEnd() - exon.getStart() + 1); // +1 since coordinates are end inclusive
                currentSeqPosition += exon.getEnd() - exon.getStart() + 1; // this is where the next start coord is

            } else { // not the first exon in the list

                formatedSeqString += intronSpace.repeat(exon.getStart() - exonsList.get(i - 1).getEnd() - 1); // -1 since coords are end inclusive
                formatedSeqString += transcSeq.substring(currentSeqPosition, currentSeqPosition + (exon.getEnd() - exon.getStart() + 1)); // +1 since coordinates are end inclusive
                currentSeqPosition += exon.getEnd() - exon.getStart() + 1;  // this is where the next start coord is

                if (i == exonsList.size() - 1) {

                    formatedSeqString += blanckCharacter.repeat(maximumCoordinate - exon.getEnd());
                }
            }


        }


        return formatedSeqString;
    }

    /**
     *
     */
    public Pane getSequencesHBox(String transcriptId, Transcript transcript, String strand, String formatedSequence) {
        int startSeqPos = (int) geneSlider.getLowValue() - geneViewerMinimumCoordinate;
        int charNum = (int) (geneSlider.getHighValue() - geneSlider.getLowValue());


        if((transcript.getStartGenomCoord()>=geneSlider.getLowValue()  && transcript.getStartGenomCoord()<=geneSlider.getHighValue() ) ||
                (transcript.getEndGenomCoord()>=geneSlider.getLowValue()  && transcript.getEndGenomCoord()<=geneSlider.getHighValue() ) ||
                (geneSlider.getLowValue() >=transcript.getStartGenomCoord() && geneSlider.getHighValue() <=transcript.getEndGenomCoord())) {


            double rectanglesMaxWidth = representationWidthFinal;


            String subSeq = formatedSequence.substring(startSeqPos, startSeqPos + charNum);

            ArrayList<Integer> varFormatedSubseqPos = new ArrayList<>();

            // mutations detected in cond
            double qualThreshold = Double.parseDouble(minQualFilterGeneBrowserTextField.getText());
            String conditionToSortBy = conditionsGeneBrowserCombobox.getValue();


            for (Variation variation : transcript.getVariations()) {
                int varGenomCoord = variation.getRefPos();
                if (varGenomCoord >= geneSlider.getLowValue() && varGenomCoord <= geneSlider.getHighValue()) {
                    varFormatedSubseqPos.add(varGenomCoord - (int) geneSlider.getLowValue());
                }
            }

            HBox seqHBox = new HBox();
            seqHBox.setStyle("-fx-background-color:  transparent;");

            // add click behavior
            seqHBox.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
                    // to filter table
//                transcIDVarTableTextField.setText(transcriptId); //TODO:!!
//                displayCds(transcriptId); // TODO: change this !!
                }
            });

            // add listeners
            //   hover listener
            seqHBox.hoverProperty().addListener((observableValue, aBoolean, t1) -> {
                if (seqHBox.isHover()) {
                    seqHBox.setStyle("-fx-background-color: #ffffb3;");

                } else {
                    seqHBox.setStyle("-fx-background-color: transparent;");
                }
            });


            Pane group = new Pane();

            final double width = rectanglesMaxWidth / charNum;
            int height = (int) Math.round(representationHeightFinal * 0.02);
            double currentX = 0;


            for (int i = 0; i < subSeq.length(); i++) {

                String nucl = subSeq.substring(i, i + 1);

                Text t = new Text(nucl);
                t.setFont(Font.font("monospace", FontWeight.BOLD, fontSize));
                t.setX((i + 0.5) * width - t.getLayoutBounds().getWidth()/2);
                t.setY(t.getLayoutBounds().getHeight());

                //t.setTextAlignment(TextAlignment.CENTER);

//            t.setY(-2);
//            t.setLayoutX(width);
//            t.setLayoutY(height);



                Rectangle r = new Rectangle();
                r.setX(i * width);

                r.setWidth(width);
                r.setHeight(height);

                currentX+=width;


                String nuclUpper = nucl.toUpperCase();
                if (varFormatedSubseqPos.contains(i)) {
                    t.setFont(Font.font(null, FontWeight.BOLD, fontSize + 3));

                    int finalI = i;
                    EventHandler<MouseEvent> eventHandler = mouseEvent -> {
                        mouseEvent.consume();
                        if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 1) {
                            int genomCoord = finalI + (int) geneSlider.getLowValue();
//                        transcIDVarTableTextField.setText(transcriptId);   //TODO:!!
//                        genomeStartCoordVarTableSpinner.getValueFactory().setValue(genomCoord);
//                        genomeEndCoordVarTableSpinner.getValueFactory().setValue(genomCoord);

                            for (CDS cds : transcript.getCdss()) {

                                Pair<Integer, Integer> genomicPosCds = cds.getGenomicPos(transcript);
                                if (finalI + 1 + startSeqPos + geneViewerMinimumCoordinate >= genomicPosCds.getKey() &&
                                        finalI + 1 + startSeqPos + geneViewerMinimumCoordinate < genomicPosCds.getValue()) {

                                    String sub = formatedSequence.substring(0, finalI + startSeqPos + 1);

                                    mutatedCdsController.showCds(cds,
                                            finalI + startSeqPos - sub.length() +
                                                    sub.replaceAll("-", "").replaceAll(" ", "").length()
                                                    - cds.getTranscriptsWithCdsPos().get(transcript).getKey());

                                    extraInfoTitledPane.setExpanded(true);
                                    infoTitleTabs.getSelectionModel().select(1);
                                    break;
                                }


                            }
                        }
                    };

                    r.setOnMouseClicked(eventHandler);
                    t.setOnMouseClicked(eventHandler);

                    EventHandler<MouseEvent> onMouseIn = mouseEvent -> {
                        ControllersBasket.getScene().getRoot().setCursor(javafx.scene.Cursor.HAND);
                    };
                    EventHandler<MouseEvent> onMouseOut = mouseEvent -> {
                        ControllersBasket.getScene().getRoot().setCursor(javafx.scene.Cursor.DEFAULT);
                    };
                    r.setOnMouseEntered(onMouseIn);
                    t.setOnMouseEntered(onMouseIn);



                    r.setOnMouseExited(onMouseOut);
                    t.setOnMouseExited(onMouseOut);



                    if (nuclUpper.equals("A")) {
                        r.setFill(Color.rgb(51, 51, 255, 1));
                    } else if (nuclUpper.equals("G")) {
                        r.setFill(Color.rgb(255, 0, 0, 1));
                    } else if (nuclUpper.equals("C")) {
                        r.setFill(Color.rgb(119, 119, 60, 1));
                    } else if (nuclUpper.equals("T")) {
                        r.setFill(Color.rgb(255, 102, 172, 1));
                    } else if (nucl.equals("-")) {
                        r.setFill(Color.LIGHTGRAY);
                        r.setOpacity(0.3);
                    } else {
                        r.setFill(Color.TRANSPARENT);
                    }
                } else {
                    t.setFont(Font.font(null, fontSize));
                    if (nuclUpper.equals("A")) {
                        r.setFill(Color.rgb(51, 51, 255, 0.3));
                    } else if (nuclUpper.equals("G")) {
                        r.setFill(Color.rgb(255, 0, 0, 0.3));
                    } else if (nuclUpper.equals("C")) {
                        r.setFill(Color.rgb(119, 119, 60, 0.3));
                    } else if (nuclUpper.equals("T")) {
                        r.setFill(Color.rgb(255, 102, 172, 0.3));
                    } else if (nucl.equals("-")) {
                        r.setFill(Color.LIGHTGRAY);
                        r.setOpacity(0.3);
                    } else {
                        r.setFill(Color.TRANSPARENT);
                    }

                }




                group.getChildren().add(r);
                group.getChildren().add(t);

            }


            // add transcript ID
            Text transcIdText = new Text(transcriptId);
            transcIdText.setFont(Font.font("monospace", fontSize));
            transcIdText.setY(transcIdText.getLayoutBounds().getHeight());
            transcIdText.setX(currentX + 5);
            group.getChildren().add(transcIdText);

            //group.setStyle("-fx-background-color: green");
//            transcIdText.setStyle("-fx-background-color: blue");

            //seqHBox.getChildren().add(group);
            //seqHBox.getChildren().add(transcIdText);
            return group;
        }
        return null;
    }





    /**
     * Filter Variants Table
     * Uses the Variants Array List obtained previously
     */
    private void filterVariantsTable() {
        varTableView.getItems().clear();

        // make a copy to not modify the original list
        LinkedList<Variation> variationsLinkedListClone = (LinkedList<Variation>) variations.clone();


        //TODO:!!

        varTableView.getItems().addAll(variationsLinkedListClone);
    }


    /**
     * controls CDS
     */
    private void displayCdsTab(String cdsSequenceOfInterest) {

        mutatedCdsController.showCds(cdss.get(cdsSequenceOfInterest), -1);

    }


    /**
     * Sets options for the combobox and displays the exons or sequences
     * //     * @param transcriptHashMap Map key is the transcript ID, Value is the Transcript object
     * controls the order in which the transcripts are displayed
     */
    private void setConditionsCombobox() {
        activateListenerConditionsGeneBrowserCombobox = false;


        conditionsGeneBrowserCombobox.getItems().clear();
        // get all conditions
        conditions = new ArrayList<>();


        for (Map.Entry<String, Transcript> transcMapEntry : transcriptHashMap.entrySet()) {
            Transcript transcript = transcMapEntry.getValue();
            for (String cond : transcript.getTpms().keySet()) {
                if (!conditions.contains(cond)) { // prevent replicates
                    conditions.add(cond);
                    conditionsGeneBrowserCombobox.getItems().add(cond);
                }
            }
        }


        activateListenerConditionsGeneBrowserCombobox = true;


        // draw graph
        conditionsGeneBrowserCombobox.getSelectionModel().select(0);



    }



    private void displayExonSplicingInformation(String chr, int startCoord, int endCoord) {

    }

    /**
     * Used to draw the tpms barchart per condition
     *
     * @param transcIds
     */
    private BarChart<Number, String> getAvgTPMBarChartByCond(Set<String> transcIds) {


        final CategoryAxis yAxis = new CategoryAxis();
        final NumberAxis xAxis = new NumberAxis();
        final BarChart<Number, String> avgTPMByConditionBarchart = new BarChart<>(xAxis, yAxis);


        xAxis.setLabel("TPM");
        yAxis.setLabel("Transc. ID");

        for (String condition : conditions) {
            XYChart.Series chartSeries = new XYChart.Series();

            chartSeries.setName(condition);
            for (String trancID : transcIds) {


                Transcript transcript = transcriptHashMap.get(trancID);
                if (transcript.identifiedInCondition(condition)) {
                    Double avgTPM = transcript.getAverageTPMByCondition(condition);
                    avgTPM = (avgTPM.equals(Double.NaN)) ? 0 : avgTPM;
                    chartSeries.getData().add(new XYChart.Data(avgTPM, trancID));
                }
            }

            avgTPMByConditionBarchart.getData().add(chartSeries);

        }

        return avgTPMByConditionBarchart;


    }



    private void drawCdsForSpecificTranscript(Transcript transcript, Pane transcriptBox) {

        String conditionToSortBy = conditionsGeneBrowserCombobox.getValue();

        int startGenomCoord = (int) geneSlider.getLowValue();
        int endGenomCoord = (int) geneSlider.getHighValue();


        double vBoxwidth = transcriptBox.getChildren().get(0).getBoundsInParent().getWidth();



        for(CDS cds: transcript.getCdss()){
            drawCds(cds, transcript, startGenomCoord, endGenomCoord, vBoxwidth);
        }

    }


    private Pair<Integer, Integer> getPepPos(String pepSeq, CDS cds, Transcript transcript){

        // replace I for L, since leusine and isoleucine have the same mass and are indistiguishable in mass spec
        String tmpPepSeq = pepSeq.replace("I", "L");
        String tmpCdsSeq = cds.getSequence().replace("I", "L");

        // get matching seq
        Pattern pattern = Pattern.compile(tmpPepSeq);
        Matcher matcher = pattern.matcher(tmpCdsSeq);
        // Check all occurrences


        Pair<Integer, Integer> cdsPos = cds.getTranscriptWithCdsPos(transcript);

        while (matcher.find()) {
            int pepMatchAaPosStart  =  matcher.start();
            int pepMatchAaPosEnd  =  matcher.end() - 1; // -1 cause end is not inclusive

            int pepRnaStart;
            int pepRnaEnd;


            if (cds.getStrand().equals("+")){
                pepRnaStart = cdsPos.getKey() + ((pepMatchAaPosStart ) * 3) - 1 ;
                pepRnaEnd = cdsPos.getKey() + ((pepMatchAaPosEnd ) * 3) + 2;
            } else {
                pepRnaEnd = cdsPos.getValue() - ((pepMatchAaPosStart ) * 3) ;
                pepRnaStart =  cdsPos.getValue() - ((pepMatchAaPosEnd ) * 3) - 3; // displaced by -1
            }

            int pepStartCoord = transcript.genomCoordFromSeqPos(pepRnaStart);
            int pepEndCoord = transcript.genomCoordFromSeqPos(pepRnaEnd);

            return new Pair<>(pepStartCoord, pepEndCoord);
        }
        return null;
    }

    private void drawCds(CDS cds, Transcript transcript, int startGenomCoord, int endGenomCoord, double containerWidth) {
        HBox cdsHBox = new HBox();
        HBox pepHBox = new HBox();

        int height = (int) Math.round(representationHeightFinal * 0.02);
        cdsHBox.setPrefHeight(height);

        Pane cdsPane = new Pane();
        Group cdsGroup = new Group();
        Pane pepPane = new Pane();
        Group pepGroup = new Group();

        double rectanglesAreaWidth = representationWidthFinal;


        int length = endGenomCoord - startGenomCoord;

        cdsPane.setPrefHeight(height);
        cdsPane.setPrefWidth(rectanglesAreaWidth);
        cdsHBox.setPrefWidth(rectanglesAreaWidth);


        boolean hasPfam = cds.hasPfam();
        String cdsSeq = cds.getSequence();
        Pair<Integer, Integer> cdsGenomicCoords = cds.getGenomicPos(transcript);

        if((cdsGenomicCoords.getKey()>startGenomCoord && cdsGenomicCoords.getKey()<endGenomCoord) ||
                (cdsGenomicCoords.getValue()>startGenomCoord && cdsGenomicCoords.getValue()<endGenomCoord) ||
                (startGenomCoord>cdsGenomicCoords.getKey() && endGenomCoord<cdsGenomicCoords.getValue()) ||
                (cdsGenomicCoords.getKey()<startGenomCoord && cdsGenomicCoords.getValue()>startGenomCoord) ||
                (cdsGenomicCoords.getKey()<endGenomCoord && cdsGenomicCoords.getValue()>endGenomCoord)) {

            int cdsStartGenomCoord = cdsGenomicCoords.getKey();
            int cdsEndGenomCoord = cdsGenomicCoords.getValue();


            for (int i = 0; i < transcript.getExons().size(); i++) {
                Exon exon = transcript.getExons().get(i);

                int cdsRectangleStart=0, cdsRectangleEnd=0;


                if(((exon.getStart()>=cdsStartGenomCoord && exon.getStart()<=cdsEndGenomCoord) ||
                        (exon.getEnd()>=cdsStartGenomCoord && exon.getEnd()<=cdsEndGenomCoord)) &&
                        ((exon.getStart()>=startGenomCoord && exon.getStart()<=endGenomCoord) ||
                                (exon.getEnd()>=startGenomCoord && exon.getEnd()<=endGenomCoord)) ||
                        (exon.getStart()<=cdsStartGenomCoord && exon.getEnd()>=cdsStartGenomCoord
                                && exon.getStart()<=cdsEndGenomCoord && exon.getEnd()>=cdsEndGenomCoord) ||
                        (exon.getStart()<=startGenomCoord && startGenomCoord<=exon.getEnd() &&
                                exon.getStart()<=endGenomCoord && endGenomCoord<=exon.getEnd()   )) {
                    if (exon.getStart() <= cdsStartGenomCoord) {

                        cdsRectangleStart = Math.max(cdsStartGenomCoord+1, startGenomCoord);
                        if (exon.getEnd() <= cdsEndGenomCoord) {
                            cdsRectangleEnd = Math.min(exon.getEnd()+1, endGenomCoord);
                        } else {
                            cdsRectangleEnd = Math.min(cdsEndGenomCoord+1, endGenomCoord);
                        }
                    } else {
                        cdsRectangleStart = Math.max(exon.getStart()+1, startGenomCoord);
                        if (exon.getEnd() <= cdsEndGenomCoord) {
                            cdsRectangleEnd = Math.min(exon.getEnd()+1, endGenomCoord);
                        } else {
                            cdsRectangleEnd = Math.min(cdsEndGenomCoord+1, endGenomCoord);
                        }

                    }
                }



                    if (cdsRectangleStart != cdsRectangleEnd) {

                        Rectangle cdsRectangle = new Rectangle();
                        cdsRectangle.setHeight(height - 1);

                        Tooltip cdsToolTip = new Tooltip("CDS");
                        cdsToolTip.setShowDelay(Duration.millis(500));
                        cdsToolTip.setFont(Font.font(fontSize));
                        cdsToolTip.setShowDuration(Duration.seconds(4));
                        Tooltip.install(cdsRectangle, cdsToolTip);

                        cdsRectangle.setOnMouseClicked(mouseEvent -> {
                            if (mouseEvent.getClickCount() == 1 && mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                                displayCdsTab(cdsSeq);
                            }
                        });

                        //  color depending on having peptides
                        if (cds.getPeptides().size() > 0) {
                            cdsRectangle.setFill(Color.rgb(217, 33, 122));
                        } else {
                            cdsRectangle.setFill(Color.rgb(217, 33, 122, 0.5));
                        }
                        cdsRectangle.setStroke(Color.BLACK);
                        cdsRectangle.setStrokeWidth(2);


                        int tmpStartGeneCoord = Math.max(cdsRectangleStart, startGenomCoord) - 1;
                        int tmpEndGeneCoord = Math.min(cdsRectangleEnd, endGenomCoord);

                        double width = rectanglesAreaWidth * Math.abs(getProportion(tmpEndGeneCoord, tmpStartGeneCoord, length));
                        double X = rectanglesAreaWidth * getProportion(tmpStartGeneCoord, startGenomCoord, length);

                        cdsRectangle.setX(X);
                        cdsRectangle.setWidth(width);

                        cdsGroup.getChildren().add(cdsRectangle);





                        double offsetY = 0;
                        if (geneSlider.getHighValue() - geneSlider.getLowValue() <= 500) {
                            Pair<String, Integer[]> pair = cds.getSubStringWithOffset(transcript, tmpStartGeneCoord, tmpEndGeneCoord);

                            if (pair != null) {

                                if (cds.getStrand().equals("-")) {
                                    Line arrowLine = new Line(X, 0.5 * height, X + width, 0.5 * height);
                                    arrowLine.setStrokeWidth(3);
                                    arrowLine.setStroke(new Color(0, 0, 0, 0.3));

                                    Line arrowLine2 = new Line(X, 0.5 * height, X + 0.1 * width, 0);
                                    arrowLine2.setStrokeWidth(3);
                                    arrowLine2.setStroke(new Color(0, 0, 0, 0.3));

                                    Line arrowLine3 = new Line(X, 0.5 * height, X + 0.1 * width, height);
                                    arrowLine3.setStrokeWidth(3);
                                    arrowLine3.setStroke(new Color(0, 0, 0, 0.3));

                                    cdsGroup.getChildren().add(arrowLine);
                                    cdsGroup.getChildren().add(arrowLine2);
                                    cdsGroup.getChildren().add(arrowLine3);

                                }

                                String subseq = pair.getKey();
                                int offset = pair.getValue()[0];
                                for (int j = 0; j < subseq.length(); j++) {
                                    Text t = new Text(String.valueOf(subseq.charAt(j)));
                                    t.setFont(Font.font("monospace", fontSize));


                                    t.setX(X + (j * 3 + offset + pair.getValue()[1] + 0.5) *
                                            (rectanglesAreaWidth * ((double) subseq.length() * 3 / (endGenomCoord - startGenomCoord)) / (double) (subseq.length() * 3)) - t.getLayoutBounds().getWidth() / 2);
                                    t.setY(t.getLayoutBounds().getHeight());

                                    cdsGroup.getChildren().add(t);
                                }


                            }

                        }
                    }

                cdsRectangleStart = 0;
                cdsRectangleEnd = 0;


                if(exon.getEnd()<cdsEndGenomCoord && exon.getEnd()>=cdsStartGenomCoord){
                    Exon nextExon = transcript.getExons().get(i+1);




                    if((nextExon.getStart()>startGenomCoord && nextExon.getStart()<=endGenomCoord) || (
                            exon.getStart()>= startGenomCoord && nextExon.getEnd()<=endGenomCoord
                            )){
                        cdsRectangleStart = Math.max(startGenomCoord, exon.getEnd()+1);
                        cdsRectangleEnd = Math.min(nextExon.getStart(), endGenomCoord);
                    }else if((exon.getStart()>=startGenomCoord && exon.getStart()<=endGenomCoord) || (
                            exon.getEnd()>=startGenomCoord && exon.getEnd()<=endGenomCoord
                    )){
                        cdsRectangleStart = Math.max(startGenomCoord, exon.getEnd()+1);
                        cdsRectangleEnd = endGenomCoord;
                    }else if(exon.getEnd()<=startGenomCoord && nextExon.getStart()>=endGenomCoord){
                        cdsRectangleStart = startGenomCoord;
                        cdsRectangleEnd = endGenomCoord;
                    }

                }



                if(cdsRectangleStart!=cdsRectangleEnd){
                    Rectangle cdsRectangleBetweenExons = new Rectangle();
                    cdsRectangleBetweenExons.setHeight(height / 8.);


                    Tooltip cdsToolTipBetweenExons = new Tooltip("CDS");
                    cdsToolTipBetweenExons.setShowDelay(Duration.millis(500));
                    cdsToolTipBetweenExons.setFont(Font.font(fontSize));
                    cdsToolTipBetweenExons.setShowDuration(Duration.seconds(4));
                    Tooltip.install(cdsRectangleBetweenExons, cdsToolTipBetweenExons);

                    cdsRectangleBetweenExons.setOnMouseClicked(mouseEvent -> {
                        if (mouseEvent.getClickCount() == 1 && mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                            displayCdsTab(cdsSeq);
                        }
                    });

                    //  color depending on having peptides
                    if (cds.getPeptides().size() > 0) {
                        cdsRectangleBetweenExons.setFill(Color.rgb(0, 0, 0, 0.2));
                    } else {
                        cdsRectangleBetweenExons.setFill(Color.rgb(0, 0, 0, 0.1));
                    }


                    cdsRectangleBetweenExons.setY(height / 8.);
                    cdsRectangleBetweenExons.setY(height/2.-cdsRectangleBetweenExons.getHeight()/2);


                    double width = rectanglesAreaWidth * Math.abs(getProportion(cdsRectangleEnd, cdsRectangleStart, length));
                    double X = rectanglesAreaWidth * getProportion(cdsRectangleStart, startGenomCoord, length);

                    cdsRectangleBetweenExons.setX(X);
                    cdsRectangleBetweenExons.setWidth(width);

                    cdsGroup.getChildren().add(cdsRectangleBetweenExons);
                }



            }

            if (cds.getPeptides().size() > 0) {

                pepGroup = getPepGroup(cds, transcript, height, startGenomCoord, endGenomCoord, rectanglesAreaWidth);
            }
            // add peptides
            if (pepGroup.getChildren().size() > 0) {
                pepPane.getChildren().add(pepGroup);

            }




            if (hasPfam) {


                for (Pfam pfam : cds.getPfams()) {
                    Rectangle pfamRectangle = new Rectangle();
                    pfamRectangle.setHeight(height);

                    pfamRectangle.setFill(Color.rgb(153, 255, 153));
                    pfamRectangle.setStroke(Color.WHITE);

                    Pair<Integer, Integer> cdsTranscriptCoord = cds.getTranscriptWithCdsPos(transcript);
                    int pfamStartCoord = cdsTranscriptCoord.getKey() + ((pfam.getAaStart() - 1) * 3) - 1; // -1 since are 1 indexed
                    int pfamEndCoord = cdsTranscriptCoord.getKey() + ((pfam.getAaEnd() - 1) * 3) + 2; // -1 since are 1 indexed


                    pfamStartCoord = transcript.genomCoordFromSeqPos(pfamStartCoord);
                    pfamEndCoord = transcript.genomCoordFromSeqPos(pfamEndCoord);

                    pfamRectangle.setOnMouseClicked(event -> {
                        drawerController.showPfam(pfam);
                        event.consume();
                        drawer.open();
                        drawer.toFront();
                    });


                    int pfamStartCoordInView = Math.max(startGenomCoord, pfamStartCoord);
                    int pfamEndCoordInView = Math.min(endGenomCoord, pfamEndCoord);

                    double totalSizeInView = endGenomCoord - startGenomCoord;

                    pfamRectangle.setX(rectanglesAreaWidth * (getProportion(pfamStartCoordInView, startGenomCoord, totalSizeInView)));
                    pfamRectangle.setWidth(rectanglesAreaWidth * (getProportion(pfamEndCoordInView, pfamStartCoordInView, totalSizeInView)));
                    cdsGroup.getChildren().add(pfamRectangle);

                    // Tooltip for pfam domain
                    Tooltip pfamToolTip = new Tooltip(pfam.getDesc());
                    pfamToolTip.setShowDelay(Duration.ZERO);
                    pfamToolTip.setFont(Font.font(fontSize));
                    Tooltip.install(pfamRectangle, pfamToolTip);
                    pfamToolTip.setShowDuration(Duration.seconds(4));

                }
            }


            // add cds
            cdsPane.getChildren().add(cdsGroup);
            cdsHBox.getChildren().add(cdsPane);
            //VBox.setMargin(cdsHBox, new Insets((int) Math.round(representationHeightFinal * 0.005), 0, 0, 0));
            geneExonsSeqsVBox.getChildren().add(cdsHBox);

            pepHBox.getChildren().add(pepPane);
            geneExonsSeqsVBox.getChildren().add(pepHBox);





        }



    }


    public Pane drawRefDNA(){
        String seq = fastaIndex.getSequenceAt(chr, (int) geneSlider.getLowValue(), (int) geneSlider.getHighValue());
        Pane group = new Pane();

        final double width = representationWidthFinal / seq.length();
        int height = (int) Math.round(representationHeightFinal * 0.02);





        for (int i = 0; i < seq.length(); i++) {

            String nucl = seq.substring(i, i + 1);

            Text t = new Text(nucl);
            t.setFont(Font.font("monospace", FontWeight.BOLD, fontSize));
            t.setX((i + 0.5) * width - t.getLayoutBounds().getWidth()/2);
            t.setTextAlignment(TextAlignment.CENTER);
            t.setY(t.getLayoutBounds().getHeight());


            Rectangle r = new Rectangle();
            r.setX(i * width);
            r.setWidth(width);
            r.setHeight(height);


            String nuclUpper = nucl.toUpperCase();
            t.setFont(Font.font(null, fontSize));
            if (nuclUpper.equals("A")) {
                r.setFill(Color.rgb(51, 51, 255, 0.3));
            } else if (nuclUpper.equals("G")) {
                r.setFill(Color.rgb(255, 0, 0, 0.3));
            } else if (nuclUpper.equals("C")) {
                r.setFill(Color.rgb(119, 119, 60, 0.3));
            } else if (nuclUpper.equals("T")) {
                r.setFill(Color.rgb(255, 102, 172, 0.3));
            } else if (nucl.equals("-")) {
                r.setFill(Color.LIGHTGRAY);
            } else {
                r.setFill(Color.TRANSPARENT);
            }

            r.toBack();
            t.toFront();

            group.getChildren().add(r);
            group.getChildren().add(t);

        }
        return group;

    }


    public void getMaxTranscriptIdWidth(){
        int maxLength = 0;
        String longestTranscriptId=null;
        for(Transcript transcript: transcriptHashMap.values()){
            if(transcript.getTranscriptId().length()>maxLength){
                maxLength = transcript.getTranscriptId().length();
                longestTranscriptId = transcript.getTranscriptId();
            }
        }
        Text text = new Text(longestTranscriptId);
        text.setFont(Font.font("monospace", fontSize));
        maxTranscriptIdWidth = text.getBoundsInLocal().getWidth();
        representationWidthFinal = geneBrowserScrollPane.getWidth() - maxTranscriptIdWidth - 10;
        representationHeightFinal = geneBrowserScrollPane.getHeight();

    }

    public void updateSettings(org.json.JSONObject settings){
        fontSize = settings.getJSONObject("Fonts").getJSONObject("browser").getInt("size");
        bamPaneController.show((int) geneSlider.getLowValue(), (int) geneSlider.getHighValue());
        if(selectedGene!=null){
            redraw();
        }

    }

    private void redraw(){
        getMaxTranscriptIdWidth();
        transcriptOrCdsCentricView();
    }

    public void showGeneBrowser(String gene){
        geneIdTextField.setText(gene);
        displaygeneFromGeneSymbol(gene, true);
    }

    public void showGeneBrowser(String gene, Peptide peptide){
        geneIdTextField.setText(gene);
        displaygeneFromGeneSymbol(gene, false);

        boolean found = false;

        for(Transcript transcript: transcriptHashMap.values()){
            if(found)
                break;
            for(CDS cds: transcript.getCdss()){
                if(found)
                    break;
                for(Peptide cdsPeptide: cds.getPeptides()){
                    if(cdsPeptide.getSequence().equals(peptide.getSequence())){
                        Pair<Integer, Integer> pepPos = getPepPos(peptide.getSequence(), cds, transcript);


                        geneSlider.setLowValue(pepPos.getKey());
                        geneSlider.setHighValue(pepPos.getValue());

                        found = true;
                        transcriptOrCdsCentricView();
                        break;
                    }
                }
            }
        }

    }

    public void showGeneAtPosition(String gene, int pos){
        geneIdTextField.setText(gene);
        displayGeneFromId(gene, pos, pos, false);
    }
    public void showGeneAtPosition(String gene, int start, int end){
        geneIdTextField.setText(gene);
        displayGeneFromId(gene, start, end,false);
    }

    public void resize(){
        if(selectedGene!=null){
            redraw();
            bamPaneController.resize(representationWidthFinal);
        }

    }
    public void onTrackFilesUpdated(){
        bamPaneController.onTrackFilesUpdated();
        bedPaneController.onTrackFilesUpdated();
    }

    @FXML
    public void findSequence() {


        if(!findSeqField.getText().equals(sequenceSearched)){
            sequenceSearchestarts = new ArrayList<>();

            for(Transcript transcript: transcriptHashMap.values()){
                int index = transcript.getSequence().indexOf(findSeqField.getText());
                if(index!=-1 && !sequenceSearchestarts.contains(transcript.getStartGenomCoord()+index)){
                    sequenceSearchestarts.add(transcript.getStartGenomCoord()+index);
                }

            }
            currentSequenceSearchMatchIndex = 0;
            if(sequenceSearchestarts.size()==0){
                seqSearchMatchIndexLabel.setText("Not found");
            }

            sequenceSearched = findSeqField.getText();
        }
        if(sequenceSearchestarts.size()>0){
            if(currentSequenceSearchMatchIndex==sequenceSearchestarts.size()){
                currentSequenceSearchMatchIndex=0;
            }

            seqSearchMatchIndexLabel.setText(currentSequenceSearchMatchIndex+1+"/"+sequenceSearchestarts.size());
            int start = sequenceSearchestarts.get(currentSequenceSearchMatchIndex++);

            geneSlider.setHighValue((double)start+findSeqField.getText().length());
            geneSlider.setLowValue(start);
            geneSlider.setHighValue((double)start+findSeqField.getText().length());
            geneSlider.setLowValue(start);
            transcriptOrCdsCentricView();
        }




    }
}