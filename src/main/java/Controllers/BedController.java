package Controllers;

import Cds.Transcript;
import FileReading.Bed;
import TablesModels.BamFile;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.dizitart.no2.Nitrite;
import utilities.BioFile;

import java.util.ArrayList;
import java.util.List;

public class BedController {

    @FXML
    private VBox mainBox;
    private List<Bed> files;
    private String chr;
    private int geneStart;
    private int geneEnd;
    private List<BamFile> selectedFiles;
    private double fontSize=22;
    private double representationWidthFinal;
    private double representationHeightFinal;
    private ArrayList<Transcript> displayedTranscripts;
    private int currentStart, currentEnd;
    private Nitrite db;

    public void setBedFiles(List<BioFile> files){
        this.files = new ArrayList<>();
        for(BioFile file: files){
            this.files.add(new Bed(file.getPath()));
        }

    }

    public void showGene(String chrId, int geneStart, int geneEnd, double representationWidthFinal, double representationHeightFinal, Nitrite db){


        this.db = db;
        this.geneStart = geneStart;
        this.geneEnd = geneEnd;
        this.currentStart = geneStart;
        this.currentEnd = geneEnd;
        this.chr = chrId;
        this.representationWidthFinal = representationWidthFinal;
        this. representationHeightFinal = representationHeightFinal;

        Platform.runLater(() -> {
            mainBox.getChildren().clear();
            if(files!=null){
                for(Bed file: files){
                    displayFile(file);
                }
            }

        });


    }

    public void displayFile(Bed file){

        Pane filePane = new Pane();
//        filePane.setPrefWidth(representationWidthFinal);
//        filePane.setPrefHeight(300);
        for(Bed.Bedrow row: file.getRows()){
            if(row.getChr().equals(chr) && row.getStart() > currentStart && row.getEnd() < currentEnd){


                Rectangle rect = new Rectangle();
                rect.setFill(Color.RED);
                rect.setHeight(50);
                rect.setWidth((row.getEnd()-row.getStart())*representationWidthFinal / (currentEnd-currentStart));
                rect.setX((row.getStart()-currentStart) * representationWidthFinal / (currentEnd-currentStart));
                filePane.getChildren().add(rect);

            }
        }
        mainBox.getChildren().add(filePane);

    }

    public void show(int start, int end){
        currentStart = start;
        currentEnd = end;
        Platform.runLater(() -> {
            mainBox.getChildren().clear();

            if(files!=null){
                for(Bed file: files){
                    displayFile(file);
                }
            }

        });
    }
}
