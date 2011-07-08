package org.nightlabs.jfire.web.admin.beaninfo;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

public class ServerConfigBeanInfo extends ExtendedBeanInfo
{
	public ServerConfigBeanInfo(Class<?> beanClass, Locale locale) throws IntrospectionException
	{
		super(Introspector.getBeanInfo(beanClass), locale);
	}
	
	private static Properties serverConfigBeanInfo;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.web.admin.beaninfo.ExtendedBeanInfo#getExtendedBeanInfo()
	 */
	@Override
	protected synchronized Properties getExtendedBeanInfo()
	{
		if(serverConfigBeanInfo == null) {
			serverConfigBeanInfo = new Properties();
			serverConfigBeanInfo.putAll(super.getExtendedBeanInfo());
			try {
				serverConfigBeanInfo.load(getClass().getResourceAsStream("serverconfigbeaninfo.properties"));
			} catch (IOException e) {
				throw new RuntimeException("Error loading extended server config bean info properties", e);
			}
		}
		return serverConfigBeanInfo;
	}
}