package Controllers;

import FileReading.AllGenesReader;
import Singletons.Config;
import Singletons.Database;
import TablesModels.FoldChangeTableModel;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import graphics.ConfidentBarChart;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import org.dizitart.no2.*;
import org.dizitart.no2.filters.Filters;
import org.json.JSONObject;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.function.DoubleBinaryOperator;

import static org.dizitart.no2.filters.Filters.*;


public class DgeTableController extends Controller {

    @FXML
    private JFXTextField geneFilterFoldChangeTextField;

    @FXML
    private JFXComboBox dgeComparisonCombobox;
    @FXML
    private JFXComboBox protComparisonCombobox;
    @FXML
    private Spinner<Double> adjPValFilterFoldChangeSpinner;
    @FXML
    private Spinner<Double> foldFilterFoldChangeSpinner;
    @FXML
    private CheckBox protFilterCheckbox;

    @FXML
    private TableView<FoldChangeTableModel> foldChangeTableView;
    @FXML
    private TableColumn<FoldChangeTableModel, String > geneSymbolFoldChangeTableColumn;
    @FXML
    private TableColumn<FoldChangeTableModel, String > geneTypeFoldChangeTableColumn;
    @FXML
    private TableColumn<FoldChangeTableModel, Double > logFoldFoldChangeTableColumn;
    @FXML
    private TableColumn<FoldChangeTableModel, Double > pValFoldChangeTableColumn;
    @FXML
    private TableColumn<FoldChangeTableModel, Boolean > hasPeptideColumn;
    @FXML
    private TableColumn<FoldChangeTableModel, Double > proteinFcColumn;
    @FXML
    private TableColumn<FoldChangeTableModel, Double > proteinPvalColumn;

    @FXML
    private Label numberOfGenesInTableLabel;

    @FXML
    private Button redrawVolcanoButton;

    @FXML
    private ImageView heatMapImageView;

    @FXML
    private VBox generalChartsVbox;
    @FXML
    private GridPane plotsGrid;
    @FXML
    private HBox selectedGeneCharts;
    @FXML
    private CheckBox peptideEvidenceCheckbox;

    @FXML
    private Button filterButton;

    @FXML
    private GridPane mainGrid;
    @FXML
    private AnchorPane mainAnchor;
    @FXML
    private KeggController keggController;
    @FXML
    GoTermsController goTermsController;
    @FXML
    private GSEAController gseaController;
    @FXML
    private BlastController blastController;


    // fold change list to Table
    private LinkedList<FoldChangeTableModel> foldChangesLinkedList;
    private boolean firstLoad = true;

    private WebViewController dgeWebview;
    private WebViewController proteinDeWebview;
    private WebViewController proteinRnaFcScatter;

