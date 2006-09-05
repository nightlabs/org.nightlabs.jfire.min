package org.nightlabs.jfire.jdo.notification;

import java.io.Serializable;

import org.nightlabs.util.Utils;

public class AbsoluteFilterID
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String sessionID;
	private long filterID;

	public AbsoluteFilterID(String sessionID, long filterID)
	{
		if (sessionID == null)
			throw new IllegalArgumentException("sessionID is null!");

		if (filterID < 0)
			throw new IllegalArgumentException("filterID < 0!!!");

		this.sessionID = sessionID;
		this.filterID = filterID;
	}

	public String getSessionID()
	{
		return sessionID;
	}
	public long getFilterID()
	{
		return filterID;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;

		if (!(obj instanceof AbsoluteFilterID))
			return false;

		AbsoluteFilterID o = (AbsoluteFilterID) obj;

		return this.sessionID.equals(o.sessionID) && this.filterID == o.filterID;
	}

	@Override
	public int hashCode()
	{
		return sessionID.hashCode() ^ Utils.hashCode(filterID);
	}
}
