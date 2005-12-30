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

package org.nightlabs.jfire.base.login;

import java.util.ArrayList;
import java.util.HashMap;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.apache.log4j.Logger;

import org.nightlabs.config.Config;
import org.nightlabs.config.ConfigException;

/**
 * @author Alexander Bieber
 */
public class JFireSecurityConfiguration extends Configuration {
	public static final Logger LOGGER = Logger.getLogger(JFireSecurityConfiguration.class);

	private LoginConfigModule loginConfigModule = null;
	private HashMap entries = new HashMap();
	
	public JFireSecurityConfiguration(){
		try {
			loginConfigModule = (LoginConfigModule) Config.sharedInstance().createConfigModule(LoginConfigModule.class);			
			refresh();
		} catch (ConfigException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see javax.security.auth.login.Configuration#refresh()
	 */
	public void refresh() {
		LOGGER.debug("Refreshing entries");
		ArrayList configEntries = loginConfigModule.getSecurityConfigurations();
		this.entries.clear();
		for (int i=0; i<configEntries.size(); i++){
			JFireSecurityConfigurationEntry confEntry = (JFireSecurityConfigurationEntry)configEntries.get(i);
			LOGGER.debug("Adding entry for "+confEntry.getApplicationName()+"("+confEntry.getLoginModuleName()+", "+confEntry.getControlFlag()+", "+confEntry.getOptions()+")");
			LOGGER.debug("Control Flag is: "+strToLoginModuleControlFlag(confEntry.getControlFlag()));
			this.entries.put(
				confEntry.getApplicationName(),
				new AppConfigurationEntry(
					confEntry.getLoginModuleName(),
					strToLoginModuleControlFlag(confEntry.getControlFlag()),
					confEntry.getOptions()
				)
			);
		}
	}

	/* (non-Javadoc)
	 * @see javax.security.auth.login.Configuration#getAppConfigurationEntry(java.lang.String)
	 */
	public AppConfigurationEntry[] getAppConfigurationEntry(String applicationName) {
//		LOGGER.debug("Having request for "+applicationName);
		if (entries.containsKey(applicationName)){
//			LOGGER.debug("Found entry for "+applicationName);
			return new AppConfigurationEntry[]{(AppConfigurationEntry)entries.get(applicationName)};
		}
		return null;
	}
	
	private static JFireSecurityConfiguration configInstance = null;
	public static void declareConfiguration(){
		Class configClass = null; //Configuration.getConfiguration().getClass();
		if (configClass == null)
			LOGGER.debug("Current security configuration is null");
		else 
			LOGGER.debug("Current security configuration is of type: "+configClass.getName());
		
		if (configClass != JFireSecurityConfiguration.class){
			configInstance = new JFireSecurityConfiguration();
			Configuration.setConfiguration(configInstance);
			LOGGER.debug("Set configuration to type: "+Configuration.getConfiguration().getClass().getName());
		}
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

}
