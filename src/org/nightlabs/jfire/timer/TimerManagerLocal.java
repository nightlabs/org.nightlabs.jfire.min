package org.nightlabs.jfire.timer;

import javax.ejb.Local;

import org.nightlabs.jfire.timer.id.TaskID;

@Local
public interface TimerManagerLocal
{
//	String ping(String message);

	/**
	 * This method is called by {@link TimerAsyncInvoke.TimerInvocation#invoke()}.
	 */
	boolean setExecutingIfActiveExecIDMatches(TaskID taskID, String activeExecID)
			throws Exception;

	/**
	 * Because the method {@link JFireTimerBean#ejbTimeout(javax.ejb.Timer)} is called without authentication
	 * and thus accessing the datastore doesn't work properly, we use this method as
	 * a delegate.
	 */
	void ejbTimeoutDelegate(TimerParam timerParam) throws Exception;
}