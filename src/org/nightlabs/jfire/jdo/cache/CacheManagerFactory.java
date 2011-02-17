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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.transaction.UserTransaction;

import org.nightlabs.jdo.ObjectIDCanonicaliser;
import org.nightlabs.jfire.base.AuthCallbackHandler;
import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.jdo.cache.bridge.JdoCacheBridge;
import org.nightlabs.jfire.jdo.cache.cluster.DirtyObjectIDPropagator;
import org.nightlabs.jfire.jdo.cache.cluster.SessionDescriptor;
import org.nightlabs.jfire.jdo.cache.cluster.SessionFailOverManager;
import org.nightlabs.jfire.jdo.notification.AbsoluteFilterID;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.FilterRegistry;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleRemoteEvent;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.config.OrganisationCf;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapterException;
import org.nightlabs.jfire.servermanager.ra.JFireServerManagerFactoryImpl;
import org.nightlabs.jfire.servermanager.ra.JFireServerManagerImpl;
import org.nightlabs.util.Canonicaliser;
import org.nightlabs.util.IOUtil;
import org.nightlabs.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class CacheManagerFactory
		implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(CacheManagerFactory.class);

	public static final String JNDI_PREFIX = "java:/jfire/cacheManagerFactory/";

	public static String getJNDIName(String organisationID)
	{
		return JNDI_PREFIX + organisationID;
	}

	public static final CacheManagerFactory getCacheManagerFactory(
			InitialContext ctx, String organisationID)
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
			setDaemon(true);
			setName("CacheManagerFactory.NotificationThread-" + (nextID++) + " (" + cacheManagerFactory.organisationID + ')');
		}

		/**
		 * @see java.lang.Thread#run()
		 */
		@Override
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

					Set<String> cacheSessionIDs = cacheManagerFactory.fetchCacheSessionIDsToNotify();
					if (cacheSessionIDs != null) {
						logger.info("Found " + cacheSessionIDs.size() + " CacheSessions to notify.");

						for (String cacheSessionID : cacheSessionIDs) {
							CacheSession session = cacheManagerFactory.getCacheSession(cacheSessionID);
							if (session == null)
								logger.error("No CacheSession found for cacheSessionID=\"" + cacheSessionID + "\"!");
							else
								session.notifyChanges();

						} // for (Iterator it = cacheSessionIDs.iterator(); it.hasNext(); ) {
					} // if (cacheSessionIDs != null) {

				} catch (Throwable t) {
					logger.error("Exception in NotificationThread!", t);
				}
			} // while (!isInterrupted()) {
		}

		private volatile boolean terminated = false;

		/**
		 * This method checks not only for <tt>super.isInterrupted()</tt>, but
		 * additionally for {@link #terminated}.
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
		 * This method calls <tt>super.interrupt()</tt>, after setting
		 * {@link #terminated} to <tt>true</tt>.
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

	private void writeObject(java.io.ObjectOutputStream out)
			throws IOException
	{
		out.defaultWriteObject();
		logger.info("***********************************************************");
		logger.info("writeObject(...) called");
		logger.info("***********************************************************");
	}

	private void readObject(java.io.ObjectInputStream in)
			throws IOException, ClassNotFoundException
	{
		throw new UnsupportedOperationException(
				"I don't have any idea what I should do if this happens? Restart new threads? When does this happen? In a cluster maybe? Marco.");
		// in.defaultReadObject();
		// notificationThread.start();
		// cacheSessionContainerManagerThread.start();
		// LOGGER.info("***********************************************************");
		// LOGGER.info("readObject(...) called");
		// LOGGER.info("***********************************************************");
	}

	private transient NotificationThread notificationThread;

	private CacheCfMod cacheCfMod;

	public CacheCfMod getCacheCfMod()
	{
		return cacheCfMod;
	}

	private transient JFireServerManagerFactoryImpl jFireServerManagerFactory;

	// private J2EEAdapter j2eeVendorAdapter;

	private File sysConfigDirectory;

	public CacheManagerFactory(JFireServerManagerFactoryImpl jfsmf,
			InitialContext ctx, OrganisationCf organisation, CacheCfMod cacheCfMod, File sysConfigDirectory)
			throws NamingException, J2EEAdapterException
	{
		this.jFireServerManagerFactory = jfsmf;
		this.organisationID = organisation.getOrganisationID();
		this.cacheCfMod = cacheCfMod;
		this.sysConfigDirectory = sysConfigDirectory;

		logger.info("Creating instance of CacheManagerFactory for organisation " + organisationID);

		activeCacheSessionContainer = new CacheSessionContainer(this);
		cacheSessionContainers.addFirst(activeCacheSessionContainer);

//		freshDirtyObjectIDContainerMaster = new DirtyObjectIDContainer();
		activeFreshDirtyObjectIDContainer = new DirtyObjectIDContainer(); // freshDirtyObjectIDContainerMaster);
		freshDirtyObjectIDContainers.addFirst(activeFreshDirtyObjectIDContainer);

		String property_CacheManagerFactoryEnable_key = CacheManagerFactory.class.getName() + ".enable";
		String property_CacheManagerFactoryEnable_value = System.getProperty(property_CacheManagerFactoryEnable_key);
		if ("false".equals(property_CacheManagerFactoryEnable_value)) {
			logger.warn("The system property \"" + property_CacheManagerFactoryEnable_key + "\" has been set to \"" + property_CacheManagerFactoryEnable_value + "\"; the CacheManagerFactory will *not* be enabled!");
		}
		else {
			notificationThread = new NotificationThread(this);
			notificationThread.start();
			cacheSessionContainerManagerThread = new CacheSessionContainerManagerThread(this);
			cacheSessionContainerManagerThread.start();
			freshDirtyObjectIDContainerManagerThread = new FreshDirtyObjectIDContainerManagerThread(this);
			freshDirtyObjectIDContainerManagerThread.start();
		}

		try {
			ctx.createSubcontext("java:/jfire");
		} catch (NameAlreadyBoundException e) {
			// ignore
		}

		try {
			ctx.createSubcontext("java:/jfire/cacheManagerFactory");
		} catch (NameAlreadyBoundException e) {
			// ignore
		}

		String jndiName = getJNDIName(organisationID);
		try {
			ctx.bind(jndiName, this);
		} catch (NameAlreadyBoundException e) {
			ctx.rebind(jndiName, this);
		}

		// // TO DO do we really want this? We don't have a plan yet on how to make
		// // the representative-organisations work.
		// if (!organisationID.equals(organisation.getMasterOrganisationID()))
		// ctx.bind(getJNDIName(organisation.getMasterOrganisationID()), this);

		if (jfsmf.getJ2EEVendorAdapter().isInCluster()) {
			dirtyObjectIDPropagator = new DirtyObjectIDPropagator(this);

			if (sessionFailOverManager == null)
				sessionFailOverManager = new SessionFailOverManager();
		}

		logger.info("CacheManagerFactory for organisation " + organisationID + " instantiated and bound into JNDI " + jndiName);
	}

	private DirtyObjectIDPropagator dirtyObjectIDPropagator;
	private static SessionFailOverManager sessionFailOverManager;

	/**
	 * key: String cacheSessionID<br/> value: CacheSession cacheSession
	 */
	private Map<String, CacheSession> cacheSessions = new HashMap<String, CacheSession>();

	/**
	 * key: Object objectID<br/> value: Map listenersByCacheSessionID {<br/>
	 * key: String cacheSessionID<br/> value: ChangeListenerDescriptor
	 * changeListener<br/> }
	 */
	private Map<Object, Map<String, ChangeListenerDescriptor>> listenersByObjectID = new HashMap<Object, Map<String, ChangeListenerDescriptor>>();

	private transient FreshDirtyObjectIDContainerManagerThread freshDirtyObjectIDContainerManagerThread;

	protected static class FreshDirtyObjectIDContainerManagerThread
			extends Thread
	{
		private static volatile int nextID = 0;

		private CacheManagerFactory cacheManagerFactory;

		public FreshDirtyObjectIDContainerManagerThread(
				CacheManagerFactory cacheManagerFactory)
		{
			this.cacheManagerFactory = cacheManagerFactory;
			setDaemon(true);
			setName("CacheManagerFactory.FreshDirtyObjectIDContainerManagerThread-" + (nextID++) + " (" + cacheManagerFactory.organisationID + ')');
		}

		@Override
		public void run()
		{
			while (!isInterrupted()) {
				try {
					long freshDirtyObjectIDContainerActivityMSec = cacheManagerFactory
							.getCacheCfMod().getFreshDirtyObjectIDContainerActivityMSec();
					DirtyObjectIDContainer active = cacheManagerFactory
							.getActiveFreshDirtyObjectIDContainer();

					long sleepMSec = freshDirtyObjectIDContainerActivityMSec
							- (System.currentTimeMillis() - active.getCreateDT()) + 1;
					if (sleepMSec > 60000) // if the thread is requested to stop, we
																	// never wait longer than 1 min (we should get
																	// an InterruptedException, but to be sure)
						sleepMSec = 60000;

					try {
						if (sleepMSec > 0)
							sleep(sleepMSec);
					} catch (InterruptedException e) {
						// ignore
					}

					active = cacheManagerFactory.getActiveFreshDirtyObjectIDContainer(); // should
																																								// still
																																								// be
																																								// the
																																								// same,
																																								// but
																																								// just
																																								// to
																																								// be
																																								// sure...
					if (System.currentTimeMillis() - active.getCreateDT() > freshDirtyObjectIDContainerActivityMSec)
						cacheManagerFactory.rollFreshDirtyObjectIDContainers();

				} catch (Throwable t) {
					logger.error(
							"Exception in FreshDirtyObjectIDContainerManagerThread!", t);
				}
			} // while (!isInterrupted()) {
		}

		private volatile boolean terminated = false;

		/**
		 * This method checks not only for <tt>super.isInterrupted()</tt>, but
		 * additionally for {@link #terminated}.
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
		 * This method calls <tt>super.interrupt()</tt>, after setting
		 * {@link #terminated} to <tt>true</tt>.
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

	private transient CacheSessionContainerManagerThread cacheSessionContainerManagerThread;

	protected static class CacheSessionContainerManagerThread
			extends Thread
	{
		private static volatile int nextID = 0;

		private CacheManagerFactory cacheManagerFactory;

		public CacheSessionContainerManagerThread(
				CacheManagerFactory cacheManagerFactory)
		{
			this.cacheManagerFactory = cacheManagerFactory;
			setDaemon(true);
			setName("CacheManagerFactory.CacheSessionContainerManagerThread-" + (nextID++) + " (" + cacheManagerFactory.organisationID + ')');
		}

		/**
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run()
		{
			while (!isInterrupted()) {
				try {
					long cacheSessionContainerActivityMSec = cacheManagerFactory.getCacheCfMod().getCacheSessionContainerActivityMSec();
					CacheSessionContainer active = cacheManagerFactory.getActiveCacheSessionContainer();

					long sleepMSec = cacheSessionContainerActivityMSec - (System.currentTimeMillis() - active.getCreateDT()) + 1;
					if (sleepMSec > 60000) // if the thread is requested to stop, we
																	// never wait longer than 1 min (we should get
																	// an InterruptedException, but to be sure)
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

					if (sessionFailOverManager != null) {
						sessionFailOverManager.cleanUpPeriodically();
					}

				} catch (Throwable t) {
					logger.error("Exception in CacheSessionContainerManagerThread!", t);
				}
			} // while (!isInterrupted()) {
		}

		private volatile boolean terminated = false;

		/**
		 * This method checks not only for <tt>super.isInterrupted()</tt>, but
		 * additionally for {@link #terminated}.
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
		 * This method calls <tt>super.interrupt()</tt>, after setting
		 * {@link #terminated} to <tt>true</tt>.
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

	/**
	 * To release expired <tt>CacheSession</tt>s after a certain time, we use
	 * this rolling <tt>LinkedList</tt> of {@link CacheSessionContainer}.
	 * <p>
	 * The first <tt>CacheSessionContainer</tt> is the active one, while the
	 * last one is the oldest and will be deleted next.
	 */
	private LinkedList<CacheSessionContainer> cacheSessionContainers = new LinkedList<CacheSessionContainer>();

	private CacheSessionContainer activeCacheSessionContainer; // initialized by constructor

	protected CacheSessionContainer getActiveCacheSessionContainer()
	{
		return activeCacheSessionContainer;
	}

	/**
	 * This method creates a new active <tt>CacheSessionContainer</tt>, pushes
	 * it as first entry into the
	 * <tt>cacheSessionContainers</tt> <tt>LinkedList</tt> and drops all last
	 * containers that are more than
	 * {@link CacheCfMod#getCacheSessionContainerCount()}.
	 */
	protected void rollCacheSessionContainers()
	{
		Set<String> sessionIDs = sessionFailOverManager != null ? new HashSet<String>() : null;

		synchronized (cacheSessionContainers) {
			if (sessionIDs != null) {
				if (cacheSessionContainers.size() > 0)
					sessionIDs.addAll(cacheSessionContainers.get(0).getCacheSessionIDs());

				if (cacheSessionContainers.size() > 1)
					sessionIDs.addAll(cacheSessionContainers.get(1).getCacheSessionIDs());
			}

			CacheSessionContainer newActiveCSC = new CacheSessionContainer(this);

			logger.debug("Creating new activeCacheSessionContainer (createDT={}).", newActiveCSC.getCreateDT());

			cacheSessionContainers.addFirst(newActiveCSC);
			activeCacheSessionContainer = newActiveCSC;

			int cacheSessionContainerCount = getCacheCfMod().getCacheSessionContainerCount();
			if (cacheSessionContainerCount < 2)
				throw new IllegalStateException("cacheSessionContainerCount = "
						+ cacheSessionContainerCount + " but must be at least 2!!!");

			while (cacheSessionContainers.size() > cacheSessionContainerCount) {
				CacheSessionContainer csc = cacheSessionContainers.removeLast();
				logger.debug("Dropping cacheSessionContainer (createDT={})", csc.getCreateDT());
				csc.close();
			}
		}

		if (sessionFailOverManager != null)
			sessionFailOverManager.refreshTimestamps(sessionIDs);
	}

	public String getOrganisationID()
	{
		return organisationID;
	}

	protected CacheSession createCacheSession(String sessionID, String userID)
	{
		CacheSession session;
		SessionDescriptor sessionDescriptor = null;

		synchronized (cacheSessions) {
			session = cacheSessions.get(sessionID);
			if (session == null && sessionFailOverManager != null)
				sessionDescriptor = sessionFailOverManager.loadSession(sessionID);

			if (session == null) {
				session = new CacheSession(this, sessionID, userID);
				cacheSessions.put(sessionID, session);
				logger.debug("Created new CacheSession for sessionID=\"" + sessionID
						+ "\" userID=\"" + userID + "\" organisationID=\"" + organisationID
						+ "\"");
			}
			else {
				if (!session.getUserID().equals(userID))
					throw new IllegalStateException("UserID mismatch! sessionID=\""
							+ sessionID + "\" userID=\"" + userID + "\" organisationID=\""
							+ organisationID + "\"");
			}

			session.setCacheSessionContainer(getActiveCacheSessionContainer());

			if (sessionDescriptor != null)
				session.setVirginCacheSession(false);
		} // synchronized (cacheSessions) {

		if (sessionDescriptor != null) {
			logger.warn("createCacheSession: Fail-over from another cluster-node! If this happens often, your configuration is probably not correct (e.g. load-balancer not managing sticky sessions).");
			resubscribeAllListeners(sessionID, userID, sessionDescriptor.getSubscribedObjectIDs(), sessionDescriptor.getFilters().values());
		}

		return session;
	}

	protected void storeCacheSessionsForFailOver(Collection<? extends CacheSession> sessions)
	{
		if (sessionFailOverManager == null || sessions == null)
			return;

		for (CacheSession session : sessions)
			storeCacheSessionForFailOver(session);
	}

	protected void storeCacheSessionForFailOver(CacheSession session)
	{
		if (sessionFailOverManager == null || session == null)
			return;

		SessionDescriptor sessionDescriptor = new SessionDescriptor(session);
		sessionFailOverManager.storeSession(sessionDescriptor);
	}

	protected CacheSession getCacheSession(String sessionID)
	{
		synchronized (cacheSessions) {
			return cacheSessions.get(sessionID);
		}
	}

	protected void resubscribeAllListeners(String sessionID, String userID,
			Set<Object> subscribedObjectIDs, Collection<IJDOLifecycleListenerFilter> filters)
	{
		sessionID = canonicaliser.canonicalise(sessionID);
		userID = canonicaliser.canonicalise(userID);
		subscribedObjectIDs = canonicaliser.canonicalise(subscribedObjectIDs);
		filters = canonicaliser.canonicalise(filters);

		CacheSession session = createCacheSession(sessionID, userID);
		CacheSession.ResubscribeResult res = session.resubscribeAllListeners(subscribedObjectIDs, filters);

		for (Object objectID : res.objectIDsRemoved)
			after_removeChangeListener(sessionID, objectID);

		for (Object objectID : res.objectIDsAdded)
			after_addChangeListener(session, new ChangeListenerDescriptor(sessionID, objectID), false);

		after_addLifecycleListenerFilters(userID, res.filtersAdded);
		storeCacheSessionForFailOver(session);
	}

	/**
	 * This method creates a new active <tt>DirtyObjectIDContainer</tt>, pushes
	 * it as first entry into the
	 * <tt>freshDirtyObjectIDContainers</tt> <tt>LinkedList</tt> and drops all
	 * last containers that are more than
	 * {@link CacheCfMod#getFreshDirtyObjectIDContainerCount()}.
	 */
	protected void rollFreshDirtyObjectIDContainers()
	{
		synchronized (freshDirtyObjectIDContainers) {
			DirtyObjectIDContainer newActiveDOC = new DirtyObjectIDContainer(); // freshDirtyObjectIDContainerMaster);
			logger.debug("Creating new activeFreshDirtyObjectIDContainer (createDT="
					+ newActiveDOC.getCreateDT() + ").");
			freshDirtyObjectIDContainers.addFirst(newActiveDOC);
			activeFreshDirtyObjectIDContainer = newActiveDOC;

			int freshDirtyObjectIDContainerCount = getCacheCfMod().getFreshDirtyObjectIDContainerCount();
			if (freshDirtyObjectIDContainerCount < 2)
				throw new IllegalStateException("freshDirtyObjectIDContainerCount = " + freshDirtyObjectIDContainerCount + " but must be at least 2!!!");

			while (freshDirtyObjectIDContainers.size() > freshDirtyObjectIDContainerCount) {
				DirtyObjectIDContainer doc = freshDirtyObjectIDContainers.removeLast();
				logger.debug("Dropping freshDirtyObjectIDContainer (createDT=" + doc.getCreateDT() + ").");
				doc.close();
			}
		}
	}

	private static final Canonicaliser canonicaliser = ObjectIDCanonicaliser.sharedInstance();

	protected void addLifecycleListenerFilters(String userID, Collection<IJDOLifecycleListenerFilter> filters)
	{
		if (logger.isDebugEnabled())
			logger.debug("addLifecycleListenerFilters: filters.size="+filters.size());

		if (filters.isEmpty())
			return;

		userID = canonicaliser.canonicalise(userID);
		filters = canonicaliser.canonicalise(filters);

		Set<CacheSession> sessions = new HashSet<CacheSession>();
		CacheSession session = null;
		for (IJDOLifecycleListenerFilter filter : filters) {
			if (filter.getFilterID() == null) {
				logger.warn("addLifecycleListenerFilters: Filter has no filterID assigned: " + filter);
				continue;
			}

			if (session == null || !session.getSessionID().equals(filter.getFilterID().getSessionID()))
				session = createCacheSession(filter.getFilterID().getSessionID(), userID);

			session.addFilter(filter);
			sessions.add(session);
		}

		after_addLifecycleListenerFilters(userID, filters); // , true);
		storeCacheSessionsForFailOver(sessions);
	}

	private void after_addLifecycleListenerFilters(String userID, Collection<IJDOLifecycleListenerFilter> filters) // , boolean excludeLocalSessionFromNotification)
	{
		if (logger.isDebugEnabled())
			logger.debug("after_addLifecycleListenerFilters: filters.size="+filters.size()); //  + " excludeLocalSessionFromNotification=" + excludeLocalSessionFromNotification);

		// collect all freshDirtyObjectIDs from all freshDirtyObjectIDContainers
		// take the newest, if there exist multiple for one objectID and the same lifecylestage

//		Map<JDOLifecycleState, Map<Object, DirtyObjectID>> lifecycleStage2freshDirtyObjectIDsMap = freshDirtyObjectIDContainerMaster.getLifecycleStage2DirtyObjectIDMap();
		Map<JDOLifecycleState, Map<Object, DirtyObjectID>> lifecycleState2freshDirtyObjectIDsMap = null;
		synchronized (freshDirtyObjectIDContainers) {
			for (DirtyObjectIDContainer dirtyObjectIDContainer : freshDirtyObjectIDContainers) {
				if (lifecycleState2freshDirtyObjectIDsMap == null) {
					lifecycleState2freshDirtyObjectIDsMap = new HashMap<JDOLifecycleState, Map<Object, DirtyObjectID>>(dirtyObjectIDContainer.getLifecycleState2DirtyObjectIDMap().size());
					for (Map.Entry<JDOLifecycleState, Map<Object, DirtyObjectID>> me0 : dirtyObjectIDContainer.getLifecycleState2DirtyObjectIDMap().entrySet()) {
						lifecycleState2freshDirtyObjectIDsMap.put(me0.getKey(), Util.cloneSerializable(new HashMap<Object, DirtyObjectID>(me0.getValue())));
					}
				}
				else {
					for (Map.Entry<JDOLifecycleState, Map<Object, DirtyObjectID>> me1 : dirtyObjectIDContainer.getLifecycleState2DirtyObjectIDMap().entrySet()) {
						Map<Object, DirtyObjectID> m1 = lifecycleState2freshDirtyObjectIDsMap.get(me1.getKey());
						if (m1 == null) {
							m1 = Util.cloneSerializable(new HashMap<Object, DirtyObjectID>(me1.getValue()));
							lifecycleState2freshDirtyObjectIDsMap.put(me1.getKey(), m1);
						}
						else {
							for (Map.Entry<Object, DirtyObjectID> me2 : Util.cloneSerializable(me1.getValue()).entrySet()) {
								DirtyObjectID foundDirtyObjectID = m1.get(me2.getKey());
								if (foundDirtyObjectID == null)
									m1.put(me2.getKey(), me2.getValue());
								else {
									DirtyObjectID newDirtyObjectID = me2.getValue();
									if (foundDirtyObjectID.getSerial() < newDirtyObjectID.getSerial()) {
										newDirtyObjectID.addSourceSessionIDs(foundDirtyObjectID.getSourceSessionIDs());
										m1.put(me2.getKey(), newDirtyObjectID);
									}
									else
										foundDirtyObjectID.addSourceSessionIDs(newDirtyObjectID.getSourceSessionIDs());
								}
							}
						}
					}
				} // if (lifecycleStage2freshDirtyObjectIDsMap != null) {
			} // for (DirtyObjectIDContainer dirtyObjectIDContainer : freshDirtyObjectIDContainers) {
		} // synchronized (freshDirtyObjectIDContainers) {

		if (logger.isDebugEnabled()) {
			logger.debug("after_addLifecycleListenerFilters: fetched freshDirtyObjectIDs from master:");
			for (Map.Entry<JDOLifecycleState, Map<Object, DirtyObjectID>> me1 : lifecycleState2freshDirtyObjectIDsMap.entrySet()) {
				logger.debug("after_addLifecycleListenerFilters:   lifecycleStage="+me1.getKey());
				for (DirtyObjectID dirtyObjectID : me1.getValue().values())
					logger.debug("after_addLifecycleListenerFilters:     dirtyObjectID="+dirtyObjectID);
			}

			logger.debug("after_addLifecycleListenerFilters: compiling sessionID2FilterID2FilterWithDirtyObjectIDs:");
		}
//		List<Map<JDOLifecycleState, Map<Object, DirtyObjectID>>> lifecycleStage2freshDirtyObjectIDsMaps = new LinkedList<Map<JDOLifecycleState,Map<Object,DirtyObjectID>>>();
//		synchronized (freshDirtyObjectIDContainers) {
//			for (DirtyObjectIDContainer freshDirtyObjectIDContainer : freshDirtyObjectIDContainers) {
//				lifecycleStage2freshDirtyObjectIDsMaps.add(freshDirtyObjectIDContainer.getLifecycleStage2DirtyObjectIDMap());
//			}
//		}

		Map<String, Map<AbsoluteFilterID, FilterWithDirtyObjectIDs>> sessionID2FilterID2FilterWithDirtyObjectIDs = null;
		for (IJDOLifecycleListenerFilter filter : filters) {
			if (filter == null) {
				logger.error("filter in filters collection is null!!!", new Exception("STACKTRACE"));
				continue;
			}
			AbsoluteFilterID filterID = filter.getFilterID();
			if (filterID == null) {
				logger.error("filter.getFilterID returned null!!! filter class: " + filter.getClass().getName(), new Exception("STACKTRACE"));
				continue;
			}
			String sessionID = filterID.getSessionID();

			if (logger.isDebugEnabled())
				logger.debug("after_addLifecycleListenerFilters:   filterID=" + filter.getFilterID());

			FilterWithDirtyObjectIDs filterWithDirtyObjectIDs = null;

			for (Map<Object, DirtyObjectID> freshDirtyObjectIDsMap : lifecycleState2freshDirtyObjectIDsMap.values()) {
				for (Map.Entry<Object, DirtyObjectID> me2 : freshDirtyObjectIDsMap.entrySet()) {
					Object objectID = me2.getKey();
					DirtyObjectID dirtyObjectID = me2.getValue();

					boolean lifecycleStageMatches = false;
					for (JDOLifecycleState lifecycleState : filter.getLifecycleStates()) {
						if (dirtyObjectID.getLifecycleState() == lifecycleState) {
							lifecycleStageMatches = true;
							break;
						}
					}
					if (!lifecycleStageMatches) {
						if (logger.isDebugEnabled())
							logger.debug("after_addLifecycleListenerFilters:     lifecycleStage does not match. filterID="+filter.getFilterID() + " dirtyObjectID=" + dirtyObjectID);

						continue;
					}

					Class<?> jdoObjectClass = getClassByObjectID(objectID);

					boolean includeSubclasses = filter.includeSubclasses();
					boolean classMatches = false;
					for (Class<?> candidateClass : filter.getCandidateClasses()) {
						if (includeSubclasses) {
							if (candidateClass.isAssignableFrom(jdoObjectClass)) {
								classMatches = true;
								break;
							}
						}
						else {
							if (candidateClass == jdoObjectClass) {
								classMatches = true;
								break;
							}
						}
					}
					if (!classMatches) {
						if (logger.isDebugEnabled())
							logger.debug("after_addLifecycleListenerFilters:     class does not match. filterID="+filter.getFilterID() + " jdoClass=" + jdoObjectClass.getName() + " dirtyObjectID=" + dirtyObjectID);

						continue;
					}

					if (sessionID2FilterID2FilterWithDirtyObjectIDs == null)
						sessionID2FilterID2FilterWithDirtyObjectIDs = new HashMap<String, Map<AbsoluteFilterID, FilterWithDirtyObjectIDs>>();

					Map<AbsoluteFilterID, FilterWithDirtyObjectIDs> filterID2FilterWithDirtyObjectIDs = sessionID2FilterID2FilterWithDirtyObjectIDs.get(sessionID);
					if (filterID2FilterWithDirtyObjectIDs == null) {
						filterID2FilterWithDirtyObjectIDs = new HashMap<AbsoluteFilterID, FilterWithDirtyObjectIDs>();
						sessionID2FilterID2FilterWithDirtyObjectIDs.put(sessionID, filterID2FilterWithDirtyObjectIDs);
					}

					if (filterWithDirtyObjectIDs == null) {
						filterWithDirtyObjectIDs = new FilterWithDirtyObjectIDs();
						filterWithDirtyObjectIDs.filter = filter;
						filterWithDirtyObjectIDs.dirtyObjectIDsRaw = new LinkedList<DirtyObjectID>();
						filterID2FilterWithDirtyObjectIDs.put(filter.getFilterID(), filterWithDirtyObjectIDs);
					}

					if (logger.isDebugEnabled())
						logger.debug("after_addLifecycleListenerFilters:     raw filtering matches. filterID="+filter.getFilterID() + " dirtyObjectID=" + dirtyObjectID);

					filterWithDirtyObjectIDs.dirtyObjectIDsRaw.add(dirtyObjectID);
				} // for (Map.Entry<Object, DirtyObjectID> me2 : freshDirtyObjectIDsMap.entrySet()) {
			} // for (Map<Object, DirtyObjectID> freshDirtyObjectIDsMap : lifecycleStage2freshDirtyObjectIDsMap.values()) {
		} // for (IJDOLifecycleListenerFilter filter : filters) {

		if (sessionID2FilterID2FilterWithDirtyObjectIDs != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("after_addLifecycleListenerFilters: added " + filters.size() + " filters, and the following freshDirtyObjectIDs matched raw filtering:");
				for (Map.Entry<String, Map<AbsoluteFilterID, FilterWithDirtyObjectIDs>> me1 : sessionID2FilterID2FilterWithDirtyObjectIDs.entrySet()) {
					logger.debug("after_addLifecycleListenerFilters:   sessionID=" + me1.getKey());
					for (Map.Entry<AbsoluteFilterID, FilterWithDirtyObjectIDs> me2 : me1.getValue().entrySet()) {
						logger.debug("after_addLifecycleListenerFilters:     filterID=" + me2.getKey());
						for (DirtyObjectID dirtyObjectID : me2.getValue().dirtyObjectIDsRaw)
							logger.debug("after_addLifecycleListenerFilters:       dirtyObjectID=" + dirtyObjectID);
					}
				}
			}

			distributeDirtyObjectIDs_filter(sessionID2FilterID2FilterWithDirtyObjectIDs);
		}
		else if (logger.isDebugEnabled()) {
			logger.debug("after_addLifecycleListenerFilters: added " + filters.size() + " filters (but no freshDirtyObjectID matched raw filtering)");
			for (IJDOLifecycleListenerFilter filter : filters) {
				logger.debug("after_addLifecycleListenerFilters:   filterID=" + filter.getFilterID());
			}
		}
	}

	protected void removeLifecycleListenerFilters(Set<AbsoluteFilterID> filterIDs)
	{
		if (filterIDs.isEmpty())
			return;

		Set<CacheSession> sessions = new HashSet<CacheSession>();
		CacheSession session = null;
		for (AbsoluteFilterID filterID : filterIDs) {
			if (session == null || !session.getSessionID().equals(filterID.getSessionID()))
				session = getCacheSession(filterID.getSessionID());

			if (session != null) {
				session.removeFilter(filterID);
				sessions.add(session);
			}
		}
		storeCacheSessionsForFailOver(sessions);
	}

	/**
	 * This method adds your cache session a listener to get notified if the
	 * persistence capable class specified by <tt>objectID</tt> has been
	 * changed.
	 * <p>
	 * In case, you have already registered a listener before, this method does
	 * nothing.
	 * <p>
	 * This method implicitely opens a new {@link CacheSession} if it is not yet
	 * existing.
	 */
	protected void addChangeListeners(String userID, Collection<ChangeListenerDescriptor> listeners)
	{
		userID = canonicaliser.canonicalise(userID);
		listeners = canonicaliser.canonicalise(listeners);

		Set<CacheSession> sessions = new HashSet<CacheSession>();
		for (ChangeListenerDescriptor l : listeners) {
			String sessionID = l.getSessionID();
			Object objectID = l.getObjectID();

			if (logger.isDebugEnabled())
				logger.debug("addChangeListener: sessionID=" + sessionID + " objectID=" + objectID);

			CacheSession session = createCacheSession(sessionID, userID);
			session.subscribeObjectID(objectID); // is synchronized itself

			after_addChangeListener(session, l, true);
			sessions.add(session);
		}
		storeCacheSessionsForFailOver(sessions);
	}

	private void after_addChangeListener(CacheSession session, ChangeListenerDescriptor l, boolean excludeLocalSessionFromNotification)
	{
		excludeLocalSessionFromNotification = false; // TODO remove this - hmmmm... it seems I must never exclude local sessions! this causes notifications to get lost!

		String sessionID = session.getSessionID();
		Object objectID = l.getObjectID();

		synchronized (listenersByObjectID) {
			Map<String, ChangeListenerDescriptor> m = listenersByObjectID.get(objectID);
			if (m == null) {
				m = new HashMap<String, ChangeListenerDescriptor>();
				listenersByObjectID.put(objectID, m);
			}
			m.put(sessionID, l);
		} // synchronized (listenersByObjectID) {

		// We cause notification, if the specified objectID became dirty (by another
		// session), lately.
		DirtyObjectID triggerNotificationDirtyObjectID = null; // if there are more than one (in multiple containers), this
                                                           // will be the first one found (and matching)!
//		synchronized (freshDirtyObjectIDContainers) {
//			for (DirtyObjectIDContainer dirtyObjectIDContainer : freshDirtyObjectIDContainers) {
//				DirtyObjectID dirtyObjectID = dirtyObjectIDContainer.getDirtyObjectID(objectID);
//				if (dirtyObjectID.getLifecycleStage() == JDOLifecycleState.NEW) {
//
//				}
//				else if (dirtyObjectID != null) {
//					Set sourceSessionIDs = dirtyObjectID.getSourceSessionIDs();
//
//					if (!excludeLocalSessionFromNotification || sourceSessionIDs.size() > 1 || !sourceSessionIDs.contains(sessionID)) {
//						triggerNotificationDirtyObjectID = dirtyObjectID;
//						break;
//					}
//				}
//			} // for (DirtyObjectIDContainer dirtyObjectIDContainer : freshDirtyObjectIDContainers) {
//		} // synchronized (freshDirtyObjectIDContainers) {

		// there can be a listener on a new object, if the object has been deleted and recreated with the same ID!!!
		// we find the newest dirtyObjectID
		DirtyObjectID triggerNotificationDirtyObjectID_new = null; // freshDirtyObjectIDContainerMaster.getDirtyObjectID(JDOLifecycleState.NEW, objectID);
		DirtyObjectID triggerNotificationDirtyObjectID_dirty = null; // freshDirtyObjectIDContainerMaster.getDirtyObjectID(JDOLifecycleState.DIRTY, objectID);
		DirtyObjectID triggerNotificationDirtyObjectID_deleted = null; // freshDirtyObjectIDContainerMaster.getDirtyObjectID(JDOLifecycleState.DELETED, objectID);
		synchronized (freshDirtyObjectIDContainers) {
			for (DirtyObjectIDContainer dirtyObjectIDContainer : freshDirtyObjectIDContainers) {
				if (triggerNotificationDirtyObjectID_new == null)
					triggerNotificationDirtyObjectID_new = dirtyObjectIDContainer.getDirtyObjectID(JDOLifecycleState.NEW, objectID);

				if (triggerNotificationDirtyObjectID_dirty == null)
					triggerNotificationDirtyObjectID_dirty = dirtyObjectIDContainer.getDirtyObjectID(JDOLifecycleState.DIRTY, objectID);

				if (triggerNotificationDirtyObjectID_deleted == null)
					triggerNotificationDirtyObjectID_deleted = dirtyObjectIDContainer.getDirtyObjectID(JDOLifecycleState.DELETED, objectID);
			}
		}

		triggerNotificationDirtyObjectID = triggerNotificationDirtyObjectID_new;

		if (triggerNotificationDirtyObjectID == null)
			triggerNotificationDirtyObjectID = triggerNotificationDirtyObjectID_dirty;
		else if (triggerNotificationDirtyObjectID_dirty != null) {
			if (triggerNotificationDirtyObjectID_dirty.getSerial() > triggerNotificationDirtyObjectID.getSerial())
				triggerNotificationDirtyObjectID = triggerNotificationDirtyObjectID_dirty;
		}

		if (triggerNotificationDirtyObjectID == null)
			triggerNotificationDirtyObjectID = triggerNotificationDirtyObjectID_deleted;
		else if (triggerNotificationDirtyObjectID_deleted != null) {
			if (triggerNotificationDirtyObjectID_deleted.getSerial() > triggerNotificationDirtyObjectID.getSerial())
				triggerNotificationDirtyObjectID = triggerNotificationDirtyObjectID_deleted;
		}

		triggerNotification:
		if (triggerNotificationDirtyObjectID != null) {
			// we exclude the newest if that's from us - if desired
			if (excludeLocalSessionFromNotification) {
				if (triggerNotificationDirtyObjectID.getSourceSessionIDs().size() <= 1 && triggerNotificationDirtyObjectID.getSourceSessionIDs().contains(sessionID)) {
					if (logger.isDebugEnabled())
						logger.debug("after_addChangeListener: excludeLocalSessionFromNotification: " + triggerNotificationDirtyObjectID);

					break triggerNotification;
				}
			}

			if (logger.isDebugEnabled())
				logger
						.debug("after_addChangeListener: immediately notifying session "
								+ sessionID
								+ " for "
								+ objectID);

			ArrayList<DirtyObjectID> notifyDirtyObjectIDs = new ArrayList<DirtyObjectID>(1);
			notifyDirtyObjectIDs.add(triggerNotificationDirtyObjectID);
			session.addDirtyObjectIDs(notifyDirtyObjectIDs);

			// pass the intestedCacheSessionIDs to the NotificationThread
			synchronized (cacheSessionIDsToNotifyMutex) {
				if (cacheSessionIDsToNotify == null)
					cacheSessionIDsToNotify = new HashSet<String>();

				cacheSessionIDsToNotify.add(sessionID);
			}
		} // triggerNotification:
	}

	/**
	 * Removes a listener which has previously been added by
	 * <tt>addChangeListener(...)</tt>.
	 */
	protected void removeChangeListeners(Collection<ChangeListenerDescriptor> listeners)
	{
		Set<CacheSession> sessions = new HashSet<CacheSession>();
		for(ChangeListenerDescriptor l : listeners) {
			String sessionID = l.getSessionID();
			Object objectID = l.getObjectID();
			if (logger.isDebugEnabled())
				logger.debug("removeChangeListeners: sessionID="+ sessionID + " objectID=" + objectID);

			CacheSession session = getCacheSession(sessionID);
			if (session != null) {
				sessions.add(session);
				session.unsubscribeObjectID(objectID); // is synchronized itself
			}

			after_removeChangeListener(sessionID, objectID);
		}
		storeCacheSessionsForFailOver(sessions);
	}

	private void after_removeChangeListener(String sessionID, Object objectID)
	{
		synchronized (listenersByObjectID) {
			Map<String, ChangeListenerDescriptor> m = listenersByObjectID.get(objectID);
			if (m != null) {
				m.remove(sessionID);

				if (m.isEmpty())
					listenersByObjectID.remove(objectID);
			}
		} // synchronized (listenersByObjectID) {
	}

	/**
	 * This method removes all listeners that have been registered for the given
	 * <tt>cacheSessionID</tt>. The method <tt>waitForChanges(...)</tt> will
	 * be released (if it's currently waiting).
	 * <p>
	 * If there is no <tt>CacheSession</tt> existing for the given ID, this
	 * method silently returns without doing anything.
	 */
	protected void closeCacheSession(String cacheSessionID)
	{
		if (logger.isDebugEnabled())
			logger.debug("Closing CacheSession with cacheSessionID=\"" + cacheSessionID + "\"");

		CacheSession session = getCacheSession(cacheSessionID);
		if (session == null) {
			logger.error("CacheSession with cacheSessionID=\"" + cacheSessionID
					+ "\" cannot be closed, because it is unknown!");
			return;
		}

		Set<Object> subscribedObjectIDs = session.getSubscribedObjectIDs();

		session.close();

		synchronized (listenersByObjectID) {
			for (Object objectID : subscribedObjectIDs) {
				Map<String, ChangeListenerDescriptor> m = listenersByObjectID.get(objectID);
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
	 * parallelization. {@link #addDirtyObjectIDs(Collection)} will create a new
	 * <tt>HashSet</tt>, if it is <tt>null</tt>, and add all cacheSessionIDs
	 * that must be notified.
	 */
	private Set<String> cacheSessionIDsToNotify = null;

	private transient Object cacheSessionIDsToNotifyMutex = new Object();

	protected Set<String> fetchCacheSessionIDsToNotify()
	{
		synchronized (cacheSessionIDsToNotifyMutex) {
			Set<String> res = cacheSessionIDsToNotify;
			cacheSessionIDsToNotify = null;
			return res;
		}
	}

	/**
	 * key: JDOLifecycleState lifecycleType<br/> value: Map {<br/>
	 * key: Object objectID<br/> value: {@link DirtyObjectID} dirtyObjectID<br/> }
	 */
	private volatile Map<JDOLifecycleState, Map<Object, DirtyObjectID>> lifecycleType2objectIDsWaitingForNotification = null;

	private transient Object objectIDsWaitingForNotificationMutex = new Object();

	private void notifyLocalListeners(LocalDirtyEvent event)
	{
		List<LocalDirtyListener> listeners = null;
		synchronized (localDirtyListenersMutex) {
			if (localDirtyListeners != null) {
				if (_localDirtyListeners == null)
					_localDirtyListeners = new LinkedList<LocalDirtyListener>(localDirtyListeners);

				listeners = _localDirtyListeners;
			}
		}

		if (listeners != null) {
			for (LocalDirtyListener localDirtyListener : listeners)
				localDirtyListener.notifyDirtyObjectIDs(event);
		}
	}

	private UUID cacheManagerFactoryID = UUID.randomUUID();

	/**
	 * Unique identifier of this CMF instance. This ID is transient. After every reboot, there will thus be another
	 * <code>cacheManagerFactoryID</code> for the same {@link #getOrganisationID() organisationID}.
	 * In a clustered environment, every cluster node has a different <code>cacheManagerFactoryID</code>
	 * for the same organisationID, too.
	 * @return the transient ID of this <code>CacheManagerFactory</code> instance.
	 */
	public UUID getCacheManagerFactoryID() {
		return cacheManagerFactoryID;
	}

	/**
	 * Call this method to notify all interested clients about the changed JDO
	 * objects. This method is called by
	 * {@link CacheManager#addDirtyObjectIDs(Collection)}.
	 * <p>
	 * Note, that the notification of remote clients works asynchronously and this
	 * method immediately returns. The {@link LocalDirtyListener}s,
	 * {@link LocalNewListener}s and {@link LocalDeletedListener}s are, however,
	 * triggered already here.
	 * </p>
	 *
	 * @param sessionID
	 *          The session which made the objects dirty / created them / deleted
	 *          them.
	 * @param objectIDs
	 *          The object-ids referencing the new/changed/deleted JDO objects.
	 * @param lifecycleStage
	 *          Defines how referenced JDO objects have been affected.
	 */
	public void addDirtyObjectIDs(
			String sessionID,
			Map<JDOLifecycleState, Map<Object, DirtyObjectID>> dirtyObjectIDs)
	{
		if (sessionID == null)
			throw new IllegalArgumentException("sessionID is null");

		if (dirtyObjectIDs == null)
			throw new IllegalArgumentException("dirtyObjectIDs is null");

		Map<Object, Class<?>> objectID2ClassMap = new HashMap<Object, Class<?>>();

		// the dirtyObjectIDs don't have the sessionID assigned yet => assign it now
		for (Map.Entry<JDOLifecycleState, Map<Object, DirtyObjectID>> me1 : dirtyObjectIDs.entrySet()) {
			for (DirtyObjectID dirtyObjectID : me1.getValue().values()) {
				Object objectID = dirtyObjectID.getObjectID();
				dirtyObjectID.addSourceSessionID(sessionID);
				objectID2ClassMap.put(objectID, getClassByObjectID(objectID));
			}
		}

//		CacheSession cacheSession = getCacheSession(sessionID);
//		if (cacheSession == null) {
//			logger.warn("addDirtyObjectIDs: There is no CacheSession with sessionID={}!!! Skipping silently.", sessionID);
//			return;
//		}

		LocalDirtyEvent localDirtyEvent = new LocalDirtyEvent(cacheManagerFactoryID, organisationID, sessionID, dirtyObjectIDs, objectID2ClassMap);
		_addDirtyObjectIDs(localDirtyEvent);
	}

	public void addDirtyObjectIDs(LocalDirtyEvent localDirtyEvent)
	{
		addObjectID2ClassMap(localDirtyEvent.getObjectID2ClassMap());
		_addDirtyObjectIDs(localDirtyEvent);
	}

	private void _addDirtyObjectIDs(LocalDirtyEvent localDirtyEvent)
	{
		String sessionID = localDirtyEvent.getSessionID();
		Map<JDOLifecycleState, Map<Object, DirtyObjectID>> dirtyObjectIDs = localDirtyEvent.getDirtyObjectIDs();

		// local listeners are triggered here (i.e. during commit), because they
		// might require to be
		// done for sure. If we did it in the NotificationThread, they might be
		// never triggered,
		// in case the server is restarted.
		notifyLocalListeners(localDirtyEvent);

		// merge the parameter dirtyObjectIDs into
		// this.lifecycleType2objectIDsWaitingForNotification
		synchronized (objectIDsWaitingForNotificationMutex) {
			// if this.lifecycleType2objectIDsWaitingForNotification does not exist,
			// we can directly set the parameter.
			if (lifecycleType2objectIDsWaitingForNotification == null)
				lifecycleType2objectIDsWaitingForNotification = dirtyObjectIDs;
			else {
				// if this.lifecycleType2objectIDsWaitingForNotification does exist, we
				// need to merge
				for (Map.Entry<JDOLifecycleState, Map<Object, DirtyObjectID>> me1 : dirtyObjectIDs.entrySet()) {
					JDOLifecycleState lifecycleStage = me1.getKey();
					Map<Object, DirtyObjectID> m2 = me1.getValue();

					Map<Object, DirtyObjectID> objectIDsWaitingForNotification = lifecycleType2objectIDsWaitingForNotification.get(lifecycleStage);

					if (objectIDsWaitingForNotification == null) {
						objectIDsWaitingForNotification = m2;
						lifecycleType2objectIDsWaitingForNotification.put(lifecycleStage, objectIDsWaitingForNotification);
					}
					else {
						for (DirtyObjectID newDirtyObjectID : m2.values()) {
							DirtyObjectID oldDirtyObjectID = objectIDsWaitingForNotification.get(newDirtyObjectID.getObjectID());
							if (oldDirtyObjectID == null)
								objectIDsWaitingForNotification.put(newDirtyObjectID.getObjectID(), newDirtyObjectID);
							else {
								oldDirtyObjectID.addSourceSessionID(sessionID);
								oldDirtyObjectID.setSerial(newDirtyObjectID.getSerial());
							}
						}
					}
				}
			}
		} // synchronized (objectIDsWaitingForNotificationMutex) {
	}

	private LinkedList<LocalDirtyListener> localDirtyListeners = null;

	private LinkedList<LocalDirtyListener> _localDirtyListeners = null;

	private transient Object localDirtyListenersMutex = new Object();

	public void addLocalDirtyListener(LocalDirtyListener localDirtyListener)
	{
		synchronized (localDirtyListenersMutex) {
			if (localDirtyListeners == null)
				localDirtyListeners = new LinkedList<LocalDirtyListener>();

			localDirtyListeners.add(localDirtyListener);
			_localDirtyListeners = null;
		}
	}

	public void removeLocalDirtyListener(LocalDirtyListener localDirtyListener)
	{
		synchronized (localDirtyListenersMutex) {
			if (localDirtyListeners == null)
				return;

			localDirtyListeners.remove(localDirtyListener);
			_localDirtyListeners = null;

			if (localDirtyListeners.isEmpty())
				localDirtyListeners = null;
		}
	}

	private DirtyObjectIDContainer activeFreshDirtyObjectIDContainer; // initialized by constructor

	/**
	 * This is a conveyer of {@link DirtyObjectIDContainer} into which the new {@link #activeFreshDirtyObjectIDContainer}
	 * is added by {@link #rollFreshDirtyObjectIDContainers()} and the old ones are removed. When an old one is removed,
	 * all its entries are removed from {@link #freshDirtyObjectIDContainerMaster} (if they still exist and have not
	 * been overwritten).
	 */
	private LinkedList<DirtyObjectIDContainer> freshDirtyObjectIDContainers = new LinkedList<DirtyObjectIDContainer>();

	protected DirtyObjectIDContainer getActiveFreshDirtyObjectIDContainer()
	{
		return activeFreshDirtyObjectIDContainer;
	}

//	private DirtyObjectIDContainer freshDirtyObjectIDContainerMaster; // initialized by constructor

	/**
	 * This method distributes those <tt>objectIDs</tt> which have been added by
	 * {@link #addDirtyObjectIDs(Collection)} to those {@link CacheSession}s that
	 * have registered listeners for the given IDs (means the objectID is
	 * subscribed in the <tt>CacheSession</tt>).
	 * <p>
	 * Additionally, it stores the {@link DirtyObjectID}s in
	 * </p>
	 * <p>
	 * This method is called by the {@link CacheManagerFactory.NotificationThread}.
	 * This way {@link #addDirtyObjectIDs(Collection)} gets faster.
	 * </p>
	 *
	 * @param objectIDs
	 *          A <tt>Collection</tt> of JDO object IDs.
	 */
	protected void distributeDirtyObjectIDs()
	{
		if (this.lifecycleType2objectIDsWaitingForNotification == null) { // IMHO no sync necessary, because,
																																			// if this value is just right now
																																			// changing, we can simply wait
																																			// for the next cycle.
			if (logger.isDebugEnabled())
				logger.debug("There are no objectIDs waiting for notification. Return immediately.");

			return;
		}

		Map<JDOLifecycleState, Map<Object, DirtyObjectID>> lifecycleType2dirtyObjectIDs;
		synchronized (objectIDsWaitingForNotificationMutex) {
			lifecycleType2dirtyObjectIDs = this.lifecycleType2objectIDsWaitingForNotification;
			this.lifecycleType2objectIDsWaitingForNotification = null;
		}

		// *** First process the implicit listeners ***

		Map<Object, DirtyObjectID> objectIDsWaitingForNotification_new = lifecycleType2dirtyObjectIDs.get(JDOLifecycleState.NEW);
		Map<Object, DirtyObjectID> objectIDsWaitingForNotification_dirty = lifecycleType2dirtyObjectIDs.get(JDOLifecycleState.DIRTY);
		Map<Object, DirtyObjectID> objectIDsWaitingForNotification_deleted = lifecycleType2dirtyObjectIDs.get(JDOLifecycleState.DELETED);

		// No need to synchronize access to activeFreshDirtyObjectIDContainer,
		// because this is never
		// null and it doesn't matter, whether this is really the active one or we
		// missed the rolling.
		DirtyObjectIDContainer activeFreshDirtyObjectIDContainer = this.activeFreshDirtyObjectIDContainer;

		if (objectIDsWaitingForNotification_new != null)
			activeFreshDirtyObjectIDContainer.addDirtyObjectIDs(objectIDsWaitingForNotification_new.values());

		if (objectIDsWaitingForNotification_dirty != null)
			activeFreshDirtyObjectIDContainer.addDirtyObjectIDs(objectIDsWaitingForNotification_dirty.values());

		if (objectIDsWaitingForNotification_deleted != null)
			activeFreshDirtyObjectIDContainer.addDirtyObjectIDs(objectIDsWaitingForNotification_deleted.values());

		// instances of String cacheSessionID
		Set<String> interestedCacheSessionIDs = new HashSet<String>();

		synchronized (listenersByObjectID) {
			// find all CacheSessions' IDs which are interested in the changed
			// objectIDs
			for (Map.Entry<JDOLifecycleState, Map<Object, DirtyObjectID>> me : lifecycleType2dirtyObjectIDs.entrySet()) {
				JDOLifecycleState lifecycleStage = me.getKey();
				Map<Object, DirtyObjectID> objectIDsWaitingForNotification = me.getValue();

				if (lifecycleStage == JDOLifecycleState.NEW)
					continue; // we don't want new ones

				if (objectIDsWaitingForNotification != null) {
					for (DirtyObjectID dirtyObjectID : objectIDsWaitingForNotification.values()) {

						Map<String, ChangeListenerDescriptor> m = listenersByObjectID.get(dirtyObjectID.getObjectID());
						if (m != null)
							interestedCacheSessionIDs.addAll(m.keySet());
					}
				}
			}
		} // synchronized (listenersByObjectID) {

		// add the DirtyObjectIDs to the found sessions
		synchronized (cacheSessions) {
			for (String cacheSessionID : interestedCacheSessionIDs) {
				CacheSession session = cacheSessions.get(cacheSessionID);
				if (session == null)
					logger.error("Could not find CacheSession for cacheSessionID=\""+cacheSessionID+"\"!");
				else {
					if (objectIDsWaitingForNotification_dirty != null)
						session.addDirtyObjectIDs(objectIDsWaitingForNotification_dirty.values()); // this
																																												// method
																																												// picks
																																												// only
																																												// those
																																												// IDs
																																												// which
																																												// the
																																												// session
																																												// has
																																												// subscribed
					if (objectIDsWaitingForNotification_deleted != null)
						session.addDirtyObjectIDs(objectIDsWaitingForNotification_deleted.values());
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("The following CacheSessions have been marked to have changed objectIDs:");
			for (String cacheSessionID : interestedCacheSessionIDs)
				logger.debug("      " + cacheSessionID);
		}

		// pass the intestedCacheSessionIDs to the NotificationThread
		if (!interestedCacheSessionIDs.isEmpty()) {
			synchronized (cacheSessionIDsToNotifyMutex) {
				if (cacheSessionIDsToNotify == null)
					cacheSessionIDsToNotify = interestedCacheSessionIDs;
				else
					cacheSessionIDsToNotify.addAll(interestedCacheSessionIDs);
			}
		}


		// *** process the explicit listeners ***
		// The order does matter, because the clients can get notified because of
		// both, a mutex OR a timeout!
		// Thus, we need to ensure, all DirtyObjectIDs for one CacheSession are
		// pushed into the CacheSession at the same time (synchronized).
		// Otherwise the client might be notified about a "new" first and later
		// about a "delete", even though the "delete" and "new" happened
		// within one transaction, with first the object being deleted and then
		// re-created.
		// But this is a bad thing, anyway, that should be avoided by the
		// developers: Once sth. is deleted, it shouldn't be recreated with
		// the same ID. If a developer still does it
		Map<String, Map<AbsoluteFilterID, FilterWithDirtyObjectIDs>> sessionID2FilterID2FilterWithDirtyObjectIDs = new HashMap<String, Map<AbsoluteFilterID,FilterWithDirtyObjectIDs>>();
		for (Map.Entry<JDOLifecycleState, Map<Object, DirtyObjectID>> me1 : lifecycleType2dirtyObjectIDs.entrySet()) {
			JDOLifecycleState lifecycleStage = me1.getKey();
			Map<Object, DirtyObjectID> objectIDsWaitingForNotification = me1.getValue();

			// group the DirtyObjectIDs by the class of the corresponding JDO object
			Map<Class<?>, LinkedList<DirtyObjectID>> class2DirtyObjectID = new HashMap<Class<?>, LinkedList<DirtyObjectID>>();
			for (DirtyObjectID dirtyObjectID : objectIDsWaitingForNotification.values()) {
				Object objectID = dirtyObjectID.getObjectID();
				Class<?> jdoObjectClass = getClassByObjectID(objectID);

				LinkedList<DirtyObjectID> dirtyObjectIDs = class2DirtyObjectID.get(jdoObjectClass);
				if (dirtyObjectIDs == null) {
					dirtyObjectIDs = new LinkedList<DirtyObjectID>();
					class2DirtyObjectID.put(jdoObjectClass, dirtyObjectIDs);
				}
				dirtyObjectIDs.add(dirtyObjectID);
			}

			// fetch the filters from the filterRegistry and populate
			// sessionID2FilterID2FilterWithDirtyObjectIDs
			for (Map.Entry<Class<?>, LinkedList<DirtyObjectID>> me2 : class2DirtyObjectID.entrySet()) {
				Class<?> jdoObjectClass = me2.getKey();
				List<DirtyObjectID> dirtyObjectIDs = me2.getValue();

				Collection<IJDOLifecycleListenerFilter> filters = filterRegistry.getMatchingFilters(lifecycleStage, jdoObjectClass);
				for (IJDOLifecycleListenerFilter filter : filters) {
					String sessionID = filter.getFilterID().getSessionID();
					Map<AbsoluteFilterID, FilterWithDirtyObjectIDs> filterID2FilterWithDirtyObjectIDs = sessionID2FilterID2FilterWithDirtyObjectIDs.get(sessionID);
					if (filterID2FilterWithDirtyObjectIDs == null) {
						filterID2FilterWithDirtyObjectIDs = new HashMap<AbsoluteFilterID, FilterWithDirtyObjectIDs>();
						sessionID2FilterID2FilterWithDirtyObjectIDs.put(sessionID, filterID2FilterWithDirtyObjectIDs);
					}

					FilterWithDirtyObjectIDs filterWithDirtyObjectIDs = filterID2FilterWithDirtyObjectIDs.get(filter.getFilterID());
					if (filterWithDirtyObjectIDs == null) {
						filterWithDirtyObjectIDs = new FilterWithDirtyObjectIDs();
						filterWithDirtyObjectIDs.filter = filter;
						filterWithDirtyObjectIDs.dirtyObjectIDsRaw = new LinkedList<DirtyObjectID>();
						filterID2FilterWithDirtyObjectIDs.put(filter.getFilterID(), filterWithDirtyObjectIDs);
					}
					filterWithDirtyObjectIDs.dirtyObjectIDsRaw.addAll(dirtyObjectIDs);
				}
			}
		}

		distributeDirtyObjectIDs_filter(sessionID2FilterID2FilterWithDirtyObjectIDs);
	}

	private void distributeDirtyObjectIDs_filter(Map<String, Map<AbsoluteFilterID, FilterWithDirtyObjectIDs>> sessionID2FilterID2FilterWithDirtyObjectIDs)
	{
		if (logger.isDebugEnabled()) {
			logger.debug("***");
			logger.debug("distributeDirtyObjectIDs_filter(...) enter");
			logger.debug("sessionID2FilterID2FilterWithDirtyObjectIDs.size=" + sessionID2FilterID2FilterWithDirtyObjectIDs.size());
			for (Map.Entry<String, Map<AbsoluteFilterID, FilterWithDirtyObjectIDs>> me : sessionID2FilterID2FilterWithDirtyObjectIDs.entrySet()) {
				logger.debug("  sessionID=" + me.getKey() + " filterID2FilterWithDirtyObjectIDs.size=" + me.getValue().size());
			}
		} // if (logger.isDebugEnabled()) {

		try {
			// iterate all filters, authenticate as the user who owns the session and
			// execute the filter method
			Set<String> interestedCacheSessionIDs = new HashSet<String>();
			for (Map.Entry<String, Map<AbsoluteFilterID, FilterWithDirtyObjectIDs>> me : sessionID2FilterID2FilterWithDirtyObjectIDs.entrySet()) {
				CacheSession cacheSession = getCacheSession(me.getKey());
				Map<AbsoluteFilterID, FilterWithDirtyObjectIDs> filterID2FilterWithDirtyObjectIDs = me.getValue();

				LoginContext loginContext;
				JFireServerManager jfsm = jFireServerManagerFactory.getJFireServerManager();
				try {
					// we use the same sessionID, because the user cannot change anything
					// anyway (no transaction => only non-transactional reads allowed - no
					// writes)
					loginContext = new LoginContext(
							"jfire", new AuthCallbackHandler(jfsm, organisationID, cacheSession.getUserID(), cacheSession.getSessionID()));

					loginContext.login();
					try {
						boolean doCommit = false;
						boolean handleTx = false;
						UserTransaction userTransaction = getUserTransaction();
						if (!JFireServerManagerImpl.isNonTransactionalRead()) {
							handleTx = true;
							try {
								userTransaction.begin();
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						}
						try {

							Lookup lookup = new Lookup(organisationID);
							PersistenceManager pm = lookup.createPersistenceManager();
							try {
								for (FilterWithDirtyObjectIDs filterWithDirtyObjectIDs : filterID2FilterWithDirtyObjectIDs.values()) {
									JDOLifecycleRemoteEvent event = new JDOLifecycleRemoteEvent(
											CacheManagerFactory.this,
											pm,
											filterWithDirtyObjectIDs.dirtyObjectIDsRaw);

									try {
										filterWithDirtyObjectIDs.dirtyObjectIDsFiltered = filterWithDirtyObjectIDs.filter.filter(event);
									} catch (Throwable t) {
										// TODO should we add all DirtyObjectIDs if we have an exception during filtering?
										logger.error("distributeDirtyObjectIDs_filter: organisationID=" + organisationID + ": Filtering DirtyObjectIDs failed!", t);
									}
								}
							} finally {
								pm.close();
							}

							doCommit = true;
						} finally {
							if (handleTx) {
								try {
									if (doCommit)
										userTransaction.commit();
									else
										userTransaction.rollback();
								} catch (Exception e) {
									throw new RuntimeException(e);
								}
							} // if (handleTx) {
						}

					} finally {
						loginContext.logout();
					}
				} finally {
					jfsm.close();
				}

				// transfer the processed data for this session to the cacheSession
				Map<AbsoluteFilterID, Collection<DirtyObjectID>> mDest = new HashMap<AbsoluteFilterID, Collection<DirtyObjectID>>();
				for (Map.Entry<AbsoluteFilterID, FilterWithDirtyObjectIDs> me1 : filterID2FilterWithDirtyObjectIDs.entrySet()) {
					AbsoluteFilterID filterID = me1.getKey();
					FilterWithDirtyObjectIDs filterWithDirtyObjectIDs = me1.getValue();
					if (filterWithDirtyObjectIDs.dirtyObjectIDsFiltered != null && !filterWithDirtyObjectIDs.dirtyObjectIDsFiltered.isEmpty()) {
						mDest.put(filterID, filterWithDirtyObjectIDs.dirtyObjectIDsFiltered);
					}
				}

				if (!mDest.isEmpty()) {
					cacheSession.addDirtyObjectIDs(mDest);
					interestedCacheSessionIDs.add(cacheSession.getSessionID());
				}
			} // iterate CacheSessions

			if (!interestedCacheSessionIDs.isEmpty()) {
				synchronized (cacheSessionIDsToNotifyMutex) {
					if (cacheSessionIDsToNotify == null)
						cacheSessionIDsToNotify = interestedCacheSessionIDs;
					else
						cacheSessionIDsToNotify.addAll(interestedCacheSessionIDs);
				}
			}
		} catch (LoginException x) {
			throw new RuntimeException(x); // a LoginException should never happen.
		}

		if (logger.isDebugEnabled()) {
			logger.debug("distributeDirtyObjectIDs_filter(...) exit");
			logger.debug("***");
		}
	}

	// private static class FilterWithDirtyObjectIDsKey
	// {
	// public FilterWithDirtyObjectIDsKey(
	// JDOLifecycleState lifecycleStage,
	// AbsoluteFilterID filterID)
	// {
	// this.sessionID = sessionID;
	// this.lifecycleStage = lifecycleStage;
	// this.filterID = filterID;
	// }
	//
	// public String sessionID;
	// public JDOLifecycleState lifecycleStage;
	// public AbsoluteFilterID filterID;
	//
	// @Override
	// public boolean equals(Object obj)
	// {
	// if (obj == this) return true;
	// if (!(obj instanceof FilterWithDirtyObjectIDsKey)) return false;
	// FilterWithDirtyObjectIDsKey o = (FilterWithDirtyObjectIDsKey) obj;
	// return
	// Utils.equals(this.sessionID, o.sessionID) &&
	// Utils.equals(this.lifecycleStage, o.lifecycleStage) &&
	// Utils.equals(this.filterID, o.filterID);
	// }
	// @Override
	// public int hashCode()
	// {
	// return Utils.hashCode(sessionID) ^ Utils.hashCode(lifecycleStage) ^
	// Utils.hashCode(filterID);
	// }
	// }

	private static class FilterWithDirtyObjectIDs
	{
		public IJDOLifecycleListenerFilter filter;

		/**
		 * Contains all DirtyObjectIDs that might match according to the raw
		 * criteria. That means the given {@link #filter} was returned for the
		 * classes of these <code>DirtyObjectID</code>s by
		 * {@link FilterRegistry#getMatchingFilters(org.nightlabs.jfire.jdo.notification.JDOLifecycleState, Class)}.
		 */
		public List<DirtyObjectID> dirtyObjectIDsRaw;

		/**
		 * This contains the result of the
		 * {@link IJDOLifecycleListenerFilter#filter(org.nightlabs.jfire.jdo.notification.JDOLifecycleRemoteEvent)}
		 * method. Before this method is called, this field is <code>null</code>.
		 */
		public Collection<DirtyObjectID> dirtyObjectIDsFiltered;
	}

	// if an object has been removed, we cannot find out the class anymore => the
	// bridge needs to tell us!
	// protected void ensureAllClassesAreKnown(Set<Object> objectIDs)
	// {
	// Set<Object> unknownObjectIDs = null;
	// synchronized (objectID2Class) {
	// for (Object objectID : objectIDs) {
	// if (!objectID2Class.containsKey(objectID)) {
	// if (unknownObjectIDs == null)
	// unknownObjectIDs = new HashSet<Object>(objectIDs.size());
	//
	// unknownObjectIDs.add(objectID);
	// }
	// }
	// } // synchronized (objectID2Class) {
	//
	// if (unknownObjectIDs == null || unknownObjectIDs.isEmpty())
	// return;
	//
	// Map<Object, Class> newObjectID2ClassEntries = new HashMap<Object,
	// Class>(unknownObjectIDs.size());
	// try {
	// InitialContext initialContext = new InitialContext();
	// try {
	// // TransactionManager transactionManager =
	// j2eeVendorAdapter.getTransactionManager(initialContext);
	// // boolean rollback = true;
	// // transactionManager.begin();
	// // try {
	// PersistenceManagerFactory pmf =
	// (PersistenceManagerFactory)initialContext.lookup(
	// OrganisationCf.PERSISTENCE_MANAGER_FACTORY_PREFIX_ABSOLUTE +
	// organisationID);
	//
	// PersistenceManager pm = pmf.getPersistenceManager();
	// try {
	// for (Object objectID : unknownObjectIDs) {
	// Class c;
	// try {
	// Object object = pm.getObjectById(objectID);
	// c = object.getClass();
	// } catch (JDOObjectNotFoundException x) {
	// logger.error("The object has already been deleted and I cannot ");
	// continue;
	// }
	// newObjectID2ClassEntries.put(objectID, c);
	// }
	// } finally {
	// pm.close();
	// }
	// // transactionManager.commit();
	// // rollback = false;
	// // } finally {
	// // if (rollback)
	// // transactionManager.rollback();
	// // }
	// // getFilterRegistry().getMatchingFilters(JDOLifecycleState.NEW,
	// jdoObjectClass)
	// } finally {
	// initialContext.close();
	// }
	// } catch (Exception e) {
	// throw new RuntimeException(e);
	// }
	//
	// synchronized (objectID2Class) {
	// objectID2Class.putAll(newObjectID2ClassEntries);
	// }
	// }

	private static final long objectID2ClassMapLifetimeMSec = 1000L * 60L * 30L; // keep each Map active for 30 minutes
	private static final int objectID2ClassMapCount = 10; // have this number of maps in the history (i.e. 10 history + 1 active).

	private LinkedList<Map<Object, Class<?>>> objectID2ClassHistory = new LinkedList<Map<Object,Class<?>>>();

	/**
	 * This map caches the classes of the jdo objects referenced by already known
	 * object IDs. key: object-id, value: Class classOfJDOObject
	 */
	private Map<Object, Class<?>> objectID2Class = new HashMap<Object, Class<?>>();
	private long objectID2ClassCreateTimestamp = System.currentTimeMillis();

	// The Map objectID2Class becomes quite huge (just had about 240 000 entries in it). Thus, I added a mechanism to remove
	// elements again after a while. Marco.

	private void rollObjectID2ClassMapIfExpired()
	{
		synchronized (objectID2ClassHistory) {
			if (System.currentTimeMillis() - objectID2ClassCreateTimestamp < objectID2ClassMapLifetimeMSec)
				return;

			objectID2ClassHistory.addFirst(objectID2Class);
			objectID2Class = new HashMap<Object, Class<?>>();
			objectID2ClassCreateTimestamp = System.currentTimeMillis();

			while (objectID2ClassHistory.size() > objectID2ClassMapCount)
				objectID2ClassHistory.removeLast();
		}
	}

	/**
	 * This method must be called by the {@link JdoCacheBridge} for all objectIDs
	 * it intends to notify about, BEFORE calling
	 * {@link #addDirtyObjectIDs(String, Collection, org.nightlabs.jfire.jdo.notification.JDOLifecycleState)}.
	 *
	 * @param objectID2ClassMap
	 *          A map from JDO object-id to class of the referenced JDO object.
	 */
	public void addObjectID2ClassMap(Map<Object, Class<?>> objectID2ClassMap)
	{
		if (objectID2ClassMap == null)
			throw new IllegalArgumentException("objectID2ClassMap must not be null");

		synchronized (objectID2ClassHistory) {
			rollObjectID2ClassMapIfExpired();

			objectID2Class.putAll(objectID2ClassMap);
		}
	}

	/**
	 * This method is only usable for objectIDs that have been registered (via {@link #addObjectID2ClassMap(Map)}) before.
	 * It delegates to {@link #getClassByObjectID(Object, boolean)} with {@code throwExceptionIfNotFound == true}.
	 */
	public Class<?> getClassByObjectID(Object objectID)
	{
		return getClassByObjectID(objectID, true);
	}

	public Class<?> getClassByObjectID(Object objectID, boolean throwExceptionIfNotFound)
	{
		Class<?> res = null;
		synchronized (objectID2ClassHistory) {
			rollObjectID2ClassMapIfExpired();

			res = objectID2Class.get(objectID);

			if (res == null) {
				for (Map<Object, Class<?>> o2c : objectID2ClassHistory) {
					res = o2c.get(objectID);
					if (res != null) {
						objectID2Class.put(objectID, res); // refresh (put into active map)
						break;
					}
				}
			}
		}

		if (throwExceptionIfNotFound && res == null)
			throw new IllegalStateException(
					"ObjectID is not known! It seems, addObjectID2ClassMap(...) has not been called or didn't contain all oids! objectID: " + objectID
			);

		return res;
	}

	// private Map<AbsoluteFilterID, Map<Object, DirtyObjectID>>
	// filterID2DirtyObjectIDsToNotify = null;
	// private transient Object filterID2DirtyObjectIDsToNotifyMutex = new
	// Object();

	private static long DIRTY_OBJECT_ID_SERIAL_FILE_INTERVAL = 999;
	private static long MAX_DIRTY_OBJECT_ID_SERIAL = Long.MAX_VALUE - 100;
	private static long MIN_DIRTY_OBJECT_ID_SERIAL = -Long.MAX_VALUE + 1000;
	private long nextDirtyObjectIDSerial_file = -Long.MAX_VALUE;
	private long nextDirtyObjectIDSerial_ram = -Long.MAX_VALUE;
	private transient Object nextDirtyObjectIDSerialMutex = new Object();

	public long nextDirtyObjectIDSerial()
	{
//		// TODO we should initialize this ID-namespace on -Long.MAX_VALUE + 1000 (the client generates
//		// synthetic DirtyObjectIDs for dependent objects (i.e. carriers) with -Long.MAX_VALUE) and
//		// a cache size of at least 1000 (there are many many changes).
//		return IDGenerator.nextID(DirtyObjectID.class.getName());
		long res;

		synchronized (nextDirtyObjectIDSerialMutex) {
			File f = new File(sysConfigDirectory, "nextDirtyObjectIDSerial." + organisationID + ".conf");
			File f2 = new File(sysConfigDirectory, "nextDirtyObjectIDSerial." + organisationID + ".conf.new");
			if (f2.exists()) {
				// If both files exist, we use the old one (f), because we can be pretty sure that it
				// is clean (i.e. complete and not corrupt) while the new one (f2) might be incompletely
				// written.
				// If only the new file exists, the old one was already deleted, but the new one not
				// yet renamed. This might happen in the very unlikely case that the server is killed
				// just between f.delete() and f2.renameTo(f). In this situation, we hope, the new file
				// is OK and rename it now.
				// Marco.
				Exception x;
				if (!f.exists()) {
					x = new IllegalStateException("File \""+f2.getAbsolutePath()+"\" exists, but \"" + f.getAbsolutePath() + "\" does not exist! Seems, the server was interrupted between deleting the old version and renaming the new one! Will try to do the renaming now.");
					f2.renameTo(f);
					if (!f.exists() || f2.exists())
						throw new IllegalStateException("Renaming the new file \""+f2.getAbsolutePath()+"\" to \""+f.getAbsolutePath()+"\" failed!");
				}
				else
					x = new IllegalStateException("File \""+f2.getAbsolutePath()+"\" exists! Seems, there was a problem with deleting the old version or renaming the new one!");

				logger.warn(x.getMessage(), x);
			}

			try {
				if (nextDirtyObjectIDSerial_ram == -Long.MAX_VALUE) {
					if (f.exists()) {
						String s = IOUtil.readTextFile(f);
						nextDirtyObjectIDSerial_file = Long.parseLong(s);
						nextDirtyObjectIDSerial_ram = nextDirtyObjectIDSerial_file;
					}
					else {
						nextDirtyObjectIDSerial_ram = MIN_DIRTY_OBJECT_ID_SERIAL;
					}
				} // if (nextDirtyObjectIDSerial_ram == -Long.MAX_VALUE) {

				res = nextDirtyObjectIDSerial_ram++;

				// it should never happen in our life-time, but in case the serial gets too big, we restart with the min-value
				if (nextDirtyObjectIDSerial_ram > MAX_DIRTY_OBJECT_ID_SERIAL) {
					nextDirtyObjectIDSerial_ram = MIN_DIRTY_OBJECT_ID_SERIAL;
					nextDirtyObjectIDSerial_file = nextDirtyObjectIDSerial_ram;
					res = nextDirtyObjectIDSerial_ram++;
				}

				// as soon as the ram-value gets higher than the value stored in the file, we rewrite the file
				if (nextDirtyObjectIDSerial_file < nextDirtyObjectIDSerial_ram) {
					nextDirtyObjectIDSerial_file = nextDirtyObjectIDSerial_ram + DIRTY_OBJECT_ID_SERIAL_FILE_INTERVAL;
					Writer w = new OutputStreamWriter(new FileOutputStream(f2), IOUtil.CHARSET_UTF_8);
					try {
						w.write(Long.toString(nextDirtyObjectIDSerial_file));
					} finally {
						w.close();
					}
					f.delete();
					f2.renameTo(f);
					if (!f.exists() || f2.exists())
						throw new IllegalStateException("Deleting the active file \""+f.getAbsolutePath()+"\" or renaming the file \""+f2.getAbsolutePath()+"\" to the active file name failed!");
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} // synchronized (nextDirtyObjectIDSerialMutex) {

		return res;

		// For a reason which I don't understand, we cannot use the IDGenerator here. This causes things like this:
//	14:43:58,152 FATAL [Thread-2882] [JFireServerManagerImpl] Login failed!
//	javax.jdo.JDODataStoreException: Update request failed
//	        at org.jpox.store.rdbms.request.UpdateRequest.updateRelatedObjectsForField(UpdateRequest.java:486)
//	        at org.jpox.store.rdbms.request.UpdateRequest.execute(UpdateRequest.java:309)
//	        at org.jpox.store.rdbms.table.ClassTable.update(ClassTable.java:2573)
//	        at org.jpox.store.StoreManager.update(StoreManager.java:967)
//	        at org.jpox.state.StateManagerImpl.flush(StateManagerImpl.java:4925)
//	        at org.jpox.AbstractPersistenceManager.flush(AbstractPersistenceManager.java:3217)
//	        at org.jpox.resource.PersistenceManagerImpl.close(PersistenceManagerImpl.java:203)
//	        at org.nightlabs.jfire.servermanager.ra.JFireServerManagerImpl.login(JFireServerManagerImpl.java:463)
//	        at org.nightlabs.jfire.base.JFireServerLoginModule.login(JFireServerLoginModule.java:134)
//	        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
//	        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
//	        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//	        at java.lang.reflect.Method.invoke(Method.java:585)
//	        at javax.security.auth.login.LoginContext.invoke(LoginContext.java:769)
//	        at javax.security.auth.login.LoginContext.access$000(LoginContext.java:186)
//	        at javax.security.auth.login.LoginContext$4.run(LoginContext.java:683)
//	        at java.security.AccessController.doPrivileged(Native Method)
//	        at javax.security.auth.login.LoginContext.invokePriv(LoginContext.java:680)
//	        at javax.security.auth.login.LoginContext.login(LoginContext.java:579)
//	        at org.jboss.security.plugins.JaasSecurityManager.defaultLogin(JaasSecurityManager.java:601)
//	        at org.jboss.security.plugins.JaasSecurityManager.authenticate(JaasSecurityManager.java:535)
//	        at org.jboss.security.plugins.JaasSecurityManager.isValid(JaasSecurityManager.java:344)
//	        at org.jboss.ejb.plugins.SecurityInterceptor.checkSecurityAssociation(SecurityInterceptor.java:211)
//	        at org.jboss.ejb.plugins.SecurityInterceptor.invokeHome(SecurityInterceptor.java:135)
//	        at org.jboss.ejb.plugins.LogInterceptor.invokeHome(LogInterceptor.java:132)
//	        at org.jboss.ejb.plugins.ProxyFactoryFinderInterceptor.invokeHome(ProxyFactoryFinderInterceptor.java:107)
//	        at org.jboss.ejb.SessionContainer.internalInvokeHome(SessionContainer.java:637)
//	        at org.jboss.ejb.Container.invoke(Container.java:975)
//	        at org.jboss.ejb.plugins.local.BaseLocalProxyFactory.invokeHome(BaseLocalProxyFactory.java:359)
//	        at org.jboss.ejb.plugins.local.LocalHomeProxy.invoke(LocalHomeProxy.java:133)
//	        at $Proxy88.create(Unknown Source)
//	        at org.nightlabs.jfire.idgenerator.IDGeneratorServer._nextIDs(IDGeneratorServer.java:139)
//	        at org.nightlabs.jfire.idgenerator.IDGenerator.nextIDs(IDGenerator.java:127)
//	        at org.nightlabs.jfire.idgenerator.IDGenerator.nextID(IDGenerator.java:104)
//	        at org.nightlabs.jfire.idgenerator.IDGenerator.nextID(IDGenerator.java:161)
//	        at org.nightlabs.ipanema.wonderland.DataCreator.createTicketLayout(DataCreator.java:373)
//	        at org.nightlabs.ipanema.wonderland.WonderlandDatastoreInitialiserBean.initialise(WonderlandDatastoreInitialiserBean.java:602)
//	        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
//	        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
//	        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//	        at java.lang.reflect.Method.invoke(Method.java:585)
//	        at org.jboss.invocation.Invocation.performCall(Invocation.java:359)
//	        at org.jboss.ejb.StatelessSessionContainer$ContainerInterceptor.invoke(StatelessSessionContainer.java:237)
//	        at org.jboss.resource.connectionmanager.CachedConnectionInterceptor.invoke(CachedConnectionInterceptor.java:158)
//	        at org.jboss.ejb.plugins.StatelessSessionInstanceInterceptor.invoke(StatelessSessionInstanceInterceptor.java:169)
//	        at org.jboss.ws.server.ServiceEndpointInterceptor.invoke(ServiceEndpointInterceptor.java:64)
//	        at org.jboss.ejb.plugins.CallValidationInterceptor.invoke(CallValidationInterceptor.java:63)
//	        at org.jboss.ejb.plugins.AbstractTxInterceptor.invokeNext(AbstractTxInterceptor.java:121)
//	        at org.jboss.ejb.plugins.TxInterceptorCMT.runWithTransactions(TxInterceptorCMT.java:350)
//	        at org.jboss.ejb.plugins.TxInterceptorCMT.invoke(TxInterceptorCMT.java:181)
//	        at org.jboss.ejb.plugins.SecurityInterceptor.invoke(SecurityInterceptor.java:168)
//	        at org.jboss.ejb.plugins.LogInterceptor.invoke(LogInterceptor.java:205)
//	        at org.jboss.ejb.plugins.ProxyFactoryFinderInterceptor.invoke(ProxyFactoryFinderInterceptor.java:136)
//	        at org.jboss.ejb.SessionContainer.internalInvoke(SessionContainer.java:648)
//	        at org.jboss.ejb.Container.invoke(Container.java:954)
//	        at sun.reflect.GeneratedMethodAccessor110.invoke(Unknown Source)
//	        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//	        at java.lang.reflect.Method.invoke(Method.java:585)
//	        at org.jboss.mx.interceptor.ReflectedDispatcher.invoke(ReflectedDispatcher.java:155)
//	        at org.jboss.mx.server.Invocation.dispatch(Invocation.java:94)
//	        at org.jboss.mx.server.Invocation.invoke(Invocation.java:86)
//	        at org.jboss.mx.server.AbstractMBeanInvoker.invoke(AbstractMBeanInvoker.java:264)
//	        at org.jboss.mx.server.MBeanServerImpl.invoke(MBeanServerImpl.java:659)
//	        at org.jboss.invocation.local.LocalInvoker$MBeanServerAction.invoke(LocalInvoker.java:169)
//	        at org.jboss.invocation.local.LocalInvoker.invoke(LocalInvoker.java:118)
//	        at org.jboss.invocation.InvokerInterceptor.invokeLocal(InvokerInterceptor.java:206)
//	        at org.jboss.invocation.InvokerInterceptor.invoke(InvokerInterceptor.java:192)
//	        at org.jboss.proxy.TransactionInterceptor.invoke(TransactionInterceptor.java:61)
//	        at org.jboss.proxy.SecurityInterceptor.invoke(SecurityInterceptor.java:70)
//	        at org.jboss.proxy.ejb.StatelessSessionInterceptor.invoke(StatelessSessionInterceptor.java:112)
//	        at org.nightlabs.authentication.jboss.CascadedAuthenticationClientInterceptorDelegate$CapsuledCaller.run(CascadedAuthenticationClientInterceptorDelegate.java:105)
//	NestedThrowablesStackTrace:
//	javax.jdo.JDOException: Invalid state, closed or no mc
//	        at org.jpox.resource.PersistenceManagerImpl.checkStatus(PersistenceManagerImpl.java:1276)
//	        at org.jpox.resource.PersistenceManagerImpl.findStateManager(PersistenceManagerImpl.java:1199)
//	        at org.jpox.store.mapping.PersistenceCapableMapping.setObject(PersistenceCapableMapping.java:375)
//	        at org.jpox.store.rdbms.fieldmanager.ParameterSetter.storeObjectField(ParameterSetter.java:144)
//	        at org.jpox.state.StateManagerImpl.providedObjectField(StateManagerImpl.java:2771)
//	        at org.nightlabs.ipanema.ticketing.venue.VenueLayout.jdoProvideField(VenueLayout.java)
//	        at org.nightlabs.ipanema.ticketing.venue.VenueLayout.jdoProvideFields(VenueLayout.java)
//	        at org.jpox.state.StateManagerImpl.provideFields(StateManagerImpl.java:3115)
//	        at org.jpox.store.rdbms.request.InsertRequest.execute(InsertRequest.java:252)
//	        at org.jpox.store.rdbms.table.ClassTable.insert(ClassTable.java:2519)
//	        at org.jpox.store.StoreManager.insert(StoreManager.java:920)
//	        at org.jpox.state.StateManagerImpl.internalMakePersistent(StateManagerImpl.java:3667)
//	        at org.jpox.state.StateManagerImpl.makePersistent(StateManagerImpl.java:3646)
//	        at org.jpox.AbstractPersistenceManager.internalMakePersistent(AbstractPersistenceManager.java:1198)
//	        at org.jpox.AbstractPersistenceManager.makePersistentInternal(AbstractPersistenceManager.java:1243)
//	        at org.jpox.resource.PersistenceManagerImpl.makePersistentInternal(PersistenceManagerImpl.java:728)
//	        at org.jpox.store.mapping.PersistenceCapableMapping.setObject(PersistenceCapableMapping.java:450)
//	        at org.jpox.store.rdbms.fieldmanager.ParameterSetter.storeObjectField(ParameterSetter.java:144)
//	        at org.jpox.state.StateManagerImpl.providedObjectField(StateManagerImpl.java:2771)
//	        at org.nightlabs.ipanema.ticketing.store.Event.jdoProvideField(Event.java)
//	        at org.nightlabs.jfire.store.ProductType.jdoProvideFields(ProductType.java)
//	        at org.jpox.state.StateManagerImpl.provideFields(StateManagerImpl.java:3115)
//	        at org.jpox.store.rdbms.request.UpdateRequest.updateRelatedObjectsForField(UpdateRequest.java:411)
//	        at org.jpox.store.rdbms.request.UpdateRequest.execute(UpdateRequest.java:309)
//	        at org.jpox.store.rdbms.table.ClassTable.update(ClassTable.java:2573)
//	        at org.jpox.store.StoreManager.update(StoreManager.java:967)
//	        at org.jpox.state.StateManagerImpl.flush(StateManagerImpl.java:4925)
//	        at org.jpox.AbstractPersistenceManager.flush(AbstractPersistenceManager.java:3217)
//	        at org.jpox.resource.PersistenceManagerImpl.close(PersistenceManagerImpl.java:203)
//	        at org.nightlabs.jfire.servermanager.ra.JFireServerManagerImpl.login(JFireServerManagerImpl.java:463)
//	        at org.nightlabs.jfire.base.JFireServerLoginModule.login(JFireServerLoginModule.java:134)
//	        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
//	        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
//	        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//	        at java.lang.reflect.Method.invoke(Method.java:585)
//	        at javax.security.auth.login.LoginContext.invoke(LoginContext.java:769)
//	        at javax.security.auth.login.LoginContext.access$000(LoginContext.java:186)
//	        at javax.security.auth.login.LoginContext$4.run(LoginContext.java:683)
//	        at java.security.AccessController.doPrivileged(Native Method)
//	        at javax.security.auth.login.LoginContext.invokePriv(LoginContext.java:680)
//	        at javax.security.auth.login.LoginContext.login(LoginContext.java:579)
//	        at org.jboss.security.plugins.JaasSecurityManager.defaultLogin(JaasSecurityManager.java:601)
//	        at org.jboss.security.plugins.JaasSecurityManager.authenticate(JaasSecurityManager.java:535)
//	        at org.jboss.security.plugins.JaasSecurityManager.isValid(JaasSecurityManager.java:344)
//	        at org.jboss.ejb.plugins.SecurityInterceptor.checkSecurityAssociation(SecurityInterceptor.java:211)
//	        at org.jboss.ejb.plugins.SecurityInterceptor.invokeHome(SecurityInterceptor.java:135)
//	        at org.jboss.ejb.plugins.LogInterceptor.invokeHome(LogInterceptor.java:132)
//	        at org.jboss.ejb.plugins.ProxyFactoryFinderInterceptor.invokeHome(ProxyFactoryFinderInterceptor.java:107)
//	        at org.jboss.ejb.SessionContainer.internalInvokeHome(SessionContainer.java:637)
//	        at org.jboss.ejb.Container.invoke(Container.java:975)
//	        at org.jboss.ejb.plugins.local.BaseLocalProxyFactory.invokeHome(BaseLocalProxyFactory.java:359)
//	        at org.jboss.ejb.plugins.local.LocalHomeProxy.invoke(LocalHomeProxy.java:133)
//	        at $Proxy88.create(Unknown Source)
//	        at org.nightlabs.jfire.idgenerator.IDGeneratorServer._nextIDs(IDGeneratorServer.java:139)
//	        at org.nightlabs.jfire.idgenerator.IDGenerator.nextIDs(IDGenerator.java:127)
//	        at org.nightlabs.jfire.idgenerator.IDGenerator.nextID(IDGenerator.java:104)
//	        at org.nightlabs.jfire.idgenerator.IDGenerator.nextID(IDGenerator.java:161)
//	        at org.nightlabs.ipanema.wonderland.DataCreator.createTicketLayout(DataCreator.java:373)
//	        at org.nightlabs.ipanema.wonderland.WonderlandDatastoreInitialiserBean.initialise(WonderlandDatastoreInitialiserBean.java:602)
//	        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
//	        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
//	        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//	        at java.lang.reflect.Method.invoke(Method.java:585)
//	        at org.jboss.invocation.Invocation.performCall(Invocation.java:359)
//	        at org.jboss.ejb.StatelessSessionContainer$ContainerInterceptor.invoke(StatelessSessionContainer.java:237)
//	        at org.jboss.resource.connectionmanager.CachedConnectionInterceptor.invoke(CachedConnectionInterceptor.java:158)
//	        at org.jboss.ejb.plugins.StatelessSessionInstanceInterceptor.invoke(StatelessSessionInstanceInterceptor.java:169)
//	        at org.jboss.ws.server.ServiceEndpointInterceptor.invoke(ServiceEndpointInterceptor.java:64)
//	        at org.jboss.ejb.plugins.CallValidationInterceptor.invoke(CallValidationInterceptor.java:63)
//	        at org.jboss.ejb.plugins.AbstractTxInterceptor.invokeNext(AbstractTxInterceptor.java:121)
//	        at org.jboss.ejb.plugins.TxInterceptorCMT.runWithTransactions(TxInterceptorCMT.java:350)
//	        at org.jboss.ejb.plugins.TxInterceptorCMT.invoke(TxInterceptorCMT.java:181)
//	        at org.jboss.ejb.plugins.SecurityInterceptor.invoke(SecurityInterceptor.java:168)
//	        at org.jboss.ejb.plugins.LogInterceptor.invoke(LogInterceptor.java:205)
//	        at org.jboss.ejb.plugins.ProxyFactoryFinderInterceptor.invoke(ProxyFactoryFinderInterceptor.java:136)
//	        at org.jboss.ejb.SessionContainer.internalInvoke(SessionContainer.java:648)
//	        at org.jboss.ejb.Container.invoke(Container.java:954)
//	        at sun.reflect.GeneratedMethodAccessor110.invoke(Unknown Source)
//	        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
//	        at java.lang.reflect.Method.invoke(Method.java:585)
//	        at org.jboss.mx.interceptor.ReflectedDispatcher.invoke(ReflectedDispatcher.java:155)
//	        at org.jboss.mx.server.Invocation.dispatch(Invocation.java:94)
//	        at org.jboss.mx.server.Invocation.invoke(Invocation.java:86)
//	        at org.jboss.mx.server.AbstractMBeanInvoker.invoke(AbstractMBeanInvoker.java:264)
//	        at org.jboss.mx.server.MBeanServerImpl.invoke(MBeanServerImpl.java:659)
//	        at org.jboss.invocation.local.LocalInvoker$MBeanServerAction.invoke(LocalInvoker.java:169)
//	        at org.jboss.invocation.local.LocalInvoker.invoke(LocalInvoker.java:118)
//	        at org.jboss.invocation.InvokerInterceptor.invokeLocal(InvokerInterceptor.java:206)
//	        at org.jboss.invocation.InvokerInterceptor.invoke(InvokerInterceptor.java:192)
//	        at org.jboss.proxy.TransactionInterceptor.invoke(TransactionInterceptor.java:61)
//	        at org.jboss.proxy.SecurityInterceptor.invoke(SecurityInterceptor.java:70)
//	        at org.jboss.proxy.ejb.StatelessSessionInterceptor.invoke(StatelessSessionInterceptor.java:112)
//	        at org.nightlabs.authentication.jboss.CascadedAuthenticationClientInterceptorDelegate$CapsuledCaller.run(CascadedAuthenticationClientInterceptorDelegate.java:105)
	}


//	private long nextDirtyObjectIDSerial = -Long.MAX_VALUE + 1000; // the client generates synthetic DirtyObjectIDs for dependent objects (i.e. carriers) with -Long.MAX_VALUE
////	private long nextDirtyObjectIDSerial = 0;
//
//
//	private transient Object nextDirtyObjectIDSerialMutex = new Object();
//
//	public long nextDirtyObjectIDSerial()
//	{
//		synchronized (nextDirtyObjectIDSerialMutex) {
//			return nextDirtyObjectIDSerial++;
//		}
//	}

	/**
	 * This method blocks and returns not before the timeout (default is
	 * {@link #WAIT_TIMEOUT}, but can be adjusted in {@link CacheSession})
	 * occured or when <tt>closeCacheSession(...)</tt> has been called or - the
	 * main reason - at least one persistence-capable object has been changed.
	 * Because this method tries to collect multiple (by returning only at
	 * predefined time spots and by reacting only at the end of a transaction), it
	 * might return many object ids.
	 *
	 * @param userID
	 *          TODO
	 *
	 * @return Returns either <tt>null</tt> if nothing changed or a
	 *         {@link NotificationBundle} of object ids. Hence
	 *         {@link NotificationBundle#isEmpty()} will never return
	 *         <code>true</code> (<code>null</code> would have been returned
	 *         instead of a <code>NotificationBundle</code>).
	 */
	protected NotificationBundle waitForChanges(String sessionID, String userID,
			long waitTimeout)
	{
		if (logger.isDebugEnabled())
			logger.debug("waitForChanges(cacheSessionID=\"" + sessionID
					+ "\") entered");

		CacheSession session = createCacheSession(sessionID, userID);

		NotificationBundle res = new NotificationBundle();

		if (session.isVirginCacheSession()) {
			session.setVirginCacheSession(false);
			res.setVirginCacheSession(true);
		}
		else {
			// we wait only if it is NOT a virgin (otherwise, the client needs to
			// urgently re-register its listeners)
			long startDT = System.currentTimeMillis();
			session.waitForChanges(waitTimeout);
			long stopDT = System.currentTimeMillis();
			long waitTime = stopDT - startDT;

			// In case the wait returned very quickly (maybe there were changes
			// already
			// when entering waitForChanges(...), we give at least
			// notificationInterval msec
			// time for additional changes to pile up.
			long ni = getCacheCfMod().getNotificationIntervalMSec(); // because it
																																// might be
																																// changed, we
																																// store it in a
																																// temp var
			if (waitTime < ni) {
				try {
					Thread.sleep(ni - waitTime);
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}

		CacheSession.DirtyObjectIDGroup dirtyObjectIDGroup = session.fetchDirtyObjectIDs();
		if (dirtyObjectIDGroup != null) {
			if (dirtyObjectIDGroup.dirtyObjectIDs != null) {
				TreeSet<DirtyObjectID> doids = null;
				for (Map<JDOLifecycleState, DirtyObjectID> m1 : dirtyObjectIDGroup.dirtyObjectIDs
						.values()) {
					if (doids == null)
						doids = new TreeSet<DirtyObjectID>(m1.values());
					else
						doids.addAll(m1.values());
				}

				if (doids != null)
					res.setDirtyObjectIDs(doids);
			} // if (dirtyObjectIDGroup.dirtyObjectIDs != null) {

			if (dirtyObjectIDGroup.filterID2DirtyObjectIDs != null) {
				Map<Long, SortedSet<DirtyObjectID>> fid2doidMap = new HashMap<Long, SortedSet<DirtyObjectID>>();

				for (Map.Entry<AbsoluteFilterID, Map<Object, Map<JDOLifecycleState, DirtyObjectID>>> me1 : dirtyObjectIDGroup.filterID2DirtyObjectIDs
						.entrySet()) {
					Long filterID = Long.valueOf(me1.getKey().getFilterID());
					TreeSet<DirtyObjectID> doids = null;
					for (Map<JDOLifecycleState, DirtyObjectID> m2 : me1
							.getValue().values()) {
						if (doids == null)
							doids = new TreeSet<DirtyObjectID>(m2.values());
						else
							doids.addAll(m2.values());
					}

					if (doids != null)
						fid2doidMap.put(filterID, doids);
				}

				if (!fid2doidMap.isEmpty())
					res.setFilterID2dirtyObjectIDs(fid2doidMap);
			} // if (dirtyObjectIDGroup.filterID2DirtyObjectIDs != null) {
		} // if (dirtyObjectIDGroup != null) {

		// Map<Object, DirtyObjectID> dirtyObjectIDs =
		// session.fetchDirtyObjectIDs();
		//
		// if (logger.isDebugEnabled()) {
		// logger.debug("CacheSession \"" + sessionID + "\" will be notified with
		// the following objectIDs:");
		// if (dirtyObjectIDs == null)
		// logger.debug(" NONE!");
		// else {
		// for (Iterator itD = dirtyObjectIDs.values().iterator(); itD.hasNext(); )
		// {
		// DirtyObjectID dirtyObjectID = (DirtyObjectID) itD.next();
		// StringBuffer causes = new StringBuffer();
		// for (Iterator itC = dirtyObjectID.getSourceSessionIDs().iterator();
		// itC.hasNext(); ) {
		// causes.append(itC.next());
		// if (itC.hasNext())
		// causes.append(',');
		// }
		// logger.debug(" " + dirtyObjectID.getObjectID() + " (causes:
		// "+causes+")");
		// }
		// }
		// } // if (logger.isDebugEnabled()) {
		//
		// if (dirtyObjectIDs != null)
		// res.setDirtyObjectIDs(new
		// ArrayList<DirtyObjectID>(dirtyObjectIDs.values()));

		if (logger.isDebugEnabled())
			logger.debug("CacheSession \""
					+ sessionID
					+ "\" ("
					+ session.getUserID()
					+ '@'
					+ organisationID
					+ "): NotificationBundle: empty="
					+ res.isEmpty()
					+ " virginCacheSession="
					+ res.isVirginCacheSession()
					+ " dirtyObjectIDs.size="
					+ (res.getDirtyObjectIDs() == null ? null : res.getDirtyObjectIDs()
							.size())
					+ " filterID2dirtyObjectIDs.size="
					+ (res.getFilterID2dirtyObjectIDs() == null ? null : res
							.getFilterID2dirtyObjectIDs().size()));

		if (res.isEmpty())
			return null;
		else
			return res;
	}

	/**
	 * @param principal
	 *          Specifies the user who is obtaining a new
	 *          <code>CacheManager</code>.
	 * @return Returns a newly created <tt>CacheManager</tt> which can be used
	 *         to access the cache. Note, that this method does not yet open a
	 *         <tt>CacheSession</tt>.
	 */
	public CacheManager getCacheManager(JFirePrincipal principal)
	{
		return new CacheManager(this, principal);
	}

	/**
	 * @return Returns a newly created <tt>CacheManager</tt> which can be used
	 *         to access the cache. Note, that this method does not assign a
	 *         <tt>cacheSessionID</tt> and therefore most methods of the
	 *         <tt>CacheManager</tt> cannot be used!
	 */
	public CacheManager getCacheManager()
	{
		return new CacheManager(this);
	}

	private transient JdoCacheBridge bridge = null;

//	private transient Timer timerCheckPersistenceManagerFactory = null;

	public UserTransaction getUserTransaction(InitialContext initialContext)
	{
		try {
			return jFireServerManagerFactory.getJ2EEVendorAdapter().getUserTransaction(initialContext);
		} catch (Exception x) {
			throw new RuntimeException(x); // if we don't get a TransactionManager things are really odd - definitely not to be expected.
		}
	}

	public UserTransaction getUserTransaction()
	{
		try {
			InitialContext initialContext = new InitialContext();
			try {
				return getUserTransaction(initialContext);
			} finally {
//				initialContext.close(); // https://www.jfire.org/modules/bugs/view.php?id=1178
			}
		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new RuntimeException(x); // if we don't get a TransactionManager things are really odd - definitely not to be expected.
		}
	}

	public synchronized void setupJdoCacheBridge(PersistenceManagerFactory pmf)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException
	{
		if (pmf == null)
			throw new NullPointerException("pmf");

		if (bridge != null) {
			logger.info("The CacheManagerFactory for organisation \""
					+ organisationID + "\" has already a JdoCacheBridge assigned: "
					+ bridge);
			logger.info("Will close the old bridge and setup a new one.");
			bridge.close();
			bridge = null;
		}

		String jdoCacheBridgeClassName = cacheCfMod.getJdoCacheBridgeClassName();

		logger.info("Creating JdoCacheBridge \"" + jdoCacheBridgeClassName
				+ "\" for organisationID=\"" + organisationID + "\"!");

		bridge = (JdoCacheBridge) Class.forName(jdoCacheBridgeClassName)
				.newInstance();
		bridge.setCacheManagerFactory(this);
		bridge.setPersistenceManagerFactory(pmf);
		bridge.init();

//		if (timerCheckPersistenceManagerFactory != null) {
//			timerCheckPersistenceManagerFactory.cancel();
//			timerCheckPersistenceManagerFactory = null;
//		}
//
//		timerCheckPersistenceManagerFactory = new Timer();
//		timerCheckPersistenceManagerFactory.schedule(new TimerTask()
//		{
//			public void run()
//			{
//				try {
//					if (bridge == null
//							|| bridge.getPersistenceManagerFactory().isClosed()) {
//						PersistenceManagerFactory pmf = JFireServerManagerFactoryImpl
//								.getPersistenceManagerFactory(organisationID);
//						if (pmf == null)
//							logger.warn("Old PersistenceManagerFactory for organisationID=\""
//									+ organisationID
//									+ "\" has been closed, but there is no new one!!");
//						else
//							setupJdoCacheBridge(pmf);
//					}
//				} catch (Exception e) {
//					logger.error(
//							"Checking PersistenceManagerFactory failed for organisationID=\""
//									+ organisationID + "\"!", e);
//				}
//			}
//		}, 0, 30000); // TODO should be configurable

	}

	public void close()
	{
		if (dirtyObjectIDPropagator != null) {
			dirtyObjectIDPropagator.close();
			dirtyObjectIDPropagator = null;
		}

		notificationThread.interrupt();
		cacheSessionContainerManagerThread.interrupt();
		freshDirtyObjectIDContainerManagerThread.interrupt();

		if (bridge != null)
			bridge.close();

		LinkedList<CacheSessionContainer> cacheSessionContainers;
		synchronized (this.cacheSessionContainers) {
			cacheSessionContainers = new LinkedList<CacheSessionContainer>(
					this.cacheSessionContainers);
		}

		for (CacheSessionContainer cacheSessionContainer : cacheSessionContainers) {
			cacheSessionContainer.close();
		}

		filterRegistry.close();
	}

	private transient FilterRegistry filterRegistry = new FilterRegistry(this);

	public FilterRegistry getFilterRegistry()
	{
		return filterRegistry;
	}
}
