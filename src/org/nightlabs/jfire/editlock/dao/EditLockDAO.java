package org.nightlabs.jfire.editlock.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.base.jdo.login.JFireLoginProvider;
import org.nightlabs.jfire.editlock.AcquireEditLockResult;
import org.nightlabs.jfire.editlock.EditLock;
import org.nightlabs.jfire.editlock.EditLockManager;
import org.nightlabs.jfire.editlock.EditLockManagerUtil;
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

	@SuppressWarnings("unchecked")
	@Implement
	protected Collection<EditLock> retrieveJDOObjects(Set<EditLockID> editLockIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
	{
		EditLockManager wm = editLockManager;
		if (wm == null) wm = EditLockManagerUtil.getHome(JFireLoginProvider.sharedInstance().getInitialContextProperties()).create();
		return wm.getEditLocks(editLockIDs, fetchGroups, maxFetchDepth);
	}

	private EditLockManager editLockManager;

	@SuppressWarnings("unchecked")
	public synchronized List<EditLock> getEditLocks(ObjectID objectID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			editLockManager = EditLockManagerUtil.getHome(JFireLoginProvider.sharedInstance().getInitialContextProperties()).create();
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
			EditLockManager wm = EditLockManagerUtil.getHome(JFireLoginProvider.sharedInstance().getInitialContextProperties()).create();
			AcquireEditLockResult acquireEditLockResult = wm.acquireEditLock(editLockTypeID, objectID, description, fetchGroups, maxFetchDepth);
			Cache.sharedInstance().put(null, acquireEditLockResult.getEditLock(), fetchGroups, maxFetchDepth);
			return acquireEditLockResult;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public void releaseEditLock(ObjectID objectID, ReleaseReason releaseReason, ProgressMonitor monitor)
	{
		try {
			EditLockManager wm = EditLockManagerUtil.getHome(JFireLoginProvider.sharedInstance().getInitialContextProperties()).create();
			wm.releaseEditLock(objectID, releaseReason);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
}
