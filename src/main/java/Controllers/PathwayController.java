package Controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import javafx.util.Pair;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import pathway.*;
import pathway.Arc;
import pathway.Reaction;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PathwayController implements Initializable {

    @FXML
    private javafx.scene.control.Label summationField;
    @FXML
    private ListView<String> pathwayListview;
    @FXML
    private ListView<String> reactionListview;
    @FXML
    private GridPane gridPane;
    @FXML
    private TextField searchField;
    @FXML
    private Pane container;

    private HashMap<String, Element> elements = new HashMap<>();
    private ArrayList<Arc> arcs = new ArrayList<>();
    private ArrayList<String> ignoreArcsTo = new ArrayList<>();
    private ArrayList<String> ignoreArcsFrom = new ArrayList<>();
    private ArrayList<javafx.scene.control.Label> labels = new ArrayList<>();

    private double maxX=0, maxY=0;

    private double xOffset, yOffset;
    private DoubleProperty fontSize = new SimpleDoubleProperty(14);

    private HashMap<String, Reaction> reactions = new HashMap<>();

    private HashMap<String, SearchResult> searchPathways;
    private HashMap<String, SearchResult> searchReactions;

    private HashMap<String, ArrayList<javafx.scene.Node>> reactionNodes;
    private HashMap<String, String> reactionLabelId;

    private Group arcBackgroundGroup;
    private Group arcGroup;
    private Group elementGroup;



    @Override
    public void initialize(URL location, ResourceBundle resources) {

        container.setOnScroll((ScrollEvent event) -> {
            // Adjust the zoom factor as per your requirement
            double zoomFactor = 1.05;
            double deltaY = event.getDeltaY();
            if (deltaY < 0){
                zoomFactor = 1.9 - zoomFactor;
                fontSize.set(fontSize.getValue()*0.90);
            }else{

                fontSize.set(fontSize.getValue()*1.03);
            }


            Scale newScale = new Scale();
            newScale.setPivotX(event.getX());
            newScale.setPivotY(event.getY());
            newScale.setX( container.getScaleX() * zoomFactor );
            newScale.setY( container.getScaleY() * zoomFactor );

            container.getTransforms().add(newScale);

            event.consume();

        });



        container.setOnMousePressed(event -> {
            xOffset = container.getTranslateX() - event.getScreenX();
            yOffset = container.getTranslateY() - event.getScreenY();
        });

        container.setOnMouseDragged(event -> {
            container.setTranslateX(event.getScreenX() + xOffset);
            container.setTranslateY(event.getScreenY() + yOffset);
            event.consume();

        });

        pathwayListview.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                summationField.setText(searchPathways.get(pathwayListview.getSelectionModel().getSelectedItem()).getSummation());
                loadPathway(searchPathways.get(pathwayListview.getSelectionModel().getSelectedItem()).getId(), null);
            }
        });
        reactionListview.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                summationField.setText(searchReactions.get(reactionListview.getSelectionModel().getSelectedItem()).getSummation());
                loadReaction(reactionListview.getSelectionModel().getSelectedItem());
            }
        });
    }



    public void parseSbgn(String sgbn, String reaction){

        container.getChildren().clear();
        elements = new HashMap<>();
        arcs = new ArrayList<>();
        ignoreArcsFrom = new ArrayList<>();
        ignoreArcsFrom = new ArrayList<>();
        reactions = new HashMap<>();
        reactionLabelId = new HashMap<>();
        reactionNodes = new HashMap<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            File file = new File("test.sbgn");
            Document doc = builder.parse(new InputSource(new StringReader(sgbn)));

            parseNode(doc.getDocumentElement());

            draw(reaction);

        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }


    }

    public void parseNode(Node node) {



        NodeList nodeList = node.getChildNodes();


        if(node.getNodeName().equals("glyph")){
            String nodeClass = node.getAttributes().getNamedItem("class").getNodeValue();
            switch (nodeClass) {
                case "complex":
                case "small molecule":
                case "simple chemical":
                case "macromolecule":
                case "unspecified entity":{

                    Element element;
                    String label = "";

                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node subNode = nodeList.item(i);
                        if (subNode.getNodeType() == Node.ELEMENT_NODE) {
                            if (subNode.getNodeName().equals("bbox")) {

                                double x = Double.parseDouble(subNode.getAttributes().getNamedItem("x").getNodeValue());
                                double y = Double.parseDouble(subNode.getAttributes().getNamedItem("y").getNodeValue());
                                double w = Double.parseDouble(subNode.getAttributes().getNamedItem("w").getNodeValue());
                                double h = Double.parseDouble(subNode.getAttributes().getNamedItem("h").getNodeValue());

                                if(x+w>maxX)
                                    maxX=x+w;
                                if(y+h>maxY)
                                    maxY=y+h;

                                element = new Element(x, y,
                                        Double.parseDouble(subNode.getAttributes().getNamedItem("w").getNodeValue()),
                                        Double.parseDouble(subNode.getAttributes().getNamedItem("h").getNodeValue()),
                                        node.getAttributes().getNamedItem("id").getNodeValue(), nodeClass, label);


                                elements.put(node.getAttributes().getNamedItem("id").getNodeValue(), element);
                            } else if (subNode.getNodeName().equals("label")) {
                                label = subNode.getAttributes().getNamedItem("text").getNodeValue();
                            }
                        }
                    }

                    break;
                }
                case "compartment": {

                    Compartment compartment;
                    Label label = null;

                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node subNode = nodeList.item(i);
                        if (subNode.getNodeType() == Node.ELEMENT_NODE) {
                            if (subNode.getNodeName().equals("bbox")) {

                                double x = Double.parseDouble(subNode.getAttributes().getNamedItem("x").getNodeValue());
                                double y = Double.parseDouble(subNode.getAttributes().getNamedItem("y").getNodeValue());
                                double w = Double.parseDouble(subNode.getAttributes().getNamedItem("w").getNodeValue());
                                double h = Double.parseDouble(subNode.getAttributes().getNamedItem("h").getNodeValue());

                                if(x+w>maxX)
                                    maxX=x+w;
                                if(y+h>maxY)
                                    maxY=y+h;


                                compartment = new Compartment(x, y,
                                        Double.parseDouble(subNode.getAttributes().getNamedItem("w").getNodeValue()),
                                        Double.parseDouble(subNode.getAttributes().getNamedItem("h").getNodeValue()),
                                        node.getAttributes().getNamedItem("id").getNodeValue());
                                compartment.setLabel(label);

                                elements.put(node.getAttributes().getNamedItem("id").getNodeValue(), compartment);
                            } else if (subNode.getNodeName().equals("label")) {


                                for (int j = 0; j < node.getChildNodes().getLength(); j++) {
                                    Node labelNode = node.getChildNodes().item(j);
                                    if (labelNode.getNodeType() == Node.ELEMENT_NODE) {

                                        for (int k = 0; k < labelNode.getChildNodes().getLength(); k++) {
                                            Node labelBboxNode = labelNode.getChildNodes().item(k);
                                            if (labelBboxNode.getNodeType() == Node.ELEMENT_NODE) {

                                                double x = Double.parseDouble(labelBboxNode.getAttributes().getNamedItem("x").getNodeValue());
                                                double y = Double.parseDouble(labelBboxNode.getAttributes().getNamedItem("y").getNodeValue());
                                                double w = Double.parseDouble(labelBboxNode.getAttributes().getNamedItem("w").getNodeValue());
                                                double h = Double.parseDouble(labelBboxNode.getAttributes().getNamedItem("h").getNodeValue());

                                                if(x+w>maxX)
                                                    maxX=x+w;
                                                if(y+h>maxY)
                                                    maxY=y+h;

                                                label = new Label(x, y,
                                                        Double.parseDouble(labelBboxNode.getAttributes().getNamedItem("w").getNodeValue()),
                                                        Double.parseDouble(labelBboxNode.getAttributes().getNamedItem("h").getNodeValue()),
                                                        labelNode.getAttributes().getNamedItem("text").getNodeValue());

                                            }
                                        }

                                    }
                                }

                            }
                        }
                    }
                    break;
                }
                case "association":
                case "process":
                case "dissociation":
                case "omitted process":{

                    Reaction reaction = null;
                    String label = "";
                    ArrayList<Port> ports = new ArrayList<>();

                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node subNode = nodeList.item(i);
                        if (subNode.getNodeType() == Node.ELEMENT_NODE) {
                            if (subNode.getNodeName().equals("bbox")) {

                                double x = Double.parseDouble(subNode.getAttributes().getNamedItem("x").getNodeValue());
                                double y = Double.parseDouble(subNode.getAttributes().getNamedItem("y").getNodeValue());
                                double w = Double.parseDouble(subNode.getAttributes().getNamedItem("w").getNodeValue());
                                double h = Double.parseDouble(subNode.getAttributes().getNamedItem("h").getNodeValue());

                                if(x+w>maxX)
                                    maxX=x+w;
                                if(y+h>maxY)
                                    maxY=y+h;

                                reaction = new Reaction(x, y,
                                        Double.parseDouble(subNode.getAttributes().getNamedItem("w").getNodeValue()),
                                        Double.parseDouble(subNode.getAttributes().getNamedItem("h").getNodeValue()),
                                        node.getAttributes().getNamedItem("id").getNodeValue(), nodeClass);

                                reaction.setLabel(label);



                                String reactionId = String.join("_", Arrays.copyOfRange(reaction.getId().split("_"), 0, 2));

                                reactionLabelId.put(label, reactionId);
                                reactions.put(reactionId , reaction);

                                elements.put(node.getAttributes().getNamedItem("id").getNodeValue(), reaction);
                            } else if (subNode.getNodeName().equals("label")) {
                                label = subNode.getAttributes().getNamedItem("text").getNodeValue();
                            } else if (subNode.getNodeName().equals("port")) {

                                double x = Double.parseDouble(subNode.getAttributes().getNamedItem("x").getNodeValue());
                                double y = Double.parseDouble(subNode.getAttributes().getNamedItem("y").getNodeValue());

                                if(x>maxX)
                                    maxX=x;
                                if(y>maxY)
                                    maxY=y;

//                                if(nodeClass.equals("process"))
//                                    ignoreArcsTo.add(subNode.getAttributes().getNamedItem("id").getNodeValue());
//                                if(nodeClass.equals("association"))
//                                    ignoreArcsFrom.add(subNode.getAttributes().getNamedItem("id").getNodeValue());

                                ports.add(new Port(x, y,
                                        subNode.getAttributes().getNamedItem("id").getNodeValue()));
                            }
                        }
                    }
                    reaction.setPorts(ports);
                }


            }
        }else if(node.getNodeName().equals("arc")){
            ArrayList<Pair<Double, Double>> points = new ArrayList<>();

            if(!ignoreArcsTo.contains(node.getAttributes().getNamedItem("target").getNodeValue()) &&
                    !ignoreArcsFrom.contains(node.getAttributes().getNamedItem("source").getNodeValue())) {

                Arc arc = new Arc(node.getAttributes().getNamedItem("source").getNodeValue(),
                        node.getAttributes().getNamedItem("target").getNodeValue(),
                        node.getAttributes().getNamedItem("class").getNodeValue(), node.getAttributes().getNamedItem("id").getNodeValue());

                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node subNode = node.getChildNodes().item(i);
                    if (subNode.getNodeType() == Node.ELEMENT_NODE) {


                        if (subNode.getNodeName().equals("glyph")) {
                            String label = "";
                            for (int j = 0; j < subNode.getChildNodes().getLength(); j++) {
                                Node glypthNode = subNode.getChildNodes().item(j);
                                if (glypthNode.getNodeType() == Node.ELEMENT_NODE) {
                                    if (glypthNode.getNodeName().equals("label"))
                                        label = glypthNode.getAttributes().getNamedItem("text").getNodeValue();
                                    else {

                                        double x = Double.parseDouble(glypthNode.getAttributes().getNamedItem("x").getNodeValue());
                                        double y = Double.parseDouble(glypthNode.getAttributes().getNamedItem("y").getNodeValue());
                                        double w = Double.parseDouble(glypthNode.getAttributes().getNamedItem("w").getNodeValue());
                                        double h = Double.parseDouble(glypthNode.getAttributes().getNamedItem("h").getNodeValue());

                                        if (x+w > maxX)
                                            maxX = x+w;
                                        if (y+h > maxY)
                                            maxY = y+h;

                                        Element element = new Element(x, y,
                                                Double.parseDouble(glypthNode.getAttributes().getNamedItem("w").getNodeValue()),
                                                Double.parseDouble(glypthNode.getAttributes().getNamedItem("h").getNodeValue()),
                                                node.getAttributes().getNamedItem("id").getNodeValue(),
                                                subNode.getAttributes().getNamedItem("class").getNodeValue(), label);
                                        arc.setGlyph(element);
                                    }
                                }
                            }
                        } else {
                            points.add(new Pair(Double.parseDouble(subNode.getAttributes().getNamedItem("x").getNodeValue()),
                                    Double.parseDouble(subNode.getAttributes().getNamedItem("y").getNodeValue())));
                        }


                    }
                }

                arc.setPoints(points);
                arcs.add(arc);
            }

        }

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                //calls this method for all the children which is Element
                parseNode(currentNode);
            }
        }


    }

    public void draw(String reactionFocus){

        elementGroup = new Group();
        arcGroup = new Group();
        arcBackgroundGroup = new Group();

        int nbCompartments = 0, compartmentIndex=0;
        for(Element element: elements.values()){
            if(element.getClass().equals(Compartment.class))
                nbCompartments++;
        }

        for(Element element: elements.values()){
            if(element.getClass().equals(Compartment.class)){
                Compartment compartment = (Compartment) element;
                Rectangle rectangle = new Rectangle(scaleCoordinates(element.getX(), "x"),
                        scaleCoordinates(element.getY(), "y"),
                        scaleCoordinates(element.getWidth(), "x"), scaleCoordinates(element.getHeight(), "y"));

                double opacity = 20+40*((double) ++compartmentIndex/nbCompartments);

                rectangle.toBack();
                Label label = compartment.getLabelObj();

                rectangle.setFill(Color.web("#F4A41A", opacity/100));
                javafx.scene.control.Label text = new javafx.scene.control.Label(label.getLabel());
                labels.add(text);
                text.setWrapText(true);
                text.setAlignment(Pos.CENTER);



                text.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";"
                        ,""));

                text.setTextFill(Color.web("#3C4ED7"));
                text.setLayoutX(scaleCoordinates(label.getX(), "x"));
                text.setPrefWidth(scaleCoordinates(label.getWidth(), "x"));
                text.setLayoutY(scaleCoordinates(label.getY(), "y"));
                text.setPrefHeight(scaleCoordinates(label.getHeight(), "y"));

                container.getChildren().add(rectangle);
                container.getChildren().add(text);
            }

        }




        for(Element element: elements.values()){

            if(element.getClass().equals(Element.class)){
                switch (element.getType()) {
                    case "simple chemical":
                        Ellipse ellipse = new Ellipse();
                        ellipse.setCenterX(scaleCoordinates(element.getX(), "x") + scaleCoordinates(element.getWidth() / 2, "x"));
                        ellipse.setCenterY(scaleCoordinates(element.getY(), "y") + scaleCoordinates(element.getHeight() / 2, "y"));
                        ellipse.setRadiusX(scaleCoordinates(element.getWidth() / 2, "x"));
                        ellipse.setRadiusY(scaleCoordinates(element.getHeight() / 2, "y"));
                        ellipse.setFill(Color.web("#A5D791"));
                        elementGroup.getChildren().add(ellipse);

                        break;
                    case "complex": {
                        Rectangle rectangle = new Rectangle(scaleCoordinates(element.getX(), "x"),
                                scaleCoordinates(element.getY(), "y"),
                                scaleCoordinates(element.getWidth(), "x"), scaleCoordinates(element.getHeight(), "y"));
                        rectangle.setFill(Color.web("#A2C6D7"));
                        elementGroup.getChildren().add(rectangle);
                        break;
                    }
                    case "macromolecule": {
                        Rectangle rectangle = new Rectangle(scaleCoordinates(element.getX(), "x"),
                                scaleCoordinates(element.getY(), "y"),
                                scaleCoordinates(element.getWidth(), "x"), scaleCoordinates(element.getHeight(), "y"));
                        rectangle.setFill(Color.web("#8DC7BB"));
                        elementGroup.getChildren().add(rectangle);
                        break;
                    }
                    case "unspecified entity": {
                        Rectangle rectangle = new Rectangle(scaleCoordinates(element.getX(), "x"),
                                scaleCoordinates(element.getY(), "y"),
                                scaleCoordinates(element.getWidth(), "x"), scaleCoordinates(element.getHeight(), "y"));
                        rectangle.setFill(Color.web("#A0BBCD"));
                        elementGroup.getChildren().add(rectangle);
                        break;
                    }
                }

                javafx.scene.control.Label text = new javafx.scene.control.Label(element.getLabel());
                labels.add(text);
                text.setWrapText(true);
                text.setAlignment(Pos.CENTER);

                text.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";"
                        ,""));


                text.setLayoutX(scaleCoordinates(element.getX(), "x"));
                text.setPrefWidth(scaleCoordinates(element.getWidth(), "x"));
                text.setLayoutY(scaleCoordinates(element.getY(), "y"));
                text.setPrefHeight(scaleCoordinates(element.getHeight(), "y"));
                text.setTextFill(Color.web("#3C4ED7"));

                elementGroup.getChildren().add(text);
            }else if(element.getClass().equals(Reaction.class)){

                Reaction reaction = (Reaction) element;

                Arc arc = new Arc(reaction.getPorts().get(0).getId(), reaction.getPorts().get(1).getId(),
                        reaction.getType(), reaction.getId());
                ArrayList<Pair<Double, Double>> points = new ArrayList<>();

                if(reaction.getType().equals("association")){

                    points.add(new Pair<>(reaction.getPorts().get(0).getX(), reaction.getPorts().get(0).getY()));
                    points.add(new Pair<>(reaction.getX(), reaction.getY()+reaction.getHeight()/2));

                }else if(reaction.getType().equals("process") || reaction.getType().equals("omitted process")  || reaction.getType().equals("dissociation")){

                    points.add(new Pair<>(reaction.getX()+reaction.getWidth(), reaction.getY()+reaction.getHeight()/2));
                    points.add(new Pair<>(reaction.getPorts().get(1).getX(), reaction.getPorts().get(1).getY()));



                    Arc arc2 = new Arc(reaction.getPorts().get(0).getId(), reaction.getPorts().get(1).getId(),
                            reaction.getType(), reaction.getId());
                    ArrayList<Pair<Double, Double>> points2 = new ArrayList<>();

                    points2.add(new Pair<>(reaction.getPorts().get(0).getX(), reaction.getPorts().get(0).getY()));
                    points2.add(new Pair<>(reaction.getPorts().get(1).getX(), reaction.getPorts().get(1).getY()));

                    arc2.setPoints(points2);
                    arcs.add(arc2);


                }
                arc.setPoints(points);

                arcs.add(arc);


                switch (reaction.getType()) {
                    case "process":
                        Rectangle rectangle = new Rectangle(scaleCoordinates(reaction.getX(), "x"),
                                scaleCoordinates(reaction.getY(), "y"),
                                scaleCoordinates(reaction.getWidth(), "x"), scaleCoordinates(element.getHeight(), "y"));
                        rectangle.setFill(Color.web("#FEFEFE"));
                        elementGroup.getChildren().add(rectangle);
                        break;
                    case "omitted process":

                        Group g = new Group();

                        Rectangle rect = new Rectangle(scaleCoordinates(reaction.getX(), "x"),
                                scaleCoordinates(reaction.getY(), "y"),
                                scaleCoordinates(reaction.getWidth(), "x"), scaleCoordinates(element.getHeight(), "y"));
                        rect.setFill(Color.web("#FEFEFE"));

                        Line l1 = new Line();
                        l1.setStartX(scaleCoordinates(reaction.getX()+reaction.getX()+reaction.getWidth()*0.2, "x"));
                        l1.setStartY(scaleCoordinates(reaction.getY()+reaction.getY()+reaction.getHeight()*0.2, "y"));
                        l1.setEndX(scaleCoordinates(reaction.getX()+reaction.getX()+reaction.getWidth()*0.4, "x"));
                        l1.setEndY(scaleCoordinates(reaction.getY()+reaction.getY()+reaction.getHeight()*0.8, "y"));

                        Line l2 = new Line();
                        l2.setStartX(scaleCoordinates(reaction.getX()+reaction.getX()+reaction.getWidth()*0.6, "x"));
                        l2.setStartY(scaleCoordinates(reaction.getY()+reaction.getY()+reaction.getHeight()*0.2, "y"));
                        l2.setEndX(scaleCoordinates(reaction.getX()+reaction.getX()+reaction.getWidth()*0.8, "x"));
                        l2.setEndY(scaleCoordinates(reaction.getY()+reaction.getY()+reaction.getHeight()*0.8, "y"));

                        g.getChildren().add(rect);
                        g.getChildren().add(l1);
                        g.getChildren().add(l2);

                        elementGroup.getChildren().add(g);
                        break;
                    case "association":
                        Ellipse ellipse = new Ellipse();
                        ellipse.setCenterX(scaleCoordinates(reaction.getX(), "x") + scaleCoordinates(reaction.getWidth() / 2, "x"));
                        ellipse.setCenterY(scaleCoordinates(reaction.getY(), "y") + scaleCoordinates(reaction.getHeight() / 2, "y"));
                        ellipse.setRadiusX(scaleCoordinates(reaction.getWidth() / 2, "x"));
                        ellipse.setRadiusY(scaleCoordinates(reaction.getHeight() / 2, "y"));
                        ellipse.setFill(Color.BLACK);
                        elementGroup.getChildren().add(ellipse);

                        break;
                    case "dissociation":
                        Ellipse ellipse1 = new Ellipse();
                        ellipse1.setCenterX(scaleCoordinates(reaction.getX(), "x") + scaleCoordinates(reaction.getWidth() / 2, "x"));
                        ellipse1.setCenterY(scaleCoordinates(reaction.getY(), "y") + scaleCoordinates(reaction.getHeight() / 2, "y"));
                        ellipse1.setRadiusX(scaleCoordinates(reaction.getWidth() / 2, "x"));
                        ellipse1.setRadiusY(scaleCoordinates(reaction.getHeight() / 2, "y"));
                        ellipse1.setFill(Color.WHITE);
                        elementGroup.getChildren().add(ellipse1);

                        Ellipse ellipse2 = new Ellipse();
                        ellipse2.setCenterX(scaleCoordinates(reaction.getX(), "x") + scaleCoordinates(reaction.getWidth() / 2, "x"));
                        ellipse2.setCenterY(scaleCoordinates(reaction.getY(), "y") + scaleCoordinates(reaction.getHeight() / 2, "y"));
                        ellipse2.setRadiusX(scaleCoordinates(reaction.getWidth() * 0.5 / 2, "x"));
                        ellipse2.setRadiusY(scaleCoordinates(reaction.getHeight() * 0.5 / 2, "y"));
                        ellipse2.setStroke(Color.BLACK);
                        ellipse2.setFill(Color.WHITE);
                        elementGroup.getChildren().add(ellipse2);
                        break;
                }

            }
        }

        for(Arc arc: arcs){
            drawArc(arc, reactionFocus);
        }

        container.getChildren().add(arcBackgroundGroup);
        container.getChildren().add(arcGroup);
        container.getChildren().add(elementGroup);

        elementGroup.toFront();
    }

    public void drawArc(Arc arc, String reactionFocus){
        Path path=null;
        Path backgroundPath=null;


        for (int i = 0; i < arc.getPoints().size(); i++) {
            if(i==0){

                path = new Path(new MoveTo(scaleCoordinates(arc.getPoints().get(0).getKey(), "x"),
                        scaleCoordinates(arc.getPoints().get(0).getValue(), "y")));
                backgroundPath = new Path(new MoveTo(scaleCoordinates(arc.getPoints().get(0).getKey(), "x"),
                        scaleCoordinates(arc.getPoints().get(0).getValue(), "y")));

            }
            else{
                path.getElements().add(new LineTo(scaleCoordinates(arc.getPoints().get(i).getKey(), "x"),
                        scaleCoordinates(arc.getPoints().get(i).getValue(), "y")));
                backgroundPath.getElements().add(new LineTo(scaleCoordinates(arc.getPoints().get(i).getKey(), "x"),
                        scaleCoordinates(arc.getPoints().get(i).getValue(), "y")));
            }
        }

        backgroundPath.setStrokeWidth(15);
        backgroundPath.setStyle("-fx-stroke: #F4F41A");
        backgroundPath.setOpacity(0);

        arcBackgroundGroup.getChildren().add(backgroundPath);
        path.setStrokeWidth(3);

        arcGroup.getChildren().add(path);

        String reactionId=null;
        Pattern pattern = Pattern.compile("(reactionVertex_\\d+)[_.]\\d+", Pattern.CASE_INSENSITIVE);

        path.setOnMouseEntered(event -> onEnterReaction((javafx.scene.Node) event.getSource()));
        path.setOnMouseExited(event -> onLeaveReaction((javafx.scene.Node) event.getSource()));
        backgroundPath.setOnMouseEntered(event -> onEnterReaction((javafx.scene.Node) event.getSource()));
        backgroundPath.setOnMouseExited(event -> onLeaveReaction((javafx.scene.Node) event.getSource()));




        if(arc.getSource().contains("reactionVertex")){

            Matcher matcher = pattern.matcher(arc.getSource());
            if(matcher.find()){
                reactionId=matcher.group(1);
            }

        }else if(arc.getTarget().contains("reactionVertex")){
            Matcher matcher = pattern.matcher(arc.getTarget());
            if(matcher.find()){
                reactionId=matcher.group(1);
            }
        }
        if(reactionId!=null){
            Tooltip t = new Tooltip(reactions.get(reactionId).getLabel());
            t.setShowDelay(new Duration(0.1));
            Tooltip.install(path, t);
        }

        if(!reactionNodes.containsKey(reactionId))
            reactionNodes.put(reactionId, new ArrayList<>());

        reactionNodes.get(reactionId).add(backgroundPath);
        reactionNodes.get(reactionId).add(path);

        if(reactionId.equals(reactionLabelId.get(reactionFocus))){
            backgroundPath.setOpacity(1);
        }


        if((!arc.getType().equals("catalysis") && !arc.getType().equals("consumption")) && !arc.getTarget().contains("reaction")) {


            double dy, dx, theta;
            Arrow arrow = new Arrow();
            arrow.setStrokeWidth(3);


            if(!firstIsCloser(elements.get(arc.getTarget()), arc.getPoints().get(0).getKey(), arc.getPoints().get(0).getValue(),
                    arc.getPoints().get(1).getKey(),
                    arc.getPoints().get(1).getValue())){
                dx = -(scaleCoordinates(arc.getPoints().get(0).getKey(), "x") - scaleCoordinates(arc.getPoints().get(1).getKey(), "x"));
                dy = -(scaleCoordinates(arc.getPoints().get(0).getValue(), "y") - scaleCoordinates(arc.getPoints().get(1).getValue(), "y"));

                theta = Math.atan(-dy / dx);
                if(dx<0)
                    theta+=Math.PI;

                arrow.setStartX(scaleCoordinates(arc.getPoints().get(1).getKey(), "x"));
                arrow.setStartY(scaleCoordinates(arc.getPoints().get(1).getValue(), "y"));



                arrow.setEndX(scaleCoordinates(arc.getPoints().get(1).getKey(), "x") + 20 * Math.cos(theta));
                arrow.setEndY(scaleCoordinates(arc.getPoints().get(1).getValue(), "y") - 20 * Math.sin(theta));
            }else{
                dx = (scaleCoordinates(arc.getPoints().get(0).getKey(), "x") - scaleCoordinates(arc.getPoints().get(1).getKey(), "x"));
                dy = (scaleCoordinates(arc.getPoints().get(0).getValue(), "y") - scaleCoordinates(arc.getPoints().get(1).getValue(), "y"));

                theta = Math.atan(-dy / dx);
                if(dx<0)
                    theta+=Math.PI;
                arrow.setStartX(scaleCoordinates(arc.getPoints().get(0).getKey(), "x"));
                arrow.setStartY(scaleCoordinates(arc.getPoints().get(0).getValue(), "y"));




                arrow.setEndX(scaleCoordinates(arc.getPoints().get(0).getKey(), "x") + 20 * Math.cos(theta));
                arrow.setEndY(scaleCoordinates(arc.getPoints().get(0).getValue(), "y") - 20 * Math.sin(theta));


            }

            arrow.toFront();
            arcGroup.getChildren().add(arrow);


        }else if(arc.getType().equals("catalysis")){
            drawCatalysisCircle((Reaction) elements.get(arc.getTarget()), arc, elements.get(arc.getSource()));
        }

    }

    public double scaleCoordinates(double pos, String axis){
        double xScale = container.getWidth()/maxX, yScale = container.getHeight()/maxY;
        return axis.equals("x")?pos*xScale:pos*yScale;
    }

    public boolean firstIsCloser(Element element, double x1, double y1, double x2, double y2){
        return Math.sqrt(Math.pow((element.getX()+element.getWidth()/2)-x1, 2) + Math.pow((element.getY()+element.getHeight()/2)-y1, 2))<
                Math.sqrt(Math.pow((element.getX()+element.getWidth()/2)-x2, 2) + Math.pow((element.getY()+element.getHeight()/2)-y2, 2));
    }

    public void drawCatalysisCircle(Reaction reaction, Arc arc, Element source){

        double x, y;

        if(firstIsCloser(source, arc.getPoints().get(0).getKey(), arc.getPoints().get(0).getValue(), arc.getPoints().get(1).getKey(),
                arc.getPoints().get(1).getValue())){
            x=arc.getPoints().get(1).getKey();
            y=arc.getPoints().get(1).getValue();
        }else{
            x=arc.getPoints().get(0).getKey();
            y=arc.getPoints().get(0).getValue();
        }

        Circle symbol = new Circle();

        if(x<reaction.getX()+ reaction.getWidth() && x>reaction.getX()){

            if(Math.abs(y-reaction.getY())<Math.abs(y-(reaction.getY()+reaction.getHeight()))){ //top
                symbol.setRadius(scaleCoordinates(reaction.getWidth()*0.5, "x"));
                symbol.setCenterX(scaleCoordinates(reaction.getX()+reaction.getWidth()/2, "x"));
                symbol.setCenterY(scaleCoordinates(y+(reaction.getY()-y)/2, "y"));
            }else{ //bottom
                symbol.setRadius(scaleCoordinates(reaction.getWidth()*0.5, "x"));
                symbol.setCenterX(scaleCoordinates(reaction.getX()+reaction.getWidth()/2, "x"));
                symbol.setCenterY(scaleCoordinates(reaction.getY()+reaction.getHeight()+(reaction.getY()-(reaction.getY()+reaction.getHeight())/2), "y"));
            }

        }else if(y<reaction.getY()+ reaction.getHeight() && y>reaction.getY()){
            if(Math.abs(x-reaction.getX())<Math.abs(x-(reaction.getX()+reaction.getWidth()))){ //left
                symbol.setRadius(scaleCoordinates(reaction.getHeight()*0.5, "y"));
                symbol.setCenterX(scaleCoordinates(x+(reaction.getX()-x), "x"));
                symbol.setCenterY(scaleCoordinates(reaction.getY()+reaction.getHeight()/2, "y"));
            }else{ //right
                symbol.setRadius(scaleCoordinates(reaction.getWidth()*0.5, "x"));
                symbol.setCenterX(scaleCoordinates(reaction.getX()+reaction.getWidth()+(reaction.getX()-(reaction.getX()+reaction.getWidth())/2), "x"));
                symbol.setCenterY(scaleCoordinates(reaction.getY()+reaction.getHeight()/2, "y"));
            }
        }
        symbol.setFill(Color.WHITE);
        symbol.setStroke(Color.BLACK);
        container.getChildren().add(symbol);

    }


    public void search() {

        searchPathways = new HashMap<>();
        searchReactions = new HashMap<>();
        reactionListview.getItems().clear();
        pathwayListview.getItems().clear();

        try{
            URL yahoo = new URL("https://reactome.org/ContentService/search/query?query="+searchField.getText().replace(" ", "%20"));
            URLConnection yc = yahoo.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            yc.getInputStream()));
            String inputLine="";
            JSONObject object=null;
            while ((inputLine = in.readLine()) != null) {

                object = new JSONObject(inputLine);
            }

            for(Object o: object.getJSONArray("results")){
                JSONObject result = (JSONObject) o;
                for(Object o2: result.getJSONArray("entries")){
                    JSONObject entry = (JSONObject) o2;


                    if(entry.getJSONArray("species").getString(0).equals("Homo sapiens")){

                        SearchResult searchResult = new SearchResult(entry.getString("exactType"), entry.getString("stId"),
                                entry.getString("name").replaceAll("<.*?>", ""),
                                entry.has("summation")?entry.getString("summation").replaceAll("<.*?>", ""):"");


                        if(searchResult.getType().equals("Pathway")) {
                            searchPathways.put(searchResult.getName(), searchResult);
                            pathwayListview.getItems().add(searchResult.getName());
                        }
                        else if(searchResult.getType().equals("Reaction") || searchResult.getType().equals("BlackBoxEvent")) {
                            searchReactions.put(searchResult.getName(), searchResult);
                            reactionListview.getItems().add(searchResult.getName());
                        }
                    }
                }
            }


            in.close();


        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public void loadPathway(String pathwayId, String reaction){

        try{
            URL url = new URL("https://reactome.org/ContentService/exporter/event/"+pathwayId+".sbgn");
            URLConnection yc = url.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            yc.getInputStream()));
            StringBuilder sbgn = new StringBuilder();

            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                sbgn.append(inputLine);
            }

            parseSbgn(sbgn.toString(), reaction);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void loadReaction(String reaction) {


        try{
            URL url = new URL("https://reactome.org/ContentService/data/pathways/low/entity/"+searchReactions.get(reaction).getId()+"?species=9606");
            URLConnection yc = url.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            yc.getInputStream()));
            StringBuilder res = new StringBuilder();

            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                res.append(inputLine);
            }

            Matcher m = Pattern.compile("displayName\":\"(.*?)\".*?\"stId\":\"(.*?)\"", Pattern.DOTALL).matcher(res.toString());


            HashMap<String, String> pathways = new HashMap<>();

            while(m.find()){
                pathways.put(m.group(1), m.group(2));
            }


            String selectedPathway;
            if(pathways.size()==1){
                selectedPathway = pathways.values().iterator().next();
            }else{
                ChoiceDialog<String> choiceDialog = new ChoiceDialog<>(pathways.keySet().iterator().next(), pathways.keySet());
                choiceDialog.showAndWait();
                selectedPathway = pathways.get(choiceDialog.getSelectedItem());

            }

            loadPathway(selectedPathway, reaction);

        }catch (Exception e){
            e.printStackTrace();
        }


    }

    private void onEnterReaction(javafx.scene.Node source){

        ArrayList<javafx.scene.Node> selectedReactionNodes = null;
        for(Map.Entry<String, ArrayList<javafx.scene.Node>> nodes: reactionNodes.entrySet()){
            for(javafx.scene.Node node: nodes.getValue()){
                if(node==source){
                    selectedReactionNodes = nodes.getValue();
                    break;
                }
            }
        }

        for(javafx.scene.Node node: selectedReactionNodes){
            if(node.getClass().equals(Path.class)){
                Path path = (Path) node;
                if(path.getStrokeWidth()>10){
                    path.setOpacity(1);
                }
            }
        }

    }

    private void onLeaveReaction(javafx.scene.Node source){

        ArrayList<javafx.scene.Node> selectedReactionNodes = null;
        for(Map.Entry<String, ArrayList<javafx.scene.Node>> nodes: reactionNodes.entrySet()){
            for(javafx.scene.Node node: nodes.getValue()){
                if(node==source){
                    selectedReactionNodes = nodes.getValue();
                    break;
                }
            }
        }

        for(javafx.scene.Node node: selectedReactionNodes){
            if(node.getClass().equals(Path.class)){
                Path path = (Path) node;
                if(path.getStrokeWidth()>10){
                    path.setOpacity(0);
                }
            }
        }

    }
}
