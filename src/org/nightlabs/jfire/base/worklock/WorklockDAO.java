package org.nightlabs.jfire.base.worklock;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.jdo.JDOObjectDAO;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.worklock.AcquireWorklockResult;
import org.nightlabs.jfire.worklock.Worklock;
import org.nightlabs.jfire.worklock.WorklockManager;
import org.nightlabs.jfire.worklock.WorklockManagerUtil;
import org.nightlabs.jfire.worklock.id.WorklockID;
import org.nightlabs.jfire.worklock.id.WorklockTypeID;

public class WorklockDAO
		extends JDOObjectDAO<WorklockID, Worklock>
{
	private static WorklockDAO sharedInstance = null;

	public static WorklockDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (WorklockDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new WorklockDAO();
			}
		}
		return sharedInstance;
	}

	@SuppressWarnings("unchecked")
	@Implement
	protected Collection<Worklock> retrieveJDOObjects(Set<WorklockID> worklockIDs,
			String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor)
			throws Exception
	{
		WorklockManager wm = worklockManager;
		if (wm == null) wm = WorklockManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
		return wm.getWorklocks(worklockIDs, fetchGroups, maxFetchDepth);
	}

	private WorklockManager worklockManager;

	@SuppressWarnings("unchecked")
	public synchronized List<Worklock> getWorklocks(ObjectID objectID, String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor)
	{
		try {
			worklockManager = WorklockManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
			try {
				Set<WorklockID> worklockIDs = worklockManager.getWorklockIDs(objectID);
				return getJDOObjects(null, worklockIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				worklockManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public AcquireWorklockResult acquireWorklock(WorklockTypeID worklockTypeID, ObjectID objectID, String[] fetchGroups, int maxFetchDepth)
	{
		try {
			WorklockManager wm = WorklockManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
			AcquireWorklockResult acquireWorklockResult = wm.acquireWorklock(worklockTypeID, objectID, fetchGroups, maxFetchDepth);
			Cache.sharedInstance().put(null, acquireWorklockResult.getWorklock(), fetchGroups, maxFetchDepth);
			return acquireWorklockResult;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

}
