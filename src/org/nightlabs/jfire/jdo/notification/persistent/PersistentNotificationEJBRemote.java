package org.nightlabs.jfire.jdo.notification.persistent;

import java.util.List;
import java.util.Set;

import javax.ejb.Remote;

import org.nightlabs.jfire.jdo.notification.persistent.id.NotificationBundleID;
import org.nightlabs.jfire.jdo.notification.persistent.id.NotificationFilterID;

@Remote
public interface PersistentNotificationEJBRemote 
{
	String ping(String message);

	void initialise();

	List<NotificationFilter> getNotificationFilters(
			Set<NotificationFilterID> notificationFilterIDs,
			String[] fetchGroups, int maxFetchDepth);

	NotificationFilter storeNotificationFilter(
			NotificationFilter notificationFilter, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	void deleteNotificationBundles(
			Set<NotificationBundleID> notificationBundleIDs);

	/**
	 * @param notificationFilterID Identifies the subscription, for which to retrieve the {@link NotificationBundle}s. Must not be <code>null</code>.
	 */
	List<NotificationBundle> getNotificationBundles(
			NotificationFilterID notificationFilterID, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * This method is called by the {@link PushNotifierOrganisation} in order to notify the
	 * subscriber about new/dirty/deleted JDO objects.
	 */
	void pushNotificationBundle(NotificationBundle notificationBundle)
			throws Exception;
}