/*
 * Created on Mar 23, 2005
 */
package org.nightlabs.jfire.servermanager.j2ee;

import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class JMSConnectionFactoryLookup
{
	public static String QUEUECF_JNDI_LINKNAME = "java:/jfire/system/QueueConnectionFactory";
	public static String TOPICCF_JNDI_LINKNAME = "java:/jfire/system/TopicConnectionFactory";

	public static QueueConnectionFactory lookupQueueConnectionFactory(InitialContext initialContext)
	throws NamingException
	{
		String realName = (String)initialContext.lookup(QUEUECF_JNDI_LINKNAME);
		return (QueueConnectionFactory) initialContext.lookup(realName);
	}

	public static TopicConnectionFactory lookupTopicConnectionFactory(InitialContext initialContext)
	throws NamingException
	{
		String realName = (String)initialContext.lookup(TOPICCF_JNDI_LINKNAME);
		return (TopicConnectionFactory) initialContext.lookup(realName);
	}
}
