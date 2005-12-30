/*
 * Created 	on Sep 9, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.config;

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.base.jdo.JDOObjectProvider;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.config.ConfigManager;
import org.nightlabs.jfire.config.ConfigManagerUtil;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.config.id.ConfigModuleID;
import org.nightlabs.jdo.ObjectID;

public class ConfigModuleProvider extends JDOObjectProvider {

	public ConfigModuleProvider() {
		super();
	}
	
	/**
	 * @see org.nightlabs.jfire.base.jdo.JDOObjectProvider#retrieveJDOObject(java.lang.String, java.lang.Object, java.lang.String[])
	 */
	protected Object retrieveJDOObject(String scope, Object objectID, String[] fetchGroups) throws Exception {
		if (!(objectID instanceof ConfigModuleID))
			throw new IllegalArgumentException("Expected ConfigModuleID as objectID-parameter but found "+objectID.getClass().getName()+": "+objectID);
		ConfigModuleID moduleID = (ConfigModuleID)objectID;
		String className = ConfigModule.getClassNameOutOfCfModKey(moduleID.cfModKey);
		Class cfModClass = null;
		try {
			cfModClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Could resolve ConfigModule class "+className, e);
		}
		String cfModID = ConfigModule.getCfModIDOutOfCfModKey(moduleID.cfModKey);		
		ConfigID configID = ConfigID.create(moduleID.organisationID, moduleID.configKey, moduleID.configType);
		ConfigManager configManager = ConfigManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();		
		return configManager.getConfigModule(configID, cfModClass, cfModID, fetchGroups);
	}
	
	/**
	 * Get the ConfigModule of the given class and cfModID for the Config defined
	 * by the given configID.
	 */
	public ConfigModule getConfigModule(ConfigID config, Class cfModClass, String cfModID, String[] fetchGroups) {
		return (ConfigModule)getJDOObject(
				null, 
				ConfigModuleID.create(
						config.organisationID,
						config.configKey,
						config.configType,
						ConfigModule.getCfModKey(cfModClass, cfModID)
				),
				fetchGroups
			);
	}

	/**
	 * Get the ConfigModule of the given class and cfModID for the given Config.
	 */
	public ConfigModule getConfigModule(Config config, Class cfModClass, String cfModID, String[] fetchGroups) {
		return getConfigModule((ConfigID)JDOHelper.getObjectId(config), cfModClass, cfModID, fetchGroups);
	}
	
	/**
	 * Get the ConfigModule of the given class and cfModID for the Config that
	 * is linked to the object with class linkObjectClass and the object-id
	 * of linkedObjectID
	 */
	public ConfigModule getConfigModule(
			String organiationID,
			ObjectID linkObjectID, 
			Class linkObjectClass, 
			Class cfModClass, 
			String cfModID, 
			String[] fetchGroups
		) 
	{
		return getConfigModule(
				ConfigID.create(
						organiationID,
						linkObjectID,
						linkObjectClass
					), 
				cfModClass, 
				cfModID, 
				fetchGroups
			);
	}
	
	private static ConfigModuleProvider sharedInstance;
	
	public static ConfigModuleProvider sharedInstance() {
		if (sharedInstance == null)
			sharedInstance = new ConfigModuleProvider();
		return sharedInstance;		
	}

}
