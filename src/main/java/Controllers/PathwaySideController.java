package Controllers;

import Singletons.Config;
import com.jfoenix.controls.JFXCheckBox;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Pair;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.RangeSlider;
import org.json.JSONArray;
import org.json.JSONObject;
import pathway.Element;
import pathway.Entity;
import pathway.SearchResult;
import pathway.alerts.DgeAlert;
import pathway.alerts.MutationAlert;
import pathway.alerts.PTMAlert;
import pathway.alerts.SplicingAlert;
import pitguiv2.App;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class PathwaySideController implements Initializable {

    @FXML
    private VBox descriptionContainer;
    @FXML
    private ComboBox<String> ptmMoreLessCombobox;
    @FXML
    private ComboBox<String> proteinMoreLessCombobox;
    @FXML
    private ComboBox<String> splicingMoreLessCombobox;
    @FXML
    private ComboBox<String> geneMoreLessCombobox;
    @FXML
    private Spinner<Double> proteinFcSpinner;
    @FXML
    private Spinner<Double> proteinPvalSpinner;
    @FXML
    private CheckBox proteinAbsoluteFcCheckbox;
    @FXML
    private Spinner<Double> ptmPvalSpinner;
    @FXML
    private Spinner<Double> ptmFcSpinner;
    @FXML
    private CheckBox ptmAbsoluteFcCheckbox;
    @FXML
    private GridPane filtersGrid;
    @FXML
    private Spinner<Double> genePvalSpinner;
    @FXML
    private ComboBox<String> msRunCombobox;
    @FXML
    private Spinner<Double> geneFcSpinner;
    @FXML
    private CheckBox geneAbsoluteFcCheckbox;
    @FXML
    private CheckBox splicingAbsoluteDpsiCheckbox;
    @FXML
    private Spinner<Double> splicingPvalSpinner;
    @FXML
    private Spinner<Double> splicingDpsiSpinner;
    @FXML
    private CheckBox splicingPFAMCheckbox;
    @FXML
    private CheckBox inCDSCheckbox;
    @FXML
    private AnchorPane PTMPane;
    @FXML
    private AnchorPane mutationPane;
    @FXML
    private AnchorPane splicingPane;
    @FXML
    private AnchorPane dgePane;
    @FXML
    private ComboBox<String> colorbyCombobox;
    @FXML
    private TabPane tabPane;
    @FXML
    private javafx.scene.control.Label summationField;
    @FXML
    private ListView<String> pathwayListview;
    @FXML
    private ListView<String> reactionListview;
    @FXML
    private GridPane gridPane;
    @FXML
    private TextField searchField;
    @FXML
    private VBox infoSelectionBox;
    private CheckComboBox<String> ptmTypesBox;

    private HashMap<String, SearchResult> searchPathways;
    private HashMap<String, SearchResult> searchReactions;
    public static PathwaySideController instance;

    ArrayList<JFXCheckBox> conditionFilterCheckboxes;
    ArrayList<RangeSlider> minSamplesConditionFilterSliders;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;

        colorbyCombobox.setItems(FXCollections.observableArrayList("RNA DGE", "Differencial protein abundance", "Splicing", "Phosphorylation"));
        colorbyCombobox.getSelectionModel().select(0);

        colorbyCombobox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            PathwayController.getInstance().colorElements();
        });

        pathwayListview.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                //summationField.setText(searchPathways.get(pathwayListview.getSelectionModel().getSelectedItem()).getSummation());
                new Thread(()-> PathwayController.getInstance().loadPathway(searchPathways.get(pathwayListview.getSelectionModel().getSelectedItem()).getId(), null)).start();
                PathwayTreeController.getInstance().expendItem(searchPathways.get(pathwayListview.getSelectionModel().getSelectedItem()).getId());
            }
        });
        reactionListview.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                //summationField.setText(searchReactions.get(reactionListview.getSelectionModel().getSelectedItem()).getSummation());
                PathwayController.getInstance().loadReaction(reactionListview.getSelectionModel().getSelectedItem());
            }
        });

        splicingMoreLessCombobox.setItems(FXCollections.observableArrayList(">", "<"));
        ptmMoreLessCombobox.setItems(FXCollections.observableArrayList(">", "<"));
        geneMoreLessCombobox.setItems(FXCollections.observableArrayList(">", "<"));
        proteinMoreLessCombobox.setItems(FXCollections.observableArrayList(">", "<"));

        splicingMoreLessCombobox.getSelectionModel().select(0);
        ptmMoreLessCombobox.getSelectionModel().select(0);
        geneMoreLessCombobox.getSelectionModel().select(0);
        proteinMoreLessCombobox.getSelectionModel().select(0);



        msRunCombobox.getItems().addAll(Config.getRuns());
        msRunCombobox.getSelectionModel().select(0);

        ptmTypesBox = new CheckComboBox<>();
        ptmTypesBox.getItems().add("Phosphorylation");
        ptmTypesBox.getItems().add("Oxidation");
        filtersGrid.add(ptmTypesBox, 1, 20);

        drawSampleFilterSliders();


        genePvalSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1, 0.05, 0.01));
        proteinPvalSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1, 0.05, 0.01));
        splicingPvalSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1, 0.05, 0.01));
        ptmPvalSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1, 0.05, 0.01));

        geneFcSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(Double.MIN_VALUE, Double.MAX_VALUE, 0, 0.01));
        proteinFcSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(Double.MIN_VALUE, Double.MAX_VALUE, 0, 0.01));
        splicingDpsiSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1, 0, 0.01));
        ptmFcSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(Double.MIN_VALUE, Double.MAX_VALUE, 0, 0.01));


        genePvalSpinner.valueProperty().addListener((obs, oldValue, newValue) -> refreshAlerts());
        geneFcSpinner.valueProperty().addListener((obs, oldValue, newValue) -> refreshAlerts());
        proteinPvalSpinner.valueProperty().addListener((obs, oldValue, newValue) -> refreshAlerts());
        proteinFcSpinner.valueProperty().addListener((obs, oldValue, newValue) -> refreshAlerts());
        splicingPvalSpinner.valueProperty().addListener((obs, oldValue, newValue) -> refreshAlerts());
        splicingDpsiSpinner.valueProperty().addListener((obs, oldValue, newValue) -> refreshAlerts());
        ptmPvalSpinner.valueProperty().addListener((obs, oldValue, newValue) -> refreshAlerts());
        ptmFcSpinner.valueProperty().addListener((obs, oldValue, newValue) -> refreshAlerts());

        geneMoreLessCombobox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> refreshAlerts());
        proteinMoreLessCombobox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> refreshAlerts());
        splicingMoreLessCombobox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> refreshAlerts());
        ptmMoreLessCombobox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> refreshAlerts());

        geneAbsoluteFcCheckbox.selectedProperty().addListener((obs, oldValue, newValue) -> refreshAlerts());
        proteinAbsoluteFcCheckbox.selectedProperty().addListener((obs, oldValue, newValue) -> refreshAlerts());
        splicingAbsoluteDpsiCheckbox.selectedProperty().addListener((obs, oldValue, newValue) -> refreshAlerts());
        ptmAbsoluteFcCheckbox.selectedProperty().addListener((obs, oldValue, newValue) -> refreshAlerts());

        splicingPFAMCheckbox.selectedProperty().addListener((obs, oldValue, newValue) -> refreshAlerts());

        inCDSCheckbox.selectedProperty().addListener((obs, oldValue, newValue) -> refreshAlerts());
        ptmTypesBox.checkModelProperty().addListener((obs, oldValue, newValue) -> refreshAlerts());




    }

    public static PathwaySideController getInstance() {
        return instance;
    }

    public void search() {

        searchPathways = new HashMap<>();
        searchReactions = new HashMap<>();
        reactionListview.getItems().clear();
        pathwayListview.getItems().clear();

        final HashSet<String> geneProductsIds = new HashSet<>();

        try{
            URL yahoo = new URL("https://reactome.org/ContentService/search/query?query="+searchField.getText().replace(" ", "%20"));
            URLConnection yc = yahoo.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            yc.getInputStream()));
            String inputLine="";
            JSONObject object=null;
            while ((inputLine = in.readLine()) != null) {

                object = new JSONObject(inputLine);
            }

            for(Object o: object.getJSONArray("results")){
                JSONObject result = (JSONObject) o;
                for(Object o2: result.getJSONArray("entries")){
                    JSONObject entry = (JSONObject) o2;


                    if(entry.getJSONArray("species").getString(0).equals("Homo sapiens")){

                        SearchResult searchResult = new SearchResult(entry.getString("exactType"), entry.getString("stId"),
                                entry.getString("name").replaceAll("<.*?>", ""),
                                entry.has("summation")?entry.getString("summation").replaceAll("<.*?>", ""):"");


                        if(searchResult.getType().equals("Pathway")) {
                            searchPathways.put(searchResult.getName(), searchResult);
                            pathwayListview.getItems().add(searchResult.getName());
                        }
                        else if(searchResult.getType().equals("Reaction") || searchResult.getType().equals("BlackBoxEvent")) {
                            searchReactions.put(searchResult.getName(), searchResult);
                            reactionListview.getItems().add(searchResult.getName());
                        }else if(searchResult.getType().equals("ReferenceGeneProduct")) {
                            geneProductsIds.add(entry.getString("stId"));
                        }
                    }
                }
            }

            for(String entity: geneProductsIds){
                searchEntityPathways(entity);
            }


            in.close();


        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void searchEntityPathways(String entityId){
        try{
            URL yahoo = new URL("https://reactome.org/ContentService/data/pathways/low/entity/"+entityId);
            URLConnection yc = yahoo.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            yc.getInputStream()));
            String inputLine="";
            JSONArray results=null;
            while ((inputLine = in.readLine()) != null) {

                results = new JSONArray(inputLine);
            }

            for(Object o: results){
                JSONObject result = (JSONObject) o;
                SearchResult searchResult = new SearchResult("Pathway", result.getString("stId"),
                        result.getString("displayName"));
                searchPathways.put(searchResult.getName(), searchResult);
                if(!pathwayListview.getItems().contains(searchResult.getName())) {
                    Platform.runLater(()->{
                        pathwayListview.getItems().add(searchResult.getName());
                    });
                }
            }
            in.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void populateSelectionTable(Element element){

        pathway.alerts.Alert.populateGenes(dgePane, element, DgeAlert.class);
        pathway.alerts.Alert.populateGenes(splicingPane, element, SplicingAlert.class);
        pathway.alerts.Alert.populateGenes(mutationPane, element, MutationAlert.class);
        pathway.alerts.Alert.populateGenes(PTMPane, element, PTMAlert.class);
        tabPane.getSelectionModel().select(1);


    }

    public HashMap<String, SearchResult> getSearchReactions() {
        return searchReactions;
    }

    public void showGeneDetails(String id){

        infoSelectionBox.getChildren().clear();

        try{
            URL url = new URL("https://reactome.org/ContentService/data/query/"+id);
            URLConnection yc = url.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            yc.getInputStream()));
            String inputLine="";
            JSONObject object=null;
            while ((inputLine = in.readLine()) != null) {
                object = new JSONObject(inputLine);
            }

            if(object.getString("className").equals("Protein")){
                Hyperlink link = new Hyperlink();
                link.setText(object.getJSONObject("referenceEntity").getString("displayName"));
                JSONObject finalObject = object;
                link.setOnAction(e -> {
                    App.getApp().getHostServices().showDocument(finalObject.getJSONObject("referenceEntity").getString("url"));
                });

                infoSelectionBox.getChildren().add(link);
            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public String getSelectedColorVariable(){
        return colorbyCombobox.getSelectionModel().getSelectedItem();
    }


    public void refreshAlerts(){

        PathwaysFilters.setGenePval(genePvalSpinner.getValue());
        PathwaysFilters.setGeneLog2Fc(geneFcSpinner.getValue());
        PathwaysFilters.setGeneAbsFc(geneAbsoluteFcCheckbox.isSelected());
        PathwaysFilters.setGeneComparisonSide(geneMoreLessCombobox.getSelectionModel().getSelectedItem().equals(">")?ComparisonSide.MORE:ComparisonSide.LESS);

        PathwaysFilters.setSplicingPVal(splicingPvalSpinner.getValue());
        PathwaysFilters.setSplicingDpsi(splicingDpsiSpinner.getValue());
        PathwaysFilters.setSplicingPFAM(splicingPFAMCheckbox.isSelected());
        PathwaysFilters.setAbsDpsi(splicingAbsoluteDpsiCheckbox.isSelected());
        PathwaysFilters.setSplicingComparisonSide(splicingMoreLessCombobox.getSelectionModel().getSelectedItem().equals(">")?ComparisonSide.MORE:ComparisonSide.LESS);

        PathwaysFilters.setMsRun(msRunCombobox.getSelectionModel().getSelectedItem());
        PathwaysFilters.setProteinPval(proteinPvalSpinner.getValue());
        PathwaysFilters.setProteinLog2fc(proteinFcSpinner.getValue());
        PathwaysFilters.setProteinComparisonSide(proteinMoreLessCombobox.getSelectionModel().getSelectedItem().equals(">")?ComparisonSide.MORE:ComparisonSide.LESS);

        PathwaysFilters.setPtmPval(ptmPvalSpinner.getValue());
        PathwaysFilters.setPtmLog2Fc(ptmFcSpinner.getValue());
        PathwaysFilters.setPtmTypes(ptmTypesBox.getItems());
        PathwaysFilters.setAbsDpsi(ptmAbsoluteFcCheckbox.isSelected());
        PathwaysFilters.setPtmComparisonSide(ptmMoreLessCombobox.getSelectionModel().getSelectedItem().equals(">")?ComparisonSide.MORE:ComparisonSide.LESS);



        HashMap<String, Pair<Integer, Integer>> mutationsSamples = new HashMap<>();
        for (int i = 0; i < conditionFilterCheckboxes.size(); i++) {
            if(conditionFilterCheckboxes.get(i).isSelected()){
                mutationsSamples.put(conditionFilterCheckboxes.get(i).getText(), new Pair<>((int) minSamplesConditionFilterSliders.get(i).getMin(), (int) minSamplesConditionFilterSliders.get(i).getMax()));
            }
        }
        PathwaysFilters.setMutationsSamples(mutationsSamples);
        PathwaysFilters.setInCDSMutation(inCDSCheckbox.isSelected());

        PathwayController.getInstance().refreshAlerts();
    }

    private void drawSampleFilterSliders(){
        Set<String> conditions = Config.getConditions();

        conditionFilterCheckboxes = new ArrayList<>();
        minSamplesConditionFilterSliders = new ArrayList<>();

        VBox container = new VBox();

        // add chechboxes for the conditions

        HashMap<String, ArrayList<String>> patients = Config.getPatientsGroups();
        if(patients!=null){
            for (Map.Entry<String, ArrayList<String>> entry : patients.entrySet()) {

                JFXCheckBox conditionEnabledCheckbox = new JFXCheckBox();
                conditionEnabledCheckbox.setText(entry.getKey());
                conditionEnabledCheckbox.setSelected(true);

//            conditionEnabledCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> filterTable());


                conditionFilterCheckboxes.add(conditionEnabledCheckbox);

                int nbSamples = entry.getValue().size();
                RangeSlider slider = new RangeSlider();
                slider.setMax(nbSamples);
                slider.setLowValue(0);
                slider.setHighValue(nbSamples);
                slider.setMajorTickUnit(1);
                slider.setShowTickLabels(true);
                slider.setShowTickMarks(true);
                slider.setSnapToTicks(true);


                slider.lowValueProperty().addListener((obs, oldval, newVal) -> slider.setLowValue(Math.round(newVal.doubleValue())));
                slider.highValueProperty().addListener((obs, oldval, newVal) -> slider.setHighValue(Math.round(newVal.doubleValue())));
                conditionEnabledCheckbox.selectedProperty().addListener((obs, oldval, newVal) -> {
                    slider.setDisable(!newVal);
                });

                minSamplesConditionFilterSliders.add(slider);

                container.getChildren().add(conditionEnabledCheckbox);
                container.getChildren().add(slider);


            }
        }else {


            for (String condition : conditions) {

                JFXCheckBox conditionEnabledCheckbox = new JFXCheckBox();
                conditionEnabledCheckbox.setText(condition);
                conditionEnabledCheckbox.setSelected(true);

//            conditionEnabledCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> filterTable());


                conditionFilterCheckboxes.add(conditionEnabledCheckbox);

                int nbSamples = Config.getSamplesInCondition(condition).size();
                RangeSlider slider = new RangeSlider();
                slider.setMax(nbSamples);
                slider.setLowValue(0);
                slider.setHighValue(nbSamples);
                slider.setMajorTickUnit(1);
                slider.setShowTickLabels(true);
                slider.setShowTickMarks(true);
                slider.setSnapToTicks(true);


                slider.lowValueProperty().addListener((obs, oldval, newVal) -> slider.setLowValue(Math.round(newVal.doubleValue())));
                slider.highValueProperty().addListener((obs, oldval, newVal) -> slider.setHighValue(Math.round(newVal.doubleValue())));
                conditionEnabledCheckbox.selectedProperty().addListener((obs, oldval, newVal) -> {
                    slider.setDisable(!newVal);
                });

                minSamplesConditionFilterSliders.add(slider);

                container.getChildren().add(conditionEnabledCheckbox);
                container.getChildren().add(slider);



            }
        }
        filtersGrid.add(container, 0, 15, 3, 1);

    }

    public void extrernalSearch(String gene){
        searchField.setText(gene);
        tabPane.getSelectionModel().select(0);
        PathwayController.getInstance().clear();
        search();
    }

    public void selectTab(int index){
        tabPane.getSelectionModel().select(index);
    }

    public void showDescription(String id, Element element) {
        descriptionContainer.getChildren().clear();
        try {
            URL yahoo = new URL("https://reactome.org/ContentService/data/query/" + id);
            URLConnection yc = yahoo.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            yc.getInputStream()));
            JSONObject res = new JSONObject(in.readLine());

            if(res.getString("className").equals("Reaction")){
                Label nameLabel = new Label(res.getString("displayName"));
                nameLabel.setWrapText(true);
                nameLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 30));
                Label descriptionLabel = new Label(res.getJSONArray("summation").getJSONObject(0).getString("text"));
                descriptionLabel.setWrapText(true);

                descriptionContainer.getChildren().add(nameLabel);
                descriptionContainer.getChildren().add(descriptionLabel);

            }else if(res.getString("className").equals("Complex")){

                Label nameLabel = new Label(res.getString("displayName"));
                nameLabel.setWrapText(true);
                nameLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 30));

                TableView<DescriptionTableRow> table = new TableView<>();
                TableColumn<DescriptionTableRow, String> nameColumn = new TableColumn<>("Name");
                TableColumn<DescriptionTableRow, String> typeColumn = new TableColumn<>("Type");

                nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
                typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
                nameColumn.prefWidthProperty().bind(table.widthProperty().divide(2));
                typeColumn.prefWidthProperty().bind(table.widthProperty().divide(2));

                table.getColumns().add(nameColumn);
                table.getColumns().add(typeColumn);

                for(Object o: res.getJSONArray("hasComponent")){
                    JSONObject component = (JSONObject) o;
                    table.getItems().add(new DescriptionTableRow(component.getJSONArray("name").getString(0), component.getString("className")));
                }



                for(Object o: res.getJSONArray("hasComponent")){
                    JSONObject component = (JSONObject) o;
                    table.getItems().add(new DescriptionTableRow(component.getJSONArray("name").getString(0), component.getString("className")));
                }

                descriptionContainer.getChildren().add(table);

                ListView<String> entitiesList = new ListView<>();
                VBox geneInfoContainer = new VBox();
                HashMap<String, String> entitiesIdMap = new HashMap<>();
                for(Entity entity: element.getEntities()){
                    entitiesList.getItems().add(entity.getName());
                    entitiesIdMap.put(entity.getName(), entity.getId());
                }
                descriptionContainer.getChildren().add(entitiesList);
                descriptionContainer.getChildren().add(geneInfoContainer);

                entitiesList.setOnMouseClicked(event -> fillGeneInfo(entitiesIdMap.get(entitiesList.getSelectionModel().getSelectedItem()), geneInfoContainer));
            }

            if(res.has("literatureReference")){
                for(Object o: res.getJSONArray("literatureReference")){
                    JSONObject ref = (JSONObject) o;
                    Hyperlink link = new Hyperlink(ref.getString("displayName")+ " ("+ref.getString("journal")+" "+ ref.getInt("year") +")");
                    link.setWrapText(true);
                    link.setOnAction(t -> App.getApp().getHostServices().showDocument(ref.getString("url")));
                    descriptionContainer.getChildren().add(link);
                }
            }

            tabPane.getSelectionModel().select(3);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fillGeneInfo(String id, VBox container){
        container.getChildren().clear();
        try {
            URL yahoo = new URL("https://reactome.org/ContentService/data/query/" + id);
            URLConnection yc = yahoo.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            yc.getInputStream()));
            JSONObject res = new JSONObject(in.readLine());
            if(res.getString("className").equals("ReferenceGeneProduct")){
                Hyperlink link = new Hyperlink(res.getString("identifier"));
                link.setWrapText(true);
                link.setOnAction(t -> App.getApp().getHostServices().showDocument("https://www.uniprot.org/uniprot/"+res.getString("identifier")));
                container.getChildren().add(link);

                Label descriptionLabel = new Label(res.getJSONArray("comment").getString(0));
                descriptionLabel.setWrapText(true);
                container.getChildren().add(descriptionLabel);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public enum ComparisonSide {
        LESS, MORE
    }

    public static class PathwaysFilters{

        private static double genePval = 0.05;
        private static double geneLog2Fc;
        private static boolean geneAbsFc = true;
        private static ComparisonSide geneComparisonSide = ComparisonSide.MORE;

        private static String msRun;
        private static double proteinPval = 0.05;
        private static double proteinLog2fc;
        private static boolean proteinAbsFc = true;
        private static ComparisonSide proteinComparisonSide = ComparisonSide.MORE;

        private static double splicingPVal = 0.05;
        private static double splicingDpsi;
        private static boolean splicingAbsFc = true;
        private static ComparisonSide splicingComparisonSide = ComparisonSide.MORE;
        private static boolean absDpsi;
        private static boolean splicingPFAM;

        private static boolean inCDSMutation;
        private static HashMap<String, Pair<Integer, Integer>> mutationsSamples;

        private static ObservableList<String> ptmTypes;
        private static double ptmPval = 0.05;
        private static double ptmLog2Fc;
        private static boolean ptmAbsFc = true;
        private static ComparisonSide ptmComparisonSide = ComparisonSide.MORE;



        public static double getGenePval() {
            return genePval;
        }

        public static void setGenePval(double genePval) {
            PathwaysFilters.genePval = genePval;
        }

        public static double getGeneLog2Fc() {
            return geneLog2Fc;
        }

        public static void setGeneLog2Fc(double geneLog2Fc) {
            PathwaysFilters.geneLog2Fc = geneLog2Fc;
        }

        public static boolean isGeneAbsFc() {
            return geneAbsFc;
        }

        public static void setAbsFc(boolean geneAbsFc) {
            PathwaysFilters.geneAbsFc = geneAbsFc;
        }

        public static String getMsRun() {
            return msRun;
        }

        public static void setMsRun(String msRun) {
            PathwaysFilters.msRun = msRun;
        }

        public static double getSplicingPVal() {
            return splicingPVal;
        }

        public static void setSplicingPVal(double splicingPVal) {
            PathwaysFilters.splicingPVal = splicingPVal;
        }

        public static double getSplicingDpsi() {
            return splicingDpsi;
        }

        public static void setSplicingDpsi(double splicingDpsi) {
            PathwaysFilters.splicingDpsi = splicingDpsi;
        }

        public static boolean isAbsDpsi() {
            return absDpsi;
        }

        public static void setAbsDpsi(boolean absDpsi) {
            PathwaysFilters.absDpsi = absDpsi;
        }

        public static boolean isSplicingPFAM() {
            return splicingPFAM;
        }

        public static void setSplicingPFAM(boolean splicingPFAM) {
            PathwaysFilters.splicingPFAM = splicingPFAM;
        }

        public static boolean isInCDSMutation() {
            return inCDSMutation;
        }

        public static void setInCDSMutation(boolean inCDSMutation) {
            PathwaysFilters.inCDSMutation = inCDSMutation;
        }

        public static HashMap<String, Pair<Integer, Integer>> getMutationsSamples() {
            return mutationsSamples;
        }

        public static void setMutationsSamples(HashMap<String, Pair<Integer, Integer>> mutationsSamples) {
            PathwaysFilters.mutationsSamples = mutationsSamples;
        }

        public static double getProteinPval() {
            return proteinPval;
        }

        public static void setProteinPval(double proteinPval) {
            PathwaysFilters.proteinPval = proteinPval;
        }

        public static double getProteinLog2fc() {
            return proteinLog2fc;
        }

        public static void setProteinLog2fc(double proteinLog2fc) {
            PathwaysFilters.proteinLog2fc = proteinLog2fc;
        }


        public static ObservableList<String> getPtmTypes() {
            return ptmTypes;
        }

        public static void setPtmTypes(ObservableList<String> ptmTypes) {
            PathwaysFilters.ptmTypes = ptmTypes;
        }

        public static double getPtmPval() {
            return ptmPval;
        }

        public static void setPtmPval(double ptmPval) {
            PathwaysFilters.ptmPval = ptmPval;
        }

        public static double getPtmLog2Fc() {
            return ptmLog2Fc;
        }

        public static void setPtmLog2Fc(double ptmLog2Fc) {
            PathwaysFilters.ptmLog2Fc = ptmLog2Fc;
        }

        public static void setGeneAbsFc(boolean geneAbsFc) {
            PathwaysFilters.geneAbsFc = geneAbsFc;
        }

        public static boolean isProteinAbsFc() {
            return proteinAbsFc;
        }

        public static void setProteinAbsFc(boolean proteinAbsFc) {
            PathwaysFilters.proteinAbsFc = proteinAbsFc;
        }

        public static boolean isSplicingAbsFc() {
            return splicingAbsFc;
        }

        public static void setSplicingAbsFc(boolean splicingAbsFc) {
            PathwaysFilters.splicingAbsFc = splicingAbsFc;
        }

        public static boolean isPtmAbsFc() {
            return ptmAbsFc;
        }

        public static void setPtmAbsFc(boolean ptmAbsFc) {
            PathwaysFilters.ptmAbsFc = ptmAbsFc;
        }

        public static ComparisonSide getGeneComparisonSide() {
            return geneComparisonSide;
        }

        public static void setGeneComparisonSide(ComparisonSide geneComparisonSide) {
            PathwaysFilters.geneComparisonSide = geneComparisonSide;
        }

        public static ComparisonSide getProteinComparisonSide() {
            return proteinComparisonSide;
        }

        public static void setProteinComparisonSide(ComparisonSide proteinComparisonSide) {
            PathwaysFilters.proteinComparisonSide = proteinComparisonSide;
        }

        public static ComparisonSide getSplicingComparisonSide() {
            return splicingComparisonSide;
        }

        public static void setSplicingComparisonSide(ComparisonSide splicingComparisonSide) {
            PathwaysFilters.splicingComparisonSide = splicingComparisonSide;
        }

        public static ComparisonSide getPtmComparisonSide() {
            return ptmComparisonSide;
        }

        public static void setPtmComparisonSide(ComparisonSide ptmComparisonSide) {
            PathwaysFilters.ptmComparisonSide = ptmComparisonSide;
        }
    }

    public class DescriptionTableRow {
        private String name;
        private String type;

        public DescriptionTableRow(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }
    }
}
