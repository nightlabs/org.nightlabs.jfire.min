<%@page import="java.beans.PropertyDescriptor"%>
<%@page import="java.util.List"%>
<%@page import="org.nightlabs.jfire.web.admin.beaninfo.ExtendedPropertyDescriptor"%>

<table class="formtable">
<%
ExtendedPropertyDescriptor[] pds = (ExtendedPropertyDescriptor[])request.getAttribute("beanedit.propertydescriptors");
for(PropertyDescriptor pd : pds) {
	if(pd.getWriteMethod() != null && !pd.isHidden()) {
%>
<tr>
	<td><%=pd.getDisplayName()%>: </td>
	<td><input type="text" name="<%=pd.getName()%>" class="extrawide"/></td>
</tr>
<%
	}
}
%>
</table>
<input type="hidden" name="beanedit.beankey" value="<%=request.getAttribute("beanedit.beankey")%>"/>
