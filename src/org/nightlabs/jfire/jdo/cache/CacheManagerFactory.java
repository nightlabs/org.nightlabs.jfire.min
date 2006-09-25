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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.AuthCallbackHandler;
import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.jdo.cache.bridge.JdoCacheBridge;
import org.nightlabs.jfire.jdo.notification.AbsoluteFilterID;
import org.nightlabs.jfire.jdo.notification.FilterRegistry;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleRemoteEvent;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.config.OrganisationCf;
import org.nightlabs.jfire.servermanager.ra.JFireServerManagerFactoryImpl;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class CacheManagerFactory
		implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger
			.getLogger(CacheManagerFactory.class);

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

	protected static class NotificationThread
			extends Thread
	{
		private static volatile int nextID = 0;

		private CacheManagerFactory cacheManagerFactory;

		public NotificationThread(CacheManagerFactory cacheManagerFactory)
		{
			this.cacheManagerFactory = cacheManagerFactory;
			setName("CacheManagerFactory.NotificationThread-" + (nextID++) + " ("
					+ cacheManagerFactory.organisationID + ')');
		}

		/**
		 * @see java.lang.Thread#run()
		 */
		public void run()
		{
			while (!isInterrupted()) {
				try {

					try {
						sleep(cacheManagerFactory.getCacheCfMod()
								.getNotificationIntervalMSec());
					} catch (InterruptedException e) {
						// ignore
					}

					if (isInterrupted())
						break;

					cacheManagerFactory.distributeDirtyObjectIDs();

					Set cacheSessionIDs = cacheManagerFactory
							.fetchCacheSessionIDsToNotify();
					if (cacheSessionIDs != null) {
						logger.info("Found " + cacheSessionIDs.size()
								+ " CacheSessions to notify.");

						for (Iterator it = cacheSessionIDs.iterator(); it.hasNext();) {
							String cacheSessionID = (String) it.next();
							CacheSession session = cacheManagerFactory
									.getCacheSession(cacheSessionID);
							if (session == null)
								logger.error("No CacheSession found for cacheSessionID=\""
										+ cacheSessionID + "\"!");
							else {
								// if (session != null)
								session.notifyChanges();
							} // if (session != null)

						} // for (Iterator it = cacheSessionIDs.iterator(); it.hasNext(); )
							// {
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

	private transient NotificationThread notificationThread = new NotificationThread(
			this);

	private CacheCfMod cacheCfMod;

	protected CacheCfMod getCacheCfMod()
	{
		return cacheCfMod;
	}

	private transient JFireServerManagerFactoryImpl jFireServerManagerFactory;

	// private J2EEAdapter j2eeVendorAdapter;

	public CacheManagerFactory(JFireServerManagerFactoryImpl jfsmf,
			InitialContext ctx, OrganisationCf organisation, CacheCfMod cacheCfMod)
			throws NamingException
	{
		this.jFireServerManagerFactory = jfsmf;
		// this.j2eeVendorAdapter = j2eeVendorAdapter;
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
			ctx.createSubcontext("java:/jfire");
		} catch (NameAlreadyBoundException e) {
			// ignore
		}

		try {
			ctx.createSubcontext("java:/jfire/cacheManagerFactory");
		} catch (NameAlreadyBoundException e) {
			// ignore
		}

		try {
			ctx.bind(getJNDIName(organisationID), this);
		} catch (NameAlreadyBoundException e) {
			ctx.rebind(getJNDIName(organisationID), this);
		}

		// // TO DO do we really want this? We don't have a plan yet on how to make
		// // the representative-organisations work.
		// if (!organisationID.equals(organisation.getMasterOrganisationID()))
		// ctx.bind(getJNDIName(organisation.getMasterOrganisationID()), this);
	}

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

	private transient FreshDirtyObjectIDContainerManagerThread freshDirtyObjectIDContainerManagerThread = new FreshDirtyObjectIDContainerManagerThread(
			this);

	protected static class FreshDirtyObjectIDContainerManagerThread
			extends Thread
	{
		private static volatile int nextID = 0;

		private CacheManagerFactory cacheManagerFactory;

		public FreshDirtyObjectIDContainerManagerThread(
				CacheManagerFactory cacheManagerFactory)
		{
			this.cacheManagerFactory = cacheManagerFactory;
			setName("CacheManagerFactory.FreshDirtyObjectIDContainerManagerThread-"
					+ (nextID++) + " (" + cacheManagerFactory.organisationID + ')');
		}

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
		public void interrupt()
		{
			terminated = true;
			super.interrupt();
		}
	}

	private transient CacheSessionContainerManagerThread cacheSessionContainerManagerThread = new CacheSessionContainerManagerThread(
			this);

	protected static class CacheSessionContainerManagerThread
			extends Thread
	{
		private static volatile int nextID = 0;

		private CacheManagerFactory cacheManagerFactory;

		public CacheSessionContainerManagerThread(
				CacheManagerFactory cacheManagerFactory)
		{
			this.cacheManagerFactory = cacheManagerFactory;
			setName("CacheManagerFactory.CacheSessionContainerManagerThread-"
					+ (nextID++) + " (" + cacheManagerFactory.organisationID + ')');
		}

		/**
		 * @see java.lang.Thread#run()
		 */
		public void run()
		{
			while (!isInterrupted()) {
				try {
					long cacheSessionContainerActivityMSec = cacheManagerFactory
							.getCacheCfMod().getCacheSessionContainerActivityMSec();
					CacheSessionContainer active = cacheManagerFactory
							.getActiveCacheSessionContainer();

					long sleepMSec = cacheSessionContainerActivityMSec
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

					active = cacheManagerFactory.getActiveCacheSessionContainer(); // should
																																					// still
																																					// be
																																					// the
																																					// same,
																																					// but
																																					// just
																																					// to
																																					// be
																																					// sure...
					if (System.currentTimeMillis() - active.getCreateDT() > cacheSessionContainerActivityMSec)
						cacheManagerFactory.rollCacheSessionContainers();

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

	private CacheSessionContainer activeCacheSessionContainer; // initialized by
																															// constructor

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
		synchronized (cacheSessionContainers) {
			CacheSessionContainer newActiveCSC = new CacheSessionContainer(this);
			logger.debug("Creating new activeCacheSessionContainer (createDT="
					+ newActiveCSC.getCreateDT() + ").");
			cacheSessionContainers.addFirst(newActiveCSC);
			activeCacheSessionContainer = newActiveCSC;

			int cacheSessionContainerCount = getCacheCfMod()
					.getCacheSessionContainerCount();
			if (cacheSessionContainerCount < 2)
				throw new IllegalStateException("cacheSessionContainerCount = "
						+ cacheSessionContainerCount + " but must be at least 2!!!");

			while (cacheSessionContainers.size() > cacheSessionContainerCount) {
				CacheSessionContainer csc = (CacheSessionContainer) cacheSessionContainers
						.removeLast();
				logger.debug("Dropping cacheSessionContainer (createDT="
						+ csc.getCreateDT() + ")");
				csc.close();
			}
		}
	}

	public String getOrganisationID()
	{
		return organisationID;
	}

	protected CacheSession createCacheSession(String sessionID, String userID)
	{
		synchronized (cacheSessions) {
			CacheSession session = (CacheSession) cacheSessions.get(sessionID);
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

			return session;
		} // synchronized (cacheSessions) {
	}

	protected CacheSession getCacheSession(String sessionID)
	{
		synchronized (cacheSessions) {
			return (CacheSession) cacheSessions.get(sessionID);
		}
	}

	protected void resubscribeAllListeners(String sessionID, String userID,
			Set<Object> subscribedObjectIDs, Collection<IJDOLifecycleListenerFilter> filters)
	{
		CacheSession session = createCacheSession(sessionID, userID);
		CacheSession.ResubscribeResult res = session.resubscribeAllListeners(subscribedObjectIDs, filters);

		for (Object objectID : res.objectIDsRemoved)
			after_removeChangeListener(sessionID, objectID);

		for (Object objectID : res.objectIDsAdded)
			after_addChangeListener(session, new ChangeListenerDescriptor(sessionID, objectID), false);

		after_addLifecycleListenerFilters(userID, res.filtersAdded);
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
			DirtyObjectIDContainer newActiveDOC = new DirtyObjectIDContainer();
			logger.debug("Creating new activeFreshDirtyObjectIDContainer (createDT="
					+ newActiveDOC.getCreateDT() + ").");
			freshDirtyObjectIDContainers.addFirst(newActiveDOC);
			activeFreshDirtyObjectIDContainer = newActiveDOC;

			int freshDirtyObjectIDContainerCount = getCacheCfMod()
					.getFreshDirtyObjectIDContainerCount();
			if (freshDirtyObjectIDContainerCount < 2)
				throw new IllegalStateException("freshDirtyObjectIDContainerCount = "
						+ freshDirtyObjectIDContainerCount + " but must be at least 2!!!");

			while (freshDirtyObjectIDContainers.size() > freshDirtyObjectIDContainerCount) {
				DirtyObjectIDContainer doc = (DirtyObjectIDContainer) freshDirtyObjectIDContainers
						.removeLast();
				logger.debug("Dropping freshDirtyObjectIDContainer (createDT="
						+ doc.getCreateDT() + ").");
				doc.close();
			}
		}
	}

	protected void addLifecycleListenerFilters(String userID, Collection<IJDOLifecycleListenerFilter> filters)
	{
		if (logger.isDebugEnabled())
			logger.debug("addLifecycleListenerFilters: filters.size="+filters.size());

		if (filters.isEmpty())
			return;

		CacheSession session = null;
		for (IJDOLifecycleListenerFilter filter : filters) {
			if (session == null || !session.getSessionID().equals(filter.getFilterID().getSessionID()))
				session = createCacheSession(filter.getFilterID().getSessionID(), userID);

			session.addFilter(filter);
		}

		after_addLifecycleListenerFilters(userID, filters); // , true);
	}

	private void after_addLifecycleListenerFilters(String userID, Collection<IJDOLifecycleListenerFilter> filters) // , boolean excludeLocalSessionFromNotification)
	{
		if (logger.isDebugEnabled())
			logger.debug("after_addLifecycleListenerFilters: filters.size="+filters.size()); //  + " excludeLocalSessionFromNotification=" + excludeLocalSessionFromNotification);

		// collect all freshDirtyObjectIDs from all freshDirtyObjectIDContainers
		// take the newest, if there exist multiple for one objectID and the same lifecylestage
		Map<DirtyObjectID.LifecycleStage, Map<Object, DirtyObjectID>> lifecycleStage2freshDirtyObjectIDsMap = new HashMap<DirtyObjectID.LifecycleStage, Map<Object,DirtyObjectID>>();
		synchronized (freshDirtyObjectIDContainers) {
			for (DirtyObjectIDContainer dirtyObjectIDContainer : freshDirtyObjectIDContainers) {
				for (Map.Entry<Object, DirtyObjectID> me : dirtyObjectIDContainer.getDirtyObjectIDs().entrySet()) {
					DirtyObjectID.LifecycleStage lifecycleStage = me.getValue().getLifecycleStage();
					Map<Object, DirtyObjectID> freshDirtyObjectIDsMap = lifecycleStage2freshDirtyObjectIDsMap.get(lifecycleStage);
					if (freshDirtyObjectIDsMap == null) {
						freshDirtyObjectIDsMap = new HashMap<Object, DirtyObjectID>();
						lifecycleStage2freshDirtyObjectIDsMap.put(lifecycleStage, freshDirtyObjectIDsMap);
					}

					if (!freshDirtyObjectIDsMap.containsKey(me.getKey()))
						freshDirtyObjectIDsMap.put(me.getKey(), me.getValue());
				}
			}
		} // synchronized (freshDirtyObjectIDContainers) {

		if (logger.isDebugEnabled()) {
			logger.debug("after_addLifecycleListenerFilters: collected freshDirtyObjectIDs:");
			for (Map.Entry<DirtyObjectID.LifecycleStage, Map<Object, DirtyObjectID>> me1 : lifecycleStage2freshDirtyObjectIDsMap.entrySet()) {
				logger.debug("after_addLifecycleListenerFilters:   lifecycleStage="+me1.getKey());
				for (DirtyObjectID dirtyObjectID : me1.getValue().values())
					logger.debug("after_addLifecycleListenerFilters:     dirtyObjectID="+dirtyObjectID);
			}

			logger.debug("after_addLifecycleListenerFilters: compiling sessionID2FilterID2FilterWithDirtyObjectIDs:");
		}

		Map<String, Map<AbsoluteFilterID, FilterWithDirtyObjectIDs>> sessionID2FilterID2FilterWithDirtyObjectIDs = null;
		for (IJDOLifecycleListenerFilter filter : filters) {
			String sessionID = filter.getFilterID().getSessionID();

			if (logger.isDebugEnabled())
				logger.debug("after_addLifecycleListenerFilters:   filterID=" + filter.getFilterID());

			FilterWithDirtyObjectIDs filterWithDirtyObjectIDs = null;
			for (Map<Object, DirtyObjectID> freshDirtyObjectIDsMap : lifecycleStage2freshDirtyObjectIDsMap.values()) {
				for (Map.Entry<Object, DirtyObjectID> me2 : freshDirtyObjectIDsMap.entrySet()) {
					Object objectID = me2.getKey();
					DirtyObjectID dirtyObjectID = me2.getValue();

					boolean lifecycleStageMatches = false;
					for (DirtyObjectID.LifecycleStage lifecycleStage : filter.getLifecycleStages()) {
						if (dirtyObjectID.getLifecycleStage() == lifecycleStage) {
							lifecycleStageMatches = true;
							break;
						}
					}
					if (!lifecycleStageMatches) {
						if (logger.isDebugEnabled())
							logger.debug("after_addLifecycleListenerFilters:     lifecycleStage does not match. filterID="+filter.getFilterID() + " dirtyObjectID=" + dirtyObjectID);
						
						continue;
					}

					Class jdoObjectClass = getClassByObjectID(objectID);

					boolean includeSubclasses = filter.includeSubclasses();
					boolean classMatches = false;
					for (Class candidateClass : filter.getCandidateClasses()) {
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

		CacheSession session = null;
		for (AbsoluteFilterID filterID : filterIDs) {
			if (session == null
					|| !session.getSessionID().equals(filterID.getSessionID()))
				session = getCacheSession(filterID.getSessionID());

			if (session != null)
				session.removeFilter(filterID);
		}
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
	protected void addChangeListener(String userID, ChangeListenerDescriptor l)
	{
		String sessionID = l.getSessionID();
		Object objectID = l.getObjectID();

		if (logger.isDebugEnabled())
			logger.debug("addChangeListener: sessionID=" + sessionID + " objectID=" + objectID);

		CacheSession session = createCacheSession(sessionID, userID);
		session.subscribeObjectID(objectID); // is synchronized itself

		after_addChangeListener(session, l, true);
	}

	private void after_addChangeListener(CacheSession session, ChangeListenerDescriptor l, boolean excludeLocalSessionFromNotification)
	{
		String sessionID = session.getSessionID();
		Object objectID = l.getObjectID();

		synchronized (listenersByObjectID) {
			Map<String, ChangeListenerDescriptor> m = (Map<String, ChangeListenerDescriptor>) listenersByObjectID.get(objectID);
			if (m == null) {
				m = new HashMap<String, ChangeListenerDescriptor>();
				listenersByObjectID.put(objectID, m);
			}
			m.put(sessionID, l);
		} // synchronized (listenersByObjectID) {

		// We cause notification, if the specified objectID became dirty (by another
		// session), lately.
		DirtyObjectID triggerNotificationDirtyObjectID = null; // if there are more
																														// than one (in
																														// multiple
																														// containers), this
																														// will be the first
																														// one found (and
																														// matching)!
		synchronized (freshDirtyObjectIDContainers) {
			for (DirtyObjectIDContainer dirtyObjectIDContainer : freshDirtyObjectIDContainers) {
				DirtyObjectID dirtyObjectID = dirtyObjectIDContainer.getDirtyObjectID(objectID);
				if (dirtyObjectID != null && dirtyObjectID.getLifecycleStage() != DirtyObjectID.LifecycleStage.NEW) {
					Set sourceSessionIDs = dirtyObjectID.getSourceSessionIDs();
					if (!excludeLocalSessionFromNotification || sourceSessionIDs.size() > 1 || !sourceSessionIDs.contains(sessionID)) {
						triggerNotificationDirtyObjectID = dirtyObjectID;
						break;
					}
				}
			} // for (DirtyObjectIDContainer dirtyObjectIDContainer : freshDirtyObjectIDContainers) {
		} // synchronized (freshDirtyObjectIDContainers) {

		if (triggerNotificationDirtyObjectID != null) {
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
		}
	}

	/**
	 * Removes a listener which has previously been added by
	 * <tt>addChangeListener(...)</tt>.
	 */
	protected void removeChangeListener(String sessionID, Object objectID)
	{
		if (logger.isDebugEnabled())
			logger.debug("removeChangeListener: sessionID="
					+ sessionID + " objectID=" + objectID);

		CacheSession session = getCacheSession(sessionID);
		if (session != null)
			session.unsubscribeObjectID(objectID); // is synchronized itself

		after_removeChangeListener(sessionID, objectID);
	}

	private void after_removeChangeListener(String sessionID, Object objectID)
	{
		synchronized (listenersByObjectID) {
			Map<String, ChangeListenerDescriptor> m = (Map<String, ChangeListenerDescriptor>) listenersByObjectID.get(objectID);
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
			logger.debug("Closing CacheSession with cacheSessionID=\""
					+ cacheSessionID + "\"");

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
	 * key: DirtyObjectID.LifecycleStage lifecycleType<br/> value: Map {<br/>
	 * key: Object objectID<br/> value: {@link DirtyObjectID} dirtyObjectID<br/> }
	 */
	private Map<DirtyObjectID.LifecycleStage, Map<Object, DirtyObjectID>> lifecycleType2objectIDsWaitingForNotification = null;

	private transient Object objectIDsWaitingForNotificationMutex = new Object();

	private void notifyLocalListeners(
			Map<DirtyObjectID.LifecycleStage, Map<Object, DirtyObjectID>> dirtyObjectIDs)
	{
		List<LocalDirtyListener> listeners = null;
		synchronized (localDirtyListenersMutex) {
			if (localDirtyListeners != null) {
				if (_localDirtyListeners == null)
					_localDirtyListeners = new LinkedList<LocalDirtyListener>(
							localDirtyListeners);

				listeners = _localDirtyListeners;
			}
		}

		if (listeners != null) {
			for (LocalDirtyListener localDirtyListener : listeners)
				localDirtyListener.notifyDirtyObjectIDs(dirtyObjectIDs);
		}
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
			Map<DirtyObjectID.LifecycleStage, Map<Object, DirtyObjectID>> dirtyObjectIDs)
	{
		if (sessionID == null)
			throw new IllegalArgumentException("sessionID is null");

		if (dirtyObjectIDs == null)
			throw new IllegalArgumentException("dirtyObjectIDs is null");

		// the dirtyObjectIDs don't have the sessionID assigned yet => assign it now
		for (Map.Entry<DirtyObjectID.LifecycleStage, Map<Object, DirtyObjectID>> me1 : dirtyObjectIDs
				.entrySet()) {
			for (DirtyObjectID dirtyObjectID : me1.getValue().values()) {
				dirtyObjectID.addSourceSessionID(sessionID);
			}
		}

		// local listeners are triggered here (i.e. during commit), because they
		// might require to be
		// done for sure. If we did it in the NotificationThread, they might be
		// never triggered,
		// in case the server is restarted.
		notifyLocalListeners(dirtyObjectIDs);

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
				for (Map.Entry<DirtyObjectID.LifecycleStage, Map<Object, DirtyObjectID>> me1 : dirtyObjectIDs
						.entrySet()) {
					DirtyObjectID.LifecycleStage lifecycleStage = me1.getKey();
					Map<Object, DirtyObjectID> m2 = me1.getValue();

					Map<Object, DirtyObjectID> objectIDsWaitingForNotification = lifecycleType2objectIDsWaitingForNotification
							.get(lifecycleStage);

					if (objectIDsWaitingForNotification == null) {
						objectIDsWaitingForNotification = m2;
						lifecycleType2objectIDsWaitingForNotification.put(lifecycleStage,
								objectIDsWaitingForNotification);
					}
					else {
						for (DirtyObjectID newDirtyObjectID : m2.values()) {
							DirtyObjectID oldDirtyObjectID = (DirtyObjectID) objectIDsWaitingForNotification
									.get(newDirtyObjectID.getObjectID());
							if (oldDirtyObjectID == null)
								objectIDsWaitingForNotification.put(newDirtyObjectID
										.getObjectID(), newDirtyObjectID);
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

	// /**
	// * Call this method to notify all interested clients about the changed
	// * JDO objects. This method is called by
	// * {@link CacheManager#addDirtyObjectIDs(Collection)}.
	// * <p>
	// * Note, that the notification of remote clients works asynchronously and
	// this method
	// * immediately returns. The {@link LocalDirtyListener}s, {@link
	// LocalNewListener}s and
	// * {@link LocalDeletedListener}s are, however, triggered already here.
	// * </p>
	// * @param sessionID The session which made the objects dirty / created them
	// / deleted them.
	// * @param objectIDs The object-ids referencing the new/changed/deleted JDO
	// objects.
	// * @param lifecycleStage Defines how referenced JDO objects have been
	// affected.
	// *
	// * TODO we should get already DirtyObjectID instances, because we otherwise
	// don't know what happened before and what after.
	// */
	// public void addDirtyObjectIDs(String sessionID, Collection<Object>
	// objectIDs, DirtyObjectID.LifecycleStage lifecycleStage)
	// {
	// if (objectIDs == null || objectIDs.isEmpty()) // to avoid unnecessary
	// errors (though null should never come here)
	// return;
	//
	// if (logger.isDebugEnabled()) {
	// logger.debug("addDirtyObjectIDs(...) called by sessionID \"" + sessionID +
	// "\" with " + objectIDs.size() + " objectIDs:");
	// for (Iterator it = objectIDs.iterator(); it.hasNext(); )
	// logger.debug(" " + it.next());
	// }
	//
	// // local listeners are triggered here (i.e. during commit), because they
	// might require to be
	// // done for sure. If we did it in the NotificationThread, they might be
	// never triggered,
	// // in case the server is restarted.
	// notifyLocalListeners(sessionID, objectIDs, lifecycleStage);
	//
	// synchronized (objectIDsWaitingForNotificationMutex) {
	// if (lifecycleType2objectIDsWaitingForNotification == null)
	// lifecycleType2objectIDsWaitingForNotification = new
	// HashMap<DirtyObjectID.LifecycleStage, Map<Object,
	// DirtyObjectID>>(DirtyObjectID.LifecycleStage.values().length);
	//
	// Map<Object, DirtyObjectID> objectIDsWaitingForNotification =
	// lifecycleType2objectIDsWaitingForNotification.get(lifecycleStage);
	//
	// if (objectIDsWaitingForNotification == null) {
	// objectIDsWaitingForNotification = new HashMap<Object,
	// DirtyObjectID>(objectIDs.size());
	// lifecycleType2objectIDsWaitingForNotification.put(lifecycleStage,
	// objectIDsWaitingForNotification);
	// }
	//
	// for (Iterator it = objectIDs.iterator(); it.hasNext(); ) {
	// Object objectID = it.next();
	// DirtyObjectID dirtyObjectID = (DirtyObjectID)
	// objectIDsWaitingForNotification.get(objectID);
	// if (dirtyObjectID != null)
	// dirtyObjectID.addSourceSessionID(sessionID);
	// else
	// objectIDsWaitingForNotification.put(objectID, new DirtyObjectID(objectID,
	// sessionID, lifecycleStage));
	// }
	// }
	// }

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

	// private LinkedList<LocalNewListener> localNewListeners = null;
	// private LinkedList<LocalNewListener> _localNewListeners = null;
	// private transient Object localNewListenersMutex = new Object();
	// public void addLocalNewListener(LocalNewListener localNewListener)
	// {
	// synchronized (localNewListenersMutex) {
	// if (localNewListeners == null)
	// localNewListeners = new LinkedList<LocalNewListener>();
	//
	// localNewListeners.add(localNewListener);
	// _localNewListeners = null;
	// }
	// }
	// public void removeLocalNewListener(LocalNewListener localNewListener)
	// {
	// synchronized (localNewListenersMutex) {
	// if (localNewListeners == null)
	// return;
	//
	// localNewListeners.remove(localNewListener);
	// _localNewListeners = null;
	//
	// if (localNewListeners.isEmpty())
	// localNewListeners = null;
	// }
	// }
	//
	// private LinkedList<LocalDeletedListener> localDeletedListeners = null;
	// private LinkedList<LocalDeletedListener> _localDeletedListeners = null;
	// private transient Object localDeletedListenersMutex = new Object();
	// public void addLocalDeletedListener(LocalDeletedListener
	// localDeletedListener)
	// {
	// synchronized (localDeletedListenersMutex) {
	// if (localDeletedListeners == null)
	// localDeletedListeners = new LinkedList<LocalDeletedListener>();
	//
	// localDeletedListeners.add(localDeletedListener);
	// _localDeletedListeners = null;
	// }
	// }
	// public void removeLocalDeletedListener(LocalDeletedListener
	// localDeletedListener)
	// {
	// synchronized (localDeletedListenersMutex) {
	// if (localDeletedListeners == null)
	// return;
	//
	// localDeletedListeners.remove(localDeletedListener);
	// _localDeletedListeners = null;
	//
	// if (localDeletedListeners.isEmpty())
	// localDeletedListeners = null;
	// }
	// }

	private DirtyObjectIDContainer activeFreshDirtyObjectIDContainer; // initialized
																																		// by
																																		// constructor

	private LinkedList<DirtyObjectIDContainer> freshDirtyObjectIDContainers = new LinkedList<DirtyObjectIDContainer>();

	protected DirtyObjectIDContainer getActiveFreshDirtyObjectIDContainer()
	{
		return activeFreshDirtyObjectIDContainer;
	}

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
		if (this.lifecycleType2objectIDsWaitingForNotification == null) { // IMHO no
																																			// sync
																																			// necessary,
																																			// because,
																																			// if this
																																			// value
																																			// is just
																																			// right
																																			// now
																																			// changing,
																																			// we can
																																			// simply
																																			// wait
																																			// for the
																																			// next
																																			// cycle.
			logger.debug("There are no objectIDs waiting for notification. Return immediately.");
			return;
		}

		Map<DirtyObjectID.LifecycleStage, Map<Object, DirtyObjectID>> lifecycleType2dirtyObjectIDs;
		synchronized (objectIDsWaitingForNotificationMutex) {
			lifecycleType2dirtyObjectIDs = this.lifecycleType2objectIDsWaitingForNotification;
			this.lifecycleType2objectIDsWaitingForNotification = null;
		}

		// *** First process the implicit listeners ***

		Map<Object, DirtyObjectID> objectIDsWaitingForNotification_new = lifecycleType2dirtyObjectIDs.get(DirtyObjectID.LifecycleStage.NEW);
		Map<Object, DirtyObjectID> objectIDsWaitingForNotification_dirty = lifecycleType2dirtyObjectIDs.get(DirtyObjectID.LifecycleStage.DIRTY);
		Map<Object, DirtyObjectID> objectIDsWaitingForNotification_deleted = lifecycleType2dirtyObjectIDs.get(DirtyObjectID.LifecycleStage.DELETED);

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
			for (Map.Entry<DirtyObjectID.LifecycleStage, Map<Object, DirtyObjectID>> me : lifecycleType2dirtyObjectIDs.entrySet()) {
				DirtyObjectID.LifecycleStage lifecycleStage = me.getKey();
				Map<Object, DirtyObjectID> objectIDsWaitingForNotification = me.getValue();

				if (lifecycleStage == DirtyObjectID.LifecycleStage.NEW)
					continue; // we don't want new ones

				if (objectIDsWaitingForNotification != null) {
					for (Iterator it = objectIDsWaitingForNotification.values().iterator(); it.hasNext(); ) {
						DirtyObjectID dirtyObjectID = (DirtyObjectID) it.next();

						Map<String, ChangeListenerDescriptor> m = listenersByObjectID.get(dirtyObjectID.getObjectID());
						if (m != null)
							interestedCacheSessionIDs.addAll(m.keySet());
					}
				}
			}
		} // synchronized (listenersByObjectID) {

		// add the DirtyObjectIDs to the found sessions
		synchronized (cacheSessions) {
			for (Iterator it = interestedCacheSessionIDs.iterator(); it.hasNext(); ) {
				String cacheSessionID = (String) it.next();
				CacheSession session = (CacheSession) cacheSessions.get(cacheSessionID);
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
			for (Iterator it = interestedCacheSessionIDs.iterator(); it.hasNext(); ) {
				String cacheSessionID = (String) it.next();
				logger.debug("      " + cacheSessionID);
			}
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
		for (Map.Entry<DirtyObjectID.LifecycleStage, Map<Object, DirtyObjectID>> me1 : lifecycleType2dirtyObjectIDs.entrySet()) {
			DirtyObjectID.LifecycleStage lifecycleStage = me1.getKey();
			Map<Object, DirtyObjectID> objectIDsWaitingForNotification = me1.getValue();

			// group the DirtyObjectIDs by the class of the corresponding JDO object
			Map<Class, LinkedList<DirtyObjectID>> class2DirtyObjectID = new HashMap<Class, LinkedList<DirtyObjectID>>();
			for (DirtyObjectID dirtyObjectID : objectIDsWaitingForNotification.values()) {
				Object objectID = dirtyObjectID.getObjectID();
				Class jdoObjectClass = getClassByObjectID(objectID);

				LinkedList<DirtyObjectID> dirtyObjectIDs = class2DirtyObjectID.get(jdoObjectClass);
				if (dirtyObjectIDs == null) {
					dirtyObjectIDs = new LinkedList<DirtyObjectID>();
					class2DirtyObjectID.put(jdoObjectClass, dirtyObjectIDs);
				}
				dirtyObjectIDs.add(dirtyObjectID);
			}

			// fetch the filters from the filterRegistry and populate
			// sessionID2FilterID2FilterWithDirtyObjectIDs
			for (Map.Entry<Class, LinkedList<DirtyObjectID>> me2 : class2DirtyObjectID.entrySet()) {
				Class jdoObjectClass = me2.getKey();
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
						Lookup lookup = new Lookup(organisationID);
						PersistenceManager pm = lookup.getPersistenceManager();
						try {
							for (FilterWithDirtyObjectIDs filterWithDirtyObjectIDs : filterID2FilterWithDirtyObjectIDs.values()) {
								JDOLifecycleRemoteEvent event = new JDOLifecycleRemoteEvent(
										CacheManagerFactory.this,
										pm,
										filterWithDirtyObjectIDs.dirtyObjectIDsRaw);

								filterWithDirtyObjectIDs.dirtyObjectIDsFiltered = filterWithDirtyObjectIDs.filter.filter(event);
							}
						} finally {
							pm.close();
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
	// DirtyObjectID.LifecycleStage lifecycleStage,
	// AbsoluteFilterID filterID)
	// {
	// this.sessionID = sessionID;
	// this.lifecycleStage = lifecycleStage;
	// this.filterID = filterID;
	// }
	//
	// public String sessionID;
	// public DirtyObjectID.LifecycleStage lifecycleStage;
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
		 * {@link FilterRegistry#getMatchingFilters(org.nightlabs.jfire.jdo.cache.DirtyObjectID.LifecycleStage, Class)}.
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
	// // getFilterRegistry().getMatchingFilters(DirtyObjectID.LifecycleStage.NEW,
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

	/**
	 * This map caches the classes of the jdo objects referenced by already known
	 * object IDs. key: object-id, value: Class classOfJDOObject
	 */
	private Map<Object, Class> objectID2Class = new HashMap<Object, Class>();

	/**
	 * This method must be called by the {@link JdoCacheBridge} for all objectIDs
	 * it intends to notify about, BEFORE calling
	 * {@link #addDirtyObjectIDs(String, Collection, org.nightlabs.jfire.jdo.cache.DirtyObjectID.LifecycleStage)}.
	 * 
	 * @param objectID2ClassMap
	 *          A map from JDO object-id to class of the referenced JDO object.
	 */
	public void addObjectID2ClassMap(Map<Object, Class> objectID2ClassMap)
	{
		if (objectID2ClassMap == null)
			throw new IllegalArgumentException("objectID2ClassMap must not be null");

		synchronized (objectID2Class) {
			objectID2Class.putAll(objectID2ClassMap);
		}
	}

	/**
	 * This method is only usable for objectIDs that have been passed to
	 * {@link #ensureAllClassesAreKnown(Set)} before.
	 */
	public Class getClassByObjectID(Object objectID)
	{
		return getClassByObjectID(objectID, true);
	}

	public Class getClassByObjectID(Object objectID, boolean throwExceptionIfNotFound)
	{
		Class res;
		synchronized (objectID2Class) {
			res = objectID2Class.get(objectID);
		}

		if (throwExceptionIfNotFound && res == null)
			throw new IllegalStateException(
					"ObjectID is not known! It seems, addObjectID2ClassMap(...) has not been called or didn't contain all oids! objectID: "
							+ objectID);

		return res;
	}

	// private Map<AbsoluteFilterID, Map<Object, DirtyObjectID>>
	// filterID2DirtyObjectIDsToNotify = null;
	// private transient Object filterID2DirtyObjectIDsToNotifyMutex = new
	// Object();

	private long nextDirtyObjectIDSerial = -Long.MAX_VALUE;
//	private long nextDirtyObjectIDSerial = 0;


	private transient Object nextDirtyObjectIDSerialMutex = new Object();

	public long nextDirtyObjectIDSerial()
	{
		synchronized (nextDirtyObjectIDSerialMutex) {
			return nextDirtyObjectIDSerial++;
		}
	}

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

		CacheSession.DirtyObjectIDGroup dirtyObjectIDGroup = session
				.fetchDirtyObjectIDs();
		if (dirtyObjectIDGroup != null) {
			if (dirtyObjectIDGroup.dirtyObjectIDs != null) {
				TreeSet<DirtyObjectID> doids = null;
				for (Map<DirtyObjectID.LifecycleStage, DirtyObjectID> m1 : dirtyObjectIDGroup.dirtyObjectIDs
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

				for (Map.Entry<AbsoluteFilterID, Map<Object, Map<DirtyObjectID.LifecycleStage, DirtyObjectID>>> me1 : dirtyObjectIDGroup.filterID2DirtyObjectIDs
						.entrySet()) {
					Long filterID = new Long(me1.getKey().getFilterID());
					TreeSet<DirtyObjectID> doids = null;
					for (Map<DirtyObjectID.LifecycleStage, DirtyObjectID> m2 : me1
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

	private transient Timer timerCheckPersistenceManagerFactory = null;

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

		if (timerCheckPersistenceManagerFactory != null) {
			timerCheckPersistenceManagerFactory.cancel();
			timerCheckPersistenceManagerFactory = null;
		}

		timerCheckPersistenceManagerFactory = new Timer();
		timerCheckPersistenceManagerFactory.schedule(new TimerTask()
		{
			public void run()
			{
				try {
					if (bridge == null
							|| bridge.getPersistenceManagerFactory().isClosed()) {
						PersistenceManagerFactory pmf = JFireServerManagerFactoryImpl
								.getPersistenceManagerFactory(organisationID);
						if (pmf == null)
							logger.warn("Old PersistenceManagerFactory for organisationID=\""
									+ organisationID
									+ "\" has been closed, but there is no new one!!");
						else
							setupJdoCacheBridge(pmf);
					}
				} catch (Exception e) {
					logger.error(
							"Checking PersistenceManagerFactory failed for organisationID=\""
									+ organisationID + "\"!", e);
				}
			}
		}, 0, 30000); // TODO should be configurable
	}

	public void close()
	{
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
