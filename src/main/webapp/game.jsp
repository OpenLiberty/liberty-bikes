<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	  <title>Web Based Tron</title>
	  <script src="//code.jquery.com/jquery-1.11.0.min.js"></script>
	  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.1/css/bootstrap.min.css">
</head>
<body style="background-image:url('images/tron_grid.jpg')">
	<br><br><br>
	<div class="col-md-offset-2 col-md-7">
		<div class="panel panel-primary">
			<div class="panel-heading"><h1>Web Based Tron</h1></div>
			<div class="panel-body">
				<div class="col-md-8" style="display:inline-block">
					<canvas id="myCanvas" width="600" height="600" style="border:1px solid #000000; background:url('images/tron_floor.jpg')"></canvas><br><br>
				</div>
				<ul id="playerList" class='list-group col-md-4 ' style="display:inline-block">
				</ul>
			</div>
			<div class="panel-footer">
				<button type="button" class="btn btn-success" onclick="startGame()">Start Game</button>
				<button type="button" class="btn btn-danger" onclick="pauseGame()">Pause Game</button>
				<button type="button" class="btn btn-warning" onclick="requeue()">Requeue</button>
			</div>
		</div>
	</div>
	<div id="output"></div>
	<script type="text/javascript" src="websocket.js"></script>
	<script type="text/javascript" src="whiteboard.js"></script>
</body>
</html>