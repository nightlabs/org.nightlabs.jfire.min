/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.login;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.math.Base62Coder;

/**
 * Helper class to login to a JFire server.
 * <p>
 * It can be instantiated and is capable of configuring a {@link LoginContext} with the values
 * provided in the constructor. Note, that before communicating with JFire {@link #login()} needs to be called
 * and you should make sure in a try-finally block that {@link #logout()} will be called, too.
 * </p>
 * <p> 
 * Additionally an instance of {@link JFireLogin} can serve IntialContextProperties
 * that developers will need for example to create EJB proxies to
 * the services, the jfire server provides. (See {@link #getInitialContextProperties()})
 * </p>
 * <b>Important:</b> When you want to use this Login outside the ServerVM, you have to call {@link JFireSecurityConfiguration#declareConfiguration()}
 * before trying to log in or put a valid SecurityConfiguration where your Java VM expects it ;-).
 * <p>
 * The Login can be configured via a {@link Properties} instance where it expects the following keys:
 * <ul>
 *   <li><b>jfire.login.organisationID</b> (the value of {@link #PROP_ORGANISATION_ID}), defines the organisationID to login to.</li>
 *   <li><b>jfire.login.userID</b> (the value of {@link #PROP_USER_ID}), defines the userID to log in with. Note that organisationID and userID will be concatted to form the username: userID@organisationID</li>
 *   <li><b>jfire.login.password</b> (the value of {@link #PROP_PASSWORD}), defines the password to login with.</li>
 *   <li><b>jfire.login.providerURL</b> (the value of {@link #PROP_PROVIDER_URL}), defines the URL of the server to login to.
 *   	This defaults to the value defined in {@link InitialContext#getEnvironment()} or to "jnp://127.0.0.1:1099"</li>
 *   <li><b>jfire.login.initialContextFactory</b> (the value of {@link #PROP_INITIAL_CONTEXT_FACTORY}), defines the login context factory to use,
 *   	defaults to "org.nightlabs.jfire.jboss.cascadedauthentication.LoginInitialContextFactory"</li>
 *   <li><b>jfire.login.securityProtocol</b> (the value of {@link #PROP_SECURITY_PROTOCOL}), defines the security protocol to use, defaults to "jfire".</li>
 *   <li><b>jfire.login.workstationID</b> (the value of {@link #PROP_WORKSTATION_ID}), defines the optional workstation-identifier.</li>
 * </ul>
 * </p>
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * @author Marius Heinzmann -- Marius[at]NightLabs[dot]de
 */
public class JFireLogin
{
	/**
	 * Log4J Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(JFireLogin.class);

	public static final String LOGIN_PREFIX = "jfire.login.";

	public static final String ORGANISATION_ID = "organisationID";
	public static final String USER_ID = "userID";
	public static final String PASSWORD = "password";
	public static final String PROVIDER_URL = "providerURL";
	public static final String INITIAL_CONTEXT_FACTORY = "initialContextFactory";
	public static final String SECURITY_PROTOCOL = "securityProtocol";
	public static final String WORKSTATION_ID = LoginData.WORKSTATION_ID;

	public static final String PROP_ORGANISATION_ID = LOGIN_PREFIX + ORGANISATION_ID;
	public static final String PROP_USER_ID = LOGIN_PREFIX + USER_ID;
	public static final String PROP_PASSWORD = LOGIN_PREFIX + PASSWORD;
	public static final String PROP_PROVIDER_URL = LOGIN_PREFIX + PROVIDER_URL;
	public static final String PROP_INITIAL_CONTEXT_FACTORY = LOGIN_PREFIX + INITIAL_CONTEXT_FACTORY;
	public static final String PROP_SECURITY_PROTOCOL = LOGIN_PREFIX + SECURITY_PROTOCOL;
	public static final String PROP_WORKSTATION_ID = LOGIN_PREFIX + WORKSTATION_ID;

	/**
	 * Encapsulates all necessary login information.
	 */
	private LoginData loginData;

	/**
	 * Creates a new {@link JFireLogin}. The values like username and password
	 * will be taken from the given Properties, see the class documentation for
	 * more details.
	 * <p>
	 * Note, that values for provider-url and initial-context-factory will not
	 * be automatically created from an InitialContext.
	 * </p>
	 * 
	 * @param properties The login configuration
	 */
	public JFireLogin(Properties properties) {
		this(properties, false);
	}
	
	/**
	 * Creates a new {@link JFireLogin}.
	 * The values like username and password will
	 * be taken from the given Properties,
	 * see the class documentation for more details.
	 *
	 * @param loginProperties The login configuration
	 * @param createMissingValuesFromInitialContext Whether to fill missing parameters from an initial context
	 */
	public JFireLogin(Properties properties, boolean createMissingValuesFromInitialContext)
	{
		Properties loginProps = org.nightlabs.util.Properties.getProperties(properties, LOGIN_PREFIX);
		String organisationID, userID, password;
		organisationID = loginProps.getProperty(ORGANISATION_ID, "");
		userID = loginProps.getProperty(USER_ID, "");
		password = loginProps.getProperty(PASSWORD, "");
		loginData = new LoginData(organisationID, userID, password);

		for (Object propKey : loginProps.keySet()) {
			if (USER_ID.equals(propKey) || PASSWORD.equals(propKey) || ORGANISATION_ID.equals(propKey))
				continue;

			if (PROVIDER_URL.equals(propKey))
				loginData.setProviderURL(loginProps.getProperty(PROVIDER_URL, null));
			else if (INITIAL_CONTEXT_FACTORY.equals(propKey))
				loginData.setInitialContextFactory(loginProps.getProperty(INITIAL_CONTEXT_FACTORY, "org.nightlabs.jfire.jboss.cascadedauthentication.LoginInitialContextFactory"));
			else if (SECURITY_PROTOCOL.equals(propKey))
				loginData.setSecurityProtocol(loginProps.getProperty(SECURITY_PROTOCOL, "jfire"));
			else {
				loginData.getAdditionalParams().put(
						(String) propKey,
						loginProps.getProperty((String)propKey)
						);
			}
		}

		if (createMissingValuesFromInitialContext) {
			// set default values if login information is incomplete
			loginData.setConnectionParametersFromInitialContext();
		}
		
		if (loginData.getSecurityProtocol() == null || loginData.getSecurityProtocol().length() == 0)
			loginData.setSecurityProtocol(LoginData.DEFAULT_SECURITY_PROTOCOL);
	}

