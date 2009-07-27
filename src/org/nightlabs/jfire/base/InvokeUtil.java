package org.nightlabs.jfire.base;

import java.util.Properties;

import javax.naming.Context;
import javax.security.auth.login.LoginContext;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.server.Server;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.config.ServerCf;
import org.nightlabs.jfire.servermanager.ra.JFireServerManagerImpl;
import org.nightlabs.util.ParameterMap;

/**
 * Utility class for creating initial-context-properties.
 * <p>
 * Note, that you should normally never need to use this class directly - it is very likely
 * that {@link Lookup#getInitialContextProperties(javax.jdo.PersistenceManager, String)}
 * serves your needs much better!
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public class InvokeUtil
{
	public static final String JNDI_PREFIX_EJB_BY_REMOTE_INTERFACE = "ejb/byRemoteInterface/";
	public static final String JNDI_PREFIX_EJB_BY_LOCAL_INTERFACE = "ejb/byLocalInterface/";

	/**
	 * Get the initial-context-properties for connecting to any organisation as any user.
	 * @deprecated Use {@link #getInitialContextProperties(String, String, UserID, String)} instead!
	 */
	@Deprecated
	public static Properties getInitialContextProperties(
			String initialContextFactory, String initialContextURL,
			String organisationID, String userID, String password)
	{
		return getInitialContextProperties(
				initialContextFactory, initialContextURL,
				UserID.create(organisationID, userID),
				password
		);
	}

	/**
	 * Get the initial-context-properties for connecting to any organisation as any user.
	 * <p>
	 * <b>Warning:</b> This method is considered internal! You should not
	 * directly call it. Very likely, you want to use
	 * {@link Lookup#getInitialContextProperties(javax.jdo.PersistenceManager, String)}
	 * instead!
	 * </p>
	 *
	 * @param initialContextFactory the fully qualified class name of the initial-context-factory.
	 * @param initialContextURL the URL of the local or remote server to connect to.
	 * @param userID the user of the organisation to connect to.
	 * @param password the password to authenticate.
	 * @return the initial-context-properties.
	 */
	public static Properties getInitialContextProperties(
			String initialContextFactory, String initialContextURL,
			UserID userID, String password)
	{
		if ((User.USER_ID_PREFIX_TYPE_ORGANISATION + userID.organisationID).equals(userID.userID)) // we login to ourselves - no properties
			return null;

		String username = (
				userID.userID
				+ User.SEPARATOR_BETWEEN_USER_ID_AND_ORGANISATION_ID
				+ userID.organisationID
				+ User.SEPARATOR_BETWEEN_ORGANISATION_ID_AND_REST
				+ JFireServerManagerImpl.LOGIN_PARAM_ALLOW_EARLY_LOGIN
				+ ParameterMap.DEFAULT_KEY_VALUE_SEPARATOR
				+ "true"
//				+ ParameterMap.DEFAULT_ENTRY_SEPARATOR
		);
		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
		props.put(Context.PROVIDER_URL, initialContextURL);
		props.put(Context.SECURITY_PRINCIPAL, username);
		props.put(Context.SECURITY_CREDENTIALS, password);
		props.put(Context.SECURITY_PROTOCOL, "jfire");

//		props.setProperty("jnp.multi-threaded", String.valueOf(true));
//		props.setProperty("jnp.restoreLoginIdentity", String.valueOf(true));

		return props;
	}

	/**
	 * Get the initial-context-properties for connecting to a local organisation (hosted
	 * on the local server). Using this method, it is not possible to connect to an organisation
	 * hosted on a remote server.
	 * <p>
	 * <b>Warning:</b> It is recommended that you don't use this method! In most use cases,
	 * {@link Lookup#getInitialContextProperties(javax.jdo.PersistenceManager, String)} is very
	 * likely what you want (and works with all organisations - no matter on which server)!
	 * </p>
	 * <p>
	 * If you really have to execute an EJB method as a different user and cannot use <code>Lookup</code>,
	 * you should
	 * better authenticate using a {@link LoginContext} with an {@link AuthCallbackHandler}
	 * and use <code>null</code> as initial-context-properties
	 * like in this code example:
	 * <pre>
	 * J2EEAdapter j2eeAdapter = (J2EEAdapter) initialContext.lookup(J2EEAdapter.JNDI_NAME);
	 * LoginContext loginContext = j2eeAdapter.createLoginContext(LoginData.DEFAULT_SECURITY_PROTOCOL, new AuthCallbackHandler(...));
	 * loginContext.login();
	 * try {
	 *   MyRemote ejb = JFireEjb3Factory.getRemoteBean(MyRemote.class, null);
	 *   // ...
	 * } finally {
	 *   loginContext.logout();
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param jFireServerManagerFactory the reference to the {@link JFireServerManagerFactory}.
	 * @param userID the user of the local organisation to connect to. Normally that's the system user of this organisation.
	 * @param password the password to authenticate.
	 * @return the initial-context-properties.
	 */
	public static Properties getInitialContextProperties(
			JFireServerManagerFactory jFireServerManagerFactory,
			UserID userID, String password
	)
	{
		ServerCf localServerCf = jFireServerManagerFactory.getLocalServer();

		return getInitialContextProperties(
				jFireServerManagerFactory.getInitialContextFactory(localServerCf.getJ2eeServerType(), true),
				localServerCf.getInitialContextURL(Server.PROTOCOL_JNP, true),
				userID, password
		);
	}

	/**
	 * Get the initial-context-properties for connecting to a local organisation (hosted
	 * on the local server). Even though you can pass any {@link ServerCf} instance, this
	 * method is intended only for passing the local <code>ServerCf</code>
	 * which is usually obtained via {@link JFireServerManagerFactory#getLocalServer()}.
	 * <p>
	 * <b>Warning:</b> This method is considered internal! You should normally not need to use it
	 * and it might soon be removed without prior notice.
	 * </p>
	 *
	 * @deprecated Use {@link #getInitialContextProperties(JFireServerManagerFactory, UserID, String)} instead. This method will very soon be removed.
	 */
	@Deprecated
	public static Properties getInitialContextProperties(
			JFireServerManagerFactory jFireServerManagerFactory,
			ServerCf localServerCf, String organisationID, String userID, String password
	)
	{
		return getInitialContextProperties(
				jFireServerManagerFactory.getInitialContextFactory(localServerCf.getJ2eeServerType(), true),
				localServerCf.getInitialContextURL(Server.PROTOCOL_JNP, true),
				UserID.create(organisationID, userID), password
		);
	}
}
