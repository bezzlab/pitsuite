package Controllers;

import FileReading.Bed;
import TablesModels.BamFile;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import utilities.BioFile;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class BamMenuController implements Initializable {

    @FXML
    public TextField newFileNameField;
    @FXML
    public TextField newFilePathField;
    @FXML
    private TableView<BioFile> bamTable;
    @FXML
    public TableColumn<BioFile, String> sampleColumn;
    @FXML
    public TableColumn<BioFile, String> conditionColumn;
    @FXML
    public TableColumn<BioFile, String> pathColumn;

    private ResultsController parentController;
    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        sampleColumn.setCellValueFactory( new PropertyValueFactory<>("sample"));
        conditionColumn.setCellValueFactory( new PropertyValueFactory<>("condition"));
        pathColumn.setCellValueFactory( new PropertyValueFactory<>("path"));

    }

    public void setStage(Stage stage){
        this.stage = stage;
    }


    public void setParentController(ResultsController parentController, Nitrite db, ObservableList<BioFile> extraFiles){

        this.parentController = parentController;



        if(extraFiles==null){
            Cursor bamPathsCollection = db.getCollection("bamPaths").find();
            for (Document bamFileDoc : bamPathsCollection) {
                String bamCond = (String) bamFileDoc.get("condition");
                String bamSample = (String) bamFileDoc.get("sample");
                String bamPath = (String) bamFileDoc.get("bamPath");

                bamTable.getItems().add(new BamFile(bamPath, bamCond, bamSample));
            }
        }else{
            bamTable.getItems().addAll(extraFiles);
        }

        parentController.setExtraFiles(bamTable.getItems());



        var columns = bamTable.getColumns();

        TableColumn<BioFile, Boolean > selectedColumn = new TableColumn<>( "Selected" );
        selectedColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectedColumn.setCellFactory(p -> {
            CheckBox checkBox = new CheckBox();
            TableCell<BioFile, Boolean> cell = new TableCell<>() {
                @Override
                public void updateItem(Boolean item, boolean empty) {
                    if (empty) {
                        setGraphic(null);
                    } else {
                        checkBox.setSelected(item);
                        setGraphic(checkBox);
                    }
                }
            };
            checkBox.selectedProperty().addListener((obs, wasSelected, isSelected) ->
                    cell.getTableRow().getItem().setSelected(isSelected));
            cell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            cell.setAlignment(Pos.CENTER);
            return cell ;
        });


        columns.add( selectedColumn );

        sampleColumn.prefWidthProperty().bind(bamTable.widthProperty().multiply(0.2));
        conditionColumn.prefWidthProperty().bind(bamTable.widthProperty().multiply(0.2));
        pathColumn.prefWidthProperty().bind(bamTable.widthProperty().multiply(0.5));
        selectedColumn.prefWidthProperty().bind(bamTable.widthProperty().multiply(0.1));

    }

    @FXML
    private void apply(){
        ArrayList<BamFile> bam = new ArrayList<>();
        ArrayList<BioFile> bed = new ArrayList<>();
        for(BioFile file: bamTable.getItems()){
            if(file.getPath().endsWith(".bed")){
                bed.add(file);
            }else{
                bam.add((BamFile) file);
            }
        }
        parentController.setBrowserFiles(bam, bed);
    }



    @FXML
    private void browseFiles(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File f = fileChooser.showOpenDialog(stage);
        newFilePathField.setText(f.getAbsolutePath());
    }

    @FXML
    private void addFile(){
        bamTable.getItems().add(new BamFile(newFilePathField.getText(), newFileNameField.getText()));
        newFileNameField.setText("");
        newFilePathField.setText("");
        parentController.setExtraFiles(bamTable.getItems());
    }

}
