<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${empty match}">
	{"status":"error", "message":"${error}"}
</c:if>
<c:if test="${! empty match}">
	{"status":"success", "id":"${match.id}", "addr":"${match.deviceAddress}", "localAddr":"${match.deviceLocalAddress}", "token":"${match.deviceToken}", "port":"${match.devicePort}"}
</c:if>
