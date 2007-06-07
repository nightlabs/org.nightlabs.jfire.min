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

package org.nightlabs.jfire.base.jdo.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jdo.JDOHelper;

import org.apache.log4j.Logger;
import org.nightlabs.config.Config;
import org.nightlabs.config.ConfigException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.jdo.JDOObjectID2PCClassMap;
import org.nightlabs.jfire.base.jdo.notification.ChangeSubjectCarrier;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleEvent;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleListener;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.jdo.JDOManager;
import org.nightlabs.jfire.jdo.JDOManagerUtil;
import org.nightlabs.jfire.jdo.cache.NotificationBundle;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.util.CollectionUtil;

/**
 * This cache was designed to hold JFire JDO objects on the client side
 * <p>
 * It stores the JDO objects by their object-id and the fetch-groups 
 * used to detach the objects. Additionally a scope can be used in case
 * the fetch-groups do not sufficently define how the object was detached 
 * (for example they might be detached with detach-load-fields) 
 * </p>
 * <p>
 * When the cache is asked for an object with a certain set of fecht-groups
 * an object from the cache is returned if its found to have been detached
 * with the same or more fetch-groups.
 * </p>
 * A singleton of this class caches <b>all</b> JDO objects 
 * in the client. To use it, you should implement your own
 * {@link org.nightlabs.jfire.base.jdo.JDOObjectProvider}.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class Cache
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(Cache.class);

	private static Cache _sharedInstance = null;

	private NotificationThread notificationThread;

	protected static class NotificationThread extends Thread
	{
		private static volatile int nextID = 0;

		private Cache cache;

		public NotificationThread(Cache cache)
		{
			this.cache = cache;
			setName("Cache.NotificationThread-" + (nextID++));
			start();
		}

		@Override
		public void run()
		{
			long lastErrorDT = 0;
			JDOManager jdoManager = null;

			// WORKAROUND: For classloading deadlock (java/osgi bug - give the Login some time to do its own classloading necessary due to LoginStateListeners being triggered)
			logger.info("NotificationThread.run: WORKAROUND for java classloading bug: delaying start of NotificationThread...");
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				// ignore silently
			}
			logger.info("NotificationThread.run: WORKAROUND for java classloading bug: delayed start of NotificationThread - continuing now!");

			while (!isInterrupted()) {
				try {
					if (jdoManager == null)
						jdoManager = JDOManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();

					NotificationBundle notificationBundle = jdoManager.waitForChanges(
							cache.getCacheCfMod().getWaitForChangesTimeoutMSec());

					if (notificationBundle != null && notificationBundle.isVirginCacheSession())
						cache.setResubscribeAllListeners(true);

					{ // implicit listeners
						SortedSet<DirtyObjectID> dirtyObjectIDs = notificationBundle == null ? null : notificationBundle.getDirtyObjectIDs();

						if (dirtyObjectIDs != null) {
							logger.info("Received notification for implicit listeners with " + dirtyObjectIDs.size() + " DirtyObjectIDs.");
							if (logger.isDebugEnabled()) {
								for (DirtyObjectID dirtyObjectID : dirtyObjectIDs)
									logger.debug("  " + dirtyObjectID);
							}

							Map<Object, DirtyObjectID> indirectlyAffectedDirtyObjectIDs = new HashMap<Object, DirtyObjectID>();
							Set<Object> objectIDsWithLifecycleStageDirty = new HashSet<Object>();

							int removedCarrierCount = 0;
							Set<Object> objectIDs = new HashSet<Object>(dirtyObjectIDs.size());
							for (DirtyObjectID dirtyObjectID : dirtyObjectIDs) {
								Object objectID = dirtyObjectID.getObjectID();
								objectIDs.add(objectID);
								if (JDOLifecycleState.DIRTY.equals(dirtyObjectID.getLifecycleState()))
									objectIDsWithLifecycleStageDirty.add(objectID);

								Set<Key> removedKeys = cache.removeByObjectID(objectID);
								removedCarrierCount += removedKeys.size();
								for (Key removedKey : removedKeys) {
									if (objectID.equals(removedKey.getObjectID()))
										continue;

									// Note, that these "synthetic" DirtyObjectIDs might have an objectID which is not really
									// a JDO object ID. This is, because removedKey.getObject() might return everything as the user
									// is able to put arbitrary objects (e.g. a Collection) with arbitrary keys into the Cache.
									// If you process DirtyObjectIDs, you must keep this in mind! Take the class
									// JDOObjectID2PCClassNotificationInterceptor as an example, how this can be done
									// (see the comment in the intercept(...) method): simply check whether the result of
									// DirtyObjectID.getObjectID() implements org.nightlabs.jdo.ObjectID
//									Class objectClass = cache.getObjectClassForObjectID(removedKey.getObjectID());
									DirtyObjectID doid = new DirtyObjectID(
											JDOLifecycleState.DIRTY, removedKey.getObjectID(),
//											objectClass.getName(),
											JDOObjectID2PCClassMap.sharedInstance().getPersistenceCapableClass(removedKey.getObjectID()).getName(),
											null, -Long.MAX_VALUE);
									indirectlyAffectedDirtyObjectIDs.put(doid.getObjectID(), doid);
								}
							}
							logger.info("Removed " + removedCarrierCount + " carriers from the cache.");

							cache.unsubscribeObjectIDs(
									objectIDs,
									cache.getCacheCfMod().getLocalListenerReactionTimeMSec());

							// remove all synthetic DirtyObjectIDs for which real dirtyObjectIDs exist
							for (Object objectID : objectIDsWithLifecycleStageDirty)
								indirectlyAffectedDirtyObjectIDs.remove(objectID);


							// create a set with all dirtyObjectIDs - the direct and the indirect=synthetic
							SortedSet<DirtyObjectID> dirtyObjectIDsForNotification = new TreeSet<DirtyObjectID>(dirtyObjectIDs);
							dirtyObjectIDsForNotification.addAll(indirectlyAffectedDirtyObjectIDs.values());

							// notify via local class based notification mechanism
							// the interceptor org.nightlabs.jfire.base.jdo.JDOObjectID2PCClassNotificationInterceptor takes care about correct class mapping
							JDOLifecycleManager.sharedInstance().notify(new NotificationEvent(
									cache,             // source
									(String)null,     // zone
									dirtyObjectIDsForNotification // dirtyObjectIDs    // subjects
							));

							// TODO the following is only for legacy (i.e. deprecated ChangeManager)
//							ArrayList<ChangeSubjectCarrier> subjectCarriers = new ArrayList<ChangeSubjectCarrier>(dirtyObjectIDs.size());
//							for (Iterator it = dirtyObjectIDs.iterator(); it.hasNext(); ) {

							ArrayList<ChangeSubjectCarrier> subjectCarriers = new ArrayList<ChangeSubjectCarrier>(dirtyObjectIDsForNotification.size());
							for (Iterator it = dirtyObjectIDsForNotification.iterator(); it.hasNext(); ) {
								DirtyObjectID dirtyObjectID = (DirtyObjectID) it.next();
								// ignore removal, because that's not supported by the old ChangeManager - new shouldn't be in that list
								if (dirtyObjectID.getLifecycleState() != JDOLifecycleState.DIRTY)
									continue;

								subjectCarriers.add(new ChangeSubjectCarrier(
										dirtyObjectID.getSourceSessionIDs(),
										dirtyObjectID.getObjectID()));
							}

//							if (!subjectCarriers.isEmpty()) {
//								org.nightlabs.jfire.base.jdo.notification.ChangeManager.sharedInstance().notify(
//										new ChangeEvent(
//												cache,             // source
//												(String)null,      // zone
//												null,              // subjects
//												null,              // subjectClasses
//												subjectCarriers)); // subjectCarriers
//								// TODO the above is only for legacy (i.e. deprecated ChangeManager)
//							}

						} // if (dirtyObjectIDs != null) {
					}

					{ // explicit listeners
						Map<Long, SortedSet<DirtyObjectID>> filterID2DirtyObjectIDs = notificationBundle == null ? null : notificationBundle.getFilterID2dirtyObjectIDs();
						if (filterID2DirtyObjectIDs != null) {
							logger.info("Received notification for " + filterID2DirtyObjectIDs.size() + " explicit listeners.");
							for (Map.Entry<Long, SortedSet<DirtyObjectID>> me : filterID2DirtyObjectIDs.entrySet()) {
								Long filterID = me.getKey();
								SortedSet<DirtyObjectID> dirtyObjectIDs = me.getValue();

								JDOLifecycleManager.sharedInstance().notify(filterID, new JDOLifecycleEvent(cache, dirtyObjectIDs));
//								JDOLifecycleListener listener = JDOLifecycleManager.sharedInstance().getLifecycleListener(filterID);
//								if (listener == null)
//									logger.error("No listener found for filterID="+filterID);
//								else {
//									if (logger.isDebugEnabled()) {
//										logger.debug("Triggering listener (filterID=" + filterID + ") with " + dirtyObjectIDs.size() + " dirtyObjectIDs:");
//										for (DirtyObjectID dirtyObjectID : dirtyObjectIDs) {
//											if (logger.isDebugEnabled())
//												logger.debug("  " + dirtyObjectID);
//										}
//									}
//
//									JDOLifecycleEvent event = new JDOLifecycleEvent(cache, dirtyObjectIDs);
//									listener.notify(event);
//								}
							}
						} // if (filterID2DirtyObjectIDs != null) {
					}

				} catch (Throwable t) {
					logger.error("Error in NotificationThread!", t);
					jdoManager = null;
					long lastErrorTimeDiff = System.currentTimeMillis() - lastErrorDT;
					long threadErrorWaitMSec = cache.getCacheCfMod().getThreadErrorWaitMSec();
					if (lastErrorTimeDiff < threadErrorWaitMSec) {
						try {
							sleep(threadErrorWaitMSec - lastErrorTimeDiff);
						} catch (InterruptedException e) {
							// ignore
						}
					}
					lastErrorDT = System.currentTimeMillis();
				}
			}
		}

		private volatile boolean terminated = false;

		/**
		 * This method checks not only for <code>super.isInterrupted()</code>,
		 * but additionally for {@link #terminated}.
		 *
		 * @see java.lang.Thread#isInterrupted()
		 * @see #interrupt()
		 */
		@Override
		public boolean isInterrupted()
		{
			return terminated || super.isInterrupted();
		}

		/**
		 * This method calls <code>super.interrupt()</code>,
		 * after setting {@link #terminated} to <code>true</code>.
		 *
		 * @see java.lang.Thread#interrupt()
		 * @see #isInterrupted()
		 */
		@Override
		public void interrupt()
		{
			terminated = true;
			super.interrupt();
		}
	}

	private volatile boolean resubscribeAllListeners = false;
	public boolean isResubscribeAllListeners()
	{
		return resubscribeAllListeners;
	}
	public void setResubscribeAllListeners(boolean resubscribeAllListeners)
	{
		this.resubscribeAllListeners = resubscribeAllListeners;
	}

	private CacheManagerThread cacheManagerThread;

	protected static class CacheManagerThread extends Thread
	{
		private static volatile int nextID = 0;

		private Cache cache;
//		private long lastResyncDT = System.currentTimeMillis();

		public CacheManagerThread(Cache cache)
		{
			this.cache = cache;
			setName("Cache.CacheManagerThread-" + (nextID++));
			start();
		}

		private Set<Object> currentlySubscribedObjectIDs = new HashSet<Object>();

		@Override
		public void run()
		{
			long lastErrorDT = 0;
			JDOManager jdoManager = null;
//			boolean resync;

			while (!isInterrupted()) {
				try {
					try {
						sleep(cache.getCacheCfMod().getCacheManagerThreadIntervalMSec());
					} catch (InterruptedException x) {
						// ignore
					}

					// *** container management below *** 
					// (this might affect the listeners to be removed - NO: We don't drop listeners when we remove objects from Cache! Marco.)
					CarrierContainer activeCarrierContainer = cache.getActiveCarrierContainer();
					if (System.currentTimeMillis() - activeCarrierContainer.getCreateDT() > cache.getCacheCfMod().getCarrierContainerActivityMSec()) {
						cache.rollCarrierContainers();
					}
					// *** container management above ***

					// *** listener management below ***
					// fill in the object graph for all new objects 
					Map<Key, Carrier> newCarriersByKey = cache.fetchNewCarriersByKey();
					if (newCarriersByKey != null) {
						for (Map.Entry<Key, Carrier> me : newCarriersByKey.entrySet()) {
							Key key = me.getKey();
							Carrier carrier = me.getValue();

							Set objectIDs = carrier.getObjectIDs();
							cache.mapObjectIDs2Key(objectIDs, key);
							cache.subscribeObjectIDs(objectIDs, 0);
						}
					} // if (newCarriersByKey != null) {

//					resync = System.currentTimeMillis() - lastResyncDT > cache.getCacheCfMod().getResyncRemoteListenersIntervalMSec();

					Map<Object, SubscriptionChangeRequest> subscriptionChanges = cache.fetchSubscriptionChangeRequests();
//					if (LOGGER.isDebugEnabled())
//						LOGGER.debug("Thread found " + subscriptionChanges.size() + " subscription change requests.");

					Map<Object, SubscriptionChangeRequest> newSubscriptionChanges = null;
					LinkedList<Object> objectIDsToSubscribe = null;
					LinkedList<Object> objectIDsToUnsubscribe = null;
					LinkedList<IJDOLifecycleListenerFilter> filtersToSubscribe = null;
					LinkedList<Long> filterIDsToUnsubscribe = null;
					boolean restoreCurrentlySubscribedObjectIDs = true;
					try {
						long now = System.currentTimeMillis();
						for (Iterator it = subscriptionChanges.entrySet().iterator(); it.hasNext(); ) {
							Map.Entry me = (Map.Entry) it.next();
							Object objectID = me.getKey();
							SubscriptionChangeRequest scr = (SubscriptionChangeRequest) me.getValue();

							if (scr.getScheduledActionDT() > now) {
								if (logger.isDebugEnabled())
									logger.debug("Subscription change request " + scr.toString() + " is delayed and will be processed in about " + (scr.getScheduledActionDT() - now) + " msec.");

								if (newSubscriptionChanges == null)
									newSubscriptionChanges = new HashMap<Object, SubscriptionChangeRequest>();

								newSubscriptionChanges.put(objectID, scr);
							}
							else {
								if (scr.getAction() == SubscriptionChangeRequest.ACTION_REMOVE) {
								// remove
									if (scr.getObjectID() != null) {
										// implicit listener
										currentlySubscribedObjectIDs.remove(objectID);

										if (objectIDsToUnsubscribe == null)
											objectIDsToUnsubscribe = new LinkedList<Object>();

										objectIDsToUnsubscribe.add(objectID);
									}
									else {
										// warning: the filterID in the filter is already reset (in removeLifecycleListenerFilter(...))
										// because we use only the filterID as saved in the map's key
										// explicit listener
										if (filterIDsToUnsubscribe == null)
											filterIDsToUnsubscribe = new LinkedList<Long>();

										filterIDsToUnsubscribe.add((Long)objectID);
									}
								}
								else {
								// add
									if (scr.getObjectID() != null) {
										// implicit listener
										// if there exists already one, we don't register it again => ignore
										if (!currentlySubscribedObjectIDs.contains(objectID)) {
											if (objectIDsToSubscribe == null)
												objectIDsToSubscribe = new LinkedList<Object>();

											objectIDsToSubscribe.add(objectID);
											currentlySubscribedObjectIDs.add(objectID);
										}
										else {
											if (logger.isDebugEnabled())
												logger.debug("Subscription change request " + scr.toString() + " is ignored, because there exists already a listener.");
										}
									}
									else {
										// explicit listener
										if (filtersToSubscribe == null)
											filtersToSubscribe = new LinkedList<IJDOLifecycleListenerFilter>();

										filtersToSubscribe.add(scr.getFilter());
									}
								}
							}
						}

						if (jdoManager == null)
							jdoManager = JDOManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();

						if (cache.isResubscribeAllListeners()) {
							logger.info("Synchronizing all listeners.");

							cache.setResubscribeAllListeners(false);
							boolean resubscribeFailed = true;
							try {
								jdoManager.resubscribeAllListeners(
										currentlySubscribedObjectIDs,
										// TODO we should ensure JDOLifecycleManager works in server-mode as well!
										JDOLifecycleManager.sharedInstance().getLifecycleListenerFilters());
								resubscribeFailed = false;
							} finally {
								if (resubscribeFailed)
									cache.setResubscribeAllListeners(true);
							}

//							lastResyncDT = System.currentTimeMillis();
						}
						else {
							if (objectIDsToUnsubscribe != null || objectIDsToSubscribe != null ||
									filterIDsToUnsubscribe != null || filtersToSubscribe != null) {
								logger.info(
										"Adding " +
										(objectIDsToSubscribe == null ? 0 : objectIDsToSubscribe.size()) +
										" and removing " +
										(objectIDsToUnsubscribe == null ? 0 : objectIDsToUnsubscribe.size()) +
										" implicit remote listeners. Adding " +
										(filtersToSubscribe == null ? 0 : filtersToSubscribe.size()) +
										" and removing " +
										(filterIDsToUnsubscribe == null ? 0 : filterIDsToUnsubscribe.size()) +
										" filters for explicit listeners.");
								
								if (logger.isDebugEnabled()) {
									logger.debug("Change listeners for the following ObjectIDs will be removed:");
									if (objectIDsToUnsubscribe == null)
										logger.debug("      NONE!");
									else {
										for (Iterator it = objectIDsToUnsubscribe.iterator(); it.hasNext(); )
											logger.debug("      " +  it.next());
									}

									logger.debug("Change listeners for the following ObjectIDs will be added:");
									if (objectIDsToSubscribe == null)
										logger.debug("      NONE!");
									else {
										for (Iterator it = objectIDsToSubscribe.iterator(); it.hasNext(); )
											logger.debug("      " +  it.next());
									}

									logger.debug("LifecycleListenerFilters with the following IDs will be removed:");
									if (filterIDsToUnsubscribe == null)
										logger.debug("      NONE!");
									else {
										for (Long filterID : filterIDsToUnsubscribe)
											logger.debug("      " +  filterID);
									}

									logger.debug("LifecycleListenerFilters will be added:");
									if (filtersToSubscribe == null)
										logger.debug("      NONE!");
									else {
										for (IJDOLifecycleListenerFilter filter : filtersToSubscribe) {
											logger.debug("      id=" +  filter.getFilterID().getFilterID() + " filter=" + filter);
										}
									}
								}

								jdoManager.removeAddListeners(
										objectIDsToUnsubscribe,
										objectIDsToSubscribe,
										filtersToSubscribe,
										filterIDsToUnsubscribe);
							}
						}

						restoreCurrentlySubscribedObjectIDs = false; // this is only executed, if there is no exception before
					} finally {
						if (restoreCurrentlySubscribedObjectIDs) {
							logger.warn("An error occured - will restore previous subscription change requests.");

							if (objectIDsToSubscribe != null)
								currentlySubscribedObjectIDs.removeAll(objectIDsToSubscribe);
							// I think it is better to have a listener too much than one missing,
							// therefore I don't restore the ones which were unsubscribed
							// and I don't know whether it really was in the set...

							cache.restoreOldSubscriptionChangeRequests(subscriptionChanges);
						}
						else
							cache.restoreOldSubscriptionChangeRequests(newSubscriptionChanges);
					}
					// *** listener management above ***

				} catch (Throwable t) {
					logger.error("Error in NotificationThread!", t);
					jdoManager = null;
					long lastErrorTimeDiff = System.currentTimeMillis() - lastErrorDT;
					long threadErrorWaitMSec = cache.getCacheCfMod().getThreadErrorWaitMSec();
					if (lastErrorTimeDiff < threadErrorWaitMSec) {
						try {
							sleep(threadErrorWaitMSec - lastErrorTimeDiff);
						} catch (InterruptedException e) {
							// ignore
						}
					}
					lastErrorDT = System.currentTimeMillis();
				}
			}
		}

		private volatile boolean terminated = false;

		/**
		 * This method checks not only for <code>super.isInterrupted()</code>,
		 * but additionally for {@link #terminated}.
		 *
		 * @see java.lang.Thread#isInterrupted()
		 * @see #interrupt()
		 */
		@Override
		public boolean isInterrupted()
		{
			return terminated || super.isInterrupted();
		}

		/**
		 * This method calls <code>super.interrupt()</code>,
		 * after setting {@link #terminated} to <code>true</code>.
		 *
		 * @see java.lang.Thread#interrupt()
		 * @see #isInterrupted()
		 */
		@Override
		public void interrupt()
		{
			terminated = true;
			super.interrupt();
		}
	}

