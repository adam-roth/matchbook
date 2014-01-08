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
			Administrative Tasks:
			<ul>
				<li><auth:link method="listUsers" context="a" id="listUsersLink" label="Manage Users" /></li>
				<li><auth:link method="manageConfiguration" context="a" id="manageConfigLink" label="Manage Server Settings" /></li>
			</ul>
		</div>
	</body>
</html>
