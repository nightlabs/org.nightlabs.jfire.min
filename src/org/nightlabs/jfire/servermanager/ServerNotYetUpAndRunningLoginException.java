package org.nightlabs.jfire.servermanager;

/**
 * @author marco schulze - marco at nightlabs dot de
 */
public class ServerNotYetUpAndRunningLoginException extends ServerNotAvailableLoginException
{
	private static final long serialVersionUID = 1L;

	public ServerNotYetUpAndRunningLoginException() {
	}

	public ServerNotYetUpAndRunningLoginException(String msg) {
		super(msg);
	}
}
