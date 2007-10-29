package org.nightlabs.jfire.base.login;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.nightlabs.j2ee.LoginData;

/**
 * CallBackHandler for {@link JFireLogin}.
 *  
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * @author Marius Heinzmann -- Marius[at]NightLabs[dot]de
 */
public class JFireLoginAuthCallbackHandler implements CallbackHandler
{

	private LoginData loginData;
	
	public JFireLoginAuthCallbackHandler(LoginData loginData)
	{
		this.loginData = loginData;
//		String additionalQuery = "";
//		if ( loginData.getAdditionalParams() != null && ! loginData.getAdditionalParams().isEmpty() )
//			additionalQuery = loginData.getAdditionalParams().toURLQuery();
		
//		this.userName = loginData.getUsername() + '?' + additionalQuery ;
	}

	public void handle(Callback[] callbacks) throws IOException,
			UnsupportedCallbackException
	{
		for (int i = 0; i < callbacks.length; ++i) {
			Callback cb = callbacks[i];
			if (cb instanceof NameCallback) {
				((NameCallback)cb).setName(loginData.getLoginDataURL());
			}
			else if (cb instanceof PasswordCallback) {
				((PasswordCallback)cb).setPassword(loginData.getPassword().toCharArray());
			}
			else throw new UnsupportedCallbackException(cb);
		}
	}

}
