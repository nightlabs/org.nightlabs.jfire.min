/**
 * 
 */
package org.nightlabs.jfire.base.jdo.login;

import java.util.Properties;

import javax.security.auth.login.LoginException;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public interface IJFireLoginProvider {
	
	String getUserID() throws LoginException;
	String getOrganisationID() throws LoginException;
	Properties getInitialContextProperties() throws LoginException;
	String getSessionID() throws LoginException;
}
