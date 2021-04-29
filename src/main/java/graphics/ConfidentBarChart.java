package graphics;

import javafx.geometry.Orientation;
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
import javafx.util.Pair;

import java.util.*;

public class ConfidentBarChart extends Pane {

    VBox mainPane = new VBox();
    HBox mainDataPane = new HBox();
    VBox rightPane = new VBox();



    Pane dataPane = new Pane();
    Pane xAxisPane = new Pane();
    Pane yAxisPane = new Pane();
    Pane titlePane = new Pane();
    HashMap<String, HashMap<String, ArrayList<Double>>> groups = new HashMap<>();

    Double manualMin;
    Double manualMax;

    String xLegend;
    String yLegend;

    String meanOrMedian = "mean";
    String reference;

    String[] colors = {"#aaf0d1","#ffa474","#bc5d58","#b2ec5d","#17806d","#fcb4d5","#e3256b","#bab86c","#fce883",
            "#cda4de","#8e4585","#1f75fe","#1a4876","#fe4eda","#c8385a","#ff7538"};

    String title;

    Pair<Double, String> horizontalLineAt;





    public ConfidentBarChart() {

        mainDataPane.getChildren().add(yAxisPane);

        Separator separator1 = new Separator();
        separator1.setPrefWidth(30);
        separator1.setOrientation(Orientation.VERTICAL);
        mainDataPane.getChildren().add(separator1);
        rightPane.getChildren().add(dataPane);
        rightPane.getChildren().add(xAxisPane);
        mainDataPane.getChildren().add(rightPane);


        mainDataPane.prefWidthProperty().bind(this.widthProperty());
        mainDataPane.prefHeightProperty().bind(this.heightProperty());

        titlePane.prefWidthProperty().bind(this.widthProperty());
        mainPane.getChildren().add(titlePane);
        mainPane.getChildren().add(mainDataPane);


        this.heightProperty().addListener((observable, oldValue, newValue) -> {
            if(ConfidentBarChart.this.getWidth()>0){
                draw();
            }

        });

    }

    public void addSeries(String name, String group, ArrayList<Double> values){
        if(!groups.containsKey(group)){
            groups.put(group, new HashMap<>());
        }
        groups.get(group).put(name, values);
    }

    public void addSeries(String name, ArrayList<Double> values){
        addSeries(name, "default", values);
    }

    public void addGroups(HashMap<String, HashMap<String, ArrayList<Double>>> groups){
        this.groups = groups;
    }


