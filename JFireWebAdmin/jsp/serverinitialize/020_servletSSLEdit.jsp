<%@ page language="java"%>
<%@ taglib uri="/tags/struts-bean" prefix="bean"%> 
<%@ taglib uri="/tags/struts-html" prefix="html"%>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>
 
<jsp:include page="../pageHeader.jsp">
	<jsp:param name="title" value="Server Initialisation - Servlet Container + SSL" />
</jsp:include>

		<h1>Servlet Container + SSL</h1>
		<%-- create a html form --%>
		<form action="serverinitialize">
			<table class="serverinitform" cellpadding="0" cellspacing="0" border="0">
				<tr>
					<td class="name">Base servlet path:</td>
					<td class="value"><input type="text" name="servletBaseURL" /> </td>
					<td class="annotation">The base URL to all servlets (including port if necessary).</td>
				</tr>
				<tr>
					<td class="name">Https servlet path:</td>
					<td class="value"><input type="text" name="servletBaseURLHttps" /> </td>
					<td class="annotation">The Https base URL to securely reach the servlets.</td>
				</tr>
				<tr>
					<td class="name">SSL certificate store:</td>
					<td class="value"><html:text  styleClass="extrawide" property="sslKeystoreFile"/> </td>
					<td class="annotation">The keystore file that contains the given alias described below.<br/>
						 <b>The location of the file has to be specified as an URL! </b><br/>
						 Only change this if you want your server to be publicly available.
						</td>
				</tr>
				<tr>
					<td class="name">SSL certificate store password:</td>
					<td class="value"><html:text  styleClass="extrawide" property="sslKeystoreFilePassword"/> </td>
					<td class="annotation">The password for the keystore.</td>
				</tr>
				<tr>
					<td class="name">Certificate alias used by the server:</td>
					<td class="value"><input type="text" name="sslServerCertificateAlias" /> </td>
					<td class="annotation">The alias of the server certificate that shall be used to for SSL
						encryption and which is contained in the keystore file given above.</td>
				</tr>
				<tr>
					<td class="name">Chosen SSL certificate password:</td>
					<td class="value"><html:text  styleClass="extrawide" property="sslServerCertificatePassword"/> </td>
					<td class="annotation">The password of the private certificate. 
						This has to be the same as the certificate store password, due to tomcat limitations of
						the connector definitions.</td>
				</tr>
			</table>
			<%-- set the parameter for the dispatch action --%>
			<html:hidden property="do" value="servletSSLSave" />	
			<br/><br/>
			<html:submit styleClass="wide">Save</html:submit>
		</form>

<jsp:include page="../pageFooter.jsp" />
