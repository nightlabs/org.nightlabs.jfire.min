package org.nightlabs.jfire.jdo.notification.persistent;

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.asyncinvoke.AsyncInvoke;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnqueueException;
import org.nightlabs.jfire.jdo.notification.persistent.id.NotificationBundleID;

public class PushManager
{
	/**
	 * This method is called by the {@link PersistentNotificationManagerFactory} via reflection.
	 * It is executed as the user who owns the {@link NotificationFilter} of the given {@link NotificationBundle}.
	 *
	 * @param notificationBundle The data that needs to be forwarded to the subscriber. It is currently attached
	 *		to a datastore (when this method is called).
	 */
	public void pushNotificationBundle(NotificationBundle notificationBundle) throws AsyncInvokeEnqueueException
	{
		NotificationBundleID notificationBundleID = (NotificationBundleID) JDOHelper.getObjectId(notificationBundle);
		AsyncInvoke.exec(
				new PushInvocation(notificationBundleID),
//				new PushSuccessCallback(notificationBundleID),
				true);
	}
}
