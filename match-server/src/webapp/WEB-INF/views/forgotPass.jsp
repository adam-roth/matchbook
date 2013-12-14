<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
	<head>
		<meta name="decorator" content="preLogin" />
	</head>
	<body>
		<div class="formContainer" id="forgotPasswordFormContainer">
			<form id="forgotPasswordForm" method="POST" action="/r/submitForgotPassword">
				<div class="formRow inputRow">
					<span class="formLabel emailLabel">Email:</span>  <input type="text" class="textInput emailInput" name="email" id="emailField" value="${email}" />
				</div>
				<div class="formRow submitRow">
					<input type="submit" class="buttonInput submitButton" id="submitButton" value="Submit" />
				</div>
			</form>
		</div>
	</body>
</html>
