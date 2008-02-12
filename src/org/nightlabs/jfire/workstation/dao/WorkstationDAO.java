package org.nightlabs.jfire.workstation.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.WorkstationManager;
import org.nightlabs.jfire.workstation.WorkstationManagerUtil;
import org.nightlabs.jfire.workstation.id.WorkstationID;
import org.nightlabs.progress.ProgressMonitor;

public class WorkstationDAO
extends BaseJDOObjectDAO<WorkstationID, Workstation>
{
	protected WorkstationDAO() { }

	private static WorkstationDAO sharedInstance = null;
	public static synchronized WorkstationDAO sharedInstance()
	{
		if (sharedInstance == null)
			sharedInstance = new WorkstationDAO();

		return sharedInstance;
	}

	@Override
	protected Collection<Workstation> retrieveJDOObjects(
			Set<WorkstationID> workstationIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	throws Exception
	{
		WorkstationManager wm = WorkstationManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
		return wm.getWorkstations(workstationIDs, fetchGroups, maxFetchDepth);
	}

	public List<Workstation> getWorkstations(Set<WorkstationID> workstationIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		if (workstationIDs == null) {
			try {
				WorkstationManager wm = WorkstationManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
				workstationIDs = wm.getWorkstationIDs();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return getJDOObjects(null, workstationIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public List<Workstation> getWorkstations(String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		return getWorkstations(null, fetchGroups, maxFetchDepth, monitor);
	}
	
	public Workstation getWorkstation(WorkstationID workstationID, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObject(null, workstationID, fetchGroups, maxFetchDepth, monitor);
	}
	
	public Workstation storeWorkstation(Workstation workstation, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		try {
			WorkstationManager wm = WorkstationManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			return wm.storeWorkstation(workstation, get, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
