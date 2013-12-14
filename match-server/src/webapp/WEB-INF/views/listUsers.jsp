<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="auth" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
	<head>
		<meta name="decorator" content="postLogin" />
	</head>
	<body>
		<div class="mainContent">
			Listing all users:
			
			<c:forEach var="user" items="${users}">
			<div class="userContainer">
				<span class="userText"><span class="userLabel">Id:</span>  ${user.id}, <span class="userLabel">Status:</span>  ${user.status.name}, <span class="userLabel">Name:</span>
				${user.name}, <span class="userLabel">Email:</span>  ${user.email}
				<c:if test="${user.trustLevel lt 10000000}">
					<span class="promoteLink"><auth:link method="promoteUserToAdmin" params="userId=${user.id}" context="a" id="promoteUserLink" label="Promote to Admin" /></span>
				</c:if>
				<c:if test="${user.trustLevel ge 10000000}">
					<span class="promotedUser">Admin</span>
				</c:if>
			</div>
			</c:forEach>
		</div>
	</body>
</html>
