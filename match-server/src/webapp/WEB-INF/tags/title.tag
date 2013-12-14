<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${! empty pageName}">
	<title>iApps - ${pageName}</title>
</c:if>
<c:if test="${empty pageName}">
	<title>iApps</title>
</c:if>