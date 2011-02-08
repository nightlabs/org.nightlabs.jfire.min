package org.nightlabs.jfire.jdo.cache.cluster;

import java.util.Map;
import java.util.UUID;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.nightlabs.jfire.jdo.cache.CacheManagerFactory;
import org.nightlabs.jfire.jdo.cache.LocalDirtyEvent;
import org.nightlabs.jfire.jdo.cache.LocalDirtyListener;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapterException;
import org.nightlabs.jfire.servermanager.j2ee.JMSConnection;
import org.nightlabs.util.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>DirtyObjectIDPropagator</code> propagates {@link DirtyObjectID}s across all cluster nodes.
 * If the server is running in {@link J2EEAdapter#isInCluster() cluster-mode}, the {@link CacheManagerFactory}
 * creates an instance of this class, otherwise it is unused.
 * <p>
 * The JDO runtime propagates lifecycle events only within one JVM. Thus, a separate cluster node is not
 * notified about objects becoming dirty and therefore cannot notify its own clients. This problem is
 * solved by this class.
 * </p>
 * <p>
 * This class uses the Java Messaging System (JMS) to notify all cluster nodes.
 * </p>
 *
 * @author marco
 */
public class DirtyObjectIDPropagator
{
	private Logger logger = LoggerFactory.getLogger(DirtyObjectIDPropagator.class);

	protected static final String TOPIC_JNDI = "topic/jfire/JFireBaseBean/RawDirtyObjectIDs";

	private CacheManagerFactory cacheManagerFactory;

	public DirtyObjectIDPropagator(CacheManagerFactory cacheManagerFactory) {
		if (cacheManagerFactory == null)
			throw new IllegalArgumentException("cacheManagerFactory == null");

		this.cacheManagerFactory = cacheManagerFactory;

		cacheManagerFactory.addLocalDirtyListener(localDirtyListener);
		jmsConsumerThread = new JMSConsumerThread();
	}

	private Thread jmsConsumerThread;

	private class JMSConsumerThread extends Thread
	{
		public JMSConsumerThread() {
			setDaemon(true);
			start();
		}

		private volatile boolean forcedInterrupt = false;

		@Override
		public void interrupt() {
			forcedInterrupt = true;
			super.interrupt();
		}

		@Override
		public boolean isInterrupted() {
			return forcedInterrupt || super.isInterrupted();
		}

		@Override
		public void run() {
			while (!isInterrupted()) {
				try {
					InitialContext localContext = new InitialContext();
					try {
						J2EEAdapter j2eeAdapter = (J2EEAdapter) localContext.lookup(J2EEAdapter.JNDI_NAME);
						JMSConnection jmsConnection = j2eeAdapter.createJMSConnection(false, Session.AUTO_ACKNOWLEDGE);
						try {
							Connection connection = jmsConnection.getConnection();
							connection.start(); // without this, we do not receive any message!
							try {
								Destination destination = jmsConnection.getDestination(TOPIC_JNDI);
								MessageConsumer messageConsumer = jmsConnection.getSession().createConsumer(destination);
								while (!isInterrupted()) {
									Message message = messageConsumer.receive(10000);
									if (message != null)
										onMessage(message);
								}
							} finally {
								connection.stop();
							}
						} finally {
							jmsConnection.close();
						}
					} finally {
						localContext.close();
					}
				} catch (Exception x) {
					logger.error("JMSConsumerThread.run: " + x, x);
				}
			}
		}
	}

	private void onMessage(Message message) {
		logger.debug("onMessage: message = {}", message);
		try {

			if (message instanceof ObjectMessage) {
				Object o = ((ObjectMessage) message).getObject();
				if (o instanceof LocalDirtyEvent) {
					onMessage((LocalDirtyEvent) o);
				}
				else
					logger.error("onMessage: message.object.class = {}", (o == null ? null : o.getClass()));

			}
			else
				logger.error("onMessage: message.class = {}", (message == null ? null : message.getClass()));

		} catch (Exception e) {
			logger.error("onMessage: " + e, e);
			throw new RuntimeException(e); // rethrow to trigger redelivery mechanism
		}
	}

	private void onMessage(LocalDirtyEvent event)
	throws Exception
	{
		if (event == null)
			throw new IllegalArgumentException("event == null");

		if (logger.isDebugEnabled()) {
			logger.debug("onMessage: event.cacheManagerFactoryID = {}", event.getCacheManagerFactoryID());
			logger.debug("onMessage: event.organisationID = {}", event.getOrganisationID());
			logger.debug("onMessage: event.sessionID = {}", event.getSessionID());

			if (logger.isTraceEnabled()) {
				for (Map.Entry<JDOLifecycleState, Map<Object, DirtyObjectID>> me1 : event.getDirtyObjectIDs().entrySet()) {
					for (Map.Entry<Object, DirtyObjectID> me2 : me1.getValue().entrySet()) {
						logger.trace("onMessage: event.dirtyObjectID = {}", me2.getValue());
					}
				}
			}
		}

		if (cacheManagerFactory.getCacheManagerFactoryID().equals(event.getCacheManagerFactoryID())) {
			logger.debug("onMessage: Skipping event, because it originates from our CacheManagerFactory (and should not be sent there again).");
			return;
		}

		event.setProperty(DirtyObjectIDPropagator.EVENT_PROPERTY_FROM_JMS, Boolean.TRUE.toString());

		cacheManagerFactory.addDirtyObjectIDs(event);
	}


	public static final String EVENT_PROPERTY_FROM_JMS =  DirtyObjectIDPropagator.class.getName() + ".fromJMS";

	private static volatile Destination destination = null;

	private static void _send(J2EEAdapter j2eeAdapter, LocalDirtyEvent event, Stopwatch stopwatch)
	throws NamingException, J2EEAdapterException, JMSException
	{
		stopwatch.start("10.createJMSConnection");
		JMSConnection jmsConnection = j2eeAdapter.createJMSConnection(false, Session.AUTO_ACKNOWLEDGE);
		stopwatch.stop("10.createJMSConnection");
		try {
			stopwatch.start("15.getSession");
			jmsConnection.getSession();
			stopwatch.stop("15.getSession");

			stopwatch.start("20.createMessage");
			Message message = jmsConnection.getSession().createObjectMessage(event);
			stopwatch.stop("20.createMessage");

			stopwatch.start("30.getDestination");
			Destination d = destination;
			if (d == null) {
				d = jmsConnection.getDestination(TOPIC_JNDI);
				destination = d;
			}
			stopwatch.stop("30.getDestination");

			stopwatch.start("40.createProducer");
			MessageProducer producer = jmsConnection.getSession().createProducer(d);
			stopwatch.stop("40.createProducer");

			stopwatch.start("50.producer_send");
			producer.send(message);
			stopwatch.stop("50.producer_send");
		} finally {
			stopwatch.start("90.jmsConnection_close");
			jmsConnection.close();
			stopwatch.stop("90.jmsConnection_close");
		}
	}

	private LocalDirtyListener localDirtyListener = new LocalDirtyListener()
	{
		@Override
		public void notifyDirtyObjectIDs(LocalDirtyEvent event) {
			UUID myCacheManagerFactoryID = cacheManagerFactory.getCacheManagerFactoryID();

			logger.debug("localDirtyListener.notifyDirtyObjectIDs: myCacheManagerFactoryID = {}", myCacheManagerFactoryID);
			logger.debug("localDirtyListener.notifyDirtyObjectIDs: event.cacheManagerFactoryID = {}", event.getCacheManagerFactoryID());

			if (event.getProperty(EVENT_PROPERTY_FROM_JMS) != null) {
				logger.debug("localDirtyListener.notifyDirtyObjectIDs: Skipping event, because we received it from the messaging system (and should not send it back again).");
				return;
			}

			Stopwatch stopwatch = new Stopwatch();
			try {
				InitialContext localContext = new InitialContext();
				try {
					stopwatch.start("00.lookupJ2EEAdapter");
					J2EEAdapter j2eeAdapter = (J2EEAdapter) localContext.lookup(J2EEAdapter.JNDI_NAME);
					stopwatch.stop("00.lookupJ2EEAdapter");

					stopwatch.start("05._send");
					try {
						_send(j2eeAdapter, event, stopwatch);
					} catch (Exception x) {
						logger.warn("localDirtyListener.notifyDirtyObjectIDs: Failed (but I will retry again): " + x, x);
						destination = null;
						_send(j2eeAdapter, event, stopwatch);
					}
					stopwatch.stop("05._send");

					logger.debug(
							"localDirtyListener.notifyDirtyObjectIDs: myCacheManagerFactoryID = {} {}",
							new Object[] { myCacheManagerFactoryID, stopwatch.createHumanReport(true) }
					);

				} finally {
					localContext.close();
				}
			} catch (Throwable e) {
				logger.error("localDirtyListener.notifyDirtyObjectIDs: " + e, e);
			}
		}
	};

	public void close() {
		if (jmsConsumerThread != null) {
			jmsConsumerThread.interrupt();
			jmsConsumerThread = null;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		close();
	}
}
