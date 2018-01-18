var wsUri = "ws://" + document.location.hostname + ":" + document.location.port + "/websocket";
var websocket = new WebSocket(wsUri);
websocket.binaryType = "arraybuffer";
var output = document.getElementById("output");
websocket.onmessage = function(evt) { onMessage(evt); };
websocket.onerror = function(evt) { onError(evt); };
websocket.onopen = function(evt) { onConnect(evt); };

function sendText(json) {
    console.log("sending text: " + json);
    websocket.send(json);
}

function sendBinary(bytes) {
    console.log("sending binary: " + Object.prototype.toString.call(bytes));
    websocket.send(bytes);
}

function onMessage(evt) {
    console.log("received: " + evt.data);
    if (typeof evt.data == "string") {
    	var json = JSON.parse(evt.data);
    	if(json.playerlist){
    		updatePlayerList(json);
    	}else if(json.requeue) {
    		location.reload();
    	}else {
    		drawImageText(evt.data);
		}
    } else {
        drawImageBinary(evt.data);
    }
}

function onError(evt) {
    writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
}

function onConnect(evt){
	var name = localStorage.getItem("username");
	sendText(JSON.stringify({"playerjoined":name}));
}

function writeToScreen(message) {
    var pre = document.createElement("p");
    pre.style.wordWrap = "break-word";
    pre.innerHTML = message;
    output.appendChild(pre);
}
