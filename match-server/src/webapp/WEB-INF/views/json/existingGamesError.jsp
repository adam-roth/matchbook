<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="output" value="" />
<c:forEach var="game" items="${games}">
	<c:if test="${! empty showComma}">
		<c:set var="output" value="${output}, " /> 
	</c:if>
	<c:set var="output" value='${output}${game.id}' />
	<c:set var="showComma" value="yes" />
</c:forEach>
{"status":"error", "message":"You are already hosting one or more games; you must cancel them before you can host a new game", "games":[${output}]}