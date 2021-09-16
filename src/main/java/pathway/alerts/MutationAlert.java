package pathway.alerts;

import Cds.Exon;
import Cds.Mutation;
import Cds.Transcript;
import Controllers.PathwayController;
import Controllers.SplicingTableController;
import Singletons.Database;
import graphics.AnchorFitter;
import graphics.ConfidentBarChart;
import javafx.geometry.Insets;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.KeyValuePair;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.filters.Filters;
import org.json.JSONObject;
import pathway.Compartment;
import pathway.Element;
import pathway.Entity;
import pathway.Gene;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.dizitart.no2.filters.Filters.eq;

public class MutationAlert extends Alert{

    private final ArrayList<Mutation> mutations = new ArrayList<>();

    public MutationAlert(String gene) {
        super(gene);
    }

    @Override
    public String getType() {
        return "mutation";
    }

    @Override
    public void drawCell(AnchorPane pane, TitledPane titledPane) {
        Accordion eventsAccordion = new Accordion();
        pane.getChildren().add(eventsAccordion);
        AnchorFitter.fitAnchor(eventsAccordion);

        for(Mutation mutation:  mutations){
            TitledPane eventTitledPane = new TitledPane();
            AnchorFitter.fitAnchor(eventTitledPane);
            eventTitledPane.setStyle("-fx-font-size: 18");
            eventTitledPane.setText(mutation.getChr()+" "+mutation.getPos()+" "+mutation.getRef()+"->"+mutation.getAlt());
            AnchorPane eventPane = new AnchorPane();
            eventTitledPane.setContent(eventPane);
            eventsAccordion.getPanes().add(eventTitledPane);




            GridPane grid = new GridPane();
            ColumnConstraints c = new ColumnConstraints();
            c.setHgrow(Priority.ALWAYS);
            RowConstraints r1 = new RowConstraints();
            r1.setVgrow(Priority.NEVER);
            RowConstraints r2 = new RowConstraints();
            r2.setVgrow(Priority.ALWAYS);
            RowConstraints r3 = new RowConstraints();
            r3.setVgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(c);
            grid.getRowConstraints().add(r1);
            grid.getRowConstraints().add(r2);
            grid.getRowConstraints().add(r3);

            eventPane.getChildren().add(grid);

            AnchorFitter.fitAnchor(grid);

            Label descriptionLabel = new Label(mutation.toString());
            grid.add(descriptionLabel, 0, 0);

            JSONObject conditions = mutation.getConditions();

            HashMap<String, ArrayList<Double>> afValues = new HashMap<>();
            HashMap<String, ArrayList<Double>> qualValues = new HashMap<>();

            for(String condition: conditions.keySet()){
                for(String sample: conditions.getJSONObject(condition).keySet()){
                    double qual = conditions.getJSONObject(condition).getJSONObject(sample).getDouble("qual");
                    double af = conditions.getJSONObject(condition).getJSONObject(sample).getDouble("AF");

                    if(!afValues.containsKey(condition)){
                        afValues.put(condition, new ArrayList<>());
                        qualValues.put(condition, new ArrayList<>());
                    }

                    afValues.get(condition).add(af);
                    qualValues.get(condition).add(qual);
                }
            }

            ConfidentBarChart afChart = new ConfidentBarChart();
            AnchorFitter.fitAnchor(afChart);
            afChart.addAll(afValues);
            afChart.setMin(0);
            afChart.setMax(1);

            ConfidentBarChart qualChart = new ConfidentBarChart();
            AnchorFitter.fitAnchor(qualChart);
            qualChart.addAll(qualValues);

            afChart.setYLegend("Allele frequency");
            afChart.draw();
            qualChart.setYLegend("Quality");
            qualChart.draw();

            grid.add(afChart, 0, 1);
            grid.add(qualChart, 0, 2);

//            AnchorPane psiChartContainer = new AnchorPane();
//            AnchorFitter.fitAnchor(psiChartContainer);
//            grid.add(psiChartContainer, 0, 2);
//            GridPane.setMargin(psiChartContainer, new Insets(20, 0, 0, 0));
//            RowConstraints r3 = new RowConstraints();
//            r3.setVgrow(Priority.ALWAYS);
//            //r3.setPercentHeight(30);
//            grid.getRowConstraints().add(r3);
//            SplicingTableController.drawPsiChart(event.getId(), psiChartContainer);
//
//
//            AnchorPane exonsPane = new AnchorPane();
//            RowConstraints r4 = new RowConstraints();
//            r4.setVgrow(Priority.SOMETIMES);
//            //r4.setPercentHeight(30);
//            grid.getRowConstraints().add(r4);
//            grid.add(exonsPane, 0, 3);
//            GridPane.setMargin(exonsPane, new Insets(20, 0, 0, 0));
//
//            //exonsPane.setStyle("-fx-background-color: red");
//
//            exonsPane.widthProperty().addListener((observable, oldValue, newValue) -> {
//                exonsPane.getChildren().clear();
//                double representationWidth = newValue.doubleValue()-5;
//                System.out.println(representationWidth);
//                Text t = new Text("ENST");
//                t.setFont(Font.font(15));
//
//                double margin = 10;
//
//                double offsetY = 0;
//
//                NitriteCollection allTranscriptsCollection = Database.getDb().getCollection("allTranscripts");
//                Cursor cursor = allTranscriptsCollection.find(eq("gene", gene));
//
//                HashMap<String, Transcript> transcripts = new HashMap<>();
//
//                for (Document doc : cursor) {
//                    if (doc.get("transcriptID", String.class).contains("ENST")) {
//                        transcripts.put(doc.get("transcriptID", String.class), new Transcript(doc));
//                    }
//                }
//
//                int geneStart = 2147483647;
//                int geneEnd = 0;
//
//
//                for (Map.Entry<String, Transcript> entry : transcripts.entrySet()) {
//                    Transcript transcript = entry.getValue();
//                    if (transcript.getStartGenomCoord() < geneStart) {
//                        geneStart = Math.toIntExact(transcript.getStartGenomCoord());
//                    }
//                    if (transcript.getEndGenomCoord() > geneEnd) {
//                        geneEnd = Math.toIntExact(transcript.getEndGenomCoord());
//                    }
//                }
//                int geneLength = geneEnd - geneStart + 1;
//
//                for (Map.Entry<String, Transcript> entry : transcripts.entrySet()) {
//                    Transcript transcript = entry.getValue();
//
//                    int currentPos = transcript.getExons().get(0).getStart();
//
//                    Text transcriptIdText = new Text(entry.getKey());
//                    exonsPane.getChildren().add(transcriptIdText);
//                    transcriptIdText.setY(offsetY);
//                    offsetY+=transcriptIdText.getLayoutBounds().getHeight();
//
//                    for (Exon exon : transcript.getExons()) {
//                        if (exon.getStart() - currentPos > 0) {
//                            Line l = new Line(representationWidth * ((double) (currentPos - geneStart + 1) / geneLength), offsetY,
//                                    representationWidth * ((double) (exon.getEnd() - geneStart + 1) / geneLength), offsetY);
//                            exonsPane.getChildren().add(l);
//                        }
//                        Rectangle rect = new Rectangle(representationWidth * ((double) (exon.getStart() - geneStart + 1) / geneLength), offsetY - t.getBoundsInLocal().getHeight() / 2,
//                                representationWidth * ((double) (exon.getEnd() - exon.getStart() + 1) / geneLength), t.getBoundsInLocal().getHeight());
//
//
//                        if(event.getId().contains("A3")){
//                            Pattern p = Pattern.compile("(\\d+)-(\\d+):(\\d+)-(\\d+)");
//                            Matcher m = p.matcher(event.getId());
//                            if(m.find()){
//                                if(exon.getEnd()==Integer.parseInt(m.group(1)) || exon.getStart()==Integer.parseInt(m.group(4)))
//                                    rect.setFill(Color.RED);
//                                else if(exon.getStart()==Integer.parseInt(m.group(2)))
//                                    rect.setFill(Color.GREEN);
//                            }
//                        }
//
//
//                        exonsPane.getChildren().add(rect);
//                        currentPos = exon.getEnd();
//                    }
//
//                    offsetY += t.getBoundsInLocal().getHeight() + margin;
//                }
//            });
//
//            AnchorPane representationPane = new AnchorPane();
//            grid.add(representationPane, 0, 4);
//            RowConstraints r5 = new RowConstraints();
//            GridPane.setMargin(representationPane, new Insets(20, 0, 10, 0));
//            r5.setVgrow(Priority.SOMETIMES);
//            //r5.setPercentHeight(30);
//            grid.getRowConstraints().add(r5);
//            representationPane.widthProperty().addListener((observable, oldValue, newValue) -> {
//                if(newValue.doubleValue()>0)
//                    SplicingTableController.drawSplicingEventRepresentation(representationPane, event.getId(), String.valueOf(event.getId().charAt(event.getId().length() - 1)), event.getId().split(":")[0].split(";")[1]);
//            });
        }
    }

