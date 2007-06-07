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

package org.nightlabs.jfire.jdo.cache.bridge;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.listener.AttachLifecycleListener;
import javax.jdo.listener.CreateLifecycleListener;
import javax.jdo.listener.DeleteLifecycleListener;
import javax.jdo.listener.DirtyLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.TransactionManager;

import org.apache.log4j.Logger;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.idgenerator.IDNamespace;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.security.SecurityReflector;



/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class JdoCacheBridgeDefault extends JdoCacheBridge
{
	private static final Logger logger = Logger.getLogger(JdoCacheBridgeDefault.class);

	public static class CacheTransactionListener
	implements Synchronization
	{
		private JdoCacheBridgeDefault bridge;
		private boolean dead = false;

		/**
		 * @param previousSynchronization In case there can only be one synchronization be registered,
		 *		the previously registered one can be replaced by an instance of this one and
		 *		passed here in order to be triggered indirectly.
		 */
		public CacheTransactionListener(JdoCacheBridgeDefault bridge)
		{
			this.bridge = bridge;
		}

		@Implement
		public void beforeCompletion()
		{
			this.dead = true;
		}

		/**
		 * An instance of <code>CacheTransactionListener</code> should only be used for one transaction. Because
		 * we unfortunately cannot unregister the listener in {@link #afterCompletion(int)} (and neither in
		 * {@link #beforeCompletion()})
		 * [calling {@link PersistenceManager#setUserObject(Object)} fails], we simply set this flag to
		 * <code>true</code> instead.
		 * <p>
		 * This problem would be solved in the first place, if a <code>PersistenceManager</code> would not have any
		 * user-object associated after a transaction has been completed. Unfortunately, the PMF does not clear this
		 * field when reusing an old {@link PersistenceManager}.
		 * </p>
		 *
		 * @return <code>false</code>, if this instance of <code>CacheTransactionListener</code> is still alive and
		 * therefore must be used in the current transaction. <code>true</code>, if the transaction associated
		 * to this instance has already been completed and therefore this listener should not be used anymore.
		 */
		public boolean isDead()
		{
			return dead;
		}

		@Implement
		public void afterCompletion(int status)
		{
			if (status == Status.STATUS_COMMITTED) { 
				try {
					if (dirtyObjectIDs == null) {
						if (logger.isDebugEnabled())
							logger.debug("afterCompletion(STATUS_COMMITTED) called, but nothing to do.");

						return;
					}

					String sessionID = bridge.securityReflector._getUserDescriptor().getSessionID();

					if (objectID2Class != null) {
						bridge.getCacheManagerFactory().addObjectID2ClassMap(objectID2Class);
						objectID2Class = null;
					}

					if (dirtyObjectIDs != null) {
						final String _sessionID = sessionID;
						final Map<JDOLifecycleState, Map<Object, DirtyObjectID>> _dirtyObjectIDs = dirtyObjectIDs;
						Thread t = new Thread() {
							@Override
							public void run()
							{
								// there seems to be a timing issue concerning the commit of a transaction - delaying a sec seems to help - TODO fix this problem in a CLEAN way!
								try { Thread.sleep(1000); } catch (InterruptedException e) { } // ignore
								bridge.getCacheManagerFactory().addDirtyObjectIDs(_sessionID, _dirtyObjectIDs);
							}
						};
						t.start();
//						bridge.getCacheManagerFactory().addDirtyObjectIDs(sessionID, dirtyObjectIDs);
						dirtyObjectIDs = null;
					}
				} catch (Throwable e) {
					logger.error("afterCompletion(STATUS_COMMITTED): " + e.getMessage(), e);
				}
			}
			else if (status == Status.STATUS_ROLLEDBACK) {
				logger.debug("afterCompletion(STATUS_ROLLEDBACK) called.");
				dirtyObjectIDs = null;
			}
			else
				logger.error("afterCompletion(...) called with unknown status: " + status, new Exception("Unknown status (" + status + ") in afterCompletion!"));
		}

		// IMHO no sync necessary, because one transaction should only be used by one thread.
		private Map<JDOLifecycleState, Map<Object, DirtyObjectID>> dirtyObjectIDs = null;
		private Map<Object, Class> objectID2Class = null;

		protected void registerClass(Object objectID, Class clazz) {
			if (objectID2Class == null)
				objectID2Class = new HashMap<Object, Class>();

			if (objectID2Class.containsKey(objectID))
				return;

			objectID2Class.put(objectID, clazz);
		}

		public void addObject(JDOLifecycleState lifecycleStage, Object object)
		{
			Class clazz = object.getClass();
			if (clazz == IDNamespace.class) // we MUST ignore the IDNamespace changes, because we use the IDGenerator below in: bridge.getCacheManagerFactory().nextDirtyObjectIDSerial()
				return;

			Object objectID = getObjectID(object);
			Object version = JDOHelper.getVersion(object); // version can be null, if the jdo object is not versioned

			registerClass(objectID, clazz);

			if (dirtyObjectIDs == null)
				dirtyObjectIDs = new HashMap<JDOLifecycleState, Map<Object,DirtyObjectID>>();

			Map<Object,DirtyObjectID> m = dirtyObjectIDs.get(lifecycleStage);
			if (m == null) {
				m = new HashMap<Object, DirtyObjectID>();
				dirtyObjectIDs.put(lifecycleStage, m);
			}

			m.put(objectID, new DirtyObjectID(lifecycleStage, objectID, clazz.getName(), version, bridge.getCacheManagerFactory().nextDirtyObjectIDSerial()));
		}
	}

	private static Object getObjectID(Object object)
	{
		Object objectID = JDOHelper.getObjectId(object);

		if (objectID == null)
			throw new IllegalArgumentException("Could not obtain the objectID from the given object!");

		return objectID;
	}

	public JdoCacheBridgeDefault()
	{
	}

	private CacheTransactionListener getCacheTransactionListener(PersistenceManager pm)
	throws ResourceException
	{
		CacheTransactionListener listener = (CacheTransactionListener)pm.getUserObject();
		// we cannot unregister the CacheTransactionListener after the transaction is finished (pm.setUserObject fails) - hence we use the "dead" flag
		if (listener == null || listener.isDead()) {
			logger.debug("PersistenceManager.getUserObject() returned null or the returned listener is dead. Will create and add a CacheTransactionListener.");
			listener = new CacheTransactionListener(this);
			try {
				TransactionManager tm = getCacheManagerFactory().getTransactionManager();
				javax.transaction.Transaction tx = tm.getTransaction();
				tx.registerSynchronization(listener);
				pm.setUserObject(listener);
			} catch (Exception x) {
				logger.error("getCacheTransactionListener: Could not register Synchronization!", x);
			}
		}
		else
			logger.debug("PersistenceManager.getUserObject() returned a living CacheTransactionListener. No registration necessary.");

		return listener;
	}

	private void registerJDOObject(JDOLifecycleState lifecycleStage, Object object)
	{
		try {
			if (object == null)
				throw new NullPointerException("object must not be null!");

			PersistenceManager pm = JDOHelper.getPersistenceManager(object);
			if (pm == null)
				throw new IllegalArgumentException("Could not obtain a PersistenceManager from this object!");

			getCacheTransactionListener(pm).addObject(lifecycleStage, object);
		} catch (Throwable t) {
			logger.error("Failed to register "+lifecycleStage+" object: " + object, t);
		}
	}

	private CreateLifecycleListener createLifecycleListener = new CreateLifecycleListener() {
		public void postCreate(InstanceLifecycleEvent event)
		{
			if (logger.isDebugEnabled())
				logger.debug("postCreate: " + event.getPersistentInstance());

			registerJDOObject(JDOLifecycleState.NEW, event.getPersistentInstance());
		}
	};

//	private StoreLifecycleListener storeLifecycleListener = new StoreLifecycleListener() {
//		public void preStore(InstanceLifecycleEvent event) { }
//		public void postStore(InstanceLifecycleEvent event)
//		{
//			if (logger.isDebugEnabled())
//				logger.debug("postStore: " + event.getPersistentInstance() + " isNew=" + JDOHelper.isNew(event.getPersistentInstance()));
//
//			if (!JDOHelper.isNew(event.getPersistentInstance()))
//				return;
//
//			registerJDOObject(JDOLifecycleState.NEW, event.getPersistentInstance());
//		}
//	};

	private DirtyLifecycleListener dirtyLifecycleListener = new DirtyLifecycleListener() {
		public void preDirty(InstanceLifecycleEvent event) { }
		public void postDirty(javax.jdo.listener.InstanceLifecycleEvent event)
		{
			if (logger.isDebugEnabled())
				logger.debug("postDirty: " + event.getPersistentInstance());

			registerJDOObject(JDOLifecycleState.DIRTY, event.getPersistentInstance());
		}
	};

	private AttachLifecycleListener attachLifecycleListener = new AttachLifecycleListener() {
		public void preAttach(InstanceLifecycleEvent event) { }
		public void postAttach(InstanceLifecycleEvent event)
		{
			if (logger.isDebugEnabled())
				logger.debug("postAttach: " + event.getPersistentInstance());

			registerJDOObject(JDOLifecycleState.DIRTY, event.getPersistentInstance());
		}
	};

	private DeleteLifecycleListener deleteLifecycleListener = new DeleteLifecycleListener() {
		public void preDelete(InstanceLifecycleEvent event)
		{
			if (logger.isDebugEnabled())
				logger.debug("preDelete: " + event.getPersistentInstance());

			registerJDOObject(JDOLifecycleState.DELETED, event.getPersistentInstance());
		}
		public void postDelete(InstanceLifecycleEvent event) { }
	};

	/**
	 * @see org.nightlabs.jfire.jdo.cache.bridge.JdoCacheBridge#init()
	 */
	public void init()
	{
		InitialContext initialContext;
		try {
			initialContext = new InitialContext();
			try {
				securityReflector = SecurityReflector.lookupSecurityReflector(initialContext);
			} finally {
				initialContext.close();
			}
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}

		PersistenceManagerFactory pmf = getPersistenceManagerFactory();

		pmf.addInstanceLifecycleListener(
				createLifecycleListener,
				objectClassArray);
//		pmf.addInstanceLifecycleListener(
//				storeLifecycleListener,
//				null);
		pmf.addInstanceLifecycleListener(
				dirtyLifecycleListener,
				objectClassArray);
		pmf.addInstanceLifecycleListener(
				attachLifecycleListener,
				objectClassArray);
		pmf.addInstanceLifecycleListener(
				deleteLifecycleListener,
				objectClassArray);
	}

	private static final Class[] objectClassArray = new Class[] { Object.class };

	private SecurityReflector securityReflector;

	/**
	 * @see org.nightlabs.jfire.jdo.cache.bridge.JdoCacheBridge#close()
	 */
	public void close()
	{
		PersistenceManagerFactory pmf = getPersistenceManagerFactory();

		if (pmf != null) {
			pmf.removeInstanceLifecycleListener(dirtyLifecycleListener);
			pmf.removeInstanceLifecycleListener(attachLifecycleListener);
		}
	}
}
