package org.nightlabs.jfire.editlock.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.editlock.AcquireEditLockResult;
import org.nightlabs.jfire.editlock.EditLock;
import org.nightlabs.jfire.editlock.EditLockManagerRemote;
import org.nightlabs.jfire.editlock.ReleaseReason;
import org.nightlabs.jfire.editlock.id.EditLockID;
import org.nightlabs.jfire.editlock.id.EditLockTypeID;
import org.nightlabs.progress.ProgressMonitor;

public class EditLockDAO extends BaseJDOObjectDAO<EditLockID, EditLock>
{
	private static EditLockDAO sharedInstance = null;

	public static EditLockDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (EditLockDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new EditLockDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	protected Collection<EditLock> retrieveJDOObjects(Set<EditLockID> editLockIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
	{
		EditLockManagerRemote m = editLockManager;
		if (m == null) m = getEjbProvider().getRemoteBean(EditLockManagerRemote.class);
		return m.getEditLocks(editLockIDs, fetchGroups, maxFetchDepth);
	}

	private EditLockManagerRemote editLockManager;

	public synchronized List<EditLock> getEditLocks(ObjectID objectID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			editLockManager = getEjbProvider().getRemoteBean(EditLockManagerRemote.class);
			try {
				Set<EditLockID> editLockIDs = editLockManager.getEditLockIDs(objectID);
				return getJDOObjects(null, editLockIDs, fetchGroups, maxFetchDepth, monitor);
			} finally {
				editLockManager = null;
			}
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public EditLock getEditLock(EditLockID editLockID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObject(null, editLockID, fetchGroups, maxFetchDepth, monitor);
	}

	public AcquireEditLockResult acquireEditLock(EditLockTypeID editLockTypeID, ObjectID objectID, String description, String[] fetchGroups, int maxFetchDepth)
	{
		try {
			EditLockManagerRemote wm = getEjbProvider().getRemoteBean(EditLockManagerRemote.class);
			AcquireEditLockResult acquireEditLockResult = wm.acquireEditLock(editLockTypeID, objectID, description, fetchGroups, maxFetchDepth);
			getCache().put(null, acquireEditLockResult.getEditLock(), fetchGroups, maxFetchDepth);
			return acquireEditLockResult;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public void releaseEditLock(ObjectID objectID, ReleaseReason releaseReason, ProgressMonitor monitor)
	{
		try {
			EditLockManagerRemote wm = getEjbProvider().getRemoteBean(EditLockManagerRemote.class);
			wm.releaseEditLock(objectID, releaseReason);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
}
