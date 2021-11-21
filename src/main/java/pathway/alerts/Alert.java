package pathway.alerts;

import Controllers.PathwayController;
import graphics.AnchorFitter;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import pathway.Element;

public abstract class Alert {

    protected String gene;

    public Alert(String gene) {
        this.gene = gene;
    }

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public abstract String getType();

    public abstract void drawCell(Pane container, TitledPane titledPane);

    public static void populateGenes(Pane container, Element element, Class alertClass){
        container.getChildren().clear();

        Alert[] alerts = element.getAlerts().stream().filter(e->e.getClass().equals(alertClass)).toArray(Alert[]::new);
        AnchorPane alertPane = new AnchorPane();
        if(alerts.length==1){
            container.getChildren().add(alertPane);
            AnchorPane.setRightAnchor(alertPane, 10.);
            AnchorPane.setLeftAnchor(alertPane, 0.);
            alerts[0].drawCell(alertPane, null);
        }else{
            Accordion accordion = new Accordion();
            AnchorFitter.fitAnchor(accordion);
            container.getChildren().add(accordion);
            for(pathway.alerts.Alert alert: alerts){
                TitledPane titledPane = new TitledPane(alert.getGene(), alertPane);
                titledPane.setMaxWidth(container.getWidth());

                accordion.getPanes().add(titledPane);
                alert.drawCell(alertPane, titledPane);
            }
        }

    }


}
