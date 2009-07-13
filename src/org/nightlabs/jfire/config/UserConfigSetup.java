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
import java.util.Collection;
import java.util.Iterator;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;

import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
// WORKAROUND: Use inheritance strategy superclass-table when jpox bug fixed
/**
 * UserConfigSetup provides the configuration-setup on User basis for JFire.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 * 		persistence-capable-superclass="org.nightlabs.jfire.config.ConfigSetup"
 *		detachable="true"
 *		table="JFireBase_UserConfigSetup"
 *
 * @jdo.inheritance strategy="new-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_UserConfigSetup")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class UserConfigSetup extends ConfigSetup
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This is the configType for ConfigGroups withing the UserConfig configuration
	 * setup.
	 */
	public static final String CONFIG_GROUP_CONFIG_TYPE_USER_CONFIG = "org.nightlabs.jfire.config.UserConfigGroup";
	/**
	 * This is the configType for Configs whithin the UserConfig Configuration
	 * setup.
	 */
	public static final String CONFIG_TYPE_USER_CONFIG = User.class.getName();

	public static final String CONFIG_SETUP_TYPE_USER = UserConfigSetup.class.getName();

	public UserConfigSetup(String organisationID) {
		super(organisationID, CONFIG_SETUP_TYPE_USER);
		this.configType = CONFIG_TYPE_USER_CONFIG;
		this.configGroupType = CONFIG_GROUP_CONFIG_TYPE_USER_CONFIG;
		this.targetClass = User.class.getName();
	}


	/**
	 * Ensures all users (userType = USER_TYPE_USER and USER_TYPE_ORGANISATION)
	 * have a Config assigned and returns all Configs.
	 *
	 * @param pm The {@link PersistenceManager} to use.
	 * @param organisationID The organisationID to check for.
	 * @param get Whether to return all User {@link Config}s.
	 */
	protected static Collection<Config> ensureAllUsersHaveConfig(PersistenceManager pm, String organisationID, boolean get) {
		boolean countEqual = false;
		if (
				// number of all Configs linked to users
				NLJDOHelper.getObjectCount(
						pm, Config.class,
						"configType == \""+UserConfigSetup.CONFIG_TYPE_USER_CONFIG+"\"",
						"this.configKey",
						false
					)
					==
				NLJDOHelper.getObjectCount(
						pm,
						User.class,
						"((userType == \""+User.USER_TYPE_USER+"\") || "+
						"(userType == \""+User.USER_TYPE_ORGANISATION+"\")) && (userID != \""+User.USER_ID_SYSTEM+"\")",
						"this.userID",
						false
					)
			) {
			countEqual = true;
			// count of Configs and Users are equal
			if (!get)
				// nothing has to be done when no result requested
				return null;
			// return all "User"-Configs when get is true
			if (organisationID == null || "".equals(organisationID))
				throw new IllegalArgumentException("Get is set to true, but no organisationID was specified.");
			return Config.getConfigsByType(pm, organisationID, User.class.getName());
		}

//		Collection users = User.getUsersByType(pm, User.USER_TYPE_USER, User.USER_ID_SYSTEM);
		Collection<User> users = User.getUsersByType(pm, User.USER_TYPE_USER, null); // System user also gets a Config
		Collection<Config> configs = new ArrayList<Config>();
		for (Iterator<User> iter = users.iterator(); iter.hasNext();) {
			User user = iter.next();
			configs.add(Config.getConfig(pm, user.getOrganisationID(), user));
		}
		if (!countEqual)
			JDOHelper.makeDirty(ConfigSetup.getConfigSetup(pm, organisationID, CONFIG_SETUP_TYPE_USER), "configType");
		return configs;
	}

	/**
	 * Returns and id-object for a Config pointing to the given User.
	 *
	 * @param user The user the referenced Config should point to
	 * @return And id-object for a Config pointing to the given User.
	 */
	public static ConfigID getUserConfigID(User user) {
		return ConfigID.create(user.getOrganisationID(), user);
	}

	/**
	 * Returns and id-object for a Config pointing to the given User.
	 *
	 * @param userID The id of the user the referenced Config should point to
	 * @return And id-object for a Config pointing to the given User.
	 */
	public static ConfigID getUserConfigID(UserID userID) {
		return ConfigID.create(userID.organisationID, ConfigID.getConfigKeyForLinkObject(userID), User.class.getName());
	}

	/**
	 * @see org.nightlabs.jfire.config.ConfigSetup#ensureSetupPrerequisites(javax.jdo.PersistenceManager)
	 */
	@Override
	public void ensureSetupPrerequisites(PersistenceManager pm) {
		ensureAllUsersHaveConfig(pm, getOrganisationID(), false);
	}

	@Override
	public Collection<Config> getAllSetupConfigs(PersistenceManager pm) {
		return ensureAllUsersHaveConfig(pm, getOrganisationID(), true);
	}
	
	/**
	 * Returns the {@link ConfigModule} of the given class linked to the 
	 * current {@link User}. If no such ConfigModule exists this method
	 * tries to auto-create it.
	 * 
	 * @param <T> The class of the config-module to be searched for (or created).
	 * @param pm The persistenceManager to use.
	 * @param cfModClass The class of the config-module to be searched for (or created).
	 * @return The {@link ConfigModule} linked to the current {@link User}
	 */
	public static <T extends ConfigModule> T getUserConfigModule(PersistenceManager pm, Class<T> cfModClass) {
		User user = SecurityReflector.getUserDescriptor().getUser(pm);
		Config config = Config.getConfig(pm, user.getOrganisationID(), user);		
		return (T) config.createConfigModule(cfModClass);
	}
}
