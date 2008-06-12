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

package org.nightlabs.jfire.jdo;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.jdo.cache.CacheManager;
import org.nightlabs.jfire.jdo.cache.NotificationBundle;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;


/**
 * @ejb.bean name="jfire/ejb/JFireBaseBean/JDOManager"
 *           jndi-name="jfire/ejb/JFireBaseBean/JDOManager"
 *           type="Stateless"
 *           transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
public abstract class JDOManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(JDOManagerBean.class);

	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate()
	throws CreateException
	{
	}
	/**
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException
	{
	}

	/**
	 * This method finds out the type of a <tt>PersistenceCapable</tt> object defined
	 * by its JDO object ID.
	 *
	 * @param objectID A JDO object ID specifying a persistent object.
	 *		This should implement {@link org.nightlabs.jdo.ObjectID}, because the client's logic
	 *		is triggered by that tagging interface.
	 * @return Returns the fully qualified class name of the JDO persistence capable object
	 *		defined by the given <tt>objectID</tt>.
	 *
	 * @throws javax.jdo.JDOObjectNotFoundException if no persistent object exists with the
	 *		given ID.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public String getPersistenceCapableClassName(Object objectID)
	{
		try {
			Class<?> clazz;

			CacheManager cm = getLookup().getCacheManager();
			try {
				clazz = cm.getClassByObjectID(objectID, false);
			} finally {
				cm.close();
			}

			if (clazz != null)
				return clazz.getName();

			PersistenceManager pm = getPersistenceManager();
			try {
				Object o = pm.getObjectById(objectID);
				return o.getClass().getName();
			} finally {
				pm.close();
			}
		} catch (Throwable t) {
			logger.error("Could not find out class for objectID! objectID.class=" + (objectID == null ? null : objectID.getClass().getName()) + " objectID=" + objectID, t);
			if (t instanceof RuntimeException)
				throw (RuntimeException)t;
			else
				throw new RuntimeException(t);
		}
	}

	/**
	 * This method removes and adds listeners to your cache session. The listeners cause
	 * the client to get notified if the
	 * persistence capable objects specified by <tt>addObjectIDs</tt> have been changed.
	 * <p>
	 * The remove action will be performed before the add action. Hence, if an objectID
	 * is in both of them, it will be added in total.
	 *
	 * @param removeObjectIDs Either <tt>null</tt> or the object-ids of those JDO objects for which to remove listeners
	 * @param addObjectIDs Either <tt>null</tt> or the object-ids of those JDO objects for which to add listeners
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports" @!This method must never use transactions, because it is not necessary and if it is (in the CacheManagerFactory), it will manage a transaction itself.
	 */
	public void removeOrAddListeners(
			Collection<Object> removeObjectIDs,
			Collection<Object> addObjectIDs,
			Collection<Long> removeFilterIDs,
			Collection<IJDOLifecycleListenerFilter> addFilters
	)
	{
		CacheManager cm = getLookup().getCacheManager(getPrincipal());
		try {

			if (removeObjectIDs != null)
				cm.removeChangeListeners(removeObjectIDs);

			if (addObjectIDs != null)
				cm.addChangeListeners(addObjectIDs);

			if (removeFilterIDs != null)
				cm.removeLifecycleListenerFilters(removeFilterIDs);

			if (addFilters != null)
				cm.addLifecycleListenerFilters(addFilters);

			if (logger.isDebugEnabled()) {
				logger.debug("removeOrAddListeners(sessionID="+getSessionID()+"): removeObjectIDs.size()=" + (removeObjectIDs == null ? null : removeObjectIDs.size()));
				if (removeObjectIDs != null) {
					for (Object oid : removeObjectIDs)
						logger.debug("removeOrAddListeners(sessionID="+getSessionID()+"):   * " + oid);
				}

				logger.debug("removeOrAddListeners(sessionID="+getSessionID()+"): addObjectIDs.size()=" + (addObjectIDs == null ? null : addObjectIDs.size()));
				if (addObjectIDs != null) {
					for (Object oid : addObjectIDs)
						logger.debug("removeOrAddListeners(sessionID="+getSessionID()+"):   * " + oid);
				}

				logger.debug("removeOrAddListeners(sessionID="+getSessionID()+"): removeFilterIDs.size()=" + (removeFilterIDs == null ? null : removeFilterIDs.size()));
				if (removeFilterIDs != null) {
					for (Long filterID : removeFilterIDs)
						logger.debug("removeOrAddListeners(sessionID="+getSessionID()+"):   * " + filterID);
				}

				logger.debug("removeOrAddListeners(sessionID="+getSessionID()+"): addFilters.size()=" + (addFilters == null ? null : addFilters.size()));
				if (addFilters != null) {
					for (IJDOLifecycleListenerFilter filter : addFilters)
						logger.debug("removeOrAddListeners(sessionID="+getSessionID()+"):   * " + filter.getFilterID().getFilterID());
				}
			}

		} finally {
			cm.close();
		}
	}

	/**
	 * This method diffs the listeners of your cache session with the ones that
	 * should be there (specified by <tt>subscribedObjectIDs</tt>). Then it adds
	 * the missing and removes the ones that shouldn't be there.
	 *
	 * @param cacheSessionID The ID of your session.
	 * @param removeObjectIDs Either <tt>null</tt> or the object-ids of those JDO objects for which to remove listeners
	 * @param addObjectIDs Either <tt>null</tt> or the object-ids of those JDO objects for which to add listeners
	 *
	 * @see JDOManager#removeAddChangeListeners(java.lang.String, java.util.Collection, java.util.Collection)
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports" @!This method must never use transactions, because it is not necessary and if it is (in the CacheManagerFactory), it will manage a transaction itself.
	 */
	public void resubscribeAllListeners(
			Set<Object> subscribedObjectIDs,
			Collection<IJDOLifecycleListenerFilter> filters
	)
	{
		CacheManager cm = getLookup().getCacheManager(getPrincipal());
		try {
			cm.resubscribeAllListeners(subscribedObjectIDs, filters);

			if (logger.isDebugEnabled()) {
				logger.debug("resubscribeAllListeners(sessionID="+getSessionID()+"): subscribedObjectIDs.size()=" + (subscribedObjectIDs == null ? null : subscribedObjectIDs.size()));
				if (subscribedObjectIDs != null) {
					for (Object oid : subscribedObjectIDs)
						logger.debug("resubscribeAllListeners(sessionID="+getSessionID()+"):   * " + oid);
				}

				logger.debug("resubscribeAllListeners(sessionID="+getSessionID()+"): filters.size()=" + (filters == null ? null : filters.size()));
				if (filters != null) {
					for (IJDOLifecycleListenerFilter filter : filters)
						logger.debug("resubscribeAllListeners(sessionID="+getSessionID()+"):   * " + filter.getFilterID().getFilterID());
				}
			}
		} finally {
			cm.close();
		}
	}

	/**
	 * This method removes all listeners that have been registered for
	 * the current cache session. The method <tt>waitForChanges(...)</tt>
	 * will be released (if it's currently waiting).
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public void closeCacheSession()
	{
		CacheManager cm = getLookup().getCacheManager(getPrincipal());
		try {
			cm.closeCacheSession();
		} finally {
			cm.close();
		}
	}

	/**
	 * This method blocks and returns not before a certain timeout occured
	 * or when <tt>closeCacheSession(...)</tt> has been called or - the main
	 * reason - at least one persistence-capable object has been changed.
	 * Because this method tries to collect multiple (by returning only at
	 * predefined time spots and by reacting only at the end of a transaction),
	 * it might return many object ids.
	 *
	 * @param cacheSessionID The ID of your cache session.
	 * @param waitTimeout The time in milliseconds defining how long this
	 *		method shall wait for changes, before it returns <tt>null</tt>.
	 *
	 * @return Returns either <tt>null</tt> if nothing changed or a {@link NotificationBundle}
	 *		of object ids. Hence {@link NotificationBundle#isEmpty()} will never return <code>true</code>
	 *		(<code>null</code> would have been returned instead of a <code>NotificationBundle</code>).
	 *
	 * @see CacheManager#waitForChanges(long)
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports" @!This method should never become transactional since it waits very very long and likely exceeds transaction timeouts. Furthermore it's not necessary to use a transaction here.
	 */
	public NotificationBundle waitForChanges(long waitTimeout)
	{
		CacheManager cm = getLookup().getCacheManager(getPrincipal());
		try {
			NotificationBundle notificationBundle = cm.waitForChanges(waitTimeout);
			if (logger.isDebugEnabled()) {
				if (notificationBundle == null)
					logger.debug("waitForChanges(sessionID="+getSessionID()+"): notificationBundle is null.");
				else {
					logger.debug("waitForChanges(sessionID="+getSessionID()+"): notificationBundle.getDirtyObjectIDs().size()=" + (notificationBundle.getDirtyObjectIDs() == null ? null : notificationBundle.getDirtyObjectIDs().size()));
					if (notificationBundle.getDirtyObjectIDs() != null) {
						for (DirtyObjectID dirtyObjectID : notificationBundle.getDirtyObjectIDs())
							logger.debug("waitForChanges(sessionID="+getSessionID()+"):   * " + dirtyObjectID);
					}

					logger.debug("waitForChanges(sessionID="+getSessionID()+"): notificationBundle.getFilterID2dirtyObjectIDs().size()=" + (notificationBundle.getFilterID2dirtyObjectIDs() == null ? null : notificationBundle.getFilterID2dirtyObjectIDs().size()));
					if (notificationBundle.getFilterID2dirtyObjectIDs() != null) {
						for (Map.Entry<Long, SortedSet<DirtyObjectID>> me1 : notificationBundle.getFilterID2dirtyObjectIDs().entrySet()) {
							logger.debug("waitForChanges(sessionID="+getSessionID()+"):   * filterID=" + me1.getKey() + " dirtyObjectIDs.size()=" + (me1.getValue() == null ? null : me1.getValue().size()));
							if (me1.getValue() != null) {
								for (DirtyObjectID dirtyObjectID : me1.getValue())
									logger.debug("waitForChanges(sessionID="+getSessionID()+"):     - " + dirtyObjectID);
							}
						}
					}
				}
			}
			return notificationBundle;
		} finally {
			cm.close();
		}
	}

}
