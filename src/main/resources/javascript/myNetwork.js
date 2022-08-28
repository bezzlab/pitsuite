// create an array with nodes


function genNetwork(nodes, edges, height) {


  var nodes = new vis.DataSet(nodes);



  var edges = new vis.DataSet(edges);

  // create a network
  var container = document.getElementById("mynetwork");
  var data = {
    nodes: nodes,
    edges: edges,
  };
  var options = {
    autoResize: true,
    height: height,
    width: '100%',
    layout: {
      hierarchical: {
        direction: 'UD'
      }
    }
  };

  var network = new vis.Network(container, data, options);
  network.setOptions(options);

  // network.on( 'click', function(properties) {
  //   var ids = properties.nodes;
  //   var clickedNodes = nodes.get(ids);
  //
  //   java.callbackFromJavaScript(JSON.stringify(clickedNodes[0]));
  // });
  //
  // network.on("doubleClick", function(properties) {
  //   var ids = properties.nodes;
  //   var clickedNodes = nodes.get(ids);
  //
  //   java.browseGene(JSON.stringify(clickedNodes[0]));
  //
  // });



}
