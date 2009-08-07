<%@ page language="java"%>

<h1>Ready for initialisation</h1>

<br/><br/>

<form method="post" onsubmit="showWaitBlock('Please wait while initialising server...')">

<input type="hidden" name="navigation" value="finish"/>
<input type="submit" value="Initialise Server Now!"/>

</form>

<%--
<jsp:include page="../pageHeader.jsp">
	<jsp:param name="title" value="Server Initialisation - Overview" />
</jsp:include>

		<div id="overviewblock">
		<h1>Overview</h1>
			<table class="serverinitoverview" cellpadding="0" cellspacing="0" border="0">
			<tr class="head">
				<td><h2>Local server</h2></td>
				<td>
					<form action="serverinitialize">
						<html:hidden property="do" value="localServerEdit" />	
						<html:submit>Change</html:submit>
					</form>
				</td>
			</tr>
			<tr>
				<td class="name">Server ID:</td>
				<td class="value"><bean:write name="serverInitForm" property="localServerServerID" /></td>
			</tr>
			<tr>
				<td class="name">Server Name:</td>
				<td class="value"><bean:write name="serverInitForm" property="localServerServerName" /></td>
			</tr>
			<tr>
				<td class="name">Server Type:</td>
				<td class="value"><bean:write name="serverInitForm" property="localServerServerType" /></td>
			</tr>
			<tr>
				<td class="name">Initial Context URL:</td>
				<td class="value"><bean:write name="serverInitForm" property="localServerInitialContextURL" /></td>
			</tr>
			<tr>
				<td class="name">Deploy Base Directory:</td>
				<td class="value"><bean:write name="serverInitForm" property="localServerDeployBaseDir" /></td>
			</tr>
			<tr>
				<td class="name">Server Configurator:</td>
				<td class="value"><bean:write name="serverInitForm" property="j2eeServerConfigurator" /></td>
			</tr>


			<tr><td><br/><!-- spacer --></td></tr>
			
			<tr class="head">
				<td><h2>Servlets + SSL</h2></td>
				<td>
					<form action="serverinitialize">
						<html:hidden property="do" value="servletSSLEdit" />	
						<html:submit>Change</html:submit>
					</form>
				</td>
			</tr>
			<tr>
				<td class="name">Base servlet path:</td>
				<td class="value"><bean:write name="serverInitForm" property="servletBaseURL" /></td>
			</tr>
			<tr>
				<td class="name">Https servlet path:</td>
				<td class="value"><bean:write name="serverInitForm" property="servletBaseURLHttps" /></td>
			</tr>
			<tr>
				<td class="name">SSL certificate store:</td>
				<td class="value"><bean:write name="serverInitForm" property="sslKeystoreFile" /></td>
			</tr>
			<tr>
				<td class="name">SSL certificate store password:</td>
				<td class="value">[not visible] <!-- <bean:write name="serverInitForm" property="sslKeystoreFilePassword" /> --> </td>
			</tr>
			<tr>
				<td class="name">Certificate alias used by the server:</td>
				<td class="value"><bean:write name="serverInitForm" property="sslServerCertificateAlias" /></td>
			</tr>
			<tr>
				<td class="name">Chosen SSL certificate password:</td>
				<td class="value">[not visible] <!-- <bean:write name="serverInitForm" property="sslServerCertificatePassword" /> --> </td>
			</tr>
			

			<tr><td><br/><!-- spacer --></td></tr>

			<tr>
				<td><h2>Database</h2></td>
				<td>
					<form action="serverinitialize">
						<html:hidden property="do" value="databaseEdit" />	
						<html:submit>Change</html:submit>
					</form>
				</td>
			</tr>
			<tr>
				<td class="name">Driver Name (non-transactional):</td>
				<td class="value"><bean:write name="serverInitForm" property="databaseDriverName_noTx" /></td>
			</tr>
			<tr>
				<td class="name">Driver Name (local-transactional):</td>
				<td class="value"><bean:write name="serverInitForm" property="databaseDriverName_localTx" /></td>
			</tr>
			<tr>
				<td class="name">Driver Name (xa):</td>
				<td class="value"><bean:write name="serverInitForm" property="databaseDriverName_xa" /></td>
			</tr>
			<tr>
				<td class="name">URL:</td>
				<td class="value"><bean:write name="serverInitForm" property="databaseURL" /></td>
			</tr>
			<tr>
				<td class="name">Name Prefix:</td>
				<td class="value"><bean:write name="serverInitForm" property="databaseNamePrefix" /></td>
			</tr>
			<tr>
				<td class="name">Name Suffix:</td>
				<td class="value"><bean:write name="serverInitForm" property="databaseNameSuffix" /></td>
			</tr>
			<tr>
				<td class="name">User Name:</td>
				<td class="value"><bean:write name="serverInitForm" property="databaseUserName" /></td>
			</tr>
			<tr>
				<td class="name">Password:</td>
				<td class="value">[not visible] <!-- <bean:write name="serverInitForm" property="databasePassword" /> --> </td>
			</tr>
			<tr>
				<td class="name">Adapter:</td>
				<td class="value"><bean:write name="serverInitForm" property="databaseAdapter" /></td>
			</tr>
			<tr>
				<td class="name">Type Mapping:</td>
				<td class="value"><bean:write name="serverInitForm" property="datasourceMetadataTypeMapping" /></td>
			</tr>
			<tr>
				<td class="name">Datasource Configuration File:</td>
				<td class="value"><bean:write name="serverInitForm" property="datasourceConfigFile" /></td>
			</tr>
			<tr>
				<td class="name">Datasource Configuration Template File:</td>
				<td class="value"><bean:write name="serverInitForm" property="datasourceConfigTemplateFile" /></td>
			</tr>


			<tr><td><br/><!-- spacer --></td></tr>

			<tr>
				<td><h2>JDO</h2></td>
				<td>
					<form action="serverinitialize">
						<html:hidden property="do" value="jdoEdit" />	
						<html:submit>Change</html:submit>
					</form>
				</td>
			</tr>
			<tr>
				<td class="name">Deployment directory:</td>
				<td class="value"><bean:write name="serverInitForm" property="jdoDeploymentDirectory" /></td>
			</tr>
			<tr>
				<td class="name">Deployment descriptor file:</td>
				<td class="value"><bean:write name="serverInitForm" property="jdoDeploymentDescriptorFile" /></td>
			</tr>
			<tr>
				<td class="name">Deployment descriptor template file:</td>
				<td class="value"><bean:write name="serverInitForm" property="jdoDeploymentDescriptorTemplateFile" /></td>
			</tr>
			<tr>
				<td class="name">Persistence configuration file:</td>
				<td class="value"><bean:write name="serverInitForm" property="jdoPersistenceConfigurationFile" /></td>
			</tr>
			<tr>
				<td class="name">Persistence configuration template file:</td>
				<td class="value"><bean:write name="serverInitForm" property="jdoPersistenceConfigurationTemplateFile" /></td>
			</tr>

			<tr><td><br/><!-- spacer --></td></tr>
			
			<tr>
				<td><h2>SMTP server</h2></td>
				<td>
					<form action="serverinitialize">
						<html:hidden property="do" value="smtpEdit" />	
						<html:submit>Change</html:submit>
					</form>
				</td>
			</tr>
			<tr>
				<td class="name">Username:</td>
				<td class="value"><bean:write name="serverInitForm" property="smtpUsername" /></td>
			</tr>
			<tr>
				<td class="name">Password:</td>
				<td class="value">[not visible]<!--<bean:write name="serverInitForm" property="smtpPassword" />--></td>
			</tr>
			<tr>
				<td class="name">Host:</td>
				<td class="value"><bean:write name="serverInitForm" property="smtpHost" /></td>
			</tr>
			<tr>
				<td class="name">Port:</td>
				<td class="value"><bean:write name="serverInitForm" property="smtpPort" /></td>
			</tr>
			<tr>
				<td class="name">Sender address:</td>
				<td class="value"><bean:write name="serverInitForm" property="smtpMailFrom" /></td>
			</tr>
			<tr>
				<td class="name">Encryption method:</td>
				<td class="value"><bean:write name="serverInitForm" property="smtpEncryptionMethod" /></td>
			</tr>
			<tr>
				<td class="name">Use authentication:</td>
				<td class="value"><bean:write name="serverInitForm" property="smtpUseAuthentication" /></td>
			</tr>		
			
			<tr><td><br/><!-- spacer --></td></tr>

			<tr>
				<td><h2>Root organisation</h2></td>
				<td>
					<form action="serverinitialize">
						<html:hidden property="do" value="rootOrganisationEdit" />	
						<html:submit>Change</html:submit>
					</form>
				</td>
			</tr>
			<tr>
				<td class="name">Organisation ID:</td>
				<td><bean:write name="serverInitForm" property="rootOrganisationOrganisationID" /></td>
			</tr>											
			
			<tr>
				<td class="name">Organisation Name:</td>
				<td class="value"><bean:write name="serverInitForm" property="rootOrganisationOrganisationName" /></td>
			</tr>											
			<tr>
				<td class="name">Server ID:</td>
				<td class="value"><bean:write name="serverInitForm" property="rootOrganisationServerID" /></td>
			</tr>											
			<tr>
				<td class="name">Server Name:</td>
				<td class="value"><bean:write name="serverInitForm" property="rootOrganisationServerName" /></td>
			</tr>											
			<tr>
				<td class="name">Server Type:</td>
				<td class="value"><bean:write name="serverInitForm" property="rootOrganisationServerType" /></td>
			</tr>											
			<tr>
				<td class="name">Initial Context URL:</td>
				<td class="value"><bean:write name="serverInitForm" property="rootOrganisationInitialContextURL" /></td>
			</tr>											


			<tr><td><br/><!-- spacer --></td></tr>

			<tr>
				<td><h2>Organisation</h2></td>
				<td>
					<form action="serverinitialize">
						<html:hidden property="do" value="organisationEdit" />	
						<html:submit>Change</html:submit>
					</form>
				</td>
			</tr>
			<tr>
				<td class="name">Organisation ID:</td>
				<td class="value"><bean:write name="serverInitForm" property="organisationOrganisationID" /></td>
			</tr>
			<tr>
				<td class="name">Organisation Name:</td>
				<td class="value"><bean:write name="serverInitForm" property="organisationOrganisationName" /></td>
			</tr>
			
			
			<tr><td><br/><!-- spacer --></td></tr>
			
			<tr>
				<td><h2>Admin User</h2></td>
				<td>
					<form action="serverinitialize">
						<html:hidden property="do" value="userEdit" />	
						<html:submit>Change</html:submit>
					</form>
				</td>
			</tr>
			<tr>
				<td class="name">User ID:</td>
				<td class="value"><bean:write name="serverInitForm" property="userUserID" /></td>
			</tr>
			<tr>
				<td class="name">Password:</td>
				<td class="value">[not visible]<!--<bean:write name="serverInitForm" property="userPassword" />--></td>
			</tr>
			
		</table>
		
		<br/><br/>
		<html:form action="serverInit" onsubmit="getElementById('overviewblock').style.display='none'; getElementById('waitblock').style.display='block';">
			<html:hidden property="do" value="serverInit" />	
			<br/><br/>
			<html:submit styleClass="wide">Perform initialisation</html:submit>
		</form>
		</div>
		
		<div id="waitblock" style="display: none">
			<br/><br/>
			<table align="center">
				<tr>
					<td style="padding-right: 10px;"><img src="/jfire/images/loading.gif"/></td>
					<td style="padding-left: 10px;"><strong>Please wait while performing server initialisation...</strong></td>
				</tr>
			</table>
			<br/><br/>
		</div>

<jsp:include page="../pageFooter.jsp" />

--%>

