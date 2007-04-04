package org.nightlabs.jfire.worklock;

import java.io.Serializable;

public class AcquireWorklockResult
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private Worklock worklock;
	private long worklockCount;

	public AcquireWorklockResult(Worklock worklock, long worklockCount)
	{
		this.worklock = worklock;
		this.worklockCount = worklockCount;
	}

	public Worklock getWorklock()
	{
		return worklock;
	}

	/**
	 * This method returns the total number of {@link Worklock}s existing for the request-object-id. If this is not
	 * 1, it means that other users have a lock on the same object. This should result in a warning shown to the
	 * end-user.
	 *
	 * @return the total number of {@link Worklock} instances existing for the object-id passed to the acquireWorklock methods.
	 */
	public long getWorklockCount()
	{
		return worklockCount;
	}

}
