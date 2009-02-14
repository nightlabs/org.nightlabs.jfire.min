package org.nightlabs.jfire.web.admin.beaninfo;

import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.apache.commons.beanutils.PropertyUtils;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class DatabaseCfBeanInfo extends SimpleBeanInfo
{
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	@Override
	public PropertyDescriptor[] getPropertyDescriptors()
	{
//		System.out.println("getPropertyDescriptors()");
		PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(DatabaseCfBeanInfo.class);
		return propertyDescriptors;
//		return null;
	}
}
