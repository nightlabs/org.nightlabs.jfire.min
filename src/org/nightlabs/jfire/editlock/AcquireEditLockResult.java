package org.nightlabs.jfire.editlock;

import java.io.Serializable;

import org.nightlabs.jfire.editlock.EditLock;

public class AcquireEditLockResult
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private EditLock editLock;
	private long editLockCount;

	public AcquireEditLockResult(EditLock editLock, long editLockCount)
	{
		this.editLock = editLock;
		this.editLockCount = editLockCount;
	}

	public EditLock getEditLock()
	{
		return editLock;
	}

	/**
	 * This method returns the total number of {@link EditLock}s existing for the request-object-id. If this is not
	 * 1, it means that other users have a lock on the same object. This should result in a warning shown to the
	 * end-user.
	 *
	 * @return the total number of {@link EditLock} instances existing for the object-id passed to the acquireEditLock methods.
	 */
	public long getEditLockCount()
	{
		return editLockCount;
	}

}
