/*
 * Created 	on Oct 4, 2004
 * 					by Alexander Bieber
 *
 */
package org.nightlabs.jfire.base.login;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

/**
 * This is the CallbackHandler for the JAAS Login.
 * It does no user interaction it rather takes username and password
 * from the Login class.
 * @author Alexander Bieber
 */
public class LoginCallbackHandler implements CallbackHandler {

	/* (non-Javadoc)
	 * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
	 */
	public void handle(Callback[] callbacks) throws IOException,
			UnsupportedCallbackException {
		try {
			for (int i = 0; i < callbacks.length; i++) {
				// user interaction has been done in implementations of ILoginHandler
				// so here we just pass the values
				if (callbacks[i] instanceof NameCallback) 
							((NameCallback)callbacks[i]).setName(Login.getLogin(false).getLoginContext().getUsername());			
				if (callbacks[i] instanceof PasswordCallback) 
								((PasswordCallback)callbacks[i]).setPassword(Login.getLogin(false).getLoginContext().getPassword().toCharArray());			
			}
		} catch (LoginException x) {
			throw new RuntimeException("Login.getLogin(false) should never throw this exception!", x);
		}
			// TODO Maybe more data has to be set ??
	}

}
