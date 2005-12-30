/*
 * Created on Jul 27, 2005
 */
package org.nightlabs.ipanema.jdo.cache;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.jdo.PersistenceManagerFactory;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.nightlabs.ipanema.jdo.cache.bridge.JdoCacheBridge;
import org.nightlabs.ipanema.servermanager.config.OrganisationCf;
import org.nightlabs.ipanema.servermanager.ra.JFireServerManagerFactoryImpl;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class CacheManagerFactory
implements Serializable
{
	public static final Logger LOGGER = Logger.getLogger(CacheManagerFactory.class);

	public static final String JNDI_PREFIX = "java:/ipanema/cacheManagerFactory/";

	public static String getJNDIName(String organisationID)
	{
		return JNDI_PREFIX + organisationID;
	}

	public static final CacheManagerFactory getCacheManagerFactory(InitialContext ctx, String organisationID)
	throws NamingException
	{
		return (CacheManagerFactory) ctx.lookup(getJNDIName(organisationID));
	}

	private String organisationID;

	protected static class NotificationThread extends Thread
	{
		private static volatile int nextID = 0;

		private CacheManagerFactory cacheManagerFactory;

		public NotificationThread(CacheManagerFactory cacheManagerFactory)
		{
			this.cacheManagerFactory = cacheManagerFactory;
			setName("CacheManagerFactory.NotificationThread-" + (nextID++) + " (" + cacheManagerFactory.organisationID + ')');
		}

		/**
		 * @see java.lang.Thread#run()
		 */
		public void run()
		{
			while (!isInterrupted()) {
				try {
	
					try {
						sleep(cacheManagerFactory.getCacheCfMod().getNotificationIntervalMSec());
					} catch (InterruptedException e) {
						// ignore
					}
	
					if (isInterrupted())
						break;

					cacheManagerFactory.distributeDirtyObjectIDs();

					Set cacheSessionIDs = cacheManagerFactory.fetchCacheSessionIDsToNotify();
					if (cacheSessionIDs != null) {
						LOGGER.info("Found " + cacheSessionIDs.size() + " CacheSessions to notify.");

						for (Iterator it = cacheSessionIDs.iterator(); it.hasNext(); ) {
							String cacheSessionID = (String) it.next();
							CacheSession session = cacheManagerFactory.getCacheSession(cacheSessionID);
							if (session == null)
								LOGGER.error("No CacheSession found for cacheSessionID=\""+cacheSessionID+"\"!");
							else {
							// if (session != null)
								session.notifyChanges();
							} // if (session != null)

						} // for (Iterator it = cacheSessionIDs.iterator(); it.hasNext(); ) {
					} // if (cacheSessionIDs != null) {

				} catch (Throwable t) {
					LOGGER.error("Exception in NotificationThread!", t);
				}
			} // while (!isInterrupted()) {
		}

		private volatile boolean terminated = false;

		/**
		 * This method checks not only for <tt>super.isInterrupted()</tt>,
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
		 * This method calls <tt>super.interrupt()</tt>,
		 * after setting {@link #terminated} to <tt>true</tt>.
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

	private void writeObject(java.io.ObjectOutputStream out)
	throws IOException
	{
		out.defaultWriteObject();
		LOGGER.info("***********************************************************");
		LOGGER.info("writeObject(...) called");
		LOGGER.info("***********************************************************");
	}

	private void readObject(java.io.ObjectInputStream in)
	throws IOException, ClassNotFoundException
	{
		throw new UnsupportedOperationException("I don't have any idea what I should do if this happens? Restart new threads? When does this happen? In a cluster maybe? Marco.");
//	 	in.defaultReadObject();
//	 	notificationThread.start();
//	 	cacheSessionContainerManagerThread.start();
//	 	LOGGER.info("***********************************************************");
//		LOGGER.info("readObject(...) called");
//		LOGGER.info("***********************************************************");
	}

	private transient NotificationThread notificationThread = new NotificationThread(this);

	private CacheCfMod cacheCfMod;

	protected CacheCfMod getCacheCfMod()
	{
		return cacheCfMod;
	}

	public CacheManagerFactory(InitialContext ctx, OrganisationCf organisation, CacheCfMod cacheCfMod)
	throws NamingException
	{
		this.organisationID = organisation.getOrganisationID();
		this.cacheCfMod = cacheCfMod;
		activeCacheSessionContainer = new CacheSessionContainer(this);
		cacheSessionContainers.addFirst(activeCacheSessionContainer);

		activeFreshDirtyObjectIDContainer = new DirtyObjectIDContainer();
		freshDirtyObjectIDContainers.addFirst(activeFreshDirtyObjectIDContainer);

		notificationThread.start();
		cacheSessionContainerManagerThread.start();
		freshDirtyObjectIDContainerManagerThread.start();

		try {
			ctx.createSubcontext("java:/ipanema");
		} catch (NameAlreadyBoundException e) {
			// ignore
		}

		try {
			ctx.createSubcontext("java:/ipanema/cacheManagerFactory");
		} catch (NameAlreadyBoundException e) {
			// ignore
		}

		ctx.bind(getJNDIName(organisationID), this);

//		// TO DO do we really want this? We don't have a plan yet on how to make
//		// the representative-organisations work.
//		if (!organisationID.equals(organisation.getMasterOrganisationID()))
//			ctx.bind(getJNDIName(organisation.getMasterOrganisationID()), this);
	}

	/**
	 * key: String cacheSessionID<br/>
	 * value: CacheSession cacheSession
	 */
	private Map cacheSessions = new HashMap();

	/**
	 * key: Object objectID<br/>
	 * value: Map listenersByCacheSessionID {<br/>
	 *		key: String cacheSessionID<br/>
	 *		value: ChangeListenerDescriptor changeListener<br/>
	 * }
	 */
	private Map listenersByObjectID = new HashMap();


	private transient FreshDirtyObjectIDContainerManagerThread freshDirtyObjectIDContainerManagerThread =
		new FreshDirtyObjectIDContainerManagerThread(this);

	protected static class FreshDirtyObjectIDContainerManagerThread extends Thread
	{
		private static volatile int nextID = 0;
		private CacheManagerFactory cacheManagerFactory;

		public FreshDirtyObjectIDContainerManagerThread(CacheManagerFactory cacheManagerFactory)
		{
			this.cacheManagerFactory = cacheManagerFactory;
			setName("CacheManagerFactory.FreshDirtyObjectIDContainerManagerThread-" + (nextID++) + " (" + cacheManagerFactory.organisationID + ')');
		}

		/**
		 * @see java.lang.Thread#run()
		 */
		public void run()
		{
			while (!isInterrupted()) {
				try {
					long freshDirtyObjectIDContainerActivityMSec = cacheManagerFactory.getCacheCfMod().getFreshDirtyObjectIDContainerActivityMSec();
					DirtyObjectIDContainer active = cacheManagerFactory.getActiveFreshDirtyObjectIDContainer();

					long sleepMSec = freshDirtyObjectIDContainerActivityMSec - (System.currentTimeMillis() - active.getCreateDT()) + 1;
					if (sleepMSec > 60000) // if the thread is requested to stop, we never wait longer than 1 min (we should get an InterruptedException, but to be sure)
						sleepMSec = 60000;

					try {
						if (sleepMSec > 0)
							sleep(sleepMSec);
					} catch (InterruptedException e) {
						// ignore
					}

					active = cacheManagerFactory.getActiveFreshDirtyObjectIDContainer(); // should still be the same, but just to be sure...
					if (System.currentTimeMillis() - active.getCreateDT() > freshDirtyObjectIDContainerActivityMSec)
						cacheManagerFactory.rollFreshDirtyObjectIDContainers();

				} catch (Throwable t) {
					LOGGER.error("Exception in FreshDirtyObjectIDContainerManagerThread!", t);
				}
			} // while (!isInterrupted()) {
		}

		private volatile boolean terminated = false;

		/**
		 * This method checks not only for <tt>super.isInterrupted()</tt>,
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
		 * This method calls <tt>super.interrupt()</tt>,
		 * after setting {@link #terminated} to <tt>true</tt>.
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

	private transient CacheSessionContainerManagerThread cacheSessionContainerManagerThread =
			new CacheSessionContainerManagerThread(this);

	protected static class CacheSessionContainerManagerThread extends Thread
	{
		private static volatile int nextID = 0;
		private CacheManagerFactory cacheManagerFactory;

		public CacheSessionContainerManagerThread(CacheManagerFactory cacheManagerFactory)
		{
			this.cacheManagerFactory = cacheManagerFactory;
			setName("CacheManagerFactory.CacheSessionContainerManagerThread-" + (nextID++) + " (" + cacheManagerFactory.organisationID + ')');
		}

		/**
		 * @see java.lang.Thread#run()
		 */
		public void run()
		{
			while (!isInterrupted()) {
				try {
					long cacheSessionContainerActivityMSec = cacheManagerFactory.getCacheCfMod().getCacheSessionContainerActivityMSec();
					CacheSessionContainer active = cacheManagerFactory.getActiveCacheSessionContainer();

					long sleepMSec = cacheSessionContainerActivityMSec - (System.currentTimeMillis() - active.getCreateDT()) + 1;
					if (sleepMSec > 60000) // if the thread is requested to stop, we never wait longer than 1 min (we should get an InterruptedException, but to be sure)
						sleepMSec = 60000;

					try {
						if (sleepMSec > 0)
							sleep(sleepMSec);
					} catch (InterruptedException e) {
						// ignore
					}

					active = cacheManagerFactory.getActiveCacheSessionContainer(); // should still be the same, but just to be sure...
					if (System.currentTimeMillis() - active.getCreateDT() > cacheSessionContainerActivityMSec)
						cacheManagerFactory.rollCacheSessionContainers();

				} catch (Throwable t) {
					LOGGER.error("Exception in CacheSessionContainerManagerThread!", t);
				}
			} // while (!isInterrupted()) {
		}

		private volatile boolean terminated = false;

		/**
		 * This method checks not only for <tt>super.isInterrupted()</tt>,
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
		 * This method calls <tt>super.interrupt()</tt>,
		 * after setting {@link #terminated} to <tt>true</tt>.
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
	 * To release expired <tt>CacheSession</tt>s after a certain time,
	 * we use this rolling <tt>LinkedList</tt> of {@link CacheSessionContainer}.
	 * <p>
	 * The first <tt>CacheSessionContainer</tt> is the active one, while the last
	 * one is the oldest and will be deleted next.
	 */
	private LinkedList cacheSessionContainers = new LinkedList();
	private CacheSessionContainer activeCacheSessionContainer; // initialized by constructor

	protected CacheSessionContainer getActiveCacheSessionContainer()
	{
		return activeCacheSessionContainer;
	}

	/**
	 * This method creates a new active <tt>CacheSessionContainer</tt>, pushes it as
	 * first entry into the <tt>cacheSessionContainers</tt> <tt>LinkedList</tt>
	 * and drops all last containers that are more than
	 * {@link CacheCfMod#getCacheSessionContainerCount()}.
	 */
	protected void rollCacheSessionContainers()
	{
		synchronized (cacheSessionContainers) {
			CacheSessionContainer newActiveCSC = new CacheSessionContainer(this);
			LOGGER.info("Creating new activeCacheSessionContainer (createDT=" + newActiveCSC.getCreateDT() + ").");
			cacheSessionContainers.addFirst(newActiveCSC);
			activeCacheSessionContainer = newActiveCSC;

			int cacheSessionContainerCount = getCacheCfMod().getCacheSessionContainerCount();
			if (cacheSessionContainerCount < 2)
				throw new IllegalStateException("cacheSessionContainerCount = "+cacheSessionContainerCount+" but must be at least 2!!!");

			while (cacheSessionContainers.size() > cacheSessionContainerCount) {
				CacheSessionContainer csc = (CacheSessionContainer) cacheSessionContainers.removeLast();
				LOGGER.info("Dropping cacheSessionContainer (createDT=" + csc.getCreateDT() + ")");
				csc.close();
			}
		}
	}

	protected CacheSession createCacheSession(String cacheSessionID)
	{
		synchronized (cacheSessions) {
			CacheSession session = (CacheSession) cacheSessions.get(cacheSessionID);
			if (session == null) {
				session = new CacheSession(this, cacheSessionID);
				cacheSessions.put(cacheSessionID, session);
				LOGGER.debug("Created new CacheSession for cacheSessionID=\""+cacheSessionID+"\"!");
			}

			session.setCacheSessionContainer(getActiveCacheSessionContainer());

			return session;
		} // synchronized (cacheSessions) {
	}

	protected CacheSession getCacheSession(String cacheSessionID)
	{
		synchronized (cacheSessions) {
			return (CacheSession) cacheSessions.get(cacheSessionID);
		}
	}

	protected void resubscribeAllChangeListeners(String cacheSessionID,
			Set subscribedObjectIDs)
	{
		CacheSession session = createCacheSession(cacheSessionID);

		LinkedList objectIDsToRemove = new LinkedList();
		LinkedList objectIDsToAdd = new LinkedList();
		Set oldSubscribedObjectIDs = session.getSubscribedObjectIDs();
		for (Iterator it = oldSubscribedObjectIDs.iterator(); it.hasNext(); ) {
			Object objectID = it.next();
			if (!subscribedObjectIDs.contains(objectID))
				objectIDsToRemove.add(objectID);
		}

		for (Iterator it = subscribedObjectIDs.iterator(); it.hasNext(); ) {
			Object objectID = it.next();
			if (!oldSubscribedObjectIDs.contains(objectID))
				objectIDsToAdd.add(objectID);
		}

		for (Iterator it = objectIDsToRemove.iterator(); it.hasNext(); )
			removeChangeListener(cacheSessionID, it.next());

		for (Iterator it = objectIDsToAdd.iterator(); it.hasNext(); )
			addChangeListener(new ChangeListenerDescriptor(cacheSessionID, it.next()));
	}

	/**
	 * This method creates a new active <tt>DirtyObjectIDContainer</tt>, pushes it as
	 * first entry into the <tt>freshDirtyObjectIDContainers</tt> <tt>LinkedList</tt>
	 * and drops all last containers that are more than
	 * {@link CacheCfMod#getFreshDirtyObjectIDContainerCount()}.
	 */
	protected void rollFreshDirtyObjectIDContainers()
	{
		synchronized (freshDirtyObjectIDContainers) {
			DirtyObjectIDContainer newActiveDOC = new DirtyObjectIDContainer();
			LOGGER.info("Creating new activeFreshDirtyObjectIDContainer (createDT=" + newActiveDOC.getCreateDT() + ").");
			freshDirtyObjectIDContainers.addFirst(newActiveDOC);
			activeFreshDirtyObjectIDContainer = newActiveDOC;

			int freshDirtyObjectIDContainerCount = getCacheCfMod().getFreshDirtyObjectIDContainerCount();
			if (freshDirtyObjectIDContainerCount < 2)
				throw new IllegalStateException("freshDirtyObjectIDContainerCount = "+freshDirtyObjectIDContainerCount+" but must be at least 2!!!");

			while (freshDirtyObjectIDContainers.size() > freshDirtyObjectIDContainerCount) {
				DirtyObjectIDContainer doc = (DirtyObjectIDContainer) freshDirtyObjectIDContainers.removeLast();
				LOGGER.info("Dropping freshDirtyObjectIDContainer (createDT=" + doc.getCreateDT() + ").");
				doc.close();
			}
		}
	}

	/**
	 * This method adds your cache session a listener to get notified if the
	 * persistence capable class specified by <tt>objectID</tt> has been changed.
	 * <p>
	 * In case, you have already registered a listener before, this method does
	 * nothing.
	 * <p>
	 * This method implicitely opens a new {@link CacheSession} if it is not
	 * yet existing.
	 */
	protected void addChangeListener(ChangeListenerDescriptor l)
	{
		String sessionID = l.getSessionID();
		Object objectID = l.getObjectID();

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("addChangeListener adding listener for session " + sessionID + " on objectID " + objectID);

		CacheSession session = createCacheSession(sessionID);
		session.subscribeObjectID(objectID); // is synchronized itself

		synchronized (listenersByObjectID) {
			Map m = (Map) listenersByObjectID.get(objectID);
			if (m == null) {
				m = new HashMap();
				listenersByObjectID.put(objectID, m);
			}
			m.put(sessionID, l);
		} // synchronized (listenersByObjectID) {


		// We cause notification, if the specified objectID became dirty (by another session), lately.
		DirtyObjectID triggerNotificationDirtyObjectID = null; // if there are more than one (in multiple containers), this will be the first one found (and matching)!
		synchronized (freshDirtyObjectIDContainers) {
			for (Iterator it = freshDirtyObjectIDContainers.iterator(); it.hasNext();) {
				DirtyObjectIDContainer dirtyObjectIDContainer = (DirtyObjectIDContainer) it.next();
				DirtyObjectID dirtyObjectID = dirtyObjectIDContainer.getDirtyObjectID(objectID);
				if (dirtyObjectID != null) {
					Set sourceSessionIDs = dirtyObjectID.getSourceSessionIDs();
					if (sourceSessionIDs.size() > 1 || !sourceSessionIDs.contains(sessionID)) {
						triggerNotificationDirtyObjectID = dirtyObjectID;
						break;
					}
				}
			} // for (Iterator it = freshDirtyObjectIDContainers.iterator(); it.hasNext();) {
		} // synchronized (freshDirtyObjectIDContainers) {

		if (triggerNotificationDirtyObjectID != null) {
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("addChangeListener immediately notifying session " + sessionID + " for newly added listener on freshly changed object " + objectID);

			ArrayList notifyDirtyObjectIDs = new ArrayList(1);
			notifyDirtyObjectIDs.add(triggerNotificationDirtyObjectID);
			session.addDirtyObjectIDs(notifyDirtyObjectIDs);

			// pass the intestedCacheSessionIDs to the NotificationThread
			synchronized (cacheSessionIDsToNotifyMutex) {
				if (cacheSessionIDsToNotify == null)
					cacheSessionIDsToNotify = new HashSet();

				cacheSessionIDsToNotify.add(sessionID);
			}
		}
	}

	/**
	 * Removes a listener which has previously been added by
	 * <tt>addChangeListener(...)</tt>.
	 */
	protected void removeChangeListener(String sessionID, Object objectID)
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("removeChangeListener removing listener for session " + sessionID + " on objectID " + objectID);

		CacheSession session = getCacheSession(sessionID);
		if (session != null)
			session.unsubscribeObjectID(objectID); // is synchronized itself

		synchronized (listenersByObjectID) {
			Map m = (Map) listenersByObjectID.get(objectID);
			if (m != null) {
				m.remove(sessionID);

				if (m.isEmpty())
					listenersByObjectID.remove(objectID);
			}
		} // synchronized (listenersByObjectID) {
	}

	/**
	 * This method removes all listeners that have been registered for
	 * the given <tt>cacheSessionID</tt>. The method <tt>waitForChanges(...)</tt>
	 * will be released (if it's currently waiting).
	 * <p>
	 * If there is no <tt>CacheSession</tt> existing for the given ID, this method
	 * silently returns without doing anything.
	 */
	protected void closeCacheSession(String cacheSessionID)
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Closing CacheSession with cacheSessionID=\""+cacheSessionID+"\"");

		CacheSession session = getCacheSession(cacheSessionID);
		if (session == null) {
			LOGGER.warn("CacheSession with cacheSessionID=\""+cacheSessionID+"\" cannot be closed, because it is unknown!");
			return;
		}

		session.close();

		synchronized (listenersByObjectID) {
			for (Iterator it = session.getSubscribedObjectIDs().iterator(); it.hasNext(); ) {
				Object objectID = it.next();
				Map m = (Map) listenersByObjectID.get(objectID);
				if (m != null) {
					m.remove(cacheSessionID);

					if (m.isEmpty())
						listenersByObjectID.remove(objectID);
				}
			}
		} // synchronized (listenersByObjectID) {
	}

	/**
	 * Those cacheSessionIDs will be taken by the
	 * {@link CacheManagerFactory.NotificationThread} and this variable will be
	 * reset to <tt>null</tt> again. This allows short locking periods and more
	 * parallelization.
	 * {@link #addDirtyObjectIDs(Collection)} will create a new <tt>HashSet</tt>,
	 * if it is <tt>null</tt>, and add all cacheSessionIDs that must be notified.
	 */
	private Set cacheSessionIDsToNotify = null;
	private transient Object cacheSessionIDsToNotifyMutex = new Object();

	protected Set fetchCacheSessionIDsToNotify() {
		synchronized (cacheSessionIDsToNotifyMutex) {
			Set res = cacheSessionIDsToNotify;
			cacheSessionIDsToNotify = null;
			return res;
		}
	}

	/**
	 * key: Object objectID<br/>
	 * value: {@link DirtyObjectID} dirtyObjectID
	 */
	private Map objectIDsWaitingForNotification = null;
	private transient Object objectIDsWaitingForNotificationMutex = new Object();

	/**
	 * Call this method to notify all interested clients about the changed
	 * JDO objects. This method is called by
	 * {@link CacheManager#addDirtyObjectIDs(Collection)}.
	 * <p>
	 * Note, that the notification works asynchronously and this method
	 * immediately returns.
	 * </p>
	 * @param sessionID The session which made the objects dirty.
	 * @param objectIDs The object-ids referencing the changed JDO objects.
	 */
	public void addDirtyObjectIDs(String sessionID, Collection objectIDs)
	{
		if (objectIDs == null || objectIDs.isEmpty()) // to avoid unnecessary errors (though null should never come here)
			return;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("addDirtyObjectIDs(...) called by sessionID \"" + sessionID + "\" with " + objectIDs.size() + " objectIDs:");
			for (Iterator it = objectIDs.iterator(); it.hasNext(); )
				LOGGER.debug("      " + it.next());
		}

		synchronized (localDirtyListenersMutex) {
			if (localDirtyListeners != null) {
				for (Iterator it = localDirtyListeners.iterator(); it.hasNext(); )
					((LocalDirtyListener)it.next()).notifyDirtyObjectIDs(objectIDs);
			}
		}

		synchronized (objectIDsWaitingForNotificationMutex) {
			if (objectIDsWaitingForNotification == null)
				objectIDsWaitingForNotification = new HashMap(objectIDs.size());

			for (Iterator it = objectIDs.iterator(); it.hasNext(); ) {
				Object objectID = it.next();
				DirtyObjectID dirtyObjectID = (DirtyObjectID) objectIDsWaitingForNotification.get(objectID);
				if (dirtyObjectID != null)
					dirtyObjectID.addSourceSessionID(sessionID);
				else
					objectIDsWaitingForNotification.put(objectID, new DirtyObjectID(objectID, sessionID));
			}
		}
	}

	private LinkedList localDirtyListeners = null;
	private transient Object localDirtyListenersMutex = new Object();
	public void addLocalDirtyListener(LocalDirtyListener localDirtyListener)
	{
		synchronized (localDirtyListenersMutex) {
			if (localDirtyListeners == null)
				localDirtyListeners = new LinkedList();

			localDirtyListeners.add(localDirtyListener);
		}
	}
	public void removeLocalDirtyListener(LocalDirtyListener localDirtyListener)
	{
		synchronized (localDirtyListenersMutex) {
			if (localDirtyListeners == null)
				return;

			localDirtyListeners.remove(localDirtyListener);

			if (localDirtyListeners.isEmpty())
				localDirtyListeners = null;
		}
	}

	private DirtyObjectIDContainer activeFreshDirtyObjectIDContainer; // initialized by constructor
	private LinkedList freshDirtyObjectIDContainers = new LinkedList();

	protected DirtyObjectIDContainer getActiveFreshDirtyObjectIDContainer()
	{
		return activeFreshDirtyObjectIDContainer;
	}

	/**
	 * This method distributes those <tt>objectIDs</tt> which have been added by
	 * {@link #addDirtyObjectIDs(Collection)} to those {@link CacheSession}s
	 * that have registered listeners for the given IDs (means the objectID is subscribed in
	 * the <tt>CacheSession</tt>).
	 * <p>
	 * Additionally, it stores the {@link DirtyObjectID}s in 
	 * </p>
	 * <p>
	 * This method is called by the {@link CacheManagerFactory.NotificationThread}.
	 * This way {@link #addDirtyObjectIDs(Collection)} gets faster.
	 * </p>
	 *
	 * @param objectIDs A <tt>Collection</tt> of JDO object IDs.
	 */
	protected void distributeDirtyObjectIDs()
	{
		if (this.objectIDsWaitingForNotification == null) { // IMHO no sync necessary, because, if this value is just right now changing, we can simply wait for the next cycle.
			LOGGER.debug("There are no objectIDs waiting for notification. Return immediately.");
			return;
		}

		// No need to synchronize access to activeFreshDirtyObjectIDContainer, because this is never
		// null and it doesn't matter, whether this is really the active one or we missed the rolling.
		activeFreshDirtyObjectIDContainer.addDirtyObjectIDs(objectIDsWaitingForNotification.values());

		Map dirtyObjectIDs;
		synchronized (objectIDsWaitingForNotificationMutex) {
			dirtyObjectIDs = this.objectIDsWaitingForNotification;
			this.objectIDsWaitingForNotification = null;
		}

		// instances of String cacheSessionID
		Set interestedCacheSessionIDs = new HashSet();

		synchronized (listenersByObjectID) {
			// find all CacheSessions' IDs which are interested in the changed objectIDs
			for (Iterator it = dirtyObjectIDs.values().iterator(); it.hasNext(); ) {
				DirtyObjectID dirtyObjectID = (DirtyObjectID) it.next();

				Map m = (Map) listenersByObjectID.get(dirtyObjectID.getObjectID());
				if (m != null)
					interestedCacheSessionIDs.addAll(m.keySet());
			}
		} // synchronized (listenersByObjectID) {

		// add the changed objectIDs to the found sessions
		synchronized (cacheSessions) {
			for (Iterator it = interestedCacheSessionIDs.iterator(); it.hasNext(); ) {
				String cacheSessionID = (String) it.next();
				CacheSession session = (CacheSession) cacheSessions.get(cacheSessionID);
				if (session == null)
					LOGGER.error("Could not find CacheSession for cacheSessionID=\""+cacheSessionID+"\"!");
				else
					session.addDirtyObjectIDs(dirtyObjectIDs.values()); // this method picks only those IDs which the session has subscribed
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("The following CacheSessions have been marked to have changed objectIDs:");
			for (Iterator it = interestedCacheSessionIDs.iterator(); it.hasNext(); ) {
				String cacheSessionID = (String) it.next();
				LOGGER.debug("      " + cacheSessionID);
			}
		}

		// pass the intestedCacheSessionIDs to the NotificationThread
		synchronized (cacheSessionIDsToNotifyMutex) {
			if (cacheSessionIDsToNotify == null)
				cacheSessionIDsToNotify = interestedCacheSessionIDs;
			else
				cacheSessionIDsToNotify.addAll(interestedCacheSessionIDs);
		}
	}

	/**
	 * This method blocks and returns not before the timeout (default is
	 * {@link #WAIT_TIMEOUT}, but can be adjusted in {@link CacheSession}) occured
	 * or when <tt>closeCacheSession(...)</tt> has been called or - the main
	 * reason - at least one persistence-capable object has been changed.
	 * Because this method tries to collect multiple (by returning only at
	 * predefined time spots and by reacting only at the end of a transaction),
	 * it might return many object ids.
	 *
	 * @return Returns either <tt>null</tt> if nothing changed or a <tt>Collection</tt>
	 *		of object ids.
	 */
	protected Collection waitForChanges(String cacheSessionID, long waitTimeout)
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("waitForChanges(cacheSessionID=\""+cacheSessionID+"\") entered");

		CacheSession session = createCacheSession(cacheSessionID);
		long startDT = System.currentTimeMillis();
		session.waitForChanges(waitTimeout);
		long stopDT = System.currentTimeMillis();
		long waitTime = stopDT - startDT;

		// In case the wait returned very quickly (maybe there were changes already
		// when entering waitForChanges(...), we give at least notificationInterval msec
		// time for additional changes to pile up.
		long ni = getCacheCfMod().getNotificationIntervalMSec(); // because it might be changed, we store it in a temp var
		if (waitTime < ni) {
			try {
				Thread.sleep(ni - waitTime);
			} catch (InterruptedException e) {
				// ignore
			}
		}

		Map dirtyObjectIDs = session.fetchDirtyObjectIDs();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("CacheSession \"" + cacheSessionID + "\" will be notified with the following objectIDs:");
			if (dirtyObjectIDs == null)
				LOGGER.debug("      NONE!");
			else {
				for (Iterator itD = dirtyObjectIDs.values().iterator(); itD.hasNext(); ) {
					DirtyObjectID dirtyObjectID = (DirtyObjectID) itD.next();
					StringBuffer causes = new StringBuffer();
					for (Iterator itC = dirtyObjectID.getSourceSessionIDs().iterator(); itC.hasNext(); ) {
						causes.append(itC.next());
						if (itC.hasNext())
							causes.append(',');
					}
					LOGGER.debug("      " + dirtyObjectID.getObjectID() + " (causes: "+causes+")");
				}
			}
		}

		if (dirtyObjectIDs == null)
			return null;

		return new ArrayList(dirtyObjectIDs.values());
	}

	/**
	 * @return Returns a newly created <tt>CacheManager</tt> which can be used to access
	 *		the cache. Note, that this method does not yet open a <tt>CacheSession</tt>.
	 */
	public CacheManager getCacheManager(String cacheSessionID)
	{
		return new CacheManager(this, cacheSessionID);
	}

	/**
	 * @return Returns a newly created <tt>CacheManager</tt> which can be used to access
	 *		the cache. Note, that this method does not assign a <tt>cacheSessionID</tt> and
	 *		therefore most methods of the <tt>CacheManager</tt> cannot be used! 
	 */
	public CacheManager getCacheManager()
	{
		return new CacheManager(this);
	}

	private transient JdoCacheBridge bridge = null;
	private transient Timer timerCheckPersistenceManagerFactory = null;

	public synchronized void setupJdoCacheBridge(PersistenceManagerFactory pmf)
	throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		if (pmf == null)
			throw new NullPointerException("pmf");

		if (bridge != null) {
			LOGGER.info("The CacheManagerFactory for organisation \""+organisationID+"\" has already a JdoCacheBridge assigned: " + bridge);
			LOGGER.info("Will close the old bridge and setup a new one.");
			bridge.close();
			bridge = null;
		}

		String jdoCacheBridgeClassName = cacheCfMod.getJdoCacheBridgeClassName();

		LOGGER.info("Creating JdoCacheBridge \""+jdoCacheBridgeClassName+"\" for organisationID=\""+organisationID+"\"!");

		bridge = (JdoCacheBridge) Class.forName(jdoCacheBridgeClassName).newInstance();
		bridge.setCacheManagerFactory(this);
		bridge.setPersistenceManagerFactory(pmf);
		bridge.init();

		if (timerCheckPersistenceManagerFactory != null) {
			timerCheckPersistenceManagerFactory.cancel();
			timerCheckPersistenceManagerFactory = null;
		}

		timerCheckPersistenceManagerFactory = new Timer();
		timerCheckPersistenceManagerFactory.schedule(new TimerTask() {
			public void run()
			{
				try {
					if (bridge == null || bridge.getPersistenceManagerFactory().isClosed()) {
						PersistenceManagerFactory pmf = JFireServerManagerFactoryImpl.getPersistenceManagerFactory(organisationID);
						if (pmf == null)
							LOGGER.warn("Old PersistenceManagerFactory for organisationID=\""+organisationID+"\" has been closed, but there is no new one!!");
						else
							setupJdoCacheBridge(pmf);
					}
				} catch (Exception e) {
					LOGGER.error("Checking PersistenceManagerFactory failed for organisationID=\""+organisationID+"\"!", e);
				}
			}
		}, 0, 30000); // TODO should be configurable
	}

	public void close()
	{
		if (bridge != null)
			bridge.close();
	}
}
