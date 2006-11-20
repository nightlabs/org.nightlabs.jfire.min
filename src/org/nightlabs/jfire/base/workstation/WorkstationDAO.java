package org.nightlabs.jfire.base.workstation;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.nightlabs.jfire.base.jdo.JDOObjectDAO;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.WorkstationManager;
import org.nightlabs.jfire.workstation.WorkstationManagerUtil;
import org.nightlabs.jfire.workstation.id.WorkstationID;

public class WorkstationDAO
extends JDOObjectDAO<WorkstationID, Workstation>
{

	protected Collection<Workstation> retrieveJDOObjects(
			Set<WorkstationID> workstationIDs, String[] fetchGroups, int maxFetchDepth,
			IProgressMonitor monitor)
	throws Exception
	{
		WorkstationManager wm = WorkstationManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
		return wm.getWorkstations(workstationIDs, fetchGroups, maxFetchDepth);
	}

	public List<Workstation> getWorkstations(Set<WorkstationID> workstationIDs, String[] fetchGroups, int maxFetchDepth,
			IProgressMonitor monitor)
	{
		if (workstationIDs == null) {
			try {
				WorkstationManager wm = WorkstationManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
				workstationIDs = wm.getWorkstationIDs();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return getJDOObjects(null, workstationIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public List<Workstation> getWorkstations(String[] fetchGroups, int maxFetchDepth,
			IProgressMonitor monitor)
	{
		return getWorkstations(null, fetchGroups, maxFetchDepth, monitor);
	}
}
