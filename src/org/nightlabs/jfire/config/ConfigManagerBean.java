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

package org.nightlabs.jfire.config;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.config.ConfigModuleNotFoundException;
import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.asyncinvoke.AsyncInvoke;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.JFireBaseEAR;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.config.id.ConfigModuleID;
import org.nightlabs.jfire.config.id.ConfigSetupID;
import org.nightlabs.jfire.config.xml.XMLConfigFactory;
import org.nightlabs.jfire.editlock.EditLockType;
import org.nightlabs.util.CollectionUtil;

/**
 * @ejb.bean name="jfire/ejb/JFireBaseBean/ConfigManager"
 *	jndi-name="jfire/ejb/JFireBaseBean/ConfigManager"
 *	type="Stateless"
 *
 * @ejb.permission role-name = "_Guest_"
 *
 * @ejb.util generate = "physical"
 **/
public abstract class ConfigManagerBean extends BaseSessionBeanImpl implements SessionBean 
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(ConfigManagerBean.class);

	/**
	 * @ejb.create-method
	 */
	public void ejbCreate() throws CreateException
	{
	}
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 *
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException { }
	
	
	/**
	 * Stores the given ConfigModule. If it is a ConfigModule belonging to a
	 * ConfigGroup, the corresponding ConfigModules of all members of this group
	 * will inherit from the given ConfigModule according to its
	 * inheritence settings.
	 *  
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public ConfigModule storeConfigModule(ConfigModule configModule, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try 
		{
			if (!getOrganisationID().equals(configModule.getOrganisationID()))
				throw new IllegalArgumentException("Attempt to store ConfigModule from a different organisation "+configModule.getOrganisationID());
			if (!JDOHelper.isDetached(configModule))
				throw new IllegalArgumentException("Pass only detached ConfigModules to this method.");
			
			// All this could be checked by configModule.isGroupAllowsOver

			if (! configModule.isGroupConfigModule()) {
				// check if module is allowed to be stored
				Config config = Config.getConfig(pm, configModule.getOrganisationID(), configModule.getConfigKey(), configModule.getConfigType());
				ConfigGroup group = config.getConfigGroup();
				if (group != null) {
					ConfigModule groupConfigModule = group.getConfigModule(configModule.getClass(), configModule.getCfModID(), false);					
					if (groupConfigModule != null && (groupConfigModule.getFieldMetaData(ConfigModule.class.getName()).getWritableByChildren() & FieldMetaData.WRITABLEBYCHILDREN_YES) == 0)
						throw new IllegalArgumentException("This ConfigModule is not allowed to be stored. It's ConfigGroup "+config.getConfigGroup().getName()+" does not allow this");
				}
			}
//			else {
//
//				// Workaround for a Cache bug, in which the cache misses to notify all objects having a modified 
//				// FieldMetaData in their object graph.
//				// This simply notifies all member ConfigModules.
//				ConfigGroup configGroup = (ConfigGroup) 
//				Config.getConfig(pm, configModule.getOrganisationID(), configModule.getConfigKey(), configModule.getConfigType());
//
//				Collection members = Config.getConfigsForGroup(pm, configGroup);
//				for (Iterator iter = members.iterator(); iter.hasNext();) {
//					Config member = (Config) iter.next();
//					ConfigModule memberModule = member.createConfigModule(configModule.getClass(), configModule.getCfModID());
//					JDOHelper.makeDirty(memberModule, ConfigModule.FIELD_NAME_FIELDMETADATAMAP);
////					if (JDOHelper.isDetached(memberModule))
////					pm.makePersistent(memberModule);
//				}
//			}
		
			ConfigModule pConfigModule = (ConfigModule)pm.makePersistent(configModule);
			
			if (pConfigModule.getConfig() instanceof ConfigGroup) {
				// is a ConfigModule of a ConfigGroup -> inherit all ConfigModules for 
				// all its members
				ConfigGroup configGroup = (ConfigGroup)pConfigModule.getConfig();
				ConfigModule.inheritAllGroupMemberConfigModules(pm, configGroup, pConfigModule);
				
				// Workaround for a Cache bug, in which the cache misses to notify all objects having a modified 
				//  FieldMetaData in their object graph.
				// This simply notifies all member ConfigModules.
				Collection members = Config.getConfigsForGroup(pm, configGroup);
				for (Iterator iter = members.iterator(); iter.hasNext();) {
					Config member = (Config) iter.next();
					ConfigModule memberModule = member.createConfigModule(configModule.getClass(), configModule.getCfModID());
					if (memberModule.getFieldMetaData(ConfigModule.FIELD_NAME_FIELDMETADATA_CONFIGMODULE).isValueInherited())
						JDOHelper.makeDirty(memberModule, ConfigModule.FIELD_NAME_FIELDMETADATAMAP);
				}
				// END of workaround
			}
			
			if (!get)
				return null;
			else {
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);
				return (ConfigModule)pm.detachCopy(pConfigModule);
			}
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Returns all ConfigGroups with the given configType. If the parameter
	 * is null all ConfigGroups will be returned.
	 * 
	 * @param configType If set, all ConfigGroups with the given configType
	 * will be returned. If null, all ConfigGroups will be returned
	 * @param fetchGroups The fetch-groups to be used to detach the found groups
	 *  
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required" 
	 * 
	 */
	public Collection getConfigGroups(String configType, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try 
		{
			Query q = pm.newQuery(ConfigGroup.class);
			if (configType != null)
				q.setFilter("this.configType == \""+configType+"\"");
			
			Collection groups = (Collection)q.execute();

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			
			Collection result = pm.detachCopyAll(groups);
			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Get all ConfigGroups.
	 * 
	 * @see #getConfigGroups(String, String[])
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Collection getConfigGroups(String[] fetchGroups, int maxFetchDepth) 
	throws ModuleException
	{
		return getConfigGroups(null, fetchGroups, maxFetchDepth);
	}
	
	
	/**
	 * Returns all Configs corresponding to the given Set of ConfigIDs.
	 * 
	 * @param configIDs The Set of ConfigIDs corresponding to the desired Configs. 
	 * @param fetchGroups The fetch-groups to be used to detach the found Configs
	 * @throws ModuleException 
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Collection<Config> getConfigs(Set<ConfigID> configIDs, String[] fetchGroups, int maxFetchDepth) throws ModuleException
	{
		if (configIDs == null)
			throw new IllegalArgumentException("The Set of ConfigIDs must not be null!");
		
		PersistenceManager pm;
		pm = getPersistenceManager();
		try 
		{
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			
			Collection<Config> configs = CollectionUtil.castCollection(pm.getObjectsById(configIDs));
			
			return CollectionUtil.castCollection(pm.detachCopyAll(configs));
			
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Returns all Configs with the given configType. If the parameter
	 * is null all Configs will be returned. 
	 * {@link Config#getConfigsByType(PersistenceManager, String, String)}
	 * 
	 * @param configType If set, all Configs with the given configType
	 * will be returned. If null, all Configs will be returned
	 * @param fetchGroups The fetch-groups to be used to detach the found Configs
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Set<Config> getConfigs(String configType, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		ConfigSetup.ensureAllPrerequisites(pm);
		
		pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
		if (fetchGroups != null)
			pm.getFetchPlan().setGroups(fetchGroups);
		
		Collection<Config> queryResult = Config.getConfigsByType(pm, getOrganisationID(), configType);
		
		return NLJDOHelper.getDetachedQueryResultAsSet(pm, queryResult);
	}
	
	/**
	 * Returns all ConfigIDs corresponding to Configs of the given <code>configType</code>.
	 * {@link Config#getConfigIDsByConfigType(PersistenceManager, String, String)}
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name = "_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Set<ConfigID> getConfigIDsByConfigType(String configType, 
			String[] fetchGroups,	int maxFetchDepth) {
		PersistenceManager pm = getPersistenceManager();
		ConfigSetup.ensureAllPrerequisites(pm);
		pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
		if (fetchGroups != null)
			pm.getFetchPlan().setGroups(fetchGroups);
		Collection<ConfigID> queryResult = Config.getConfigIDsByConfigType(pm, getOrganisationID(), configType);
		
		return NLJDOHelper.getDetachedQueryResultAsSet(pm, queryResult);
	}
	
	/**
	 * Get all Configs.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Collection getConfigs(String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		return getConfigs((String)null, fetchGroups, maxFetchDepth);
	}
	
	/**
	 * Get a certain Config.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Config getConfig(ConfigID configID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try 
		{
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			
			Config config = (Config)pm.getObjectById(configID);
			
			return (Config)pm.detachCopy(config);
			
		} finally {
			pm.close();
		}
	}
	
	
	/* *********************************************************************** */
	/* ************************** getConfigModule stuff ********************** */
	/* *********************************************************************** */

	/**
	 * Searches the ConfigModule of the given Config, cfModClass and cfModID.
	 * If not found it will be autocreated.
	 * 
	 * @param userConfigID The UserConfigID the returned ConfigModule should belong to
	 * @param cfModClass The ConfigModule's class
	 * @param cfModID The ConfigModules cfModID (suffix)
	 * @param fetchGroups The fetch-groups to be used to detach the ConfigModule
	 * @return The ConfigModule of the given userConfig, cfModClass and cfModID
	 * @throws ModuleException
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public ConfigModule getConfigModule(ConfigID configID, Class cfModClass, String cfModID, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		return getConfigModule(getPersistenceManager(), configID, cfModClass, cfModID, fetchGroups, maxFetchDepth);
	}
	
	/**
	 * Helper Method returning the ConfigModule corresponding to the given ConfigModuleID and if non-existant
	 * creates a new ConfigModule and if necessary Config, too.
	 * 
	 * @param pm the {@link PersistenceManager} to use 
	 * @param configID the ConfigID of the Config, which shall contain the searched ConfigModule.
	 * @param cfModClass The ConfigModule's class
	 * @param cfModID The ConfigModules cfModID (suffix)
	 * @param fetchGroups The fetch-groups to be used to detach the ConfigModule
	 * @return The ConfigModule of the given userConfig, cfModClass and cfModID
	 * @throws ModuleException
	 */
	protected ConfigModule getConfigModule(PersistenceManager pm, ConfigID configID, Class cfModClass, String cfModID, 
			String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		try 
		{
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			
			ConfigSetup.ensureAllPrerequisites(pm);
			Config config = (Config)pm.getObjectById(configID);
			return getCreateConfigModule(pm, config, cfModClass, cfModID, fetchGroups, maxFetchDepth);
		} catch(Exception e) {
			throw new RuntimeException("Could not download ConfigModules!\n", e);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Searches the ConfigModule for the given keyObject and if there is none, one is created.
	 * 
	 * @param keyObjectID The ObjectID of the Object the Config holding the ConfigModule is assigned to. 
	 * @param cfModClass The classname of the ConfigModule desired
	 * @param cfModID The cfModID of the ConfigModule desired
	 * @param fetchGroups The fetch-groups to detach the returned ConfigModule with
	 * @return
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public ConfigModule createConfigModule(
			ObjectID keyObjectID, Class cfModClass, String cfModID,
			String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try 
		{
			ConfigSetup.ensureAllPrerequisites(pm);
			Object keyObject = pm.getObjectById(keyObjectID);
			Config config = Config.getConfig(pm, getOrganisationID(), keyObject); // Config is autocreated, if necessary

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			ConfigModule configModule = config.createConfigModule(cfModClass, cfModID);
			return (ConfigModule)pm.detachCopy(configModule);
		} finally {
			pm.close();
		}
	}

	/**
	 * Searches the ConfigModule for the given keyObject.
	 * 
	 * @param keyObjectID The ObjectID of the Object the Config holding the ConfigModule is assigned to. 
	 * @param cfModClass The classname of the ConfigModule desired
	 * @param cfModID The cfModID of the ConfigModule desired
	 * @param throwExceptionIfNotFound If <code>true</code> and the ConfigModule does not exist, a {@link ConfigModuleNotFoundException} will be thrown. If it's <code>false</code>, <code>null</code> will be returned instead of an exception.
	 * @param fetchGroups The fetch-groups to detach the returned ConfigModule with
	 * @return Returns <code>null</code> (if allowed) or an instance of a class extending ConfigModule.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public ConfigModule getConfigModule(
			ConfigID keyObjectID, Class cfModClass, String cfModID, boolean throwExceptionIfNotFound,
			String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try 
		{
			ConfigSetup.ensureAllPrerequisites(pm);
			Object keyObject = pm.getObjectById(keyObjectID);
			Config config = Config.getConfig(pm, getOrganisationID(), keyObject); // Config is autocreated, if necessary
			
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			ConfigModule configModule = config.getConfigModule(cfModClass, cfModID, throwExceptionIfNotFound);
			if (configModule == null)
				return null;

			return (ConfigModule)pm.detachCopy(configModule);

		} finally {
			pm.close();
		}
	}

	/**
	 * Returns a Collection of {@link ConfigModule}s, corresponding to the set of {@link ConfigModuleID}s.
	 * If a corresponding ConfigModule doesn't exists it and its surrounding {@link Config} are created.
	 * 
	 * @param moduleIDs the set of {@link ConfigModuleID}s for which to gather the corresponding {@link ConfigModule}s
	 * @param fetchGroups the FetchGroups for the detached {@link ConfigModule}s
	 * @param maxFetchDepth the maximum fetch depth for the detached ConfigModules.
	 * @return a Collection of {@link ConfigModule}s, corresponding to the set of {@link ConfigModuleID}s.
	 * @throws ModuleException a wrapper for many kinds of Exceptions.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Collection<ConfigModule> getConfigModules(Set<ConfigModuleID> moduleIDs, String[] fetchGroups, int maxFetchDepth) 
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			ArrayList<ConfigModule> searchedModules = new ArrayList<ConfigModule>(moduleIDs.size());

			for (ConfigModuleID moduleID : moduleIDs)
				searchedModules.add((ConfigModule)pm.detachCopy(Config.getConfigModule(pm, moduleID))); 

			return searchedModules;
		} finally {
			pm.close();
		}
	}

	/**
	 * Helper method for the other getConfigModule methods, which searches for ConfigModule 
	 * corresponding to the given cfModID and if it doesn't exist creates one. 
	 */
	protected ConfigModule getCreateConfigModule(PersistenceManager pm, Config config, Class cfModClass, String cfModID, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		logger.debug("config.organisatinID "+config.getOrganisationID());
		ConfigModule configModule = config.createConfigModule(cfModClass, cfModID);

		logger.debug("Have configmodule: "+configModule);
		logger.debug("configModule.organisationID: "+configModule.getOrganisationID());
		logger.debug("configModule.configType: "+configModule.getConfigType());
		logger.debug("configModule.configKey: "+configModule.getConfigKey());
		logger.debug("configModule.cfModID: "+configModule.getCfModID());
		logger.debug("configModule.cfModKey: "+configModule.getCfModKey());
		
		return (ConfigModule)pm.detachCopy(configModule);
	}
	
	/**
	 * Returns the ConfigModule of the ConfigGroup of the Config corresponding to the given ConfigID
	 * and with the given Class and moduleID.
	 * 
	 * @param childID the {@link ConfigID} of the child's {@link Config}.
	 * @param configModuleClass the Class of the ConfigModule to return.
	 * @param moduleID the module ID in the case there is more than one instance of that ConfigModule. 
	 * @param fetchGroups the fetchGroups with which to detach the ConfigModule.
	 * @param maxFetchDepth the maximum fetch depth while detaching.
	 * @return the ConfigModule of the ConfigGroup of the Config corresponding to the given ConfigID
	 * and with the given Class and moduleID.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public ConfigModule getGroupConfigModule(ConfigID childID, Class configModuleClass, String moduleID, 
			String[] fetchGroups, int maxFetchDepth) throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		if (fetchGroups != null)
			pm.getFetchPlan().setGroups(fetchGroups);
		pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
		
		try {
			Config config = (Config) pm.getObjectById(childID);
			if (config == null)
				throw new ModuleException("There is no corresponding Config to the given ConfigID!");
			
			if (config.getConfigGroup() == null)
				return null; // just return null to enable the client to check whether there is a group or not.
			
			ConfigGroup group = config.getConfigGroup();
			ConfigModule groupsModule = group.getConfigModule(configModuleClass, moduleID);
			return (ConfigModule) pm.detachCopy(groupsModule);
		} finally {
			pm.close();
		}
	}

	/* *********************************************************************** */
	/* ************************** ConfigSetup stuff ************************** */
	/* *********************************************************************** */
	
	/**
	 * Add a new ConfigGroup with ge given configKey, the given groupType as 
	 * configType and the given groupName.
	 * 
	 * @param get If true the new ConfigGroup will be detached and returned. If
	 * false null is returned.
	 * @param fetchGroups The fetch-groups to be used to detach the returned group 
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public ConfigGroup addConfigGroup(String configKey, String groupType, String groupName, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{		
		PersistenceManager pm;
		pm = getPersistenceManager();
		try 
		{			
			ConfigGroup group = new ConfigGroup(getOrganisationID(), configKey, groupType);
			group.setName(groupName);
			pm.makePersistent(group);
			
			if (get) {
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);

				ConfigGroup result = (ConfigGroup)pm.detachCopy(group);
				
				ConfigSetup.ensureAllPrerequisites(pm);
				
				// TODO: Make the ConfigSetup dirty that has the given groupType
				
				return result;
			}
			return null;
		} finally {
			pm.close();
		}
	}
	
// This method did not detach its result, it would not have worked!	
//	/**
//	 * Returns a Collection of all ConfigSetups known. 
//	 * 
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 * @ejb.transaction type = "Required"
//	 */
//	public Collection getConfigSetups(String[] fetchGroups, int maxFetchDepth)
//	throws ModuleException
//	{		
//		PersistenceManager pm = getPersistenceManager();
//		try	{		
//			if (fetchGroups != null)
//				pm.getFetchPlan().setGroups(fetchGroups);
//			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
//				
//			return ConfigSetup.getConfigSetups(pm);
//			
//		} finally {
//			pm.close();
//		}
//	}
	
// Duplicate method. = getConfigSetup	
//	/**
//	 * Returns a complete ConfigSetup of the given type. A complete
//	 * ConfigSetup contains all Configs and ConfigGroups of that setup.
//	 * 
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 * @ejb.transaction type = "Required"
//	 */
//	public ConfigSetup getCompleteConfigSetup(
//			String configSetupType, 
//			String[] groupsFetchGropus, 
//			String[] configsFetchGroups
//		)
//	throws ModuleException
//	{		
//		PersistenceManager pm;
//		pm = getPersistenceManager();
//		try 
//		{		
//			return 
//				ConfigSetup.getConfigSetup(pm, getOrganisationID(), configSetupType)
//					.getCompleteConfigSetup(pm, getOrganisationID(), groupsFetchGropus, configsFetchGroups);
//		} finally {
//			pm.close();
//		}
//	}
	
	/**
	 * Returns the ConfigSetup with the given configSetupID.
	 * <p>
	 * The {@link ConfigSetup} will hereby be prepared by
	 * its {@link ConfigSetup#getCompleteConfigSetup(PersistenceManager, String, String[], String[])}.
	 * </p>
	 * 
	 * @param configSetupID The id of the {@link ConfigSetup} to return.
	 * @param groupsFetchGropus Fetch-groups to detach the {@link ConfigGroup}s with, that are part of the setup.
	 * 		If this is <code>null</code>, {@link FetchPlan#DEFAULT} will be used.
	 * @param configsFetchGroups Fetch-groups to detach the {@link Config}s with, that are part of the setup.
	 * 		If this is <code>null</code>, {@link FetchPlan#DEFAULT} will be used.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public ConfigSetup getConfigSetup(
			ConfigSetupID configSetupID, 
			String[] groupsFetchGropus, int groupsMaxFetchDepth,
			String[] configsFetchGroups, int configsMaxFetchDepth
		)
	throws ModuleException
	{		
		PersistenceManager pm;
		pm = getPersistenceManager();
		try 
		{			
			ConfigSetup configSetup = (ConfigSetup) pm.getObjectById(configSetupID);
			return 
				configSetup
					.getCompleteConfigSetup(
							pm, 
							groupsFetchGropus, groupsMaxFetchDepth, 
							configsFetchGroups, configsMaxFetchDepth);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Returns a list of ConfigSetups for the given {@link ConfigSetupID}s.
	 * 
	 * @param setupIDs The set of {@link ConfigSetupID}s for which to retrieve the corresponding ConfigSetups.
	 * @param fetchGroups The fetch groups with which to detach the object.
	 * @param maxFetchDepth The maximal fetch depth. 
	 * @return a list of ConfigSetups for the given {@link ConfigSetupID}s.
	 * 
	 * @ejb.interface-method 
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public List<ConfigSetup> getConfigSetups(Set<ConfigSetupID> setupIDs, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, setupIDs, ConfigSetup.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Returns the {@link ConfigSetupID}s of all ConfigSetups.
	 *  
	 * @param fetchGroups the fetch groups, with which the ConfigSetups shall be detached.
	 * @param maxFetchDepth the maximum fetch depth to use for detaching. 
	 * @return the {@link ConfigSetupID}s of all ConfigSetups.
	 * 
	 * @ejb.interface-method 
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Collection<ConfigSetupID> getAllConfigSetupIDs() {
		PersistenceManager pm = getPersistenceManager();
		try {
			return ConfigSetup.getAllConfigSetupIDs(pm);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Stores the given ConfigSetup if belonging to the current organisation and
	 * throws and exception if not.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public void storeConfigSetup(Collection setup)
	throws ModuleException
	{		
		PersistenceManager pm;
		pm = getPersistenceManager();
		try 
		{			
//			if (! getOrganisationID().equals(setup.getOrganisationID()) )
//					throw new IllegalArgumentException("The given ConfigSetup does not belong to the current organisation, but to "+setup.getOrganisationID());
			ConfigSetup.storeConfigSetup(pm, setup);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Stores the given {@link Config} and returns a detached copy if the caller requests it.
	 * @param config The {@link Config} to be stored.
	 * @param get A boolean indicating whether a detached copy of the stored {@link Config} should be returned.
	 * @param fetchGroups The fetch groups to be used
	 * @param maxFetchDepth The maximal fetch depth to be used
	 * @return A detached copy of the stored {@link Config} if <code>get == true</code> and <code>null</code> otherwise.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Config storeConfig(Config config, boolean get, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			config = pm.makePersistent(config);
			if (!get)
				return null;
			
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			
			return pm.detachCopy(config);
		} finally {
			pm.close();
		}
	}

	/**
	 * Applies the inheritence of a groupModule to the given ConfigModule if the Config corresponding to the 
	 * given ModuleID is in a ConfigGroup. Finally, returns a detached copy of the ConfigModule corresponding 
	 * to the given ConfigModuleID with the given <code>fetchGroups</code> and the given <code>maxFetchDepth</code>.   
	 * @param IDOfModuleToInherit ID of the ConfigModule, which shall inherit. 
	 * @param get Whether or not a detached Copy of the corresponding ConfigModule shall be returned. 
	 * @param fetchGroups FetchGroups of detached copy to return.
	 * @param maxFetchDepth FetchDepth of detached copy to return.
	 * @return a detached Copy of the ConfigModule of the given ConfigModuleID after applying group inheritence.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public ConfigModule applyGroupInheritence(ConfigModuleID IDOfModuleToInherit, boolean get, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm;
		pm = getPersistenceManager();
		try 
		{
			Set<String> fetchPlan = new HashSet<String>();
			if (fetchGroups != null)
				fetchPlan.addAll(Arrays.asList(fetchGroups));
			fetchPlan.add(ConfigModule.FETCH_GROUP_CONFIG);
			fetchPlan.add(Config.FETCH_GROUP_CONFIG_GROUP);
			pm.getFetchPlan().setGroups(fetchPlan);
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			
			ConfigModule moduleToUpdate = (ConfigModule) pm.getObjectById(IDOfModuleToInherit);

			if (!getOrganisationID().equals(moduleToUpdate.getOrganisationID()))
				throw new IllegalArgumentException("Attempt to store ConfigModule from a different organisation "+moduleToUpdate.getOrganisationID());
			
			// Get the config of the given module
			ConfigGroup config = moduleToUpdate.getConfig().getConfigGroup();
			// if given ConfigModule is in no group, simply return it
			if (config != null) {
				ConfigModule groupsModule = config.getConfigModule(moduleToUpdate.getClass());
				ConfigModule.inheritConfigModule(groupsModule, moduleToUpdate);				
			}
			fetchPlan.remove(ConfigModule.FETCH_GROUP_CONFIG);
			fetchPlan.remove(Config.FETCH_GROUP_CONFIG_GROUP);
			pm.getFetchPlan().setGroups(fetchPlan);
			
			if (get)
				return (ConfigModule) pm.detachCopy(moduleToUpdate);
			else
				return null;
		} finally {
			pm.close();
		}
	}

	/**
	 * Initializes the JFireBase Config-System.
	 * 
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	public void initialise() 
	throws ModuleException 
	{
// TODO remove debug stuff below
		try {
			for (int i = 0; i < 20; ++i) {
				AsyncInvoke.exec(new Invocation() {
					private static final long serialVersionUID = 1L;
					@Override
					public Serializable invoke() throws Exception
					{
						PersistenceManager pm = getPersistenceManager();
						try {
							Thread.sleep(10000);
							if (Math.random() >= 0.5)
								throw new RuntimeException("Test");

							return null;
						} finally {
							pm.close();
						}
					}
				}, true);
			}
		} catch (Exception e) {
			logger.warn("", e);
		}
// TODO remove debug stuff above

		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			// BEGIN XML-Config
			// The stuff for the XML config has nothing to do with the JFire config system. Instead, it makes
			// the XML-based config from NightLabsBase available in the server. This should only be used for
			// the DateFormatter and the NumberFormatter! Do not use it for anything else!!! Marco.
			System.setProperty(org.nightlabs.config.Config.PROPERTY_KEY_CONFIG_FACTORY, XMLConfigFactory.class.getName());
			// END XML-Config

			String organisationID = getOrganisationID();
			
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireBaseEAR.MODULE_NAME);
			if (moduleMetaData != null)
				return;

			logger.info("Initialization of Configuration in JFireBase started...");

			// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
			moduleMetaData = new ModuleMetaData(
					JFireBaseEAR.MODULE_NAME, "0.9.3-0-beta", "0.9.3-0-beta");
			pm.makePersistent(moduleMetaData);

			UserConfigSetup userConfigSetup = new UserConfigSetup(organisationID);
			pm.makePersistent(userConfigSetup);

			WorkstationConfigSetup workstationConfigSetup = new WorkstationConfigSetup(organisationID);
			pm.makePersistent(workstationConfigSetup);
			
			pm.makePersistent(new EditLockType(JFireBaseEAR.EDIT_LOCK_TYPE_ID_CONFIG_MODULE));
			pm.makePersistent(new EditLockType(JFireBaseEAR.EDIT_LOCK_TYPE_ID_CONFIG));
		} finally {
			pm.close();
		}
	}
	
	
}
