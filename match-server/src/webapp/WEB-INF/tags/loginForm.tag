<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="formContainer" id="loginFormContainer">
	<form id="loginForm" method="POST" action="/r/submitLogin">
		<div class="formRow inputRow">
			<span class="formLabel emailLabel">eMail</span>  <input type="text" class="textInput emailInput" name="email" id="emailField" value="${email}" />
		</div>
		<div class="formRow inputRow">
			<span class="formLabel passwordLabel">Password</span>  <input type="password" class="textInput passwordInput" name="pass" id="passwordField" />
		</div>
		<div class="formRow checkboxRow submitRow">
			<c:set var="checked" value="" />
			<c:if test="${! empty remember}">
				<c:set var="checked" value="checked='true'" />
			</c:if>
			<span class="rememberMeContainer"><input type="checkbox" class="checkboxInput rememberMeInput" name="remember" id="rememberBox" ${checked} /><span class="formLabel rememberMeLabel">Remember Me</span></span>
			<input type="submit" class="buttonInput submitButton" id="loginSubmitButton" value="Log In" />
		</div>
		<c:if test="${! empty nextUrl}">
			<input type="hidden" name="nextUrl" value="${nextUrl}" />
		</c:if>
	</form>
</div>