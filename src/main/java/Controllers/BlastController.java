package Controllers;

import Singletons.Config;
import TablesModels.FoldChangeTableModel;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.apache.commons.io.IOUtils;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import utilities.BioFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlastController implements Initializable {

    @FXML
    private CheckBox predictedCheckbox;
    @FXML
    private JFXTextField searchSpeciesField;
    @FXML
    private TableColumn<Hit, Double> queryCoverageColumn;
    @FXML
    private TableColumn<Hit, Double> hitCoverageColumn;
    @FXML
    private TextField searchField;
    @FXML
    private TableView<Hit> hitTable;
    @FXML
    private TableColumn<Hit, String> definitionColumn;
    @FXML
    private Pane alignmentPane;
    private BlastIndex blastIndex;
    private ArrayList<Hit> allHits;
    private ScrollPane seqAlignmentPane;
    private AutoCompletionBinding<String> speciesAutocompleteBinding;




    @Override
    public void initialize(URL location, ResourceBundle resources) {

        blastIndex = new BlastIndex();
        blastIndex.load();
        definitionColumn.setCellValueFactory( new PropertyValueFactory<>("definition"));
        queryCoverageColumn.setCellValueFactory( new PropertyValueFactory<>("queryCoverage"));
        hitCoverageColumn.setCellValueFactory( new PropertyValueFactory<>("hitCoverage"));

        definitionColumn.prefWidthProperty().bind(hitTable.widthProperty().multiply(0.8));
        queryCoverageColumn.prefWidthProperty().bind(hitTable.widthProperty().multiply(0.1));
        hitCoverageColumn.prefWidthProperty().bind(hitTable.widthProperty().multiply(0.1));

        queryCoverageColumn.setSortType(TableColumn.SortType.DESCENDING);
        hitTable.getSortOrder().add(queryCoverageColumn);
        hitTable.sort();

        hitTable.setRowFactory(tv -> {
            TableRow<Hit> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!(row.isEmpty())) {
                    if (event.getButton().equals(MouseButton.PRIMARY)){

                        if ( event.getClickCount() == 1 ) {
                            drawAlignment(hitTable.getSelectionModel().getSelectedItem());
                        }
                    }
                }

            });
            return row;
        });


        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterHits();
        });
        searchSpeciesField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterHits();
        });
        predictedCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            filterHits();
        });


    }



    public void selectGene(String geneId){

        hitTable.getItems().clear();
        alignmentPane.getChildren().clear();
        allHits = new ArrayList<>();

        try {

            long positionToRead = blastIndex.getRecords().get(geneId).getStart();
            int amountBytesToRead = (int) (blastIndex.getRecords().get(geneId).getEnd()-positionToRead);

            RandomAccessFile f = new RandomAccessFile(new File("/media/esteban/data/a2_denovo/blast/output.xml"),"r");
            //f.seek(2643);
            byte[] b = new byte[amountBytesToRead];
            f.seek(positionToRead);

            f.read(b);
            String str = new String(b);

            Pattern queryLengthPattern = Pattern.compile("<Iteration_query-len>(\\d+)</Iteration_query-len>");
            Matcher queryLengthMatcher = queryLengthPattern.matcher(str);
            if(queryLengthMatcher.find()) {
                int queryLength = Integer.parseInt(queryLengthMatcher.group(1));


                Pattern pattern = Pattern.compile("<Hit>(.*?)</Hit>", Pattern.DOTALL);
                Matcher hitsMatcher = pattern.matcher(str);
                while(hitsMatcher.find()){
                    for (int i = 1; i <= hitsMatcher.groupCount(); i++) {
                        String hitStr = hitsMatcher.group(i);

                        pattern = Pattern.compile("<Hit_def>(.*?)</Hit_def>.*?<Hit_len>(\\d+)</Hit_len>", Pattern.DOTALL);
                        Matcher hitMatcher = pattern.matcher(hitStr);
                        if (hitMatcher.find()) {
                            String hitDef = hitMatcher.group(1);
                            int hitLen = Integer.parseInt(hitMatcher.group(2));
                            Hit hit = new Hit(hitDef, hitLen, queryLength);

                            pattern = Pattern.compile("<Hsp>(.*?)</Hsp>", Pattern.DOTALL);
                            Matcher HspsMatcher = pattern.matcher(hitStr);
                            while (HspsMatcher.find()) {
                                for (int j = 1; j <= HspsMatcher.groupCount(); j++) {
                                    String hspStr = HspsMatcher.group(j);
                                    pattern = Pattern.compile("<Hsp_evalue>(.*?)</Hsp_evalue>.*?<Hsp_query-from>" +
                                            "(.*?)</Hsp_query-from>.*?<Hsp_query-to>(.*?)</Hsp_query-to>.*?" +
                                            "<Hsp_hit-from>(.*?)</Hsp_hit-from>.*?<Hsp_hit-to>(.*?)</Hsp_hit-to>.*?" +
                                            "<Hsp_qseq>(.*?)</Hsp_qseq>.*?<Hsp_hseq>(.*?)</Hsp_hseq>", Pattern.DOTALL);
                                    Matcher hspMatcher = pattern.matcher(hspStr);
                                    if (hspMatcher.find()) {
                                        int hitFrom = Integer.parseInt(hspMatcher.group(4));
                                        int hitTo = Integer.parseInt(hspMatcher.group(5));
                                        if(hitFrom<hitTo){
                                            hit.addHsp(new Hsp(Double.parseDouble(hspMatcher.group(1)), Integer.parseInt(hspMatcher.group(2)),
                                                    Integer.parseInt(hspMatcher.group(3)), hitFrom, hitTo, hspMatcher.group(6), hspMatcher.group(7)));

                                        }else{
                                            hit.addHsp(new Hsp(Double.parseDouble(hspMatcher.group(1)), Integer.parseInt(hspMatcher.group(2)),
                                                    Integer.parseInt(hspMatcher.group(3)), hitLen-hitFrom, hitLen-hitTo, hspMatcher.group(6), hspMatcher.group(7)));

                                        }
                                    }

                                }
                            }
                            allHits.add(hit);
                        }
                    }

                }
            }


            hitTable.getItems().addAll(allHits);
            hitTable.sort();




        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterHits(){


        System.out.println("phio;hhohio");
        ArrayList<Hit> filteredHits = new ArrayList<>();
        for (Hit hit : allHits) {

            if (searchField.getText().length()>0 &&!hit.getDefinition().toUpperCase(Locale.ROOT).contains(searchField.getText().toUpperCase(Locale.ROOT))) {
                continue;
            }
            if (searchSpeciesField.getText().length()>0 && !hit.getSpecies().toUpperCase(Locale.ROOT).contains(searchSpeciesField.getText().toUpperCase(Locale.ROOT))) {
                continue;
            }
            if(!predictedCheckbox.isSelected() && hit.isPredicted()) {
                continue;
            }
            filteredHits.add(hit);

        }


        hitTable.getItems().clear();
        hitTable.getItems().addAll(filteredHits);

        hitTable.sort();

        HashSet<String> species = new HashSet<>();
        for(Hit hit: filteredHits){
            species.add(hit.getSpecies());
        }

        if(speciesAutocompleteBinding!=null){
            speciesAutocompleteBinding.dispose();
        }
        speciesAutocompleteBinding = TextFields.bindAutoCompletion(searchSpeciesField, species);
        //speciesAutocompleteBinding.prefWidthProperty().bind(searchSpeciesField.widthProperty());

    }

    private void drawAlignment(Hit hit){

        alignmentPane.getChildren().clear();

        double width = alignmentPane.getWidth();


        int dashQueryCount = 0;
        int dashHitCount = 0;

        int queryOffsetStart = 0;
        int hitOffsetStart = 0;
        int queryOffsetEnd = 0;
        int hitOffsetEnd = 0;

        int offsetEnd = 0;

        ArrayList<Hsp> sortedHsp = hit.getHsps();
        sortedHsp.sort(Comparator.comparing(Hsp::getQueryFrom));

        int hspIndex = 0;
        for(Hsp hsp: sortedHsp){

            if(hspIndex==0) {

                queryOffsetStart = hsp.getQueryFrom();
                hitOffsetStart = hsp.getHitFrom();


            }else if(hspIndex==sortedHsp.size()-1){

                queryOffsetEnd = hit.getQueryLength() - hsp.getQueryTo();
                hitOffsetEnd = hit.getLength() - hsp.getHitTo();
            }

            hspIndex++;


        }



        int totalSeqLength = Math.max(queryOffsetStart, hitOffsetStart)
                + Math.max(hit.getQueryLength()+dashQueryCount, hit.getLength()+dashHitCount)
                + Math.max(queryOffsetEnd, hitOffsetEnd);

        double pixelsPerNucleotide = width / totalSeqLength;

        Rectangle queryRectangle = new Rectangle();
        queryRectangle.setWidth(hit.getQueryLength()*pixelsPerNucleotide);
        queryRectangle.setX(Math.max(0, hitOffsetStart-queryOffsetStart)*pixelsPerNucleotide);
        queryRectangle.setHeight(50);
        queryRectangle.setStyle("-fx-fill: #F4F4F4; -fx-stroke: black; -fx-stroke-width: 1;");

        Rectangle hitRectangle = new Rectangle();
        hitRectangle.setWidth(hit.getLength()*pixelsPerNucleotide);
        hitRectangle.setX(Math.max(0, queryOffsetStart-hitOffsetStart)*pixelsPerNucleotide);
        hitRectangle.setHeight(50);
        hitRectangle.setStyle("-fx-fill: #F4F4F4; -fx-stroke: black; -fx-stroke-width: 1;");
        hitRectangle.setY(100);

        alignmentPane.getChildren().add(queryRectangle);
        alignmentPane.getChildren().add(hitRectangle);

        for(Hsp hsp: sortedHsp){
            Rectangle queryHspRectangle = new Rectangle();
            Rectangle hitHspRectangle = new Rectangle();

            queryHspRectangle.setStyle("-fx-fill: red; -fx-stroke: black; -fx-stroke-width: 3;");
            hitHspRectangle.setStyle("-fx-fill: red; -fx-stroke: black; -fx-stroke-width: 3;");

            queryHspRectangle.setX((hsp.getQueryFrom()+Math.max(0, hitOffsetStart-queryOffsetStart))*pixelsPerNucleotide);
            hitHspRectangle.setX((hsp.getHitFrom()+Math.max(0, queryOffsetStart-hitOffsetStart))*pixelsPerNucleotide);

            queryHspRectangle.setWidth((hsp.getQueryTo()-hsp.getQueryFrom())*pixelsPerNucleotide);
            hitHspRectangle.setWidth((hsp.getHitTo()-hsp.getHitFrom())*pixelsPerNucleotide);

            hitHspRectangle.setY(100);

            queryHspRectangle.setHeight(50);
            hitHspRectangle.setHeight(50);

            queryHspRectangle.setOnMouseClicked(event -> {
                drawHsp(hsp, width);
            });
            hitHspRectangle.setOnMouseClicked(event -> {
                drawHsp(hsp, width);
            });


            Line line = new Line((hsp.getQueryFrom()+Math.max(0, hitOffsetStart-queryOffsetStart))*pixelsPerNucleotide+
                    ((hsp.getQueryTo()-hsp.getQueryFrom())*pixelsPerNucleotide)/2, 50, (hsp.getHitFrom()+Math.max(0, queryOffsetStart-hitOffsetStart))*pixelsPerNucleotide+
                    ((hsp.getHitTo()-hsp.getHitFrom())*pixelsPerNucleotide)/2, 100);
            line.setStrokeWidth(4);


            alignmentPane.getChildren().add(queryHspRectangle);
            alignmentPane.getChildren().add(hitHspRectangle);
            alignmentPane.getChildren().add(line);


        }


    }

    private void drawHsp(Hsp hsp, double width){

        alignmentPane.getChildren().remove(seqAlignmentPane);
        seqAlignmentPane = new ScrollPane();
        seqAlignmentPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        Pane pane = new Pane();

        //double pixelsPerNucleotide = width / hsp.getHseq().length();

        for (int i = 0; i < hsp.getHseq().length(); i++) {
            Text queryNucleotide = new Text(String.valueOf(hsp.getQseq().charAt(i)));
            queryNucleotide.setFont(Font.font("monospace", 20));
            Text hitNucleotide = new Text(String.valueOf(hsp.getHseq().charAt(i)));
            hitNucleotide.setFont(Font.font("monospace", 20));

            Rectangle queryRectangle = new Rectangle();
            Rectangle hitRectangle = new Rectangle();

            queryRectangle.setWidth(queryNucleotide.getLayoutBounds().getWidth());
            queryRectangle.setHeight(queryNucleotide.getLayoutBounds().getHeight());
            hitRectangle.setWidth(queryNucleotide.getLayoutBounds().getWidth());
            hitRectangle.setHeight(queryNucleotide.getLayoutBounds().getHeight());

            if(hsp.getQseq().charAt(i) == hsp.getHseq().charAt(i)){
                queryRectangle.setFill(Color.GREEN);
                hitRectangle.setFill(Color.GREEN);
            }else{
                queryRectangle.setFill(Color.RED);
                hitRectangle.setFill(Color.RED);
            }

            queryNucleotide.setX(i*queryNucleotide.getLayoutBounds().getWidth());
            hitNucleotide.setX(i*hitNucleotide.getLayoutBounds().getWidth());

            queryRectangle.setX(i*queryNucleotide.getLayoutBounds().getWidth());
            hitRectangle.setX(i*hitNucleotide.getLayoutBounds().getWidth());

            queryNucleotide.setY(queryNucleotide.getLayoutBounds().getHeight());
            hitNucleotide.setY(2*queryNucleotide.getLayoutBounds().getHeight()+20);

            queryRectangle.setY(0);
            hitRectangle.setY(queryNucleotide.getLayoutBounds().getHeight()+20);

            pane.getChildren().add(queryRectangle);
            pane.getChildren().add(hitRectangle);

            pane.getChildren().add(queryNucleotide);
            pane.getChildren().add(hitNucleotide);


        }
        seqAlignmentPane.setContent(pane);
        seqAlignmentPane.setPrefWidth(width);
        seqAlignmentPane.setLayoutY(200);
        alignmentPane.getChildren().add(seqAlignmentPane);
    }


    public class BlastIndex{


        HashMap<String, BlastIndexRecord> records = new HashMap<>();


        public void load(){
            String filepath = "/media/esteban/data/a2_denovo/blast/blastIndex.csv";
            try {
                File myObj = new File(filepath);
                Scanner myReader = new Scanner(myObj);
                myReader.nextLine();
                while (myReader.hasNextLine()) {
                    String[] data = myReader.nextLine().split(",");
                    records.put(data[0], new BlastIndexRecord(Long.parseLong(data[1]), Long.parseLong(data[2])));
                }
                myReader.close();
            } catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }


        public HashMap<String, BlastIndexRecord> getRecords() {
            return records;
        }

        public class BlastIndexRecord{
            private String gene;
            private long start;
            private long end;

            public BlastIndexRecord(long start, long end) {
                this.start = start;
                this.end = end;
            }

            public long getStart() {
                return start;
            }

            public long getEnd() {
                return end;
            }
        }
    }

    public class Hit{
        private String definition;
        private int length;
        private ArrayList<Hsp> hsps = new ArrayList<>();
        private int queryLength;

        public Hit(String def, int hitLength, int queryLength) {
            this.definition = def;
            this.length = hitLength;
            this.queryLength = queryLength;
        }

        public String getDefinition() {
            return definition;
        }

        public int getLength() {
            return length;
        }

        public void addHsp(Hsp hsp){
            hsps.add(hsp);
        }

        public ArrayList<Hsp> getHsps() {
            return hsps;
        }

        public int getQueryLength(){
            return queryLength;
        }

        public double getEvalue(){
            double minEval = 1.;
            for(Hsp hsp: hsps){
                if(hsp.getEvalue()<minEval){
                    minEval = hsp.getEvalue();
                }
            }
            return minEval;
        }

        public double getQueryCoverage(){

            int lengthCoveredByHsp = 0;
            for(Hsp hsp: hsps){
                lengthCoveredByHsp+=hsp.getQueryTo()-hsp.getQueryFrom()+1;
            }
            return (double) lengthCoveredByHsp/queryLength;
        }
        public double getHitCoverage(){
            int lengthCoveredByHsp = 0;
            for(Hsp hsp: hsps){
                lengthCoveredByHsp+=hsp.getHitTo()-hsp.getHitFrom()+1;
            }
            return (double) lengthCoveredByHsp/length;
        }

        public String getSpecies(){
            Pattern pattern = Pattern.compile("(?:PREDICTED: )*([a-zA-Z]+\\s[a-zA-Z]+).*");
            Matcher m = pattern.matcher(definition);
            if(m.find()){
                return m.group(1);
            }
            return null;
        }

        public boolean isPredicted(){
            return definition.contains("PREDICTED");
        }
    }
    public class Hsp{
        private double evalue;
        private Integer queryFrom;
        private int queryTo;
        private int hitFrom;
        private int hitTo;
        private String qseq;
        private String hseq;

        public Hsp(double evalue, int queryFrom, int queryTo, int hitFrom, int hitTo, String qseq, String hseq) {
            this.evalue = evalue;
            this.queryFrom = queryFrom;
            this.queryTo = queryTo;
            this.hitFrom = hitFrom;
            this.hitTo = hitTo;
            this.hseq = hseq;
            this.qseq = qseq;
        }

        public double getEvalue() {
            return evalue;
        }

        public Integer getQueryFrom() {
            return queryFrom;
        }

        public int getQueryTo() {
            return queryTo;
        }

        public int getHitFrom() {
            return hitFrom;
        }

        public int getHitTo() {
            return hitTo;
        }


        public String getQseq() {
            return qseq;
        }

        public String getHseq() {
            return hseq;
        }
    }
}
