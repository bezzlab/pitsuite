package Controllers;


import Cds.Peptide;
import FileReading.AllGenesReader;
import TablesModels.BamFile;
import export.ProVcf;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.json.JSONObject;
import pitguiv2.Config;
import pitguiv2.Settings;
import utilities.BioFile;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;


public class ResultsController implements Initializable {


    @FXML
    public AnchorPane mainPane;
    private MutationsTableController mutationsTableController;
    private BrowserController browserController;
    private DgeTableController dgeTableController;
    private SplicingTableController splicingTableController;
    private PeptideMapController peptideMapController;
    private PeptideTableController peptideTableController;
    private TranscriptUsageController transcriptUsageController;
    // tabs
    @FXML
    private TabPane resultsTabPane;
    @FXML
    private Tab variationTab;
    @FXML
    private Tab browserTab;
    @FXML
    private Tab dgeTab;
    @FXML
    private Tab splicingTab;
    @FXML
    private MenuBar menuBar;

    private HostServices hostServices;



    private Stage stage;
    private Config config;
    private Timeline timeline;
    private String projectName;
    private JSONObject settings;
    private AllGenesReader allGenesReader;
    private Nitrite db;

    private ObservableList<BioFile> extraFiles = null;



    public void setStage(Stage stage){
        this.stage = stage;


    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){

        Menu menu1 = new Menu("File");
        MenuItem menuItem1 = new MenuItem("Close project");
        MenuItem menuItem2 = new MenuItem("Settings");
        menu1.getItems().add(menuItem1);
        menu1.getItems().add(menuItem2);

        Menu menu2 = new Menu("Gene browser");
        MenuItem menu2Item1 = new MenuItem("Bam files");
        menu2.getItems().add(menu2Item1);

        Menu menu3 = new Menu("Export");
        MenuItem menu3Item1 = new MenuItem("Variations");
        menu3.getItems().add(menu3Item1);

        menuBar.getMenus().add(menu1);
        menuBar.getMenus().add(menu2);
        menuBar.getMenus().add(menu3);

        menuItem2.setOnAction(e -> {
            showSettingsWindow();
        });
        menu2Item1.setOnAction(e -> {
            showBamWindow();
        });

        menu3Item1.setOnAction(e -> {
            saveProVcf();
        });







    }

    public void load(){

        resultsTabPane.getSelectionModel().select(2);
        FXMLLoader fxmlLoader1 = new FXMLLoader(SettingsController.class.getResource("/dgeTable" + ".fxml"));
        try {
            Parent root = fxmlLoader1.load();
            dgeTableController = fxmlLoader1.getController();
            dgeTableController.setParentController(this, settings, projectName, db, allGenesReader);
            resultsTabPane.getTabs().get(2).setContent(root);
        } catch(Exception e){
            e.printStackTrace();
        }




        Platform.runLater(() -> {
            FXMLLoader fxmlLoader2 = new FXMLLoader(SettingsController.class.getResource("/splicingTable" + ".fxml"));
            try {
                Parent root = fxmlLoader2.load();

                splicingTableController = fxmlLoader2.getController();
                splicingTableController.setParentControler(this, db, allGenesReader);
                resultsTabPane.getTabs().get(3).setContent(root);
            } catch(Exception e){
                e.printStackTrace();
            }
        });



        PauseTransition pauseTransition = new PauseTransition(Duration.seconds(0.2));
        pauseTransition.setOnFinished(event -> {
            Platform.runLater(() -> {
                FXMLLoader fxmlLoader = new FXMLLoader(SettingsController.class.getResource("/mutationsTable" + ".fxml"));
                try {
                    Parent root = fxmlLoader.load();
                    mutationsTableController = fxmlLoader.getController();
                    mutationsTableController.setParentControler(this, projectName, db, allGenesReader);
                    resultsTabPane.getTabs().get(0).setContent(root);
                } catch(Exception e){
                    e.printStackTrace();
                }
            });


            Platform.runLater(() -> {
                FXMLLoader fxmlLoader2 = new FXMLLoader(SettingsController.class.getResource("/browser" + ".fxml"));
                try {
                    Parent root = fxmlLoader2.load();

                    browserController = fxmlLoader2.getController();
                    browserController.setParentControler(this, settings, hostServices, projectName, db, allGenesReader);
                    resultsTabPane.getTabs().get(1).setContent(root);
                } catch(Exception e){
                    e.printStackTrace();
                }
            });

//            Platform.runLater(() -> {
//                FXMLLoader fxmlLoader2 = new FXMLLoader(SettingsController.class.getResource("/peptideMap" + ".fxml"));
//                try {
//                    Parent root = fxmlLoader2.load();
//
//                    peptideMapController = fxmlLoader2.getController();
//                    peptideMapController.setParentControler(this, settings, hostServices, projectName, db, allGenesReader);
//                    resultsTabPane.getTabs().get(4).setContent(root);
//                } catch(Exception e){
//                    e.printStackTrace();
//                }
//            });



            Platform.runLater(() -> {
                FXMLLoader fxmlLoader2 = new FXMLLoader(SettingsController.class.getResource("/peptideTable" + ".fxml"));
                try {
                    Parent root = fxmlLoader2.load();

                    peptideTableController = fxmlLoader2.getController();
                    peptideTableController.setParentController(this, db);
                    resultsTabPane.getTabs().get(5).setContent(root);
                } catch(Exception e){
                    e.printStackTrace();
                }
            });

            Platform.runLater(() -> {
                FXMLLoader fxmlLoader2 = new FXMLLoader(SettingsController.class.getResource("/transcriptUsage.fxml"));
                try {
                    Parent root = fxmlLoader2.load();

                    transcriptUsageController = fxmlLoader2.getController();

                    transcriptUsageController.setParentControler(this, db, allGenesReader);

                    resultsTabPane.getTabs().get(4).setContent(root);
                } catch(Exception e){
                    e.printStackTrace();
                }
            });



        });
        pauseTransition.play();


    }


