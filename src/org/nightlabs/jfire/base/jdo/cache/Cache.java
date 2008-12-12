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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.jdo.JDOHelper;

import org.apache.log4j.Logger;
import org.nightlabs.config.Config;
import org.nightlabs.config.ConfigException;
import org.nightlabs.environment.Environment;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.jdo.JDOObjectID2PCClassMap;
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
import org.nightlabs.util.Util;

/**
 * This cache was designed to hold JFire JDO objects on the client side.
 * <p>
 * It stores the JDO objects by their object-id and the fetch-groups
 * used to detach the objects. Additionally, a scope can be used in case
 * the fetch-groups do not sufficiently define how the object was detached
 * (for example they might be detached without detach-unload-fields and with
 * manually accessing fields before detachment).
 * </p>
 * <p>
 * When the cache is asked for an object with a certain set of fetch-groups
 * an object from the cache is returned, if it's found to have been detached
 * with the same or more fetch-groups. This behaviour is configured via
 * {@link CacheCfMod#getExactFetchGroupsOnly()} and should be deactivated
 * during development in order to prevent Heisenbugs.
 * </p>
 * A singleton of this class caches (possibly) all JDO objects
 * in the client. To use it, you should implement your own
 * {@link org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO}.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class Cache
{
//	private static final boolean DEBUG_TEST = true;

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(Cache.class);

	private static Cache _sharedInstance = null;

	private NotificationThread notificationThread;
	private JDOLifecycleManager _jdoLifecycleManager;
	protected JDOLifecycleManager getJDOLifecycleManager() {
		if (_jdoLifecycleManager == null)
			throw new IllegalStateException("No JDOLifecycleManager assigned!");

		return _jdoLifecycleManager;
	}

	protected static class NotificationThread extends Thread
	{
		private static volatile int nextID = 0;

		private Cache cache;

		public NotificationThread(Cache cache)
		{
			this.cache = cache;
			setName("Cache.NotificationThread-" + (nextID++));
			setDaemon(true);
			setPriority(Thread.NORM_PRIORITY);
			start();
		}

		@Override
		public void run()
		{
			long lastErrorDT = 0;
			JDOManager jdoManager = null;

//			// WORKAROUND: For classloading deadlock (java/osgi bug - give the Login some time to do its own classloading necessary due to LoginStateListeners being triggered)
//			logger.info("NotificationThread.run: WORKAROUND for java classloading bug: delaying start of NotificationThread...");
//			try {
//				Thread.sleep(10000);
//			} catch (InterruptedException e1) {
//				// ignore silently
//			}
//			logger.info("NotificationThread.run: WORKAROUND for java classloading bug: delayed start of NotificationThread - continuing now!");

//			if (logger.isDebugEnabled())
//				logger.debug("NotificationThread.run: DEBUG_TEST=" + DEBUG_TEST);

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
							logger.debug("NotificationThread.run: Received notification for implicit listeners with " + dirtyObjectIDs.size() + " DirtyObjectIDs.");
							if (logger.isTraceEnabled()) {
								for (DirtyObjectID dirtyObjectID : dirtyObjectIDs)
									logger.trace("NotificationThread.run:   " + dirtyObjectID);
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

								Set<Key> removedKeys = cache.removeByObjectID(objectID, true);
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
									Object removedKeyObjectID = removedKey.getObjectID();
									DirtyObjectID doid = createSyntheticDirtyObjectID(removedKeyObjectID);
									indirectlyAffectedDirtyObjectIDs.put(doid.getObjectID(), doid);

									if (logger.isDebugEnabled())
										logger.debug("NotificationThread.run:   created synthetic \"" + doid + "\" for real \"" + objectID + "\"");
								}
							}
							if (logger.isDebugEnabled())
								logger.info("NotificationThread.run: Removed " + removedCarrierCount + " carriers from the cache.");

							cache.unsubscribeObjectIDs(
									objectIDs,
									cache.getCacheCfMod().getLocalListenerReactionTimeMSec());

							// remove all synthetic DirtyObjectIDs for which real dirtyObjectIDs exist
							for (Object objectID : objectIDsWithLifecycleStageDirty)
								indirectlyAffectedDirtyObjectIDs.remove(objectID);


							// create a set with all dirtyObjectIDs - the direct and the indirect=synthetic
							SortedSet<DirtyObjectID> dirtyObjectIDsForNotification = new TreeSet<DirtyObjectID>(dirtyObjectIDs);
							dirtyObjectIDsForNotification.addAll(indirectlyAffectedDirtyObjectIDs.values());

							if (logger.isDebugEnabled()) {
								logger.debug("NotificationThread.run: about to notify implicit listeners for " + dirtyObjectIDsForNotification.size() + " DirtyObjectIDs:");
								if (logger.isTraceEnabled())
								{
									for (DirtyObjectID dirtyObjectID : dirtyObjectIDsForNotification)
										logger.trace("NotificationThread.run:   * " + dirtyObjectID);
								}
							}

							// notify via local class based notification mechanism
							// the interceptor org.nightlabs.jfire.base.jdo.JDOObjectID2PCClassNotificationInterceptor takes care about correct class mapping
							cache.getJDOLifecycleManager().notify(new NotificationEvent(
									cache,             // source
									(String)null,     // zone
									dirtyObjectIDsForNotification // dirtyObjectIDs    // subjects
							));

							// TODO the following is only for legacy (i.e. deprecated ChangeManager)
//							ArrayList<ChangeSubjectCarrier> subjectCarriers = new ArrayList<ChangeSubjectCarrier>(dirtyObjectIDs.size());
//							for (Iterator it = dirtyObjectIDs.iterator(); it.hasNext(); ) {

//							ArrayList<ChangeSubjectCarrier> subjectCarriers = new ArrayList<ChangeSubjectCarrier>(dirtyObjectIDsForNotification.size());
//							for (Iterator<DirtyObjectID> it = dirtyObjectIDsForNotification.iterator(); it.hasNext(); ) {
//								DirtyObjectID dirtyObjectID = it.next();
//								// ignore removal, because that's not supported by the old ChangeManager - new shouldn't be in that list
//								if (dirtyObjectID.getLifecycleState() != JDOLifecycleState.DIRTY)
//									continue;
//
//								subjectCarriers.add(new ChangeSubjectCarrier(
//										dirtyObjectID.getSourceSessionIDs(),
//										dirtyObjectID.getObjectID()));
//							}

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
							if (logger.isDebugEnabled())
								logger.info("NotificationThread.run: Received notification for " + filterID2DirtyObjectIDs.size() + " explicit listeners.");
							for (Map.Entry<Long, SortedSet<DirtyObjectID>> me : filterID2DirtyObjectIDs.entrySet()) {
								Long filterID = me.getKey();
								SortedSet<DirtyObjectID> dirtyObjectIDs = me.getValue();

								cache.getJDOLifecycleManager().notify(filterID, new JDOLifecycleEvent(cache, dirtyObjectIDs));
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
			setDaemon(true);
			setPriority(Thread.NORM_PRIORITY);
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

					// *** dependency container management below ***
					if (System.currentTimeMillis() - cache.objectID2KeySet_dependency_removed_active_createTimestamp > cache.getCacheCfMod().getOldGraphDependencyContainerActivityMSec()) {
						cache.roll_objectID2KeySet_dependency_removed_history();
					}
					// *** dependency container management above ***

					// *** listener management below ***
//					// fill in the object graph for all new objects - we do this synchronous now.
//					Map<Key, Carrier> newCarriersByKey = cache.fetchNewCarriersByKey();
//					if (newCarriersByKey != null) {
//						for (Map.Entry<Key, Carrier> me : newCarriersByKey.entrySet()) {
//							Key key = me.getKey();
//							Carrier carrier = me.getValue();
//
//							Set objectIDs = carrier.getObjectIDs();
//							cache.mapObjectIDs2Key(objectIDs, key);
//							cache.subscribeObjectIDs(objectIDs, 0);
//						}
//					} // if (newCarriersByKey != null) {

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
						for (Iterator<Entry<Object, SubscriptionChangeRequest>> it = subscriptionChanges.entrySet().iterator(); it.hasNext(); ) {
							Map.Entry<Object, SubscriptionChangeRequest> me = it.next();
							Object objectID = me.getKey();
							SubscriptionChangeRequest scr = me.getValue();

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
							if (logger.isDebugEnabled())
								logger.debug("Synchronizing all listeners.");

							cache.setResubscribeAllListeners(false);
							boolean resubscribeFailed = true;
							try {
								if (isInterrupted())
									return;

								jdoManager.resubscribeAllListeners(
										currentlySubscribedObjectIDs,
										cache.getJDOLifecycleManager().getLifecycleListenerFilters());
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

								if (logger.isDebugEnabled())
									logger.debug(
										"Adding " +
										(objectIDsToSubscribe == null ? 0 : objectIDsToSubscribe.size()) +
										" and removing " +
										(objectIDsToUnsubscribe == null ? 0 : objectIDsToUnsubscribe.size()) +
										" implicit remote listeners. Adding " +
										(filtersToSubscribe == null ? 0 : filtersToSubscribe.size()) +
										" and removing " +
										(filterIDsToUnsubscribe == null ? 0 : filterIDsToUnsubscribe.size()) +
										" filters for explicit listeners.");

								if (logger.isTraceEnabled()) {
									logger.trace("Change listeners for the following ObjectIDs will be removed:");
									if (objectIDsToUnsubscribe == null)
										logger.trace("      NONE!");
									else {
										for (Iterator<?> it = objectIDsToUnsubscribe.iterator(); it.hasNext(); )
											logger.trace("      " +  it.next());
									}

									logger.trace("Change listeners for the following ObjectIDs will be added:");
									if (objectIDsToSubscribe == null)
										logger.trace("      NONE!");
									else {
										for (Iterator<?> it = objectIDsToSubscribe.iterator(); it.hasNext(); )
											logger.trace("      " +  it.next());
									}

									logger.trace("LifecycleListenerFilters with the following IDs will be removed:");
									if (filterIDsToUnsubscribe == null)
										logger.trace("      NONE!");
									else {
										for (Long filterID : filterIDsToUnsubscribe)
											logger.trace("      " +  filterID);
									}

									logger.trace("LifecycleListenerFilters will be added:");
									if (filtersToSubscribe == null)
										logger.trace("      NONE!");
									else {
										for (IJDOLifecycleListenerFilter filter : filtersToSubscribe) {
											logger.trace("      id=" +  filter.getFilterID().getFilterID() + " filter=" + filter);
										}
									}
								}

								if (isInterrupted())
									return;

								jdoManager.removeOrAddListeners(
										objectIDsToUnsubscribe,
										objectIDsToSubscribe,
										filterIDsToUnsubscribe,
										filtersToSubscribe);
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
			for (Iterator<SubscriptionChangeRequest> it = oldChangeRequests.values().iterator(); it.hasNext(); ) {
				SubscriptionChangeRequest scr = it.next();
				logger.debug("      " + scr);
			}
		}

		synchronized (subscriptionChangeRequestsMutex) {
			if (logger.isDebugEnabled()) {
				if (subscriptionChangeRequests.isEmpty())
					logger.debug("There are no new subscription change requests to merge into the old ones. Simply replacing them.");
				else {
					logger.debug("There are new subscription change requests which will be merged into the old ones:");
					for (Iterator<SubscriptionChangeRequest> it = subscriptionChangeRequests.values().iterator(); it.hasNext(); ) {
						SubscriptionChangeRequest scr = it.next();
						logger.debug("      " + scr);
					}
				}
			}

			oldChangeRequests.putAll(subscriptionChangeRequests);
			subscriptionChangeRequests = oldChangeRequests;

			if (logger.isDebugEnabled()) {
				logger.debug("These are the subscription change requests after restore:");
				for (Iterator<SubscriptionChangeRequest> it = subscriptionChangeRequests.values().iterator(); it.hasNext(); ) {
					SubscriptionChangeRequest scr = it.next();
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
							null, // we have no JDO object-id
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
							null, // the objectID
							null, // filter, // we better don't pass filter anymore in order to ensure noone accesses this objec anymore
							delayMSec));
		}
	}

	/**
	 * This method subscribes the given <code>objectIDs</code> asynchronously
	 * via the {@link CacheManagerThread} (which calls
	 * {@link JDOManager#addChangeListeners(java.lang.String, java.util.Collection)}).
	 */
	protected void subscribeObjectIDs(Collection<?> objectIDs, long delayMSec)
	{
		synchronized (subscriptionChangeRequestsMutex) {
			for (Iterator<?> it = objectIDs.iterator(); it.hasNext(); ) {
				Object objectID = it.next();

				if (!(objectID instanceof ObjectID)) // we do not subscribe listeners to non-jdo keys
					continue;

				SubscriptionChangeRequest scr = subscriptionChangeRequests.get(objectID);
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
								null, // no filter
								delayMSec));
			}
		}
	}

	/**
	 * This method unsubscribes the given <code>objectIDs</code> asynchronously
	 * via the {@link CacheManagerThread} (which calls
	 * {@link JDOManager#removeChangeListeners(java.lang.String, java.util.Collection)}).
	 */
	protected void unsubscribeObjectIDs(Collection<?> objectIDs, long delayMSec)
	{
		synchronized (subscriptionChangeRequestsMutex) {
			for (Iterator<?> it = objectIDs.iterator(); it.hasNext(); ) {
				Object objectID = it.next();

				SubscriptionChangeRequest scr = subscriptionChangeRequests.get(objectID);
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
								null, // no filter
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
		logger.info("setServerMode: serverMode="+serverMode);

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
	public static Cache sharedInstance()
	{
		synchronized (Cache.class) { // we synchronise both sharedInstance-methods (of JDOLifecycleManager and Cache) via the same mutex in order to prevent dead-locks
			try {
				if (serverMode) {
					if (serverModeSharedInstances == null) {
						serverModeSharedInstances = new HashMap<String, Cache>();
						JDOLifecycleManager.setServerMode(true);
					}

					String userName = getCurrentUserName();
					Cache cache = serverModeSharedInstances.get(userName);
					if (cache == null) {
						logger.info("sharedInstance: creating new Cache instance in serverMode");
						cache = new Cache();
						serverModeSharedInstances.put(userName, cache);
						cache.open(getCurrentSessionID());
					}
					return cache;
				}
				else {
					if (_sharedInstance == null) {
						logger.info("sharedInstance: creating new Cache instance in clientMode (not-serverMode)");
						_sharedInstance = new Cache();
						if (autoOpen)
							_sharedInstance.open(getCurrentSessionID());
					}

					return _sharedInstance;
				}
			} catch (ConfigException e) {
				throw new RuntimeException(e);
			}
		} // synchronized (Cache.class) {
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
	 * This LinkedList of synthetic objectIDs stores temporarily all entries that are deleted from
	 * {@link #objectID2KeySet_dependency}. Since they are still needed a while, they will be kept
	 * in this <code>LinkedList</code>. The length of this list is limited and every minute (configurable via
	 * {@link CacheCfMod#getOldGraphDependencyContainerActivityMSec()} and {@link CacheCfMod#getOldGraphDependencyContainerCount()}),
	 * a new <code>Map</code> is created and added to the beginning of the list, while all old <code>Map</code>s
	 * are deleted from the end of the list.
	 *
	 * see https://www.jfire.org/modules/bugs/view.php?id=84
	 */
	private LinkedList<Map<Object, Set<Key>>> objectID2KeySet_dependency_removed_history = new LinkedList<Map<Object,Set<Key>>>();
	private Map<Object, Set<Key>> objectID2KeySet_dependency_removed_active = null;
	private long objectID2KeySet_dependency_removed_active_createTimestamp = System.currentTimeMillis();

	/**
	 * In contrast to {@link #objectID2KeySet_dependency}, this map references only those {@link Key}s,
	 * that point to the same objectID. And this map is only used for scope <code>null</code>.
	 */
	private Map<Object, Set<Key>> objectID2KeySet_alternative = new HashMap<Object, Set<Key>>();

//	/**
//	 * When a new object is put into the Cache, it is immediately registered in
//	 * {@link #key2Carrier}, {@link #objectID2KeySet_dependency} and here. In {@link #objectID2KeySet_dependency}
//	 * however, there is only the main <code>objectID</code> registered immediately.
//	 * All contained objects within the object graph are added later in the
//	 * {@link CacheManagerThread}. This thread replaces this Map using
//	 * {@link #fetchNewCarriersByKey()}.
//	 *
//	 * key: {@link Key} key<br/>
//	 * value: {@link Carrier} carrier
//	 */
//	private Map<Key, Carrier> newCarriersByKey = new HashMap<Key, Carrier>();

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

//	/**
//	 * @return If the Map is empty, it returns <code>null</code>,
//	 *		otherwise it replaces the current map by a new one and
//	 *		returns it.
//	 */
//	protected synchronized Map<Key, Carrier> fetchNewCarriersByKey()
//	{
//		if (newCarriersByKey.isEmpty())
//			return null;
//
//		Map<Key, Carrier> res = newCarriersByKey;
//		newCarriersByKey = new HashMap<Key, Carrier>();
//		return res;
//	}

	protected synchronized void rollCarrierContainers()
	{
		if (logger.isDebugEnabled())
			logger.debug("Creating new activeCarrierContainer.");
		CarrierContainer newActiveCC = new CarrierContainer(this);
		carrierContainers.addFirst(newActiveCC);
		activeCarrierContainer = newActiveCC;

		long carrierContainerCount = getCacheCfMod().getCarrierContainerCount();

		if (carrierContainerCount < 2)
			throw new IllegalStateException("carrierContainerCount = "+carrierContainerCount+" but must be at least 2!!!");

		while (carrierContainers.size() > carrierContainerCount) {
			CarrierContainer cc = carrierContainers.removeLast();
			if (logger.isDebugEnabled())
				logger.debug("Dropping carrierContainer (created " + cc.getCreateDT() + ")");
			cc.close();
		}
	}

	protected synchronized void roll_objectID2KeySet_dependency_removed_history()
	{
		objectID2KeySet_dependency_removed_active = new HashMap<Object, Set<Key>>();
		objectID2KeySet_dependency_removed_active_createTimestamp = System.currentTimeMillis();
		objectID2KeySet_dependency_removed_history.addFirst(objectID2KeySet_dependency_removed_active);

		long oldGraphDependencyContainerCount = cacheCfMod.getOldGraphDependencyContainerCount();

		if (oldGraphDependencyContainerCount < 3)
			throw new IllegalStateException("carrierContainerCount = "+oldGraphDependencyContainerCount+" but must be at least 3!!!");

		int removedContainerCount = 0; long removedObjectIDCount = 0;
		while (objectID2KeySet_dependency_removed_history.size() > oldGraphDependencyContainerCount) {
			removedObjectIDCount += objectID2KeySet_dependency_removed_history.removeLast().size();
			++removedContainerCount;
		}

		if (logger.isInfoEnabled())
			logger.info("roll_objectID2KeySet_dependency_removed_history: created new container and removed " + removedContainerCount + " with " + removedObjectIDCount + " dependency records.");
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
		cacheCfMod = Config.sharedInstance().createConfigModule(CacheCfMod.class);
		Config.sharedInstance().save(); // TODO remove this as soon as we have a thread that periodically saves it.

		// set initial active containers
		rollCarrierContainers();
		roll_objectID2KeySet_dependency_removed_history();
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
	 * <p>
	 * <b><u>Important:</u> Never modify an object that is managed by the cache!</b> Always copy it (using {@link Util#cloneSerializable(Object)})
	 * and modify the clone!!!
	 * </p>
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
	 * <p>
	 * <b><u>Important:</u> Never modify an object that is managed by the cache!</b> Always copy it (using {@link Util#cloneSerializable(Object)})
	 * and modify the clone!!!
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
		Carrier carrier = key2Carrier.get(key);
		if (carrier == null) {
			if (logger.isDebugEnabled())
				logger.debug("No Carrier found for key: " + key.toString());

			// If and only if the scope is null, we search for a record that contains
			// AT LEAST our required fetch groups and AT LEAST our maxFetchDepth.
			if (scope == null) {
				boolean exactFetchGroupsOnly;
				//check the environment variable if it's for productive or devolopment purpose.
				if (Environment.getEnvironment() == Environment.productive)
					exactFetchGroupsOnly = cacheCfMod.getExactFetchGroupsOnly();
				else
					exactFetchGroupsOnly = true;

//				if (cacheCfMod.getExactFetchGroupsOnly().booleanValue()) {
				if (exactFetchGroupsOnly) {
					if (logger.isDebugEnabled())
						logger.debug("exactFetchGroupsOnly is enabled => not searching for alternative entries having more than the desired fetch-groups.");
				}
				else {
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

							carrier = key2Carrier.get(candidateKey);
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
				} // if (!"true".equals(property_CacheExactFetchGroups_value)) {
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

	/**
	 * This method puts JDO objects into the cache. Therefore, it
	 * obtains the JDO objectIDs and uses them as keys.
	 * <p>
	 * <b><u>Important:</u> Never modify an object that is managed by the cache!</b> That means,
	 * after you put an object into the cache, copy it (using {@link Util#cloneSerializable(Object)})
	 * and modify the clone!!! Alternatively, you can put a clone into the cache, of course.
	 * </p>
	 *
	 * @param scope Either <code>null</code> to indicate that the object has been fetched normally using
	 *		the fetchGroups or a String specifying a customized fetch-method. This can e.g. be the case
	 *		if the object has been detached using the detach-on-close feature. If you cache a non-jdo-object,
	 *		you should specify <code>scope</code> and pass <code>fetchGroups = null</code>.
	 * @param objects the objects to be cached. The cache will recursively scan it and register server-sided
	 *		change-listeners for all found objectIDs. If one of the objects in the graph gets changed, the
	 *		cache will forget this main-object.
	 * @param fetchGroups Either <code>null</code> or the fetchGroups with which the object has been retrieved.
	 *		If you cache a non-jdo-object, you should pass <code>null</code> here and use <code>scope</code>.
	 * @param maxFetchDepth the maximum fetch-depth that was passed to JDO when detaching the object or -1, if
	 *		the object was obtained in a "special" way indicated by <code>scope</code>.
	 */
	public void putAll(String scope, Collection<?> objects, String[] fetchGroups, int maxFetchDepth)
	{
		_putAll(scope, objects, CollectionUtil.array2HashSet(fetchGroups), maxFetchDepth);
	}
	/**
	 * This method puts JDO objects into the cache. Therefore, it
	 * obtains the JDO objectIDs and uses them as keys.
	 * <p>
	 * <b><u>Important:</u> Never modify an object that is managed by the cache!</b> That means,
	 * after you put an object into the cache, copy it (using {@link Util#cloneSerializable(Object)})
	 * and modify the clone!!! Alternatively, you can put a clone into the cache, of course.
	 * </p>
	 *
	 * @param scope Either <code>null</code> to indicate that the object has been fetched normally using
	 *		the fetchGroups or a String specifying a customized fetch-method. This can e.g. be the case
	 *		if the object has been detached using the detach-on-close feature. If you cache a non-jdo-object,
	 *		you should specify <code>scope</code> and pass <code>fetchGroups = null</code>.
	 * @param objects the objects to be cached. The cache will recursively scan it and register server-sided
	 *		change-listeners for all found objectIDs. If one of the objects in the graph gets changed, the
	 *		cache will forget this main-object.
	 * @param fetchGroups Either <code>null</code> or the fetchGroups with which the object has been retrieved.
	 *		If you cache a non-jdo-object, you should pass <code>null</code> here and use <code>scope</code>.
	 * @param maxFetchDepth the maximum fetch-depth that was passed to JDO when detaching the object or -1, if
	 *		the object was obtained in a "special" way indicated by <code>scope</code>.
	 */
	public void putAll(String scope, Collection<?> objects, Set<String> fetchGroups, int maxFetchDepth)
	{
		_putAll(
				scope, objects,
				(fetchGroups == null ? null : new HashSet<String>(fetchGroups)),
				maxFetchDepth);
	}
	protected void _putAll(String scope, Collection<?> objects, Set<String> fetchGroups, int maxFetchDepth)
	{
		if (objects == null)
			throw new NullPointerException("objects must not be null!");

		for (Iterator<?> it = objects.iterator(); it.hasNext(); ) {
			Object object = it.next();
			_put(scope, JDOHelper.getObjectId(object), object, fetchGroups, maxFetchDepth);
		}
	}

	/**
	 * This method calls {@link #put(String, Object, Set)}.
	 * <p>
	 * <b><u>Important:</u> Never modify an object that is managed by the cache!</b> That means,
	 * after you put the object into the cache, copy it (using {@link Util#cloneSerializable(Object)})
	 * and modify the clone!!! Alternatively, you can put a clone into the cache, of course.
	 * </p>
	 */
	public void put(String scope, Object object, String[] fetchGroups, int maxFetchDepth)
	{
		_put(scope, JDOHelper.getObjectId(object), object, CollectionUtil.array2HashSet(fetchGroups), maxFetchDepth);
	}

	/**
	 * This method puts a JDO object into the cache. Therefore, it
	 * obtains the JDO objectID and uses it as key.
	 * See {@link #put(String, Object, Object, Set, int)} for detailed documentation.
	 * <p>
	 * <b><u>Important:</u> Never modify an object that is managed by the cache!</b> That means,
	 * after you put the object into the cache, copy it (using {@link Util#cloneSerializable(Object)})
	 * and modify the clone!!! Alternatively, you can put a clone into the cache, of course.
	 * </p>
	 */
	public void put(String scope, Object object, Set<String> fetchGroups, int maxFetchDepth)
	{
		_put(
				scope, JDOHelper.getObjectId(object), object,
				(fetchGroups == null ? null : new HashSet<String>(fetchGroups)),
				maxFetchDepth);
	}

	/**
	 * This method puts a JDO object into the cache. Therefore, it
	 * obtains the JDO objectID and uses it as key.
	 * See {@link #put(String, Object, Object, Set, int)} for detailed documentation.
	 * <p>
	 * <b><u>Important:</u> Never modify an object that is managed by the cache!</b> That means,
	 * after you put the object into the cache, copy it (using {@link Util#cloneSerializable(Object)})
	 * and modify the clone!!! Alternatively, you can put a clone into the cache, of course.
	 * </p>
	 */
	public void put(String scope, Object objectID, Object object, String[] fetchGroups, int maxFetchDepth)
	{
		_put(scope, objectID, object, CollectionUtil.array2HashSet(fetchGroups), maxFetchDepth);
	}

	/**
	 * You can put non-jdo objects into the cache using this method. If they contain jdo-objects
	 * in their object graph, the cache will however register listeners for these contained objects.
	 * <p>
	 * <b><u>Important:</u> Never modify an object that is managed by the cache!</b> That means,
	 * after you put the object into the cache, copy it (using {@link Util#cloneSerializable(Object)})
	 * and modify the clone!!! Alternatively, you can put a clone into the cache, of course.
	 * </p>
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
	 * @param maxFetchDepth the maximum fetch-depth that was passed to JDO when detaching the object or -1, if
	 *		the object was obtained in a "special" way indicated by <code>scope</code>.
	 */
	public void put(String scope, Object objectID, Object object, Set<String> fetchGroups, int maxFetchDepth)
	{
		_put(
				scope, objectID, object,
				(fetchGroups == null ? null : new HashSet<String>(fetchGroups)),
				maxFetchDepth);
	}
	protected void _put(String scope, Object objectID, Object object, Set<String> fetchGroups, int maxFetchDepth)
	{
		assertOpen();

		if (objectID == null)
			throw new NullPointerException("objectID must not be null!");

		if (object == null)
			throw new NullPointerException("object must not be null!");

		JDOObjectID2PCClassMap.sharedInstance().initPersistenceCapableClass(objectID, object.getClass());

		Key key = new Key(scope, objectID, fetchGroups, maxFetchDepth);

		if (logger.isDebugEnabled())
			logger.debug("_put: Putting object into cache. key: " + key.toString());

		synchronized (this) {
			// remove the old carrier - if existing
			Carrier oldCarrier = key2Carrier.get(key);
			if (oldCarrier != null) {
				Object oldObject = oldCarrier.getObject();
				if (NLJDOHelper.compareObjectVersions(oldObject, object) <= 0) {
					if (logger.isDebugEnabled()) {
						if (logger.isTraceEnabled())
							logger.trace("_put: The old object in the cache is newer than the new object; ignoring put request. key: " + key.toString(), new Exception("StackTrace"));
						else
							logger.debug("_put: The old object in the cache is newer than the new object; ignoring put request. key: " + key.toString());
					}
					return;
				}

				if (logger.isDebugEnabled())
					logger.debug("_put: There was an old carrier for the same key in the cache; removing it. key: " + key.toString());

				oldCarrier.setCarrierContainer(null);
			}

			// the constructor of Carrier self-registers in the active CarrierContainer
			Carrier carrier = new Carrier(key, object, getActiveCarrierContainer());

			// store the new carrier in our main cache map
			key2Carrier.put(key, carrier);
//			// ...and in the newCarriers map
//			newCarriersByKey.put(key, carrier);

			// register the key by its objectID (for fast removal if the object changed)
			// note that the whole object graph is filled in later by the CacheManagerThread - no more! it's one below in this method since it's not expensive!
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

			// fill in the object graph
			{
				long start_fillObjectGraph = System.currentTimeMillis();
				Set<?> objectIDs;
				try {
					objectIDs = carrier.getObjectIDs();
				} catch (Exception e) {
					objectIDs = null;
					logger.error("carrier.getObjectIDs() failed!", e);
				}
				if (objectIDs != null) {
					this.mapObjectIDs2Key(objectIDs, key);
					this.subscribeObjectIDs(objectIDs, 0);
				}
				long duration_fillObjectGraph = System.currentTimeMillis() - start_fillObjectGraph;
				if (duration_fillObjectGraph > 100)
					logger.warn("put: fillObjectGraph took more than 100 msec! it took " + duration_fillObjectGraph + " msec! key=" + key);
			}
		} // synchronized (this) {

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
	protected synchronized void mapObjectIDs2Key(Collection<?> objectIDs, Key key)
	{
		for (Iterator<?> it = objectIDs.iterator(); it.hasNext(); ) {
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
	 * @param objectID Remove one or all dependencies for the object referenced by this objectID.
	 * @param key If <code>null</code>, all keys depending on the given <code>objectID</code> will be removed. If non-<code>null</code>, only this one
	 *		key will be removed.
	 */
	protected synchronized void removeDependency(Object objectID, Key key)
	{
		if (objectID == null)
			throw new IllegalArgumentException("objectID must not be null!");

		// key can be null => no check

		Set<Key> keySet = objectID2KeySet_dependency.get(objectID);
		if (keySet != null) {
			Set<Key> keySetRemoved = objectID2KeySet_dependency_removed_active.get(objectID);
			if (keySetRemoved == null) {
				keySetRemoved = new HashSet<Key>();
				objectID2KeySet_dependency_removed_active.put(objectID, keySetRemoved);
			}

			if (key == null) {
				keySetRemoved.addAll(keySet);
				objectID2KeySet_dependency.remove(objectID);
			}
			else {
				keySetRemoved.add(key);
				keySet.remove(key);
				if (keySet.isEmpty())
					objectID2KeySet_dependency.remove(objectID);
			}
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

		Carrier oldCarrier = key2Carrier.remove(key);
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

		for (Iterator<?> it = objectIDs.iterator(); it.hasNext(); ) {
			Object objectID = it.next(); // key.getObjectID();
			removeDependency(objectID, key);
//			Set<Key> keySet = objectID2KeySet_dependency.get(objectID);
//			if (keySet != null) {
//				keySet.remove(key);
//
//				if (!DEBUG_TEST) {
//					if (keySet.isEmpty())
//						objectID2KeySet_dependency.remove(objectID);
//				}
//			}
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

	/**
	 * This method calls {@link #removeByObjectIDClass(Class, boolean)} with <code>returnRemovedDependencies == false</code>,
	 * thus only returning really deleted <code>Key</code>s - no history entries.
	 */
	public synchronized Set<Key> removeByObjectIDClass(Class<?> clazz)
	{
		return removeByObjectIDClass(clazz, false);
	}
	public synchronized Set<Key> removeByObjectIDClass(Class<?> clazz, boolean returnRemovedDependencies)
	{
		// TODO we need an index for this feature!!! Iterating all objectIDs is too inefficient!
		Set<Object> objectIDsToRemove = new HashSet<Object>();
		for (Object objectID : objectID2KeySet_dependency.keySet()) {
			if (clazz.isInstance(objectID))
				objectIDsToRemove.add(objectID);
		}

		Set<Key> res = new HashSet<Key>();
		for (Object objectID : objectIDsToRemove)
			res.addAll(removeByObjectID(objectID, returnRemovedDependencies));

		return res;
	}

	public synchronized void removeAll()
	{
		carrierContainers.clear();
		rollCarrierContainers();
		objectID2KeySet_dependency_removed_history.clear();
		roll_objectID2KeySet_dependency_removed_history();
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
	public synchronized Set<Key> removeByObjectID(Object objectID, boolean returnRemovedDependencies)
	{
		if (logger.isDebugEnabled())
			logger.debug("removeByObjectID: Removing all Carriers for: " + objectID);

		objectID2KeySet_alternative.remove(objectID);

		Set<Key> res = null;
		Set<Key> keySet = objectID2KeySet_dependency.get(objectID);
		if (keySet != null)
			res = new HashSet<Key>(keySet);

//		Set<Key> keySet;
//		if (DEBUG_TEST)
//			keySet = objectID2KeySet_dependency.get(objectID);
//		else
//			keySet = objectID2KeySet_dependency.remove(objectID);

		if (returnRemovedDependencies) {
			for (Map<Object, Set<Key>> objectID2KeySet_dependency_removed : objectID2KeySet_dependency_removed_history) {
				Set<Key> keySetRemoved = objectID2KeySet_dependency_removed.get(objectID);
				if (keySetRemoved != null) {
					if (res == null)
						res = new HashSet<Key>(keySetRemoved);
					else
						res.addAll(keySetRemoved);
				}
			}
		} // if (returnRemovedDependencies) {

		if (res == null) {
			if (logger.isDebugEnabled())
				logger.debug("removeByObjectID: No entry in objectID2KeySet_dependency for: " + objectID);

			return emptyKeySet;
		}

		if (keySet != null) {
			for (Iterator<?> it = keySet.iterator(); it.hasNext(); ) {
				Key key = (Key) it.next();
				Carrier carrier = key2Carrier.remove(key);
				if (carrier != null) {
					if (logger.isDebugEnabled())
						logger.debug("removeByObjectID: Removing Carrier: key=\"" + key + "\"");

					carrier.setCarrierContainer(null);
				}
				else {
					if (logger.isDebugEnabled())
						logger.debug("removeByObjectID: There was a key in the objectID2KeySet_dependency, but no carrier for it in key2Carrier! key=\"" + key + "\"");
				}
			}
		}

		removeDependency(objectID, null);

		return res;
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

		this.sessionID = sessionID;

		this._jdoLifecycleManager = JDOLifecycleManager.sharedInstance(); // force this to be initialised :-)

		this.cacheManagerThread = new CacheManagerThread(this);
		this.notificationThread = new NotificationThread(this);
	}

	public synchronized void close()
	{
		// stop the threads
		if (cacheManagerThread != null) {
			cacheManagerThread.interrupt();
			cacheManagerThread = null;
		}
		if (notificationThread != null) {
			notificationThread.interrupt();
			notificationThread = null;
		}

		try {
			JDOManager jm = JDOManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();

			// remove all listeners for this session - done by remote closeCacheSession(...)
			jm.closeCacheSession();
		} catch (Exception x) {
			logger.warn("Closing CacheSession on server failed!", x); // the server closes cacheSessions after a certain expiry time anyway
		}

		// clear the cache
		removeAll();

		// forget the sessionID - a new one will automatically be generated
		sessionID = null;
	}

	public void refreshAll() {
		Set<Object> objectIDs = new HashSet<Object>();
		final List<DirtyObjectID> dirtyObjectIDs = new ArrayList<DirtyObjectID>();
		synchronized(this) {
			for (Object objectID : objectID2KeySet_dependency.keySet()) {
				if (objectIDs.add(objectID))
					dirtyObjectIDs.add(createSyntheticDirtyObjectID(objectID));
			}

			for (Map<Object, Set<Key>> m : objectID2KeySet_dependency_removed_history) {
				for (Object objectID : m.keySet()) {
					if (objectIDs.add(objectID))
						dirtyObjectIDs.add(createSyntheticDirtyObjectID(objectID));
				}
			}

			for (Key key : key2Carrier.keySet()) {
				Object objectID = key.getObjectID();
				if (objectIDs.add(objectID))
					dirtyObjectIDs.add(createSyntheticDirtyObjectID(objectID));
			}

			removeAll();
		} // synchronized(this) {

		if (!dirtyObjectIDs.isEmpty()) {
			Thread notificationThread = new Thread() {
				@Override
				public void run() {
					try {
						// notify via local class based notification mechanism
						// the interceptor org.nightlabs.jfire.base.jdo.JDOObjectID2PCClassNotificationInterceptor takes care about correct class mapping
						getJDOLifecycleManager().notify(new NotificationEvent(
								this,             // source
								(String)null,     // zone
								dirtyObjectIDs    // subjects
						));
					} catch (Throwable t) {
						logger.error("refreshAll: notificationThread.run: " + t.getClass() + ": " + t.getMessage(), t);
					}
				}
			};
			notificationThread.start();
		}
	}

	private static DirtyObjectID createSyntheticDirtyObjectID(Object objectID)
	{
		String jdoObjectClassName = null;
		if (objectID instanceof ObjectID)
			jdoObjectClassName = JDOObjectID2PCClassMap.sharedInstance().getPersistenceCapableClass(objectID).getName(); // TODO can't this line cause problems if it's not a JDOObjectID?

		DirtyObjectID doid = new DirtyObjectID(
				JDOLifecycleState.DIRTY, objectID,
//										objectClass.getName(),
				jdoObjectClassName,
				null, -Long.MAX_VALUE);
		return doid;
	}
}
