package org.nightlabs.jfire.base.login;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.base.security.SecurityReflectorClient;
import org.nightlabs.jfire.security.SecurityReflector;

/**
 * {@link JFireClient} can be used to build a simple Java JFire client.
 * <p>
 * <b>Important: </b> As (if possible) {@link JFireClient} interacts with a singleton instance of {@link SecurityReflectorClient}
 * it is a bad idea to use more than one {@link JFireClient} in a Java VM.
 * </p>
 * <p>
 * To have a fully functional JFire client API you'll also need to call the method
 * {@link #registerDefaultStaticJFireClientClasses()} which will register the default client implementations
 * of some JFire server/client wide static API parts like the IDGenerator or the SecurityReflector.
 * </p>
 * <p>
 * {@link JFireLogin} is not intended to be used inside the JFire Server VM it should rather be used
 * when a client login is needed in Java.
 * </p>
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de --->
 */
public class JFireClient {

	private static final Logger logger = Logger.getLogger(JFireClient.class);

	private JFireLogin login;

	/**
	 * Create a new {@link JFireClient}.
	 * @param loginData The login-data to use or <code>null</code> if it should be set later.
	 */
	public JFireClient(LoginData loginData) {
		if (loginData != null)
			login = new JFireLogin(loginData);
	}

	/**
	 * Set the {@link LoginData} for this client.
	 * This method will not login automatically, but it will
	 * perform a {@link #logout()} if old login-data is found.
	 *
	 * @param loginData The {@link LoginData} to set.
	 * @throws LoginException If logout fails.
	 */
	public synchronized void setLoginData(LoginData loginData)
	throws LoginException
	{
		if (login != null) {
			login.logout();
			login = null;
		}
		login = new JFireLogin(loginData);
	}

	/**
	 * Used internally
	 */
	private JFireLogin getLogin() {
		if (login == null)
			throw new IllegalStateException("The login/loginData was not assigned yet.");
		return login;
	}

	/**
	 * Returns Properties for an {@link InitialContext} that are configured
	 * with the login-data of this client. The result can be used for example
	 * to create EJB proxies to access JFire server methods.
	 *
	 * @return Properties for an {@link InitialContext} configured with the login-data of this client.
	 * @throws NamingException Might occur when trying to auto-resolve the providerURL.
	 */
	public synchronized Properties getInitialContextProperties()
	throws NamingException
	{
		return getLogin().getInitialContextProperties();
	}

	/**
	 * Performs a login with the current login-data.
	 *
	 * @throws LoginException When login fails
	 */
	public synchronized void login()
	throws LoginException
	{
		getLogin().login();
		try {
			SecurityReflector sr = SecurityReflector.sharedInstance();
			if (!(sr instanceof SecurityReflectorClient))
				throw new IllegalStateException("SecurityReflector is not an instance of " + SecurityReflectorClient.class.getName());
			((SecurityReflectorClient) sr).setLoginData(getLogin().getLoginData());
			((SecurityReflectorClient) sr).registerAuthorizedObjectRefLifecycleListener();
		} catch (Exception e) {
			logger.warn("The SecurityReflector currently set is not compatible with this JFireLogin.", e);
		}
	}

	/**
	 * Performs a logout.
	 *
	 * @throws LoginException When logout fails.
	 */
	public synchronized void logout()
	throws LoginException
	{
		getLogin().logout();
		try {
			SecurityReflector sr = SecurityReflector.sharedInstance();
			if (!(sr instanceof SecurityReflectorClient))
				throw new IllegalStateException("SecurityReflector is not an instance of " + SecurityReflectorClient.class.getName());
			((SecurityReflectorClient) sr).unregisterAuthorizedObjectRefLifecycleListener();
		} catch (Exception e) {
			logger.warn("The SecurityReflector currently set is not compatible with this JFireLogin.", e);
		}
	}

	/**
	 * This will register the default client implementations of the static JFire API parts.
	 * Currently:
	 * <ul>
	 *   <li>SecurityReflector</li>
	 *   <li>IDGenerator</li>
	 *   <li>JDOLifecycleManager</li>
	 * </ul>
	 */
	public static void registerDefaultStaticJFireClientClasses() {
		// We cannot use org.nightlabs.jfire.idgenerator.IDGenerator.PROPERTY_KEY_ID_GENERATOR_CLASS
		// or IDGeneratorClient,  because this would cause the server side class to be loaded -
		// and probably we're OFFLINE and can't do that!
		System.setProperty("org.nightlabs.jfire.idgenerator.idGeneratorClass", "org.nightlabs.jfire.base.idgenerator.IDGeneratorClient"); //$NON-NLS-1$ //$NON-NLS-2$
		System.setProperty("org.nightlabs.jfire.security.SecurityReflector", "org.nightlabs.jfire.base.security.SecurityReflectorClient"); //$NON-NLS-1$ //$NON-NLS-2$
		System.setProperty("org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager", "org.nightlabs.jfire.base.ui.jdo.notification.JDOLifecycleManager"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
