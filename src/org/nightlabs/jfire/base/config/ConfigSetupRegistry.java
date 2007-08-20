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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.nightlabs.base.extensionpoint.AbstractEPProcessor;
import org.nightlabs.base.extensionpoint.EPProcessorException;
import org.nightlabs.base.notification.NotificationAdapterJob;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.dao.ConfigSetupDAO;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.config.id.ConfigSetupID;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ConfigSetupRegistry extends AbstractEPProcessor 
{
	private static final String CLASS_ELEMENT = "class"; //$NON-NLS-1$
	private static final String CONFIG_SETUP_TYPE_ELEMENT = "configSetupType"; //$NON-NLS-1$
	private static final String VISUALISER_ELEMENT = "visualiser"; //$NON-NLS-1$

	public static final String EXTENSION_POINT_ID = "org.nightlabs.jfire.base.configsetupvisualiser"; //$NON-NLS-1$
	
	private static final String[] CONFIG_SETUP_FETCH_GROUPS = new String[] 
	  { FetchPlan.DEFAULT, ConfigSetup.FETCH_GROUP_CONFIG_MODULE_CLASSES };
//	private static final String[] DEFAULT_FETCH_GROUP_CONFIGS = new String[]
//    { FetchPlan.DEFAULT, Config.FETCH_GROUP_CONFIG_GROUP };
	
	/**
	 * IMPORTANT: The following registry does only work correctly if the following properties are true...<br>
	 * 
	 * <p>There can be at most one ConfigSetup linked to a given <code>Objectclass</code> in the JDO-Datastore.</p>
	 * <p>This means that there is at most one ConfigSetup with the <code>configType</code> (exclusive)or 
	 * 		<code>groupConfigType</code> equal to <code>Objectclass</code>.</p> 
	 */
	
	/**
	 * key: String ConfigSetup.configType
	 * value: ConfigSetup configSetup
	 */
	private Map<String, ConfigSetup> configSetupsByType = null;
	
	/**
	 * key: String ConfigSetup.groupConfigType
	 */
	private Map<String, ConfigSetup> configSetupsByGroupType = null;
	
	/**
	 * key: String configSetupType
	 * value: ConfigSetupVisualiser setupVisualiser
	 */
	private Map<String, ConfigSetupVisualiser> setupVisualiserByType = new HashMap<String, ConfigSetupVisualiser>();
	
	/**
	 * key: String configSetupType
	 * value: ConfigPreferencesNode mergedTreeNode
	 */
	private Map<String, ConfigPreferenceNode> mergedTreeNodes = new HashMap<String, ConfigPreferenceNode>();
	
	/**
	 * 
	 */
	public ConfigSetupRegistry() {
		super();
	}
	
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
		
		return configSetupsByType.get(configSetupType);
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
		return configSetupsByGroupType.get(configGroupID.configType);
	}
	
	/**
	 * Returns the ConfigID if the given ConfigID corresponds to a Config, which is
	 * a member of a group. Otherwise <code>null</code> is returned.
	 * @param configID the ConfigID of the Config, whose ConfigGroup's ID is wanted.
	 * @return the ID of the group of the corresponding Config of the given ConfigID.
	 */
