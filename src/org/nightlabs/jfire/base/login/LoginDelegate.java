package org.nightlabs.jfire.base.login;

import javax.security.auth.login.LoginException;

import org.nightlabs.base.ui.login.ILoginDelegate;
import org.nightlabs.base.ui.login.LoginState;

public class LoginDelegate implements ILoginDelegate {

	public LoginState getLoginState()
	{
		switch (Login.sharedInstance().getLoginState()) {
			case Login.LOGINSTATE_LOGGED_IN:
				return LoginState.LOGGED_IN;
			case Login.LOGINSTATE_LOGGED_OUT:
				return LoginState.LOGGED_OUT;
			case Login.LOGINSTATE_OFFLINE:
				return LoginState.OFFLINE;
			default:
				throw new IllegalStateException("Unknown login state: " + Login.sharedInstance().getLoginState()); //$NON-NLS-1$
		}
	}

	public void login()
	throws LoginException
	{
		Login.getLogin();
	}

	public void logout() {
		Login.sharedInstance().logout();
	}

}
