<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

		<h1>Server initialisation partially done and restart required!</h1>
		<br/><br/>
		The server is currently shutting down, because its configuration was modified in a way that requires
		a restart!
		<br/><br/>
		You have to start it again manually, if you don't have a watchdog (service monitor) installed.
		<br/><br/>
		Your desired organisation was <b>not</b> yet created! It will be created when the server starts the next time.
		<br/><br/>
		<c:url var="loginUrl" value="/"/>
		<a href="${loginUrl}">Login here (after you started your server again)</a>

