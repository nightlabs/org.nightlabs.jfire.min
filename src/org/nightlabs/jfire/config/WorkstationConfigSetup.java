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
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.WorkstationResolveStrategy;
import org.nightlabs.jfire.workstation.id.WorkstationID;

import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
// TODO: WORKAROUND: Use inheritance strategy superclass-table when jpox bug fixed
/**
 * UserConfigSetup provides the configuration-setup on User basis for JFire.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 * 		persistence-capable-superclass="org.nightlabs.jfire.config.ConfigSetup"
 *		detachable = "true"
 *		table="JFireBase_WorkstationConfigSetup"
 *
 * @jdo.inheritance strategy = "new-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_WorkstationConfigSetup")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class WorkstationConfigSetup extends ConfigSetup
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This is the configType for ConfigGroups withing the UserConfig configuration
	 * setup.
	 */
	public static final String CONFIG_GROUP_CONFIG_TYPE_WORKSTATION_CONFIG = "org.nightlabs.jfire.config.WorkstationConfigGroup";
	
	/**
	 * This is the configType for Configs whithin the UserConfig Configuration
	 * setup.
	 */
	public static final String CONFIG_TYPE_WORKSTATION_CONFIG = Workstation.class.getName();

	/**
	 * This is the configSetupType for ConfigSetups whithin the UserConfig Configuration
	 * setup.
	 */
	public static final String CONFIG_SETUP_TYPE_WORKSTATION = WorkstationConfigSetup.class.getName();

	public WorkstationConfigSetup(String organisationID) {
		super(organisationID, CONFIG_SETUP_TYPE_WORKSTATION);
		this.configType = CONFIG_TYPE_WORKSTATION_CONFIG;
		this.configGroupType = CONFIG_GROUP_CONFIG_TYPE_WORKSTATION_CONFIG;
		this.targetClass = Workstation.class.getName();
	}


	/**
	 * Ensures all users (userType = USER_TYPE_USER and USER_TYPE_ORGANISATION)
	 * have a Config assigned and returns all Configs.
	 *
	 * @param pm The {@link PersistenceManager} to use.
	 * @param organisationID The organisationID for which to chech.
	 * @param get Whether to return all Workstation {@link Config}s.
	 */
	protected static Collection<Config> ensureAllWorkstationsHaveConfig(PersistenceManager pm, String organisationID, boolean get) {
		if (
				// number of all Configs linked to users
				NLJDOHelper.getObjectCount(
						pm, Config.class,
						"configType == \""+CONFIG_TYPE_WORKSTATION_CONFIG+"\"",
						"this.configKey",
						false
					)
					==
				NLJDOHelper.getObjectCount(
						pm,
						Workstation.class,
						null,
						"this.workstationID",
						false
					)
			) {
			if (!get)
				// nothing has to be done when no result requested
				return null;
			// return all "Workstation"-Configs when get is true
			if (organisationID == null || "".equals(organisationID))
				throw new IllegalArgumentException("Get is set to true, but no organisationID was specified.");
			return Config.getConfigsByType(pm, organisationID, CONFIG_TYPE_WORKSTATION_CONFIG);
		}

		Collection<Config> configs = new ArrayList<Config>();
		for (Iterator<Workstation> iter = pm.getExtent(Workstation.class).iterator(); iter.hasNext();) {
			Workstation workstation = iter.next();
			configs.add(Config.getConfig(pm, workstation.getOrganisationID(), workstation));
		}
		// Commented as it led to configType beeing null in db afterwards
//		if (!countEqual)
//			JDOHelper.makeDirty(ConfigSetup.getConfigSetup(pm, organisationID, CONFIG_SETUP_TYPE_WORKSTATION), "configType");
		return configs;
	}

	/**
	 * Returns and id-object for a Config pointing to the given Workstation.
	 *
	 * @param workstation The workstation the referenced Config should point to
	 * @return And id-object for a Config pointing to the given workstation.
	 */
	public static ConfigID getWorkstationConfigID(Workstation workstation) {
		return ConfigID.create(workstation.getOrganisationID(), workstation);
	}

	/**
	 * Returns and id-object for a Config pointing to the given Workstation.
	 *
	 * @param workstationID The workstationID the referenced Config should point to
	 * @return And id-object for a Config pointing to the given workstation.
	 */
	public static ConfigID getWorkstationConfigID(WorkstationID workstationID) {
		return ConfigID.create(workstationID.organisationID, ConfigID.getConfigKeyForLinkObject(workstationID), Workstation.class.getName());
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigSetup#ensureSetupPrerequisites(javax.jdo.PersistenceManager)
	 */
	@Override
	public void ensureSetupPrerequisites(PersistenceManager pm) {
		ensureAllWorkstationsHaveConfig(pm, getOrganisationID(), false);
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigSetup#getAllSetupConfigs(javax.jdo.PersistenceManager)
	 */
	@Override
	public Collection<Config> getAllSetupConfigs(PersistenceManager pm) {
		return ensureAllWorkstationsHaveConfig(pm, getOrganisationID(), true);
	}
	
	/**
	 * Returns the {@link ConfigModule} of the given class linked to the 
	 * {@link Workstation} the current user is logged on. 
	 * If no such ConfigModule exists this method
	 * tries to auto-create it.
	 * 
	 * @param <T> The class of the config-module to be searched for (or created).
	 * @param pm The persistenceManager to use.
	 * @param cfModClass The class of the config-module to be searched for (or created).
	 * @return The {@link ConfigModule} linked to the current {@link Workstation}.
	 */
	public static <T extends ConfigModule> T getWorkstationConfigModule(PersistenceManager pm, Class<T> cfModClass) {
		Workstation workstation = Workstation.getWorkstation(pm, WorkstationResolveStrategy.FALLBACK);
		Config config = Config.getConfig(pm, workstation.getOrganisationID(), workstation);
		return (T) config.createConfigModule(cfModClass);
	}
}
