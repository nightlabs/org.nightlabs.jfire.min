package org.nightlabs.jfire.jdo;

import java.util.Collection;
import java.util.Set;

import javax.ejb.Remote;

import org.nightlabs.jfire.jdo.cache.CacheManager;
import org.nightlabs.jfire.jdo.cache.NotificationBundle;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;

@Remote
public interface JDOManagerRemote 
{
	String ping(String message);

	/**
	 * This method finds out the type of a <tt>PersistenceCapable</tt> object defined
	 * by its JDO object ID.
	 *
	 * @param objectID A JDO object ID specifying a persistent object.
	 *		This should implement {@link org.nightlabs.jdo.ObjectID}, because the client's logic
	 *		is triggered by that tagging interface.
	 * @return Returns the fully qualified class name of the JDO persistence capable object
	 *		defined by the given <tt>objectID</tt>.
	 *
	 * @throws javax.jdo.JDOObjectNotFoundException if no persistent object exists with the
	 *		given ID.
	 */
	String getPersistenceCapableClassName(Object objectID);

	/**
	 * This method removes and adds listeners to your cache session. The listeners cause
	 * the client to get notified if the
	 * persistence capable objects specified by <tt>addObjectIDs</tt> have been changed.
	 * <p>
	 * The remove action will be performed before the add action. Hence, if an objectID
	 * is in both of them, it will be added in total.
	 *
	 * @param removeObjectIDs Either <tt>null</tt> or the object-ids of those JDO objects for which to remove listeners
	 * @param addObjectIDs Either <tt>null</tt> or the object-ids of those JDO objects for which to add listeners
	 */
	void removeOrAddListeners(Collection<Object> removeObjectIDs,
			Collection<Object> addObjectIDs, Collection<Long> removeFilterIDs,
			Collection<IJDOLifecycleListenerFilter> addFilters);

	/**
	 * This method diffs the listeners of your cache session with the ones that
	 * should be there (specified by <tt>subscribedObjectIDs</tt>). Then it adds
	 * the missing and removes the ones that shouldn't be there.
	 *
	 * @param cacheSessionID The ID of your session.
	 * @param removeObjectIDs Either <tt>null</tt> or the object-ids of those JDO objects for which to remove listeners
	 * @param addObjectIDs Either <tt>null</tt> or the object-ids of those JDO objects for which to add listeners
	 *
	 * @see JDOManager#removeAddChangeListeners(java.lang.String, java.util.Collection, java.util.Collection)
	 */
	void resubscribeAllListeners(Set<Object> subscribedObjectIDs,
			Collection<IJDOLifecycleListenerFilter> filters);

	/**
	 * This method removes all listeners that have been registered for
	 * the current cache session. The method <tt>waitForChanges(...)</tt>
	 * will be released (if it's currently waiting).
	 */
	void closeCacheSession();

	/**
	 * This method blocks and returns not before a certain timeout occured
	 * or when <tt>closeCacheSession(...)</tt> has been called or - the main
	 * reason - at least one persistence-capable object has been changed.
	 * Because this method tries to collect multiple (by returning only at
	 * predefined time spots and by reacting only at the end of a transaction),
	 * it might return many object ids.
	 *
	 * @param cacheSessionID The ID of your cache session.
	 * @param waitTimeout The time in milliseconds defining how long this
	 *		method shall wait for changes, before it returns <tt>null</tt>.
	 *
	 * @return Returns either <tt>null</tt> if nothing changed or a {@link NotificationBundle}
	 *		of object ids. Hence {@link NotificationBundle#isEmpty()} will never return <code>true</code>
	 *		(<code>null</code> would have been returned instead of a <code>NotificationBundle</code>).
	 *
	 * @see CacheManager#waitForChanges(long)
	 */
	NotificationBundle waitForChanges(long waitTimeout);
}