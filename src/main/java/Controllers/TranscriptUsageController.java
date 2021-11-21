package Controllers;

import Cds.Exon;
import Cds.Transcript;
import FileReading.AllGenesReader;
import Singletons.Database;
import TablesModels.SplicingEventsTableModel;
import TablesModels.TranscriptUsageTableModel;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import graphics.ConfidentBarChart;
import graphics.ConfidentLineChart;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import org.dizitart.no2.*;


import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;

import static org.dizitart.no2.filters.Filters.*;

public class TranscriptUsageController extends Controller {

    @FXML
    private TabPane rightTabPane;
    @FXML
    private Pane exonsPane;
    @FXML
    private TableView<TranscriptUsageTableModel> transcriptUsageTableView;
    @FXML
    private TableColumn<TranscriptUsageTableModel, String> geneSymbolTableColumn;
    @FXML
    private TableColumn<TranscriptUsageTableModel, String> transcriptTableColumn;
    @FXML
    private TableColumn<TranscriptUsageTableModel, Double> dPsiTableColumn;
    @FXML
    private TableColumn<TranscriptUsageTableModel, Double> pValTableColumn;
    @FXML
    private Spinner<Double> pValFilterSplicingSpinner;
    @FXML
    private JFXCheckBox peptideEvidenceCheckbox;
    @FXML
    private Button filterSplicingButton;
    @FXML
    private JFXTextField geneFilterSplicingTextField;
    @FXML
    private JFXComboBox<String> comparisonSplicingCombobox;
    @FXML
    private GridPane mainGrid;
    @FXML
    private KeggController keggController;
    @FXML
    private GoTermsController goTermsController;

    private ResultsController parentController;
    private ConfidentLineChart confidentLineChart;
    private ConfidentBarChart transcriptExpressionBarchart;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


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

        geneSymbolTableColumn.setCellValueFactory( new PropertyValueFactory<>("gene"));
        transcriptTableColumn.setCellValueFactory( new PropertyValueFactory<>("transcript"));
        dPsiTableColumn.setCellValueFactory( new PropertyValueFactory<>("deltaPsi"));
        pValTableColumn.setCellValueFactory( new PropertyValueFactory<>("pval"));


        geneSymbolTableColumn.prefWidthProperty().bind(transcriptUsageTableView.widthProperty().divide(4));
        transcriptTableColumn.prefWidthProperty().bind(transcriptUsageTableView.widthProperty().divide(4));
        dPsiTableColumn.prefWidthProperty().bind(transcriptUsageTableView.widthProperty().divide(4));
        pValTableColumn.prefWidthProperty().bind(transcriptUsageTableView.widthProperty().divide(4));


