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
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.jboss.cascadedauthentication;

import java.lang.reflect.Proxy;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginContext;

import org.apache.log4j.Logger;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationContext;
import org.jboss.proxy.ClientContainer;
import org.jboss.proxy.ejb.GenericEJBInterceptor;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.base.login.JFireLogin;
import org.nightlabs.jfire.jboss.authentication.JFireServerLoginModule;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.util.Util;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class CascadedAuthenticationClientInterceptorDelegate extends GenericEJBInterceptor
{
	public static final long serialVersionUID = 1L;

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(CascadedAuthenticationClientInterceptorDelegate.class);

	public CascadedAuthenticationClientInterceptorDelegate() { }

	private static Set<String> organisationIDs_thisServer = Collections.synchronizedSet(new HashSet<String>());
//	private static Set<String> organisationIDs_otherServer = Collections.synchronizedSet(new HashSet<String>());

	private static Set<String> userNames_thisServer = Collections.synchronizedSet(new HashSet<String>());
//	private static Set<String> userNames_otherServer = Collections.synchronizedSet(new HashSet<String>());

	private static boolean isUserOnThisServer(String userName)
	{
		if (userNames_thisServer.contains(userName))
			return true;

//		if (userNames_otherServer.contains(userName))
//			return false;

		String organisationID = LoginData.PATTERN_SPLIT_LOGIN.split(userName)[1];
		if (organisationIDs_thisServer.contains(organisationID)) {
			userNames_thisServer.add(userName);
			return true;
		}

//		if (organisationIDs_otherServer.contains(organisationID)) {
//			userNames_otherServer.add(userName);
//			return false;
//		}

		// TODO how can we find out, whether it's another server? Not being local doesn't mean other, because it might not yet exist and be created later here.
		try {
			InitialContext ctx = new InitialContext();
			try {
				JFireServerManagerFactory jfsmf = (JFireServerManagerFactory)ctx.lookup(JFireServerManagerFactory.JNDI_NAME);

				if (jfsmf.containsOrganisation(organisationID)) {
					userNames_thisServer.add(userName);
					organisationIDs_thisServer.add(organisationID);
					return true;
				}
				return false;

			} finally {
				ctx.close();
			}
		} catch (NamingException x) {
			throw new RuntimeException(x);
		}
	}

	@Override
	public Object invoke(Invocation invocation) throws Throwable
	{
		// first of all, we save our current identity in order to restore it later
		Principal oldPrincipal = SecurityAssociation.getPrincipal();
		Object oldCredential = SecurityAssociation.getCredential();
		String oldPassword = null;
		if (oldCredential != null) {
			if (oldCredential instanceof String)
				oldPassword = (String) oldCredential;
			else if (oldCredential instanceof char[])
				oldPassword = new String((char[])oldCredential);
			else
				throw new IllegalStateException("SecurityAssociation.getCredential() returned an object of invalid type (" + (oldCredential == null ? null : oldCredential.getClass().getName()) + ")! Expected: java.lang.String or char[]");
		}

		// check, whether we have a UserDescriptor associated to the current thread.
		UserDescriptor userDescriptor = UserDescriptor.getUserDescriptor();
//		UserDescriptor userDescriptor = null; // we don't do this anymore since we work always on the same thread - our interesting infos should be in the invocation context

		// If there is no UserDescriptor sticking to the current thread, we check the
		// InvocationContext.
		if (userDescriptor == null) {
			InvocationContext context = invocation.getInvocationContext();
			userDescriptor = (UserDescriptor)context.getValue(UserDescriptor.CONTEXT_KEY);
//			userDescriptor = (UserDescriptor)invocation.getInvocationContext().getValue(UserDescriptor.CONTEXT_KEY);

			if(logger.isDebugEnabled())
				logger.debug("invoke: > method="+invocation.getMethod().getDeclaringClass().getName()+"#"+invocation.getMethod().getName()+
						" SecurityAssociation.principal="+SecurityAssociation.getPrincipal()+
						" SecurityAssociation.callerPrincipal="+SecurityAssociation.getCallerPrincipal()+": No UserDescriptor associated with current thread. Fetched "+
						(userDescriptor == null ? null : userDescriptor.userName)+" from invocationContext: " + // (context == null ? null : context.getClass().getName())+'@'+System.identityHashCode(context) + "#" +
						invocation.getInvocationContext());
		}
		else {
			if(logger.isDebugEnabled())
				logger.debug("invoke: > method="+invocation.getMethod().getDeclaringClass().getName()+"#"+invocation.getMethod().getName()+
						" SecurityAssociation.principal="+SecurityAssociation.getPrincipal()+
						" SecurityAssociation.callerPrincipal="+SecurityAssociation.getCallerPrincipal()+": UserDescriptor associated with current thread: "+
						(userDescriptor == null ? null : userDescriptor.userName));
		}

		// If there's still no UserDescriptor, it means that we do sth. locally within the server.
		// Therefore, we use the current SecurityAssociation.
		// Unfortunately, this is necessary - which basically means we'll have always a CapsuledCaller in the server :-( Marco. 2006-09-03
		if (userDescriptor == null) {
//			Principal principal = SecurityAssociation.getPrincipal();
			Principal principal = oldPrincipal;
			if (principal != null) {
//				Object pwo = SecurityAssociation.getCredential();
				Object pwo = oldCredential;
				String pw;
				if (pwo instanceof String)
					pw = (String) pwo;
				else if (pwo instanceof char[])
					pw = new String((char[])pwo);
				else
					throw new IllegalStateException("SecurityAssociation.getCredential() returned an object of invalid type (" + (pwo == null ? null : pwo.getClass().getName()) + ")! Expected: java.lang.String or char[]");

				userDescriptor = new UserDescriptor(principal.getName(), pw);
			}

			if(logger.isDebugEnabled())
				logger.debug("invoke: > method="+invocation.getMethod().getDeclaringClass().getName()+"#"+invocation.getMethod().getName()+
						" SecurityAssociation.principal="+SecurityAssociation.getPrincipal()+
						" SecurityAssociation.callerPrincipal="+SecurityAssociation.getCallerPrincipal()+": UserDescriptor could not be obtained from invocation and not from Thread! Using current principal.");
		}

//		Object result;
//		if (userDescriptor == null) {
//		if(logger.isDebugEnabled())
//		logger.debug("invoke: method="+invocation.getMethod().getDeclaringClass().getName()+"#"+invocation.getMethod().getName()+": userDescriptor == null => invoking directly (no wrapper thread)");

//		result = getNext().invoke(invocation);
//		}
//		else { // invoke on wrapper thread
//		Mutex waitForNotification = new Mutex();
//		CapsuledCaller cc = new CapsuledCaller(this, invocation, userDescriptor, waitForNotification);
//		while (cc.isAlive() && !waitForNotification.isFinished()) {
//		synchronized(waitForNotification) {
//		try { waitForNotification.wait(10000); } catch (InterruptedException x) { }
//		} // synchronized(waitForNotification) {
//		} // while (!waitForNotification.isFinished()) {

//		if (cc.exception != null)
//		throw new RuntimeException("Cascaded invocation via CapsuledCaller thread failed!", cc.exception);

//		result = cc.result;
//		}

		String oldUserName = oldPrincipal == null ? null : oldPrincipal.getName();
		if (oldUserName != null) {
			int idx = oldUserName.indexOf('?');
			if (idx >= 0)
				oldUserName = oldUserName.substring(0, idx);
		}

		String newUserName = userDescriptor == null ? null : userDescriptor.userName;
		boolean changeIdentity = newUserName != null;
		if (changeIdentity) {
			int idx = newUserName.indexOf('?');
			if (idx >= 0)
				newUserName = newUserName.substring(0, idx);

			if (Util.equals(newUserName, oldUserName))
				changeIdentity = false;
		} // if (changeIdentity) {

		LoginData loginData = null;
		LoginContext loginContext = null;
		if (changeIdentity) {
			if (userDescriptor != null) {
				boolean localLogin = isUserOnThisServer(newUserName);
				if (localLogin) {
					loginData = new LoginData(userDescriptor.userName, userDescriptor.password);
					loginContext = new LoginContext(LoginData.DEFAULT_SECURITY_PROTOCOL, new JFireLogin(loginData).getAuthCallbackHandler());
					loginContext.login();
				}
				else { // if we logged in to a remote-server, it would try it locally and fail => hence we only set the identity without a real login
					SecurityAssociation.setPrincipal(new SimplePrincipal(userDescriptor.userName));
					SecurityAssociation.setCredential(userDescriptor.password.toCharArray());
				}
			}
		}

		Object result;
		try {

			result = this.getNext().invoke(invocation);

		} finally {
			if (changeIdentity) {
				if (loginContext != null) {
					// We have to logout, because we must use restore-login-identity, since it otherwise doesn't
					// work with a mix of local beans (e.g. StoreManagerHelperLocal) and foreign-organisation
					// non-local beans (e.g. TradeManager on another organisation).
					loginContext.logout();

					if (oldPrincipal != null) {
						loginData = new LoginData(oldPrincipal.getName(), oldPassword);
						loginContext = new LoginContext(LoginData.DEFAULT_SECURITY_PROTOCOL, new JFireLogin(loginData).getAuthCallbackHandler());
						JFireServerLoginModule.cascadedAuthenticationRestoreIdentityBegin(oldPrincipal);
						try {
							loginContext.login();
							// I think the following catch clause is not necessary anymore, because prepareCascadedAuthenticationRestoreIdentity
							// should guarantee that login is successful.
//						} catch (Exception x) {
//							// During server-setup it might happen that we cannot re-login at the organisation "__foobar_organisation_for_initial_login__",
//							// hence, we ignore this problem (leaving us simply unauthenticated).
//							if (!"__foobar_organisation_for_initial_login__".equals(loginData.getOrganisationID())) // not so clean, but at least a good way to prevent the message at every server-setup
//								logger.error("Cannot re-login as \"" + (loginData == null ? null : loginData.getPrincipalName()) + "\" after having executed a bean method as a different user (\"" + (userDescriptor == null ? null : userDescriptor.userName) + "\")!", x);
						} finally {
							JFireServerLoginModule.cascadedAuthenticationRestoreIdentityEnd(oldPrincipal);
						}
					}
				}
				else {
					SecurityAssociation.setPrincipal(oldPrincipal);
					SecurityAssociation.setCredential(oldCredential);
				}
			} // if (restoreOldLogin) {

			if(logger.isDebugEnabled())
				logger.debug("invoke: < method="+invocation.getMethod().getDeclaringClass().getName()+"#"+invocation.getMethod().getName()+
						" SecurityAssociation.principal="+SecurityAssociation.getPrincipal()+
						" SecurityAssociation.callerPrincipal="+SecurityAssociation.getCallerPrincipal()+
						" oldPrincipal="+oldPrincipal);
		}

		if (!(result instanceof Proxy)) {
			if(logger.isDebugEnabled())
				logger.debug("invoke: method="+invocation.getMethod().getDeclaringClass().getName()+"#"+invocation.getMethod().getName()+": after invocation: result \""+result+"\" is NOT an instance of Proxy!");
			return result;
		}

		// I think, we either have to synchronize the tagging of the result or always clone it. Otherwise it's not thread-safe.

//		synchronized (result) {
//			ClientContainer clientContainer = (ClientContainer) Proxy.getInvocationHandler(result);
//
//			// Check whether it's necessary to clone the result. Since the EJBHome.create() method returns the same instance of
//			// the EJB proxy if we're communicating with the same server, we have to clone it, if it already has a different
//			// UserDescriptor assigned (not the current).
//			// Obviously, the server pools stateless session bean proxies and ignores the information of the
//			// initial-context-properties (there are different user names + different credentials).
//			UserDescriptor oldUserDescriptor = (UserDescriptor) clientContainer.context.getValue(UserDescriptor.CONTEXT_KEY);
//			if (oldUserDescriptor != null && !oldUserDescriptor.equals(userDescriptor)) {
//				if(logger.isDebugEnabled())
//					logger.debug("invoke: method="+invocation.getMethod().getDeclaringClass().getName()+"#"+invocation.getMethod().getName()+
//							": after invocation: The clientContainer already contains a UserDescriptor in its context, but it is referencing another user (\"" +
//							oldUserDescriptor.userName +
//							"\" instead of \"" +
//							(userDescriptor == null ? null : userDescriptor.userName) +
//							"\")! Will clone the result. clientContainer="+clientContainer+" context=" +
//							clientContainer.context.toString());
//
//				result = Util.cloneSerializable(result);
//				clientContainer = (ClientContainer) Proxy.getInvocationHandler(result);
//			}
//
//			if(logger.isDebugEnabled())
//				logger.debug("invoke: method="+invocation.getMethod().getDeclaringClass().getName()+"#"+invocation.getMethod().getName()+
//						": after invocation: copying UserDescriptor (username="+userDescriptor.userName+") to context: clientContainer="+clientContainer+" context=" +
//						// (clientContainer.context == null ? null : clientContainer.context.getClass().getName())+'@'+System.identityHashCode(clientContainer.context) + "#"+
//						clientContainer.context.toString());
//
//			clientContainer.context.setValue(UserDescriptor.CONTEXT_KEY, userDescriptor);
//		}

		result = Util.cloneSerializable(result);
		ClientContainer clientContainer = (ClientContainer) Proxy.getInvocationHandler(result);
		if(logger.isDebugEnabled())
			logger.debug("invoke: method="+invocation.getMethod().getDeclaringClass().getName()+"#"+invocation.getMethod().getName()+
					": after invocation: copying UserDescriptor (username="+userDescriptor.userName+") to context: clientContainer="+clientContainer+" context=" +
					// (clientContainer.context == null ? null : clientContainer.context.getClass().getName())+'@'+System.identityHashCode(clientContainer.context) + "#"+
					clientContainer.context.toString());

		clientContainer.context.setValue(UserDescriptor.CONTEXT_KEY, userDescriptor);

		return result;
	}
}
