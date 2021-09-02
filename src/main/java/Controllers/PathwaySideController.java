package Controllers;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import pathway.Entity;
import pathway.Gene;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;
import org.json.JSONObject;
import pathway.Element;
import pathway.SearchResult;
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
import java.util.ResourceBundle;

public class PathwaySideController implements Initializable {


    public static PathwaySideController instance;

    @FXML
    private ComboBox<String> colorbyCombobox;
    @FXML
    private javafx.scene.control.Label selectionLabel;
    @FXML
    private TableView<GeneValueTable> selectionTable;
    @FXML
    private TableColumn<GeneValueTable, String> entityNameSelectionColumn;
    @FXML
    private TableColumn<GeneValueTable, String> entityTypeSelectionColumn;
    @FXML
    private TableColumn<GeneValueTable, Double> entityValueSelectionColumn;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;

        colorbyCombobox.setItems(FXCollections.observableArrayList("RNA DGE", "Differencial protein abundance", "Phosphorylation"));
        colorbyCombobox.getSelectionModel().select(0);

        colorbyCombobox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            PathwayController.getInstance().colorElements();
        });

        pathwayListview.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                summationField.setText(searchPathways.get(pathwayListview.getSelectionModel().getSelectedItem()).getSummation());
                PathwayController.getInstance().loadPathway(searchPathways.get(pathwayListview.getSelectionModel().getSelectedItem()).getId(), null);
            }
        });
        reactionListview.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                summationField.setText(searchReactions.get(reactionListview.getSelectionModel().getSelectedItem()).getSummation());
                PathwayController.getInstance().loadReaction(reactionListview.getSelectionModel().getSelectedItem());
            }
        });

        entityNameSelectionColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        entityValueSelectionColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        entityTypeSelectionColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        entityNameSelectionColumn.prefWidthProperty().bind(selectionTable.widthProperty().divide(3));
        entityValueSelectionColumn.prefWidthProperty().bind(selectionTable.widthProperty().divide(3));
        entityTypeSelectionColumn.prefWidthProperty().bind(selectionTable.widthProperty().divide(3));

        selectionTable.setRowFactory( tv -> {
            TableRow<GeneValueTable> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && (! row.isEmpty()) ) {
                    showGeneDetails(row.getItem().getId());
                }
            });
            return row ;
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
                        }
                    }
                }
            }


            in.close();


        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void populateSelectionTable(Element element){

        selectionLabel.setText(element.getLabel());

        ArrayList<GeneValueTable> rows = new ArrayList<>();
        for(Entity entity: element.getEntities()){
            rows.add(new GeneValueTable(entity.getId(), entity.getName(), entity.getType(), entity.getValue()));
        }
        selectionTable.getItems().clear();
        selectionTable.getItems().addAll(rows);
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

    @FXML
    public void openDGE() {
        DgeTableController.getInstance().selectGene(selectionTable.getSelectionModel().getSelectedItem().getName());
        ResultsController.getInstance().moveToTab(2);
    }
    @FXML
    public void openBrowser() {
        GeneBrowserController.getInstance().showGeneBrowser(selectionTable.getSelectionModel().getSelectedItem().getName());
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
