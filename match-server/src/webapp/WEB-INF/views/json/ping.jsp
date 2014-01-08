<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${empty error}">
	{"status":"success"}
</c:if>
<c:if test="${! empty error}">
	{"status":"error", "message":"${error}"}
</c:if>
