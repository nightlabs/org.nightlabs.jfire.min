/*
 * Created on Jul 24, 2005
 */
package org.nightlabs.jfire.base.jdo.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.apache.log4j.Logger;

import org.nightlabs.ModuleException;
import org.nightlabs.config.Config;
import org.nightlabs.config.ConfigException;
import org.nightlabs.jfire.base.jdo.notification.ChangeEvent;
import org.nightlabs.jfire.base.jdo.notification.ChangeManager;
import org.nightlabs.jfire.base.jdo.notification.ChangeSubjectCarrier;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.jdo.JDOManager;
import org.nightlabs.jfire.jdo.JDOManagerUtil;
import org.nightlabs.jfire.jdo.cache.DirtyObjectID;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.util.Utils;

/**
 * A singleton of this class caches <b>all</b> JDO objects 
 * in the client. To use it, you should implement your own
 * {@link org.nightlabs.jfire.base.jdo.JDOObjectProvider}.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class Cache
{
	public static Logger LOGGER = Logger.getLogger(Cache.class);

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

		/**
		 * @see java.lang.Thread#run()
		 */
		public void run()
		{
			long lastErrorDT = 0;
			JDOManager jdoManager = null;

			while (!isInterrupted()) {
				try {
					if (jdoManager == null)
						jdoManager = JDOManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();

					Collection dirtyObjectIDs = jdoManager.waitForChanges(
							cache.getCacheCfMod().getWaitForChangesTimeoutMSec());

					if (dirtyObjectIDs != null) {
						LOGGER.info("Received change notification with " + dirtyObjectIDs.size() + " objectIDs.");
						int removedCarrierCount = 0;
						Set objectIDs = new HashSet(dirtyObjectIDs.size());
						for (Iterator it = dirtyObjectIDs.iterator(); it.hasNext(); ) {
							DirtyObjectID dirtyObjectID = (DirtyObjectID)it.next();
							objectIDs.add(dirtyObjectID.getObjectID());
							removedCarrierCount += cache.remove((ObjectID)dirtyObjectID.getObjectID());
						}
						LOGGER.info("Removed " + removedCarrierCount + " carriers from the cache.");

						cache.unsubscribeObjectIDs(
								objectIDs,
								cache.getCacheCfMod().getLocalListenerReactionTimeMSec());

						// notify via local class based notification mechanism
						ArrayList subjectCarriers = new ArrayList(dirtyObjectIDs.size());
						for (Iterator it = dirtyObjectIDs.iterator(); it.hasNext(); ) {
							DirtyObjectID dirtyObjectID = (DirtyObjectID) it.next();
							subjectCarriers.add(new ChangeSubjectCarrier(
									dirtyObjectID.getSourceSessionIDs(),
									dirtyObjectID.getObjectID()));
						}

						ChangeManager.sharedInstance().notify(
								new ChangeEvent(
										this,             // source
										(String)null,     // zone
										null,             // subjects
										null,             // subjectClasses
										subjectCarriers)); // subjectCarriers
					} // if (changeObjectIDs != null) {

				} catch (Throwable t) {
					LOGGER.error("Error in NotificationThread!", t);
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
		public void interrupt()
		{
			terminated = true;
			super.interrupt();
		}
	}

	private CacheManagerThread cacheManagerThread;

	protected static class CacheManagerThread extends Thread
	{
		private static volatile int nextID = 0;

		private Cache cache;
		private long lastResyncDT = System.currentTimeMillis();

		public CacheManagerThread(Cache cache)
		{
			this.cache = cache;
			setName("Cache.CacheManagerThread-" + (nextID++));
			start();
		}

		private Set currentlySubscribedObjectIDs = new HashSet();

		/**
		 * @see java.lang.Thread#run()
		 */
		public void run()
		{
			long lastErrorDT = 0;
			JDOManager jdoManager = null;
			boolean resync;

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
					Map newCarriersByKey = cache.fetchNewCarriersByKey();
					if (newCarriersByKey != null) {
						for (Iterator itCarriers = newCarriersByKey.entrySet().iterator(); itCarriers.hasNext(); ) {
							Map.Entry me = (Map.Entry)itCarriers.next();
							Key key = (Key) me.getKey();
							Carrier carrier = (Carrier) me.getValue();

							Set objectIDs = carrier.getObjectIDs();
							cache.mapObjectIDs2Key(objectIDs, key);
							cache.subscribeObjectIDs(objectIDs, 0);
						}
					} // if (newCarriersByKey != null) {

					resync = System.currentTimeMillis() - lastResyncDT > cache.getCacheCfMod().getResyncRemoteListenersIntervalMSec();

					if (jdoManager == null)
						jdoManager = JDOManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();

					Map subscriptionChanges = cache.fetchSubscriptionChangeRequests();
					if (LOGGER.isDebugEnabled())
						LOGGER.debug("Thread found " + subscriptionChanges.size() + " subscription change requests.");

					Map newSubscriptionChanges = null;
					LinkedList objectIDsToSubscribe = null;
					LinkedList objectIDsToUnsubscribe = null;
					boolean restoreCurrentlySubscribedObjectIDs = true;
					try {
						long now = System.currentTimeMillis();
						for (Iterator it = subscriptionChanges.entrySet().iterator(); it.hasNext(); ) {
							LOGGER.debug("Before it.next()");
							Map.Entry me = (Map.Entry) it.next();
							LOGGER.debug("After it.next()");
							Object objectID = me.getKey();
							SubscriptionChangeRequest scr = (SubscriptionChangeRequest) me.getValue();

							if (scr.getScheduledActionDT() > now) {
								if (LOGGER.isDebugEnabled())
									LOGGER.debug("Subscription change request " + scr.toString() + " is delayed and will be processed in about " + (scr.getScheduledActionDT() - now) + " msec.");

								if (newSubscriptionChanges == null)
									newSubscriptionChanges = new HashMap();

								newSubscriptionChanges.put(objectID, scr);
							}
							else {
								if (scr.getAction() == SubscriptionChangeRequest.ACTION_REMOVE) {
								// remove
									currentlySubscribedObjectIDs.remove(objectID);

									if (objectIDsToUnsubscribe == null)
										objectIDsToUnsubscribe = new LinkedList();

									objectIDsToUnsubscribe.add(objectID);
								}
								else {
								// add
									// if there exists already one, we don't register it again => ignore
									if (!currentlySubscribedObjectIDs.contains(objectID)) {
										if (objectIDsToSubscribe == null)
											objectIDsToSubscribe = new LinkedList();

										objectIDsToSubscribe.add(objectID);
										currentlySubscribedObjectIDs.add(objectID);
									}
									else {
										if (LOGGER.isDebugEnabled())
											LOGGER.debug("Subscription change request " + scr.toString() + " is ignored, because there exists already a listener.");
									}
								}
							}
						}

						if (resync) {
							LOGGER.info("Synchronizing remote change listeners.");

							jdoManager.resubscribeAllChangeListeners(
									currentlySubscribedObjectIDs);

							lastResyncDT = System.currentTimeMillis();
						}
						else {
							if (objectIDsToUnsubscribe != null || objectIDsToSubscribe != null) {
								LOGGER.info(
										"Adding " +
										(objectIDsToSubscribe == null ? 0 : objectIDsToSubscribe.size()) +
										" and removing " +
										(objectIDsToUnsubscribe == null ? 0 : objectIDsToUnsubscribe.size()) +
										" remote change listeners.");

								if (LOGGER.isDebugEnabled()) {
									LOGGER.debug("Change listeners for the following ObjectIDs will be removed:");
									if (objectIDsToUnsubscribe == null)
										LOGGER.debug("      NONE!");
									else {
										for (Iterator it = objectIDsToUnsubscribe.iterator(); it.hasNext(); )
											LOGGER.debug("      " +  it.next());
									}

									LOGGER.debug("Change listeners for the following ObjectIDs will be added:");
									if (objectIDsToSubscribe == null)
										LOGGER.debug("      NONE!");
									else {
										for (Iterator it = objectIDsToSubscribe.iterator(); it.hasNext(); )
											LOGGER.debug("      " +  it.next());
									}
								}

								jdoManager.removeAddChangeListeners(
										objectIDsToUnsubscribe,
										objectIDsToSubscribe);
							}
						}

						restoreCurrentlySubscribedObjectIDs = false; // this is only executed, if there is no exception before
					} finally {
						if (restoreCurrentlySubscribedObjectIDs) {
							LOGGER.warn("An error occured - will restore previous subscription change requests.");

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
					LOGGER.error("Error in NotificationThread!", t);
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
		public void interrupt()
		{
			terminated = true;
			super.interrupt();
		}
	}

	/**
	 * key: Object objectID<br/>
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
	private Map subscriptionChangeRequests = new HashMap();
	private Object subscriptionChangeRequestsMutex = new Object();

	/**
	 * @return Returns the {@link #objectIDsToSubscribe} and replaces
	 *		the field by a new instance of <code>HashMap</code>.
	 */
	protected Map fetchSubscriptionChangeRequests()
	{
		Map res;
		synchronized (subscriptionChangeRequestsMutex) {
			res = subscriptionChangeRequests;
			subscriptionChangeRequests = new HashMap();
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
	protected void restoreOldSubscriptionChangeRequests(Map oldChangeRequests)
	{
		if (oldChangeRequests == null || oldChangeRequests.isEmpty()) {
			LOGGER.debug("There are no old subscription change requests. Won't do anything.");
			return;
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Restoring older subscription change requests:");
			for (Iterator it = oldChangeRequests.values().iterator(); it.hasNext(); ) {
				SubscriptionChangeRequest scr = (SubscriptionChangeRequest) it.next();
				LOGGER.debug("      " + scr);
			}
		}

		synchronized (subscriptionChangeRequestsMutex) {
			if (LOGGER.isDebugEnabled()) {
				if (subscriptionChangeRequests.isEmpty())
					LOGGER.debug("There are no new subscription change requests to merge into the old ones. Simply replacing them.");
				else {
					LOGGER.debug("There are new subscription change requests which will be merged into the old ones:");
					for (Iterator it = subscriptionChangeRequests.values().iterator(); it.hasNext(); ) {
						SubscriptionChangeRequest scr = (SubscriptionChangeRequest) it.next();
						LOGGER.debug("      " + scr);
					}
				}
			}

			oldChangeRequests.putAll(subscriptionChangeRequests);
			subscriptionChangeRequests = oldChangeRequests;

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("These are the subscription change requests after restore:");
				for (Iterator it = subscriptionChangeRequests.values().iterator(); it.hasNext(); ) {
					SubscriptionChangeRequest scr = (SubscriptionChangeRequest) it.next();
					LOGGER.debug("      " + scr);
				}
			}
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

				SubscriptionChangeRequest scr = (SubscriptionChangeRequest) subscriptionChangeRequests.get(objectID);
				if (scr != null) {
					if (scr.getAction() == SubscriptionChangeRequest.ACTION_ADD
							&&
							scr.getScheduledActionDT() < System.currentTimeMillis() + delayMSec) {

						if (LOGGER.isDebugEnabled())
							LOGGER.debug("Ignoring request to subscribe ObjectID, because there is already a request, which is scheduled earlier. ObjectID: " + objectID);

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
						if (LOGGER.isDebugEnabled())
							LOGGER.debug("Ignoring request to unsubscribe ObjectID, because there is already a request, which is scheduled earlier. ObjectID: " + objectID);

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

	/**
	 * @return Returns the singleton of this class.
	 *
	 * @throws In case the cache needs to be created and a {@link ConfigException} occurs while obtaining {@link CacheCfMod}. 
	 */
	public static Cache sharedInstance()
	{
		try {
			if (_sharedInstance == null)
				_sharedInstance = new Cache();
	
			return _sharedInstance;
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
	private Map carriersByKey = new HashMap();

	/**
	 * key: Object objectID<br/>
	 * value: Set of Key
	 */
	private Map keySetsByObjectID = new HashMap();

	/**
	 * When a new object is put into the Cache, it is immediately registered in
	 * {@link #carriersByKey}, {@link #keySetsByObjectID} and here. In {@link #keySetsByObjectID}
	 * however, there is only the main <code>objectID</code> registered immediately.
	 * All contained objects within the object graph are added later in the
	 * {@link CacheManagerThread}. This thread replaces this Map using
	 * {@link #fetchNewCarriersByKey()}.
	 *
	 * key: {@link Key} key<br/>
	 * value: {@link Carrier} carrier
	 */
	private Map newCarriersByKey = new HashMap();

	/**
	 * This is a rolling carrier-registration with the activeCarrierContainer
	 * being the first entry.
	 */
	private LinkedList carrierContainers = new LinkedList();
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
	protected synchronized Map fetchNewCarriersByKey()
	{
		if (newCarriersByKey.isEmpty())
			return null;

		Map res = newCarriersByKey;
		newCarriersByKey = new HashMap();
		return res;
	}

	protected synchronized void rollCarrierContainers()
	{
		LOGGER.info("Creating new activeCarrierContainer.");
		CarrierContainer newActiveCC = new CarrierContainer(this);
		carrierContainers.addFirst(newActiveCC);
		activeCarrierContainer = newActiveCC;

		long carrierContainerCount = getCacheCfMod().getCarrierContainerCount();

		if (carrierContainerCount < 2)
			throw new IllegalStateException("carrierContainerCount = "+carrierContainerCount+" but must be at least 2!!!");

		while (carrierContainers.size() > carrierContainerCount) {
			CarrierContainer cc = (CarrierContainer) carrierContainers.removeLast();
			LOGGER.info("Dropping carrierContainer (created " + cc.getCreateDT() + ")");
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
		LOGGER.info("Creating new Cache instance.");
		cacheCfMod = (CacheCfMod) Config.sharedInstance().createConfigModule(CacheCfMod.class);
		Config.sharedInstance().saveConfFile(); // TODO remove this as soon as we have a thread that periodically saves it.
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
	public Object get(String scope, Object objectID, String[] fetchGroups)
	{
		return get(scope, objectID, Utils.array2HashSet(fetchGroups));
	}

	/**
	 * Use this method to retrieve an object from the cache. This method updates
	 * the access timestamp, if an object has been found.
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
	public synchronized Object get(String scope, Object objectID, Set fetchGroups)
	{
		assertOpen();

		Key key = new Key(scope, objectID, fetchGroups);
		Carrier carrier = (Carrier) carriersByKey.get(key);
		if (carrier == null) {
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("No Carrier found for key: " + key.toString());

			return null;
		}

		Object object = carrier.getObject();
		if (object == null) { // remove the unnecessary keys from all indices.
			LOGGER.warn("Found Carrier, but object has already been released by the garbage collector! If this message occurs often, give the VM more memory or the Cache a shorter object-lifetime! Alternatively, you may switch to hard references. key: " + key.toString());

			remove(key);
			return null;
		}

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Found Carrier - will return object from cache. key: " + key.toString());

		carrier.setAccessDT();
		return object;
	}

	public void putAll(String scope, Collection objects, String[] fetchGroups)
	{
		putAll(scope, objects, Utils.array2HashSet(fetchGroups));
	}

	public void putAll(String scope, Collection objects, Set fetchGroups)
	{
		if (objects == null)
			throw new NullPointerException("objects must not be null!");

		for (Iterator it = objects.iterator(); it.hasNext(); )
			put(scope, it.next(), fetchGroups);
	}

	/**
	 * This method calls {@link #put(String, Object, Set)}.
	 */
	public void put(String scope, Object object, String[] fetchGroups)
	{
		put(scope, object, Utils.array2HashSet(fetchGroups));
	}

	/**
	 * This method puts a jdo object into the cache. Therefore, it
	 * obtains the objectID and calls {@link #put(String, Object, Object, Set)}.
	 */
	public void put(String scope, Object object, Set fetchGroups)
	{
		put(scope, JDOHelper.getObjectId(object), object, fetchGroups);
	}

	/**
	 * This method calls {@link #put(String, Object, Object, Set)}.
	 */
	public void put(String scope, Object objectID, Object object, String[] fetchGroups)
	{
		put(scope, objectID, object, Utils.array2HashSet(fetchGroups));
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
	public void put(String scope, Object objectID, Object object, Set fetchGroups)
	{
		assertOpen();

		if (object == null)
			throw new NullPointerException("object must not be null!");

		if (objectID == null)
			throw new NullPointerException("objectID must not be null!");

		Key key = new Key(scope, objectID, fetchGroups);

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Putting object into cache. key: " + key.toString());

		synchronized (this) {
			// remove the old carrier - if existing
			Carrier oldCarrier = (Carrier) carriersByKey.get(key);
			if (oldCarrier != null) {
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("There was an old carrier for the same key in the cache; removing it. key: " + key.toString());

				oldCarrier.setCarrierContainer(null);
			}

			// the constructor of Carrier self-registers in the active CarrierContainer
			Carrier carrier = new Carrier(key, object, getActiveCarrierContainer());

			// store the new carrier in our main cache map
			carriersByKey.put(key, carrier);
			// ...and in the newCarriers map
			newCarriersByKey.put(key, carrier);

			// register the key by its objectID (for fast removal if the object changed)
			Set keySet = (Set) keySetsByObjectID.get(objectID);
			if (keySet == null) {
				keySet = new HashSet();
				keySetsByObjectID.put(objectID, keySet);
			}
			keySet.add(key);
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
	 * by {@link #fetchNewCarriersByKey()}. It adds entries to {@link #keySetsByObjectID}.
	 *
	 * @param objectIDs The object-ids that should point to the given key.
	 * @param key The key that should be mapped by all the given objectIDs.
	 */
	protected synchronized void mapObjectIDs2Key(Collection objectIDs, Key key)
	{
		for (Iterator it = objectIDs.iterator(); it.hasNext(); ) {
			Object objectID = it.next();

			Set keySet = (Set) keySetsByObjectID.get(objectID);
			if (keySet == null) {
				keySet = new HashSet();
				keySetsByObjectID.put(objectID, keySet);
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
	protected synchronized void remove(Key key)
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Removing Carrier for key: " + key.toString());

		Carrier oldCarrier = (Carrier) carriersByKey.remove(key);
		Set objectIDs;
		if (oldCarrier == null) {
			objectIDs = new HashSet();
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

			Set keySet = (Set) keySetsByObjectID.get(objectID);
			if (keySet != null) {
				keySet.remove(key);

				if (keySet.isEmpty())
					keySetsByObjectID.remove(objectID);
			}
		}

		if (oldCarrier != null)
			oldCarrier.setCarrierContainer(null);
	}

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
	 */
	protected synchronized int remove(ObjectID objectID)
	{
		LOGGER.debug("Removing all Carriers for objectID: " + objectID);

		Set keySet = (Set) keySetsByObjectID.remove(objectID);
		if (keySet == null)
			return 0;

		int removedCarrierCount = 0;
		for (Iterator it = keySet.iterator(); it.hasNext(); ) {
			Key key = (Key) it.next();
			Carrier carrier = (Carrier) carriersByKey.remove(key);
			if (carrier != null) {
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Removing Carrier: key=\""+key.toString()+"\"");

				carrier.setCarrierContainer(null);
				removedCarrierCount++;
			}
			else
				LOGGER.warn("There was a key in the keySetsByObjectID, but no carrier for it in carriersByKey! key=\""+key.toString()+"\"");
		}
		return removedCarrierCount;
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

	public synchronized void open(String sessionID)
	{
		if (sessionID == null || sessionID.length() < 2)
			throw new IllegalArgumentException("sessionID must be a String with at least 2 characters!");

		this.sessionID = sessionID;
		this.cacheManagerThread = new CacheManagerThread(this);
		this.notificationThread = new NotificationThread(this);
	}

	public synchronized void close()
	throws ModuleException
	{
		try {
			JDOManager jm = JDOManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();

			// remove all listeners for this session - done by remote closeCacheSession(...)
			jm.closeCacheSession();

			// stop the threads
			cacheManagerThread.interrupt();
			cacheManagerThread = null;
			notificationThread.interrupt();
			notificationThread = null;

			// clear the cache
			carriersByKey.clear();
			keySetsByObjectID.clear();

			// forget the sessionID - a new one will automatically be generated
			sessionID = null;
		} catch (ModuleException x) {
			throw x;
		} catch (Exception x) {
			throw new ModuleException(x);
		}
	}
}
