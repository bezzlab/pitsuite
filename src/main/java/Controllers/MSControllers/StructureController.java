package Controllers.MSControllers;

import graphics.AnchorFitter;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureIO;
import org.biojava.nbio.structure.align.gui.jmol.JmolPanel;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ResourceBundle;

public class StructureController implements Initializable {

    @FXML
    private ComboBox<String> pdbCombo;
    @FXML
    private AnchorPane container;

    private JmolPanel jmolPanel;

    private static StructureController instance;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pdbCombo.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> loadId(newValue));
        instance = this;
    }

    public void load(HashSet<String> pdbIds){
        container.getChildren().clear();
        ArrayList<String> idsList = new ArrayList<>(pdbIds);
        pdbCombo.getItems().clear();
        pdbCombo.getItems().addAll(idsList);


        if(idsList.size()>0) {
            pdbCombo.getSelectionModel().select(0);
            loadId(idsList.get(0));
        }
    }

    private void loadId(String pdbId){

        new Thread(()-> {
            try {
                System.out.println(pdbId);
                Structure structure = StructureIO.getStructure(pdbId);
                jmolPanel = new JmolPanel();
                jmolPanel.setStructure(structure);



                SwingNode node = new SwingNode();
                AnchorFitter.fitAnchor(node);
                SwingUtilities.invokeLater(() -> node.setContent(jmolPanel));

                Platform.runLater(() -> {
                    container.getChildren().add(node);
                });

                jmolPanel.evalString("select * ; color chain;");
                jmolPanel.evalString("select *; spacefill off; wireframe off; cartoon on;  ");
                jmolPanel.evalString("select ligands; cartoon off; wireframe 0.3; spacefill 0.5; color cpk;");
            } catch (IOException | StructureException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void colorPositions(int start, int end){
        if(jmolPanel!=null){
            System.out.println(start+"-"+end);
            jmolPanel.evalString("select "+start+"-"+end+" ; color orange;");
        }
    }

    public void reset(){
        if(jmolPanel!=null){
            jmolPanel.evalString("select * ; color chain;");
            jmolPanel.evalString("select *; spacefill off; wireframe off; cartoon on;  ");
            jmolPanel.evalString("select ligands; cartoon off; wireframe 0.3; spacefill 0.5; color cpk;");
        }
    }

    public static StructureController getInstance() {
        return instance;
    }
}
