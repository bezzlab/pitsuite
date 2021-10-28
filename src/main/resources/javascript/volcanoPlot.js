function displayVolcano(belowThresholdJson, foldChangesNegativeFoldJson, foldChangesPositiveFoldJson,
                        pValThreshold, foldChangeThreshold, maxPval, width, height, title, fontsize){

    var  posFold= {
        x: foldChangesPositiveFoldJson.logFoldChange,
        y:  foldChangesPositiveFoldJson.logPval,
        mode: 'markers',
        type: 'scatter',
        name: 'Increase',
        text: foldChangesPositiveFoldJson.texts,
        textposition: 'top center',
        textfont: {
            family:  'Times New Roman'
        },
        textposition: 'bottom center',
        marker: {size:9, color:"#1E73BE"}
    };

    var negFold = {
        x: foldChangesNegativeFoldJson.logFoldChange,
        y: foldChangesNegativeFoldJson.logPval ,
        mode: 'markers',
        type: 'scatter',
        name: 'Decrease',
        text:foldChangesNegativeFoldJson.texts,
        textposition: 'top center',
        textfont: {
            family:  'Times New Roman'
        },
        textposition: 'bottom center',
        marker: {size:9, color:"#C70039"}
    };

    var belowThreshold = {
        x: belowThresholdJson.logFoldChange,
        y:  belowThresholdJson.logPval,
        mode: 'markers',
        opacity: 0.5,
        type: 'scatter',
        name: 'Not changed',
        text: belowThresholdJson.texts,
        textfont: {
            family:  'Times New Roman'
        },
        textposition: 'bottom center',
        marker: {size:8, color:"gray"}
    };


    var foldValues = belowThresholdJson.logFoldChange;
    foldValues = foldValues.concat(foldChangesNegativeFoldJson.logFoldChange);
    foldValues = foldValues.concat(foldChangesPositiveFoldJson.logFoldChange);

    var foldMinimum = Math.min.apply(Math, foldValues);
    var foldMaximum = Math.max.apply(Math, foldValues);


    var logPValues = belowThreshold.logPval;
    foldValues = foldValues.concat(foldChangesNegativeFoldJson.logPval);
    foldValues = foldValues.concat(foldChangesPositiveFoldJson.logPval)

    // var pValMaximum = Math.max.apply(Math, foldValues);

    var data = [posFold, negFold, belowThreshold];
    // height=700
    // width=1500

    var layout = {
        showlegend: false,
        showtitle: false,
        width: width,
        height:height,
        plot_bgcolor: "#F4F4F4",
        paper_bgcolor: "#F4F4F4",
        hovermode:'closest',
        title: title,
        // margin: {
        //     l: 60,
        //     r: 10,
        //     b: 0,
        //     t: 10,
        //     pad: 4
        // },
        // title: {
        //     text:'sasda',
        //     font: {
        //         family: 'Courier New, monospace',
        //         size: 24
        //     },
        //     xref: 'paper',
        //     x: 0.05,
        // },
        font: {
            size: fontsize-5,
        },
        xaxis: {
            title: {
                text: '-Log2 Fold Change',
                font: {
                    family: 'Courier New, monospace',
                    // size: 18,
                    color: '#140D0C'
                }
            },
            range: [foldMinimum - 1, foldMaximum + 1]
        },
        yaxis: {
            title: {
                text: '-Log10 P-value',
                font: {
                    family: 'Courier New, monospace',
                    // size: 18,
                    color: '#140D0C'
                }
            },
            range:[0,maxPval]
        },
        shapes: [
            {
                type: 'line',
                x0: -1000,
                y0: pValThreshold,
                x1: 1000,
                y1: pValThreshold,
                line: {
                    color: 'gray',
                    width: 1,
                    dash:'dot'
                }
            },
            {
                type: 'line',
                x0: - foldChangeThreshold,
                y0: 0,
                x1: - foldChangeThreshold,
                y1: 1000,
                line: {
                    color: 'gray',
                    width: 1,
                    dash:'dot'
                }
            },
            {
                type: 'line',
                x0:  foldChangeThreshold,
                y0: 0,
                x1:  foldChangeThreshold,
                y1: 1000,
                line: {
                    color: 'gray',
                    width: 1,
                    dash:'dot'
                }
            }]
    };

    Plotly.newPlot('container', data, layout, {displayModeBar: false});

    var myPlot = document.getElementById('container')
    myPlot.on('plotly_click', function(data){

        java.callbackFromJavaScript(data.points[0].text);
    });


}


function cleanVolcano() {
    document.getElementById("container").innerHTML = "";

}



