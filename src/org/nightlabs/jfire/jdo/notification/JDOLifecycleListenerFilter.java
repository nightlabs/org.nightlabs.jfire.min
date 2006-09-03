package org.nightlabs.jfire.jdo.notification;


public abstract class JDOLifecycleListenerFilter
		implements IJDOLifecycleListenerFilter
{
	private long filterID = -1;

	public long getFilterID()
	{
		return filterID;
	}
	public void setFilterID(long filterID)
	{
		this.filterID = filterID;
	}
	public boolean includeSubclasses()
	{
		return true;
	}
}
