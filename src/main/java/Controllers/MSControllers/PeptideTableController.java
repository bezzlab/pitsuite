package Controllers.MSControllers;

import Cds.PSM;
import Cds.PTM;
import Cds.Peptide;
import Controllers.PlotSaver;
import Controllers.ResultsController;
import Singletons.Database;
import TablesModels.PeptideSampleModel;
import com.jfoenix.controls.JFXComboBox;
import graphics.AnchorFitter;
import graphics.ConfidentBarChart;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import Singletons.Config;
import utilities.MSRun;
import utilities.MassSpecModificationSample;
import utilities.MassSpecSample;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

public class PeptideTableController implements Initializable {

    @FXML
    private PAGController pagController;
    @FXML
    private AnchorPane intensitiesChartContainer;

    @FXML
    private GridPane peptideDetailsGrid;
    @FXML
    private TableView<PeptideSampleModel> peptideSampleTable;
    @FXML
    private TableView<PSM> psmTable;
    @FXML
    private TableColumn<PSM, Double> psmProbabilityColumn;
    @FXML
    private TableColumn<PSM, String> psmFileColumn;
    @FXML
    private TableColumn<PSM, Integer> psmIndexColumn;
    @FXML
    private TableView<PTM> suggestedPTMFilterTable;
    @FXML
    private TableView<PTM> ptmFilterTable;
    @FXML
    private TableColumn<PTM, String> suggestedPTMNameColumn;
    @FXML
    private TableColumn<PTM, Double> suggestedPTMMassShiftColumn;
    @FXML
    private TableColumn<PTM, String> PTMNameFilterColumn;
    @FXML
    private TableColumn<PTM, Double> PTMMassShiftFilterColumn;
    @FXML
    private TableColumn<Peptide, Integer> nbGenesColumn;
    @FXML
    private TableColumn<Peptide, Double> foldChangeColumn;
    @FXML
    private AnchorPane spectrumViewer;

    @FXML
    private ComboBox<String> condACombobox;
    @FXML
    private ComboBox<String> condBCombobox;

    @FXML
    private TableColumn<PeptideSampleModel, String> peptideSampleTableSampleColumn;
    @FXML
    private TableColumn<PeptideSampleModel, Double> peptideSampleTableProbabilityColumn;
    @FXML
    private TableView<MassSpecModificationSample> modificationsTable;
    @FXML
    private TableView<Peptide> peptideTable;
    @FXML
    private TableColumn<Peptide, String> peptideColumn;
    @FXML
    private JFXComboBox<String> runCombobox;
    @FXML
    private WebView webview;
    @FXML
    private SpectrumViewerController spectrumViewerController;

    private ResultsController parentController;
    private ArrayList<Peptide> allPeptides;
    private Peptide selectedPeptide;
    private Peptide peptideToFind;
    private MSRun selectedRun;

    private static PeptideTableController instance;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        instance = this;

        peptideColumn.setCellValueFactory( new PropertyValueFactory<>("sequence"));
        peptideColumn.prefWidthProperty().bind(peptideTable.widthProperty().multiply(0.6));
        nbGenesColumn.setCellValueFactory(new PropertyValueFactory<>("nbGenes"));
        nbGenesColumn.prefWidthProperty().bind(peptideTable.widthProperty().multiply(0.15));
        foldChangeColumn.setCellValueFactory(new PropertyValueFactory<>("foldChange"));
        foldChangeColumn.prefWidthProperty().bind(peptideTable.widthProperty().multiply(0.25));

        peptideSampleTableSampleColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getSample()));
        peptideSampleTableProbabilityColumn.setCellValueFactory(cellData ->
                new ReadOnlyDoubleWrapper(cellData.getValue().getProbability()).asObject());

        peptideSampleTableSampleColumn.prefWidthProperty().bind(peptideSampleTable.widthProperty().divide(2));
        peptideSampleTableProbabilityColumn.prefWidthProperty().bind(peptideSampleTable.widthProperty().divide(2));


        psmProbabilityColumn.setCellValueFactory(new PropertyValueFactory<>("prob"));
        psmIndexColumn.setCellValueFactory(new PropertyValueFactory<>("specIndex"));
        psmFileColumn.setCellValueFactory(new PropertyValueFactory<>("file"));

        psmProbabilityColumn.prefWidthProperty().bind(psmTable.widthProperty().multiply(0.2));
        psmIndexColumn.prefWidthProperty().bind(psmTable.widthProperty().multiply(0.2));
        psmFileColumn.prefWidthProperty().bind(psmTable.widthProperty().multiply(0.6));

        peptideSampleTableProbabilityColumn.setCellFactory(tc -> new TableCell<>() {

            @Override
            protected void updateItem(Double probability, boolean empty) {
                super.updateItem(probability, empty);
                if (empty) {
                    setText(null);
                } else {
                    DecimalFormat df = new DecimalFormat("0.00##");
                    setText(df.format(probability));
                }
            }
        });

        peptideSampleTable.setRowFactory(tv -> {
            TableRow<PeptideSampleModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!(row.isEmpty())) {
                    if (event.getButton().equals(MouseButton.PRIMARY)){
                        Peptide peptide = peptideTable.getSelectionModel().getSelectedItem();

                        drawIntensitiesChart(selectedPeptide.getIntensities(row.getItem().getSample()), intensitiesChartContainer);

                        selectPeptideRun(peptide, row.getItem().getSample());

                    }

                }
            });
            return row;
        });



        peptideTable.setRowFactory(tv -> {
            TableRow<Peptide> row = new TableRow<>();
            row.setOnMouseClicked(event -> {

                modificationsTable.getItems().clear();
                psmTable.getItems().clear();
                spectrumViewerController.clear();

                if (!(row.isEmpty())) {
                    if (event.getButton().equals(MouseButton.PRIMARY)){
                        Peptide peptide = peptideTable.getSelectionModel().getSelectedItem();

                        if(!Config.isCombinedRun(runCombobox.getSelectionModel().getSelectedItem())){
                            drawIntensitiesChart(selectedRun.getIntensities(peptide.getSequence()), intensitiesChartContainer);
                        }

                        selectPeptide(peptide);

                    }

                }
            });
            return row;
        });


        modificationsTable.setRowFactory(tv -> {
            TableRow<MassSpecModificationSample> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!(row.isEmpty())) {
                    if (event.getButton().equals(MouseButton.PRIMARY)){
                        MassSpecModificationSample rowData = modificationsTable.getSelectionModel().getSelectedItem();

                        psmTable.getItems().clear();
                        for(PSM psm: rowData.getPsms()){
                            psmTable.getItems().add(psm);
                        }

                        drawIntensitiesChart(selectedRun.getIntensities(peptideTable.getSelectionModel()
                                .getSelectedItem().getSequence(), rowData.getPtms()), intensitiesChartContainer);

                    }

                }
            });
            return row;
        });


        psmTable.setRowFactory(tv -> {
            TableRow<PSM> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!(row.isEmpty())) {
                    if (event.getButton().equals(MouseButton.PRIMARY)){

                        drawIntensitiesChart(row.getItem().getIntensities(), intensitiesChartContainer);

                        spectrumViewerController.setConfig(Config.getInstance());
                        spectrumViewerController.select(psmTable.getSelectionModel().getSelectedItem()
                                , selectedRun, selectedPeptide.getSequence());
                    }

                }
            });
            return row;
        });


        //Filters

        suggestedPTMFilterTable.setRowFactory( tv -> {
            TableRow<PTM> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    PTM rowData = row.getItem();
                    suggestedPTMFilterTable.getItems().remove(rowData);
                    ptmFilterTable.getItems().add(rowData);
                }
            });
            return row ;
        });

        ptmFilterTable.setRowFactory( tv -> {
            TableRow<PTM> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    PTM rowData = row.getItem();
                    ptmFilterTable.getItems().remove(rowData);
                    suggestedPTMFilterTable.getItems().add(rowData);
                }
            });
            return row ;
        });

        suggestedPTMNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        suggestedPTMMassShiftColumn.setCellValueFactory(new PropertyValueFactory<>("massShift"));

        PTMNameFilterColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        PTMMassShiftFilterColumn.setCellValueFactory(new PropertyValueFactory<>("massShift"));
        populateSuggestedPtms();


    }

    public static PeptideTableController getInstance(){return instance;}


    public void load(){
        spectrumViewerController.setConfig(ResultsController.getInstance().getConfig());

        for ( String run : Config.getRuns()){
            runCombobox.getItems().add(run);
        }

        runCombobox.getSelectionModel().select(0);
        loadRun();
    }

    @FXML
    public void loadRun(){
        loadRun(null);
    }


    @FXML
    public void loadRun(String peptideSeq){


        selectedRun = new MSRun(runCombobox.getSelectionModel().getSelectedItem(), Config.getOutputPath());


        new Thread(() -> {

            HashSet<String> conditions = new HashSet<>();
            for ( String subrun : Config.getSubRuns(selectedRun.getName())){
                for(String sample: Config.getRunSamples(subrun))
                    conditions.add(sample.split("/")[0]);

            }

            Iterator<String> condIterator = conditions.iterator();

            condACombobox.getItems().addAll(conditions);
            condBCombobox.getItems().addAll(conditions);




            if(conditions.size()>2) {
                Platform.runLater(() -> {
                    condACombobox.getSelectionModel().select(0);
                    condBCombobox.getSelectionModel().select(1);
                });

                selectedRun.load(Database.getDb(),  runCombobox.getSelectionModel().getSelectedItem(),
                         peptideToFind, condIterator.next(), condIterator.next());
            }else
                selectedRun.load(Database.getDb(),  runCombobox.getSelectionModel().getSelectedItem(),
                         peptideToFind, null, null);


            Platform.runLater(() -> {
                peptideTable.getItems().clear();
                peptideTable.getItems().addAll(selectedRun.getAllPeptides());
                allPeptides = new ArrayList<>(selectedRun.getAllPeptides());



                if(peptideSeq!=null){
                    int i = 0;

                    for(Peptide peptide: selectedRun.getAllPeptides()){


                        if(peptide.getSequence().equals(peptideSeq)){
                            int finalI = i;


                            peptideTable.requestFocus();
                            peptideTable.getSelectionModel().select(finalI);
                            peptideTable.getFocusModel().focus(finalI);
                            peptideTable.scrollTo(finalI);
                            selectPeptide(peptide);


                            break;
                        }
                        i++;
                    }
                }
            });


        }).start();



    }

    public void findPeptideInTable(String peptideSeq, String run){


        runCombobox.getSelectionModel().select(run);

        loadRun(peptideSeq);


    }

    public void selectPeptide(Peptide peptide){

        peptideToFind=null;
        pagController.showMap(peptide.getSequence(), runCombobox.getSelectionModel().getSelectedItem());

        if(peptideSampleTable.getColumns().size()==2){
            if(!Config.hasQuantification(runCombobox.getSelectionModel().getSelectedItem())){
                TableColumn<MassSpecSample, Integer> spectralCountColumn = new TableColumn<>("Spectral count");
                spectralCountColumn.setCellValueFactory(cellData ->
                        new ReadOnlyIntegerWrapper(cellData.getValue().getSpectralCount()).asObject());
                //peptideSampleTable.getColumns().add(spectralCountColumn);
                GridPane.setRowSpan(spectrumViewer, 3);
                GridPane.setRowSpan(intensitiesChartContainer, 0);

            }else{


                GridPane.setRowSpan(spectrumViewer, 2);
                GridPane.setRowSpan(intensitiesChartContainer, 1);

            }
        }

        peptideSampleTable.getItems().clear();
        modificationsTable.getItems().clear();
        psmTable.getItems().clear();
        spectrumViewerController.clear();


        for(String run: peptide.getRuns()){
            peptideSampleTable.getItems().add(new PeptideSampleModel(run, peptide.getProbability()));
        }

        selectedPeptide = peptide;

    }


    public void selectPeptideRun(Peptide peptide, String run){

        HashMap<HashSet<PTM>, MassSpecModificationSample> ptmSamples = new HashMap<>();

        for (PSM psm: peptide.getPsms(run)){
            if (ptmSamples.containsKey(psm.getModifications())) {
                ptmSamples.get(psm.getModifications()).addPSM(psm);
            }else{
                MassSpecModificationSample modificationsSample = new MassSpecModificationSample(psm.getModifications());
                modificationsSample.addPSM(psm);
                ptmSamples.put(psm.getModifications(), modificationsSample);
            }
        }




        modificationsTable.getItems().clear();

        modificationsTable.getColumns().clear();

        TableColumn<MassSpecModificationSample, String> modificationsColumn = new TableColumn<>("Modifications");
        modificationsColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().ptmsToString()));
        modificationsTable.getColumns().add(modificationsColumn);

        TableColumn<MassSpecModificationSample, String> ptmProbabilityColumn = new TableColumn<>("Probability");
        ptmProbabilityColumn.setCellValueFactory(new PropertyValueFactory<>("probability"));
        modificationsTable.getColumns().add(ptmProbabilityColumn);

        Set<String> runSamples = selectedRun.getChannels();

        modificationsColumn.prefWidthProperty().bind(modificationsTable.widthProperty().multiply(0.8));
        ptmProbabilityColumn.prefWidthProperty().bind(modificationsTable.widthProperty().multiply(0.2));


        modificationsTable.getItems().addAll(ptmSamples.values());

    }



    private void populateSuggestedPtms(){
        suggestedPTMFilterTable.getItems().add(new PTM("S", "(Phospho (STY))"));
        suggestedPTMFilterTable.getItems().add(new PTM("T", "(Phospho (STY))"));
        suggestedPTMFilterTable.getItems().add(new PTM("Y", "(Phospho (STY))"));
        suggestedPTMFilterTable.getItems().add(new PTM("M", "(Phospho (STY))"));
    }

    @FXML
    public void filter(){
        Iterator<Peptide> it = allPeptides.iterator();
        peptideTable.getItems().clear();
        while (it.hasNext()){
            Peptide peptide = it.next();
            for(PTM ptm: ptmFilterTable.getItems()){
                if(peptide.contains(ptm)){
                    peptideTable.getItems().add(peptide);
                    break;
                }
            }

        }
    }

    public void showPeptide(Peptide peptide){
        String run = peptide.getRunName();


        if(runCombobox.getSelectionModel().getSelectedItem()==null || !runCombobox.getSelectionModel().getSelectedItem().equals("run")){
            runCombobox.getSelectionModel().select(run);
            peptideToFind = peptide;
            loadRun();
        }else{
            for(Peptide tablePeptide: peptideTable.getItems()){
                if(tablePeptide.getSequence().equals(peptide.getSequence())){
                    selectPeptide(tablePeptide);
                    return;
                }
            }
        }

    }

    public static void drawIntensitiesChart(HashMap<String, Double> intensities, Pane container){

        container.getChildren().clear();

        HashMap<String, ArrayList<Double>> conditionIntensities = new HashMap<>();
        for(Map.Entry<String, Double> entry: intensities.entrySet()){
            String condition = entry.getKey().split("/")[0];
            if(!conditionIntensities.containsKey(condition))
                conditionIntensities.put(condition, new ArrayList<>());
            conditionIntensities.get(condition).add(entry.getValue());
        }
        ArrayList<XYChart.Series> allSeries = new ArrayList<>();

        ConfidentBarChart confidentBarChart = new ConfidentBarChart();

        for (Map.Entry<String, ArrayList<Double>> entry : conditionIntensities.entrySet()) {



            int i = 0;
            for (Double intensity : entry.getValue()) {
                if (i + 1 > allSeries.size()) {
                    allSeries.add(new XYChart.Series());
                }

                allSeries.get(i).getData().add(new XYChart.Data(entry.getKey(), intensity));
                i++;
            }
            confidentBarChart.addSeries(entry.getKey(), entry.getValue());
        }

        final MenuItem saveImageItem = new MenuItem("Save plot");
        PlotSaver plotSaver = new PlotSaver("barchart");
        saveImageItem.setOnAction(event -> {
            plotSaver.setBarchartData(allSeries, (Stage) confidentBarChart.getScene().getWindow());
        });
        final MenuItem saveDataItem = new MenuItem("Save data");
        saveDataItem.setOnAction(event -> {
            plotSaver.saveBarchartData(allSeries, "Condition", "Intensity",
                    (Stage) confidentBarChart.getScene().getWindow());
        });

        final ContextMenu menu = new ContextMenu(
                saveImageItem,
                saveDataItem
        );

        confidentBarChart.setOnMouseClicked(event -> {
            if (MouseButton.SECONDARY.equals(event.getButton())) {
                menu.show(confidentBarChart.getScene().getWindow(), event.getScreenX(), event.getScreenY());
            }
        });

        confidentBarChart.draw();
        confidentBarChart.setYLegend("Intensity");
        AnchorFitter.fitAnchor(confidentBarChart);
        HBox.setHgrow(confidentBarChart, Priority.ALWAYS);
        container.getChildren().add(confidentBarChart);
    }



}
