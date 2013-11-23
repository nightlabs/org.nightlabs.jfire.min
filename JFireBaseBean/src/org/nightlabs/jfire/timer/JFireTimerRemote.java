package org.nightlabs.jfire.timer;

import javax.ejb.Remote;

@Remote
public interface JFireTimerRemote
{
	String ping(String message);

	void startTimer();
}