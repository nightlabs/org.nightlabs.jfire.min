/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

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
	private static final long serialVersionUID = 1L;

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(CacheSession.class);

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

	private Set<Object> subscribedObjectIDs = new HashSet<Object>();
	private Set<Object> _subscribedObjectIDs = null;


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
	 * @return Returns the subscribed object IDs in form of an unmodifiable copy of the internal set.
	 */
	public Set getSubscribedObjectIDs()
	{
		Set res = _subscribedObjectIDs;
		if (res == null) {
			synchronized (subscribedObjectIDs) {
				_subscribedObjectIDs = Collections.unmodifiableSet(
						new HashSet<Object>(subscribedObjectIDs));

				res = _subscribedObjectIDs;
			}
		}

		return res;
	}

	/**
	 * key: Object objectID<br/>
	 * value: {@link DirtyObjectID} dirtyObjectID
	 */
	private Map<Object, DirtyObjectID> dirtyObjectIDs = new HashMap<Object, DirtyObjectID>();
	private transient Object dirtyObjectIDsMutex = new Object();

	/**
	 * This method replaces the <tt>Set dirtyObjectIDs</tt> by a new (empty) one
	 * and returns the old <tt>Set</tt>, if it contains at least one entry. If
	 * it is empty, this method does nothing and returns <tt>null</tt>.
	 * <p>
	 * If there are {@link DirtyObjectID}s that have their <code>changeDT</code> still younger than
	 * {@link System#currentTimeMillis()} <code>minus</code> {@link CacheCfMod#getDelayNotificationMSec()},
	 * they will be filtered and added to the new Map.
	 * </p>
	 *
	 * @return Returns either <tt>null</tt> or a <tt>Map</tt> of jdo object-IDs. This Map will never be empty (instead of an
	 *		empty Map, null is returned). 
	 */
	public Map<Object, DirtyObjectID> fetchDirtyObjectIDs()
	{
		if (closed) {
			if (logger.isDebugEnabled())
				logger.debug("fetchChangedObjectIDs() in CacheSession(cacheSessionID=\""+cacheSessionID+"\") will return null, because the session is closed!");

			return null;
		}

		Map<Object, DirtyObjectID> res;
		synchronized (dirtyObjectIDsMutex) {
			if (dirtyObjectIDs.isEmpty())
				res = null;
			else {
				res = dirtyObjectIDs;
				dirtyObjectIDs = new HashMap<Object, DirtyObjectID>();

				long youngestChangeDT = System.currentTimeMillis() - cacheManagerFactory.getCacheCfMod().getDelayNotificationMSec();

				for (Iterator it = res.entrySet().iterator(); it.hasNext(); ) {
					Map.Entry me = (Map.Entry) it.next();
					DirtyObjectID dirtyObjectID = (DirtyObjectID) me.getValue();
					if (dirtyObjectID.getChangeDT() > youngestChangeDT) {
						// it's too new => remove it from the result and delay it by putting it back to (the new) this.dirtyObjectIDs map.
						dirtyObjectIDs.put(me.getKey(), dirtyObjectID);
						it.remove();
					}
				}

				if (res.isEmpty())
					res = null;
			}
		} // synchronized (dirtyObjectIDsMutex) {

		if (logger.isDebugEnabled()) {
			logger.debug("fetchChangedObjectIDs() in CacheSession(cacheSessionID=\""+cacheSessionID+"\") will return " +
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
	public void addDirtyObjectIDs(Collection<DirtyObjectID> dirtyOjectIDs)
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
	 * If <tt>dirtyObjectIDs</tt> is empty or contains only entries that are too young for immediate
	 * notification, this method will start blocking.
	 * <p>
	 * It will block, until either {@link #notifyChanges()} is called <b>and</b> at
	 * least one dirtyObjectID is old enough for notification or the {@link #waitTimeout}
	 * occured.
	 * </p>
	 * <p>
	 * If <tt>dirtyObjectIDs</tt> contains at least one entry that is old enough (the delay
	 * {@link CacheCfMod#getDelayNotificationMSec()} already expired),
	 * this method immediately returns.
	 * </p>
	 * <p>
	 * If the waitTimeout is changed
	 * after this method already started waiting, it has very likely no effect (there are rare situations
	 * in which it might have an effect though).
	 * </p>
	 *
	 * @see #notifyChanges()
	 */
	public void waitForChanges(long waitTimeout)
	{
		if (logger.isDebugEnabled())
			logger.debug("CacheSession \"" + cacheSessionID + "\" entered waitForChanges with waitTimeout=" + waitTimeout + ".");

		long methodBeginDT = System.currentTimeMillis();

		CacheCfMod cacheCfMod = cacheManagerFactory.getCacheCfMod();
		long waitMin = cacheCfMod.getWaitForChangesTimeoutMin();
		long waitMax = cacheCfMod.getWaitForChangesTimeoutMax();

		if (waitTimeout < waitMin) {
			logger.warn("waitTimeout (" + waitTimeout + " msec) < waitForChangesTimeoutMin (" + waitMin + " msec)! Will ignore and use waitForChangesTimeoutMin!");
			waitTimeout = waitMin;
		}

		if (waitTimeout > waitMax) {
			logger.warn("waitTimeout (" + waitTimeout + " msec) > waitForChangesTimeoutMax (" + waitMax + " msec)! Will ignore and use waitForChangesTimeoutMax!");
			waitTimeout = waitMax;
		}

		do {
			long actualWaitMSec = waitTimeout;
			synchronized (dirtyObjectIDsMutex) {
	
				if (!dirtyObjectIDs.isEmpty()) {
					long delayMSec = cacheManagerFactory.getCacheCfMod().getDelayNotificationMSec();

					if (logger.isDebugEnabled())
						logger.debug("CacheSession \"" + cacheSessionID + "\" has changed objectIDs. Checking their age (taking delayNotificationMSec="+delayMSec+" into account).");

					long youngestChangeDT = System.currentTimeMillis() - delayMSec;
					for (Iterator it = dirtyObjectIDs.entrySet().iterator(); it.hasNext(); ) {
						Map.Entry me = (Map.Entry) it.next();
						DirtyObjectID dirtyObjectID = (DirtyObjectID) me.getValue();
						if (dirtyObjectID.getChangeDT() < youngestChangeDT) {
							// it's old enough => we have at least one that needs immediate notification
							if (logger.isDebugEnabled())
								logger.debug("CacheSession \"" + cacheSessionID + "\" has changed objectIDs and at least one of them is old enough for notification. Return immediately.");

							return;
						}
						else {
							long tmp = System.currentTimeMillis() - dirtyObjectID.getChangeDT(); // how old is it
							tmp = delayMSec - tmp; // the rest time that is needed to reach the minimum age 
							if (tmp < actualWaitMSec) {
								if (logger.isDebugEnabled())
									logger.debug("CacheSession \"" + cacheSessionID + "\" has a changed objectID, but it is still too young for immediate notification. Will wait, but shorten the waitTimeout from " + actualWaitMSec + " to " + tmp + " msec.");
	
								actualWaitMSec = tmp;
							}
						}
					}
				}

				if (logger.isDebugEnabled())
						logger.debug("CacheSession \"" + cacheSessionID + "\" will wait " + actualWaitMSec + " msec for changed objects.");

				try {
					if (actualWaitMSec > 0)
						dirtyObjectIDsMutex.wait(actualWaitMSec);
				} catch (InterruptedException e) {
					// ignore
				}

				if (logger.isDebugEnabled())
					logger.debug("CacheSession \"" + cacheSessionID + "\" woke up.");
			} // synchronized (dirtyObjectIDsMutex) {
		} while (System.currentTimeMillis() - methodBeginDT < waitTimeout);
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
