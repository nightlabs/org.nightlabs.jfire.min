/**
 * 
 */
package org.nightlabs.jfire.base.jdo.login;

import java.util.Properties;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public interface IJFireLoginProvider {
	
	Properties getInitialContextProperties();	
	String getSessionID();	

}
