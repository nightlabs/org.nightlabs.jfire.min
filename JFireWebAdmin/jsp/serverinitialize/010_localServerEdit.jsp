<%@ page language="java"%>
 
<jsp:include page="../pageHeader.jsp">
	<jsp:param name="title" value="Server Initialisation - Local server" />
</jsp:include>

		<h1>Local server</h1>
		<form action="serverinitialize">
			<table class="serverinitform" cellpadding="0" cellspacing="0" border="0">
				<tr>
					<td class="name">Server ID:</td>
					<td class="value"><input type="text" name="localServerServerID" /></td>
					<td class="annotation">(can NOT be changed after server initialisation)</td>
				</tr>
				<tr>
					<td class="name">Server Name:</td>
					<td class="value"><input type="text" name="localServerServerName" /></td>
				</tr>
				<tr>
					<td class="name">Server Type:</td>
					<td class="value"><input type="text" name="localServerServerType" /></td>
					<td class="annotation">(probably no change needed)</td>
				</tr>
				<tr>
					<td class="name">Initial Context URL:</td>
					<td class="value"><input type="text" name="localServerInitialContextURL" /></td>
					<td class="annotation">(probably no change needed)</td>
				</tr>
				<tr>
					<td class="name">Deploy Base Directory:</td>
					<td class="value"><input type="text" name="localServerDeployBaseDir" /></td>
					<td class="annotation">(probably no change needed)</td>
				</tr>
				<tr>
					<td class="name">Server Configurator:</td>
					<td class="value">
						<html:select styleClass="extrawide" property="j2eeServerConfigurator">
							<html:optionsCollection property="j2eeAvailableServerConfigurators" label="second" value="first" />
						</html:select>
					</td>
					<td class="annotation">(changing this after server initialisation can destroy your configuration)</td>
				</tr>
			</table>
			<%-- set the parameter for the dispatch action --%>
			<html:hidden property="do" value="localServerSave" />	
			<br/><br/>
			<html:submit styleClass="wide">Save</html:submit>
		</form>

<jsp:include page="../pageFooter.jsp" />
