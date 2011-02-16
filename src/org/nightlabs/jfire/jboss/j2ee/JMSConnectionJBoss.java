package org.nightlabs.jfire.jboss.j2ee;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.QueueConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.nightlabs.jfire.servermanager.j2ee.AbstractJMSConnection;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter;

public class JMSConnectionJBoss extends AbstractJMSConnection
{
	protected JMSConnectionJBoss(J2EEAdapter j2eeAdapter, boolean transacted, int acknowledgeMode) {
		super(j2eeAdapter, transacted, acknowledgeMode);
	}

//	@Override
//	protected InitialContext _createInitialContext() throws NamingException {
//		if (getJ2EEAdapter().isInCluster()) {
//			Properties p = new Properties();
//			p.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
//			p.put(Context.URL_PKG_PREFIXES, "jboss.naming:org.jnp.interfaces");
//			p.put(Context.PROVIDER_URL, "localhost:1100"); // HA-JNDI port.
//			return new InitialContext(p);
//		}
//		else
//			return new InitialContext();
//	}

	@Override
	protected ConnectionFactory _getConnectionFactory()
	throws NamingException, JMSException
	{
		ConnectionFactory connectionFactory;
		if (isTransacted()) {
			InitialContext localContext = new InitialContext();
			connectionFactory = (ConnectionFactory) localContext.lookup("java:/JmsXA");
//			connectionFactory = (ConnectionFactory) initialContext.lookup("XAConnectionFactory"); // This does not work :-(
		}
		else {
			InitialContext initialContext = getInitialContext();
			connectionFactory = (QueueConnectionFactory) initialContext.lookup("ConnectionFactory");
		}
		return connectionFactory;
	}

}
