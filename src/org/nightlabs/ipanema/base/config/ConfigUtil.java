/*
 * Created 	on Aug 25, 2005
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.config;

import org.nightlabs.ipanema.base.login.Login;
import org.nightlabs.ipanema.config.ConfigModule;
import org.nightlabs.ipanema.config.UserConfigSetup;
import org.nightlabs.ipanema.config.WorkstationConfigSetup;
import org.nightlabs.ipanema.security.id.UserID;
import org.nightlabs.ipanema.workstation.id.WorkstationID;

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
	public static ConfigModule getUserCfMod(Class cfModClass, String[] fetchGroups) {
		try {
			UserID userID = UserID.create(Login.getLogin().getOrganisationID(), Login.getLogin().getUserID());
			return ConfigModuleProvider.sharedInstance().getConfigModule(
					UserConfigSetup.getUserConfigID(userID),
					cfModClass,
					null,
					fetchGroups
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
	public static ConfigModule getWorkstationCfMod(Class cfModClass, String[] fetchGroups) {
		try {
			WorkstationID workstationID = WorkstationID.create(
					Login.getLogin().getOrganisationID(), 
					Login.getLogin().getWorkstationID()
				);
			return ConfigModuleProvider.sharedInstance().getConfigModule(
					WorkstationConfigSetup.getWorkstationConfigID(workstationID),
					cfModClass,
					null,
					fetchGroups
				);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}
	

}
