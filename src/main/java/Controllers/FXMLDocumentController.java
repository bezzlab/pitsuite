package Controllers;


import Controllers.PITrun.PITRunLocalController;
import Controllers.config_generation.ConfigGenerationController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import mongoDB.DatabaseGeneration;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class FXMLDocumentController implements Initializable {

    private PITRunLocalController PITLocalRunnerController;
    @FXML
    private VBox runPitLocalPane;
    @FXML
    private ListView projectsListview;
    @FXML
    private VBox runPitPane;
    @FXML
    private PITCloudController PITCloudController;
    @FXML
    private ConfigGenerationController configGenerationController;
    @FXML
    private MenuItem openProjectMenuItem;
    @FXML
    private Button newProject;
    @FXML
    private ProgressBar loadingBar;
    @FXML
    private Label progressDialog;
    @FXML
    private SplitPane splitPane;
    @FXML
    private VBox configPane;
    @FXML
    Button genConfigFile;
    @FXML
    private Button existingProject;
    @FXML
    private Label directoryLabel;
    @FXML
    private Label directoryToSaveDBLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private TextField directoryFieldTextField;
    @FXML
    private TextField pathTosaveDatabaseTextField;
    @FXML
    private Button directoryFieldBrowseButton;
    @FXML
    private Button pathTosaveDatabaseButton;
    @FXML
    private TextField newProjNameTextField;
    @FXML
    private Button createButton;


    private String existingProjectsJsonPath;
    private Stage stage;
    String currDatabase = null;

    private static FXMLDocumentController instance;



    public void setStage(Stage stage){
        this.stage = stage;
    }

    public void configGen() {
        configPane.setVisible(true);
        splitPane.setVisible(false);
        runPitPane.setVisible(false);
    }


    public void hide(){
        configPane.setVisible(false);
        runPitPane.setVisible(false);
        splitPane.setVisible(true);
    }

    public void newProject() {
        newProject.setVisible(false);
        existingProject.setVisible(false);
        directoryFieldTextField.setVisible(true);
        directoryLabel.setVisible(true);
        directoryToSaveDBLabel.setVisible(true);
        directoryFieldBrowseButton.setVisible(true);
        nameLabel.setVisible(true);
        newProjNameTextField.setVisible(true);
        createButton.setVisible(true);
        genConfigFile.setVisible(false);
        pathTosaveDatabaseTextField.setVisible(true);
        pathTosaveDatabaseButton.setVisible(true);
    }

    public void browse() {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        instance = this;
        directoryFieldTextField.setEditable(false); // not possible to edit the path, only from the one extracted with the button
        pathTosaveDatabaseTextField.setEditable(false);
        createButton.setDisable(true);
        pathTosaveDatabaseTextField.setVisible(false);
        pathTosaveDatabaseButton.setVisible(false);

//        newProjectButtonsVBox.setVisible(true);
//        projectImportDetailsVBox.setVisible(false);
//        importingLoadingVBox.setVisible(false);


        // create previously existing projects json
        existingProjectsJsonPath = "./existing.json";

        progressDialog.setVisible(false);
        loadingBar.setVisible(false);
        configPane.setVisible(false);
        directoryFieldBrowseButton.setVisible(false);
        directoryFieldTextField.setVisible(false);
        directoryLabel.setVisible(false);
        directoryToSaveDBLabel.setVisible(false);
        newProjNameTextField.setVisible(false);
        nameLabel.setVisible(false);
        createButton.setVisible(false);

        File existingProjects = new File(existingProjectsJsonPath);
         if (!existingProjects.exists()) {
             try {
                 existingProjects.createNewFile();

                 FileWriter existingProjJsonWriter = new FileWriter(existingProjectsJsonPath);
                 org.json.simple.JSONArray jsonArray = new org.json.simple.JSONArray();
                 existingProjJsonWriter.write(jsonArray.toString());
                 existingProjJsonWriter.flush();
                 existingProjJsonWriter.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }

         // read existing projects
//         FileInputStream fis = null;
        JSONParser parser = new JSONParser();
        org.json.simple.JSONArray existingProjectsJArray = null;

        try {
            existingProjectsJArray = (org.json.simple.JSONArray) parser.parse(new FileReader(existingProjectsJsonPath));
        } catch (Exception e) {
            e.printStackTrace();
        }






        if (existingProjectsJArray != null && existingProjectsJArray.size() > 0) {
            Iterator<org.json.simple.JSONObject> jobjIt = existingProjectsJArray.iterator();
            while (jobjIt.hasNext()) {
                org.json.simple.JSONObject jobj = jobjIt.next();
                String projName = (String) jobj.get("projName");
                String projectPath = (String) jobj.get("path");

                Hyperlink existingHyperLink = new Hyperlink();
                existingHyperLink.setText(projName);

                Tooltip pathTooltip = new Tooltip(projectPath);

                Tooltip.install(existingHyperLink, pathTooltip);
//                existingProjectsPathsMap.put(projName, projectPath);

                projectsListview.getItems().add(existingHyperLink);



                // open project
                existingHyperLink.setOnAction(ActionEvent -> {
                    openProject(projectPath);
                });


            }
        }


        // buttons and listeners
//        genConfigFile.setOnAction(actionEvent -> {
//            projectsHBox.setVisible(false);
//            configPane.setVisible(true);
//        });

//        newProject.setOnAction(actionEvent -> {
//            newProjectButtonsVBox.setVisible(false);
//            projectImportDetailsVBox.setVisible(true);
//        });


        directoryFieldBrowseButton.setOnAction(event -> {
            DirectoryChooser chooser = new DirectoryChooser();
            File selectedFile = chooser.showDialog(((Node) event.getTarget()).getScene().getWindow());
            directoryFieldTextField.setText(selectedFile.toString());
        });


        pathTosaveDatabaseButton.setOnAction(event -> {
            DirectoryChooser chooser = new DirectoryChooser();
            File selectedFile = chooser.showDialog(((Node) event.getTarget()).getScene().getWindow());

            pathTosaveDatabaseTextField.setText(selectedFile.toString());
        });

        createButton.setOnAction(event -> {
//            importingLoadingVBox.setVisible(true);
            loadingBar.setVisible(true);
            importData(); // import the data into a database
        });




        openProjectMenuItem.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose Database File");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Database (*.db)", "*.db");
            fileChooser.getExtensionFilters().add(extFilter);

            File selectedFile = fileChooser.showOpenDialog(((MenuItem)event.getTarget()).getParentPopup().getOwnerWindow());

            if (selectedFile != null) {
                Path selectedDatabasePath = Paths.get(selectedFile.getAbsolutePath());
                openProject(selectedDatabasePath.toString());
            }
        });


        // listener for the buttons
        newProjNameTextField.textProperty().addListener((observableValue, s, t1) -> {
            String newProjName = newProjNameTextField.getText().strip();
            if (newProjName.length() > 0 && directoryFieldTextField.getText().length() > 0 && directoryFieldTextField.getText().length() > 0) {
                createButton.setDisable(false);
            }
        });

        directoryFieldTextField.textProperty().addListener((observableValue, s, t1) -> {
            String newProjName = newProjNameTextField.getText().strip();
            if (newProjName.length() > 0 && directoryFieldTextField.getText().length() > 0 && directoryFieldTextField.getText().length() > 0) {
                createButton.setDisable(false);
            }
        });

        pathTosaveDatabaseTextField.textProperty().addListener((observableValue, s, t1) -> {
            String newProjName = newProjNameTextField.getText().strip();
            if (newProjName.length() > 0 && directoryFieldTextField.getText().length() > 0 && directoryFieldTextField.getText().length() > 0) {
                createButton.setDisable(false);
            }
        });






    }

    /**
     * opens an existing database.
     * @param projectPath path to database
     */
    private void openProject(String projectPath){

        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());

        FXMLLoader fxmlLoader = new FXMLLoader(FXMLDocumentController.class.getResource("/results" + ".fxml"));
        Parent root;
        try {
            root = fxmlLoader.load();
            ResultsController resultsController = fxmlLoader.getController();
            resultsController.setStage(stage);

            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

            if(screenBounds.getWidth() > 3500 && screenBounds.getHeight() > 200){
                scene.getStylesheets().add(getClass().getResource("/cssStyleSheets/css.css").toExternalForm());
            }

            stage.close();
            stage.setScene(scene);

            stage.show();
            stage.setMaximized(true);

            resultsController.setProjectName(projectPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void importData (){
        final String[] databasePath = new String[1];
        final boolean[] wasDBImportedCorrectly = {false};

        newProjNameTextField.setEditable(false); // not editable while importing
        createButton.setDisable(true);

        // get new project name, which is used as name for the database
        String newProjName = newProjNameTextField.getText();
        currDatabase = newProjName;

        Path newProjSelectedPath = Paths.get(directoryFieldTextField.getText());
        Path pathToSaveDB = Paths.get(pathTosaveDatabaseTextField.getText());

        // check if the folder exist...
        // TODO: add here a check to see if the minimum required files are in the folder...
        File selectedPathFile = new File(directoryFieldTextField.getText());
        if (!selectedPathFile.exists()){
            System.out.println("selected path doesn't exist!!");
        }

        // buttons visibility
        progressDialog.setVisible(true);
        progressDialog.setText("Ready for import");
        loadingBar.setVisible(true);
//            loadingBar.setProgress(0.0);


        // generate database
        loadingBar.setVisible(true);
        progressDialog.setVisible(true);

        // Creating the pool of threads for the executorService used by the database generation and updating the ui
        // during import
        ExecutorService executorService = Executors.newFixedThreadPool(10);


        Future future = executorService.submit(() -> {
            DatabaseGeneration databaseGeneration =  new DatabaseGeneration();
            try {
                // generate a folder where the database is going to be saved...
                databasePath[0] = pathToSaveDB + "/" + newProjName + ".db";

                wasDBImportedCorrectly[0] = databaseGeneration.genDatabase(newProjSelectedPath.toString(), databasePath[0], loadingBar, progressDialog);

                executorService.shutdown();

                newProjNameTextField.setEditable(true); // not editable while importing

                // if project was well imported...
                // to write a new proj into the existing projects JSON
                JSONParser parser = new JSONParser();
                org.json.simple.JSONArray existingProjectsJArray = null;
                try {
                    existingProjectsJArray = (org.json.simple.JSONArray) parser.parse(new FileReader(existingProjectsJsonPath));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // save into existing projects JSON
                JSONObject projJson = new JSONObject();
                projJson.put("projName", newProjName);
                projJson.put("path", databasePath[0]);
                existingProjectsJArray.add(projJson);
                FileWriter existingProjJsonFileWriter = null;

                try {
                    existingProjJsonFileWriter = new FileWriter(existingProjectsJsonPath);
                    existingProjJsonFileWriter.write(existingProjectsJArray.toString());
                } catch (IOException e) {
                    System.out.println(e);
                } finally {
                    existingProjJsonFileWriter.flush();
                    existingProjJsonFileWriter.close();
                }




            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.gc(); // garbage collector

                // open imported project ....or not...
                Platform.runLater(() -> {
                    afterImport(wasDBImportedCorrectly[0], databasePath[0]);
                });
            }

        });



    }

    private void afterImport(boolean wasImportSuccesful, String databasePath){
//         garbage collector after import
        System.gc();
        System.out.println("DocCtrl L422 ImportData after import...");
        // needs to be in the gui thread
        if (wasImportSuccesful){
            // open the project after import
            openProject(databasePath);
        } else { // remove db if not imported correctly
            newProjNameTextField.setEditable(true); // not editable while importing
            createButton.setDisable(false);
            try {
                File dbFile = new File(databasePath);
                dbFile.delete();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    @FXML
    public void onRunPIT() {
        splitPane.setVisible(false);
        runPitPane.setVisible(true);
    }

    public Stage getStage() {
        return stage;
    }

    public static FXMLDocumentController getInstance(){ return instance; }

    public void onRunPITLocally(ActionEvent actionEvent) {
        splitPane.setVisible(false);
        runPitLocalPane.setVisible(true);
    }
}
