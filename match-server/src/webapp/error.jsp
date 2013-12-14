<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page isELIgnored="false" %>
<%@ page isErrorPage="true" import="java.io.*"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
	<meta name="decorator" content="preLogin" />
</head>
<body>
	<!-- Get the exception object -->
	<c:set var="ex" value='${requestScope["javax.servlet.error.exception"]}'/>
	<c:set var="code" value='${requestScope["javax.servlet.error.status_code"]}' />

	<!-- Exception message(s) -->	
	Something is broken (code=${code}):  ${ex.message}
	
	<c:if test="${! empty ex.cause.message}">
		<p>${ex.cause.message}</p>
	</c:if>
	
	<!-- Stack trace -->
	<pre>
<% 
// if there is an exception, print it
if (exception != null) {
exception.printStackTrace(new PrintWriter(out));
}
%>
	</pre>
</body>
</html>