    public void draw(){

        clear();

        double titleHeight = makeTitle();

        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;
        double offsetX = 0;


        for(Map.Entry<String, HashMap<String, ArrayList<Double>>> groupEntry: groups.entrySet()) {
            for (Map.Entry<String, ArrayList<Double>> entry : groupEntry.getValue().entrySet()) {

                Double agregatedValue;
                if(meanOrMedian.equals("mean"))
                    agregatedValue = entry.getValue().stream().mapToDouble(val -> val).average().orElse(0.0);
                 else{
                    double[] values = entry.getValue().stream().mapToDouble(val -> val).toArray();
                    Arrays.sort(values);
                    if(values.length==0)
                        agregatedValue = 0.;
                    else if (values.length % 2 == 0)
                        agregatedValue = (values[values.length/2] + values[values.length/2 - 1])/2;
                    else
                        agregatedValue = values[values.length/2];
                }


                double sd = 0;
                for (double value : entry.getValue()) {
                    sd += Math.pow(value - agregatedValue, 2);

                    if (value > max) {
                        max = value;
                    }
                    if (value < min) {
                        min = value;
                    }
                }

                if(meanOrMedian.equals("mean")) {
                    sd /= (entry.getValue().size() - 1);
                    sd = Math.sqrt(sd);
                    double confInterval = 1.96 * sd / Math.sqrt(entry.getValue().size());

                    if (agregatedValue + confInterval > max) {
                        max = agregatedValue + confInterval;
                    }

                    if (agregatedValue - confInterval < min) {
                        min = Math.max(agregatedValue - confInterval, 0);
                    }

                }

            }
        }


        if(manualMax!=null)
            max = manualMax;

        if(manualMin!=null)
            min = manualMin;

        double maxLabelHeight = 0;
        for(Map.Entry<String, HashMap<String, ArrayList<Double>>> groupEntry: groups.entrySet()) {
            for (Map.Entry<String, ArrayList<Double>> entry : groupEntry.getValue().entrySet()) {
                Text xLabel = new Text(entry.getKey());
                xLabel.setFont(Font.font(20));
                double labelHeight = xLabel.getLayoutBounds().getWidth();

                if (labelHeight > maxLabelHeight) {
                    maxLabelHeight = labelHeight;
                }
            }
        }






        double yAxisWidth = makeYAxis(min, max, this.getHeight()-maxLabelHeight - titleHeight);
        double groupLabelsWidth = 0;
        if(groups.size()>1){
            groupLabelsWidth = makeGroupLegend();
        }

        drawDashedLines(this.getWidth() - yAxisWidth,  this.getHeight()-maxLabelHeight - titleHeight);



        double barWidth = (this.getWidth() - yAxisWidth - groupLabelsWidth);
        if(groups.containsKey("default"))
            barWidth-=20*(groups.get("default").size()-1);
        else
            barWidth-=20*(groups.size()-1);

        int nbBars = 0;
        for(Map.Entry<String, HashMap<String, ArrayList<Double>>> entry: groups.entrySet()){
            nbBars+=entry.getValue().size();
        }
        barWidth/=nbBars;


        for(Map.Entry<String, HashMap<String, ArrayList<Double>>> groupEntry: groups.entrySet()) {
            for (Map.Entry<String, ArrayList<Double>> entry : groupEntry.getValue().entrySet()) {
                Text xLabel = new Text(entry.getKey());
                xLabel.setFont(Font.font("monospace", 20));

                double labelWidth = xLabel.getLayoutBounds().getWidth();


                xLabel.setRotate(90);
                //xLabel.setY(xLabel.getLayoutBounds().getHeight());

                xLabel.setTranslateY(labelWidth / 2 + 5);


                xLabel.setX(offsetX + barWidth / 2 - labelWidth / 2);


                xAxisPane.getChildren().add(xLabel);


                offsetX += barWidth + 20;
            }
        }



        double pixelsPerVal = (this.getHeight() - maxLabelHeight - titleHeight) /max;
        double height = this.getHeight() - maxLabelHeight - titleHeight;
        dataPane.setPrefHeight(height);

        offsetX = 0;


        int colorIndex = 0;
        for(Map.Entry<String, HashMap<String, ArrayList<Double>>> groupEntry: groups.entrySet()) {
            for (Map.Entry<String, ArrayList<Double>> entry : groupEntry.getValue().entrySet()) {

                double agregatedValue;
                if(meanOrMedian.equals("mean"))
                    agregatedValue = entry.getValue().stream().mapToDouble(val -> val).average().orElse(0.0);
                else{
                    double[] values = entry.getValue().stream().mapToDouble(val -> val)
                            .filter((d) -> !Double.isNaN(d)).toArray();
                    Arrays.sort(values);
                    if (values.length == 0)
                        agregatedValue = 0.;
                    else if (values.length % 2 == 0)
                        agregatedValue = (values[values.length/2] + values[values.length/2 - 1])/2;
                    else
                        agregatedValue = values[values.length/2];
                }

                double sd = 0;
                for (double value : entry.getValue()) {
                    sd += Math.pow(value - agregatedValue, 2);
                }

                sd /= (entry.getValue().size() - 1);
                sd = Math.sqrt(sd);

                double confInterval = 1.96 * sd / Math.sqrt(entry.getValue().size());


                Rectangle rec = new Rectangle();
                rec.setX(offsetX);
                rec.setWidth(barWidth);

                rec.setY(height - height * (agregatedValue / max));
                rec.setHeight(height * (agregatedValue / max));



                rec.setFill(Color.web(colors[colorIndex]));
                dataPane.getChildren().add(rec);

                if(meanOrMedian.equals("mean")) {
                    Line intervalLine = new Line();
                    intervalLine.setStartX(offsetX + barWidth / 2);
                    intervalLine.setEndX(offsetX + barWidth / 2);

                    intervalLine.setEndY(Math.min(height, height - height * (agregatedValue / max) + confInterval * pixelsPerVal));

                    Line intervalLineHorizTop = new Line();
                    intervalLineHorizTop.setStartX(offsetX + barWidth * 0.25);
                    intervalLineHorizTop.setEndX(offsetX + barWidth * 0.75);


                    Line intervalLineHorizBottom = new Line();
                    intervalLineHorizBottom.setStartX(offsetX + barWidth * 0.25);
                    intervalLineHorizBottom.setEndX(offsetX + barWidth * 0.75);
                    intervalLineHorizBottom.setStartY(Math.min(height, height - height * (agregatedValue / max) + confInterval * pixelsPerVal));
                    intervalLineHorizBottom.setEndY(Math.min(height, height - height * (agregatedValue / max) + confInterval * pixelsPerVal));

                    if (manualMax != null && agregatedValue + confInterval > max) {
                        intervalLine.setStartY(0);
                        intervalLineHorizTop.setStartY(0);
                        intervalLineHorizTop.setEndY(0);
                    } else {
                        intervalLine.setStartY(height - height * (agregatedValue / max) - confInterval * pixelsPerVal);
                        intervalLineHorizTop.setStartY(height - height * (agregatedValue / max) - confInterval * pixelsPerVal);
                        intervalLineHorizTop.setEndY(height - height * (agregatedValue / max) - confInterval * pixelsPerVal);
                    }


                    dataPane.getChildren().add(intervalLine);
                    dataPane.getChildren().add(intervalLineHorizTop);
                    dataPane.getChildren().add(intervalLineHorizBottom);
                }

                if(!entry.getKey().equals(reference)) {
                    for (double value : entry.getValue()) {
                        Circle c = new Circle();
                        double radius = Math.min(barWidth / 8, 10);
                        c.setRadius(radius);
                        Random rand = new Random();
                        double x = rand.nextDouble();
                        x *= barWidth - 2 * radius;
                        x += radius;

                        c.setCenterX(offsetX + x);
                        c.setCenterY(height - height * value / max);
                        dataPane.getChildren().add(c);

                    }
                }

                offsetX += barWidth;

                if(groups.size()==1){
                    colorIndex++;
                    offsetX += 20;
                }


            }
            if(groups.size()>1){
                colorIndex++;
                offsetX += 20;
            }
        }

        if(xLegend!=null){
            Text legend = new Text(xLegend);
            legend.setFont(Font.font(30));
            legend.setX(this.getWidth()/2 - legend.getLayoutBounds().getWidth()/2);
            legend.setY(80);
            xAxisPane.getChildren().add(legend);
        }


        if (horizontalLineAt!=null){
            Line line = new Line();
            line.setStartX(0);
            line.setEndX(offsetX);
            double lineValue = horizontalLineAt.getKey();
            line.setStartY(height - (lineValue /(max-min))*height);
            line.setEndY(height - (lineValue /(max-min))*height);
            dataPane.getChildren().add(line);
        }




        this.getChildren().add(mainPane);
    }


