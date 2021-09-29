package Controllers;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.ResourceBundle;

public class PathwayTreeController implements Initializable {

    @FXML
    private TreeView<Pathway> treeView;

    private final HashMap<String, TreeItem<Pathway>> items = new HashMap<>();
    private static PathwayTreeController instance;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        new Thread(this::retrieveTree).start();
        instance = this;

        treeView.setCellFactory(param -> {
            TreeCell<Pathway> treeCell = new TreeCell<>() {
                @Override
                protected void updateItem(Pathway item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText("");
                        setGraphic(null);
                        return;
                    }

                    setText(item.toString());
                }
            };

            treeCell.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent e) -> {
                if (e.getClickCount() % 2 == 0 && e.getButton().equals(MouseButton.PRIMARY)) {
                    TreeItem<Pathway> item = treeView.getSelectionModel().getSelectedItem();
                    PathwayController.getInstance().loadPathway(item.getValue().getId(), null);
                    e.consume();
                }
            });
            return treeCell;
        });
    }


    private void retrieveTree(){
        try{
            URL url = new URL("https://reactome.org/ContentService/data/eventsHierarchy/9606");
            URLConnection yc = url.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            yc.getInputStream()));
            StringBuilder res = new StringBuilder();

            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                res.append(inputLine);
            }

            parseTree( new JSONArray(res.toString()));

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void parseTree(JSONArray tree){
        TreeItem<Pathway> root = new TreeItem<>();

        Platform.runLater(() -> {
            treeView.setRoot(root);
            treeView.setShowRoot(false);
            for (int i = 0; i < tree.length(); i++) {
                JSONObject node = tree.getJSONObject(i);
                TreeItem<Pathway> item = new TreeItem<>(new Pathway(node.getString("name"), node.getString("stId")));
                items.put(item.getValue().getId(), item);
                root.getChildren().add(item);
                parseNode(node, item);

            }
        });

    }

    private void parseNode(JSONObject node, TreeItem<Pathway> parent){
        if(node.has("children")) {
            for (int i = 0; i < node.getJSONArray("children").length(); i++) {
                JSONObject chilNode = node.getJSONArray("children").getJSONObject(i);
                if(chilNode.getString("type").contains("Pathway")) {
                    TreeItem<Pathway> item = new TreeItem<>(new Pathway(chilNode.getString("name"), chilNode.getString("stId")));
                    items.put(item.getValue().getId(), item);
                    parent.getChildren().add(item);
                    parseNode(chilNode, item);
                }

            }
        }

    }

    public void expendItem(String id){

        for(TreeItem<Pathway> item: items.values()){
            item.setExpanded(false);
        }
        treeView.getRoot().setExpanded(true);

        TreeItem<Pathway> item = items.get(id);
        treeView.getSelectionModel().select(item);
        expendItem(item);
    }
    public void expendItem(TreeItem<Pathway> item){
        item.setExpanded(true);
        if(item.getParent()!=null)
            expendItem(item.getParent());
    }

    public static PathwayTreeController getInstance(){
        return instance;
    }

    private class Pathway{
        private String name;
        private String id;

        public Pathway(String name, String id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
