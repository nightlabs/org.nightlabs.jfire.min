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

import java.util.Collection;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jfire.config.id.ConfigID;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * A ConfigGroup is itself a Config so it can hold ConfigModules. Configs have
 * a member configGroup to indicate membership to a ConfigGroup.
 * ConfigGroups should use their configType as group type. One group
 * type should be created for all keyObjects used by Configs.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 * 		persistence-capable-superclass="org.nightlabs.jfire.config.Config"
 *		detachable="true"
 *		table="JFireBase_ConfigGroup"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="ConfigGroup.this" fetch-groups="Config.this, default" fields="name"
 * @jdo.fetch-group name="ConfigGroup.name" fetch-groups="default" fields="name"
 *
 *  @jdo.query
 *		name="getConfigGroupsByKeyObjectClass"
 *		query="SELECT
 *			WHERE this.organisationID == paramOrganisationID &&
 *            this.configType == paramClassname
 *			PARAMETERS String paramOrganisationID, String paramClassname
 *			import java.lang.String"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_ConfigGroup")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"Config.this", "default"},
		name=ConfigGroup.FETCH_GROUP_THIS_CONFIG_GROUP,
		members=@Persistent(name="name")),
	@FetchGroup(
		fetchGroups={"default"},
		name=ConfigGroup.FETCH_GROUP_NAME,
		members=@Persistent(name="name"))
})
@Queries(
	@javax.jdo.annotations.Query(
		name=ConfigGroup.QUERY_GET_CONFIG_GROUPS_BY_KEY_OBJECT_CLASS,
		value="SELECT WHERE this.organisationID == paramOrganisationID && this.configType == paramClassname PARAMETERS String paramOrganisationID, String paramClassname import java.lang.String")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ConfigGroup extends Config
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon! 
	 */
	public static final String FETCH_GROUP_THIS_CONFIG_GROUP = "ConfigGroup.this";
	
	public static final String FETCH_GROUP_NAME = "ConfigGroup.name";

	public static final String QUERY_GET_CONFIG_GROUPS_BY_KEY_OBJECT_CLASS = "getConfigGroupsByKeyObjectClass";

	protected ConfigGroup() {
		super();
	}

	public ConfigGroup(String organisationID, String configKey, String configType) {
		super(organisationID, configKey, configType);
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String name;

	/**
	 * Return a name for this ConfigGroup
	 *
	 * @return A name for this ConfigGroup
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set this ConfigGroup's name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the ConfigGroup the specified Config is assigned to. Will return null
	 * if the referenced Config can not be found or is not assigned to a ConfigGroup.
	 *
	 * @param pm PersistenceManager to lookup the Config.
	 * @param configID References a Config the assigned ConfigGroup should be searched for.
	 *
	 * @return ConfigGroup for referenced Config or null.
	 */
	public static ConfigGroup getConfigGroupForConfig(PersistenceManager pm, ConfigID configID) {
		Config config = (Config)pm.getObjectById(configID);
		return config.getConfigGroup();
	}


	/**
	 * Convenience? method for {@link #getConfigGroupForConfig(PersistenceManager, ConfigID)}
	 */
	public static ConfigGroup getConfigGroupForConfig(
			PersistenceManager pm,
			String organisationID,
			String configKey,
			String implementationClassName)
	{
		return getConfigGroupForConfig(pm, ConfigID.create(organisationID, configKey, implementationClassName));
	}


	/**
	 * Get all ConfigGroups with the given organisationID and configType.
	 * ConfigGroups should use their configType as group type. One group
	 * type should be created for all keyObjects used by Configs.
	 */
	@SuppressWarnings("unchecked")
	public static Collection<ConfigGroup> getConfigGroupsByKeyObjectClass(PersistenceManager pm, String organistationID, String configType) {
		Query q = pm.newNamedQuery(ConfigGroup.class, QUERY_GET_CONFIG_GROUPS_BY_KEY_OBJECT_CLASS);
		Collection<ConfigGroup> configs = (Collection<ConfigGroup>)q.execute(organistationID, configType);
		return configs;
	}

}
