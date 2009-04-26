<%@ page language="java"%>
<%@ taglib uri="/tags/struts-bean" prefix="bean"%> 
<%@ taglib uri="/tags/struts-html" prefix="html"%>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>
 
<jsp:include page="../pageHeader.jsp">
	<jsp:param name="title" value="Server Initialisation - SMTP" />
</jsp:include>

		<h1>SMTP server</h1>
		<%-- create a html form --%>
		<form action="serverinitialize">
			<%-- print out the form data --%>
			<table class="serverinitform" cellpadding="0" cellspacing="0" border="0">
				<tr>
					<td class="name">Username:</td>
					<td class="value"><input type="text" name="smtpUsername" /></td>
				</tr>
				<tr>
					<td class="name">Password:</td>
					<td class="value"><html:password styleClass="extrawide" property="smtpPassword" /></td>
				</tr>
				<tr>
					<td class="name">Host:</td>
					<td class="value"><input type="text" name="smtpHost" /></td>
				</tr>
				<tr>
					<td class="name">Port (leave empty for default):</td>
					<td class="value"><input type="text" name="smtpPort" /></td>
				</tr>
				<tr>
					<td class="name">Sender address:</td>
					<td class="value"><input type="text" name="smtpMailFrom" /></td>
				</tr>
				<tr>
					<td class="name">Encryption method:</td>
					<td>
					<html:select property="smtpEncryptionMethod">
						<html:option value="none" >none</html:option>
						<html:option value="ssl" >ssl</html:option>
					</html:select>
					</td>					
				</tr>
				<tr>
					<td class="name">Use authentication:</td>
					<td class="value"><html:checkbox property="smtpUseAuthentication" /></td>
				</tr>
			</table>
			<%-- set the parameter for the dispatch action --%>
			<html:hidden property="do" value="smtpSave" />
			<br/><br/>
			<html:submit styleClass="wide">Save</html:submit>
		</form>

<jsp:include page="../pageFooter.jsp" />