    private int fontSize;




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {



        // fold Change Table: reflection for the getters
        geneSymbolFoldChangeTableColumn.setCellValueFactory( new PropertyValueFactory<>("geneSymbol"));
        geneTypeFoldChangeTableColumn.setCellValueFactory( new PropertyValueFactory<>("type"));
        logFoldFoldChangeTableColumn.setCellValueFactory( new PropertyValueFactory<>("logFoldChange"));
        pValFoldChangeTableColumn.setCellValueFactory( new PropertyValueFactory<>("pVal"));
        hasPeptideColumn.setCellValueFactory( new PropertyValueFactory<>("hasPeptideEvidence"));
//        proteinFcColumn.setCellValueFactory( new PropertyValueFactory<>("proteinFc"));
//        proteinPvalColumn.setCellValueFactory( new PropertyValueFactory<>("proteinPval"));


        proteinFcColumn.setCellValueFactory(p -> {
            if (p.getValue() != null && protComparisonCombobox.getValue()!=null) {
                return new SimpleObjectProperty<Double>(p.getValue().getProteinFc(protComparisonCombobox.getValue().toString()));
            }
            return null;
        });
        proteinPvalColumn.setCellValueFactory(p -> {
            if (p.getValue() != null && protComparisonCombobox.getValue()!=null) {
                return new SimpleObjectProperty<Double>(p.getValue().getProteinPval(protComparisonCombobox.getValue().toString()));
            }
            return null;
        });

        Comparator<Double> proteinFcComparator = (o1, o2) -> {
            final boolean isDesc = proteinFcColumn.getSortType() == TableColumn.SortType.DESCENDING;
            if (o1 == null && o2 == null) return 0;
            else if (o1 == null && o2 != null) return isDesc ? -1 : 1;
            else if (o1 != null && o2 == null) return isDesc ? 1 : -1;
            else return Double.compare(o1, o2);
        };

        Comparator<Double> proteinPvalComparator = (o1, o2) -> {
            final boolean isDesc = proteinPvalColumn.getSortType() == TableColumn.SortType.DESCENDING;
            if (o1 == null && o2 == null) return 0;
            else if (o1 == null && o2 != null) return isDesc ? -1 : 1;
            else if (o1 != null && o2 == null) return isDesc ? 1 : -1;
            else return Double.compare(o1, o2);
        };

        proteinFcColumn.setComparator(proteinFcComparator);
        proteinPvalColumn.setComparator(proteinPvalComparator);

        geneSymbolFoldChangeTableColumn.prefWidthProperty().bind(foldChangeTableView.widthProperty().divide(7));
        geneTypeFoldChangeTableColumn.prefWidthProperty().bind(foldChangeTableView.widthProperty().divide(7));
        logFoldFoldChangeTableColumn.prefWidthProperty().bind(foldChangeTableView.widthProperty().divide(7));
        pValFoldChangeTableColumn.prefWidthProperty().bind(foldChangeTableView.widthProperty().divide(7));
        hasPeptideColumn.prefWidthProperty().bind(foldChangeTableView.widthProperty().divide(7));
        proteinFcColumn.prefWidthProperty().bind(foldChangeTableView.widthProperty().divide(7));
        proteinPvalColumn.prefWidthProperty().bind(foldChangeTableView.widthProperty().divide(7));




        // p-value spinner

        StringConverter<Double> doubleConverter = new StringConverter<>() {
            final DecimalFormat df = new DecimalFormat("#.#####");

            @Override
            public String toString(Double object) {
                if (object == null) {
                    return "";
                }
                return df.format(object);
            }

            @Override
            public Double fromString(String string) {
                try {
                    if (string == null) {
                        return null;
                    }
                    string = string.trim();
                    if (string.length() < 1) {
                        return null;
                    }
                    return df.parse(string).doubleValue();
                } catch (ParseException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };

        SpinnerValueFactory<Double> pValValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1, 1, 0.0001);
        pValValueFactory.setConverter(doubleConverter);
        adjPValFilterFoldChangeSpinner.setValueFactory(pValValueFactory);
        adjPValFilterFoldChangeSpinner.setEditable(true);

        SpinnerValueFactory<Double> folChangeValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 7, 0, 0.01);
        foldFilterFoldChangeSpinner.setValueFactory(folChangeValueFactory);
        foldFilterFoldChangeSpinner.setEditable(true);


        foldChangeTableView.setRowFactory(tv -> {
            TableRow<FoldChangeTableModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!(row.isEmpty())) {
                    if (event.getButton().equals(MouseButton.PRIMARY)){

                        if ( event.getClickCount() == 1 ) {
                            FoldChangeTableModel rowData = foldChangeTableView.getSelectionModel().getSelectedItem();


                            selectedGeneCharts.getChildren().clear();
                            drawSelectedGeneReadCount(rowData.getGeneSymbol());
                            drawSelectedGeneProteinQuant(rowData.getGeneSymbol());

                            if (goTermsController.isGoLoaded()) {
                                goTermsController.setGoTermsGeneTable(rowData.getGeneSymbol());
                            }

                            keggController.setKeggGeneTable(rowData.getGeneSymbol());
                            blastController.selectGene(rowData.getGeneSymbol());



                        } else if ( event.getClickCount() == 2 ) {
                            parentController.showGeneBrowser(row.getItem().getGeneSymbol());
                        }
                    }
                }
            });
            return row;
        });
