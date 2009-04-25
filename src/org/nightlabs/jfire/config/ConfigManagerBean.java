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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.config.ConfigException;
import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImplEJB3;
import org.nightlabs.jfire.base.JFireBaseEAR;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.config.id.ConfigModuleID;
import org.nightlabs.jfire.config.id.ConfigSetupID;
import org.nightlabs.jfire.editlock.EditLockType;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.version.Version;

/**
 * @ejb.bean name="jfire/ejb/JFireBaseBean/ConfigManager"
 *	jndi-name="jfire/ejb/JFireBaseBean/ConfigManager"
 *	type="Stateless"
 *
 * @ejb.permission role-name = "_Guest_"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 **/
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
@RolesAllowed("_Guest_")
public class ConfigManagerBean extends BaseSessionBeanImplEJB3 implements ConfigManagerRemote
{
	private static final long serialVersionUID = 1L;
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(ConfigManagerBean.class);

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigManagerRemote#storeConfigModule(org.nightlabs.jfire.config.ConfigModule, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public ConfigModule storeConfigModule(ConfigModule configModule, boolean get, String[] fetchGroups, int maxFetchDepth)
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

			ConfigModule pConfigModule = pm.makePersistent(configModule);

			if (pConfigModule.getConfig() instanceof ConfigGroup) {
				// is a ConfigModule of a ConfigGroup -> inherit all ConfigModules for
				// all its members
				ConfigGroup configGroup = (ConfigGroup)pConfigModule.getConfig();
				ConfigModule.inheritAllGroupMemberConfigModules(pm, configGroup, pConfigModule);

				// Workaround for a Cache bug, in which the cache misses to notify all objects having a modified
				//  FieldMetaData in their object graph.
				// This simply notifies all member ConfigModules.
				Collection<Config> members = Config.getConfigsForGroup(pm, configGroup);
				for (Iterator<Config> iter = members.iterator(); iter.hasNext();) {
					Config member = iter.next();
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
				return pm.detachCopy(pConfigModule);
			}
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigManagerRemote#getConfigGroups(java.lang.String, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@SuppressWarnings("unchecked")
	@Override
	public Collection<ConfigGroup> getConfigGroups(String configType, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try
		{
			Query q = pm.newQuery(ConfigGroup.class);
			if (configType != null)
				q.setFilter("this.configType == \""+configType+"\"");

			Collection<ConfigGroup> groups = (Collection<ConfigGroup>)q.execute();

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Collection<ConfigGroup> result = pm.detachCopyAll(groups);
			return result;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigManagerRemote#getConfigGroups(java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Collection<ConfigGroup> getConfigGroups(String[] fetchGroups, int maxFetchDepth)
	{
		return getConfigGroups(null, fetchGroups, maxFetchDepth);
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigManagerRemote#getConfigs(java.util.Set, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Collection<Config> getConfigs(Set<ConfigID> configIDs, String[] fetchGroups, int maxFetchDepth)
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigManagerRemote#getConfigs(java.lang.String, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Set<Config> getConfigs(String configType, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		ConfigSetup.ensureAllPrerequisites(pm);

		pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
		if (fetchGroups != null)
			pm.getFetchPlan().setGroups(fetchGroups);

		Collection<Config> queryResult = Config.getConfigsByType(pm, getOrganisationID(), configType);

		return NLJDOHelper.getDetachedQueryResultAsSet(pm, queryResult);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigManagerRemote#getConfigIDsByConfigType(java.lang.String, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigManagerRemote#getConfigs(java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Collection<Config> getConfigs(String[] fetchGroups, int maxFetchDepth)
	{
		return getConfigs((String)null, fetchGroups, maxFetchDepth);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigManagerRemote#getConfig(org.nightlabs.jfire.config.id.ConfigID, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
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

			return pm.detachCopy(config);

		} finally {
			pm.close();
		}
	}


	/* *********************************************************************** */
	/* ************************** getConfigModule stuff ********************** */
	/* *********************************************************************** */

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigManagerRemote#getConfigModule(org.nightlabs.jfire.config.id.ConfigID, java.lang.Class, java.lang.String, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public ConfigModule getConfigModule(ConfigID configID, Class<? extends ConfigModule> cfModClass, String cfModID, String[] fetchGroups, int maxFetchDepth)
	{
		return getConfigModule(getPersistenceManager(), configID, cfModClass, cfModID, fetchGroups, maxFetchDepth);
	}

	/**
	 * Helper Method returning the ConfigModule corresponding to the given ConfigModuleID and if non-existant
	 * creates a new ConfigModule and if necessary {@link Config}, too.
	 *
	 * @param pm the {@link PersistenceManager} to use
	 * @param configID the ConfigID of the {@link Config}, which shall contain the searched ConfigModule.
	 * @param cfModClass The ConfigModule's class
	 * @param cfModID The ConfigModules cfModID (suffix)
	 * @param fetchGroups The fetch-groups to be used to detach the ConfigModule
	 * @return The ConfigModule of the given userConfig, cfModClass and cfModID
	 */
	protected ConfigModule getConfigModule(PersistenceManager pm, ConfigID configID, Class<? extends ConfigModule> cfModClass, String cfModID,
			String[] fetchGroups, int maxFetchDepth)
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigManagerRemote#createConfigModule(org.nightlabs.jdo.ObjectID, java.lang.Class, java.lang.String, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public ConfigModule createConfigModule(
			ObjectID keyObjectID, Class<? extends ConfigModule> cfModClass, String cfModID,
			String[] fetchGroups, int maxFetchDepth)
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
			return pm.detachCopy(configModule);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigManagerRemote#getConfigModule(org.nightlabs.jfire.config.id.ConfigID, java.lang.Class, java.lang.String, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public ConfigModule getConfigModule(
			ConfigID keyObjectID, Class<? extends ConfigModule> cfModClass, String cfModID, boolean throwExceptionIfNotFound,
			String[] fetchGroups, int maxFetchDepth)
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

			return pm.detachCopy(configModule);

		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigManagerRemote#getConfigModules(java.util.Set, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Collection<ConfigModule> getConfigModules(Set<ConfigModuleID> moduleIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			ArrayList<ConfigModule> searchedModules = new ArrayList<ConfigModule>(moduleIDs.size());

			for (ConfigModuleID moduleID : moduleIDs)
				searchedModules.add(pm.detachCopy(Config.getConfigModule(pm, moduleID)));

			return searchedModules;
		} finally {
			pm.close();
		}
	}

	/**
	 * Helper method for the other getConfigModule methods, which searches for ConfigModule
	 * corresponding to the given cfModID and if it doesn't exist creates one.
	 */
	protected ConfigModule getCreateConfigModule(PersistenceManager pm, Config config, Class<? extends ConfigModule> cfModClass, String cfModID, String[] fetchGroups, int maxFetchDepth)
	{
		logger.debug("config.organisatinID "+config.getOrganisationID());
		ConfigModule configModule = config.createConfigModule(cfModClass, cfModID);

		logger.debug("Have configmodule: "+configModule);
		logger.debug("configModule.organisationID: "+configModule.getOrganisationID());
		logger.debug("configModule.configType: "+configModule.getConfigType());
		logger.debug("configModule.configKey: "+configModule.getConfigKey());
		logger.debug("configModule.cfModID: "+configModule.getCfModID());
		logger.debug("configModule.cfModKey: "+configModule.getCfModKey());

		return pm.detachCopy(configModule);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigManagerRemote#getGroupConfigModule(org.nightlabs.jfire.config.id.ConfigID, java.lang.Class, java.lang.String, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public ConfigModule getGroupConfigModule(
			ConfigID childID, Class<? extends ConfigModule> configModuleClass, String moduleID, boolean autoCreate,
			String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		if (fetchGroups != null) {
			pm.getFetchPlan().setGroups(fetchGroups);
		}
		pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);

		try {
			Config config = (Config) pm.getObjectById(childID);
			if (config == null)
				throw new ConfigException("There is no corresponding Config to the given ConfigID!");

			if (config.getConfigGroup() == null)
				return null; // just return null to enable the client to check whether there is a group or not.

			ConfigGroup group = config.getConfigGroup();
			ConfigModule groupsModule = null;
			if (autoCreate) {
				groupsModule = group.createConfigModule(configModuleClass, moduleID);
			} else {
				groupsModule = group.getConfigModule(configModuleClass, moduleID);
			}
			return pm.detachCopy(groupsModule);
		} finally {
			pm.close();
		}
	}

	/* *********************************************************************** */
	/* ************************** ConfigSetup stuff ************************** */
	/* *********************************************************************** */

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigManagerRemote#addConfigGroup(java.lang.String, java.lang.String, java.lang.String, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public ConfigGroup addConfigGroup(String configKey, String groupType, String groupName, boolean get, String[] fetchGroups, int maxFetchDepth)
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

				ConfigGroup result = pm.detachCopy(group);

				ConfigSetup.ensureAllPrerequisites(pm);

				// TODO: Make the ConfigSetup dirty that has the given groupType

				return result;
			}
			return null;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigManagerRemote#getConfigSetup(org.nightlabs.jfire.config.id.ConfigSetupID, java.lang.String[], int, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public ConfigSetup getConfigSetup(
			ConfigSetupID configSetupID,
			String[] groupsFetchGropus, int groupsMaxFetchDepth,
			String[] configsFetchGroups, int configsMaxFetchDepth
		)
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigManagerRemote#getConfigSetups(java.util.Set, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public List<ConfigSetup> getConfigSetups(Set<ConfigSetupID> setupIDs, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, setupIDs, ConfigSetup.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigManagerRemote#getAllConfigSetupIDs()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Collection<ConfigSetupID> getAllConfigSetupIDs() {
		PersistenceManager pm = getPersistenceManager();
		try {
			return ConfigSetup.getAllConfigSetupIDs(pm);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigManagerRemote#storeConfigSetup(java.util.Collection)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public void storeConfigSetup(Collection<Config> setup)
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigManagerRemote#storeConfig(org.nightlabs.jfire.config.Config, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigManagerRemote#applyGroupInheritence(org.nightlabs.jfire.config.id.ConfigModuleID, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
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
				return pm.detachCopy(moduleToUpdate);
			else
				return null;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigManagerRemote#initialise()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void initialise()
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			// has been moved to the ConfigFactoryInitialiser
//			System.setProperty(org.nightlabs.config.Config.PROPERTY_KEY_CONFIG_FACTORY, XMLConfigFactory.class.getName());

			String organisationID = getOrganisationID();

			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireBaseEAR.MODULE_NAME);
			if (moduleMetaData != null)
				return;

			logger.info("Initialization of Configuration in JFireBase started...");

			// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
			Version version = new Version(0, 9, 5, 0, "beta");
			moduleMetaData = new ModuleMetaData(JFireBaseEAR.MODULE_NAME, version, version);
			pm.makePersistent(moduleMetaData);

			UserConfigSetup userConfigSetup = new UserConfigSetup(organisationID);
			pm.makePersistent(userConfigSetup);

			WorkstationConfigSetup workstationConfigSetup = new WorkstationConfigSetup(organisationID);
			pm.makePersistent(workstationConfigSetup);

			pm.makePersistent(new EditLockType(JFireBaseEAR.EDIT_LOCK_TYPE_ID_CONFIG_MODULE));
			pm.makePersistent(new EditLockType(JFireBaseEAR.EDIT_LOCK_TYPE_ID_CONFIG));

			// WORKAROUND JPOX Bug to avoid concurrent modification at runtime
			pm.getExtent(ConfigModule.class, true);
		} finally {
			pm.close();
		}
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigManagerRemote#ping(java.lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("_Guest_")
	@Override
	public String ping(String message) {
		return super.ping(message);
	}
}
