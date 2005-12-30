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

package org.nightlabs.jfire.base.config;

import javax.jdo.FetchPlan;

import org.nightlabs.jfire.base.security.UserProvider;
import org.nightlabs.jfire.config.ConfigGroup;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class UserCSVisualiser implements ConfigSetupVisualiser {

	private static String[] USER_FETCH_GROUPS = new String[] {FetchPlan.DEFAULT}; 	
	
	/**
	 * 
	 */
	public UserCSVisualiser() {
		super();
	}

	/**
	 * @see org.nightlabs.jfire.base.config.ConfigSetupVisualiser#getKeyObjectName(org.nightlabs.jfire.base.config.id.ConfigID)
	 */
	public String getKeyObjectName(ConfigID configID) {
		try {
			UserID userID = new UserID(configID.configKey);
			User user = UserProvider.sharedInstance().getUser(userID, USER_FETCH_GROUPS);
			return user.getUserID()+" ("+user.getName()+")";
		} catch (Exception e) {
			return configID.configKey;
		} 
	}

	public String getConfigDescription(ConfigID configID) {
		ConfigSetup setup = ConfigSetupRegistry.sharedInstance().getConfigSetupForConfigType(configID);
		if (setup == null)
			return configID.configKey;
		if (ConfigSetupRegistry.sharedInstance().isConfigGroup(configID)) {
			ConfigGroup group = setup.getConfigGroup(configID.configKey);
			return "ConfigGroup "+group.getName();
		}		
		return "Config for user "+getKeyObjectName(configID);
	}
	
}
