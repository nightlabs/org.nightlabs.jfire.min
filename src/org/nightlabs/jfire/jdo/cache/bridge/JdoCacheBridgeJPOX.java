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

import java.util.HashSet;

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
import org.nightlabs.jfire.jdo.cache.DirtyObjectID;
import org.nightlabs.jfire.servermanager.j2ee.SecurityReflector;



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
//				if (dirtyObjectIDs != null)
//					bridge.getCacheManagerFactory().addDirtyObjectIDs(dirtyObjectIDs);
//
//				dirtyObjectIDs = null;
//			}
//			else if (Status.STATUS_ROLLEDBACK == status) {
//				dirtyObjectIDs = null;
//			}
//		}

		public void localTransactionCommitted(javax.resource.spi.ConnectionEvent event)
		{
			try {
				if (newObjectIDs == null && dirtyObjectIDs == null && deletedObjectIDs == null) {
					if (logger.isDebugEnabled())
						logger.debug("localTransactionCommitted(...) called, but nothing to do.");

					return;
				}

				if (logger.isDebugEnabled())
					logger.debug("localTransactionCommitted(...) called. newObjectIDs: "+(newObjectIDs == null ? null : newObjectIDs.size())+"  dirtyObjectIDs: "+(dirtyObjectIDs == null ? null : dirtyObjectIDs.size())+"  deletedObjectIDs: "+(deletedObjectIDs == null ? null : deletedObjectIDs.size()));

				String sessionID = bridge.securityReflector.whoAmI().getSessionID();

				if (newObjectIDs != null) {
					bridge.getCacheManagerFactory().addDirtyObjectIDs(sessionID, newObjectIDs, DirtyObjectID.LifecycleStage.NEW);
					newObjectIDs = null;
				}

				if (dirtyObjectIDs != null) {
					bridge.getCacheManagerFactory().addDirtyObjectIDs(sessionID, dirtyObjectIDs, DirtyObjectID.LifecycleStage.DIRTY);
					dirtyObjectIDs = null;
				}

				if (deletedObjectIDs != null) {
					bridge.getCacheManagerFactory().addDirtyObjectIDs(sessionID, deletedObjectIDs, DirtyObjectID.LifecycleStage.DELETED);
					deletedObjectIDs = null;
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
		private HashSet<Object> newObjectIDs = null;
		private HashSet<Object> dirtyObjectIDs = null;
		private HashSet<Object> deletedObjectIDs = null;

		public void addNewObjectID(Object objectID)
		{
			if (newObjectIDs == null)
				newObjectIDs = new HashSet<Object>();

			if (!newObjectIDs.contains(objectID))
				newObjectIDs.add(objectID);
		}

		public void addDirtyObjectID(Object objectID)
		{
			if (dirtyObjectIDs == null)
				dirtyObjectIDs = new HashSet<Object>();

			if (!dirtyObjectIDs.contains(objectID))
				dirtyObjectIDs.add(objectID);
		}

		public void addDeletedObjectID(Object objectID)
		{
			if (deletedObjectIDs == null)
				deletedObjectIDs = new HashSet<Object>();

			if (!deletedObjectIDs.contains(objectID))
				deletedObjectIDs.add(objectID);
		}
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

	private void registerJDOObject(Object object, DirtyObjectID.LifecycleStage lifecycleStage)
	{
		try {
			if (object == null)
				throw new NullPointerException("object must not be null!");

			PersistenceManager pm = JDOHelper.getPersistenceManager(object);
			if (pm == null)
				throw new IllegalArgumentException("Could not obtain a PersistenceManager from this object!");

			Object objectID = JDOHelper.getObjectId(object);
			if (objectID == null)
				throw new IllegalArgumentException("Could not obtain the objectID from the given object!");

			switch (lifecycleStage) {
				case NEW:
					getCacheTransactionListener(pm).addNewObjectID(objectID);
					break;
				case DIRTY:
					getCacheTransactionListener(pm).addDirtyObjectID(objectID);
					break;
				case DELETED:
					getCacheTransactionListener(pm).addDeletedObjectID(objectID);
					break;
				default:
					throw new IllegalStateException("Unknown LifecycleStage: " + lifecycleStage);
			}
		} catch (Throwable t) {
			logger.error("Failed to make object dirty: " + object, t);
		}
	}

	private CreateLifecycleListener createLifecycleListener = new CreateLifecycleListener() {
		public void postCreate(InstanceLifecycleEvent event)
		{
			if (logger.isDebugEnabled())
				logger.debug("postCreate: " + event.getPersistentInstance() + " (isNew="+JDOHelper.isNew(event.getPersistentInstance())+")");

			registerJDOObject(event.getPersistentInstance(), DirtyObjectID.LifecycleStage.NEW);
		}
	};

// TODO The StoreLifecycleListener is probably not necessary.
//	private StoreLifecycleListener storeLifecycleListener = new StoreLifecycleListener() {
//		public void preStore(InstanceLifecycleEvent event) {}
//		public void postStore(InstanceLifecycleEvent event)
//		{
//			if (logger.isDebugEnabled())
//				logger.debug("postStore: " + event.getPersistentInstance() + " (isNew="+JDOHelper.isNew(event.getPersistentInstance())+")");
//
////			if (JDOHelper.isNew(event.getPersistentInstance()))
////				registerJDOObject(event.getPersistentInstance(), LifecycleStage.NEW);
//		}
//	};

	private DirtyLifecycleListener dirtyLifecycleListener = new DirtyLifecycleListener() {
		public void preDirty(InstanceLifecycleEvent event) { }
		public void postDirty(javax.jdo.listener.InstanceLifecycleEvent event)
		{
			if (logger.isDebugEnabled())
				logger.debug("postDirty: " + event.getPersistentInstance()); // source="+event.getSource()+" target="+event.getTarget());

//			makeDirty(event.getSource());
			registerJDOObject(event.getPersistentInstance(), DirtyObjectID.LifecycleStage.DIRTY);
		}
	};

	private AttachLifecycleListener attachLifecycleListener = new AttachLifecycleListener() {
		public void preAttach(InstanceLifecycleEvent event) { }
		public void postAttach(InstanceLifecycleEvent event)
		{
			if (logger.isDebugEnabled())
				logger.debug("postAttach: " + event.getPersistentInstance()); // source="+event.getSource()+" target="+event.getTarget());

//			makeDirty(event.getSource());
			registerJDOObject(event.getPersistentInstance(), DirtyObjectID.LifecycleStage.DIRTY);
		}
	};

	private DeleteLifecycleListener deleteLifecycleListener = new DeleteLifecycleListener() {
		public void preDelete(InstanceLifecycleEvent event)
		{
			if (logger.isDebugEnabled())
				logger.debug("preDelete: " + event.getPersistentInstance());

			registerJDOObject(event.getPersistentInstance(), DirtyObjectID.LifecycleStage.DELETED);
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
				null);
		pmf.addInstanceLifecycleListener(
				dirtyLifecycleListener,
				null);
		pmf.addInstanceLifecycleListener(
				attachLifecycleListener,
				null);
		pmf.addInstanceLifecycleListener(
				deleteLifecycleListener,
				null);
	}

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
