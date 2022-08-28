package Controllers.drawerControllers;

import Cds.Pfam;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.filters.Filters;

public class PfamDrawerController {

    @FXML
    public Label nameLabel;
    @FXML
    public Label descLabel;
    @FXML
    ScrollPane scrollPane;
    @FXML
    AnchorPane mainPane;





    public void show(Pfam pfam){

        nameLabel.setText(pfam.getDesc());

        //scrollPane.setPrefWidth(mainPane.getWidth());
        //descLabel.setPrefWidth(scrollPane.getWidth());
        descLabel.prefWidthProperty().bind(mainPane.widthProperty());

        Nitrite db = Nitrite.builder().filePath("./databases/pfam").openOrCreate();

        NitriteCollection collection = db.getCollection("domains");
        Document document = collection.find(Filters.eq("pfam", pfam.getId())).firstOrDefault();

        descLabel.setText((String) document.get("desc"));

        db.close();

    }

}
