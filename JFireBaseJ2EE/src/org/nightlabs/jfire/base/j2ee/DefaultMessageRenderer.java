/**
 * 
 */
package org.nightlabs.jfire.base.j2ee;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class DefaultMessageRenderer extends JMSMessageRenderer {

	/**
	 * 
	 */
	public DefaultMessageRenderer() {
	}

	/** {@inheritDoc}
	 * @see org.nightlabs.jfire.base.j2ee.JMSMessageRenderer#matches(javax.jms.Message)
	 */
	@Override
	public boolean matches(Message message) {
		return true;
	}

	/** {@inheritDoc}
	 * @throws JMSException
	 * @see org.nightlabs.jfire.base.j2ee.JMSMessageRenderer#renderMessage(javax.jms.Message)
	 */
	@Override
	public String renderMessage(Message message) throws JMSException {
		if (message instanceof BytesMessage) {
			return "BytesMessage: BodyLength = " + ((BytesMessage) message).getBodyLength();
		} else if (message instanceof MapMessage) {
			return "MapMessage: " + System.identityHashCode(message);
		} else if (message instanceof ObjectMessage) {
			return "ObjectMessage: ObjectType: " + ((ObjectMessage) message).getObject().getClass().getName();
		} else if (message instanceof MapMessage) {
			return "StreamMessage: " + System.identityHashCode(message);
		} else if (message instanceof TextMessage) {
			return "TextMessage: " + ((TextMessage) message).getText();
		}
		return "Message: " + System.identityHashCode(message);
	}

}
