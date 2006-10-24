package org.nightlabs.jfire.base.tree;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.notification.NotificationAdapterJob;
import org.nightlabs.base.tree.TreeContentProvider;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleAdapterJob;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleEvent;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleListener;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.jdo.cache.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.SimpleLifecycleListenerFilter;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;
import org.nightlabs.util.CollectionUtil;

public abstract class ActiveTreeContentProvider<JDOObjectID, JDOObject>
extends TreeContentProvider
{
	/**
	 * This method is called on a worker thread and must retrieve JDO objects for
	 * the given object-ids from the server.
	 *
	 * @param monitor
	 * @return
	 */
	protected abstract Collection<JDOObject> getJDOObjects(Set<JDOObjectID> objectIDs, IProgressMonitor monitor);

	protected abstract Collection<JDOObject> getAllJDOObjects(IProgressMonitor monitor);

	/**
	 * This method is always called on the UI thread.
	 */
	protected abstract void fireChangeEvent();

	protected abstract void sortJDOObjects(JDOObject[] objects);

	/**
	 * Unfortunately, it is not possible to determine the class of a generic at runtime. Therefore,
	 * we cannot know with which types the generic ActiveTreeContentProvider has been created.
	 * I hope that Java will - in the future - improve the generics! Marco.
	 */
	protected abstract Class getJdoObjectClass();

	private Map<JDOObjectID, JDOObject> jdoObjectID2jdoObject = null;
	private Object jdoObjectID2jdoObjectMutex = new Object();
	private JDOObject[] jdoObjects = null;
	private JDOLifecycleListener lifecycleListener = new JDOLifecycleAdapterJob("Loading New jdoObjects")
	{
		private SimpleLifecycleListenerFilter lifecycleListenerFilter = new SimpleLifecycleListenerFilter(
				getJdoObjectClass(), true,
				new DirtyObjectID.LifecycleStage[] { DirtyObjectID.LifecycleStage.NEW });

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
					Collection<JDOObject> jdoObjects = getJDOObjects(jdoObjectIDsToLoad, getProgressMonitor());
					for (JDOObject jdoObject : jdoObjects)
						jdoObjectID2jdoObject.put((JDOObjectID) JDOHelper.getObjectId(jdoObject), jdoObject);

					createJdoObjectArray();
				}
			} // synchronized (jdoObjectID2jdoObjectMutex) {

			Display.getDefault().asyncExec(new Runnable() {
				public void run()
				{
					fireChangeEvent();
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
					Collection<JDOObject> jdoObjects = getJDOObjects(jdoObjectIDsToLoad, getProgressMonitor());
					for (JDOObject jdoObject : jdoObjects)
						jdoObjectID2jdoObject.put((JDOObjectID) JDOHelper.getObjectId(jdoObject), jdoObject);

					createJdoObjectArray();
				}
			} // synchronized (jdoObjectID2jdoObjectMutex) {

			Display.getDefault().asyncExec(new Runnable()
			{
				public void run()
				{
					fireChangeEvent();
				}
			});
		}
	};

	public ActiveTreeContentProvider()
	{
		JDOLifecycleManager.sharedInstance().addLifecycleListener(lifecycleListener);
		JDOLifecycleManager.sharedInstance().addNotificationListener(getJdoObjectClass(), notificationListener);
	}
	/**
	 * You <b>must</b> call this method once you don't need this content provider anymore.
	 * It performs some clean-ups, e.g. unregistering all listeners. 
	 */
	public void close()
	{
		JDOLifecycleManager.sharedInstance().removeLifecycleListener(lifecycleListener);
		JDOLifecycleManager.sharedInstance().removeNotificationListener(getJdoObjectClass(), notificationListener);
	}

	private void createJdoObjectArray()
	{
		jdoObjects = (JDOObject[]) CollectionUtil.collection2TypedArray(jdoObjectID2jdoObject.values(), getJdoObjectClass());
//		jdoObjects = (JDOObject[]) jdoObjectID2jdoObject.values().toArray();
		sortJDOObjects(jdoObjects);
	}

	public Object[] getElements(Object inputElement)
	{
		if (jdoObjects != null)
			return jdoObjects;

		Job job = new Job("Loading Data") {
			protected IStatus run(IProgressMonitor monitor)
			{
				Collection<JDOObject> jdoObjects = getAllJDOObjects(monitor);

				synchronized (jdoObjectID2jdoObjectMutex) {
					if (jdoObjectID2jdoObject == null)
						jdoObjectID2jdoObject = new HashMap<JDOObjectID, JDOObject>();

					jdoObjectID2jdoObject.clear();
					for (JDOObject jdoObject : jdoObjects)
						jdoObjectID2jdoObject.put((JDOObjectID) JDOHelper.getObjectId(jdoObject), jdoObject);

					createJdoObjectArray();
				} // synchronized (jdoObjectID2jdoObjectMutex) {

				return Status.OK_STATUS;
			}
		};
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event)
			{
				super.done(event);
				fireChangeEvent();
			}
		});
		job.setPriority(Job.SHORT);
		job.schedule();

		return new String[] { "Loading data..." };
	}

}
