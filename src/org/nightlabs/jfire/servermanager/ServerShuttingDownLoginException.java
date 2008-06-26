package org.nightlabs.jfire.servermanager;

/**
 * @author marco schulze - marco at nightlabs dot de
 */
public class ServerShuttingDownLoginException extends ServerNotAvailableLoginException
{
	private static final long serialVersionUID = 1L;

	public ServerShuttingDownLoginException() {
	}

	public ServerShuttingDownLoginException(String msg) {
		super(msg);
	}
}
