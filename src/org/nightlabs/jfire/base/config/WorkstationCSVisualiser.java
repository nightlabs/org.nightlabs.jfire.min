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

package org.nightlabs.jfire.base.config;

import javax.jdo.FetchPlan;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.jfire.config.ConfigGroup;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.dao.ConfigSetupDAO;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.dao.WorkstationDAO;
import org.nightlabs.jfire.workstation.id.WorkstationID;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class WorkstationCSVisualiser implements ConfigSetupVisualiser 
{
	private static String[] WORKSTATION_FETCH_GROUPS = new String[] {FetchPlan.DEFAULT}; 	
	
	/**
	 * @see org.nightlabs.jfire.base.config.ConfigSetupVisualiser#getKeyObjectName(org.nightlabs.jfire.base.config.id.ConfigID)
	 */
	public String getKeyObjectName(ConfigID configID) {
		try {
			WorkstationID workstaionID = new WorkstationID(configID.configKey);
			Workstation workstation = WorkstationDAO.sharedInstance().getWorkstation(
					workstaionID, WORKSTATION_FETCH_GROUPS, 
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, 
					new NullProgressMonitor()
				);
			return workstation.getWorkstationID();
		} catch (Exception e) {
			return configID.configKey;
		} 
	}
	
	public String getConfigDescription(ConfigID configID) 
	{
		ProgressMonitor monitor = new NullProgressMonitor();
		ConfigSetup setup = ConfigSetupDAO.sharedInstance().getConfigSetupForConfigType(configID, monitor);
		if (setup == null)
			return configID.configKey;
		if (ConfigSetupDAO.sharedInstance().isConfigGroup(configID, monitor)) {
			ConfigGroup group = setup.getConfigGroup(configID.configKey);
			return String.format(Messages.getString("org.nightlabs.jfire.base.config.WorkstationCSVisualiser.configGroupDescription"), group.getName()); //$NON-NLS-1$
		}		
		return String.format(Messages.getString("org.nightlabs.jfire.base.config.WorkstationCSVisualiser.workstationConfigDescription"), getKeyObjectName(configID)); //$NON-NLS-1$
	}
}
