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

import org.apache.log4j.Logger;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationContext;
import org.jboss.proxy.ClientContainer;
import org.jboss.proxy.Interceptor;
import org.jboss.proxy.ejb.GenericEJBInterceptor;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
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
	
	public CascadedAuthenticationClientInterceptorDelegate()
	{
		// LOGGER.info("instance created!");
	}

	public static class Mutex
	{
		private boolean finished = false;
		
		/**
		 * @return Returns the finished.
		 */
		public synchronized boolean isFinished()
		{
			return finished;
		}
		/**
		 * @param finished The finished to set.
		 */
		public synchronized void setFinished(boolean finished)
		{
			this.finished = finished;
			this.notifyAll();
		}
	}

	private static long nextCapsuledCallerThreadID = 0;
	protected static synchronized long nextCapsuledCallerThreadID()
	{
		long res = nextCapsuledCallerThreadID++;
		return res;
	}

	public static class CapsuledCaller extends Thread 
	{
		public Throwable exception = null;
		public Object result = null;

		private Interceptor interceptor;
		private Invocation invocation;
		private Mutex notifyThisOnFinish;
		private UserDescriptor userDescriptor;

		public CapsuledCaller(Interceptor _interceptor, Invocation _invocation, UserDescriptor _userDescriptor, Mutex _notifyThisOnFinish)
		{
			this.setName("CapsuledCaller-" + Long.toString(nextCapsuledCallerThreadID(), 36));
			this.interceptor = _interceptor;
			this.invocation = _invocation;
			this.notifyThisOnFinish = _notifyThisOnFinish;
			this.userDescriptor = _userDescriptor;
			this.start();
		}

		@Override
		public void run()
		{
//			Principal orig_callerPrincipal = SecurityAssociation.getCallerPrincipal();
//			Principal orig_principal = SecurityAssociation.getPrincipal();
//			Object orig_credential = SecurityAssociation.getCredential();

			if(logger.isDebugEnabled()) {
				logger.debug("run: >>>>>>>>>>>>>>>> begin invoke on wrapper thread: method="+invocation.getMethod().getDeclaringClass().getName()+"#"+invocation.getMethod().getName()+" username=" + userDescriptor.userName);
//				logger.debug("run: >> orig_callerPrincipal=" + orig_callerPrincipal);
//				logger.debug("run: >> orig_principal=" + orig_principal);
			}
			try {

				SecurityAssociation.setPrincipal(new SimplePrincipal(userDescriptor.userName));
				SecurityAssociation.setCredential(userDescriptor.password.toCharArray());
				result = interceptor.getNext().invoke(invocation);

//				LoginContext loginContext = new LoginContext("jfire", new CallbackHandler() {
//					public void handle(Callback[] callbacks)
//							throws IOException, UnsupportedCallbackException
//					{
//						 for (int i = 0; i < callbacks.length; i++) {
//		            if (callbacks[i] instanceof NameCallback) {
//		                NameCallback nc = (NameCallback)callbacks[i];
//		                nc.setName(userDescriptor.userName);
//		            } else if (callbacks[i] instanceof PasswordCallback) {
//		                PasswordCallback pc = (PasswordCallback)callbacks[i];
//		                pc.setPassword(userDescriptor.password.toCharArray());
//		            } else {
//		                throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
//		            }
//		        }
//					}
//				});
//
//				loginContext.login();
//				try {
//					Subject subject = loginContext.getSubject();
//
//					result = interceptor.getNext().invoke(invocation);
//
//				} finally {
//					loginContext.logout();
//				}
			} catch (Throwable e) {
				exception = e;
			} finally {
			if(logger.isDebugEnabled())
				logger.debug("run: <<<<<<<<<<<<<<<< end invoke on wrapper thread: method="+invocation.getMethod().getDeclaringClass().getName()+"#"+invocation.getMethod().getName()+" username=" + userDescriptor.userName);

//				SecurityAssociation.setPrincipal(orig_principal);
//				SecurityAssociation.setCredential(orig_credential);
			}

			// notify the other thread, thus he can continue
			notifyThisOnFinish.setFinished(true);
		}
	}

	/**
	 * @see org.jboss.proxy.Interceptor#invoke(org.jboss.invocation.Invocation)
	 */
	@Override
	public Object invoke(Invocation invocation) throws Throwable
	{
		// First we check, whether we have a UserDescriptor associated to the current thread.
		UserDescriptor userDescriptor = UserDescriptor.getUserDescriptor();

		// If there is no UserDescriptor sticking to the current thread, we check the
		// InvocationContext.
		if (userDescriptor == null) {
			InvocationContext context = invocation.getInvocationContext();
			userDescriptor = (UserDescriptor)context.getValue(UserDescriptor.CONTEXT_KEY);
//			userDescriptor = (UserDescriptor)invocation.getInvocationContext().getValue(UserDescriptor.CONTEXT_KEY);

			if(logger.isDebugEnabled())
				logger.debug("invoke: method="+invocation.getMethod().getDeclaringClass().getName()+"#"+invocation.getMethod().getName()+
						" SecurityAssociation.principal="+SecurityAssociation.getPrincipal()+
						" SecurityAssociation.callerPrincipal="+SecurityAssociation.getCallerPrincipal()+": No UserDescriptor associated with current thread. Fetched "+
						(userDescriptor == null ? null : userDescriptor.userName)+" from invocationContext: " + // (context == null ? null : context.getClass().getName())+'@'+System.identityHashCode(context) + "#" +
						invocation.getInvocationContext());
		}
		else {
			if(logger.isDebugEnabled())
				logger.debug("invoke: method="+invocation.getMethod().getDeclaringClass().getName()+"#"+invocation.getMethod().getName()+
						" SecurityAssociation.principal="+SecurityAssociation.getPrincipal()+
						" SecurityAssociation.callerPrincipal="+SecurityAssociation.getCallerPrincipal()+": UserDescriptor associated with current thread: "+
						(userDescriptor == null ? null : userDescriptor.userName));
		}

		// If there's still no UserDescriptor, it means that we do sth. locally within the server.
		// Therefore, we use the current SecurityAssociation.
		// Unfortunately, this is necessary - which basically means we'll have always a CapsuledCaller in the server :-( Marco. 2006-09-03
		if (userDescriptor == null) {
			Principal principal = SecurityAssociation.getPrincipal();
			if (principal != null) {
				Object pwo = SecurityAssociation.getCredential();
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
				logger.debug("invoke: method="+invocation.getMethod().getDeclaringClass().getName()+"#"+invocation.getMethod().getName()+
						" SecurityAssociation.principal="+SecurityAssociation.getPrincipal()+
						" SecurityAssociation.callerPrincipal="+SecurityAssociation.getCallerPrincipal()+": UserDescriptor could not be obtained from invocation and not from Thread! Using current principal.");
		}

		Object result;
		if (userDescriptor == null) {
			if(logger.isDebugEnabled())
				logger.debug("invoke: method="+invocation.getMethod().getDeclaringClass().getName()+"#"+invocation.getMethod().getName()+": userDescriptor == null => invoking directly (no wrapper thread)");

			result = getNext().invoke(invocation);
		}
		else { // invoke on wrapper thread
			Mutex waitForNotification = new Mutex();
			CapsuledCaller cc = new CapsuledCaller(this, invocation, userDescriptor, waitForNotification);
			while (cc.isAlive() && !waitForNotification.isFinished()) {
				synchronized(waitForNotification) {
					try { waitForNotification.wait(10000); } catch (InterruptedException x) { }
				} // synchronized(waitForNotification) {
			} // while (!waitForNotification.isFinished()) {
	
			if (cc.exception != null)
				throw new RuntimeException("Cascaded invocation via CapsuledCaller thread failed!", cc.exception);
	
			result = cc.result;
		}

		if (!(result instanceof Proxy)) {
			if(logger.isDebugEnabled())
				logger.debug("invoke: method="+invocation.getMethod().getDeclaringClass().getName()+"#"+invocation.getMethod().getName()+": after invocation: result \""+result+"\" is NOT an instance of Proxy!");
			return result;
		}

		ClientContainer clientContainer = (ClientContainer) Proxy.getInvocationHandler(result);

		// Check whether it's necessary to clone the result. Since the EJBHome.create() method returns the same instance of
		// the EJB proxy if we're communicating with the same server, we have to clone it, if it already has a different
		// UserDescriptor assigned (not the current).
		// Obviously, the server pools stateless session bean proxies and ignores the information of the
		// initial-context-properties (there are different user names + different credentials).
		UserDescriptor oldUserDescriptor = (UserDescriptor) clientContainer.context.getValue(UserDescriptor.CONTEXT_KEY);
		if (oldUserDescriptor != null && !oldUserDescriptor.equals(userDescriptor)) {
			if(logger.isDebugEnabled())
				logger.debug("invoke: method="+invocation.getMethod().getDeclaringClass().getName()+"#"+invocation.getMethod().getName()+
						": after invocation: The clientContainer already contains a UserDescriptor in its context, but it is referencing another user (\"" +
						oldUserDescriptor.userName +
						"\" instead of \"" +
						(userDescriptor == null ? null : userDescriptor.userName) +
						"\")! Will clone the result. clientContainer="+clientContainer+" context=" +
						clientContainer.context.toString());

			result = Util.cloneSerializable(result);
			clientContainer = (ClientContainer) Proxy.getInvocationHandler(result);
		}

		if(logger.isDebugEnabled())
			logger.debug("invoke: method="+invocation.getMethod().getDeclaringClass().getName()+"#"+invocation.getMethod().getName()+
					": after invocation: copying UserDescriptor (username="+userDescriptor.userName+") to context: clientContainer="+clientContainer+" context=" +
					// (clientContainer.context == null ? null : clientContainer.context.getClass().getName())+'@'+System.identityHashCode(clientContainer.context) + "#"+
					clientContainer.context.toString());

		clientContainer.context.setValue(UserDescriptor.CONTEXT_KEY, userDescriptor);

		return result;
	}
}
