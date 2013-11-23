package org.nightlabs.jfire.servermanager.j2ee;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.NamingException;

/**
 * Single-threaded high-level API to work with JMS (hiding the JEE-server-implementation-specific stuff).
 * Note, that you must call {@link #close()}!!!
 *
 * @author marco
 */
public interface JMSConnection
{
	J2EEAdapter getJ2EEAdapter();

	/**
	 * Is a transactional context used with JMS? See {@link Connection#createSession(boolean, int)}.
	 *
	 * @return whether this connection works in a transactional context with JMS.
	 */
	boolean isTransacted();

	int getAcknowledgeMode();

//	LoginContext createLoginContext() throws J2EEAdapterException;
//
//	ConnectionFactory getConnectionFactory(boolean withXA)
//	throws J2EEAdapterException;
//
//	Queue getQueue(String queueName)
//	throws J2EEAdapterException;
//
//	Topic getTopic(String topicName)
//	throws J2EEAdapterException;

	/**
	 * Close this connection and release all corresponding resources. After this method
	 * was called, all other methods fail with an {@link IllegalStateException}. This method
	 * can be called multiple times (the second and all following calls have no effect at all).
	 */
	void close();

	Connection getConnection() throws NamingException, JMSException;

	/**
	 * Get a {@link Session}. This is single-threaded object!
	 *
	 * @return a <code>Session</code>.
	 */
	Session getSession() throws NamingException, JMSException;

	/**
	 * Get a {@link Destination}, i.e. usually a {@link Queue} or a {@link Topic}. This is not single-threaded
	 * and can thus be shared across transactions.
	 *
	 * @param jndiName the name of the queue/topic in the JNDI. This can be a full path with slashes.
	 * @return the object found in JNDI.
	 * @throws NamingException
	 * @throws JMSException
	 */
	Destination getDestination(String jndiName) throws NamingException, JMSException;

//	/**
//	 * Get a {@link MessageProducer} for the queue or topic denoted by <code>jndiName</code>.
//	 * <p>
//	 * Multiple calls to this method should return the same instance.
//	 * </p>
//	 *
//	 * @param jndiName the name of the queue or topic in the JNDI.
//	 * @return a {@link MessageProducer} which can be used to write into the specified queue/topic.
//	 */
//	MessageProducer getProducer(String jndiName) throws NamingException, JMSException;

}
