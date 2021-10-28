package Controllers;

import Cds.Exon;
import Cds.Transcript;
import Singletons.ColorPalette;
import Singletons.Config;
import Singletons.TrackFiles;
import TablesModels.BamFile;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.IntervalList;
import htsjdk.samtools.util.SamLocusIterator;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import pitguiv2.Settings;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class BamController implements Initializable {


    @FXML
    public VBox mainBox;

    private String chr;
    private int geneStart;
    private int geneEnd;
    private List<BamFile> selectedFiles;
    private double fontSize=22;
    private double representationWidthFinal;
    private double representationHeightFinal;
    private ArrayList<Transcript> displayedTranscripts;
    private int currentStart, currentEnd;
    private Nitrite db;

    private final HashMap<String, List<HBox>> panes = new HashMap<>();


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }



    public void showGene(String chrId, int geneStart, int geneEnd, double representationWidthFinal, double representationHeightFinal, Nitrite db){

        this.db = db;
        this.geneStart = geneStart;
        this.geneEnd = geneEnd;
        this.chr = chrId;
        this.representationWidthFinal = representationWidthFinal;
        this. representationHeightFinal = representationHeightFinal;

        setCondSamplDepth(chrId, geneStart, geneEnd);

    }

    public void onTrackFilesUpdated(){


        for (BamFile file : TrackFiles.getBamFiles()) {
            if(file.isSelected()){
                if (selectedFiles!= null && selectedFiles.contains(file) && file.getDepth()==null) {
                    file.setDepth(selectedFiles.stream()
                            .filter(previousFile -> file.getPath().equals(previousFile.getPath()))
                            .findAny().get().getDepth());
                } else {
                    try {
                        if(chr!=null)
                            file.setDepth(getGeneRnaDepth(file.getPath(), chr, geneStart, geneEnd));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }

        this.selectedFiles = TrackFiles.getBamFiles();
        if(chr!=null){
            drawDepth(currentStart, currentEnd);
        }

    }

    public void show(int start, int end){
        currentStart = start;
        currentEnd = end;
        panes.clear();
        drawDepth(start, end);
    }

    private void setCondSamplDepth(String chrId, int geneStart, int geneEnd) {

        ArrayList<Thread> threads = new ArrayList<>();

        if(selectedFiles==null) {
            selectedFiles = new ArrayList<>();

            Cursor bamPathsCollection = db.getCollection("bamPaths").find();

            for (Document bamFileDoc : bamPathsCollection) {
                String bamCond = (String) bamFileDoc.get("condition");
                String bamSample = (String) bamFileDoc.get("sample");
                String bamPath = (String) bamFileDoc.get("bamPath");
                BamFile file = new BamFile(bamPath, bamCond, bamSample);
                selectedFiles.add(file);
                TrackFiles.addBam(file);

            }
        }

        // read bam file
        for (BamFile file: selectedFiles) {
            if(file.isSelected()){
                Thread t = new Thread(() -> {
                    try {
                        file.setDepth(getGeneRnaDepth(file.getPath(), chrId, geneStart, geneEnd));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                t.start();
                threads.add(t);
            }


        }

        try{
            for(Thread t: threads){
                t.join();
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }



    private int[] getGeneRnaDepth(String filePath, String chrId, int geneStart, int geneEnd) throws IOException {

        long startTime = System.currentTimeMillis();

        int[] depthList = new int[geneEnd-geneStart+1];

        SamReader samReader = SamReaderFactory.makeDefault().open(new File(filePath));

        Interval interval = new Interval(chrId, geneStart, geneEnd);


        IntervalList iL = new IntervalList(samReader.getFileHeader());
        iL.add(interval);

        SamLocusIterator sli = new SamLocusIterator(samReader, iL, true);

        int i = 0;
        for (SamLocusIterator.LocusInfo locusInfo : sli) {
            // get depth
            depthList[i++] = locusInfo.getRecordAndOffsets().size();

        }

        sli.close();
        samReader.close();

        return depthList;
    }


    private void drawDepth(int start, int end) {

        if(selectedFiles.size()>0) {

            mainBox.getChildren().clear();


            int indexStart = start - geneStart;
            int indexEnd = end - geneStart;

            ArrayList<String> conditions = new ArrayList<>(Config.getConditions());

            ExecutorService es = Executors.newFixedThreadPool(selectedFiles.size());


            for (BamFile file : selectedFiles) {
                if (file.isSelected()) {

                    es.execute(() -> {
                        // get values for the graph, if distance is high, the values are accumulated
                        Thread junctionThread = new Thread(() -> getBamJunctions(file, chr, start, end));
                        junctionThread.start();

                        int[] depthList = file.getDepth();


                        int interval = 1;
                        int cumsum = 0;
                        boolean isAccumulated = false;
                        ArrayList<Integer> indexes = new ArrayList<>();

                        if ((indexEnd - indexStart) > 10000) {
                            isAccumulated = true;
                            interval = 30;
                        } else if ((indexEnd - indexStart) > 5000) {
                            isAccumulated = true;
                            interval = 10;
                        } else if ((indexEnd - indexStart) > 1000) {
                            isAccumulated = true;
                            interval = 3;
                        }

                        int[] depthListFinal = new int[(int) Math.ceil((double) (indexEnd - indexStart) / interval)];

                        int ymax;
                        if (isAccumulated) {

                            for (int i = indexStart; i < indexEnd; i++) {
                                if ((i - indexStart) % interval == 0) {
                                    cumsum += depthList[i];
                                    indexes.add(i);
                                    depthListFinal[(int) Math.floor((double) (i - indexStart) / interval)] = cumsum;

                                    cumsum = 0;
                                } else {

                                    cumsum += depthList[i];
                                }

                            }
                            depthListFinal[depthListFinal.length - 1] = cumsum;

                            ymax = Arrays.stream(depthListFinal).summaryStatistics().getMax();
                        } else {
                            ymax = Arrays.stream(depthList).summaryStatistics().getMax();
                            depthListFinal = Arrays.stream(depthList, indexStart, indexEnd).toArray();
                        }

                        int[] finalDepthListFinal = depthListFinal;


                        final HBox areaPlotHBox = new HBox();

                        Text condSampleText = new Text(file.getCondition() + " " + (file.getSample() != null ? file.getSample() : ""));
                        condSampleText.setFont(Font.font("monospace", fontSize));

                        areaPlotHBox.setPrefWidth(representationWidthFinal);


                        areaPlotHBox.setStyle("-fx-background-color: transparent;");


                        AnchorPane plotPane = new AnchorPane();
                        plotPane.setPrefHeight(representationHeightFinal * 0.05);


                        areaPlotHBox.getChildren().clear();

                        Path path = new Path();
                        path.getElements().add(new MoveTo(0, representationHeightFinal * 0.05));

                        for (int i = 0; i < finalDepthListFinal.length; i++) {
                            if (i != 0)
                                path.getElements().add(new LineTo(i * representationWidthFinal / finalDepthListFinal.length,
                                        representationHeightFinal * 0.05 - finalDepthListFinal[i] * representationHeightFinal * 0.05 / ymax));

                            if (i == finalDepthListFinal.length - 1)
                                path.getElements().add(new LineTo(i * representationWidthFinal / finalDepthListFinal.length, representationHeightFinal * 0.05));
                        }
                        path.getElements().add(new ClosePath());
                        path.setFill(Color.web(ColorPalette.getColor(conditions.indexOf(file.getCondition()))));

                        Pane coveragePane = new Pane();
                        coveragePane.setPrefWidth(representationWidthFinal);
                        coveragePane.setPrefHeight(representationHeightFinal * 0.05);

                        coveragePane.getChildren().add(path);
                        plotPane.getChildren().add(coveragePane);

                        Pane sashimiPane = new Pane();

                        sashimiPane.toFront();

                        plotPane.getChildren().add(sashimiPane);

                        areaPlotHBox.getChildren().add(plotPane);

                        Image eyeImage = new Image("visibility.png");
                        Button hideButton = new Button();
                        ImageView imageView = new ImageView(eyeImage);

                        imageView.setFitHeight(condSampleText.getBoundsInLocal().getHeight() * 1.2);
                        imageView.setPreserveRatio(true);

                        hideButton.setGraphic(imageView);
                        hideButton.setOnMouseClicked(event -> {
                            event.consume();
                            file.setSelected(false);
                            mainBox.getChildren().remove(areaPlotHBox);
                        });


//                        condSampleText.setLayoutY(representationHeightFinal* 0.05/2 -
//                                condSampleText.getLayoutBounds().getHeight()/2);

                        HBox.setMargin(condSampleText, new Insets(representationHeightFinal * 0.05 / 2, 0, 0, 0));
                        HBox.setMargin(hideButton, new Insets(representationHeightFinal * 0.05 / 2, 0, 0, 0));

                        areaPlotHBox.getChildren().add(hideButton);
                        areaPlotHBox.getChildren().add(condSampleText);

                        VBox.setVgrow(areaPlotHBox, Priority.ALWAYS);

                        if (!panes.containsKey(file.getCondition())) {
                            panes.put(file.getCondition(), Collections.synchronizedList(new ArrayList<>()));
                        }
                        panes.get(file.getCondition()).add(areaPlotHBox);


                        VBox.setMargin(areaPlotHBox, new Insets(0, 0, 0, 0));


                        try {
                            junctionThread.join();
                            drawSashami(sashimiPane, file, start, end, geneStart, representationHeightFinal * 0.05, areaPlotHBox, coveragePane);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    });
                }
            }


            es.shutdown();
            try {
                boolean finished = es.awaitTermination(1, TimeUnit.MINUTES);
                for (List<HBox> boxes : panes.values()) {
                    for (HBox box : boxes) {
                        Platform.runLater(() -> mainBox.getChildren().add(box));
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



    private void getBamJunctions(BamFile file, String chrId, int geneStart, int geneEnd){
        SamReader samReader = SamReaderFactory.makeDefault().open(new File(file.getPath()));

        HashMap<Exon, HashSet<SAMRecord>> exonsReads = displayedExons();


        SAMRecordIterator it = samReader.queryContained(chrId, geneStart, geneEnd);
        for (SAMRecordIterator iter = it; iter.hasNext(); ) {

            SAMRecord samRecord = iter.next();


            for(Map.Entry<Exon, HashSet<SAMRecord>> exon: exonsReads.entrySet()){
                if((samRecord.getAlignmentStart()>=exon.getKey().getStart() && samRecord.getAlignmentStart()<=exon.getKey().getEnd()) ||
                        (samRecord.getAlignmentEnd()>=exon.getKey().getStart() && samRecord.getAlignmentEnd()<=exon.getKey().getEnd())){
                    exon.getValue().add(samRecord);
                }
            }
        }


        HashMap<Pair<Exon, Exon>, Integer> junctions = new HashMap<>();



        ArrayList<Exon> exons = new ArrayList(exonsReads.keySet());
        for (int i = 0; i < exons.size(); i++) {
            for (int j = i+1; j < exons.size(); j++) {
                HashSet<SAMRecord> reads1 = exonsReads.get(exons.get(i));
                HashSet<SAMRecord> reads2 = exonsReads.get(exons.get(j));
                Set<SAMRecord> intersection = new HashSet<>();
                for(Iterator<SAMRecord> r1It = reads1.iterator(); r1It.hasNext();){
                    intersection.add(r1It.next());
                }


                intersection.retainAll(reads2);
                if(intersection.size()>0){
                    junctions.put(new Pair<>(exons.get(i), exons.get(j)), intersection.size());
                }

            }
        }

        file.setJunctions(junctions);


    }

    private HashMap<Exon, HashSet<SAMRecord>> displayedExons(){


        HashMap<Exon, HashSet<SAMRecord>> exons = new HashMap<>();


        for(Transcript transcript: displayedTranscripts){
            for(Iterator<Exon> exonIt = transcript.getExons().iterator(); exonIt.hasNext();){
//            for(Exon exon: transcript.getExons()){
                Exon exon = exonIt.next();
                if(!exons.containsKey(exon)){
                    boolean overlaps = false;
                    for(Exon exon2: exons.keySet()){
                        if((exon2.getStart() == exon.getStart() && exon2.getEnd()!=exon.getEnd()) ||
                        (exon2.getStart() != exon.getStart() && exon2.getEnd()==exon.getEnd())){
                            overlaps=true;
                            break;
                        }
                    }
                    if(!overlaps){
                        exons.put(exon, new HashSet<>());
                    }

                }
            }
        }

        return exons;
    }

    public void drawSashami(Pane sashimiPane, BamFile file, int start, int end, int geneViewerMinimumCoordinate, double boxHeight, HBox container, Pane coveragePane){

        long startTime = System.currentTimeMillis();

        int max = Arrays.stream(file.getDepth()).summaryStatistics().getMax();

        HashMap<Exon, Integer> startBottomCount = new HashMap<>();
        HashMap<Exon, Integer> endBottomCount = new HashMap<>();
        HashMap<Exon, Integer> startTopCount = new HashMap<>();
        HashMap<Exon, Integer> endTopCount = new HashMap<>();

        HashMap<Pair<Exon, Exon>, Group> junctionsGraphicGroup = new HashMap<>();

        int maxTopGlobal = 0;
        int maxBottomGlobal = 0;

        Text t = new Text();
        t.setFont(Font.font(14));
        double textHeight = t.getBoundsInLocal().getHeight();

        int minReads = Settings.getInstance().getMinSashimiReads();

        for(Map.Entry<Pair<Exon, Exon>, Integer> junction: file.getJunctions().entrySet()){

            if(junction.getValue()>minReads) {

                Exon exon1 = junction.getKey().getKey();
                Exon exon2 = junction.getKey().getValue();

                if (!startBottomCount.containsKey(exon1)) {
                    startBottomCount.put(exon1, 0);
                }
                if (!endBottomCount.containsKey(exon1)) {
                    endBottomCount.put(exon1, 0);
                }
                if (!startTopCount.containsKey(exon1)) {
                    startTopCount.put(exon1, 0);
                }
                if (!endTopCount.containsKey(exon1)) {
                    endTopCount.put(exon1, 0);
                }
                if (!startBottomCount.containsKey(exon2)) {
                    startBottomCount.put(exon2, 0);
                }
                if (!endBottomCount.containsKey(exon2)) {
                    endBottomCount.put(exon2, 0);
                }
                if (!startTopCount.containsKey(exon2)) {
                    startTopCount.put(exon2, 0);
                }
                if (!endTopCount.containsKey(exon2)) {
                    endTopCount.put(exon2, 0);
                }

                Exon exonStart;
                Exon exonEnd;
                if (exon1.getStart() < exon2.getStart()) {
                    exonStart = exon1;
                    exonEnd = exon2;
                } else {
                    exonStart = exon2;
                    exonEnd = exon1;
                }
                if (exonStart.getEnd() != exonEnd.getEnd() && exonStart.getStart() != exonEnd.getStart()) {
                    int maxTop = Math.max(endTopCount.get(exonStart), startTopCount.get(exonEnd));
                    int maxBottom = Math.max(endBottomCount.get(exonStart), startBottomCount.get(exonEnd));


                    if (maxTop > maxBottom) {

                        if (endBottomCount.get(exonStart) + 1 > maxBottomGlobal) {
                            maxBottomGlobal = endBottomCount.get(exonStart) + 1;
                            boxHeight += textHeight;
                        }
                        if (startBottomCount.get(exonStart) + 1 > maxBottomGlobal) {
                            maxBottomGlobal = startBottomCount.get(exonStart) + 1;
                            boxHeight += textHeight;

                        }

                        endBottomCount.replace(exonStart, endBottomCount.get(exonStart) + 1);
                        startBottomCount.replace(exonEnd, startBottomCount.get(exonEnd) + 1);


                    } else {

                        if (endTopCount.get(exonStart) + 1 > maxTopGlobal) {
                            maxTopGlobal = endTopCount.get(exonStart) + 1;
                            boxHeight += textHeight;
                        }
                        if (startTopCount.get(exonStart) + 1 > maxTopGlobal) {
                            maxTopGlobal = startTopCount.get(exonStart) + 1;
                            boxHeight += textHeight;

                        }


                        endTopCount.replace(exonStart, endTopCount.get(exonStart) + 1);
                        startTopCount.replace(exonEnd, startTopCount.get(exonEnd) + 1);
                    }
                }
            }

        }


        startBottomCount = new HashMap<>();
        endBottomCount = new HashMap<>();
        startTopCount = new HashMap<>();
        endTopCount = new HashMap<>();
        boxHeight+=(maxBottomGlobal+maxTopGlobal)*textHeight;

        double plottop = boxHeight/2-coveragePane.getPrefHeight()/2;
        double plotbottom = plottop+coveragePane.getPrefHeight();

        coveragePane.setLayoutY(plottop);



        for(Map.Entry<Pair<Exon, Exon>, Integer> junction: file.getJunctions().entrySet()){

            if(junction.getValue()>minReads) {
                Exon exon1 = junction.getKey().getKey();
                Exon exon2 = junction.getKey().getValue();

                if (!startBottomCount.containsKey(exon1)) {
                    startBottomCount.put(exon1, 0);
                }
                if (!endBottomCount.containsKey(exon1)) {
                    endBottomCount.put(exon1, 0);
                }
                if (!startTopCount.containsKey(exon1)) {
                    startTopCount.put(exon1, 0);
                }
                if (!endTopCount.containsKey(exon1)) {
                    endTopCount.put(exon1, 0);
                }
                if (!startBottomCount.containsKey(exon2)) {
                    startBottomCount.put(exon2, 0);
                }
                if (!endBottomCount.containsKey(exon2)) {
                    endBottomCount.put(exon2, 0);
                }
                if (!startTopCount.containsKey(exon2)) {
                    startTopCount.put(exon2, 0);
                }
                if (!endTopCount.containsKey(exon2)) {
                    endTopCount.put(exon2, 0);
                }

                Exon exonStart;
                Exon exonEnd;
                if (exon1.getStart() < exon2.getStart()) {
                    exonStart = exon1;
                    exonEnd = exon2;
                } else {
                    exonStart = exon2;
                    exonEnd = exon1;
                }

                //            int maxTop = Math.max(Collections.max(startTopCount.values()), Collections.max(endTopCount.values()));
                //            int maxBottom = Math.max(Collections.max(startBottomCount.values()), Collections.max(endBottomCount.values()));


                if (exonStart.getEnd() != exonEnd.getEnd() && exonStart.getStart() != exonEnd.getStart()) {


                    int indexStart1 = exonStart.getStart() - geneViewerMinimumCoordinate;
                    int indexEnd1 = exonStart.getEnd() - geneViewerMinimumCoordinate;
                    int maxExon1Depth = Arrays.stream(Arrays.copyOfRange(file.getDepth(), indexStart1, indexEnd1)).summaryStatistics().getMax();

                    int indexStart2 = exonEnd.getStart() - geneViewerMinimumCoordinate;
                    int indexEnd2 = exonEnd.getEnd() - geneViewerMinimumCoordinate;
                    int maxExon2Depth = Arrays.stream(Arrays.copyOfRange(file.getDepth(), indexStart2, indexEnd2)).summaryStatistics().getMax();

                    int maxTop = Math.max(endTopCount.get(exonStart), startTopCount.get(exonEnd));
                    int maxBottom = Math.max(endBottomCount.get(exonStart), startBottomCount.get(exonEnd));

                    double exonStartPos = representationWidthFinal * (((double) (exonStart.getStart() + exonStart.getEnd()) / 2 -
                            start)) / (end - start);
                    double exonEndPos = representationWidthFinal * (((double) (exonEnd.getStart() + exonEnd.getEnd()) / 2 -
                            start)) / (end - start);

                    double exon2Height = coveragePane.getPrefHeight() * (double) maxExon2Depth / max;
                    double exon1Height = coveragePane.getPrefHeight() * (double) maxExon1Depth / max;
                    double maxHeight = Math.max(exon1Height, exon2Height);

                    Line l1;
                    Line l2;
                    Line l3;
                    Text readCount = new Text(String.valueOf(junction.getValue()));
                    readCount.setFont(Font.font(14));
                    readCount.setX((exonStartPos + exonEndPos) / 2);


                    readCount.toFront();

                    if (maxTop > maxBottom) {

                        if (endBottomCount.get(exonStart) + 1 > maxBottomGlobal) {
                            maxBottomGlobal = endBottomCount.get(exonStart) + 1;
                            boxHeight += textHeight;
                        }
                        if (startBottomCount.get(exonStart) + 1 > maxBottomGlobal) {
                            maxBottomGlobal = startBottomCount.get(exonStart) + 1;
                            boxHeight += textHeight;

                        }

                        endBottomCount.replace(exonStart, endBottomCount.get(exonStart) + 1);
                        startBottomCount.replace(exonEnd, startBottomCount.get(exonEnd) + 1);


                        l1 = new Line(exonStartPos, plotbottom, exonStartPos, plotbottom + textHeight * (maxBottom + 1));
                        l2 = new Line(exonEndPos, plotbottom, exonEndPos, plotbottom + textHeight * (maxBottom + 1));
                        l3 = new Line(exonStartPos, plotbottom + textHeight * (maxBottom + 1), exonEndPos, plotbottom + textHeight * (maxBottom + 1));

                        readCount.setY(plotbottom + textHeight * (maxBottom + 1) - 3);

                    } else {

                        if (endTopCount.get(exonStart) + 1 > maxTopGlobal) {
                            maxTopGlobal = endTopCount.get(exonStart) + 1;
                            boxHeight += textHeight;
                        }
                        if (startTopCount.get(exonStart) + 1 > maxTopGlobal) {
                            maxTopGlobal = startTopCount.get(exonStart) + 1;
                            boxHeight += textHeight;

                        }


                        endTopCount.replace(exonStart, endTopCount.get(exonStart) + 1);
                        startTopCount.replace(exonEnd, startTopCount.get(exonEnd) + 1);

                        l1 = new Line(exonStartPos, plotbottom - exon1Height, exonStartPos, plotbottom - maxHeight - textHeight * (maxTop + 1));
                        l2 = new Line(exonEndPos, plotbottom - exon2Height, exonEndPos, plotbottom - maxHeight - textHeight * (maxTop + 1));
                        l3 = new Line(exonStartPos, plotbottom - maxHeight - textHeight * (maxTop + 1), exonEndPos, plotbottom - maxHeight - textHeight * (maxTop + 1));
                        readCount.setY(plotbottom - maxHeight - textHeight * (maxTop + 1) - 3);
                    }

                    l1.setStrokeWidth(2);
                    l2.setStrokeWidth(2);
                    l3.setStrokeWidth(2);
                    l1.setFill(Color.rgb(0,0,0,0.6));
                    l2.setFill(Color.rgb(0,0,0,0.6));
                    l3.setFill(Color.rgb(0,0,0,0.6));

                    EventHandler inEvent = event -> {
                        l1.setStroke(Color.RED);
                        l2.setStroke(Color.RED);
                        l3.setStroke(Color.RED);
                        readCount.setFill(Color.RED);
                        l1.toFront();
                        l2.toFront();
                        l3.toFront();
                        l1.setStrokeWidth(5);
                        l2.setStrokeWidth(5);
                        l3.setStrokeWidth(5);
                        hightlightJunctionInOtherFiles(junction.getKey(), true);
                        alternativeJunctions(exonStart, exonEnd, true);
                    };

                    EventHandler outEvent = event -> {
                        l1.setStroke(Color.BLACK);
                        l2.setStroke(Color.BLACK);
                        l3.setStroke(Color.BLACK);
                        readCount.setFill(Color.BLACK);
                        readCount.toFront();
                        l1.setStrokeWidth(2);
                        l2.setStrokeWidth(2);
                        l3.setStrokeWidth(2);
                        hightlightJunctionInOtherFiles(junction.getKey(), false);
                        alternativeJunctions(exonStart, exonEnd, false);
                    };

                    l1.setOnMouseEntered(inEvent);
                    l2.setOnMouseEntered(inEvent);
                    l3.setOnMouseEntered(inEvent);
                    readCount.setOnMouseEntered(inEvent);

                    l1.setOnMouseExited(outEvent);
                    l2.setOnMouseExited(outEvent);
                    l3.setOnMouseExited(outEvent);
                    readCount.setOnMouseExited(outEvent);


                    Group group = new Group();
                    junctionsGraphicGroup.put(junction.getKey(), group);

                    group.getChildren().add(l1);
                    group.getChildren().add(l2);
                    group.getChildren().add(l3);
                    group.getChildren().add(readCount);
                    Platform.runLater(()-> sashimiPane.getChildren().add(group));


                }
            }

        }


        container.setPrefHeight(boxHeight-textHeight);

        file.setJunctionsGraphicGroup(junctionsGraphicGroup);
        long estimatedTime = System.currentTimeMillis() - startTime;
        //System.out.println("sashimi: "+estimatedTime);
    }

    public void setFontSize(double fontSize){
        this.fontSize = fontSize;
    }

    public void setDisplayedTranscripts(ArrayList<Transcript> transcripts){
        this.displayedTranscripts = transcripts;
    }

    private void alternativeJunctions(Exon exonStart, Exon exonEnd, boolean turnOn){
        for(BamFile file: selectedFiles){
            for(Map.Entry<Pair<Exon, Exon>, Group> entry: file.getJunctionsGraphicGroup().entrySet()){
                Pair<Exon, Exon> junction = entry.getKey();

                Exon otherExonStart;
                Exon otherExonEnd;
                if(junction.getKey().getStart()<junction.getValue().getStart()){
                    otherExonStart = junction.getKey();
                    otherExonEnd = junction.getValue();
                }else {
                    otherExonStart = junction.getValue();
                    otherExonEnd = junction.getKey();
                }

                if((!otherExonStart.equals(exonStart) && otherExonEnd.equals(exonEnd)) ||
                        (otherExonStart.equals(exonStart)) && !otherExonEnd.equals(exonEnd)){
                    Group graphicGroup = entry.getValue();
                    if(graphicGroup!=null){

                        Line l1;
                        Line l2 = (Line) graphicGroup.getChildren().get(2);
                        Line l3 = (Line) graphicGroup.getChildren().get(1);
                        Text readCount;

                        if(graphicGroup.getChildren().get(0).getClass()==Line.class){
                            l1 = (Line) graphicGroup.getChildren().get(0);
                            readCount = (Text) graphicGroup.getChildren().get(3);
                        }else{
                            l1 = (Line) graphicGroup.getChildren().get(3);
                            readCount = (Text) graphicGroup.getChildren().get(0);
                        }




                        if(turnOn){
                            l1.setStroke(Color.GREEN);
                            l2.setStroke(Color.GREEN);
                            l3.setStroke(Color.GREEN);
                            readCount.setFill(Color.GREEN);
                            l1.toFront();
                            l2.toFront();
                            l3.toFront();
                            l1.setStrokeWidth(5);
                            l2.setStrokeWidth(5);
                            l3.setStrokeWidth(5);
                        }else{
                            l1.setStroke(Color.BLACK);
                            l2.setStroke(Color.BLACK);
                            l3.setStroke(Color.BLACK);
                            readCount.setFill(Color.BLACK);
                            readCount.toFront();
                            l1.setStrokeWidth(2);
                            l2.setStrokeWidth(2);
                            l3.setStrokeWidth(2);
                        }
                    }
                }


            }
        }

    }

    private void hightlightJunctionInOtherFiles(Pair<Exon, Exon> junction, boolean turnOn){
        for(BamFile file: selectedFiles) {

            if (file.isSelected()) {

                Group graphicGroup = file.getGroupForJunction(junction);
                if (graphicGroup != null) {

                    Line l1;
                    Line l2 = (Line) graphicGroup.getChildren().get(2);
                    Line l3 = (Line) graphicGroup.getChildren().get(1);
                    Text readCount;

                    if (graphicGroup.getChildren().get(0).getClass() == Line.class) {
                        l1 = (Line) graphicGroup.getChildren().get(0);
                        readCount = (Text) graphicGroup.getChildren().get(3);
                    } else {
                        l1 = (Line) graphicGroup.getChildren().get(3);
                        readCount = (Text) graphicGroup.getChildren().get(0);
                    }


                    if (turnOn) {
                        l1.setStroke(Color.RED);
                        l2.setStroke(Color.RED);
                        l3.setStroke(Color.RED);
                        readCount.setFill(Color.RED);
                        l1.toFront();
                        l2.toFront();
                        l3.toFront();
                        l1.setStrokeWidth(5);
                        l2.setStrokeWidth(5);
                        l3.setStrokeWidth(5);
                    } else {
                        l1.setStroke(Color.BLACK);
                        l2.setStroke(Color.BLACK);
                        l3.setStroke(Color.BLACK);
                        readCount.setFill(Color.BLACK);
                        readCount.toFront();
                        l1.setStrokeWidth(2);
                        l2.setStrokeWidth(2);
                        l3.setStrokeWidth(2);
                    }
                }
            }
        }
    }

    public void resize(double representationWidthFinal){
        this.representationWidthFinal = representationWidthFinal;
        if(chr!=null){
            drawDepth(currentStart, currentEnd);
        }
    }

}