        transcriptUsageTableView.setRowFactory(tv -> {
            TableRow<TranscriptUsageTableModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!(row.isEmpty())) {
                    if (event.getButton().equals(MouseButton.PRIMARY)){

                        if ( event.getClickCount() == 1 ) {

                            TranscriptUsageTableModel tmpSplEvent = transcriptUsageTableView.getSelectionModel().getSelectedItem();
                            drawConfidentChart(tmpSplEvent.getTranscript(), tmpSplEvent.getGene());

                            //goTermsController.setGoTermsGeneTable(tmpSplEvent.getGene());


                        } else if ( event.getClickCount() == 2 ) {
                            parentController.showGeneBrowser(row.getItem().getGene());
                        }
                    }
                }
            });
            return row;
        });

    }


    public void setParentControler(ResultsController parent,  AllGenesReader allGenesReader){
        parentController = parent;
        
        for(String collection: Database.getDb().listCollectionNames()){
            if(collection.contains("transcriptUsageDPSI")){
                comparisonSplicingCombobox.getItems().add(collection.replace("transcriptUsageDPSI_", ""));
            }
        }

        if(allGenesReader.getGenesLoadedProperty().get()){
            Platform.runLater(() -> {
                keggController.setParentController(this);
                goTermsController.setParentController(this, allGenesReader, Database.getDb());
            });
        }else{
            allGenesReader.getGenesLoadedProperty().addListener((observableValue, aBoolean, t1) -> {
                if (allGenesReader.getGenesLoadedProperty().get()) {
                    Platform.runLater(() -> {
                        keggController.setParentController(this);
                        goTermsController.setParentController(this, allGenesReader, Database.getDb());
                    });
                }
            });
        }

        setTableContentAndListeners();
    }

    private void setTableContentAndListeners(){

        comparisonSplicingCombobox.valueProperty().addListener((observableValue, o, t1) -> {
            filterTranscriptUsageTable();
        });

        filterSplicingButton.setOnAction(actionEvent -> {
            filterTranscriptUsageTable();
        });

        // display the table
        comparisonSplicingCombobox.getSelectionModel().select(0);

    }


    public void filterTranscriptUsageTable(){

        ArrayList<String> genesWithGoFilterList = (!goTermsController.isGoLoaded())?null:goTermsController.genesWithGoTermsForFilter();

        ArrayList<String> genesList = new ArrayList<>();
        ArrayList<TranscriptUsageTableModel> transcripts = new ArrayList();

        new Thread(() -> {
            String comparison = comparisonSplicingCombobox.getValue() ;
            double pvalThreshold = pValFilterSplicingSpinner.getValue();

            String geneSymbolFilter = geneFilterSplicingTextField.getText().toUpperCase().trim();


            // get the info for the table
            NitriteCollection splicingDPsiColl = Database.getDb().getCollection("transcriptUsageDPSI_"+comparisonSplicingCombobox.getValue());

            // filters
            List<Filter> filters = new ArrayList<>();

            filters.add(lte("pval", pvalThreshold));
            if (geneSymbolFilter.length() > 0 ) { // TODO: change this to check if in list
                filters.add(eq("geneName", geneSymbolFilter));
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
                    if (keggController.isKeggLoaded() ){
                        if (!keggController.isInKegg(tmpGeneName)){
                            continue;
                        }
                    }
                    String tmpTranscript = (String) tmpDoc.get("transcript");
                    Double tmpDeltaPsi = Double.valueOf(tmpDoc.get("deltaPsi").toString());
                    Double tmpPval = Double.valueOf(tmpDoc.get("pval").toString());



                    transcripts.add(new TranscriptUsageTableModel(tmpTranscript, tmpGeneName,  tmpDeltaPsi, tmpPval));


                    if (!genesList.contains(tmpGeneName)) {
                        genesList.add(tmpGeneName);
                    }


                }
            }
            boolean finalUpdateTableBool = updateTableBool;
            Platform.runLater(() -> {
                if(finalUpdateTableBool) {
                    transcriptUsageTableView.getItems().clear();
                    transcriptUsageTableView.getItems().addAll(transcripts);

//                    drawEventTypeChart(seEvents.get(), mxEvents.get(), a5Events.get(), a3Events.get(), riEvents.get(), afEvents.get(), alEvents.get(),
//                            splicingEventsTableView.getItems().size());
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

    private void drawConfidentChart(String transcript, String gene){
        NitriteCollection transcriptUsageCollection = Database.getDb().getCollection("transcriptUsage");

        Cursor cursor = transcriptUsageCollection.find(eq("geneName", gene));

        mainGrid.getChildren().remove(confidentLineChart);

        confidentLineChart = new ConfidentLineChart();
        confidentLineChart.setMax(1);
        confidentLineChart.setMin(0);

        confidentLineChart.setOnMouseLabelHoverCallback(this::drawTranscriptExpression);



        for (Document tmpDoc : cursor) {
            HashMap<String, Double> samplesPsi = (HashMap<String, Double>) tmpDoc.get("psi");

            HashMap<String, ArrayList<Double>> allPsi = new HashMap<>();

            for(Map.Entry<String, Double> psi: samplesPsi.entrySet()){
                String[] conditionSample = psi.getKey().split("_");

                if(!allPsi.containsKey(conditionSample[0])){
                    allPsi.put(conditionSample[0], new ArrayList<>());
                }
                allPsi.get(conditionSample[0]).add(psi.getValue());
            }

            confidentLineChart.addSeries((String) tmpDoc.get("transcript"), allPsi);
        }


        double maxLabelHeight = 0;
        for(Map.Entry<String, HashMap<String, ArrayList<Double>>> seriesEntry: confidentLineChart.getSeries().entrySet()) {
            for (Map.Entry<String, ArrayList<Double>> entry : seriesEntry.getValue().entrySet()) {


                Text xLabel = new Text(entry.getKey());
                xLabel.setFont(Font.font(17));
                double labelHeight = xLabel.getLayoutBounds().getWidth();

                if (labelHeight > maxLabelHeight) {
                    maxLabelHeight = labelHeight;
                }

            }
        }

        //GridPane.setRowIndex(confidentLineChart, 1);
        mainGrid.add(confidentLineChart, 0, 2);
        drawExons(gene, maxLabelHeight);
    }

    private void drawExons(String gene, double maxCategoryLabelHeight){

        exonsPane.getChildren().clear();



        double representationWidth = exonsPane.getWidth();


        NavigableSet<String> transcriptUsage = confidentLineChart.getSeries().navigableKeySet();

        Text t = new Text(transcriptUsage.iterator().next());
        t.setFont(Font.font(15));

        double margin = (mainGrid.getHeight()*0.55 - maxCategoryLabelHeight - transcriptUsage.size()*t.getLayoutBounds().getHeight()) / (transcriptUsage.size()+1);

        double offsetY = mainGrid.getHeight()*0.4 - rightTabPane.getTabMinHeight() + margin - t.getLayoutBounds().getHeight()/2;

        NitriteCollection allTranscriptsCollection = Database.getDb().getCollection("allTranscripts");
        Cursor cursor = allTranscriptsCollection.find(eq("gene", gene));

        HashMap<String, Transcript> transcripts = new HashMap<>();

        for(Document doc: cursor){
            if(transcriptUsage.contains(doc.get("transcriptID", String.class))){
                transcripts.put(doc.get("transcriptID", String.class), new Transcript(doc));
            }
        }

        int geneStart = 2147483647;
        int geneEnd = 0;


        for(String transcriptID: transcriptUsage){
            Transcript transcript = transcripts.get(transcriptID);
            if(transcript.getStartGenomCoord()<geneStart){
                geneStart = Math.toIntExact(transcript.getStartGenomCoord());
            }
            if(transcript.getEndGenomCoord()>geneEnd){
                geneEnd = Math.toIntExact(transcript.getEndGenomCoord());
            }
        }
        int geneLength = geneEnd - geneStart + 1;

        for(String transcriptID: transcriptUsage){
            Transcript transcript = transcripts.get(transcriptID);

            int currentPos = geneStart;

            for (Exon exon: transcript.getExons()){
                if(exon.getStart()-currentPos>0){
                    Line l = new Line(representationWidth * ((double) (currentPos-geneStart+1)/geneLength), offsetY,
                            representationWidth * ((double) (exon.getEnd()-geneStart+1)/geneLength), offsetY);
                    exonsPane.getChildren().add(l);
                }
                Rectangle rect = new Rectangle(representationWidth * ((double) (exon.getStart()-geneStart+1)/geneLength), offsetY-t.getBoundsInLocal().getHeight()/2,
                        representationWidth * ((double) (exon.getEnd()-exon.getStart()+1)/geneLength), t.getBoundsInLocal().getHeight());
                exonsPane.getChildren().add(rect);
                currentPos = exon.getEnd();
            }
            if(currentPos<geneEnd){
                Line l = new Line(representationWidth * ((double) (currentPos-geneStart+1)/geneLength), offsetY,
                        representationWidth, offsetY);
                exonsPane.getChildren().add(l);
            }

            offsetY+=t.getBoundsInLocal().getHeight() + margin;
        }


    }

    public String drawTranscriptExpression(String transcript){
        System.out.println(transcript);

        exonsPane.getChildren().remove(transcriptExpressionBarchart);

        NitriteCollection transcriptUsageCollection = Database.getDb().getCollection("transcriptCounts");

        Document doc = transcriptUsageCollection.find(eq("transcript", transcript)).firstOrDefault();
        HashMap<String, Double> readCounts = doc.get("readCounts", HashMap.class);

        HashMap<String, ArrayList<Double>> conditionCounts = new HashMap<>();

        for(Map.Entry<String, Double> entry: readCounts.entrySet()){
            String[] conditionSample = entry.getKey().split("/");
            if(!conditionCounts.containsKey(conditionSample[0])){
                conditionCounts.put(conditionSample[0], new ArrayList<>());
            }
            conditionCounts.get(conditionSample[0]).add(entry.getValue());
        }

        transcriptExpressionBarchart = new ConfidentBarChart();
        transcriptExpressionBarchart.addAll(conditionCounts);
        transcriptExpressionBarchart.setPrefHeight(mainGrid.getHeight()*0.4 - rightTabPane.getTabMinHeight());
        transcriptExpressionBarchart.setPrefWidth(exonsPane.getWidth());

        exonsPane.getChildren().add(transcriptExpressionBarchart);

        return "";
    }
}
