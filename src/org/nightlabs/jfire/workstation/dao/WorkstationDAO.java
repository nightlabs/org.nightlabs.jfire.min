package org.nightlabs.jfire.workstation.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.WorkstationManagerRemote;
import org.nightlabs.jfire.workstation.id.WorkstationID;
import org.nightlabs.jfire.workstation.search.WorkstationQuery;
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

	private WorkstationManagerRemote workstationManager;

	@Override
	protected Collection<Workstation> retrieveJDOObjects(
			Set<WorkstationID> workstationIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor
	) throws Exception
	{
		WorkstationManagerRemote wm = workstationManager;

		if (wm == null)
			wm = JFireEjb3Factory.getRemoteBean(WorkstationManagerRemote.class, SecurityReflector.getInitialContextProperties());

		return wm.getWorkstations(workstationIDs, fetchGroups, maxFetchDepth);
	}

	public List<Workstation> getWorkstations(Set<WorkstationID> workstationIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, workstationIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public synchronized List<Workstation> getWorkstations(QueryCollection<? extends WorkstationQuery> workstationQueries, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			workstationManager = JFireEjb3Factory.getRemoteBean(WorkstationManagerRemote.class, SecurityReflector.getInitialContextProperties());
			Set<WorkstationID> workstationIDs = workstationManager.getWorkstationIDs(workstationQueries);
			return getJDOObjects(null, workstationIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			workstationManager = null;
		}
	}

	public synchronized List<Workstation> getWorkstations(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			workstationManager = JFireEjb3Factory.getRemoteBean(WorkstationManagerRemote.class, SecurityReflector.getInitialContextProperties());
			Set<WorkstationID> workstationIDs = workstationManager.getWorkstationIDs();
			return getJDOObjects(null, workstationIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			workstationManager = null;
		}
	}

	public Workstation getWorkstation(WorkstationID workstationID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObject(null, workstationID, fetchGroups, maxFetchDepth, monitor);
	}

	public Workstation storeWorkstation(Workstation workstation, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		try {
			WorkstationManagerRemote wm = JFireEjb3Factory.getRemoteBean(WorkstationManagerRemote.class, SecurityReflector.getInitialContextProperties());
			Workstation res = wm.storeWorkstation(workstation, get, fetchGroups, maxFetchDepth);
			if (res != null)
				getCache().put(null, res, fetchGroups, maxFetchDepth);
			return res;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