//	private Map<Long, Subsc> filterSubscribeRequests = new HashMap<Long, IJDOLifecycleListenerFilter>();
//	private Object filterSubscribeRequestsMutex = new Object();

	/**
	 * key: Object objectID (can be an instance of {@link Long} which references the filterID then and means the {@link SubscriptionChangeRequest} has an {@link IJDOLifecycleListenerFilter})<br/>
	 * value: SubscriptionChangeRequest
	 * <p>
	 * The
	 * {@link CacheManagerThread} will fetch them (if non-empty) and
	 * register/unregisters listeners for them. When doing this, it will replace
	 * the <code>Set</code> by a newly created empty <code>HashSet</code>. After it has done
	 * its work, it will merge the not-yet-performed changes (they can be delayed)
	 * with the new changes that have been added here while the <code>CacheManagerThread</code>
	 * was busy.
	 */
	private Map<Object, SubscriptionChangeRequest> subscriptionChangeRequests = new HashMap<Object, SubscriptionChangeRequest>();
	private Object subscriptionChangeRequestsMutex = new Object();

	/**
	 * @return Returns the {@link #objectIDsToSubscribe} and replaces
	 *		the field by a new instance of <code>HashMap</code>.
	 */
	protected Map<Object, SubscriptionChangeRequest> fetchSubscriptionChangeRequests()
	{
		Map<Object, SubscriptionChangeRequest> res;
		synchronized (subscriptionChangeRequestsMutex) {
			res = subscriptionChangeRequests;
			subscriptionChangeRequests = new HashMap<Object, SubscriptionChangeRequest>();
		}
		return res;
	}

	/**
	 * This method transfers (and by that overwrites older values)
	 * the new data (from {@link #subscriptionChangeRequests}) to <code>oldChanges</code>.
	 * Then, it replaces the field <code>subscriptionChangeRequests</code> with
	 * <code>oldChanges</code>.
	 * <p>
	 * This is called by the {@link CacheManagerThread} in case of failure or if
	 * some of the fetched {@link SubscriptionChangeRequest}s are delayed and could not
	 * yet be done.
	 *
	 * @param oldChangeRequests Either <code>null</code> or a <code>Map</code> like the one
	 *		returned from {@link #fetchSubscriptionChangeRequests()}.
	 */
	protected void restoreOldSubscriptionChangeRequests(Map<Object, SubscriptionChangeRequest> oldChangeRequests)
	{
		if (oldChangeRequests == null || oldChangeRequests.isEmpty()) {
//			LOGGER.debug("There are no old subscription change requests. Won't do anything.");
			return;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Restoring older subscription change requests:");
			for (Iterator it = oldChangeRequests.values().iterator(); it.hasNext(); ) {
				SubscriptionChangeRequest scr = (SubscriptionChangeRequest) it.next();
				logger.debug("      " + scr);
			}
		}

		synchronized (subscriptionChangeRequestsMutex) {
			if (logger.isDebugEnabled()) {
				if (subscriptionChangeRequests.isEmpty())
					logger.debug("There are no new subscription change requests to merge into the old ones. Simply replacing them.");
				else {
					logger.debug("There are new subscription change requests which will be merged into the old ones:");
					for (Iterator it = subscriptionChangeRequests.values().iterator(); it.hasNext(); ) {
						SubscriptionChangeRequest scr = (SubscriptionChangeRequest) it.next();
						logger.debug("      " + scr);
					}
				}
			}

			oldChangeRequests.putAll(subscriptionChangeRequests);
			subscriptionChangeRequests = oldChangeRequests;

			if (logger.isDebugEnabled()) {
				logger.debug("These are the subscription change requests after restore:");
				for (Iterator it = subscriptionChangeRequests.values().iterator(); it.hasNext(); ) {
					SubscriptionChangeRequest scr = (SubscriptionChangeRequest) it.next();
					logger.debug("      " + scr);
				}
			}
		}
	}

	/**
	 * Though this method is public, you <b>must not</b> call it directly. Instead, you should
	 * use the API provided by {@link JDOLifecycleManager}.
	 * <p>
	 * This method is called by {@link JDOLifecycleManager} when a new {@link JDOLifecycleListener}
	 * is added.
	 * </p>
	 *
	 * @param filter The filter to be added asynchronously
	 */
	public void addLifecycleListenerFilter(IJDOLifecycleListenerFilter filter, long delayMSec)
	{
		synchronized (subscriptionChangeRequestsMutex) {
			Long filterID = filter.getFilterID().getFilterID();
			
			SubscriptionChangeRequest scr = subscriptionChangeRequests.get(filterID);
			if (scr != null) {
				if (scr.getAction() == SubscriptionChangeRequest.ACTION_ADD
						&&
						scr.getScheduledActionDT() < System.currentTimeMillis() + delayMSec) {

					if (logger.isDebugEnabled())
						logger.warn("Ignoring request to add LifecycleListenerFilter, because there is already a request, which is scheduled earlier. filterID="+filterID+" filter: " + filter);

					return;
				}
			}

			subscriptionChangeRequests.put(
					filterID,
					new SubscriptionChangeRequest(
							SubscriptionChangeRequest.ACTION_ADD,
							filter,
							delayMSec));
		}
	}

	/**
	 * Though this method is public, you <b>must not</b> call it directly. Instead, you should
	 * use the API provided by {@link JDOLifecycleManager}.
	 * <p>
	 * This method is called by {@link JDOLifecycleManager} when a {@link JDOLifecycleListener}
	 * is removed.
	 * </p>
	 *
	 * @param filter The filter to be added asynchronously
	 */
	public void removeLifecycleListenerFilter(IJDOLifecycleListenerFilter filter, long delayMSec)
	{
		synchronized (subscriptionChangeRequestsMutex) {
			Long filterID = filter.getFilterID().getFilterID();
			
			SubscriptionChangeRequest scr = subscriptionChangeRequests.get(filterID);
			if (scr != null) {
				if (scr.getAction() == SubscriptionChangeRequest.ACTION_REMOVE
						&&
						scr.getScheduledActionDT() < System.currentTimeMillis() + delayMSec) {

					if (logger.isDebugEnabled())
						logger.warn("Ignoring request to remove LifecycleListenerFilter, because there is already a request, which is scheduled earlier. filterID="+filterID+" filter: " + filter);

					return;
				}
			}

			// the CacheManagerThread doesn't access filter.filterID anymore,
			// hence it's safe to reset it here.
			filter.setFilterID(null);
			subscriptionChangeRequests.put(
					filterID,
					new SubscriptionChangeRequest(
							SubscriptionChangeRequest.ACTION_REMOVE,
							filter,
							delayMSec));
		}
	}

	/**
	 * This method subscribes the given <code>objectIDs</code> asynchronously
	 * via the {@link CacheManagerThread} (which calls
	 * {@link JDOManager#addChangeListeners(java.lang.String, java.util.Collection)}).
	 */
	protected void subscribeObjectIDs(Collection objectIDs, long delayMSec)
	{
		synchronized (subscriptionChangeRequestsMutex) {
			for (Iterator it = objectIDs.iterator(); it.hasNext(); ) {
				Object objectID = it.next();

				if (!(objectID instanceof ObjectID)) // we do not subscribe listeners to non-jdo keys
					continue;

				SubscriptionChangeRequest scr = (SubscriptionChangeRequest) subscriptionChangeRequests.get(objectID);
				if (scr != null) {
					if (scr.getAction() == SubscriptionChangeRequest.ACTION_ADD
							&&
							scr.getScheduledActionDT() < System.currentTimeMillis() + delayMSec) {

						if (logger.isDebugEnabled())
							logger.debug("Ignoring request to subscribe ObjectID, because there is already a request, which is scheduled earlier. ObjectID: " + objectID);

						continue;
					}
				}

				subscriptionChangeRequests.put(
						objectID,
						new SubscriptionChangeRequest(SubscriptionChangeRequest.ACTION_ADD,
								objectID,
								delayMSec));
			}
		}
	}

	/**
	 * This method unsubscribes the given <code>objectIDs</code> asynchronously
	 * via the {@link CacheManagerThread} (which calls
	 * {@link JDOManager#removeChangeListeners(java.lang.String, java.util.Collection)}).
	 */
	protected void unsubscribeObjectIDs(Collection objectIDs, long delayMSec)
	{
		synchronized (subscriptionChangeRequestsMutex) {
			for (Iterator it = objectIDs.iterator(); it.hasNext(); ) {
				Object objectID = it.next();

				SubscriptionChangeRequest scr = (SubscriptionChangeRequest) subscriptionChangeRequests.get(objectID);
				if (scr != null) {
					if (scr.getAction() == SubscriptionChangeRequest.ACTION_REMOVE
							&&
							scr.getScheduledActionDT() < System.currentTimeMillis() + delayMSec) {
						if (logger.isDebugEnabled())
							logger.debug("Ignoring request to unsubscribe ObjectID, because there is already a request, which is scheduled earlier. ObjectID: " + objectID);

						return;
					}
				}

				subscriptionChangeRequests.put(
						objectID,
						new SubscriptionChangeRequest(SubscriptionChangeRequest.ACTION_REMOVE,
								objectID,
								delayMSec));
			}
		}
	}

	private static boolean serverMode = false;

	private static boolean autoOpen = true;

	public static synchronized boolean isAutoOpen()
	{
		return autoOpen;
	}
	/**
	 * If the <code>Cache</code> is used in server-mode, it ignores this flag and always opens automatically.
	 * In client-mode, however, the cache is only opened automatically, iff this flag is <code>true</code>.
	 * <p>
	 * The default value of <code>autoOpen</code> is <code>true</code>.
	 * </p>
	 *
	 * @param autoOpen if <code>true</code>, automatically open the <code>Cache</code> as soon as
	 *		the shared instance is created.
	 */
	public static synchronized void setAutoOpen(boolean autoOpen)
	{
		Cache.autoOpen = autoOpen;
	}

	public static synchronized void setServerMode(boolean serverMode)
	{
		if (serverMode && _sharedInstance != null)
			throw new IllegalStateException("Cannot switch to serverMode after a client-mode-sharedInstance has been created!");

		if (!serverMode && serverModeSharedInstances != null)
			throw new IllegalStateException("Cannot switch to clientMode after a server-mode-sharedInstance has been created!");

		Cache.serverMode = serverMode;
	}

	private static Map<String, Cache> serverModeSharedInstances = null;

	private static String getCurrentUserName()
	{
		return SecurityReflector.getUserDescriptor().getCompleteUserID();
	}

	private static String getCurrentSessionID()
	{
		return SecurityReflector.getUserDescriptor().getSessionID();
	}

	/**
	 * @return Returns the singleton of this class - or the pseudo-shared instance for the currently logged in user.
	 *
	 * @throws In case the cache needs to be created and a {@link ConfigException} occurs while obtaining {@link CacheCfMod}. 
	 */
	public static synchronized Cache sharedInstance()
	{
		try {
			if (serverMode) {
				if (serverModeSharedInstances == null)
					serverModeSharedInstances = new HashMap<String, Cache>();

				String userName = getCurrentUserName();
				Cache cache = serverModeSharedInstances.get(userName);
				if (cache == null) {
					cache = new Cache();
					serverModeSharedInstances.put(userName, cache);
					cache.open(getCurrentSessionID());
				}
				return cache;
			}
			else {
				if (_sharedInstance == null) {
					_sharedInstance = new Cache();
					if (autoOpen)
						_sharedInstance.open(getCurrentSessionID());
				}

				return _sharedInstance;
			}
		} catch (ConfigException e) {
			throw new RuntimeException(e);
		}
	}

	private String sessionID = null;

	public synchronized String getSessionID()
	{
		assertOpen();

//		if (sessionID == null) {
//			Base62Coder coder = Base62Coder.sharedInstance();
//			sessionID = coder.encode(System.currentTimeMillis(), 1) + '-' + coder.encode((long)(Math.random() * 10000), 1);
//			LOGGER.info("The Cache doesn't have a sessionID. Assigning \""+getSessionID()+"\"");
//		}

		return sessionID;
	}

	/**
	 * key: {@link Key} key<br/>
	 * value: {@link Carrier} carrier
	 */
	private Map<Key, Carrier> key2Carrier = new HashMap<Key, Carrier>();

	/**
	 * This map references all keys that are affected when an object becomes invalidated
	 * (i.e. removed or dirty on the server). That means, it contains relations for the
	 * whole object graph.
	 * <p>
	 * Note, that the transversal is asynchronously by the {@link CacheManagerThread}. 
	 * </p>
	 *
	 * key: Object objectID<br/>
	 * value: Set of Key
	 */
	private Map<Object, Set<Key>> objectID2KeySet_dependency = new HashMap<Object, Set<Key>>();

	/**
	 * In contrast to {@link #objectID2KeySet_dependency}, this map references only those {@link Key}s,
	 * that point to the same objectID. And this map is only used for scope <code>null</code>.
	 */
	private Map<Object, Set<Key>> objectID2KeySet_alternative = new HashMap<Object, Set<Key>>();

	/**
	 * When a new object is put into the Cache, it is immediately registered in
	 * {@link #key2Carrier}, {@link #objectID2KeySet_dependency} and here. In {@link #objectID2KeySet_dependency}
	 * however, there is only the main <code>objectID</code> registered immediately.
	 * All contained objects within the object graph are added later in the
	 * {@link CacheManagerThread}. This thread replaces this Map using
	 * {@link #fetchNewCarriersByKey()}.
	 *
	 * key: {@link Key} key<br/>
	 * value: {@link Carrier} carrier
	 */
	private Map<Key, Carrier> newCarriersByKey = new HashMap<Key, Carrier>();

	/**
	 * This is a rolling carrier-registration with the activeCarrierContainer
	 * being the first entry.
	 */
	private LinkedList<CarrierContainer> carrierContainers = new LinkedList<CarrierContainer>();
	private CarrierContainer activeCarrierContainer;

	protected CarrierContainer getActiveCarrierContainer()
	{
		return activeCarrierContainer;
	}

	/**
	 * @return If the Map is empty, it returns <code>null</code>,
	 *		otherwise it replaces the current map by a new one and
	 *		returns it.
	 */
	protected synchronized Map<Key, Carrier> fetchNewCarriersByKey()
	{
		if (newCarriersByKey.isEmpty())
			return null;

		Map<Key, Carrier> res = newCarriersByKey;
		newCarriersByKey = new HashMap<Key, Carrier>();
		return res;
	}

	protected synchronized void rollCarrierContainers()
	{
		logger.info("Creating new activeCarrierContainer.");
		CarrierContainer newActiveCC = new CarrierContainer(this);
		carrierContainers.addFirst(newActiveCC);
		activeCarrierContainer = newActiveCC;

		long carrierContainerCount = getCacheCfMod().getCarrierContainerCount();

		if (carrierContainerCount < 2)
			throw new IllegalStateException("carrierContainerCount = "+carrierContainerCount+" but must be at least 2!!!");

		while (carrierContainers.size() > carrierContainerCount) {
			CarrierContainer cc = (CarrierContainer) carrierContainers.removeLast();
			logger.info("Dropping carrierContainer (created " + cc.getCreateDT() + ")");
			cc.close();
		}
	}

	private CacheCfMod cacheCfMod;

	protected CacheCfMod getCacheCfMod()
	{
		return cacheCfMod;
	}

	/**
	 * Should not be used! Use {@link #sharedInstance()} instead!
	 *
	 * @throws ConfigException If it fails to obtain the {@link CacheCfMod} config module.
	 */
	protected Cache() throws ConfigException
	{
		logger.info("Creating new Cache instance.");
		cacheCfMod = (CacheCfMod) Config.sharedInstance().createConfigModule(CacheCfMod.class);
		Config.sharedInstance().save(); // TODO remove this as soon as we have a thread that periodically saves it.
		activeCarrierContainer = new CarrierContainer(this);
		carrierContainers.addFirst(activeCarrierContainer);
//		notificationThread.start();
//		cacheManagerThread.start();
	}

