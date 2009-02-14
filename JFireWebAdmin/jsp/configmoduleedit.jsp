<%@page import="java.beans.PropertyDescriptor"%>
<%@page import="java.util.List"%>

<table class="formtable">
<%
List<PropertyDescriptor> pds = (List<PropertyDescriptor>)request.getAttribute("propertydescriptors");
for(PropertyDescriptor pd : pds) {
	if(pd.getWriteMethod() != null && !pd.isHidden()) {
%>
<tr><td><%=pd.getDisplayName()%>: </td><td><input type="text" name="<%=pd.getName()%>" class="extrawide"/></td></tr>
<%
	}
}
%>
</table>
