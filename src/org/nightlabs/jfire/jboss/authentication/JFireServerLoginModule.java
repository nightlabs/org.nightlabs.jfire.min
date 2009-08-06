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

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimpleGroup;
import org.jboss.security.SecurityAssociation.SubjectContext;
import org.jboss.security.auth.spi.AbstractServerLoginModule;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.util.IOUtil;
import org.nightlabs.util.Util;


/**
 * @author niklas schiffler - nick at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author marco schulze - marco at nightlabs dot de
 */
public class JFireServerLoginModule extends AbstractServerLoginModule
{
	private static final Logger logger = Logger.getLogger(JFireServerLoginModule.class);

	@SuppressWarnings("unchecked")
	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options)
	{
		super.initialize(subject, callbackHandler, sharedState, options);
	}

	private static final boolean debugAuthenticatedLoginModules = false;

	private static class LoginDebugData {
		public long commitTimestamp;
		public Throwable commitStackTrace;
	}

	private static final Map<JFireServerLoginModule, LoginDebugData> authenticatedLoginModule2loginDebugData;
	static {
		if (debugAuthenticatedLoginModules)
			authenticatedLoginModule2loginDebugData = Collections.synchronizedMap(new HashMap<JFireServerLoginModule, LoginDebugData>());
		else
			authenticatedLoginModule2loginDebugData = null;
	}

	public static void debugDumpAuthenticatedLoginModules()
	throws IOException
	{
		if (authenticatedLoginModule2loginDebugData == null)
			throw new IllegalStateException("debugAuthenticatedLoginModules is not enabled!");

		Map<JFireServerLoginModule, LoginDebugData> copy;
		synchronized (authenticatedLoginModule2loginDebugData) {
			copy = new HashMap<JFireServerLoginModule, LoginDebugData>(authenticatedLoginModule2loginDebugData);
		}

		File tempDir = IOUtil.createUserTempDir("JFireServerLoginModule.", ".temp");

		for (Map.Entry<JFireServerLoginModule, LoginDebugData> me : copy.entrySet()) {
			File f = new File(tempDir, "commit-" + Long.toString(me.getValue().commitTimestamp, 36) + ".txt");
			IOUtil.writeTextFile(f, Util.getStackTraceAsString(me.getValue().commitStackTrace));
		}
	}

	private JFirePrincipal jfirePrincipal = null;
	private LoginData loginData = null;
	private boolean ignoreLogout = false;

	protected String getIdentityHashStr()
	{
		return Integer.toHexString(System.identityHashCode(this));
	}

	public JFireServerLoginModule()
	{
		if (logger.isTraceEnabled())
			logger.trace("(" + getIdentityHashStr() + ") default constructor");
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean login() throws LoginException
	{
		super.loginOk = false;
		ignoreLogout = false;

		NameCallback nc = new NameCallback("username: ");
		PasswordCallback pc = new PasswordCallback("password: ", false);

		Callback[] callbacks = {nc, pc};

		String login;
		String password;

		// get the login information via the callbacks
		try {
			callbackHandler.handle(callbacks);
			login = ((NameCallback)callbacks[0]).getName();
			char[] tmpPassword = ((PasswordCallback)callbacks[1]).getPassword();

			if (tmpPassword == null)
				password = ""; // an empty password does never pass UserLocal.checkPassword(...) [it's too short], so we can continue with it
			else
				password = new String(tmpPassword);

			((PasswordCallback)callbacks[1]).clearPassword();
		} catch (Exception x) {
			logger.fatal("Callback handling failed!", x);
			throw new LoginException(x.getMessage());
		}

		if (logger.isDebugEnabled()) {
			if (logger.isTraceEnabled())
				logger.trace("(" + getIdentityHashStr() + ") login: " + login, new Exception("StackTrace"));
			else
				logger.debug("(" + getIdentityHashStr() + ") login: " + login);
		}

		loginData = new LoginData(login, password);

		try {
			// create lookup object for user's organisationID
			Lookup lookup = new Lookup(loginData.getOrganisationID());

			// and delegate the login to the jfireServerManager
			JFireServerManager jfireServerManager = lookup.getJFireServerManager();
			try {
				this.jfirePrincipal = jfireServerManager.login(loginData);
			} finally {
				jfireServerManager.close();
			}
		} catch (LoginException e) {
			throw e;
		} catch(Throwable e) {
			logger.fatal("Login failed!", e);
			throw new LoginException(e.getMessage());
		}

//		super.subject.getPrincipals().add(ip); // doing this in commit

		// I don't know whether this sharedState is necessary, but I saw it in all the other login modules.
		// I think it's only required for combining multiple login-modules. Marco.
		sharedState.put("javax.security.auth.login.name", login);
		sharedState.put("javax.security.auth.login.password", password);

		super.loginOk = true;
		return true;
	}

	@Override
	public boolean commit() throws LoginException
	{
		if (!super.commit()) {
			logger.error("(" + getIdentityHashStr() + ") commit: org.jboss.security.auth.spi.AbstractServerLoginModule.commit() returned false!");
			return false;
		}

		if (logger.isDebugEnabled()) {
			if (logger.isTraceEnabled())
				logger.trace("(" + getIdentityHashStr() + ") commit: " + jfirePrincipal + " (id="+Integer.toHexString(System.identityHashCode(jfirePrincipal))+")", new Exception("StackTrace"));
			else
				logger.debug("(" + getIdentityHashStr() + ") commit: " + jfirePrincipal + " (id="+Integer.toHexString(System.identityHashCode(jfirePrincipal))+")");
		}

		if (jfirePrincipal == null)
			throw new NullPointerException("Why the hell is commit() called before login?!");

// copied more or less from JBoss' ClientLoginModule
		// Set the login principal and credential and subject
		SubjectContext subjectContext = SecurityAssociation.peekSubjectContext();
		Principal subjectContextPrincipal = subjectContext == null ? null : subjectContext.getPrincipal();
		if (!jfirePrincipal.equals(subjectContextPrincipal)) {
			SecurityAssociation.pushSubjectContext(subject, jfirePrincipal, loginData.getPassword());

			// Add the login principal to the subject if is not there
			Set<Principal> principals = subject.getPrincipals();
			if (principals.contains(jfirePrincipal) == false)
				principals.add(jfirePrincipal);
			// end copy

			if (authenticatedLoginModule2loginDebugData != null) {
				LoginDebugData ldd = new LoginDebugData();
				ldd.commitTimestamp = System.currentTimeMillis();
				ldd.commitStackTrace = new Exception("StackTrace");
				authenticatedLoginModule2loginDebugData.put(this, ldd);
			}
		}
		else {
			if (logger.isDebugEnabled())
				logger.debug("(" + getIdentityHashStr() + ") commit: " + jfirePrincipal + " (id="+Integer.toHexString(System.identityHashCode(jfirePrincipal))+"): We are already authenticated correctly - skipping this commit!");

			Set<Principal> principals = subject.getPrincipals();
			if (principals.contains(jfirePrincipal) == false)
				throw new IllegalStateException("(" + getIdentityHashStr() + ") commit: We are authenticated correcly already according to the SecurityAssociation, but the subject does not contain our principal yet!!!");

			// JBoss behaves very strangely concerning calling logout. In most cases, it does not call logout, if
			// we encountered this situation (strange that it calls login in the first place, but well...
			// We set the firePrincipal to another (previously logged-in) principal and suppress logout. Marco.
			ignoreLogout = true;
			jfirePrincipal = (JFirePrincipal) subjectContextPrincipal;
		}

		loginData = null; // forget the password - this login-module doesn't need its reference anymore
		return true;
	}

	@Override
	public boolean abort() throws LoginException
	{
		if (logger.isDebugEnabled()) {
			if (logger.isTraceEnabled())
				logger.trace("(" + getIdentityHashStr() + ") abort: " + jfirePrincipal + " (id="+Integer.toHexString(System.identityHashCode(jfirePrincipal))+")", new Exception("StackTrace"));
			else
				logger.debug("(" + getIdentityHashStr() + ") abort: " + jfirePrincipal + " (id="+Integer.toHexString(System.identityHashCode(jfirePrincipal))+")");
		}

		// Reset to the state before login() was called.
		loginData = null;
		jfirePrincipal = null;
		return super.abort();
	}

	@Override
	public boolean logout() throws LoginException
	{
		if (ignoreLogout) {
			logger.debug("(" + getIdentityHashStr() + ") logout: ignoreLogout is true => skipping logout action in this login-module instance!");
			return super.logout();
		}

		if (logger.isDebugEnabled()) {
			if (logger.isTraceEnabled())
				logger.trace("(" + getIdentityHashStr() + ") logout: logging out " + jfirePrincipal + " (id="+Integer.toHexString(System.identityHashCode(jfirePrincipal))+")", new Exception("StackTrace"));
			else
				logger.debug("(" + getIdentityHashStr() + ") logout: logging out " + jfirePrincipal + " (id="+Integer.toHexString(System.identityHashCode(jfirePrincipal))+")");
		}

		if (jfirePrincipal == null) {
			logger.warn(
					"(" + getIdentityHashStr() + ") logout: Logging out without being logged in!!!",
					new IllegalStateException("There is no jfirePrincipal! Either logout was already called or login+commit never happened!")
			);

			return super.logout();
		}

// copied more or less from JBoss' ClientLoginModule
		SubjectContext subjectContext = SecurityAssociation.popSubjectContext();
// end copy

		if (subjectContext == null) {
			logger.warn("(" + getIdentityHashStr() + ") logout: SecurityAssociation.popSubjectContext() returned null!");
		}
		else {
			Principal subjectContextPrincipal = subjectContext.getPrincipal();
			if (jfirePrincipal != null && jfirePrincipal != subjectContextPrincipal) {
				if (logger.isDebugEnabled()) {
					if (logger.isTraceEnabled())
						logger.trace("(" + getIdentityHashStr() + ") logout: SecurityAssociation.popSubjectContext() did not reveal principal " + jfirePrincipal + " (id="+Integer.toHexString(System.identityHashCode(jfirePrincipal))+") but instead " + subjectContextPrincipal + " (id="+Integer.toHexString(System.identityHashCode(subjectContextPrincipal))+")", new Exception("StackTrace"));
					else
						logger.debug("(" + getIdentityHashStr() + ") logout: SecurityAssociation.popSubjectContext() did not reveal principal " + jfirePrincipal + " (id="+Integer.toHexString(System.identityHashCode(jfirePrincipal))+") but instead " + subjectContextPrincipal + " (id="+Integer.toHexString(System.identityHashCode(subjectContextPrincipal))+")");
				}

				int counter = 0;
				do {
					++counter;
					subjectContext = SecurityAssociation.popSubjectContext();
					subjectContextPrincipal = subjectContext == null ? null : subjectContext.getPrincipal();

					if (counter > 100)
						throw new IllegalStateException("Popping " + (counter - 1) + " times still did not reveal the principal we have pushed before!");

				} while (jfirePrincipal != subjectContextPrincipal);
			}
		}

		subject.getPrincipals().remove(jfirePrincipal);
		jfirePrincipal = null;

		if (authenticatedLoginModule2loginDebugData != null) {
			authenticatedLoginModule2loginDebugData.remove(this);
		}

		return super.logout();
	}

	@Override
	protected Principal getIdentity()
	{
		if (logger.isTraceEnabled()) {
			logger.trace("(" + getIdentityHashStr() + ") *********************************************************");
			logger.trace("(" + getIdentityHashStr() + ") getIdentity() returning JFirePrincipal: "+jfirePrincipal);
		}
		return jfirePrincipal;
	}

	@Override
	protected Group[] getRoleSets()
		throws LoginException
	{
		if (logger.isTraceEnabled()) {
			logger.trace("(" + getIdentityHashStr() + ") *********************************************************");
			logger.trace("(" + getIdentityHashStr() + ") getRoleSets()");
		}

		if (jfirePrincipal == null)
			throw new NullPointerException("Why the hell is getRoleSets() called before login?!");

		Group callerPrincipal = new SimpleGroup("CallerPrincipal");
		callerPrincipal.addMember(jfirePrincipal);
		return new Group[]{jfirePrincipal.getRoleSet(), callerPrincipal};
	}

}
