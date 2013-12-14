<%@ taglib tagdir="/WEB-INF/tags" prefix="auth" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="formContainer" id="registerFormContainer">
	<form id="registerForm" method="POST" action="/r/submitRegister" onsubmit="return verifyPassword();">
		<div class="formRow inputRow">
			<span class="formLabel nameLabel">Name</span>  <input type="text" class="textInput nameInput" name="name" id="nameField" value="${name}" />
		</div>
		<div class="formRow inputRow">
			<span class="formLabel emailLabel">eMail</span>  <input type="text" class="textInput emailInput" name="email" id="emailField" value="${email}" />
		</div>
		<div class="formRow inputRow">
			<span class="formLabel passwordLabel">Password</span>  <input type="password" class="textInput passwordInput" name="pass" id="passwordField" onkeyup="chkPass(this.value);" />
			<auth:passwordScore id="resetPasswordScore" />
		</div>
		<div class="formRow inputRow">
			<span class="formLabel passwordConfirmLabel">Verify</span>  <input type="password" class="textInput passwordConfirmInput" name="conf" id="passwordConfirmField" />
		</div>
		<div class="formRow submitRow">
			<input type="submit" class="buttonInput submitButton" id="registerSubmitButton" value="Register" />
		</div>
	</form>
</div>