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

package org.nightlabs.jfire.asyncinvoke;

import javax.jms.ObjectMessage;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.security.auth.login.LoginContext;

import org.apache.log4j.Logger;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.AuthCallbackHandler;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public abstract class AsyncInvokerBaseBean
implements javax.ejb.MessageDrivenBean, javax.jms.MessageListener
{
	private static final long serialVersionUID = 1L;

	/**
	 * LOG4J logger used by this class
	 */
	protected Logger logger() {
		return Logger.getLogger(this.getClass());
	}

	protected javax.ejb.MessageDrivenContext messageContext = null;

	/**
	 * Required method for container to set context.
	 */
	public void setMessageDrivenContext(javax.ejb.MessageDrivenContext messageContext)
	throws javax.ejb.EJBException
	{
		this.messageContext = messageContext;
	}

	/**
	 * Required creation method for message-driven beans.
	 *
	 * @ejb.create-method
	 */
	public void ejbCreate()
	{
		//no specific action required for message-driven beans
	}

	/**
	 * Required removal method for message-driven beans.
	 */
	public void ejbRemove()
	{
		messageContext = null;
	}


	public void onMessage(javax.jms.Message message)
	{
//		if (logger().isDebugEnabled())
//			logger().debug("onMessage(...) entered.");
		logger().info("onMessage(...) entered.");

		try {
			if (!(message instanceof ObjectMessage)) {
				logger().error("Message is not an instance of ObjectMessage: " + message);
				return;
			}

			Object obj = ((ObjectMessage)message).getObject();
			if (!(obj instanceof AsyncInvokeEnvelope)) {
				logger().error("Object wrapped in ObjectMessage is not an instance of AsyncInvokeEnvelope: " + message);
				return;
			}

			InitialContext initCtxNotAuthenticated = new InitialContext();

			// we need to wait for the system to be up, ready and running
			// wait max 5 min for the JFireServerManagerFactory to pop up in JNDI
			long startDT = System.currentTimeMillis();
			JFireServerManagerFactory ismf = null;
			if (logger().isDebugEnabled())
				logger().debug("looking up JFireServerManagerFactory...");

			do {
				if (System.currentTimeMillis() - startDT > 5 * 60 * 1000)
					throw new IllegalStateException("JFireServerManagerFactory did not pop up in JNDI within timeout (hardcoded 5 min)!");

				try {
					ismf = (JFireServerManagerFactory) initCtxNotAuthenticated.lookup(JFireServerManagerFactory.JNDI_NAME);
				} catch (NameNotFoundException x) {
					// ignore
					ismf = null;
					logger().info("JFireServerManagerFactory is not (yet) bound into JNDI! Will wait and try again...");
				}

				if (ismf == null)
					try { Thread.sleep(3000); } catch (InterruptedException x) { }

			} while (ismf == null);

			if (logger().isDebugEnabled())
				logger().debug("checking whether JFireServerManagerFactory is up and running...");

			startDT = System.currentTimeMillis();
			do {
				if (System.currentTimeMillis() - startDT > 10 * 60 * 1000)
					throw new IllegalStateException("JFireServer did not start within timeout (hardcoded 10 min)!");

				if (ismf.isShuttingDown()) {
					throw new IllegalStateException("Server is shutting down! Cannot process message anymore!");
				}

				if (!ismf.isUpAndRunning()) {
					try { Thread.sleep(3000); } catch (InterruptedException x) { }
					logger().info("JFireServerManagerFactory is not (yet) up and running! Will wait and try again...");
				}

			} while (!ismf.isUpAndRunning());

			if (logger().isDebugEnabled())
				logger().debug("JFireServerManagerFactory is up and running. Will process asynchronous invocation.");

			AsyncInvokeEnvelope envelope = (AsyncInvokeEnvelope) obj;

//			if (pseudoExternalInvoke) {
//				JFireServerManager ism = ismf.getJFireServerManager();
//				try {
//					SecurityReflector.UserDescriptor caller = envelope.getCaller();
//					Hashtable props = new Properties();
//					ServerCf localServer = ismf.getLocalServer();
//					String initialContextFactory = ismf.getInitialContextFactory(localServer.getJ2eeServerType(), true);
//					props.put(InitialContext.INITIAL_CONTEXT_FACTORY, initialContextFactory);
//					props.put(InitialContext.PROVIDER_URL, localServer.getInitialContextURL());
//					props.put(InitialContext.SECURITY_PRINCIPAL, caller.getUserID() + '@' + caller.getOrganisationID());
//					props.put(InitialContext.SECURITY_CREDENTIALS, ism.jfireSecurity_createTempUserPassword(caller.getOrganisationID(), caller.getUserID()));
//					props.put(InitialContext.SECURITY_PROTOCOL, "jfire");
//
//					AsyncInvokerDelegate invokerDelegate = null;
//
//					try {
//						invokerDelegate = AsyncInvokerDelegateUtil.getHome(props).create();
//					} catch (Exception x) {
//						logger().fatal("Obtaining stateless session bean AsyncInvokerDelegate failed!", x);
//						messageContext.setRollbackOnly();
//					}
//
//					if (invokerDelegate != null)
//						doInvoke(envelope, invokerDelegate);
//				} finally {
//					ism.close();
//				}
//			}
//			else {


			LoginContext loginContext;
			JFireServerManager ism = ismf.getJFireServerManager();
			try {
				loginContext = new LoginContext(
						LoginData.DEFAULT_SECURITY_PROTOCOL, createAuthCallbackHandler(ism, envelope));

				loginContext.login();
				try {
					AsyncInvokerDelegateLocal invokerDelegate = null;

					try {
						invokerDelegate = AsyncInvokerDelegateUtil.getLocalHome().create();
					} catch (Exception x) {
						logger().fatal("Obtaining stateless session bean AsyncInvokerDelegateLocal failed!", x);
						messageContext.setRollbackOnly();
					}

					if (invokerDelegate != null)
						doInvoke(envelope, invokerDelegate);

				} finally {
					loginContext.logout();
				}

			} finally {
				ism.close();
			}

//			}

			// the above code does not work in this scenario:
			// 1) the asyncInvocation obtains a local bean to its local organisation
			// 2) then, it obtains a remote beans to another organisation
			// 3) then it calls an EJB method on the remote bean
			// 4) if it calls now a method on the local bean, it is instead executed remotely :-(
			// Hence, I try the below code - hope, this works.
			// Marco. 2007-07-30
			//
			// I modified the cascaded-authentication-stuff lately and fixed a similar bug. So maybe, it works now - I'll try the above code again.
			// Marco. 2008-01-19

//			JFireServerManager ism = ismf.getJFireServerManager();
//			try {
//				String organisationID = envelope.getCaller().getOrganisationID();
//				String userID = envelope.getCaller().getUserID();
//				String pw = ism.jfireSecurity_createTempUserPassword(organisationID, userID);
//
//				if (logger().isDebugEnabled()) {
//					logger().debug("AsyncInvokerBaseBean.onMessage: organisationID=\"" + organisationID + "\" userID=\"" + userID +"\" password=\"" + pw + "\"");
//				}
//
//				AsyncInvokerDelegate invokerDelegate = null;
//				invokerDelegate = AsyncInvokerDelegateUtil.getHome(
//						InvokeUtil.getInitialContextProperties(
//								ismf, ismf.getLocalServer(),
//								organisationID, userID, pw)).create();
//				doInvoke(envelope, invokerDelegate);
//			} finally {
//				ism.close();
//			}

		} catch (Throwable x) {
			logger().fatal("Processing message failed!", x);
			messageContext.setRollbackOnly();
		}
	}

	private static AuthCallbackHandler createAuthCallbackHandler(JFireServerManager ism, AsyncInvokeEnvelope envelope) {
		SecurityReflector.UserDescriptor caller = envelope.getCaller();
		return new AuthCallbackHandler(ism,
				caller.getOrganisationID(),
				caller.getUserID(),
				ObjectIDUtil.makeValidIDString(null, true));
	}

//	private static final boolean pseudoExternalInvoke = false;

	protected abstract void doInvoke(AsyncInvokeEnvelope envelope, Delegate invokerDelegate);
}
