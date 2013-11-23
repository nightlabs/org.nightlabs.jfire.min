<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h1>Organisations on this Server</h1>

<div class="organisationlist">
	<c:forEach items="${organisationCfs}" var="organisation" varStatus="organisationLoopStatus">
	<div style="margin-top: 5px; margin-bottom: 5px; border: 1px solid black; padding: 5px;" class="organisationlistitem">
	<div style="font-weight: bold; margin-bottom: 2px">${organisation.organisationName}</div>
	<div>Id: ${organisation.organisationID}</div>
	<div>Admins:
		<c:forEach items="${organisation.serverAdmins}" var="serverAdmin" varStatus="adminLoop">
			${serverAdmin}<c:if test="${!adminLoop.last}">, </c:if>
		</c:forEach>
	</div>
	</div>
	</c:forEach>
</div>

<div class="createorganisation">
<a href="createorganisation">Create New Organisation...</a>
</div>
