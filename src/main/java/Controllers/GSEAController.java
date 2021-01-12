package Controllers;

import Singletons.Database;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.Filter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.dizitart.no2.filters.Filters.*;

public class GSEAController  implements Initializable {

    @FXML
    private ComboBox<String> proteinsKeggCombo;
    @FXML
    private ComboBox<String> rnaKeggCombo;
    @FXML
    private Spinner<Double> rnaLog2fcSpinner;
    @FXML
    private Spinner<Double> proteinLog2fcSpinner;
    @FXML
    private Button runButton;
    @FXML
    private Spinner<Double> rnaPvalueSpinner;
    @FXML
    private ComboBox rnaSignCombo;
    @FXML
    private CheckBox rnaAbsCheckbox;
    @FXML
    private Pane rnaImageContainer;
    @FXML
    private Spinner<Double> proteinPvalueSpinner;
    @FXML
    private ComboBox proteinSignCombo;
    @FXML
    private CheckBox proteinAbsCheckbox;
    @FXML
    private Pane proteinImageContainer;
    @FXML
    private VBox container;

    private Controller parentController;



    public GSEAController(){

    }

    public void setParentController(Controller parentController){
        this.parentController = parentController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rnaSignCombo.getItems().add(">");
        rnaSignCombo.getItems().add("<");
        proteinSignCombo.getItems().add(">");
        proteinSignCombo.getItems().add("<");

        rnaKeggCombo.getItems().add("GO");
        proteinsKeggCombo.getItems().add("GO");
        rnaKeggCombo.getItems().add("KEGG");
        proteinsKeggCombo.getItems().add("KEGG");

        rnaSignCombo.getSelectionModel().select(0);
        proteinSignCombo.getSelectionModel().select(0);

        rnaKeggCombo.getSelectionModel().select(0);
        proteinsKeggCombo.getSelectionModel().select(0);

        rnaAbsCheckbox.setSelected(true);
        proteinAbsCheckbox.setSelected(true);

        SpinnerValueFactory<Double> pvalueFactorRna = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1,
                0.05, 0.01);
        rnaPvalueSpinner.setValueFactory(pvalueFactorRna);

