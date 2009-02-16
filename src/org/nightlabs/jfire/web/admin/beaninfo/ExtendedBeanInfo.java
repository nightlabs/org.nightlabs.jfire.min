package org.nightlabs.jfire.web.admin.beaninfo;

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 *
 */
public class ExtendedBeanInfo extends SimpleBeanInfo 
{
	private BeanInfo baseBeanInfo;
	private String beanClassSymbolicName;
	private Locale locale;

	String getBeanClassSymbolicName() 
	{
		return beanClassSymbolicName;
	}

	Locale getLocale() 
	{
		return locale;
	}

	public ExtendedBeanInfo(BeanInfo baseBeanInfo, Locale locale) throws IntrospectionException
	{
		System.out.println("base bean info: "+baseBeanInfo);
		this.baseBeanInfo = baseBeanInfo;
		this.beanClassSymbolicName = getExtendedBeanInfo().getProperty(baseBeanInfo.getBeanDescriptor().getBeanClass().getName());
		System.out.println("Bean class symbolic name: "+this.beanClassSymbolicName);
		this.locale = locale;
	}

	private static Properties extendedPropertyDescriptions;
	private static Map<Locale, ResourceBundle> resourceBundles = new HashMap<Locale, ResourceBundle>(1);

	protected static synchronized Properties getExtendedBeanInfo() 
	{
		if(extendedPropertyDescriptions == null) {
			extendedPropertyDescriptions = new Properties();
			try {
				extendedPropertyDescriptions.load(ExtendedPropertyDescriptor.class.getResourceAsStream("beaninfo.properties"));
			} catch (IOException e) {
				throw new RuntimeException("Error loading extended bean info properties", e);
			}
		}
		return extendedPropertyDescriptions;
	}
	
	protected static ResourceBundle getResourceBundle(Locale locale) 
	{
		synchronized (resourceBundles) {
			ResourceBundle resourceBundle = resourceBundles.get(locale);
			if(resourceBundle == null) {
				resourceBundle = ResourceBundle.getBundle("org.nightlabs.jfire.web.admin.beaninfo.beaninfolang");
				resourceBundles.put(locale, resourceBundle);
			}
			return resourceBundle;
		}
	}
	
	public String getLocalizedValue(String key)
	{
		if(beanClassSymbolicName != null) {
			try {
				System.out.println("Looking for localized value: "+beanClassSymbolicName+"."+key);
				return getResourceBundle(locale).getString(beanClassSymbolicName+"."+key);
			} catch(MissingResourceException e) {
				// ignore and return null
			}
		}
		return null;
	}
	
	public String getLocalizedPropertyValue(String propertyName, String key)
	{
		return getLocalizedValue("property."+propertyName+"."+key);
//		if(beanClassSymbolicName != null) {
//			try {
//				return getResourceBundle(locale).getString(beanClassSymbolicName+".property."+propertyName+"."+key);
//			} catch(MissingResourceException e) {
//				// ignore and return null
//			}
//		}
//		return null;
	}
	
	public String getValue(String key)
	{
		if(beanClassSymbolicName != null)
			return getExtendedBeanInfo().getProperty(beanClassSymbolicName+"."+key);
		return null;
	}
	
	public String getPropertyValue(String propertyName, String key)
	{
		return getValue("property."+propertyName+"."+key);
//		if(beanClassSymbolicName != null)
//			return getExtendedBeanInfo().getProperty(beanClassSymbolicName+".property."+propertyName+"."+key);
//		return null;
	}
	
	public Boolean getBooleanPropertyValue(String propertyName, String key)
	{
		String b = getPropertyValue(propertyName, key);
		if(b != null) {
			if("true".equalsIgnoreCase(b))
				return true;
			if("false".equalsIgnoreCase(b))
				return false;
		}
		return null;
	}

	public BeanInfo[] getAdditionalBeanInfo() {
		return baseBeanInfo.getAdditionalBeanInfo();
	}

	private ExtendedBeanDescriptor extendedBeanDescriptor = null;
	
	public ExtendedBeanDescriptor getBeanDescriptor() 
	{
		if(extendedBeanDescriptor == null)
			extendedBeanDescriptor = new ExtendedBeanDescriptor(this, baseBeanInfo.getBeanDescriptor()); 
		return extendedBeanDescriptor;
	}

	public int getDefaultEventIndex() {
		return baseBeanInfo.getDefaultEventIndex();
	}

	public int getDefaultPropertyIndex() {
		return baseBeanInfo.getDefaultPropertyIndex();
	}

	public EventSetDescriptor[] getEventSetDescriptors() {
		return baseBeanInfo.getEventSetDescriptors();
	}

	public Image getIcon(int iconKind) {
		return baseBeanInfo.getIcon(iconKind);
	}

	public MethodDescriptor[] getMethodDescriptors() {
		return baseBeanInfo.getMethodDescriptors();
	}

	private ExtendedPropertyDescriptor[] extendedPropertyDescriptors = null;
	
	public ExtendedPropertyDescriptor[] getPropertyDescriptors() 
	{
		if(extendedPropertyDescriptors == null) {
			PropertyDescriptor[] propertyDescriptors = baseBeanInfo.getPropertyDescriptors();
			extendedPropertyDescriptors = new ExtendedPropertyDescriptor[propertyDescriptors.length];
			for (int i = 0; i < propertyDescriptors.length; i++) {
				try {
					extendedPropertyDescriptors[i] = new ExtendedPropertyDescriptor(this, propertyDescriptors[i]);
				} catch (IntrospectionException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return extendedPropertyDescriptors;
	}	
	
	public Map<String, ExtendedPropertyDescriptor> getExtendedPropertyDescriptorsByName()
	{
		ExtendedPropertyDescriptor[] epds = getPropertyDescriptors();
		Map<String, ExtendedPropertyDescriptor> result = new HashMap<String, ExtendedPropertyDescriptor>(epds.length);
		for (ExtendedPropertyDescriptor epd : epds)
			result.put(epd.getName(), epd);
		return result;
	}
	
//	/* (non-Javadoc)
//	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
//	 */
//	@Override
//	public ExtendedBeanDescriptor getBeanDescriptor() 
//	{
//		return new ExtendedBeanDescriptor(this);
//	}
//	
//	@Override
//	public ExtendedPropertyDescriptor[] getPropertyDescriptors() 
//	{
//	}
	
	
	
}
