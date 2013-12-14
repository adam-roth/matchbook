<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="method" required="true" %>
<%@ attribute name="target" required="false" %>
<%@ attribute name="params" required="false" %>
<%@ attribute name="label" required="false" %>
<%@ attribute name="context" required="false" %>
<%@ attribute name="onclick" required="false" %>
<c:set var="linkOnclick" value="" />
<c:if test="${! empty onclick}">
	<c:set var="linkOnclick" value="onclick=&quot;${onclick}&quot;" />
</c:if>
<c:if test="${! empty params}">
	<c:set var="params" value="?${params}" />
</c:if>
<c:if test="${ empty context}">
	<c:set var="context" value="r" />
</c:if>
<a id="${id}" target="${target}" href="${serverHome}/${context}/${method}${params}" ${linkOnclick}>${label}</a>