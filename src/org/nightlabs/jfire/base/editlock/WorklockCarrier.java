package org.nightlabs.jfire.base.editlock;

import java.util.Date;

import org.nightlabs.jfire.worklock.Worklock;

public class WorklockCarrier
{
	private Worklock worklock;

	private Date lastUserActivityDT;

	public WorklockCarrier(Worklock worklock)
	{
		this(worklock, null);
	}
	
	public WorklockCarrier(Worklock worklock, WorklockCarrier oldWorklockCarrier)
	{
		this.worklock = worklock;

		if (oldWorklockCarrier == null) {
			this.lastUserActivityDT = new Date();
		}
		else {
			this.lastUserActivityDT = oldWorklockCarrier.lastUserActivityDT;
			this.worklockCallback = oldWorklockCarrier.worklockCallback;
		}
	}

	public Worklock getWorklock()
	{
		return worklock;
	}
	public Date getLastUserActivityDT()
	{
		return lastUserActivityDT;
	}
	public void setLastUserActivityDT(Date lastUserActivityDT)
	{
		this.lastUserActivityDT = lastUserActivityDT;
	}
	public void setLastUserActivityDT()
	{
		setLastUserActivityDT(new Date());
	}

	private WorklockCallback worklockCallback;

	public WorklockCallback getWorklockCallbackListener()
	{
		return worklockCallback;
	}
	public void setWorklockCallbackListener(
			WorklockCallback worklockCallback)
	{
		this.worklockCallback = worklockCallback;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!(obj instanceof WorklockCarrier)) return false;
		WorklockCarrier o = (WorklockCarrier) obj;
		return this.worklock.equals(o.worklock);
	}

	@Override
	public int hashCode()
	{
		return worklock.hashCode();
	}
}
