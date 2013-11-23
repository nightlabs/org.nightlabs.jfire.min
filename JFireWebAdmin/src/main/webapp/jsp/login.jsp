<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${errorMessage != null}">
<p>
Error: ${errorMessage}
</p>
</c:if>

<h1>Login</h1>

<form method="post">
<table class="formatable">
<tr><td>Organisation Id:</td><td><input type="text" name="organisationId" class="extrawide"/></td></tr>
<tr><td>Username:</td><td><input type="text" name="username" class="extrawide"/></td></tr>
<tr><td>Password:</td><td><input type="password" name="password" class="extrawide"/></td></tr>
</table>
<c:if test="${redirect != null}">
<input type="hidden" name="redirect" value="${redirect}"/>
</c:if>
<input type="submit" value="Login" class="wide"/>
</form>
