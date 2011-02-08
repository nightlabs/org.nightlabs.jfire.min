package org.nightlabs.jfire.servermanager.j2ee;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJMSConnection
implements JMSConnection
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractJMSConnection.class);

	protected AbstractJMSConnection(J2EEAdapter j2eeAdapter, boolean transacted, int acknowledgeMode) {
		if (j2eeAdapter == null)
			throw new IllegalArgumentException("j2eeAdapter == null");

		this.j2eeAdapter = j2eeAdapter;
		this.transacted = transacted;
		this.acknowledgeMode = acknowledgeMode;
	}

	private J2EEAdapter j2eeAdapter;

	private boolean transacted = true;
	private int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;

	boolean closed = false;

	private static volatile ConnectionFactory connectionFactory; // I read that the lookup from JNDI can take a while in a cluster environment and caching is recommended. Can I cache it this way, i.e. statically? See the code in #createConnection()
	private Connection connection;
//	private Map<String, MessageProducer> jndiName2producer = new HashMap<String, MessageProducer>();

	private Session session;

	@Override
	public J2EEAdapter getJ2EEAdapter() {
		return j2eeAdapter;
	}

	@Override
	public boolean isTransacted() {
		return transacted;
	}

	@Override
	public int getAcknowledgeMode() {
		return acknowledgeMode;
	}

	protected abstract ConnectionFactory _getConnectionFactory() throws NamingException, JMSException;

	protected final ConnectionFactory getConnectionFactory() throws NamingException, JMSException
	{
		assertNotClosed();

		if (connectionFactory == null)
			connectionFactory = _getConnectionFactory();

		return connectionFactory;
	}

	@Override
	public Connection getConnection() throws NamingException, JMSException
	{
		if (connection == null)
			connection = createConnection();

		return connection;
	}

	protected Connection createConnection()
	throws NamingException, JMSException
	{
		// Obtain the ConnectionFactory outside - thus, if it's not yet cached and obtaining it from JNDI fails,
		// we immediately escalate (without retrying).
		ConnectionFactory cf = getConnectionFactory();

		try {
			Connection connection = cf.createConnection();
			return connection;
		} catch (Exception x) {
			logger.warn("createConnection: Failed (but I will retry again): " + x, x);

			// If obtaining the connection failed, we clear our cached ConnectionFactory and try it again, before escalating.
			connectionFactory = null;

			return getConnectionFactory().createConnection();
		}
	}

	protected void assertNotClosed()
	{
		if (closed)
			throw new IllegalStateException("This JMSConnection is already closed!");
	}

	@Override
	public void close()
	{
		closed = true;

		// It is not necessary to explicitely close the session, if we close the Connection (what we do below).
		// But nevertheless, it doesn't hurt ;-)
		if (session != null) try { session.close(); } catch (Exception e) {
			logger.warn("session.close() failed: " + e, e);
		}
		session = null;

		if (connection != null) try { connection.close(); } catch (Exception e) {
			logger.warn("connection.close() failed: " + e, e);
		}
		connection = null;

		if (initialContext != null) try { initialContext.close(); } catch (Exception e) {
			logger.warn("initialContext.close() failed: " + e, e);
		}
		initialContext = null;

		connectionFactory = null;
	}

	/**
	 * Create an instance of {@link InitialContext} that is able to look up the
	 * JMS {@link Destination}s as well as the {@link ConnectionFactory} from JNDI.
	 * <p>
	 * The default implementation simply executes <code>new InitialContext()</code>,
	 * but you might need to override this method and configure your <code>InitialContext</code>
	 * (with some properties) in certain situations
	 * (e.g. when your JEE server provides a separate HA JNDI in a cluster and you
	 * cannot access the "normal" [i.e. local] JNDI).
	 * </p>
	 * @return the {@link InitialContext} used to look up JMS objects.
	 * @throws NamingException in case creating the {@link InitialContext} fails.
	 */
	protected InitialContext _createInitialContext() throws NamingException
	{
		return new InitialContext();
	}

	private InitialContext initialContext;

	protected InitialContext getInitialContext() throws NamingException
	{
		if (initialContext == null)
			initialContext = _createInitialContext();

		return initialContext;
	}

	private Map<String, Destination> jndiName2destination = new HashMap<String, Destination>();

	@Override
	public Destination getDestination(String jndiName) throws NamingException, JMSException
	{
		assertNotClosed();

		Destination destination = jndiName2destination.get(jndiName);
		if (destination == null) {
			destination = _getDestination(jndiName);
			jndiName2destination.put(jndiName, destination);
		}

		return destination;
	}

	protected Destination _getDestination(String jndiName) throws NamingException, JMSException
	{
		return (Destination) getInitialContext().lookup(jndiName);
	}

	@Override
	public Session getSession()
	throws NamingException, JMSException
	{
		assertNotClosed();

		if (session == null)
			session = getConnection().createSession(isTransacted(), acknowledgeMode);

		return session;
	}
}
