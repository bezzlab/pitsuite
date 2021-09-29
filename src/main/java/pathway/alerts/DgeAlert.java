package pathway.alerts;


import Controllers.DgeTableController;
import Controllers.PathwayController;
import Controllers.PathwaySideController;
import Singletons.Database;
import graphics.AnchorFitter;
import graphics.ConfidentBarChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Pair;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.filters.Filters;
import org.json.JSONObject;
import pathway.Compartment;
import pathway.Element;
import pathway.Entity;
import pathway.Gene;
import pitguiv2.Settings;

import java.util.HashMap;
import java.util.Map;


public class DgeAlert extends Alert {

    double fc;
    double pval;
    private final HashMap<String, Pair<Double, Double>> proteins = new HashMap<>();


    public DgeAlert(String gene, double fc, double pval) {
        super(gene);
        this.fc =fc;
        this.pval = pval;
    }


    public double getFc() {
        return fc;
    }

    public double getPval() {
        return pval;
    }

    public void addMsRun(String run, double fc, double pval){
        proteins.put(run, new Pair<>(fc, pval));
    }
    public void addMsRun(String run, double fc){
        proteins.put(run, new Pair<>(fc, Double.NaN));
    }

    @Override
    public String getType() {
        return "dge";
    }

    public HashMap<String, Pair<Double, Double>> getProteins() {
        return proteins;
    }

    @Override
    public void drawCell(Pane pane, TitledPane titledPane){

        AnchorPane.setBottomAnchor(pane, 0.);
        AnchorPane.setTopAnchor(pane, 0.);


        GridPane grid = new GridPane();
        ColumnConstraints c = new ColumnConstraints();
        c.setHgrow(Priority.NEVER);
        c.setPercentWidth(97);
        RowConstraints r1 = new RowConstraints();
        r1.setVgrow(Priority.NEVER);
        r1.setPercentHeight(10);
        RowConstraints r2 = new RowConstraints();
        r2.setVgrow(Priority.ALWAYS);
        r2.setPercentHeight(30);
        grid.getColumnConstraints().add(c);
        grid.getRowConstraints().add(r1);
        grid.getRowConstraints().add(r2);

        TableView<CellTableRow> changeTable = new TableView<>();
        TableColumn<CellTableRow, String> levelColumn = new TableColumn<>("Level");
        TableColumn<CellTableRow, String> fcColumn = new TableColumn<>("Fold change");
        TableColumn<CellTableRow, String> pvalColumn = new TableColumn<>("p-value");
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        fcColumn.setCellValueFactory(new PropertyValueFactory<>("fc"));
        pvalColumn.setCellValueFactory(new PropertyValueFactory<>("pval"));
        changeTable.getColumns().add(levelColumn);
        changeTable.getColumns().add(fcColumn);
        changeTable.getColumns().add(pvalColumn);



        //changeTable.prefHeightProperty().bind(Bindings.size(changeTable.getItems()).multiply(changeTable.getFixedCellSize()).add(30));

        changeTable.getItems().add(new CellTableRow("RNA", fc, pval));




        double fontSize = Settings.getInstance().getSettings().getJSONObject("Fonts").getJSONObject("charts").getInt("size");

        AnchorPane readCountsPane = new AnchorPane();

        pane.getChildren().add(grid);

        AnchorPane.setBottomAnchor(grid, 0.);
        AnchorPane.setTopAnchor(grid, 0.);
        grid.setMaxWidth(titledPane.getMaxWidth()-10);
        grid.setPrefWidth(titledPane.getMaxWidth()-10);




        grid.getChildren().add(changeTable);

        AnchorPane.setBottomAnchor(changeTable, 0.);
        AnchorPane.setTopAnchor(changeTable, 0.);


        grid.getChildren().add(readCountsPane);

        AnchorPane.setBottomAnchor(readCountsPane, 0.);
        AnchorPane.setTopAnchor(readCountsPane, 0.);


        GridPane.setRowIndex(readCountsPane, 1);


        ConfidentBarChart confidentBarChart = DgeTableController.drawSelectedGeneReadCount(gene, readCountsPane, fontSize);


        titledPane.expandedProperty().addListener((observable, oldValue, newValue) -> confidentBarChart.draw());

        int i=0;


        for(Map.Entry<String, Pair<Double, Double>> entry: proteins.entrySet()){
            VBox container = new VBox();

            grid.add(container, 0, 2+i++);
            RowConstraints r = new RowConstraints();
            r.setVgrow(Priority.ALWAYS);
            r.setPercentHeight(60);
            grid.getRowConstraints().add(r);


            changeTable.getItems().add(new CellTableRow("Protein "+entry.getKey(), entry.getValue().getKey(), entry.getValue().getValue()));

            DgeTableController.drawSelectedGeneProteinQuant(gene, container, fontSize, entry.getKey());
        }

        if(proteins.entrySet().size()==0)
            r2.setPercentHeight(90);


    }

