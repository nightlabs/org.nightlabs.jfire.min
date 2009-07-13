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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.JDODataStoreException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.config.ConfigException;
import org.nightlabs.config.ConfigModuleNotFoundException;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.config.id.ConfigModuleID;
import org.nightlabs.util.Util;

import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Key;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;

/**
 * Configs mainly hold a list of ConfigModules and are intended to be linked
 * to other persistent objects in datastore. The linkage is done via the members
 * {@link #configType} and {@link #configKey} where the type should be the
 * classname of the linked object and the key the String representation of
 * its id-object.
 * {@link org.nightlabs.jfire.config.id.ConfigID} provides static methods
 * to create a new ConfigID that represents a Config linked to a given object.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Niklas Schiffler <nick@nightlabs.de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.config.id.ConfigID"
 *		detachable="true"
 *		table="JFireBase_Config"
 *
 * @jdo.inheritance strategy = "new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, configKey, configType"
 *		include-imports="id/ConfigID.imports.inc"
 *		include-body="id/ConfigID.body.inc"
 *
 * @jdo.query
 *		name="getConfig"
 *		query="SELECT
 *			WHERE organisationID == paramOrganisationID &&
 *            configKey == paramConfigKey &&
 *            configType == paramConfigType
 *			PARAMETERS String paramOrganisationID, String paramConfigKey, String paramConfigType
 *			import java.lang.String"
 *
 * @jdo.query
 *		name="getConfigsByType"
 *		query="SELECT
 *			WHERE organisationID == paramOrganisationID &&
 *            configType == paramConfigType
 *			PARAMETERS String paramOrganisationID, String paramConfigType
 *			import java.lang.String"
 *
 *	@jdo.query
 *		name="getConfigIDsByConfigType"
 *		query="SELECT JDOHelper.getObjectId(this)
 *			WHERE configType == paramConfigType &&
 *						organisationID == paramOrganisationID
 *			PARAMETERS String paramOrganisationID, String paramConfigType
 *			import java.lang.String"
 *
 *  @jdo.query
 *		name="getConfigsForGroup"
 *		query="SELECT
 *			WHERE this.configGroup != null &&
 *						this.configGroup.organisationID == paramConfigGroupOrganisationID &&
 *						this.configGroup.configType == paramConfigGroupConfigType &&
 *						this.configGroup.configKey == paramConfigGroupConfigKey
 *			PARAMETERS String paramConfigGroupOrganisationID, String paramConfigGroupConfigType, String paramConfigGroupConfigKey
 *			import java.lang.String"
 *
 *  @jdo.query
 *		name="getGroupsConfigModule"
 *		query="SELECT
 *			WHERE this.configGroup != null &&
 *						this.configGroup.organisationID == paramConfigGroupOrganisationID &&
 *						this.configGroup.configType == paramConfigGroupConfigType &&
 *						this.configGroup.configKey == paramConfigGroupConfigKey
 *			PARAMETERS String paramConfigGroupOrganisationID, String paramConfigGroupConfigType, String paramConfigGroupConfigKey
 *			import java.lang.String"
 *
 * @jdo.fetch-group name="Config.configModules" fields="configModules"
 * @jdo.fetch-group name="Config.configGroup" fields="configGroup"
 * @jdo.fetch-group name="Config.this" fetch-groups="default" fields="configModules, configGroup"
 */
