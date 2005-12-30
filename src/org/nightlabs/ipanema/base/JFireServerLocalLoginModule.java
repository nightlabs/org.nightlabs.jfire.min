/*
 * Created on 02.03.2004
 */
package org.nightlabs.ipanema.base;

import java.security.Principal;
import java.security.acl.Group;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.jboss.security.SimpleGroup;
import org.jboss.security.auth.spi.AbstractServerLoginModule;

/**
 * @author nick@nightlabs.de
 */
public class JFireServerLocalLoginModule extends AbstractServerLoginModule
{
	public static Logger LOGGER = Logger.getLogger(JFireServerLocalLoginModule.class);

	protected JFireServerLocalLoginManager.LocalPrincipal principal = null;
	protected Object loginCredential = null;

	public boolean login() throws LoginException
	{
		super.loginOk = false;
		NameCallback nc = new NameCallback("username: ");
		PasswordCallback pc = new PasswordCallback("password: ", false);

		Callback[] callbacks = {nc, pc};

		String username;
		String password;

		InitialContext initialContext;
		try {
			initialContext = new InitialContext();
		} catch (NamingException e) {
			LoginException x = new LoginException("Creating InitialContext failed!");
			x.initCause(e);
			throw x;
		}

		// get the login information via the callbacks
		try 
		{
			callbackHandler.handle(callbacks);
			username = ((NameCallback)callbacks[0]).getName();
			char[] tmpPassword = ((PasswordCallback)callbacks[1]).getPassword();
			password = new String(tmpPassword);
			((PasswordCallback)callbacks[1]).clearPassword();
			loginCredential = password;
		} catch (Exception x) {
			LOGGER.fatal("Callback handling failed!", x);
			throw new LoginException(x.getMessage());
		}

		try {
			principal = (JFireServerLocalLoginManager.LocalPrincipal) JFireServerLocalLoginManager
					.getJFireServerLocalLoginManager(initialContext).getPrincipal(username);
		} catch (NamingException e) {
			LoginException x = new LoginException("Unable to find JFireServerLocalLoginManager in JNDI!");
			x.initCause(e);
			throw x;
		}

		if (!principal.getPassword().equals(password))
			principal = null;

		if (principal == null) {
			LOGGER.error("Login by " + username + " failed: Unknown user or invalid password!");
			throw new LoginException("Unknown user or invalid password!");
		}

		LOGGER.info("Login by " + username + " successful!");

		super.subject.getPrincipals().add(principal);

		super.loginOk = true;
		return true;
	}

	protected Principal getIdentity()
	{
		LOGGER.debug("getIdentity() returning Principal: "+principal);
		return principal;
	}

	protected Group[] getRoleSets()
		throws LoginException
	{
		LOGGER.debug("getRoleSets()");

		if (principal == null)
			throw new NullPointerException("Why the hell is getRoleSets() called before login?!");

		Group callerPrincipal = new SimpleGroup("CallerPrincipal");
		callerPrincipal.addMember(principal);
		return new Group[]{principal.getRoleSet(), callerPrincipal};
	}

}
