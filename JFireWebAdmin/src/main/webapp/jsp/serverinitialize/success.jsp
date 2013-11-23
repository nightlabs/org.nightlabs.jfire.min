<%@ page language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

		<h1>Server initialisation was successful!</h1>
		<br/><br/>
		<c:url var="loginUrl" value="/"/>
		<a href="${loginUrl} }">Login now!</a>

