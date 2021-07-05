package Controllers;

import graphics.PannableCanvas;
import graphics.SceneGestures;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import pathway.*;
import pathway.Arc;
import pathway.Process;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

public class PathwayController implements Initializable {

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

    private double maxX=0, maxY=0;

    private double xOffset, yOffset;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        container.setOnScroll((ScrollEvent event) -> {
            // Adjust the zoom factor as per your requirement
            double zoomFactor = 1.05;
            double deltaY = event.getDeltaY();
            if (deltaY < 0){
                zoomFactor = 1.9 - zoomFactor;
            }
            container.setScaleX(container.getScaleX() * zoomFactor);
            container.setScaleY(container.getScaleY() * zoomFactor);
        });



        container.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = container.getTranslateX() - event.getScreenX();
                yOffset = container.getTranslateY() - event.getScreenY();
            }
        });

        container.setOnMouseDragged(event -> {
            //setManaged(false);
            container.setTranslateX(event.getScreenX() + xOffset);
            container.setTranslateY(event.getScreenY() + yOffset);
            event.consume();

        });
    }

    public void parseSbgn(String sgbn){

        container.getChildren().clear();
        elements = new HashMap<>();
        arcs = new ArrayList<>();
        ignoreArcsFrom = new ArrayList<>();
        ignoreArcsFrom = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            File file = new File("test.sbgn");
            Document doc = builder.parse(new InputSource(new StringReader(sgbn)));

            parseNode(doc.getDocumentElement());

            draw();

        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }


    }

    public void parseNode(Node node) {
        // do something with the current node instead of System.out
        System.out.println(node.getNodeName());

        NodeList nodeList = node.getChildNodes();


        if(node.getNodeName().equals("glyph")){
            String nodeClass = node.getAttributes().getNamedItem("class").getNodeValue();
            switch (nodeClass) {
                case "complex":
                case "small molecule":
                case "simple chemical":
                case "macromolecule": {

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
                case "dissociation": {

                    Process process = null;
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

                                process = new Process(x, y,
                                        Double.parseDouble(subNode.getAttributes().getNamedItem("w").getNodeValue()),
                                        Double.parseDouble(subNode.getAttributes().getNamedItem("h").getNodeValue()),
                                        node.getAttributes().getNamedItem("id").getNodeValue(), nodeClass);

                                process.setLabel(label);

                                elements.put(node.getAttributes().getNamedItem("id").getNodeValue(), process);
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
                    process.setPorts(ports);
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

    public void draw(){


        for(Element element: elements.values()){

            if(element.getClass().equals(Element.class)){
                if(element.getType().equals("simple chemical")){
                    Ellipse ellipse = new Ellipse();
                    ellipse.setCenterX(scaleCoordinates(element.getX(), "x")+scaleCoordinates(element.getWidth()/2, "x"));
                    ellipse.setCenterY(scaleCoordinates(element.getY(), "y")+scaleCoordinates(element.getHeight()/2, "y"));
                    ellipse.setRadiusX(scaleCoordinates(element.getWidth()/2, "x"));
                    ellipse.setRadiusY(scaleCoordinates(element.getHeight()/2, "y"));
                    ellipse.setFill(Color.web("#A5D791"));
                    container.getChildren().add(ellipse);

                }else if(element.getType().equals("complex")){
                    Rectangle rectangle = new Rectangle(scaleCoordinates(element.getX(), "x"),
                            scaleCoordinates(element.getY(), "y"),
                            scaleCoordinates(element.getWidth(), "x"), scaleCoordinates(element.getHeight(), "y"));
                    rectangle.setFill(Color.web("#A2C6D7"));
                    container.getChildren().add(rectangle);
                }

                javafx.scene.control.Label text = new javafx.scene.control.Label(element.getLabel());
                text.setWrapText(true);
                text.setAlignment(Pos.CENTER);


                text.setLayoutX(scaleCoordinates(element.getX(), "x"));
                text.setPrefWidth(scaleCoordinates(element.getWidth(), "x"));
                text.setLayoutY(scaleCoordinates(element.getY(), "y"));
                text.setPrefHeight(scaleCoordinates(element.getHeight(), "y"));
//                text.setX(scaleCoordinates(element.getX(), "x")+scaleCoordinates(element.getWidth()/2, "x")-text.getLayoutBounds().getWidth()/2);
//                text.setY(scaleCoordinates(element.getY(), "y")+scaleCoordinates(element.getHeight()/2, "y")-text.getLayoutBounds().getHeight()/2);
                text.setTextFill(Color.web("#3C4ED7"));

                container.getChildren().add(text);
            }else if(element.getClass().equals(Process.class)){

                Process process = (Process) element;
                Path path=null;

                if(process.getType().equals("association")){
                    path = new Path(new MoveTo(scaleCoordinates(process.getPorts().get(0).getX(), "x"),
                            scaleCoordinates(process.getPorts().get(0).getY(), "y")),
                            new LineTo(scaleCoordinates(process.getX(), "x"),
                                    scaleCoordinates(process.getY()+process.getHeight()/2, "y")), new ClosePath());
                }else if(process.getType().equals("process") || process.getType().equals("dissociation")){
                    path = new Path(new MoveTo(scaleCoordinates(process.getX()+process.getWidth(), "x"),
                            scaleCoordinates(process.getY()+process.getHeight()/2, "y")),
                            new LineTo(scaleCoordinates(process.getPorts().get(1).getX(), "x"),
                                    scaleCoordinates(process.getPorts().get(1).getY(), "y")), new ClosePath());
                }else{
                    path = new Path();
                }


                path.toBack();
                container.getChildren().add(path);


                switch (process.getType()) {
                    case "process":
                        Rectangle rectangle = new Rectangle(scaleCoordinates(process.getX(), "x"),
                                scaleCoordinates(process.getY(), "y"),
                                scaleCoordinates(process.getWidth(), "x"), scaleCoordinates(element.getHeight(), "y"));
                        rectangle.setFill(Color.web("#FEFEFE"));
                        container.getChildren().add(rectangle);
                        break;
                    case "association":
                        Ellipse ellipse = new Ellipse();
                        ellipse.setCenterX(scaleCoordinates(process.getX(), "x") + scaleCoordinates(process.getWidth() / 2, "x"));
                        ellipse.setCenterY(scaleCoordinates(process.getY(), "y") + scaleCoordinates(process.getHeight() / 2, "y"));
                        ellipse.setRadiusX(scaleCoordinates(process.getWidth() / 2, "x"));
                        ellipse.setRadiusY(scaleCoordinates(process.getHeight() / 2, "y"));
                        ellipse.setFill(Color.BLACK);
                        container.getChildren().add(ellipse);


                        break;
                    case "dissociation":
                        Ellipse ellipse1 = new Ellipse();
                        ellipse1.setCenterX(scaleCoordinates(process.getX(), "x") + scaleCoordinates(process.getWidth() / 2, "x"));
                        ellipse1.setCenterY(scaleCoordinates(process.getY(), "y") + scaleCoordinates(process.getHeight() / 2, "y"));
                        ellipse1.setRadiusX(scaleCoordinates(process.getWidth() / 2, "x"));
                        ellipse1.setRadiusY(scaleCoordinates(process.getHeight() / 2, "y"));
                        ellipse1.setFill(Color.WHITE);
                        container.getChildren().add(ellipse1);

                        Ellipse ellipse2 = new Ellipse();
                        ellipse2.setCenterX(scaleCoordinates(process.getX(), "x") + scaleCoordinates(process.getWidth() / 2, "x"));
                        ellipse2.setCenterY(scaleCoordinates(process.getY(), "y") + scaleCoordinates(process.getHeight() / 2, "y"));
                        ellipse2.setRadiusX(scaleCoordinates(process.getWidth() * 0.5 / 2, "x"));
                        ellipse2.setRadiusY(scaleCoordinates(process.getHeight() * 0.5 / 2, "y"));
                        ellipse2.setStroke(Color.BLACK);
                        ellipse2.setFill(Color.WHITE);
                        container.getChildren().add(ellipse2);
                        break;
                }


            }


        }

        for(Arc arc: arcs){
            drawArc(arc);
        }
    }

    public void drawArc(Arc arc){
        Path path=null;
        for (int i = 0; i < arc.getPoints().size(); i++) {
            if(i==0){

                path = new Path(new MoveTo(scaleCoordinates(arc.getPoints().get(0).getKey(), "x"),
                        scaleCoordinates(arc.getPoints().get(0).getValue(), "y")));

            }
            else{
                path.getElements().add(new LineTo(scaleCoordinates(arc.getPoints().get(i).getKey(), "x"),
                        scaleCoordinates(arc.getPoints().get(i).getValue(), "y")));
            }
        }
        path.getElements().add(new ClosePath());
        path.toBack();

        container.getChildren().add(path);

        if((!arc.getType().equals("catalysis") && !arc.getType().equals("consumption")) && !arc.getTarget().contains("reaction")) {


            double dy, dx, theta;
            Arrow arrow = new Arrow();

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
            container.getChildren().add(arrow);


        }else if(arc.getType().equals("catalysis")){
            drawCatalysisCircle((Process) elements.get(arc.getTarget()), arc, elements.get(arc.getSource()));
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

    public void drawCatalysisCircle(Process reaction, Arc arc, Element source){

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


    public void search(MouseEvent mouseEvent) {


        try{
            URL yahoo = new URL("https://reactome.org/ContentService/search/query?query="+searchField.getText().replace(" ", "%20"));
            URLConnection yc = yahoo.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            yc.getInputStream()));
            String inputLine="";
            JSONObject object=null;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
                object = new JSONObject(inputLine);
            }

            String pathwayId = object.getJSONArray("results").getJSONObject(0).
                    getJSONArray("entries").getJSONObject(0).getString("stId");

            in.close();


            yahoo = new URL("https://reactome.org/ContentService/exporter/event/"+pathwayId+".sbgn");
            yc = yahoo.openConnection();
            in = new BufferedReader(
                    new InputStreamReader(
                            yc.getInputStream()));
            StringBuilder sbgn = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                sbgn.append(inputLine);
            }


            parseSbgn(sbgn.toString());








        }catch (Exception e){
            e.printStackTrace();
        }


    }
}
