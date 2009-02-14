<%@ page language="java"%>
<%@ taglib uri="/tags/struts-bean" prefix="bean"%> 
<%@ taglib uri="/tags/struts-html" prefix="html"%>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>
 
<jsp:include page="../pageHeader.jsp">
	<jsp:param name="title" value="Server Initialisation - Database" />
</jsp:include>

		<h1>Database</h1>
		<%-- create a html form --%>
		<form action="serverinitialize">
			<table class="serverinitform" cellpadding="0" cellspacing="0" border="0">
				<tr>
					<td class="name">Default Values:</td>
					<td class="value">
						<html:select property="databaseDefaultKey">
							<%
							for(java.util.Iterator itDefaultKey = org.nightlabs.jfire.servermanager.config.DatabaseCf.defaults().keySet().iterator(); itDefaultKey.hasNext(); ) {
								String defaultKey = (String) itDefaultKey.next();
							%>
								<html:option value="<%=defaultKey%>" ><%=defaultKey%></html:option>
							<%
							}
							%>
						</html:select>
						<html:hidden property="do" value="databaseLoadDefaults" />
						<html:submit>Load Default Values</html:submit>
					</td>
				</tr>
			</table>
		</form>
		
		<br/>
		
		<form action="serverinitialize">
			<%-- print out the form data --%>
			<table class="serverinitform" cellpadding="0" cellspacing="0" border="0">
				<tr>
					<td class="name">Driver Name for non-transactional use:</td>
					<td class="value"><input type="text" name="databaseDriverName_noTx" /></td>
					<td class="annotation">(probably no change needed)</td>
				</tr>
				<tr>
					<td class="name">Driver Name for local-transactional use:</td>
					<td class="value"><input type="text" name="databaseDriverName_localTx" /></td>
					<td class="annotation">(probably no change needed)</td>
				</tr>
				<tr>
					<td class="name">Driver Name for xa (distributed) transactions:</td>
					<td class="value"><input type="text" name="databaseDriverName_xa" /></td>
					<td class="annotation">(probably no change needed)</td>
				</tr>
				<tr>
					<td class="name">URL:</td>
					<td class="value"><input type="text" name="databaseURL" /></td>
					<td class="annotation">(probably no change needed)</td>
				</tr>
				<tr>
					<td class="name">Name Prefix:</td>
					<td class="value"><input type="text" name="databaseNamePrefix" /></td>
					<td class="annotation">(probably no change needed)</td>
				</tr>
				<tr>
					<td class="name">Name Suffix:</td>
					<td class="value"><input type="text" name="databaseNameSuffix" /></td>
					<td class="annotation">(probably no change needed)</td>
				</tr>
				<tr>
					<td class="name">User Name:</td>
					<td class="value"><input type="text" name="databaseUserName" /></td>
				</tr>
				<tr>
					<td class="name">Password:</td>
					<td class="value"><input type="text" name="databasePassword" /></td>
				</tr>
				<tr>
					<td class="name">Adapter:</td>
					<td class="value"><input type="text" name="databaseAdapter" /></td>
					<td class="annotation">(probably no change needed)</td>
				</tr>
				<tr>
					<td class="name">Type Mapping:</td>
					<td class="value"><input type="text" name="datasourceMetadataTypeMapping" /></td>
					<td class="annotation">(probably no change needed)</td>
				</tr>
				<tr>
					<td class="name">Datasource Configuration File:</td>
					<td class="value"><input type="text" name="datasourceConfigFile" /></td>
					<td class="annotation">(probably no change needed)</td>
				</tr>
				<tr>
					<td class="name">Datasource Configuration Template File:</td>
					<td class="value"><input type="text" name="datasourceConfigTemplateFile" /></td>
					<td class="annotation">(probably no change needed)</td>
				</tr>
			</table>
			<%-- set the parameter for the dispatch action --%>
			<html:hidden property="do" value="databaseSave" />
			<br/><br/>
			<html:submit styleClass="wide">Save</html:submit>
		</form>

<jsp:include page="../pageFooter.jsp" />
