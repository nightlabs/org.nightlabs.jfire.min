<%@ page language="java"%>
<%@ taglib uri="/tags/struts-bean" prefix="bean"%> 
<%@ taglib uri="/tags/struts-html" prefix="html"%>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>
 
<jsp:include page="../pageHeader.jsp">
	<jsp:param name="title" value="Server Initialisation - JDO" />
</jsp:include>

		<h1>JDO</h1>
		<%-- create a html form --%>
		<form action="serverinitialize">
			<table class="serverinitform" cellpadding="0" cellspacing="0" border="0">
				<tr>
					<td class="name">Deployment directory:</td>
					<td class="value"><input type="text" name="jdoDeploymentDirectory" /></td>
					<td class="annotation">(probably no change needed)</td>
				</tr>
				<tr>
					<td class="name">Deployment descriptor file:</td>
					<td class="value"><input type="text" name="jdoDeploymentDescriptorFile" /></td>
					<td class="annotation">(probably no change needed)</td>
				</tr>
				<tr>
					<td class="name">Deployment descriptor template file:</td>
					<td class="value"><input type="text" name="jdoDeploymentDescriptorTemplateFile" /></td>
					<td class="annotation">(probably no change needed)</td>
				</tr>
				<tr>
					<td class="name">Persistence configuration file:</td>
					<td class="value"><input type="text" name="jdoPersistenceConfigurationFile" /></td>
					<td class="annotation">(probably no change needed)</td>
				</tr>
				<tr>
					<td class="name">Persistence configuration template file:</td>
					<td class="value"><input type="text" name="jdoPersistenceConfigurationTemplateFile" /></td>
					<td class="annotation">(probably no change needed)</td>
				</tr>
			</table>
			<%-- set the parameter for the dispatch action --%>
			<html:hidden property="do" value="jdoSave" />	
			<br/><br/>
			<html:submit styleClass="wide">Save</html:submit>
		</form>

<jsp:include page="../pageFooter.jsp" />
