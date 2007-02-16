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

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.security.auth.login.LoginContext;
import javax.transaction.TransactionManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jfire.base.AuthCallbackHandler;
import org.nightlabs.jfire.jdo.cache.CacheManagerFactory;
import org.nightlabs.jfire.jdo.cache.LocalDirtyListener;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.persistent.id.PushNotifierID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.ra.JFireServerManagerFactoryImpl;

public class PersistentNotificationManagerFactory implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger
			.getLogger(PersistentNotificationManagerFactory.class);

	public static final String JNDI_PREFIX = "java:/jfire/persistentNotificationManagerFactory/";

	public static String getJNDIName(String organisationID)
	{
		return JNDI_PREFIX + organisationID;
	}

	public static final PersistentNotificationManagerFactory getOrganisationSyncManagerFactory(
			InitialContext ctx, String organisationID) throws NamingException
	{
		return (PersistentNotificationManagerFactory) ctx
				.lookup(getJNDIName(organisationID));
	}

	private String organisationID;
	private transient JFireServerManagerFactory jFireServerManagerFactory;
	private transient TransactionManager transactionManager;
	private PersistenceManagerFactory persistenceManagerFactory;

	private transient DirtyObjectIDBuffer dirtyObjectIDBuffer;
	private transient Timer timerOutgoing = new Timer();
	private transient Timer timerIncoming = new Timer();

	public PersistentNotificationManagerFactory(InitialContext ctx,
			String organisationID, JFireServerManagerFactory jFireServerManagerFactory,
			TransactionManager transactionManager, PersistenceManagerFactory pmf)
			throws NamingException, DirtyObjectIDBufferException
	{
		this.organisationID = organisationID;
		this.jFireServerManagerFactory = jFireServerManagerFactory;
		this.transactionManager = transactionManager;
		this.persistenceManagerFactory = pmf;

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

		CacheManagerFactory.getCacheManagerFactory(ctx, organisationID)
				.addLocalDirtyListener(new LocalDirtyListener() {
					public void notifyDirtyObjectIDs(Map<JDOLifecycleState, Map<Object, DirtyObjectID>> dirtyObjectIDs)
					{
						try {
							dirtyObjectIDBuffer.addDirtyObjectIDs(dirtyObjectIDs);
						} catch (Exception x) {
							logger.error("Storing DirtyObjectIDs into temporary persistent buffer failed!", x);
						}
//						// TODO handle all other LifecylceStages!
//						Map<Object, DirtyObjectID> m = dirtyObjectIDs.get(JDOLifecycleState.DIRTY);
//						if (m != null)
//							makeDirty(m.keySet());
					}
				});

		timerOutgoing.schedule(new TimerTask() {
			public void run()
			{
				processDirtyObjectIDsOutgoing();
			}
		}, 0, 15000); // TODO the 15000 should come from a config module

		timerIncoming.schedule(new TimerTask() {
			public void run()
			{
				processDirtyObjectIDsIncoming();
			}
		}, 0, 20000); // TODO the 20000 should come from a config module
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
	public PersistenceManagerFactory getPersistenceManagerFactory()
	{
		return persistenceManagerFactory;
	}

	/**
	 * This method is called by {@link #timerIncoming} and therefore does not run concurrently. 
	 */
	private void processDirtyObjectIDsIncoming()
	{
		try {
			ensureOpenPersistenceManagerFactory();

			boolean doCommit = false;
			transactionManager.begin();
			try {
				PersistenceManager pm = persistenceManagerFactory.getPersistenceManager();
				try {
//					// key: String context
//					// value: SyncContext syncContext
//					Map syncContexts = new HashMap();
//
//					Collection listenerDescriptors = IncomingChangeListenerDescriptor.findDirtyListenerDescriptors(pm);
//					for (Iterator it = listenerDescriptors.iterator(); it.hasNext(); ) {
//						IncomingChangeListenerDescriptor ld = (IncomingChangeListenerDescriptor) it.next();
//						String context = ld.getContext();
//						SyncContext syncContext = (SyncContext) syncContexts.get(context);
//						if (syncContext == null) {
//							syncContext = (SyncContext) pm.getObjectById(SyncContextID.create(context));
//							syncContexts.put(context, syncContext);
//						}
//						syncContext.synchronize(ld);
//
//						ld.setDirty(false);
//					}
				} finally {
					pm.close();
				}

				doCommit = true;
			} finally {
				if (doCommit)
					transactionManager.commit();
				else
					transactionManager.rollback();
			}
		} catch (Throwable t) {
			logger.error("Processing incoming dirty objects failed!", t);
		}
	}

	private synchronized void ensureOpenPersistenceManagerFactory()
	throws ModuleException
	{
		if (persistenceManagerFactory == null || persistenceManagerFactory.isClosed()) {
			PersistenceManagerFactory pmf = JFireServerManagerFactoryImpl.getPersistenceManagerFactory(organisationID);
			if (pmf == null)
				throw new ModuleException("PersistenceManagerFactory for organisationID=\""+organisationID+"\" could not be re-obtained from JNDI!");

			persistenceManagerFactory = pmf;
		}
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

	private Class pushManagerClass = null;

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

	/**
	 * This method is called by {@link #timerOutgoing} and therefore does not run concurrently. 
	 */
	private void processDirtyObjectIDsOutgoing()
	{
		try {
			ensureOpenPersistenceManagerFactory();

			// 1st step: fetch temporary dirty objectIDs and distribute the change-markers to all interested listeners

			// fetchDirtyObjectIDs() still keeps the objectIDs in case sth. goes wrong - but they're marked for removal
			Collection<Map<JDOLifecycleState, Map<Object, DirtyObjectID>>> dirtyObjectIDsRaw = dirtyObjectIDBuffer.fetchDirtyObjectIDs();

			// condense the raw objects (keep only the newest DirtyObjectID for every JDO object and every lifecycle state)
			Map<JDOLifecycleState, Map<Object, DirtyObjectID>> dirtyObjectIDsMap = condenseDirtyObjectIDCollection(dirtyObjectIDsRaw);

			List<Object> asyncInvokeEnvelopes = new ArrayList<Object>();

			boolean doCommit = false;
			transactionManager.begin();
			try {
				PersistenceManager pm = persistenceManagerFactory.getPersistenceManager();
				try {

					// find roughly matching subscriptions (by prefiltering according to jdo-lifecycle-state & class)
					Map<User, Map<Subscription, List<DirtyObjectID>>> user2subscription2DirtyObjectID_beforeFilter = new HashMap<User, Map<Subscription,List<DirtyObjectID>>>(); 

					for (Map.Entry<JDOLifecycleState, Map<Object, DirtyObjectID>> me1 : dirtyObjectIDsMap.entrySet()) {
						for (Map.Entry<Object, DirtyObjectID> me2 : me1.getValue().entrySet()) {
							DirtyObjectID dirtyObjectID = me2.getValue();

							// get the candidates (by pre-filtering to some criteria)
							Set<Subscription> subscriptions = Subscription.getCandidates(pm, dirtyObjectID);

							// add the mapping
							for (Subscription subscription : subscriptions) {
								Map<Subscription, List<DirtyObjectID>> subscription2DirtyObjectID_beforeFilter = user2subscription2DirtyObjectID_beforeFilter.get(subscription.getUser());
								if (subscription2DirtyObjectID_beforeFilter == null) {
									subscription2DirtyObjectID_beforeFilter = new HashMap<Subscription, List<DirtyObjectID>>();
									user2subscription2DirtyObjectID_beforeFilter.put(subscription.getUser(), subscription2DirtyObjectID_beforeFilter);
								}

								List<DirtyObjectID> dirtyObjectIDs = subscription2DirtyObjectID_beforeFilter.get(subscription);
								if (dirtyObjectIDs == null) {
									dirtyObjectIDs = new ArrayList<DirtyObjectID>();
									subscription2DirtyObjectID_beforeFilter.put(subscription, dirtyObjectIDs);
								}
								dirtyObjectIDs.add(dirtyObjectID);
							}

						}
					}


					if (!user2subscription2DirtyObjectID_beforeFilter.isEmpty()) {
						pm.getExtent(PushNotifier.class); // initialize meta-data

						// This Map stores, whether a PushNotifier exists for the given subscriberType - so multiple Subscriptions with
						// the same subscriberType do not require multiple JDO accesses.
						Map<String, Boolean> subscriberType2NeedsPush = new HashMap<String, Boolean>();

						LoginContext loginContext;
						JFireServerManager jfsm = jFireServerManagerFactory.getJFireServerManager();
						try {
							for (Map.Entry<User, Map<Subscription, List<DirtyObjectID>>> me1 : user2subscription2DirtyObjectID_beforeFilter.entrySet()) {
								User user = me1.getKey();

								if (!organisationID.equals(user.getOrganisationID()))
									throw new IllegalStateException("PersistentNotificationManagerFactory.organisationID ("+organisationID+") != user.organisationID ("+user.getOrganisationID()+")");

								loginContext = new LoginContext(
										"jfire", new AuthCallbackHandler(jfsm, organisationID, user.getUserID(), this.getClass().getSimpleName()));

								loginContext.login();
								try {

									// filter the DirtyObjectIDs
									for (Map.Entry<Subscription, List<DirtyObjectID>> me2 : me1.getValue().entrySet()) {
										Subscription subscription = me2.getKey();
										Collection<DirtyObjectID> dirtyObjectIDs = subscription.filter(me2.getValue());

										if (dirtyObjectIDs != null && !dirtyObjectIDs.isEmpty()) {
											NotificationBundle notificationBundle = (NotificationBundle) pm.makePersistent(
													new NotificationBundle(subscription, dirtyObjectIDs));

											Boolean needsPush = subscriberType2NeedsPush.get(subscription.getSubscriberType());
											if (needsPush == null) {
												try {
													pm.getObjectById(PushNotifierID.create(subscription.getSubscriberType()));
													needsPush = Boolean.TRUE;
												} catch (JDOObjectNotFoundException x) {
													needsPush = Boolean.FALSE;
												}

												subscriberType2NeedsPush.put(subscription.getSubscriberType(), needsPush);
											}

											if (needsPush.booleanValue())
												pushNotificationBundle(notificationBundle);
										}
									}

								} finally {
									loginContext.logout();
								}

							}
						} finally {
							jfsm.close();
						}
					}

//					for (Iterator itObjectIDs = dirtyObjectIDs.iterator(); itObjectIDs.hasNext(); ) {
//						Object objectID = itObjectIDs.next();
//
//						Collection listenerDescriptors = OutgoingChangeListenerDescriptor.findNonDirtyListenerDescriptorsForObjectID(pm, objectID);
//						for (Iterator itLD = listenerDescriptors.iterator(); itLD.hasNext(); ) {
//							OutgoingChangeListenerDescriptor ld = (OutgoingChangeListenerDescriptor) itLD.next();
//							ld.setDirty(true);
//						}
//					}

				} finally {
					pm.close();
				}

				doCommit = true;
			} finally {
				if (doCommit)
					transactionManager.commit();
				else
					transactionManager.rollback();
			}

			// remove the objectids that have been marked for removal by fetchDirtyObjectIDs() before.
			dirtyObjectIDBuffer.clearFetchedDirtyObjectIDs();


//			// create OrganisationSyncDelegateImpl if not yet existing
//			if (organisationSyncDelegate == null) {
//				organisationSyncDelegate = (OrganisationSyncDelegate) Class.forName(ORGANISATION_SYNC_DELEGATE_CLASS_NAME).newInstance();
//				organisationSyncDelegate.init(this);
//			}

//			// 2nd step: notify the subscribers that have dirty listeners
//			doCommit = false;
//			transactionManager.begin();
//			try {
//				PersistenceManager pm = persistenceManagerFactory.getPersistenceManager();
//				try {
//
////					// organise the listeners by organisation (in order to have only one RMI call per organisation)
////
////					// key: String organisationID
////					// value: Collection outgoingChangeListenerDescriptors
////					Map listenerDescriptorsByOrganisationID = new HashMap();
////
////					Collection listenerDescriptors = OutgoingChangeListenerDescriptor.findDirtyListenerDescriptors(pm);
////					for (Iterator itLD = listenerDescriptors.iterator(); itLD.hasNext(); ) {
////						OutgoingChangeListenerDescriptor ld = (OutgoingChangeListenerDescriptor) itLD.next();
////						Collection c = (Collection) listenerDescriptorsByOrganisationID.get(ld.getOrganisationID());
////						if (c == null) {
////							c = new LinkedList();
////							listenerDescriptorsByOrganisationID.put(ld.getOrganisationID(), c);
////						}
////						c.add(ld);
////					}
////
////					// iterate the organisations and pass the objectIDs of the modified objects.
////					for (Iterator it = listenerDescriptorsByOrganisationID.entrySet().iterator(); it.hasNext(); ) {
////						Map.Entry me = (Map.Entry)it.next();
////						String organisationID = (String) me.getKey();
////						try {
////							Collection descriptors = (Collection) me.getValue();
////							// organise the objectIDs by context
////							Map dirtyObjectIDCarriersByContext = new HashMap();
////							for (Iterator itLD = descriptors.iterator(); itLD.hasNext(); ) {
////								OutgoingChangeListenerDescriptor ld = (OutgoingChangeListenerDescriptor) itLD.next();
////								Object objectID = ObjectIDUtil.createObjectID(ld.getObjectIDClassName(), ld.getObjectIDFieldPart());
////								String context = ld.getContext();
////								DirtyObjectIDCarrier carrier = (DirtyObjectIDCarrier) dirtyObjectIDCarriersByContext.get(context);
////								if (carrier == null) {
////									carrier = new DirtyObjectIDCarrier(context);
////									dirtyObjectIDCarriersByContext.put(context, carrier);
////								}
////								carrier.addObjectID(objectID);
////							}
////
////							// notify the other organisation (specified by organisationID)
////							if (!dirtyObjectIDCarriersByContext.isEmpty()) {
////								organisationSyncDelegate.notifyDirtyObjectIDs(
////										pm, organisationID, dirtyObjectIDCarriersByContext.values());
////							}
////
////							// clear the dirty flag (if we come here, the other organisation was successfully notified)
////							for (Iterator itLD = descriptors.iterator(); itLD.hasNext(); ) {
////								OutgoingChangeListenerDescriptor ld = (OutgoingChangeListenerDescriptor) itLD.next();
////								ld.setDirty(false);
////							}
////						} catch (Throwable t) {
////							logger.error("Notifying organisation \"" + organisationID + "\" failed!", t);
////						}
////					} // for (Iterator it = listenerDescriptorsByOrganisationID.entrySet().iterator(); it.hasNext(); ) {
//
//				} finally {
//					pm.close();
//				}
//
//				doCommit = true;
//			} finally {
//				if (doCommit)
//					transactionManager.commit();
//				else
//					transactionManager.rollback();
//			}
		} catch (Throwable t) {
			logger.error("Processing outgoing dirty objects failed!", t);
		}
	}

//	/**
//	 * This method filters out all jdo object-ids which do not implement
//	 * {@link ObjectID} and passes the filtered set to
//	 * {@link DirtyObjectIDBuffer#addDirtyObjectIDs(Collection)}.
//	 *
//	 * @param objectIDs
//	 */
//	private void makeDirty(Collection objectIDs)
//	{
//		try {
//			// TODO filter objectIDs of classes that have no listeners at all.
//			// or maybe it's faster to dump simply all into the DirtyObjectIDBuffer?!
//
//			Set filteredObjectIDs = new HashSet(objectIDs.size());
//			for (Iterator it = objectIDs.iterator(); it.hasNext(); ) {
//				Object objectID = it.next();
//				if (!(objectID instanceof ObjectID))
//					logger.warn("Ignoring objectID \"" + objectID + "\" (" + objectID.getClass() + ") because it does not implement " + ObjectID.class +"!");
//				else
//					filteredObjectIDs.add(objectID);
//			}
//			dirtyObjectIDBuffer.addDirtyObjectIDs(filteredObjectIDs);
//		} catch (DirtyObjectIDBufferException e) {
//			throw new RuntimeException(e);
//		}
//	}
}
