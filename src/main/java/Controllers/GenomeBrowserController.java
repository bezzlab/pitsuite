package Controllers;

import Gene.Gene;
import TablesModels.FoldChangeTableModel;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.filters.Filters;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GenomeBrowserController implements Initializable {


    // chromosome
    @FXML
    private JFXComboBox<String> chrCombobox;
    // comparison
    @FXML
    private JFXComboBox<String > comparisonCombobox;



    @FXML
    private HBox genomeHBoxSlider;
    @FXML
    private HBox genomeViewerHBox;
    @FXML
    private VBox genomeViewerVBox;

    @FXML
    private Button genomeBrowserMoveLeftButton;
    @FXML
    private Button genomeBrowserMoveRightButton;

    @FXML
    private JFXTextField minGenomCoordGenomBrowserTextField;
    @FXML
    private JFXTextField maxGenomCoordGenomBrowserTextField;

    @FXML
    private Slider genomeZoomBrowserSlider;
    @FXML
    private Label numbOfGenesGenomBrowser;

    @FXML
    private Label numberOfGenesInTableLabel;


    // table
    @FXML
    private JFXTextField geneFilterFoldChangeTextField;
    @FXML
    private Spinner<Double> adjPValFilterFoldChangeSpinner;
    @FXML
    private Spinner<Double> foldFilterFoldChangeSpinner;
    @FXML
    private TableView<FoldChangeTableModel> foldChangeTableView;
    @FXML
    private TableColumn<FoldChangeTableModel, String > geneSymbolFoldChangeTableColumn;
    @FXML
    private TableColumn<FoldChangeTableModel, Double > logFoldFoldChangeTableColumn;
    @FXML
    private TableColumn<FoldChangeTableModel, Double > pValFoldChangeTableColumn;
    @FXML
    private TableColumn<FoldChangeTableModel, Double > adjPValFoldChangeTableColumn;

    @FXML
    private WebView volcanoPlotWebView;

    @FXML
    private TitledPane foldChangeTitledPane;
    @FXML
    private ScrollPane genomeBrowserScrollPane;
    @FXML
    GridPane browserGrid;

    private boolean isNetworkWebviewLoaded = false;

    private ResultsController parentController;
    private HashMap<String, ArrayList<String>> geneSymbolsThresholdsMap;
    private Nitrite db;




    ///TODO: remove this comment: key = "chr1"  , value = "123123, 1923818"  <- start, end of coord
    private HashMap<String, Pair<Integer, Integer>> chrMinMaxCoordsMap;
    private HashMap<String, Gene> genesMap;

    // fold change list to Table
    private LinkedList<FoldChangeTableModel> foldChangesLinkedList;



    // list of conditions
    private ArrayList<String> conditions;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // genome Browser slider, this is for the window size

        genomeZoomBrowserSlider.setMin(1);
        genomeZoomBrowserSlider.setMax(20);



        // fold Change Table: reflection for the getters
        geneSymbolFoldChangeTableColumn.setCellValueFactory( new PropertyValueFactory<>("geneSymbol"));
        logFoldFoldChangeTableColumn.setCellValueFactory( new PropertyValueFactory<>("logFoldChange"));
        pValFoldChangeTableColumn.setCellValueFactory( new PropertyValueFactory<>("pVal"));
        adjPValFoldChangeTableColumn.setCellValueFactory( new PropertyValueFactory<>("adjPVal"));

        // p-value spinner

        StringConverter<Double> doubleConverter = new StringConverter<Double>() {
            DecimalFormat df = new DecimalFormat("#.####");
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

        SpinnerValueFactory<Double> pValValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1, 0.05, 0.0001);
        pValValueFactory.setConverter(doubleConverter);
        adjPValFilterFoldChangeSpinner.setValueFactory(pValValueFactory);
        adjPValFilterFoldChangeSpinner.setEditable(true);

        SpinnerValueFactory<Double> folChangeValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 7, 1, 0.01);
        foldFilterFoldChangeSpinner.setValueFactory(folChangeValueFactory);
        foldFilterFoldChangeSpinner.setEditable(true);


        foldChangeTitledPane.expandedProperty().addListener((observableValue, aBoolean, t1) -> {
            ObservableList<RowConstraints> rowConstraints = browserGrid.getRowConstraints();
            if (foldChangeTitledPane.isExpanded()) {
                rowConstraints.get(0).setPercentHeight(70);
                rowConstraints.get(1).setPercentHeight(30);
            } else {
                rowConstraints.get(0).setPercentHeight(98);
                rowConstraints.get(1).setPercentHeight(2);
            }
        });

    }

    /*
     * Used to set the parent, from the FXML Document Controller,
     * So that when data is loaded, it can handle the first view of the tab
     */
    public void setParentControler(ResultsController parent, Nitrite db){
        parentController = parent;
        this.db = db;

        new Thread(() -> {
            chrMinMaxCoordsMap = new HashMap<>();


            Set<String> collectionNames = db.listCollectionNames();

            ArrayList<String> collectionsList = new ArrayList<>();
            for (String collection : collectionNames) {
                if (collection.contains("_dge")) {
                    String collNameString = collection.replace("_dge", "");
                    collectionsList.add(collNameString);
                }
            }





            NitriteCollection chrCollection = db.getCollection("chromMap");

            Cursor chrCursor = chrCollection.find();

            // Adding each chromosome to combobox
            ArrayList<Integer> chrIdsNumbers = new ArrayList<>();
            ArrayList<String> chrIdsStrings = new ArrayList<>();
            for(Document chrDoc : chrCursor) {


                String chrId = (String) chrDoc.get("chromosome");
                int chrMin = (int) chrDoc.get("min");
                int chrMax = (int) chrDoc.get("max");

                chrMinMaxCoordsMap.put(chrId, new Pair<>(chrMin, chrMax));

                String chr = chrId.replace("chr","");
                try {
                    int chrNumber = Integer.parseInt(chr);
                    chrIdsNumbers.add(chrNumber);
                } catch (Exception e){
                    chrIdsStrings.add(chr);
                }
            }


            // sort the items
            Collections.sort(chrIdsNumbers);
            Collections.sort(chrIdsStrings);

            ArrayList<String> chrIdsFinal = new ArrayList<>();
            for (int chrId: chrIdsNumbers){
                chrIdsFinal.add(Integer.toString(chrId));
            }

            chrIdsFinal.addAll(chrIdsStrings);

            Platform.runLater(() -> {
                chrCombobox.getItems().addAll(chrIdsFinal);

                comparisonCombobox.getItems().addAll(collectionsList);
                comparisonCombobox.valueProperty().addListener((observable, oldValue, newValue) -> {
                    if (chrCombobox.getValue() != null ){
                        String chr = "chr" +  chrCombobox.getValue();
                        String dgeCollectionName = comparisonCombobox.getValue() + "_dge";
                        displayGenomeBrowser(chr, dgeCollectionName);
                    }

                });

                chrCombobox.valueProperty().addListener((observable, oldValue, newValue) -> {
                    if (comparisonCombobox.getValue() != null ){
                        String chr = "chr" +  chrCombobox.getValue();
                        String dgeCollectionName = comparisonCombobox.getValue() + "_dge";
                        displayGenomeBrowser(chr, dgeCollectionName);
                    }

                });
            });

        }).start();

    }


    /**
     * main function to display the genes in a chromosome
     * @param chromosome: chromosome name.
     */
    private void displayGenomeBrowser(String chromosome, String dgeCollectionName){
        // get information of that chromosome
        genesMap =  getGenesFromDb(chromosome); // set genemap
        foldChangesLinkedList = getFoldChangesList(genesMap, dgeCollectionName);


        // clear hbox containing slider
        genomeHBoxSlider.getChildren().clear();
        genomeHBoxSlider.setAlignment(Pos.CENTER);




        // define slider
        Slider genomeSlider = new Slider();
        genomeSlider.prefWidthProperty().bind(genomeHBoxSlider.widthProperty().multiply(0.8));
        int minChromCoord = chrMinMaxCoordsMap.get(chromosome).getKey() - 30; // -+50 to add some space at the beggining and end
        int maxChromCoord = chrMinMaxCoordsMap.get(chromosome).getValue() + 30;
        genomeSlider.setMin(minChromCoord);
        genomeSlider.setMax(maxChromCoord);

        genomeSlider.setShowTickLabels(true);
        genomeSlider.setShowTickMarks(true);

        System.out.println(maxChromCoord+" "+minChromCoord+ " " +(double) ( maxChromCoord - minChromCoord) / 5);
        genomeSlider.setMajorTickUnit( (double) ( maxChromCoord - minChromCoord) / 5);
        genomeSlider.setMinorTickCount(5);

        // add listeners
        genomeSlider.valueProperty().addListener((observableValue, number, t1) -> {
            genesHBoxToGenomeBrowser(chromosome, (int) genomeSlider.getValue(), (int) (genomeSlider.getValue() + (int) (genomeZoomBrowserSlider.getValue()  * 1000000) ));
            minGenomCoordGenomBrowserTextField.setText(Integer.toString( (int) genomeSlider.getValue()));
            maxGenomCoordGenomBrowserTextField.setText(Integer.toString((int) genomeSlider.getValue() + (int) (genomeZoomBrowserSlider.getValue()  * 1000000) ) );
        });


        genomeZoomBrowserSlider.valueProperty().addListener((observableValue, o, t1) -> {
            genesHBoxToGenomeBrowser(chromosome, (int) genomeSlider.getValue(), (int)(genomeSlider.getValue()  + (int) (genomeZoomBrowserSlider.getValue()  * 1000000) ));
            maxGenomCoordGenomBrowserTextField.setText(Integer.toString((int) genomeSlider.getValue() + (int) (genomeZoomBrowserSlider.getValue()  * 1000000) ) );
        });


        genomeBrowserMoveLeftButton.setOnAction(actionEvent -> {
            double value = genomeSlider.getValue() - (1000000 * (genomeZoomBrowserSlider.getValue()) / 3.0);
            genomeSlider.setValue(value);
        });

        genomeBrowserMoveRightButton.setOnAction(actionEvent -> {
            double value = genomeSlider.getValue() + (1000000 * (genomeZoomBrowserSlider.getValue()) / 3.0);
            genomeSlider.setValue(value);
        });

//        genomeBrowserSlider.valueProperty().addListener((observableValue, o, t1) -> {
//                    genesHBoxToGenomeBrowser(chromosome, (int) genomeSlider.getValue(), (int)(genomeSlider.getValue()  + (int) (genomeBrowserSlider.getValue()  * 1000000) ));


                    // add genome slider
        genomeHBoxSlider.getChildren().add(genomeSlider);



        foldChangeTableView.getItems().addAll(foldChangesLinkedList);
        numberOfGenesInTableLabel.setText(Integer.toString(foldChangesLinkedList.size()));

        // set Filtering options when filtering options are modified
        geneFilterFoldChangeTextField.textProperty().addListener((observableValue, s, t1) -> {
            filterFoldChangeTable(); // generic filtering function
        });

        adjPValFilterFoldChangeSpinner.valueProperty().addListener((observableValue, o, t1) -> {
            filterFoldChangeTable();
            // display Volcano plot
            volcanoPlotThread(foldChangesLinkedList, adjPValFilterFoldChangeSpinner.getValue(), foldFilterFoldChangeSpinner.getValue() );
            // redraw the genes
            genesHBoxToGenomeBrowser(chromosome, (int) genomeSlider.getValue(), (int) (genomeSlider.getValue() + (int) (genomeZoomBrowserSlider.getValue()  * 1000000) ));

        });

        foldFilterFoldChangeSpinner.valueProperty().addListener((observableValue, o, t1) -> {
            filterFoldChangeTable();
            // display Volcano plot
            volcanoPlotThread(foldChangesLinkedList, adjPValFilterFoldChangeSpinner.getValue(), foldFilterFoldChangeSpinner.getValue() );
            // redaw the genes
            genesHBoxToGenomeBrowser(chromosome, (int) genomeSlider.getValue(), (int) (genomeSlider.getValue() + (int) (genomeZoomBrowserSlider.getValue()  * 1000000) ));
        });

        // filter,  since at least p-val  and foldchange filtering should be there
        filterFoldChangeTable();

        getFunctionForVolcanoPlotSetGenesMap(foldChangesLinkedList, 0.05, 1);
        // display Volcano plot
        volcanoPlotThread(foldChangesLinkedList, adjPValFilterFoldChangeSpinner.getValue(), foldFilterFoldChangeSpinner.getValue() );

        // needs to be run here cause coloring depend of the thresholds
        genomeSlider.setValue(minChromCoord );

    }



    /**
     * Adds to genomeViewerVBox a hboxes of all genes within a start-end coordinate range.
     * Calls another function drawGeneRectangles() to actually add the HBox containing the gene.
     * @param chromosome name of the chromosome.
     * @param startCoord genomic location of start position of the window.
     * @param endCoord genomic location of end position of the window.
     */
    private void genesHBoxToGenomeBrowser(String chromosome, int startCoord, int endCoord){
        genomeViewerVBox.getChildren().clear();

        AtomicInteger countGenesInWindow = new AtomicInteger();


        ArrayList<Pair<String, Gene>> genesNameGenePairsArray = new ArrayList<>();

        for (Map.Entry<String, Gene> genesEntry : genesMap.entrySet()) {
            String geneID = genesEntry.getKey();
            Gene tmpGene = genesEntry.getValue();

            if (tmpGene.getEndCoordinate() >= startCoord && tmpGene.getStartCoordinate() <= endCoord) {
                countGenesInWindow.incrementAndGet();

                // add genes to list

                genesNameGenePairsArray.add(new Pair<> (geneID, tmpGene));

            }
        }

        // sort  genesNameGenePairsArray by start
        genesNameGenePairsArray.sort(Comparator.comparingInt(p -> p.getValue().getStartCoordinate()));

        //  pix == distance ?
        double zoomLevel = genomeZoomBrowserSlider.getValue() * 1000000;
        double vBoxwidth =  genomeViewerVBox.getWidth();
        int tenPixInGenom = (int) ((10 * zoomLevel) / vBoxwidth);



        int count = 0;
        Text tmpGeneNameText;
        int nextMinCoord = startCoord;

        while (genesNameGenePairsArray.size() > 0 ) {// if there are still elements


            // each cycle new iterator and list
            ArrayList<Pair<String, Gene>> tmpGenesNameGenePairsArray = new ArrayList<>();
            Iterator<Pair<String, Gene>> genesPairsIt = genesNameGenePairsArray.iterator();

            boolean firstElement = true;
            while (genesPairsIt.hasNext()) {
                Pair<String, Gene> pairNameGene = genesPairsIt.next();
                if (firstElement) {
                    tmpGenesNameGenePairsArray.add(pairNameGene);
                    genesPairsIt.remove(); // remove since it's already being added
                    count++;
                    firstElement = false;

                    if (pairNameGene.getValue().getEndCoordinate() > endCoord) {
                        drawGenes(tmpGenesNameGenePairsArray, startCoord, endCoord); //draw before break
                        break;
                    }

                    tmpGeneNameText = new Text( pairNameGene.getValue().getSymbol());
                    int minCoordInView = Math.max(startCoord, pairNameGene.getValue().getStartCoordinate());
                    int textPixWidthInGenom = (int) ((tmpGeneNameText.getLayoutBounds().getWidth() * zoomLevel) / vBoxwidth);
                    nextMinCoord = Math.max((minCoordInView + textPixWidthInGenom + tenPixInGenom), (pairNameGene.getValue().getEndCoordinate() + tenPixInGenom));
                } else { // if it's not the first element

                    if (pairNameGene.getValue().getStartCoordinate() >= (nextMinCoord)) { // min distance between genes
                        tmpGenesNameGenePairsArray.add(pairNameGene);
                        genesPairsIt.remove(); // remove since it's already being added
                        count++;


                        if (pairNameGene.getValue().getEndCoordinate() > endCoord) {
                            drawGenes(tmpGenesNameGenePairsArray, startCoord, endCoord); //draw before break
                            break;
                        }

                        tmpGeneNameText = new Text( pairNameGene.getValue().getSymbol());
                        int minCoordInView = Math.max(startCoord, pairNameGene.getValue().getStartCoordinate());
                        int textPixWidthInGenom = (int) ((tmpGeneNameText.getLayoutBounds().getWidth() * zoomLevel) / vBoxwidth);
                        nextMinCoord = Math.max((minCoordInView + textPixWidthInGenom + tenPixInGenom), (pairNameGene.getValue().getEndCoordinate() + tenPixInGenom));

                    }
                }
                if(!genesPairsIt.hasNext()){
                    drawGenes(tmpGenesNameGenePairsArray, startCoord, endCoord); //draw before next cycle
                }
            }
        }


        // displays the number of genes viewed inside the window
        String visibleVsTotal = countGenesInWindow + "/" + foldChangesLinkedList.size();
        numbOfGenesGenomBrowser.setText(visibleVsTotal);

    }


    private void drawGenes(ArrayList<Pair<String, Gene>> genesNamesGene, int startGenomCoord, int endGenomCoord){
        HBox geneHBox = new HBox();

        geneHBox.setPrefHeight(browserGrid.getHeight()*0.04);

        Pane genePane = new Pane();
        Group group = new Group();
        int vBoxwidth = (int) browserGrid.getWidth()-10;

        int height = (int) Math.round(browserGrid.getHeight()*0.03);
        int length = endGenomCoord - startGenomCoord;

        genePane.setPrefHeight(height);
        genePane.setMinWidth(vBoxwidth);


        for( Pair<String, Gene> pairNameGene: genesNamesGene){
            Gene gene = pairNameGene.getValue();
            String geneSymbol = gene.getSymbol();
            // rectangle that represents the gene. Position, and add to group.
            Rectangle geneRectangle = new Rectangle();
            geneRectangle.setHeight(height - 1);


            // on click action
            geneRectangle.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getClickCount() == 1 && mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                    geneFilterFoldChangeTextField.setText(geneSymbol);
                }
            });


            // change color depending on the p-value and foldChange thresholds
            if (geneSymbolsThresholdsMap.get("pos").contains(gene.getSymbol())){
                geneRectangle.setFill(Color.BLUE);
                geneRectangle.setStroke(Color.BLUE);
            } else if (geneSymbolsThresholdsMap.get("neg").contains(gene.getSymbol())){
                geneRectangle.setFill(Color.RED);
                geneRectangle.setStroke(Color.RED);
            } else if (geneSymbolsThresholdsMap.get("below").contains(gene.getSymbol())){
                geneRectangle.setFill(Color.GRAY);
                geneRectangle.setStroke(Color.GRAY);
            } else {
                geneRectangle.setFill(Color.BLACK);
                geneRectangle.setStroke(Color.GRAY);
            }

            int tmpStartGeneCoord = Math.max(gene.getStartCoordinate(), startGenomCoord);
            int tmpEndGeneCoord = Math.min(gene.getEndCoordinate(), endGenomCoord);

            geneRectangle.setX( vBoxwidth * getProportion(tmpStartGeneCoord, startGenomCoord, length));
            geneRectangle.setWidth(vBoxwidth * getProportion(tmpEndGeneCoord, tmpStartGeneCoord, length));

            group.getChildren().add(geneRectangle);

            // gene name


            Text geneNameText = new Text(geneSymbol);
            geneNameText.setX( vBoxwidth * getProportion(tmpStartGeneCoord, startGenomCoord, length) );
            geneNameText.setY(20);
            group.getChildren().add(geneNameText);

        }
        genePane.getChildren().add(group);
        geneHBox.getChildren().add(genePane);
        genomeViewerVBox.getChildren().add(geneHBox);

    }

    /**
     * sets the genes Map, which is used to generate the representation (rectangles)
     * @param chromosome
     * The information should be obtained from the database.
     */
    private HashMap<String, Gene> getGenesFromDb(String chromosome){
        HashMap<String, Gene> geneMapCurr = new HashMap<>();

        //    generate the map for all the genes in a particular chromosome

        NitriteCollection geneCollection = db.getCollection("allGenes");

        Cursor geneMapResults = geneCollection.find(Filters.eq("chr", chromosome));


       for (org.dizitart.no2.Document currGene : geneMapResults){


            String geneId = (String) currGene.get("geneID");
            String symbol = (String) currGene.get("symbol");
           ArrayList<String> transcripts = (ArrayList<String>) currGene.get("transcripts");
            int start = (int) currGene.get("start");
            int end = (int) currGene.get("end");
            String chrom = (String) currGene.get("chr");


            // Adds current gene info to hashmap which is then assigned to the gene Map later
            // genesMap is what is used to finally display the information
            Gene geneInfo = new Gene(symbol, chrom, start, end, transcripts);
            geneMapCurr.put(geneId, geneInfo);

        }



        return geneMapCurr;
    }

    /**
     * gets Foldchanges ArrayList from the database
     * @param genesHashMap map of genes, previously obtained
     */
    private LinkedList<FoldChangeTableModel> getFoldChangesList(HashMap<String, Gene> genesHashMap, String dgeCollectionName){
        LinkedList<FoldChangeTableModel> tmpFoldChangesLinkedList = new LinkedList<>();

        // get list of symbols. Used for the query
        ArrayList<String> symbolList = new ArrayList<>();
        for (Gene gene: genesHashMap.values()){
            symbolList.add(gene.getSymbol());
        }

        Cursor dgeFindIt = db.getCollection(dgeCollectionName).find(Filters.in("symbol", symbolList));

        for (Document dgeDoc: dgeFindIt){
            String dgeSymbol = dgeDoc.get("symbol", String.class);
            double dgeFoldChange =  dgeDoc.get("foldChange", double.class);
            double dgePvalAdj =  dgeDoc.get("pvalAdj", double.class);

            FoldChangeTableModel tmpFoldChangeTable = new FoldChangeTableModel(dgeSymbol, dgeFoldChange, dgePvalAdj);
            tmpFoldChangesLinkedList.add(tmpFoldChangeTable);
        }


        return tmpFoldChangesLinkedList;

    }




    /**
     * Gets a proportion, used instead of having to write the math operation each time.
     * @param end end position or coordinate.
     * @param start  start position or coordinate.
     * @param seqTotalSize total size of the window or sequence
     * @return a double that is the proportion.
     */
    private double getProportion (double end, double start, double seqTotalSize){

        if (seqTotalSize > 0) {
            return  (end - start) /(seqTotalSize);
        }
        return 0;
    }

    private void filterFoldChangeTable(){

        foldChangeTableView.getItems().clear();
        LinkedList<FoldChangeTableModel> foldChangesArrayClone = (LinkedList<FoldChangeTableModel>) foldChangesLinkedList.clone();

        String geneSymbolFilter = geneFilterFoldChangeTextField.getText().toUpperCase().trim();
        double pValFilter = (double) adjPValFilterFoldChangeSpinner.getValue();
        double foldChangeFilter = (double) foldFilterFoldChangeSpinner.getValue();



        Iterator<FoldChangeTableModel> foldChangeIt = foldChangesArrayClone.iterator();

        // filter by name
        if (geneSymbolFilter.length() > 0 ) {
            while (foldChangeIt.hasNext()) {
                FoldChangeTableModel tmpFoldChange = foldChangeIt.next();
                if (!tmpFoldChange.getGeneSymbol().contains(geneSymbolFilter)) {
                    foldChangeIt.remove();
                    continue;
                }
            }
        }

        // filter by pval
        foldChangeIt = foldChangesArrayClone.iterator();
        while (foldChangeIt.hasNext()) {
            FoldChangeTableModel tmpFoldChange = foldChangeIt.next();
            if (tmpFoldChange.getPVal() > pValFilter || Math.abs(tmpFoldChange.getLogFoldChange()) < foldChangeFilter) {
                foldChangeIt.remove();
            }
        }


        foldChangeTableView.getItems().addAll(foldChangesArrayClone);
        String percentage = Integer.toString( (int) ((double) foldChangesArrayClone.size() / (double) foldChangesLinkedList.size() *100 ));
        String labelText = foldChangesArrayClone.size() + "/" + foldChangesLinkedList.size() + " ("+ percentage  + "%)";
        numberOfGenesInTableLabel.setText(labelText);



    }


    /**
     * sends a thread to draw the volcano plot. A thread is used cause it may take some time to display
     * @param foldChangesLinkedList list of fold changes, this values come from the table
     * @param pvalThreshold  pval threshold
     * @param foldThreshold  fold change threshold
     */
    private void volcanoPlotThread(LinkedList<FoldChangeTableModel> foldChangesLinkedList, double pvalThreshold, double foldThreshold){
        if (foldChangesLinkedList.size() > 0){
            String volcanoFunction = getFunctionForVolcanoPlotSetGenesMap(foldChangesLinkedList, pvalThreshold, foldThreshold);
            new Thread(() -> drawVolcanoPlot(volcanoFunction)) {{start();}};
        } else {
            new Thread(() -> cleanNetworkWebView()) {{start();}};
        }

    }

    /**
     * draws the volcano plot and defines the geneSymbolsThresholds.
     * @param foldChangesLinkedList list of fold changes, this values come from the table
     * @param pvalThreshold   pval threshold
     * @param foldThreshold   fold change threshold
     */
    private String getFunctionForVolcanoPlotSetGenesMap(LinkedList<FoldChangeTableModel> foldChangesLinkedList, double pvalThreshold, double foldThreshold) {

        // new genesMap
        geneSymbolsThresholdsMap = new HashMap<>();

        // lists for data
        ArrayList<String> foldChangesPositiveFoldSymbol = new ArrayList<>();
        ArrayList<Double> foldChangesPositiveFoldLog10PVal = new ArrayList<>();
        ArrayList<Double> foldChangesPositiveFoldLog2FoldChange = new ArrayList<>();

        ArrayList<String> foldChangesNegativeFoldSymbol = new ArrayList<>();
        ArrayList<Double> foldChangesNegativeFoldLog10PVal = new ArrayList<>();
        ArrayList<Double> foldChangesNegativeFoldLog2FoldChange = new ArrayList<>();

        ArrayList<String> foldChangesBelowThresholdFoldSymbol = new ArrayList<>();
        ArrayList<Double> foldChangesBelowThresholdFoldLog10PVal = new ArrayList<>();
        ArrayList<Double> foldChangesBelowThresholdFoldLog2FoldChange = new ArrayList<>();

        // for the map
        ArrayList<String> belowFoldThresholdSymbol = new ArrayList<>();
        ArrayList<String> posSymbol = new ArrayList<>();
        ArrayList<String> negSymbol = new ArrayList<>();

        // set lists

        for (FoldChangeTableModel foldChange : foldChangesLinkedList) {

            if (-Math.log10(foldChange.getPVal()) == Double.POSITIVE_INFINITY) {
                if (foldChange.getLogFoldChange() >= foldThreshold) {
                    posSymbol.add(foldChange.getGeneSymbol());
                } else if (foldChange.getLogFoldChange() <= foldThreshold) {
                    negSymbol.add(foldChange.getGeneSymbol());
                } else {
                    belowFoldThresholdSymbol.add(foldChange.getGeneSymbol());
                }

                //to remove infinity from plot
                continue;
            }



            // if NaN, add it to the list and pass to the next cycle
            if ( Double.isNaN(foldChange.getPVal()) || Double.isNaN(foldChange.getLogFoldChange()) ){
                belowFoldThresholdSymbol.add(foldChange.getGeneSymbol()); // for the threshold map
                continue;
            }

            //  > p-val (careful, it is not log10 pval)     ||   ( -foldthreshold < foldchange < foldthreshold )
            if (foldChange.getPVal() > pvalThreshold || (foldChange.getLogFoldChange() > -foldThreshold && foldChange.getLogFoldChange() < foldThreshold)) {
                belowFoldThresholdSymbol.add(foldChange.getGeneSymbol()); // for the threshold map
//                foldChangesBelowThresholdFoldSymbol.add("'" + foldChange.getGeneSymbol() + "'");
//                foldChangesBelowThresholdFoldLog10PVal.add(-Math.log10(foldChange.getAdjPVal()));
//                foldChangesBelowThresholdFoldLog2FoldChange.add(foldChange.getLogFoldChange());
            } else {
                // foldChange.getAdjPVal() <= pvalThreshold
                if (foldChange.getLogFoldChange() <= foldThreshold) {
                    negSymbol.add(foldChange.getGeneSymbol()); // for the threshold map
                    foldChangesNegativeFoldSymbol.add("'" + foldChange.getGeneSymbol() + "'");
                    foldChangesNegativeFoldLog10PVal.add(-Math.log10(foldChange.getPVal()));
                    foldChangesNegativeFoldLog2FoldChange.add(foldChange.getLogFoldChange());
                } else {
                    posSymbol.add(foldChange.getGeneSymbol()); // for the threshold map
                    foldChangesPositiveFoldSymbol.add("'" + foldChange.getGeneSymbol() + "'");
                    foldChangesPositiveFoldLog10PVal.add(-Math.log10(foldChange.getPVal()));
                    foldChangesPositiveFoldLog2FoldChange.add(foldChange.getLogFoldChange());
                }
            }
        }


        // add to the genes symbol map
        geneSymbolsThresholdsMap.put("pos", posSymbol);
        geneSymbolsThresholdsMap.put("neg", negSymbol);
        geneSymbolsThresholdsMap.put("below", belowFoldThresholdSymbol);


        // generate json Strings to send to the  js function that generates the volcano plot in plotly
        StringBuilder foldChangesBelowThresholdJsonString = new StringBuilder();
        foldChangesBelowThresholdJsonString.append("{'logFoldChange': ");
        foldChangesBelowThresholdJsonString.append("[]");
//        foldChangesBelowThresholdJsonString.append(foldChangesBelowThresholdFoldLog2FoldChange.toString());
        foldChangesBelowThresholdJsonString.append(", 'logPval':");
        foldChangesBelowThresholdJsonString.append("[]");
//        foldChangesBelowThresholdJsonString.append(foldChangesBelowThresholdFoldLog10PVal.toString());
        foldChangesBelowThresholdJsonString.append(", 'texts':");
        foldChangesBelowThresholdJsonString.append("[]");
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

        ArrayList<Double> maxPVals = new ArrayList<>();
        maxPVals.add(Collections.max(foldChangesNegativeFoldLog10PVal));
        maxPVals.add(Collections.max(foldChangesPositiveFoldLog10PVal));
//        maxPVals.add(Collections.max(foldChangesBelowThresholdFoldLog10PVal));

        String maxPval = Double.toString(Collections.max(maxPVals) + 0.5);


        // javascript function that would be executed in the webview
        return "displayVolcano(" + foldChangesBelowThresholdJsonString + "," + foldChangesNegativeJsonString + "," + foldChangesPositiveJsonString + "," + pValLog10Threshold + "," + foldChangeThreshold + "," + maxPval + ")";

    }

    private void drawVolcanoPlot (String sendFunction){


        // plot volcano
        Platform.runLater(() -> {
            WebEngine webEngine = volcanoPlotWebView.getEngine();

            if(isNetworkWebviewLoaded){
                webEngine.executeScript(sendFunction);
            }else{
                webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        try {
                            webEngine.executeScript(new String(Files.readAllBytes(Paths.get(getClass().getResource("/javascript/plotly.js").toURI()))));
                            webEngine.executeScript(new String(Files.readAllBytes(Paths.get(getClass().getResource("/javascript/volcanoPlot.js").toURI()))));
//                            JSObject window = (JSObject) webEngine.executeScript("window");
//                            window.setMember("javaConnector", javaConnector);
                            webEngine.executeScript(sendFunction);
                        } catch (IOException | URISyntaxException e) {
                            e.printStackTrace();
                        }

                    }
                });
                webEngine.load(getClass().getResource("/html/plot.html").toExternalForm());
                isNetworkWebviewLoaded = true;
            }
        });


    }


    public void cleanNetworkWebView(){
        Platform.runLater(() -> {
            WebEngine webEngine = volcanoPlotWebView.getEngine();

            if(isNetworkWebviewLoaded){
                webEngine.executeScript("cleanVolcano()");
            }else{
                webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        try {
                            webEngine.executeScript(new String(Files.readAllBytes(Paths.get(getClass().getResource("/javascript/plotly.js").toURI()))));
                            webEngine.executeScript(new String(Files.readAllBytes(Paths.get(getClass().getResource("/javascript/volcanoPlot.js").toURI()))));
//                            JSObject window = (JSObject) webEngine.executeScript("window");
//                            window.setMember("javaConnector", javaConnector);

                            webEngine.executeScript("cleanVolcano()");
                        } catch (IOException | URISyntaxException e) {
                            e.printStackTrace();
                        }

                    }
                });
                webEngine.load(getClass().getResource("/html/plot.html").toExternalForm());
                isNetworkWebviewLoaded = true;
            }
        });
    }






}

