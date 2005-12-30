/*
 * Created on Jul 27, 2005
 */
package org.nightlabs.jfire.jdo.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class CacheSession
implements Serializable
{
	public static final Logger LOGGER = Logger.getLogger(CacheSession.class);

	private CacheManagerFactory cacheManagerFactory;
	private String cacheSessionID;

	public CacheSession(CacheManagerFactory cacheManagerFactory, String cacheSessionID)
	{
		if (cacheManagerFactory == null)
			throw new NullPointerException("cacheManagerFactory");

		if (cacheSessionID == null)
			throw new NullPointerException("cacheSessionID");

		this.cacheManagerFactory = cacheManagerFactory;
		this.cacheSessionID = cacheSessionID;
	}

	private volatile boolean closed = false;

	protected void assertOpen()
	{
		if (closed)
			throw new IllegalStateException("This instance of CacheSession (id=\""+cacheSessionID+"\") has already been closed!");
	}

	/**
	 * This method is called by {@link CacheManagerFactory#closeCacheSession(String)}.
	 * It will trigger {@link #notifyChanges()} in order to release any waiting
	 * client and additionally it will call {@link #setCacheSessionContainer(CacheSessionContainer)}
	 * with argument <tt>null</tt>.
	 * <p>
	 * After a <tt>CacheSession</tt> has been closed, it cannot be changed anymore. But
	 * if the <tt>CacheManagerFactory</tt> is used with the same <tt>cacheSessionID</tt>,
	 * it will create a new instance of <tt>CacheSession</tt> with the same ID.
	 */
	public void close()
	{
		closed = true;

		notifyChanges();
		setCacheSessionContainer(null);
	}

	/**
	 * @return Returns the cacheSessionID.
	 */
	public String getCacheSessionID()
	{
		return cacheSessionID;
	}

//	/**
//	 * key: Object objectID<br/>
//	 * value: ChangeListenerDescriptor changeListener
//	 */
//	private Map changeListeners = new HashMap();
//	private Set _subscribedObjectIDs = null;
//	private Collection _changeListeners = null;
//
//	/**
//	 * This method adds a <tt>ChangeListenerDescriptor</tt> to the backing <tt>Set</tt>. Note,
//	 * that a second call for the same listener (or another one with the same objectID)
//	 * does not add-up, because it can exist only once in the backing <tt>Set</tt>.
//	 *
//	 * @param objectID The JDO object ID to be subscribed.
//	 */
//	public void addChangeListener(ChangeListenerDescriptor listener)
//	{
//		assertOpen();
//
//		if (!cacheSessionID.equals(listener.getCacheSessionID()))
//			throw new IllegalArgumentException("this.cacheSessionID (\""+cacheSessionID+"\") != listener.cacheSessionID (\""+listener.getCacheSessionID()+"\")");
//
//		synchronized (changeListeners) {
//			changeListeners.put(listener.getObjectID(), listener);
//			_subscribedObjectIDs = null;
//			_changeListeners = null;
//		}
//	}
//
//	/**
//	 * This method removes a <tt>ChangeListenerDescriptor</tt> from the backing <tt>Set</tt>.
//	 *
//	 * @param listener The listener to be removed. Note that a listener can only exist
//	 *		once in the used <tt>Set</tt> and therefore one call to this method removes
//	 *		it - no matter how often {@link #addChangeListener(ChangeListenerDescriptor)} was called!
//	 */
//	public void removeChangeListener(ChangeListenerDescriptor listener)
//	{
//		assertOpen();
//
//		if (!cacheSessionID.equals(listener.getCacheSessionID()))
//			throw new IllegalArgumentException("this.cacheSessionID (\""+cacheSessionID+"\") != listener.cacheSessionID (\""+listener.getCacheSessionID()+"\")");
//
//		synchronized (changeListeners) {
//			changeListeners.remove(listener.getObjectID());
//			_subscribedObjectIDs = null;
//			_changeListeners = null;
//		}
//	}
//
//	public Collection getChangeListeners()
//	{
//		Collection res = _changeListeners;
//		if (res == null) {
//			synchronized (changeListeners) {
//				_changeListeners = Collections.unmodifiableCollection(
//						new LinkedList(changeListeners.values()));
//
//				res = _changeListeners;
//			}
//		}
//
//		return res;
//	}

	private Set subscribedObjectIDs = new HashSet();
	private Set _subscribedObjectIDs = null;


	/**
	* This method adds the given <tt>objectID</tt> to the backing <tt>Set</tt>. Note,
	* that a second call for the same JDO object ID
	* does not add-up, because it can exist only once in the backing <tt>Set</tt>.
	*
	* @param objectID The JDO object ID to be subscribed.
	*/
	public void subscribeObjectID(Object objectID)
	{
		assertOpen();

		synchronized (subscribedObjectIDs) {
			subscribedObjectIDs.add(objectID);
			_subscribedObjectIDs = null;
		}
	}
	
	/**
	* This method removes the given <tt>objectID</tt> from the backing <tt>Set</tt>.
	*
	* @param objectID The JDO object ID to be removed.
	*/
	public void unsubscribeObjectID(Object objectID)
	{
		assertOpen();

		synchronized (subscribedObjectIDs) {
			subscribedObjectIDs.remove(objectID);
			_subscribedObjectIDs = null;
		}
	}


	/**
	 * @return Returns the subscribed object IDs.
	 */
	public Set getSubscribedObjectIDs()
	{
		Set res = _subscribedObjectIDs;
		if (res == null) {
			synchronized (subscribedObjectIDs) {
				_subscribedObjectIDs = Collections.unmodifiableSet(
						new HashSet(subscribedObjectIDs));

				res = _subscribedObjectIDs;
			}
		}

		return res;
	}

	/**
	 * key: Object objectID<br/>
	 * value: {@link DirtyObjectID} dirtyObjectID
	 */
	private Map dirtyObjectIDs = new HashMap();
	private transient Object dirtyObjectIDsMutex = new Object();

	/**
	 * This method replaces the <tt>Set dirtyObjectIDs</tt> by a new (empty) one
	 * and returns the old <tt>Set</tt>, if it contains at least one entry. If
	 * it is empty, this method does nothing and returns <tt>null</tt>.
	 *
	 * @return Returns either <tt>null</tt> or a <tt>Set</tt> of jdo object-IDs.
	 */
	public Map fetchDirtyObjectIDs()
	{
		if (closed) {
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("fetchChangedObjectIDs() in CacheSession(cacheSessionID=\""+cacheSessionID+"\") will return null, because the session is closed!");

			return null;
		}

		Map res;
		synchronized (dirtyObjectIDsMutex) {
			if (dirtyObjectIDs.isEmpty())
				res = null;
			else {
				res = dirtyObjectIDs;
				dirtyObjectIDs = new HashMap();
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("fetchChangedObjectIDs() in CacheSession(cacheSessionID=\""+cacheSessionID+"\") will return " +
					(res == null ? "null" : ("a Set with " + res.size() + " entries")) + ".");
		}

		return res;
	}

	/**
	 * Adds all the given {@link DirtyObjectID}s to the backing <tt>Map</tt> in order
	 * to notify the listening client. All objectIDs for which no listener is registered
	 * will be ignored (and NOT added to the <tt>Map</tt>).
	 * <p>
	 * Note, that this method will NOT yet notify anyone! Notification is done indirectly.
	 */
	public void addDirtyObjectIDs(Collection dirtyOjectIDs)
	{
		assertOpen();

		synchronized (dirtyObjectIDsMutex) {
			Set subscribedObjectIDs = getSubscribedObjectIDs();

			for (Iterator it = dirtyOjectIDs.iterator(); it.hasNext(); ) {
				DirtyObjectID dirtyObjectID = (DirtyObjectID) it.next();
				Object objectID = dirtyObjectID.getObjectID();
				if (subscribedObjectIDs.contains(objectID)) {
					DirtyObjectID dObj = (DirtyObjectID) dirtyObjectIDs.get(objectID);
					if (dObj != null)
						dObj.addSourceSessionIDs(dirtyObjectID.getSourceSessionIDs());
					else
						dirtyObjectIDs.put(objectID, dirtyObjectID);
				}
			}
		}
	}

	/**
	 * @see #waitForChanges(long)
	 */
	public void notifyChanges()
	{
		synchronized (dirtyObjectIDsMutex) {
			dirtyObjectIDsMutex.notifyAll();
		}
	}

	/**
	 * If <tt>dirtyObjectIDs</tt> is empty,
	 * this method will block until either {@link #notifyChanges()} is called
	 * or the {@link #waitTimeout} occured. If <tt>dirtyObjectIDs</tt> contains
	 * at least one entry, this method immediately returns.
	 * <p>
	 * If the waitTimeout is changed
	 * after this method already started waiting, it has no effect.
	 *
	 * @see #notifyChanges()
	 */
	public void waitForChanges(long waitTimeout)
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("CacheSession \"" + cacheSessionID + "\" entered waitForChanges with waitTimeout=" + waitTimeout + ".");

		CacheCfMod cacheCfMod = cacheManagerFactory.getCacheCfMod();
		long waitMin = cacheCfMod.getWaitForChangesTimeoutMin();
		long waitMax = cacheCfMod.getWaitForChangesTimeoutMax();

		if (waitTimeout < waitMin) {
			LOGGER.warn("waitTimeout (" + waitTimeout + " msec) < waitForChangesTimeoutMin (" + waitMin + " msec)! Will ignore and use waitForChangesTimeoutMin!");
			waitTimeout = waitMin;
		}

		if (waitTimeout > waitMax) {
			LOGGER.warn("waitTimeout (" + waitTimeout + " msec) > waitForChangesTimeoutMax (" + waitMax + " msec)! Will ignore and use waitForChangesTimeoutMax!");
			waitTimeout = waitMax;
		}

		synchronized (dirtyObjectIDsMutex) {

			if (!dirtyObjectIDs.isEmpty()) {
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("CacheSession \"" + cacheSessionID + "\" has already changed objectIDs. Return immediately.");

				return;
			}

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("CacheSession \"" + cacheSessionID + "\" will wait " + waitTimeout + " msec for changed objects.");

			try {
				dirtyObjectIDsMutex.wait(waitTimeout);
			} catch (InterruptedException e) {
				// ignore
			}

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("CacheSession \"" + cacheSessionID + "\" woke up.");
		}
	}


	private CacheSessionContainer cacheSessionContainer = null;
	private transient Object cacheSessionContainerMutex = new Object();

	public CacheSessionContainer getCacheSessionContainer()
	{
		return cacheSessionContainer;
	}

	/**
	 * This method puts this <tt>CacheSession</tt> into the given
	 * <tt>CacheSessionContainer</tt>. If this <tt>CacheSession</tt> has been
	 * in another container before, it will be removed, because it can only
	 * be within one (or none) container.
	 *
	 * @param _cacheSessionContainer Either <tt>null</tt> or a <tt>CacheSessionContainer</tt>.
	 */
	public void setCacheSessionContainer(CacheSessionContainer _cacheSessionContainer)
	{
		synchronized (cacheSessionContainerMutex) {
			if (_cacheSessionContainer == cacheSessionContainer)
				return;

			if (cacheSessionContainer != null) {
				cacheSessionContainer.removeCacheSession(this);
				cacheSessionContainer = null;
			}

			cacheSessionContainer = _cacheSessionContainer;
			if (cacheSessionContainer != null)
				cacheSessionContainer.addCacheSession(this);
		}
	}
}
