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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

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
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.JFireBaseEAR;
import org.nightlabs.jfire.config.id.ConfigID;

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
		PersistenceManager pm;
		pm = getPersistenceManager();
		try 
		{
			if (!getOrganisationID().equals(configModule.getOrganisationID()))
				throw new IllegalArgumentException("Attempt to store ConfigModule from a different organisation "+configModule.getOrganisationID());
			if (!JDOHelper.isDetached(configModule))
				throw new IllegalArgumentException("Pass only detached ConfigModules to this method.");
			
			ConfigModule pConfigModule = (ConfigModule)pm.makePersistent(configModule);
			
			if (pConfigModule.getConfig() instanceof ConfigGroup) {
				// is a ConfigModule of a ConfigGroup -> inherit all ConfigModules for 
				// all its members
				ConfigModule.inheritAllGroupMemberConfigModules(pm, (ConfigGroup)pConfigModule.getConfig(), pConfigModule);
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
	 * Returns all Configs with the given configType. If the parameter
	 * is null all Configs will be returned.
	 * 
	 * @param configType If set, all Configs with the given configType
	 * will be returned. If null, all Configs will be returned
	 * @param fetchGroups The fetch-groups to be used to detach the found Configs
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Collection getConfigs(String configType, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try 
		{
			ConfigSetup.ensureAllPrerequisites(pm);
			Query q = pm.newQuery(Config.class);
			if (configType != null)
				q.setFilter("this.configType == \""+configType+"\"");
			
			Collection configs = (Collection)q.execute();

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Collection result = pm.detachCopyAll(configs);
			return result;
		} finally {
			pm.close();
		}
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
		return getConfigs(null, fetchGroups, maxFetchDepth);
	}
	
	/**
	 * Get a certain Config.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Config getConfig(ConfigID configID, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
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
	 * 
	 */
	public ConfigModule getConfigModule(ConfigID configID, Class cfModClass, String cfModID, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try 
		{
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			else
				pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
			
			ConfigSetup.ensureAllPrerequisites(pm);
			Config config = (Config)pm.getObjectById(configID);
			return getConfigModule(pm, config, cfModClass, cfModID, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}	
	
	/**
	 * Helper method for the other getConfigModule methods 
	 */
	protected ConfigModule getConfigModule(PersistenceManager pm, Config config, Class cfModClass, String cfModID, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		logger.debug("config.organisatinID "+config.getOrganisationID());
		ConfigModule configModule = null;
		boolean groupAllowOverwrite = true;
		configModule = config.createConfigModule(cfModClass, cfModID);
//		configModule = ConfigModule.getAutoCreateConfigModule(pm, config, cfModClass, cfModID);

		logger.debug("Have configmodule: "+configModule);
		logger.debug("configModule.organisationID: "+configModule.getOrganisationID());
		logger.debug("configModule.configType: "+configModule.getConfigType());
		logger.debug("configModule.configKey: "+configModule.getConfigKey());
		logger.debug("configModule.cfModID: "+configModule.getCfModID());
		logger.debug("configModule.cfModKey: "+configModule.getCfModKey());
		
//		pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
//		if (fetchGroups != null)
//			pm.getFetchPlan().setGroups(fetchGroups);
//		else
//			pm.getFetchPlan().clearGroups();

		ConfigGroup configGroup = ConfigGroup.getConfigGroupForConfig(
				pm, 
				ConfigID.create(
						config.getOrganisationID(),
						config.getConfigKey(),
						config.getConfigType()
					)
			);
		if (configGroup != null) {
			ConfigModule groupModule = ConfigModule.getConfigModule(pm, configGroup, cfModClass, cfModID);
			if (groupModule != null)
				groupAllowOverwrite = groupModule.isAllowOverride();
		}
		ConfigModule result = (ConfigModule)pm.detachCopy(configModule);
		result.setGroupAllowOverwrite(groupAllowOverwrite);
		return result;
	}

	/**
	 * Searches the ConfigModule for the given keyObject and inherits all its 
	 * fields from the ConfigModule in the Configs configGroup according to its 
	 * inheritance-settings. 
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
	 * Searches the ConfigModule for the given keyObject and inherits all its 
	 * fields from the ConfigModule in the Configs configGroup according to its 
	 * inheritance-settings. 
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
			ObjectID keyObjectID, Class cfModClass, String cfModID, boolean throwExceptionIfNotFound,
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

//
//			return ConfigModule.getConfigModuleForKeyObject(
//					pm, 
//					getOrganisationID(), 
//					keyObject, 
//					cfModClass, 
//					cfModID, 
//					fetchGroups,
//					maxFetchDepth
//				);
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
	
	/**
	 * Returns a Collection of all complete ConfigSetups known. A complete
	 * ConfigSetup contains all Configs and ConfigGroups of that setup.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Collection getConfigSetups(
			String[] groupsFetchGropus, 
			String[] configsFetchGroups
		)
	throws ModuleException
	{		
		PersistenceManager pm;
		pm = getPersistenceManager();
		try 
		{		
			// Maybe its wise to set default to the fetchplan befor doing anything :-)
			pm.getFetchPlan().setGroups(new String[] {FetchPlan.DEFAULT});			
			Collection result = new ArrayList();
			Collection setups = ConfigSetup.getConfigSetups(pm);
			for (Iterator iter = setups.iterator(); iter.hasNext();) {
				ConfigSetup setup = (ConfigSetup) iter.next();
				result.add(
						setup.getCompleteConfigSetup(
								pm, 
								getOrganisationID(),
								groupsFetchGropus,
								configsFetchGroups
						)
				);
			}
			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Returns a complete ConfigSetup of the given type. A complete
	 * ConfigSetup contains all Configs and ConfigGroups of that setup.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public ConfigSetup getCompleteConfigSetup(
			String configSetupType, 
			String[] groupsFetchGropus, 
			String[] configsFetchGroups
		)
	throws ModuleException
	{		
		PersistenceManager pm;
		pm = getPersistenceManager();
		try 
		{		
			return 
				ConfigSetup.getConfigSetup(pm, getOrganisationID(), configSetupType)
					.getCompleteConfigSetup(pm, getOrganisationID(), groupsFetchGropus, configsFetchGroups);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public ConfigSetup getConfigSetup(
			String configSetupType, 
			String[] groupsFetchGropus, 
			String[] configsFetchGroups
		)
	throws ModuleException
	{		
		PersistenceManager pm;
		pm = getPersistenceManager();
		try 
		{			
			return 
				ConfigSetup.getConfigSetup(pm, getOrganisationID(), configSetupType)
					.getCompleteConfigSetup(pm, getOrganisationID(), groupsFetchGropus, configsFetchGroups);
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
	 * Initializes the JFireBase Config-System.
	 * 
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public void initialize() 
	throws ModuleException 
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			String organisationID = getOrganisationID();

			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireBaseEAR.MODULE_NAME);
			if (moduleMetaData != null)
				return;

			logger.info("Initialization of Configuration in JFireBase started...");

			// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
			moduleMetaData = new ModuleMetaData(
					JFireBaseEAR.MODULE_NAME, "1.0.0-0-beta", "1.0.0-0-beta");
			pm.makePersistent(moduleMetaData);

			UserConfigSetup userConfigSetup = new UserConfigSetup(organisationID);
			pm.makePersistent(userConfigSetup);

			WorkstationConfigSetup workstationConfigSetup = new WorkstationConfigSetup(organisationID);
			pm.makePersistent(workstationConfigSetup);
			workstationConfigSetup.getConfigModuleClasses().add(FeatureSetConfigModule.class.getName());
		} finally {
			pm.close();
		}
	}
	
	
}