@PersistenceCapable(
	objectIdClass=ConfigID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Config")
@FetchGroups({
	@FetchGroup(
		name=Config.FETCH_GROUP_CONFIG_MODULES,
		members=@Persistent(name="configModules")),
	@FetchGroup(
		name=Config.FETCH_GROUP_CONFIG_GROUP,
		members=@Persistent(name="configGroup")),
	@FetchGroup(
		fetchGroups={"default"},
		name=Config.FETCH_GROUP_THIS_CONFIG,
		members={@Persistent(name="configModules"), @Persistent(name="configGroup")})
})
@Queries({
	@javax.jdo.annotations.Query(
		name=Config.QUERY_GET_CONFIG,
		value="SELECT WHERE organisationID == paramOrganisationID && configKey == paramConfigKey && configType == paramConfigType PARAMETERS String paramOrganisationID, String paramConfigKey, String paramConfigType import java.lang.String"),
	@javax.jdo.annotations.Query(
		name=Config.QUERY_GET_CONFIGS_BY_TYPE,
		value="SELECT WHERE organisationID == paramOrganisationID && configType == paramConfigType PARAMETERS String paramOrganisationID, String paramConfigType import java.lang.String"),
	@javax.jdo.annotations.Query(
		name=Config.QUERY_GET_CONFIGIDS_BY_TYPE,
		value="SELECT JDOHelper.getObjectId(this) WHERE configType == paramConfigType && organisationID == paramOrganisationID PARAMETERS String paramOrganisationID, String paramConfigType import java.lang.String"),
	@javax.jdo.annotations.Query(
		name=Config.QUERY_GET_CONFIGS_FOR_GROUP,
		value="SELECT WHERE this.configGroup != null && this.configGroup.organisationID == paramConfigGroupOrganisationID && this.configGroup.configType == paramConfigGroupConfigType && this.configGroup.configKey == paramConfigGroupConfigKey PARAMETERS String paramConfigGroupOrganisationID, String paramConfigGroupConfigType, String paramConfigGroupConfigKey import java.lang.String"),
	@javax.jdo.annotations.Query(
		name="getGroupsConfigModule",
		value="SELECT WHERE this.configGroup != null && this.configGroup.organisationID == paramConfigGroupOrganisationID && this.configGroup.configType == paramConfigGroupConfigType && this.configGroup.configKey == paramConfigGroupConfigKey PARAMETERS String paramConfigGroupOrganisationID, String paramConfigGroupConfigType, String paramConfigGroupConfigKey import java.lang.String")
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Config implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * Logger used by this class.
	 */
	private static final Logger logger = Logger.getLogger(Config.class);

	public static final String FETCH_GROUP_CONFIG_MODULES = "Config.configModules";
	public static final String FETCH_GROUP_CONFIG_GROUP = "Config.configGroup";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_CONFIG = "Config.this";

	public static final String QUERY_GET_CONFIGIDS_BY_TYPE = "getConfigIDsByConfigType";
	public static final String QUERY_GET_CONFIG = "getConfig";
	public static final String QUERY_GET_CONFIGS_BY_TYPE = "getConfigsByType";
	public static final String QUERY_GET_CONFIGS_FOR_GROUP = "getConfigsForGroup";

	/**
	 * @jdo.field primary-key="true"
	 * !@jdo.column length="100"
	 */
	@PrimaryKey
	protected String organisationID;

	/**
	 * The classname of object associated to this Config.
	 *
	 * @jdo.field primary-key="true"
	 * !@jdo.column length="100"
	 */
	@PrimaryKey
	protected String configType;

	/**
	 * A Config should be linked to an object in datastore (e.g User,
	 * Workstation). This member is either used to represent the objects
	 * the Config is linked to or is used customly by ConfigGroups.
	 *
	 * @jdo.field primary-key="true"
	 * !@jdo.column length="200"
	 */
	@PrimaryKey
	protected String configKey;

	/**
	 * key: String ConfigModule.getClass.getName()+ConfigModule.getCgModID()<br/>
	 * value: ConfigModule configModule<br/>
	 *
	 * @jdo.field
	 *    persistence-modifier="persistent"
	 *    collection-type="map"
	 *    key-type="java.lang.String"
	 *    value-type="org.nightlabs.jfire.config.ConfigModule"
	 *    mapped-by="config"
	 *
	 * @jdo.key mapped-by="cfModKey"
	 *
	 * @!jdo.join
	 * @!jdo.map-vendor-extension vendor-name="jpox" key="key-field" value="cfModKey"
	 */
	@Persistent(
		mappedBy="config",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Key(mappedBy="cfModKey")
	protected Map<String, ConfigModule> configModules = new HashMap<String, ConfigModule>();

	protected Config() { }

	public Config(String organisationID, Object configKeyObject)
	{
		this.organisationID = organisationID;
		this.configKey = JDOHelper.getObjectId(configKeyObject).toString();
		this.configType = this.getClass().getName();
	}

	public Config(String organisationID, String configKey, String configType)
	{
		this.organisationID = organisationID;
		this.configKey = configKey;
		this.configType = configType;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	protected ConfigGroup configGroup;


	public void addConfigModule(ConfigModule cfMod)
	{
		configModules.put(cfMod.getCfModKey(), cfMod);
	}

	public void removeConfigModule(String cfModKey)
	{
		configModules.remove(cfModKey);
	}

	public void removeConfigModule(ConfigModule cfMod)
	{
		configModules.remove(cfMod.getCfModKey());
	}

	/**
	 * Returns the defined ConfigModule or throws a {@link ConfigModuleNotFoundException}
	 *
	 * @param cfModClass The ConfigModule class to be retrieved
	 * @param cfModID The cfModID of the ConfigModule to be retrieved
	 * @return The defined ConfigModule.
	 */
	public <T extends ConfigModule> T getConfigModule(Class<T> cfModClass, String cfModID)
	throws ConfigModuleNotFoundException
	{
		return getConfigModule(cfModClass, cfModID, true);
	}

	/**
	 * Returns the defined ConfigModule or throws a {@link ConfigModuleNotFoundException} or returns
	 * <code>null</code> (depending on <code>throwExceptionIfNotFound</code>).
	 *
	 * @param cfModClass The ConfigModule class to be retrieved
	 * @param cfModID The cfModID of the ConfigModule to be retrieved
	 * @param throwExceptionIfNotFound If <code>true</code> and the ConfigModule does not exist, a {@link ConfigModuleNotFoundException} will be thrown. If it's <code>false</code>, <code>null</code> will be returned instead of an exception.
	 * @return The defined ConfigModule.
	 */
	@SuppressWarnings("unchecked")
	public <T extends ConfigModule> T getConfigModule(Class<T> cfModClass, String cfModID, boolean throwExceptionIfNotFound)
	throws ConfigModuleNotFoundException
	{
		String cfModKey = ConfigModule.getCfModKey(cfModClass, cfModID);
		ConfigModule res;

		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		// performance optimization: we don't load the whole map, if we are "alive" (attached to datastore)
		if (pm == null)
			res = configModules.get(cfModKey);
		else
			res = ConfigModule.getConfigModule(pm, this, cfModClass, cfModID);

		if (res == null && throwExceptionIfNotFound)
			throw new ConfigModuleNotFoundException("cfModClass="+cfModClass.getName() + " cfModID="+cfModID);

		return (T) res;
	}

	/**
	 * Returns the ConfigModule of the given Class or throws a {@link ConfigModuleNotFoundException}.
	 * @param cfModClass The ConfigModule class to be retrieved
	 * @return The ConfigModule of the given Class.
	 */
	public <T extends ConfigModule> T getConfigModule(Class<T> cfModClass)
	throws ConfigModuleNotFoundException
	{
		return getConfigModule(cfModClass, null);
	}
	/**
	 * @param cfModClass The ConfigModule class to be retrieved
	 * @param throwExceptionIfNotFound If <code>true</code> and the ConfigModule does not exist, a {@link ConfigModuleNotFoundException} will be thrown. If it's <code>false</code>, <code>null</code> will be returned instead of an exception.
	 * @return Returns an instance of the class specified by <code>cfModClass</code> (extending {@link ConfigModule}) or <code>null</code>,
	 *		if <code>throwExceptionIfNotFound == false</code> and the module doesn't exist.
	 */
	public ConfigModule getConfigModule(Class<? extends ConfigModule> cfModClass, boolean throwExceptionIfNotFound)
	throws ConfigModuleNotFoundException
	{
		return getConfigModule(cfModClass, null, throwExceptionIfNotFound);
	}

	private <T extends ConfigModule> T _createConfigModule(Class<T> cfModClass, String cfModID)
	{
		try {
			// FIXME: this pm is never closed! (Marc)
			PersistenceManager pm = JDOHelper.getPersistenceManager(this);
	
			if (pm == null)
				throw new IllegalArgumentException("Can not create a new ConfigModule on a non-persistent Config");
	
			ConfigModule result = cfModClass.newInstance();
			result.setConfig(this);
			result.setPrimaryKeyFields(getOrganisationID(), getConfigKey(), getConfigType(), cfModID);
			result.setGroupConfigModule(this instanceof ConfigGroup);
			result = pm.makePersistent(result);
			try {
				this.addConfigModule(result);
			} catch (JDODataStoreException e) {
				// WORKAROUND: For a strange error while persisting (adding to map) - Duplicate key exception?
				// Have to make JPOX test case for that :-(
				logger.error("Catched "+e+" when adding new ConfigModule to add to configModules list. As a workaround, I try to continue, however", e);
			}
			result.init();
	
			T cfMod = getConfigModule(cfModClass, cfModID);
			// initialise by registered intialisers
			List<ConfigModuleInitialiser> intitialisers = ConfigModuleInitialiser.getSortedInitialisersForConfigModule(pm, cfMod);
			for (ConfigModuleInitialiser initialiser : intitialisers) {
				initialiser.initialiseConfigModule(pm, cfMod);
			}
			return cfMod;
		} catch(Exception e) {
			throw new ConfigException("Creating config module failed", e);
		}
	}

	/**
	 * Get the ConfigModule for the given Config and class. A new ConfigModule will
	 * be created, if no appropriate one can be found in the Config.
	 *
	 * @param cfModClass The classname of the ConfigModule
	 * @param cfModID The cfModID of the ConfigModule, optional
	 * @return Searched ConfigModule, never null
	 */
	public <T extends ConfigModule> T createConfigModule(Class<T> cfModClass, String cfModID)
	{
		T cfMod = getConfigModule(cfModClass, cfModID, false);

		if (cfMod == null) {
			cfMod = _createConfigModule(cfModClass, cfModID);

			if (!(this instanceof ConfigGroup)) {
				if (this.getConfigGroup() != null) {
					ConfigModule groupModule = this.getConfigGroup().createConfigModule(cfModClass, cfModID);
					ConfigModule.inheritConfigModule(groupModule, cfMod);
				}
			}
		}

		if (cfMod.getOrganisationID() == null)
			throw new IllegalStateException("Created ConfigModule with organisationID = null!");

		return cfMod;
	}

	/**
	 * Calls {@link #createConfigModule(Class, String) } with <code>cfModID = null</code>.
	 */
	public <T extends ConfigModule> T createConfigModule(Class<T> cfModClass)
	{
		return createConfigModule(cfModClass, null);
	}

	public String getOrganisationID()
	{
		return organisationID;
	}

	/**
	 * A Config should be linked to an object in datastore (e.g User,
	 * Workstation). This member is either used to represent the objects
	 * the Config is linked to (in ObjectID-String-representation) 
	 * or is used customly by ConfigGroups.
	 *  
	 * @return The configKey of this config.
	 */
	public String getConfigKey()
	{
		return configKey;
	}

	/**
	 * Sets the configGroup this Config belongs to.
	 *
	 * @param configGroup The ConfigGroup this Config should belong to
	 */
	public void setConfigGroup(ConfigGroup configGroup) {
		this.configGroup = configGroup;
	}

	/**
	 * Returns the ConfigGroup this Config is assigned to. Might be null.
	 *
	 * @return The configGroup this Config is assigned to. Might be null.
	 */
	public ConfigGroup getConfigGroup() {
		return configGroup;
	}

	/**
	 * The configType of Configs should be the classname of the object
	 * the Config is linked to. The configKey should then be used to uniquely
	 * identify the linked object.
	 * For ConfigGroups this field is used as group-type.
	 *
	 * @return The configType of this Config
	 */
	public String getConfigType() {
		return configType;
	}

	/**
	 * Returns an already persistent Config with the given primary key or
	 * newly creates and returns it.
	 *
	 * @param pm PersistenceManager to use
	 * @param organisationID organisationID of the UserConfig
	 * @param configKey configID of the Config
	 * @param configType The configType of the desired Config
	 * @return The defined UserConfig
	 */
	public static Config getConfig(PersistenceManager pm, String organisationID, String configKey, String configType) {
		Config correspondingConfig;
		try {
			 correspondingConfig = (Config) pm.getObjectById(ConfigID.create(organisationID, configKey, configType));
			 correspondingConfig.getConfigKey(); // FIXME: JPOX Workaround
		} catch (JDOObjectNotFoundException e) {
			correspondingConfig= new Config(organisationID, configKey, configType);
			return pm.makePersistent(correspondingConfig);
		}
		return correspondingConfig;
	}

	/**
	 * Convenience method for {@link #getConfig(PersistenceManager, String, String, String)}
	 * Passes the String representation of the keyObject's jdo key as configKey	 *
	 * and the keyObject's classname as configType.
	 *
	 * @param pm The PersistenceManger to use.
	 * @param organisationID The organisationID to search/create the config for
	 * @param configKeyObject The object (not the id-object) the Config should be linked to.
	 * 		This objects id-object string representation will be the configKey of the returned Config
	 * 		and the name of its class will be the configType.
	 */
	public static Config getConfig(PersistenceManager pm, String organisationID, Object configKeyObject) {
		if (organisationID == null)
			throw new IllegalArgumentException("organisationID must not be null!");

		if (configKeyObject == null)
			throw new IllegalArgumentException("configKeyObject must not be null!");

		Object configKeyObjectID = JDOHelper.getObjectId(configKeyObject);
		if (configKeyObjectID == null)
			throw new IllegalArgumentException("configKeyObject does not have a JDOObjectID: " + configKeyObject);

		return getConfig(pm, organisationID, configKeyObjectID.toString(), configKeyObject.getClass().getName());
	}

	/**
	 * Returns the ConfigModule corresponding to the given ConfigModuleID. If there is no such
	 * ConfigModule or Config, both are created.
	 *
	 * @param pm the {@link PersistenceManager} to use.
	 * @param moduleID the {@link ConfigModuleID} for the module to return.
	 * @return the ConfigModule corresponding to the given ConfigModuleID
	 * @throws ModuleException a wrapper for many kinds of Exceptions
	 */
	@SuppressWarnings("unchecked")
	public static ConfigModule getConfigModule(PersistenceManager pm, ConfigModuleID moduleID) {
		if (moduleID == null)
			throw new IllegalArgumentException("Given ConfigModuleID is null!");
		Class<ConfigModule> configModuleClass;
		try {
			configModuleClass = (Class<ConfigModule>) Class.forName(ConfigModule.getClassNameOutOfCfModKey(moduleID.cfModKey));
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Given ConfigModuleID stores an unknown kind of ConfigModule!", e);
		}
		Config correspondingConfig = getConfig(pm, moduleID.organisationID, moduleID.configKey, moduleID.configType);
		String cfModID = ConfigModule.getCfModIDOutOfCfModKey(moduleID.cfModKey);
		return correspondingConfig.createConfigModule(configModuleClass, cfModID);
	}

	/**
	 * Returns all Configs with the given configType.
	 *
	 * @param pm PersistenceManager to use
	 * @param organisationID organisationID of the Configs
	 * @param configType The configType of the Configs to return
	 * @return All Configs of given configType
	 */
	@SuppressWarnings("unchecked")
	public static Collection<Config> getConfigsByType(PersistenceManager pm, String organisationID, String configType) {
		Query q = pm.newNamedQuery(Config.class, QUERY_GET_CONFIGS_BY_TYPE);
		return (Collection<Config>)q.execute(organisationID, configType);
	}

	/**
	 * Returns all ConfigIDs corresponding to Configs of the given type.
	 * @param pm {@link PersistenceManager} to use.
	 * @param organisationID organisationID of the Configs
	 * @param configType The configType of the Configs to return
	 * @return All ConfigIDs corresponding to Configs of the given <code>configType</code>
	 */
	@SuppressWarnings("unchecked")
	public static Collection<ConfigID> getConfigIDsByConfigType(PersistenceManager pm, String organisationID, String configType) {
		Query q = pm.newNamedQuery(Config.class, QUERY_GET_CONFIGIDS_BY_TYPE);
		return (Collection<ConfigID>) q.execute(organisationID, configType);
	}

	/**
	 * Get all Configs that are members of the given ConfigGroup
	 *
	 * @param pm The PersistenceManager to use
	 * @param configGroup The ConfigGroup to search members for
	 */
	@SuppressWarnings("unchecked")
	public static Collection<Config> getConfigsForGroup(PersistenceManager pm, ConfigGroup configGroup) {
		Query q = pm.newNamedQuery(Config.class, QUERY_GET_CONFIGS_FOR_GROUP);
		return (Collection<Config>) q.execute(
				configGroup.getOrganisationID(),
				configGroup.getConfigType(),
				configGroup.getConfigKey()
		);
	}

	/**
	 * Get all Configs that are members of the ConfigGroup defined by the
	 * given ids.
	 */
	@SuppressWarnings("unchecked")
	public static Collection<Config> getConfigsForGroup(PersistenceManager pm, String organisationID, String configType, String configKey) {
		Query q = pm.newNamedQuery(Config.class, QUERY_GET_CONFIGS_FOR_GROUP);
		return (Collection<Config>) q.execute(organisationID, configType, configKey);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((configKey == null) ? 0 : configKey.hashCode());
		result = PRIME * result + ((configType == null) ? 0 : configType.hashCode());
		result = PRIME * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass())
			return false;
		final Config other = (Config) obj;
		return
				Util.equals(other.configKey, this.configKey) &&
				Util.equals(other.configType, this.configType) &&
				Util.equals(other.organisationID, this.organisationID);
	}
}
