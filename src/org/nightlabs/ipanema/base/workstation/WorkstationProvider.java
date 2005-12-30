/*
 * Created 	on Aug 26, 2005
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.workstation;

import org.nightlabs.ipanema.base.jdo.JDOObjectProvider;
import org.nightlabs.ipanema.base.login.Login;
import org.nightlabs.ipanema.workstation.Workstation;
import org.nightlabs.ipanema.workstation.WorkstationManager;
import org.nightlabs.ipanema.workstation.WorkstationManagerUtil;
import org.nightlabs.ipanema.workstation.id.WorkstationID;

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

	protected Object retrieveJDOObject(String scope, Object objectID, String[] fetchGroups) throws Exception {
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
  public Workstation getWorkstation(WorkstationID id, String[] fetchGroups)
	{
		return (Workstation)super.getJDOObject(null, id, fetchGroups);
	}	
	
	
	private static WorkstationProvider sharedInstance;
	
	public static WorkstationProvider sharedInstance() {
		if (sharedInstance == null) 
			sharedInstance = new WorkstationProvider();
		return sharedInstance;
	}

}
