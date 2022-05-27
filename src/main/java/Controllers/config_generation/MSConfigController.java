package Controllers.config_generation;

import Controllers.FXMLDocumentController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Pair;

import java.io.File;
import java.net.URL;
import java.util.*;

public class MSConfigController implements Initializable {

    @FXML
    private Button removeFileButton;
    @FXML
    private Button deleteRunButton;
    @FXML
    private VBox labelsGridContainer;
    @FXML
    private ListView<String> rawFilesList;
    @FXML
    private ListView<MSRunConfig> runsList;
    @FXML
    private TextField runNameField;
    @FXML
    private ListView combinedRunsList;
    @FXML
    private ComboBox<String> combinedRunCombo;
    @FXML
    private ListView<String> fixedModList;
    @FXML
    private ListView<String> modsList;
    @FXML
    private ListView<String> variableModList;
    @FXML
    private ComboBox<String> labelTypeCombo;

    HashMap<Pair<ComboBox<String>, ComboBox<String>>, String> labelsMap;

    private static MSConfigController instance;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        instance = this;

        modsList.getItems().addAll("Oxidation (M)", "Phospho (STY)");
        labelTypeCombo.getItems().addAll("TMT-8Plex", "TMT-10Plex", "SILAC");
        fixedModList.getItems().add("Carbamidomethyl (C)");
        combinedRunCombo.getItems().add("None");
        combinedRunCombo.getSelectionModel().select(0);

        labelTypeCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showLabels());
        labelTypeCombo.getSelectionModel().select(0);

        runsList.setCellFactory(lv -> new ListCell<MSRunConfig>() {
            @Override
            protected void updateItem(MSRunConfig item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? null : item.getName() );
            }
        });

        deleteRunButton.disableProperty().bind(runsList.getSelectionModel().selectedItemProperty().isNull());
        removeFileButton.disableProperty().bind(rawFilesList.getSelectionModel().selectedItemProperty().isNull());
    }

    public void addRunName() {

    }

    public void addCombinedRun() {
        TextInputDialog td = new TextInputDialog("Enter combined run name");
        Optional<String> condition= td.showAndWait();
        if(condition.isPresent() && !combinedRunsList.getItems().contains(condition.get())){
            combinedRunCombo.getItems().add(condition.get());
            combinedRunsList.getItems().add(condition.get());

        }
    }
    @FXML
    public void addFixedMod() {
        if(modsList.getSelectionModel().getSelectedItem()!=null){
            fixedModList.getItems().add(modsList.getSelectionModel().getSelectedItem());
            modsList.getItems().remove(modsList.getSelectionModel().getSelectedItem());
        }
    }
    @FXML
    public void removeFixedMod() {
        if(fixedModList.getSelectionModel().getSelectedItem()!=null){
            modsList.getItems().add(fixedModList.getSelectionModel().getSelectedItem());
            fixedModList.getItems().remove(fixedModList.getSelectionModel().getSelectedItem());
        }
    }
    @FXML
    public void addVariableMod() {
        if(modsList.getSelectionModel().getSelectedItem()!=null){
            variableModList.getItems().add(modsList.getSelectionModel().getSelectedItem());
            modsList.getItems().remove(modsList.getSelectionModel().getSelectedItem());
        }
    }
    @FXML
    public void removeVariableMod() {
        if(variableModList.getSelectionModel().getSelectedItem()!=null){
            modsList.getItems().add(variableModList.getSelectionModel().getSelectedItem());
            variableModList.getItems().remove(variableModList.getSelectionModel().getSelectedItem());
        }
    }

    @FXML
    public void addRawFile() {
        FileChooser directoryChooser = new FileChooser();
        List<File> selectedFiles = directoryChooser.showOpenMultipleDialog(FXMLDocumentController.getInstance().getStage());
        if(selectedFiles!=null){
            for(File file: selectedFiles){
                rawFilesList.getItems().add(file.getAbsolutePath());
            }
        }



    }

    public void showLabels(){
        labelsGridContainer.getChildren().clear();
        String[] labels = new String[]{};
        if(labelTypeCombo.getSelectionModel().getSelectedItem().equals("TMT-10Plex")){
            labels = new String[]{"126","127N","127C","128N","128C","129N","129C","130N","130C","131"};
        }else if(labelTypeCombo.getSelectionModel().getSelectedItem().equals("TMT-8Plex")){
            labels = new String[]{"126","127N","127C","128C","129N","129C","130C","131"};
        }else if(labelTypeCombo.getSelectionModel().getSelectedItem().equals("SILAC")){
            labels = new String[]{"Light", "Medium", "Heavy"};
        }
        GridPane labelsPane = new GridPane();

        labelsMap = new HashMap<>();

        if(labelTypeCombo.getSelectionModel().getSelectedItem().contains("TMT")){

            for (int i = 0; i < labels.length; i++) {
                Label name = new Label(labels[i]);
                ComboBox<String> conditionCombo = new ComboBox<>();
                ComboBox<String> sampleCombo = new ComboBox<>();
                conditionCombo.getItems().addAll(SampleConfigController.getInstance().getConditions());

                conditionCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                    sampleCombo.getItems().clear();
                    sampleCombo.getItems().addAll(SampleConfigController.getInstance().getSamples(newValue));
                    sampleCombo.getSelectionModel().select(0);
                });
                conditionCombo.getSelectionModel().select(0);

                labelsPane.add(name, 0, i);
                labelsPane.add(conditionCombo, 1, i);
                labelsPane.add(sampleCombo, 2, i);

                labelsMap.put(new Pair<>(conditionCombo, sampleCombo), labels[i]);
            }
        }else if(labelTypeCombo.getSelectionModel().getSelectedItem().contains("SILAC")){
            for (int i = 0; i < labels.length; i++) {
                Label name = new Label(labels[i]);
                ComboBox<String> conditionCombo = new ComboBox<>();
                conditionCombo.getItems().add("None");
                conditionCombo.getSelectionModel().select(0);
                conditionCombo.getItems().addAll(SampleConfigController.getInstance().getConditions());
                labelsPane.add(name, 0, i);
                labelsPane.add(conditionCombo, 1, i);
                labelsMap.put(new Pair<>(conditionCombo, null), labels[i]);

            }
        }
        labelsGridContainer.getChildren().add(labelsPane);
    }

    @FXML
    public void onNext() {
        ConfigGenerationController.getInstance().generateConfigFile();

    }
    @FXML
    public void addRun() {
        MSRunConfig run = new MSRunConfig(runNameField.getText());
        run.setFiles(new ArrayList<>(rawFilesList.getItems()));
        run.setFixedMods(new ArrayList<>(fixedModList.getItems()));
        run.setVariableMods(new ArrayList<>(variableModList.getItems()));
        if(!combinedRunCombo.getSelectionModel().getSelectedItem().equals("None"))
            run.setCombinedRun(combinedRunCombo.getSelectionModel().getSelectedItem());

        HashMap<String, String> sampleLabelMap = new HashMap<>();
        for(Map.Entry<Pair<ComboBox<String>, ComboBox<String>>, String> entry: labelsMap.entrySet()){
            String sampleName = entry.getKey().getKey().getSelectionModel().getSelectedItem();
            if(entry.getKey().getValue()!=null && entry.getKey().getValue().getSelectionModel().getSelectedItem()!=null && !entry.getKey().getValue().getSelectionModel().getSelectedItem().equals("None"))
                sampleName+= "/"+entry.getKey().getValue().getSelectionModel().getSelectedItem();

            sampleLabelMap.put(entry.getValue(), sampleName);
        }
        run.setLabels(sampleLabelMap);
        if(labelTypeCombo.getSelectionModel().getSelectedItem().contains("TMT")){
            run.setLabelType("TMT");
        }
        if(labelTypeCombo.getSelectionModel().getSelectedItem().equals("SILAC")){
            run.setLabelType("SILAC");
        }

        runsList.getItems().add(run);
        rawFilesList.getItems().clear();
        runNameField.setText("");
    }

    public static MSConfigController getInstance() {
        return instance;
    }

    public ObservableList<MSRunConfig> getRuns(){
        return runsList.getItems();
    }

    @FXML
    public void onBack() {
        ConfigGenerationController.getInstance().backToSamples();
    }

    public void deleteRun() {
        runsList.getItems().remove(runsList.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void removeFile() {
        rawFilesList.getItems().remove(rawFilesList.getSelectionModel().getSelectedItem());
    }
}
