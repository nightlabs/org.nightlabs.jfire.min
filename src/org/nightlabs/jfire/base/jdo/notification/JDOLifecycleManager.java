package org.nightlabs.jfire.base.jdo.notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nightlabs.base.notification.NotificationManager;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.jdo.JDOManager;
import org.nightlabs.jfire.jdo.JDOManagerUtil;
import org.nightlabs.jfire.jdo.cache.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.AbsoluteFilterID;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.notification.NotificationEvent;

/**
 * Use the shared instance of this manager to get notified
 * about changes on JDO object or similar objects where
 * you can't register a change listener directly.
 * <p>
 * For many objects which are obtained from the server, there exist
 * multiple instances in the client. Often, these instances even are
 * replaced with every change (because a new copy is loaded from the server).
 * Hence, you can't use a property change support within these objects or
 * similar mechanisms.
 * <p>
 * To handle JDO object changes with this manager works as follows:
 * <ul>
 * 	<li>
 * 		Register a listener on the object-id-class of the JDO object that interests
 * 		you.
 * 	</li>
 * 	<li>
 *  	The JFire-Cache automatically triggers change notifications whenever objects change in
 *		the server (you don't need to take care about that). You only need to put all objects
 *		which you retrieve into the {@link Cache} for this feature to work.
 *	</li>
 *	<li>
 *		In your listener, you check, whether you currently display the object which is
 *		referenced by the given ID. If so, reload it from the server.
 *	</li>
 *	<li>
 *		Note that {@link NotificationEvent#getSubjects()} will always return instances of {@link DirtyObjectID}. Hence,
 *		you can check via {@link DirtyObjectID#getLifecycleType()} what exactly happened: The jdo object could have
 *		become dirty (changed on the server / attached) or deleted from the datastore.
 *	</li>
 * </ul>
 * When registering a listener, you can define, how the listener should be called
 * (<tt>notificationMode</tt>): This means on which thread and whether to wait or not.
 * In most cases, it makes sense to use a worker thread, because then you don't need to
 * manually handle the reload-process asnychronously (and you shouldn't do it on the GUI
 * thread, because it's expensive).
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class JDOLifecycleManager
		extends NotificationManager
{
	private static JDOLifecycleManager _sharedInstance = null;

	public static JDOLifecycleManager sharedInstance()
	{
		if (_sharedInstance == null)
			_sharedInstance = new JDOLifecycleManager();
		
		return _sharedInstance;
	}

	protected JDOLifecycleManager()
	{
	}

	private long nextFilterID = 0;
	private Object nextFilterIDMutex = new Object();

	protected long nextFilterID()
	{
		long res;
		synchronized (nextFilterIDMutex) {
			res = nextFilterID++;
			if (res > Long.MAX_VALUE - 100)
				throw new IllegalStateException("nextFilterID is getting out of range! restart your client!"); // this should really never happen, but I'm a perfectionist ;-) Marco.

			return res;
		}
	}

	public boolean containsLifecycleListener(JDOLifecycleListener listener)
	{
		synchronized (lifecycleListeners) {
			return lifecycleListeners.contains(listener);
		}
	}

	/**
	 * This method adds a {@link JDOLifecycleListener}. If it has already been added
	 * before, this method silently returns without any action.
	 *
	 * @param listener The listener that shall be registered.
	 */
	public void addLifecycleListener(JDOLifecycleListener listener)
	{
		// check param
		if (listener == null)
			throw new IllegalArgumentException("listener must not be null!");

		// is it already registered?
		synchronized (lifecycleListeners) {
			if (lifecycleListeners.contains(listener))
				return; // silently return
		}

		IJDOLifecycleListenerFilter jdoLifecycleListenerFilter = listener.getJDOLifecycleListenerFilter();
		if (jdoLifecycleListenerFilter == null)
			throw new IllegalArgumentException("listener.getJDOLifecycleListenerFilter() returned null!");

		// check, whether there's already an ID assigned
		if (jdoLifecycleListenerFilter.getFilterID() != null)
			throw new IllegalArgumentException(
					"The listener shares its IJDOLifecycleListenerFilter with another listener. That is not possible, sorry. " +
					"If you get this error without sharing, you probably mis-implemented the getFilterID() method, because " +
					"it returned a value instead of null. Read the " +
					"documentation or better extend the class JDOLifecycleListenerFilter instead of directly implementing the " +
					"interface.");

		// assign sessionID and a unique id
		jdoLifecycleListenerFilter.setFilterID(
				new AbsoluteFilterID(Cache.sharedInstance().getSessionID(), nextFilterID()));

		// add the listener
		synchronized (lifecycleListeners) {
			lifecycleListeners.add(listener);
			filterID2LifecycleListener.put(jdoLifecycleListenerFilter.getFilterID().getFilterID(), listener);
		}

		// TODO we need to put the filter into the Cache, which then registers it in the server. NOT directly do it here!
		try {
			Login.getLogin();
			JDOManager m = JDOManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
			ArrayList<IJDOLifecycleListenerFilter> filters = new ArrayList<IJDOLifecycleListenerFilter>(1);
			filters.add(jdoLifecycleListenerFilter);
			m.addLifecycleListenerFilters(filters);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public void removeLifecycleListener(JDOLifecycleListener listener)
	{
		IJDOLifecycleListenerFilter jdoLifecycleListenerFilter = listener.getJDOLifecycleListenerFilter();

		synchronized (lifecycleListeners) {
			if (!lifecycleListeners.remove(listener))
				return; // no need to do anything else, if it was not registered

			if (filterID2LifecycleListener.remove(jdoLifecycleListenerFilter.getFilterID().getFilterID()) != listener)
				throw new IllegalStateException("Two JDOLifecycleListeners used the same filter and we didn't recognize it before.");
		}

		// TODO we need to do this asynchronously via the cache!!!
		try {
			Login.getLogin();
			JDOManager m = JDOManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
			HashSet<Long> filterIDs = new HashSet<Long>(1);
			filterIDs.add(jdoLifecycleListenerFilter.getFilterID().getFilterID());
			m.removeLifecycleListenerFilters(filterIDs);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	private Set<JDOLifecycleListener> lifecycleListeners = new HashSet<JDOLifecycleListener>();
	/**
	 * Accessing this object must be synchronized using {@link #lifecycleListeners} as mutex.
	 */
	private Map<Long, JDOLifecycleListener> filterID2LifecycleListener = new HashMap<Long, JDOLifecycleListener>();

	/**
	 * @param filterID The ID of the filter.
	 * @return Returns <code>null</code>, if no matching listener can be found or that instance of {@link JDOLifecycleListener} that
	 *		matches the given filterID.
	 */
	public JDOLifecycleListener getLifecycleListener(Long filterID)
	{
		synchronized (lifecycleListeners) {
			return filterID2LifecycleListener.get(filterID);
		}
	}

}
