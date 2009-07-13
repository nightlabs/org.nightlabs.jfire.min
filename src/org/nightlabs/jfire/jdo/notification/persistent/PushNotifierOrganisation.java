package org.nightlabs.jfire.jdo.notification.persistent;

import java.lang.reflect.Method;

import javax.jdo.PersistenceManager;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;

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
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class PushNotifierOrganisation
extends PushNotifier
{
	public PushNotifierOrganisation(String subscriberType)
	{
		super(subscriberType);

		if (!SubscriptionUtil.SUBSCRIBER_TYPE_ORGANISATION.equals(subscriberType))
			throw new IllegalArgumentException("The PushNotifierOrganisation must be created with subscriberType SUBSCRIBER_TYPE_ORGANISATION!!! This subscriberType is illegal: " + subscriberType);
	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected PushNotifierOrganisation() {}

	private static Class<?> delegate = null;

	@Override
	public void push(NotificationBundle notificationBundle)
	throws Exception
	{
		if (delegate == null)
			delegate = Class.forName("org.nightlabs.jfire.jdo.notification.persistent.PushNotifierOrganisationDelegate");

		Object push_instance = delegate.newInstance();
		Method push_method = delegate.getMethod("push", new Class[] { PersistenceManager.class, NotificationBundle.class });
		push_method.invoke(push_instance, new Object[] { getPersistenceManager(), notificationBundle });
	}
}
