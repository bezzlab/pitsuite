package Controllers;

import javafx.fxml.Initializable;
import javafx.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pathway.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class PathwayController implements Initializable {

    private ArrayList<Element> elements = new ArrayList<>();
    private ArrayList<Arc> arcs = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        parseSbgn();
    }

    public void parseSbgn(){

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            File file = new File("test.sbgn");
            Document doc = builder.parse(file);

            parseNode(doc.getDocumentElement());

            System.out.println("a");

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
                case "macromolecule": {

                    Element element;
                    String label = "";

                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node subNode = nodeList.item(i);
                        if (subNode.getNodeType() == Node.ELEMENT_NODE) {
                            if (subNode.getNodeName().equals("bbox")) {
                                element = new Element(Double.parseDouble(subNode.getAttributes().getNamedItem("x").getNodeValue()),
                                        Double.parseDouble(subNode.getAttributes().getNamedItem("y").getNodeValue()),
                                        Double.parseDouble(subNode.getAttributes().getNamedItem("w").getNodeValue()),
                                        Double.parseDouble(subNode.getAttributes().getNamedItem("h").getNodeValue()),
                                        node.getAttributes().getNamedItem("id").getNodeValue(), nodeClass, label);


                                elements.add(element);
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
                                compartment = new Compartment(Double.parseDouble(subNode.getAttributes().getNamedItem("x").getNodeValue()),
                                        Double.parseDouble(subNode.getAttributes().getNamedItem("y").getNodeValue()),
                                        Double.parseDouble(subNode.getAttributes().getNamedItem("w").getNodeValue()),
                                        Double.parseDouble(subNode.getAttributes().getNamedItem("h").getNodeValue()),
                                        node.getAttributes().getNamedItem("id").getNodeValue());
                                compartment.setLabel(label);

                                elements.add(compartment);
                            } else if (subNode.getNodeName().equals("label")) {


                                for (int j = 0; j < node.getChildNodes().getLength(); j++) {
                                    Node labelNode = node.getChildNodes().item(j);
                                    if (labelNode.getNodeType() == Node.ELEMENT_NODE) {

                                        for (int k = 0; k < labelNode.getChildNodes().getLength(); k++) {
                                            Node labelBboxNode = labelNode.getChildNodes().item(k);
                                            if (labelBboxNode.getNodeType() == Node.ELEMENT_NODE) {

                                                label = new Label(Double.parseDouble(labelBboxNode.getAttributes().getNamedItem("x").getNodeValue()),
                                                        Double.parseDouble(labelBboxNode.getAttributes().getNamedItem("y").getNodeValue()),
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
                case "association": {

                    Association association = null;
                    String label = "";
                    ArrayList<Port> ports = new ArrayList<>();

                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node subNode = nodeList.item(i);
                        if (subNode.getNodeType() == Node.ELEMENT_NODE) {
                            if (subNode.getNodeName().equals("bbox")) {
                                association = new Association(Double.parseDouble(subNode.getAttributes().getNamedItem("x").getNodeValue()),
                                        Double.parseDouble(subNode.getAttributes().getNamedItem("y").getNodeValue()),
                                        Double.parseDouble(subNode.getAttributes().getNamedItem("w").getNodeValue()),
                                        Double.parseDouble(subNode.getAttributes().getNamedItem("h").getNodeValue()),
                                        node.getAttributes().getNamedItem("id").getNodeValue());

                                association.setLabel(label);

                                elements.add(association);
                            } else if (subNode.getNodeName().equals("label")) {
                                label = subNode.getAttributes().getNamedItem("text").getNodeValue();
                            } else if (subNode.getNodeName().equals("port")) {
                                ports.add(new Port(Double.parseDouble(subNode.getAttributes().getNamedItem("x").getNodeValue()),
                                        Double.parseDouble(subNode.getAttributes().getNamedItem("y").getNodeValue()),
                                        subNode.getAttributes().getNamedItem("id").getNodeValue()));
                            }
                        }
                    }
                    break;
                }


            }
        }else if(node.getNodeName().equals("arc")){
            ArrayList<Pair<Double, Double>> points = new ArrayList<>();

            Arc arc = new Arc(node.getAttributes().getNamedItem("source").getNodeValue(),
                    node.getAttributes().getNamedItem("target").getNodeValue(),
                    node.getAttributes().getNamedItem("class").getNodeValue(), node.getAttributes().getNamedItem("id").getNodeValue());

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node subNode = node.getChildNodes().item(i);
                if (subNode.getNodeType() == Node.ELEMENT_NODE) {
                    points.add(new Pair(subNode.getAttributes().getNamedItem("x").getNodeValue(),
                            Double.parseDouble(subNode.getAttributes().getNamedItem("y").getNodeValue())));
                }
            }

            arc.setPoints(points);
            arcs.add(arc);

        }

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                //calls this method for all the children which is Element
                parseNode(currentNode);
            }
        }


    }

    public double scaleCoordinates(){
        return 0.;
    }


}
