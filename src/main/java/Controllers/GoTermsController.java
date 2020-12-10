package Controllers;

import FileReading.AllGenesReader;
import TablesModels.GoTerm;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.textfield.TextFields;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.Filter;
import org.dizitart.no2.Nitrite;

import java.net.URL;
import java.util.*;

import static org.dizitart.no2.filters.Filters.*;

public class GoTermsController extends Controller {


    @FXML
    private VBox container;
    @FXML
    private Accordion accordion;
    @FXML
    private VBox containerForGeneVBox;
    @FXML
    private VBox containerFilteringGoVBox;
    @FXML
    private Label geneGoTermsLabel;
    @FXML
    private TableView<GoTerm> goTermsOfAGeneTable;
    @FXML
    private TableColumn<GoTerm, String> goIdGeneTableColumn;
    @FXML
    private TableColumn<GoTerm, String> goNameGeneTableColumn;
    @FXML
    private TableView<GoTerm> filterTable;
    @FXML
    private TableColumn<GoTerm, String> goIdFilterTableColumn;
    @FXML
    private TableColumn<GoTerm, String> goNameFilterTableColumn;
    @FXML
    private Button addFilterButton;
    @FXML
    private Button removeFromTableButton;
    @FXML
    private JFXTextField searchField;
    @FXML
    private GridPane webViewGridPane;

    private Nitrite db;


    private Controller parentController;
    private AllGenesReader allGenesReader;

    private WebViewController goTermsWebview;
    private boolean goLoaded;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        goLoaded = false;

        accordion.setExpandedPane(accordion.getPanes().get(0));

        addFilterButton.setDisable(true);
        removeFromTableButton.setDisable(true);

        goIdGeneTableColumn.setCellValueFactory( new PropertyValueFactory<>("goId"));
        goNameGeneTableColumn.setCellValueFactory( new PropertyValueFactory<>("name"));

        containerForGeneVBox.prefWidthProperty().bind(container.widthProperty());
        goTermsOfAGeneTable.prefWidthProperty().bind(container.widthProperty());
        goIdGeneTableColumn.prefWidthProperty().bind(container.widthProperty().divide(3));
        goNameGeneTableColumn.prefWidthProperty().bind(container.widthProperty().divide(3/2));


        goIdFilterTableColumn.setCellValueFactory( new PropertyValueFactory<>("goId"));
        goNameFilterTableColumn.setCellValueFactory( new PropertyValueFactory<>("name"));

        containerFilteringGoVBox.prefWidthProperty().bind(container.widthProperty());
        filterTable.prefWidthProperty().bind(container.widthProperty());
        goIdFilterTableColumn.prefWidthProperty().bind(container.widthProperty().divide(3));
        goNameFilterTableColumn.prefWidthProperty().bind(container.widthProperty().divide(3/2));


        goTermsOfAGeneTable.setRowFactory( tv -> {
            TableRow<GoTerm> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (! row.isEmpty()){
                    if (event.getClickCount() == 2 &&  event.getButton().equals(MouseButton.PRIMARY)) {
                        GoTerm rowData = row.getItem();
                        checkIfGoInTableAndAdd(rowData.getGoId());
                    }
                }

            });
            return row ;
        });

        webViewGridPane.prefWidthProperty().bind(container.widthProperty());
        filterTable.prefHeightProperty().bind(container.heightProperty().divide(3));
        webViewGridPane.prefHeightProperty().bind(container.heightProperty().divide(3/2));

    }


    public void setParentController(Controller parentController, AllGenesReader allGenesReader, Nitrite db){

        this.parentController = parentController;
        this.allGenesReader = allGenesReader;
        this.db = db;

        filterTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                removeFromTableButton.setDisable(false);
            } else {
                removeFromTableButton.setDisable(true);
            }
        });


        Platform.runLater(()-> {
            TextFields.bindAutoCompletion(searchField, allGenesReader.getGoTermsStringForAutoCompletion()).prefWidthProperty().bind(searchField.widthProperty());

        });


        searchField.textProperty().addListener((observableValue, s, t1) -> {
            String searchedGO = extractGo();

            if(searchedGO != null){
                showGoOrCleanThread(searchedGO);
                addFilterButton.setDisable(false);
            } else {
                showGoOrCleanThread("");
                addFilterButton.setDisable(true);
            }
        });


        goLoaded = true;

