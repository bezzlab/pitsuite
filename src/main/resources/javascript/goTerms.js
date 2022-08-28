function getNodes(selectedId, inputIdName){
    var idNameArray = inputIdName;

    var nodes = new Array;
    let counter = 0;
    for (let [key, value] of Object.entries(idNameArray)){
        if (key == selectedId) {
            nodes[counter] = {id: key, label: `<b><code> ${key} </code></b> \n ${value.slice(0,1).toUpperCase() + value.slice(1)}`, font: {multi: true}, color: {background: 'khaki'}};
        } else {
            nodes[counter] = {id: key, label: `<b><code> ${key} </code></b> \n ${value.slice(0,1).toUpperCase() + value.slice(1)}`, font: {multi: true},};
        }
        counter ++;
    };
    return nodes;
}

function getEdges(inputIsA) {
    var isAObject = inputIsA;
    let relations = new Array;

    for(let [key, value] of Object.entries(isAObject)){
        for (var j = 0; j < value.length; j++) {
            relations.push({from: key, to: value[j]});
        };
    };
    return relations;
}


function cleanNetwork( width, height) {
    resize(width, height);
    document.getElementById("mynetwork").innerHTML = "";
}

function resize(width, height){
    var widthString = width.toString().concat("px");
    var heightString = height.toString().concat("px");
    document.getElementById("mynetwork").style.width = widthString;
    document.getElementById("mynetwork").style.height = heightString;
}

function plotNetwork(selectedId, inputIdName, inputIsA,  width, height, fontsize){
    let nodes = getNodes(selectedId, inputIdName);
    let  edges = getEdges(inputIsA);

    resize(width, height);

    // create the network
    var container = document.getElementById("mynetwork");
    var data = {
        nodes: nodes,
        edges: edges
    };
    var options = {
        layout: {
            hierarchical: {
                sortMethod: "directed",
                shakeTowards: "roots",
            },
        },
        nodes: {
            color: {
                background: 'white',
            },
            widthConstraint:
                { minimum: 100, maximum: 150,},
            shape: "box",
            font: {
                bold: {
                    color: "#0077aa"
                }
            }
        },
        edges: {
            smooth: true,
            arrows: { to: true }
        }
    };

    network = new vis.Network(container, data, options);
    network.on('doubleClick', function (properties) {
        var nodeID = properties.nodes[0];
        if (nodeID) {
            var clickedNode = this.body.nodes[nodeID];
            sendToJava(clickedNode.options.id);
        }
    });
}


function sendToJava (nodeId) {
    java.callbackFromJavaScript(nodeId);
};

