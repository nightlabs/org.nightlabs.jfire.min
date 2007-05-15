/**
 * 
 */
package org.nightlabs.jfire.base.login;

import java.util.Properties;

import javax.security.auth.login.LoginException;

import org.nightlabs.jfire.base.jdo.login.IJFireLoginProvider;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class JFireLoginProviderRCP implements IJFireLoginProvider {

	/**
	 * 
	 */
	public JFireLoginProviderRCP() {
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.login.IJFireLoginProvider#getInitialContextProperties()
	 */
	public Properties getInitialContextProperties() throws LoginException {
		return Login.getLogin().getInitialContextProperties();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.login.IJFireLoginProvider#getSessionID()
	 */
	public String getSessionID() throws LoginException {
		return Login.getLogin().getSessionID();
	}

	public String getOrganisationID() throws LoginException {
		return Login.getLogin().getOrganisationID();
	}

	public String getUserID() throws LoginException {
		return Login.getLogin().getUserID();
	}
}