//        searchField.setText("GO:0003674: molecular_function");
    }

    public boolean isGoLoaded() {
        return goLoaded;
    }

    public void setGoTermsGeneTable(String geneSymbol){

        if (!geneSymbol.equals(geneGoTermsLabel.getText())){ // not the same gene
            goTermsOfAGeneTable.getItems().clear();

            geneGoTermsLabel.setText(geneSymbol);
            new Thread(() -> {
                Document geneDoc = db.getCollection("allGenes" ).find(eq("symbol", geneSymbol)).firstOrDefault();
                ArrayList<String> goOfGeneList = (ArrayList<String>) geneDoc.get("goTerms");
                if (goOfGeneList != null) {
                    for (String goTerm: goOfGeneList){
                        Platform.runLater(()-> {
                            goTermsOfAGeneTable.getItems().add(new GoTerm(goTerm, allGenesReader.getGoTermsMap().get(goTerm)));
                        });
                    }
                }
            }).start();

        }


    }

    /**
     * gets the GoTerm from the TextField
     * @return a GO term, or null if what is in the textfield does not correspond to a GO term
     */
    private String extractGo(){
        String searchedString = searchField.getText().trim().toUpperCase().replace("GO:", "");
        String searchedGO =  "";
        if (!searchedString.contains("GO:")) {
            searchedGO = "GO:" + searchedString.split(":")[0];
        } else {
            searchedGO = searchedString.split(" ")[0];
        }

        if (allGenesReader.getGoTermsMap().keySet().contains(searchedGO)){
            return searchedGO;
        } else {
            return  null;
        }

    }

    @FXML
    private void addFilter(){

        String goTerm = extractGo();
        checkIfGoInTableAndAdd(goTerm);

    }

    private void checkIfGoInTableAndAdd(String goTerm){
        boolean isInTable = false;

        // check
        if (goTerm != null ){
            String goName = allGenesReader.getGoTermsMap().get(goTerm);
            GoTerm searchedGo = new GoTerm(goTerm, goName);
            for (GoTerm goInTable : filterTable.getItems()){
                if (goInTable.getGoId().equals(goTerm)){
                    isInTable = true;
                    break;
                }
            }

            // add
            if (!isInTable){
                filterTable.getItems().add(searchedGo);
            }

        }
    }

    public ArrayList<String> genesWithGoTermsForFilter(){
        ArrayList<String> genesWithGoTermsList = new ArrayList<>();
        ArrayList<String> goTermsList = new ArrayList<>();

        filterTable.getItems().forEach(goTerm -> goTermsList.add(goTerm.getGoId()));


        if (goTermsList.size() > 0 ) {

            // filters
            List<Filter> filters = new ArrayList<>();
            goTermsList.forEach(s -> filters.add(elemMatch("goTerms", eq("$", s))));

            Cursor genesWithSelectedGoCursor = db.getCollection("allGenes").find(and(filters.toArray(new Filter[]{})));


            if (genesWithSelectedGoCursor.size() == 0){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Info");
                alert.setHeaderText(null);
                alert.setContentText("No gene found with selected GO Terms. Table won't update.");

                alert.showAndWait();
            } else {
                for (Document geneDoc: genesWithSelectedGoCursor){
                    genesWithGoTermsList.add((String) geneDoc.get("symbol"));
                }
            }
        } else {
            return null;
        }

        if (genesWithGoTermsList.size() == 0){
            //TODO: add dialog, don't do filtering
        }

        return genesWithGoTermsList;

    }

    @FXML
    private void removeFilter(){
        filterTable.getItems().remove(filterTable.getSelectionModel().getSelectedItem());
    }
    @FXML
    private void filter(){
        if(parentController.getClass()==DgeTableController.class){
           DgeTableController controller = (DgeTableController) parentController;
           controller.filterFoldChangeTable();
        } else if(parentController.getClass()==SplicingTableController.class){
            SplicingTableController controller = (SplicingTableController) parentController;
            controller.filterSplicingTable();
        }
    }

    private void showGoOrCleanThread(String  searchedGo) {
        if(allGenesReader.getGoTermsMap().keySet().contains(searchedGo)){
            new Thread(() -> showGoGraph(searchedGo)) {{start();}};
        } else {
            new Thread(() -> drawGraph("cleanNetwork()", goTermsWebview)){{start();}};  //TODO: complete this
        }
    }

    private void showGoGraph(String  searchedGo) {

        HashMap<String, ArrayList<String>> goIdIsAMap = new HashMap<>();

        ArrayList<String> cumGoTerms = new ArrayList<>();


        boolean oneLevelParents = true; //TODO: change this to a checkbox or something
        cumGoTerms.add(searchedGo); // first element

        // parents
        if (oneLevelParents) {  // only 1 level up
            ArrayList<String> isAGoList = allGenesReader.getGoTermsIsAMap().get(searchedGo);
            goIdIsAMap.put(searchedGo, isAGoList);
            for (String searchedIsA : isAGoList) {
                cumGoTerms.add(searchedIsA);
            }
        } else { // the whole tree

            int count = 0;
            while (count < cumGoTerms.size()) {
                String goTerm = cumGoTerms.get(count);

                if (!goIdIsAMap.keySet().contains(goTerm)) { // new go Id
                    ArrayList<String> isAGoList = allGenesReader.getGoTermsIsAMap().get(searchedGo);
                    goIdIsAMap.put(goTerm, isAGoList);

                    for (String isAGo : isAGoList) {
                        if (!cumGoTerms.contains(isAGo)) {
                            cumGoTerms.add(isAGo);
                        }
                    }
                }

                count++;
            }
        }

        // children (1 level down)
        ArrayList<String> dummyList = new ArrayList<>();
        dummyList.add(searchedGo);
        for (Map.Entry<String, ArrayList<String>> goMapEntry : allGenesReader.getGoTermsIsAMap().entrySet()) {
            if (goMapEntry.getValue().contains(searchedGo)) {
                String childGo = goMapEntry.getKey();
                goIdIsAMap.put(childGo, dummyList);
                cumGoTerms.add(childGo);
            }
        }


        StringBuilder nodesString = new StringBuilder();
        nodesString.append("{");
        for (int i = 0; i < cumGoTerms.size(); i++) {
            String goTerm = cumGoTerms.get(i);
            nodesString.append("\"" + goTerm + "\":" + "\"" + allGenesReader.getGoTermsMap().get(goTerm) + "\"");
            if (i < (cumGoTerms.size() - 1)) {
                nodesString.append(", ");
            }
        }
        nodesString.append("}");

        StringBuilder edgesString = new StringBuilder();
        edgesString.append("{");
        int count = 0;
        for (Map.Entry<String, ArrayList<String>> mapEntry : goIdIsAMap.entrySet()) {
            count++;
            edgesString.append("\"" + mapEntry.getKey() + "\": [");
            ArrayList<String> isAGoTerms = mapEntry.getValue();
            for (int i = 0; i < isAGoTerms.size(); i++) {
                edgesString.append("\"" + isAGoTerms.get(i) + "\"");
                if (i < (isAGoTerms.size() - 1)) {
                    edgesString.append(", ");
                }
            }
            edgesString.append("]");
            if (count < goIdIsAMap.size()) {
                edgesString.append(",");
            }

        }
        edgesString.append("}");

        String functionToExecute = "plotNetwork(\"" + searchedGo + "\" ," + nodesString + "," + edgesString + ")";
        drawGraph(functionToExecute, goTermsWebview);
    }



    private void drawGraph (String sendFunction, WebViewController webViewController){

        GoTermsController _this = this;

        if(goTermsWebview==null){

            Platform.runLater(() -> {
                int webWidth = (int) containerFilteringGoVBox.getWidth()-10;
                int webHeight = (int) webViewGridPane.getHeight()-10;
                goTermsWebview = new WebViewController("goTerms", new String[]{"vis", "goTerms"}, webWidth, webHeight,
                        12, _this, "GO");

                webViewGridPane.add(goTermsWebview.getWebView(), 0,0);
                webViewController.execute(sendFunction);
            });


        }else{
            Platform.runLater(() -> webViewController.execute(sendFunction));
        }



    }



    public void getGoIdFromJs(String goId){
        searchField.setText(goId);
    }

}
