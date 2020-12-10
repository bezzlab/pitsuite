package Controllers;

import FileReading.Bed;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.Pair;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class AddBamController implements Initializable {

    @FXML
    private JFXTextField conditionTextField;

    @FXML
    private JFXTextField sampleTextField;
    @FXML
    private JFXTextField filePathTextField;
    @FXML
    private Button searchButton;
    @FXML
    private Button addButton;
    @FXML
    private TableView<CondSamplePathForTable> condSamplePathTableView;
    @FXML
    private TableColumn<CondSamplePathForTable, String> condPathTableColumn;
    @FXML
    private TableColumn<CondSamplePathForTable, String> samplePathTableColumn;
    @FXML
    private TableColumn<CondSamplePathForTable, String> pathPathTableColumn;
    @FXML
    private Button removeFromTableButton;
    @FXML
    private Button clearTableButton;



    private HashMap<Pair<String, String>, String> condSampleBamPathMap;
    private ArrayList<Bed> bedFiles = new ArrayList<>();

    private boolean condTextFieldNotNull = false;
    private boolean sampleTextFieldNotNull = false;
    private boolean pathTextFieldNotNull =false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        filePathTextField.setEditable(false);
        addButton.setDisable(true);
        condSamplePathTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // to autommatically generate the getters
        condPathTableColumn.setCellValueFactory( new PropertyValueFactory<>("condition"));
        samplePathTableColumn.setCellValueFactory( new PropertyValueFactory<>("sample"));
        pathPathTableColumn.setCellValueFactory( new PropertyValueFactory<>("path"));


        conditionTextField.textProperty().addListener((observableValue, s, t1) -> {
            String condition = conditionTextField.getText().strip();
            if (condition.length() > 0){
                condTextFieldNotNull = true;
            } else {
                condTextFieldNotNull = false;
            }

            if (condTextFieldNotNull  && sampleTextFieldNotNull && pathTextFieldNotNull) {
                addButton.setDisable(false);
            }
        });

        // condition textfield
        conditionTextField.textProperty().addListener((observableValue, s, t1) -> {
            String condition = conditionTextField.getText().strip();
            if (condition.length() > 0){
                condTextFieldNotNull = true;
            } else {
                condTextFieldNotNull = false;
            }

            if (condTextFieldNotNull  && sampleTextFieldNotNull && pathTextFieldNotNull) {
                addButton.setDisable(false);
            } else {
                addButton.setDisable(true);
            }
        });

        // sample textfield
        sampleTextField.textProperty().addListener((observableValue, s, t1) -> {
            String sample = sampleTextField.getText().strip();
            if (sample.length() > 0){
                sampleTextFieldNotNull = true;
            } else {
                sampleTextFieldNotNull = false;
            }

            if (condTextFieldNotNull  && sampleTextFieldNotNull && pathTextFieldNotNull) {
                addButton.setDisable(false);
            }else {
                addButton.setDisable(true);
            }
        });


        // filePathTextField
        filePathTextField.textProperty().addListener((observableValue, s, t1) -> {
            String filePath = filePathTextField.getText().strip();

            if (filePath.length() > 0){
                pathTextFieldNotNull = true;
            } else {
                pathTextFieldNotNull = false;
            }

            if (condTextFieldNotNull  && sampleTextFieldNotNull && pathTextFieldNotNull) {
                addButton.setDisable(false);
            } else {
                addButton.setDisable(true);
            }
        });

        searchButton.setOnAction(actionEvent -> {
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("BAM (*.bam)");

            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(extFilter);

            File file = fileChooser.showOpenDialog(searchButton.getScene().getWindow());
            if (file != null) {
                filePathTextField.setText(file.getPath());
            }

        });

        addButton.setOnAction(actionEvent -> {

            String condition = conditionTextField.getText().strip();
            String sample = sampleTextField.getText().strip();
            String path = filePathTextField.getText().strip();


            // check if path already in the table
            boolean pathInTable = false;
            for(CondSamplePathForTable tableItem : condSamplePathTableView.getItems()){
                if (tableItem.getPath().equals(path)){
                    pathInTable = true;
                    break;
                }
            }

            // if path is not in the table, add an item
            if (!pathInTable){
                CondSamplePathForTable tableItem = new CondSamplePathForTable(condition, sample, path);
                condSamplePathTableView.getItems().add(tableItem);
                conditionTextField.setText("");
                sampleTextField.setText("");
                filePathTextField.setText("");
            }

        });


        removeFromTableButton.setOnAction(actionEvent -> {
            condSamplePathTableView.getItems().removeAll(condSamplePathTableView.getSelectionModel().getSelectedItems());
            condSamplePathTableView.getSelectionModel().clearSelection();
        });

        clearTableButton.setOnAction(actionEvent -> {
            condSamplePathTableView.getItems().clear();
        });




    }



    public void displayTable(HashMap<Pair<String, String>, String> pathsMap){
        setCondSampleBamPathMap(pathsMap);
        condSamplePathMapToTable();
    }


    public void setCondSampleBamPathMap(HashMap<Pair<String, String>, String> pathsMap){
        condSampleBamPathMap = pathsMap;
    }

    public HashMap<Pair<String, String>, String> getCondSampleBamPathMap() {

        tableToCondSamplePathMap();
        return condSampleBamPathMap;
    }

    public ArrayList<Bed> getBedFiles(){
        return bedFiles;
    }


    public void condSamplePathMapToTable(){
        condSamplePathTableView.getItems().clear();
        for (Map.Entry<Pair<String, String>, String> pathMapEntry: condSampleBamPathMap.entrySet()){
            String condition = pathMapEntry.getKey().getKey();
            String sample = pathMapEntry.getKey().getValue();
            String path = pathMapEntry.getValue();

            condSamplePathTableView.getItems().add(new CondSamplePathForTable(condition, sample, path));
        }
    }

    public void tableToCondSamplePathMap(){
        condSampleBamPathMap = new HashMap<>();

        for(CondSamplePathForTable tableItem : condSamplePathTableView.getItems()){
            String condition = tableItem.getCondition();
            String sample = tableItem.getSample();
            String path = tableItem.getPath();

            if(path.endsWith(".bed")){
                bedFiles.add(new Bed(path));
            }else{
                Pair<String, String> condSamplePair = new Pair<String, String>(condition, sample);
                condSampleBamPathMap.put(condSamplePair, path);
            }


        }

    }

    public class CondSamplePathForTable{
        private String condition;
        private String sample;
        private String path;

        public CondSamplePathForTable(String condition, String sample, String path) {
            this.condition = condition;
            this.sample = sample;
            this.path = path;
        }

        public String getCondition() {
            return condition;
        }

        public String getSample() {
            return sample;
        }

        public String getPath() {
            return path;
        }
    }
}
