/*
 * Created 	on Aug 26, 2005
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.config;

import javax.jdo.FetchPlan;

import org.nightlabs.ipanema.base.security.UserProvider;
import org.nightlabs.ipanema.config.ConfigGroup;
import org.nightlabs.ipanema.config.ConfigSetup;
import org.nightlabs.ipanema.config.id.ConfigID;
import org.nightlabs.ipanema.security.User;
import org.nightlabs.ipanema.security.id.UserID;

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
	 * @see org.nightlabs.ipanema.base.config.ConfigSetupVisualiser#getKeyObjectName(org.nightlabs.ipanema.base.config.id.ConfigID)
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
