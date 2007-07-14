package org.nightlabs.jfire.base.jdo.notification.queue;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;

public abstract class QueuedJDOLifecycleListenerThreadImpl
extends QueuedJDOLifecycleListener
{
	private static final Logger logger = Logger.getLogger(QueuedJDOLifecycleListenerThreadImpl.class);

	public QueuedJDOLifecycleListenerThreadImpl()
	{
		init();
	}
	public QueuedJDOLifecycleListenerThreadImpl(
			IJDOLifecycleListenerFilter jdoLifecycleListenerFilter,
			Set<Class> notificationSubjectClasses)
	{
		super(jdoLifecycleListenerFilter, notificationSubjectClasses);
		init();
	}

	private void init()
	{
		queueThread.start();
	}

	private Thread queueThread = new Thread()
	{
		@Override
		public void run()
		{
			while (!isInterrupted() && !end) {
				try {
					List<DirtyObjectID> dirtyObjectIDs = pollDirtyObjectIDs();
					if (dirtyObjectIDs == null) {
						synchronized (this) {
							this.wait(30000);
						}
					}
					else {
						QueuedJDOLifecycleListenerThreadImpl.this.notify(new QueuedJDOLifecycleEvent(QueuedJDOLifecycleListenerThreadImpl.this, dirtyObjectIDs));
					}
				} catch (Throwable t) {
					logger.error("Exception in queueThread.run!", t);
				}
			}
		}

		private volatile boolean end = false;

		@Override
		public void interrupt()
		{
			end = true;
			super.interrupt();
		}
	};

	@Implement
	protected void wakeupProcessEvents()
	{
		synchronized (queueThread) {
			queueThread.notifyAll();
		}
	}

	@Override
	public synchronized void close()
	{
		queueThread.interrupt();
		super.close();
	}

}
