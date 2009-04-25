package org.nightlabs.jfire.editlock;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.editlock.id.EditLockID;
import org.nightlabs.jfire.editlock.id.EditLockTypeID;
import org.nightlabs.timepattern.TimePatternFormatException;

@Remote
public interface EditLockManagerRemote {

	void initialise() throws TimePatternFormatException;

	/**
	 * This method first searches for an existing {@link EditLock} on the JDO object
	 * referenced by the given <code>objectID</code> and owned by the current user.
	 * If none such <code>EditLock</code> exists, a new one will be created. If a previously
	 * existing one could be found, its {@link EditLock#setLastAcquireDT()} method will be called
	 * in order to renew it.
	 * <p>
	 * </p>
	 * @param editLockTypeID If a new <code>EditLock</code> is created, it will be assigned the {@link EditLockType}
	 *		referenced by this id. If the EditLock previously existed, this parameter is ignored.
	 * @param objectID The id of the JDO object that shall be locked.
	 * @param description The editLock's description which will be shown to the user and should make clear what is locked (e.g. the name of the object referenced by <code>objectID</code>).
	 * @param fetchGroups The fetch-groups used for detaching the created/queried {@link EditLock}.
	 * @param maxFetchDepth The maximum fetch-depth for detaching the created/queried {@link EditLock}.
	 */
	AcquireEditLockResult acquireEditLock(EditLockTypeID editLockTypeID,
			ObjectID objectID, String description, String[] fetchGroups,
			int maxFetchDepth);

	void releaseEditLock(ObjectID objectID, ReleaseReason releaseReason);

	Set<EditLockID> getEditLockIDs(ObjectID objectID);

	List<EditLock> getEditLocks(Collection<EditLockID> editLockIDs,
			String[] fetchGroups, int maxFetchDepth);

	String ping(String message);

}