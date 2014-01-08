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
			<c:if test="${! empty libraries}">
				Current libraries:
				<c:forEach var="lib" items="${libraries}">
				<div class="libContainer">
					<span class="libText"><span class="libLabel">Id:</span>  ${lib.id}, <span class="libLabel">Name:</span>  ${lib.name}, <span class="libLabel">Token:</span> ${lib.libraryLicenseToken}</span>
				</div>
				</c:forEach>
			</c:if>
		</div>
		
		<div class="warning">
			<span class="warningLabel">Warning:</span>  <div class="warningText">These settings control the runtime behavior of the server.  Setting incorrect or invalid values may cause the 
			server to malfunction, perhaps catastrophically.  You should only modify these values if you are absolutely sure that you know what you are doing.</div>
			<div class="warningText">Note that an invalid configuration can always be corrected by using the 'Restore Defaults' option.  This will revert all settings to the original 
			values specified at server startup.</div>
		</div>
		<div class="formContainer" id="manageConfigurationFormContainer">
			<form id="manageConfigurationForm" method="POST" action="${serverHome}/a/submitManageConfiguration">
				<c:set var="index" value="0" />
				<c:forEach var="label" items="${fieldLabels}">
					<div class="formRow inputRow">
						<span class="formLabel">${label}</span>  <input type="text" class="textInput" name="${fieldNames[index]}" value="${fieldValues[index]}" />
					</div>
					<c:set var="index" value="${index + 1}" />
				</c:forEach>
				<div class="formRow inputRow rescueOption">
					<span class="formLabel">Restore Defaults</span> <input type="checkbox" name="resetConfiguration" value="true" />
				</div>
				<div class="formRow submitRow">
					<input type="submit" class="buttonInput submitButton" id="adminSubmitButton" value="Update Settings" />
				</div>
			</form>
		</div>
	</body>
</html>