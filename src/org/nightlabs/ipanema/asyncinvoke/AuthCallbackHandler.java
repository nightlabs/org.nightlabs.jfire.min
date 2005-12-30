/*
 * Created on Mar 24, 2005
 */
package org.nightlabs.ipanema.asyncinvoke;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.nightlabs.ipanema.servermanager.JFireServerManager;
import org.nightlabs.ipanema.servermanager.j2ee.SecurityReflector;

import org.nightlabs.jdo.ObjectIDUtil;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class AuthCallbackHandler implements CallbackHandler
{
	private String organisationID;
	private String userID;
	private String sessionID;
	private String userName;
	private char[] password;

	public AuthCallbackHandler(JFireServerManager ism, AsyncInvokeEnvelope envelope) {
		SecurityReflector.UserDescriptor caller = envelope.getCaller();
		this.organisationID = caller.getOrganisationID();
		this.userID = caller.getUserID();
		this.sessionID = ObjectIDUtil.makeValidIDString(null, true);
		this.userName = userID + '@' + organisationID + '/' + sessionID;
		this.password = ism.ipanemaSecurity_createTempUserPassword(
				organisationID, userID).toCharArray();
	}

	/**
	 * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
	 */
	public void handle(Callback[] callbacks)
	throws IOException,
			UnsupportedCallbackException
	{
//		if (callbacks.length != 2)
//			throw new IllegalArgumentException("callbacks.length != 2!");
//
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
