package graphics;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.*;
import java.util.function.Function;

public class ConfidentLineChart extends Pane {

    HBox mainPane = new HBox();
    VBox rightPane = new VBox();


    Pane dataPane = new Pane();
    Pane xAxisPane = new Pane();
    Pane yAxisPane = new Pane();
    TreeMap<String, HashMap<String, ArrayList<Double>>> series = new TreeMap<>();


    Double manualMin;
    Double manualMax;



    String xLegend;
    String yLegend;

    String[] colors = {"#ee4035", "#f37736", "#fdf498", "#7bc043", "#0392cf",
            "#d11141","#00b159","#00aedb","#f37735","#ffc425",
            "#ebf4f6","bdeaee","#76b4bd","#58668b","#5e5656"};

    ArrayList<String> xCategories = new ArrayList<>();

    HashMap<String, ArrayList<Path>> seriesGroup = new HashMap<>();

    Function<String, String> onMouseLabelHoverCallback;

    String highlitedLabel;





    public ConfidentLineChart() {


        mainPane.getChildren().add(yAxisPane);

        Separator separator1 = new Separator();
        separator1.setPrefWidth(30);
        separator1.setOrientation(Orientation.VERTICAL);
        mainPane.getChildren().add(separator1);
        rightPane.getChildren().add(dataPane);
        rightPane.getChildren().add(xAxisPane);
        mainPane.getChildren().add(rightPane);




//        mainPane.prefWidthProperty().bind(this.widthProperty());
//        mainPane.prefHeightProperty().bind(this.heightProperty());


        this.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(ConfidentLineChart.this.getWidth()>0){
                    draw();
                }

            }
        });

    }

    public void addSeries(String name,HashMap<String, ArrayList<Double>> values){
        series.put(name, values);
    }

    public void draw(){



        clear();


        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;
        double offsetX = 0;


        double longestLabelWidth = 0;

        for(String label: series.keySet()){
            Text t = new Text(label);
            t.setFont(Font.font(15));
            double width = t.getBoundsInLocal().getWidth();
            if(width>longestLabelWidth){
                longestLabelWidth = width;
            }
        }


        for(Map.Entry<String, HashMap<String, ArrayList<Double>>> seriesEntry: series.entrySet()){
            for(Map.Entry<String, ArrayList<Double>> entry: seriesEntry.getValue().entrySet()){
                Double average = entry.getValue().stream().mapToDouble(val -> val).average().orElse(0.0);
                double sd = 0;
                for (double value : entry.getValue()) {
                    sd += Math.pow(value - average, 2);

                    if (value > max) {
                        max = value;
                    }
                    if (value < min) {
                        min = value;
                    }
                }
                sd = Math.sqrt(sd);
                sd /= (entry.getValue().size() - 1);

                double confInterval = 1.96 * sd / Math.sqrt(entry.getValue().size());

                if (average + confInterval > max) {
                    max = average + confInterval;
                }

                if (average - confInterval < min) {
                    min = Math.max(average - confInterval, 0);
                }


            }

        }


        if(manualMax!=null)
            max = manualMax;

        if(manualMin!=null)
            min = manualMin;

        double maxLabelHeight = 0;
        for(Map.Entry<String, HashMap<String, ArrayList<Double>>> seriesEntry: series.entrySet()) {
            for (Map.Entry<String, ArrayList<Double>> entry : seriesEntry.getValue().entrySet()) {

                if(!xCategories.contains(entry.getKey())){
                    xCategories.add(entry.getKey());
                    Text xLabel = new Text(entry.getKey());
                    xLabel.setFont(Font.font(17));
                    double labelHeight = xLabel.getLayoutBounds().getWidth();

                    if (labelHeight > maxLabelHeight) {
                        maxLabelHeight = labelHeight;
                    }
                }
            }
        }






        double yAxisWidth = makeYAxis(min, max, this.getHeight()-maxLabelHeight);

        drawDashedLines(this.getWidth() - yAxisWidth - longestLabelWidth,  this.getHeight()-maxLabelHeight);

        double barWidth = (this.getWidth() - yAxisWidth - longestLabelWidth) / series.entrySet().iterator().next().getValue().size() - 20;

        int i=0;




        for(String xCategory: xCategories) {

            Text xLabel = new Text(xCategory);
            xLabel.setFont(Font.font(17));

            double labelWidth = xLabel.getLayoutBounds().getWidth();


            xLabel.setRotate(90);
            //xLabel.setY(xLabel.getLayoutBounds().getHeight());

            xLabel.setTranslateY(labelWidth / 2 + 5);


            xLabel.setX(offsetX + barWidth / 2 - labelWidth / 2);


            xAxisPane.getChildren().add(xLabel);


            offsetX += barWidth + 20;
            i++;

        }



        double pixelsPerVal = (this.getHeight() - maxLabelHeight) /max;
        double height = this.getHeight() - maxLabelHeight;
        dataPane.setPrefHeight(height);




        int lineIndex = 0;
        for(Map.Entry<String, HashMap<String, ArrayList<Double>>> seriesEntry: series.entrySet()){

            ArrayList<Path> group = new  ArrayList<>();


            offsetX = 0;

            int seriesIndex = 0;


            Path path = new Path();
            group.add(path);
            path.setStroke(Color.web(colors[lineIndex]));
            path.setStrokeWidth(3);
            Path intervalPath = new Path();

            ArrayList<Double> lowerInterval = new ArrayList<>();
            ArrayList<Double> upperInterval = new ArrayList<>();


            for(Map.Entry<String, ArrayList<Double>> entry: seriesEntry.getValue().entrySet()){
                Double average = entry.getValue().stream().mapToDouble(val -> val).average().orElse(0.0);

                if(seriesIndex==0){
                    path.getElements().add(new MoveTo(offsetX+barWidth/2, height - height * average /max));
                }else{
                    path.getElements().add(new LineTo(offsetX+barWidth/2, height - height * average /max));
                }

                double sd = 0;
                for(double value: entry.getValue()){
                    sd+=Math.pow(value-average, 2);
                }
                sd = Math.sqrt(sd);
                sd/=(entry.getValue().size()-1);

                double confInterval = 1.96 * sd / Math.sqrt(entry.getValue().size());


                if(seriesIndex==0){
                    intervalPath.getElements().add(new MoveTo(offsetX+barWidth/2, Math.min(height, height - height * (average/max) + confInterval*pixelsPerVal)));
                }else{
                    intervalPath.getElements().add(new LineTo(offsetX+barWidth/2, Math.min(height, height - height * (average/max) + confInterval*pixelsPerVal)));
                }

                lowerInterval.add(Math.min(height, height - height * (average/max) + confInterval*pixelsPerVal));
                upperInterval.add(Math.min(height, height - height * (average/max) - confInterval*pixelsPerVal));






    //            if(manualMax!=null && average + confInterval>max){
    //                intervalLine.setStartY(0);
    //                intervalLineHorizTop.setStartY(0);
    //                intervalLineHorizTop.setEndY(0);
    //            }else{
    //                intervalLine.setStartY(height - height * (average/max) - confInterval*pixelsPerVal);
    //                intervalLineHorizTop.setStartY(height - height * (average/max) - confInterval*pixelsPerVal);
    //                intervalLineHorizTop.setEndY(height - height * (average/max) - confInterval*pixelsPerVal);
    //            }
    //
    //
    //            dataPane.getChildren().add(intervalLine);
    //            dataPane.getChildren().add(intervalLineHorizTop);
    //            dataPane.getChildren().add(intervalLineHorizBottom);

    //            int valIndex = 0;
    //            for(double value: entry.getValue()){
    //                Circle c = new Circle();
    //                double radius = Math.min(barWidth /8, 10);
    //                c.setRadius(radius);
    //                Random rand = new Random();
    //                double x = rand.nextDouble();
    //                x*=barWidth - 2 * radius;
    //                x+=radius;
    //
    //                c.setCenterX(offsetX+x);
    //                c.setCenterY(height - height * value /max);
    //                dataPane.getChildren().add(c);
    //                valIndex++;
    //            }





                offsetX+=barWidth+20;
                seriesIndex++;

            }

            dataPane.getChildren().add(path);

            Collections.reverse(upperInterval);
            i=0;
            offsetX-=barWidth+20;

            intervalPath.getElements().add(new LineTo(offsetX+barWidth/2,upperInterval.get(0)));

            for(double pos: upperInterval){
                if(i==0){
                    intervalPath.getElements().add(new MoveTo(offsetX+barWidth/2, pos));
                }else{
                    intervalPath.getElements().add(new LineTo(offsetX+barWidth/2, pos));
                }
                i++;
                offsetX-=barWidth+20;
            }

            intervalPath.getElements().add(new LineTo(offsetX+barWidth/2+barWidth+20,lowerInterval.get(0)));

            //intervalPath.setStroke(Color.RED);
            intervalPath.setFill(Color.web(colors[lineIndex], 0.4));
            group.add(intervalPath);
            seriesGroup.put(seriesEntry.getKey(), group);
            dataPane.getChildren().add(intervalPath);

            lineIndex++;
        }

        drawLabels(this.getWidth() - yAxisWidth - longestLabelWidth, this.getHeight()-maxLabelHeight);



        if(xLegend!=null){
            Text legend = new Text(xLegend);
            legend.setFont(Font.font(30));
            legend.setX(this.getWidth()/2 - legend.getLayoutBounds().getWidth()/2);
            legend.setY(80);
            xAxisPane.getChildren().add(legend);
        }




        this.getChildren().add(mainPane);
    }


    public double makeYAxis(double min, double max, double height){

        double tickIndent = (max-min)/10;

        double widestText = 0;

        for (int i = 0; i <= 10; i++) {
            Text text = new Text(String.valueOf(Math.round(tickIndent*i)));
            text.setFont(Font.font(12));

            if(text.getLayoutBounds().getWidth()>widestText){
                widestText = text.getLayoutBounds().getWidth();
            }
        }


        for (int i = 0; i <= 10; i++) {

            Text text;
            if(max-min>10){
                text = new Text(String.valueOf(Math.round(tickIndent*i)));
            }else{
                text = new Text(String.format("%.2f", tickIndent*i));
            }

            text.setFont(Font.font(12));
            text.setX(5 - text.getLayoutBounds().getWidth() + widestText);
            text.setY(height - height/10 * i + text.getLayoutBounds().getHeight()/2);
            yAxisPane.getChildren().add(text);


        }

        for (int i = 0; i <= 10; i++) {
            Line l = new Line();
            l.setStartY(height - height/10 * i);
            l.setEndY(height - height/10 * i);
            l.setEndX(20 + widestText);
            l.setStartX(10 + widestText);
            yAxisPane.getChildren().add(l);
        }

        double yAxisWidth = 20 + widestText;

        Line line = new Line();
        line.setStartY(height);
        line.setEndY(0);
        line.setEndX(yAxisWidth);
        line.setStartX(yAxisWidth);
        yAxisPane.getChildren().add(line);

        yAxisPane.setPrefWidth(yAxisWidth);
        return yAxisWidth;
    }


    public HBox getPane(){
        return mainPane;
    }

    public void setLegend(String x, String y){
        xLegend = x;
        yLegend = y;
    }
    public void setXLegend(String x){
        xLegend = x;
    }
    public void setYLegend(String y){
        yLegend = y;
    }

    public void clear(){
        mainPane.getChildren().clear();
        mainPane = new HBox();
        xAxisPane = new Pane();
        dataPane = new Pane();
        yAxisPane = new Pane();
        rightPane = new VBox();

        rightPane.getChildren().add(dataPane);
        rightPane.getChildren().add(xAxisPane);

        mainPane.getChildren().add(yAxisPane);
        mainPane.getChildren().add(rightPane);


        this.getChildren().clear();
    }

    private void drawDashedLines(double width, double height){
        for (int i = 0; i <= 10; i++) {
            Line l = new Line(0, height - height/10 * i, width, height - height/10 * i);
            l.getStrokeDashArray().addAll(2d);
            l.setStroke(Color.rgb(127,127,127));
            l.setOpacity(0.5);
            dataPane.getChildren().add(l);
        }
    }

