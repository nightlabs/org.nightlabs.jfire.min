<%@ page language="java"%>
<%@ taglib uri="/tags/struts-bean" prefix="bean"%> 
<%@ taglib uri="/tags/struts-html" prefix="html"%>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>
 
<jsp:include page="../pageHeader.jsp">
	<jsp:param name="title" value="Server Initialisation - Admin User" />
</jsp:include>

		<h1>Admin User</h1>
		<%-- create a html form --%>
		<form action="serverinitialize">
			<table class="serverinitform" cellpadding="0" cellspacing="0" border="0">
				<tr>
					<td class="name">User ID:</td>
					<td class="value"><input type="text" name="userUserID" /></td>
				</tr>
				<tr>
					<td class="name">Password:</td>
					<td class="value"><html:password styleClass="extrawide" property="userPassword" /></td>
					<td class="annotation">(default password for user "francois" is "test")</td>
				</tr>
			</table>
			<%-- set the parameter for the dispatch action --%>
			<html:hidden property="do" value="userSave" />	
			<br/><br/>
			<html:submit styleClass="wide">Save</html:submit>
		</form>

<jsp:include page="../pageFooter.jsp" />
