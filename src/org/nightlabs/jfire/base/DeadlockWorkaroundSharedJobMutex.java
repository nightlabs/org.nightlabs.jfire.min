package org.nightlabs.jfire.base;

public class DeadlockWorkaroundSharedJobMutex
{
	private static DeadlockWorkaroundSharedJobMutex sharedInstance = null;

	public static DeadlockWorkaroundSharedJobMutex sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (DeadlockWorkaroundSharedJobMutex.class) {
				if (sharedInstance == null)
					sharedInstance = new DeadlockWorkaroundSharedJobMutex();
			}
		}
		return sharedInstance;
	}

	private DeadlockWorkaroundSharedJobMutex() { }

}
