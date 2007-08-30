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
import java.util.List;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.apache.log4j.Logger;
import org.nightlabs.config.ConfigException;

/**
 * @author Alexander Bieber
 */
public class JFireSecurityConfiguration extends Configuration {
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(JFireSecurityConfiguration.class);

	private HashMap<String, AppConfigurationEntry> entries = new HashMap<String, AppConfigurationEntry>();
	
	public JFireSecurityConfiguration(){
		try {
			refresh();
		} catch (ConfigException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see javax.security.auth.login.Configuration#refresh()
	 */
	public void refresh() {
		logger.debug("Refreshing entries"); //$NON-NLS-1$
		this.entries.clear();

		// TODO Reimplement LoginModule to make it indepenent of JBoss.
		
		List<JFireSecurityConfigurationEntry> configEntries = new ArrayList<JFireSecurityConfigurationEntry>();
		configEntries.add(new JFireSecurityConfigurationEntry("jfire", "org.jboss.security.ClientLoginModule")); //$NON-NLS-1$ //$NON-NLS-2$
		
		for (JFireSecurityConfigurationEntry confEntry : configEntries) {
			logger.debug("Adding entry for "+confEntry.getApplicationName()+"("+confEntry.getLoginModuleName()+", "+confEntry.getControlFlag()+", "+confEntry.getOptions()+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			logger.debug("Control Flag is: "+strToLoginModuleControlFlag(confEntry.getControlFlag())); //$NON-NLS-1$
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
		Class<?> configClass = null; //Configuration.getConfiguration().getClass();
		if (configClass == null)
			logger.debug("Current security configuration is null"); //$NON-NLS-1$
		else 
			logger.debug("Current security configuration is of type: "+configClass.getName()); //$NON-NLS-1$
		
		if (configClass != JFireSecurityConfiguration.class){
			configInstance = new JFireSecurityConfiguration();
			Configuration.setConfiguration(configInstance);
			logger.debug("Set configuration to type: "+Configuration.getConfiguration().getClass().getName()); //$NON-NLS-1$
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
