<%@page import="java.beans.PropertyDescriptor"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="org.apache.commons.beanutils.BeanUtils"%>
<%@page import="org.nightlabs.jfire.web.admin.beaninfo.ExtendedPropertyDescriptor"%>
<%@page import="org.nightlabs.jfire.web.admin.beaninfo.ExtendedBeanInfo"%>
<%@page import="org.nightlabs.jfire.web.admin.servlet.BeanEditServlet"%>

<%
	int beanKey = (Integer) request
			.getAttribute(BeanEditServlet.BEANKEY_ATTRIBUTE_KEY);
	Object bean = request
			.getAttribute(BeanEditServlet.BEAN_ATTRIBUTE_KEY);
	ExtendedBeanInfo beanInfo = (ExtendedBeanInfo) request
			.getAttribute(BeanEditServlet.BEANINFO_ATTRIBUTE_KEY);
	ExtendedPropertyDescriptor[] pds = beanInfo
			.getPropertyDescriptors();
	String displayName = beanInfo.getBeanDescriptor().getDisplayName();
	if (displayName == null || displayName.isEmpty()) {
		displayName = bean.getClass().getSimpleName();
	}
%>

<h1><%=displayName%></h1>

<%
	String shortDescription = beanInfo.getBeanDescriptor()
			.getShortDescription();
	if (shortDescription != null && !shortDescription.isEmpty()
			&& !shortDescription.equals(displayName)) {
%>
<p><%=shortDescription%></p>
<%
	}
%>

<table class="formtable">
<%
	for (ExtendedPropertyDescriptor pd : pds) {
		if (pd.isHidden())
			continue;

		if (pd.isMap()) {
			// We currently handle a map as if it was an object with each entry being a field.
			// Thus, at the moment we don't allow to add/remove entries - this functionality might be added later.
			// Furthermore, we currently only support maps of type Map<String, String>. 
			// Marco.
			if (pd.getMapKeyType() == String.class
					&& pd.getMapValueType() == String.class) {
				@SuppressWarnings("unchecked")
				Map<String, String> map = (Map<String, String>) pd
						.getValue(bean);
				for (Map.Entry<String, String> me : map.entrySet()) {
					String name = "beanedit." + beanKey + ".value."
							+ pd.getName() + '[' + me.getKey() + ']';
					Object realValue = me.getValue();
					String value = realValue != null ? String
							.valueOf(realValue) : "";
%>
				<tr>
					<td valign="top"><%=pd.getDisplayName()%> (<%=me.getKey()%>): </td>
					<td valign="top" style="padding-left: 8px;">
						<input type="<%=pd.isPasswordField()? "password" : "text"%>" name="<%=name%>" value="<%=(value == null ? "" : value)%>" class="extrawide"/>
					</td>
<%
	String propertyShortDescription = pd
							.getShortDescription();
					if (propertyShortDescription != null
							&& !propertyShortDescription.isEmpty()
							&& !propertyShortDescription.equals(pd
									.getDisplayName())) {
%>
					<td valign="middle" style="padding-left: 16px;"><small><i>(<%=propertyShortDescription%>)</i></small></td>
<%
	}
%>
				</tr>
<%
	}
			}
		} else if (pd.getWriteMethod() != null) {
			String name = "beanedit." + beanKey + ".value."
					+ pd.getName();
			Object realValue = pd.getValue(bean);
			String value = realValue != null ? String
					.valueOf(realValue) : "";
%>
<tr>
	<td valign="top"><%=pd.getDisplayName()%>: </td>
	<td valign="top" style="padding-left: 8px;">
		<%
			if (pd.getPropertyType() == Boolean.class
							|| pd.getPropertyType() == Boolean.TYPE) {
		%>
		<input type="radio" name="<%=name%>" value="true" <%if ("true".equals(value)) {%> checked="checked"<%}%>> Yes
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		<input type="radio" name="<%=name%>" value="false" <%if (!"true".equals(value)) {%> checked="checked"<%}%>> No
		<%
			} else {
						List<String> values = pd.getPossibleValues();
						if (values != null && !values.isEmpty()) {
		%>
		<select name="<%=name%>">
		<%
			for (String v : values) {
		%>
			<option value="<%=v%>"<%if (v.equals(value)) {%> selected="selected"<%}%>><%=pd.getPossibleValueDisplayName(v)%></option>
		<%
			}
		%>
		</select>
		<%
			} else {
		%>
		<input type="<%=pd.isPasswordField()? "password" : "text"%>" name="<%=name%>" value="<%=(value == null ? "" : value)%>" class="extrawide"/>
		<%
			}
					}
		%>
	</td>
	<%
		String propertyShortDescription = pd.getShortDescription();
				if (propertyShortDescription != null
						&& !propertyShortDescription.isEmpty()
						&& !propertyShortDescription.equals(pd
								.getDisplayName())) {
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
