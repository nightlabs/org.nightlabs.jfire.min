package org.nightlabs.jfire.base.jdo.notification.queue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleAdapterCallerThread;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleEvent;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleListener;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.notification.NotificationAdapterCallerThread;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;

public abstract class QueuedJDOLifecycleListener
{
	/**
	 * This is used for an explicit listener which is registered for new-notifications. Can be <code>null</code>.
	 */
	private IJDOLifecycleListenerFilter jdoLifecycleListenerFilter;

	/**
	 * This is used for a class-based implicit listener which is registered for dirty/deleted-notifications. Can be <code>null</code>.
	 */
	private Set<Class<?>> notificationSubjectClasses;

	public QueuedJDOLifecycleListener()
	{
	}
	public QueuedJDOLifecycleListener(IJDOLifecycleListenerFilter jdoLifecycleListenerFilter, Set<Class<?>> notificationSubjectClasses)
	{
		this.setJdoLifecycleListenerFilter(jdoLifecycleListenerFilter);
		this.setNotificationSubjectClasses(notificationSubjectClasses);
	}

	public synchronized IJDOLifecycleListenerFilter getJdoLifecycleListenerFilter()
	{
		assertOpen();
		return jdoLifecycleListenerFilter;
	}
	public synchronized void setJdoLifecycleListenerFilter(IJDOLifecycleListenerFilter jdoLifecycleListenerFilter)
	{
		assertOpen();
		JDOLifecycleManager m = getJDOLifecycleManager();
		if (this.jdoLifecycleListener != null)
			m.removeLifecycleListener(this.jdoLifecycleListener);

		this.jdoLifecycleListenerFilter = jdoLifecycleListenerFilter;
		this.jdoLifecycleListener = createJDOLifecycleListener();
		if (this.jdoLifecycleListener != null)
			m.addLifecycleListener(this.jdoLifecycleListener);
	}
	public synchronized Set<Class<?>> getNotificationSubjectClasses()
	{
		assertOpen();
		if (notificationSubjectClasses == null)
			return null;

		return Collections.unmodifiableSet(notificationSubjectClasses);
	}
	public synchronized void setNotificationSubjectClasses(Set<Class<?>> notificationSubjectClasses)
	{
		assertOpen();
		JDOLifecycleManager m = getJDOLifecycleManager();
		if (this.notificationListener != null) {
			for (Class<?> subjectClass : this.notificationSubjectClasses)
				m.removeNotificationListener(null, subjectClass, this.notificationListener);
		}

		this.notificationSubjectClasses = notificationSubjectClasses;
		this.notificationListener = createNotificationListener();
		if (this.notificationListener != null) {
			for (Class<?> subjectClass : this.notificationSubjectClasses)
				m.addNotificationListener(null, subjectClass, this.notificationListener);
		}
	}

	protected JDOLifecycleManager getJDOLifecycleManager()
	{
		return JDOLifecycleManager.sharedInstance();
	}

	private ConcurrentLinkedQueue<Object> events = new ConcurrentLinkedQueue<Object>();

	protected ConcurrentLinkedQueue<Object> getEvents()
	{
		return events;
	}

	/**
	 * @return <code>null</code> if the queue is empty or a {@link List} of {@link DirtyObjectID}s extracted from
	 *		the events in the order in which the events came in.
	 */
	protected List<DirtyObjectID> pollDirtyObjectIDs()
	{
		List<DirtyObjectID> dirtyObjectIDs = null;

//		Map<Object, Boolean> objectID2exists = new HashMap<Object, Boolean>();

		Object event;
		do {
			event = getEvents().poll();
			if (event != null) {
				if (dirtyObjectIDs == null)
					dirtyObjectIDs = new ArrayList<DirtyObjectID>();

				Set<DirtyObjectID> eventDirtyObjectIDs;
				if (event instanceof JDOLifecycleEvent) {
					JDOLifecycleEvent e = (JDOLifecycleEvent) event;
					eventDirtyObjectIDs = e.getDirtyObjectIDs();
				}
				else if (event instanceof NotificationEvent) {
					NotificationEvent e = (NotificationEvent) event;
					eventDirtyObjectIDs = e.getSubjects();
				}
				else
					throw new IllegalStateException("Object in events queue is neither a JDOLifecycleEvent nor a NotificationEvent! It is an instance of " + event.getClass().getName() + ": " + event);

//				for (DirtyObjectID dirtyObjectID : eventDirtyObjectIDs) {
//					switch (dirtyObjectID.getLifecycleState()) {
//						case NEW:
//							objectID2exists.put(dirtyObjectID.getObjectID(), Boolean.TRUE);
//							break;
//						case DELETED:
//							objectID2exists.put(dirtyObjectID.getObjectID(), Boolean.FALSE);
//							break;
//						case DIRTY:
//							// nothing to do
//							break;
//						default:
//							throw new IllegalStateException("Unknown JDOLifecycleState: " + dirtyObjectID.getLifecycleState());
//					}
//				}
				// TODO consolidate (i.e. deduplicate + ignore new/dirty, if already deleted)

				dirtyObjectIDs.addAll(eventDirtyObjectIDs);
			}
		} while (event != null);

		return dirtyObjectIDs;
	}

	private NotificationListener notificationListener = null;

	private NotificationListener createNotificationListener()
	{
		if (this.notificationSubjectClasses == null)
			return null;

		return new NotificationAdapterCallerThread()
		{
			public void notify(org.nightlabs.notification.NotificationEvent notificationEvent) {
				getEvents().add(notificationEvent);
				wakeupProcessEvents();
			}
		};
	}

	private JDOLifecycleListener jdoLifecycleListener = null;

	private JDOLifecycleListener createJDOLifecycleListener()
	{
		if (this.jdoLifecycleListenerFilter == null)
			return null;

		return new JDOLifecycleAdapterCallerThread()
		{
			public IJDOLifecycleListenerFilter getJDOLifecycleListenerFilter()
			{
				return jdoLifecycleListenerFilter;
			}

			public void notify(JDOLifecycleEvent event)
			{
				getEvents().add(event);
				wakeupProcessEvents();
			}
		};
	}

	protected abstract void wakeupProcessEvents();

	protected abstract void notify(QueuedJDOLifecycleEvent event);

	private boolean open = true;
	protected void assertOpen()
	{
		if (!open)
			throw new IllegalStateException("This queue is not open: " + this);
	}

	public synchronized void close()
	{
		if (!open)
			return;

		// deregister the listeners
		setNotificationSubjectClasses(null);
		setJdoLifecycleListenerFilter(null);
		open = false;
	}
}
