<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="auth" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
	<head>
		<meta name="decorator" content="preLogin" />
	</head>
	<body>
		<auth:loginForm />
		
		<div class="registerContainer">
			<span class="registerText">Don't have an account?  <auth:link id="registerLink" method="register" label="${messages['text.click.here.label']}"/> to register.</span>
		</div>
		<div class="passwordResetContainer">
			<span class="passwordResetText">Forgot your password?  <auth:link method="forgotPassword" id="passwordResetLink" label="${messages['text.click.here.label']}" /> to reset.</span>
		</div>
	</body>
</html>