//	/**
//	 * @param sa Either <code>null</code> or an array of <code>String</code>.
//	 * @return Returns <code>null</code> if <code>sa</code> is <code>null</code> or a new instance
//	 *		of <code>HashSet</code> with all entries from the String array.
//	 */
//	protected static Set stringArray2Set(String[] sa)
//	{
//		if (sa == null)
//			return null;
//
//		Set set = new HashSet(sa.length);
//		for (int i = 0; i < sa.length; ++i)
//			set.add(sa[i]);
//
//		return set;
//	}

	/**
	 * This method calls {@link #get(String, Object, Set)} - look there for more details.
	 *
	 * @param scope Either <code>null</code> (default) or a <code>String</code> to separate
	 *		a special namespace in the cache. This is necessary, if the method with which
	 *		the object has been fetched from the server has modified the object and it
	 *		therefore differs from the result of the default fetch method, even if the
	 *		<code>fetchGroups</code> are identical.
	 * @param objectID A non-<code>null</code> JDO object ID referencing a persistence-capable
	 *		object which may exist in this cache.
	 * @param fetchGroups Either <code>null</code> or a String array of those JDO fetch groups,
	 *		that should have been used to retrieve the object. This String array is
	 *		transformed to a <code>Set</code>.
	 *
	 * @return Returns either <code>null</code> or the desired JDO object.
	 */
	public Object get(String scope, Object objectID, String[] fetchGroups, int maxFetchDepth)
	{
		return get(scope, objectID, CollectionUtil.array2HashSet(fetchGroups), maxFetchDepth);
	}

	/**
	 * Use this method to retrieve an object from the cache. This method updates
	 * the access timestamp, if an object has been found.
	 * <p>
	 * If <code>scope == null</code>, this method searches for an object that has
	 * been retrieved with <b>at least</b> the required <code>fetchGroups</code> and <b>at least</b>
	 * the required <code>maxFetchDepth</code>, in case no exact match can be found.
	 * </p>
	 *
	 * @param scope Either <code>null</code> (default) or a <code>String</code> to separate
	 *		a special namespace in the cache. This is necessary, if the method with which
	 *		the object has been fetched from the server has modified the object and it
	 *		therefore differs from the result of the default fetch method, even if the
	 *		<code>fetchGroups</code> are identical.
	 * @param objectID A non-<code>null</code> JDO object ID referencing a persistence-capable
	 *		object which may exist in this cache.
	 * @param fetchGroups Either <code>null</code> or a Set of String with those JDO fetch
	 *		groups, that should have been used to retrieve the object.
	 *
	 * @return Returns either <code>null</code> or the desired JDO object.
	 */
	public synchronized Object get(String scope, Object objectID, Set<String> fetchGroups, int maxFetchDepth)
	{
		assertOpen();
		Object object = null;

		Key key = new Key(scope, objectID, fetchGroups, maxFetchDepth);
		Carrier carrier = (Carrier) key2Carrier.get(key);
		if (carrier == null) {
			if (logger.isDebugEnabled())
				logger.debug("No Carrier found for key: " + key.toString());

			// If and only if the scope is null, we search for a record that contains
			// AT LEAST our required fetch groups and AT LEAST our maxFetchDepth.
			if (scope == null) {
				if (logger.isDebugEnabled())
					logger.debug("scope == null => searching for alternative entries (which contain at least the required fetch groups)...");

				Set<Key> keySet = objectID2KeySet_alternative.get(objectID);
				if (keySet != null) {
					iterateCandidateKey: for (Key candidateKey : keySet) {
						// is the scope correct?
						if (candidateKey.getScope() != null)
							continue iterateCandidateKey;
						
						// is the maxFetchDepth sufficient?
						if (maxFetchDepth < 0) {
							if (maxFetchDepth != NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT)
								throw new IllegalArgumentException("maxFetchDepth < 0 but maxFetchDepth != NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT");
	
							if (maxFetchDepth != candidateKey.getMaxFetchDepth())
								continue iterateCandidateKey;
						}
						else if (maxFetchDepth > candidateKey.getMaxFetchDepth())
							continue iterateCandidateKey;
	
						// does the candidateKey contain all required fetchgroups?
						if (fetchGroups != null) {
							if (candidateKey.getFetchGroups() == null || !candidateKey.getFetchGroups().containsAll(fetchGroups))
								continue iterateCandidateKey;
						}
	
						carrier = (Carrier) key2Carrier.get(candidateKey);
						if (carrier != null) {
							object = carrier.getObject();
							if (object == null) {
								logger.warn("Found Carrier, but object has already been released by the garbage collector! If this message occurs often, give the VM more memory or the Cache a shorter object-lifetime! Alternatively, you may switch to hard references. searched key: " + key.toString() + " candidateKey: " + candidateKey.toString());

								removeByKey(candidateKey);
								carrier = null;
							}
							else {
								if (logger.isDebugEnabled())
									logger.debug("Found alternative entry with at least the required fetch groups: searched key: " + key.toString() + " candidateKey: " + candidateKey.toString());

								carrier.setAccessDT();
								return object;
							}

						} // if (carrier != null) {
					} // iterateCandidateKey
				} // if (keySet != null) {

				if (logger.isDebugEnabled())
					logger.debug("...no alternative entry found.");
			} // if (scope == null) {

			return null;
		} // if (carrier == null) {

		object = carrier.getObject();
		if (object == null) { // remove the unnecessary keys from all indices.
			logger.warn("Found Carrier, but object has already been released by the garbage collector! If this message occurs often, give the VM more memory or the Cache a shorter object-lifetime! Alternatively, you may switch to hard references. key: " + key.toString());

			removeByKey(key);
			return null;
		}

		if (logger.isDebugEnabled())
			logger.debug("Found Carrier - will return object from cache. key: " + key.toString());

		carrier.setAccessDT();
		return object;
	}

	public void putAll(String scope, Collection objects, String[] fetchGroups, int maxFetchDepth)
	{
		putAll(scope, objects, CollectionUtil.array2HashSet(fetchGroups), maxFetchDepth);
	}

	public void putAll(String scope, Collection objects, Set<String> fetchGroups, int maxFetchDepth)
	{
		if (objects == null)
			throw new NullPointerException("objects must not be null!");

		for (Iterator it = objects.iterator(); it.hasNext(); )
			put(scope, it.next(), fetchGroups, maxFetchDepth);
	}

	/**
	 * This method calls {@link #put(String, Object, Set)}.
	 */
	public void put(String scope, Object object, String[] fetchGroups, int maxFetchDepth)
	{
		put(scope, object, CollectionUtil.array2HashSet(fetchGroups), maxFetchDepth);
	}

	/**
	 * This method puts a jdo object into the cache. Therefore, it
	 * obtains the objectID and calls {@link #put(String, Object, Object, Set)}.
	 */
	public void put(String scope, Object object, Set<String> fetchGroups, int maxFetchDepth)
	{
		put(scope, JDOHelper.getObjectId(object), object, fetchGroups, maxFetchDepth);
	}

	/**
	 * This method calls {@link #put(String, Object, Object, Set)}.
	 */
	public void put(String scope, Object objectID, Object object, String[] fetchGroups, int maxFetchDepth)
	{
		put(scope, objectID, object, CollectionUtil.array2HashSet(fetchGroups), maxFetchDepth);
	}

	/**
	 * You can put non-jdo objects into the cache using this method. If they contain jdo-objects
	 * in their object graph, the cache will however register listeners for these contained objects.
	 *
	 * @param scope Either <code>null</code> to indicate that the object has been fetched normally using
	 *		the fetchGroups or a String specifying a customized fetch-method. This can e.g. be the case
	 *		if the object has been detached using the detach-on-close feature. If you cache a non-jdo-object,
	 *		you should specify <code>scope</code> and pass <code>fetchGroups = null</code>.
	 * @param objectID When storing a jdo object, the jdo-object-id, or (when storing a non-jdo-object)
	 *		any key-object you want.
	 * @param object The object to be cached. The cache will recursively scan it and register server-sided
	 *		change-listeners for all found objectIDs (including the one passed as parameter <code>objectID</code>).
	 *		If one of the objects in the graph gets changed, the cache will forget this main-object.
	 * @param fetchGroups Either <code>null</code> or the fetchGroups with which the object has been retrieved.
	 *		If you cache a non-jdo-object, you should pass <code>null</code> here and use <code>scope</code>.
	 */
	public void put(String scope, Object objectID, Object object, Set<String> fetchGroups, int maxFetchDepth)
	{
		assertOpen();

		if (object == null)
			throw new NullPointerException("object must not be null!");

		if (objectID == null)
			throw new NullPointerException("objectID must not be null!");

		JDOObjectID2PCClassMap.sharedInstance().initPersistenceCapableClass(objectID, object.getClass());

		Key key = new Key(scope, objectID, fetchGroups, maxFetchDepth);

		if (logger.isDebugEnabled())
			logger.debug("Putting object into cache. key: " + key.toString());

		synchronized (this) {
			// remove the old carrier - if existing
			Carrier oldCarrier = (Carrier) key2Carrier.get(key);
			if (oldCarrier != null) {
				if (logger.isDebugEnabled())
					logger.debug("There was an old carrier for the same key in the cache; removing it. key: " + key.toString());

				oldCarrier.setCarrierContainer(null);
			}

			// the constructor of Carrier self-registers in the active CarrierContainer
			Carrier carrier = new Carrier(key, object, getActiveCarrierContainer());

			// store the new carrier in our main cache map
			key2Carrier.put(key, carrier);
			// ...and in the newCarriers map
			newCarriersByKey.put(key, carrier);

			// register the key by its objectID (for fast removal if the object changed)
			// note that the whole object graph is filled in later by the CacheManagerThread
			{
				Set<Key> keySet = objectID2KeySet_dependency.get(objectID);
				if (keySet == null) {
					keySet = new HashSet<Key>();
					objectID2KeySet_dependency.put(objectID, keySet);
				}
				keySet.add(key);
			}

			// register the key for finding alternatives with more fetchgroups (only in scope null)
			if (scope == null) {
				Set<Key> keySet = objectID2KeySet_alternative.get(objectID);
				if (keySet == null) {
					keySet = new HashSet<Key>();
					objectID2KeySet_alternative.put(objectID, keySet);
				}
				keySet.add(key);
			}
		}

//		// register the class of the pc object in the JDOObjectID2PCClassMap to minimize lookups.
//		JDOObjectID2PCClassMap.sharedInstance().initPersistenceCapableClass(
//				objectID, object.getClass());
// THIS IS NOW DONE in Carrier.getObjectIDs()

//		// Subscribe a listener to this object. This will not necessarily cause a remote
//		// method call, because the CacheManagerThread keeps a Set of all registered listeners
//		// and minimizes traffic by that.
//		subscribeObjectID(objectID, 0);
// THIS IS NOW DONE VIA newCarriersByKey in the CacheManagerThread
	}

	/**
	 * This method is called by the {@link CacheManagerThread} if there are objects returned
	 * by {@link #fetchNewCarriersByKey()}. It adds entries to {@link #objectID2KeySet_dependency}.
	 *
	 * @param objectIDs The object-ids that should point to the given key.
	 * @param key The key that should be mapped by all the given objectIDs.
	 */
	protected synchronized void mapObjectIDs2Key(Collection objectIDs, Key key)
	{
		for (Iterator it = objectIDs.iterator(); it.hasNext(); ) {
			Object objectID = it.next();

			Set<Key> keySet = objectID2KeySet_dependency.get(objectID);
			if (keySet == null) {
				keySet = new HashSet<Key>();
				objectID2KeySet_dependency.put(objectID, keySet);
			}
			keySet.add(key);
		}
	}

	/**
	 * This method is called by {@link CarrierContainer#close()} and removes
	 * the <code>Carrier</code> specified by the given <code>Key</code> from the <code>Cache</code>.
	 * Note, that this method DOES NOT unregister server-sided listeners in any case!
	 *
	 * @param key The key referencing the <code>Carrier</code> that shall be removed.
	 */
	protected synchronized void removeByKey(Key key)
	{
		if (logger.isDebugEnabled())
			logger.debug("Removing Carrier for key: " + key.toString());

		Carrier oldCarrier = (Carrier) key2Carrier.remove(key);
		Set<Object> objectIDs;
		if (oldCarrier == null) {
			objectIDs = new HashSet<Object>();
			objectIDs.add(key.getObjectID());
		}
		else {
			try {
				objectIDs = oldCarrier.getObjectIDs();
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		for (Iterator it = objectIDs.iterator(); it.hasNext(); ) {
			Object objectID = it.next(); // key.getObjectID();

			Set<Key> keySet = objectID2KeySet_dependency.get(objectID);
			if (keySet != null) {
				keySet.remove(key);

				if (keySet.isEmpty())
					objectID2KeySet_dependency.remove(objectID);
			}
		}

		Set<Key> keySet = objectID2KeySet_alternative.get(key.getObjectID());
		if (keySet != null) {
			keySet.remove(key);

			if (keySet.isEmpty())
				objectID2KeySet_alternative.remove(key);
		}

		if (oldCarrier != null)
			oldCarrier.setCarrierContainer(null);
	}

	public synchronized Set<Key> removeByObjectIDClass(Class clazz)
	{
		// TODO we need an index for this feature!!! Iterating all objectIDs is too inefficient!
		Set<Object> objectIDsToRemove = new HashSet<Object>();
		for (Object objectID : objectID2KeySet_dependency.keySet()) {
			if (clazz.isInstance(objectID))
				objectIDsToRemove.add(objectID);
		}

		Set<Key> res = new HashSet<Key>();
		for (Object objectID : objectIDsToRemove)
			res.addAll(removeByObjectID(objectID));

		return res;
	}

	public synchronized void removeAll()
	{
		carrierContainers.clear();
		rollCarrierContainers();
		key2Carrier.clear();
		objectID2KeySet_dependency.clear();
		objectID2KeySet_alternative.clear();
		// important: we do NOT clear the newCarriersByKey in order to have the listeners registered, still.
	}

//	public synchronized void populateDependentObjectIDs(Object objectID, Set<Object> dependentObjectIDs)
//	{
//		Set<Key> keySet = objectID2KeySet_dependency.remove(objectID);
//		if (keySet == null)
//			return;
//
//		for (Key key : keySet)
//			dependentObjectIDs.add(key.getObjectID());
//	}

	private static final Set<Key> emptyKeySet = Collections.unmodifiableSet(new HashSet<Key>(0));

	/**
	 * This method is called by the <code>NotificationThread</code>, if an object has
	 * been changed. It will
	 * remove all cached instances (all scopes & all fetch-groups) of the
	 * JDO object which is referenced by the ID <code>objectID</code>. It will NOT fire
	 * any local notifications! This is done by the <code>NotificationThread</code>.
	 * It will NOT deregister any server-sided listeners, either. This is done by
	 * the <code>NotificationThread</code>, too.
	 *
	 * @param objectID The JDO object-id of the persistance-capable object.
	 * @return a Set of all removed {@link Key}s - never null
	 */
	public synchronized Set<Key> removeByObjectID(Object objectID)
	{
		logger.debug("Removing all Carriers for objectID: " + objectID);

		objectID2KeySet_alternative.remove(objectID);

		Set<Key> keySet = objectID2KeySet_dependency.remove(objectID);
		if (keySet == null)
			return emptyKeySet;

		int removedCarrierCount = 0;
		for (Iterator it = keySet.iterator(); it.hasNext(); ) {
			Key key = (Key) it.next();
			Carrier carrier = (Carrier) key2Carrier.remove(key);
			if (carrier != null) {
				if (logger.isDebugEnabled())
					logger.debug("Removing Carrier: key=\""+key.toString()+"\"");

				carrier.setCarrierContainer(null);
				removedCarrierCount++;
			}
			else
				logger.warn("There was a key in the objectID2KeySet_dependency, but no carrier for it in key2Carrier! key=\""+key.toString()+"\"");
		}
		return keySet;
	}

	public synchronized boolean isOpen()
	{
		return sessionID != null;
	}

	public synchronized void assertOpen()
	{
		if (sessionID == null)
			throw new IllegalStateException("Cache is currently not open!");
	}

	/**
	 * This method opens the cache for a given sessionID. If the cache is currently open
	 * for another session, it will be closed and reopened. If it is already open for the
	 * same sessionID, this method returns without any action.
	 *
	 * @param sessionID A unique ID for the current session.
	 */
	public synchronized void open(String sessionID)
	{
		if (this.sessionID != null) {
			if (this.sessionID.equals(sessionID))
				return;

			close();
		}

		if (sessionID == null || sessionID.length() < 2)
			throw new IllegalArgumentException("sessionID must be a String with at least 2 characters!");

		JDOLifecycleManager.sharedInstance(); // force this to be initialised :-)

		this.sessionID = sessionID;
		this.cacheManagerThread = new CacheManagerThread(this);
		this.notificationThread = new NotificationThread(this);
	}

	public synchronized void close()
	{
		try {
			JDOManager jm = JDOManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();

			// remove all listeners for this session - done by remote closeCacheSession(...)
			jm.closeCacheSession();
		} catch (Exception x) {
			logger.warn("Closing CacheSession on server failed!", x); // the server closes cacheSessions after a certain expiry time anyway
		}

		// stop the threads
		cacheManagerThread.interrupt();
		cacheManagerThread = null;
		notificationThread.interrupt();
		notificationThread = null;

		// clear the cache
		removeAll();

		// forget the sessionID - a new one will automatically be generated
		sessionID = null;
	}
}
