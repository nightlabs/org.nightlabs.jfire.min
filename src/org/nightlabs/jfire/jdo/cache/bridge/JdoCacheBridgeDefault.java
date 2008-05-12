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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.listener.AttachLifecycleListener;
import javax.jdo.listener.CreateLifecycleListener;
import javax.jdo.listener.DeleteLifecycleListener;
import javax.jdo.listener.DirtyLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.StoreLifecycleListener;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.apache.log4j.Logger;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.idgenerator.IDNamespace;
import org.nightlabs.jfire.jdo.cache.CacheManagerFactory;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.math.Base62Coder;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class JdoCacheBridgeDefault extends JdoCacheBridge
{
	private static final Logger logger = Logger.getLogger(JdoCacheBridgeDefault.class);

	/**
	 * If this is &lt;= 0, the {@link CacheTransactionListener#afterCompletion(int)} will
	 * directly forward all collected events to the {@link CacheManagerFactory}. If this
	 * is &gt; 0, the said method will spawn a {@link Thread} and execute it asynchronously
	 * after the given delay in milliseconds.
	 */
	private static final int notificationDelayAfterCompletionMSec = 2000;

	private BlockingQueue<Runnable> flush_queue = new LinkedBlockingDeque<Runnable>();
	private ThreadPoolExecutor flush_threadPoolExecutor = new ThreadPoolExecutor(10, 20, 60000, TimeUnit.MILLISECONDS, flush_queue);

	public static class CacheTransactionListener
	implements Synchronization
	{
		private static final Logger logger = Logger.getLogger(CacheTransactionListener.class);

		private JdoCacheBridgeDefault bridge;
		private volatile boolean dead = false;

		public CacheTransactionListener(JdoCacheBridgeDefault bridge)
		{
			this.bridge = bridge;
			_sessionID = bridge.securityReflector._getUserDescriptor().getSessionID();
		}

		public String getIdentityString()
		{
			return Base62Coder.sharedInstance().encode(System.identityHashCode(this), 1);
		}

		private void debug(String msg)
		{
			logger.debug("CacheTransactionListener[" + getIdentityString() + "]" + msg);
		}
		private void error(String msg, Throwable t)
		{
			logger.error("CacheTransactionListener[" + getIdentityString() + "]" + msg, t);
		}

		@Override
		public void beforeCompletion()
		{
			if (logger.isDebugEnabled())
				debug("beforeCompletion: called");

//			_sessionID = bridge.securityReflector._getUserDescriptor().getSessionID();

			if (synchronization != null)
				synchronization.beforeCompletion();
		}

		private String _sessionID;

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

		private Synchronization synchronization;

		@Override
		public void afterCompletion(int status)
		{
			this.dead = true;

			if (synchronization != null)
				synchronization.afterCompletion(status);

			objectIDs_preStoreCalled.clear();

			if (status == Status.STATUS_COMMITTED) {
				try {
					if (this.dirtyObjectIDs == null) {
						if (logger.isDebugEnabled())
							debug("afterCompletion(STATUS_COMMITTED) called, but nothing to do.");

						return;
					}

					if (logger.isDebugEnabled())
						debug("afterCompletion(STATUS_COMMITTED) called.");

// we are not authenticated anymore - for whatever reason :-( trying to get this info in constructor, now - seems to work.
					if (_sessionID == null)
						throw new IllegalStateException("afterCompletion: sessionID is not assigned!");

					final Map<JDOLifecycleState, Map<Object, DirtyObjectID>> _dirtyObjectIDs = this.dirtyObjectIDs;
					this.dirtyObjectIDs = null;
					final Map<Object, Class<?>> _objectID2Class = this.objectID2Class;
					this.objectID2Class = null;

					Runnable runnable = new Runnable() {
						@Implement
						public void run()
						{
							try {
								if (_objectID2Class != null)
									bridge.getCacheManagerFactory().addObjectID2ClassMap(_objectID2Class);

								// there seems to be a timing issue concerning the commit of a transaction - delaying a sec seems to help - TODO fix this problem in a CLEAN way!
								if (notificationDelayAfterCompletionMSec > 0) {
									try { Thread.sleep(notificationDelayAfterCompletionMSec); } catch (InterruptedException e) { } // ignore
								}

								bridge.getCacheManagerFactory().addDirtyObjectIDs(_sessionID, _dirtyObjectIDs);

								if (logger.isDebugEnabled()) {
									debug("afterCompletion(STATUS_COMMITTED).Runnable: pumped DirtyObjectIDs into CacheManagerFactory: _dirtyObjectIDs.size()=" + _dirtyObjectIDs.size());
									for (Map.Entry<JDOLifecycleState, Map<Object, DirtyObjectID>> me : _dirtyObjectIDs.entrySet()) {
										debug("afterCompletion(STATUS_COMMITTED).Runnable:   * " + me.getKey());
										for (Object oid : me.getValue().keySet()) {
											debug("afterCompletion(STATUS_COMMITTED).Runnable:     - " + oid);
										}
									}
								}

							} catch (Throwable e) {
								error("afterCompletion(STATUS_COMMITTED).Runnable: " + e.getMessage(), e);
							}
						}
					};

					if (notificationDelayAfterCompletionMSec > 0) {
//						Thread t = new Thread(runnable);
//						t.start();
						bridge.flush_threadPoolExecutor.execute(runnable);
					}
					else
						runnable.run();

				} catch (Throwable e) {
					error("afterCompletion(STATUS_COMMITTED): " + e.getMessage(), e);
				}
			}
			else if (status == Status.STATUS_ROLLEDBACK) {
				if (logger.isDebugEnabled())
					debug("afterCompletion(STATUS_ROLLEDBACK) called.");

				dirtyObjectIDs = null;
			}
			else
				error("afterCompletion(...) called with unknown status: " + status, new Exception("Unknown status (" + status + ") in afterCompletion!"));
		}

		// IMHO no sync necessary, because one transaction should only be used by one thread. We use volatile, though, to ensure that it's immediately visible if it has been nulled - just in case.
		private volatile Map<JDOLifecycleState, Map<Object, DirtyObjectID>> dirtyObjectIDs = null;
		private Map<Object, Class<?>> objectID2Class = null;

		protected void registerClass(Object objectID, Class<?> clazz) {
			if (objectID2Class == null)
				objectID2Class = new HashMap<Object, Class<?>>();

			if (objectID2Class.containsKey(objectID))
				return;

			objectID2Class.put(objectID, clazz);
		}

		public void addObject(JDOLifecycleState lifecycleStage, Object object)
		{
			Class<?> clazz = object.getClass();
			if (clazz == IDNamespace.class) // we MUST ignore the IDNamespace changes, because we use the IDGenerator below in: bridge.getCacheManagerFactory().nextDirtyObjectIDSerial() - WRONG! We don't use the IDGenerator here anymore, but still we can optimize it and ignore this! It's not necessary.
				return;

			Object objectID = getObjectID(object);

			if (isDead()) {
				logger.warn("addObject: already dead! Invocation too late! lifecycleStage=" + lifecycleStage + " object=" + object + " objectID=" + objectID, new Exception("StackTrace"));
				return;
			}

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

		private Set<Object> objectIDs_preStoreCalled = new HashSet<Object>();

		public void registerPreStoreCallForObjectID(Object objectID)
		{
			objectIDs_preStoreCalled.add(objectID);
		}
		public boolean isPreStoreCallRegisteredForObjectID(Object objectID)
		{
			return objectIDs_preStoreCalled.contains(objectID);
		}
	} // public static class CacheTransactionListener

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
	{
		
		CacheTransactionListener listener = (CacheTransactionListener)pm.getUserObject();
		// we cannot unregister the CacheTransactionListener after the transaction is finished (pm.setUserObject fails) - hence we use the "dead" flag
		if (listener == null || listener.isDead()) {
			if (logger.isDebugEnabled()) {
				if (listener == null)
					logger.debug("getCacheTransactionListener: PersistenceManager.getUserObject() returned null. Will create and add a CacheTransactionListener.");
				else
					logger.debug("getCacheTransactionListener: PersistenceManager.getUserObject() returned a dead listener ["+listener.getIdentityString()+"]. Will create and add a new CacheTransactionListener.");
			}

			listener = new CacheTransactionListener(this);
			try {
//				TransactionManager tm = getCacheManagerFactory().getTransactionManager();
//				javax.transaction.Transaction tx = tm.getTransaction();
//				tx.registerSynchronization(listener);

				// save the currently assigned Synchronization (if there is any) for delegation
				listener.synchronization = pm.currentTransaction().getSynchronization();

				// assign our listener as current Synchronization
				pm.currentTransaction().setSynchronization(listener);

				// assign the listener to the current PersistenceManager so we find it again
				// (i.e. share it and don't create a new one within this transaction).
				pm.setUserObject(listener);

				if (logger.isDebugEnabled())
					logger.debug("getCacheTransactionListener: CacheTransactionListener ["+listener.getIdentityString()+"] registered as Synchronization in current transaction.");
			} catch (Exception x) {
				logger.error("getCacheTransactionListener: Could not register Synchronization! The CacheTransactionListener ["+listener.getIdentityString()+"] was NOT registered!", x);
			}
		}
		else {
			if (logger.isDebugEnabled())
				logger.debug("getCacheTransactionListener: PersistenceManager.getUserObject() returned a living CacheTransactionListener ["+listener.getIdentityString()+"]. No registration necessary.");
		}

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
				logger.debug("CreateLifecycleListener.postCreate: " + JDOHelper.getObjectId(event.getPersistentInstance()));

			registerJDOObject(JDOLifecycleState.NEW, event.getPersistentInstance());
		}
	};

	private StoreLifecycleListener storeLifecycleListener = new StoreLifecycleListener() {
		public void preStore(InstanceLifecycleEvent event) {
			Object object = event.getPersistentInstance();
			Object objectID = JDOHelper.getObjectId(object);
			if (objectID != null) {
				PersistenceManager pm = JDOHelper.getPersistenceManager(object);
				if (pm == null)
					throw new IllegalArgumentException("Could not obtain a PersistenceManager from this object!");

				getCacheTransactionListener(pm).registerPreStoreCallForObjectID(objectID);

				if (logger.isDebugEnabled())
					logger.debug("StoreLifecycleListener.preStore: " + objectID);
			}
		}
		public void postStore(InstanceLifecycleEvent event) { }
	};

	private DirtyLifecycleListener dirtyLifecycleListener = new DirtyLifecycleListener() {
		public void preDirty(InstanceLifecycleEvent event) { }
		public void postDirty(javax.jdo.listener.InstanceLifecycleEvent event)
		{
			if (logger.isDebugEnabled())
				logger.debug("DirtyLifecycleListener.postDirty: " + JDOHelper.getObjectId(event.getPersistentInstance()));

			registerJDOObject(JDOLifecycleState.DIRTY, event.getPersistentInstance());
		}
	};

	private AttachLifecycleListener attachLifecycleListener = new AttachLifecycleListener() {
		public void preAttach(InstanceLifecycleEvent event) {
		}
		public void postAttach(InstanceLifecycleEvent event)
		{
			Object object = event.getPersistentInstance();
			Object objectID = JDOHelper.getObjectId(object);
			PersistenceManager pm = JDOHelper.getPersistenceManager(object);
			if (pm == null)
				throw new IllegalArgumentException("Could not obtain a PersistenceManager from this object!");

			CacheTransactionListener cacheTransactionListener = getCacheTransactionListener(pm);
			if (cacheTransactionListener.isPreStoreCallRegisteredForObjectID(objectID)) {
				cacheTransactionListener.addObject(JDOLifecycleState.NEW, event.getPersistentInstance());

				if (logger.isDebugEnabled())
					logger.debug("AttachLifecycleListener.postAttach [new]: " + objectID);
			}
			else {
				cacheTransactionListener.addObject(JDOLifecycleState.DIRTY, event.getPersistentInstance());

				if (logger.isDebugEnabled())
					logger.debug("AttachLifecycleListener.postAttach [dirty]: " + objectID);
			}
		}
	};

	private DeleteLifecycleListener deleteLifecycleListener = new DeleteLifecycleListener() {
		public void preDelete(InstanceLifecycleEvent event)
		{
			if (logger.isDebugEnabled())
				logger.debug("DeleteLifecycleListener.preDelete: " + JDOHelper.getObjectId(event.getPersistentInstance()));

			registerJDOObject(JDOLifecycleState.DELETED, event.getPersistentInstance());
		}
		public void postDelete(InstanceLifecycleEvent event) { }
	};

	/* non-javadoc
	 * @see org.nightlabs.jfire.jdo.cache.bridge.JdoCacheBridge#init()
	 */
	@Override
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
		pmf.addInstanceLifecycleListener(
				storeLifecycleListener,
				objectClassArray);
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

	private static final Class<?>[] objectClassArray = null; // new Class[] { Object.class };

	private SecurityReflector securityReflector;

	/* non-javadoc
	 * @see org.nightlabs.jfire.jdo.cache.bridge.JdoCacheBridge#close()
	 */
	@Override
	public void close()
	{
		PersistenceManagerFactory pmf = getPersistenceManagerFactory();

		if (pmf != null) {
			pmf.removeInstanceLifecycleListener(createLifecycleListener);
			pmf.removeInstanceLifecycleListener(storeLifecycleListener);
			pmf.removeInstanceLifecycleListener(dirtyLifecycleListener);
			pmf.removeInstanceLifecycleListener(attachLifecycleListener);
			pmf.removeInstanceLifecycleListener(deleteLifecycleListener);
		}

		flush_threadPoolExecutor.shutdown();
	}
}
