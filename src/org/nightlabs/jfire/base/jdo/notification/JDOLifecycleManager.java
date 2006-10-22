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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.notification.NotificationListenerJob;
import org.nightlabs.base.notification.NotificationManager;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.jdo.cache.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.AbsoluteFilterID;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;

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
extends NotificationManager
{
	private static final Logger logger = Logger.getLogger(JDOLifecycleManager.class);
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

	public void notify(Long filterID, final JDOLifecycleEvent event)
	{
		final JDOLifecycleListener listener = getLifecycleListener(filterID);
		if (listener == null)
			logger.error("No listener found for filterID="+filterID);

		if (notifyRCP(filterID, event, listener))
			return;

		if (listener instanceof JDOLifecycleListenerCallerThread) {
			listener.notify(event);
		}
		else if (listener instanceof JDOLifecycleListenerWorkerThreadAsync) {
			Thread worker = new Thread() {
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

	private boolean notifyRCP(Long filterID, final JDOLifecycleEvent event, final JDOLifecycleListener listener)
	{
		if (listener instanceof JDOLifecycleListenerJob) {
			JDOLifecycleListenerJob l = (JDOLifecycleListenerJob) listener;

			Job job = l.getJob(event);
			if (job == null) {
				String jobName = l.getJobName();
				if (jobName == null)
					jobName = "Processing JDOLifecycle";

				job = new Job(jobName) {
					protected IStatus run(IProgressMonitor monitor)
					{
						((JDOLifecycleListenerJob)listener).setProgressMonitor(monitor);
						((JDOLifecycleListenerJob)listener).notify(event);

						return Status.OK_STATUS;
					}
				};
				job.setRule(l.getRule());
				job.setPriority(l.getPriority());
				job.setUser(l.isUser());
				job.setSystem(l.isSystem());
			}
			job.schedule(l.getDelay());
			return true;
		}
		else if (listener instanceof JDOLifecycleListenerSWTThreadAsync) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run()
				{
					listener.notify(event);
				}
			});
			return true;
		}
		else if (listener instanceof JDOLifecycleListenerSWTThreadSync) {
			Display.getDefault().syncExec(new Runnable() {
				public void run()
				{
					listener.notify(event);
				}
			});
			return true;
		}
		else
			return false;
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

		if (!(listener instanceof JDOLifecycleListenerAWTThreadAsync) &&
				!(listener instanceof JDOLifecycleListenerAWTThreadSync) &&
				!(listener instanceof JDOLifecycleListenerCallerThread) &&
				!(listener instanceof JDOLifecycleListenerJob) &&
				!(listener instanceof JDOLifecycleListenerSWTThreadAsync) &&
				!(listener instanceof JDOLifecycleListenerSWTThreadSync) &&
				!(listener instanceof JDOLifecycleListenerWorkerThreadAsync))
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
				new AbsoluteFilterID(Cache.sharedInstance().getSessionID(), nextFilterID()));

		// add the listener
		synchronized (lifecycleListeners) {
			lifecycleListeners.add(listener);
			filterID2LifecycleListener.put(jdoLifecycleListenerFilter.getFilterID().getFilterID(), listener);
			lifecycleListenerFilters = null;
		}

		Cache.sharedInstance().addLifecycleListenerFilter(jdoLifecycleListenerFilter, 0);
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
		}

		Cache.sharedInstance().removeLifecycleListenerFilter(jdoLifecycleListenerFilter, 0);
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
				for (JDOLifecycleListener listener : lifecycleListeners)
					res.add(listener.getJDOLifecycleListenerFilter());

				lifecycleListenerFilters = Collections.unmodifiableList(res);
			}
			return lifecycleListenerFilters;
		}
	}
}
