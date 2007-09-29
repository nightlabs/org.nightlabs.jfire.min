/**
 * 
 */
package org.nightlabs.jfire.base.jdo.notification;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.notification.NotificationListenerJob;
import org.nightlabs.base.ui.notification.NotificationListenerSWTThreadAsync;
import org.nightlabs.base.ui.notification.NotificationListenerSWTThreadSync;
import org.nightlabs.base.ui.notification.NotificationManagerInterceptorEPProcessor;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.notification.NotificationEvent;
import org.nightlabs.notification.NotificationListener;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class JDOLifecycleManagerRCP extends JDOLifecycleManager {

	private static Logger logger = Logger.getLogger(JDOLifecycleManagerRCP.class);
	/**
	 * 
	 */
	public JDOLifecycleManagerRCP() {
		new NotificationManagerInterceptorEPProcessor(this).process();
	}
	
	@Override
	protected Collection<Class<? extends NotificationListener>> getValidListenerTypes() {
		Collection<Class<? extends NotificationListener>> superResult = super.getValidListenerTypes();
		superResult.add(NotificationListenerJob.class);
		superResult.add(NotificationListenerSWTThreadAsync.class);
		superResult.add(NotificationListenerSWTThreadAsync.class);
		return superResult;
	}
	
	@Override
	protected Collection<Class<? extends JDOLifecycleListener>> getValidJDOListenerTypes() {
		Collection<Class<? extends JDOLifecycleListener>> superResult = super.getValidJDOListenerTypes();
		superResult.add(JDOLifecycleListenerJob.class);
		superResult.add(JDOLifecycleListenerSWTThreadSync.class);
		superResult.add(JDOLifecycleListenerSWTThreadAsync.class);
		return superResult;
	}

	/**
	 * @see org.nightlabs.notification.NotificationManager#performNotification(java.lang.String, org.nightlabs.notification.NotificationListener, org.nightlabs.notification.NotificationEvent)
	 */
	@Override
	protected void performNotification(String notificationMode, final NotificationListener listener,
			final NotificationEvent event)
	{
		listener.setActiveNotificationEvent(event);

		if (NotificationListenerJob.class.getName().equals(notificationMode)) {
			NotificationListenerJob l = (NotificationListenerJob) listener;

			Job job = l.createJob(event);
			if (job == null) {
				String jobName = l.getJobName();
				if (jobName == null)
					jobName = Messages.getString("org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManagerRCP.notificationJob"); //$NON-NLS-1$

				job = new Job(jobName) {
					protected IStatus run(IProgressMonitor monitor)
					{
						((NotificationListenerJob)listener).setProgressMonitor(monitor);
						((NotificationListenerJob)listener).notify(event);

						return Status.OK_STATUS;
					}
				};
				job.setRule(l.getRule());
				job.setPriority(l.getPriority());
				job.setUser(l.isUser());
				job.setSystem(l.isSystem());
			}
			job.schedule(l.getDelay());
		}
		else if (NotificationListenerSWTThreadAsync.class.getName().equals(notificationMode)) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run()
				{
					listener.notify(event);
				}
			});
		}
		else if (NotificationListenerSWTThreadSync.class.getName().equals(notificationMode)) {
			Display.getDefault().syncExec(new Runnable() {
				public void run()
				{
					listener.notify(event);
				}
			});
		}
		else
			super.performNotification(notificationMode, listener, event);
	}
	
	public void notify(Long filterID, final JDOLifecycleEvent event)
	{
		final JDOLifecycleListener listener = getLifecycleListener(filterID);
		if (listener == null) {
			logger.error("No listener found for filterID="+filterID); //$NON-NLS-1$
			return;
		}

		if (notifyRCP(filterID, event, listener))
			return;
	}	
	
	private boolean notifyRCP(Long filterID, final JDOLifecycleEvent event, final JDOLifecycleListener listener)
	{
		listener.setActiveJDOLifecycleEvent(event);

		if (listener instanceof JDOLifecycleListenerJob) {
			JDOLifecycleListenerJob l = (JDOLifecycleListenerJob) listener;

			Job job = l.getJob(event);
			if (job == null) {
				String jobName = l.getJobName();
				if (jobName == null)
					jobName = Messages.getString("org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManagerRCP.lifecycleJob"); //$NON-NLS-1$

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
	

	
	
}
