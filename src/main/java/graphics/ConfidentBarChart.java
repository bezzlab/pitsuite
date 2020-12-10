package graphics;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Dimension2D;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ConfidentBarChart extends Pane {

    HBox mainPane = new HBox();
    VBox rightPane = new VBox();


    Pane dataPane = new Pane();
    Pane xAxisPane = new Pane();
    Pane yAxisPane = new Pane();
    HashMap<String, ArrayList<Double>> series = new HashMap<>();

    Double manualMin;
    Double manualMax;



    String xLegend;
    String yLegend;

    String[] colors = {"#ee4035", "#f37736", "#fdf498", "#7bc043", "#0392cf",
    "#d11141","#00b159","#00aedb","#f37735","#ffc425",
    "#ebf4f6","bdeaee","#76b4bd","#58668b","#5e5656"};





    public ConfidentBarChart() {


        mainPane.getChildren().add(yAxisPane);

        Separator separator1 = new Separator();
        separator1.setPrefWidth(30);
        separator1.setOrientation(Orientation.VERTICAL);
        mainPane.getChildren().add(separator1);
        rightPane.getChildren().add(dataPane);
        rightPane.getChildren().add(xAxisPane);
        mainPane.getChildren().add(rightPane);


        mainPane.prefWidthProperty().bind(this.widthProperty());
        mainPane.prefHeightProperty().bind(this.heightProperty());


        this.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(ConfidentBarChart.this.getWidth()>0){
                    draw();
                }

            }
        });

    }

    public void addSeries(String name, ArrayList<Double> values){
        series.put(name, values);
    }

    public void draw(){




        clear();


        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;
        double offsetX = 0;


        for(Map.Entry<String, ArrayList<Double>> entry: series.entrySet()){
            Double average = entry.getValue().stream().mapToDouble(val -> val).average().orElse(0.0);
            double sd = 0;
            for(double value: entry.getValue()){
                sd+=Math.pow(value-average, 2);

                if(value>max){
                    max=value;
                }
                if(value<min){
                    min=value;
                }
            }
            sd = Math.sqrt(sd);
            sd/=(entry.getValue().size()-1);

            double confInterval = 1.96 * sd / Math.sqrt(entry.getValue().size());

            if(average + confInterval > max){
                max = average + confInterval;
            }

            if(average - confInterval < min){
                min = Math.max(average - confInterval, 0);
            }




        }


        if(manualMax!=null)
            max = manualMax;

        if(manualMin!=null)
            min = manualMin;

        double maxLabelHeight = 0;
        for(Map.Entry<String, ArrayList<Double>> entry: series.entrySet()){
            Text xLabel = new Text(entry.getKey());
            xLabel.setFont(Font.font(17));
            double labelHeight = xLabel.getLayoutBounds().getWidth();

            if(labelHeight>maxLabelHeight){
                maxLabelHeight = labelHeight;
            }
        }






        double yAxisWidth = makeYAxis(min, max, this.getHeight()-maxLabelHeight);

        drawDashedLines(this.getWidth() - yAxisWidth,  this.getHeight()-maxLabelHeight);

        double barWidth = (this.getWidth() - yAxisWidth) / series.size() - 20;

        int i=0;

        for(Map.Entry<String, ArrayList<Double>> entry: series.entrySet()){
            Text xLabel = new Text(entry.getKey());
            xLabel.setFont(Font.font(17));

            double labelWidth = xLabel.getLayoutBounds().getWidth();


            xLabel.setRotate(90);
            //xLabel.setY(xLabel.getLayoutBounds().getHeight());

            xLabel.setTranslateY(labelWidth/2+5);


            xLabel.setX(offsetX + barWidth/2 - labelWidth/2 );



            xAxisPane.getChildren().add(xLabel);



            offsetX+=barWidth+20;
            i++;
        }



        double pixelsPerVal = (this.getHeight() - maxLabelHeight) /max;
        double height = this.getHeight() - maxLabelHeight;
        dataPane.setPrefHeight(height);

        offsetX = 0;

        int seriesIndex = 0;
        for(Map.Entry<String, ArrayList<Double>> entry: series.entrySet()){
            Double average = entry.getValue().stream().mapToDouble(val -> val).average().orElse(0.0);

            double sd = 0;
            for(double value: entry.getValue()){
                sd+=Math.pow(value-average, 2);
            }
            sd = Math.sqrt(sd);
            sd/=(entry.getValue().size()-1);

            double confInterval = 1.96 * sd / Math.sqrt(entry.getValue().size());




            Rectangle rec = new Rectangle();
            rec.setX(offsetX);
            rec.setWidth(barWidth);

            rec.setY(height - height * (average/max));
            rec.setHeight(height * (average/max));
            rec.setFill(Color.web(colors[seriesIndex]));

            Line intervalLine = new Line();
            intervalLine.setStartX(offsetX+barWidth/2);
            intervalLine.setEndX(offsetX+barWidth/2);

            intervalLine.setEndY(Math.min(height, height - height * (average/max) + confInterval*pixelsPerVal));

            Line intervalLineHorizTop = new Line();
            intervalLineHorizTop.setStartX(offsetX+barWidth*0.25);
            intervalLineHorizTop.setEndX(offsetX+barWidth*0.75);


            Line intervalLineHorizBottom = new Line();
            intervalLineHorizBottom.setStartX(offsetX+barWidth*0.25);
            intervalLineHorizBottom.setEndX(offsetX+barWidth*0.75);
            intervalLineHorizBottom.setStartY(Math.min(height, height - height * (average/max) + confInterval*pixelsPerVal));
            intervalLineHorizBottom.setEndY(Math.min(height, height - height * (average/max) + confInterval*pixelsPerVal));

            if(manualMax!=null && average + confInterval>max){
                intervalLine.setStartY(0);
                intervalLineHorizTop.setStartY(0);
                intervalLineHorizTop.setEndY(0);
            }else{
                intervalLine.setStartY(height - height * (average/max) - confInterval*pixelsPerVal);
                intervalLineHorizTop.setStartY(height - height * (average/max) - confInterval*pixelsPerVal);
                intervalLineHorizTop.setEndY(height - height * (average/max) - confInterval*pixelsPerVal);
            }

            dataPane.getChildren().add(rec);
            dataPane.getChildren().add(intervalLine);
            dataPane.getChildren().add(intervalLineHorizTop);
            dataPane.getChildren().add(intervalLineHorizBottom);

            int valIndex = 0;
            for(double value: entry.getValue()){
                Circle c = new Circle();
                double radius = Math.min(barWidth /8, 10);
                c.setRadius(radius);
                Random rand = new Random();
                double x = rand.nextDouble();
                x*=barWidth - 2 * radius;
                x+=radius;

                c.setCenterX(offsetX+x);
                c.setCenterY(height - height * value /max);
                dataPane.getChildren().add(c);
                valIndex++;
            }





            offsetX+=barWidth+20;
            seriesIndex++;

        }

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

    public void addAll(HashMap<String, ArrayList<Double>> series){
        this.series = series;
    }

    public void setMin(double min){
        this.manualMin = min;
    }

    public void setMax(double max){
        this.manualMax = max;
    }

//    protected Object autoRange(double minValue, double maxValue, double length, double labelSize) {
//
//        // calculate the number of tick-marks we can fit in the given length
//        int numOfTickMarks = (int)Math.floor(length/labelSize);
//        // can never have less than 2 tick marks one for each end
//        numOfTickMarks = Math.max(numOfTickMarks, 2);
//        //int minorTickCount = Math.max(getMinorTickCount(), 1);
//
//        double range = maxValue-minValue;
//
//        if (range != 0 && range/(numOfTickMarks*minorTickCount) <= Math.ulp(minValue)) {
//            range = 0;
//        }
//        // pad min and max by 2%, checking if the range is zero
//        final double paddedRange = (range == 0)
//                ? minValue == 0 ? 2 : Math.abs(minValue)*0.02
//                : Math.abs(range)*1.02;
//        final double padding = (paddedRange - range) / 2;
//        // if min and max are not zero then add padding to them
//        double paddedMin = minValue - padding;
//        double paddedMax = maxValue + padding;
//        // check padding has not pushed min or max over zero line
//        if ((paddedMin < 0 && minValue >= 0) || (paddedMin > 0 && minValue <= 0)) {
//            // padding pushed min above or below zero so clamp to 0
//            paddedMin = 0;
//        }
//        if ((paddedMax < 0 && maxValue >= 0) || (paddedMax > 0 && maxValue <= 0)) {
//            // padding pushed min above or below zero so clamp to 0
//            paddedMax = 0;
//        }
//        // calculate tick unit for the number of ticks can have in the given data range
//        double tickUnit = paddedRange/(double)numOfTickMarks;
//        // search for the best tick unit that fits
//        double tickUnitRounded = 0;
//        double minRounded = 0;
//        double maxRounded = 0;
//        int count = 0;
//        double reqLength = Double.MAX_VALUE;
//        String formatter = "0.00000000";
//        // loop till we find a set of ticks that fit length and result in a total of less than 20 tick marks
//        while (reqLength > length || count > 20) {
//            int exp = (int)Math.floor(Math.log10(tickUnit));
//            final double mant = tickUnit / Math.pow(10, exp);
//            double ratio = mant;
//            if (mant > 5d) {
//                exp++;
//                ratio = 1;
//            } else if (mant > 1d) {
//                ratio = mant > 2.5 ? 5 : 2.5;
//            }
//            if (exp > 1) {
//                formatter = "#,##0";
//            } else if (exp == 1) {
//                formatter = "0";
//            } else {
//                final boolean ratioHasFrac = Math.rint(ratio) != ratio;
//                final StringBuilder formatterB = new StringBuilder("0");
//                int n = ratioHasFrac ? Math.abs(exp) + 1 : Math.abs(exp);
//                if (n > 0) formatterB.append(".");
//                for (int i = 0; i < n; ++i) {
//                    formatterB.append("0");
//                }
//                formatter = formatterB.toString();
//
//            }
//            tickUnitRounded = ratio * Math.pow(10, exp);
//            // move min and max to nearest tick mark
//            minRounded = Math.floor(paddedMin / tickUnitRounded) * tickUnitRounded;
//            maxRounded = Math.ceil(paddedMax / tickUnitRounded) * tickUnitRounded;
//            // calculate the required length to display the chosen tick marks for real, this will handle if there are
//            // huge numbers involved etc or special formatting of the tick mark label text
//            double maxReqTickGap = 0;
//            double last = 0;
//            count = (int)Math.ceil((maxRounded - minRounded)/tickUnitRounded);
//            double major = minRounded;
//            for (int i = 0; major <= maxRounded && i < count; major += tickUnitRounded, i++)  {
//                //Dimension2D markSize = measureTickMarkSize(major, getTickLabelRotation(), formatter);
//                //double size = side.isVertical() ? markSize.getHeight() : markSize.getWidth();
////                double size = markSize.getHeight();
////                if (i == 0) { // first
////                    last = size/2;
////                } else {
////                    maxReqTickGap = Math.max(maxReqTickGap, last + 6 + (size/2) );
////                }
//            }
//            reqLength = (count-1) * maxReqTickGap;
//            tickUnit = tickUnitRounded;
//
//            // fix for RT-35600 where a massive tick unit was being selected
//            // unnecessarily. There is probably a better solution, but this works
//            // well enough for now.
//            if (numOfTickMarks == 2 && reqLength > length) {
//                break;
//            }
//            if (reqLength > length || count > 20) tickUnit *= 2; // This is just for the while loop, if there are still too many ticks
//        }
//        // calculate new scale
//        final double newScale = calculateNewScale(length, minRounded, maxRounded);
//        // return new range
//        return new Object[]{minRounded, maxRounded, tickUnitRounded, newScale, formatter};
//    }

    protected final double calculateNewScale(double length, double lowerBound, double upperBound) {
        double newScale = 1;
        newScale = ((upperBound-lowerBound) == 0) ? -length : -(length / (upperBound - lowerBound));
        return newScale;
    }




}
