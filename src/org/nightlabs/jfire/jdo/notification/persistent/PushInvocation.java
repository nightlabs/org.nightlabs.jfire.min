package org.nightlabs.jfire.jdo.notification.persistent;

import java.io.Serializable;

import javax.jdo.PersistenceManager;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.jdo.notification.persistent.id.NotificationBundleID;
import org.nightlabs.jfire.jdo.notification.persistent.id.PushNotifierID;

public class PushInvocation
extends Invocation
{
	private static final long serialVersionUID = 1L;

	private NotificationBundleID notificationBundleID;

	public PushInvocation(NotificationBundleID notificationBundleID)
	{
		this.notificationBundleID = notificationBundleID;
	}

	@Implement
	public Serializable invoke()
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(NotificationBundle.class);
			NotificationBundle notificationBundle = (NotificationBundle) pm.getObjectById(notificationBundleID);

			pm.getExtent(PushNotifier.class);
			PushNotifier pushNotifier = (PushNotifier) pm.getObjectById(
					PushNotifierID.create(notificationBundle.getSubscriberType()));

			pushNotifier.push(notificationBundle);

			pm.deletePersistent(notificationBundle);
		} finally {
			pm.close();
		}
		return null;
	}

}
