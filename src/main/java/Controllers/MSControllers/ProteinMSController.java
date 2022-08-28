package Controllers.MSControllers;

import Cds.*;
import Controllers.Controller;
import Controllers.DgeTableController;
import Controllers.GeneBrowserController;
import FileReading.UniprotProteinsCollection;
import Singletons.Config;
import Singletons.Database;
import graphics.CopyableText;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.filters.Filters;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utilities.MSRun;

import java.net.URL;
import java.util.*;

import static org.dizitart.no2.filters.Filters.and;
import static org.dizitart.no2.filters.Filters.eq;

public class ProteinMSController extends Controller implements Initializable {

    @FXML
    private ComboBox<String> comparisonCombo;
    @FXML
    private StructureController structureController;
    @FXML
    private PAGController pagController;
    @FXML
    private SpectrumViewerController spectrumViewerController;
    @FXML
    private HBox intensitiesPane;
    @FXML
    private AnchorPane structurePane;
    @FXML
    private AnchorPane spectrumPane;
    @FXML
    private AnchorPane pagPane;
    @FXML
    private GridPane grid;
    @FXML
    private TableColumn<PSM, String> psmFileColumn;
    @FXML
    private TableColumn<PSM, Long> psmTableIndexColumn;
    @FXML
    private TableView<PSM> psmTable;
    @FXML
    private TableColumn<PSM, String> psmTableSequenceColumn;
    @FXML
    private TableColumn<PSM, String> psmTableRunColumn;
    @FXML
    private ListView<String> peptidesListView;
    @FXML
    private TableView<ProteinRow> proteinsTable;
    @FXML
    private TableColumn<ProteinRow, String> proteinTableGeneColumn;
    @FXML
    private TableColumn<ProteinRow, Double> proteinTableLog2FcColumn;
    @FXML
    private TableColumn<ProteinRow, Double> proteinTablePvalueColumn;
    @FXML
    private Pane proteinsRepresentationPane;
    @FXML
    private ComboBox<String> runCombo;


    private MSRun currentRun;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        proteinTableGeneColumn.setCellValueFactory( new PropertyValueFactory<>("name"));
        proteinTableLog2FcColumn.setCellValueFactory( new PropertyValueFactory<>("log2Fc"));
        proteinTablePvalueColumn.setCellValueFactory( new PropertyValueFactory<>("pvalue"));

        proteinTableGeneColumn.prefWidthProperty().bind(proteinsTable.widthProperty().divide(3));
        proteinTableLog2FcColumn.prefWidthProperty().bind(proteinsTable.widthProperty().divide(3));
        proteinTablePvalueColumn.prefWidthProperty().bind(proteinsTable.widthProperty().divide(3));

        psmTableSequenceColumn.setCellValueFactory( new PropertyValueFactory<>("modifiedSequence"));
        psmTableRunColumn.setCellValueFactory( new PropertyValueFactory<>("run"));
        psmFileColumn.setCellValueFactory( new PropertyValueFactory<>("file"));
        psmTableIndexColumn.setCellValueFactory( new PropertyValueFactory<>("specIndex"));

        psmTableSequenceColumn.prefWidthProperty().bind(psmTable.widthProperty().divide(4));
        psmTableRunColumn.prefWidthProperty().bind(psmTable.widthProperty().divide(4));
        psmFileColumn.prefWidthProperty().bind(psmTable.widthProperty().divide(4));
        psmTableIndexColumn.prefWidthProperty().bind(psmTable.widthProperty().divide(4));

