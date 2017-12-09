<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>Tron</title>
		<script src="//code.jquery.com/jquery-1.11.0.min.js"></script>
		<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.1/css/bootstrap.min.css">
	</head>
	<body>
		<h1>Tron Login</h1>
		 <div class="col-md-offset-1">
			 Enter Username:
		 	<input type="text" id="username" name="username"></input>
		 	<input type="submit" class="btn btn-success" value="Login" onclick="storeName()">
		 </div>
	</body>
<script type="text/javascript">
function storeName(){
	localStorage.setItem("username", $("#username").val());
	location.href = "game.jsp";
}
</script>
</html>