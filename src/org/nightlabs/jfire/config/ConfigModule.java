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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Key;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Value;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;

import org.nightlabs.inheritance.FieldInheriter;
import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.inheritance.Inheritable;
import org.nightlabs.inheritance.InheritanceCallbacks;
import org.nightlabs.jdo.inheritance.JDOInheritanceManager;
import org.nightlabs.jdo.inheritance.JDOSimpleFieldInheriter;
import org.nightlabs.jfire.config.id.ConfigModuleID;
import org.nightlabs.util.Util;

/**
 * Subclasses of ConfigModule are used to store User, Workstation (or more)
 * based configuration modules on the JFire server.
 * <p>
 * To register a new ConfigModule create, a new subclass of ConfigModule and
 * register its class to the appropriate {@link ConfigSetup}.
 * <p>
 * {@link ConfigModule}s are not intended to be instantiated manually, they are managed
 * by their {@link Config}. {@link Config#_createConfigModule(Class, String)}.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Niklas Schiffler <nick@nightlabs.de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.config.id.ConfigModuleID"
 *		detachable="true"
 *		table="JFireBase_ConfigModule"
 *
 * @jdo.version strategy="version-number"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="organisationID, configKey, configType, cfModKey"
 *
 * @jdo.query
 *    name="getConfigModule"
 *    query="SELECT
 *      WHERE organisationID == paramOrganisationID &&
 *            configKey == paramConfigKey &&
 *            configType == paramConfigType &&
 *            cfModKey == paramCfModKey
 *      PARAMETERS String paramOrganisationID, String paramConfigKey, String paramConfigType, String paramCfModKey
 *      import java.lang.String"
 *
 * @jdo.query
 *    name="getExistingConfigModulesForConfig"
 *    query="SELECT
 *      WHERE organisationID == paramOrganisationID &&
 *            configKey == paramConfigKey &&
 *            configType == paramConfigType
 *      PARAMETERS String paramOrganisationID, String paramConfigKey, String paramConfigType
 *      import java.lang.String"
 *
 * @jdo.fetch-group name="ConfigModule.config" fields="config"
 * @jdo.fetch-group name="ConfigModule.fieldMetaDataMap" fields="fieldMetaDataMap"
 * @jdo.fetch-group name="ConfigModule.this" fetch-groups="default, ConfigModule.fieldMetaDataMap"
 */
