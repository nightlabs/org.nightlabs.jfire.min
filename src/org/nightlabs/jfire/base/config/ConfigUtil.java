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

import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.config.UserConfigSetup;
import org.nightlabs.jfire.config.WorkstationConfigSetup;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.workstation.id.WorkstationID;

/**
 * Provides static convenience methods for retrieving ConfigModules.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ConfigUtil {
	
	/**
	 * Returns the ConfigModule of the given class for the user currently logged in.
	 *  
	 * @param cfModClass The class of the ConfigModule to get.
	 * @param fetchGroups The fetch-groups the ConfigModule should be detached with
	 */
	public static ConfigModule getUserCfMod(Class cfModClass, String[] fetchGroups, int maxFetchDepth) {
		try {
			UserID userID = UserID.create(Login.getLogin().getOrganisationID(), Login.getLogin().getUserID());
			return ConfigModuleProvider.sharedInstance().getConfigModule(
					UserConfigSetup.getUserConfigID(userID),
					cfModClass,
					null,
					fetchGroups,
					maxFetchDepth
				);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}
	
	/**
	 * Returns the ConfigModule of the given class for the workstation the current
	 * user is logged on.
	 *  
	 * @param cfModClass The class of the ConfigModule to get.
	 * @param fetchGroups The fetch-groups the ConfigModule should be detached with
	 */
	public static ConfigModule getWorkstationCfMod(Class cfModClass, String[] fetchGroups, int maxFetchDepth) {
		try {
			WorkstationID workstationID = WorkstationID.create(
					Login.getLogin().getOrganisationID(), 
					Login.getLogin().getWorkstationID()
				);
			return ConfigModuleProvider.sharedInstance().getConfigModule(
					WorkstationConfigSetup.getWorkstationConfigID(workstationID),
					cfModClass,
					null,
					fetchGroups,
					maxFetchDepth
				);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}
	

}
