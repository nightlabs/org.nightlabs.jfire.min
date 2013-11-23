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

package org.nightlabs.jfire.jdo.notification.persistent;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.security.auth.login.LoginContext;
import javax.transaction.UserTransaction;

import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.AuthCallbackHandler;
import org.nightlabs.jfire.jdo.cache.CacheManagerFactory;
import org.nightlabs.jfire.jdo.cache.LocalDirtyEvent;
import org.nightlabs.jfire.jdo.cache.LocalDirtyListener;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.persistent.id.NotificationFilterID;
import org.nightlabs.jfire.jdo.notification.persistent.id.PushNotifierID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter;
import org.nightlabs.jfire.servermanager.ra.JFireServerManagerFactoryImpl;
import org.nightlabs.jfire.servermanager.ra.JFireServerManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistentNotificationManagerFactory implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(PersistentNotificationManagerFactory.class);

	public static final String JNDI_PREFIX = "java:/jfire/persistentNotificationManagerFactory/";

	public static String getJNDIName(String organisationID)
	{
		return JNDI_PREFIX + organisationID;
	}

	public static final PersistentNotificationManagerFactory getOrganisationSyncManagerFactory(InitialContext ctx, String organisationID) throws NamingException
	{
		return (PersistentNotificationManagerFactory) ctx.lookup(getJNDIName(organisationID));
	}

	private String organisationID;
	private transient JFireServerManagerFactory jFireServerManagerFactory;
	private transient UserTransaction userTransaction;

	private transient DirtyObjectIDBuffer dirtyObjectIDBuffer;
	private transient Timer timerOutgoing = new Timer();

	public PersistentNotificationManagerFactory(InitialContext ctx,
			String organisationID, JFireServerManagerFactory jFireServerManagerFactory,
			UserTransaction userTransaction, PersistenceManagerFactory pmf)
			throws NamingException, DirtyObjectIDBufferException
	{
		this.organisationID = organisationID;
		this.jFireServerManagerFactory = jFireServerManagerFactory;
		this.userTransaction = userTransaction;

		String dirtyObjectIDBufferClassName = DirtyObjectIDBufferFileSystem.class.getName(); // TODO read the class name from a configuration module

		try {
			this.dirtyObjectIDBuffer = (DirtyObjectIDBuffer) Class.forName(dirtyObjectIDBufferClassName).newInstance();
		} catch (Exception x) {
			throw new DirtyObjectIDBufferException(x);
		}

		this.dirtyObjectIDBuffer.init(this);

		try {
			ctx.createSubcontext("java:/jfire");
		} catch (NameAlreadyBoundException e) {
			// ignore
		}

		try {
			ctx.createSubcontext("java:/jfire/persistentNotificationManagerFactory");
		} catch (NameAlreadyBoundException e) {
			// ignore
		}

		ctx.bind(getJNDIName(organisationID), this);

		final CacheManagerFactory cacheManagerFactory = CacheManagerFactory.getCacheManagerFactory(ctx, organisationID);
		cacheManagerFactory.addLocalDirtyListener(new LocalDirtyListener() {
					public void notifyDirtyObjectIDs(LocalDirtyEvent event)
					{
						if (!cacheManagerFactory.getCacheManagerFactoryID().equals(event.getCacheManagerFactoryID())) {
							logger.debug("LocalDirtyListener.notifyDirtyObjectIDs: Ignoring dirty object IDs from ");
						}

						try {
							dirtyObjectIDBuffer.addDirtyObjectIDs(event.getDirtyObjectIDs());
						} catch (Exception x) {
							logger.error("Storing DirtyObjectIDs into temporary persistent buffer failed!", x);
						}
					}
				});

		String enableString = System.getProperty(PersistentNotificationManagerFactory.class.getName() + ".enable");
		boolean enable = !Boolean.FALSE.toString().equals(enableString);
		if (logger.isDebugEnabled())
			logger.debug(PersistentNotificationManagerFactory.class.getName() + ".enable=" + enableString);

		if (!enable)
			logger.info(PersistentNotificationManagerFactory.class.getName() + ".enable is false! Will not enable PersistentNotificationManagerFactory for organisation \"" + organisationID + "\"!");
		else {
			timerOutgoing.schedule(new TimerTask() {
				@Override
				public void run()
				{
					processDirtyObjectIDsOutgoing();
				}
			}, 0, 15000); // TODO the 15000 should come from a config module
		}
	}

	private void writeObject(java.io.ObjectOutputStream out)
	throws IOException
	{
		out.defaultWriteObject();
	}

	private void readObject(java.io.ObjectInputStream in)
	throws IOException, ClassNotFoundException
	{
		throw new UnsupportedOperationException("I don't have any idea what I should do if this happens? Restart new threads? When does this happen? In a cluster maybe? Marco.");
	}

	public String getOrganisationID()
	{
		return organisationID;
	}

	private static Map<JDOLifecycleState, Map<Object, DirtyObjectID>> condenseDirtyObjectIDCollection(Collection<Map<JDOLifecycleState, Map<Object, DirtyObjectID>>> dirtyObjectIDsRaw)
	{
		Map<JDOLifecycleState, Map<Object, DirtyObjectID>> res = new HashMap<JDOLifecycleState, Map<Object,DirtyObjectID>>();

		for (Map<JDOLifecycleState, Map<Object, DirtyObjectID>> rawM1 : dirtyObjectIDsRaw) {
			for (Map.Entry<JDOLifecycleState, Map<Object, DirtyObjectID>> rawME1 : rawM1.entrySet()) {
				Map<Object, DirtyObjectID> m1 = res.get(rawME1.getKey());
				if (m1 == null)
					res.put(rawME1.getKey(), rawME1.getValue());
				else {
					for (Map.Entry<Object, DirtyObjectID> rawME2 : rawME1.getValue().entrySet()) {

						DirtyObjectID rawDO = rawME2.getValue();
						DirtyObjectID destDO = m1.get(rawDO.getObjectID());

						if (destDO == null)
							m1.put(rawDO.getObjectID(), rawDO);
						else {
							if (destDO.getSerial() > rawDO.getSerial())
								destDO.addSourceSessionIDs(rawDO.getSourceSessionIDs());
							else {
								rawDO.addSourceSessionIDs(destDO.getSourceSessionIDs());
								m1.put(rawDO.getObjectID(), rawDO);
							}
						}

					}
				}
			}
		}

		return res;
	}

	private Class<?> pushManagerClass = null;

	private void pushNotificationBundle(NotificationBundle notificationBundle)
	throws ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException
	{
		if (pushManagerClass == null) {
			try {
				pushManagerClass = Class.forName("org.nightlabs.jfire.jdo.notification.persistent.PushManager");
			} catch (ClassNotFoundException x) {
				logger.error("Could not load class org.nightlabs.jfire.jdo.notification.persistent.PushManager! Are you sure that JFireBaseBean is deployed?", x);
				throw x;
			}
		}

		Object pushManager = pushManagerClass.newInstance();
		Method pushNotificationBundle = pushManagerClass.getMethod("pushNotificationBundle", new Class[] { NotificationBundle.class });

		pushNotificationBundle.invoke(pushManager, new Object[] { notificationBundle });
	}

	private long processDirtyObjectIDsOutgoingRunNumber = -1;

	/**
	 * This method is called by {@link #timerOutgoing} and therefore does not run concurrently.
	 */
	private void processDirtyObjectIDsOutgoing()
	{
		String logPrefix = "processDirtyObjectIDsOutgoing["+ ObjectIDUtil.longObjectIDFieldToString(++this.processDirtyObjectIDsOutgoingRunNumber) +"]: ";
		try {
			if (jFireServerManagerFactory.isShuttingDown()) {
				logger.info(logPrefix + "System is shutting down - returning immediately.");
				return;
			}

			long totalStart = System.currentTimeMillis();
			long totalDirtyObjectIDCount = 0; // the total number of dirtyObjectIDs incoming and processed
			long prefilteredGroupCount = 0; // the number of groups (for each group there is one prefiltering action)
			long filterCount = 0; // how many times was NotificationFilter.filter(...) called
			long filteredDirtyObjectIDCount = 0; // what's left after real filtering - and thus finally delivered via NotificationBundle instances

			PersistenceManagerFactory persistenceManagerFactory = JFireServerManagerFactoryImpl.getPersistenceManagerFactory(organisationID);

			// 1st step: fetch temporary dirty objectIDs and distribute the change-markers to all interested listeners

			// fetchDirtyObjectIDs() still keeps the objectIDs in case sth. goes wrong - but they're marked for removal
			Collection<Map<JDOLifecycleState, Map<Object, DirtyObjectID>>> dirtyObjectIDsRaw = dirtyObjectIDBuffer.fetchDirtyObjectIDs();

			// condense the raw objects (keep only the newest DirtyObjectID for every JDO object and every lifecycle state)
			Map<JDOLifecycleState, Map<Object, DirtyObjectID>> dirtyObjectIDsMap = condenseDirtyObjectIDCollection(dirtyObjectIDsRaw);

			// search for NotificationFilters according to class, lifecycle-state
			Map<UserID, Map<NotificationFilterID, List<DirtyObjectID>>> userID2notificationFilterID2DirtyObjectID_beforeFilter = new HashMap<UserID, Map<NotificationFilterID,List<DirtyObjectID>>>();
			{
				boolean doCommit = false;
				boolean handleTx = false;
				if (!JFireServerManagerImpl.isNonTransactionalRead()) {
					handleTx = true;
					userTransaction.begin();
				}
				try {

					PersistenceManager pm = persistenceManagerFactory.getPersistenceManager();
					try {
						// find roughly matching subscriptions (by prefiltering according to jdo-lifecycle-state & class)
						Map<String, Map<JDOLifecycleState, List<DirtyObjectID>>> className2lifecycleState2dirtyObjectIDs = new HashMap<String, Map<JDOLifecycleState,List<DirtyObjectID>>>();

						{
							long start = System.currentTimeMillis();

							for (Map.Entry<JDOLifecycleState, Map<Object, DirtyObjectID>> me1 : dirtyObjectIDsMap.entrySet()) {
								for (Map.Entry<Object, DirtyObjectID> me2 : me1.getValue().entrySet()) {
									DirtyObjectID dirtyObjectID = me2.getValue();

									Map<JDOLifecycleState, List<DirtyObjectID>> lifecycleState2dirtyObjectIDs = className2lifecycleState2dirtyObjectIDs.get(dirtyObjectID.getObjectClassName());
									if (lifecycleState2dirtyObjectIDs == null) {
										lifecycleState2dirtyObjectIDs = new HashMap<JDOLifecycleState, List<DirtyObjectID>>();
										className2lifecycleState2dirtyObjectIDs.put(dirtyObjectID.getObjectClassName(), lifecycleState2dirtyObjectIDs);
									}

									List<DirtyObjectID> dirtyObjectIDs = lifecycleState2dirtyObjectIDs.get(dirtyObjectID.getLifecycleState());
									if (dirtyObjectIDs == null) {
										dirtyObjectIDs = new ArrayList<DirtyObjectID>();
										lifecycleState2dirtyObjectIDs.put(dirtyObjectID.getLifecycleState(), dirtyObjectIDs);
									}

									dirtyObjectIDs.add(dirtyObjectID);
									++totalDirtyObjectIDCount;
								}
							}

							long duration = System.currentTimeMillis() - start;
							if (duration > 20000)
								logger.warn(logPrefix + "Grouping " + totalDirtyObjectIDCount + " DirtyObjectIDs for prefiltering took " + duration + " msec.");
							else {
								if (logger.isDebugEnabled())
									logger.debug(logPrefix + "Grouping " + totalDirtyObjectIDCount + " DirtyObjectIDs for prefiltering took " + duration + " msec.");
							}
						}

						{
							long start = System.currentTimeMillis();

							for (Map.Entry<String, Map<JDOLifecycleState, List<DirtyObjectID>>> me_1 : className2lifecycleState2dirtyObjectIDs.entrySet()) {
								Class<?> dirtyObjectClass = Class.forName(me_1.getKey());
								for (Map.Entry<JDOLifecycleState, List<DirtyObjectID>> me_2 : me_1.getValue().entrySet()) {
									JDOLifecycleState lifecycleState = me_2.getKey();
									List<DirtyObjectID> srcDirtyObjectIDs = me_2.getValue();

									// get the candidates (by pre-filtering to some criteria)
									Set<NotificationFilter> notificationFilters = NotificationFilter.getCandidates(pm, dirtyObjectClass, lifecycleState);

									// add the mapping
									for (NotificationFilter notificationFilter : notificationFilters) {
										UserID userID = (UserID) JDOHelper.getObjectId(notificationFilter.getUser());
										NotificationFilterID notificationFilterID = (NotificationFilterID) JDOHelper.getObjectId(notificationFilter);
										Map<NotificationFilterID, List<DirtyObjectID>> notificationFilterID2DirtyObjectID__beforeFilter = userID2notificationFilterID2DirtyObjectID_beforeFilter.get(userID);
										if (notificationFilterID2DirtyObjectID__beforeFilter == null) {
											notificationFilterID2DirtyObjectID__beforeFilter = new HashMap<NotificationFilterID, List<DirtyObjectID>>();
											userID2notificationFilterID2DirtyObjectID_beforeFilter.put(userID, notificationFilterID2DirtyObjectID__beforeFilter);
										}

										List<DirtyObjectID> destDirtyObjectIDs = notificationFilterID2DirtyObjectID__beforeFilter.get(notificationFilterID);
										if (destDirtyObjectIDs == null) {
											destDirtyObjectIDs = new ArrayList<DirtyObjectID>(srcDirtyObjectIDs.size());
											notificationFilterID2DirtyObjectID__beforeFilter.put(notificationFilterID, destDirtyObjectIDs);
										}
										destDirtyObjectIDs.addAll(srcDirtyObjectIDs);
									}

									++prefilteredGroupCount;
								}
							}

							long duration = System.currentTimeMillis() - start;
							if (duration > 20000)
								logger.warn(logPrefix + "Prefiltering " + prefilteredGroupCount + " groups of DirtyObjectIDs took " + duration + " msec.");
							else if (logger.isDebugEnabled())
								logger.debug(logPrefix + "Prefiltering " + prefilteredGroupCount + " groups of DirtyObjectIDs took " + duration + " msec.");
						}
					} finally {
						pm.close();
					}

					doCommit = true;
				} finally {
					if (handleTx) {
						if (doCommit)
							userTransaction.commit();
						else
							userTransaction.rollback();
					}
				}
			}

			// execute the filter method as the NotificationFilter's owner and push if possible
			Map<String, Boolean> subscriberType2NeedsPush = new HashMap<String, Boolean>();
			if (!userID2notificationFilterID2DirtyObjectID_beforeFilter.isEmpty()) {
				J2EEAdapter j2eeAdapter;
				{
					InitialContext ctx = new InitialContext();
					try {
						j2eeAdapter = (J2EEAdapter) ctx.lookup(J2EEAdapter.JNDI_NAME);
					} finally {
						ctx.close();
					}
				}

				boolean doCommit = false;
				userTransaction.begin();
				try {
					for (Map.Entry<UserID, Map<NotificationFilterID, List<DirtyObjectID>>> me1 : userID2notificationFilterID2DirtyObjectID_beforeFilter.entrySet()) {
						UserID userID = me1.getKey();
						LoginContext loginContext;
						JFireServerManager jfsm = jFireServerManagerFactory.getJFireServerManager();
						try {
//							loginContext = new LoginContext(
//									"jfire", new AuthCallbackHandler(jfsm, organisationID, userID.userID, this.getClass().getSimpleName()));
							loginContext = j2eeAdapter.createLoginContext(
									LoginData.DEFAULT_SECURITY_PROTOCOL,
									new AuthCallbackHandler(jfsm, organisationID, userID.userID, this.getClass().getSimpleName())
							);
						} finally {
							jfsm.close();
						}

						loginContext.login();
						try {
							PersistenceManager pm = persistenceManagerFactory.getPersistenceManager();
							try {

								for (Map.Entry<NotificationFilterID, List<DirtyObjectID>> me2 : me1.getValue().entrySet()) {
									NotificationFilter notificationFilter = (NotificationFilter) pm.getObjectById(me2.getKey());
									Collection<DirtyObjectID> dirtyObjectIDsFiltered = notificationFilter.filter(me2.getValue());
									++filterCount;

									if (dirtyObjectIDsFiltered != null && !dirtyObjectIDsFiltered.isEmpty()) {
										NotificationBundle notificationBundle = pm.makePersistent(
												new NotificationBundle(notificationFilter, dirtyObjectIDsFiltered));

										filteredDirtyObjectIDCount += dirtyObjectIDsFiltered.size();

										Boolean needsPush = subscriberType2NeedsPush.get(notificationFilter.getSubscriberType());
										if (needsPush == null) {
											try {
												pm.getObjectById(PushNotifierID.create(notificationFilter.getSubscriberType()));
												needsPush = Boolean.TRUE;
											} catch (JDOObjectNotFoundException x) {
												needsPush = Boolean.FALSE;
											}

											subscriberType2NeedsPush.put(notificationFilter.getSubscriberType(), needsPush);
										}

										if (needsPush.booleanValue())
											pushNotificationBundle(notificationBundle);
									}
								}

							} finally {
								pm.close();
							}
						} finally {
							loginContext.logout();
						}
					}
					doCommit = true;
				} finally {
					if (doCommit)
						userTransaction.commit();
					else
						userTransaction.rollback();
				}
			}

			// remove the objectids that have been marked for removal by fetchDirtyObjectIDs() before.
			dirtyObjectIDBuffer.clearFetchedDirtyObjectIDs();

			long totalDuration = System.currentTimeMillis() - totalStart;
			if (totalDuration > 40000)
				logger.warn(logPrefix + "totalDirtyObjectIDCount=" + totalDirtyObjectIDCount + " prefilteredGroupCount=" + prefilteredGroupCount + " filterCount=" + filterCount  + " filteredDirtyObjectIDCount=" + filteredDirtyObjectIDCount + " totalDuration=" + totalDuration + " msec.");
			else if (logger.isDebugEnabled())
				logger.debug(logPrefix + "totalDirtyObjectIDCount=" + totalDirtyObjectIDCount + " prefilteredGroupCount=" + prefilteredGroupCount + " filterCount=" + filterCount  + " filteredDirtyObjectIDCount=" + filteredDirtyObjectIDCount + " totalDuration=" + totalDuration + " msec.");
		} catch (Throwable t) {
			logger.error("Processing outgoing dirty objects failed!", t);
		}
	}
}
