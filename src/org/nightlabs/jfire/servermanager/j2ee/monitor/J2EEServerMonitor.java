/**
 * 
 */
package org.nightlabs.jfire.servermanager.j2ee.monitor;

import java.util.Collection;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.naming.NamingException;

/**
 * A {@link J2EEServerMonitor} is used to query statistical data
 * from the application server like the messages in queues.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public interface J2EEServerMonitor {

	/**
	 * Lists the JMS {@link Queue}s known within this server.
	 * 
	 * @return A list of {@link Queue}s known within this server.
	 */
	Collection<Queue> listQueues() throws NamingException, JMSException;
	
	/**
	 * Lists all {@link Message} in the given {@link Queue}.
	 * 
	 * @param queue The {@link Queue} to find the messages for.
	 * @return All messages in the given Queue
	 */
	Collection<Message> listQueueMessages(Queue queue) throws NamingException, JMSException;
}
