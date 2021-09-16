package pathway.alerts;

import Cds.PTM;
import Controllers.PathwayController;
import Singletons.Database;
import graphics.AnchorFitter;
import graphics.ConfidentBarChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.*;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.filters.Filters;
import org.json.JSONObject;
import pathway.Compartment;
import pathway.Element;
import pathway.Entity;
import pathway.Gene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PTMAlert extends Alert {

    private final ArrayList<PTM> ptms = new ArrayList<>();

    public PTMAlert(String gene) {
        super(gene);
    }

    @Override
    public String getType() {
        return "ptm";
    }

    @Override
    public void drawCell(AnchorPane pane, TitledPane titledPane) {
        Accordion eventsAccordion = new Accordion();
        pane.getChildren().add(eventsAccordion);
        AnchorFitter.fitAnchor(eventsAccordion);

        for (PTM ptm : ptms) {
            TitledPane eventTitledPane = new TitledPane();
            AnchorFitter.fitAnchor(eventTitledPane);
            eventTitledPane.setStyle("-fx-font-size: 18");
            eventTitledPane.setText(ptm.getType() + " " + ptm.getResidue() + "(" + ptm.getPos() + ")");
            AnchorPane eventPane = new AnchorPane();
            eventTitledPane.setContent(eventPane);
            eventsAccordion.getPanes().add(eventTitledPane);


            GridPane grid = new GridPane();
            ColumnConstraints c = new ColumnConstraints();
            c.setHgrow(Priority.ALWAYS);
            RowConstraints r1 = new RowConstraints();
            r1.setVgrow(Priority.NEVER);
            RowConstraints r2 = new RowConstraints();
            r2.setVgrow(Priority.NEVER);
            RowConstraints r3 = new RowConstraints();
            r3.setVgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(c);
            grid.getRowConstraints().add(r1);
            grid.getRowConstraints().add(r2);
            grid.getRowConstraints().add(r3);

            eventPane.getChildren().add(grid);

            AnchorFitter.fitAnchor(grid);

            Label log2FcLabel = new Label("Log2 fold change: " + ptm.getLog2fc());
            grid.add(log2FcLabel, 0, 0);

            Label pvalLabel = new Label("P-value: " + ptm.getPval());
            grid.add(pvalLabel, 0, 1);

            JSONObject intensities = ptm.getIntensities();
            HashMap<String, ArrayList<Double>> intensitiesHashMap = new HashMap<>();
            for (String sample : intensities.keySet()) {
                String[] sampleSplit = sample.split("/");
                if (!intensitiesHashMap.containsKey(sampleSplit[0]))
                    intensitiesHashMap.put(sampleSplit[0], new ArrayList<>());
                intensitiesHashMap.get(sampleSplit[0]).add(intensities.getDouble(sample));

            }

            ConfidentBarChart chart = new ConfidentBarChart();
            chart.addAll(intensitiesHashMap);
            chart.draw();
            AnchorFitter.fitAnchor(chart);
            grid.add(chart, 0, 2);

        }
    }

    public static void setAlerts(Element element, PathwayController pathwayController){


        String[] genes = element.getEntities().stream().map(Entity::getName).toArray(String[]::new);

        if(genes.length>0) {

            Cursor dgeFindCursor = Database.getDb().getCollection("ptm").find(Filters.in("gene", (Object[]) genes));
            HashMap<String, ArrayList<PTM>> ptms = new HashMap<>();

            Pattern pattern = Pattern.compile("\\(([A-Z])(\\d+)\\)");

            for (org.dizitart.no2.Document doc : dgeFindCursor) {

                JSONObject json = new JSONObject(doc);

                Matcher matcher = pattern.matcher(json.getString("id"));
                boolean matchFound = matcher.find();
                if (matchFound) {
                    PTM ptm = new PTM(matcher.group(1), Integer.parseInt(matcher.group(2)), json.getString("gene"), json.getDouble("log2fc"), json.has("pval") ? json.getDouble("pval") : Double.NaN, json.getJSONObject("samples"),
                            json.getString("type"));
                    if (!ptms.containsKey(ptm.getGene()))
                        ptms.put(ptm.getGene(), new ArrayList<>());

                    ptms.get(ptm.getGene()).add(ptm);
                }

            }


            if (!element.getClass().equals(Compartment.class) && (element.getType().equals("macromolecule") || element.getType().equals("complex"))) {

                for (Gene gene : element.getEntities().stream().filter(e -> e.getClass().equals(Gene.class)).toArray(Gene[]::new)) {

                    String geneName = gene.getName().split(" ")[0].split("\\(")[0].split("-")[0];

                    if (ptms.containsKey(geneName)) {
                        PTMAlert alert = new PTMAlert(geneName);
                        for (PTM ptm : ptms.get(geneName)) {
                            alert.addPTM(ptm);
                        }
                        if (alert.getPtms().size() > 0)
                            element.setAlert(alert, pathwayController);
                    }
                }
            }
        }

    }

    private void addPTM(PTM ptm) {
        ptms.add(ptm);
    }

    public ArrayList<PTM> getPtms() {
        return ptms;
    }
}
