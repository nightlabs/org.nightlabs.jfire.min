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
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;

import org.apache.log4j.Logger;
import org.jpox.resource.PersistenceManagerImpl;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.security.SecurityReflector;



/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class JdoCacheBridgeJPOX extends JdoCacheBridge
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(JdoCacheBridgeJPOX.class);

	public static class CacheTransactionListener implements ConnectionEventListener
	{
		private JdoCacheBridgeJPOX bridge;
//		private Synchronization previousSynchronization;

		/**
		 * @param previousSynchronization In case there can only be one synchronization be registered,
		 *		the previously registered one can be replaced by an instance of this one and
		 *		passed here in order to be triggered indirectly.
		 */
		public CacheTransactionListener(JdoCacheBridgeJPOX bridge)
//				Synchronization previousSynchronization)
		{
			this.bridge = bridge;
//			this.previousSynchronization = previousSynchronization;
		}

//		/**
//		 * @see javax.transaction.Synchronization#beforeCompletion()
//		 */
//		public void beforeCompletion()
//		{
//			if (previousSynchronization != null) {
//				if (LOGGER.isDebugEnabled())
//					LOGGER.debug("beforeCompletion() will call previousSynchronization.beforeCompletion() first. previousSynchronization: " + previousSynchronization);
//
//				previousSynchronization.beforeCompletion();
//			}
//			else if (LOGGER.isDebugEnabled())
//				LOGGER.debug("beforeCompletion(): No previousSynchronization to trigger.");
//		}
//
//		/**
//		 * @see javax.transaction.Synchronization#afterCompletion(int)
//		 */
//		public void afterCompletion(int status)
//		{
//			if (previousSynchronization != null) {
//				if (LOGGER.isDebugEnabled())
//					LOGGER.debug("afterCompletion("+status+") will call previousSynchronization.afterCompletion(...) first. previousSynchronization: " + previousSynchronization);
//
//				previousSynchronization.afterCompletion(status);
//			}
//			else if (LOGGER.isDebugEnabled())
//				LOGGER.debug("afterCompletion("+status+"): No previousSynchronization to trigger.");
//
//			if (Status.STATUS_COMMITTED == status) {
//				if (dirtyObjectIDsRaw != null)
//					bridge.getCacheManagerFactory().addDirtyObjectIDs(dirtyObjectIDsRaw);
//
//				dirtyObjectIDsRaw = null;
//			}
//			else if (Status.STATUS_ROLLEDBACK == status) {
//				dirtyObjectIDsRaw = null;
//			}
//		}

		public void localTransactionCommitted(javax.resource.spi.ConnectionEvent event)
		{
			try {
				if (dirtyObjectIDs == null) {
					if (logger.isDebugEnabled())
						logger.debug("localTransactionCommitted(...) called, but nothing to do.");

					return;
				}

				String sessionID = bridge.securityReflector._getUserDescriptor().getSessionID();

				if (objectID2Class != null) {
					bridge.getCacheManagerFactory().addObjectID2ClassMap(objectID2Class);
					objectID2Class = null;
				}

				if (dirtyObjectIDs != null) {
					bridge.getCacheManagerFactory().addDirtyObjectIDs(sessionID, dirtyObjectIDs);
					dirtyObjectIDs = null;
				}
			} catch (Throwable e) {
				logger.error("", e);
			}
		}

		public void localTransactionRolledback(javax.resource.spi.ConnectionEvent event)
		{
			logger.debug("localTransactionRolledback(...) called.");
			dirtyObjectIDs = null;
		}

		public void connectionClosed(ConnectionEvent event) { }
		public void connectionErrorOccurred(ConnectionEvent event) { }
		public void localTransactionStarted(ConnectionEvent event) { }

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
			Object objectID = getObjectID(object);
			Object version = JDOHelper.getVersion(object); // version can be null, if the jdo object is not versioned

			registerClass(objectID, object.getClass());

			if (dirtyObjectIDs == null)
				dirtyObjectIDs = new HashMap<JDOLifecycleState, Map<Object,DirtyObjectID>>();

			Map<Object,DirtyObjectID> m = dirtyObjectIDs.get(lifecycleStage);
			if (m == null) {
				m = new HashMap<Object, DirtyObjectID>();
				dirtyObjectIDs.put(lifecycleStage, m);
			}

			m.put(objectID, new DirtyObjectID(lifecycleStage, objectID, version, bridge.getCacheManagerFactory().nextDirtyObjectIDSerial()));
		}
	}

	private static Object getObjectID(Object object)
	{
		Object objectID = JDOHelper.getObjectId(object);

		if (objectID == null)
			throw new IllegalArgumentException("Could not obtain the objectID from the given object!");

		return objectID;
	}

	public JdoCacheBridgeJPOX()
	{
	}

	private CacheTransactionListener getCacheTransactionListener(PersistenceManager _pm)
	throws ResourceException
	{
		if (!(_pm instanceof PersistenceManagerImpl))
			throw new ClassCastException("PersistenceManager is not an instance of " + PersistenceManagerImpl.class + " but " + _pm == null ? "null" : _pm.getClass().getName() + "!");

		PersistenceManagerImpl pm = (PersistenceManagerImpl) _pm;
//		JdoTransactionHandle th = (JdoTransactionHandle) pm.getLocalTransaction();
		CacheTransactionListener listener = (CacheTransactionListener)pm.getUserObject();
		if (listener == null) {
			logger.debug("PersistenceManagerImpl.getUserObject() returned null. Will create and add a CacheTransactionListener.");
			listener = new CacheTransactionListener(this);
			pm.addConnectionEventListener(listener);
			pm.setUserObject(listener);
		}
		else
			logger.debug("PersistenceManagerImpl.getUserObject() returned a CacheTransactionListener. No registration necessary.");

		return listener;
//		Synchronization oldSync = th.getSynchronization();
//		if (oldSync instanceof CacheTransactionListener) {
//			LOGGER.debug("JdoTransactionHandle.getSynchronization() returned already an instance of CacheTransactionListener. No registration necessary.");
//			return (CacheTransactionListener)oldSync;
//		}
//		else {
//			LOGGER.debug("JdoTransactionHandle.getSynchronization() returned \"" + oldSync + "\". Will register a new CacheTransactionListener.");
//			CacheTransactionListener res = new CacheTransactionListener(this, oldSync);
//			th.setSynchronization(res);
//			return res;
//		}
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
