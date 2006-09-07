package org.nightlabs.jfire.jdo.cache;

import java.io.Serializable;
import java.util.Map;
import java.util.SortedSet;

public class NotificationBundle
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private boolean virginCacheSession = false;
	private SortedSet<DirtyObjectID> dirtyObjectIDs = null;
	private Map<Long, SortedSet<DirtyObjectID>> filterID2dirtyObjectIDs = null;

	/**
	 * @return <code>true</code>, if this is the first NotificationBundle that has been emitted from a
	 *		{@link CacheSession}. <code>false</code> on all subsequent calls of
	 *		{@link CacheManagerFactory#waitForChanges(String, String, long)} for the same cacheSession.
	 */
	public boolean isVirginCacheSession()
	{
		return virginCacheSession;
	}
	public void setVirginCacheSession(boolean virginCacheSession)
	{
		this.virginCacheSession = virginCacheSession;
	}

	/**
	 * @return Returns either <code>null</code> or instances of {@link DirtyObjectID} containing detailed information for
	 *		the implicite notification
	 */
	public SortedSet<DirtyObjectID> getDirtyObjectIDs()
	{
		return dirtyObjectIDs;
	}
	public void setDirtyObjectIDs(SortedSet<DirtyObjectID> dirtyObjectIDs)
	{
		this.dirtyObjectIDs = dirtyObjectIDs;
	}

	/**
	 * @return Returns either <code>null</code> or the {@link DirtyObjectID}s grouped by filterIDs for explicit listeners.
	 */
	public Map<Long, SortedSet<DirtyObjectID>> getFilterID2dirtyObjectIDs()
	{
		return filterID2dirtyObjectIDs;
	}
	public void setFilterID2dirtyObjectIDs(
			Map<Long, SortedSet<DirtyObjectID>> filterID2dirtyObjectIDs)
	{
		this.filterID2dirtyObjectIDs = filterID2dirtyObjectIDs;
	}

	public boolean isEmpty()
	{
		return
				!virginCacheSession &&
				(dirtyObjectIDs == null || dirtyObjectIDs.isEmpty()) &&
				(filterID2dirtyObjectIDs == null || filterID2dirtyObjectIDs.isEmpty());
	}
}
