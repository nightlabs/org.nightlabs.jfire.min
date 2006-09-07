package org.nightlabs.jfire.base.jdo.notification;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

public abstract class JDOLifecycleAdapterJob
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

	/**
	 * @see org.nightlabs.base.notification.NotificationListenerJob#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	/**
	 * @see org.nightlabs.base.notification.NotificationListenerJob#getProgressMonitor()
	 */
	public IProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	/**
	 * @see org.nightlabs.base.notification.NotificationListenerJob#getRule()
	 */
	public ISchedulingRule getRule()
	{
		return null;
	}

	/**
	 * The default implementation of this method returns {@link Job#SHORT}.
	 *
	 * @see org.nightlabs.base.notification.NotificationListenerJob#getPriority()
	 */
	public int getPriority()
	{
		return Job.SHORT;
	}

	/**
	 * The default implementation of this method returns 0.
	 *
	 * @see org.nightlabs.base.notification.NotificationListenerJob#getDelay()
	 */
	public long getDelay()
	{
		return 0;
	}

	/**
	 * The default implementation of this method returns false.
	 *
	 * @see org.nightlabs.base.notification.NotificationListenerJob#isUser()
	 */
	public boolean isUser()
	{
		return false;
	}

	/**
	 * The default implementation of this method returns false.
	 *
	 * @see org.nightlabs.base.notification.NotificationListenerJob#isSystem()
	 */
	public boolean isSystem()
	{
		return false;
	}
}
