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
import javax.naming.NamingException;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;

/**
 * Helper class to login to a JFire server.
 * <p>
 * It can be instantiated an serve IntialContextProperties 
 * that developers will need for example to create EJB proxies to
 * the services the jfire server provides. (See {@link #getInitialContextProperties()})
 * </p>
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
 * </p>
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class JFireLogin
{
	/**
	 * Log4J Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(JFireLogin.class);
	
	public static final String PROP_ORGANISATION_ID = "jfire.login.organisationID";
	public static final String PROP_USER_ID = "jfire.login.userID";
	public static final String PROP_PASSWORD = "jfire.login.password";
	public static final String PROP_PROVIDER_URL = "jfire.login.providerURL";
	public static final String PROP_INITIAL_CONTEXT_FACTORY = "jfire.login.initialContextFactory";
	public static final String PROP_SECURITY_PROTOCOL = "jfire.login.securityProtocol";
	
	private String organisationID;
	private String userID;
	// username is the concatted product of userID and organisationID (userID@organisationID)
	private String username;
	private String password;
	private String sessionID;
	private String providerURL;
	private String initialContextFactory;
	private String securityProtocol;
	
	private Properties loginProperties;

	/**
	 * Creates a new {@link JFireLogin}.
	 * The values like username and password will 
	 * be taken from the given Properties, 
	 * see the class documentation for more details.
	 * 
	 * @param loginProperties The login configuration.
	 */
	public JFireLogin(Properties loginProperties)
	{
		this.loginProperties = loginProperties;
		this.organisationID = getProperty(PROP_ORGANISATION_ID, "");
		this.userID = getProperty(PROP_USER_ID, "");
		this.password = getProperty(PROP_PASSWORD, "");
		this.providerURL = getProperty(PROP_PROVIDER_URL, null);
		this.initialContextFactory = getProperty(PROP_INITIAL_CONTEXT_FACTORY, "org.nightlabs.jfire.jboss.cascadedauthentication.LoginInitialContextFactory");
		this.securityProtocol = getProperty(PROP_SECURITY_PROTOCOL, "jfire");
//		// TODO is this correct here or should we 
//		this.sessionID = Base62Coder.sharedInstance().encode(System.currentTimeMillis(), 1) + '-' + Base62Coder.sharedInstance().encode((long)(10000 * Math.random()), 1);
		this.sessionID = JFireLogin.class.getName(); // TODO how do we handle the sessionID?!?!

		StringBuffer sbUser = new StringBuffer();
		sbUser.append(userID);
		sbUser.append('@');
		sbUser.append(organisationID);
		username = sbUser.toString();
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
	 * Used internally to define config default values.
	 */
	private String getProperty(String key, String def) {
		String result = def;
		if (loginProperties.containsKey(key))
			result = loginProperties.getProperty(key);
		return result;
	}
	
	/**
	 * @return The organisationID.
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	/**
	 * @return The userID.
	 */
	public String getUserID() {
		return userID;
	}
	/**
	 * @return The password.
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * @return The sessionID.
	 */
	public String getSessionID() {
		return sessionID;
	}
	
	// *** end of properties *************************
	
	protected transient Properties initialContextProperties = null;
//	protected transient InitialContext initialContext = null;
//	protected transient LoginContext loginContext = null;

	/**
	 * @return The {@link CallbackHandler} that can serve JFire login credentials accoring to this {@link JFireLogin}.
	 */
	public JFireLoginAuthCallbackHandler getAuthCallbackHandler()
	{
		return new JFireLoginAuthCallbackHandler(organisationID, userID, sessionID, password.toCharArray());
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

			if (providerURL == null)
				providerURL = (String) new InitialContext().getEnvironment().get(InitialContext.PROVIDER_URL);

			if (providerURL == null)
				providerURL = "jnp://127.0.0.1:1099";
			

			Properties props = new Properties();
//			props.put(InitialContext.INITIAL_CONTEXT_FACTORY,	"org.jboss.security.jndi.LoginInitialContextFactory");
			props.put(InitialContext.INITIAL_CONTEXT_FACTORY, initialContextFactory);
			props.put(InitialContext.PROVIDER_URL, providerURL);
			props.put(InitialContext.SECURITY_PRINCIPAL, username);
			props.put(InitialContext.SECURITY_CREDENTIALS, password);
			props.put(InitialContext.SECURITY_PROTOCOL, securityProtocol);

			initialContextProperties = props;
		}
		return initialContextProperties;
	}
	
	private transient LoginContext loginContext = null;
	
	/**
	 * Performs a login via {@link LoginContext}.
	 * 
	 * @throws LoginException When login fails
	 */
	public void login() 
	throws LoginException 
	{
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
		}
	}
	
}
