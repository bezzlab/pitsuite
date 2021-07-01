package Controllers;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONObject;
import pitguiv2.Settings;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotSaver {

    String chartType;


    public PlotSaver(String chartType) {
        this.chartType = chartType;
    }

    public void setData(ArrayList<XYChart.Series> allSeries, Stage stage) {

        try {
            String filename = "tmp/" + UUID.randomUUID() + ".csv";
            PrintWriter writer = new PrintWriter(filename, StandardCharsets.UTF_8);
            for (XYChart.Series series : allSeries) {
                for (int i = 0; i < series.getData().size(); i++) {
                    XYChart.Data<String, Double> d = (XYChart.Data<String, Double>) series.getData().get(i);
                    writer.println(series.getName()+","+d.getXValue()+","+d.getYValue());
                }

            }

            writer.close();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Image");
            File file = fileChooser.showSaveDialog(stage);
            run(filename, file.getAbsolutePath());
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public void setBarchartData(ArrayList<XYChart.Series> allSeries, Stage stage) {
        try {
            String filename = "tmp/" + UUID.randomUUID() + ".csv";
            PrintWriter writer = new PrintWriter(filename, StandardCharsets.UTF_8);
            for (XYChart.Series series : allSeries) {
                for (int i = 0; i < series.getData().size(); i++) {
                    XYChart.Data<String, Double> d = (XYChart.Data<String, Double>) series.getData().get(i);
                    writer.println(d.getXValue()+","+d.getYValue());
                }

            }

            writer.close();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Image");
            File file = fileChooser.showSaveDialog(stage);
            run(filename, file.getAbsolutePath(), "Normalised read counts");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setBarchartData(HashMap<String, ArrayList<Double>> conditionsData, Stage stage) {
        try {
            String filename = "tmp/" + UUID.randomUUID() + ".csv";
            PrintWriter writer = new PrintWriter(filename, StandardCharsets.UTF_8);
            for (Map.Entry<String, ArrayList<Double>> entry : conditionsData.entrySet()) {
                for (int i = 0; i < entry.getValue().size(); i++) {
                    writer.println(entry.getKey()+","+entry.getValue().get(i));
                }

            }

            writer.close();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Image");
            File file = fileChooser.showSaveDialog(stage);
            run(filename, file.getAbsolutePath(), "PSI", 0., 1.);
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    public void run(String dataPath, String outputPath){

        ProcessBuilder pb = new ProcessBuilder(Settings.getInstance().getPathToR(), "Rscripts/"+chartType+".R",
                dataPath, outputPath);

        try {


            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader inStreamReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line = inStreamReader.readLine();
            while (line != null) {
                line = inStreamReader.readLine();
                System.out.println(line);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void run(String dataPath, String outputPath, String yLabel, double yMin, double yMax){

        ProcessBuilder pb = new ProcessBuilder(Settings.getInstance().getPathToR(), "Rscripts/"+chartType+".R",
                dataPath, outputPath, yLabel, String.valueOf(yMin), String.valueOf(yMax));

        try {


            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader inStreamReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line = inStreamReader.readLine();
            while (line != null) {
                line = inStreamReader.readLine();
                System.out.println(line);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void run(String dataPath, String outputPath, String yLabel){

        ProcessBuilder pb = new ProcessBuilder(Settings.getInstance().getPathToR(), "Rscripts/"+chartType+".R",
                dataPath, outputPath, yLabel);

        try {


            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader inStreamReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line = inStreamReader.readLine();
            while (line != null) {
                line = inStreamReader.readLine();
                System.out.println(line);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void savePng(WritableImage image, Stage stage){
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Save Image");
        File file = fileChooser.showSaveDialog(stage);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void saveBarchartData(ArrayList<XYChart.Series> allSeries, String firstColumnName, String secondColumnName, Stage stage) {
        try {




            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
            fileChooser.setTitle("Save data");
            File file = fileChooser.showSaveDialog(stage);

            String filename = file.getAbsolutePath();
            PrintWriter writer = new PrintWriter(filename, StandardCharsets.UTF_8);

            writer.println(firstColumnName+","+secondColumnName);
            for (XYChart.Series series : allSeries) {
                for (int i = 0; i < series.getData().size(); i++) {
                    XYChart.Data<String, Double> d = (XYChart.Data<String, Double>) series.getData().get(i);
                    writer.println(d.getXValue()+","+d.getYValue());
                }

            }
            writer.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
