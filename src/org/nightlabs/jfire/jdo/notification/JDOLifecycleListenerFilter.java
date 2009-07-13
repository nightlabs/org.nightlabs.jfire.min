package org.nightlabs.jfire.jdo.notification;


public abstract class JDOLifecycleListenerFilter
		implements IJDOLifecycleListenerFilter
{
	private static final long serialVersionUID = 1L;
	private AbsoluteFilterID filterID = null;

	public AbsoluteFilterID getFilterID()
	{
		return filterID;
	}
	public void setFilterID(AbsoluteFilterID filterID)
	{
		this.filterID = filterID;
	}
	public boolean includeSubclasses()
	{
		return true;
	}
}
