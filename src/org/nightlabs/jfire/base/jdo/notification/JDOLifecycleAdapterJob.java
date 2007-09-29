package org.nightlabs.jfire.base.jdo.notification;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.nightlabs.base.ui.progress.ProgressMonitorWrapper;
import org.nightlabs.progress.ProgressMonitor;

public abstract class JDOLifecycleAdapterJob
extends JDOLifecycleAdapter
implements JDOLifecycleListenerJob
{
	private String jobName = null;

	public JDOLifecycleAdapterJob() { }

	public JDOLifecycleAdapterJob(String jobName)
	{
		this.jobName = jobName;
	}

	public Job getJob(JDOLifecycleEvent event)
	{
		return null;
	}

	public String getJobName()
	{
		return jobName;
	}
	
	private IProgressMonitor progressMonitor;
	
	private ProgressMonitor progressMonitorWrapper;

	/**
	 * @see org.nightlabs.base.ui.notification.NotificationListenerJob#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	/**
	 * @see org.nightlabs.base.ui.notification.NotificationListenerJob#getProgressMonitor()
	 */
	public IProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	/**
	 * Returns a {@link ProgressMonitor} implementation wrapping around the
	 * {@link IProgressMonitor} set in {@link #setProgressMonitor(IProgressMonitor)}.
	 * 
	 * @return A {@link ProgressMonitor} implementation wrapping around the
	 * 		{@link IProgressMonitor} set in {@link #setProgressMonitor(IProgressMonitor)}.
	 */
	public ProgressMonitor getProgressMontitorWrapper() {
		if (progressMonitorWrapper == null) {
			if (progressMonitor == null)
				throw new IllegalStateException("getProgressMontitorWrapper must not be called before setProgressMonitor(IProgressMonitor)."); //$NON-NLS-1$
			progressMonitorWrapper = new ProgressMonitorWrapper(progressMonitor);
		}
		return progressMonitorWrapper;
	}
	
	/**
	 * @see org.nightlabs.base.ui.notification.NotificationListenerJob#getRule()
	 */
	public ISchedulingRule getRule()
	{
		return null;
	}

	/**
	 * The default implementation of this method returns {@link Job#SHORT}.
	 *
	 * @see org.nightlabs.base.ui.notification.NotificationListenerJob#getPriority()
	 */
	public int getPriority()
	{
		return Job.SHORT;
	}

	/**
	 * The default implementation of this method returns 0.
	 *
	 * @see org.nightlabs.base.ui.notification.NotificationListenerJob#getDelay()
	 */
	public long getDelay()
	{
		return 0;
	}

	/**
	 * The default implementation of this method returns false.
	 *
	 * @see org.nightlabs.base.ui.notification.NotificationListenerJob#isUser()
	 */
	public boolean isUser()
	{
		return false;
	}

	/**
	 * The default implementation of this method returns false.
	 *
	 * @see org.nightlabs.base.ui.notification.NotificationListenerJob#isSystem()
	 */
	public boolean isSystem()
	{
		return false;
	}
}
