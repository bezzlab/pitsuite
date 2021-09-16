package Controllers;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import pathway.Entity;
import pathway.Gene;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;
import org.json.JSONObject;
import pathway.Element;
import pathway.SearchResult;
import pathway.alerts.DgeAlert;
import pathway.alerts.MutationAlert;
import pathway.alerts.PTMAlert;
import pathway.alerts.SplicingAlert;
import pitguiv2.App;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ResourceBundle;

public class PathwaySideController implements Initializable {

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

    private HashMap<String, SearchResult> searchPathways;
    private HashMap<String, SearchResult> searchReactions;
    public static PathwaySideController instance;

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
                PathwayController.getInstance().loadPathway(searchPathways.get(pathwayListview.getSelectionModel().getSelectedItem()).getId(), null);
            }
        });
        reactionListview.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                //summationField.setText(searchReactions.get(reactionListview.getSelectionModel().getSelectedItem()).getSummation());
                PathwayController.getInstance().loadReaction(reactionListview.getSelectionModel().getSelectedItem());
            }
        });


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

    @FXML
    public void openDGE() {

        ResultsController.getInstance().moveToTab(2);
    }
    @FXML
    public void openBrowser() {

        ResultsController.getInstance().moveToTab(1);

    }
    @FXML
    public void openSplicing() {
    }
    @FXML
    public void openMutations() {
    }

    public String getSelectedColorVariable(){
        return colorbyCombobox.getSelectionModel().getSelectedItem();
    }

    public class GeneValueTable{
        private final String name;
        private Double value;
        private final String type;
        private String id;

        public GeneValueTable(String id, String name, String type, Double value) {
            this.name = name;
            this.value = value;
            this.type = type;
            this.id = id;
        }
        public GeneValueTable(String id, String name, String type) {
            this.name = name;
            this.type = type;
            this.id = id;
        }


        public Double getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getId() {
            return id;
        }
    }
}
