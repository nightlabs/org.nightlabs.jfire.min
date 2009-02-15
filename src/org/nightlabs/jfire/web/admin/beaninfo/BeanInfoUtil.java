package org.nightlabs.jfire.web.admin.beaninfo;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Locale;

public class BeanInfoUtil 
{
	public static ExtendedPropertyDescriptor[] getExtendedPropertyDescriptors(Class<?> beanClass, Locale locale) throws IntrospectionException
	{
		BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
		PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
		ExtendedPropertyDescriptor[] extendedPropertyDescriptors = new ExtendedPropertyDescriptor[propertyDescriptors.length];
		for (int i = 0; i < propertyDescriptors.length; i++)
			extendedPropertyDescriptors[i] = new ExtendedPropertyDescriptor(beanClass, propertyDescriptors[i], locale);
		return extendedPropertyDescriptors;
	}
}
