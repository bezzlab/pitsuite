package Controllers;

import FileReading.AllGenesReader;
import TablesModels.FoldChangeTableModel;
import TablesModels.GoTerm;
import TablesModels.KeggPathway;
import TablesModels.SplicingEventsTableModel;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.controlsfx.control.textfield.TextFields;
import org.dizitart.no2.Document;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static org.dizitart.no2.filters.Filters.eq;

public class KeggController implements Initializable {

    @FXML
    private Label selectedGeneLabel;
    @FXML
    private JFXTextField showKeggField;
    @FXML
    private ImageView keggView;
    @FXML
    private VBox container;
    @FXML
    private TitledPane selectedGenePane;
    @FXML
    private TableView<KeggPathway> geneKeggTable;
    @FXML
    private TableColumn<KeggPathway, String> geneKeggIdColumn;
    @FXML
    private TableColumn<KeggPathway, String> geneKeggDefColumn;
    @FXML
    private TitledPane filterPane;
    @FXML
    private TableView<KeggPathway> filterTable;
    @FXML
    private TableColumn<KeggPathway, String> filterIdColumn;
    @FXML
    private TableColumn<KeggPathway, String> filterDefColumn;
    @FXML
    private JFXTextField searchField;

    private Controller parentController;
    private AllGenesReader allGenesReader;
    private boolean keggLoaded;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        keggLoaded = false;

        filterIdColumn.setCellValueFactory( new PropertyValueFactory<>("id"));
        filterDefColumn.setCellValueFactory( new PropertyValueFactory<>("name"));
        filterIdColumn.prefWidthProperty().bind(filterTable.widthProperty().divide(3));
        filterDefColumn.prefWidthProperty().bind(filterTable.widthProperty().divide(3/2));

        geneKeggIdColumn.setCellValueFactory( new PropertyValueFactory<>("id"));
        geneKeggDefColumn.setCellValueFactory( new PropertyValueFactory<>("name"));
        geneKeggIdColumn.prefWidthProperty().bind(geneKeggTable.widthProperty().divide(3));
        geneKeggDefColumn.prefWidthProperty().bind(geneKeggTable.widthProperty().divide(3/2));

        keggView.fitWidthProperty().bind(container.widthProperty());


    }


    public void setParentController(Controller parentController, AllGenesReader allGenesReader){
        this.parentController = parentController;
        this.allGenesReader = allGenesReader;

        TextFields.bindAutoCompletion(searchField, allGenesReader.getAllKeggNames());
        TextFields.bindAutoCompletion(showKeggField, allGenesReader.getAllKeggNames());
        keggLoaded = true;
    }

    @FXML
    private void addFilter(){
        filterTable.getItems().add(allGenesReader.getAllPathways().stream().filter(kegg -> searchField.getText()
                .equals(kegg.getName())).findFirst().orElse(null));
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
    @FXML
    private void showKegg(){

        String keggId = allGenesReader.getAllPathways().stream().filter(kegg -> showKeggField.getText()
                .equals(kegg.getName())).findFirst().orElse(null).getId();

        preparePathviewPlot(keggId);

        Platform.runLater(() -> {
            keggView.setImage(new Image("file:plots/"+keggId+".pathview.png"));
//            keggView.setFitWidth(keggView.getViewport().getWidth());
//            keggView.setFitHeight(keggView.getViewport().getHeight());
        });

    }


    public boolean isKeggLoaded() {
        return keggLoaded;
    }

    private void preparePathviewPlot(String keggId){


        PrintWriter writer;
        try {
            writer = new PrintWriter("Rscripts/pathviewData.csv", StandardCharsets.UTF_8);

            writer.println(",fc");

            if(parentController.getClass()==DgeTableController.class){
                DgeTableController controller = (DgeTableController) parentController;
                for(FoldChangeTableModel row: controller.getTableData()){
                    writer.println(row.getGeneSymbol()+","+row.getLogFoldChange());
                }
            }else if(parentController.getClass()==SplicingTableController.class){
                SplicingTableController controller = (SplicingTableController) parentController;
                for(SplicingEventsTableModel row: controller.getDataTableBiggestGeneDpsi()){
                    writer.println(row.getGeneSymbol()+","+row.getDeltaPsi());
                }
            }

            writer.close();

            ProcessBuilder pb = new ProcessBuilder("Rscript", "Rscripts/pathview.R", "Rscripts/pathviewData.csv",
                    "plots/pathview.jpeg", keggId);

            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader inStreamReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line = inStreamReader.readLine();
            while (line != null) {
                line = inStreamReader.readLine();
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<KeggPathway> getSelectedKeggFilters(){
        return filterTable.getItems();
    }

    public boolean isInKegg(FoldChangeTableModel row){
        ArrayList<KeggPathway> geneKeggs = allGenesReader.getKegg(row.getGeneSymbol());

        if(filterTable.getItems().size()==0) return true;

        if(geneKeggs==null) return false;

        for(KeggPathway geneKegg: geneKeggs){
            for(KeggPathway keggPathway: filterTable.getItems()){
                if(geneKegg.equals(keggPathway)){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isInKegg(String gene){
        ArrayList<KeggPathway> geneKeggs = allGenesReader.getKegg(gene);

        if(filterTable.getItems().size()==0) return true;

        if(geneKeggs==null) return false;

        for(KeggPathway geneKegg: geneKeggs){
            for(KeggPathway keggPathway: filterTable.getItems()){
                if(geneKegg.equals(keggPathway)){
                    return true;
                }
            }
        }
        return false;
    }

    public void setKeggGeneTable(String geneSymbol){

        if (!geneSymbol.equals(selectedGeneLabel.getText())){ // not the same gene
            geneKeggTable.getItems().clear();

            selectedGeneLabel.setText(geneSymbol);
            new Thread(() -> {

                ArrayList<KeggPathway> keggPathways = allGenesReader.getKegg(geneSymbol);
                if (keggPathways != null) {
                    for (KeggPathway kegg: keggPathways){
                        geneKeggTable.getItems().add(kegg);
                        System.out.println(kegg);
                    }
                }
            }).start();

        }

    }

}
