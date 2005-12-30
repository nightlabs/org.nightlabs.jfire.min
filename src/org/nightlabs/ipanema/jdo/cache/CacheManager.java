/*
 * Created on Jul 27, 2005
 */
package org.nightlabs.ipanema.jdo.cache;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class CacheManager
{
	private CacheManagerFactory cacheManagerFactory;
	private String cacheSessionID;

	/**
	 * You can only use some of the methods after having created an instance with
	 * this constructor before having called {@link #initCacheSessionID(String)}.
	 *
	 * @param cacheManagerFactory
	 */
	protected CacheManager(CacheManagerFactory cacheManagerFactory)
	{
		this(cacheManagerFactory, (String)null);
	}

	protected CacheManager(CacheManagerFactory cacheManagerFactory, String cacheSessionID)
	{
		this.cacheManagerFactory = cacheManagerFactory;
		this.cacheSessionID = cacheSessionID;
	}

	public void initCacheSessionID(String cacheSessionID)
	{
		if (this.cacheSessionID != null)
			throw new IllegalStateException("CacheSessionID is already initialized!");

		this.cacheSessionID = cacheSessionID;
	}

	protected void assertCacheSessionIDExisting()
	{
		if (cacheSessionID == null)
			throw new IllegalStateException("The cacheSessionID is unknown! Either create the CacheManager with a cacheSessionID or use the method initCacheSessionID(...)!");
	}

	/**
	 * Use this method in order to subscribe your current <tt>CacheSession</tt>
	 * on changes of the JDO object specified by <tt>objectID</tt>.
	 * <p>
	 * This method implicitely opens a <tt>CacheSession</tt>.
	 *
	 * @param objectID A JDO object ID referencing a JDO object in which
	 *		you are interested
	 *		(see {@link javax.jdo.JDOHelper#getObjectId(java.lang.Object)}).
	 */
	public void addChangeListener(Object objectID)
	{
		assertCacheSessionIDExisting();
		cacheManagerFactory.addChangeListener(new ChangeListenerDescriptor(
				cacheSessionID, objectID));
	}

	/**
	 * @param objectIDs Instances of JDO object IDs.
	 *
	 * @see #addChangeListener(Object)
	 */
	public void addChangeListeners(Collection objectIDs)
	{
		assertCacheSessionIDExisting();
		for (Iterator it = objectIDs.iterator(); it.hasNext(); )
			cacheManagerFactory.addChangeListener(new ChangeListenerDescriptor(
					cacheSessionID, it.next()));
	}

	public void resubscribeAllChangeListeners(Set subscribedObjectIDs)
	{
		assertCacheSessionIDExisting();
		cacheManagerFactory.resubscribeAllChangeListeners(
				cacheSessionID, subscribedObjectIDs);
	}

	/**
	 * Removes a listener that has been previously added by
	 * {@link #addChangeListener(Object)}. Note, that one single call of this method
	 * always removes the listener, even if <tt>addChangeListener(...)</tt>
	 * has been called multiple times.
	 *
	 * @param objectID A JDO object ID referencing a JDO object
	 *		(see {@link javax.jdo.JDOHelper#getObjectId(java.lang.Object)}).
	 */
	public void removeChangeListener(Object objectID)
	{
		assertCacheSessionIDExisting();
		cacheManagerFactory.removeChangeListener(cacheSessionID, objectID);
	}

	/**
	 * @param objectIDs Instances of JDO object IDs.
	 *
	 * @see #removeChangeListener(Object)
	 */
	public void removeChangeListeners(Collection objectIDs)
	{
		assertCacheSessionIDExisting();
		for (Iterator it = objectIDs.iterator(); it.hasNext(); )
			cacheManagerFactory.removeChangeListener(cacheSessionID, it.next());
	}

	/**
	 * This method closes the current <tt>CacheSession</tt> and removes all
	 * listeners of this session. A subsequent call to {@link #addChangeListener(Object)}
	 * or {@link #waitForChanges()} automatically reopens a new empty session
	 * (for the same <tt>cacheSessionID</tt>).
	 * <p>
	 * If a <tt>CacheSession</tt> has not been used for a configurable time (default
	 * is 30 minutes) - e.g. because the client is not online anymore -,
	 * it will be closed automatically. Each call to {@link #waitForChanges()}
	 * or {@link #addChangeListener(Object)} will keep the session alive.
	 */
	public void closeCacheSession()
	{
		assertCacheSessionIDExisting();
		cacheManagerFactory.closeCacheSession(cacheSessionID);
	}

	/**
	 * This method causes <b>all</b> <tt>CacheSession</tt>s to be notified which
	 * have a listener on one or more of the given <tt>objectIDs</tt>.
	 * The notification works asynchronously, hence this method quickly returns
	 * and if it is called multiple times within a short time, the
	 * objectIDs will be collected and sent together to the client.
	 * @param objectIDs The IDs of the JDO objects that have been changed
	 *		(see {@link javax.jdo.JDOHelper#getObjectId(java.lang.Object)}).
	 */
	public void addDirtyObjectIDs(String sessionID, Collection objectIDs)
	{
		cacheManagerFactory.addDirtyObjectIDs(sessionID, objectIDs);
	}

	/**
	 * This method blocks until either the timeout occurs or
	 * changes for the current <tt>CacheSession</tt> have
	 * occured.
	 *
	 * @param waitTimeout The timeout in milliseconds after which this method
	 *		will return <tt>null</tt> if no changes occur before.
	 *
	 * @return Returns either <tt>null</tt> or a <tt>Collection</tt> of JDO object IDs
	 *		(see {@link javax.jdo.JDOHelper#getObjectId(java.lang.Object)}).
	 */
	public Collection waitForChanges(long waitTimeout)
	{
		assertCacheSessionIDExisting();
		return cacheManagerFactory.waitForChanges(cacheSessionID, waitTimeout);
	}
}
