<%@page import="java.beans.PropertyDescriptor"%>
<%@page import="java.util.List"%>
<%@page import="org.nightlabs.jfire.web.admin.beaninfo.ExtendedPropertyDescriptor"%>
<%@page import="org.apache.commons.beanutils.BeanUtils"%>
<%@page import="java.util.Map"%>
<%@page import="org.nightlabs.jfire.web.admin.beaninfo.ExtendedBeanInfo"%>

<%
int beanKey = (Integer)request.getAttribute("beanedit.beankey");
Map<Integer, Object> beans = (Map<Integer, Object>)request.getSession().getAttribute("beanedit.beans");
Object bean = beans.get(beanKey);
ExtendedBeanInfo beanInfo = (ExtendedBeanInfo)request.getAttribute("beanedit.beaninfo");
ExtendedPropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
String displayName = beanInfo.getBeanDescriptor().getDisplayName();
if(displayName == null || displayName.isEmpty()) {
	displayName = bean.getClass().getSimpleName();
}
%>

<h1><%=displayName%></h1>

<%
String shortDescription = beanInfo.getBeanDescriptor().getShortDescription();
if(shortDescription != null && !shortDescription.isEmpty() && !shortDescription.equals(displayName)) {
%>
<p><%=shortDescription%></p>
<%
}
%>

<table class="formtable">
<%
for(ExtendedPropertyDescriptor pd : pds) {
	if(pd.getWriteMethod() != null && !pd.isHidden()) {
		String name = "beanedit.value."+pd.getName();
		String value = BeanUtils.getSimpleProperty(bean, pd.getName());
%>
<tr>
	<td valign="top"><%=pd.getDisplayName()%>: </td>
	<td valign="top" style="padding-left: 8px;">
		<%
		if(pd.getPropertyType() == Boolean.class || pd.getPropertyType() == Boolean.TYPE) { 
		%>
		<input type="radio" name="<%=name%>" value="true" <%if("true".equals(value)) {%> checked="checked"<%}%>> Yes
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		<input type="radio" name="<%=name%>" value="false" <%if(!"true".equals(value)) {%> checked="checked"<%}%>> No
		<%
		} else {
			List<String> values = pd.getPossibleValues();
			if(values != null && !values.isEmpty()) {
		%>
		<select name="<%=name%>">
		<%
				for(String v : values) {
		%>
			<option value="<%=v%>"<%if(v.equals(value)){%> selected="selected"<%}%>><%=pd.getPossibleValueDisplayName(v)%></option>
		<%	
				}
		%>
		</select>
		<%
			} else {
		%>
		<input type="text" name="<%=name%>" value="<%=(value==null ? "" : value)%>" class="extrawide"/>
		<%
			}
		}
		%>
	</td>
	<%
	String propertyShortDescription = pd.getShortDescription();
	if(propertyShortDescription != null && !propertyShortDescription.isEmpty() && !propertyShortDescription.equals(pd.getDisplayName())) {
	%>
	<td valign="middle" style="padding-left: 16px;"><small><i>(<%=propertyShortDescription%>)</i></small></td>
	<%
	}
	%>
	
</tr>
<%
	}
}
%>
</table>
<br/>
<input type="hidden" name="beanedit.beankey" value="<%=beanKey%>"/>
