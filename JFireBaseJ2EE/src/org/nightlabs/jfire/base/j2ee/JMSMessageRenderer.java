/**
 * 
 */
package org.nightlabs.jfire.base.j2ee;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public abstract class JMSMessageRenderer {

	public JMSMessageRenderer() {
	}

	public abstract boolean matches(Message message);
	
	public abstract String renderMessage(Message message) throws JMSException;
	
}
