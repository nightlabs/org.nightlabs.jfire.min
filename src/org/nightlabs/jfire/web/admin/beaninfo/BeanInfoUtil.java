package org.nightlabs.jfire.web.admin.beaninfo;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BeanInfoUtil 
{
//	public static ExtendedPropertyDescriptor[] getExtendedPropertyDescriptors(Class<?> beanClass, Locale locale) throws IntrospectionException
//	{
//		BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
//		PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
//		ExtendedPropertyDescriptor[] extendedPropertyDescriptors = new ExtendedPropertyDescriptor[propertyDescriptors.length];
//		for (int i = 0; i < propertyDescriptors.length; i++)
//			extendedPropertyDescriptors[i] = new ExtendedPropertyDescriptor(beanClass, propertyDescriptors[i], locale);
//		return extendedPropertyDescriptors;
//	}
//	
//	public static Map<String, ExtendedPropertyDescriptor> getExtendedPropertyDescriptorsByName(Class<?> beanClass, Locale locale) throws IntrospectionException
//	{
//		ExtendedPropertyDescriptor[] epds = getExtendedPropertyDescriptors(beanClass, locale);
//		Map<String, ExtendedPropertyDescriptor> result = new HashMap<String, ExtendedPropertyDescriptor>(epds.length);
//		for (ExtendedPropertyDescriptor epd : epds)
//			result.put(epd.getName(), epd);
//		return result;
//	}
}