        SpinnerValueFactory<Double> pvalueFactorProtein = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1,
                0.05, 0.01);
        proteinPvalueSpinner.setValueFactory(pvalueFactorProtein);

        SpinnerValueFactory<Double> log2fcFactorRna = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100,
                1, 0.1);
        rnaLog2fcSpinner.setValueFactory(log2fcFactorRna);

        SpinnerValueFactory<Double> log2fcFactorProtein = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100,
                1, 0.1);
        proteinLog2fcSpinner.setValueFactory(log2fcFactorProtein);
    }

    @FXML public void onRunGsea(){

        new Thread(() -> {
            drawGSEA(rnaKeggCombo.getSelectionModel().getSelectedItem().toLowerCase(), "rna");
        }).start();
        new Thread(() -> {
            drawGSEA(rnaKeggCombo.getSelectionModel().getSelectedItem().toLowerCase(), "proteins");
        }).start();


    }

    public void drawGSEA(String GoOrKegg, String rnaOrProtein){



            Platform.runLater(() -> {
                ProgressIndicator progress = new ProgressIndicator();

                progress.setMaxSize(rnaImageContainer.getWidth()/2, rnaImageContainer.getHeight()/2);

                VBox loadingBox = new VBox();
                loadingBox.setPrefWidth(rnaImageContainer.getWidth());
                Text t = new Text("Loading GSEA");
                t.setFont(Font.font(25));
                loadingBox.getChildren().add(t);
                loadingBox.getChildren().add(progress);

                t.setTextAlignment(TextAlignment.CENTER);

                loadingBox.setAlignment(Pos.CENTER);
                if(rnaOrProtein.equals("rna")){
                    rnaImageContainer.getChildren().clear();
                    rnaImageContainer.getChildren().add(loadingBox);
                }else{
                    proteinImageContainer.getChildren().clear();
                    proteinImageContainer.getChildren().add(loadingBox);
                }
            });


            ArrayList<String> genes = getGenes(rnaOrProtein);
            analyseGetSet(genes, GoOrKegg, rnaOrProtein);
            Platform.runLater(() -> {
                ImageView viewer = new ImageView();
                if(rnaOrProtein.equals("rna")){
                    viewer.setImage(new Image("file:plots/gseaRna.jpeg"));
                    rnaImageContainer.getChildren().clear();
                    rnaImageContainer.getChildren().add(viewer);
                }

                else{
                    viewer.setImage(new Image("file:plots/gseaProteins.jpeg"));
                    proteinImageContainer.getChildren().clear();
                    proteinImageContainer.getChildren().add(viewer);
                }

            });


    }


    private ArrayList<String> getGenes(String rnaOrProtein) {

        List<Filter> filters = new ArrayList<>(1);

        if(rnaOrProtein.equals("rna")){
            if(rnaAbsCheckbox.isSelected()){
                if(rnaSignCombo.getSelectionModel().getSelectedItem().equals(">")){
                    filters.add(and(lte("padj", rnaPvalueSpinner.getValue()),
                            or(lt("log2fc", -rnaLog2fcSpinner.getValue()), gt("log2fc", rnaLog2fcSpinner.getValue())
                            )));
                }else{
                    filters.add(and(lte("padj", rnaPvalueSpinner.getValue()),
                            and(gt("log2fc", -rnaLog2fcSpinner.getValue()), lt("log2fc", rnaLog2fcSpinner.getValue())
                            )));
                }

            }else{
                if(rnaSignCombo.getSelectionModel().getSelectedItem().equals(">")){
                    filters.add(and(lte("padj", rnaPvalueSpinner.getValue()),
                            gt("log2fc", rnaLog2fcSpinner.getValue())));
                }else{
                    filters.add(and(lte("padj", rnaPvalueSpinner.getValue()),
                            lt("log2fc", rnaLog2fcSpinner.getValue())));
                }
            }

        }




        Cursor dgeFindCursor;
        if(rnaOrProtein.equals("rna"))
            dgeFindCursor = Database.getDb().getCollection(parentController.getSelectedComparison()+"_dge")
                .find(and(filters.toArray(new Filter[]{})));
        else
            dgeFindCursor = Database.getDb().getCollection(parentController.getSelectedComparison()+"_dge")
                    .find();

        ArrayList<String> genes = new ArrayList<>(dgeFindCursor.size());



        for (Document dgeDoc : dgeFindCursor) {

            if(rnaOrProtein.equals("rna")){
                genes.add((String) dgeDoc.get("symbol"));
            }else{


                if (dgeDoc.containsKey("ms")) {

                    org.json.simple.JSONObject runsObj = (org.json.simple.JSONObject) dgeDoc.get("ms");
                    for (Object runName : runsObj.keySet()) {


                        org.json.simple.JSONObject run = (org.json.simple.JSONObject) runsObj.get(runName);

                        Double padj = null;
                        if (run.containsKey("padj")) {
                            padj = (Double) run.get("padj");
                        }

                        Double log2fc = (Double) run.get("log2fc");

                        if (proteinAbsCheckbox.isSelected()) {
                            if (rnaSignCombo.getSelectionModel().getSelectedItem().equals(">")) {
                                if (log2fc != null && Math.abs(log2fc) > proteinLog2fcSpinner.getValue()) {
                                    genes.add((String) dgeDoc.get("symbol"));
                                }
                            } else {
                                if (log2fc != null && Math.abs(log2fc) < proteinLog2fcSpinner.getValue()) {
                                    genes.add((String) dgeDoc.get("symbol"));
                                }
                            }

                        } else {
                            if (rnaSignCombo.getSelectionModel().getSelectedItem().equals(">")) {
                                if (log2fc != null && log2fc > proteinLog2fcSpinner.getValue()) {
                                    genes.add((String) dgeDoc.get("symbol"));
                                }
                            } else {
                                if (log2fc != null && log2fc < proteinLog2fcSpinner.getValue()) {
                                    genes.add((String) dgeDoc.get("symbol"));
                                }
                            }
                        }
                    }
                }
            }

        }
        return genes;
    }

    public void analyseGetSet(ArrayList<String> genes, String goOrKegg, String rnaOrProtein){


        PrintWriter writer;
        try {
            if(rnaOrProtein.equals("rna"))
                writer = new PrintWriter("Rscripts/gseaGenesRna.txt", StandardCharsets.UTF_8);
            else
                writer = new PrintWriter("Rscripts/gseaGenesProteins.txt", StandardCharsets.UTF_8);
            writer.write(String.join(",", genes));

//            writer.println(",fc");
//
//            if(parentController.getClass()==DgeTableController.class){
//                DgeTableController controller = (DgeTableController) parentController;
//                for(FoldChangeTableModel row: controller.getTableData()){
//                    writer.println(row.getGeneSymbol()+","+row.getLogFoldChange());
//                }
//            }else if(parentController.getClass()==SplicingTableController.class){
//                SplicingTableController controller = (SplicingTableController) parentController;
//                for(SplicingEventsTableModel row: controller.getDataTableBiggestGeneDpsi()){
//                    writer.println(row.getGeneSymbol()+","+row.getDeltaPsi());
//                }
//            }

            writer.close();
            ProcessBuilder pb;
            if(rnaOrProtein.equals("rna"))
                pb = new ProcessBuilder("Rscript", "Rscripts/GSEA.R",
                        goOrKegg, rnaOrProtein , String.valueOf(rnaImageContainer.getWidth()), String.valueOf(rnaImageContainer.getHeight()));
            else
                pb = new ProcessBuilder("Rscript", "Rscripts/GSEA.R",
                        goOrKegg, rnaOrProtein , String.valueOf(proteinImageContainer.getWidth()), String.valueOf(proteinImageContainer.getHeight()));


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


}
