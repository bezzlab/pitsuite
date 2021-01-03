package Singletons;

import Controllers.GeneBrowserController;
import Controllers.PeptideTableController;
import Controllers.ResultsController;
import javafx.scene.Scene;

public class ControllersBasket {

    private static GeneBrowserController geneBrowserController;
    private static PeptideTableController peptideTableController;
    private static ResultsController resultsController;
    private static Scene scene;


    public static GeneBrowserController getGeneBrowserController() {
        return geneBrowserController;
    }

    public static void setGeneBrowserController(GeneBrowserController geneBrowserController) {
        ControllersBasket.geneBrowserController = geneBrowserController;
    }

    public static PeptideTableController getPeptideTableController() {
        return peptideTableController;
    }

    public static void setPeptideTableController(PeptideTableController peptideTableController) {
        ControllersBasket.peptideTableController = peptideTableController;
    }

    public static ResultsController getResultsController() {
        return resultsController;
    }

    public static void setResultsController(ResultsController resultsController) {
        ControllersBasket.resultsController = resultsController;
    }

    public static Scene getScene() {
        return scene;
    }

    public static void setScene(Scene scene) {
        ControllersBasket.scene = scene;
    }
}
