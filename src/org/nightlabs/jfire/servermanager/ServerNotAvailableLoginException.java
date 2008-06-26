package org.nightlabs.jfire.servermanager;

import javax.security.auth.login.LoginException;

/**
 * @author marco schulze - marco at nightlabs dot de
 */
public class ServerNotAvailableLoginException extends LoginException
{
	private static final long serialVersionUID = 1L;

	public ServerNotAvailableLoginException() {
	}

	public ServerNotAvailableLoginException(String msg) {
		super(msg);
	}
}
