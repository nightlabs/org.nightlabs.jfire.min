/*
 * Created 	on Aug 26, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.config;

import org.nightlabs.jfire.config.id.ConfigID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public interface ConfigSetupVisualiser {
	
	/**
	 * Returns a displayable name for the keyObject
	 * linked the given Config.
	 * If the name is multilingual a localized
	 * name should be returned.
	 * This method should never return null or an empty string
	 * but the configKey when an error occurs in name resolving.
	 * 
	 * @param configID The ConfigID to which's linked object the name is searched.
	 * @return A displayable name for the object linked to the given Config.
	 */
	String getKeyObjectName(ConfigID configID);
	
	/**
	 * Returns a localized description of the given Config which might
	 * be a ConfigGroup as well.
	 * Here as well the configKey should be returned on an error, not null or
	 * an empty String.
	 */
	String getConfigDescription(ConfigID configID);
	
	
}