//        foldChangeTableView.setRowFactory( tv -> {
//            TableRow<FoldChangeTableModel> row = new TableRow<>();
//            row.setOnMouseClicked(event -> {
//                if (event.getClickCount() == 1 && (! row.isEmpty()) ) {
//                    FoldChangeTableModel rowData = row.getItem();
//                    selectedGeneCharts.getChildren().clear();
//                    drawSelectedGeneReadCount(rowData.getGeneSymbol());
//                    drawSelectedGeneProteinQuant(rowData.getGeneSymbol());
//                }
//            });
//            return row ;
//        });


    }


    /*
     * Used to set the parent, from the FXML Document Controller,
     * So that when data is loaded, it can handle the first view of the tab
     */

    public void setParentController(ResultsController parent, JSONObject settings, String databaseName, AllGenesReader allGenesReader){

        super.setParentControler(parent, settings, databaseName);


        allGenesReader.getGenesLoadedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (allGenesReader.getGenesLoadedProperty().get()) {
                Platform.runLater(() ->{
                    keggController.setParentController(this, allGenesReader);
                    goTermsController.setParentController(this, allGenesReader, Database.getDb());
                    gseaController.setParentController(this);
                });
            }
        });

        final DgeTableController _this = this;

        new Thread(() -> {
            fontSize = settings.getJSONObject("Fonts").getJSONObject("charts").getInt("size");


            // set comparison combobox options
            Set<String> collectionNames = Database.getDb().listCollectionNames();

            for ( String collNameString : collectionNames){
                if (collNameString.contains("_dge")){
                    String dgeComparison = collNameString.replace("_dge", "");
                    dgeComparisonCombobox.getItems().add(dgeComparison);
                }
            }




            Platform.runLater(() -> {
//                dgeWebview = new WebViewController("plot", new String[]{"plotly", "volcanoPlot"}, plotsGrid.getWidth(), plotsGrid.getHeight()/3,
//                        "Gene expression", fontSize, _this);
//                proteinDeWebview = new WebViewController("plot", new String[]{"plotly", "volcanoPlot"}, plotsGrid.getWidth(),
//                        plotsGrid.getHeight()/3, "Protein abundance", fontSize, _this);
//                proteinRnaFcScatter = new WebViewController("plot", new String[]{"plotly", "scatterPlot"}, plotsGrid.getWidth(),
//                        mainGrid.getHeight()/3, "RNA-Protein fold change correlation", fontSize, _this);
//
//
//                plotsGrid.add(dgeWebview.getWebView(), 0, 0);
//                plotsGrid.add(proteinDeWebview.getWebView(), 0, 1);
//                plotsGrid.add(proteinRnaFcScatter.getWebView(), 0, 2);

                setTableContentAndListeners();
            });

        }).start();




    }



    private void setTableContentAndListeners(){

        dgeComparisonCombobox.getSelectionModel().select(0);
        filterFoldChangeTable(); // set when first open
        firstLoad=false;

        filterButton.setOnAction(actionEvent -> {
            filterFoldChangeTable();
        });

        dgeComparisonCombobox.valueProperty().addListener((observableValue, o, t1) -> {
            filterFoldChangeTable();
        });

//        // set Filtering options when filtering options are modified
//        geneFilterFoldChangeTextField.textProperty().addListener((observableValue, s, t1) -> {
//            filterFoldChangeTable(); // generic filtering function
//        });
//
//        adjPValFilterFoldChangeSpinner.valueProperty().addListener((observableValue, o, t1) -> {
//            filterFoldChangeTable();// generic filtering function
//            // display Volcano plot
////            volcanoPlotThread(foldChangesArray, pValFilterFoldChangeSpinner.getValue(), foldFilterFoldChangeSpinner.getValue() );
//
//        });
//
//        foldFilterFoldChangeSpinner.valueProperty().addListener((observableValue, o, t1) -> {
//            filterFoldChangeTable();// generic filtering function
//        });
//
//        peptideEvidenceCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> filterFoldChangeTable());





        redrawVolcanoButton.setOnAction(actionEvent -> {
            volcanoPlotThread(foldChangesLinkedList, adjPValFilterFoldChangeSpinner.getValue(), foldFilterFoldChangeSpinner.getValue(), "dge" );
        });



    }


    public void filterFoldChangeTableThread(){
        new Thread(() -> {
            filterFoldChangeTable();
        }).start();
    }
    public void filterFoldChangeTable(){
        foldChangesLinkedList = new LinkedList<>();

        ArrayList<String> genesWithGoFilterList = (!goTermsController.isGoLoaded())?null:goTermsController.genesWithGoTermsForFilter();


        String dgeCollectionName = dgeComparisonCombobox.getValue().toString() + "_dge";
        boolean filterByProt = protFilterCheckbox.isSelected();
        boolean hasPeptideEvidenceFilter = peptideEvidenceCheckbox.isSelected();
        Double pvalThreshold = adjPValFilterFoldChangeSpinner.getValue();
        Double foldThreshold = foldFilterFoldChangeSpinner.getValue();
        String geneSymbolFilter = geneFilterFoldChangeTextField.getText().strip().toUpperCase();

        List<Filter> filters = new ArrayList<>(4);
        if(geneSymbolFilter.length()>0){

            if(!Config.isReferenceGuided()){
                filters.add(or(regex("symbol", "^.*" + geneSymbolFilter + ".*$"),
                        regex("names", "^.*" + geneSymbolFilter + ".*$")));
            }else{
                filters.add(regex("symbol", "^.*" + geneSymbolFilter + ".*$"));
            }
        }
//        filters.add(eq("type", "lncRNA"));

        filters.add(and(lte("padj", pvalThreshold), not(
                and(gt("log2fc", -foldThreshold), lt("log2fc", foldThreshold))
        )));

        if(hasPeptideEvidenceFilter){
            filters.add( eq("hasPeptideEvidence", true));
        }
        if (genesWithGoFilterList != null && genesWithGoFilterList.size() > 0) {
            if (genesWithGoFilterList.size() == 1) {
                filters.add(eq("symbol", genesWithGoFilterList.get(0)));
            } else  {
                filters.add(in("symbol", genesWithGoFilterList.toArray()));
            }

        }

        Cursor dgeFindCursor = Database.getDb().getCollection(dgeCollectionName).find(and(filters.toArray(new Filter[]{})));
        boolean updateTableBool = true;

        if (dgeFindCursor.size() == 0) {
            updateTableBool = false;
        } else {

            HashSet<String> msRuns = new HashSet<>();



            for (Document dgeDoc : dgeFindCursor) {

                String dgeGeneSymbol = (String) dgeDoc.get("symbol");
                String type = null;
                if(dgeDoc.get("type")!=null){
                    type = (String) dgeDoc.get("type");
                }


                if (keggController.isKeggLoaded()){
                    if (!keggController.isInKegg(dgeGeneSymbol)) {
                        continue;
                    }
                }


                double dgeFoldChange = (double) dgeDoc.get("log2fc");
                double dgePVal = (double) dgeDoc.get("padj");
                //boolean hasPeptideEvidence = (boolean) dgeDoc.get("hasPeptideEvidence");
                boolean hasPeptideEvidence = false;

                FoldChangeTableModel currFoldChange = new FoldChangeTableModel(dgeGeneSymbol, type, dgeFoldChange, dgePVal);

                boolean addToTable=true;

                if(dgeDoc.containsKey("ms")){

                    org.json.simple.JSONObject runsObj = (org.json.simple.JSONObject) dgeDoc.get("ms");
                    for(Object runName: runsObj.keySet()){

                        msRuns.add((String) runName);
                        org.json.simple.JSONObject run = (org.json.simple.JSONObject) runsObj.get(runName);

                        Double padj=null;
                        if(run.containsKey("padj")){
                            padj = (Double) run.get("padj");
                        }

                        if(filterByProt && (padj==null || padj>pvalThreshold)){
                            addToTable=false;
                            break;
                        }

                        try{
                            currFoldChange.addMsRun((String) runName, (Double) run.get("log2fc"), padj);
                        }catch (ClassCastException e){
                            e.printStackTrace();
                        }

                    }


                }else if(filterByProt){
                    addToTable=false;
                }
                if(addToTable)
                    foldChangesLinkedList.add(currFoldChange);

            }

            protComparisonCombobox.getItems().clear();
            protComparisonCombobox.getItems().addAll(msRuns);


        }


        boolean finalUpdateTableBool = updateTableBool;
        Platform.runLater(() -> {
                if(finalUpdateTableBool) {
                    protComparisonCombobox.getSelectionModel().select(0);
                    foldChangeTableView.getItems().clear(); // clear the table
                    foldChangeTableView.getItems().addAll(foldChangesLinkedList);
                    numberOfGenesInTableLabel.setText(foldChangeTableView.getItems().size() + " ");
                    if(selectedGeneCharts.getChildren().size()==1){
                        selectedGeneCharts.getChildren().clear();
                    }

                    if(dgeWebview==null){
                        dgeWebview = new WebViewController("plot", new String[]{"plotly", "volcanoPlot"}, plotsGrid.getWidth(), plotsGrid.getHeight()/3,
                                "Gene expression", fontSize, this);
                        proteinDeWebview = new WebViewController("plot", new String[]{"plotly", "volcanoPlot"}, plotsGrid.getWidth(),
                                plotsGrid.getHeight()/3, "Protein abundance", fontSize, this);
                        proteinRnaFcScatter = new WebViewController("plot", new String[]{"plotly", "scatterPlot"}, plotsGrid.getWidth(),
                                mainGrid.getHeight()/3, "RNA-Protein fold change correlation", fontSize, this);


                        plotsGrid.add(dgeWebview.getWebView(), 0, 0);
                        plotsGrid.add(proteinDeWebview.getWebView(), 0, 1);
                        plotsGrid.add(proteinRnaFcScatter.getWebView(), 0, 2);
                    }

                    volcanoPlotThread(foldChangesLinkedList, adjPValFilterFoldChangeSpinner.getValue(), foldFilterFoldChangeSpinner.getValue(), "dge");
                    if(protComparisonCombobox.getValue()!=null){
                        proteinDeWebview.getWebView().setVisible(true);
                        proteinRnaFcScatter.getWebView().setVisible(true);
                        volcanoPlotThread(foldChangesLinkedList, adjPValFilterFoldChangeSpinner.getValue(), foldFilterFoldChangeSpinner.getValue(), "proteinDe");
                        proteinRnaFcPlotThread(foldChangesLinkedList);
                    }else{
                        proteinDeWebview.getWebView().setVisible(false);
                        proteinRnaFcScatter.getWebView().setVisible(false);
                    }

                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Info");
                    alert.setHeaderText(null);
                    alert.setContentText("No gene found with selected search criteria. Table won't update.");

                    alert.showAndWait();
                }
        });


    }


    /**
     * sends a thread to draw the volcano plot. A thread is used cause it may take some time to display
     * @param foldChangesArray list of fold changes, this values come from the table
     * @param pvalThreshold  pval threshold
     * @param foldThreshold  fold change threshold
     */
    private void volcanoPlotThread(LinkedList<FoldChangeTableModel> foldChangesArray, double pvalThreshold, double foldThreshold, String type){
        if (foldChangesArray.size() > 0){
            String volcanoFunction = getFunctionForVolcanoPlotSetGenesMap(foldChangesArray, pvalThreshold, foldThreshold, type);
            new Thread(() -> drawPlot(volcanoFunction, type.equals("dge")?dgeWebview:proteinDeWebview)) {{start();}};
        }
    }
    private void proteinRnaFcPlotThread(LinkedList<FoldChangeTableModel> foldChangesArray){
        if (foldChangesArray.size() > 0){
            String function = getFunctionProteinRnaFcScatter(foldChangesArray);
            new Thread(() -> drawPlot(function, proteinRnaFcScatter)) {{start();}};
        }
    }


    /**
     * draws the volcano plot and defines the geneSymbolsThresholds.
     * @param foldChangesArray list of fold changes, this values come from the table
     * @param pvalThreshold   pval threshold
     * @param foldThreshold   fold change threshold
     */
    private String getFunctionForVolcanoPlotSetGenesMap(LinkedList<FoldChangeTableModel> foldChangesArray, double pvalThreshold, double foldThreshold, String type) {

        // lists for data

        ArrayList<String> foldChangesPositiveFoldSymbol = new ArrayList<>();
        ArrayList<Double> foldChangesPositiveFoldLog10PVal = new ArrayList<>();
        ArrayList<Double> foldChangesPositiveFoldLog2FoldChange = new ArrayList<>();


        ArrayList<String> foldChangesNegativeFoldSymbol = new ArrayList<>();
        ArrayList<Double> foldChangesNegativeFoldLog10PVal = new ArrayList<>();
        ArrayList<Double> foldChangesNegativeFoldLog2FoldChange = new ArrayList<>();


//        ArrayList<String> foldChangesBelowThresholdFoldSymbol = new ArrayList<>();
//        ArrayList<Double> foldChangesBelowThresholdFoldLog10PVal = new ArrayList<>();
//        ArrayList<Double> foldChangesBelowThresholdFoldLog2FoldChange = new ArrayList<>();



        // set lists

        Double foldChange, pvalue;

        for (FoldChangeTableModel fcModel : foldChangesArray) {


            if(type.equals("dge")){
                foldChange = fcModel.getLogFoldChange();
                pvalue = fcModel.getpVal();
            }else{
                foldChange = fcModel.getProteinFc(protComparisonCombobox.getValue().toString());
                pvalue = fcModel.getProteinPval(protComparisonCombobox.getValue().toString());
            }

            if (pvalue==null || -Math.log10(pvalue) == Double.POSITIVE_INFINITY) {


                //to remove infinity from plot
                continue;
            }


            // if NaN, add it to the list and pass to the next cycle
            if ( Double.isNaN(pvalue) || Double.isNaN(foldChange)){
//                belowFoldThresholdSymbol.add(foldChange.getGeneSymbol()); // for the threshold map
                continue;
            }

            //  > p-val (careful, it is not log10 pval)     ||   -foldthreshold < foldchange < foldthreshold
            if ((type.equals("dge") && (pvalue> pvalThreshold || (foldChange > -foldThreshold && foldChange < foldThreshold)) ||
                    (type.equals("proteinDe") && protFilterCheckbox.isSelected() && pvalue>pvalThreshold))) {
//                belowFoldThresholdSymbol.add(foldChange.getGeneSymbol()); // for the threshold map
//                foldChangesBelowThresholdFoldSymbol.add("'" + foldChange.getGeneSymbol() + "'");
//                foldChangesBelowThresholdFoldLog10PVal.add(-Math.log10(foldChange.getPVal()));
//                foldChangesBelowThresholdFoldLog2FoldChange.add(foldChange.getLogFoldChange());
            } else {
                if (foldChange < 0) {
                    foldChangesNegativeFoldSymbol.add("'" + fcModel.getGeneSymbol() + "'");
                    foldChangesNegativeFoldLog10PVal.add(-Math.log10(pvalue));
                    foldChangesNegativeFoldLog2FoldChange.add(foldChange);
                } else {

                    foldChangesPositiveFoldSymbol.add("'" + fcModel.getGeneSymbol() + "'");
                    foldChangesPositiveFoldLog10PVal.add(-Math.log10(pvalue));
                    foldChangesPositiveFoldLog2FoldChange.add(foldChange);

                }
            }
        }




        // generate json Strings to send to the  js function that generates the volcano plot in plotly
        StringBuilder foldChangesBelowThresholdJsonString = new StringBuilder();
        foldChangesBelowThresholdJsonString.append("{'logFoldChange': []");
//        foldChangesBelowThresholdJsonString.append(foldChangesBelowThresholdFoldLog2FoldChange.toString());
        foldChangesBelowThresholdJsonString.append(", 'logPval': []");
//        foldChangesBelowThresholdJsonString.append(foldChangesBelowThresholdFoldLog10PVal.toString());
        foldChangesBelowThresholdJsonString.append(", 'texts': []");
//        foldChangesBelowThresholdJsonString.append(foldChangesBelowThresholdFoldSymbol.toString());
        foldChangesBelowThresholdJsonString.append("}");


        StringBuilder foldChangesPositiveJsonString = new StringBuilder();
        foldChangesPositiveJsonString.append("{'logFoldChange': ");
        foldChangesPositiveJsonString.append(foldChangesPositiveFoldLog2FoldChange.toString());
        foldChangesPositiveJsonString.append(", 'logPval':");
        foldChangesPositiveJsonString.append(foldChangesPositiveFoldLog10PVal.toString());
        foldChangesPositiveJsonString.append(", 'texts':");
        foldChangesPositiveJsonString.append(foldChangesPositiveFoldSymbol.toString());
        foldChangesPositiveJsonString.append("}");


        StringBuilder foldChangesNegativeJsonString = new StringBuilder();
        foldChangesNegativeJsonString.append("{'logFoldChange': ");
        foldChangesNegativeJsonString.append(foldChangesNegativeFoldLog2FoldChange.toString());
        foldChangesNegativeJsonString.append(", 'logPval':");
        foldChangesNegativeJsonString.append(foldChangesNegativeFoldLog10PVal.toString());
        foldChangesNegativeJsonString.append(", 'texts':");
        foldChangesNegativeJsonString.append(foldChangesNegativeFoldSymbol.toString());
        foldChangesNegativeJsonString.append("}");

        // threshold values
        String pValLog10Threshold = Double.toString(-Math.log10(pvalThreshold));
        String foldChangeThreshold = Double.toString(foldThreshold);
        boolean seen = false;

        ArrayList<Double> maxPVals = new ArrayList<>();

        if(foldChangesNegativeFoldLog10PVal.size()>0){
            maxPVals.add(Collections.max(foldChangesNegativeFoldLog10PVal));
        }else{
            maxPVals.add(1.);
        }
        if(foldChangesPositiveFoldLog10PVal.size()>0){
            maxPVals.add(Collections.max(foldChangesPositiveFoldLog10PVal));
        }else{
            maxPVals.add(1.);
        }

//        maxPVals.add(Collections.max(foldChangesBelowThresholdFoldLog10PVal));

        String maxPval = Double.toString(Collections.max(maxPVals) + 0.5);


        // javascript function that would be executed in the webview
        return  "displayVolcano(" + foldChangesBelowThresholdJsonString + "," + foldChangesNegativeJsonString + "," + foldChangesPositiveJsonString + "," + pValLog10Threshold + "," + foldChangeThreshold + "," + maxPval + ")";
    }

    private String getFunctionProteinRnaFcScatter(LinkedList<FoldChangeTableModel> foldChangesArray){


        LinkedList<Double> x = new LinkedList<>();
        LinkedList<Double> y = new LinkedList<>();
        LinkedList<String> symbols = new LinkedList<>();

        for(FoldChangeTableModel fcModel: foldChangesArray){
            x.add(fcModel.getLogFoldChange());
            y.add(fcModel.getProteinFc(protComparisonCombobox.getValue().toString()));
            symbols.add(fcModel.getGeneSymbol());
        }

        StringBuilder names = new StringBuilder("[");

        if (symbols.size() == 0){
            names.append("]");
        } else {
            for(String name: symbols){
                names.append("\"").append(name).append("\",");
            }
            names.replace(names.length()-1, names.length(), "]");
        }


        return "displayPlot("+Arrays.toString(x.toArray())+","+Arrays.toString(y.toArray())+","+names+")";

    }

    private void drawPlot (String sendFunction, WebViewController webViewController){


        Platform.runLater(() -> webViewController.execute(sendFunction));

    }



    private void setHeatMapImageView(String dgeComparison) {
        String dgeComparisonString = dgeComparison.split("_")[0];



        NitriteCollection pngHeatMapPathsCollection = Database.getDb().getCollection("pngHeatMapPaths");

        Document queryHeatMapDoc = new Document();
        queryHeatMapDoc.put("comparison", dgeComparisonString);



        Document pngPathDoc = pngHeatMapPathsCollection.find(Filters.eq("comparison", dgeComparisonString)).firstOrDefault();

        if (pngPathDoc.get("pngHeatMapPath") != null) {
            String pngPathString = (String) pngPathDoc.get("pngHeatMapPath");
            Image heatMap = new Image("file:" + pngPathString);
            heatMapImageView.setImage(heatMap);
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
        bc.setLegendVisible(false);
//        bc.setStyle("-fx-font-size: " + fontSize + "px;");

        ArrayList<XYChart.Series> allSeries = new ArrayList<>();

        ConfidentBarChart bc2 = new ConfidentBarChart();
        bc2.setTitle("Normalised read counts");

        for(Document result: documents){
            JSONObject res = new JSONObject(result).getJSONObject("counts");


            for (String conditionKey : res.keySet()) {

                ArrayList<Double> samplesCounts = new ArrayList<>();


                JSONObject condition = res.getJSONObject(conditionKey);
                int i = 0;
                for (String sampleKey : condition.keySet()) {
                    if (i + 1 > allSeries.size()) {
                        allSeries.add(new XYChart.Series());
                    }

                    samplesCounts.add(condition.getDouble(sampleKey));
                    allSeries.get(i).getData().add(new XYChart.Data(conditionKey, condition.getInt(sampleKey)));
                    i++;
                }
                bc2.addSeries(conditionKey, samplesCounts);



            }
        }
        for(XYChart.Series series: allSeries){
            bc.getData().add(series);
        }
        bc2.draw();
        HBox.setHgrow(bc2, Priority.ALWAYS);
        selectedGeneCharts.getChildren().add(bc2);






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
        yAxisLineChart.setLabel("Peptide intensity");
        LineChart<String,Number> lineChart =
                new LineChart<>(xAxisLineChart,yAxisLineChart);
        lineChart.setTitle("Differential peptide intensity");
        



        ArrayList<XYChart.Series> allSeriesAbundance = new ArrayList<>();
        ArrayList<XYChart.Series> allPeptidesSeries = new ArrayList<>();

//        XYChart.Series s = new XYChart.Series();
//        allSeriesAbundance.add(new XYChart.Data(conditionKey+" "+sampleKey, condition.getInt(sampleKey));


        boolean isSILAC = true;

        ConfidentBarChart proteinConfidentBarChart = new ConfidentBarChart();
        proteinConfidentBarChart.setMeanOrMedian("median");
        proteinConfidentBarChart.setTitle("Differencial protein abundance");
        proteinConfidentBarChart.setMin(0);
        proteinConfidentBarChart.setReference("Nsi");

        for(Document result: documents){


            if(isSILAC){

                JSONObject res = new JSONObject(result).getJSONObject("peptides");
                String reference = "Nsi";

                HashMap<String, HashMap<String, ArrayList<Double>>> groups = new HashMap<>();
                for(String peptide: res.keySet()){
                    JSONObject peptideObj = res.getJSONObject(peptide);
                    for(String subRun: peptideObj.keySet()){

                        if(!groups.containsKey(subRun)){
                            groups.put(subRun, new HashMap<>());
                        }

                        JSONObject subRunObj = peptideObj.getJSONObject(subRun);

                        Set<String> conditions = subRunObj.keySet();
                        for(String condition: conditions){
                            if(!groups.get(subRun).containsKey(condition)){
                                groups.get(subRun).put(condition, new ArrayList<>());
                            }

                            if(condition.equals("Nsi")){
                                groups.get(subRun).get(condition).add(1.);
                            }else{
                                double ratio = subRunObj.getDouble(condition)/subRunObj.getDouble("Nsi");
                                if(ratio!=Double.POSITIVE_INFINITY){
                                    groups.get(subRun).get(condition)
                                            .add(ratio);
                                }

                            }
                        }
                    }


                }


                proteinConfidentBarChart.addGroups(groups);



                for (String Peptide : res.keySet()) {

                    XYChart.Series peptideSeries = new XYChart.Series();
                    peptideSeries.setName(Peptide);
                    allPeptidesSeries.add(peptideSeries);

                    JSONObject peptideObj = res.getJSONObject(Peptide);

                    for (String runKey : peptideObj.keySet()) {
                        JSONObject runObject = peptideObj.getJSONObject(runKey);
                        //peptideSeries.getData().add(new XYChart.Data(conditionKey+" "+sampleKey, Math.log10(condition.getInt(sampleKey))/Math.log10(2)));
                        for (String conditionKey : runObject.keySet()) {
                            double ratio = runObject.getDouble(conditionKey);
                            //peptideSeries.getData().add(new XYChart.Data(conditionKey+" "+sampleKey, Math.log10(condition.getInt(sampleKey))/Math.log10(2)));
                            peptideSeries.getData().add(new XYChart.Data(conditionKey+" "+runKey, ratio));

                        }

                    }
                }
            }else {


                JSONObject res = new JSONObject(result).getJSONObject("abundance");
                int i = 0;
                for (String conditionKey : res.keySet()) {
                    double intensity = res.getDouble(conditionKey);

                    if (i + 1 > allSeriesAbundance.size()) {
                        allSeriesAbundance.add(new XYChart.Series());
                    }
                    allSeriesAbundance.get(i).getData().add(new XYChart.Data(conditionKey, intensity));

                }


                res = new JSONObject(result).getJSONObject("peptides");
                for (String PSM : res.keySet()) {

                    XYChart.Series peptideSeries = new XYChart.Series();
                    peptideSeries.setName(PSM);
                    allPeptidesSeries.add(peptideSeries);

                    JSONObject peptideObj = res.getJSONObject(PSM);

                    for (String conditionKey : peptideObj.keySet()) {
                        double intensity = peptideObj.getDouble(conditionKey);
                        //peptideSeries.getData().add(new XYChart.Data(conditionKey+" "+sampleKey, Math.log10(condition.getInt(sampleKey))/Math.log10(2)));
                        peptideSeries.getData().add(new XYChart.Data(conditionKey, intensity));

                    }
                }
            }

        }
        for(XYChart.Series series: allSeriesAbundance){
            barChart.getData().add(series);
        }
        HBox.setHgrow(proteinConfidentBarChart, Priority.ALWAYS);

        selectedGeneCharts.getChildren().add(proteinConfidentBarChart);
        proteinConfidentBarChart.draw();

        for(XYChart.Series series: allPeptidesSeries){
            lineChart.getData().add(series);
        }
        HBox.setHgrow(lineChart, Priority.ALWAYS);
        selectedGeneCharts.getChildren().add(lineChart);


    }

    public void updateSettings(org.json.JSONObject settings){
        fontSize = settings.getJSONObject("Fonts").getJSONObject("charts").getInt("size");

        if(foldChangeTableView.getSelectionModel().getSelectedItem()!=null){
            String gene = foldChangeTableView.getSelectionModel().getSelectedItem().getGeneSymbol();

            selectedGeneCharts.getChildren().clear();
            drawSelectedGeneReadCount(gene);
            drawSelectedGeneProteinQuant(gene);
        }


    }

    public void resize(){
//        dgeWebview.resize();
//        proteinDeWebview.resize();
//        proteinRnaFcScatter.resize();

    }

    public ObservableList<FoldChangeTableModel> getTableData(){
        return foldChangeTableView.getItems();
    }

    @Override
    public String getSelectedComparison(){
        return dgeComparisonCombobox.getValue().toString();
    }

}
