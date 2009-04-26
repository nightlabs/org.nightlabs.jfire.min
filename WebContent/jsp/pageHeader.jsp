<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:url value="/" var="url_home"/>
<c:url value="/css" var="url_css"/>
<c:url value="/img" var="url_img"/>
<?xml version="1.0" encoding="iso-8859-1"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<c:if test="${title != null}">
		<title>${title}</title>
		</c:if>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<link rel="StyleSheet" href="${url_css}/jfire.css" type="text/css"/>
		<script type="text/javascript">
		<!--
		function showWaitBlock(message)
		{
			document.getElementById('allcontents').style.display = "none";
			document.getElementById('waitblock').style.display = "block";
			if(message != null)
				document.getElementById('waitblockmessage').innerHTML = message;
		}
		-->
		</script>
	</head>
	<body>
		<div class="pagehead" style="border-top: 1px solid #dddddd; border-bottom: 1px solid #dddddd; margin-bottom: 30px;">
			<img src="${url_img}/jfire-logo.jpg" />
		</div>

		<div id="waitblock" style="display: none; padding: 32px;">
			<img id="waitblockimage" src="${url_img}/loading.gif"/>
			<div id="waitblockmessage"></div>
		</div>

		<div id="allcontents">		
		<c:if test="${fn:length(internal_errors) > 0}">
		<div class="errorblock">
		Errors:
		<ul>
		<c:forEach items="${internal_errors}" var="error">
		<li><c:if test="${error.localizedMessage == '' || error.localizedMessage == null}">${error.class.simpleName} </c:if>${error.localizedMessage}</li>
		</c:forEach>
		</ul>
		</div>
		</c:if>
		
		<div class="page" style="background-image:url(${url_img}/jfire-grey.gif); background-position:right top; background-repeat:no-repeat; padding-bottom: 60px">
		<!-- END OF pageHeader.jsp -->	

