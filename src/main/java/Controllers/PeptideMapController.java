package Controllers;

import FileReading.AllGenesReader;
import Singletons.Database;
import javafx.application.HostServices;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.filters.Filters;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;


public class PeptideMapController {
    private ResultsController parentController;
    private String databaseProjectName;
    @FXML
    private AnchorPane mainPane;
    @FXML
    private TextField searchBox;





    public void setParentControler(ResultsController parent, JSONObject settings, HostServices hostServices, String databaseName,
                                    AllGenesReader allGenesReader) {
        parentController = parent;
        databaseProjectName = databaseName;


    }


    public void search() {


        NitriteCollection collection = Database.getDb().getCollection("pepMap");
        NitriteCollection geneCollection = Database.getDb().getCollection("genMap");

        String peptide = searchBox.getText();
        Cursor results = collection.find(Filters.eq("peptide", peptide));
        Document first = results.firstOrDefault();
        ArrayList<String> genes = (ArrayList<String>) first.get("genes");
        String peptideResult = (String) first.get("peptide");

        // This section is generating the script to send to the html
        String genesString = "[";
        int count = 0;
        for(String gene : genes) {
            if(count >= genes.size()-1){

                genesString = genesString.concat("\"" + gene + "\"");
            } else {
                genesString = genesString.concat("\"" + gene + "\"" + ",");
            }
            count++;
        }
        genesString = genesString.concat("]");
        System.out.println(genesString);

        WebView webView = new WebView();

        WebEngine webEngine = webView.getEngine();

        String myScript = "genNetwork(\"" + peptideResult + "\", " + genesString + ")";
        System.out.println(myScript);



        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                try {

                    webEngine.executeScript(new String(Files.readAllBytes(Paths.get(getClass().getResource("/javascript/vis.js").toURI()))));
                    webEngine.executeScript(new String(Files.readAllBytes(Paths.get(getClass().getResource("/javascript/mynetwork.js").toURI()))));

                    JSObject jsobj = (JSObject) webEngine.executeScript("window");

                    webEngine.executeScript(myScript);
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }

            }
        });
        webView.getEngine().load(getClass().getResource("/html/mynetwork.html").toString());

        VBox vBox = new VBox(webView);
        vBox.setLayoutX(50);
        vBox.setLayoutY(100);
        Database.getDb().close();
        mainPane.getChildren().add(vBox);

    }

}
