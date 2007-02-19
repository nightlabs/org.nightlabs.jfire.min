package org.nightlabs.jfire.jdo.notification.persistent;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.base.Lookup;

public class PushNotifierOrganisationDelegate
{
	public void push(PersistenceManager pm, NotificationBundle notificationBundle)
	throws Exception
	{
		Subscription subscription = notificationBundle.getSubscription();
		if (!PushNotifierOrganisation.SUBSCRIBER_TYPE_ORGANISATION.equals(subscription.getSubscriberType()))
			throw new IllegalArgumentException("notificationBundle.subscriberType illegal: " + subscription.getSubscriberType());

		String subscriberOrganisationID = subscription.getSubscriberID();
		PersistentNotificationEJB pn = PersistentNotificationEJBUtil.getHome(Lookup.getInitialContextProperties(pm, subscriberOrganisationID)).create();

	}
}
