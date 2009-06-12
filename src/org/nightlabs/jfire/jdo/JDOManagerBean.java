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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
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
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class JDOManagerBean
extends BaseSessionBeanImpl
implements JDOManagerRemote
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(JDOManagerBean.class);

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jdo.JDOManagerRemote#getPersistenceCapableClassName(java.lang.Object)
	 */
	@RolesAllowed("_Guest_")
	@Override
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

			PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jdo.JDOManagerRemote#removeOrAddListeners(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("_Guest_")
	@Override
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jdo.JDOManagerRemote#resubscribeAllListeners(java.util.Set, java.util.Collection)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("_Guest_")
	@Override
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jdo.JDOManagerRemote#closeCacheSession()
	 */
	@RolesAllowed("_Guest_")
	@Override
	public void closeCacheSession()
	{
		CacheManager cm = getLookup().getCacheManager(getPrincipal());
		try {
			cm.closeCacheSession();
		} finally {
			cm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jdo.JDOManagerRemote#waitForChanges(long)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("_Guest_")
	@Override
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
