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

package org.nightlabs.jfire.base.workstation;

import org.nightlabs.jfire.base.jdo.JDOObjectProvider;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.WorkstationManager;
import org.nightlabs.jfire.workstation.WorkstationManagerUtil;
import org.nightlabs.jfire.workstation.id.WorkstationID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class WorkstationProvider extends JDOObjectProvider {

	/**
	 * 
	 */
	public WorkstationProvider() {
		super();
	}

	protected Object retrieveJDOObject(String scope, Object objectID, String[] fetchGroups, int maxFetchDepth) throws Exception {
		WorkstationManager wm = WorkstationManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
		if (!(objectID instanceof WorkstationID))
			return null;
		return wm.getWorkstation(((WorkstationID)objectID).organisationID, ((WorkstationID)objectID).workstationID, fetchGroups);
	}
	
	/**
	 * Returns the workstation with the given WorkstationID and detached with 
	 * the given fetchGroups (out of Cache if found).
	 * 
	 * @param id WorkstationID of the desired Workstation
	 * @param fetchGroups FetchGroups to detach the Workstation with
	 */
  public Workstation getWorkstation(WorkstationID id, String[] fetchGroups, int maxFetchDepth)
	{
		return (Workstation)super.getJDOObject(null, id, fetchGroups, maxFetchDepth);
	}	
	
	
	private static WorkstationProvider sharedInstance;
	
	public static WorkstationProvider sharedInstance() {
		if (sharedInstance == null) 
			sharedInstance = new WorkstationProvider();
		return sharedInstance;
	}

}
