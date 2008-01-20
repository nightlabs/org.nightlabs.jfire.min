/**
 * 
 */
package org.nightlabs.jfire.jboss.j2ee.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.servermanager.j2ee.JMSConnectionFactoryLookup;
import org.nightlabs.jfire.servermanager.j2ee.monitor.J2EEServerMonitor;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class J2EEServerMonitorJBoss implements J2EEServerMonitor {

	/**
	 * Log4J Logger for {@link J2EEServerMonitorJBoss}.
	 */
	private static final Logger logger = Logger.getLogger(J2EEServerMonitorJBoss.class);
	
	/**
	 * 
	 */
	public J2EEServerMonitorJBoss() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Message> listQueueMessages(Queue queue)  throws NamingException, JMSException {
		List<Message> msgs = new ArrayList<Message>();
		InitialContext initialContext = SecurityReflector.createInitialContext();
		try {
			QueueConnectionFactory connectionFactory = JMSConnectionFactoryLookup.lookupQueueConnectionFactory(initialContext);
			Connection connection = connectionFactory.createConnection();
			try {
//				connection.start();
				Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
				try {
					QueueBrowser queueBrowser = session.createBrowser(queue);
					try {
						//				session.setMessageListener(arg0)
						//				SpySession
						Enumeration<?> en = queueBrowser.getEnumeration();
						while (en.hasMoreElements()) {
							Object el = en.nextElement();
							if (el instanceof Message) {
								msgs.add((Message) el);
							}
						}
					} finally {
						queueBrowser.close();
					}
				} finally {
					session.close();
				}
			} finally {
				connection.close();
			}
		} finally {
			initialContext.close();
		}
		return msgs;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Queue> listQueues() throws NamingException, JMSException {
		logger.debug("Try getting queues");
		Collection<Queue> result = new LinkedList<Queue>();
		InitialContext ic = SecurityReflector.createInitialContext();
		try {
			listQueues(ic, "", result);
		} finally {
			ic.close();
		}
		return result;
	}

	private void listQueues(InitialContext ic, String base, Collection<Queue> queues) throws NamingException {
		logger.debug("Listing for " + base);
		NamingEnumeration<NameClassPair> ne = null;
		try {
			ne = ic.list(base);
		} catch (NameNotFoundException e) {
			logger.debug("Listing failed for " + base + ", probably not really bound ...");
			return;
		} catch (Exception ex) {
//			logger.error("Listing failed for " + base + ", unknown error.", ex);
			return;
		}
		while(ne.hasMoreElements()){
			NameClassPair ncp = ne.nextElement();
			String lookupPath = base == null || "".equals(base) ? ncp.getName() : base + "/" + ncp.getName(); 
			logger.debug("Have NameClassPair: " + ncp.getName() + " " + ncp.getClassName());
			Object jndiObj = null;
			try {
				jndiObj = ic.lookup(lookupPath);
			} catch (NameNotFoundException e) {
				logger.debug("Lookup of " + ncp.getName() + " failed, probably not really bound ...");
			} catch (Exception ex) {
				logger.debug("Lookup of " + ncp.getName() + " failed, unknown error.", ex);
			}
			if (jndiObj instanceof Queue) {
				Queue queue = (Queue) jndiObj;
				queues.add(queue);
			} else {
				listQueues(ic, lookupPath, queues);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getQueueDepth(Queue queue) throws NamingException, JMSException {
		Collection<Message> msgs = listQueueMessages(queue);
		if (msgs == null)
			return 0;
		return msgs.size();
	}
	
}

