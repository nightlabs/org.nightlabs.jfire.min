package org.nightlabs.jfire.testsuite;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.shutdownafterstartup.ShutdownControlHandle;

public class JFireTestRunnerInvocation
extends Invocation
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(JFireTestRunnerInvocation.class);

	private ShutdownControlHandle shutdownControlHandle;

	private static String runningSessionID = Long.toString(System.currentTimeMillis(), 36);

	private String sessionID;

	/**
	 * Create an instance of this invocation with an optional {@link ShutdownControlHandle}.
	 * @param shutdownControlHandle a handle to shutdown the system after the invocation is run or <code>null</code>, if no shutdown should be triggered.
	 */
	public JFireTestRunnerInvocation(ShutdownControlHandle shutdownControlHandle) {
		this.shutdownControlHandle = shutdownControlHandle;
		this.sessionID = runningSessionID;
	}

	@Override
	public Serializable invoke() throws Exception {
		try {
			// It happens that we shut down the server before this invocation finished. Because
			// the invocation is automatically added at the next startup of the server, we don't want
			// old invocations to be executed. Hence we check whether it's still the same session.
			if (!runningSessionID.equals(sessionID))
				return null;

			try {
				JFireTestManagerLocal m = JFireTestManagerUtil.getLocalHome().create();
				m.runAllTestSuites();
			} finally {
				if (shutdownControlHandle != null) {
					JFireServerManager jfsm = getJFireServerManager();
					try {
						jfsm.shutdownAfterStartup_shutdown(shutdownControlHandle);
					} finally {
						jfsm.close();
					}
				}
			}
		} catch (Throwable t) {
			// if this fails, the async-invoke-framework tries it again and again => simply log and successfully return
			logger.error(t.getClass().getName() + ": " + t.getMessage(), t);
		}
		return null;
	}
}