//	public ConfigID getGroupForConfig(ConfigID configID) {
//		ConfigSetup setup = getConfigSetupForConfigType(configID);
//		Config config = (Config) setup.getConfigsMap().get(configID);
//		if (config != null && config.getConfigGroup() != null)			
//			return (ConfigID) JDOHelper.getObjectId(config.getConfigGroup());
//		return null;
//	}
		
	
	/**
	 * Checks whether a ConfigSetup is registered that links Configs to Objects
	 * of the given linkClassName.
	 * 
	 * @param linkClassName The classname of the linked objects.
	 * @return Whether there is a ConfigSetup registered that links objects of the 
	 * given class.
	 */
	public boolean containsRegistrationForLinkClass(String linkClassName) {
		return getConfigSetupForConfigType(linkClassName) != null;
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
		return getConfigSetupForConfigType(configID.configType);
	}
	
	/**
	 * Returns the ConfigSetup, which links to the Object whose classname == <code>configType</code>.
	 * 
	 * @param configType the configType(full classname) of the Object to check for Linkage of ConfigSetups.
	 * @return the ConfigSetup, which links to the Object with classname == <code>configType</code>.
	 */
	public ConfigSetup getConfigSetupForConfigType(String configType) {
		if (configType == null || "".equals(configType)) //$NON-NLS-1$
			throw new IllegalArgumentException("Parameter configType must not be null or empty!"); //$NON-NLS-1$
		
		if (configSetupsByType == null)
			getConfigSetups();
		
		ConfigSetup tmpSetup = configSetupsByType.get(configType);
		if (tmpSetup != null)
			return tmpSetup;
		
		return configSetupsByGroupType.get(configType);
	}
	
	/**
	 * Checks whether the given ConfigID is the id-object of a ConfigGroup.
	 */
	public boolean isConfigGroup(ConfigID configID) {		
		return getConfigSetupForGroup(configID) != null;
	}
	
	/**
	 * Checks whether a ConfigSetup is registered that has a Config linked to 
	 * the given linkObject.
	 * 
	 * @param linkObject The object to check linkage for.
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
	public ConfigPreferenceNode getMergedPreferenceRootNode(String scope, ConfigID configID)
	throws NoSetupPresentException
	{
		ConfigSetup setup = getConfigSetupForConfigType(configID);
		if (setup == null)
			throw new NoSetupPresentException("No Setup found related to this configID: "+configID); //$NON-NLS-1$
		
		ConfigPreferenceNode rootNode = mergedTreeNodes.get(scope+setup.getConfigSetupType());
		if (rootNode != null)
			return rootNode;
		ConfigPreferenceNode registeredRootNode = ConfigPreferencePageRegistry.sharedInstance().getPreferencesRootNode();
		rootNode = new ConfigPreferenceNode("", "", "", null, null, null, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		Set<String> mergeModules = new HashSet<String>();
		mergeModules.addAll(setup.getConfigModuleClasses());		

		for (Iterator iter = registeredRootNode.getChildren().iterator(); iter.hasNext();) {
			ConfigPreferenceNode childNode = (ConfigPreferenceNode) iter.next();
			// recursively merge
			mergeSetupNodes(setup, mergeModules, childNode, rootNode);
		}

		// for all remaining classes add a null-Node
		for (Iterator iter = mergeModules.iterator(); iter.hasNext();) {
			String moduleClassName = (String) iter.next();
			ConfigPreferenceNode node = new ConfigPreferenceNode("", moduleClassName, "", rootNode, null, null, null); //$NON-NLS-1$ //$NON-NLS-2$
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
			Set<String> mergeModules, 
			ConfigPreferenceNode orgNode,
			ConfigPreferenceNode newNodeParent) 
	{
		String nodeClassName = orgNode.getConfigModuleClass() != null ? orgNode.getConfigModuleClass().getName() : "";  //$NON-NLS-1$
		boolean hasRegistration = setup.getConfigModuleClasses().contains(nodeClassName); 
//			(orgNode.createPreferencePage() != null) && 
		if (hasRegistration) {
			mergeModules.remove(nodeClassName);
			ConfigPreferenceNode newNode = new ConfigPreferenceNode(
					orgNode.getConfigPreferenceID(),
					orgNode.getConfigPreferenceName(),
					orgNode.getCategoryID(),
					newNodeParent,
					orgNode.getElement(),
					orgNode.getPreferencePage(),
					null // FIXME: insert here the modID stuff?
				);			
			newNodeParent.addChild(newNode);
			for (Iterator iter = orgNode.getChildren().iterator(); iter.hasNext();) {
				ConfigPreferenceNode child = (ConfigPreferenceNode) iter.next();
				mergeSetupNodes(setup, mergeModules, child, newNode);
			}
		}
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
			configSetupsByType = new HashMap<String, ConfigSetup>();
		if (configSetupsByGroupType == null)
			configSetupsByGroupType = new HashMap<String, ConfigSetup>();
		
		// add the setup to the
		configSetupsByType.put(setup.getConfigType(), setup);
		configSetupsByGroupType.put(setup.getConfigGroupType(), setup);
				
		mergedTreeNodes.remove(setup.getConfigSetupType()); // FIXME: why removing this?
	}
	
	private void getConfigSetups() {
		if (configSetupsByType != null)
			return;
		Collection setups = ConfigSetupDAO.sharedInstance().getAllConfigSetups(
				CONFIG_SETUP_FETCH_GROUPS, 
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, 
				new NullProgressMonitor());

		for (Iterator iter = setups.iterator(); iter.hasNext();) {
			ConfigSetup setup = (ConfigSetup) iter.next();
			integrateConfigSetup(setup);
		}
	}
	
	/**
	 * Listener for changes of ConfigSetups.
	 */
	private NotificationListener setupChangeListener = new NotificationAdapterJob() {
		public void notify(NotificationEvent notificationEvent) {
			if (notificationEvent.getFirstSubject() instanceof DirtyObjectID) {
				DirtyObjectID dirtyObjectID = (DirtyObjectID) notificationEvent.getFirstSubject();
				if (dirtyObjectID.getObjectID() instanceof ConfigSetupID) {
					ConfigSetupID setupID = (ConfigSetupID)dirtyObjectID.getObjectID();				
					try {
						ConfigSetup newSetup = ConfigSetupDAO.sharedInstance().getConfigSetup(setupID, 
								CONFIG_SETUP_FETCH_GROUPS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, getProgressMonitorWrapper());
						integrateConfigSetup(newSetup);	
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	};  
	
//	/**
//	 * Listener for changes of Configs
//	 */
//	private NotificationListener configChangeListener = new NotificationAdapterJob() {
//		public void notify(NotificationEvent notificationEvent) {
//			if (notificationEvent.getFirstSubject() instanceof ConfigID) {
//				ConfigID configID = (ConfigID)notificationEvent.getFirstSubject();
//				Class jdoObjectClass = JDOObjectID2PCClassMap.sharedInstance().getPersistenceCapableClass(configID);
//				if (jdoObjectClass != Config.class)
//					return;
//				Config config = null;
//				try {
//					ConfigManager configManager = ConfigManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
//					config = configManager.getConfig(configID, DEFAULT_FETCH_GROUP_CONFIGS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
//				} catch (Exception e) {
//					throw new RuntimeException(e);
//				}
//				Cache.sharedInstance().put(null, config, DEFAULT_FETCH_GROUP_CONFIGS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
//				for (Iterator iter = configSetupsByType.values().iterator(); iter.hasNext();) {
//					ConfigSetup setup = (ConfigSetup) iter.next();
//					if (setup.getConfigsMap().get(configID) == null)
//						continue;
//					setup.getConfigsMap().put(configID, config);
//					if (config.getConfigGroup() != null)
//						config.setConfigGroup(setup.getConfigGroup(config.getConfigGroup().getConfigKey()));
//				}
//			}
//		}
//	};  

//	/**
//	 * Listener for changes of ConfigGroups
//	 */
//	private NotificationListener configGroupChangeListener = new NotificationListenerWorkerThreadAsync() {
//		public void notify(NotificationEvent notificationEvent) {
//			if (notificationEvent.getFirstSubject() instanceof ConfigID) {
//				ConfigID configID = (ConfigID)notificationEvent.getFirstSubject();
//				ConfigGroup group = null;
//				try {
//					ConfigManager configManager = ConfigManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
//					group = (ConfigGroup)configManager.getConfig(configID, DEFAULT_FETCH_GROUP_GROUPS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
//				} catch (Exception e) {
//					throw new RuntimeException(e);
//				}
//				Cache.sharedInstance().put(null, group, DEFAULT_FETCH_GROUP_GROUPS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
//				
//				for (Iterator iter = configSetupsByType.values().iterator(); iter.hasNext();) {
//					ConfigSetup setup = (ConfigSetup) iter.next();
//					if (setup.getConfigGroupsByID().get(configID) == null)
//						continue;					
//					setup.getConfigGroupsMap().put(group.getConfigKey(), group);
//					setup.getConfigGroupsByID().put(configID, group);
//					for (Iterator iterator = setup.getConfigs().iterator(); iterator.hasNext();) {
//						Config config = (Config) iterator.next();
//						if (config.getConfigGroup() == null)
//							continue;
//						if (configID.equals(JDOHelper.getObjectId(config.getConfigGroup())))
//							config.setConfigGroup(group);
//					}
//				}
//			}
//		}
//	};  
	
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
			JDOLifecycleManager.sharedInstance().addNotificationListener(ConfigSetup.class, sharedInstance.setupChangeListener);
//			JDOLifecycleManager.sharedInstance().addNotificationListener(Config.class, sharedInstance.configChangeListener);
//			JDOLifecycleManager.sharedInstance().addNotificationListener(ConfigGroup.class, sharedInstance.configGroupChangeListener);
			// FIXME: where are these listeners deregistered?
			sharedInstance.process();
		}
		return sharedInstance;
	}

	public String getExtensionPointID() {
		return EXTENSION_POINT_ID;
	}

	public void processElement(IExtension extension, IConfigurationElement element) throws Exception {
		if (element.getName().equals(VISUALISER_ELEMENT)) {
			String configSetupType = element.getAttribute(CONFIG_SETUP_TYPE_ELEMENT);
			if (configSetupType == null || "".equals(configSetupType)) //$NON-NLS-1$
				throw new EPProcessorException("Attribute configSetupType is invalid for a configsetupvisualiser"); //$NON-NLS-1$
			ConfigSetupVisualiser visualiser = null;
			try {
				visualiser = (ConfigSetupVisualiser)element.createExecutableExtension(CLASS_ELEMENT);
			} catch (CoreException e) {
				throw new EPProcessorException("Could not instatiate ConfigSetupVisualiser",e); //$NON-NLS-1$
			}
			setupVisualiserByType.put(configSetupType, visualiser);
		}
	}
	
	public class NoSetupPresentException extends Exception {
		private static final long serialVersionUID = 1L;

		public NoSetupPresentException() {
			super();
		}
		
		public NoSetupPresentException(String message) {
			super(message);
		}
		
		public NoSetupPresentException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
