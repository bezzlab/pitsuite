package Controllers;

import Singletons.Database;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Pair;
import netscape.javascript.JSObject;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.Filter;
import org.dizitart.no2.NitriteCollection;
import org.json.simple.JSONObject;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.DoubleBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ResourceBundle;

import static org.dizitart.no2.filters.Filters.*;

public class SystemController implements Initializable {

    @FXML
    private ComboBox<String> sortCombobox;
    @FXML
    private TextField geneField;
    @FXML
    private ComboBox<String> typeCombo;
    @FXML
    private WebView webview;

    int nodesLimit = 10;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        typeCombo.setItems(FXCollections.observableArrayList(
                "Transcription factor targets",
                "Transcription factor source"
        ));

        sortCombobox.setItems(FXCollections.observableArrayList(
                "Decreasing DGE",
                "Increasing DGE"
        ));
    }

    private String getNodes(String gene, String targets) {


        ArrayList<String> nodes = new ArrayList<>();
        ArrayList<Pair<Integer, Integer>> links = new ArrayList<>();

        ArrayList<String> nodesJson = new ArrayList<>();
        ArrayList<String> linksJson = new ArrayList<>();

        ArrayList<String> genes = new ArrayList<>();


        nodes.add(gene);
        nodesJson.add("{id:" + (nodes.size() - 1) + ", label: \"" +
                nodes.get(nodes.size() - 1) + "\", shape: \"ellipse\", color: \"#BF9D7A\"}");

        ArrayList<GeneValue> genesValues = new ArrayList<>();
        Cursor dgeFindCursor = Database.getDb().getCollection("Nsivssi_dge").find(in("symbol", targets.split(",")));


        for (Document dgeDoc : dgeFindCursor) {
            genesValues.add(new GeneValue((String) dgeDoc.get("symbol"), (double) dgeDoc.get("log2fc")));
        }

        genesValues.sort(Comparator.comparing(GeneValue::getValue));

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        int c = 1;

        for (GeneValue target : genesValues) {
            if(c<nodesLimit) {
                if (target.getValue() < min)
                    min = target.getValue();
                else if (target.getValue() > max)
                    max = target.getValue();
            }
            c++;
        }



        c = 1;
        for (GeneValue target : genesValues) {
            if(c<nodesLimit) {



                nodes.add(target.getGene());
                nodesJson.add("{id:" + c + ", label: \"" +
                        target.getGene() + "\", shape: \"ellipse\", color: \""+getHexColor(target.getValue(), min, max)+"\"}");

                Pair<Integer, Integer> pepPair = new Pair<>(0, nodes.size() - 1);
                if (!links.contains(pepPair)) {
                    links.add(pepPair);
                }
                c++;
            }
        }


        StringBuilder nodesStr = new StringBuilder("[");
        StringBuilder linksStr = new StringBuilder("[");

        for (int i = 0; i < nodesJson.size(); i++) {


            if (i < nodes.size() - 1) {
                nodesStr.append(nodesJson.get(i)).append(",");
            } else {
                nodesStr.append(nodesJson.get(i)).append("]");
            }
        }


        for (int i = 0; i < links.size(); i++) {

            Pair<Integer, Integer> pair = links.get(i);

            if (i < links.size() - 1) {
                linksStr.append("{from:").append(pair.getKey()).append(", to:  ").append(pair.getValue()).append("},");
            } else {
                linksStr.append("{from:").append(pair.getKey()).append(", to: ").append(pair.getValue()).append("}]");
            }

        }


        return "genNetwork(" + nodesStr + ", " + linksStr + "," + 1800 + ")";

    }

    public void load(String gene, String type) {
        URL yahoo = null;
        try {
            String urlStr = "http://localhost:5000/";
            if(type.equals("Transcription factor targets")){
                urlStr+="tfTargets?gene=";
            }else if(type.equals("Transcription factor source")){
                urlStr+="tfSource?gene=";
            }


            urlStr+=gene;


            yahoo = new URL(urlStr);
            URLConnection yc = yahoo.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            yc.getInputStream()));
            String inputLine;

            StringBuffer res = new StringBuffer();

            while ((inputLine = in.readLine()) != null)
                res.append(inputLine);
            in.close();

            WebEngine webEngine = webview.getEngine();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    String command = getNodes(gene, res.toString());

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {

                                webEngine.executeScript(command);
                            } else {
                                webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                                    if (newState == Worker.State.SUCCEEDED) {
                                        try {

                                            webEngine.executeScript(new String(Files.readAllBytes(Paths.get(getClass().getResource("/javascript/vis.js").toURI()))));
                                            webEngine.executeScript(new String(Files.readAllBytes(Paths.get(getClass().getResource("/javascript/systemNetwork.js").toURI()))));

                                            //JSObject jsobj = (JSObject) webEngine.executeScript("window");

                                            //jsobj.setMember("java", new PeptideTableController.SelectNodeBridge());


                                            webEngine.executeScript(command);
                                        } catch (IOException | URISyntaxException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                });
                                webview.getEngine().load(getClass().getResource("/html/mynetwork.html").toString());
                            }
                        }

                    });
                }
            }).start();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void confirmGene(MouseEvent mouseEvent) {
        load(geneField.getText(), typeCombo.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void decreaseNodes(MouseEvent mouseEvent) {
        nodesLimit-=5;
        load(geneField.getText(), typeCombo.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void increaseNodes(MouseEvent mouseEvent) {
        nodesLimit+=5;
        load(geneField.getText(), typeCombo.getSelectionModel().getSelectedItem());
    }

    class GeneValue{
        String gene;
        Double value;

        public GeneValue(String gene, Double value) {
            this.gene = gene;
            this.value = value;
        }

        public String getGene() {
            return gene;
        }

        public Double getValue() {
            return value;
        }
    }

    public String getHexColor(double value, double min, double max){


        System.out.println(value+" "+min+" "+max);

        double hue = Color.BLUE.getHue() + (Color.RED.getHue() - Color.BLUE.getHue()) * (value - min) / (max - min) ;

        Color color = Color.hsb(hue, 1., 1.);

        return String.format("#%02X%02X%02X",
                ((int)color.getRed())*255,
                ((int)color.getGreen())*255,
                ((int)color.getBlue())*255);

    }
}
