package Controllers;

import Cds.CDS;
import Cds.Peptide;
import Cds.Transcript;
import Controllers.MSControllers.PeptideTableController;
import Controllers.drawerControllers.DrawerController;
import TablesModels.Variation;
import com.jfoenix.controls.JFXDrawer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MutatedCdsController implements Initializable {


    @FXML
    public ScrollPane scrollPane;
    @FXML
    private VBox mainBox;
    private JFXDrawer drawer;
    private DrawerController drawerController;


    private HashMap<Variation, Boolean> selectedVariations;
    private String aaSeq;
    private String rnaSeq;
    private static HashMap<String, String> translationTable;
    private double letterWidth=0;
    private double letterHeight=0;
    private Pane newAASeqBox = new Pane();
    private Pane peptidesPane = new Pane();
    private VBox extendedSequencesBox = new VBox();
    private ResultsController resultsController;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translationTable = new HashMap<>();
        defineTranslationTable();


    }

    public void setDrawer(JFXDrawer drawer, DrawerController drawerController){
        this.drawer = drawer;
        this.drawerController = drawerController;
    }

    public void setResultsController(ResultsController resultsController){
        this.resultsController = resultsController;
    }

    public void showCds(Variation selectedVariation, CDS cds, int centerPos){

        aaSeq = cds.getSequence();
        selectedVariations = new HashMap<>();

        mainBox.getChildren().clear();
        newAASeqBox.getChildren().clear();
        peptidesPane.getChildren().clear();



        Pane reverseComplementRnaBox = new Pane();
        Pane rnaBox = new Pane();
        Pane aabox = new Pane();
        Pane mutationBox = new Pane();

        Pane mutationPane = new Pane();


        if (cds.getStrand().equals("+")) {
            rnaSeq = cds.getRnaSeq();
        }else{
            rnaSeq = getReverseComplementRna(cds.getRnaSeq());
            centerPos = rnaSeq.length() - centerPos;
        }



        Text testLetter = new Text("T");
        testLetter.setFont(Font.font("Monospaced", 30));
        letterWidth = testLetter.getLayoutBounds().getWidth();
        letterHeight = testLetter.getLayoutBounds().getHeight();
        mutationBox.setPrefHeight(letterHeight*2);

//        for (int i = 0; i < rnaSeq.length(); i++) {
//            Text t = new Text(String.valueOf(rnaSeq.charAt(i)));
//            t.setFont(Font.font("Monospaced", 30));
//            rnaBox.getChildren().add(t);
//            letterWidth = t.getLayoutBounds().getWidth();
//        }
        Text t = new Text(rnaSeq);

        t.setFont(Font.font("Monospaced", 30));
        rnaBox.getChildren().add(t);

        Text trc = new Text(getReverseComplementRna(rnaSeq));

        trc.setFont(Font.font("Monospaced", 30));
        reverseComplementRnaBox.getChildren().add(trc);


        StringBuilder aaSeqsb = new StringBuilder();
        for (int i = 0; i < rnaSeq.length(); i++) {

            if(i%3==1){
                aaSeqsb.append(aaSeq.charAt((int)Math.floor((double)i/3)));
            }else{
                aaSeqsb.append(" ");
            }
        }

        Text taa = new Text(aaSeqsb.toString());
        taa.setFont(Font.font("Monospaced", 30));
        aabox.getChildren().add(taa);

        HashSet<Variation> addedVariations = new HashSet<>();

        for(Map.Entry<Transcript, Pair<Integer, Integer>> transcript: cds.getTranscriptsWithCdsPos().entrySet()){

            for(Variation variation: transcript.getKey().getVariations()){


                if(!addedVariations.contains(variation) && !variation.isSilent()){
                    System.out.println(variation.isSilent());
                    int pos = variation.getPositionInTranscript(transcript.getKey().getTranscriptId())-cds.getTranscriptsWithCdsPos().get(transcript.getKey()).getKey()+1;

                    if(cds.getStrand().equals("-")){
                        pos = rnaSeq.length() - pos - 1;
                    }


                    double x = pos*letterWidth;

                    if(pos>0 && pos<rnaSeq.length()) {

                        if (variation.getRef().length() == 1 && variation.getAlt().length() == 1) {

                            String alt = variation.getAlt();


                            for (int i = 0; i < alt.length(); i++) {
                                String altRna;
                                if(cds.getStrand().equals("+")){
                                    altRna = String.valueOf(alt.charAt(i));
                                }else{
                                    altRna = getReverseComplementRna(String.valueOf(alt.charAt(i)));
                                }

                                t = new Text(altRna);
                                t.setFont(Font.font("Monospaced", 30));
                                t.setX(x);
                                t.setY(30);

                                if(selectedVariation!=null && selectedVariation==variation)
                                    t.setFill(Color.rgb(106, 254, 13, 0.8));
                                else
                                    t.setFill(Color.rgb(247, 65, 65, 0.5));
                                mutationBox.getChildren().add(t);

                                x += letterWidth;


                                selectedVariations.put(variation, false);


                                Text finalT = t;
                                t.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {

                                    if(e.getButton().equals(MouseButton.PRIMARY)){
                                        selectedVariations.replace(variation, !selectedVariations.get(variation));
                                        if (selectedVariations.get(variation)) {
                                            finalT.setFill(Color.rgb(247, 65, 65, 1));
                                        } else {
                                            finalT.setFill(Color.rgb(247, 65, 65, 0.5));
                                        }
                                        showAlternativeCDS(cds, transcript.getKey());
                                    }else if(e.getButton().equals(MouseButton.SECONDARY)){
                                        drawerController.showVariation(variation);
                                        drawer.open();
                                        drawer.toFront();
                                    }
                                });

                            }
                        } else if (variation.getAlt().equals("")) {

                            String ref = variation.getRef();


                            if(cds.getStrand().equals("-")){
                                ref = getReverseComplementRna(ref);
                            }

                            for (int i = 0; i < ref.length(); i++) {

                                String refRna;
                                if(cds.getStrand().equals("+")){
                                    refRna = String.valueOf(ref.charAt(i));
                                }else{
                                    refRna = getReverseComplementRna(String.valueOf(ref.charAt(i)));
                                }

                                t = new Text(refRna);
                                t.setFont(Font.font("Monospaced", 30));
                                t.setX(x);
                                t.setY(30);
                                t.setFill(Color.rgb(247, 65, 65, 0.5));
                                mutationBox.getChildren().add(t);

                                Line line1 = new Line(x, 30, x + letterWidth, 30 + letterHeight);
                                Line line2 = new Line(x, 30 + letterHeight, x + letterWidth, 30);
                                mutationBox.getChildren().add(line1);
                                mutationBox.getChildren().add(line2);

                                x += letterWidth;


                                selectedVariations.put(variation, false);


                                Text finalT = t;
                                t.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                                    selectedVariations.replace(variation, !selectedVariations.get(variation));
                                    if (selectedVariations.get(variation)) {
                                        finalT.setFill(Color.rgb(247, 65, 65, 1));
                                    } else {
                                        finalT.setFill(Color.rgb(247, 65, 65, 0.5));
                                    }

                                    showAlternativeCDS(cds, transcript.getKey());
                                });
                            }

                        } else if (variation.getRef().equals("")) {

                            String alt = variation.getAlt();
                            x-= (alt.length()-1) * letterWidth / 2 + letterWidth / 2;

                            double xStart = x;

                            if(cds.getStrand().equals("-")){
                                alt = getReverseComplementRna(alt);
                            }

                            t = new Text(alt);
                            t.setFont(Font.font("Monospaced", 30));
                            t.setX(xStart);
                            t.setY(30);
                            t.setFill(Color.rgb(247, 65, 65, 0.5));
                            mutationBox.getChildren().add(t);


                            x += alt.length()*letterWidth;


                            selectedVariations.put(variation, false);


                            double xEnd = x;
                            Line line1 = new Line(xStart, letterHeight, xStart + (xEnd - xStart) / 2, 20 + letterHeight);
                            Line line2 = new Line(xEnd, letterHeight, xStart + (xEnd - xStart) / 2, 20 + letterHeight);
                            line1.setStrokeWidth(5);
                            line2.setStrokeWidth(5);
                            line1.setStroke(Color.rgb(247, 65, 65, 0.5));
                            line2.setStroke(Color.rgb(247, 65, 65, 0.5));


                            Text finalT = t;
                            t.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                                selectedVariations.replace(variation, !selectedVariations.get(variation));
                                if (selectedVariations.get(variation)) {
                                    finalT.setFill(Color.rgb(247, 65, 65, 1));
                                    line1.setStroke(Color.rgb(247, 65, 65, 1));
                                    line2.setStroke(Color.rgb(247, 65, 65, 1));
                                } else {
                                    finalT.setFill(Color.rgb(247, 65, 65, 0.5));
                                    line1.setStroke(Color.rgb(247, 65, 65, 0.5));
                                    line2.setStroke(Color.rgb(247, 65, 65, 0.5));
                                }

                                showAlternativeCDS(cds, transcript.getKey());
                            });


                            mutationBox.getChildren().add(line1);
                            mutationBox.getChildren().add(line2);


                        }
                        addedVariations.add(variation);
                    }
                }



            }
        }

        mutationPane.setPrefHeight(100);
        mutationPane.setPrefWidth(50000);
        mutationPane.setStyle("-fx-background-color: #868ED5");
        VBox.setVgrow(mutationPane, Priority.ALWAYS);
        VBox.setVgrow(reverseComplementRnaBox, Priority.ALWAYS);
        VBox.setVgrow(rnaBox, Priority.ALWAYS);
        VBox.setVgrow(aabox, Priority.ALWAYS);
        mainBox.getChildren().add(mutationBox);
        mainBox.getChildren().add(rnaBox);
        //mainBox.getChildren().add(reverseComplementRnaBox);
        mainBox.getChildren().add(aabox);
        mainBox.getChildren().add(newAASeqBox);
        mainBox.getChildren().add(extendedSequencesBox);
        mainBox.getChildren().add(peptidesPane);

        if(centerPos>-1){
            scrollPane.setHvalue((double)centerPos/rnaSeq.length());
        }


    }

    private void showAlternativeCDS(CDS cds, Transcript transcript){

        newAASeqBox.getChildren().clear();
        extendedSequencesBox.getChildren().clear();

        char[] charArray = rnaSeq.toCharArray();
        ArrayList<String> charList = new ArrayList<>(Arrays.asList(rnaSeq.split("")));

        for(Map.Entry<Variation, Boolean> variation: selectedVariations.entrySet()){
            if(variation.getValue()){
                int pos = variation.getKey().getPositionInTranscript(transcript.getTranscriptId())
                        -cds.getTranscriptsWithCdsPos().get(transcript).getKey()+1;

                if(cds.getStrand().equals("+")) {
                    if (variation.getKey().getRef().length() == 0) {
                        for (int i = variation.getKey().getAlt().length() - 1; i >= 0; i--) {
                            charList.add(pos, String.valueOf(variation.getKey().getAlt().charAt(i)));
                        }

                    } else if (variation.getKey().getAlt().length() == 0) {
                        for (int i = 0; i < variation.getKey().getRef().length(); i++) {
                            charList.remove(pos);
                        }
                    } else {

                        for (int i = 0; i < variation.getKey().getRef().length(); i++) {
                            charList.remove(pos);
                        }
                        for (int i = 0; i < variation.getKey().getAlt().length(); i++) {
                            charList.add(pos, String.valueOf(variation.getKey().getAlt().charAt(i)));
                        }

                    }
                }else{
                    if (variation.getKey().getRef().length() == 0) {
                        for (int i = variation.getKey().getAlt().length() - 1; i >= 0; i--) {
                            charList.add(rnaSeq.length()-pos-1,
                                    getReverseComplementRna(String.valueOf(variation.getKey().getAlt().charAt(i))));
                        }

                    } else if (variation.getKey().getAlt().length() == 0) {
                        for (int i = 0; i < variation.getKey().getRef().length(); i++) {
                            charList.remove(rnaSeq.length()-pos-1);
                        }
                    } else {

                        for (int i = 0; i < variation.getKey().getRef().length(); i++) {
                            charList.remove(rnaSeq.length()-pos-1);
                        }
                        for (int i = 0; i < variation.getKey().getAlt().length(); i++) {
                            charList.add(rnaSeq.length()-pos-1,
                                    getReverseComplementRna(String.valueOf(variation.getKey().getAlt().charAt(i))));
                        }

                    }
                }



            }
        }

        StringBuilder sb = new StringBuilder();
        for(String nc: charList){
            sb.append(nc);
        }

        String newAASeq = translateSequence(sb.toString());

        if(!newAASeq.endsWith("*")){
            extendSequence(cds);
        }else {

            StringBuilder aaSeqsb = new StringBuilder();
            double x = 0;
            for (int i = 0; i < rnaSeq.length(); i++) {

                if (i > newAASeq.length() * 3) {
                    break;
                }
                Text t;
                if (i % 3 == 1) {
                    t = new Text(String.valueOf(newAASeq.charAt((int) Math.floor((double) i / 3))));
                    if (newAASeq.charAt((int) Math.floor((double) i / 3)) != aaSeq.charAt((int) Math.floor((double) i / 3))) {
                        t.setFill(Color.RED);
                    }
                } else {
                    t = new Text(" ");
                }
                t.setX(x);
                t.setY(10);
                t.setFont(Font.font("Monospaced", 30));
                x += letterWidth;
                newAASeqBox.getChildren().add(t);
            }
//            Text t = new Text(aaSeqsb.toString());
//            t.setFont(Font.font("Monospaced", 30));
        }


        addPeptides(cds, newAASeq, transcript);

    }

    public static String translateSequence(String seq){
        StringBuilder aaSeq = new StringBuilder();
        for(int i = 0; i < seq.length(); i+= 3){
            if (i+3 > seq.length()){
                break;
            }
            String codon = seq.substring(i, i+3);
            aaSeq.append(translationTable.get(codon));

            if (translationTable.get(codon).equals("*")){
                break;
            }
        }

        return aaSeq.toString();
    }

    public static String getReverseComplementRna(String rnaSeq){
        StringBuilder output  = new StringBuilder();
        for (int i = rnaSeq.length() -1 ; i >= 0; i --){
            String nuclUpper = String.valueOf(rnaSeq.charAt(i)).toUpperCase();
            boolean isUpperCase = Character.isUpperCase(rnaSeq.charAt(i));

            switch (nuclUpper) {
                case "G":
                    output.append((isUpperCase) ? "C" : "c");
                    break;
                case "C":
                    output.append((isUpperCase) ? "G" : "g");
                    break;
                case "A":
                    output.append((isUpperCase) ? "T" : "t");
                    break;
                case "T":
                    output.append((isUpperCase) ? "A" : "a");
                    break;
            }
        }
        return output.toString();
    }


    private void defineTranslationTable(){
        translationTable.put("ATA","I");
        translationTable.put("ATC","I");
        translationTable.put("ATT","I");
        translationTable.put("ATG","M");
        translationTable.put("ACA","T");
        translationTable.put("ACC","T");
        translationTable.put("ACG","T");
        translationTable.put("ACT","T");
        translationTable.put("AAC","N");
        translationTable.put("AAT","N");
        translationTable.put("AAA","K");
        translationTable.put("AAG","K");
        translationTable.put("AGC","S");
        translationTable.put("AGT","S");
        translationTable.put("AGA","R");
        translationTable.put("AGG","R");
        translationTable.put("CTA","L");
        translationTable.put("CTC","L");
        translationTable.put("CTG","L");
        translationTable.put("CTT","L");
        translationTable.put("CCA","P");
        translationTable.put("CCC","P");
        translationTable.put("CCG","P");
        translationTable.put("CCT","P");
        translationTable.put("CAC","H");
        translationTable.put("CAT","H");
        translationTable.put("CAA","Q");
        translationTable.put("CAG","Q");
        translationTable.put("CGA","R");
        translationTable.put("CGC","R");
        translationTable.put("CGG","R");
        translationTable.put("CGT","R");
        translationTable.put("GTA","V");
        translationTable.put("GTC","V");
        translationTable.put("GTG","V");
        translationTable.put("GTT","V");
        translationTable.put("GCA","A");
        translationTable.put("GCC","A");
        translationTable.put("GCG","A");
        translationTable.put("GCT","A");
        translationTable.put("GAC","D");
        translationTable.put("GAT","D");
        translationTable.put("GAA","E");
        translationTable.put("GAG","E");
        translationTable.put("GGA","G");
        translationTable.put("GGC","G");
        translationTable.put("GGG","G");
        translationTable.put("GGT","G");
        translationTable.put("TCA","S");
        translationTable.put("TCC","S");
        translationTable.put("TCG","S");
        translationTable.put("TCT","S");
        translationTable.put("TTC","F");
        translationTable.put("TTT","F");
        translationTable.put("TTA","L");
        translationTable.put("TTG","L");
        translationTable.put("TAC","Y");
        translationTable.put("TAT","Y");
        translationTable.put("TAA","*");
        translationTable.put("TAG","*");
        translationTable.put("TGC","C");
        translationTable.put("TGT","C");
        translationTable.put("TGA","*");
        translationTable.put("TGG","W");
    }

    private void addPeptides(CDS cds, String alernativeCDS, Transcript transcript){
        peptidesPane.getChildren().clear();
        if(cds.getPeptides()!=null){
            for(Peptide peptide : cds.getPeptides()){
                Pattern pattern = Pattern.compile(peptide.getSequence());
                Matcher matcher = pattern.matcher(alernativeCDS);
                while(matcher.find()){
                    int pepMatchAaPosStart  =  matcher.start();
                    int pepMatchAaPosEnd  =  matcher.end() - 1;

                    Rectangle r = new Rectangle();
                    r.setWidth(letterWidth*3*(pepMatchAaPosEnd - pepMatchAaPosStart + 1));
                    r.setHeight(100);
                    r.setX(letterWidth*3*pepMatchAaPosStart);
                    r.setFill(Color.rgb(100,100,100));
                    peptidesPane.getChildren().add(r);

                    Text t = new Text("Peptide");
                    t.setFill(Color.WHITE);
                    t.setFont(new Font(20));
                    t.toFront();
                    t.setX(letterWidth*3*pepMatchAaPosStart + letterWidth * 3 * (pepMatchAaPosEnd - pepMatchAaPosStart + 1)/2 - t.getLayoutBounds().getWidth()/2);
                    t.setY(50-t.getLayoutBounds().getHeight()/4);

                    r.setOnMouseClicked(mouseEvent -> {
                        if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                            if(mouseEvent.getClickCount() == 2){
                                resultsController.showPeptideTab(peptide);
                                PeptideTableController.getInstance().findPeptideInTable(peptide.getSequence(), peptide.getRunName());
                            }
                        }
                    });
                }
            }

            for(Map.Entry<Variation, Boolean> entry: selectedVariations.entrySet()) {
                if(entry.getValue()) {

                    ArrayList<String> runs = new ArrayList<>();
                    for (Peptide peptide :entry.getKey().getPeptides(transcript.getTranscriptId())) {
                        runs.add(peptide.getRunName());
                    }

                    for (Peptide peptide :entry.getKey().getPeptides(transcript.getTranscriptId())) {

                        Pattern pattern = Pattern.compile(peptide.getSequence());
                        Matcher matcher = pattern.matcher(alernativeCDS);
                        while (matcher.find()) {
                            int pepMatchAaPosStart = matcher.start();
                            int pepMatchAaPosEnd = matcher.end() - 1;

                            Rectangle r = new Rectangle();
                            r.setWidth(letterWidth * 3 * (pepMatchAaPosEnd - pepMatchAaPosStart + 1));
                            r.setHeight(100);
                            r.setX(letterWidth * 3 * pepMatchAaPosStart);
                            r.setFill(Color.rgb(193, 154, 0, 0.4));
                            peptidesPane.getChildren().add(r);

                            Text t = new Text("Peptide");
                            t.setFill(Color.WHITE);
                            t.setFont(new Font(20));
                            t.toFront();
                            t.setX(letterWidth*3*pepMatchAaPosStart + letterWidth * 3 * (pepMatchAaPosEnd - pepMatchAaPosStart + 1)/2 - t.getLayoutBounds().getWidth()/2);
                            t.setY(50-t.getLayoutBounds().getHeight()/4);
                            peptidesPane.getChildren().add(t);

                            r.setOnMouseClicked(mouseEvent -> {
                                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                                    if (mouseEvent.getClickCount() == 2) {

                                        if(runs.size()>1){
                                            ChoiceDialog<String> choiceDialog = new ChoiceDialog<>(runs.get(0), runs);
                                            choiceDialog.showAndWait();
                                            PeptideTableController.getInstance().findPeptideInTable(peptide.getSequence(), choiceDialog.getSelectedItem());


                                        }else
                                            PeptideTableController.getInstance().findPeptideInTable(peptide.getSequence(), peptide.getRunName());

                                        resultsController.showPeptideTab(peptide);



                                    }
                                }
                            });
                        }
                    }
                }
            }
        }

    }

    public void reset(){
        mainBox.getChildren().clear();
    }

    public void extendSequence(CDS cds){


        for(Map.Entry<Transcript , Pair<Integer, Integer>> entry: cds.getTranscriptsWithCdsPos().entrySet()){
            Transcript transcript = entry.getKey();
            String rna;
            Pair<Integer, Integer> cdsPos = entry.getValue();
            if(cds.getStrand().equals("+")){
                rna = transcript.getSequence(cdsPos.getKey()-1);
            }else{
                rna = getReverseComplementRna(transcript.getSequence(1, cdsPos.getValue()));
            }


            ArrayList<String> charList = new ArrayList<>(Arrays.asList(rna.split("")));

            for(Map.Entry<Variation, Boolean> variation: selectedVariations.entrySet()){
                if(variation.getValue()){
                    int pos = variation.getKey().getPositionInTranscript(transcript.getTranscriptId())
                            -cds.getTranscriptsWithCdsPos().get(transcript).getKey()+1;

                    if(cds.getStrand().equals("+")) {
                        if (variation.getKey().getRef().length() == 0) {
                            for (int i = variation.getKey().getAlt().length() - 1; i >= 0; i--) {
                                charList.add(pos, String.valueOf(variation.getKey().getAlt().charAt(i)));
                            }

                        } else if (variation.getKey().getAlt().length() == 0) {
                            for (int i = 0; i < variation.getKey().getRef().length(); i++) {
                                charList.remove(pos);
                            }
                        } else {

                            for (int i = 0; i < variation.getKey().getRef().length(); i++) {
                                charList.remove(pos);
                            }
                            for (int i = 0; i < variation.getKey().getAlt().length(); i++) {
                                charList.add(pos, String.valueOf(variation.getKey().getAlt().charAt(i)));
                            }

                        }
                    }else{
                        if (variation.getKey().getRef().length() == 0) {
                            for (int i = variation.getKey().getAlt().length() - 1; i >= 0; i--) {
                                charList.add(rna.length()-pos-1,
                                        getReverseComplementRna(String.valueOf(variation.getKey().getAlt().charAt(i))));
                            }

                        } else if (variation.getKey().getAlt().length() == 0) {
                            for (int i = 0; i < variation.getKey().getRef().length(); i++) {
                                charList.remove(rna.length()-pos-1);
                            }
                        } else {

                            for (int i = 0; i < variation.getKey().getRef().length(); i++) {
                                charList.remove(rna.length()-pos-1);
                            }
                            for (int i = 0; i < variation.getKey().getAlt().length(); i++) {
                                charList.add(rna.length()-pos-1,
                                        getReverseComplementRna(String.valueOf(variation.getKey().getAlt().charAt(i))));
                            }

                        }
                    }



                }
            }

            StringBuilder sb = new StringBuilder();
            for(String nc: charList){
                sb.append(nc);
            }

            String newAASeq = translateSequence(sb.toString());


            Pane newRnaPane = new Pane();
            Text t = new Text(rna.substring(0, newAASeq.length()*3));

            t.setFont(Font.font("Monospaced", 30));
            newRnaPane.getChildren().add(t);
            extendedSequencesBox.getChildren().add(newRnaPane);

            Pane newAASeqPane = new Pane();


            double x = 0;
            for (int i = 0; i < newAASeq.length()*3; i++) {

                if (i > newAASeq.length() * 3) {
                    break;
                }

                if (i % 3 == 1) {
                    t = new Text(String.valueOf(newAASeq.charAt((int) Math.floor((double) i / 3))));
                    if ((int) Math.floor((double) i / 3)>aaSeq.length()-1 || newAASeq.charAt((int) Math.floor((double) i / 3))
                            != aaSeq.charAt((int) Math.floor((double) i / 3))) {
                        t.setFill(Color.RED);
                    }
                } else {
                    t = new Text(" ");
                }
                t.setX(x);
                t.setY(10);
                t.setFont(Font.font("Monospaced", 30));
                x += letterWidth;
                newAASeqPane.getChildren().add(t);
            }
            extendedSequencesBox.getChildren().add(newAASeqPane);
//            Text t = new Text(aaSeqsb.toString());
//            t.setFont(Font.font("Monospaced", 30));

        }


    }

    public static boolean isSilentMutation(Variation variation, Transcript transcript) {


        if(transcript.getHasCds()){
            for(CDS cds: transcript.getCdss()){
                String rna = cds.getRnaSeq();
                int startInCDS = variation.getPositionInCds(cds, transcript);

                if(startInCDS>0 && startInCDS<cds.getSequence().length()) {
                    String newRna = rna.substring(0, startInCDS) + variation.getAlt() + rna.substring(startInCDS + 1 + variation.getRef().length() - 1);
                    String prot;
                    if (cds.getStrand().equals("-")) {
                        prot = MutatedCdsController.translateSequence(MutatedCdsController.getReverseComplementRna(newRna));
                    } else {
                        prot = MutatedCdsController.translateSequence(newRna);
                    }
                    if (!prot.equals(cds.getSequence())) {
                        return false;
                    }
                }

            }
            return true;
        }
        return true;
    }
}
