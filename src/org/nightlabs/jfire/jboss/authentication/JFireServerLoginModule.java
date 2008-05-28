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

package org.nightlabs.jfire.jboss.authentication;

import java.security.Principal;
import java.security.acl.Group;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.jboss.security.SimpleGroup;
import org.jboss.security.auth.spi.AbstractServerLoginModule;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.servermanager.JFireServerManager;


/**
 * @author nick@nightlabs.de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class JFireServerLoginModule extends AbstractServerLoginModule
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(JFireServerLoginModule.class);

	protected Lookup lookup;
	protected JFirePrincipal ip = null;
//	protected Object loginCredential = null; // why did we store this? I'll comment it out and see if it still works.

	private String identityHashStr = null;
	protected String getIdentityHashStr()
	{
		if (identityHashStr == null)
			identityHashStr = Integer.toString(System.identityHashCode(this), Character.MAX_RADIX);

		return identityHashStr;
	}

	public JFireServerLoginModule()
	{
		if (logger.isTraceEnabled())
			logger.trace("(" + getIdentityHashStr() + ") default constructor");
	}

	@Override
	public boolean login() throws LoginException
	{
		super.loginOk = false;
		NameCallback nc = new NameCallback("username: ");
		PasswordCallback pc = new PasswordCallback("password: ", false);

		Callback[] callbacks = {nc, pc};

		LoginData loginData;
		String login;
		String password;

		// get the login information via the callbacks
		try
		{
			callbackHandler.handle(callbacks);
			login = ((NameCallback)callbacks[0]).getName();
			char[] tmpPassword = ((PasswordCallback)callbacks[1]).getPassword();
//			if (tmpPassword == null)
//				throw new IllegalStateException("No password set! username = " + login);

			if (tmpPassword == null)
				password = ""; // an empty password does never pass UserLocal.checkPassword(...) [it's too short], so we can continue with it
			else
				password = new String(tmpPassword);

			((PasswordCallback)callbacks[1]).clearPassword();
//			loginCredential = password;
		} catch (Exception x) {
			logger.fatal("Callback handling failed!", x);
			throw new LoginException(x.getMessage());
		}

		if (logger.isTraceEnabled())
			logger.trace("(" + getIdentityHashStr() + ") login: " + login, new Exception("StackTrace"));
		else if (logger.isDebugEnabled())
			logger.debug("(" + getIdentityHashStr() + ") login: " + login);

		loginData = new LoginData(login, password);

		try
		{
			// create lookup object for user's organisationID
			this.lookup = new Lookup(loginData.getOrganisationID());

			// and delegate the login to the jfireServerManager
			JFireServerManager jfireServerManager = lookup.getJFireServerManager();
			try {
				this.ip = jfireServerManager.login(loginData);
				super.subject.getPrincipals().add(ip);
				super.loginOk = true;
				return true;
			} finally {
				jfireServerManager.close();
			}
		} catch (LoginException e) {
			throw e;
		} catch(Throwable e) {
			logger.fatal("Login failed!", e);
			throw new LoginException(e.getMessage());
		}
	}
	
	@Override
	public boolean commit() throws LoginException
	{
		if (!super.commit()) {
			logger.error("(" + getIdentityHashStr() + ") org.jboss.security.auth.spi.AbstractServerLoginModule.commit() returned false!");
			return false;
		}

//	 Set the login principal and credential and subject
//    SecurityAssociation.setPrincipal(ip);
//    SecurityAssociation.setCredential(loginCredential);
//    SecurityAssociation.setSubject(subject);
		//shouldn't the above stuff be done by JBoss? Why do we do it here? I think we don't need this anymore since we now use the ClientLoginModule additionally (see login-config.xml)

    if (logger.isTraceEnabled())
			logger.trace("(" + getIdentityHashStr() + ") commit: " + ip, new Exception("StackTrace"));

//    // Add the login principal to the subject if is not there
//    Set principals = subject.getPrincipals();
//    if (principals.contains(principal) == false)
//       principals.add(principal);
    return true;
	}

	@Override
	public boolean logout() throws LoginException
	{
// Well, the real client login module does SecurityAssociation.clear(), but
// unfortunately, async method call doesn't work this way. So please don't
// clear it.
// I guess, that the JBoss does internally manage its SecurityAsscociation itself
// and we should not use this client action in the server.
//		SecurityAssociation.clear();
		return super.logout();
	}

	@Override
	protected Principal getIdentity()
	{
		if (logger.isTraceEnabled()) {
			logger.trace("(" + getIdentityHashStr() + ") *********************************************************");
			logger.trace("(" + getIdentityHashStr() + ") getIdentity() returning JFirePrincipal: "+ip);
		}
		return ip;
	}

	@Override
	protected Group[] getRoleSets()
		throws LoginException
	{
		if (logger.isTraceEnabled()) {
			logger.trace("(" + getIdentityHashStr() + ") *********************************************************");
			logger.trace("(" + getIdentityHashStr() + ") getRoleSets()");
		}

		if (ip == null)
			throw new NullPointerException("Why the hell is getRoleSets() called before login?!");

		Group callerPrincipal = new SimpleGroup("CallerPrincipal");
		callerPrincipal.addMember(ip);
		return new Group[]{ip.getRoleSet(), callerPrincipal};
	}

}
