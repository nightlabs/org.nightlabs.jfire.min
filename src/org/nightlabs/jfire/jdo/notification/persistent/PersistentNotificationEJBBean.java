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

import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.jdo.notification.persistent.id.NotificationBundleID;
import org.nightlabs.jfire.jdo.notification.persistent.id.NotificationFilterID;
import org.nightlabs.jfire.jdo.notification.persistent.id.NotificationReceiverID;
import org.nightlabs.jfire.jdo.notification.persistent.id.PushNotifierID;

/**
 * @ejb.bean
 *		name="jfire/ejb/JFireBaseBean/PersistentNotificationEJB"
 *		jndi-name="jfire/ejb/JFireBaseBean/PersistentNotificationEJB"
 *		type="Stateless"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class PersistentNotificationEJBBean
extends BaseSessionBeanImpl implements PersistentNotificationEJBRemote
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(PersistentNotificationEJBBean.class);

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jdo.notification.persistent.PersistentNotificationEJBRemote#initialise()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void initialise()
	{
		PersistenceManager pm = this.getPersistenceManager();
		try {
			pm.getExtent(PushNotifierOrganisation.class);
			try {
				PushNotifier pushNotifier = (PushNotifier) pm.getObjectById(PushNotifierID.create(SubscriptionUtil.SUBSCRIBER_TYPE_ORGANISATION));
				logger.info("There exists already a PushNotifier for the subscriberType '"+ SubscriptionUtil.SUBSCRIBER_TYPE_ORGANISATION +"': " + pushNotifier.getClass());
			} catch (JDOObjectNotFoundException x) {
				logger.info("Creating and persisting PushNotifier '"+PushNotifierOrganisation.class.getName()+"' for the subscriberType '"+ SubscriptionUtil.SUBSCRIBER_TYPE_ORGANISATION +"'.");
				pm.makePersistent(new PushNotifierOrganisation(SubscriptionUtil.SUBSCRIBER_TYPE_ORGANISATION));
			}

		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jdo.notification.persistent.PersistentNotificationEJBRemote#getNotificationFilters(java.util.Set, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public List<NotificationFilter> getNotificationFilters(Set<NotificationFilterID> notificationFilterIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try  {
			return NLJDOHelper.getDetachedObjectList(pm, notificationFilterIDs, NotificationFilter.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jdo.notification.persistent.PersistentNotificationEJBRemote#storeNotificationFilter(org.nightlabs.jfire.jdo.notification.persistent.NotificationFilter, boolean, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public NotificationFilter storeNotificationFilter(NotificationFilter notificationFilter, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (logger.isDebugEnabled())
				logger.debug("storeNotificationFilter: user="+getPrincipal() + " notificationFilter: " + notificationFilter);

			return NLJDOHelper.storeJDO(pm, notificationFilter, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jdo.notification.persistent.PersistentNotificationEJBRemote#deleteNotificationBundles(java.util.Set)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public void deleteNotificationBundles(Set<NotificationBundleID> notificationBundleIDs)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Set<NotificationBundle> notificationBundles = NLJDOHelper.getObjectSet(pm, notificationBundleIDs, NotificationBundleID.class);
			pm.deletePersistentAll(notificationBundles);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jdo.notification.persistent.PersistentNotificationEJBRemote#getNotificationBundles(org.nightlabs.jfire.jdo.notification.persistent.id.NotificationFilterID, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public List<NotificationBundle> getNotificationBundles(NotificationFilterID notificationFilterID, String[] fetchGroups, int maxFetchDepth)
	{
		if (notificationFilterID == null)
			throw new IllegalArgumentException("subscriptionID must not be null!");

		PersistenceManager pm = getPersistenceManager();
		try  {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return (List<NotificationBundle>) pm.detachCopyAll(NotificationBundle.getNotificationBundles(pm, notificationFilterID));
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jdo.notification.persistent.PersistentNotificationEJBRemote#pushNotificationBundle(org.nightlabs.jfire.jdo.notification.persistent.NotificationBundle)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public void pushNotificationBundle(NotificationBundle notificationBundle)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(NotificationReceiver.class);

			NotificationReceiverID notificationReceiverID = NotificationReceiverID.create(
					notificationBundle.getOrganisationID(),
					notificationBundle.getSubscriberType(),
					notificationBundle.getSubscriberID(),
					notificationBundle.getSubscriptionID());
			NotificationReceiver notificationReceiver;
			try {
				notificationReceiver = (NotificationReceiver) pm.getObjectById(notificationReceiverID);
			} catch (JDOObjectNotFoundException x) {
				logger.error("No NotificationReceiver existing for NotificationBundle: " + notificationReceiverID, x);
				return;
			}

			notificationReceiver.onReceiveNotificationBundle(notificationBundle);
		} finally {
			pm.close();
		}
	}
}
