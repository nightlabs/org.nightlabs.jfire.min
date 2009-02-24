/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.web.admin;

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * @author marco
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class SessionLogin
{
	private static final Logger log = Logger.getLogger(SessionLogin.class);
	
	private static final String SESSION_KEY = "login";

	public static SessionLogin getLogin(HttpSession session) throws NotAuthenticatedException
	{
		SessionLogin login = (SessionLogin)session.getAttribute(SESSION_KEY);
		if (login == null)
			throw new NotAuthenticatedException();
		return login;
	}

	public static SessionLogin login(HttpSession session, String _organisationID, String _userID, String _password) throws AuthenticationFailedException
	{
		logout(session);
		try {
			SessionLogin login = new SessionLogin(_organisationID, _userID, _password);
			session.setAttribute(SESSION_KEY, login);
			log.info("Login successful");
			return login;
		} catch(Throwable e) {
			log.info("Login failed", e);
			throw new AuthenticationFailedException("Login failed", e);
		}
	}

	public static void logout(HttpSession session)
	{
		session.setAttribute(SESSION_KEY, null);
	}
	
	public static boolean haveLogin(HttpSession session)
	{
		return session.getAttribute(SESSION_KEY) != null;
	}
	
	private String organisationID;
	private String userID;
	// username is the concatted product of userID and organisationID (userID@organisationID)
	private String username;
	private String password;
//	private LoginContext loginContext;

	SessionLogin(String _organisationID, String _userID, String _password)
	{
		this.organisationID = _organisationID;
		this.userID = _userID;
		this.password = _password;

		StringBuffer sbUser = new StringBuffer();
		sbUser.append(userID);
		sbUser.append('@');
		sbUser.append(organisationID);
		username = sbUser.toString();

//		if (SecurityAssociation.getSubject() != null) {
//			SecurityAssociation.setPrincipal(null);
//			SecurityAssociation.setCredential(null);
//			SecurityAssociation.setSubject(null);
//		}

		// The call to getInitialContext should already test the validity of
		// username and password.
		getInitialContext();


//		try {
//			char[] pwd = password.toCharArray();
//			AppCallbackHandler handler = new AppCallbackHandler(username, pwd);
//			SecurityAssociation.getPrincipal();
//			loginContext = new LoginContext("jfire", handler);
//			if (loginContext.getSubject() != null)
//				loginContext.logout();
//			loginContext.login();
//		} catch (LoginException x) {
//			throw new ModuleException(x);
//		}
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	/**
	 * @return Returns the userID.
	 */
	public String getUserID() {
		return userID;
	}
	/**
	 * @return Returns the password.
	 */
	public String getPassword() {
		return password;
	}
	// *** end of properties *************************

	protected transient Hashtable<?, ?> initialContextProperties = null;
	protected transient InitialContext initialContext = null;
//	protected transient LoginContext loginContext = null;

	private static String providerURL = null;

	public Hashtable<?, ?> getInitialContextProperties()
	{
//		System.out.println(this.getClass().getName()+"#getInitialContextProperties(): begin");

		try {
			if (initialContextProperties == null) {
				//System.out.println(this.getClass().getName()+"#getInitialContextProperties(): generating props");


// TODO This is not clean! The properties should come from a config file or we should ask the server core
//		for his address - he knows the local server and how to access it.
				if (providerURL == null)
					providerURL = (String) new InitialContext().getEnvironment().get(Context.PROVIDER_URL);

				if (providerURL == null)
					providerURL = "jnp://127.0.0.1:1099";
// end todo

				Properties props = new Properties();
				props.put(Context.INITIAL_CONTEXT_FACTORY,	"org.jboss.security.jndi.LoginInitialContextFactory");
				props.put(Context.PROVIDER_URL, providerURL); // "jnp://127.0.0.1:1099");
				props.put(Context.SECURITY_PRINCIPAL, username);
				props.put(Context.SECURITY_CREDENTIALS, password);
				props.put(Context.SECURITY_PROTOCOL, "jfire");

				initialContextProperties = props;
			}
			return initialContextProperties;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public InitialContext getInitialContext()
	{
//		System.out.println(this.getClass().getName()+"#getInitialContext(): begin");
		try {
			if (initialContext != null)
				return initialContext;

//			System.out.println(this.getClass().getName()+"#getInitialContext(): creating new initctx.");

			initialContext = new InitialContext(getInitialContextProperties());
			return initialContext;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}


//	public static class AppCallbackHandler implements CallbackHandler {
//		private String username;
//		private char[] password;
//
//		public AppCallbackHandler(String username, char[] password) {
//			this.username = username;
//			this.password = password;
//		}
//
//		public void handle(Callback[] callbacks)
//			throws IOException, UnsupportedCallbackException
//		{
//			NameCallback nc;
//			PasswordCallback pc;
//
//			for (int i = 0; i < callbacks.length; i++) {
//				if (callbacks[i] instanceof NameCallback) {
//					nc = (NameCallback) callbacks[i];
//					nc.setName(username);
//				} else if (callbacks[i] instanceof PasswordCallback) {
//					pc = (PasswordCallback) callbacks[i];
//					pc.setPassword(password);
//				} else if (callbacks[i] == null)
//					throw new NullPointerException("callbacks["+i+"] is null!");
//				else
//					throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback: "+callbacks[i].getClass().getName());
//			}
//		}
//
//	}

}
