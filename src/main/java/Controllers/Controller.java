package Controllers;

import javafx.fxml.Initializable;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public abstract class Controller implements Initializable {



    ResultsController parentController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setParentControler(ResultsController parent, JSONObject settings, String databaseProjectName){
        this.parentController = parent;
    }

    public void goToGeneBrowser(String gene){
        parentController.showGeneBrowser(gene);
    }

    public ArrayList<String> getSelectedGeneNames() {
        return null;
    }

    public String getSelectedComparison(){
        return "";
    }
}
