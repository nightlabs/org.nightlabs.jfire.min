/*
 * Created on Jul 30, 2005
 */
package org.nightlabs.jfire.jdo.cache.bridge;

import java.util.HashSet;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.listener.AttachLifecycleListener;
import javax.jdo.listener.DirtyLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;

import org.apache.log4j.Logger;
import org.jpox.resource.PersistenceManagerImpl;
import org.nightlabs.jfire.servermanager.j2ee.SecurityReflector;



/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class JdoCacheBridgeJPOX extends JdoCacheBridge
{
	public static final Logger LOGGER = Logger.getLogger(JdoCacheBridgeJPOX.class);

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
				LOGGER.debug("localTransactionCommitted(...) called.");
				if (dirtyObjectIDs != null) {
					bridge.getCacheManagerFactory().addDirtyObjectIDs(
							bridge.securityReflector.whoAmI().getSessionID(), dirtyObjectIDs);
					dirtyObjectIDs = null;
				}
			} catch (Throwable e) {
				LOGGER.error("", e);
			}
		}

		public void localTransactionRolledback(javax.resource.spi.ConnectionEvent event)
		{
			LOGGER.debug("localTransactionRolledback(...) called.");
			dirtyObjectIDs = null;
		}

		/**
		 * @see javax.resource.spi.ConnectionEventListener#connectionClosed(javax.resource.spi.ConnectionEvent)
		 */
		public void connectionClosed(ConnectionEvent event)
		{
		}

		/**
		 * @see javax.resource.spi.ConnectionEventListener#connectionErrorOccurred(javax.resource.spi.ConnectionEvent)
		 */
		public void connectionErrorOccurred(ConnectionEvent event)
		{
		}

		/**
		 * @see javax.resource.spi.ConnectionEventListener#localTransactionStarted(javax.resource.spi.ConnectionEvent)
		 */
		public void localTransactionStarted(ConnectionEvent event)
		{
		}

		// IMHO no sync necessary, because one transaction should only be used by one thread.
		private HashSet dirtyObjectIDs = null;

		public void addDirtyObjectID(Object objectID)
		{
			if (dirtyObjectIDs == null)
				dirtyObjectIDs = new HashSet();

			if (!dirtyObjectIDs.contains(objectID))
				dirtyObjectIDs.add(objectID);
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
			LOGGER.debug("PersistenceManagerImpl.getUserObject() returned null. Will create and add a CacheTransactionListener.");
			listener = new CacheTransactionListener(this);
			pm.addConnectionEventListener(listener);
			pm.setUserObject(listener);
		}
		else
			LOGGER.debug("PersistenceManagerImpl.getUserObject() returned a CacheTransactionListener. No registration necessary.");

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

	private void makeDirty(Object object)
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

			getCacheTransactionListener(pm).addDirtyObjectID(objectID);
		} catch (Throwable t) {
			LOGGER.error("Failed to make object dirty: " + object, t);
		}
	}

	private DirtyLifecycleListener dirtyLifecycleListener = new DirtyLifecycleListener() {
		public void preDirty(InstanceLifecycleEvent arg0)
		{
		}
		public void postDirty(javax.jdo.listener.InstanceLifecycleEvent event)
		{
			LOGGER.debug("object became dirty: source="+event.getSource()+" target="+event.getTarget());
			makeDirty(event.getSource());
		}
	};

	private AttachLifecycleListener attachLifecycleListener = new AttachLifecycleListener() {
		public void preAttach(InstanceLifecycleEvent event)
		{
		}
		public void postAttach(InstanceLifecycleEvent event)
		{
			LOGGER.debug("object was attached: source="+event.getSource()+" target="+event.getTarget());
			makeDirty(event.getSource());
		}
	};

	/**
	 * @see org.nightlabs.jfire.jdo.cache.bridge.JdoCacheBridge#init()
	 */
	public void init()
	{
		PersistenceManagerFactory pmf = getPersistenceManagerFactory();

		pmf.addInstanceLifecycleListener(
				dirtyLifecycleListener,
				null);
		pmf.addInstanceLifecycleListener(
				attachLifecycleListener,
				null);

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

		// A new object cannot be open anywhere and therefore, there are no listeners
		// anyway for it.
//		pmf.addInstanceLifecycleListener(
//				new StoreLifecycleListener() {
//					public void preStore(InstanceLifecycleEvent event)
//					{
//					}
//					public void postStore(InstanceLifecycleEvent event)
//					{
//						LOGGER.debug("object was stored: source="+event.getSource()+" target="+event.getTarget());
//						makeDirty(event.getSource());
//					}
//				},
//				null);

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
