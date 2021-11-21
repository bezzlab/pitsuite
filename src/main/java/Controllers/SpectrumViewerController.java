package Controllers;

import Cds.PSM;
import TablesModels.PeptideSampleModel;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXSlider;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import javafx.util.Pair;
import org.apache.commons.io.IOUtils;
import Singletons.Config;
import utilities.MSRun;
import utilities.spectrumviewer.FragmentRow;
import utilities.spectrumviewer.Ion;
import utilities.spectrumviewer.Peptide;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpectrumViewerController implements Initializable {

    @FXML
    public JFXCheckBox a1;
    @FXML
    public JFXCheckBox a2;
    @FXML
    public JFXCheckBox a3;
    @FXML
    public JFXCheckBox b1;
    @FXML
    public JFXCheckBox b2;
    @FXML
    public JFXCheckBox b3;
    @FXML
    public JFXCheckBox c1;
    @FXML
    public JFXCheckBox c2;
    @FXML
    public JFXCheckBox c3;
    @FXML
    public JFXCheckBox x1;
    @FXML
    public JFXCheckBox x2;
    @FXML
    public JFXCheckBox x3;
    @FXML
    public JFXCheckBox y1;
    @FXML
    public JFXCheckBox y2;
    @FXML
    public JFXCheckBox y3;
    @FXML
    public JFXCheckBox z1;
    @FXML
    public JFXCheckBox z2;
    @FXML
    public JFXCheckBox z3;

    @FXML
    public JFXCheckBox nh3Checkbox;
    @FXML
    public JFXCheckBox h2oCheckbox;
    @FXML
    public JFXCheckBox h3po4Checkbox;

    @FXML
    public TableView ionTable;
    @FXML
    private JFXSlider toleranceSlider;
    @FXML
    private Pane spectrumPane;

    private Pane zoomContainer;
    private Peptide peptide;


    private Config config;
    private HashMap<String, HashMap<Long, Long>> mzmlIndexes;
    private WebView specWebview;
    private double[] mzs;
    private double[] intensities;
    private ArrayList<Pair<Double, Double>> peaks;
    private HashMap<String, HashMap<Integer, ArrayList<Ion>>> ionSeries;
    private String sequence;
    double massError = 20;
    private boolean isSelectedZoomRegion;
    private double selectingRegionStart;
    private double maxZoom=Double.POSITIVE_INFINITY;
    private double minZoom=0;
    private double minMz;
    private double maxMz;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
            toleranceSlider.setMin(0);
            toleranceSlider.setMax(50);
        toleranceSlider.setOnMouseReleased(event -> {
            massError = toleranceSlider.getValue();
            init(mzs, intensities, sequence);
        });

        zoomContainer = new Pane();
        zoomContainer.prefWidthProperty().bind(spectrumPane.widthProperty());
        zoomContainer.prefHeightProperty().bind(spectrumPane.heightProperty());
        spectrumPane.getChildren().add(zoomContainer);
        isSelectedZoomRegion = false;

        spectrumPane.setOnMouseMoved(mouseEvent -> {
            double xValue = mouseEvent.getX();


            if(isSelectedZoomRegion){
                zoomContainer.getChildren().clear();

                double start, end;
                if(xValue<selectingRegionStart){
                    start = xValue;
                    end = selectingRegionStart;
                }else{
                    start = selectingRegionStart;
                    end = xValue;
                }
                Rectangle rec = new Rectangle();
                rec.setWidth(end-start);
                rec.setHeight(spectrumPane.getHeight());
                rec.setX(start);
                rec.setY(0);
                rec.setFill(new Color(0.418, 0.7344, 0.7773, 0.4));
                zoomContainer.getChildren().add(rec);
                zoomContainer.toFront();
            }
        });


        spectrumPane.addEventFilter(MouseEvent.MOUSE_CLICKED, mouseEvent -> {

            if(mouseEvent.getTarget().getClass()!=Rectangle.class && mouseEvent.getTarget().getClass()!=Text.class){

                if(isSelectedZoomRegion){

                    double start, end;
                    if(mouseEvent.getX()<selectingRegionStart){
                        start = mouseEvent.getX();
                        end = selectingRegionStart;
                    }else{
                        start = selectingRegionStart;
                        end = mouseEvent.getX();
                    }

                    minZoom = minMz + (start/spectrumPane.getWidth()) * (maxMz - minMz);
                    maxZoom = minMz + (end/spectrumPane.getWidth()) * (maxMz - minMz);
                    zoomContainer.getChildren().clear();

                    init(mzs, intensities, sequence);

                }else{
                    selectingRegionStart = mouseEvent.getX();
                }
                isSelectedZoomRegion = !isSelectedZoomRegion;
            }


        });

        ChangeListener changeListener = (ChangeListener<Boolean>) (observable, oldValue, newValue) -> init(mzs, intensities, sequence);

        a1.selectedProperty().addListener(changeListener);
        a2.selectedProperty().addListener(changeListener);
        a3.selectedProperty().addListener(changeListener);
        b1.selectedProperty().addListener(changeListener);
        b2.selectedProperty().addListener(changeListener);
        b3.selectedProperty().addListener(changeListener);
        c1.selectedProperty().addListener(changeListener);
        c2.selectedProperty().addListener(changeListener);
        c3.selectedProperty().addListener(changeListener);
        x1.selectedProperty().addListener(changeListener);
        x2.selectedProperty().addListener(changeListener);
        x3.selectedProperty().addListener(changeListener);
        y1.selectedProperty().addListener(changeListener);
        y2.selectedProperty().addListener(changeListener);
        y3.selectedProperty().addListener(changeListener);
        z1.selectedProperty().addListener(changeListener);
        z2.selectedProperty().addListener(changeListener);
        z3.selectedProperty().addListener(changeListener);

        ChangeListener neutralLossListener = (observable, oldValue, newValue) -> {
            ArrayList<Peptide.NeutralLoss> losses = getNeutralLosses(peptide);
            peptide.recalculateLossOptions(losses, 1);
            drawGraph(getDatasets(sequence, peptide,  peaks), mzs, intensities);
        };

        h2oCheckbox.selectedProperty().addListener(neutralLossListener);
        nh3Checkbox.selectedProperty().addListener(neutralLossListener);
        h3po4Checkbox.selectedProperty().addListener(neutralLossListener);


    }


    public void setConfig(Config config){
        this.config = config;
    }

    public void getMzmlIndex(String run){

        mzmlIndexes = new HashMap<>();
        for(String sample: config.getRunSamples(run)){
            //String filename = config.getOutputPath()+"/ms/"+run+"/"+sample+"/files/mzml.index";
            String filename = config.getOutputPath()+"/ms/"+run+"/"+"/files/mzml.index";

                HashMap<Long, Long> sampleHashMap = new HashMap<>();
            try {
                Files.lines(Path.of(filename)).forEach(line -> {
                    String[] lineSplit = line.split(",");
                    sampleHashMap.put(Long.parseLong(lineSplit[0]), Long.parseLong(lineSplit[1]));
                });

                mzmlIndexes.put(sample, sampleHashMap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @FXML
    public void resetZoom(){
        init(mzs, intensities, sequence);
    }

    public void runLorikeet(double[] mz, double[] intensities, String peptide){

        WebEngine webEngine = specWebview.getEngine();
        webEngine.reload();

        specWebview.getEngine().load(getClass().getResource("/Lorikeet/html/example_use2.html").toString());

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {


                String command = "var ms2peaks =[";
                for (int i = 0; i < mz.length - 1; i++) {
                    command += "[" + mz[i] + "," + intensities[i] + "],";
                }

                command += "[" + mz[mz.length - 1] + "," + intensities[mz.length - 1] + "]]";
                webEngine.executeScript(command);
                command="";
                command += "$(\"#lorikeet\").specview({\"sequence\":\"" + peptide + "\",\n" +
                        "\t\t\t\t\t\t\t\t\"peaks\": ms2peaks});\t";



                webEngine.executeScript(command);
            }


        });
        webEngine.load(getClass().getResource("/Lorikeet/html/test.html").toExternalForm());

    }


    public void select(PSM psm, MSRun run, String peptide){
        parseBinaries(psm, run, peptide);
    }

    private void parseBinaries(PSM psm, MSRun run, String peptide){

        String filepath = Config.getRunPath(run.getName())+"/"+psm.getFile()+".mzML";

        HashMap<Integer, Long> index = run.getIndex(psm.getFile());



        try {

            long positionToRead = index.get(psm.getSpecIndex());
            int amountBytesToRead = (int) (index.get(psm.getSpecIndex()+1)-positionToRead);
            FileInputStream fis = null;
            RandomAccessFile f = new RandomAccessFile(new File(filepath),"r");
            //f.seek(2643);
            byte[] b = new byte[amountBytesToRead];
            f.seek(positionToRead);

            f.read(b);
            String str = new String(b);

            Pattern p = Pattern.compile("<binary>(.*)</binary>");
            Matcher m = p.matcher(str);

            final List<String> matches = new ArrayList<>();
            while (m.find()){
                matches.add(m.group(1));
            }

            ProcessBuilder processBuilder = new ProcessBuilder("python", "convertBinaryPeaks.py", matches.get(0), matches.get(1));
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            String result = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
            String[] data = result.split("]");
            String[] mzStr = data[0].substring(1).split(",");
            double[] mz = new double[mzStr.length];
            for (int i = 0; i < mzStr.length; i++) {
                mz[i] = Double.parseDouble(mzStr[i]);
            }

            String[] mzIntensity = data[1].substring(3).split(",");
            double[] intensity = new double[mzIntensity.length];
            for (int i = 0; i < mzIntensity.length; i++) {
                intensity[i] = Double.parseDouble(mzIntensity[i]);
            }

            mzs=mz;
            intensities=intensity;
            sequence=peptide;

            init(mz, intensity, peptide);


        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    private void drawGraph(ArrayList<HashMap<String, Object>> data, double[] mzs, double[] intensities){

        spectrumPane.getChildren().clear();
        spectrumPane.getChildren().add(zoomContainer);



        Text labelForSize = new Text("y4+++");
        double textHeight = labelForSize.getLayoutBounds            ().getWidth();
        double textWidth = labelForSize.getLayoutBounds().getHeight();
        labelForSize.setFont(Font.font("monospace", 16));
        labelForSize.getTransforms().add(new Rotate(90));
        double totalWidth = spectrumPane.getWidth()-10;
        double totalHeight = spectrumPane.getHeight()-10-labelForSize.getLayoutBounds().getWidth();

        double maxntensity = 0;
        for (double intensity : intensities) {
            if (intensity > maxntensity)
                maxntensity = intensity;
        }

        double mzRange = 0;
        minMz=Float.POSITIVE_INFINITY;
        maxMz=0;
        for (double mz  : mzs) {
            if (mz < minMz)
                minMz = mz;
            if (mz > maxMz)
                maxMz = mz;
        }



        if(minZoom>minMz){
            minMz = minZoom;
        }

        if(maxZoom<maxMz){
            maxMz = maxZoom;
        }

        mzRange = maxMz - minMz;

        int i = 0;
        for(HashMap<String, Object> series: data){
            Line line;
            String color = (String) series.get("color");
            if(i==0){
                ArrayList<Pair<Double, Double>> peaks = (ArrayList<Pair<Double, Double>>) series.get("data");

                for(Pair<Double, Double> peak: peaks){

                    if(peak.getKey()>=minMz && peak.getKey()<=maxMz){
                        line = new Line(totalWidth*(peak.getKey()-minMz)/mzRange, totalHeight+textHeight,
                                totalWidth*(peak.getKey()-minMz)/mzRange, totalHeight-totalHeight*peak.getValue()/maxntensity+textHeight);
                        line.setStroke(Color.web(color));

                        spectrumPane.getChildren().add(line);
                    }

                }
            }else{
                ArrayList<Double[]> peaks = (ArrayList<Double[]>) series.get("data");
                ArrayList<String> labels = (ArrayList<String>) series.get("label");
                for (int j = 0; j < peaks.size(); j++) {

                    if(peaks.get(j)[0]>=minMz && peaks.get(j)[0]<=maxMz) {

                        Text label = new Text(labels.get(j));

                        label.setFont(Font.font("monospace", 16));
                        label.getTransforms().add(new Rotate(-90));
                        label.setLayoutX(totalWidth * (peaks.get(j)[0] - minMz) / mzRange - textWidth / 4);
                        label.setLayoutY(totalHeight - totalHeight * peaks.get(j)[1] / maxntensity + textHeight);


                        line = new Line(totalWidth * (peaks.get(j)[0] - minMz) / mzRange, totalHeight + textHeight,
                                totalWidth * (peaks.get(j)[0] - minMz) / mzRange, totalHeight - totalHeight * peaks.get(j)[1] / maxntensity + textHeight);
                        line.setStroke(Color.web(color));

                        spectrumPane.getChildren().add(line);
                        spectrumPane.getChildren().add(label);
                    }
                }
            }
            i++;
        }

        minZoom = 0;
        maxZoom=Double.POSITIVE_INFINITY;






    }

    private void init(double[] mzs, double[] intensities, String sequence){

        peaks = new ArrayList<>();
        for (int i = 0; i < mzs.length; i++) {
            peaks.add(new Pair<>(mzs[i], intensities[i]));
        }

        peptide = new Peptide(sequence, new ArrayList<>(), new ArrayList<>(),
                0., 0., 1);

        ArrayList<HashMap<String, Object>> data = getDatasets(sequence, peptide, peaks);
        drawGraph(data, mzs, intensities);
        makeIonTable();
        //runLorikeet(mzs, intensities, sequence);
    }


    private ArrayList<HashMap<String, Object>> getDatasets(String sequence, Peptide peptide, ArrayList<Pair<Double, Double>> peaks){

         ionSeries = new HashMap<>();
        ionSeries.put("a", new HashMap<>());
        ionSeries.put("b", new HashMap<>());
        ionSeries.put("c", new HashMap<>());
        ionSeries.put("x", new HashMap<>());
        ionSeries.put("y", new HashMap<>());
        ionSeries.put("z", new HashMap<>());

        HashMap<String, HashMap<Integer, ArrayList<Double[]>>> ionSeriesMatch = new HashMap<>();
        ionSeriesMatch.put("a", new HashMap<>());
        ionSeriesMatch.put("b", new HashMap<>());
        ionSeriesMatch.put("c", new HashMap<>());
        ionSeriesMatch.put("x", new HashMap<>());
        ionSeriesMatch.put("y", new HashMap<>());
        ionSeriesMatch.put("z", new HashMap<>());

        HashMap<String, HashMap<Integer, ArrayList<String>>> ionSeriesLabels = new HashMap<>();
        ionSeriesLabels.put("a", new HashMap<>());
        ionSeriesLabels.put("b", new HashMap<>());
        ionSeriesLabels.put("c", new HashMap<>());
        ionSeriesLabels.put("x", new HashMap<>());
        ionSeriesLabels.put("y", new HashMap<>());
        ionSeriesLabels.put("z", new HashMap<>());

        HashMap<String, HashMap<Integer, Double>> ionSeriesAntic = new HashMap<>();
        ionSeriesAntic.put("a", new HashMap<>());
        ionSeriesAntic.put("b", new HashMap<>());
        ionSeriesAntic.put("c", new HashMap<>());
        ionSeriesAntic.put("x", new HashMap<>());
        ionSeriesAntic.put("y", new HashMap<>());
        ionSeriesAntic.put("z", new HashMap<>());

        ArrayList<Ion> selectedIons = getSelectedIonTypes();
        calculateTheoreticalSeries(selectedIons, ionSeries, sequence, peptide);


        ArrayList<HashMap<String, Object>> data = new ArrayList<>();
        HashMap<String, Object> baseSeries = new HashMap<>();
        baseSeries.put("data", peaks);
        baseSeries.put("color", "#bbbbbb");
        baseSeries.put("labelType", "none");
        baseSeries.put("ionSeries", ionSeries);
        data.add(baseSeries);

        ArrayList<HashMap<String, Object>> seriesMatches = getSeriesMatches(selectedIons, ionSeries, ionSeriesMatch, ionSeriesLabels,
                ionSeriesAntic, peaks, peptide);
        data.addAll(seriesMatches);
        return data;
    }

    private ArrayList<Ion>  getSelectedIonTypes(){
        ArrayList<Ion> selectedIons = new ArrayList<>();

        if(a1.isSelected()){
            selectedIons.add(new Ion("a", "008000", 1, "n"));
        }
        if(a2.isSelected()){
            selectedIons.add(new Ion("a", "2E8B57", 2, "n"));
        }
        if(a3.isSelected()){
            selectedIons.add(new Ion("a", "9ACD32", 3, "n"));
        }
        if(b1.isSelected()){
            selectedIons.add(new Ion("b", "0000ff", 1, "n"));
        }
        if(b2.isSelected()){
            selectedIons.add(new Ion("b", "4169E1", 2, "n"));
        }
        if(b3.isSelected()){
            selectedIons.add(new Ion("b", "00BFFF", 3, "n"));
        }
        if(c1.isSelected()){
            selectedIons.add(new Ion("c", "008B8B", 1, "n"));
        }
        if(c2.isSelected()){
            selectedIons.add(new Ion("c", "20B2AA", 2, "n"));
        }
        if(c3.isSelected()){
            selectedIons.add(new Ion("c", "66CDAA", 3, "n"));
        }
        if(x1.isSelected()){
            selectedIons.add(new Ion("x", "4B0082", 1, "c"));
        }
        if(x2.isSelected()){
            selectedIons.add(new Ion("x", "800080", 2, "c"));
        }
        if(x3.isSelected()){
            selectedIons.add(new Ion("x", "9932CC", 3, "c"));
        }
        if(y1.isSelected()){
            selectedIons.add(new Ion("y", "ff0000", 1, "c"));
        }
        if(y2.isSelected()){
            selectedIons.add(new Ion("y", "FA8072", 2, "c"));
        }
        if(y3.isSelected()){
            selectedIons.add(new Ion("y", "FFA07A", 3, "c"));
        }
        if(z1.isSelected()){
            selectedIons.add(new Ion("z", "FF8C00", 1, "c"));
        }
        if(z2.isSelected()){
            selectedIons.add(new Ion("z", "FFA500", 2, "c"));
        }
        if(z3.isSelected()){
            selectedIons.add(new Ion("z", "FFD700", 3, "c"));
        }


        return selectedIons;
    }

    public void calculateTheoreticalSeries(ArrayList<Ion> selectedIons,
                                           HashMap<String, HashMap<Integer, ArrayList<Ion>>> ionSeries,
                                           String sequence, Peptide peptide){
        ArrayList<Ion> todoIonSeries = new ArrayList<>();
        ArrayList<ArrayList<Ion>> todoIonSeriesData = new ArrayList<>();
        for(Ion sion: selectedIons){
            switch (sion.getType()) {
                case "a":
                    if (ionSeries.get("a").containsKey(sion.getCharge()))
                        continue;
                    todoIonSeries.add(sion);
                    ionSeries.get("a").put(sion.getCharge(), new ArrayList<>());
                    todoIonSeriesData.add(ionSeries.get("a").get(sion.getCharge()));

                    break;
                case "b":
                    if (ionSeries.get("b").containsKey(sion.getCharge()))
                        continue;
                    todoIonSeries.add(sion);
                    ionSeries.get("b").put(sion.getCharge(), new ArrayList<>());
                    todoIonSeriesData.add(ionSeries.get("b").get(sion.getCharge()));

                    break;
                case "c":
                    if (ionSeries.get("c").containsKey(sion.getCharge()))
                        continue;
                    todoIonSeries.add(sion);
                    ionSeries.get("c").put(sion.getCharge(), new ArrayList<>());
                    todoIonSeriesData.add(ionSeries.get("c").get(sion.getCharge()));

                    break;
                case "x":
                    if (ionSeries.get("x").containsKey(sion.getCharge()))
                        continue;
                    todoIonSeries.add(sion);
                    ionSeries.get("x").put(sion.getCharge(), new ArrayList<>());
                    todoIonSeriesData.add(ionSeries.get("x").get(sion.getCharge()));

                    break;
                case "y":
                    if (ionSeries.get("y").containsKey(sion.getCharge()))
                        continue;
                    todoIonSeries.add(sion);
                    ionSeries.get("y").put(sion.getCharge(), new ArrayList<>());
                    todoIonSeriesData.add(ionSeries.get("y").get(sion.getCharge()));

                    break;
                case "z":
                    if (ionSeries.get("z").containsKey(sion.getCharge()))
                        continue;
                    todoIonSeries.add(sion);
                    ionSeries.get("z").put(sion.getCharge(), new ArrayList<>());
                    todoIonSeriesData.add(ionSeries.get("z").get(sion.getCharge()));

                    break;
            }

            String massType = "mono";
            for (int i = 1; i < sequence.length(); i++) {
                for (int j = 0; j < todoIonSeries.size(); j++) {
                    Ion tion = todoIonSeries.get(j);
                    ArrayList<Ion> ionSeriesData = todoIonSeriesData.get(j);
                    Ion ion = Ion.getSeriesIon(tion, peptide, i, massType);
                    if(ion.getTerm().equals("n")){
                        ionSeriesData.add(ion);
                    }else if(ion.getTerm().equals("c")){
                        ionSeriesData.add(0, ion);
                    }
                }
            }
        }
    }

    public ArrayList<HashMap<String, Object>> getSeriesMatches(ArrayList<Ion> selectedIonTypes, HashMap<String,
            HashMap<Integer, ArrayList<Ion>>> ionSeries,
                                 HashMap<String, HashMap<Integer, ArrayList<Double[]>>>  ionSeriesMatch,
                                 HashMap<String, HashMap<Integer, ArrayList<String>>>  ionSeriesLabels,
                                 HashMap<String, HashMap<Integer, Double>>  ionSeriesAntic,
                                 ArrayList<Pair<Double, Double>> peaks, Peptide peptide){

        ArrayList<HashMap<String, Object>> dataSeries = new ArrayList<>();
        String massType = "mono";
        String peakAssignmentType = "intense";
        String peakLabelType = "ion";
        String massErrorUnit="Th";
        for(Ion ion: selectedIonTypes){

            if(!ionSeriesMatch.get(ion.getType()).containsKey(ion.getCharge())){
                ArrayList<Object> adata = calculateMatchingPeaks(peptide, ionSeries.get(ion.getType()).get(ion.getCharge()),
                        peaks, massError, massErrorUnit, peakAssignmentType, massType);
                if(adata!=null && adata.size() > 0){
                    ionSeriesMatch.get(ion.getType()).put(ion.getCharge(), (ArrayList<Double[]>) adata.get(0));
                    ionSeriesLabels.get(ion.getType()).put(ion.getCharge(), (ArrayList<String>) adata.get(1));
                    ionSeriesAntic.get(ion.getType()).put(ion.getCharge(), (Double) adata.get(2));

                    HashMap<String, Object> series = new HashMap<>();
                    series.put("data", ionSeriesMatch.get(ion.getType()).get(ion.getCharge()));
                    series.put("color", ion.getColor());
                    series.put("labelType", peakLabelType);
                    series.put("label", ionSeriesLabels.get(ion.getType()).get(ion.getCharge()));
                    dataSeries.add(series);

                }
            }

        }
        return dataSeries;
    }

    public ArrayList<Object> calculateMatchingPeaks(Peptide peptide, ArrayList<Ion> ionSeries, ArrayList<Pair<Double, Double>> allPeaks, double massTolerance,
                                                    String massErrorUnit, String peakAssignmentType, String massType){
        int peakIndex = 0;

        ArrayList<Object> matchData = new ArrayList<>();
        matchData.add(new ArrayList<Double[]>());
        matchData.add(new ArrayList<String>());
        matchData.add(0.);

        for(Ion sion: ionSeries){
            int minIndex = 2147483647;
            ArrayList<Peptide.LossCombinationList> neutralLossOptions = peptide.getPotentialLosses(sion);
            int index = getMatchForIon(sion, matchData, allPeaks, peakIndex, massTolerance, massErrorUnit, peakAssignmentType,
                    null, massType);
            minIndex = Math.min(minIndex, index);
            for (int n = 1; n < neutralLossOptions.size(); n++) {
                Peptide.LossCombinationList loss_options_with_n_losses = neutralLossOptions.get(n);
                for (int k = 0; k < loss_options_with_n_losses.lossCombinationCount(); k++) {
                    Peptide.LossCombination lossCombination = loss_options_with_n_losses.getLossCombination(k);
                    index = getMatchForIon(sion, matchData, allPeaks, peakIndex, massTolerance, massErrorUnit, peakAssignmentType,
                            lossCombination, massType);
                    minIndex = Math.min(minIndex, index);

                }
            }
            peakIndex = minIndex;
        }
        return matchData;
    }

    public int getMatchForIon(Ion sion, ArrayList<Object> matchData, ArrayList<Pair<Double, Double>> allPeaks, int peakIndex, double massTolerance,
                               String massErrorUnit, String peakAssignmentType, Peptide.LossCombination neutralLosses, String massType){
        if(neutralLosses==null){
            sion.setMatch(false);
        }

        double ionmz = ionMz(sion, neutralLosses, massType);
        String peakLabel = getLabel(sion, neutralLosses);

        HashMap<String, Object> __ret = getMatchingPeak(peakIndex, allPeaks, ionmz, massTolerance, massErrorUnit, peakAssignmentType);
        peakIndex = (int) __ret.get("peakIndex");
        Pair<Double, Double> bestPeak = (Pair<Double, Double>) __ret.get("bestPeak");
        if(bestPeak!=null){
            ArrayList<Double[]> matchData0 = (ArrayList<Double[]>) matchData.get(0);
            matchData0.add(new Double[]{bestPeak.getKey(), bestPeak.getValue(), (double) __ret.get("theoreticalMz")});
            ArrayList<String> matchData1 = (ArrayList<String>) matchData.get(1);
            matchData1.add(peakLabel);
            matchData.set(2, (double) matchData.get(2)+bestPeak.getValue());
            if(neutralLosses==null) {
                sion.setMatch(true);
            }
        }


        return peakIndex;
    }

    public double ionMz(Ion sion, Peptide.LossCombination neutralLosses, String massType) {
        double ionmz;
        if(neutralLosses==null)
            ionmz = sion.getMz();
        else {
            ionmz = Ion.getIonMzWithLoss(sion, neutralLosses, massType);
        }
        return ionmz;
    }

    public String getLabel(Ion sion, Peptide.LossCombination neutralLosses) {
        var label = sion.getLabel();
        if(neutralLosses!=null) {
            label += neutralLosses.getLabel();
        }
        return label;
    }

    public HashMap<String, Object> getMatchingPeak(int peakIndex, ArrayList<Pair<Double, Double>> allPeaks, double ionmz,
                                                   double massTolerance, String toleranceUnit, String peakAssignmentType) {

        double bestDistance = Double.POSITIVE_INFINITY;
        Pair<Double, Double> bestPeak = null;
        var tolerantPeakMin = ionmz - massTolerance;
        var tolerantPeakMax = ionmz + massTolerance;

        for (var j = peakIndex; j < allPeaks.size(); j += 1) {

            Pair<Double, Double> peak = allPeaks.get(j);

            if(toleranceUnit.equals("ppm"))
            {
                var tolerance = (massTolerance * peak.getKey())/1000000;
                tolerantPeakMin = ionmz - tolerance;
                tolerantPeakMax = ionmz + tolerance;
            }
            // peak is before the current ion we are looking at
            if (peak.getKey() < tolerantPeakMin)
                continue;

            // peak is beyond the current ion we are looking at
            if (peak.getKey() > tolerantPeakMax) {
                peakIndex = j;
                break;
            }

            // peak is within +/- massTolerance of the current ion we are looking at

            // if this is the first peak in the range
            if (bestPeak==null) {
                //console.log("found a peak in range, "+peak.mz);
                bestPeak = peak;
                bestDistance = Math.abs(ionmz - peak.getKey());
                continue;
            }

            // if peak assignment method is Most Intense
            if (peakAssignmentType.equals("intense")) {
                if (peak.getValue()> bestPeak.getValue()) {
                    bestPeak = peak;
                    continue;
                }
            }

            // if peak assignment method is Closest Peak
            if (peakAssignmentType.equals("close")) {
                var dist = Math.abs(ionmz - peak.getKey());
                if (dist < bestDistance) {
                    bestPeak = peak;
                    bestDistance = dist;
                }
            }
        }

        HashMap<String, Object> results = new HashMap<>();
        results.put("peakIndex", peakIndex);
        results.put("bestPeak", bestPeak);
        results.put("theoreticalMz", ionmz);
        return results;
    }

    public ArrayList<Peptide.NeutralLoss> getNeutralLosses(Peptide peptide) {
        ArrayList<Peptide.NeutralLoss> neutralLosses = new ArrayList<>();

        if(h2oCheckbox.isSelected()){
            neutralLosses.add(peptide.getLossForLabel("o"));
        }
        if(nh3Checkbox.isSelected()){
            neutralLosses.add(peptide.getLossForLabel("*"));
        }
        if(h3po4Checkbox.isSelected()){
            neutralLosses.add(peptide.getLossForLabel("p"));
        }
        return neutralLosses;
    }

    public ArrayList<Ion> getSelectedNtermIons(ArrayList<Ion> selectedIonTypes) {
        ArrayList<Ion> ntermIons = new ArrayList<>();

        for (Ion sion : selectedIonTypes) {
            if (sion.getType().equals("a") || sion.getType().equals("b") || sion.getType().equals("c"))
                ntermIons.add(sion);
        }
        ntermIons.sort((i1, i2) -> {
            if (i1.getType().equals(i2.getType()))
                return (i1.getCharge() - i2.getCharge());
            return i1.getType().compareTo(i2.getType());
        });
        return ntermIons;
    }

    public ArrayList<Ion> getSelectedCtermIons(ArrayList<Ion> selectedIonTypes) {
        ArrayList<Ion> ctermIons = new ArrayList<>();

        for (Ion sion : selectedIonTypes) {
            if (sion.getType().equals("x") || sion.getType().equals("y") || sion.getType().equals("z"))
                ctermIons.add(sion);
        }
        ctermIons.sort((i1, i2) -> {
            if (i1.getType().equals(i2.getType()))
                return (i1.getCharge() - i2.getCharge());
            return i1.getType().compareTo(i2.getType());
        });

        return ctermIons;
    }

    public void makeIonTable() {

        ionTable.getItems().clear();
        ionTable.getColumns().clear();
        // selected ions
        ArrayList<Ion> selectedIonTypes = getSelectedIonTypes();
        ArrayList<Ion> ntermIons = getSelectedNtermIons(selectedIonTypes);
        ArrayList<Ion> ctermIons = getSelectedCtermIons(selectedIonTypes);

        NumberFormat df = DecimalFormat.getInstance();
        df.setMinimumFractionDigits(2);

        ArrayList<ArrayList< String>> colors = new ArrayList<>();

        for(Ion ion: ntermIons){
            TableColumn<FragmentRow, Double> col = new TableColumn<>(ion.getType()+ion.getCharge()+"+");
            col.setCellValueFactory(param -> new SimpleDoubleProperty(param.getValue().getMass(ion.getType()+ion.getCharge()+"+")).asObject());

            Callback factory = new Callback<TableColumn<FragmentRow, Double>, TableCell<FragmentRow, Double>>() {

                private int columns = ionTable.getColumns().size();

                @Override
                public TableCell<FragmentRow, Double> call(TableColumn<FragmentRow, Double> param) {
                    return new TableCell<FragmentRow, Double>() {

                        private int columnIndex = param.getTableView().getColumns().indexOf(param);

                        @Override
                        public void updateIndex(int i) {
                            super.updateIndex(i);
                            // select color based on index of row/column
                            if (i >= 0) {
                                // select color repeating the color, if we run out of colors

                                String color;
                                String fontColor = "#000000";
                                if(colors.size() > i && colors.get(i).size()>columnIndex){
                                    color =  colors.get(i).get(columnIndex);
                                    if(!color.equals("#ffffff")){
                                        fontColor="#ffffff";
                                    }

                                }else{
                                    color =  "ffffff";
                                }
                                this.setStyle("-fx-background-color: " + color + ";-fx-text-fill: "+fontColor+";");
                            }
                        }

                        @Override
                        protected void updateItem(Double mz, boolean empty) {
                            super.updateItem(mz, empty);
                            if (empty || mz==0) {
                                setText(null);
                            } else {
                                setText(df.format(mz));
                            }
                        }

                    };
                }

            };

            col.setCellFactory(factory);
            ionTable.getColumns().add(col);
        }
        TableColumn<FragmentRow, String> col = new TableColumn<>("aa");
        col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getAA()));
        ionTable.getColumns().add(col);

        for(Ion ion: ctermIons){
            TableColumn<FragmentRow, Double> col2 = new TableColumn<>(ion.getType()+ion.getCharge()+"+");
            col2.setCellValueFactory(param -> new SimpleDoubleProperty(param.getValue().getMass(ion.getType()+ion.getCharge()+"+")).asObject());

            Callback factory = new Callback<TableColumn<FragmentRow, Double>, TableCell<FragmentRow, Double>>() {

                private int columns = ionTable.getColumns().size();

                @Override
                public TableCell<FragmentRow, Double> call(TableColumn<FragmentRow, Double> param) {
                    return new TableCell<FragmentRow, Double>() {

                        private int columnIndex = param.getTableView().getColumns().indexOf(param);

                        @Override
                        public void updateIndex(int i) {
                            super.updateIndex(i);
                            // select color based on index of row/column
                            if (i >= 0) {
                                // select color repeating the color, if we run out of colors

                                String color;
                                String fontColor = "#000000";
                                if(colors.size() > i && colors.get(i).size()>columnIndex){
                                    color =  colors.get(i).get(columnIndex);
                                    if(!color.equals("#ffffff")){
                                        fontColor="#ffffff";
                                    }

                                }else{
                                    color =  "ffffff";
                                }
                                this.setStyle("-fx-background-color: " + color + ";-fx-text-fill: "+fontColor+";");
                            }
                        }

                        @Override
                        protected void updateItem(Double mz, boolean empty) {
                            super.updateItem(mz, empty);
                            if (empty || mz==0) {
                                setText(null);
                            } else {

                                setText(df.format(mz));
                            }
                        }

                    };
                }

            };

            col2.setCellFactory(factory);

            ionTable.getColumns().add(col2);
        }


        for(var i = 0; i < peptide.getSequence().length(); i += 1) {
            char aaChar = peptide.getSequence().charAt(i);
            FragmentRow row = new FragmentRow();
            row.setAa(aaChar);

            ArrayList<String> rowColors = new ArrayList<>();
            // nterm ions
            for(var n = 0; n < ntermIons.size(); n += 1) {


                Ion nIon = ntermIons.get(n);


                if(i < peptide.getSequence().length() - 1) {
                    ArrayList<Ion> seriesData = ionSeries.get(nIon.getType()).get(nIon.getCharge());

//                    var cls = "";
//                    var style = "";
//                    if(seriesData.get(i).isMatch()) {
//                        cls="matchIon";
//                        style="style='background-color:"+Ion.getSeriesColor(ntermIons[n])+";'";
//                    }

                    if(seriesData.get(i).isMatch()){
                        rowColors.add(Ion.getSeriesColor(seriesData.get(i)));
                    }else{
                        rowColors.add("#ffffff");
                    }

                    row.put(seriesData.get(i).getType()+seriesData.get(i).getCharge()+"+", seriesData.get(i));
//                    myTable +=    "<td class='"+cls+"' "+style+" >" +round(seriesData[i].mz)+  "</td>";
                }
                else {
//                    myTable +=    "<td>" +"&nbsp;"+  "</td>";
                    row.put(nIon.getLabel(), null);
                }
            }

//            myTable += "<td class='numCell'>"+(i+1)+"</td>";
//            if(options.peptide.varMods()[i+1])
//                myTable += "<td class='seq modified'>"+aaChar+"</td>";
//            else
//                myTable += "<td class='seq'>"+aaChar+"</td>";
//            myTable += "<td class='numCell'>"+(options.sequence.length - i)+"</td>";
            for(var n = 0; n < ctermIons.size(); n += 1) {


                Ion cIon = ctermIons.get(n);


                if(i > 0) {
                    int idx = sequence.length() - i - 1;
                    ArrayList<Ion> seriesData = ionSeries.get(cIon.getType()).get(cIon.getCharge());

//                    var cls = "";
//                    var style = "";
//                    if(seriesData.get(i).isMatch()) {
//                        cls="matchIon";
//                        style="style='background-color:"+Ion.getSeriesColor(ntermIons[n])+";'";
//                    }

                    if(seriesData.get(idx).isMatch()){
                        rowColors.add(Ion.getSeriesColor(seriesData.get(idx)));
                    }else{
                        rowColors.add("#ffffff");
                    }

                    row.put(seriesData.get(idx).getLabel().charAt(0)+String.valueOf(seriesData.get(idx).getCharge())+"+", seriesData.get(idx));
//                    myTable +=    "<td class='"+cls+"' "+style+" >" +round(seriesData[i].mz)+  "</td>";
                }
                else {
//                    myTable +=    "<td>" +"&nbsp;"+  "</td>";
                    row.put(cIon.getLabel(), null);
                }
            }

            colors.add(rowColors);
            ionTable.getItems().add(row);

        }

        //showAnticInfo(container); // Total annotated ion current
    }

    public void clear(){
        ionTable.getItems().clear();
        spectrumPane.getChildren().clear();
        spectrumPane.getChildren().add(zoomContainer);
    }


}
