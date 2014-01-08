<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="output" value="" />
<c:forEach var="user" items="${users}">
	<c:if test="${! empty showComma}">
		<c:set var="output" value="${output}, " /> 
	</c:if>
	<c:set var="output" value="${output}${user.jsonString}" />
	<c:set var="showComma" value="yes" />
</c:forEach>
[${output}]