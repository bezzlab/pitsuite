package Controllers;

import Cds.Peptide;
import Singletons.Config;
import Singletons.Database;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.Filter;
import org.dizitart.no2.NitriteCollection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utilities.MSRun;

import javax.print.Doc;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.dizitart.no2.filters.Filters.*;

public class BlastTabController implements Initializable {

    @FXML
    private JFXTextField hitSearchField;
    @FXML
    private TableView<ProteinTableRow> proteinTable;
    @FXML
    private TableColumn<ProteinTableRow, String> proteinColumn;
    @FXML
    private TableColumn<ProteinTableRow, Double> evalueProteinColumn;
    @FXML
    private CheckBox peptideFilterBox;
    @FXML
    private TableColumn<Hit, Double> queryCoverageColumn;
    @FXML
    private TableColumn<Hit, Double> hitCoverageColumn;
    @FXML
    private TableColumn<Hit, String> definitionColumn;
    @FXML
    private TableColumn<Hit, Double> evalueColumn;
    @FXML
    private TableColumn<Hit, Integer> hspsColumn;
    @FXML
    private TableView<Hit> hitTable;
    @FXML
    private Pane alignmentPane;
    private ArrayList<Hit> allHits;
    private BlastIndex blastIndex;
    private ScrollPane seqAlignmentPane;
    private AutoCompletionBinding<String> speciesAutocompleteBinding;
    @FXML
    private CheckBox predictedCheckbox;
    @FXML
    private JFXTextField searchSpeciesField;
    @FXML
    private TextField searchField;
    @FXML
    private TextField querySearchField;
    @FXML
    private Spinner<Double> evalThresholdSpinner = new Spinner<Double>();

    private ArrayList<String> queriesList = new ArrayList<String>();
    private ObservableList<String> observableList = FXCollections.observableArrayList();

    @FXML
    private Label numberOfHits;
    @FXML
    private Text eValueTextField;
    @FXML
    private Text hitLengthTextField;
    @FXML
    private Label noOfQueries;

    private ArrayList<Hsp> hspsList = new ArrayList<Hsp>();

    private ArrayList<Peptide> allPeptides; /* all peptides from "allTranscripts.json" */
    @FXML
    private Label numberOfPeptides;
    @FXML
    private Label peptideSeqs;

    private String selectedProtein;



        @Override
        public void initialize (URL location, ResourceBundle resources){
            blastIndex = new BlastIndex();
            blastIndex.load();

            /* evalue spinner */
            SpinnerValueFactory<Double> evalFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 5, 1, 0.001);
            evalFactory.setConverter(doubleConverter);
            evalThresholdSpinner.setValueFactory(evalFactory);
            evalThresholdSpinner.getValueFactory().setValue(0.010);
            evalThresholdSpinner.setEditable(true);

            proteinColumn.setCellValueFactory(new PropertyValueFactory<>("protein"));
            evalueProteinColumn.setCellValueFactory(new PropertyValueFactory<>("evalue"));
            proteinColumn.prefWidthProperty().bind(proteinTable.widthProperty().divide(2));
            evalueProteinColumn.prefWidthProperty().bind(proteinTable.widthProperty().divide(2));

            proteinTable.getSortOrder().add(evalueProteinColumn);

            /* hit table */
            definitionColumn.setCellValueFactory(new PropertyValueFactory<>("definition"));
            queryCoverageColumn.setCellValueFactory(new PropertyValueFactory<>("queryCoverage"));
            hitCoverageColumn.setCellValueFactory(new PropertyValueFactory<>("hitCoverage"));
            evalueColumn.setCellValueFactory(new PropertyValueFactory<>("EValue"));
            hspsColumn.setCellValueFactory(new PropertyValueFactory<>("noOfHsps"));

            definitionColumn.prefWidthProperty().bind(hitTable.widthProperty().multiply(0.6));
            queryCoverageColumn.prefWidthProperty().bind(hitTable.widthProperty().multiply(0.1));
            hitCoverageColumn.prefWidthProperty().bind(hitTable.widthProperty().multiply(0.1));
            evalueColumn.prefWidthProperty().bind(hitTable.widthProperty().multiply(0.1));
            hspsColumn.prefWidthProperty().bind(hitTable.widthProperty().multiply(0.1));

            queryCoverageColumn.setSortType(TableColumn.SortType.DESCENDING);
            hitTable.getSortOrder().add(evalueColumn);
            hitTable.sort();