//    public void addAll(HashMap<String, ArrayList<Double>> series){
//        this.series = series;
//    }

    public void setMin(double min){
        this.manualMin = min;
    }

    public void setMax(double max){
        this.manualMax = max;
    }

    public void drawLabels(double xStart, double height){

        Text t = new Text(series.keySet().iterator().next());
        t.setFont(Font.font(15));

        double margin = (height - series.keySet().size()*t.getLayoutBounds().getHeight()) / (series.keySet().size()+1);

        double y = margin;

        int i = 0;
        for(String label: series.keySet()){
            t = new Text(label);
            t.setFont(Font.font(15));
            t.setX(xStart);
            t.setY(y);
            t.setFill(Color.web(colors[i]));
            y+=t.getLayoutBounds().getHeight() + margin;
            dataPane.getChildren().add(t);
            i++;

            Text finalT = t;
            t.setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    ArrayList<Path> group = seriesGroup.get(finalT.getText());

                    for(Map.Entry<String, ArrayList<Path>> entry: seriesGroup.entrySet()){
                        if(entry.getKey().equals(finalT.getText())){
                            for(Path path:group){
                                path.setStrokeWidth(7);
                                path.toFront();
                            }

                            if(highlitedLabel==null || !highlitedLabel.equals(finalT.getText())){
                                if(onMouseLabelHoverCallback!=null){
                                    onMouseLabelHoverCallback.apply(finalT.getText());
                                }
                                highlitedLabel = finalT.getText();
                            }

                        }else{
                            for(Path path:entry.getValue()){
                                path.setStrokeWidth(1);
                            }
                        }
                    }


                }
            });

        }

    }

    public TreeMap<String, HashMap<String, ArrayList<Double>>> getSeries(){
        return series;
    }

    public void setOnMouseLabelHoverCallback(Function<String, String> function){
        onMouseLabelHoverCallback = function;
    }





}
