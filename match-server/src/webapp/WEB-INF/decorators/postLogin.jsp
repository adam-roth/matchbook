<%@ page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="auth" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator"	prefix="decorator"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
	<head>
		<meta http-equiv="content-type" content="text/html; charset=utf-8" />
		<!-- set title, include common CSS and javascript  files here -->
		<auth:title />
		
		<!-- the next line includes the <head> section from the page being rendered -->
		<decorator:head /> 
	</head>
	
	<body>
		<!-- define common page elements here (nav menus, headers, footers, etc.) -->
		
		
		<!-- reusable container for various status messages -->
		<div class="messageContainer">
			<c:if test="${! empty error}"> 
				<div class="errorContainer">
					<span class="errorLabel">Error:</span>  <span class="errorText">${error}</span>
				</div>
			</c:if>
			<c:if test="${! empty message}"> 
				<div class="messageContainer">
					<span class="messageText">${message}</span>
				</div>
			</c:if>
		</div>
		<!-- the next line includes the <body> section from the page being rendered -->
		<decorator:body />
		
		<!-- Admin link -->
		<c:if test="${user.trustLevel >= 10000000}">
			<div class="adminContainer">
				<span class="adminText"><auth:link id="adminLink" method="adminLinks" context="a" label="Admin"/></span> | 
			</div>
		</c:if>
		
		<!-- logout link for all post-login pages -->
		<div class="logoutContainer">
			<span class="logoutText"><auth:link id="logoutLink" method="logout" label="Click here"/> to log-out.</span>
		</div>
	</body>
</html> 
		