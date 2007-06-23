package org.nightlabs.jfire.base;

public class DeadlockWorkaroundSharedMutex
{
	private static final boolean enableWorkaround = false;

	private static Object sharedMutex = null;

	public static Object getMutex()
	{
		if (enableWorkaround) {
			if (sharedMutex == null) {
				synchronized (DeadlockWorkaroundSharedMutex.class) {
					if (sharedMutex == null)
						sharedMutex = new Object();
				}
			}
			return sharedMutex;
		}
		else
			return new Object(); // disabled (every synchronized block gets a separate mutex = non-blocking!
	}

	private DeadlockWorkaroundSharedMutex() { }

}
