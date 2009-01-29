package org.nightlabs.jfire.base.jdo.notification;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.jdo.notification.AbsoluteFilterID;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.util.Util;

/**
 * Use the shared instance of this manager to get notified
 * about changes on JDO objects or similar objects where
 * you can't register a change listener directly.
 * <p>
 * This implementation of {@link NotificationManager} supports two
 * different kinds of listeners:
 * <ul>
 * <li>
 * class-based listeners that are registered in the server implicitely
 * </li>
 * <li>
 * filter-based listeners that are registered (and <b>must be unregistered!</b>)
 * in the server explicitely.
 * </li>
 * </ul>
 * Read this document for more details:
 * https://www.jfire.org/modules/phpwiki/index.php/ClientSide%20JDO%20Lifecycle%20Listeners
 * </p>
 * <p>
 * For many objects which are obtained from the server, there exist
 * multiple instances in the client. Often, these instances even are
 * replaced with every change (because a new copy is loaded from the server).
 * Hence, you can't use a property change support within these objects or
 * similar mechanisms.
 * <p>
 * <u><b>implicit class-based listeners</b></u>
 * <br/>
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
 * When registering a listener, you can define, how the listener should be called by
 * choosing an implementation of {@link NotificationListener}. In most cases, you'll
 * probably want to extend {@link NotificationListenerJob}.
 * </p>
 * <p>
 * <u><b>filter-based explicit listeners</b></u>
 * <br/>
 * This is similar to the implicit listeners, but you need to register <b>and unregister(!)</b>
 * the listeners explicitely. See {@link #addLifecycleListener(JDOLifecycleListener)} for more details.
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class JDOLifecycleManager
extends org.nightlabs.notification.NotificationManager
{
	public static final String PROPERTY_KEY_JDO_LIFECYCLE_MANAGER = JDOLifecycleManager.class.getName();

	private static final Logger logger = Logger.getLogger(JDOLifecycleManager.class);

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

	public void notify(Long filterID, final JDOLifecycleEvent event)
	{
		final JDOLifecycleListener listener = getLifecycleListener(filterID);
		if (listener == null) {
			logger.error("No listener found for filterID="+filterID);
			return;
		}
		listener.setActiveJDOLifecycleEvent(event);

		if (listener instanceof JDOLifecycleListenerCallerThread) {
			listener.notify(event);
		}
		else if (listener instanceof JDOLifecycleListenerWorkerThreadAsync) {
			Thread worker = new Thread() {
				@Override
				public void run() {
					listener.notify(event);
				}
			};
			worker.start();
		}
		else if (listener instanceof JDOLifecycleListenerAWTThreadSync) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						listener.notify(event);
					}
				});
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
		else if (listener instanceof JDOLifecycleListenerAWTThreadAsync) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					listener.notify(event);
				}
			});
		}
		else
			throw new IllegalArgumentException("listener does not implement one of the supported interfaces: " + listener);
	}

	/**
	 * If you extend the <tt>NotificationManager</tt> to support additional
	 * notification modes, you need to extend this method and add your listener types
	 * here.
	 * @return The collection of valid listener types for this NotificationManager
	 */
	protected Collection<Class<? extends JDOLifecycleListener>> getValidJDOListenerTypes() {
		Collection<Class<? extends JDOLifecycleListener>> result = new HashSet<Class<? extends JDOLifecycleListener>>();
		result.add(JDOLifecycleListenerAWTThreadAsync.class);
		result.add(JDOLifecycleListenerAWTThreadSync.class);
		result.add(JDOLifecycleListenerCallerThread.class);
		result.add(JDOLifecycleListenerWorkerThreadAsync.class);
		return result;
	}

	protected Class<? extends JDOLifecycleListener> checkJDOListenerType(JDOLifecycleListener listener) {
		for (Class<? extends JDOLifecycleListener> listenerClass : getValidJDOListenerTypes()) {
			if (listenerClass.isAssignableFrom(listener.getClass()))
				return listenerClass;
		}
		return null;
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

		Class<? extends JDOLifecycleListener> listenerMatch = checkJDOListenerType(listener);
		if (listenerMatch == null)
			throw new IllegalArgumentException("listener does not implement one of the supported interfaces: " + listener.getClass().getName());

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
				new AbsoluteFilterID(cache.getSessionID(), nextFilterID()));

		// add the listener
		synchronized (lifecycleListeners) {
			lifecycleListeners.add(listener);
			filterID2LifecycleListener.put(jdoLifecycleListenerFilter.getFilterID().getFilterID(), listener);
			lifecycleListenerFilters = null;
		}

		cache.addLifecycleListenerFilter(jdoLifecycleListenerFilter, 0);
	}

	public void removeLifecycleListener(JDOLifecycleListener listener)
	{
		IJDOLifecycleListenerFilter jdoLifecycleListenerFilter = listener.getJDOLifecycleListenerFilter();

		synchronized (lifecycleListeners) {
			if (!lifecycleListeners.remove(listener))
				return; // no need to do anything else, if it was not registered

			if (filterID2LifecycleListener.remove(jdoLifecycleListenerFilter.getFilterID().getFilterID()) != listener)
				throw new IllegalStateException("Two JDOLifecycleListeners used the same filter and we didn't recognize it before.");

			lifecycleListenerFilters = null;
//		}

			// IMHO it's necessary to call the cache.removeLife...Filter method inside this synchronized
			// block, because otherwise we might have listener-filters with filterID = null.
			// I hope that it does not cause dead locks (the cache locks on another mutex). I hope that
			// now this exception doesn't occur anymore:
//			20:43:24,173 ERROR [CacheManagerFactory] filter.getFilterID returned null!!! filter class: org.nightlabs.jfire.trade.notification.ArticleContainerLifecycleListenerFilter
//			java.lang.Exception: STACKTRACE
//      at org.nightlabs.jfire.jdo.cache.CacheManagerFactory.after_addLifecycleListenerFilters(CacheManagerFactory.java:700)
//      at org.nightlabs.jfire.jdo.cache.CacheManagerFactory.resubscribeAllListeners(CacheManagerFactory.java:574)
//      at org.nightlabs.jfire.jdo.cache.CacheManager.resubscribeAllListeners(CacheManager.java:119)
//      at org.nightlabs.jfire.jdo.JDOManagerBean.resubscribeAllListeners(JDOManagerBean.java:217)
			cache.removeLifecycleListenerFilter(jdoLifecycleListenerFilter, 0);
		}

		// clear the filterID in case the JDOLifecycleListener wants to re-register this filter
		// instance with changed filter properties. (Marius)
		// This is already done in cache.removeLifecycleListenerFilter(jdoLifecycleListenerFilter, 0); above. Commented it out. Marco.
//		jdoLifecycleListenerFilter.setFilterID(null);
		if (jdoLifecycleListenerFilter.getFilterID() != null)
			throw new IllegalStateException("jdoLifecycleListenerFilter.getFilterID() is not null!");
	}

	private Set<JDOLifecycleListener> lifecycleListeners = new HashSet<JDOLifecycleListener>();
	/**
	 * Accessing this object must be synchronized using {@link #lifecycleListeners} as mutex.
	 */
	private Map<Long, JDOLifecycleListener> filterID2LifecycleListener = new HashMap<Long, JDOLifecycleListener>();

	/**
	 * This is a cache used by {@link #getLifecycleListenerFilters()} and invalidated (i.e. set to <code>null</code>)
	 * by {@link #addLifecycleListener(JDOLifecycleListener)} and {@link #removeLifecycleListener(JDOLifecycleListener)}.
	 * Once it has been created, it is not modified anymore - thus it can be safely returned to the outside world
	 * (as a read-only list).
	 * <p>
	 * From 2008-10-30 on, the elements in this List are clones (created via {@link Util#cloneSerializable(Object, ClassLoader)}).
	 * </p>
	 */
	private List<IJDOLifecycleListenerFilter> lifecycleListenerFilters = null;

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

	public Collection<IJDOLifecycleListenerFilter> getLifecycleListenerFilters()
	{
		synchronized (lifecycleListeners) {
			if (lifecycleListenerFilters == null) {
				ArrayList<IJDOLifecycleListenerFilter> res = new ArrayList<IJDOLifecycleListenerFilter>(lifecycleListeners.size());
				for (JDOLifecycleListener listener : lifecycleListeners) {
					IJDOLifecycleListenerFilter lifecycleListenerFilter = listener.getJDOLifecycleListenerFilter();
					// We clone in order to avoid this exception:
//				20:43:24,173 ERROR [CacheManagerFactory] filter.getFilterID returned null!!! filter class: org.nightlabs.jfire.trade.notification.ArticleContainerLifecycleListenerFilter
//				java.lang.Exception: STACKTRACE
//	      at org.nightlabs.jfire.jdo.cache.CacheManagerFactory.after_addLifecycleListenerFilters(CacheManagerFactory.java:700)
//	      at org.nightlabs.jfire.jdo.cache.CacheManagerFactory.resubscribeAllListeners(CacheManagerFactory.java:574)
//	      at org.nightlabs.jfire.jdo.cache.CacheManager.resubscribeAllListeners(CacheManager.java:119)
//	      at org.nightlabs.jfire.jdo.JDOManagerBean.resubscribeAllListeners(JDOManagerBean.java:217)
					// I assume, that, without cloning, the Cache.CacheManagerThread.run method works with filters that were manipulated after they
					// were fetched via this method.
					lifecycleListenerFilter = Util.cloneSerializable(lifecycleListenerFilter, lifecycleListenerFilter.getClass().getClassLoader());
					res.add(lifecycleListenerFilter);
				}

				lifecycleListenerFilters = Collections.unmodifiableList(res);
			}
			return lifecycleListenerFilters;
		}
	}

	private static boolean serverMode = false;

	public static synchronized void setServerMode(boolean serverMode)
	{
		logger.info("setServerMode: serverMode="+serverMode);

		if (serverMode && _sharedInstance != null)
			throw new IllegalStateException("Cannot switch to serverMode after a client-mode-sharedInstance has been created!");

		if (!serverMode && serverModeSharedInstances != null)
			throw new IllegalStateException("Cannot switch to clientMode after a server-mode-sharedInstance has been created!");

		JDOLifecycleManager.serverMode = serverMode;
	}

	public static boolean isServerMode()
	{
		return serverMode;
	}

	private static Map<String, JDOLifecycleManager> serverModeSharedInstances = null;

	/**
	 * This is used, if we're not using JNDI, but a System property (i.e. in the client)
	 */
	private static JDOLifecycleManager _sharedInstance = null;


	public static JDOLifecycleManager sharedInstance()
	{
		synchronized (Cache.class) { // we synchronise both sharedInstance-methods (of JDOLifecycleManager and Cache) via the same mutex in order to prevent dead-locks
			if (serverMode) {
				if (serverModeSharedInstances == null)
					serverModeSharedInstances = new HashMap<String, JDOLifecycleManager>();

				String userName = getCurrentUserName();
				JDOLifecycleManager jdoLifecycleManager = serverModeSharedInstances.get(userName);
				if (jdoLifecycleManager == null) {
					logger.info("sharedInstance: creating new JDOLifecycleManager in serverMode");
					jdoLifecycleManager = createJDOLifecycleManager();
					serverModeSharedInstances.put(userName, jdoLifecycleManager);
					jdoLifecycleManager.cache = Cache.sharedInstance();
				}
				return jdoLifecycleManager;
			}
			else {
				if (_sharedInstance == null) {
					logger.info("sharedInstance: creating new JDOLifecycleManager in clientMode (non-serverMode)");
					_sharedInstance = createJDOLifecycleManager();
					_sharedInstance.cache = Cache.sharedInstance();
				}
				return _sharedInstance;
			}
		} // synchronized (Cache.class) {
	}

	private Cache cache;

	private static Class<?> jdoLifecycleManagerClass = null;

	private static JDOLifecycleManager createJDOLifecycleManager()
	{
		if (jdoLifecycleManagerClass == null) {
			String className = System.getProperty(PROPERTY_KEY_JDO_LIFECYCLE_MANAGER);
			if (className == null)
				throw new IllegalStateException("System property PROPERTY_KEY_JDO_LIFECYCLE_MANAGER (" + PROPERTY_KEY_JDO_LIFECYCLE_MANAGER + ") not set!");

			try {
				jdoLifecycleManagerClass = Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

		try {
			return (JDOLifecycleManager) jdoLifecycleManagerClass.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static String getCurrentUserName()
	{
		return SecurityReflector.getUserDescriptor().getCompleteUserID();
	}

}
