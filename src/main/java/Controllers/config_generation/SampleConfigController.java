package Controllers.config_generation;

import Controllers.FXMLDocumentController;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.*;

public class SampleConfigController implements Initializable {

    @FXML
    private TextField refGtfField;
    @FXML
    private TextField refFastaField;
    @FXML
    private CheckBox splicingCheckbox;
    @FXML
    private CheckBox dgeCheckbox;
    @FXML
    private CheckBox mutationsCheckbox;
    @FXML
    private TextField threadsField;
    @FXML
    private TextField projectNameField;
    @FXML
    private TextField outputField;
    @FXML
    private ListView<String> conditionsList;
    @FXML
    private ComboBox<String> refConditionCombo;
    @FXML
    private TextField sampleNameField;
    @FXML
    private ComboBox<String> sampleConditionCombo;
    @FXML
    private RadioButton pairEndedButton;
    @FXML
    private RadioButton singleEndedButton;
    @FXML
    private VBox fastqBox;
    @FXML
    private ListView<Sample> samplesList;
    @FXML
    private RadioButton refGuidedButton;
    @FXML
    private RadioButton denovoButton;
    @FXML
    private TextField protBlastField;

    private TextField leftField;
    private TextField rightField;
    private TextField singleField;

    private final ArrayList<Sample> samples = new ArrayList<>();

    private static SampleConfigController instance;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        instance = this;

