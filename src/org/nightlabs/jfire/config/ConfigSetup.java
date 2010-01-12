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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.config.id.ConfigSetupID;
import org.nightlabs.util.Util;

/**
 * A ConfigSetup represents the setup of the membership of {@link Config}s of a certain
 * type in {@link ConfigGroup}s.
 * <p>
 * Subclasses of ConfigSetup are used to maintain different
 * types of {@link Config} grouping (e.g for User- or Workstationconfiguration).
 * </p>
 * <p>
 * Though this is a JDO object it does not have links into the datastore. It
 * is rather a singleton in the server providing logic. Most of the members
 * have persitence-modifier="none" and filled on demand.
 * </p>
 * <p>
 * The abstract method {@link #getCompleteConfigSetup(PersistenceManager, String[], int, String[], int)}
 * is responsible for creating an instance of ConfigSetup that has these
 * non-persistent members filled with detached copies of all {@link Config}s and {@link ConfigGroup}s
 * belonging to one {@link ConfigSetup} type.
 * </p>
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.config.id.ConfigSetupID"
 *		detachable="true"
 *		table="JFireBase_ConfigSetup"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, configSetupType"
 *
 * @jdo.query
 *    name="getAllConfigSetupIDs"
 *    query="SELECT JDOHelper.getObjectId(this)"
 *
 * @jdo.fetch-group name="ConfigSetup.configModuleClasses" fields="configModuleClasses"
 * @jdo.fetch-group name="ConfigSetup.this" fetch-groups="default" fields="configModuleClasses"
 *
 */
