package Controllers;

import Cds.PTM;
import FileReading.Phosphosite;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.*;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import pathway.Arc;
import pathway.Label;
import pathway.*;
import pathway.alerts.DgeAlert;
import pathway.alerts.MutationAlert;
import pathway.alerts.PTMAlert;
import pathway.alerts.SplicingAlert;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PathwayController implements Initializable {

    public static PathwayController instance;

    @FXML
    private Pane container;

    private HashMap<String, Element> elements = new HashMap<>();
    private ArrayList<Arc> arcs = new ArrayList<>();
    private ArrayList<String> ignoreArcsTo = new ArrayList<>();
    private ArrayList<String> ignoreArcsFrom = new ArrayList<>();
    private ArrayList<javafx.scene.control.Label> labels = new ArrayList<>();

    private double maxX=0, maxY=0;

    private double xOffset, yOffset;
    private final DoubleProperty fontSize = new SimpleDoubleProperty(10);

    private HashMap<String, Reaction> reactions = new HashMap<>();


    private HashMap<String, ArrayList<javafx.scene.Node>> reactionNodes;
    private HashMap<String, String> reactionLabelId;

    private HashMap<String, JSONObject> entitiesInfo;

    private Group arcBackgroundGroup;
    private Group arcGroup;
    private Group elementGroup;
    private Group heatmapScaleGroup;

    
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        instance = this;

        container.setOnScroll((ScrollEvent event) -> {

            double zoomFactor = 1.05;
            double deltaY = event.getDeltaY();
            if (deltaY < 0)
                zoomFactor = 1.9 - zoomFactor;

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


    }

    public static PathwayController getInstance(){ return instance; }


    public void parseSbgn(String sgbn, String pathway, String reaction){

        clear();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(sgbn)));

            parseNode(doc.getDocumentElement());
            addReactionDbId(pathway);
            draw(reaction);
            colorElements();


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

                                if(element.getType().equals("macromolecule")){
                                    addEntitiesInfo(element);
                                }else if(element.getType().equals("complex")){
                                    addEntitiesInfo(element);
                                }


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
                case "uncertain process":
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
                Rectangle rectangle = new Rectangle(scaleCoordinates(element.getX()),
                        scaleCoordinates(element.getY()),
                        scaleCoordinates(element.getWidth()), scaleCoordinates(element.getHeight()));

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
                text.setLayoutX(scaleCoordinates(label.getX()));
                text.setPrefWidth(scaleCoordinates(label.getWidth()));
                text.setLayoutY(scaleCoordinates(label.getY()));
                text.setPrefHeight(scaleCoordinates(label.getHeight()));

                container.getChildren().add(rectangle);
                container.getChildren().add(text);
            }

        }



        ArrayList<Thread> threads = new ArrayList(elements.size());
        for(Element element: elements.values()){

            if(element.getClass().equals(Element.class)){
                switch (element.getType()) {
                    case "simple chemical":
                        Ellipse ellipse = new Ellipse();
                        ellipse.setCenterX(scaleCoordinates(element.getX()) + scaleCoordinates(element.getWidth() / 2));
                        ellipse.setCenterY(scaleCoordinates(element.getY()) + scaleCoordinates(element.getHeight() / 2));
                        ellipse.setRadiusX(scaleCoordinates(element.getWidth() / 2));
                        ellipse.setRadiusY(scaleCoordinates(element.getHeight() / 2));
                        ellipse.setFill(Color.web("#A5D791"));

                        element.setNode(ellipse);

                        elementGroup.getChildren().add(element.getNodeGroups());

                        break;
                    case "complex": {
                        Rectangle rectangle = new Rectangle(scaleCoordinates(element.getX()),
                                scaleCoordinates(element.getY()),
                                scaleCoordinates(element.getWidth()), scaleCoordinates(element.getHeight()));


                        element.getNodeGroups().setOnMouseClicked(event -> {
                            if(event.getClickCount()==2){
                                PathwaySideController.getInstance().populateSelectionTable(element);
                            }else if(event.getClickCount()==1){
                                PathwaySideController.getInstance().showDescription(String.valueOf(entitiesInfo.get(element.getLabel()).getInt("peDbId")), element);
                            }
                        });

                        rectangle.setFill(Color.web("#A2C6D7"));
                        elementGroup.getChildren().add(element.getNodeGroups());
                        element.setNode(rectangle);
                        break;
                    }
                    case "macromolecule": {
                        Rectangle rectangle = new Rectangle(scaleCoordinates(element.getX()),
                                scaleCoordinates(element.getY()),
                                scaleCoordinates(element.getWidth()), scaleCoordinates(element.getHeight()));
                        rectangle.setFill(Color.web("#8DC7BB"));
                        elementGroup.getChildren().add(element.getNodeGroups());

                        element.getNodeGroups().setOnMouseClicked(event -> {
                            if(event.getClickCount()==2){
                                PathwaySideController.getInstance().populateSelectionTable(element);
                            }
                        });
                        element.setNode(rectangle);
                        break;
                    }
                    case "unspecified entity": {
                        Rectangle rectangle = new Rectangle(scaleCoordinates(element.getX()),
                                scaleCoordinates(element.getY()),
                                scaleCoordinates(element.getWidth()), scaleCoordinates(element.getHeight()));
                        rectangle.setFill(Color.web("#A0BBCD"));
                        elementGroup.getChildren().add(element.getNodeGroups());
                        element.setNode(rectangle);
                        break;
                    }
                }

                javafx.scene.control.Label text = new javafx.scene.control.Label(element.getLabel());
                labels.add(text);
                text.setWrapText(true);
                text.setAlignment(Pos.CENTER);

                text.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";"
                        ,""));


                text.setLayoutX(scaleCoordinates(element.getX()));
                text.setPrefWidth(scaleCoordinates(element.getWidth()));
                text.setLayoutY(scaleCoordinates(element.getY()));
                text.setPrefHeight(scaleCoordinates(element.getHeight()));
                text.setTextFill(Color.web("#3C4ED7"));

                element.setNodeLabel(text);

            }else if(element.getClass().equals(Reaction.class)){

                Reaction reaction = (Reaction) element;

                Arc arc = new Arc(reaction.getPorts().get(0).getId(), reaction.getPorts().get(1).getId(),
                        reaction.getType(), reaction.getId());
                ArrayList<Pair<Double, Double>> points = new ArrayList<>();

                if(reaction.getType().equals("association")){

                    points.add(new Pair<>(reaction.getPorts().get(0).getX(), reaction.getPorts().get(0).getY()));
                    points.add(new Pair<>(reaction.getX(), reaction.getY()+reaction.getHeight()/2));

                }else if(reaction.getType().contains("process") || reaction.getType().equals("dissociation")){

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
                        Rectangle rectangle = new Rectangle(scaleCoordinates(reaction.getX()),
                                scaleCoordinates(reaction.getY()),
                                scaleCoordinates(reaction.getWidth()), scaleCoordinates(element.getHeight()));
                        rectangle.setFill(Color.web("#FEFEFE"));
                        elementGroup.getChildren().add(rectangle);
                        break;
                    case "omitted process":

                        Group g = new Group();

                        Rectangle rect = new Rectangle(scaleCoordinates(reaction.getX()),
                                scaleCoordinates(reaction.getY()),
                                scaleCoordinates(reaction.getWidth()), scaleCoordinates(element.getHeight()));
                        rect.setFill(Color.web("#FEFEFE"));

                        Line l1 = new Line();
                        l1.setStartX(scaleCoordinates(reaction.getX()+reaction.getWidth()*0.2));
                        l1.setStartY(scaleCoordinates(reaction.getY()+reaction.getHeight()*0.2));
                        l1.setEndX(scaleCoordinates(reaction.getX()+reaction.getWidth()*0.4));
                        l1.setEndY(scaleCoordinates(reaction.getY()+reaction.getHeight()*0.8));

                        Line l2 = new Line();
                        l2.setStartX(scaleCoordinates(reaction.getX()+reaction.getWidth()*0.6));
                        l2.setStartY(scaleCoordinates(reaction.getY()+reaction.getHeight()*0.2));
                        l2.setEndX(scaleCoordinates(reaction.getX()+reaction.getWidth()*0.8));
                        l2.setEndY(scaleCoordinates(reaction.getY()+reaction.getHeight()*0.8));

                        g.getChildren().add(rect);
                        g.getChildren().add(l1);
                        g.getChildren().add(l2);

                        elementGroup.getChildren().add(g);
                        break;
                    case "association":
                        Ellipse ellipse = new Ellipse();
                        ellipse.setCenterX(scaleCoordinates(reaction.getX()) + scaleCoordinates(reaction.getWidth() / 2));
                        ellipse.setCenterY(scaleCoordinates(reaction.getY()) + scaleCoordinates(reaction.getHeight() / 2));
                        ellipse.setRadiusX(scaleCoordinates(reaction.getWidth() / 2));
                        ellipse.setRadiusY(scaleCoordinates(reaction.getHeight() / 2));
                        ellipse.setFill(Color.BLACK);
                        elementGroup.getChildren().add(ellipse);

                        break;
                    case "dissociation":
                        Ellipse ellipse1 = new Ellipse();
                        ellipse1.setCenterX(scaleCoordinates(reaction.getX()) + scaleCoordinates(reaction.getWidth() / 2));
                        ellipse1.setCenterY(scaleCoordinates(reaction.getY()) + scaleCoordinates(reaction.getHeight() / 2));
                        ellipse1.setRadiusX(scaleCoordinates(reaction.getWidth() / 2));
                        ellipse1.setRadiusY(scaleCoordinates(reaction.getHeight() / 2));
                        ellipse1.setFill(Color.WHITE);
                        elementGroup.getChildren().add(ellipse1);

                        Ellipse ellipse2 = new Ellipse();
                        ellipse2.setCenterX(scaleCoordinates(reaction.getX()) + scaleCoordinates(reaction.getWidth() / 2));
                        ellipse2.setCenterY(scaleCoordinates(reaction.getY()) + scaleCoordinates(reaction.getHeight() / 2));
                        ellipse2.setRadiusX(scaleCoordinates(reaction.getWidth() * 0.5 / 2));
                        ellipse2.setRadiusY(scaleCoordinates(reaction.getHeight() * 0.5 / 2));
                        ellipse2.setStroke(Color.BLACK);
                        ellipse2.setFill(Color.WHITE);
                        elementGroup.getChildren().add(ellipse2);
                        break;
                }

            }
            threads.add(setAlerts(element));
        }
        for(Thread thread: threads){
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
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

                path = new Path(new MoveTo(scaleCoordinates(arc.getPoints().get(0).getKey()),
                        scaleCoordinates(arc.getPoints().get(0).getValue())));
                backgroundPath = new Path(new MoveTo(scaleCoordinates(arc.getPoints().get(0).getKey()),
                        scaleCoordinates(arc.getPoints().get(0).getValue())));

            }
            else{
                path.getElements().add(new LineTo(scaleCoordinates(arc.getPoints().get(i).getKey()),
                        scaleCoordinates(arc.getPoints().get(i).getValue())));
                backgroundPath.getElements().add(new LineTo(scaleCoordinates(arc.getPoints().get(i).getKey()),
                        scaleCoordinates(arc.getPoints().get(i).getValue())));
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

            String finalReactionId = reactionId;
            path.setOnMouseClicked(event -> PathwaySideController.getInstance().showDescription(reactions.get(finalReactionId).getDbId(), null));
            backgroundPath.setOnMouseClicked(event -> PathwaySideController.getInstance().showDescription(reactions.get(finalReactionId).getDbId(), null));

            if(reactions.get(finalReactionId).getDbId()!=null){
                path.setStroke(Color.BLUE);
            }
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
                dx = -(scaleCoordinates(arc.getPoints().get(0).getKey()) - scaleCoordinates(arc.getPoints().get(1).getKey()));
                dy = -(scaleCoordinates(arc.getPoints().get(0).getValue()) - scaleCoordinates(arc.getPoints().get(1).getValue()));

                theta = Math.atan(-dy / dx);
                if(dx<0)
                    theta+=Math.PI;

                arrow.setStartX(scaleCoordinates(arc.getPoints().get(1).getKey()));
                arrow.setStartY(scaleCoordinates(arc.getPoints().get(1).getValue()));



                arrow.setEndX(scaleCoordinates(arc.getPoints().get(1).getKey()) + 20 * Math.cos(theta));
                arrow.setEndY(scaleCoordinates(arc.getPoints().get(1).getValue()) - 20 * Math.sin(theta));
            }else{
                dx = (scaleCoordinates(arc.getPoints().get(0).getKey()) - scaleCoordinates(arc.getPoints().get(1).getKey()));
                dy = (scaleCoordinates(arc.getPoints().get(0).getValue()) - scaleCoordinates(arc.getPoints().get(1).getValue()));

                theta = Math.atan(-dy / dx);
                if(dx<0)
                    theta+=Math.PI;
                arrow.setStartX(scaleCoordinates(arc.getPoints().get(0).getKey()));
                arrow.setStartY(scaleCoordinates(arc.getPoints().get(0).getValue()));




                arrow.setEndX(scaleCoordinates(arc.getPoints().get(0).getKey()) + 20 * Math.cos(theta));
                arrow.setEndY(scaleCoordinates(arc.getPoints().get(0).getValue()) - 20 * Math.sin(theta));


            }

            arrow.toFront();
            arcGroup.getChildren().add(arrow);


        }else if(arc.getType().equals("catalysis")){
            drawCatalysisCircle((Reaction) elements.get(arc.getTarget()), arc, elements.get(arc.getSource()));
        }

    }

    public double scaleCoordinates(double pos){
        double scale = Math.min(container.getWidth()/maxX, container.getHeight()/maxY);
        return pos*scale;
    }

    public boolean firstIsCloser(Element element, double x1, double y1, double x2, double y2){
        return Math.sqrt(Math.pow((element.getX()+element.getWidth()/2)-x1, 2) + Math.pow((element.getY()+element.getHeight()/2)-y1, 2))<
                Math.sqrt(Math.pow((element.getX()+element.getWidth()/2)-x2, 2) + Math.pow((element.getY()+element.getHeight()/2)-y2, 2));
    }

    public void drawCatalysisCircle(Reaction reaction, Arc arc, Element source){

        double x, y;

        Pair<Double, Double> start = arc.getPoints().get(0);
        Pair<Double, Double> end = arc.getPoints().get(arc.getPoints().size()-1);


        if(firstIsCloser(source, start.getKey(), start.getValue(), start.getKey(),
                end.getValue())){
            x=end.getKey();
            y=end.getValue();
        }else{
            x=start.getKey();
            y=start.getValue();
        }

        Circle symbol = new Circle();
        symbol.setRadius(scaleCoordinates(reaction.getWidth()*0.5));
//
//        symbol.setCenterX(scaleCoordinates(Math.min(x, (reaction.getX()+reaction.getWidth()/2))+Math.abs(x-(reaction.getX()+reaction.getWidth()/2))/2));
//        symbol.setCenterY(scaleCoordinates(Math.min(y, (reaction.getY()))+Math.abs(y-(reaction.getY()))/2));

        if(x<reaction.getX()){
            symbol.setCenterX(scaleCoordinates(Math.min(x, (reaction.getX()+reaction.getWidth()/2))+Math.abs(x-(reaction.getX()+reaction.getWidth()/2))/2));
        }else {
            symbol.setCenterX(scaleCoordinates(Math.min(x, (reaction.getX() + reaction.getWidth() / 2)) + Math.abs(x - (reaction.getX() + reaction.getWidth() / 2)) / 2));

        }
        if(y<reaction.getY()){
            symbol.setCenterY(scaleCoordinates(Math.min(y, (reaction.getY()))+Math.abs(y-(reaction.getY()))/2));
        }else{
            symbol.setCenterY(scaleCoordinates(Math.min(y, (reaction.getY()+reaction.getHeight()))+Math.abs(y-(reaction.getY()+reaction.getHeight()))/2));
        }
//

//        if(x<reaction.getX()+ reaction.getWidth() && x>reaction.getX()){
//
//            if(Math.abs(y-reaction.getY())<Math.abs(y-(reaction.getY()+reaction.getHeight()))){ //top
//                symbol.setRadius(scaleCoordinates(reaction.getWidth()*0.5));
//                symbol.setCenterX(scaleCoordinates(reaction.getX()+reaction.getWidth()/2));
//                symbol.setCenterY(scaleCoordinates(y+(reaction.getY()-y)/2));
//            }else{ //bottom
//                symbol.setRadius(scaleCoordinates(reaction.getWidth()*0.5));
//                symbol.setCenterX(scaleCoordinates(reaction.getX()+reaction.getWidth()/2));
//                symbol.setCenterY(scaleCoordinates(reaction.getY()+reaction.getHeight()+(symbol.getRadius()/2)));
//            }
//
//        }else if(y<reaction.getY()+ reaction.getHeight() && y>reaction.getY()){
//            if(Math.abs(x-reaction.getX())<Math.abs(x-(reaction.getX()+reaction.getWidth()))){ //left
//                symbol.setRadius(scaleCoordinates(reaction.getHeight()*0.5));
//
//                symbol.setCenterY(scaleCoordinates(reaction.getY()+reaction.getHeight()/2));
//            }else{ //right
//                symbol.setRadius(scaleCoordinates(reaction.getWidth()*0.5));
//
//                symbol.setCenterY(scaleCoordinates(reaction.getY()+reaction.getHeight()/2));
//            }
//        }
        symbol.setFill(Color.WHITE);
        symbol.setStroke(Color.BLACK);
        container.getChildren().add(symbol);

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

            getEntitiesInfo(pathwayId);

            Platform.runLater(()->parseSbgn(sbgn.toString(), pathwayId, reaction));

            ;
        }catch (Exception e){
            e.printStackTrace();
        }

    }



    public void loadReaction(String reaction) {


        try{
            URL url = new URL("https://reactome.org/ContentService/data/pathways/low/entity/"+PathwaySideController.getInstance().getSearchReactions().get(reaction).getId()+"?species=9606");
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

    private void getMacromoleculeInfo(Element element){
        try{
            URL yahoo = new URL("https://reactome.org/ContentService/search/query?query="+element.getLabel().replace(" ", "%20"));
            URLConnection yc = yahoo.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            yc.getInputStream()));
            String inputLine="";
            JSONObject object=null;
            while ((inputLine = in.readLine()) != null) {

                object = new JSONObject(inputLine);
            }


            boolean found = false;
            for(Object o : object.getJSONArray("results")){
                JSONObject result = (JSONObject) o;
                for(Object o2: result.getJSONArray("entries")){
                    JSONObject entry = (JSONObject) o2;
                    if(entry.getString("name").replaceAll("<.*?>", "").equals(element.getLabel())){

                        element.addEntity(new Gene(entry.getString("stId"),
                                entry.has("referenceName")?entry.getString("referenceName").replaceAll("<.*?>", ""):"",
                                entry.getString("referenceIdentifier")));
                        found=true;
                        break;
                    }
                }
                if(found)
                    break;

            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getEntitiesInfo(String pathwayId){
        entitiesInfo = new HashMap<>();

        try{
            URL yahoo = new URL("https://reactome.org/ContentService/data/participants/"+pathwayId);
            URLConnection yc = yahoo.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            yc.getInputStream()));
            String inputLine="";
            StringBuilder l = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {

                l.append(inputLine);
            }


            JSONArray res = new JSONArray(l.toString());
            for(Object o : res){
                JSONObject entity = (JSONObject) o;
                entitiesInfo.put(entity.getString("displayName").split(" \\[")[0], entity);

            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void addEntitiesInfo(Element element){
        try{


            for (Object o: entitiesInfo.get(element.getLabel()).getJSONArray("refEntities")) {
                JSONObject entityEntry = (JSONObject) o;
                element.addEntity(new Gene(String.valueOf(entityEntry.getLong("dbId")), entityEntry.getString("displayName").split(" ")[1]));
            }

        }catch (Exception e){
            //e.printStackTrace();
        }
    }

    public void colorElements(){

        double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;

        for(Element element: elements.values()){

            element.getColorRectangles().getChildren().clear();

            if(!element.getClass().equals(Compartment.class) && (element.getType().equals("macromolecule") || element.getType().equals("complex"))){

                if(PathwaySideController.getInstance().getSelectedColorVariable().equals("Phosphorylation")){
                    for(PTMAlert alert: element.getAlerts().stream().filter(e -> e.getClass().equals(PTMAlert.class)).toArray(PTMAlert[]::new)){
                        ArrayList<PTM> ptms = alert.getPtms();

                        double maxAmplitude = 0;
                        for(PTM ptm: ptms){
                            if(Math.abs(ptm.getLog2fc())>maxAmplitude) {
                                if (ptm.getLog2fc() < min)
                                    min = ptm.getLog2fc();
                                else if (ptm.getLog2fc() > max)
                                    max = ptm.getLog2fc();
                                maxAmplitude = Math.abs(ptm.getLog2fc());
                            }
                        }
                    }

                }else if(PathwaySideController.getInstance().getSelectedColorVariable().equals("RNA DGE")){
                    for(DgeAlert alert: element.getAlerts().stream().filter(e -> e.getClass().equals(DgeAlert.class)).toArray(DgeAlert[]::new)){
                        double fc = alert.getFc();
                        if(fc<min)
                            min = fc;
                        else if(fc>max)
                            max = fc;
                    }
                }else if(PathwaySideController.getInstance().getSelectedColorVariable().equals("Differencial protein abundance")){
                    for(DgeAlert alert: element.getAlerts().stream().filter(e -> e.getClass().equals(DgeAlert.class)).toArray(DgeAlert[]::new)){
                        double meanFc = alert.getProteins().values().stream().mapToDouble(Pair::getKey).average().orElse(Double.NaN);
                        if(!Double.isNaN(meanFc)) {
                            if (meanFc < min)
                                min = meanFc;
                            else if (meanFc > max)
                                max = meanFc;
                        }
                    }
                }else if(PathwaySideController.getInstance().getSelectedColorVariable().equals("Splicing")){
                    for(SplicingAlert alert: element.getAlerts().stream().filter(e -> e.getClass().equals(SplicingAlert.class)).toArray(SplicingAlert[]::new)){
                        ArrayList<SplicingAlert.SplicingEvent> events = alert.getEvents();
                        
                        double maxAmplitude = 0;
                        for(SplicingAlert.SplicingEvent event: events){
                            if(Math.abs(event.getDpsi())>maxAmplitude) {
                                if (event.getDpsi() < min)
                                    min = event.getDpsi();
                                else if (event.getDpsi() > max)
                                    max = event.getDpsi();
                                maxAmplitude = Math.abs(event.getDpsi());
                            }
                        }
                    }
                }
            }
        }

        for(Element element: elements.values()){
            if(!element.getClass().equals(Compartment.class) && (element.getType().equals("macromolecule")|| element.getType().equals("complex"))){
                int i=0;

                if(PathwaySideController.getInstance().getSelectedColorVariable().equals("Phosphorylation")){
                    int nbSites = Phosphosite.getPhosphositesNumberInElement(element.getLabel());
                    if(nbSites>0) {
                        for (PTMAlert alert : element.getAlerts().stream().filter(e -> e.getClass().equals(PTMAlert.class)).toArray(PTMAlert[]::new)) {


                            double maxAmplitude = 0;
                            PTM maxAmplitudePtm = null;
                            for (PTM ptm : alert.getPtms()) {
                                if (Math.abs(ptm.getLog2fc()) > maxAmplitude) {
                                    maxAmplitude = Math.abs(ptm.getLog2fc());
                                    maxAmplitudePtm = ptm;
                                }

                            }

                            Rectangle r = new Rectangle();

                            Rectangle complexRectangle = (Rectangle) element.getNode();

                            System.out.println(nbSites + " " + maxAmplitudePtm.getGene() + " " + maxAmplitudePtm.getPos());
                            r.setX(complexRectangle.getX() + (((double) i / nbSites) * complexRectangle.getWidth()));
                            r.setY(complexRectangle.getY());
                            r.setHeight(complexRectangle.getHeight());
                            r.setWidth((complexRectangle.getWidth() / nbSites));
                            element.getColorRectangles().getChildren().add(r);
                            element.getColorRectangles().toFront();
                            element.getNodeLabel().toFront();

                            alert.getPtms().stream().mapToDouble(PTM::getLog2fc).map(Math::abs).max().getAsDouble();
                            double hue = Color.GREEN.getHue() + (Color.RED.getHue() - Color.GREEN.getHue()) * (maxAmplitudePtm.getLog2fc() - min) / (max - min);
                            Color color = Color.hsb(hue, 1.0, 1.0);
                            r.setStyle("-fx-fill: " + toHexString(color));
                            i++;
                        }
                    }

                }else if(PathwaySideController.getInstance().getSelectedColorVariable().equals("RNA DGE")){

                    Gene[] genes = element.getEntities().stream().filter(e -> e.getClass().equals(Gene.class)).toArray(Gene[]::new);

                    for(DgeAlert alert: element.getAlerts().stream().filter(e -> e.getClass().equals(DgeAlert.class)).toArray(DgeAlert[]::new)){

                        Rectangle r = new Rectangle();

                        Rectangle complexRectangle = (Rectangle) element.getNode();

                        r.setX(complexRectangle.getX() + (((double) i / genes.length) * complexRectangle.getWidth()));
                        r.setY(complexRectangle.getY());
                        r.setHeight(complexRectangle.getHeight());
                        r.setWidth((complexRectangle.getWidth() / genes.length));
                        element.getColorRectangles().getChildren().add(r);
                        element.getColorRectangles().toFront();
                        element.getNodeLabel().toFront();

                        double hue = Color.GREEN.getHue() + (Color.RED.getHue() - Color.GREEN.getHue()) * (alert.getFc() - min) / (max - min);
                        Color color = Color.hsb(hue, 1.0, 1.0);
                        r.setStyle("-fx-fill: " + toHexString(color));

                        i++;
                    }

                }else if(PathwaySideController.getInstance().getSelectedColorVariable().equals("Differencial protein abundance")){

                    Gene[] genes = element.getEntities().stream().filter(e -> e.getClass().equals(Gene.class)).toArray(Gene[]::new);

                    for(DgeAlert alert: element.getAlerts().stream().filter(e -> e.getClass().equals(DgeAlert.class)).toArray(DgeAlert[]::new)){

                        double meanFc = alert.getProteins().values().stream().mapToDouble(Pair::getKey).average().orElse(Double.NaN);
                        if(!Double.isNaN(meanFc)) {
                            Rectangle r = new Rectangle();

                            Rectangle complexRectangle = (Rectangle) element.getNode();

                            r.setX(complexRectangle.getX() + (((double) i / genes.length) * complexRectangle.getWidth()));
                            r.setY(complexRectangle.getY());
                            r.setHeight(complexRectangle.getHeight());
                            r.setWidth((complexRectangle.getWidth() / genes.length));
                            element.getColorRectangles().getChildren().add(r);
                            element.getColorRectangles().toFront();
                            element.getNodeLabel().toFront();

                            double hue = Color.GREEN.getHue() + (Color.RED.getHue() - Color.GREEN.getHue()) * (alert.getFc() - min) / (max - min);
                            Color color = Color.hsb(hue, 1.0, 1.0);
                            r.setStyle("-fx-fill: " + toHexString(color));

                            i++;
                        }
                    }
                }else if(PathwaySideController.getInstance().getSelectedColorVariable().equals("Splicing")){

                    Gene[] genes = element.getEntities().stream().filter(e -> e.getClass().equals(Gene.class)).toArray(Gene[]::new);
                    for(SplicingAlert alert: element.getAlerts().stream().filter(e -> e.getClass().equals(SplicingAlert.class)).toArray(SplicingAlert[]::new)){
                        ArrayList<SplicingAlert.SplicingEvent> events = alert.getEvents();
                        
                        double maxAmplitude = 0;
                        SplicingAlert.SplicingEvent maxAmplitudeEvent = null;
                        for(SplicingAlert.SplicingEvent event: events){
                            if (Math.abs(event.getDpsi())>maxAmplitude){
                                maxAmplitude = Math.abs(event.getDpsi());
                                maxAmplitudeEvent = event;
                            }
                               
                        }

                        Rectangle r = new Rectangle();

                        Rectangle complexRectangle = (Rectangle) element.getNode();

                        r.setX(complexRectangle.getX() + (((double) i / genes.length) * complexRectangle.getWidth()));
                        r.setY(complexRectangle.getY());
                        r.setHeight(complexRectangle.getHeight());
                        r.setWidth((complexRectangle.getWidth() / genes.length));
                        element.getColorRectangles().getChildren().add(r);
                        element.getColorRectangles().toFront();
                        element.getNodeLabel().toFront();

                        double hue = Color.GREEN.getHue() + (Color.RED.getHue() - Color.GREEN.getHue()) * (maxAmplitudeEvent.getDpsi() - min) / (max - min);
                        Color color = Color.hsb(hue, 1.0, 1.0);
                        r.setStyle("-fx-fill: " + toHexString(color));

                        i++;
                        
                        
                    }
                }
            }
        }

        if(heatmapScaleGroup!=null)
            container.getChildren().remove(heatmapScaleGroup);
        heatmapScaleGroup = new Group();

        Stop[] stops = new Stop[] { new Stop(0, Color.hsb(Color.GREEN.getHue(), 1.0, 1.0)),
                new Stop(0.5,Color.hsb(Color.YELLOW.getHue(), 1.0, 1.0)),
                new Stop(1,Color.hsb(Color.RED.getHue(), 1.0, 1.0))};
        LinearGradient lg1 = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
        Rectangle r1 = new Rectangle(0, 0, 100, 100);
        r1.setFill(lg1);
        r1.setHeight(50);
        r1.setWidth(200);
        r1.setX(container.getWidth()-250);
        heatmapScaleGroup.getChildren().add(r1);

        NumberFormat formatter = new DecimalFormat("#0.00");

        javafx.scene.control.Label minLabel = new javafx.scene.control.Label(formatter.format(min));
        minLabel.setLayoutX(container.getWidth()-260);
        minLabel.setLayoutY(50);
        javafx.scene.control.Label maxLabel = new javafx.scene.control.Label(formatter.format(max));
        maxLabel.setLayoutX(container.getWidth()-65);
        maxLabel.setLayoutY(50);
        heatmapScaleGroup.getChildren().add(minLabel);
        heatmapScaleGroup.getChildren().add(maxLabel);

        container.getChildren().add(heatmapScaleGroup);
        container.toFront();

    }

    private static String format(double val) {
        String in = Integer.toHexString((int) Math.round(val * 255));
        return in.length() == 1 ? "0" + in : in;
    }

    public static String toHexString(Color value) {
        return "#" + (format(value.getRed()) + format(value.getGreen()) + format(value.getBlue()) + format(value.getOpacity()))
                .toUpperCase();
    }

    public Thread setAlerts(Element element){

        Thread t  = new Thread(()-> {
            //long startTime = System.currentTimeMillis();
            DgeAlert.setAlerts(element, this, true);
            //.out.println("DGE "+(System.currentTimeMillis() - startTime));
            //startTime = System.currentTimeMillis();
            SplicingAlert.setAlerts(element, this);
            //System.out.println("splicing "+(System.currentTimeMillis() - startTime));
            //startTime = System.currentTimeMillis();
            MutationAlert.setAlerts(element, this);
            //System.out.println("mutation "+(System.currentTimeMillis() - startTime));
            //startTime = System.currentTimeMillis();
            PTMAlert.setAlerts(element, this);
            //System.out.println("ptm "+(System.currentTimeMillis() - startTime));
        });
        t.start();
        return t;

    }


    public void refreshAlerts() {

        new Thread(()-> {
            ArrayList<Thread> threads = new ArrayList<>();
        for(Element element:elements.values()){
                element.clearAlerts();
                threads.add(setAlerts(element));
            }
        for(Thread thread:threads){
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    public void clear() {
        container.getChildren().clear();
        container.getTransforms().clear();
        container.setTranslateX(0);
        container.setTranslateY(0);
        maxX = 0; maxY = 0;
        xOffset = 0; yOffset = 0;
        elements = new HashMap<>();
        arcs = new ArrayList<>();
        ignoreArcsFrom = new ArrayList<>();
        reactions = new HashMap<>();
        reactionLabelId = new HashMap<>();
        reactionNodes = new HashMap<>();
    }

    public void addReactionDbId(String pathwayId){
        try{
            URL yahoo = new URL("https://reactome.org/ContentService/data/pathway/"+pathwayId+"/containedEvents");
            URLConnection yc = yahoo.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            yc.getInputStream()));

            JSONArray  object = new JSONArray(in.readLine());

            for(Object o : object){
                JSONObject result = (JSONObject) o;
                for(Reaction reaction: reactions.values()){
                    if(reaction.getLabel().equals(result.getString("displayName"))){
                        reaction.setDbId(result.getString("stId"));
                        break;
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
