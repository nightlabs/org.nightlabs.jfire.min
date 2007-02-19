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

import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.jdo.notification.persistent.id.NotificationBundleID;
import org.nightlabs.jfire.jdo.notification.persistent.id.PushNotifierID;
import org.nightlabs.jfire.jdo.notification.persistent.id.SubscriptionID;

/**
 * @ejb.bean
 *		name="jfire/ejb/JFireBaseBean/PersistentNotificationEJB"
 *		jndi-name="jfire/ejb/JFireBaseBean/PersistentNotificationEJB"
 *		type="Stateless"
 *   
 * @ejb.util generate="physical"
 */
public abstract class PersistentNotificationEJBBean
extends BaseSessionBeanImpl implements SessionBean 
{
	private static final Logger logger = Logger.getLogger(PersistentNotificationEJBBean.class);

	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	/**
	 * @ejb.create-method  
	 * @ejb.permission role-name="LanguageManager-read"
	 */
	public void ejbCreate() throws CreateException { }

	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 * 
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException { }

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 **/
	public void initialise() 
	{
		PersistenceManager pm = this.getPersistenceManager();
		try {
			pm.getExtent(PushNotifierOrganisation.class);
			try {
				PushNotifier pushNotifier = (PushNotifier) pm.getObjectById(PushNotifierID.create(PushNotifierOrganisation.SUBSCRIBER_TYPE_ORGANISATION));
				logger.info("There exists already a PushNotifier for the subscriberType '"+ PushNotifierOrganisation.SUBSCRIBER_TYPE_ORGANISATION +"': " + pushNotifier.getClass());
			} catch (JDOObjectNotFoundException x) {
				logger.info("Creating and persisting PushNotifier '"+PushNotifierOrganisation.class.getName()+"' for the subscriberType '"+ PushNotifierOrganisation.SUBSCRIBER_TYPE_ORGANISATION +"'.");
				pm.makePersistent(new PushNotifierOrganisation(PushNotifierOrganisation.SUBSCRIBER_TYPE_ORGANISATION));
			}

		} finally {
			pm.close();
		}
	}

//	/**
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 **/
//	public Set<SubscriptionID> getSubscriptionIDs()
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try  {
//
//		} finally {
//			pm.close();
//		}
//	}
//
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 **/
	@SuppressWarnings("unchecked")
	public List<Subscription> getSubscriptions(Set<SubscriptionID> subscriptionIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try  {
			return NLJDOHelper.getDetachedObjectList(pm, subscriptionIDs, Subscription.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 **/
	public Subscription storeSubscription(Subscription subscription, boolean get, String[] fetchGroups, int maxFetchDepth) 
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return (Subscription) NLJDOHelper.storeJDO(pm, subscription, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 **/
	@SuppressWarnings("unchecked")
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

	/**
	 * @param subscriptionID Identifies the subscription, for which to retrieve the {@link NotificationBundle}s. Must not be <code>null</code>.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 **/
	@SuppressWarnings("unchecked")
	public List<NotificationBundle> getNotificationBundles(SubscriptionID subscriptionID, String[] fetchGroups, int maxFetchDepth)
	{
		if (subscriptionID == null)
			throw new IllegalArgumentException("subscriptionID must not be null!");

		PersistenceManager pm = getPersistenceManager();
		try  {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return (List<NotificationBundle>) pm.detachCopyAll(NotificationBundle.getNotificationBundles(pm, subscriptionID));
		} finally {
			pm.close();
		}
	}

	/**
	 * This method is called by the {@link PushNotifierOrganisation} in order to notify the
	 * subscriber about new/dirty/deleted JDO objects.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 **/
	public void notifySubscription(NotificationBundle notificationBundle)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			
		} finally {
			pm.close();
		}
	}
}