        ToggleGroup assemblyRadioGroup = new ToggleGroup();
        refGuidedButton.setToggleGroup(assemblyRadioGroup);
        denovoButton.setToggleGroup(assemblyRadioGroup);
        assemblyRadioGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> onAssemblyChange(assemblyRadioGroup));

        ToggleGroup readsRadioGroup = new ToggleGroup();
        pairEndedButton.setToggleGroup(readsRadioGroup);
        singleEndedButton.setToggleGroup(readsRadioGroup);
        readsRadioGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> onReadsChange(readsRadioGroup));

        pairEndedButton.setSelected(true);

        samplesList.setCellFactory(lv -> new ListCell<Sample>() {
            @Override
            protected void updateItem(Sample item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? null : item.getCondition()+"/"+item.getName() );
            }
        });

    }

    private void onReadsChange(ToggleGroup readsRadioGroup) {
        fastqBox.getChildren().clear();
        if(readsRadioGroup.getSelectedToggle()==pairEndedButton){
            leftField = new TextField();
            leftField.setPromptText("Left reads fastq");
            fastqBox.getChildren().add(leftField);
            rightField = new TextField();
            rightField.setPromptText("Right reads fastq");
            fastqBox.getChildren().add(rightField);
            leftField.setOnMouseClicked(event -> {
                FileChooser directoryChooser = new FileChooser();
                File selectedDirectory = directoryChooser.showOpenDialog(FXMLDocumentController.getInstance().getStage());
                if(selectedDirectory!=null)
                    leftField.setText(selectedDirectory.getAbsolutePath());
            });
            rightField.setOnMouseClicked(event -> {
                FileChooser directoryChooser = new FileChooser();
                File selectedDirectory = directoryChooser.showOpenDialog(FXMLDocumentController.getInstance().getStage());
                if(selectedDirectory!=null)
                    rightField.setText(selectedDirectory.getAbsolutePath());
            });


        }else{
            singleField = new TextField();
            singleField.setPromptText("Reads fastq");
            fastqBox.getChildren().add(singleField);
            singleField.setOnMouseClicked(event -> {
                FileChooser directoryChooser = new FileChooser();
                File selectedDirectory = directoryChooser.showOpenDialog(FXMLDocumentController.getInstance().getStage());
                if(selectedDirectory!=null)
                    singleField.setText(selectedDirectory.getAbsolutePath());
            });
        }
    }

    public void pickOutput() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(FXMLDocumentController.getInstance().getStage());
        outputField.setText(selectedDirectory.getAbsolutePath());
    }

    public void addCondition() {
        TextInputDialog td = new TextInputDialog("Enter condition name");
        Optional<String> condition= td.showAndWait();
        if(condition.isPresent() && !conditionsList.getItems().contains(condition.get())){
            conditionsList.getItems().add(condition.get());
            refConditionCombo.getItems().add(condition.get());
            sampleConditionCombo.getItems().add(condition.get());
            if(refConditionCombo.getItems().size()==1){
                refConditionCombo.getSelectionModel().select(0);
            }
            if(sampleConditionCombo.getItems().size()==1){
                sampleConditionCombo.getSelectionModel().select(0);
            }
        }

    }

    @FXML
    public void addSample() {
        Sample sample;
        if(pairEndedButton.isSelected()){
            sample = new Sample(sampleNameField.getText(), sampleConditionCombo.getSelectionModel().getSelectedItem(), leftField.getText(), rightField.getText());
            samples.add(sample);
        }else{
            sample = new Sample(sampleNameField.getText(), sampleConditionCombo.getSelectionModel().getSelectedItem(), singleField.getText());
            samples.add(sample);
        }

        samplesList.getItems().add(sample);
        leftField.setText("");
        rightField.setText("");
        singleField.setText("");
    }
    @FXML
    public void addMS() {
        ConfigGenerationController.getInstance().showMSConfig();
    }

    public void onAssemblyChange(ToggleGroup assemblyRadioGroup){
            Toggle toggle = assemblyRadioGroup.getSelectedToggle();
            if(toggle==refGuidedButton){
                refFastaField.setDisable(false);
                refGtfField.setDisable(false);
            }else{
                refFastaField.setDisable(true);
                refGtfField.setDisable(true);
        }
    }


    public void pickProtBlast() {
    }

    public void pickGtf() {
        FileChooser directoryChooser = new FileChooser();
        File selectedDirectory = directoryChooser.showOpenDialog(FXMLDocumentController.getInstance().getStage());
        if(selectedDirectory!=null)
            refGtfField.setText(selectedDirectory.getAbsolutePath());
    }

    public void pickFasta() {
        FileChooser directoryChooser = new FileChooser();
        File selectedDirectory = directoryChooser.showOpenDialog(FXMLDocumentController.getInstance().getStage());
        if(selectedDirectory!=null)
            refFastaField.setText(selectedDirectory.getAbsolutePath());
    }

    public HashSet<String> getConditions(){
        HashSet<String> conditions = new HashSet<>();
        for(Sample sample: samples){
            conditions.add(sample.getCondition());
        }
        return conditions;
    }
    public HashSet<String> getSamples(String conditions){
        HashSet<String> conditionSamples = new HashSet<>();
        for(Sample sample: samples){
            if(sample.getCondition().equals(conditions))
                conditionSamples.add(sample.getName());
        }
        return conditionSamples;
    }

    public static SampleConfigController getInstance() {
        return instance;
    }

    public ObservableList<Sample> getSamples(){
        return samplesList.getItems();
    }

    public int getThreads() {
        if(!threadsField.getText().isEmpty()){
            try {
                return Integer.parseInt(threadsField.getText());
            } catch(NumberFormatException e){
                return 8;
            }
        }
        return 8;

    }

    public String getOutput() {
        return outputField.getText();
    }

    public String getProjectName() {
        return projectNameField.getText();
    }

    public String getRefGtf() {
        return refGtfField.getText().isEmpty()?null:refGtfField.getText();
    }

    public String getRefFasta() {
        return refFastaField.getText().isEmpty()?null:refFastaField.getText();
    }

    public boolean runDge(){
        return dgeCheckbox.isSelected();
    }
    public boolean runSplicing(){
        return splicingCheckbox.isSelected();
    }
    public boolean runMutations(){
        return mutationsCheckbox.isSelected();
    }

    public String getRefCondition(){
        return refConditionCombo.getSelectionModel().getSelectedItem();
    }

}
