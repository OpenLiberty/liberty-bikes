var canvas = document.getElementById("myCanvas");
var context = canvas.getContext("2d");
            
function getCurrentPos(evt) {
    var rect = canvas.getBoundingClientRect();
    return {
        x: evt.clientX - rect.left,
        y: evt.clientY - rect.top
    };
}
            
function defineImage(evt) {
    var currentPos = getCurrentPos(evt);
    defineImageAtCoords(currentPos.x, currentPos.y);
}

function defineImageAtCoords(x, y){
    for (i = 0; i <document.inputForm.color.length; i++) {
        if (document.inputForm.color[i].checked) {
            var color = document.inputForm.color[i];
            break;
        }
    }
            
    for (i = 0; i < document.inputForm.shape.length; i++) {
        if (document.inputForm.shape[i].checked) {
            var shape = document.inputForm.shape[i];
            break;
        }
    }
    
    var json = JSON.stringify({
        "shape": shape.value,
        "color": color.value,
        "coords": {
            "x": x,
            "y": y
        }
    });
    drawImageText(json);
    if (document.getElementById("instant").checked) {
        sendText(json);
    }
}

function defineImageBinary() {
    var image = context.getImageData(0, 0, canvas.width, canvas.height);
    var buffer = new ArrayBuffer(image.data.length);
    var bytes = new Uint8Array(buffer);
    for (var i=0; i<bytes.length; i++) {
        bytes[i] = image.data[i];
    }
    sendBinary(buffer);
}

function drawImageText(image) {
    var json = JSON.parse(image);
    context.fillStyle = json.color;
    switch (json.shape) {
    case "circle":
        context.beginPath();
        context.arc(json.coords.x, json.coords.y, 5, 0, 2 * Math.PI, false);
        context.fill();
        break;
    case "square":
    default:
        context.fillRect(json.coords.x, json.coords.y, 5, 5);
        break;
    }
}

function drawImageBinary(blob) {
    var bytes = new Uint8Array(blob);
    
    var imageData = context.createImageData(canvas.width, canvas.height);
    
    for (var i=8; i<imageData.data.length; i++) {
        imageData.data[i] = bytes[i];
    }
    context.putImageData(imageData, 0, 0);
}

function updatePlayerList(json){
	var list = "<li class='list-group-item active'>Players</li>";
	for(i in json.playerlist){
		var player = json.playerlist[i];
		list += "<li class='list-group-item'><font color='" + player.color + "' size='5'>" + player.name + "</font>  :  " + getStatus(player.status) + "</li>";
	}
	$("#playerList").html(list);
}

function getStatus(status){
	if(status === "Connected")
		return "<span class='label label-primary'>Connected</span>";
	if(status === "Alive" || status === "Winner")
		return "<span class='label label-success'>"+status+"</span>";
	if(status === "Dead")
		return "<span class='label label-danger'>Dead</span>";
	if(status === "Disconnected")
		return "<span class='label label-default'>Disconnected</span>";
}

function startGame(){
	sendText(JSON.stringify({"message":"GAME_START"}));
}

function pauseGame(){
	sendText(JSON.stringify({"message":"GAME_PAUSE"}));
}

function requeue(){
	sendText(JSON.stringify({"message":"GAME_REQUEUE"}));
}

window.onkeydown = function(e) {
    var key = e.keyCode ? e.keyCode : e.which;
    
    if(key == 38)
    	sendText(JSON.stringify({"direction":"UP"}));
    else if(key == 40)
    	sendText(JSON.stringify({"direction":"DOWN"}));
    else if(key == 37)
    	sendText(JSON.stringify({"direction":"LEFT"}));
    else if(key == 39)
    	sendText(JSON.stringify({"direction":"RIGHT"}));
}