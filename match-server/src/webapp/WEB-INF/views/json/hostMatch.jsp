<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${empty match}">
	{"status":"error", "message":"${error}"}
</c:if>
<c:if test="${! empty match}">
	{"status":"success", "matchId":"${match.id}", "password":"${match.password}"}
</c:if>
