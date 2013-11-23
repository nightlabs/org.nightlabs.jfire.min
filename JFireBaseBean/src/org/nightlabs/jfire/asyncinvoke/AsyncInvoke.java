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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapterException;
import org.nightlabs.jfire.servermanager.j2ee.JMSConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is your entry point for asynchronous method invocation. Simply call one of
 * the <tt>exec(...)</tt> methods.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class AsyncInvoke
{
	private static Logger logger = LoggerFactory.getLogger(AsyncInvoke.class);

	protected static final String QUEUE_INVOCATION = "queue/jfire/JFireBaseBean/AsyncInvokerInvocationQueue";
	protected static final String QUEUE_SUCCESSCALLBACK = "queue/jfire/JFireBaseBean/AsyncInvokerSuccessCallbackQueue";
	protected static final String QUEUE_ERRORCALLBACK = "queue/jfire/JFireBaseBean/AsyncInvokerErrorCallbackQueue";
	protected static final String QUEUE_UNDELIVERABLECALLBACK = "queue/jfire/JFireBaseBean/AsyncInvokerUndeliverableCallbackQueue";

	//	private InitialContext initialContext = null;

	protected AsyncInvoke()
	{
	}

	/**
	 * This is a convenience method which calls {@link #asyncInvoke(Invocation, SuccessCallback, ErrorCallback, UndeliverableCallback)}.
	 * @param enableXA TODO
	 * @throws AsyncInvokeEnqueueException If creating the async invoke envelope or enqueueing the invokation failed
	 */
	public static void exec(Invocation invocation, boolean enableXA)
	throws AsyncInvokeEnqueueException
	{
		exec(invocation, null, null, null, enableXA);
	}

	/**
	 * This is a convenience method which calls {@link #asyncInvoke(Invocation, SuccessCallback, ErrorCallback, UndeliverableCallback)}.
	 * @param enableXA TODO
	 * @throws AsyncInvokeEnqueueException If creating the async invoke envelope or enqueueing the invokation failed
	 */
	public static void exec(
			Invocation invocation, SuccessCallback successCallback, boolean enableXA)
	throws AsyncInvokeEnqueueException
	{
		exec(invocation, successCallback, null, null, enableXA);
	}

	/**
	 * This is a convenience method which calls {@link #asyncInvoke(Invocation, SuccessCallback, ErrorCallback, UndeliverableCallback)}.
	 * @param enableXA TODO
	 * @throws AsyncInvokeEnqueueException If creating the async invoke envelope or enqueueing the invokation failed
	 */
	public static void exec(
			Invocation invocation, ErrorCallback errorCallback, boolean enableXA)
	throws AsyncInvokeEnqueueException
	{
		exec(invocation, null, errorCallback, null, enableXA);
	}

	/**
	 * This is a convenience method which calls {@link #asyncInvoke(Invocation, SuccessCallback, ErrorCallback, UndeliverableCallback)}.
	 * @param enableXA TODO
	 * @throws AsyncInvokeEnqueueException If creating the async invoke envelope or enqueueing the invokation failed
	 */
	public static void exec(
			Invocation invocation, UndeliverableCallback undeliverableCallback, boolean enableXA)
	throws AsyncInvokeEnqueueException
	{
		exec(invocation, null, null, undeliverableCallback, enableXA);
	}

	/**
	 * @param invocation The <tt>Invocation</tt> that should be done asynchronously.
	 * <tt>invocation</tt> MUST NOT be <tt>null</tt>.
	 * @param successCallback An optional callback that is executed in a separate asynchronous
	 * transaction (means a failure does not affect the previous <tt>Invocation</tt>). To include
	 * your success callback in the same XATransaction as the invocation, leave this param <tt>null</tt>
	 * and do your callback directly in <tt>invocation</tt>. <tt>successCallback</tt> might be <tt>null</tt>.
	 * @param errorCallback An optional callback that is triggered every time the <tt>invocation</tt>
	 * failed. It is executed asynchronously within a separate XATransaction (unlinked to the
	 * <tt>invocation</tt> which is a necessity as the original XATransaction will be rolled back in
	 * case of an error). <tt>errorCallback</tt> might be <tt>null</tt>.
	 * @param undeliverableCallback After a certain number of failed invocation retries, the <tt>invocation</tt>
	 * is given up and this callback triggered. The default configuration is to retry an <tt>invocation</tt> once
	 * per hour and to give up after a month (31 days).  <tt>undeliverableCallback</tt> might be <tt>null</tt>.
	 * @param enableXA TODO
	 * @param enableXA Whether or not to use an XA transaction and thus hook into the currently active global transaction <i>GT</i>.
	 *		If this is <code>true</code>, the message will not be processed (and thus the invocation occur), before the
	 *		<i>GT</i> is committed. If <code>GT</code> is rolled back, the invocation will never occur.
	 *		If <code>enableXA</code> is <code>false</code>, the <code>envelope</code> will be enqueued without a transaction
	 *		and thus, processing starts immediately - probably before the <i>GT</i> is complete. This will cause
	 *		problems, if the asynchronously called code requires JDO objects written/manipulated within <i>GT</i>.
	 * @throws AsyncInvokeEnqueueException If creating the async invoke envelope or enqueueing the invokation failed
	 */
	public static void exec(
			Invocation invocation, SuccessCallback successCallback,
			ErrorCallback errorCallback, UndeliverableCallback undeliverableCallback, boolean enableXA)
	throws AsyncInvokeEnqueueException
	{
		AsyncInvokeEnvelope envelope;
		try {
			envelope = new AsyncInvokeEnvelope(
					invocation, successCallback, errorCallback, undeliverableCallback);
		} catch (NamingException e) {
			throw new AsyncInvokeEnqueueException(e);
		}
		enqueue(QUEUE_INVOCATION, envelope, enableXA);
	}

//	protected static class AuthCallbackHandler implements CallbackHandler
//	{
//		private String userName;
//		private char[] password;
//
//		public AuthCallbackHandler(String userName, char[] password) {
//			this.userName = userName;
//			this.password = password;
//		}
//
//		@Override
//		public void handle(Callback[] callbacks)
//		throws IOException,
//		UnsupportedCallbackException
//		{
//			for (int i = 0; i < callbacks.length; ++i) {
//				Callback cb = callbacks[i];
//				if (cb instanceof NameCallback) {
//					((NameCallback)cb).setName(userName);
//				}
//				else if (cb instanceof PasswordCallback) {
//					((PasswordCallback)cb).setPassword(password);
//				}
//				else throw new UnsupportedCallbackException(cb);
//			}
//		}
//	}

	private static Map<String, Destination> jndiName2Queue = Collections.synchronizedMap(new HashMap<String, Destination>());

	private static void _enqueue(J2EEAdapter j2eeAdapter, String queueJNDIName, AsyncInvokeEnvelope envelope, boolean enableXA)
	throws NamingException, J2EEAdapterException, JMSException
	{
		JMSConnection jmsConnection = j2eeAdapter.createJMSConnection(enableXA, Session.AUTO_ACKNOWLEDGE);
		try {
			Message message = jmsConnection.getSession().createObjectMessage(envelope);

			Destination destination = jndiName2Queue.get(queueJNDIName);
			if (destination == null) {
				destination = jmsConnection.getDestination(queueJNDIName);
				jndiName2Queue.put(queueJNDIName, destination);
			}

			MessageProducer producer = jmsConnection.getSession().createProducer(destination);
			producer.send(message);
		} finally {
			jmsConnection.close();
		}
	}


	/**
	 * @param queueJNDIName The queue into which the <code>envelope</code> should be enqueued. This should be one
	 *		of: {@link #QUEUE_INVOCATION}, {@link #QUEUE_SUCCESSCALLBACK}, {@link #QUEUE_ERRORCALLBACK}, {@link #QUEUE_UNDELIVERABLECALLBACK}
	 * @param envelope The envelope containing all the AsyncInvoke data.
	 * @param enableXA Whether or not to use an XA transaction and thus hook into the currently active global transaction <i>GT</i>.
	 *		If this is <code>true</code>, the message will not be processed (and thus the invocation occur), before the
	 *		<i>GT</i> is committed. If <code>GT</code> is rolled back, the invocation will never occur.
	 *		If <code>enableXA</code> is <code>false</code>, the <code>envelope</code> will be enqueued without a transaction
	 *		and thus, processing starts immediately - probably before the <i>GT</i> is complete. This will cause
	 *		problems, if the asynchronously called code requires JDO objects written/manipulated within <i>GT</i>.
	 * @throws AsyncInvokeEnqueueException If enqueueing the invokation failed
	 */
	protected static void enqueue(String queueJNDIName, AsyncInvokeEnvelope envelope, boolean enableXA)
	throws AsyncInvokeEnqueueException
	{
		try {
			InitialContext localContext = new InitialContext();
			try {
				J2EEAdapter j2eeAdapter = (J2EEAdapter) localContext.lookup(J2EEAdapter.JNDI_NAME);

				try {
					_enqueue(j2eeAdapter, queueJNDIName, envelope, enableXA);
				} catch (Exception x) {
					logger.warn("enqueue: Failed (but I will retry again): " + x, x);
					jndiName2Queue.remove(queueJNDIName);
					_enqueue(j2eeAdapter, queueJNDIName, envelope, enableXA);
				}

			} finally {
				localContext.close();
			}
		} catch(Exception e) {
			throw new AsyncInvokeEnqueueException(e);
		}

//		try {
//			// Cluster and JBoss specific. This must be configurable!!!
//			Properties p = new Properties();
//			p.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
//			p.put(Context.URL_PKG_PREFIXES, "jboss.naming:org.jnp.interfaces");
//			p.put(Context.PROVIDER_URL, "localhost:1100"); // HA-JNDI port.
//			InitialContext initialContext = new InitialContext(p);
//
////			InitialContext initialContext = new InitialContext();
//			try {
//				InitialContext localContext = new InitialContext();
//				J2EEAdapter j2eeAdapter = (J2EEAdapter) localContext.lookup(J2EEAdapter.JNDI_NAME);
////				JFireServerLocalLoginManager m = JFireServerLocalLoginManager.getJFireServerLocalLoginManager(localContext);
////
////				AuthCallbackHandler mqCallbackHandler = new AuthCallbackHandler(
////						JFireServerLocalLoginManager.PRINCIPAL_LOCALQUEUEWRITER,
////						m.getPrincipal(JFireServerLocalLoginManager.PRINCIPAL_LOCALQUEUEWRITER).getPassword().toCharArray());
////
////				LoginContext loginContext = new LoginContext("jfireLocal", mqCallbackHandler);
//				LoginContext loginContext = j2eeAdapter.jms_createLoginContext();
//				loginContext.login();
//				try {
//					ConnectionFactory connectionFactory;
//					if (enableXA) {
//						// TODO "java:/JmsXA" should be configurable?!
//						connectionFactory = (ConnectionFactory) localContext.lookup("java:/JmsXA");
////						connectionFactory = (ConnectionFactory) initialContext.lookup("XAConnectionFactory"); // This does not work :-(
//					}
//					else {
//						// TODO "ConnectionFactory" should be configurable?!
//						connectionFactory = (QueueConnectionFactory) initialContext.lookup("ConnectionFactory");
//					}
//
//					Connection connection = null;
//					Session session = null;
//					MessageProducer sender = null;
//					try {
//						Queue queue = (Queue) initialContext.lookup(queueJNDIName);
//						connection = connectionFactory.createConnection();
//						session = connection.createSession(enableXA, Session.AUTO_ACKNOWLEDGE);
//						sender = session.createProducer(queue);
//
//						Message message = session.createObjectMessage(envelope);
//						sender.send(message);
//					} finally {
//						if (sender != null) try { sender.close(); } catch (Exception e) {
//							logger.warn("sender.close() failed: " + e, e);
//						}
//						if (session != null) try { session.close(); } catch (Exception e) {
//							logger.warn("session.close() failed: " + e, e);
//						}
//						if (connection != null) try { connection.close(); } catch (Exception e) {
//							logger.warn("connection.close() failed: " + e, e);
//						}
//					}
////					}
////					else {
//////						QueueConnectionFactory connectionFactory = (QueueConnectionFactory) initialContext.lookup("UIL2ConnectionFactory"); // TODO switch to normal ConnectionFactory below? maybe more portable? or make this configurable?
////						connectionFactory = (QueueConnectionFactory) initialContext.lookup("ConnectionFactory");
////
////						QueueConnection connection = null;
////						QueueSession session = null;
////						QueueSender sender = null;
////
////						try {
////							connection = connectionFactory.createQueueConnection();
////							session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
////							//					session = connection.createQueueSession(true, Session.AUTO_ACKNOWLEDGE); // transacted = true
////							Queue queue = (Queue) initialContext.lookup(queueJNDIName);
////							sender = session.createSender(queue);
////
////							Message message = session.createObjectMessage(envelope);
////							sender.send(message);
////						} finally {
////							if (sender != null) try { sender.close(); } catch (Exception ignore) { }
////							if (session != null) try { session.close(); } catch (Exception ignore) { }
////							if (connection != null) try { connection.close(); } catch (Exception ignore) { }
////						}
////					}
//				} finally {
//					loginContext.logout();
//				}
//				localContext.close();
//			} finally {
//				initialContext.close();
//			}
//		} catch(Exception e) {
//			throw new AsyncInvokeEnqueueException(e);
//		}
	}
}
