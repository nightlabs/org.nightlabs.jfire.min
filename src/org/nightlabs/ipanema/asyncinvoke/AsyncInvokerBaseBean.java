/*
 * Created on Mar 25, 2005
 */
package org.nightlabs.ipanema.asyncinvoke;

import javax.jms.ObjectMessage;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.security.auth.login.LoginContext;

import org.apache.log4j.Logger;
import org.nightlabs.ipanema.servermanager.JFireServerManager;
import org.nightlabs.ipanema.servermanager.JFireServerManagerFactory;
import org.nightlabs.ipanema.servermanager.j2ee.SecurityReflector;

import org.nightlabs.ipanema.asyncinvoke.AsyncInvokerDelegateLocal;
import org.nightlabs.ipanema.asyncinvoke.AsyncInvokerDelegateUtil;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public abstract class AsyncInvokerBaseBean
implements javax.ejb.MessageDrivenBean, javax.jms.MessageListener
{
	protected Logger logger = Logger.getLogger(this.getClass());

	protected javax.ejb.MessageDrivenContext messageContext = null;

	/** 
	 * Required method for container to set context.
	 * @generated 
	 */
	public void setMessageDrivenContext(
			javax.ejb.MessageDrivenContext messageContext)
			throws javax.ejb.EJBException
	{
		this.messageContext = messageContext;
	}

	/** 
	 * Required creation method for message-driven beans. 
	 *
	 * @ejb.create-method 
	 * @generated
	 */
	public void ejbCreate()
	{
		//no specific action required for message-driven beans 
	}

	/** 
	 * Required removal method for message-driven beans. 
	 * @generated
	 */
	public void ejbRemove()
	{
		messageContext = null;
	}


	public void onMessage(javax.jms.Message message)
	{
		try {
			if (!(message instanceof ObjectMessage)) {
				logger.error("Message is not an instance of ObjectMessage: " + message);
				return;
			}

			Object obj = ((ObjectMessage)message).getObject();
			if (!(obj instanceof AsyncInvokeEnvelope)) {
				logger.error("Object wrapped in ObjectMessage is not an instance of AsyncInvokeEnvelope: " + message);
				return;
			}

			InitialContext initCtxNotAuthenticated = new InitialContext();

			logger.info("*****************************************************************");
			logger.info("*****************************************************************");
			logger.info("*****************************************************************");

			// we need to wait for the system to be up, ready and running
			// wait max 5 min for the JFireServerManagerFactory to pop up in JNDI
			long startDT = System.currentTimeMillis();
			JFireServerManagerFactory ismf = null;
			do {
				logger.info("-----------------------------------------------------------------");

				if (System.currentTimeMillis() - startDT > 5 * 60 * 1000)
					throw new IllegalStateException("JFireServerManagerFactory did not pop up in JNDI within timeout (hardcoded 5 min)!");

				try {
					ismf = (JFireServerManagerFactory) initCtxNotAuthenticated.lookup(JFireServerManagerFactory.JNDI_NAME);
				} catch (NameNotFoundException x) {
					// ignore
					ismf = null;
				}

				if (ismf == null)
					try { Thread.sleep(3000); } catch (InterruptedException x) { }

			} while (ismf == null);

			startDT = System.currentTimeMillis();
			do {
				logger.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

				if (System.currentTimeMillis() - startDT > 10 * 60 * 1000)
					throw new IllegalStateException("JFireServer did not start within timeout (hardcoded 10 min)!");

				if (!ismf.isUpAndRunning())
					try { Thread.sleep(3000); } catch (InterruptedException x) { }

			} while (!ismf.isUpAndRunning());

			logger.info("*****************************************************************");
			logger.info("*****************************************************************");

			AsyncInvokeEnvelope envelope = (AsyncInvokeEnvelope) obj;

			Invocation invocation = envelope.getInvocation();

			SecurityReflector.UserDescriptor caller = envelope.getCaller();
			LoginContext loginContext;
			JFireServerManager ism = ismf.getJFireServerManager();
			try {
				loginContext = new LoginContext(
						"ipanema", new AuthCallbackHandler(ism, envelope));

//				Hashtable props = new Properties();
//				String initialContextFactory = ismf.getInitialContextFactory(localServer.getJ2eeServerType(), true);
//				props.put(InitialContext.INITIAL_CONTEXT_FACTORY, initialContextFactory);
//				props.put(InitialContext.PROVIDER_URL, localServer.getInitialContextURL());
//				props.put(InitialContext.SECURITY_PRINCIPAL, caller.getUserID() + '@' + caller.getOrganisationID());
//				props.put(InitialContext.SECURITY_CREDENTIALS, ism.ipanemaSecurity_createTempUserPassword(caller.getOrganisationID(), caller.getUserID()));
//				props.put(InitialContext.SECURITY_PROTOCOL, "ipanema");

				loginContext.login();
				try {
					AsyncInvokerDelegateLocal invokerDelegate = null;

					try {
						invokerDelegate = AsyncInvokerDelegateUtil.getLocalHome().create();
					} catch (Exception x) {
						logger.fatal("Obtaining stateless session bean AsyncInvokerDelegateLocal failed!", x);
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

		} catch (Throwable x) {
			logger.fatal("Processing message failed!", x);
			messageContext.setRollbackOnly();
		}
	}

	protected abstract void doInvoke(AsyncInvokeEnvelope envelope, AsyncInvokerDelegateLocal invokerDelegate);
}