    public double makeYAxis(double min, double max, double height){

        double tickIndent = (max-min)/10;

        double widestText = 0;

        for (int i = 0; i <= 10; i++) {
            Text text;
            if(max-min>10){
                text = new Text(String.valueOf(Math.round(tickIndent*i)));
            }else{
                text = new Text(String.format("%.2f", tickIndent*i));
            }
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

    private double makeGroupLegend(){
        int i=0;

        Pane container = new Pane();
        Text t = new Text("T");
        t.setFont(Font.font("monospace", 16));

        double paneHeight = groups.size()*t.getLayoutBounds().getHeight() + (groups.size()-1)*10;
        double offsetY = (this.getHeight()-paneHeight) / 2;

        double maxWidth = 0;

        for(String group: groups.keySet()){
            Text groupText = new Text(group);
            groupText.setFont(Font.font("monospace", 16));
            Rectangle rectangle = new Rectangle();
            rectangle.setWidth(20);
            rectangle.setHeight(groupText.getLayoutBounds().getHeight());
            rectangle.setFill(Color.web(colors[i]));

            double width = 25 + groupText.getLayoutBounds().getWidth();
            if(width>maxWidth)
                maxWidth = width;

            rectangle.setY(offsetY);
            groupText.setY(offsetY);
            groupText.setX(25);
            groupText.setY(offsetY+groupText.getLayoutBounds().getHeight());

            offsetY+=rectangle.getHeight()+10;
            i++;

            container.getChildren().add(rectangle);
            container.getChildren().add(groupText);

        }
        mainDataPane.getChildren().add(container);
        return maxWidth;
    }

    private double makeTitle(){
        if(title!=null){
            Text t = new Text(title);
            t.setFont(Font.font("monospace", 20));
            t.setX((this.getWidth() - t.getLayoutBounds().getWidth()) / 2);
            titlePane.getChildren().add(t);
            return t.getLayoutBounds().getHeight();
        }
        return 0.;
    }


    public HBox getPane(){
        return mainDataPane;
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
        mainDataPane.getChildren().clear();
        titlePane.getChildren().clear();
        mainDataPane = new HBox();
        xAxisPane = new Pane();
        dataPane = new Pane();
        yAxisPane = new Pane();
        rightPane = new VBox();

        rightPane.getChildren().add(dataPane);
        rightPane.getChildren().add(xAxisPane);

        mainDataPane.getChildren().add(yAxisPane);
        mainDataPane.getChildren().add(rightPane);

        mainPane.getChildren().add(titlePane);
        mainPane.getChildren().add(mainDataPane);


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

    public void addAll(String group, HashMap<String, ArrayList<Double>> series){
        groups.put(group, series);
    }
    public void addAll(HashMap<String, ArrayList<Double>> series){
        groups.put("default", series);
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

    public void setMeanOrMedian(String val){
        this.meanOrMedian = val;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setReference(String ref){
        this.reference = ref;
    }

    public void drawHorizontalLineAt(Double value, String label){
        horizontalLineAt = new Pair<>(value, label);
    }




}
