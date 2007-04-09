package org.nightlabs.jfire.base.editlock;

import java.util.Date;

import org.nightlabs.jfire.editlock.EditLock;

public class EditLockCarrier
{
	private EditLock editLock;

	private Date lastUserActivityDT;

	public EditLockCarrier(EditLock editLock)
	{
		this(editLock, null);
	}
	
	public EditLockCarrier(EditLock editLock, EditLockCarrier oldEditLockCarrier)
	{
		this.editLock = editLock;

		if (oldEditLockCarrier == null) {
			this.lastUserActivityDT = new Date();
		}
		else {
			this.lastUserActivityDT = oldEditLockCarrier.lastUserActivityDT;
			this.editLockCallback = oldEditLockCarrier.editLockCallback;
		}
	}

	public EditLock getEditLock()
	{
		return editLock;
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

	private EditLockCallback editLockCallback;

	public EditLockCallback getEditLockCallbackListener()
	{
		return editLockCallback;
	}
	public void setEditLockCallbackListener(
			EditLockCallback editLockCallback)
	{
		this.editLockCallback = editLockCallback;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!(obj instanceof EditLockCarrier)) return false;
		EditLockCarrier o = (EditLockCarrier) obj;
		return this.editLock.equals(o.editLock);
	}

	@Override
	public int hashCode()
	{
		return editLock.hashCode();
	}
}
