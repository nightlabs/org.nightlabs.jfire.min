package org.nightlabs.jfire.timer;

import java.io.Serializable;

public class TimerParam
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public TimerParam() { }
	public TimerParam(String organisationID)
	{
		this.organisationID = organisationID;
	}
	public String organisationID;
}