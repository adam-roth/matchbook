<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${! empty pageName}">
	<title>Matchbook - ${pageName}</title>
</c:if>
<c:if test="${empty pageName}">
	<title>Matchbook</title>
</c:if>