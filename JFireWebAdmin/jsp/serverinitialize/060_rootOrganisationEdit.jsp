<%@ page language="java"%>
<%@ taglib uri="/tags/struts-bean" prefix="bean"%> 
<%@ taglib uri="/tags/struts-html" prefix="html"%>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>
 
<jsp:include page="../pageHeader.jsp">
	<jsp:param name="title" value="Server Initialisation - Root Organisation" />
</jsp:include>

		<h1>Root Organisation</h1>
		
		<p class="description">
		A root-organisation is only necessary, if you plan to setup a network with multiple organisations.
		For a test-system, it's seldom required. Therefore it's recommended not to change anything here.
		If the root-organisation's identifier is left empty, a stand-alone-system will be set up.
		</p>
		
		<%-- create a html form --%>
		<form action="serverinitialize">
			<table class="serverinitform" cellpadding="0" cellspacing="0" border="0">
				<tr>
					<td class="name">Organisation ID:</td>
					<td class="value"><input type="text" name="rootOrganisationOrganisationID" /></td>
				</tr>
				<tr>
					<td class="name">Organisation Name:</td>
					<td class="value"><input type="text" name="rootOrganisationOrganisationName" /></td>
				</tr>
				<tr>
					<td class="name">Server ID:</td>
					<td class="value"><input type="text" name="rootOrganisationServerID" /></td>
				</tr>
				<tr>
					<td class="name">Server Name:</td>
					<td class="value"><input type="text" name="rootOrganisationServerName" /></td>
				</tr>
				<tr>
					<td class="name">Server Type:</td>
					<td class="value"><input type="text" name="rootOrganisationServerType" /></td>
				</tr>
				<tr>
					<td class="name">Initial Context URL:</td>
					<td class="value"><input type="text" name="rootOrganisationInitialContextURL" /></td>
				</tr>
			</table>
			<%-- set the parameter for the dispatch action --%>
			<html:hidden property="do" value="rootOrganisationSave" />	
			<br/><br/>
			<html:submit styleClass="wide">Save</html:submit>
		</form>

<jsp:include page="../pageFooter.jsp" />