        proteinsTable.setRowFactory(tv -> {
            TableRow<ProteinRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!(row.isEmpty())) {
                    if (event.getButton().equals(MouseButton.PRIMARY)){
                        drawProteins(row.getItem().getName());
                        showProteinIntensity(row.getItem().getName());
                        structureController.load(UniprotProteinsCollection.getPDBIds(row.getItem().getName()));
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
                        loadSpectrum(row.getItem());
                        showPeptideIntensity(row.getItem().getIntensities());
                    }
                }
            });
            return row;
        });

        peptidesListView.setOnMouseClicked(event -> {
            onPeptideSelected(peptidesListView.getSelectionModel().getSelectedItem());
            pagController.showMap(peptidesListView.getSelectionModel().getSelectedItem(), currentRun.getName());
        });



        load();
    }

    public void load(){
        runCombo.getItems().addAll(Config.getRuns());
        runCombo.getSelectionModel().select(0);


        Set<String> conditions = Config.getRunConditions(runCombo.getSelectionModel().getSelectedItem());
        conditions.retainAll(Config.getConditions());
        List<String> combinations = Config.getComparisons(new ArrayList<>(conditions));
        for(String comparison: combinations){
            comparisonCombo.getItems().add(comparison);
        }
        if(comparisonCombo.getItems().size()>0)
            comparisonCombo.getSelectionModel().select(0);
        loadRun();
    }

    public void loadRun(){

        new Thread(()->{

            String run = runCombo.getSelectionModel().getSelectedItem();
            currentRun = new MSRun(run);


            Cursor dgeFindCursor = Database.getDb().getCollection(comparisonCombo.getSelectionModel().getSelectedItem().replace(" vs ", "vs")+"_dge").find();

            for(Document doc: dgeFindCursor){
                if (doc.containsKey("ms")){
                   JSONObject msDoc =  doc.get("ms", JSONObject.class);
                   if(msDoc.containsKey(run)){
                       try{
                           JSONObject runDoc = (JSONObject) msDoc.get(run);
                           double dgeFoldChange;
                           if (runDoc.get("log2fc") instanceof Long)
                               dgeFoldChange = ((Long) runDoc.get("log2fc")).doubleValue();
                           else
                               dgeFoldChange = (double) runDoc.get("log2fc");

                           double dgePVal = Double.NaN;
                           if (runDoc.containsKey("padj")) {
                               if (runDoc.get("padj") instanceof Long)
                                   dgePVal = ((Long) runDoc.get("padj")).doubleValue();
                               else
                                   dgePVal = (double) runDoc.get("padj");
                           }
                           proteinsTable.getItems().add(new ProteinRow((String) doc.get("symbol"), dgeFoldChange, dgePVal));
                       } catch (Exception e){
                           e.printStackTrace();
                       }


                   }
                }
            }

        }).start();

    }

    public void drawProteins(String gene){
        proteinsRepresentationPane.getChildren().clear();
        HashSet<String> allPeptides = new HashSet<>();

        HashSet<String> uniprotSequences = UniprotProteinsCollection.getSequences(gene);



        NitriteCollection allTranscriptsCollection = Database.getDb().getCollection("allTranscripts");
        Cursor cursor = allTranscriptsCollection.find(eq("gene", gene));

        HashMap<String, Transcript> transcripts = new HashMap<>();

        for(Document doc: cursor){
            if(doc.containsKey("CDS")){
                JSONObject allCds = (JSONObject) doc.get("CDS");
                for(Object cdsId: allCds.keySet()){
                    JSONObject cdsObj = (JSONObject) allCds.get(cdsId);
                    if(cdsObj.containsKey("peptides")){
                        transcripts.put(doc.get("transcriptID", String.class), new Transcript(doc));
                        JSONArray peptides = (JSONArray) cdsObj.get("peptides");
                        for(Object o: peptides){
                            JSONObject peptideObj = (JSONObject) o;
                            allPeptides.add((String) peptideObj.get("sequence"));
                        }
                    }
                }
            }
        }

        double idsWidth = 0;
        for(String transcriptID: transcripts.keySet()){
            Text t = new Text(transcriptID);
            t.setFont(Font.font("monospace", 15));
            double width = t.getLayoutBounds().getWidth();
            if(width>idsWidth)
                idsWidth=width;
        }

        int geneStart = 2147483647;
        int geneEnd = 0;
        int offsetY = 0;


        for(Map.Entry<String, Transcript> entry: transcripts.entrySet()){
            Transcript transcript = entry.getValue();
            if(transcript.getStartGenomCoord()<geneStart){
                geneStart = Math.toIntExact(transcript.getStartGenomCoord());
            }
            if(transcript.getEndGenomCoord()>geneEnd){
                geneEnd = Math.toIntExact(transcript.getEndGenomCoord());
            }
        }
        int geneLength = geneEnd - geneStart + 1;

        double representationWidth = proteinsRepresentationPane.getWidth()-idsWidth;

        for(Map.Entry<String, Transcript> entry: transcripts.entrySet()){
            Transcript transcript = entry.getValue();
            CopyableText t = new CopyableText(entry.getKey());
            t.setFont(Font.font("monospace", 15));
            t.setY(offsetY+t.getLayoutBounds().getHeight());
            proteinsRepresentationPane.getChildren().add(t);

            boolean isUniProtTranscript = false;
            for(CDS cds: entry.getValue().getCdss()){
                for(String uniprotSequence: uniprotSequences){
                    if(cds.getSequence().replace("*", "").equals(uniprotSequence)){
                        isUniProtTranscript = true;
                        break;
                    }
                }
            }

            int currentPos = geneStart;

            for (Exon exon: transcript.getExons()){
                if(currentPos>transcript.getStartGenomCoord() && currentPos<transcript.getEndGenomCoord()){
                    Line l = new Line(idsWidth+representationWidth * ((double) (currentPos-geneStart+1)/geneLength), offsetY+t.getLayoutBounds().getHeight()/2,
                            idsWidth+representationWidth * ((double) (exon.getEnd()-geneStart+1)/geneLength), offsetY+t.getLayoutBounds().getHeight()/2);
                    proteinsRepresentationPane.getChildren().add(l);
                }
                Rectangle rect = new Rectangle(idsWidth+representationWidth * ((double) (exon.getStart()-geneStart+1)/geneLength), offsetY,
                        representationWidth * ((double) (exon.getEnd()-exon.getStart()+1)/geneLength), t.getBoundsInLocal().getHeight());
                if(isUniProtTranscript){
                    rect.setFill(Color.BLUE);
                }
                proteinsRepresentationPane.getChildren().add(rect);
                currentPos = exon.getEnd();
            }

            offsetY+=t.getBoundsInLocal().getHeight();


            for(CDS cds: entry.getValue().getCdss()){
                if(cds.getPeptides().size()>0){

                    Pair<Integer, Integer> genomicCoords = cds.getGenomicPos(entry.getValue());
                    int cdsStartGenomCoord = genomicCoords.getKey();
                    int cdsEndGenomCoord = genomicCoords.getValue();
                    int exonIndex=0;
                    int cdsNucIndex = 1;

                    for(Exon exon: entry.getValue().getExons()){
                        int cdsRectangleStart=-1, cdsRectangleEnd=-1;
                        if(((exon.getStart()>=cdsStartGenomCoord && exon.getStart()<=cdsEndGenomCoord) ||
                                (exon.getEnd()>=cdsStartGenomCoord && exon.getEnd()<=cdsEndGenomCoord))  ||
                                (exon.getStart()<=cdsStartGenomCoord && exon.getEnd()>=cdsStartGenomCoord
                                        && exon.getStart()<=cdsEndGenomCoord && exon.getEnd()>=cdsEndGenomCoord)) {

                            if (exon.getStart() <= cdsStartGenomCoord) {

                                cdsRectangleStart = cdsStartGenomCoord+1;
                                if (exon.getEnd() <= cdsEndGenomCoord) {
                                    cdsRectangleEnd = exon.getEnd()+1;
                                } else {
                                    cdsRectangleEnd = cdsEndGenomCoord+1;
                                }
                            } else {
                                cdsRectangleStart = exon.getStart()+1;
                                if (exon.getEnd() <= cdsEndGenomCoord) {
                                    cdsRectangleEnd = exon.getEnd()+1;
                                } else {
                                    cdsRectangleEnd = cdsEndGenomCoord+1;
                                }

                            }
                            if (cdsRectangleStart != cdsRectangleEnd) {

                                Rectangle cdsRectangle = new Rectangle();
                                cdsRectangle.setHeight(t.getLayoutBounds().getHeight() - 1);

                                Tooltip cdsToolTip = new Tooltip("CDS");
                                cdsToolTip.setShowDelay(Duration.millis(500));
                                cdsToolTip.setFont(Font.font(15));
                                cdsToolTip.setShowDuration(Duration.seconds(4));
                                Tooltip.install(cdsRectangle, cdsToolTip);


                                cdsRectangle.setFill(Color.rgb(217, 33, 122));

                                int tmpStartGeneCoord = cdsRectangleStart - 1;
                                double width = representationWidth * Math.abs(getProportion(cdsRectangleEnd, tmpStartGeneCoord, geneLength));
                                double X = idsWidth+representationWidth * getProportion(tmpStartGeneCoord, geneStart, geneLength);

                                cdsRectangle.setX(X);
                                cdsRectangle.setWidth(width);
                                cdsRectangle.setY(offsetY);

                                int finalCdsNucIndex = cdsNucIndex;
                                int finalCdsRectangleEnd = cdsRectangleEnd;
                                int finalCdsRectangleStart = cdsRectangleStart;
                                cdsRectangle.setOnMouseEntered(event ->{
                                    if(cds.getStrand().equals("+")){
                                        StructureController.getInstance().colorPositions((finalCdsNucIndex+1)/3, (finalCdsNucIndex+1 + finalCdsRectangleEnd - finalCdsRectangleStart +1)/3);
                                    }else{
                                        StructureController.getInstance().colorPositions(cds.getSequence().length()+1-(finalCdsNucIndex + finalCdsRectangleEnd - finalCdsRectangleStart +1)/3, cds.getSequence().length()-(finalCdsNucIndex)/3+1);
                                    }

                                });
                                if(cdsRectangleStart!=-1 && cdsRectangleEnd!=-1){
                                    cdsNucIndex+=cdsRectangleEnd-cdsRectangleStart+1;
                                }
                                cdsRectangle.setOnMouseExited(event -> StructureController.getInstance().reset());

                                proteinsRepresentationPane.getChildren().add(cdsRectangle);
                            }

                        }

                        if(exon.getEnd()<cdsEndGenomCoord && exon.getEnd()>=cdsStartGenomCoord) {
                            Exon nextExon = transcript.getExons().get(exonIndex + 1);
                            cdsRectangleStart = exon.getEnd() + 1;
                            cdsRectangleEnd = nextExon.getStart();


                            if (cdsRectangleStart != cdsRectangleEnd) {
                                Rectangle cdsRectangleBetweenExons = new Rectangle();
                                cdsRectangleBetweenExons.setHeight(t.getLayoutBounds().getHeight() / 8.);


                                Tooltip cdsToolTipBetweenExons = new Tooltip("CDS");
                                cdsToolTipBetweenExons.setShowDelay(Duration.millis(500));
                                cdsToolTipBetweenExons.setFont(Font.font(15));
                                cdsToolTipBetweenExons.setShowDuration(Duration.seconds(4));
                                Tooltip.install(cdsRectangleBetweenExons, cdsToolTipBetweenExons);


                                //  color depending on having peptides
                                Color lineColor;
                                double opacity = cds.getPeptides().size() > 0 ? 0.5 : 0.3;
                                lineColor = new Color(0, 0, 0, opacity);

                                cdsRectangleBetweenExons.setFill(lineColor);


                                cdsRectangleBetweenExons.setY(t.getLayoutBounds().getHeight() / 8.);
                                cdsRectangleBetweenExons.setY(offsetY + t.getLayoutBounds().getHeight() / 2. - cdsRectangleBetweenExons.getHeight() / 2);


                                double width = representationWidth * Math.abs(getProportion(cdsRectangleEnd, cdsRectangleStart, geneLength));
                                double X = idsWidth+representationWidth * getProportion(cdsRectangleStart, geneStart, geneLength);

                                cdsRectangleBetweenExons.setX(X);
                                cdsRectangleBetweenExons.setWidth(width);
                                cdsRectangleBetweenExons.setFill(Color.rgb(217, 33, 122));


                                proteinsRepresentationPane.getChildren().add(cdsRectangleBetweenExons);
                            }
                        }

                        exonIndex++;


                    }
                    offsetY+=t.getBoundsInLocal().getHeight();

                    Group peptideGroup = GeneBrowserController.getPepGroup(cds, entry.getValue(), t.getLayoutBounds().getHeight(), geneStart, geneEnd, representationWidth, 15, isUniProtTranscript?this:null);
                    peptideGroup.setLayoutY(offsetY);
                    peptideGroup.setLayoutX(idsWidth);
                    proteinsRepresentationPane.getChildren().add(peptideGroup);
                }
            }
            offsetY+=t.getBoundsInLocal().getHeight()+10;
        }

        fillPeptide(allPeptides);
    }

    public void fillPeptide(HashSet<String> peptides){
        peptidesListView.getItems().clear();
        peptidesListView.getItems().addAll(peptides);
    }

    public void onPeptideSelected(String peptide){
        psmTable.getItems().clear();
        Document doc = Database.getDb().getCollection("peptideMap").find(and(Filters.eq("peptide", peptide), Filters.eq("run", runCombo.getSelectionModel().getSelectedItem()))).firstOrDefault();
        JSONObject intensitiesJson = (JSONObject) ((JSONObject) doc.get("intensity")).get(runCombo.getSelectionModel().getSelectedItem());
        HashMap<String, Double> intensities = new HashMap<>();
        for (Object sampleO : intensitiesJson.keySet()) {
            intensities.put((String) sampleO, (Double) intensitiesJson.get(sampleO));
        }

        showPeptideIntensity(intensities);
        JSONArray psms = (JSONArray) doc.get("psms");
        for(Object o : psms){
            JSONObject psmJson = (JSONObject) o;
            PSM psm  = new PSM((String) psmJson.get("mod"), (String) psmJson.get("run"),  (long) psmJson.get("specIndex"), (String) psmJson.get("file"));
            JSONObject psmIntensitiesJson = (JSONObject) psmJson.get("intensity");
            HashMap<String, Double> psmIntensities = new HashMap<>();
            for (Object sampleO : psmIntensitiesJson.keySet()) {
                psmIntensities.put((String) sampleO, (Double) psmIntensitiesJson.get(sampleO));
            }
            psm.setIntensities(psmIntensities);

            psmTable.getItems().add(psm);
        }
    }

    public void loadSpectrum(PSM psm){
        spectrumViewerController.clear();
        spectrumViewerController.setConfig(Config.getInstance());
        spectrumViewerController.select(psm , currentRun, peptidesListView.getSelectionModel().getSelectedItem());

    }
    private double getProportion(double end, double start, double totalSize) {

        if (totalSize > 0) {
            return (end - start) / (totalSize);
        }
        return 0;
    }

    public void showProteinIntensity(String gene){
        intensitiesPane.getChildren().clear();
        DgeTableController.drawSelectedGeneProteinQuant(gene, intensitiesPane, 15, runCombo.getSelectionModel().getSelectedItem(), true);
    }

    public void showPeptideIntensity(HashMap<String, Double> intensities){
        intensitiesPane.getChildren().clear();
        PeptideTableController.drawIntensitiesChart(intensities, intensitiesPane);
    }

    public class ProteinRow{
        private final String name;
        private final double log2Fc;
        private final double pvalue;

        public ProteinRow(String name, double log2Fc, double pvalue) {
            this.name = name;
            this.log2Fc = log2Fc;
            this.pvalue = pvalue;
        }

        public String getName() {
            return name;
        }

        public double getLog2Fc() {
            return log2Fc;
        }

        public double getPvalue() {
            return pvalue;
        }
    }








    }
