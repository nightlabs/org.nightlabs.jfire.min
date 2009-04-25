package org.nightlabs.jfire.jdo.notification.persistent;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.Lookup;

public class PushNotifierOrganisationDelegate
{
	public void push(PersistenceManager pm, NotificationBundle notificationBundle)
	throws Exception
	{
		if (!SubscriptionUtil.SUBSCRIBER_TYPE_ORGANISATION.equals(notificationBundle.getSubscriberType()))
			throw new IllegalArgumentException("notificationBundle.subscriberType illegal: " + notificationBundle.getSubscriberType());

		String subscriberOrganisationID = notificationBundle.getSubscriberID();
		PersistentNotificationEJBRemote pn = JFireEjb3Factory.getRemoteBean(PersistentNotificationEJBRemote.class, Lookup.getInitialContextProperties(pm, subscriberOrganisationID));
//		PersistentNotificationEJB pn = PersistentNotificationEJBUtil.getHome(Lookup.getInitialContextProperties(pm, subscriberOrganisationID)).create();
		pn.pushNotificationBundle(notificationBundle);
	}
}
