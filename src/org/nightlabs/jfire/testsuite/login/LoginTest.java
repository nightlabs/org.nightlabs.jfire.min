/**
 * 
 */
package org.nightlabs.jfire.testsuite.login;

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
public class LoginTest extends TestCase
{

	public LoginTest() { }

	public void testCorrectLogin() throws Exception {
		JFireLogin login = JFireTestLogin.getUserLogin(JFireTestLogin.USER_QUALIFIER_SERVER_ADMIN);
		login.login(); // Warning! even though it's fine for testing here, switching the identity during a running transaction might cause trouble.
		UserManagerUtil.getHome(login.getInitialContextProperties()).create();		
	}

	public void testInCorrectLogin() {
		JFireLogin login = new JFireLogin("chezfrancois.jfire.org", "francoiz", "text");
		try {
			login.login(); // Warning! even though it's fine for testing here, switching the identity during a running transaction might cause trouble.
		} catch (LoginException e) {
			// OK, caught some login exception
			return;
		}
		fail("Could login with wrong credentials");
//		UserManagerUtil.getHome(login.getInitialContextProperties()).create();		
	}

}