    public static void setAlerts(Element element, PathwayController pathwayController){

        String[] genes = element.getEntities().stream().map(Entity::getName).toArray(String[]::new);

        if(genes.length>0) {

            Cursor eventsCursor = Database.getDb().getCollection("mutations").find(Filters.in("gene", (Object[]) genes));
            HashMap<String, ArrayList<Mutation>> mutations = new HashMap<>();

            for (org.dizitart.no2.Document doc : eventsCursor) {
                JSONObject jsonDoc = new JSONObject(doc);
                Mutation mutation;

                //TODO: remove if else->unecessary if mutations have been annotated in PIT
                if(doc.containsKey("inCDS") && doc.get("inCDS")!=null){
                    mutation = new Mutation(doc.get("gene", String.class), doc.get("chr", String.class), (Long) doc.get("refPos"), doc.get("ref", String.class), doc.get("alt", String.class), jsonDoc.getJSONObject("condition"),
                            jsonDoc.getJSONObject("transcripts"), (boolean) doc.get("inCDS"), (boolean) doc.get("silent"));
                }else{
                    mutation = new Mutation(doc.get("gene", String.class), doc.get("chr", String.class), (Long) doc.get("refPos"), doc.get("ref", String.class), doc.get("alt", String.class), jsonDoc.getJSONObject("condition"),
                        jsonDoc.getJSONObject("transcripts"));

                }


                if (!mutations.containsKey(mutation.getGene()))
                    mutations.put(mutation.getGene(), new ArrayList<>());
                mutations.get(mutation.getGene()).add(mutation);

            }


            if (!element.getClass().equals(Compartment.class) && (element.getType().equals("macromolecule") || element.getType().equals("complex"))) {

                for (Gene gene : element.getEntities().stream().filter(e -> e.getClass().equals(Gene.class)).toArray(Gene[]::new)) {

                    String geneName = gene.getName().split(" ")[0].split("\\(")[0].split("-")[0];

                    if (mutations.containsKey(geneName)) {
                        MutationAlert alert = new MutationAlert(geneName);
                        for (Mutation mutation : mutations.get(geneName)) {
                            alert.addMutation(mutation);
                        }
                        if (alert.getMutations().size() > 0)
                            element.setAlert(alert, pathwayController);
                    }
                }
            }
        }
    }

    private void addMutation(Mutation mutation) {
        mutations.add(mutation);
    }

    public ArrayList<Mutation> getMutations() {
        return mutations;
    }
}