@PersistenceCapable(
	objectIdClass=ConfigSetupID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_ConfigSetup")
@FetchGroups({
	@FetchGroup(
		name=ConfigSetup.FETCH_GROUP_CONFIG_MODULE_CLASSES,
		members=@Persistent(name="configModuleClasses")),
	@FetchGroup(
		fetchGroups={"default"},
		name=ConfigSetup.FETCH_GROUP_THIS_CONFIG_SETUP,
		members=@Persistent(name="configModuleClasses"))
})
@Queries(
	@javax.jdo.annotations.Query(
		name=ConfigSetup.QUERY_GET_ALL_CONFIGSETUPIDS,
		value="SELECT JDOHelper.getObjectId(this)")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public abstract class ConfigSetup implements Serializable {
	private static final Logger logger = Logger.getLogger(ConfigSetup.class);
	private static final long serialVersionUID = 1L;
	public static final String FETCH_GROUP_CONFIG_MODULE_CLASSES = "ConfigSetup.configModuleClasses";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_CONFIG_SETUP = "ConfigSetup.this";

	public static final String QUERY_GET_ALL_CONFIGSETUPIDS = "getAllConfigSetupIDs";

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ConfigSetup() {
		if (logger.isDebugEnabled())
			logger.debug("Constructor ConfigSetup(): " + this);
	}

	public ConfigSetup(String organisationID, String configSetupType) {
		this.organisationID = organisationID;
		this.configSetupType = configSetupType;
		configModuleClasses = new HashSet<String>();

		if (logger.isDebugEnabled())
			logger.debug("Constructor ConfigSetup(String organisationID, String configSetupType): " + this);
	}

  /**
   * @jdo.field primary-key="true"
   * @jdo.column length="100"
   */
  @PrimaryKey
  @Column(length=100)
	private String organisationID;

  /**
   * @jdo.field primary-key="true"
   * @jdo.column length="100"
   */
  @PrimaryKey
  @Column(length=100)
	private String configSetupType;

	/**
	 * The classname of the object this ConfigSetup links to.
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	protected String targetClass;

	/**
	 * The configType of Configs in this ConfigSetup. Mostly the same as
	 * targetClass.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	protected String configType;

	/**
	 * The configType of ConfigGroups in this ConfigSetup.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	protected String configGroupType;

	/**
	 * A set of all allowed ConfigModule classes for this ConfigSetup.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="java.lang.String"
	 *		table="JFireBase_ConfigSetup_configModuleClasses"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		table="JFireBase_ConfigSetup_configModuleClasses",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<String> configModuleClasses;

	/**
	 * Subclasses are requested here to ensure their specific prerequisites are
	 * fulfilled.
	 *
	 * @param pm The PersistenceManager to use
	 */
	public abstract void ensureSetupPrerequisites(PersistenceManager pm);

	/**
	 * Should return all {@link Config} belonging to this ConfigSetup.
	 *
	 * @param pm The PersistenceManager to use
	 */
	public abstract Collection<Config> getAllSetupConfigs(PersistenceManager pm);

	/**
	 * Return a non-persitent Serializable complete ConfigSetup of its type.
	 * A complete setup is a ConfigSetup with the configs List with
	 * all Configs part of this ConfigSetup the configGroups Map filled
	 * with all ConfigGroups of this ConfigSetup.
	 *
	 * @param pm The PersistenceManager to use.
	 * @param groupsFetchGropus Fetch-groups to detach the {@link ConfigGroup}s with.
	 * 		If this is <code>null</code>, {@link FetchPlan#DEFAULT} will be used.
	 * @param groupsMaxFetchDepth The maximum fetch-depth for detaching the {@link ConfigGroup}s of this ConfigSetup.
	 * @param configsFetchGroups Fetch-groups to detach the {@link Config}s with.
	 * 		If this is <code>null</code>, {@link FetchPlan#DEFAULT} will be used.
	 * @param configsMaxFetchDepth The maximum fetch-depth for detaching the {@link Config}s of this ConfigSetup.
	 * @return The complete ConfigSetup along with its {@link ConfigGroup}s and {@link Config}s.
	 * @see #getAllSetupConfigs(PersistenceManager)
	 */
	public ConfigSetup getCompleteConfigSetup(
			PersistenceManager pm,
			String[] groupsFetchGropus, int groupsMaxFetchDepth,
			String[] configsFetchGroups, int configsMaxFetchDepth
		)
	{
		// WORKAROUND: No generics used because of BCEL bug.
		Collection<Config> configs = getAllSetupConfigs(pm);
		pm.getFetchPlan().setGroup(FETCH_GROUP_THIS_CONFIG_SETUP);
		ConfigSetup result = pm.detachCopy(this);
//		Collection groups = (Collection)pm.newQuery(UserConfigGroup.class).execute();
		Collection<ConfigGroup> groups = ConfigGroup.getConfigGroupsByKeyObjectClass(pm, getOrganisationID(), configGroupType);
		if (groupsFetchGropus != null)
			pm.getFetchPlan().setGroups(groupsFetchGropus);
		else
			pm.getFetchPlan().setGroups(new String[] {FetchPlan.DEFAULT});
		Collection<ConfigGroup> dGroups = pm.detachCopyAll(groups);
		for (Iterator<ConfigGroup> iter = dGroups.iterator(); iter.hasNext();) {
			ConfigGroup dGroup = iter.next();
			result.configGroups.put(dGroup.getConfigKey(), dGroup);
			result.configGroupsByID.put((ConfigID)JDOHelper.getObjectId(dGroup), dGroup);
		}

		if (configsFetchGroups != null)
			pm.getFetchPlan().setGroups(configsFetchGroups);
		else
			pm.getFetchPlan().setGroups(new String[] {FetchPlan.DEFAULT});

		for (Iterator<Config> iter = configs.iterator(); iter.hasNext();) {
			Config config = iter.next();
			Config dConfig = pm.detachCopy(config);
			if (config.getConfigGroup() != null)
				dConfig.setConfigGroup(result.getConfigGroup(config.getConfigGroup().getConfigKey()));
			result.configs.put((ConfigID)JDOHelper.getObjectId(dConfig), dConfig);
		}
		return result;
	}

	/**
	 * All config groups in this setup.
	 * key: String configKey of the configKey<br/>
	 * value: ConfigGroup
	 *
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private Map<String, ConfigGroup> configGroups = new HashMap<String, ConfigGroup>();

	/**
	 * All ConfigGroups by their ObjectID
	 *
	 * key: ConfigID configGroupID<br/>
	 * value: ConfigGroup configGroup
	 *
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private Map<ConfigID, ConfigGroup> configGroupsByID = new HashMap<ConfigID, ConfigGroup>();

	/**
	 * A list of all Configs of this ConfigSetup.
	 * value-type: Config
	 * key: ConfigID configID<br/>
	 * value: Config config
	 *
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private Map<ConfigID, Config> configs = new HashMap<ConfigID, Config>();

	/**
	 * A list of Configs that were modified.
	 * value-type: Config
	 *
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient List<Config> modifiedConfigs;

	/**
	 * @return A Collection of all ConfigGroups in this ConfigSetup.
	 */
	public Collection<ConfigGroup> getConfigGroups() {
		return configGroups.values();
	}

	/**
	 * @return The Map of {@link ConfigGroup}s with
	 * 		their configKey as key value.
	 */
	public Map<String, ConfigGroup> getConfigGroupsMap() {
		return configGroups;
	}

	/**
	 * @return A Map of all ConfigGroups in this {@link ConfigSetup} with their JDO-id ({@link ConfigID}) as key.
	 */
	public Map<ConfigID, ConfigGroup> getConfigGroupsByID() {
		return configGroupsByID;
	}

	/**
	 * @return A list of all Configs in this ConfigSetup.
	 */
	public Collection<Config> getConfigs() {
		return configs.values();
	}

	/**
	 * @return The Map of all {@link Config}s in this this ConfigSetup with their JDO-id ({@link ConfigID}) as key.
	 */
	public Map<ConfigID, Config> getConfigsMap() {
		return configs;
	}

	/**
	 * @return The ConfigGroup out of configGroups with the given groupConfigKey.
	 */
	public ConfigGroup getConfigGroup(String groupConfigKey) {
		return configGroups.get(groupConfigKey);
	}

	/**
	 * Moves the given {@link Config} to the ConfigGroup with the configKey specified by
	 * groupConfigKey (Removes the {@link Config} from all other groups first)
	 *
	 * @param config The {@link Config} to move.
	 * @param groupConfigKey The configKey of the groups the {@link Config} should belong to.
	 */
	public void moveConfigToGroup(Config config, String groupConfigKey) {
		config.setConfigGroup(getConfigGroup(groupConfigKey));
		getModifiedConfigs().add(config);
//		// find and remove it in the list
//		for (Iterator iter = configGroups.iterator(); iter.hasNext();) {
//			ConfigGroup group = (ConfigGroup)iter.next();
//			boolean breakGlobal = false;
//			for (Iterator iterator = group.get.iterator(); iterator.hasNext();) {
//				Config _config = (Config) iterator.next();
//				if (_config.getConfigKey().equals(config.getConfigKey())) {
//					iterator.remove();
//					breakGlobal = true;
//					break;
//				}
//			}
//			if (breakGlobal)
//				break;
//		}
//		// add to the right place
//		List configList = (List)configsByGroup.get(groupConfigKey);
//		if (configList == null) {
//			configList = new ArrayList();
//			configsByGroup.put(groupConfigKey, configList);
//		}
//		configList.add(config);
	}

	/**
	 * @return All modified Configs.
	 */
	public List<Config> getModifiedConfigs() {
		if (modifiedConfigs == null)
			modifiedConfigs = new ArrayList<Config>();
		return modifiedConfigs;
	}

	/**
	 * Clears the list of modified Configs.
	 */
	public void clearModifiedConfigs() {
		getModifiedConfigs().clear();
	}

	/**
	 * Get the List of Configs hold by the ConfigGroup with the given
	 * groupConfigKey.
	 */
	public List<Config> getConfigsForGroup(String groupConfigKey) {
//		List<Config> result = new ArrayList<Config>();
//		untyped, because of BCEL bug
		List<Config> result = new ArrayList<Config>();
		for (Iterator<Config> iter = configs.values().iterator(); iter.hasNext();) {
			Config config = iter.next();
			if (config.getConfigGroup() == null)
				continue;
			if (config.getConfigGroup().getConfigKey().equals(groupConfigKey))
				result.add(config);
		}
		return result;
//		List configsList = (List)configsByGroup.get(groupConfigKey);
//		if (configList == null) {
//			configList = new ArrayList();
//			configsByGroup.put(groupConfigKey, configList);
//		}
//		return configList;
	}

	public List<Config> getConfigsNotInGroup(String groupConfigKey)
	{
//		List<Config> result = new ArrayList<Config>();
//		untyped, because of BCEL bug
		List<Config> result = new ArrayList<Config>();
		for (Iterator<Config> iter = configs.values().iterator(); iter.hasNext();)
		{
			Config config = iter.next();
			if(config.getConfigGroup() == null)
				result.add(config);
			else
			if (!config.getConfigGroup().getConfigKey().equals(groupConfigKey))
				result.add(config);
		}
		return result;
	}

//	/**
//	 * Returns the ConfigGroup the given Config is grouped in or null.
//	 *
//	 * @param config A Config grouped in the returned ConfigGroup
//	 */
//	public ConfigGroup getGroupForConfig(Config config) {
//		for (Iterator iter = configsByGroup.entrySet().iterator(); iter.hasNext();) {
//			Map.Entry entry = (Map.Entry) iter.next();
//			String groupConfigKey = (String)entry.getKey();
//			List configList = (List)entry.getValue();
//			for (Iterator iterator = configList.iterator(); iterator.hasNext();) {
//				Config _config = (Config) iterator.next();
//				if (_config.getConfigKey().equals(config.getConfigKey())) {
//					if (!groupConfigKey.equals(UNASSIGNED_GROUP_ID)) {
//						// find the group
//						for (Iterator groupIterator = configGroups.iterator(); groupIterator.hasNext();) {
//							ConfigGroup group = (ConfigGroup) groupIterator.next();
//							if (group.getConfigKey().equals(groupConfigKey))
//								return group;
//						}
//					}
//					else
//						return null;
//				}
//			}
//		}
//		return null;
//	}

	/**
	 * Stores the given part of a ConfigSetup represented by a Collection of
	 * Configs by simply setting the configGroup of the persistent instances
	 * of all Configs in the collection. Additionally this method inherits
	 * the fields of all ConfigModules within a Config in the given setup
	 * from the ones of the corresponding ConfigModule in their new ConfigGroup.
	 *
	 * @param pm PersitenceManager to reflect the setup to
	 * @param setup The setup that should be stored
	 */
	protected static void storeConfigSetup(PersistenceManager pm, Collection<Config> setup) {
		for (Config config : setup) {
			ConfigID configID = ConfigID.create(config.getOrganisationID(), config.getConfigKey(), config.configType);
			Config persistentConfig = (Config)pm.getObjectById(configID);
			ConfigGroup persistentGroup = null;
			if (config.getConfigGroup() != null) {
				ConfigGroup group = config.getConfigGroup();
//				ConfigID groupID = ConfigID.create(group.organisationID, group.getConfigKey(), group.configType);
//				ConfigID groupID = (ConfigID) JDOHelper.getObjectId(group);
//				persistentGroup = (ConfigGroup)pm.getObjectById(groupID);
				persistentGroup = group;
			}
//			ConfigGroup group = persistentConfig.getConfigGroup();
			// set the ConfigGroup of the config
			persistentConfig.setConfigGroup(persistentGroup);
			if (persistentGroup != null) {
				// if the ConfigGroup changed, inherit the values from the new
				// group according to the inheritance settings of the ConfigModules
				// in the new group
				Collection<ConfigModule> _groupModules = ConfigModule.getExistingConfigModulesForConfig(pm, persistentGroup);
				// WORKAROUND: untyped, because of BCEL bug
				Map<String, ConfigModule> groupModules = new HashMap<String, ConfigModule>();
				Collection<ConfigModule> configModules = ConfigModule.getExistingConfigModulesForConfig(pm, persistentConfig);
				for (Iterator<ConfigModule> iterator = _groupModules.iterator(); iterator.hasNext();) {
					ConfigModule module = iterator.next();
					groupModules.put(module.getCfModKey(), module);
				}
				for (Iterator<ConfigModule> iterator = configModules.iterator(); iterator.hasNext();) {
					ConfigModule module = iterator.next();
					ConfigModule groupModule = groupModules.get(module.getCfModKey());
					if (groupModule != null)
						ConfigModule.inheritConfigModule(groupModule, module);
				}
			}
		}

//		for (Iterator iter = setup.configsByGroup.entrySet().iterator(); iter.hasNext();) {
//			Map.Entry entry = (Map.Entry) iter.next();
//			String groupConfigKey = (String)entry.getKey();
//			ConfigGroup group = null;
//			if (!UNASSIGNED_GROUP_ID.equals(groupConfigKey)) {
//				ConfigID groupID = ConfigID.create(setup.organisationID, groupConfigKey, setup.configSetupType);
//				group = (ConfigGroup)pm.getObjectById(groupID);
//			}
//			List configList = (List)entry.getValue();
//			for (Iterator iterator = configList.iterator(); iterator.hasNext();) {
//				Config config = (Config) iterator.next();
//				ConfigID configID = ConfigID.create(config.getOrganisationID(), config.getConfigKey(), config.configType);
//				Config persistentConfig = (Config)pm.getObjectById(configID);
//				persistentConfig.setConfigGroup(group);
//			}
//		}
	}

	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return The configType of ConfigGroups in this ConfigSetup.
	 */
	public String getConfigGroupType() {
		return configGroupType;
	}

	/**
	 * @return The type of this ConfigSetup.
	 */
	public String getConfigSetupType() {
		return configSetupType;
	}

	/**
	 * @return A list of ConfigModule classes that can be linked
	 * to Configs in this ConfigSetup.
	 */
	public Set<String> getConfigModuleClasses() {
		if (logger.isDebugEnabled())
			logger.debug("getConfigModuleClasses: " + this + " : isPersistent=" + JDOHelper.isPersistent(this) + " isNew=" + JDOHelper.isNew(this));

		if (configModuleClasses == null)
			throw new IllegalStateException("JDO error! configModuleClasses == null : " + this);

		return configModuleClasses;
	}

	/**
	 * @return The configType of Configs in this ConfigSetup.
	 */
	public String getConfigType() {
		return configType;
	}

	/**
	 * @return The classname of the Objects Configs of this ConfigSetup link to.
	 */
	public String getTargetClass() {
		return targetClass;
	}

	/**
	 * @return All ConfigSetups in datastore.
	 */
	public static Collection<ConfigSetup> getConfigSetups(PersistenceManager pm) {
		return (Collection<ConfigSetup>)pm.newQuery(pm.getExtent(ConfigSetup.class, true)).execute();
	}

	/**
	 * @return The ConfigSetup with the given organisatioID and configSetupType
	 */
	public static ConfigSetup getConfigSetup(PersistenceManager pm, String organisationID, String configSetupType) {
		ConfigSetupID setupID = ConfigSetupID.create(organisationID, configSetupType);
		return (ConfigSetup)pm.getObjectById(setupID);
	}

	/**
	 * Iterates all registered ConfigSetups and calls
	 * {@link #ensureSetupPrerequisites(PersistenceManager)}.
	 */
	public static void ensureAllPrerequisites(PersistenceManager pm)
	{
		Collection<ConfigSetup> setups = getConfigSetups(pm);
		for (Iterator<ConfigSetup> iter = setups.iterator(); iter.hasNext();) {
			ConfigSetup setup = iter.next();
			setup.ensureSetupPrerequisites(pm);
		}
	}

	/**
	 * Returns a collection with all {@link ConfigSetupID}s of all ConfigSetups. <br>
	 * Note: The returned collection is already processed by {@link NLJDOHelper#getDetachedQueryResult(PersistenceManager, Collection)}
	 * and therefore serialisable!
	 *
	 * @param pm the {@link PersistenceManager} to use.
	 * @return a usable Collection with all {@link ConfigSetupID}s of all ConfigSetups.
	 */
	public static Set<ConfigSetupID> getAllConfigSetupIDs(PersistenceManager pm) {
		Query query = pm.newNamedQuery(ConfigSetup.class, ConfigSetup.QUERY_GET_ALL_CONFIGSETUPIDS);
		return NLJDOHelper.getDetachedQueryResultAsSet(pm, (Collection<ConfigSetupID>) query.execute());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((configSetupType == null) ? 0 : configSetupType.hashCode());
		result = PRIME * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (! (obj instanceof ConfigSetup))
			return false;

		final ConfigSetup other = (ConfigSetup) obj;

		return Util.equals(organisationID, other.organisationID) &&
					 Util.equals(configGroupType, other.configSetupType);
	}

}