            proteinTable.setRowFactory(tv -> {
                TableRow<ProteinTableRow> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (!(row.isEmpty())) {
                        if (event.getButton().equals(MouseButton.PRIMARY)) {
                            selectProtein(row.getItem());
                            numberOfHits.setText(hitTable.getItems().size() + "");
                        }else if(event.getButton().equals(MouseButton.SECONDARY)){
                            final ContextMenu rowMenu = new ContextMenu();
                            MenuItem dgeMenu = new MenuItem("Show Differencial gene expression");
                            dgeMenu.setOnAction(e -> {
                                ResultsController.getInstance().moveToTab(2);
                                DgeTableController.getInstance().searchForGene(row.getItem().getProtein().split("_i")[0]);
                            });

                            MenuItem browserMenu = new MenuItem("Show gene browser");
                            browserMenu.setOnAction(e -> {
                                ResultsController.getInstance().moveToTab(1);
                                GeneBrowserController.getInstance().showGeneBrowser(row.getItem().getProtein().split("_i")[0]);
                            });

                            rowMenu.getItems().add(dgeMenu);
                            rowMenu.getItems().add(browserMenu);

                            row.contextMenuProperty().bind(
                                    Bindings.when(row.emptyProperty())
                                            .then((ContextMenu) null)
                                            .otherwise(rowMenu));
                        }
                    }

                });
                return row;
            });


            hitTable.setRowFactory(tv -> {
                TableRow<Hit> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (!(row.isEmpty())) {
                        if (event.getButton().equals(MouseButton.PRIMARY)) {

                            if (event.getClickCount() == 1) {
                                drawAlignment(hitTable.getSelectionModel().getSelectedItem());

                                hitLengthTextField.setText(String.valueOf(hitTable.getSelectionModel().getSelectedItem().getLength()));
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

            querySearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterQueries();
            });
            hitSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterQueries();
            });

            evalThresholdSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
                ProteinTableRow g = proteinTable.getSelectionModel().getSelectedItem();
                selectProtein(g);
                filterHits();

            });
        }

        /* load peptide list for peptide browser */
        public HashSet<Peptide> findGenePeptides (String gene) {

            NitriteCollection collection = Database.getDb().getCollection("genePeptides");
            Cursor cursor = collection.find(eq("gene", gene));
            HashSet<Peptide> peptides = new HashSet<>(cursor.size());
            for (Document doc : cursor) {
                org.json.simple.JSONObject peptidesObj = doc.get("peptides", org.json.simple.JSONObject.class);
                for(Object o: peptidesObj.keySet()){
                    String peptide = (String) o;
                    if(peptidesObj.get(peptide) instanceof org.json.simple.JSONObject){
                        peptides.add(new Peptide(peptide, new MSRun((String) doc.get("run"))));
                    }
                }

            }
            return peptides;
        }


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

        public class BlastIndex {

            HashMap<String, BlastIndex.BlastIndexRecord> records = new HashMap<>();

            public void load() {
                String filepath = Config.getOutputPath()+"/blast/blastIndex.csv";
                try {
                    File myObj = new File(filepath);
                    Scanner myReader = new Scanner(myObj);
                    myReader.nextLine();
                    while (myReader.hasNextLine()) {
                        String[] data = myReader.nextLine().split(",");
                        records.put(data[0], new BlastIndex.BlastIndexRecord(Long.parseLong(data[1]), Long.parseLong(data[2])));
                    }
                    myReader.close();
                } catch (FileNotFoundException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                }
                getKeys();
                filterQueries();
                getNoOfQueries();
            }

            public void getKeys() {
                queriesList.clear();
                queriesList.addAll(records.keySet());
            }



            public HashMap<String, BlastIndex.BlastIndexRecord> getRecords() {
                return records;
            }

            public class BlastIndexRecord {
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

                public String getGene() {
                    return gene;
                }
            }
        }


        public void selectProtein (ProteinTableRow row){

            Double eValThreshold = evalThresholdSpinner.getValue();

            hitTable.getItems().clear();
            alignmentPane.getChildren().clear();
            allHits = new ArrayList<>();
            selectedProtein = row.getProtein();

            if(Double.isNaN(row.getEvalue()))
                drawNovelSequence(selectedProtein);
            else {


                try {
                    long positionToRead = blastIndex.getRecords().get(selectedProtein).getStart();
                    int amountBytesToRead = (int) (blastIndex.getRecords().get(selectedProtein).getEnd() - positionToRead);


                    RandomAccessFile f = new RandomAccessFile(new File(Config.getOutputPath() + "/blast/output.xml"), "r");

                    byte[] b = new byte[amountBytesToRead];
                    f.seek(positionToRead);

                    f.read(b);
                    String str = new String(b);

                    Pattern queryLengthPattern = Pattern.compile("<Iteration_query-len>(\\d+)</Iteration_query-len>");
                    Matcher queryLengthMatcher = queryLengthPattern.matcher(str);
                    if (queryLengthMatcher.find()) {
                        int queryLength = Integer.parseInt(queryLengthMatcher.group(1));
                        Pattern pattern = Pattern.compile("<Hit>(.*?)</Hit>", Pattern.DOTALL);
                        Matcher hitsMatcher = pattern.matcher(str);
                        while (hitsMatcher.find()) {
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
                                                Hsp hsp1 = new Hsp(Double.parseDouble(hspMatcher.group(1)), Integer.parseInt(hspMatcher.group(2)),
                                                        Integer.parseInt(hspMatcher.group(3)), hitFrom, hitTo, hspMatcher.group(6), hspMatcher.group(7));

                                                if (hitFrom < hitTo) {
                                                    if (hsp1.getEvalue() < eValThreshold) {

                                                        hit.addHsp(new Hsp(Double.parseDouble(hspMatcher.group(1)), Integer.parseInt(hspMatcher.group(2)),
                                                                Integer.parseInt(hspMatcher.group(3)), hitFrom, hitTo, hspMatcher.group(6), hspMatcher.group(7)));
                                                    }
                                                } else {
                                                    if (hsp1.getEvalue() < eValThreshold) {

                                                        hit.addHsp(new Hsp(Double.parseDouble(hspMatcher.group(1)), Integer.parseInt(hspMatcher.group(2)),
                                                                Integer.parseInt(hspMatcher.group(3)), hitLen - hitFrom, hitLen - hitTo, hspMatcher.group(6), hspMatcher.group(7)));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    boolean hspsNotEmpty = true;
                                    if (hit.getHsps().isEmpty()) {
                                        hspsNotEmpty = false;
                                    }
                                    if (hspsNotEmpty) {
                                        allHits.add(hit);

                                    }
                                }
                            }
                        }
                    }

                    hitTable.getItems().addAll(allHits);
                    numberOfHits.setText(hitTable.getItems().size() + "");


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        private void filterHits () {

            ArrayList<Hit> filteredHits = new ArrayList<>();
            for (Hit hit : allHits) {

                if (searchField.getText().length() > 0 && !hit.getDefinition().toUpperCase(Locale.ROOT).contains(searchField.getText().toUpperCase(Locale.ROOT))) {
                    continue;
                }
                if (searchSpeciesField.getText().length() > 0 && !hit.getSpecies().toUpperCase(Locale.ROOT).contains(searchSpeciesField.getText().toUpperCase(Locale.ROOT))) {
                    continue;
                }
                if (!predictedCheckbox.isSelected() && hit.isPredicted()) {
                    continue;
                }
                filteredHits.add(hit);

            }

            hitTable.getItems().clear();
            hitTable.getItems().addAll(filteredHits);

            hitTable.sort();

            HashSet<String> species = new HashSet<>();
            for (Hit hit : filteredHits) {
                species.add(hit.getSpecies());
            }

            if (speciesAutocompleteBinding != null) {
                speciesAutocompleteBinding.dispose();
            }
            speciesAutocompleteBinding = TextFields.bindAutoCompletion(searchSpeciesField, species);
        }

        @FXML
        private void filterQueries () {

            proteinTable.getItems().clear();

            NitriteCollection collection = Database.getDb().getCollection("blast");
            List<Filter> filters = new ArrayList<>(4);
            if(!querySearchField.getText().isEmpty()){
                filters.add(regex("protein", "^.*" + querySearchField.getText() + ".*$"));
            }
            if(!hitSearchField.getText().isEmpty()){
                filters.add(regex("hits", "^.*" + hitSearchField.getText() + ".*$"));
            }
            if(peptideFilterBox.isSelected()){
                filters.add(eq("hasPeptideEvidence", true));
            }

            Iterator<Document> iterator = collection.find(and(filters.toArray(new Filter[]{}))).iterator();
            while(iterator.hasNext()) {
                Document doc = iterator.next();
                if(doc.containsKey("lowestEvalue")){
                    proteinTable.getItems().add(new ProteinTableRow((String) doc.get("protein"), (double) doc.get("lowestEvalue")));
                }else{
                    proteinTable.getItems().add(new ProteinTableRow((String) doc.get("protein"), Double.NaN));
                }

            }


//            if(peptideFilterBox.isSelected()){
//                Iterator<Document> iterator = Database.getDb().getCollection("allTranscripts").find().iterator();
//                while(iterator.hasNext()){








//            ArrayList<String> filteredQueries = new ArrayList<>();
//
//            HashMap<String, Boolean> proteinsHavePeptides = new HashMap<>();
//            if(peptideFilterBox.isSelected()){
//                Iterator<Document> iterator = Database.getDb().getCollection("allTranscripts").find().iterator();
//                while(iterator.hasNext()){
//                    Document transcript = iterator.next();
//                    if(transcript.containsKey("CDS")) {
//                        JSONObject allCdsObj = transcript.get("CDS", JSONObject.class);
//                        for (Object cdsId : allCdsObj.keySet()) {
//                            JSONObject cds = (JSONObject) allCdsObj.get(cdsId);
//                            proteinsHavePeptides.put((String) cdsId, cds.containsKey("peptides"));
//                        }
//                    }
//                }
//            }
//            for (String q : queriesList) {
//                if (querySearchField.getText().length() == 0 || q.toUpperCase(Locale.ROOT).contains(querySearchField.getText().toUpperCase(Locale.ROOT))) {
//                    if(!peptideFilterBox.isSelected() || proteinsHavePeptides.get(q))
//                        filteredQueries.add(q);
//                }
//            }
//
//            listView.getItems().clear();
//            listView.getItems().addAll(filteredQueries);

        }

        private void drawAlignment (Hit hit){

            alignmentPane.getChildren().clear();

            double eValThreshold = evalThresholdSpinner.getValue();

            double opacity = 0.85;

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

            /* list for controlling hsps with a keyboard */
            hspsList.addAll(sortedHsp);


            int hspIndex = 0;

            for (Hsp hsp : sortedHsp) {
                if (sortedHsp.size() > 1) {
                    opacity = 0.56;
                } else if (sortedHsp.size() > 2) {
                    opacity = 0.52;
                }

                if (hspIndex == 0) {
                    queryOffsetStart = hsp.getQueryFrom();
                    hitOffsetStart = hsp.getHitFrom();
                } else if (hspIndex == sortedHsp.size() - 1) {
                    queryOffsetEnd = hit.getQueryLength() - hsp.getQueryTo();
                    hitOffsetEnd = hit.getLength() - hsp.getHitTo();
                }
                hspIndex++;
            }

            int totalSeqLength = Math.max(queryOffsetStart, hitOffsetStart)
                    + Math.max(hit.getQueryLength() + dashQueryCount, hit.getLength() + dashHitCount)
                    + Math.max(queryOffsetEnd, hitOffsetEnd);

            double pixelsPerNucleotide = width / totalSeqLength;

            Rectangle queryRectangle = new Rectangle();
            queryRectangle.setWidth(hit.getQueryLength() * pixelsPerNucleotide);
            queryRectangle.setX(Math.max(0, hitOffsetStart - queryOffsetStart) * pixelsPerNucleotide);
            queryRectangle.setHeight(50);
            queryRectangle.setStyle("-fx-fill: #F4F4F4; -fx-stroke: #0e1111; -fx-stroke-width: 1;");
            queryRectangle.setArcHeight(14.0);
            queryRectangle.setArcWidth(7.0);


            Rectangle hitRectangle = new Rectangle();
            hitRectangle.setWidth(hit.getLength() * pixelsPerNucleotide);
            hitRectangle.setX(Math.max(0, queryOffsetStart - hitOffsetStart) * pixelsPerNucleotide);
            hitRectangle.setHeight(50);
            hitRectangle.setStyle("-fx-fill: #F4F4F4; -fx-stroke: #0e1111; -fx-stroke-width: 1;");
            hitRectangle.setY(100);
            hitRectangle.setArcHeight(14.0);
            hitRectangle.setArcWidth(7.0);
            Text hitText = new Text(" Hit");
            hitText.setX((Math.max(0, queryOffsetStart - hitOffsetStart) * pixelsPerNucleotide) + 10);
            hitText.setY(100);

            alignmentPane.getChildren().add(queryRectangle);
            alignmentPane.getChildren().add(hitRectangle);

            for (Hsp hsp : sortedHsp) {
                Rectangle queryHspRectangle = new Rectangle();
                Rectangle hitHspRectangle = new Rectangle();

                queryHspRectangle.setStyle("-fx-fill: #34568B; -fx-stroke: black; -fx-stroke-width: 3;");
                queryHspRectangle.setOpacity(opacity);
                queryHspRectangle.setArcHeight(14.0);
                queryHspRectangle.setArcWidth(7.0);

                hitHspRectangle.setStyle("-fx-fill: #34568B; -fx-stroke: black; -fx-stroke-width: 3;");
                hitHspRectangle.setOpacity(opacity);
                hitHspRectangle.setArcHeight(14.0);
                hitHspRectangle.setArcWidth(7.0);

                queryHspRectangle.setX((hsp.getQueryFrom() + Math.max(0, hitOffsetStart - queryOffsetStart)) * pixelsPerNucleotide);
                hitHspRectangle.setX((hsp.getHitFrom() + Math.max(0, queryOffsetStart - hitOffsetStart)) * pixelsPerNucleotide);

                queryHspRectangle.setWidth((hsp.getQueryTo() - hsp.getQueryFrom()) * pixelsPerNucleotide);
                hitHspRectangle.setWidth((hsp.getHitTo() - hsp.getHitFrom()) * pixelsPerNucleotide);

                hitHspRectangle.setY(100);

                queryHspRectangle.setHeight(50);
                hitHspRectangle.setHeight(50);

                /* css colors list for hsp outlines */
                List<String> givenList = Arrays.asList("#e6194B;", "#3cb44b;", "#ffe119;", "#4363d8;", "#f58231;", "#911eb4;", "#42d4f4;", "#f032e6;", "#bfef45;", "#fabed4;", "#469990;", "#dcbeff;", "#9A6324;", "#fffac8;", "#800000;", "#aaffc3;", "#808000;", "#ffd8b1;", "#000075;");
                Random rand = new Random();
                String randomElement = givenList.get(rand.nextInt(givenList.size()));
                List<String> usedColors = Arrays.asList();
                queryHspRectangle.setOnMouseClicked(event -> {

                    drawHsp(hsp, width);
                    eValueTextField.setText(String.valueOf(hsp.getEvalue()));
                    hitHspRectangle.setStyle("-fx-fill: #34568B; -fx-stroke-width: 3; -fx-stroke: " + randomElement);
                    queryHspRectangle.setStyle("-fx-fill: #34568B; -fx-stroke-width: 3; -fx-stroke: " + randomElement);
                });
                hitHspRectangle.setOnMouseClicked(event -> {
                    drawHsp(hsp, width);
                    eValueTextField.setText(String.valueOf(hsp.getEvalue()));
                    hitHspRectangle.setStyle("-fx-fill: #34568B; -fx-stroke-width: 3; -fx-stroke: " + randomElement);
                    queryHspRectangle.setStyle("-fx-fill: #34568B; -fx-stroke-width: 3; -fx-stroke: " + randomElement);
                });


                Line line = new Line((hsp.getQueryFrom() + Math.max(0, hitOffsetStart - queryOffsetStart)) * pixelsPerNucleotide +
                        ((hsp.getQueryTo() - hsp.getQueryFrom()) * pixelsPerNucleotide) / 2, 50, (hsp.getHitFrom() + Math.max(0, queryOffsetStart - hitOffsetStart)) * pixelsPerNucleotide +
                        ((hsp.getHitTo() - hsp.getHitFrom()) * pixelsPerNucleotide) / 2, 100);
                line.setStrokeWidth(2);
                line.setStyle("-fx-stroke: #0e1725;");


                alignmentPane.getChildren().add(queryHspRectangle);
                alignmentPane.getChildren().add(hitHspRectangle);
                alignmentPane.getChildren().add(line);

            }

            /* listener to control hsps with Enter key */
            final int[] current_index = {0};
            hitTable.setOnKeyPressed(event -> {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    current_index[0] = (current_index[0] + 1) % sortedHsp.size();
                    drawHsp(sortedHsp.get(current_index[0]), width);
                    eValueTextField.setText(String.valueOf(sortedHsp.get(current_index[0]).getEvalue()));
                }
            });

        }

        private void drawHsp (Hsp hsp,double width){

            alignmentPane.getChildren().remove(seqAlignmentPane);
            seqAlignmentPane = new ScrollPane();
            seqAlignmentPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            Text t = new Text("T");
            t.setFont(Font.font("monospace", FontWeight.SEMI_BOLD, 20));
            seqAlignmentPane.setPrefHeight((t.getLayoutBounds().getHeight()+10)*4);
            Pane pane = new Pane();

            /* define dictionaries for conservative replacement colouring (EMBL-EBI Clustal Omega scheme used) */

            Map<String, Character> conserv_replace_dict = new HashMap<>();
            Map<Character, String> reverse_dict = new HashMap<>();

            HashSet<Peptide> proteinPeptides = findGenePeptides(selectedProtein.split("_i")[0]);

            List<Character> small_red = Arrays.asList('A', 'V', 'F', 'P', 'M', 'I', 'L', 'W');
            List<Character> acidic_blue = Arrays.asList('D', 'E');
            List<Character> basic_magenta = Arrays.asList('K', 'R');
            List<Character> hydroxyl_green = Arrays.asList('S', 'T', 'Y', 'H', 'C', 'N', 'G', 'Q');

            for (Character c : small_red) {
                conserv_replace_dict.put("red", c);
                reverse_dict.put(c, "red");
            }
            for (Character c : acidic_blue) {
                conserv_replace_dict.put("blue", c);
                reverse_dict.put(c, "blue");
            }
            for (Character c : basic_magenta) {
                conserv_replace_dict.put("magenta", c);
                reverse_dict.put(c, "magenta");
            }
            for (Character c : hydroxyl_green) {
                conserv_replace_dict.put("green", c);
                reverse_dict.put(c, "green");
            }


            for (int i = 0; i < hsp.getHseq().length(); i++) {
                Text queryNucleotide = new Text(String.valueOf(hsp.getQseq().charAt(i)));
                FontWeight fontWeight = FontWeight.SEMI_BOLD;
                queryNucleotide.setFont(Font.font("monospace", fontWeight, 20));
                Text hitNucleotide = new Text(String.valueOf(hsp.getHseq().charAt(i)));
                hitNucleotide.setFont(Font.font("monospace", fontWeight, 20));
                Text symbolText = new Text("I");

                Rectangle queryRectangle = new Rectangle();
                Rectangle hitRectangle = new Rectangle();
                Rectangle symbolRectangle = new Rectangle();

                queryRectangle.setWidth(queryNucleotide.getLayoutBounds().getWidth());
                queryRectangle.setHeight(queryNucleotide.getLayoutBounds().getHeight());
                hitRectangle.setWidth(queryNucleotide.getLayoutBounds().getWidth());
                hitRectangle.setHeight(queryNucleotide.getLayoutBounds().getHeight());
                symbolRectangle.setWidth(queryNucleotide.getLayoutBounds().getWidth());
                symbolRectangle.setWidth(queryNucleotide.getLayoutBounds().getWidth());

                /* draw alignments symbols (I, *) and colours */
                if (hsp.getQseq().charAt(i) == hsp.getHseq().charAt(i)) {
                    if (reverse_dict.get(hsp.getQseq().charAt(i)).equals("red")) {
                        queryRectangle.setFill(Paint.valueOf("#CD5C5C"));
                        hitRectangle.setFill(Paint.valueOf("#CD5C5C"));
                    } else if (reverse_dict.get(hsp.getQseq().charAt(i)).equals("blue")) {
                        queryRectangle.setFill(Paint.valueOf("#6A5ACD"));
                        hitRectangle.setFill(Paint.valueOf("#6A5ACD"));
                    } else if (reverse_dict.get(hsp.getQseq().charAt(i)).equals("magenta")) {
                        queryRectangle.setFill(Paint.valueOf("#BA55D3"));
                        hitRectangle.setFill(Paint.valueOf("#BA55D3"));
                    } else if (reverse_dict.get(hsp.getQseq().charAt(i)).equals("green")) {
                        queryRectangle.setFill(Paint.valueOf("#6B8E23"));
                        hitRectangle.setFill(Paint.valueOf("#6B8E23"));
                    }
                    symbolText = new Text("I");
                } else if (reverse_dict.containsKey(hsp.getQseq().charAt(i)) && reverse_dict.containsKey(hsp.getHseq().charAt(i))) {
                    if (reverse_dict.get(hsp.getQseq().charAt(i)) == reverse_dict.get((hsp.getHseq().charAt(i)))) {

                        queryRectangle.setFill(Paint.valueOf("#FF6F61"));
                        hitRectangle.setFill(Paint.valueOf("#FF6F61"));

                        symbolText = new Text("\u2731");
                        FontWeight fontWeight2 = FontWeight.BOLD;
                        symbolText.setFont(Font.font("monospace", fontWeight2, 11));

                        if (reverse_dict.get(hsp.getQseq().charAt(i)).equals("red")) {
                            queryRectangle.setFill(Paint.valueOf("#CD5C5C"));
                            hitRectangle.setFill(Paint.valueOf("#CD5C5C"));
                        } else if (reverse_dict.get(hsp.getQseq().charAt(i)).equals("blue")) {
                            queryRectangle.setFill(Paint.valueOf("#6A5ACD"));
                            hitRectangle.setFill(Paint.valueOf("#6A5ACD"));
                        } else if (reverse_dict.get(hsp.getQseq().charAt(i)).equals("magenta")) {
                            queryRectangle.setFill(Paint.valueOf("#BA55D3"));
                            hitRectangle.setFill(Paint.valueOf("#BA55D3"));
                        } else if (reverse_dict.get(hsp.getQseq().charAt(i)).equals("green")) {
                            queryRectangle.setFill(Paint.valueOf("#6B8E23"));
                            hitRectangle.setFill(Paint.valueOf("#6B8E23"));
                        }

                    } else {
                        queryRectangle.setFill(Paint.valueOf("#DCDCDC"));
                        hitRectangle.setFill(Paint.valueOf("#DCDCDC"));
                        symbolText = new Text(" ");
                    }

                    symbolRectangle.setFill(Paint.valueOf("#481a57"));

                } else {
                    queryRectangle.setFill(Paint.valueOf("#DCDCDC"));
                    hitRectangle.setFill(Paint.valueOf("#DCDCDC"));
                    symbolText = new Text(" ");

                }

                queryNucleotide.setX(i * queryNucleotide.getLayoutBounds().getWidth());
                if (symbolText.getText().contains("I")) {
                    symbolText.setX(i * queryNucleotide.getLayoutBounds().getWidth() + ((queryNucleotide.getLayoutBounds().getWidth()) / 2) - 1.5);
                } else {
                    symbolText.setX(i * queryNucleotide.getLayoutBounds().getWidth() + ((queryNucleotide.getLayoutBounds().getWidth()) / 2) - 5);
                }

                hitNucleotide.setX(i * hitNucleotide.getLayoutBounds().getWidth());


                queryRectangle.setX(i * queryNucleotide.getLayoutBounds().getWidth());
                symbolRectangle.setX(i * queryRectangle.getLayoutBounds().getWidth());
                hitRectangle.setX(i * hitNucleotide.getLayoutBounds().getWidth());

                queryNucleotide.setY(queryNucleotide.getLayoutBounds().getHeight());
                symbolText.setY(2 * queryNucleotide.getLayoutBounds().getHeight() - 10);
                hitNucleotide.setY(3 * queryNucleotide.getLayoutBounds().getHeight() - 8);

                queryRectangle.setY(0);
                symbolRectangle.setY(queryRectangle.getLayoutBounds().getHeight() - 10);
                hitRectangle.setY(2 * queryRectangle.getLayoutBounds().getHeight() - 8);


                pane.getChildren().add(queryRectangle);
                pane.getChildren().add(symbolRectangle);
                pane.getChildren().add(hitRectangle);

                pane.getChildren().add(queryNucleotide);
                pane.getChildren().add(symbolText);
                pane.getChildren().add(hitNucleotide);
            }

            HashMap<String, ArrayList<String>> peptideRuns = new HashMap<>();
            for(Peptide peptide: proteinPeptides){
                if(!peptideRuns.containsKey(peptide.getSequence())){
                    peptideRuns.put(peptide.getSequence(), new ArrayList<>());
                }
                peptideRuns.get(peptide.getSequence()).add(peptide.getRunName());
            }


            for(Peptide peptide: proteinPeptides){
                int index = hsp.getQseq().replace("-", "").indexOf(peptide.getSequence());
                if(index!=-1){
                    int alignedIndex = 0;
                    int aaCount=0;
                    while(aaCount<index){
                        if(hsp.getQseq().charAt(alignedIndex)!='-'){
                            aaCount++;
                        }
                        alignedIndex++;
                    }


                    int peptideAAcount = 0;
                    int alignedPeptideLength = 0;
                    int i = 0;
                    while(peptideAAcount<peptide.getSequence().length()){
                        if(hsp.getQseq().charAt(alignedIndex+i)!='-'){
                            peptideAAcount++;
                        }
                        alignedPeptideLength++;
                        i++;
                    }

                    Rectangle peptideRectangle = new Rectangle();
                    Text peptideAA = new Text(String.valueOf(peptide.getSequence().charAt(0)));
                    FontWeight fontWeight = FontWeight.SEMI_BOLD;
                    peptideAA.setFont(Font.font("monospace", fontWeight, 20));
                    peptideRectangle.setWidth(peptideAA.getLayoutBounds().getWidth()*alignedPeptideLength);
                    peptideRectangle.setX(alignedIndex*peptideAA.getLayoutBounds().getWidth());
                    peptideRectangle.setHeight(peptideAA.getLayoutBounds().getHeight());
                    peptideRectangle.setY(3*peptideAA.getLayoutBounds().getHeight());
                    peptideRectangle.setFill(Color.YELLOW);
                    pane.getChildren().add(peptideRectangle);
                    Text peptideLabel = new Text("Peptide");
                    peptideLabel.setFont(Font.font("monospace", fontWeight, 20));
                    peptideLabel.setX(alignedIndex*peptideAA.getLayoutBounds().getWidth()+peptideRectangle.getWidth()/2-peptideLabel.getLayoutBounds().getWidth()/2);
                    peptideLabel.setY(3*peptideAA.getLayoutBounds().getHeight()+peptideLabel.getLayoutBounds().getHeight()-5);
                    pane.getChildren().add(peptideLabel);
                    addPeptideClickEvents(peptideRectangle, peptideLabel, peptide, peptideRuns);


                }
            }


            seqAlignmentPane.setContent(pane);
            seqAlignmentPane.setPrefWidth(width);
            seqAlignmentPane.setLayoutY(200);
            alignmentPane.getChildren().add(seqAlignmentPane);


        }

        private void drawNovelSequence(String proteinId){

            alignmentPane.getChildren().remove(seqAlignmentPane);
            seqAlignmentPane = new ScrollPane();
            seqAlignmentPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            Text t1 = new Text("T");
            t1.setFont(Font.font("monospace", FontWeight.SEMI_BOLD, 20));
            seqAlignmentPane.setMinHeight((t1.getLayoutBounds().getHeight()+10)*4);

            System.out.println(proteinId.split(".p")[0]);
            Document doc = Database.getDb().getCollection("allTranscripts")
                    .find(eq("transcriptID", proteinId.split(".p")[0])).firstOrDefault();
            JSONObject allCds = (JSONObject) doc.get("CDS");
            JSONObject cdsObj = (JSONObject) allCds.get(proteinId);

            String seq = (String) cdsObj.get("sequence");
            JSONArray peptides=null;
            if(cdsObj.containsKey("peptides")){
                peptides = (JSONArray) cdsObj.get("peptides");
            }



            Pane pane = new Pane();
            for (int i = 0; i < seq.length(); i++) {
                Text t = new Text(String.valueOf(seq.charAt(i)));
                t.setFont(Font.font("monospace", FontWeight.SEMI_BOLD, 20));
                t.setX(i*t.getLayoutBounds().getWidth());
                t.setY(t.getLayoutBounds().getHeight());
                pane.getChildren().add(t);
            }
            if(peptides!=null){


                HashMap<String, ArrayList<String>> peptideRuns = new HashMap<>();
                for(Object o: peptides){
                    JSONObject peptide = (JSONObject) o;
                    String peptideSeq = (String) peptide.get("sequence");
                    String peptideRun = (String) peptide.get("run");
                    if(!peptideRuns.containsKey(peptideSeq)){
                        peptideRuns.put(peptideSeq, new ArrayList<>());
                    }
                    peptideRuns.get(peptideSeq).add(peptideRun);
                }


                for(Object o: peptides){
                    JSONObject p = (JSONObject) o;
                    String peptideSeq = (String) p.get("sequence");
                    Peptide peptide = new Peptide(peptideSeq, new MSRun((String) p.get("run")));
                    int index = seq.indexOf(peptideSeq);
                    if(index!=-1){
                        Text t = new Text(String.valueOf(seq.charAt(0)));
                        Rectangle r = new Rectangle();
                        r.setX(index*t.getLayoutBounds().getWidth());
                        r.setWidth(peptideSeq.length()*t.getLayoutBounds().getWidth());
                        r.setHeight(t.getLayoutBounds().getHeight()+10);
                        r.setY(t.getLayoutBounds().getHeight()+20);
                        r.setFill(Color.YELLOW);
                        pane.getChildren().add(r);
                        Text peptideLabel = new Text("Peptide");
                        peptideLabel.setFont(Font.font("monospace", FontWeight.SEMI_BOLD, 20));
                        peptideLabel.setX(index*t.getLayoutBounds().getWidth()+r.getWidth()/2-peptideLabel.getLayoutBounds().getWidth()/2);
                        peptideLabel.setY(peptideLabel.getLayoutBounds().getHeight()+peptideLabel.getLayoutBounds().getHeight());
                        pane.getChildren().add(peptideLabel);
                        addPeptideClickEvents(r, peptideLabel, peptide, peptideRuns);
                    }
                }
            }

            seqAlignmentPane.setContent(pane);
            alignmentPane.getChildren().add(seqAlignmentPane);

        }

        public void addPeptideClickEvents(Rectangle rectangle, Text peptideLabel,  Peptide peptide, HashMap<String, ArrayList<String>> peptideRuns){


            EventHandler<MouseEvent> eventHandler = mouseEvent -> {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {

                    if(peptideRuns.get(peptide.getSequence()).size()>1){
                        ChoiceDialog<String> choiceDialog = new ChoiceDialog<>(peptideRuns.get(peptide.getSequence()).get(0),
                                (peptideRuns.get(peptide.getSequence())));
                        choiceDialog.showAndWait();
                        PeptideTableController.getInstance().findPeptideInTable(peptide.getSequence(), choiceDialog.getSelectedItem());
                    }else
                        PeptideTableController.getInstance().findPeptideInTable(peptide.getSequence(), peptide.getRunName());

                    ResultsController.getInstance().showPeptideTab(peptide);

                }
            };
            rectangle.setOnMouseClicked(eventHandler);
            peptideLabel.setOnMouseClicked(eventHandler);

        }


        public static class Hit {
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

            public void addHsp(Hsp hsp) {
                hsps.add(hsp);
            }

            public ArrayList<Hsp> getHsps() {
                return hsps;
            }

            public Integer getNoOfHsps() {
                Integer n = hsps.size();
                return n;
            }

            public int getQueryLength() {
                return queryLength;
            }

            public double getEValue() {
                double minEval = 1.;
                for (Hsp hsp : hsps) {
                    if (hsp.getEvalue() < minEval) {
                        minEval = hsp.getEvalue();
                    }
                }
                return minEval;
            }

            public double getQueryCoverage() {

                int lengthCoveredByHsp = 0;
                for (Hsp hsp : hsps) {
                    lengthCoveredByHsp += hsp.getQueryTo() - hsp.getQueryFrom() + 1;
                }
                return (double) lengthCoveredByHsp / queryLength;
            }

            public double getHitCoverage() {
                int lengthCoveredByHsp = 0;
                for (Hsp hsp : hsps) {
                    lengthCoveredByHsp += hsp.getHitTo() - hsp.getHitFrom() + 1;
                }
                return (double) lengthCoveredByHsp / length;
            }

            public String getSpecies() {
                Pattern pattern = Pattern.compile("(?:PREDICTED: )*([a-zA-Z]+\\s[a-zA-Z]+).*");
                Matcher m = pattern.matcher(definition);
                if (m.find()) {
                    return m.group(1);
                }
                return null;
            }

            public boolean isPredicted() {
                return definition.contains("PREDICTED");
            }
        }

        public static class Hsp {
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

            public int getQueryFrom() {
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

        private void getNoOfQueries () {
            Integer number = proteinTable.getItems().size();
            noOfQueries.setText(number.toString());
        }

        public class ProteinTableRow{
            private final String protein;
            private final double evalue;

            public ProteinTableRow(String protein, double evalue) {
                this.protein = protein;
                this.evalue = evalue;
            }

            public String getProtein() {
                return protein;
            }

            public double getEvalue() {
                return evalue;
            }
        }
}










