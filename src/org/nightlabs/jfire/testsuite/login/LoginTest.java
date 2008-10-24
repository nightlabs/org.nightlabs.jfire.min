/**
 *
 */
package org.nightlabs.jfire.testsuite.login;

import javax.security.auth.login.LoginException;

import junit.framework.TestCase;

import org.nightlabs.jfire.base.login.JFireLogin;
import org.nightlabs.jfire.security.JFireSecurityManagerUtil;
import org.nightlabs.jfire.testsuite.JFireTestSuite;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
@JFireTestSuite(JFireLoginTestSuite.class)
public class LoginTest extends TestCase
{
	public LoginTest() { }

	public void testCorrectLogin()
	throws Exception
	{
		JFireLogin login = JFireTestLogin.getUserLogin(JFireTestLogin.USER_QUALIFIER_SERVER_ADMIN);
		login.login();
		try {
			JFireSecurityManagerUtil.getHome(login.getInitialContextProperties()).create();
		} finally {
			login.logout();
		}
	}

	public void testIncorrectLogin_wrongUserName()
	throws Exception
	{
		JFireLogin login = new JFireLogin("chezfrancois.jfire.org", "francoiz", "test");
		try {
			try {
				login.login();
			} catch (LoginException e) {
				// OK, caught some login exception
				return;
			}
		} finally {
			login.logout();
		}
		fail("Could login with wrong credentials");
	}

	public void testIncorrectLogin_wrongPassword()
	throws Exception
	{
		JFireLogin login = new JFireLogin("chezfrancois.jfire.org", "francois", "text");
		try {
			try {
				login.login();
			} catch (LoginException e) {
				// OK, caught some login exception
				return;
			}
		} finally {
			login.logout();
		}
		fail("Could login with wrong credentials");
	}
}