    public void setProjectName(String projectName){

        this.projectName = projectName;
        db = Nitrite.builder().filePath(projectName).openOrCreate();
        loadConfig(projectName);
        settings = loadSettings();
        Settings.getInstance().setSetting(settings);


        allGenesReader = new AllGenesReader("data/kegg.csv", "data/go-basic.obo",  db);



        load();

        ChangeListener resizeListener = (observable, oldValue, newValue) -> {
            if(timeline!=null){
                timeline.stop();
            }

            timeline = new Timeline(new KeyFrame(

                    Duration.millis(300),
                    ae -> resize()));
            timeline.play();
        };

        mainPane.widthProperty().addListener(resizeListener);
        mainPane.heightProperty().addListener(resizeListener);




        //browserController.setParentControler(this, settings, hostServices, projectName);


    }

    private void loadConfig(String projectName){


        Cursor configFind = db.getCollection("config").find();
        Document configDoc = configFind.iterator().next();

        config = new Config(new JSONObject(configDoc));
    }

    private JSONObject loadSettings(){
        try {
            Scanner in = new Scanner(new FileReader("./settings.json"));
            StringBuilder sb = new StringBuilder();
            while(in.hasNext()) {
                sb.append(in.next());
            }

            return new JSONObject(sb.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }





    /**
     * When a Transcript id is selected, it shows the Browser tab,
     * with the information of the Gene (not the info of the particular transcript only).
     */
    public void showBrowserFromTranscId(String gene, int position){
        resultsTabPane.getSelectionModel().select(browserTab);
        // TODO: modify this line
        browserController.geneBrowserDisplayGeneFromId(gene, position);

    }

    public void showBrowserFromTranscId(String gene, int start, int end){
        resultsTabPane.getSelectionModel().select(browserTab);
        // TODO: modify this line
        browserController.geneBrowserDisplayGeneFromId(gene, start, end);

    }

    public Config getConfig(){return config;}

    private void showSettingsWindow(){

        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        FXMLLoader fxmlLoader = new FXMLLoader(SettingsController.class.getResource("/settings" + ".fxml"));

        Parent root = null;
        Stage newWindow = new Stage();
        try {
            root = fxmlLoader.load();
            SettingsController controller = fxmlLoader.getController();
            controller.setParentController(this);
            //controller.setStage(stage);

            Scene scene = new Scene(root, screenBounds.getWidth()/3, screenBounds.getHeight()/2);

            if(screenBounds.getWidth() > 3500 && screenBounds.getHeight() > 200){
                scene.getStylesheets().add(getClass().getResource("/cssStyleSheets/css.css").toExternalForm());
            }
            newWindow.setScene(scene);

            newWindow.sizeToScene();
            newWindow.show();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showBamWindow(){

        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        FXMLLoader fxmlLoader = new FXMLLoader(SettingsController.class.getResource("/bamMenu" + ".fxml"));

        Parent root = null;
        Stage newWindow = new Stage();
        try {
            root = fxmlLoader.load();
            BamMenuController controller = fxmlLoader.getController();
            controller.setParentController(this, db, extraFiles);
            controller.setStage(newWindow);

            Scene scene = new Scene(root, screenBounds.getWidth()/3, screenBounds.getHeight()/2);

            if(screenBounds.getWidth() > 3500 && screenBounds.getHeight() > 200){
                scene.getStylesheets().add(getClass().getResource("/cssStyleSheets/css.css").toExternalForm());
            }
            newWindow.setScene(scene);

            newWindow.sizeToScene();
            newWindow.show();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateSetting(JSONObject settings){
        browserController.updateSettings(settings);
        dgeTableController.updateSettings(settings);
    }
    

    public void showGeneBrowser(String gene){
        resultsTabPane.getSelectionModel().select(1);
        browserController.showGeneBrowser(gene);
    }

    public void showGeneBrowser(String gene, Peptide peptide){
        resultsTabPane.getSelectionModel().select(1);
        browserController.showGeneBrowser(gene, peptide);
    }

    public HostServices getHostServices() {
        return hostServices;
    }

    public void resize(){
        mutationsTableController.resize();
        browserController.resize();
        dgeTableController.resize();
        splicingTableController.resize();
    }

    public void setBrowserFiles(List<BamFile> bam, ArrayList<BioFile> bed){
        browserController.setBrowserFiles(bam, bed);
    }

    public void showPeptideTab(Peptide peptide){
        resultsTabPane.getSelectionModel().select(4);
        peptideTableController.showPeptide(peptide);

    }

    private void saveProVcf(){
        ProVcf.generate("/media/esteban/data/outputVariationPeptide2/variants.provcf", mutationsTableController.getSelectedMutations(), db);
    }

    public void setExtraFiles(ObservableList<BioFile> extraFiles){
        this.extraFiles = extraFiles;
    }


}
