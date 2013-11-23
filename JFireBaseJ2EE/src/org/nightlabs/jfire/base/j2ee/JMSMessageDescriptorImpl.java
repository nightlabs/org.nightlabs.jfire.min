package org.nightlabs.jfire.base.j2ee;

import java.util.ArrayList;
import java.util.Collection;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Topic;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class JMSMessageDescriptorImpl implements JMSMessageDescriptor {

	private static final long serialVersionUID = 1L;
	
	private String jmsMessageID;
	private long jmsTimeStamp;
	private String destinationName;
	private int jmsDeliveryMode;
	private boolean jmsRedelivered;
	private String jmsType;
	private long jmsExpiration;
	private int jmsPriority;
	
	private String description;
	
	public JMSMessageDescriptorImpl(Message message) throws JMSException {
		this.jmsMessageID = message.getJMSMessageID();
		this.jmsTimeStamp = message.getJMSTimestamp();
		if (message.getJMSDestination() instanceof Queue)
			this.destinationName = ((Queue)message.getJMSDestination()).getQueueName();
		else if (message.getJMSDestination() instanceof Topic)
			this.destinationName = ((Topic)message.getJMSDestination()).getTopicName();
		this.jmsDeliveryMode = message.getJMSDeliveryMode();
		this.jmsRedelivered = message.getJMSRedelivered();
		this.jmsType = message.getJMSType();
		this.jmsExpiration = message.getJMSExpiration();
		this.jmsPriority = message.getJMSPriority();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.j2ee.JMSMessageDescriptor#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.j2ee.JMSMessageDescriptor#getJMSDeliveryMode()
	 */
	@Override
	public int getJMSDeliveryMode() {
		return jmsDeliveryMode;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.j2ee.JMSMessageDescriptor#getJMSDestinationName()
	 */
	@Override
	public String getJMSDestinationName() {
		return destinationName;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.j2ee.JMSMessageDescriptor#getJMSExpiration()
	 */
	@Override
	public long getJMSExpiration() {
		return jmsExpiration;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.j2ee.JMSMessageDescriptor#getJMSMessageID()
	 */
	@Override
	public String getJMSMessageID() {
		return jmsMessageID;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.j2ee.JMSMessageDescriptor#getJMSTimeStamp()
	 */
	@Override
	public long getJMSTimeStamp() {
		return jmsTimeStamp;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.j2ee.JMSMessageDescriptor#getJMSPriority()
	 */
	@Override
	public int getJMSPriority() {
		return jmsPriority;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.j2ee.JMSMessageDescriptor#getJMSRedelivered()
	 */
	@Override
	public boolean getJMSRedelivered() {
		return jmsRedelivered;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.j2ee.JMSMessageDescriptor#getJMSType()
	 */
	@Override
	public String getJMSType() {
		return jmsType;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param destinationName the destinationName to set
	 */
	public void setDestinationName(String destinationName) {
		this.destinationName = destinationName;
	}

	/**
	 * @param jmsDeliveryMode the jmsDeliveryMode to set
	 */
	public void setJMSDeliveryMode(int jmsDeliveryMode) {
		this.jmsDeliveryMode = jmsDeliveryMode;
	}

	/**
	 * @param jmsExpiration the jmsExpiration to set
	 */
	public void setJMSExpiration(long jmsExpiration) {
		this.jmsExpiration = jmsExpiration;
	}

	/**
	 * @param jmsMessageID the jmsMessageID to set
	 */
	public void setJMSMessageID(String jmsMessageID) {
		this.jmsMessageID = jmsMessageID;
	}

	/**
	 * @param jmsMessageTimeStamp the jmsMessageTimeStamp to set
	 */
	public void setJMSTimeStamp(long jmsMessageTimeStamp) {
		this.jmsTimeStamp = jmsMessageTimeStamp;
	}

	/**
	 * @param jmsPriority the jmsPriority to set
	 */
	public void setJMSPriority(int jmsPriority) {
		this.jmsPriority = jmsPriority;
	}

	/**
	 * @param jmsRedelivered the jmsRedelivered to set
	 */
	public void setJMSRedelivered(boolean jmsRedelivered) {
		this.jmsRedelivered = jmsRedelivered;
	}

	/**
	 * @param jmsType the jmsType to set
	 */
	public void setJMSType(String jmsType) {
		this.jmsType = jmsType;
	}
	
	public static Collection<JMSMessageDescriptor> getMessageDescriptors(Collection<Message> msgs, Collection<JMSMessageRenderer> renderers)
	throws JMSException
	{
		Collection<JMSMessageDescriptor> result = new ArrayList<JMSMessageDescriptor>(msgs.size());
		DefaultMessageRenderer defRenderer = new DefaultMessageRenderer();
		for (Message message : msgs) {
			JMSMessageDescriptorImpl descriptor = new JMSMessageDescriptorImpl(message);
			result.add(descriptor);
			for (JMSMessageRenderer renderer : renderers) {
				if (renderer.matches(message)) {
					descriptor.setDescription(renderer.renderMessage(message));
					break;
				}
			}
			descriptor.setDescription(defRenderer.renderMessage(message));
		}
		return result;
	}
}
