<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
	<head>
		<meta name="decorator" content="postLogin" />
	</head>
	<body>
		<div class="mainContent">
			<div class="userStats">
				Login successful, you are [id=${user.id}, email=${user.email}, hashedPassword=${user.hashedPassword}, salt=${user.salt}]!
			</div>
			<div class="gameList">
				<c:forEach var="game" items="${user.games}">
					<div class="gameRow">
						${game.appIdentifier} = ${game.privateKey}
					</div>
				</c:forEach>
			</div>
			<div class="addGame">
				<form method="POST" action="/u/addGame">
					<input type="text" value="" name="name" />
					<input type="submit" value="Add Game" />
				</form>
			</div>
		</div>
	</body>
</html>
