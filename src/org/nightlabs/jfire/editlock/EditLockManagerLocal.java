package org.nightlabs.jfire.editlock;

import java.util.Collection;

import javax.ejb.Local;

import org.nightlabs.jfire.editlock.id.EditLockID;
import org.nightlabs.jfire.timer.id.TaskID;

@Local
public interface EditLockManagerLocal
{
	void cleanupEditLocks(TaskID taskID) throws Exception;

	Collection<? extends EditLockID> cleanupEditLocks_getExpiredEditLocks();

	void cleanupEditLocks_releaseEditLock(EditLockID editLockID)
			throws Exception;
}