    public static void setAlerts(Element element, PathwayController pathwayController, boolean useFilters){


        String[] genes = element.getEntities().stream().map(Entity::getName).toArray(String[]::new);

        if(genes.length>0) {

            Cursor dgeFindCursor = Database.getDb().getCollection("Nsivssi_dge").find(Filters.in("symbol",  genes));
            HashMap<String, JSONObject> dge = new HashMap<>();

            for (org.dizitart.no2.Document doc : dgeFindCursor) {


                JSONObject json = new JSONObject(doc);

                dge.put((String) json.get("symbol"), json);


            }

            if (!element.getClass().equals(Compartment.class) && (element.getType().equals("macromolecule") || element.getType().equals("complex"))) {

                for (Gene gene : element.getEntities().stream().filter(e -> e.getClass().equals(Gene.class)).toArray(Gene[]::new)) {

                    String geneName = gene.getName().split(" ")[0].split("\\(")[0].split("-")[0];
                    if (dge.containsKey(geneName) && dge.get(geneName).has("padj")) {
                        DgeAlert alert = new DgeAlert(geneName, dge.get(geneName).getDouble("log2fc"), dge.get(geneName).getDouble("padj"));
                        boolean addAlert = !useFilters || (dge.get(geneName).getDouble("padj") < PathwaySideController.PathwaysFilters.getGenePval() &&
                                ((PathwaySideController.PathwaysFilters.isGeneAbsFc() &&
                                        ((PathwaySideController.PathwaysFilters.getGeneComparisonSide() == PathwaySideController.ComparisonSide.MORE && Math.abs(alert.getFc()) > PathwaySideController.PathwaysFilters.getGeneLog2Fc()))
                                        || ((PathwaySideController.PathwaysFilters.getGeneComparisonSide() == PathwaySideController.ComparisonSide.LESS && Math.abs(alert.getFc()) < PathwaySideController.PathwaysFilters.getGeneLog2Fc())))
                                        ||
                                        (!PathwaySideController.PathwaysFilters.isGeneAbsFc() &&
                                                ((PathwaySideController.PathwaysFilters.getGeneComparisonSide() == PathwaySideController.ComparisonSide.MORE && alert.getFc() > PathwaySideController.PathwaysFilters.getGeneLog2Fc()))
                                                || ((PathwaySideController.PathwaysFilters.getGeneComparisonSide() == PathwaySideController.ComparisonSide.LESS && alert.getFc() < PathwaySideController.PathwaysFilters.getGeneLog2Fc())))));

                        if (dge.get(geneName).has("ms")) {
                            for (String msRun : dge.get(geneName).getJSONObject("ms").keySet()) {
                                JSONObject runObj = dge.get(geneName).getJSONObject("ms").getJSONObject(msRun);
                                if (runObj.has("padj")) {
                                    alert.addMsRun(msRun, dge.get(geneName).getJSONObject("ms").getJSONObject(msRun).getDouble("log2fc"), dge.get(geneName).getJSONObject("ms").getJSONObject(msRun).getDouble("padj"));

                                    if(!useFilters || (runObj.getDouble("padj") < PathwaySideController.PathwaysFilters.getProteinPval() &&
                                            ((PathwaySideController.PathwaysFilters.isProteinAbsFc() &&
                                                    ((PathwaySideController.PathwaysFilters.getProteinComparisonSide() == PathwaySideController.ComparisonSide.MORE && Math.abs(runObj.getDouble("log2fc")) > PathwaySideController.PathwaysFilters.getProteinLog2fc()))
                                                    || ((PathwaySideController.PathwaysFilters.getProteinComparisonSide() == PathwaySideController.ComparisonSide.LESS && Math.abs(runObj.getDouble("log2fc")) < PathwaySideController.PathwaysFilters.getProteinLog2fc())))
                                                    ||
                                                    (!PathwaySideController.PathwaysFilters.isProteinAbsFc() &&
                                                            ((PathwaySideController.PathwaysFilters.getProteinComparisonSide() == PathwaySideController.ComparisonSide.MORE && runObj.getDouble("log2fc") > PathwaySideController.PathwaysFilters.getProteinLog2fc()))
                                                            || ((PathwaySideController.PathwaysFilters.getProteinComparisonSide() == PathwaySideController.ComparisonSide.LESS && runObj.getDouble("log2fc") < PathwaySideController.PathwaysFilters.getProteinLog2fc())))))){
                                        addAlert=true;
                                    }



                                } else {
                                    alert.addMsRun(msRun, runObj.getDouble("log2fc"));
                                }
                            }
                        }
                        if(addAlert)
                            element.setAlert(alert, pathwayController);
                    }
                }
            }
        }
    }



    public static class CellTableRow{
        private final String level;
        private final double fc;
        private final double pval;

        public CellTableRow(String level, double fc, double pval) {
            this.level = level;
            this.fc = fc;
            this.pval = pval;
        }

        public String getLevel() {
            return level;
        }

        public double getFc() {
            return fc;
        }

        public double getPval() {
            return pval;
        }
    }

}
