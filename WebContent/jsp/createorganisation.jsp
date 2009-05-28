<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h1>Create a new Organisation</h1>

<form method="post" onsubmit="showWaitBlock('Creating organisation - please stand by')">
<table class="formtable">
<tr><td>Organisation Id:</td><td><input type="text" name="organisationId" class="extrawide"/></td></tr>
<tr><td>Organisation Display Name:</td><td><input type="text" name="organisationName" class="extrawide"/></td></tr>
<tr><td>Admin Username:</td><td><input type="text" name="username" class="extrawide"/></td></tr>
<tr><td>Admin Password:</td><td><input type="password" name="password" class="extrawide"/></td></tr>
<tr><td>Admin Password (again):</td><td><input type="password" name="password2" class="extrawide"/></td></tr>
<tr><td>Server Admin:</td><td><input type="checkbox" name="serveradmin" value="true"/> Make this user also a Server Administrator</td></tr>
</table>
<input type="hidden" name="action" value="createorganisation"/>
<input type="submit" value="Create Organisation" class="wide"/>
</form>
