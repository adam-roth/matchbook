<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="auth" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
	<head>
		<meta name="decorator" content="preLogin" />
	</head>
	<body>		
		<auth:registerForm />
		<div class="loginContainer">
			<span class="registerText">Already have an account?  <auth:link id="loginLink" method="login" label="Click here"/> to login.</span>
		</div>
	</body>
</html>
