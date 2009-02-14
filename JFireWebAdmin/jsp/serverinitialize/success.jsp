<%@ page language="java"%>
<%@ taglib uri="/tags/struts-bean" prefix="bean"%> 
<%@ taglib uri="/tags/struts-html" prefix="html"%>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>
 
<jsp:include page="../pageHeader.jsp">
	<jsp:param name="title" value="Server Initialisation - Success" />
</jsp:include>

		<h1>Server initialisation was successful!</h1>
		<br/><br/>
		<a href="/jfire/">Login now!</a>

<jsp:include page="../pageFooter.jsp" />
