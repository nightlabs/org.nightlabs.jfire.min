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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.nightlabs.base.extensionpoint.AbstractEPProcessor;
import org.nightlabs.base.extensionpoint.EPProcessorException;
import org.nightlabs.jfire.base.jdo.JDOObjectID2PCClassMap;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.base.jdo.notification.ChangeManager;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.config.ConfigGroup;
import org.nightlabs.jfire.config.ConfigManager;
import org.nightlabs.jfire.config.ConfigManagerUtil;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.config.id.ConfigSetupID;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;
import org.nightlabs.notification.NotificationListenerWorkerThreadAsync;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ConfigSetupRegistry extends AbstractEPProcessor {

	public static final String EXTENSION_POINT_ID = "org.nightlabs.jfire.base.configsetupvisualiser";
	
	private static final String[] DEFAULT_FETCH_GROUP_GROUPS = new String[] 
	  { FetchPlan.DEFAULT };
	private static final String[] DEFAULT_FETCH_GROUP_CONFIGS = new String[]
    { FetchPlan.DEFAULT, Config.FETCH_GROUP_CONFIG_GROUP };
	
	/**
	 * key: String configSetupType
	 * value: ConfigSetup configSetup
	 */
	private Map configSetupsByType = null;
	
	/**
	 * key: String configSetupType
	 * value: ConfigSetupVisualiser setupVisualiser
	 */
	private Map setupVisualiserByType = new HashMap();
	
	/**
	 * key: String configSetupType
	 * value: ConfigPreferencesNode mergedTreeNode
	 */
	private Map mergedTreeNodes = new HashMap();
	
	/**
	 * Returns the ConfigSetup of the given type if existent, null otherwise.
	 * I the ConfigSetups were not loaded from the server yet, they will be
	 * loaded and put into the client cache.
	 * 
	 * @param configSetupType
	 * @return The ConfigSetup of the given configSetupType or null.
	 */
	public ConfigSetup getConfigSetup(String configSetupType) {
		if (configSetupsByType == null)
			getConfigSetups();
		
		return (ConfigSetup)configSetupsByType.get(configSetupType);
	}
	
	/**
	 * Returns the ConfigType that holds ConfigGroups of the same type as
	 * the given ConfigGroup. Be sure to pass the ConfigID of a ConfigGroup
	 * here.
	 * 
	 * @param configGroupID The ConfigID of a ConfigGroup.
	 * @return ConfigType with configGroupType like the given ID's configType.
	 */
	public ConfigSetup getConfigSetupForGroup(ConfigID configGroupID) {
		if (configSetupsByType == null)
			getConfigSetups();
		for (Iterator iter = configSetupsByType.values().iterator(); iter.hasNext();) {
			ConfigSetup configSetup = (ConfigSetup) iter.next();
			if (configSetup.getConfigGroupType().equals(configGroupID.configType))
				return configSetup;
		}
		return null;
	}
		
	
	/**
	 * Checks whether a ConfigSetup is registered that links Configs to Objects
	 * of the given linkClassName.
	 * 
	 * @param linkClassName The classname of the linked objects.
	 * @return Whether there is a ConfigSetup registered that links objects of the 
	 * given class.
	 */
	public boolean containsRegistrationForLinkClass(String linkClassName) {
		if (configSetupsByType == null)
			getConfigSetups();
		boolean result = false;
		if (linkClassName == null || "".equals(linkClassName))
			throw new IllegalArgumentException("Parameter linkClassName must not be null or empty!");
		for (Iterator iter = configSetupsByType.values().iterator(); iter.hasNext();) {
			ConfigSetup setup = (ConfigSetup) iter.next();
			result = linkClassName.equals(setup.getConfigType());
			if (result)
				break;
		}
		return result;
	}
	
	/**
	 * Returns the ConfigSetup that either has Configs or ConfigGroups with 
	 * the configType of the given ConfigID.
	 * 
	 * @param configID The ConfigID which type should be part of the returned ConfigSetup
	 * @return The ConfigSetup that either has Configs or ConfigGroups with 
	 * the configType of the given ConfigID.
	 */
	public ConfigSetup getConfigSetupForConfigType(ConfigID configID) {
		if (configSetupsByType == null)
			getConfigSetups();
		boolean result = false;
		for (Iterator iter = configSetupsByType.values().iterator(); iter.hasNext();) {
			ConfigSetup setup = (ConfigSetup) iter.next();
			if (setup.getConfigType().equals(configID.configType) ||
					setup.getConfigGroupType().equals(configID.configType))
				return setup;
		}
		return null;
	}
	
	/**
	 * Checks whether the given ConfigID is the id-object of a ConfigGroup.
	 */
	public boolean isConfigGroup(ConfigID configID) {		
		ConfigSetup setup = getConfigSetupForConfigType(configID);
		if (setup == null)
			return false;
		return setup.getConfigGroupType().equals(configID.configType);
	}
	
	/**
	 * Checks whether a ConfigSetup is registered that has a Config linked to 
	 * the given linkObject.
	 * 
	 * @param linkObject The object to check lingage for.
	 * @return Whether there is a ConfigSetup registered that links the given
	 * linkObject.
	 */
	public boolean containsRegistrationForLinkObject(Object linkObjec) {
		return containsRegistrationForLinkClass(linkObjec.getClass());
	}
	
	/**
	 * Checks whether a ConfigSetup is registered that links Configs to Objects
	 * of the given Class.
	 * 
	 * @param linkClass The Class of the linked objects.
	 * @return Whether there is a ConfigSetup registered that links objects of the 
	 * given linkClass.
	 */
	public boolean containsRegistrationForLinkClass(Class linkClass) {
		return containsRegistrationForLinkClass(linkClass.getName());
	}
	
	/**
	 * Returns a merged tree of ConfigPreferenceNodes.
	 * The set contains all registered PreferencePages that edit a ConfigModule 
	 * registered in the ConfigSetup holding Configs with a configType as of the given configID.
	 * Additionally a ConfigPreferenceNode for all remaining ConfigModuleClasses
	 * in the found ConfigSetup will be added to the returned node, but these
	 * won't contain a PreferencePage and therefore will not be editable.
	 */
	public ConfigPreferenceNode getMergedPreferenceRootNode(String scope, ConfigID configID) {
		ConfigSetup setup = getConfigSetupForConfigType(configID);
		ConfigPreferenceNode rootNode = (ConfigPreferenceNode)mergedTreeNodes.get(scope+setup.getConfigSetupType());
		if (rootNode != null)
			return rootNode;
		ConfigPreferenceNode registeredRootNode = ConfigPreferencePageRegistry.sharedInstance().getPreferencesRootNode();
		rootNode = new ConfigPreferenceNode("","","",null,null);
		
		Set mergeModules = new HashSet();
		mergeModules.addAll(setup.getConfigModuleClasses());		
		
		for (Iterator iter = registeredRootNode.getChildren().iterator(); iter.hasNext();) {
			ConfigPreferenceNode childNode = (ConfigPreferenceNode) iter.next();
			// recursively merge
			mergeSetupNodes(setup, mergeModules, childNode, rootNode);
		}
		
		// for all remaining classes add a null-Node
		for (Iterator iter = mergeModules.iterator(); iter.hasNext();) {
			String moduleClassName = (String) iter.next();
			ConfigPreferenceNode node = new ConfigPreferenceNode("", moduleClassName, "", rootNode, null);
			rootNode.addChild(node);
		}
		mergedTreeNodes.put(scope+setup.getConfigSetupType(), rootNode);
		return rootNode;
	}
	
	/**
	 * Private helper that recursively adds registrations of ConfigPreferencePages
	 * to a new ConfigPreferenceNode if the given ConfigSetup has a registration
	 * for the appropriate ConfigModule-class. Stops in the tree when no registration
	 * was found in the setup, so the further in the tree even if adequate will
	 * not be found. 
	 */
	private void mergeSetupNodes(
			ConfigSetup setup, 
			Set mergeModules, 
			ConfigPreferenceNode orgNode,
			ConfigPreferenceNode newNodeParent) 
	{
		String nodeClassName = orgNode.getPreferencePage().getConfigModuleClass().getName();
		boolean hasRegistration = 
			(orgNode.getPreferencePage() != null) && 
			(setup.getConfigModuleClasses().contains(nodeClassName));
		if (hasRegistration) {
			mergeModules.remove(nodeClassName);
			ConfigPreferenceNode newNode = new ConfigPreferenceNode(
					orgNode.getConfigPreferenceID(),
					orgNode.getConfigPreferenceName(),
					orgNode.getCategoryID(),
					newNodeParent,
					orgNode.getPreferencePage()
				);			
			newNodeParent.addChild(newNode);
			for (Iterator iter = orgNode.getChildren().iterator(); iter.hasNext();) {
				ConfigPreferenceNode child = (ConfigPreferenceNode) iter.next();
				mergeSetupNodes(setup, mergeModules, child, newNode);
			}
		}
	}

	/**
	 * 
	 */
	public ConfigSetupRegistry() {
		super();
	}

	/**
	 * Integerates the given setup into this registrys map and into
	 * the Cache.
	 * 
	 * @param setup The setup to integrate
	 */
	private void integrateConfigSetup(ConfigSetup setup) 
	{
		if (configSetupsByType == null)
			configSetupsByType = new HashMap();
		// add the setup to the
		configSetupsByType.put(setup.getConfigSetupType(), setup);
		
		// Add the setup itself to the Cache for notifications
		Cache.sharedInstance().put(null, setup, DEFAULT_FETCH_GROUP_GROUPS);
		// now add all Configs to the Cache
		for (Iterator iterator = setup.getConfigs().iterator(); iterator.hasNext();) {
			Config config = (Config) iterator.next();
			Cache.sharedInstance().put(null, config, DEFAULT_FETCH_GROUP_CONFIGS);
		}
		// and finally all ConfigGroups
		for (Iterator iterator = setup.getConfigGroups().iterator(); iterator.hasNext();) {
			ConfigGroup group = (ConfigGroup) iterator.next();
			Cache.sharedInstance().put(null, group, DEFAULT_FETCH_GROUP_GROUPS);
		}
		mergedTreeNodes.remove(setup.getConfigSetupType());
	}
	
	private void getConfigSetups() {
		if (configSetupsByType != null)
			return;
		Collection setups = null;
		try {
			ConfigManager configManager = ConfigManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
			setups = configManager.getConfigSetups(DEFAULT_FETCH_GROUP_GROUPS, DEFAULT_FETCH_GROUP_CONFIGS);			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		for (Iterator iter = setups.iterator(); iter.hasNext();) {
			ConfigSetup setup = (ConfigSetup) iter.next();
			integrateConfigSetup(setup);
		}
	}
	
	/**
	 * Listener for changes of ConfigSetups.
	 */
	private NotificationListener setupChangeListener = new NotificationListenerWorkerThreadAsync() {
		public void notify(NotificationEvent notificationEvent) {
			if (notificationEvent.getFirstSubject() instanceof ConfigSetupID) {
				String configSetupType = ((ConfigSetupID)notificationEvent.getFirstSubject()).configSetupType;				
				try {
					ConfigManager configManager = ConfigManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
					ConfigSetup setup = configManager.getConfigSetup(
							configSetupType, 
							DEFAULT_FETCH_GROUP_GROUPS, 
							DEFAULT_FETCH_GROUP_CONFIGS
						);
					integrateConfigSetup(setup);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	};  
	
	/**
	 * Listener for changes of Configs
	 */
	private NotificationListener configChangeListener = new NotificationListenerWorkerThreadAsync() {
		public void notify(NotificationEvent notificationEvent) {
			if (notificationEvent.getFirstSubject() instanceof ConfigID) {
				ConfigID configID = (ConfigID)notificationEvent.getFirstSubject();
				Class jdoObjectClass = JDOObjectID2PCClassMap.sharedInstance().getPersistenceCapableClass(configID);
				if (jdoObjectClass != Config.class)
					return;
				Config config = null;
				try {
					ConfigManager configManager = ConfigManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
					config = configManager.getConfig(configID, DEFAULT_FETCH_GROUP_CONFIGS);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				Cache.sharedInstance().put(null, config, DEFAULT_FETCH_GROUP_CONFIGS);
				for (Iterator iter = configSetupsByType.values().iterator(); iter.hasNext();) {
					ConfigSetup setup = (ConfigSetup) iter.next();
					if (setup.getConfigsMap().get(configID) == null)
						continue;
					setup.getConfigsMap().put(configID, config);
					if (config.getConfigGroup() != null)
						config.setConfigGroup(setup.getConfigGroup(config.getConfigGroup().getConfigKey()));
				}
			}
		}
	};  

	/**
	 * Listener for changes of ConfigGroups
	 */
	private NotificationListener configGroupChangeListener = new NotificationListenerWorkerThreadAsync() {
		public void notify(NotificationEvent notificationEvent) {
			if (notificationEvent.getFirstSubject() instanceof ConfigID) {
				ConfigID configID = (ConfigID)notificationEvent.getFirstSubject();
				ConfigGroup group = null;
				try {
					ConfigManager configManager = ConfigManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
					group = (ConfigGroup)configManager.getConfig(configID, DEFAULT_FETCH_GROUP_GROUPS);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				Cache.sharedInstance().put(null, group, DEFAULT_FETCH_GROUP_GROUPS);
				
				for (Iterator iter = configSetupsByType.values().iterator(); iter.hasNext();) {
					ConfigSetup setup = (ConfigSetup) iter.next();
					if (setup.getConfigGroupsByID().get(configID) == null)
						continue;					
					setup.getConfigGroupsMap().put(group.getConfigKey(), group);
					setup.getConfigGroupsByID().put(configID, group);
					for (Iterator iterator = setup.getConfigs().iterator(); iterator.hasNext();) {
						Config config = (Config) iterator.next();
						if (config.getConfigGroup() == null)
							continue;
						if (configID.equals(JDOHelper.getObjectId(config.getConfigGroup())))
							config.setConfigGroup(group);
					}
				}
			}
		}
	};  
	
	/**
	 * Returns a ConfigSetupVisualiser for the ConfigSetup of the given
	 * configSetupType or null if none can be found.
	 */
	public ConfigSetupVisualiser getVisualiser(String configSetupType) {
		return (ConfigSetupVisualiser)setupVisualiserByType.get(configSetupType);
	}
	
	/**
	 * Returns the visualiser assosiated to the ConfigSetup the given Config
	 * is part of, or null if it can't be found. 
	 */
	public ConfigSetupVisualiser getVisualiserForConfig(ConfigID configID) {
		ConfigSetup setup = getConfigSetupForConfigType(configID);
		if (setup == null)
			return null;
		return getVisualiser(setup.getConfigSetupType());
	}

	private static ConfigSetupRegistry sharedInstance;

	public static ConfigSetupRegistry sharedInstance() {
		if (sharedInstance == null) {
			sharedInstance = new ConfigSetupRegistry();
//			ChangeManager.sharedInstance().addNotificationListener(ConfigSetup.class, sharedInstance.setupChangeListener);
			ChangeManager.sharedInstance().addNotificationListener(Config.class, sharedInstance.configChangeListener);
			ChangeManager.sharedInstance().addNotificationListener(ConfigGroup.class, sharedInstance.configGroupChangeListener);
			try {
				sharedInstance.process();
			} catch (EPProcessorException e) {
				throw new RuntimeException(e);
			}
		}
		return sharedInstance;
	}

	public String getExtensionPointID() {
		return EXTENSION_POINT_ID;
	}

	public void processElement(IExtension extension, IConfigurationElement element) throws EPProcessorException {
		if (element.getName().equals("visualiser")) {
			String configSetupType = element.getAttribute("configSetupType");
			if (configSetupType == null || "".equals(configSetupType))
				throw new EPProcessorException("Attribute configSetupType is invalid for a configsetupvisualiser");
			ConfigSetupVisualiser visualiser = null;
			try {
				visualiser = (ConfigSetupVisualiser)element.createExecutableExtension("class");
			} catch (CoreException e) {
				throw new EPProcessorException("Could not instatiate ConfigSetupVisualiser",e);
			}
			setupVisualiserByType.put(configSetupType, visualiser);
		}
	}
	
}
