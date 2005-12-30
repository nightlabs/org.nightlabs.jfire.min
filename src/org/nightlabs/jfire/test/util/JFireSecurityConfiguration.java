/*
 * Created 	on Oct 5, 2004
 *
 */
package org.nightlabs.jfire.test.util;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

/**
 * @author Alexander Bieber <alex@nightlabs.de>
 * @author Niklas Schiffler <nick@nightlabs.de>
 */
public class JFireSecurityConfiguration extends Configuration 
{
	private static JFireSecurityConfiguration configInstance = null;
	private JFireSecurityConfigurationEntry configurationEntry;

	
	public JFireSecurityConfiguration()
	{
		configurationEntry = new JFireSecurityConfigurationEntry("jfire", "org.jboss.security.ClientLoginModule");
	}
	
	public AppConfigurationEntry[] getAppConfigurationEntry(String name) 
	{
		if(name.equals(configurationEntry.getApplicationName()))
			return 	new AppConfigurationEntry[] {new AppConfigurationEntry(
					configurationEntry.getLoginModuleName(),
					strToLoginModuleControlFlag(configurationEntry.getControlFlag()),
					configurationEntry.getOptions()
				)};
		else
			return new AppConfigurationEntry[] {};
	}

	private static AppConfigurationEntry.LoginModuleControlFlag strToLoginModuleControlFlag(String flag){
		if (flag.toLowerCase().equals(JFireSecurityConfigurationEntry.MODULE_CONTROL_FLAG_REQUIRED))
			return AppConfigurationEntry.LoginModuleControlFlag.REQUIRED;
		else if (flag.toLowerCase().equals(JFireSecurityConfigurationEntry.MODULE_CONTROL_FLAG_REQUISITE))
			return AppConfigurationEntry.LoginModuleControlFlag.REQUISITE;
		else if (flag.toLowerCase().equals(JFireSecurityConfigurationEntry.MODULE_CONTROL_FLAG_SUFFICIENT))
			return AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT;
		else if (flag.toLowerCase().equals(JFireSecurityConfigurationEntry.MODULE_CONTROL_FLAG_OPTIONAL))
			return AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL;
		
		return AppConfigurationEntry.LoginModuleControlFlag.REQUIRED;
	}

	public void refresh() 
	{
	}
	
	public static void declareConfiguration()
	{
		if(configInstance == null)
			configInstance = new JFireSecurityConfiguration();
		
		if (configInstance instanceof JFireSecurityConfiguration)
				Configuration.setConfiguration(configInstance);
	}
}
