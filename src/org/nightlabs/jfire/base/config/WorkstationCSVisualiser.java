/*
 * Created 	on Aug 26, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.config;

import javax.jdo.FetchPlan;

import org.nightlabs.jfire.base.workstation.WorkstationProvider;
import org.nightlabs.jfire.config.ConfigGroup;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.id.WorkstationID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class WorkstationCSVisualiser implements ConfigSetupVisualiser {

	private static String[] WORKSTATION_FETCH_GROUPS = new String[] {FetchPlan.DEFAULT}; 	
	
	/**
	 * 
	 */
	public WorkstationCSVisualiser() {
		super();
	}

	/**
	 * @see org.nightlabs.jfire.base.config.ConfigSetupVisualiser#getKeyObjectName(org.nightlabs.jfire.base.config.id.ConfigID)
	 */
	public String getKeyObjectName(ConfigID configID) {
		try {
			WorkstationID workstaionID = new WorkstationID(configID.configKey);
			Workstation workstation = WorkstationProvider.sharedInstance().getWorkstation(workstaionID, WORKSTATION_FETCH_GROUPS);
			return workstation.getWorkstationID();
		} catch (Exception e) {
			return configID.configKey;
		} 
	}
	
	public String getConfigDescription(ConfigID configID) {
		ConfigSetup setup = ConfigSetupRegistry.sharedInstance().getConfigSetupForConfigType(configID);
		if (setup == null)
			return configID.configKey;
		if (ConfigSetupRegistry.sharedInstance().isConfigGroup(configID)) {
			ConfigGroup group = setup.getConfigGroup(configID.configKey);
			return "ConfigGroup "+group.getName();
		}		
		return "Config for workstation "+getKeyObjectName(configID);
	}

}
