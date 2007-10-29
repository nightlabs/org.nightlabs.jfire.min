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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.math.Base62Coder;

/**
 * Helper class to login to a JFire server.
 * <p>
 * It can be instantiated and serves IntialContextProperties 
 * that developers will need for example to create EJB proxies to
 * the services, the jfire server provides. (See {@link #getInitialContextProperties()})
 * </p>
 * <b>Important:</b> When you want to use this Login outside the ServerVM, you have to call {@link JFireSecurityConfiguration#declareConfiguration()}
 * before trying to log in or put a valid SecurityConfiguration where your Java VM expects it ;-).
 * <p>
 * The Login can be configured via a {@link Properties} instance where it expects the following keys:
 * <ul>
 *   <li><b>jfire.login.organisationID</b> (The value of {@link #PROP_ORGANISATION_ID}), defines the organisationID to login to.</li>
 *   <li><b>jfire.login.userID</b> (The value of {@link #PROP_USER_ID}), defines the userID to log in with. Note that organisationID and userID will be concatted to form the username: userID@organisationID</li>
 *   <li><b>jfire.login.password</b> (The value of {@link #PROP_PASSWORD}), defines the password to login with.</li>
 *   <li><b>jfire.login.providerURL</b> (The value of {@link #PROP_PROVIDER_URL}), defines the URL of the server to login to. 
 *   	This defaults to the value defined in {@link InitialContext#getEnvironment()} or to "jnp://127.0.0.1:1099"</li>
 *   <li><b>jfire.login.initialContextFactory</b> (The value of {@link #PROP_INITIAL_CONTEXT_FACTORY}), defines the login context factory to use, 
 *   	defaults to "org.nightlabs.jfire.jboss.cascadedauthentication.LoginInitialContextFactory"</li>
 *   <li><b>jfire.login.securityProtocol</b> (The value of {@link #PROP_SECURITY_PROTOCOL}), defines the security protocol to use, defaults to "jfire".</li>
 * </ul>
 * </p> * 
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
	public static final String PROP_ORGANISATION_ID = LOGIN_PREFIX + "organisationID";
	public static final String PROP_USER_ID = LOGIN_PREFIX + "userID";
	public static final String PROP_PASSWORD = LOGIN_PREFIX + "password";
	public static final String PROP_PROVIDER_URL = LOGIN_PREFIX + "providerURL";
	public static final String PROP_INITIAL_CONTEXT_FACTORY = LOGIN_PREFIX + "initialContextFactory";
	public static final String PROP_SECURITY_PROTOCOL = LOGIN_PREFIX + "securityProtocol";
	public static final String PROP_WORKSTATION_ID = LOGIN_PREFIX + LoginData.WORKSTATION_ID;
	
	/**
	 * Encapsulates all necessary login information.
	 */
	private LoginData loginData;
	
	/**
	 * Creates a new {@link JFireLogin}.
	 * The values like username and password will 
	 * be taken from the given Properties, 
	 * see the class documentation for more details.
	 * 
	 * @param loginProperties The login configuration, this can also be a pr
	 */
	public JFireLogin(Properties properties)
	{
		Properties loginProps = org.nightlabs.util.Properties.getProperties(properties, LOGIN_PREFIX);
		String organisationID, userID, password;
		organisationID = loginProps.getProperty(PROP_ORGANISATION_ID, "");
		userID = loginProps.getProperty(PROP_USER_ID, "");
		password = loginProps.getProperty(PROP_PASSWORD, "");
		loginData = new LoginData(organisationID, userID, password);
		
		for (Object propKey : loginProps.keySet()) {
			if (PROP_PROVIDER_URL.equals(propKey))
				loginData.setProviderURL(loginProps.getProperty(PROP_PROVIDER_URL, null));
			else if (PROP_INITIAL_CONTEXT_FACTORY.equals(propKey))
				loginData.setInitialContextFactory(loginProps.getProperty(PROP_INITIAL_CONTEXT_FACTORY, "org.nightlabs.jfire.jboss.cascadedauthentication.LoginInitialContextFactory"));
			else if (PROP_SECURITY_PROTOCOL.equals(propKey))
				loginData.setSecurityProtocol(loginProps.getProperty(PROP_SECURITY_PROTOCOL, "jfire"));
			else {
				loginData.getAdditionalParams().put(
						((String)propKey).substring(LOGIN_PREFIX.length()), 
						loginProps.getProperty((String)propKey)
						);
			}
		}
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
		return loginData.getOrganisationID();
	}
	/**
	 * @return The password.
	 */
	public String getPassword() {
		return loginData.getPassword();
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
	 * @return The {@link CallbackHandler} that can serve JFire login credentials according to this {@link JFireLogin}.
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
	 * @throws NamingException Might occur when trying to auto-resolve the providerURL.   
	 */
	public Properties getInitialContextProperties()
	throws NamingException
	{
		logger.debug(this.getClass().getName()+"#getInitialContextProperties(): begin");

		if (initialContextProperties == null) {
			logger.debug(this.getClass().getName()+"#getInitialContextProperties(): generating props");

			String providerURL = loginData.getProviderURL();
			if (providerURL == null) {
				InitialContext initialContext = new InitialContext();
				providerURL = (String) initialContext.getEnvironment().get(Context.PROVIDER_URL); // TODO does this really work? Alternatively, we might read it from a registry - in the server, this registry exists already (but this is a client lib)...
				initialContext.close();
			}

			if (providerURL == null)
				providerURL = "jnp://127.0.0.1:1099";
			

			Properties props = new Properties();
			props.put(Context.INITIAL_CONTEXT_FACTORY, loginData.getInitialContextFactory());
			props.put(Context.PROVIDER_URL, providerURL);
			props.put(Context.SECURITY_PRINCIPAL, loginData.getPrincipalName());
			props.put(Context.SECURITY_CREDENTIALS, loginData.getPassword());
			props.put(Context.SECURITY_PROTOCOL, loginData.getSecurityProtocol());

			initialContextProperties = props;
		}
		return initialContextProperties;
	}
	
	private transient LoginContext loginContext = null;
	
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
		Base62Coder coder = Base62Coder.sharedInstance();
		loginData.setSessionID(
				coder.encode(System.currentTimeMillis(), 1) + '-' +
				coder.encode((long)(Math.random() * 14776335), 1)); // 14776335 is the highest value encoded in 4 digits ("zzzz")

		loginContext = new LoginContext("jfire", getAuthCallbackHandler());
		loginContext.login();
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
			loginContext.logout();
			loginContext = null;
			initialContextProperties = null;
			loginData.setSessionID(null);
		}
	}
	
}
