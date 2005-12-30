/*
 * Created 	on Oct 4, 2004
 */
package org.nightlabs.ipanema.test.util;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;


/**
 * @author Alexander Bieber <alex@nightlabs.de>
 * @author Niklas Schiffler <nick@nightlabs.de>
 */
public class LoginCallbackHandler implements CallbackHandler {

	private JFireTestCase testCase;
	
	public LoginCallbackHandler(JFireTestCase testCase)
	{
		this.testCase = testCase;
	}
	
	/* (non-Javadoc)
	 * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
	 */
	public void handle(Callback[] callbacks) 
		throws IOException
	{
		for (int i = 0; i < callbacks.length; i++) 
		{
			if (callbacks[i] instanceof NameCallback) 
				((NameCallback)callbacks[i]).setName(testCase.getLoginContext().getUsername());			
			if (callbacks[i] instanceof PasswordCallback) 
				((PasswordCallback)callbacks[i]).setPassword(testCase.getLoginContext().getPassword().toCharArray());			
		}
	}

}
