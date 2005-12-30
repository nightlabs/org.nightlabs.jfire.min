/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

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
