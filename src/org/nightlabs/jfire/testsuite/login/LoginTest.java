/**
 * 
 */
package org.nightlabs.jfire.testsuite.login;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import junit.framework.TestCase;

import org.nightlabs.jfire.base.login.JFireLogin;
import org.nightlabs.jfire.security.UserManagerUtil;
import org.nightlabs.jfire.testsuite.JFireTestSuite;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
@JFireTestSuite(JFireLoginTestSuite.class)
public class LoginTest extends TestCase {

	/**
	 * 
	 */
	public LoginTest() {
	}

	public void testCorrectLogin() throws LoginException, RemoteException, CreateException, NamingException {
		JFireLogin login = new JFireLogin("chezfrancois.jfire.org", "francois", "test");
		login.login();
		UserManagerUtil.getHome(login.getInitialContextProperties()).create();		
	}
	
	public void testInCorrectLogin() {
		JFireLogin login = new JFireLogin("chezfrancois.jfire.org", "francoiz", "text");
		try {
			login.login();
		} catch (LoginException e) {
			// OK, caught some login exception
			return;
		}
		fail("Could login with wrong credentials");
//		UserManagerUtil.getHome(login.getInitialContextProperties()).create();		
	}
}
