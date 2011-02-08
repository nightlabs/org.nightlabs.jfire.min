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
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.jdo.notification.AbsoluteFilterID;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.FilterRegistry;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;

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
	private String sessionID;
	private String userID;

	private FilterRegistry filterRegistry;

//	public CacheSessionCoordinate getCoordinate() {
//		return new CacheSessionCoordinate(cacheManagerFactory.getOrganisationID(), userID, sessionID);
//	}
//
//	public CacheSession(CacheManagerFactory cacheManagerFactory, CacheSessionCoordinate coordinate)
//	{
//		this(cacheManagerFactory, coordinate.getSessionID(), coordinate.getUserID());
//
//		if (!cacheManagerFactory.getOrganisationID().equals(coordinate.getOrganisationID()))
//			throw new IllegalArgumentException("cacheManagerFactory.organisationID != coordinateSet.organisationID :: " + cacheManagerFactory.getOrganisationID() + " != " + coordinate.getOrganisationID());
//	}

	/**
	 * @param cacheManagerFactory The {@link CacheManagerFactory} which created this {@link CacheSession}.
	 * @param sessionID The sessionID as specified by the client. He cannot "hijack" sessions anyway, because the userID is checked.
	 * @param userID The userID of the user - the organisationID is clear.
	 */
	public CacheSession(CacheManagerFactory cacheManagerFactory, String sessionID, String userID)
	{
		if (cacheManagerFactory == null)
			throw new IllegalArgumentException("cacheManagerFactory is null");

		if (sessionID == null)
			throw new IllegalArgumentException("sessionID is null");

		if (userID == null)
			throw new IllegalArgumentException("userID is null");

		this.cacheManagerFactory = cacheManagerFactory;
		this.sessionID = sessionID;
		this.userID = userID;

		this.filterRegistry = cacheManagerFactory.getFilterRegistry();
	}

	private volatile boolean closed = false;

	protected void assertOpen()
	{
		if (closed)
			throw new IllegalStateException("This instance of CacheSession (id=\""+sessionID+"\") has already been closed!");
	}

	/**
	 * This method is called by {@link CacheManagerFactory#closeCacheSession(String)}.
	 * It will trigger {@link #notifyChanges()} in order to release any waiting
	 * client and additionally it will call {@link #setCacheSessionContainer(CacheSessionContainer)}
	 * with argument <tt>null</tt>.
	 * <p>
	 * After a <tt>CacheSession</tt> has been closed, it cannot be changed anymore. But
	 * if the <tt>CacheManagerFactory</tt> is used with the same <tt>sessionID</tt>,
	 * it will create a new instance of <tt>CacheSession</tt> with the same ID.
	 */
	public void close()
	{
		if (closed)
			return;

		// We first remove all filters from the FilterRegistry to make sure, no notifications
		// are routed here anymore.
		LinkedList<IJDOLifecycleListenerFilter> filters;
		synchronized (this.filters) {
			filters = new LinkedList<IJDOLifecycleListenerFilter>(this.filters.values());
			this.filters.clear();
		}

		for (IJDOLifecycleListenerFilter filter : filters)
			filterRegistry.removeFilter(filter);

		// Then we mark this instance as closed.
		// From now on, all operations on this object should either fail or become no-ops.
		closed = true;

		notifyChanges();

		setCacheSessionContainer(null);
	}

	/**
	 * @return Returns the sessionID.
	 */
	public String getSessionID()
	{
		return sessionID;
	}
	public String getUserID()
	{
		return userID;
	}

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
	public Set<Object> getSubscribedObjectIDs()
	{
		assertOpen();

		Set<Object> res = _subscribedObjectIDs;
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
	private Map<Object, Map<JDOLifecycleState, DirtyObjectID>> dirtyObjectIDs = new HashMap<Object, Map<JDOLifecycleState, DirtyObjectID>>();
	private transient Object dirtyObjectIDsMutex = new Object();

	/**
	 * This field is synchronized via {@link #dirtyObjectIDsMutex}, too!
	 */
	private Map<AbsoluteFilterID, Map<Object, Map<JDOLifecycleState, DirtyObjectID>>> filterID2DirtyObjectIDs = new HashMap<AbsoluteFilterID, Map<Object,Map<JDOLifecycleState, DirtyObjectID>>>();

	private Map<AbsoluteFilterID, IJDOLifecycleListenerFilter> filters = new HashMap<AbsoluteFilterID, IJDOLifecycleListenerFilter>();
	private Map<AbsoluteFilterID, IJDOLifecycleListenerFilter> _filters = null;

	public void addFilter(IJDOLifecycleListenerFilter filter)
	{
		assertOpen();

		synchronized (filters) {
			filters.put(filter.getFilterID(), filter);
			_filters = null;
		}

		cacheManagerFactory.getFilterRegistry().addFilter(filter);
	}

	public void removeFilter(IJDOLifecycleListenerFilter filter)
	{
		removeFilter(filter.getFilterID());
	}

	public void removeFilter(AbsoluteFilterID filterID)
	{
		assertOpen();

		// We remove FIRST from the index [i.e. our registry] (in order not to get no notifications anymore)
		// and then here.
		cacheManagerFactory.getFilterRegistry().removeFilter(filterID);

		synchronized (filters) {
			filters.remove(filterID);
			_filters = null;
		}
	}

	public Map<AbsoluteFilterID, IJDOLifecycleListenerFilter> getFilters()
	{
		assertOpen();

		Map<AbsoluteFilterID, IJDOLifecycleListenerFilter> res = _filters;

		if (res == null) {
			synchronized (filters) {
				_filters = Collections.unmodifiableMap(
						new HashMap<AbsoluteFilterID, IJDOLifecycleListenerFilter>(filters));

				res = _filters;
			}
		}

		return res;
	}

	private boolean virginCacheSession = true;
	public boolean isVirginCacheSession()
	{
		return virginCacheSession;
	}
	public void setVirginCacheSession(boolean virginCacheSession)
	{
		this.virginCacheSession = virginCacheSession;
	}

	public static class DirtyObjectIDGroup
	{
		public Map<Object, Map<JDOLifecycleState, DirtyObjectID>> dirtyObjectIDs;
		public Map<AbsoluteFilterID, Map<Object, Map<JDOLifecycleState, DirtyObjectID>>> filterID2DirtyObjectIDs;
	}

	/**
	 * This method replaces the <tt>Set dirtyObjectIDsRaw</tt> by a new (empty) one
	 * and returns the old <tt>Set</tt>, if it contains at least one entry. If
	 * it is empty, this method does nothing and returns <tt>null</tt>.
	 * <p>
	 * If there are {@link DirtyObjectID}s that have their <code>changeDT</code> still younger than
	 * {@link System#currentTimeMillis()} <code>minus</code> {@link CacheCfMod#getDelayNotificationMSec()},
	 * they will be filtered and added to the new Map.
	 * </p>
	 *
	 * @return Returns either <tt>null</tt> or an instance of {@link DirtyObjectIDGroup}.
	 */
	public DirtyObjectIDGroup fetchDirtyObjectIDs()
	{
		if (closed) {
			if (logger.isDebugEnabled())
				logger.debug("fetchChangedObjectIDs() in CacheSession(sessionID=\""+sessionID+"\") will return null, because the session is closed!");

			return null;
		}

		DirtyObjectIDGroup res = new DirtyObjectIDGroup();

		synchronized (dirtyObjectIDsMutex) {
			if (dirtyObjectIDs.isEmpty())
				res.dirtyObjectIDs = null;
			else {
				res.dirtyObjectIDs = dirtyObjectIDs;
				dirtyObjectIDs = new HashMap<Object, Map<JDOLifecycleState, DirtyObjectID>>();

//				long youngestChangeDT = System.currentTimeMillis() - cacheManagerFactory.getCacheCfMod().getDelayNotificationMSec();
//
//				for (Iterator it = res.entrySet().iterator(); it.hasNext(); ) {
//					Map.Entry me = (Map.Entry) it.next();
//					DirtyObjectID dirtyObjectID = (DirtyObjectID) me.getValue();
//					if (dirtyObjectID.getChangeDT() > youngestChangeDT) {
//						// it's too new => remove it from the result and delay it by putting it back to (the new) this.dirtyObjectIDs map.
//						dirtyObjectIDs.put(me.getKey(), dirtyObjectID);
//						it.remove();
//					}
//				}
			}

			if (filterID2DirtyObjectIDs.isEmpty())
				res.filterID2DirtyObjectIDs = null;
			else {
				res.filterID2DirtyObjectIDs = filterID2DirtyObjectIDs;
				filterID2DirtyObjectIDs = new HashMap<AbsoluteFilterID, Map<Object,Map<JDOLifecycleState, DirtyObjectID>>>();
			}

			if (res.dirtyObjectIDs == null && res.filterID2DirtyObjectIDs == null)
				res = null;
		} // synchronized (dirtyObjectIDsMutex) {

//		if (logger.isDebugEnabled()) {
//			logger.debug("fetchChangedObjectIDs() in CacheSession(sessionID=\""+sessionID+"\") will return " +
//					(res == null ? "null" : ("a Set with " + res.size() + " entries")) + ".");
//		}

		return res;
	}

	/**
	 * In contrast to {@link #addDirtyObjectIDs(Collection)}, this method adds DirtyObjectIDs for
	 * the explicit listeners (identified via their filterID). This method adds ALL given
	 * parameters to the internal map!
	 *
	 * @param filterID2DirtyObjectIDs
	 */
	public void addDirtyObjectIDs(Map<AbsoluteFilterID, Collection<DirtyObjectID>> filterID2DirtyObjectIDs)
	{
		try {
			assertOpen();
		} catch (Exception e) {
			logger.warn("addDirtyObjectIDs: assertOpen() failed!", e);
			return;
		}

		synchronized (dirtyObjectIDsMutex) {
			for (Map.Entry<AbsoluteFilterID, Collection<DirtyObjectID>> me : filterID2DirtyObjectIDs.entrySet()) {
				AbsoluteFilterID filterID = me.getKey();
				Collection<DirtyObjectID> dirtyObjectIDs = me.getValue();

				Map<Object, Map<JDOLifecycleState, DirtyObjectID>> m1 = this.filterID2DirtyObjectIDs.get(filterID);
				if (m1 == null) {
					m1 = new HashMap<Object, Map<JDOLifecycleState, DirtyObjectID>>();
					this.filterID2DirtyObjectIDs.put(filterID, m1);
				}

				for (DirtyObjectID dirtyObjectID : dirtyObjectIDs) {
					Object objectID = dirtyObjectID.getObjectID();

					Map<JDOLifecycleState, DirtyObjectID> m2 = m1.get(objectID);
					if (m2 == null) {
						m2 = new HashMap<JDOLifecycleState, DirtyObjectID>();
						m1.put(objectID, m2);
					}

					DirtyObjectID dObj = m2.get(dirtyObjectID.getLifecycleState());
					if (dObj == null)
						m2.put(dirtyObjectID.getLifecycleState(), dirtyObjectID);
					else {
						DirtyObjectID older = dObj;
						DirtyObjectID newer = dirtyObjectID;
						if (older.getSerial() > newer.getSerial()) {
							DirtyObjectID tmp = older;
							older = newer;
							newer = tmp;
						}
						newer.addSourceSessionIDs(older.getSourceSessionIDs());
						m2.put(dirtyObjectID.getLifecycleState(), newer);
					}
				} // for (DirtyObjectID dirtyObjectID : dirtyObjectIDs) {
			} // for (Map.Entry<AbsoluteFilterID, Collection<DirtyObjectID>> me : filterID2DirtyObjectIDs.entrySet()) {
		} // synchronized (dirtyObjectIDsMutex) {
	}

	/**
	 * Adds all the given {@link DirtyObjectID}s to the backing <tt>Map</tt> in order
	 * to notify the listening client. All objectIDs for which no listener is registered
	 * will be ignored (and NOT added to the <tt>Map</tt>).
	 * <p>
	 * Note, that this method will NOT yet notify anyone! Notification is done indirectly.
	 */
	public void addDirtyObjectIDs(Collection<DirtyObjectID> dirtyObjectIDs)
	{
		assertOpen();

		synchronized (dirtyObjectIDsMutex) {
			Set<?> subscribedObjectIDs = getSubscribedObjectIDs();

			for (DirtyObjectID dirtyObjectID : dirtyObjectIDs) {
				Object objectID = dirtyObjectID.getObjectID();

				if (subscribedObjectIDs.contains(objectID)) { // check, whether this sessionID is interested in the given objectID
					Map<JDOLifecycleState, DirtyObjectID> m = this.dirtyObjectIDs.get(objectID);
					if (m == null) {
						m = new HashMap<JDOLifecycleState, DirtyObjectID>();
						this.dirtyObjectIDs.put(objectID, m);
					}
					DirtyObjectID dObj = m.get(dirtyObjectID.getLifecycleState());
					if (dObj == null)
						m.put(dirtyObjectID.getLifecycleState(), dirtyObjectID);
					else {
						DirtyObjectID older = dObj;
						DirtyObjectID newer = dirtyObjectID;
						if (older.getSerial() > newer.getSerial()) {
							DirtyObjectID tmp = older;
							older = newer;
							newer = tmp;
						}
						newer.addSourceSessionIDs(older.getSourceSessionIDs());
						m.put(dirtyObjectID.getLifecycleState(), newer);
					}
				} // if (subscribedObjectIDs.contains(objectID)) { // check, whether this sessionID is interested in the given objectID
			} // for (DirtyObjectID dirtyObjectID : dirtyObjectIDs) {
		} // synchronized (dirtyObjectIDsMutex) {
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
	 * If <tt>dirtyObjectIDsRaw</tt> is empty or contains only entries that are too young for immediate
	 * notification, this method will start blocking.
	 * <p>
	 * It will block, until either {@link #notifyChanges()} is called <b>and</b> at
	 * least one dirtyObjectID is old enough for notification or the {@link #waitTimeout}
	 * occured.
	 * </p>
	 * <p>
	 * If <tt>dirtyObjectIDsRaw</tt> contains at least one entry that is old enough (the delay
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
			logger.debug("CacheSession \"" + sessionID + "\" entered waitForChanges with waitTimeout=" + waitTimeout + ".");

		if (closed) {
			logger.error("This CacheSession is closed! sessionID="+sessionID, new Exception("CacheSession closed!"));
			return;
		}

//		long methodBeginDT = System.currentTimeMillis();

		CacheCfMod cacheCfMod = cacheManagerFactory.getCacheCfMod();
		long waitMin = cacheCfMod.getWaitForChangesTimeoutMin();
		long waitMax = cacheCfMod.getWaitForChangesTimeoutMax();

		if (waitTimeout < waitMin) {
			logger.warn("CacheSession \"" + sessionID + "\" ("+userID + '@' + cacheManagerFactory.getOrganisationID()+"): waitTimeout (" + waitTimeout + " msec) < waitForChangesTimeoutMin (" + waitMin + " msec)! Will ignore and use waitForChangesTimeoutMin!");
			waitTimeout = waitMin;
		}

		if (waitTimeout > waitMax) {
			logger.warn("CacheSession \"" + sessionID + "\" ("+userID + '@' + cacheManagerFactory.getOrganisationID()+"): waitTimeout (" + waitTimeout + " msec) > waitForChangesTimeoutMax (" + waitMax + " msec)! Will ignore and use waitForChangesTimeoutMax!");
			waitTimeout = waitMax;
		}

//		do {
			long actualWaitMSec = waitTimeout;
			synchronized (dirtyObjectIDsMutex) {

//				if (!dirtyObjectIDs.isEmpty()) {
//					long delayMSec = cacheManagerFactory.getCacheCfMod().getDelayNotificationMSec();
//
//					if (logger.isDebugEnabled())
//						logger.debug("CacheSession \"" + sessionID + "\" has changed objectIDs. Checking their age (taking delayNotificationMSec="+delayMSec+" into account).");
//
//					long youngestChangeDT = System.currentTimeMillis() - delayMSec;
//					for (Iterator it = dirtyObjectIDs.entrySet().iterator(); it.hasNext(); ) {
//						Map.Entry me = (Map.Entry) it.next();
//						DirtyObjectID dirtyObjectID = (DirtyObjectID) me.getValue();
//						if (dirtyObjectID.getChangeDT() < youngestChangeDT) {
//							// it's old enough => we have at least one that needs immediate notification
//							if (logger.isDebugEnabled())
//								logger.debug("CacheSession \"" + sessionID + "\" has changed objectIDs and at least one of them is old enough for notification. Return immediately.");
//
//							return;
//						}
//						else {
//							long tmp = System.currentTimeMillis() - dirtyObjectID.getChangeDT(); // how old is it
//							tmp = delayMSec - tmp; // the rest time that is needed to reach the minimum age
//							if (tmp < actualWaitMSec) {
//								if (logger.isDebugEnabled())
//									logger.debug("CacheSession \"" + sessionID + "\" has a changed objectID, but it is still too young for immediate notification. Will wait, but shorten the waitTimeout from " + actualWaitMSec + " to " + tmp + " msec.");
//
//								actualWaitMSec = tmp;
//							}
//						}
//					}
//				}

				if (logger.isDebugEnabled())
						logger.debug("CacheSession \"" + sessionID + "\" will wait " + actualWaitMSec + " msec for changed objects.");

				try {
					if (actualWaitMSec > 0)
						dirtyObjectIDsMutex.wait(actualWaitMSec);
				} catch (InterruptedException e) {
					// ignore
				}

				if (logger.isDebugEnabled())
					logger.debug("CacheSession \"" + sessionID + "\" woke up.");
			} // synchronized (dirtyObjectIDsMutex) {
//		} while (System.currentTimeMillis() - methodBeginDT < waitTimeout);
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

	public static class ResubscribeResult
	{
		public LinkedList<Object> objectIDsRemoved = new LinkedList<Object>();
		public LinkedList<Object> objectIDsAdded = new LinkedList<Object>();
		public LinkedList<IJDOLifecycleListenerFilter> filtersRemoved = new LinkedList<IJDOLifecycleListenerFilter>();
		public LinkedList<IJDOLifecycleListenerFilter> filtersAdded = new LinkedList<IJDOLifecycleListenerFilter>();
	}

	public ResubscribeResult resubscribeAllListeners(
			Set<Object> subscribedObjectIDs,
			Collection<IJDOLifecycleListenerFilter> filters)
	{
		ResubscribeResult res = new ResubscribeResult();

		synchronized (this.subscribedObjectIDs) {
			LinkedList<Object> objectIDsToRemove = res.objectIDsRemoved;
			LinkedList<Object> objectIDsToAdd = res.objectIDsAdded;

			Set<Object> oldSubscribedObjectIDs = this.subscribedObjectIDs;

			for (Iterator<Object> it = oldSubscribedObjectIDs.iterator(); it.hasNext();) {
				Object objectID = it.next();
				if (!subscribedObjectIDs.contains(objectID))
					objectIDsToRemove.add(objectID);
			}

			for (Iterator<Object> it = subscribedObjectIDs.iterator(); it.hasNext();) {
				Object objectID = it.next();
				if (!oldSubscribedObjectIDs.contains(objectID))
					objectIDsToAdd.add(objectID);
			}

			for (Object objectID : objectIDsToRemove)
				unsubscribeObjectID(objectID);

			for (Object objectID : objectIDsToAdd)
				subscribeObjectID(objectID);
		}

		synchronized (this.filters) {
			LinkedList<IJDOLifecycleListenerFilter> filtersToRemove = res.filtersRemoved;
			LinkedList<IJDOLifecycleListenerFilter> filtersToAdd = res.filtersAdded;

			Map<AbsoluteFilterID, IJDOLifecycleListenerFilter> oldFilters = this.filters;
			Map<AbsoluteFilterID, IJDOLifecycleListenerFilter> newFilters = new HashMap<AbsoluteFilterID, IJDOLifecycleListenerFilter>(filters.size());
			for (IJDOLifecycleListenerFilter filter : filters) {
				newFilters.put(filter.getFilterID(), filter);
			}

			for (Map.Entry<AbsoluteFilterID, IJDOLifecycleListenerFilter> me : oldFilters.entrySet()) {
				if (!newFilters.containsKey(me.getKey()))
					filtersToRemove.add(me.getValue());
			}

			for (Map.Entry<AbsoluteFilterID, IJDOLifecycleListenerFilter> me : newFilters.entrySet()) {
				if (!oldFilters.containsKey(me.getKey()))
					filtersToAdd.add(me.getValue());
			}

			for (IJDOLifecycleListenerFilter filter : filtersToRemove)
				removeFilter(filter.getFilterID());

			for (IJDOLifecycleListenerFilter filter : filtersToAdd)
				addFilter(filter);
		}

		return res;
	}
}
