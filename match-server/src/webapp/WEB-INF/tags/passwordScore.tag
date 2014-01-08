<%@ taglib tagdir="/WEB-INF/tags" prefix="auth" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ attribute name="id" required="true" %>
<!-- FIXME:  should localize the strings used by the password score widget -->
<div class="passwordScore" id="${id}">
	<div id="scorebarBorder">
        <div id="score">0%</div>
        <div id="scorebar">&nbsp;</div>
        <div id="complexity">Too Short</div>
    </div>
</div>