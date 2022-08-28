package Controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class configGeneration  {

    @FXML
    private Button newProjectButton;

    @FXML
    private Button analysisButton;

    @FXML
    private Button testButton;

    @FXML
    private GridPane initialGridPane;
    @FXML
    private TextField directoryField;
    @FXML
    private TextField nameField;
    @FXML
    private Spinner sampleNumber;
    @FXML
    private GridPane gridpane = new GridPane();
    @FXML
    private CheckBox checkBox;
    @FXML
    private TextField amino_acid;
    @FXML
    private TextField shift;
    @FXML
    private ToggleGroup assemblyType;
    @FXML
    private RadioButton denovo;
    @FXML
    private RadioButton reference;
    @FXML
    private TextField outputField;
    @FXML
    private TextField referenceField;

    @FXML
    GridPane leftGridPane;

    TableView modTable1 = new TableView();
    TableView modTable2 = new TableView();
    TableView modTable3 = new TableView();
    TableView modTable4 = new TableView();


    @FXML
    private Label loadingLabel;
    @FXML
    private ProgressBar loadingBar;
    @FXML
    private GridPane loadingPane;
    @FXML
    private VBox contentPane;

    private FXMLDocumentController parent;

    private Map<String, Integer> conditionsDict = new HashMap<>();
    private Map<String, String> aminoDict = new HashMap<>();
    private Map<String, String> shiftDict = new HashMap<>();
    private Map<String, String> runDict = new HashMap<>();
    private Map<String, Boolean> readTypeDict = new HashMap<>();
    private Map<String, String> mzmlBackDict = new HashMap<>();
    private Map<String, ArrayList> fixedModsDict = new HashMap<>();
    private Map<String, ArrayList> variableModsDict = new HashMap<>();
    private Map<String, ArrayList> fixedModsDictRev = new HashMap<>();
    private Map<String, ArrayList> variableModsDictRev = new HashMap<>();
    private ArrayList<String> conditionNameList = new ArrayList<>();

    private boolean isDenovo;
    private String output_directory;
    private String reference_directory;
    private String precursor;
    private String fragment;
    private String maxMissedCleaves;
    private ArrayList<String> outputs = new ArrayList<>();

    // HashMaps for runs window
    private HashMap<String, ArrayList<String>> mzmlPaths = new HashMap<>();
    private HashMap<String, String> PreTolerances = new HashMap<>();
    private HashMap<String, String> FragTolerances = new HashMap<>();
    private HashMap<String, String> missedCleaves = new HashMap<>();
    private HashMap<String, ArrayList<String>> runModsFixed = new HashMap<>();
    private HashMap<String, ArrayList<String>> runModsVar= new HashMap<>();
    private HashMap<String, String> labelType = new HashMap<>();
    private HashMap<String,HashMap<String, String>> labelInfo = new HashMap<>();
    private ArrayList<String> runNameList = new ArrayList<>();
    private Map<String, ArrayList<String>> tmtLabelMap = new HashMap<>();
    private Map<String, ArrayList<String>> tmtSampleMap = new HashMap<>();
    private Map<String, ArrayList<String>> tmtSampleNolMap = new HashMap<>();


    //HashMap to match run with condition
    private HashMap<String, String> conditionRunDict = new HashMap<>();

    private ComboBox runList = new ComboBox();



    private Set keys;
    private Iterator x;

    private JSONObject condition_json = new JSONObject();

    private org.json.simple.JSONArray condition_final = new org.json.simple.JSONArray();
    private ListView AddedList = new ListView();

    public void initialize() {
        AddedList.getItems().add("Added conditions: ");
        initialGridPane.getChildren().clear();
        TextField test = new TextField();
        Label conditionName = new Label();
        conditionName.setText("Condition name: ");
        initialGridPane.setRowIndex(conditionName, 2);
        initialGridPane.setColumnIndex(conditionName, 0);
        initialGridPane.setColumnIndex(AddedList, 3);
        initialGridPane.setRowIndex(AddedList, 5);

        Label sampleNo = new Label();
        sampleNo.setText("Number of samples:");
        initialGridPane.setRowIndex(sampleNo, 2);
        initialGridPane.setColumnIndex(sampleNo, 1);

        nameField = new TextField();
        initialGridPane.setRowIndex(nameField, 3);
        initialGridPane.setColumnIndex(nameField, 0);


        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,100,1);
        sampleNumber = new Spinner();
        sampleNumber.setValueFactory(valueFactory);
        initialGridPane.setRowIndex(sampleNumber, 3);
        initialGridPane.setColumnIndex(sampleNumber, 1);
        sampleNumber.setEditable(true);

        Label outputLabel = new Label();
        outputLabel.setText("Output directory:");
        initialGridPane.setRowIndex(outputLabel, 10);
        initialGridPane.setColumnIndex(outputLabel, 0);

        outputField = new TextField();
        initialGridPane.setRowIndex(outputField, 11);
        initialGridPane.setColumnIndex(outputField, 0);

        Button outputButton = new Button();
        outputButton.setText("Browse");
        initialGridPane.setRowIndex(outputButton, 11);
        initialGridPane.setColumnIndex(outputButton, 1);
        outputButton.setOnAction(actionEvent -> {
            try {
                String directory = directoryBrowser(actionEvent);
                outputField.setText(directory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Label referenceLabel = new Label();
        referenceLabel.setText("Reference proteome/genome:");
        initialGridPane.setRowIndex(referenceLabel, 12);
        initialGridPane.setColumnIndex(referenceLabel, 0);

        referenceField = new TextField();
        initialGridPane.setRowIndex(referenceField, 13);
        initialGridPane.setColumnIndex(referenceField, 0);

        Button referenceButton = new Button();
        referenceButton.setText("Browse");
        initialGridPane.setRowIndex(referenceButton, 13);
        initialGridPane.setColumnIndex(referenceButton, 1);
        referenceButton.setOnAction(actionEvent -> {
            try {
                String directory = onBrowseButtonClick(actionEvent);
                referenceField.setText(directory);
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        Button addButton = new Button();
        addButton.setText("Add condition");
        initialGridPane.setRowIndex(addButton, 4);
        initialGridPane.setColumnIndex(addButton, 0);
        addButton.setOnAction(actionEvent -> {
            onAddButtonClick(actionEvent);
        });

        Button nextButton = new Button();
        nextButton.setText("Next");
        initialGridPane.setRowIndex(nextButton, 4);
        initialGridPane.setColumnIndex(nextButton, 1);
        nextButton.setOnAction(actionEvent -> {
            onNextButtonClick(actionEvent);

        });

        checkBox = new CheckBox();
        checkBox.setText("Forward and reverse");
        initialGridPane.setRowIndex(checkBox, 5);
        initialGridPane.setColumnIndex(checkBox, 0);
        checkBox.setVisible(false);

        ToggleGroup assemblyType = new ToggleGroup();
        RadioButton denovo = new RadioButton();
        denovo.setText("Denovo assembly");
        denovo.setSelected(true);
        initialGridPane.setRowIndex(denovo, 6);
        initialGridPane.setColumnIndex(denovo, 0);
        denovo.setToggleGroup(assemblyType);

        RadioButton reference = new RadioButton();
        reference.setText("Reference assembly");
        initialGridPane.setRowIndex(reference, 6);
        initialGridPane.setColumnIndex(reference, 1);
        reference.setToggleGroup(assemblyType);

        initialGridPane.getChildren().add(conditionName);
        initialGridPane.getChildren().add(sampleNo);
        initialGridPane.getChildren().add(nameField);
        initialGridPane.getChildren().add(sampleNumber);
        initialGridPane.getChildren().add(outputLabel);
        initialGridPane.getChildren().add(outputField);
        initialGridPane.getChildren().add(outputButton);
        initialGridPane.getChildren().add(referenceLabel);
        initialGridPane.getChildren().add(referenceField);
        initialGridPane.getChildren().add(referenceButton);
        initialGridPane.getChildren().add(addButton);
        initialGridPane.getChildren().add(nextButton);
        initialGridPane.getChildren().add(checkBox);
        initialGridPane.getChildren().add(denovo);
        initialGridPane.getChildren().add(reference);
        initialGridPane.getChildren().add(AddedList);
    }

    public String directoryBrowser(ActionEvent event) throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("src"));
        File selectedDirectory = directoryChooser.showDialog(((Node) event.getTarget()).getScene().getWindow());
        System.out.println(selectedDirectory);
        Path selectedPath = selectedDirectory.toPath();
        String final_path = selectedPath.toString();
        return final_path;
    }

    public String onBrowseButtonClick(ActionEvent event) throws IOException {


        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("src"));
        File selectedDirectory2 = fileChooser.showOpenDialog(((Node) event.getTarget()).getScene().getWindow());
        Path selectedPath = selectedDirectory2.toPath();
        String final_path = selectedPath.toString();

        return final_path;
    }

    public void onAddButtonClick(ActionEvent e) {


        String condition = (String) nameField.getText();
        int noOfSamples = (int) sampleNumber.getValue();
        conditionNameList.add(condition);
        AddedList.getItems().add(condition);
        conditionsDict.put(condition, noOfSamples);
        readTypeDict.put(condition, checkBox.isSelected());
        nameField.clear();
        keys = conditionsDict.keySet();
        x = keys.iterator();

    }

    public void onNextButtonClick(ActionEvent e) {


        Map<String, String> mzmlDict = new HashMap<>();
        Map<String, String> rightDict = new HashMap<>();
        Map<String, String> leftDict = new HashMap<>();
        List<TextField> textFields = new ArrayList<>();
        List<Button> buttons = new ArrayList<>();
        List<ComboBox> boxes = new ArrayList<>();


        initialGridPane.getChildren().clear();

        initialGridPane.setAlignment(Pos.CENTER);

        Button newRun = new Button();
        newRun.setText("Add new run");
        initialGridPane.setRowIndex(newRun, 0);
        initialGridPane.setColumnIndex(newRun, 0);
        initialGridPane.getChildren().add(newRun);
        newRun.setOnAction(actionEvent -> {
            popupTest2();
        });


        Label conditionLabel = new Label();
        conditionLabel.setText("Run:");
        initialGridPane.setColumnIndex(conditionLabel, 7);
        initialGridPane.setRowIndex(conditionLabel, 1);
        initialGridPane.getChildren().add(conditionLabel);



        Object curr_key = x.next();
        String curr_condition = curr_key.toString();
        int noOfSamples = conditionsDict.get(curr_key);
        boolean isSelected = readTypeDict.get(curr_condition);
        String[] sampleList = new String[noOfSamples];
        Label condition = new Label("Condition: " + curr_condition);
        initialGridPane.setRowIndex(condition, 0);
        initialGridPane.setColumnIndex(condition, 3);
        Label mzml_label = new Label("mzml:");
        initialGridPane.setRowIndex(mzml_label, 1);
        initialGridPane.setColumnIndex(mzml_label, 1);
        Label sample_label = new Label("Sample Name");
        Label right_label = new Label("Right:");
        Label left_label = new Label("Left:");
        initialGridPane.setRowIndex(sample_label, 1);
        initialGridPane.setColumnIndex(sample_label, 0);
        initialGridPane.setRowIndex(right_label, 1);
        initialGridPane.setColumnIndex(right_label, 3);
        initialGridPane.setRowIndex(left_label, 1);
        initialGridPane.setColumnIndex(left_label, 5);
        initialGridPane.getChildren().add(condition);

        initialGridPane.getChildren().add(right_label);
        initialGridPane.getChildren().add(sample_label);
        initialGridPane.getChildren().add(left_label);
//        right_label.setVisible(false);
//        left_label.setVisible(false);
        if (isSelected) {
            Label mzmlBackLabel = new Label();
            mzmlBackLabel.setText("mzml REV: ");
            initialGridPane.setRowIndex(mzmlBackLabel, 1);
            initialGridPane.setColumnIndex(mzmlBackLabel, 7);
        }

        for (int i = 1; i <= noOfSamples; i++) {
            TextField sampleNo = new TextField();
            TextField mzml = new TextField();
            TextField right = new TextField();
            TextField left = new TextField();
            Button browse_mzml = new Button("Browse");
            Button browse_right = new Button("Browse");
            Button browse_left = new Button("Browse");
            ComboBox runList2 = new ComboBox();

            for(String curr_runname : runNameList) {
                runList2.getItems().add(curr_runname);
            }


            if(isSelected) {
                System.out.println("------Check passed--------");
                TextField mzml_back = new TextField();
                mzml_label.setText("MZML fwd: ");

                Button browse_mzml_back = new Button();
                browse_mzml_back.setText("Browse");

                initialGridPane.setRowIndex(mzml_back, i +2);
                initialGridPane.setColumnIndex(mzml_back, 7);
                Button mzml_back_button = new Button();
                mzml_back_button.setText("Browse");
                initialGridPane.setRowIndex(mzml_back_button, i+2);
                initialGridPane.setColumnIndex(mzml_back_button, 8);
                initialGridPane.getChildren().add(mzml_back);
                initialGridPane.getChildren().add(mzml_back_button);
                mzml_back_button.setOnAction(actionEvent -> {
                    try {
                        String path = onBrowseButtonClick(actionEvent);
                        mzml_back.setText(path);
                    } catch (IOException x) {
                        x.printStackTrace();
                    }
                });
            }
            browse_mzml.setOnAction(actionEvent -> {
                try {
                    String path = onBrowseButtonClick(actionEvent);
                    mzml.setText(path);
                } catch (IOException x) {
                    x.printStackTrace();
                }
            });

            browse_right.setOnAction(actionEvent -> {
                try {
                    String path = onBrowseButtonClick(actionEvent);
                    right.setText(path);
                } catch (IOException x) {
                    x.printStackTrace();
                }
            });

            browse_left.setOnAction(actionEvent -> {
                try {
                    String path = onBrowseButtonClick(actionEvent);
                    left.setText(path);

                } catch (IOException x) {
                    x.printStackTrace();
                }
            });

            initialGridPane.setRowIndex(sampleNo, i + 2);
            initialGridPane.setColumnIndex(sampleNo, 0);
            initialGridPane.setRowIndex(mzml, i + 2);
            initialGridPane.setColumnIndex(mzml, 1);
            initialGridPane.setRowIndex(browse_mzml, i + 2);
            initialGridPane.setColumnIndex(browse_mzml, 2);
            initialGridPane.setRowIndex(right, i + 2);
            initialGridPane.setColumnIndex(right, 3);
            initialGridPane.setRowIndex(browse_right, i + 2);
            initialGridPane.setColumnIndex(browse_right, 4);
            initialGridPane.setRowIndex(left, i + 2);
            initialGridPane.setColumnIndex(left, 5);
            initialGridPane.setRowIndex(browse_left, i + 2);
            initialGridPane.setColumnIndex(browse_left, 6);
            initialGridPane.setRowIndex(runList2, i + 2);
            initialGridPane.setColumnIndex(runList2, 7);
            textFields.add(sampleNo);

            textFields.add(right);
            textFields.add(left);

            buttons.add(browse_right);
            buttons.add(browse_left);
            boxes.add(runList2);
//            right.setVisible(false);
//            left.setVisible(false);
//            browse_left.setVisible(false);
//            browse_right.setVisible(false);

        }
        initialGridPane.getChildren().addAll(buttons);
        initialGridPane.getChildren().addAll(textFields);
        initialGridPane.getChildren().addAll(boxes);
        Button nextButton = new Button("Next");
        initialGridPane.setRowIndex(nextButton, noOfSamples + 3);
        initialGridPane.getChildren().add(nextButton);

        nextButton.setOnAction(actionEvent -> {
            int count = 0;

            for (Node child : initialGridPane.getChildren()) {
                int col_index = 1;
                int row_index = 0;

                try {
                    col_index = initialGridPane.getColumnIndex(child);
                    row_index = initialGridPane.getRowIndex(child);

                } catch (Exception x) {
                    System.out.println("I dunno");
                }

                if (child instanceof TextField && col_index == 0) {
                    String sampleName = ((TextField) child).getText();
                    for (Node node : initialGridPane.getChildren()) {
                        if (initialGridPane.getRowIndex(node) == row_index && initialGridPane.getColumnIndex(node) == col_index + 1) {
                            String mzml_path = ((TextField) node).getText();
                            mzmlDict.put(sampleName, mzml_path);
                        }
                        if (initialGridPane.getRowIndex(node) == row_index && initialGridPane.getColumnIndex(node) == col_index + 3) {
                            String right_path = ((TextField) node).getText();
                            rightDict.put(sampleName, right_path);
                        }
                        if (initialGridPane.getRowIndex(node) == row_index && initialGridPane.getColumnIndex(node) == col_index + 5) {
                            String left_path = ((TextField) node).getText();
                            leftDict.put(sampleName, left_path);
                        }
                        if (initialGridPane.getRowIndex(node) == row_index && initialGridPane.getColumnIndex(node) == col_index + 9) {
                            String selectedRun = (String)((ComboBox) node).getValue();
                            runDict.put(sampleName, selectedRun);
                        }
                        if(isSelected) {
                            if (initialGridPane.getRowIndex(node) == row_index && initialGridPane.getColumnIndex(node) == col_index + 7) {
                                String mzmlBack_path = ((TextField) node).getText();
                                mzmlBackDict.put(sampleName, mzmlBack_path);
                            }
                        }
                    }
                    sampleList[count] = ((TextField) child).getText();


                    count++;
                }

            }



            writeCondition(condition_json, condition_final, curr_condition, sampleList, mzmlDict, rightDict, leftDict,
                    isSelected, aminoDict, shiftDict, mzmlBackDict, runDict);

            if (x.hasNext()) {

                onNextButtonClick(actionEvent);

            } else {

                initialGridPane.getChildren().clear();
                initialGridPane.setAlignment(Pos.CENTER);
                Label finished = new Label();
                finished.setText("Configuration file has been generated");
                initialGridPane.getChildren().add(finished);


                Button goBackButton = new Button();
                initialGridPane.setRowIndex(goBackButton, 2);
                initialGridPane.setColumnIndex(goBackButton, 0);
                goBackButton.setText("Go back to main menu");
                goBackButton.setOnAction(actionEvent1 ->{
                    parent.hide();
                });
                initialGridPane.getChildren().add(goBackButton);

                compileJSON(condition_json, isDenovo, output_directory, reference_directory, precursor, fragment,
                        maxMissedCleaves);
                System.out.println("No more conditions");


            }
        });
    }

    public class commonMods {
        private String modName = null;
        private String shift = null;

        public commonMods() {
        }

        public commonMods(String modName, String shift) {
            this.modName = modName;
            this.shift = shift;
        }

        public String getModName() {
            return modName;
        }

        public void setModName(String modName) {
            this.modName = modName;
        }

        public String getShift() {
            return shift;
        }

        public void setShift(String shift) {
            this.shift = shift;
        }
    }

    public void setParent(FXMLDocumentController parent) {
        this.parent = parent;
    }

    public void popupTest2(){
        Stage primaryStage = (Stage) initialGridPane.getScene().getWindow();
        final Stage dialog = new Stage();
        dialog.setMaximized(false);
        dialog.setFullScreen(false);
        dialog.setWidth(1366);
        dialog.setHeight(900);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);

        ScrollPane s3 = new ScrollPane();
        s3.setFitToWidth(true);
        s3.setFitToHeight(true);

        BorderPane popupAnchor = new BorderPane();

        s3.setContent(popupAnchor);

        GridPane popupGridPane = new GridPane();
        popupGridPane.setAlignment(Pos.CENTER);
        popupGridPane.setHgap(10);
        popupGridPane.setVgap(10);
        ScrollBar s = new ScrollBar();
        popupGridPane.getChildren().add(s);

        popupAnchor.setCenter(popupGridPane);

        GridPane leftpopGridPane = new GridPane();
        leftpopGridPane.setAlignment(Pos.CENTER_LEFT);
        leftpopGridPane.setVgap(10);
        leftpopGridPane.setHgap(10);
        Label test = new Label();
        test.setText("This is a test");
        leftpopGridPane.getChildren().add(test);
        popupAnchor.setLeft(leftpopGridPane);

        GridPane rightpopGridPane = new GridPane();
        rightpopGridPane.setAlignment(Pos.CENTER_RIGHT);
        rightpopGridPane.setVgap(10);
        rightpopGridPane.setHgap(10);
        rightpopGridPane.getChildren().add(test);
        popupAnchor.setRight(rightpopGridPane);

        Scene dialogScene = new Scene(s3,1920, 1080);
        dialog.setScene(dialogScene);
        dialog.show();

        boolean isFwd = true;
        output_directory = outputField.getText();
        reference_directory = referenceField.getText();
        isDenovo = false;
//        if(denovo.isSelected()) {
//            isDenovo = true;
//        } else {
//            isDenovo = false;
//        }

        HashMap<String, String> curr_label_info = new HashMap<>();

        ArrayList<String> toAdd_var = new ArrayList<>();
        ArrayList<String> toAdd_fixed = new ArrayList<>();
        ArrayList<String> toAdd_var2 = new ArrayList<>();
        ArrayList<String> toAdd_fixed2 = new ArrayList<>();


        ListView fixed_list = new ListView();
        fixed_list.getItems().add("Fixed mods:");
        fixed_list.setPrefWidth(130);

        ListView var_list = new ListView();
        var_list.getItems().add("Variable mods:");
        var_list.setPrefWidth(130);


        HBox rightVbox = new HBox(var_list);
        HBox leftHbox = new HBox(fixed_list);

        leftHbox.setSpacing(10);
        rightpopGridPane.getChildren().add(rightVbox);
        leftpopGridPane.getChildren().add(leftHbox);
        rightVbox.setAlignment(Pos.CENTER_RIGHT);
        VBox.setMargin(leftHbox, new Insets(10,10,10,10));

        Label mzSettingsLabel = new Label();
        mzSettingsLabel.setText("Mass spec settings:");
        popupGridPane.setRowIndex(mzSettingsLabel, 0);
        popupGridPane.setColumnIndex(mzSettingsLabel, 0);
        popupGridPane.getChildren().add(mzSettingsLabel);

        TextField runName = new TextField();
        popupGridPane.setRowIndex(runName, 0);
        popupGridPane.setColumnIndex(runName, 1);
        popupGridPane.getChildren().add(runName);

        Label precursorLabel = new Label();
        precursorLabel.setText("Precursor M/Z tolerance:");
        popupGridPane.setRowIndex(precursorLabel, 1);
        popupGridPane.setColumnIndex(precursorLabel, 0);
        popupGridPane.getChildren().add(precursorLabel);

        TextField precursorMZ = new TextField();
        popupGridPane.setRowIndex(precursorMZ, 2);
        popupGridPane.setColumnIndex(precursorMZ, 0);
        popupGridPane.getChildren().add(precursorMZ);

        Label fragmentLabel = new Label();
        fragmentLabel.setText("Fragment M/Z tolerance:");
        popupGridPane.setRowIndex(fragmentLabel, 1);
        popupGridPane.setColumnIndex(fragmentLabel, 1);
        popupGridPane.getChildren().add(fragmentLabel);

        TextField fragmentMZ = new TextField();
        popupGridPane.setRowIndex(fragmentMZ, 2);
        popupGridPane.setColumnIndex(fragmentMZ, 1);
        popupGridPane.getChildren().add(fragmentMZ);

        Label missedLabel = new Label();
        missedLabel.setText("Max missed cleavages:");
        popupGridPane.setRowIndex(missedLabel, 3);
        popupGridPane.setColumnIndex(missedLabel, 0);
        popupGridPane.getChildren().add(missedLabel);

        TextField missedCleavages = new TextField();
        popupGridPane.setRowIndex(missedCleavages, 4);
        popupGridPane.setColumnIndex(missedCleavages, 0);
        popupGridPane.getChildren().add(missedCleavages);

        Label mzmlPathLabel = new Label();
        mzmlPathLabel.setText("mzml File Path: ");
        popupGridPane.setRowIndex(mzmlPathLabel, 3);
        popupGridPane.setColumnIndex(mzmlPathLabel, 1);
        popupGridPane.getChildren().add(mzmlPathLabel);

        ArrayList<String> mzmlPathList = new ArrayList<>();
        TextField mzmlPath = new TextField();
        popupGridPane.setRowIndex(mzmlPath, 4);
        popupGridPane.setColumnIndex(mzmlPath, 1);
        popupGridPane.getChildren().add(mzmlPath);

        Button mzmlBrowse = new Button();
        mzmlBrowse.setText("Browse");
        popupGridPane.setRowIndex(mzmlBrowse, 5);
        popupGridPane.setColumnIndex(mzmlBrowse, 1);
        popupGridPane.getChildren().add(mzmlBrowse);

        mzmlBrowse.setOnAction(actionEvent -> {
            try {
                String mzmlPathString = onBrowseButtonClick(actionEvent);
                mzmlPath.setText(mzmlPathString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Button addmzmlPath = new Button();
        addmzmlPath.setText("Add mzml file to run");
        popupGridPane.setRowIndex(addmzmlPath, 6);
        popupGridPane.setColumnIndex(addmzmlPath, 1);
        popupGridPane.getChildren().add(addmzmlPath);

        addmzmlPath.setOnAction(actionEvent -> {
            mzmlPathList.add(mzmlPath.getText());
        });


        Label aminoLabel = new Label();
        Label shiftLabel = new Label();
        aminoLabel.setText("Amino acid:");
        shiftLabel.setText("Shift:");
        popupGridPane.setRowIndex(aminoLabel, 11);
        popupGridPane.setColumnIndex(aminoLabel, 0);
        popupGridPane.setRowIndex(shiftLabel, 11);
        popupGridPane.setColumnIndex(shiftLabel, 1);

        amino_acid = new TextField();
        shift = new TextField();
        popupGridPane.setRowIndex(amino_acid, 12);
        popupGridPane.setColumnIndex(amino_acid, 0);
        popupGridPane.setRowIndex(shift, 12);
        popupGridPane.setColumnIndex(shift, 1);
        popupGridPane.getChildren().add(aminoLabel);
        popupGridPane.getChildren().add(shiftLabel);
        popupGridPane.getChildren().add(amino_acid);
        popupGridPane.getChildren().add(shift);

        ArrayList<String> fixed_mods = new ArrayList<>();
        ArrayList<String> variable_mods = new ArrayList<>();
        ArrayList<String> fixed_mods2 = new ArrayList<>();
        ArrayList<String> variable_mods2 = new ArrayList<>();

        TableView modsTable = new TableView();
        TableColumn<String, commonMods> nameColumn = new TableColumn<>("Mod Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("modName"));

        TableColumn<String, commonMods> shiftColumn = new TableColumn<>("Shift");
        shiftColumn.setCellValueFactory(new PropertyValueFactory<>("shift"));

        modsTable.getColumns().add(nameColumn);
        modsTable.getColumns().add(shiftColumn);

        modsTable.getItems().add(new commonMods("Acetylation of K", "42.01"));
        modsTable.getItems().add(new commonMods("Acetylation of protein N-term", "42.01"));
        modsTable.getItems().add(new commonMods("Carbamidomethylation of C", "57.02"));
        modsTable.getItems().add(new commonMods("Deamidation of N", "0.98"));
        modsTable.getItems().add(new commonMods("Deamidation of Q", "0.98"));
        modsTable.getItems().add(new commonMods("Oxidation of M", "15.99"));
        modsTable.getItems().add(new commonMods("Phosphorylation of S", "79.97"));
        modsTable.getItems().add(new commonMods("Phosphorylation of T", "79.97"));
        modsTable.getItems().add(new commonMods("Phosphorylation of Y", "79.97"));
        modsTable.getItems().add(new commonMods("Pyrolidone from E", "-18.01"));
        modsTable.getItems().add(new commonMods("Pyrolidone from Q", "-17.03"));
        modsTable.getItems().add(new commonMods("Pyrolidone from carbamidomethylated C", "-17.03"));

        modsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
//        popupGridPane.setRowIndex(modsTable, 13);
        popupGridPane.add(modsTable,0,13,1,2);

        ListView<String> common_mods = new ListView<>();

        common_mods.getItems().add("Acetylation of K");
        common_mods.getItems().add("Acetylation of protein N-term");
        common_mods.getItems().add("Carbamidomethylation of C");
        common_mods.getItems().add("Deamidation of N");
        common_mods.setPrefHeight(200);
//        popupGridPane.setRowIndex(common_mods, 13);
//        popupGridPane.getChildren().add(common_mods);

        ListView<String> mass_shift = new ListView<>();
        mass_shift.getItems().add("42.01");
        mass_shift.getItems().add("42.01");
        mass_shift.getItems().add("57.02");
        mass_shift.getItems().add("0.98");
        mass_shift.setPrefHeight(200);
        popupGridPane.setRowIndex(mass_shift, 13);
        popupGridPane.setColumnIndex(mass_shift, 1);
//        popupGridPane.getChildren().add(mass_shift);



        Button fixed_add = new Button();
        fixed_add.setText("<< Add to fixed mods");
        popupGridPane.setRowIndex(fixed_add, 17);
        Button variable_add = new Button();
        variable_add.setText("Add to variable mods >>");
        popupGridPane.setRowIndex(variable_add, 17);
        popupGridPane.setColumnIndex(variable_add,1);
        popupGridPane.getChildren().add(fixed_add);
        popupGridPane.getChildren().add(variable_add);

        Button nextButton = new Button();
        nextButton.setText("Add run");
        popupGridPane.setRowIndex(nextButton, 30);
        popupGridPane.setColumnIndex(nextButton, 0);
        popupGridPane.getChildren().add(nextButton);

        RadioButton silac = new RadioButton();
        RadioButton tmt = new RadioButton();
        silac.setText("SILAC");
        tmt.setText("TMT");
        ToggleGroup LabelType = new ToggleGroup();
        popupGridPane.setRowIndex(silac, 18);
        popupGridPane.setRowIndex(tmt, 19);
        popupGridPane.getChildren().add(silac);
        popupGridPane.getChildren().add(tmt);
        silac.setToggleGroup(LabelType);
        tmt.setToggleGroup(LabelType);

        Label silacLabel = new Label();
        silacLabel.setText("Label: ");
        popupGridPane.setRowIndex(silacLabel, 20);
        popupGridPane.getChildren().add(silacLabel);

        ComboBox silacOptions = new ComboBox();
        silacOptions.getItems().add("Heavy");
        silacOptions.getItems().add("Light");
        silacOptions.getItems().add("No label");
        popupGridPane.setRowIndex(silacOptions, 21);
        popupGridPane.getChildren().add(silacOptions);

        ComboBox silacOptions2 = new ComboBox();
        silacOptions2.getItems().add("Heavy");
        silacOptions2.getItems().add("Light");
        silacOptions2.getItems().add("No label");
        popupGridPane.setRowIndex(silacOptions2, 22);
        popupGridPane.getChildren().add(silacOptions2);

        ComboBox silacOptions3 = new ComboBox();
        silacOptions3.getItems().add("Heavy");
        silacOptions3.getItems().add("Light");
        silacOptions3.getItems().add("No label");
        popupGridPane.setRowIndex(silacOptions3, 23);
        popupGridPane.getChildren().add(silacOptions3);

        Label conditionLabel = new Label();
        conditionLabel.setText("Condition name: ");
        popupGridPane.setRowIndex(conditionLabel, 20);
        popupGridPane.setColumnIndex(conditionLabel, 1);
        popupGridPane.getChildren().add(conditionLabel);

        Label sampleLabel = new Label();
        sampleLabel.setText("Sample name: ");
        popupGridPane.setRowIndex(sampleLabel, 20);
        popupGridPane.setColumnIndex(sampleLabel, 2);
        popupGridPane.getChildren().add(sampleLabel);




        ComboBox sampleOptions = new ComboBox();
        popupGridPane.setRowIndex(sampleOptions, 21);
        popupGridPane.setColumnIndex(sampleOptions, 1);
        popupGridPane.getChildren().add(sampleOptions);
        TextField sampleName = new TextField();
        popupGridPane.setRowIndex(sampleName, 21);
        popupGridPane.setColumnIndex(sampleName, 2);
        popupGridPane.getChildren().add(sampleName);

        ComboBox sampleOptions2 = new ComboBox();
        TextField sampleName2 = new TextField();
        popupGridPane.setRowIndex(sampleOptions2, 22);
        popupGridPane.setColumnIndex(sampleOptions2, 1);
        popupGridPane.getChildren().add(sampleOptions2);
        popupGridPane.setRowIndex(sampleName2, 22);
        popupGridPane.setColumnIndex(sampleName2, 2);
        popupGridPane.getChildren().add(sampleName2);

        ComboBox sampleOptions3 = new ComboBox();
        TextField sampleName3 = new TextField();
        popupGridPane.setRowIndex(sampleOptions3, 23);
        popupGridPane.setColumnIndex(sampleOptions3, 1);
        popupGridPane.getChildren().add(sampleOptions3);
        popupGridPane.setRowIndex(sampleName3, 23);
        popupGridPane.setColumnIndex(sampleName3, 2);
        popupGridPane.getChildren().add(sampleName3);



        for(int i=0; i < conditionNameList.size(); i++) {
            String curr_name = conditionNameList.get(i);
            sampleOptions.getItems().add(curr_name);
            sampleOptions2.getItems().add(curr_name);
            sampleOptions3.getItems().add(curr_name);
        }

        Label sampleNumbertmtLabel = new Label();
        sampleNumbertmtLabel.setText("No of samples in run: ");
        popupGridPane.setRowIndex(sampleNumbertmtLabel, 24);
        popupGridPane.getChildren().add(sampleNumbertmtLabel);

        Spinner sampleNumbertmt = new Spinner();
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,100,1);

        sampleNumbertmt.setValueFactory(valueFactory);
        popupGridPane.setRowIndex(sampleNumbertmt, 25);
        popupGridPane.getChildren().add(sampleNumbertmt);

        Button sampleNumbertmtButton = new Button();


        popupGridPane.setRowIndex(sampleNumbertmtButton, 25);
        popupGridPane.setColumnIndex(sampleNumbertmtButton, 1);
        sampleNumbertmtButton.setText("Apply");
        popupGridPane.getChildren().add(sampleNumbertmtButton);




        sampleNumbertmtButton.setOnAction(actionEvent -> {

            int noOfSamplestmt = (int) sampleNumbertmt.getValue();

            for(int i = 0; i < noOfSamplestmt; i++) {
                popupGridPane.getChildren().remove(nextButton);
                ComboBox tmtLabelField = new ComboBox();
                ComboBox tmtSampleField = new ComboBox();
                TextField tmtSampleNumber = new TextField();
                popupGridPane.setRowIndex(tmtLabelField, 29 + i);
                popupGridPane.setColumnIndex(tmtLabelField, 0);
                popupGridPane.setRowIndex(tmtSampleField, 29 + i);
                popupGridPane.setColumnIndex(tmtSampleField, 1);
                popupGridPane.setRowIndex(tmtSampleNumber, 29 + i);
                popupGridPane.setColumnIndex(tmtSampleNumber, 2);

                tmtLabelField.getItems().add("126");
                tmtLabelField.getItems().add("127N");
                tmtLabelField.getItems().add("127C");
                tmtLabelField.getItems().add("128N");
                tmtLabelField.getItems().add("128C");
                tmtLabelField.getItems().add("129C");
                tmtLabelField.getItems().add("130N");
                tmtLabelField.getItems().add("130C");
                tmtLabelField.getItems().add("131");

                for (String name : conditionNameList) {
                    tmtSampleField.getItems().add(name);
                }


                tmtLabelField.setId("tmtLabelField" + String.valueOf(i));
                tmtSampleField.setId("tmtSampleField" + String.valueOf(i));
                tmtSampleNumber.setId("tmtSampleNumber" + String.valueOf(i));

                popupGridPane.getChildren().add(tmtLabelField);
                popupGridPane.getChildren().add(tmtSampleField);
                popupGridPane.getChildren().add(tmtSampleNumber);
            }
            popupGridPane.setRowIndex(nextButton, 30+noOfSamplestmt+1);
            popupGridPane.getChildren().add(nextButton);
        });

        LabelType.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observableValue, Toggle toggle, Toggle t1) {
                if (LabelType.getSelectedToggle() == silac) {
                    sampleNumbertmt.setDisable(true);
                    sampleNumbertmtButton.setDisable(true);
                    silacOptions.setDisable(false);
                    silacOptions2.setDisable(false);
                    silacOptions3.setDisable(false);
                    sampleOptions.setDisable(false);
                    sampleOptions2.setDisable(false);
                    sampleOptions3.setDisable(false);
                }
                if (LabelType.getSelectedToggle() == tmt) {
                    sampleNumbertmt.setDisable(false);
                    sampleNumbertmtButton.setDisable(false);
                    silacOptions.setDisable(true);
                    silacOptions2.setDisable(true);
                    silacOptions3.setDisable(true);
                    sampleOptions.setDisable(true);
                    sampleOptions2.setDisable(true);
                    sampleOptions3.setDisable(true);
                }
            }
        });




        common_mods.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        fixed_add.setOnAction(actionEvent -> {

            TableView.TableViewSelectionModel selectionModel = modsTable.getSelectionModel();
            selectionModel.setSelectionMode(SelectionMode.MULTIPLE);
            modsTable.setSelectionModel(selectionModel);
            ObservableList selectedItems2 = selectionModel.getSelectedItems();


            for (Object each : selectedItems2) {

                configGeneration.commonMods x = (configGeneration.commonMods) each;
                String curr_modName = x.getModName();

                if(curr_modName.equals("Acetylation of K")) {
                    fixed_mods.add("K(42.01)");
                }
                if(curr_modName.equals("Acetylation of protein N-term")) {
                    fixed_mods.add("N(42.01)");
                }
                if(curr_modName.equals("Carbamidomethylation of C")) {
                    fixed_mods.add("C(57.02");
                }
                if(curr_modName.equals("Deamidation of N")) {
                    fixed_mods.add("N(0.98)");
                }
                if(curr_modName.equals("Deamidation of Q")) {
                    fixed_mods.add("Q(0.98)");
                }
                if(curr_modName.equals("Oxidation of M")) {
                    fixed_mods.add("M(15.99)");
                }
                if(curr_modName.equals("Phosphorylation of S")) {
                    fixed_mods.add("S(79.97)");
                }
                if(curr_modName.equals("Phosphorylation of T")) {
                    fixed_mods.add("T(79.97)");
                }
                if(curr_modName.equals("Phosphorylation of Y")) {
                    fixed_mods.add("Y(79.97)");
                }
                if(curr_modName.equals("Pyrolidone from E")) {
                    fixed_mods.add("E(-18.01)");
                }
                if(curr_modName.equals("Pyrolidone from Q")) {
                    fixed_mods.add("Q(-17.03)");
                }
                if(curr_modName.equals("Pyrolidone from carbamidomethylated C")) {
                    fixed_mods.add("C(-17.03)");
                }
                modsTable.getItems().remove(each);

            }

            if(amino_acid.getText().isEmpty() == false) {
                fixed_mods.add(amino_acid.getText() + "(" + shift.getText() + ")");
            }

            for (int i = 0; i < fixed_mods.size(); i++) {
                toAdd_fixed.add(fixed_mods.get(i));
                fixed_list.getItems().add(fixed_mods.get(i));

            }

            fixed_mods.clear();
        });

        variable_add.setOnAction(actionEvent -> {
            TableView.TableViewSelectionModel selectionModel = modsTable.getSelectionModel();
            selectionModel.setSelectionMode(SelectionMode.MULTIPLE);
            ObservableList selectedItems2 = selectionModel.getSelectedItems();


            for (Object each : selectedItems2) {

                configGeneration.commonMods x = (configGeneration.commonMods) each;
                String curr_modName = x.getModName();

                if(curr_modName.equals("Acetylation of K")) {
                    variable_mods.add("K(42.01)");

                }
                if(curr_modName.equals("Acetylation of protein N-term")) {
                    variable_mods.add("N(42.01)");
                }
                if(curr_modName.equals("Carbamidomethylation of C")) {
                    variable_mods.add("C(57.02");
                }
                if(curr_modName.equals("Deamidation of N")) {
                    variable_mods.add("N(0.98)");
                }
                if(curr_modName.equals("Deamidation of Q")) {
                    variable_mods.add("Q(0.98)");
                }
                if(curr_modName.equals("Oxidation of M")) {
                    variable_mods.add("M(15.99)");
                }
                if(curr_modName.equals("Phosphorylation of S")) {
                    variable_mods.add("S(79.97)");
                }
                if(curr_modName.equals("Phosphorylation of T")) {
                    variable_mods.add("T(79.97)");
                }
                if(curr_modName.equals("Phosphorylation of Y")) {
                    variable_mods.add("Y(79.97)");
                }
                if(curr_modName.equals("Pyrolidone from E")) {
                    variable_mods.add("E(-18.01)");
                }
                if(curr_modName.equals("Pyrolidone from Q")) {
                    variable_mods.add("Q(-17.03)");
                }
                if(curr_modName.equals("Pyrolidone from carbamidomethylated C")) {
                    variable_mods.add("C(-17.03)");
                }

                modsTable.getItems().remove(each);

            }

            if (amino_acid.getText().isEmpty() == false) {
                variable_mods.add(amino_acid.getText() + "(" + shift.getText() + ")");
            }

            for (int i = 0; i < variable_mods.size(); i++) {
                toAdd_var.add(variable_mods.get(i));
                var_list.getItems().add(variable_mods.get(i));

            }

            variable_mods.clear();

        });
        nextButton.setOnAction(actionEvent -> {
            PreTolerances.put(runName.getText(), precursorMZ.getText());
            FragTolerances.put(runName.getText(), fragmentMZ.getText());
            missedCleaves.put(runName.getText(), missedCleavages.getText());
            mzmlPaths.put(runName.getText(), mzmlPathList);
            runModsFixed.put(runName.getText(),toAdd_fixed);
            runModsFixed.put(runName.getText(), toAdd_var);
            runNameList.add(runName.getText());
            Object selected = LabelType.getSelectedToggle();
            if(selected.equals(silac)) {
                labelType.put(runName.getText(), "SILAC");
                HashMap<String, String> currLabelInfo = new HashMap<>();
                currLabelInfo.put((String) silacOptions.getValue(),(String) sampleOptions.getValue());
                currLabelInfo.put((String) silacOptions2.getValue(),(String) sampleOptions2.getValue());
                currLabelInfo.put((String) silacOptions3.getValue(),(String) sampleOptions3.getValue());
                labelInfo.put(runName.getText(), currLabelInfo);

            } else if (selected.equals(tmt)) {
                labelType.put(runName.getText(), "TMT");

                ArrayList<String> tmtLabel = new ArrayList<>();
                ArrayList<String> tmtSample= new ArrayList<>();
                ArrayList<String> tmtSampleNo = new ArrayList<>();
                System.out.println("Just before for loop");
                for (Node child : popupGridPane.getChildren()) {
                    if(child instanceof Button){
                        System.out.println("Button skipped");
                        continue;
                    }
                    System.out.println("Starting for loop");
                    String currTMTLabel;
                    String currTMTSample;
                    String currTMTSampleNo;
                    System.out.println(popupGridPane.getRowIndex(child));
                    if (popupGridPane.getRowIndex(child) == null) {
                        continue;
                    }
                    if (popupGridPane.getRowIndex(child) >= 29) {
                        System.out.println("Is this check ever passed?");
                        if (popupGridPane.getColumnIndex(child) == null) {
                            System.out.println("Column index null");
                            continue;
                        }
                        if (popupGridPane.getColumnIndex(child) == 0) {
                            currTMTLabel = (String) ((ComboBox) child).getValue();
                            tmtLabel.add(currTMTLabel);
                            System.out.println("Adds label");
                        }
                        if (popupGridPane.getColumnIndex(child) == 1) {
                            currTMTSample = (String) ((ComboBox) child).getValue();
                            tmtSample.add(currTMTSample);
                            System.out.println("Adds sample");
                        }
                        if (popupGridPane.getColumnIndex(child) == 2) {
                            currTMTSampleNo = ((TextField) child).getText();
                            tmtSampleNo.add(currTMTSampleNo);
                            System.out.println("Adds sample no");
                        }

                    }

                }

                tmtLabelMap.put(runName.getText(), tmtLabel);
                tmtSampleMap.put(runName.getText(), tmtSample);
                tmtSampleNolMap.put(runName.getText(), tmtSampleNo);
                System.out.println("Adds to hashmap");


            }

            for (Map.Entry<String,HashMap<String, String>> entry : labelInfo.entrySet())
                System.out.println("Key = " + entry.getKey() +
                        ", Value = " + entry.getValue());
            runList.getItems().add(runName.getText());

            for (Node component : initialGridPane.getChildren()) {
                if (component instanceof ComboBox) {
                    for (int i = 0; i < runNameList.size(); i++) {
                        ((ComboBox) component).getItems().add(runNameList.get(i));
                    }
                }
            }
        });
    }

    public void writeCondition(JSONObject condition_json, org.json.simple.JSONArray condition_final, String condition_name,
                               String[] sampleList, Map<String, String> mzmlDict, Map<String, String> rightDict,
                               Map<String, String> leftDict, boolean isSelected, Map<String, String> aminoDict,
                               Map<String, String> shiftDict, Map<String, String> mzmlBackDict, Map<String, String> runDict) {

        JSONObject samples = new JSONObject();
        String curr_amino = aminoDict.get(condition_name);
        String curr_shift = shiftDict.get(condition_name);
        JSONObject runFinal = new JSONObject();



        for (int i = 1; i <= sampleList.length; i++) {
            JSONObject sample_details = new JSONObject();
            String sampleName = sampleList[i - 1];

            sample_details.put("mzml", runDict.get(sampleName));

            sample_details.put("left", rightDict.get(sampleName));
            sample_details.put("right", leftDict.get(sampleName));
            samples.put(sampleName, sample_details);

        }
        JSONObject mod_details = new JSONObject();



        JSONObject mod = new JSONObject();


        mod.put("samples", samples);

        condition_json.put(condition_name, mod);
        JSONObject conditions = new JSONObject();
        conditions.put("condition",condition_json);


        condition_final.add(conditions);


    }

    public void compileJSON(JSONObject condition, boolean isDenovo, String output_directory, String reference_directory,
                            String precursor, String fragment, String maxMissedCleaves) {


        JSONObject runFinal = new JSONObject();
        org.json.simple.JSONArray conditions = new org.json.simple.JSONArray();


        JSONObject final_condition = new JSONObject();
        final_condition.put("output", output_directory);


        if (isDenovo) {
            final_condition.put("referenceProteome", reference_directory);
        } else {
            final_condition.put("referenceGenome", reference_directory);
        }
        final_condition.put("conditions", condition);

        for (int i = 0; i < runNameList.size(); i++) {

            String curr_key = runNameList.get(i);

            JSONObject massSpecSettings_details = new JSONObject();
            massSpecSettings_details.put("Precursor M/Z tolerance", PreTolerances.get(curr_key));
            massSpecSettings_details.put("Fragment M/Z tolerance", FragTolerances.get(curr_key));
            massSpecSettings_details.put("Max missed cleavages", missedCleaves.get(curr_key));




            JSONObject runFinalDetails = new JSONObject();

            JSONObject run_mod_details = new JSONObject();

            run_mod_details.put("fixed", runModsFixed.get(curr_key));
            run_mod_details.put("variable", runModsVar.get(curr_key));
            runFinalDetails.put("modifications", run_mod_details);


            runFinalDetails.put("files", mzmlPaths.get(curr_key));


            JSONObject labelDetails = new JSONObject();

            if(labelType.get(curr_key).equals("SILAC")) {
                HashMap<String, String> curr_labels = labelInfo.get(curr_key);


                for (Map.Entry<String, String> entry : curr_labels.entrySet()) {
                    System.out.println("Key = " + entry.getKey() +
                            ", Value = " + entry.getValue());
                    if (entry.getKey() != null) {
                        labelDetails.put(entry.getKey(), entry.getValue());

                    }
                }
            }
            else {

                ArrayList<String> curr_labels = tmtLabelMap.get(curr_key);

                ArrayList<String> curr_samples = tmtSampleMap.get(curr_key);

                ArrayList<String> curr_sampleNos = tmtSampleNolMap.get(curr_key);

                System.out.println(curr_labels);
                for(int j = 0; j < curr_labels.size(); j++) {
                    System.out.println("Run: " + j);
                    labelDetails.put(curr_samples.get(j)+"/"+curr_sampleNos.get(j), curr_labels.get(j));
                }
                System.out.println("Adding them works");
            }

            runFinalDetails.put(labelType.get(curr_key), labelDetails);
            runFinalDetails.put("Mass spec settings", massSpecSettings_details);

            runFinal.put(curr_key, runFinalDetails);

            System.out.println("For loop succesfully ran");

        }
        final_condition.put("mzml", runFinal);


        conditions.add(final_condition);

        String prettyJSON = prettyJSON(conditions.toJSONString());

        try (FileWriter file = new FileWriter("conditions.json")) {

            file.write(prettyJSON);
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static String prettyJSON(String jsonString) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(jsonString);
        String prettyJsonString = gson.toJson(je);
        System.out.println(prettyJsonString);
        return prettyJsonString;
    }

}
