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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.notification.NotificationAdapterJob;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleAdapterJob;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleEvent;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleListener;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.jdo.cache.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.SimpleLifecycleListenerFilter;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;

/**
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
	protected abstract Collection<JDOObject> retrieveJDOObjects(Set<JDOObjectID> objectIDs, IProgressMonitor monitor);

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
	protected abstract Collection<JDOObject> retrieveJDOObjects(IProgressMonitor monitor);

	/**
	 * This method is always called on the UI thread.
	 */
	protected abstract void onJDOObjectsChanged();

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
				new DirtyObjectID.LifecycleStage[] { DirtyObjectID.LifecycleStage.NEW });
	}

	private Map<JDOObjectID, JDOObject> jdoObjectID2jdoObject = null;
	private Object jdoObjectID2jdoObjectMutex = new Object();
	private List<JDOObject> jdoObjects = null;
	private JDOLifecycleListener lifecycleListener = new JDOLifecycleAdapterJob("Loading New jdoObjects")
	{
		private IJDOLifecycleListenerFilter lifecycleListenerFilter = createJDOLifecycleListenerFilter();

		public IJDOLifecycleListenerFilter getJDOLifecycleListenerFilter()
		{
			return lifecycleListenerFilter;
		}

		public void notify(JDOLifecycleEvent event)
		{
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
					Collection<JDOObject> jdoObjects = retrieveJDOObjects(jdoObjectIDsToLoad, getProgressMonitor());
					for (JDOObject jdoObject : jdoObjects)
						jdoObjectID2jdoObject.put((JDOObjectID) JDOHelper.getObjectId(jdoObject), jdoObject);

					createJDOObjectList();
				}
			} // synchronized (jdoObjectID2jdoObjectMutex) {

			Display.getDefault().asyncExec(new Runnable() {
				public void run()
				{
					onJDOObjectsChanged();
				}
			});
		}
	};

	private NotificationListener notificationListener = new NotificationAdapterJob("Loading Changed jdoObjects")
	{
		public void notify(NotificationEvent notificationEvent)
		{
			synchronized (jdoObjectID2jdoObjectMutex) {
				if (jdoObjectID2jdoObject == null)
					return; // nothing loaded yet

				HashSet<JDOObjectID> jdoObjectIDsToLoad = new HashSet<JDOObjectID>();
				for (DirtyObjectID dirtyObjectID : (Set<? extends DirtyObjectID>)notificationEvent.getSubjects()) {
					JDOObjectID jdoObjectID = (JDOObjectID) dirtyObjectID.getObjectID();

					if (DirtyObjectID.LifecycleStage.DELETED.equals(dirtyObjectID.getLifecycleStage()))
						jdoObjectID2jdoObject.remove(jdoObjectID);
					else if (jdoObjectID2jdoObject.containsKey(jdoObjectID)) // only load it, if it's already here
						jdoObjectIDsToLoad.add(jdoObjectID);
				}

				if (!jdoObjectIDsToLoad.isEmpty()) {
					Collection<JDOObject> jdoObjects = retrieveJDOObjects(jdoObjectIDsToLoad, getProgressMonitor());
					for (JDOObject jdoObject : jdoObjects)
						jdoObjectID2jdoObject.put((JDOObjectID) JDOHelper.getObjectId(jdoObject), jdoObject);

					createJDOObjectList();
				}
			} // synchronized (jdoObjectID2jdoObjectMutex) {

			Display.getDefault().asyncExec(new Runnable()
			{
				public void run()
				{
					onJDOObjectsChanged();
				}
			});
		}
	};

	private boolean listenersExist = false;
	private boolean closed = false;

	protected void assertOpen()
	{
		if (closed)
			throw new IllegalStateException("This instance of ActiveTreeContentProvider is already closed: " + this);
	}

	/**
	 * You <b>must</b> call this method once you don't need this content provider anymore.
	 * It performs some clean-ups, e.g. unregistering all listeners. 
	 */
	public void close()
	{
		assertOpen();
		if (listenersExist) {
			if (logger.isDebugEnabled())
				logger.debug("close: unregistering listeners (" + getJDOObjectClass() + ')');

			JDOLifecycleManager.sharedInstance().removeLifecycleListener(lifecycleListener);
			JDOLifecycleManager.sharedInstance().removeNotificationListener(getJDOObjectClass(), notificationListener);
		}
		else {
			if (logger.isDebugEnabled())
				logger.debug("close: there are no listeners - will not unregister (" + getJDOObjectClass() + ')');
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
				logger.debug("getElements: registering listeners (" + getJDOObjectClass() + ')');

			listenersExist = true;
			JDOLifecycleManager.sharedInstance().addLifecycleListener(lifecycleListener);
			JDOLifecycleManager.sharedInstance().addNotificationListener(getJDOObjectClass(), notificationListener);
		}

		if (jdoObjects != null)
			return jdoObjects;

		Job job = new Job("Loading Data") {
			protected IStatus run(IProgressMonitor monitor)
			{
				Collection<JDOObject> jdoObjects = retrieveJDOObjects(monitor);

				synchronized (jdoObjectID2jdoObjectMutex) {
					if (jdoObjectID2jdoObject == null)
						jdoObjectID2jdoObject = new HashMap<JDOObjectID, JDOObject>();

					jdoObjectID2jdoObject.clear();
					for (JDOObject jdoObject : jdoObjects)
						jdoObjectID2jdoObject.put((JDOObjectID) JDOHelper.getObjectId(jdoObject), jdoObject);

					createJDOObjectList();
				} // synchronized (jdoObjectID2jdoObjectMutex) {

				return Status.OK_STATUS;
			}
		};
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event)
			{
				super.done(event);
				onJDOObjectsChanged();
			}
		});
		job.setPriority(Job.SHORT);
		job.schedule();

		return null;
	}

}
