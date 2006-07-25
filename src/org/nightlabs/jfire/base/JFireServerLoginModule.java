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

package org.nightlabs.jfire.base;

import java.security.Principal;
import java.security.acl.Group;
import java.util.regex.Pattern;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimpleGroup;
import org.jboss.security.auth.spi.AbstractServerLoginModule;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.servermanager.JFireServerManager;


/**
 * @author nick@nightlabs.de
 */
public class JFireServerLoginModule extends AbstractServerLoginModule
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(JFireServerLoginModule.class);

	protected Lookup lookup;
	protected JFirePrincipal ip = null;
	protected Object loginCredential = null;

//	protected static Pattern SPLIT_USERNAME_PATTERN = Pattern.compile("[*@*]");
	public static Pattern SPLIT_USERNAME_PATTERN = Pattern.compile("[@\\/]");

	public boolean login() throws LoginException
	{
//		LOGGER.info(Thread.currentThread().toString() + ": login()");

		super.loginOk = false;
		NameCallback nc = new NameCallback("username: ");
		PasswordCallback pc = new PasswordCallback("password: ", false);
		
		Callback[] callbacks = {nc, pc};
		
		String username;
		String password;
		String userID;
		String organisationID;
		String sessionID = null;
		boolean userIsOrganisation;

		// get the login information via the callbacks
		try 
		{
			callbackHandler.handle(callbacks);
			username = ((NameCallback)callbacks[0]).getName();
			char[] tmpPassword = ((PasswordCallback)callbacks[1]).getPassword();
			if (tmpPassword == null)
				throw new IllegalStateException("No password set! username = " + username);

			password = new String(tmpPassword);
			((PasswordCallback)callbacks[1]).clearPassword();
			loginCredential = password;
		} catch (Exception x) {
			logger.fatal("Callback handling failed!", x);
			throw new LoginException(x.getMessage());
		}

		logger.info(Thread.currentThread().toString() + ": Login requested by " + username);
//		Principal previousPrincipal = SecurityAssociation.getPrincipal();
//		if (previousPrincipal != null && username.equals(previousPrincipal.getName()))
//			previousPrincipal = null;
//
////		Principal runAsPrincipal = SecurityAssociation.peekRunAsRole();
//		if (previousPrincipal != null) {
////			SecurityAssociation.pushRunAsRole(new SimplePrincipal("test"));
//			LOGGER.info("Already logged in as "+previousPrincipal+"! Ignoring login as "+username+"!!!");
////			super.loginOk = true; // this caused a bad exception!
////			LOGGER.debug("SecurityAssociation.peekRunAsRole(): "+runAsPrincipal);
//			return true;
//		}
//
//
//		LOGGER.info("Not yet logged in. Trying to login "+username);

		// set username and organisationID + userIsOrganisation
		userIsOrganisation = false;
		String tmpStr = username;
		if(tmpStr.startsWith(User.USERID_PREFIX_TYPE_ORGANISATION))
		{
			userIsOrganisation = true;
			tmpStr = tmpStr.substring(User.USERID_PREFIX_TYPE_ORGANISATION.length());
		}
		String[] txt = SPLIT_USERNAME_PATTERN.split(tmpStr);
		if(txt.length != 2 && txt.length != 3)
			throw new LoginException("Invalid user string (use user@organisation/session, session is optional)");
		if(txt[0].length() == 0 || txt[1].length() == 0)
			throw new LoginException("Invalid user string (use user@organization/session, session is optional)");
		userID = userIsOrganisation?User.USERID_PREFIX_TYPE_ORGANISATION+txt[0]:txt[0];
		organisationID = txt[1];

		if (txt.length < 3 || "".equals(txt[2]))
			sessionID = txt[1] + '_' + txt[0];
		else
			sessionID = txt[2];

		try
		{
			// create lookup object for user's organisationID
			this.lookup = new Lookup(organisationID);

			// and delegate the login to the jfireServerManager
			JFireServerManager jfireServerManager = lookup.getJFireServerManager();
			try {
				this.ip = jfireServerManager.login(organisationID, userID, sessionID, password);
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
	
	/**
	 * @see org.jboss.security.auth.spi.AbstractServerLoginModule#commit()
	 */
	public boolean commit() throws LoginException
	{
		if (!super.commit()) {
			logger.error("org.jboss.security.auth.spi.AbstractServerLoginModule.commit() returned false!");
			return false;
		}

//	 Set the login principal and credential and subject
    SecurityAssociation.setPrincipal(ip);
    SecurityAssociation.setCredential(loginCredential);
    SecurityAssociation.setSubject(subject);

//    // Add the login principal to the subject if is not there
//    Set principals = subject.getPrincipals();
//    if (principals.contains(principal) == false)
//       principals.add(principal);
    return true;
	}

	/**
	 * @see org.jboss.security.auth.spi.AbstractServerLoginModule#logout()
	 */
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

	protected Principal getIdentity()
	{
		logger.debug("*********************************************************");
		logger.debug("getIdentity() returning JFirePrincipal: "+ip);
		return ip;
	}

	protected Group[] getRoleSets()
		throws LoginException
	{
		logger.debug("*********************************************************");
		logger.debug("getRoleSets()");
		
		if (ip == null)
			throw new NullPointerException("Why the hell is getRoleSets() called before login?!");

		Group callerPrincipal = new SimpleGroup("CallerPrincipal");
		callerPrincipal.addMember(ip);
		return new Group[]{ip.getRoleSet(), callerPrincipal};
	}

}
