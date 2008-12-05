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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
import org.nightlabs.jfire.jboss.cascadedauthentication.CascadedAuthenticationClientInterceptorDelegate;
import org.nightlabs.jfire.security.RoleSet;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.servermanager.JFireServerManager;


/**
 * @author niklas schiffler - nick at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class JFireServerLoginModule extends AbstractServerLoginModule
{
	private static final Logger logger = Logger.getLogger(JFireServerLoginModule.class);
	private static Map<String, RoleSet> userPK2roleSet = Collections.synchronizedMap(new HashMap<String, RoleSet>());
	private static ThreadLocal<Principal> cascadedAuthenticationRestoreIdentityPrincipal = new ThreadLocal<Principal>();

	/**
	 * Prepare restoring a previous principal. This is necessary, because {@link CascadedAuthenticationClientInterceptorDelegate}
	 * might restore a previous identity when a transaction is being rolled back. In this situation, the {@link JFireServerManager#login(LoginData)}
	 * will otherwise fail, because the transaction is not active anymore. In order to guarantee that restoring the old identity succeeds,
	 * we keep RoleSets in {@link #userPK2roleSet} and create a {@link JFirePrincipal} from there instead of calling
	 * {@link JFireServerManager#login(LoginData)} which would require an active transaction.
	 * <p>
	 * If this method was called, the {@link #login()} method below (or more precisely the {@link #commit()} method) will
	 * restore from the stack instead of really logging in.
	 * </p>
	 *
	 * @param principalToRestore the principal that is about to be restored.
	 */
	public static void cascadedAuthenticationRestoreIdentityBegin(Principal principalToRestore)
	{
		if (principalToRestore == null)
			throw new IllegalArgumentException("principal must not be null!");

		Principal principalAlreadyPreparedForRestoring = cascadedAuthenticationRestoreIdentityPrincipal.get();
		if (principalAlreadyPreparedForRestoring != null)
			throw new IllegalStateException("There is already another principal prepared for restoring: " + principalAlreadyPreparedForRestoring);

		cascadedAuthenticationRestoreIdentityPrincipal.set(principalToRestore);
	}

	public static void cascadedAuthenticationRestoreIdentityEnd(Principal principalToRestore)
	{
		if (principalToRestore == null)
			throw new IllegalArgumentException("principal must not be null!");

		Principal principalPreparedForRestoring = cascadedAuthenticationRestoreIdentityPrincipal.get();
		if (principalPreparedForRestoring == null)
			throw new IllegalStateException("cascadedAuthenticationRestoreIdentityBegin was not called!");

		if (principalPreparedForRestoring != principalToRestore)
			throw new IllegalStateException("cascadedAuthenticationRestoreIdentityBegin was called with a different principal! principalPreparedForRestoring=" + principalPreparedForRestoring + " principalToRestore=" + principalToRestore);

		cascadedAuthenticationRestoreIdentityPrincipal.remove();
	}

//	protected Lookup lookup;
	protected JFirePrincipal ip = null;

	private String identityHashStr = null;
	protected String getIdentityHashStr()
	{
		if (identityHashStr == null)
			identityHashStr = Integer.toHexString(System.identityHashCode(this));

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
		try {
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

		String userKey = loginData.getUserID() + User.SEPARATOR_BETWEEN_USER_ID_AND_ORGANISATION_ID + loginData.getOrganisationID();
		Principal principalPreparedForRestoring = cascadedAuthenticationRestoreIdentityPrincipal.get();
		if (principalPreparedForRestoring != null) {
//			this.ip = principalPreparedForRestoring;

			RoleSet roleSet = userPK2roleSet.get(userKey);
			if (roleSet == null)
				throw new IllegalStateException("No RoleSet found for userKey=" + userKey);

			this.ip = new JFirePrincipal(loginData, loginData.getUserID().startsWith(User.USER_ID_PREFIX_TYPE_ORGANISATION), new Lookup(loginData.getOrganisationID()), roleSet);
		}
		else {
			try {
				// create lookup object for user's organisationID
				Lookup lookup = new Lookup(loginData.getOrganisationID());

				// and delegate the login to the jfireServerManager
				JFireServerManager jfireServerManager = lookup.getJFireServerManager();
				try {
					this.ip = jfireServerManager.login(loginData);
				} finally {
					jfireServerManager.close();
				}

				userPK2roleSet.put(userKey, ip.getRoleSet());
			} catch (LoginException e) {
				throw e;
			} catch(Throwable e) {
				logger.fatal("Login failed!", e);
				throw new LoginException(e.getMessage());
			}
		}

		super.subject.getPrincipals().add(ip);
		super.loginOk = true;
		return true;
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

		if (ip == null)
			throw new NullPointerException("Why the hell is commit() called before login?!");

//    principalName2principal.put(ip.getName(), ip);
//    LinkedList<JFirePrincipal> principalStackThisThread = principalStack.get();
//    principalStackThisThread.push(ip);
//    logger.info("commit: principalStackThisThread.size()=" + principalStackThisThread.size());
//    for (JFirePrincipal jfirePrincipal : principalStackThisThread)
//			logger.info("  * " + jfirePrincipal);

		return true;
	}

	@Override
	public boolean logout() throws LoginException
	{
//		LinkedList<JFirePrincipal> principalStackThisThread = principalStack.get();
//		principalStackThisThread.pop();
//		logger.info("logout: principalStackThisThread.size()=" + principalStackThisThread.size());
//		for (JFirePrincipal jfirePrincipal : principalStackThisThread)
//			logger.info("  * " + jfirePrincipal);

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