	public JFireLogin(LoginData loginData) {
		this.loginData = loginData;
	}

	/**
	 * A convenience method that creates {@link Properties} from the given
	 * parameters and calls {@link #JFireLogin(Properties)}.
	 *
	 * @param _organisationID The organisationID to use.
	 * @param _userID The userID to use.
	 * @param _password The password to use.
	 */
	public JFireLogin(String _organisationID, String _userID, String _password) {
		this(createLoginProperties(_organisationID, _userID, _password));
	}

	/**
	 * Use internally for convenience constructor
	 */
	private static Properties createLoginProperties(String _organisationID, String _userID, String _password) {
		Properties props = new Properties();
		props.setProperty(PROP_ORGANISATION_ID, _organisationID);
		props.setProperty(PROP_USER_ID, _userID);
		props.setProperty(PROP_PASSWORD, _password);
		return props;
	}

	/**
	 * @return The organisationID.
	 */
	public String getOrganisationID() {
		return loginData.getOrganisationID();
	}
	/**
	 * @return The userID.
	 */
	public String getUserID() {
		return loginData.getUserID();
	}
	/**
	 * @return The password.
	 */
	public String getPassword() {
		return loginData.getPassword();
	}

	public String getWorkstationID() {
		return loginData.getWorkstationID();
	}

	/**
	 * @return The sessionID.
	 */
	public String getSessionID() {
		return loginData.getSessionID();
	}

	// *** end of properties *************************

	protected transient Properties initialContextProperties = null;

	/**
	 * @return The {@link javax.security.auth.CallbackHandler} that can serve JFire login credentials according to this {@link JFireLogin}.
	 */
	public JFireLoginAuthCallbackHandler getAuthCallbackHandler()
	{
		return new JFireLoginAuthCallbackHandler(loginData);
	}

	/**
	 * Returns Properties for an {@link InitialContext} that are configured with the values according to this
	 * {@link JFireLogin}. The result can be used for example to create EJB proxies to
	 * access JFire server methods.
	 *
	 * @return Properties for an {@link InitialContext} configured with the values of this JFireLogin.
	 */
	public Properties getInitialContextProperties()
	{
		if (logger.isDebugEnabled())
			logger.debug("getInitialContextProperties: begin");

		if (initialContextProperties == null) {
			if (logger.isDebugEnabled())
				logger.debug("getInitialContextProperties: generating props");

			if (loginContext == null)
				throw new IllegalStateException("Not authenticated! Call login() first! And don't forget to logout() afterwards.");

			initialContextProperties = loginData.getInitialContextProperties();
		}
		return initialContextProperties;
	}

	private transient LoginContext loginContext = null;
//	private transient UserDescriptor userDescriptorOnLogin = null;

	/**
	 * Performs a login via {@link LoginContext}.
	 *
	 * TODO I doubt that this will work fine when used in the server (project JFireTestSuite): IMHO, it's a
	 * bad idea to switch the user within a running
	 * transaction, already having a PersistenceManager. Especially logout will probably fail, since I doubt
	 * that the previous identity will really be restored.
	 *
	 * @throws LoginException When login fails
	 */
	public void login()
	throws LoginException
	{
		loginData.setSessionID(getLoginSessionID());
		LoginContext lc = new LoginContext("jfire", getAuthCallbackHandler());
		lc.login();
		loginContext = lc; // only assign the field if the login was successful - otherwise leave it null.
	}
	
	/**
	 * Get the session id for the login. By default, this will return a unique id
	 * every time the method if called. Subclasses may override this method to
	 * change this behaviour (i.e. re-use a session id).
	 * @return The login session id
	 */
	protected String getLoginSessionID() {
		Base62Coder coder = Base62Coder.sharedInstance();
		return 
				coder.encode(System.currentTimeMillis(), 1) + '-' +
				coder.encode((long)(Math.random() * 14776335), 1); // 14776335 is the highest value encoded in 4 digits ("zzzz")
	}

	/**
	 * Performs a logout via {@link LoginContext}.
	 *
	 * @throws LoginException When logout fails.
	 */
	public void logout()
	throws LoginException
	{
		if (loginContext != null) {
//			UserDescriptor userDescriptorAfterLogout;
//			do {
				loginContext.logout();

//				try {
//					userDescriptorAfterLogout = SecurityReflector.getUserDescriptor();
//				} catch (NoUserException x) {
//					userDescriptorAfterLogout = null;
//				}
//
//			} while (!Util.equals(userDescriptorOnLogin, userDescriptorAfterLogout));

			loginContext = null;
			initialContextProperties = null;
			loginData.setSessionID(null);
		}
	}

	public LoginData getLoginData() {
		return loginData;
	}
}
