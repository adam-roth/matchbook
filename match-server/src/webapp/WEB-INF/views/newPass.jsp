<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="auth" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
	<head>
		<meta name="decorator" content="preLogin" />
	</head>
	<body>		
		<div class="formContainer" id="registerFormContainer">
			<form id="registerForm" method="POST" action="/r/submitNewPassword" onsubmit="return verifyPassword();">
				<div class="formRow inputRow">
					<span class="formLabel emailLabel">Email:</span>  <span class="readOnlyFormText">${email}</span>
				</div>
				<div class="formRow inputRow">
					<span class="formLabel passwordLabel">Pass:</span>  <input type="password" class="textInput passwordInput" name="pass" id="passwordField" onkeyup="chkPass(this.value);" />
					<auth:passwordScore id="resetPasswordScore" />
				</div>
				<div class="formRow inputRow">
					<span class="formLabel passwordConfirmLabel">Confirm Pass:</span>  <input type="password" class="textInput passwordConfirmInput" name="conf" id="passwordConfirmField" />
				</div>
				<div class="formRow submitRow">
					<input type="submit" class="buttonInput submitButton" id="submitButton" value="Submit" />
				</div>
				<input type="hidden" name="email" value="${email}" />
				<input type="hidden" name="userId" value="${userId}" />
				<input type="hidden" name="token" value ="${token}" />
			</form>
		</div>
	</body>
</html>
