/**
 * 
 */
package org.nightlabs.jfire.base.login;

import java.util.Properties;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.jdo.login.IJFireLoginProvider;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class JFireLoginProviderRCP implements IJFireLoginProvider {
	
	private static Logger logger = Logger.getLogger(JFireLoginProviderRCP.class);

	/**
	 * 
	 */
	public JFireLoginProviderRCP() {
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.login.IJFireLoginProvider#getInitialContextProperties()
	 */
	public Properties getInitialContextProperties() {
		try {
			return Login.getLogin().getInitialContextProperties();
		} catch (LoginException e) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.login.IJFireLoginProvider#getSessionID()
	 */
	public String getSessionID() {
		try {
			return Login.getLogin().getSessionID();
		} catch (LoginException e) {
			throw new RuntimeException(e);
		}
	}
}
