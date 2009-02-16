package org.nightlabs.jfire.web.admin.beaninfo;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ExtendedPropertyDescriptor extends PropertyDescriptor 
{
	private ExtendedBeanInfo beanInfo;

	public ExtendedPropertyDescriptor(ExtendedBeanInfo beanInfo, PropertyDescriptor pd) throws IntrospectionException
	{
		super(pd.getName(), pd.getReadMethod(), pd.getWriteMethod());
		this.beanInfo = beanInfo;
	}
	
	@Override
	public String getDisplayName() 
	{
		String s = beanInfo.getLocalizedPropertyValue(getName(), "displayName");
		return s == null ? super.getDisplayName() : s;
	}
	
	@Override
	public String getShortDescription() 
	{
		String s = beanInfo.getLocalizedPropertyValue(getName(), "shortDescription");
		return s == null ? super.getShortDescription() : s;
	}
	
	@Override
	public boolean isHidden() 
	{
		Boolean b = beanInfo.getBooleanPropertyValue(getName(), "hidden");
		return b == null ? super.isHidden() : b;
	}
}
