package org.nightlabs.jfire.web.admin.beaninfo;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ExtendedPropertyDescriptor extends PropertyDescriptor 
{
	private static Properties extendedPropertyDescriptions;
	private static Map<Locale, ResourceBundle> resourceBundles = new HashMap<Locale, ResourceBundle>(1);

	public static synchronized Properties getExtendedPropertyDescriptions() 
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

	public static ResourceBundle getResourceBundle(Locale locale) 
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

	private String beanClassSymbolicName;
	private Locale locale;

	public ExtendedPropertyDescriptor(Class<?> beanClass, PropertyDescriptor pd, Locale locale) throws IntrospectionException
	{
		super(pd.getName(), pd.getReadMethod(), pd.getWriteMethod());
		this.beanClassSymbolicName = getExtendedPropertyDescriptions().getProperty(beanClass.getName());
		this.locale = locale;
	}

	private String getLocalizedPropertyValue(String key)
	{
		if(beanClassSymbolicName != null) {
			try {
				return getResourceBundle(locale).getString(beanClassSymbolicName+".property."+getName()+"."+key);
			} catch(MissingResourceException e) {
				// ignore and return null
			}
		}
		return null;
	}
	
	private String getPropertyValue(String key)
	{
		if(beanClassSymbolicName != null)
			return getExtendedPropertyDescriptions().getProperty(beanClassSymbolicName+".property."+getName()+"."+key);
		return null;
	}
	
	private Boolean getBooleanPropertyValue(String key)
	{
		String b = getPropertyValue(key);
		if(b != null) {
			if("true".equalsIgnoreCase(b))
				return true;
			if("false".equalsIgnoreCase(b))
				return false;
		}
		return null;
	}
	
	@Override
	public String getDisplayName() 
	{
		String s = getLocalizedPropertyValue("displayName");
		return s == null ? super.getDisplayName() : s;
	}
	
	@Override
	public String getShortDescription() 
	{
		String s = getLocalizedPropertyValue("shortDescription");
		return s == null ? super.getShortDescription() : s;
	}
	
	@Override
	public boolean isHidden() 
	{
		Boolean b = getBooleanPropertyValue("hidden");
		return b == null ? super.isHidden() : b;
	}
}
