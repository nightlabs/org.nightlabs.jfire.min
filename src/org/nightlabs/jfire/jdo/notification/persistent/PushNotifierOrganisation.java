package org.nightlabs.jfire.jdo.notification.persistent;

import javax.jdo.PersistenceManager;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.base.Lookup;

/**
 * This implementation of 
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class PushNotifierOrganisation
extends PushNotifier
{
	public static final String SUBSCRIBER_TYPE_ORGANISATION = "organisation";

	public PushNotifierOrganisation(String subscriberType)
	{
		super(subscriberType);

		if (!SUBSCRIBER_TYPE_ORGANISATION.equals(subscriberType))
			throw new IllegalArgumentException("The PushNotifierOrganisation must be created with subscriberType SUBSCRIBER_TYPE_ORGANISATION!!! This subscriberType is illegal: " + subscriberType);
	}

	/**
	 * @deprecated Only for JDO!
	 */
	protected PushNotifierOrganisation() {}

	@Implement
	public void push(NotificationBundle notificationBundle)
	throws Exception
	{
		Subscription subscription = notificationBundle.getSubscription();
		if (!SUBSCRIBER_TYPE_ORGANISATION.equals(subscription.getSubscriberType()))
			throw new IllegalArgumentException("notificationBundle.subscriberType illegal: " + subscription.getSubscriberType());

		PersistenceManager pm = getPersistenceManager();

		String subscriberOrganisationID = subscription.getSubscriberID();
		PersistentNotificationEJB pn = PersistentNotificationEJBUtil.getHome(Lookup.getInitialContextProperties(pm, subscriberOrganisationID)).create();

	}
}