@PersistenceCapable(
	objectIdClass=ConfigModuleID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_ConfigModule")
@Version(strategy=VersionStrategy.VERSION_NUMBER)
@FetchGroups({
	@FetchGroup(
		name=ConfigModule.FETCH_GROUP_CONFIG,
		members=@Persistent(name="config")),
	@FetchGroup(
		name=ConfigModule.FETCH_GROUP_FIELDMETADATAMAP,
		members=@Persistent(name="fieldMetaDataMap")),
	@FetchGroup(
		fetchGroups={"default", ConfigModule.FETCH_GROUP_FIELDMETADATAMAP},
		name=ConfigModule.FETCH_GROUP_THIS_CONFIG_MODULE,
		members={})
})
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Queries({
	@javax.jdo.annotations.Query(
		name=ConfigModule.QUERY_GET_CONFIGMODULE,
		value="SELECT WHERE organisationID == paramOrganisationID && configKey == paramConfigKey && configType == paramConfigType && cfModKey == paramCfModKey PARAMETERS String paramOrganisationID, String paramConfigKey, String paramConfigType, String paramCfModKey import java.lang.String"),
	@javax.jdo.annotations.Query(
		name=ConfigModule.QUERY_GET_EXISTING_CONFIG_MODULES_FOR_CONFIG,
		value="SELECT WHERE organisationID == paramOrganisationID && configKey == paramConfigKey && configType == paramConfigType PARAMETERS String paramOrganisationID, String paramConfigKey, String paramConfigType import java.lang.String")
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public abstract class ConfigModule implements Serializable, Inheritable, InheritanceCallbacks
{
//	private static final Logger logger = Logger.getLogger(ConfigModule.class);
	private static final long serialVersionUID = 1L;
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_CONFIG_MODULE = "ConfigModule.this";
	public static final String FETCH_GROUP_CONFIG = "ConfigModule.config";
	public static final String FETCH_GROUP_FIELDMETADATAMAP = "ConfigModule.fieldMetaDataMap";

	public static final String QUERY_GET_CONFIGMODULE = "getConfigModule";
	public static final String QUERY_GET_EXISTING_CONFIG_MODULES_FOR_CONFIG = "getExistingConfigModulesForConfig";

	public static final String FIELD_NAME_FIELDMETADATAMAP = "fieldMetaDataMap";
	public static final String FIELD_NAME_FIELDMETADATA_CONFIGMODULE = ConfigModule.class.getName();

	/**
	 * In contrast to our other JDO classes, <code>ConfigModule</code>s only have one
	 * constructor - the default constructor. If you want to execute certain code
	 * in your subclasses only on the very first construction (i.e. the real creation of
	 * your JDO object), you should implement the method {@link #init()}.
	 * <p>
	 * This constructor
	 * is called every time when a ConfigModule is loaded from the datastore or when a copy
	 * is detached etc..
	 * </p>
	 */
	protected ConfigModule() {}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @!jdo.column length="200"
	 */
	@PrimaryKey
	private String configKey;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	protected String configType;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="150"
	 */
	@PrimaryKey
	@Column(length=150)
	private String cfModKey;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String cfModID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Config config;

	/**
	 * key: String fieldName
	 * value: ConfigModuleSimpleFieldMetaData fieldMetaData
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="org.nightlabs.jfire.config.ConfigModuleSimpleFieldMetaData"
	 *		dependent-value="true"
	 *		mapped-by="configModule"
	 *
	 * @jdo.key mapped-by="fieldName"
	 *
	 * @!jdo.join
	 *
	 * @!jdo.map-vendor-extension vendor-name="jpox" key="key-field" value="fieldName"
	 */
	@Persistent(
		mappedBy="configModule",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Key(mappedBy="fieldName")
	@Value(dependent="true")
	protected Map<String, ConfigModuleSimpleFieldMetaData> fieldMetaDataMap;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	protected boolean groupConfigModule;

	/**
	 * This method is called only once at the very first time a new ConfigModule is created. Do
	 * not override this method for initialisations, but use {@link #init()} instead.
	 *
	 * @param organisationID 1st part of pk
	 * @param configKey 2nd part of pk
	 * @param configType 3rd part of pk
	 * @param cfModID 4th part of pk
	 */
	protected void setPrimaryKeyFields(String organisationID, String configKey, String configType, String cfModID)
	{
		if (this.organisationID != null)
			throw new IllegalStateException("Primary Key fields were already set. This method can only be called once");
		if (organisationID == null)
			throw new IllegalArgumentException("Parameter organisationID must not be null!");
		this.organisationID = organisationID;

		if (this.configKey != null)
			throw new IllegalStateException("Primary Key fields were already set. This method can only be called once");
		if (configKey == null)
			throw new IllegalArgumentException("Parameter configKey must not be null!");
		this.configKey = configKey;

		if (this.configType != null)
			throw new IllegalStateException("Primary Key fields were already set. This method can only be called once");
		if (configKey == null)
			throw new IllegalArgumentException("Parameter configKey must not be null!");
		this.configType = configType;

		this.cfModID = cfModID;
		this.fieldMetaDataMap = new HashMap<String, ConfigModuleSimpleFieldMetaData>();
		cfModKey = getCfModKey(this.getClass(), cfModID);
	}

	/**
	 * Create the cfModKey for a ConfigModule. It contains the
	 * classname and cfModID separated by /.
	 */
	public static String getCfModKey(Class<? extends ConfigModule> cfModClass, String cfModID) {
		String result = cfModClass.getName();
		if (cfModID != null && !"".equals(cfModID))
			result += "/" + cfModID;
		return result;
	}

	/**
	 * Extract the classname out of a given cfModKey
	 */
	public static String getClassNameOutOfCfModKey(String cfModKey) {
		if (!cfModKey.contains("/"))
			return cfModKey;
		else
			return cfModKey.substring(0, cfModKey.indexOf("/"));
	}

	/**
	 * Extract the cfModID out of a given cfModKey
	 */
	public static String getCfModIDOutOfCfModKey(String cfModKey) {
		String className = getClassNameOutOfCfModKey(cfModKey);
		String result = cfModKey.replace(className, "");
		if (result.equals(""))
			return null;
		if (result.substring(0,1).equals("/"))
			result = result.substring(1, result.length());
		if (result.equals(""))
			return null;
		else
			return result;
	}

	/**
	 * @return The organisationID, which is 1st part of the 5-part primary-key
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	
	/**
	 * The config-key is usually the String-representation of the object
	 * the Config of this ConfigModule is linked to.
	 * 
	 * @return The configKey, which is the 2nd part of the 5-part primary-key
	 */
	public String getConfigKey()
	{
		return configKey;
	}
	
	/**
	 * The config-type is usually the class-name of the object a ConfigModule
	 * is linked to.
	 * 
	 * @return The configType, which is the 3rd part of the 5-part primary-key
	 */
	public String getConfigType() {
		return configType;
	}
	
	/**
	 * The cfModKey is usually a combination of the classname of the ConfigModule
	 * and the cfModID.
	 * 
	 * @return The cfModKey of this ConfigModule.
	 */
	public String getCfModKey()
	{
		return cfModKey;
	}
	/**
	 * @return The cfModID, which is the 4th part of 5-part primary-key
	 */
	public String getCfModID()
	{
		return cfModID;
	}

	/**
	 * This method is called after creation of a ConfigModule by the Config (together with {@link #setPrimaryKeyFields(String, String, String, String)}).
	 *
	 * @param config The config which manages this ConfigModule.
	 */
	protected void setConfig(Config config)
	{
		this.config = config;
	}

	/**
	 * Get the Config that holds this ConfigModules.
	 */
	public Config getConfig() {
		return config;
	}

	/**
	 * Returns whether this {@link ConfigModule} belongs to a {@link ConfigGroup}.
	 *
	 * @return Whether this {@link ConfigModule} belongs to a {@link ConfigGroup}.
	 */
	public boolean isGroupConfigModule() {
		return groupConfigModule;
	}

	void setGroupConfigModule(boolean configModuleOfGroup) {
		this.groupConfigModule = configModuleOfGroup;
	}

	/**
	 * This callback method is called when a <code>ConfigModule</code> is created the
	 * first time and after it has been persisted to the datastore (hence, you can obtain
	 * a {@link PersistenceManager} using <code>JDOHelper.getPersistenceManager(this)</code>
	 * if necessary).
	 * <p>
	 * Important: This method is only called exactly once for every <code>ConfigModule</code> and never again!
	 * Hence, when your <code>ConfigModule</code> is changed, you have to react on this by other means (e.g. a
	 * {@link javax.jdo.listener.StoreCallback}), if desired.
	 * </p>
	 * <p>
	 * Use this to initialize members and set default values for your <code>ConfigModule</code> implementation.
	 * </p>
	 * <p>
	 * Additionally <code>ConfigModule</code>s can be initialised by {@link ConfigModuleInitialiser}s.
	 * </p>
	 */
	public abstract void init();

	private static JDOSimpleFieldInheriter fieldInheriter = new JDOSimpleFieldInheriter();

	public FieldInheriter getFieldInheriter(String fieldName)
	{
		return fieldInheriter;
	}

	private static Set<String> nonInheritableFields = new HashSet<String>();

	public FieldMetaData getFieldMetaData(String fieldName)
	{
		if (fieldName.startsWith("jdo"))
			return null;

		synchronized (nonInheritableFields)
		{
			if (nonInheritableFields.isEmpty())
			{
				nonInheritableFields.add("organisationID");
				nonInheritableFields.add("configType");
				nonInheritableFields.add("configKey");
				nonInheritableFields.add("cfModID");
				nonInheritableFields.add("cfModKey");
				nonInheritableFields.add("config");
				nonInheritableFields.add("fieldMetaDataMap");
				nonInheritableFields.add("groupConfigModule");
			}
			if (nonInheritableFields.contains(fieldName))
				return null;
		}

		return _getFieldMetaData(getFieldMetaData_translateFieldName(fieldName));
	}

	/**
	 * Usually there is only one fieldMetaData for one ConfigModule
	 * 	-> either inherit everything or nothing depending on settings.
	 *
	 * Note: To change this behaviour overwrite this method!
	 *
	 * @param fieldName the name of the field for which to retrieve the {@link FieldMetaData}
	 * @return the translated name of the field.
	 */
	protected String getFieldMetaData_translateFieldName(String fieldName) {
		return ConfigModule.class.getName();
	}

	protected FieldMetaData _getFieldMetaData(String fieldName)
	{
		ConfigModuleSimpleFieldMetaData fmd = fieldMetaDataMap.get(fieldName);
		if (fmd == null) {
			fmd = new ConfigModuleSimpleFieldMetaData(this, fieldName);
			fieldMetaDataMap.put(fieldName, fmd);
		}
		return fmd;
	}

	public void preInherit(Inheritable mother, Inheritable child) {
		// Debugging problems with the FielMetaData information.
//		if (mother != this)
//			return;
//
//		ConfigModule motherModule = (ConfigModule) mother;
//		ConfigModule childModule = (ConfigModule) child;
//
//		motherModule.getFieldMetaData(FIELD_NAME_FIELDMETADATA_CONFIGMODULE).isValueInherited();
//		childModule.getFieldMetaData(FIELD_NAME_FIELDMETADATA_CONFIGMODULE).isValueInherited();
//		if (logger.isDebugEnabled())
//			logger.debug("preInherit: childClass=" + child.getClass().getName() + " childValueInherited=" + childValueInherited);
	}

	public void postInherit(Inheritable mother, Inheritable child)
	{
//		ConfigModule childModule = (ConfigModule) child;
//
//		boolean childValueInherited = childModule.getFieldMetaData(FIELD_NAME_FIELDMETADATA_CONFIGMODULE).isValueInherited();
//		if (logger.isDebugEnabled())
//			logger.debug("postInherit: childClass=" + child.getClass().getName() + " childValueInherited=" + childValueInherited);
	}

	/**
	 * This method queries the datstore and returns the desired ConfigModule or <code>null</code>. You should
	 * not use it directly! Use {@link Config#getConfigModule(Class, String, boolean) } instead.
	 */
	protected static ConfigModule getConfigModule(PersistenceManager pm, Config config, Class<? extends ConfigModule> cfModClass, String cfModID)
	{
		Query q = pm.newNamedQuery(ConfigModule.class, QUERY_GET_CONFIGMODULE);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("paramOrganisationID", config.getOrganisationID());
		args.put("paramConfigKey", config.getConfigKey());
		args.put("paramConfigType", config.getConfigType());
		args.put("paramCfModKey", getCfModKey(cfModClass, cfModID));
		q.setUnique(true);
		return (ConfigModule)q.executeWithMap(args);
	}

	/**
	 * Inherits the ConfigModules corresponding to the given groupConfig of all
	 * members of the given configGroup from the given configModule.
	 *
	 * @param pm The PersistenceManager to use
	 */
	public static void inheritAllGroupMemberConfigModules(PersistenceManager pm, ConfigGroup configGroup, ConfigModule groupModule)
	{
		if (!JDOHelper.isPersistent(configGroup))
			throw new IllegalArgumentException("Please call this method only with a persitent instance of a ConfigGroup.");
		if (!JDOHelper.isPersistent(groupModule))
			throw new IllegalArgumentException("Please call this method only with a persitent instance of a ConfigModule.");

		Collection<Config> members = Config.getConfigsForGroup(pm, configGroup);
		for (Iterator<Config> iter = members.iterator(); iter.hasNext();) {
			Config member = iter.next();
//			ConfigModule configModule = getAutoCreateConfigModule(pm, member, groupModule.getClass(), groupModule.getCfModID());
			ConfigModule configModule = member.createConfigModule(groupModule.getClass(), groupModule.getCfModID());
//			inheritConfigModule(groupModule, configModule);
//			configModule.setGroupAllowsOverride(groupModule.isGroupMembersMayOverride());
			inheritConfigModule(groupModule, configModule);
		}
	}

	/**
	 * Inherits all fields from the given groupModule to the given configModule
	 * according to the inheritance settings in the groupModule
	 */
	protected static void inheritConfigModule(ConfigModule groupModule, ConfigModule configModule) {
		(new JDOInheritanceManager()).inheritAllFields(groupModule, configModule);
//		JDOHelper.makeDirty(configModule);
	}

	/**
	 * Get all ConfigModules that were already created for the given config.
	 */
	public static Collection<ConfigModule> getExistingConfigModulesForConfig(PersistenceManager pm, Config config) {
		Query query = pm.newNamedQuery(ConfigModule.class, QUERY_GET_EXISTING_CONFIG_MODULES_FOR_CONFIG);
		return (Collection<ConfigModule> )query.execute(config.getOrganisationID(), config.getConfigKey(), config.getConfigType());
	}


//	public void preInherit(Inheritable _mother, Inheritable _child) {
//		if (_mother != this)
//			return;
//
//		ConfigModule mother = (ConfigModule) _mother;
//		ConfigModule child = (ConfigModule) _child;
//
//		FieldMetaData m_fmd = mother.getFieldMetaData(ConfigModule.class.getName());
//		FieldMetaData c_fmd = child.getFieldMetaData(ConfigModule.class.getName());
//
//		if ((m_fmd.getWritableByChildren() & FieldMetaData.WRITABLEBYCHILDREN_YES) == 0) {
//			if (!c_fmd.isValueInherited())
//				c_fmd.setValueInherited(true);
//
//			if (c_fmd.isWritable())
//				c_fmd.setWritable(false);
//		}
//		else {
//			if (!c_fmd.isWritable())
//				c_fmd.setWritable(true);
//		}
//
//		copyWritableByChildren(m_fmd, mother);
//		copyWritableByChildren(c_fmd, child);
//	}
//
//	private static void copyWritableByChildren(FieldMetaData sourceFmd, ConfigModule targetCfMod)
//	{
//		for (Iterator it = targetCfMod.fieldMetaDataMap.values().iterator(); it.hasNext();) {
//			FieldMetaData	fmd = (FieldMetaData) it.next();
//			if (fmd == sourceFmd)
//				continue;
//
//			fmd.setWritableByChildren(
//					(byte)(sourceFmd.getWritableByChildren() | FieldMetaData.WRITABLEBYCHILDREN_INHERITED));
//			fmd.setWritable(sourceFmd.isWritable());
//		}
//	}

//	public void postInherit(Inheritable mother, Inheritable child) {
//
//	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((cfModKey == null) ? 0 : cfModKey.hashCode());
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
		final ConfigModule o = (ConfigModule) obj;
		return
				Util.equals(organisationID, o.organisationID) &&
				Util.equals(configKey, o.configKey) &&
				Util.equals(configType, o.configType) &&
				Util.equals(cfModKey, o.cfModKey);
	}
}
