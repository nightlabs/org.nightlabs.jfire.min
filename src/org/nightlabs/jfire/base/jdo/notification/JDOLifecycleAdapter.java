package org.nightlabs.jfire.base.jdo.notification;


public abstract class JDOLifecycleAdapter
implements JDOLifecycleListener
{
	private JDOLifecycleEvent activeJDOLifecycleEvent;

	public JDOLifecycleEvent getActiveJDOLifecycleEvent()
	{
		return activeJDOLifecycleEvent;
	}

	public void setActiveJDOLifecycleEvent(JDOLifecycleEvent jdoLifecycleEvent)
	{
		this.activeJDOLifecycleEvent = jdoLifecycleEvent;
	}
}
