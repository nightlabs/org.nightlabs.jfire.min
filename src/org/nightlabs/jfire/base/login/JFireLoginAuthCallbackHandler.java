package org.nightlabs.jfire.base.login;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * CallBackHandler for {@link JFireLogin}.
 *  
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class JFireLoginAuthCallbackHandler implements CallbackHandler
{
	private String organisationID;
	private String userID;
	private String sessionID;
	private String userName;
	private char[] password;

	public JFireLoginAuthCallbackHandler(String organisationID, String userID, String sessionID, char[] password)
	{
		this.organisationID = organisationID;
		this.userID = userID;
		this.sessionID = sessionID;
		this.password = password;
		this.userName = this.userID + '@' + this.organisationID + '/' + this.sessionID;
	}

	public void handle(Callback[] callbacks) throws IOException,
			UnsupportedCallbackException
	{
		for (int i = 0; i < callbacks.length; ++i) {
			Callback cb = callbacks[i];
			if (cb instanceof NameCallback) {
				((NameCallback)cb).setName(userName);
			}
			else if (cb instanceof PasswordCallback) {
				((PasswordCallback)cb).setPassword(password);
			}
			else throw new UnsupportedCallbackException(cb);
		}
	}

}
