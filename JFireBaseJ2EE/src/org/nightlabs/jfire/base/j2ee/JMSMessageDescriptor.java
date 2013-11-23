/**
 * 
 */
package org.nightlabs.jfire.base.j2ee;

import java.io.Serializable;

/**
 * MessageDescriptor used to pass queue information to remote clients.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public interface JMSMessageDescriptor extends Serializable {

	/**
	 * @see javax.jms.Message#getJMSMessageID()
	 */
	String getJMSMessageID();
	/**
	 * @see javax.jms.Message#getJMSTimestamp()
	 */
	long getJMSTimeStamp();
	/**
	 * @see javax.jms.Message#getJMSDestination()
	 */
	String getJMSDestinationName();
	/**
	 * @see javax.jms.Message#getJMSDeliveryMode()
	 */
	int getJMSDeliveryMode();
	/**
	 * @see javax.jms.Message#getJMSRedelivered()
	 */
	boolean getJMSRedelivered();
	/**
	 * @see javax.jms.Message#getJMSType()
	 */
	String getJMSType();
	/**
	 * @see javax.jms.Message#getJMSExpiration()
	 */
	long getJMSExpiration();
	/**
	 * @see javax.jms.Message#getJMSPriority()
	 */
	int getJMSPriority();

	/**
	 * @return A description of the message (usually rendered by an {@link JMSMessageRenderer}.)
	 */
	String getDescription();
	
}
