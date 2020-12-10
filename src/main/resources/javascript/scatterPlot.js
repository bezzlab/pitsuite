function displayPlot(x, y, names, width, height, title, fontsize){
    var trace1 = {
        x: x,
        y: y,
        mode: 'markers',
        type: 'scatter',
        name: 'Team A',
        text: names,
        marker: { size: 12 }
    };

    var data = [ trace1 ];

    var layout = {
        xaxis: {
            range: [ Math.min(x), Math.max(x) ]
        },
        yaxis: {
            range: [Math.min(y), Math.max(y)]
        },
        title: title,
        plot_bgcolor: "#F4F4F4",
        paper_bgcolor: "#F4F4F4",
        hovermode:'closest',
        width: width,
        height:height,
        font: {
            size: fontsize-5,
        }
    };

    Plotly.newPlot('container', data, layout, {displayModeBar: false});

    var myPlot = document.getElementById('container')
    myPlot.on('plotly_click', function(data){
        java.callbackFromJavaScript(data.points[0].text);
    });

}