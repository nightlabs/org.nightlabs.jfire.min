package org.nightlabs.jfire.base.jdo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.job.Job;
import org.nightlabs.base.notification.NotificationAdapterJob;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleAdapterJob;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleEvent;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleListener;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.SimpleLifecycleListenerFilter;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;
import org.nightlabs.notification.SubjectCarrier;
import org.nightlabs.progress.ProgressMonitor;

/**
 * A subclass should be instantiated to show data in an UI element ({@link org.eclipse.swt.widgets.List}, {@link org.eclipse.swt.widgets.Combo}, table and the like).
 * This controller will retrieve the data and update the UI element whenever this data changes or new instances are created (matching the filter criteria).
 * <p>
 * Note, that this class is not appropriate for tree structures. For managing trees, use the {@link org.nightlabs.jfire.base.jdo.tree.ActiveJDOObjectTreeController} instead.
 * </p>
 * <p>
 * More details about how to use this class can be found in our wiki:
 * <a href="https://www.jfire.org/modules/phpwiki/index.php/ActiveJDOObjectController">https://www.jfire.org/modules/phpwiki/index.php/ActiveJDOObjectController</a>
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public abstract class ActiveJDOObjectController<JDOObjectID, JDOObject>
{
	private static final Logger logger = Logger.getLogger(ActiveJDOObjectController.class);

	/**
	 * This method is called on a worker thread and must retrieve JDO objects for
	 * the given object-ids from the server.
	 *
	 * @param objectIDs The jdo object ids representing the desired objects.
	 * @param monitor The monitor.
	 * @return Returns the jdo objects that correspond to the requested <code>objectIDs</code>.
	 */
	protected abstract Collection<JDOObject> retrieveJDOObjects(Set<JDOObjectID> objectIDs, ProgressMonitor monitor);

	/**
	 * This method is called on a worker thread and must retrieve all JDO
	 * objects this controller shall manage. In many cases, this is simply
	 * the complete extent of a class (i.e. all instances that exist in the datastore).
	 * If this is not the complete extent of the class specified by
	 * {@link #getJDOObjectClass()}, you must override {@link #createJDOLifecycleListenerFilter()}
	 * in order to filter newly created objects already on the server side.
	 *
	 * @param monitor The monitor.
	 * @return Returns all those jdo objects that this 
	 */
	protected abstract Collection<JDOObject> retrieveJDOObjects(ProgressMonitor monitor);

	private ListenerList jdoObjectsChangedListeners = new ListenerList();

	/**
	 * This method is always called on the UI thread. You can chose whether you override it in order to react on changes
	 * or add a listener via {@link #addJDOObjectsChangedListener(JDOObjectsChangedListener)}.
	 *
	 * @param event The event containing details about which JDOObjects have been loaded from the server or have been deleted.
	 */
	protected void onJDOObjectsChanged(JDOObjectsChangedEvent<JDOObjectID, JDOObject> event)
	{
	}

	private void fireJDOObjectsChangedEvent(Collection<JDOObject> loadedJDOObjects, Map<JDOObjectID, JDOObject> ignoredJDOObjects, Map<JDOObjectID, JDOObject> deletedJDOObjects)
	{
		if (closed) {
			logger.warn("fireJDOObjectsChangedEvent: already closed: " + this); //$NON-NLS-1$
			return;
		}

		JDOObjectsChangedEvent<JDOObjectID, JDOObject> event = new JDOObjectsChangedEvent<JDOObjectID, JDOObject>(
				this, loadedJDOObjects, ignoredJDOObjects, deletedJDOObjects);

		onJDOObjectsChanged(event);
		if (!jdoObjectsChangedListeners.isEmpty()) {
			Object[] listeners = jdoObjectsChangedListeners.getListeners();
			for (Object listener : listeners) {
				JDOObjectsChangedListener<JDOObjectID, JDOObject> l = (JDOObjectsChangedListener<JDOObjectID, JDOObject>) listener;
				l.onJDOObjectsChanged(event);
			}
		}
	}

	public void addJDOObjectsChangedListener(JDOObjectsChangedListener<JDOObjectID, JDOObject> listener)
	{
		jdoObjectsChangedListeners.add(listener);
	}

	public void removeJDOObjectsChangedListener(JDOObjectsChangedListener<JDOObjectID, JDOObject> listener)
	{
		jdoObjectsChangedListeners.remove(listener);
	}

	protected abstract void sortJDOObjects(List<JDOObject> objects);

	/**
	 * Unfortunately, it is not possible to determine the class of a generic at runtime. Therefore,
	 * we cannot know with which types the generic ActiveTreeContentProvider has been created.
	 * I hope that Java will - in the future - improve the generics! Marco.
	 */
	protected abstract Class getJDOObjectClass();

	protected IJDOLifecycleListenerFilter createJDOLifecycleListenerFilter()
	{
		return new SimpleLifecycleListenerFilter(
				getJDOObjectClass(), true,
				new JDOLifecycleState[] { JDOLifecycleState.NEW });
	}

	private Map<JDOObjectID, JDOObject> jdoObjectID2jdoObject = null;
	private Object jdoObjectID2jdoObjectMutex = new Object();
	private List<JDOObject> jdoObjects = null;
	private JDOLifecycleListener lifecycleListener = new JDOLifecycleAdapterJob(Messages.getString("org.nightlabs.jfire.base.jdo.ActiveJDOObjectController.loadingNewObjectsJob")) //$NON-NLS-1$
	{
		private IJDOLifecycleListenerFilter lifecycleListenerFilter = createJDOLifecycleListenerFilter();

		public IJDOLifecycleListenerFilter getJDOLifecycleListenerFilter()
		{
			return lifecycleListenerFilter;
		}

		public void notify(final JDOLifecycleEvent event)
		{
			final Collection<JDOObject> loadedJDOObjects;
			final Set<JDOObjectID> ignoredJDOObjectIDs;
			final Map<JDOObjectID, JDOObject> ignoredJDOObjects;
			synchronized (jdoObjectID2jdoObjectMutex) {
				if (jdoObjectID2jdoObject == null)
					return; // nothing loaded yet

				HashSet<JDOObjectID> jdoObjectIDsToLoad = new HashSet<JDOObjectID>();
				for (DirtyObjectID dirtyObjectID : event.getDirtyObjectIDs()) {
					JDOObjectID jdoObjectID = (JDOObjectID) dirtyObjectID.getObjectID();
					// only load it, if it's really new - i.e. we don't have it yet!
					if (!jdoObjectID2jdoObject.containsKey(jdoObjectID))
						jdoObjectIDsToLoad.add(jdoObjectID);
				}

				if (!jdoObjectIDsToLoad.isEmpty()) {
					Collection<JDOObject> jdoObjects = retrieveJDOObjects(jdoObjectIDsToLoad, getProgressMontitorWrapper());
					loadedJDOObjects = jdoObjects;
					ignoredJDOObjectIDs = new HashSet<JDOObjectID>(jdoObjectIDsToLoad);
					if (jdoObjects != null) {
						for (JDOObject jdoObject : jdoObjects) {
							JDOObjectID jdoObjectID = (JDOObjectID) JDOHelper.getObjectId(jdoObject);
							ignoredJDOObjectIDs.remove(jdoObjectID);
							jdoObjectID2jdoObject.put(jdoObjectID, jdoObject);
						}						
					}
					if (ignoredJDOObjectIDs.isEmpty())
						ignoredJDOObjects = null;
					else {
						ignoredJDOObjects = new HashMap<JDOObjectID, JDOObject>(ignoredJDOObjectIDs.size());
						for (JDOObjectID jdoObjectID : ignoredJDOObjectIDs) {
							JDOObject jdoObject = jdoObjectID2jdoObject.remove(jdoObjectID);
							ignoredJDOObjects.put(jdoObjectID, jdoObject);
						}
					}

					createJDOObjectList();
				}
				else {
					loadedJDOObjects = null;
					ignoredJDOObjects = null;
					ignoredJDOObjectIDs = null;
				}
			} // synchronized (jdoObjectID2jdoObjectMutex) {

			Display.getDefault().asyncExec(new Runnable() {
				public void run()
				{
					if (loadedJDOObjects != null)
						fireJDOObjectsChangedEvent(loadedJDOObjects, ignoredJDOObjects, null);
				}
			});
		}
	};

	private NotificationListener notificationListener = new NotificationAdapterJob(Messages.getString("org.nightlabs.jfire.base.jdo.ActiveJDOObjectController.loadingChangedObjectsJob")) //$NON-NLS-1$
	{
		public void notify(final NotificationEvent notificationEvent)
		{
			final Collection<JDOObject> loadedJDOObjects;
			final Set<JDOObjectID> ignoredJDOObjectIDs;
			final Map<JDOObjectID, JDOObject> ignoredJDOObjects;
			final Map<JDOObjectID, JDOObject> deletedJDOObjects;

			synchronized (jdoObjectID2jdoObjectMutex) {
				if (jdoObjectID2jdoObject == null)
					return; // nothing loaded yet

				HashSet<JDOObjectID> jdoObjectIDsToLoad = new HashSet<JDOObjectID>();
				Map<JDOObjectID, JDOObject> _deletedJDOObjects = null;
				for (SubjectCarrier subjectCarrier : (List<? extends SubjectCarrier>)notificationEvent.getSubjectCarriers()) {
					DirtyObjectID dirtyObjectID = (DirtyObjectID) subjectCarrier.getSubject();
					JDOObjectID jdoObjectID = (JDOObjectID) dirtyObjectID.getObjectID();

					if (JDOLifecycleState.DELETED.equals(dirtyObjectID.getLifecycleState())) {
						JDOObject jdoObject = jdoObjectID2jdoObject.remove(jdoObjectID);
						if (_deletedJDOObjects == null)
							_deletedJDOObjects = new HashMap<JDOObjectID, JDOObject>();
						_deletedJDOObjects.put(jdoObjectID, jdoObject);
						jdoObjectIDsToLoad.remove(jdoObjectID);
					}
					else if (jdoObjectID2jdoObject.containsKey(jdoObjectID)) // only load it, if it's already here
						jdoObjectIDsToLoad.add(jdoObjectID);
				}

				if (!jdoObjectIDsToLoad.isEmpty()) {
					Collection<JDOObject> jdoObjects = retrieveJDOObjects(jdoObjectIDsToLoad, getProgressMonitorWrapper());
					ignoredJDOObjectIDs = new HashSet<JDOObjectID>(jdoObjectIDsToLoad);
					loadedJDOObjects = jdoObjects;
					for (JDOObject jdoObject : jdoObjects) {
						JDOObjectID jdoObjectID = (JDOObjectID) JDOHelper.getObjectId(jdoObject);
						ignoredJDOObjectIDs.remove(jdoObjectID);
						jdoObjectID2jdoObject.put(jdoObjectID, jdoObject);
					}
					if (ignoredJDOObjectIDs.isEmpty())
						ignoredJDOObjects = null;
					else {
						ignoredJDOObjects = new HashMap<JDOObjectID, JDOObject>(ignoredJDOObjectIDs.size());
						for (JDOObjectID jdoObjectID : ignoredJDOObjectIDs) {
							JDOObject jdoObject = jdoObjectID2jdoObject.remove(jdoObjectID);
							ignoredJDOObjects.put(jdoObjectID, jdoObject);
						}
					}

					createJDOObjectList();
				}
				else {
					ignoredJDOObjectIDs = null;
					ignoredJDOObjects = null;
					loadedJDOObjects = null;
				}

				deletedJDOObjects = _deletedJDOObjects;
			} // synchronized (jdoObjectID2jdoObjectMutex) {

			Display.getDefault().asyncExec(new Runnable()
			{
				public void run()
				{
					notificationEvent.getSubjectCarriers();
					fireJDOObjectsChangedEvent(loadedJDOObjects, ignoredJDOObjects, deletedJDOObjects);
				}
			});
		}
	};

	private boolean listenersExist = false;
	private boolean closed = false;

	protected void assertOpen()
	{
		if (closed)
			throw new IllegalStateException("This instance of ActiveJDOObjectController is already closed: " + this); //$NON-NLS-1$
	}

	/**
	 * You <b>must</b> call this method once you don't need this controller anymore.
	 * It performs some clean-ups, e.g. unregistering all listeners. 
	 */
	public void close()
	{
		assertOpen();
		if (listenersExist) {
			if (logger.isDebugEnabled())
				logger.debug("close: unregistering listeners (" + getJDOObjectClass() + ')'); //$NON-NLS-1$

			JDOLifecycleManager.sharedInstance().removeLifecycleListener(lifecycleListener);
			JDOLifecycleManager.sharedInstance().removeNotificationListener(getJDOObjectClass(), notificationListener);
		}
		else {
			if (logger.isDebugEnabled())
				logger.debug("close: there are no listeners - will not unregister (" + getJDOObjectClass() + ')'); //$NON-NLS-1$
		}
		closed = true;
	}

	protected void createJDOObjectList()
	{
		jdoObjects = new ArrayList<JDOObject>(jdoObjectID2jdoObject.values());
		sortJDOObjects(jdoObjects);
	}

	/**
	 * This method will immediately return. If there is no data available yet, this method will return <code>null</code>
	 * and a {@link Job} will be launched in order to fetch the data.
	 *
	 * @return <code>null</code>, if there is no data here yet. An instance of {@link List} containing
	 *		jdo objects. If a modification happened, this list will be recreated.
	 */
	public List<JDOObject> getJDOObjects()
	{
		assertOpen();
		if (!listenersExist) {
			if (logger.isDebugEnabled())
				logger.debug("getElements: registering listeners (" + getJDOObjectClass() + ')'); //$NON-NLS-1$

			listenersExist = true;
			JDOLifecycleManager.sharedInstance().addLifecycleListener(lifecycleListener);
			JDOLifecycleManager.sharedInstance().addNotificationListener(getJDOObjectClass(), notificationListener);
		}

		if (jdoObjects != null)
			return jdoObjects;

		Job job = new Job(Messages.getString("org.nightlabs.jfire.base.jdo.ActiveJDOObjectController.loadingDataJob")) { //$NON-NLS-1$
			protected IStatus run(ProgressMonitor monitor)
			{
				final Collection<JDOObject> jdoObjects = retrieveJDOObjects(monitor);

				synchronized (jdoObjectID2jdoObjectMutex) {
					if (jdoObjectID2jdoObject == null)
						jdoObjectID2jdoObject = new HashMap<JDOObjectID, JDOObject>();

					jdoObjectID2jdoObject.clear();
					for (JDOObject jdoObject : jdoObjects)
						jdoObjectID2jdoObject.put((JDOObjectID) JDOHelper.getObjectId(jdoObject), jdoObject);

					createJDOObjectList();
				} // synchronized (jdoObjectID2jdoObjectMutex) {

				Display.getDefault().asyncExec(new Runnable()
				{
					public void run()
					{
						fireJDOObjectsChangedEvent(jdoObjects, null, null);
					}
				});

				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.SHORT);
		job.schedule();

		return null;
	}

}
