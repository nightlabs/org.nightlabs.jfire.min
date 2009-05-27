<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${errorMessage != null}">
<p>
${errorMessage}
</p>
</c:if>
<c:if test="${errorMessage == null}">
<p>
An error occured. See log for details.
</p>
</c:if>

