/**
 *
 */
package org.nightlabs.jfire.testsuite.login;

import javax.security.auth.login.LoginException;

import junit.framework.TestCase;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.login.JFireLogin;
import org.nightlabs.jfire.security.JFireSecurityManagerRemote;
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
		LoginData loginData = JFireTestLogin.getUserLoginData(JFireTestLogin.USER_QUALIFIER_SERVER_ADMIN);
		JFireSecurityManagerRemote ejb = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, loginData.getInitialContextProperties());
		ejb.ping("test");
	}

	public void testCorrectLoginManyTimes_onlyLogin()
	throws Exception
	{
		for (int i = 0; i < 10000; ++i) {
			JFireLogin login = new JFireLogin("chezfrancois.jfire.org", "francois", "test");
			login.login();
			login.logout();
		}
	}

	public void testIncorrectLogin_wrongUserName_onlyLogin()
	throws Exception
	{
		JFireLogin login = new JFireLogin("chezfrancois.jfire.org", "francoiz", "test");
		try {
			login.login();
		} catch (LoginException e) {
			// OK, caught some login exception
			return;
		}
		// Logout in case we were able to login - and only then.
		// We MUST NOT(!!!) logout when the login failed! This would corrupt our login-stack (cascaded authentication!). Marco.
		login.logout();
		fail("Could login with wrong credentials");
	}

	public void testIncorrectLogin_wrongUserName_onlyLogin_2()
	throws Exception
	{
		JFireLogin login = new JFireLogin("chezfrancois.jfire.org", "francoiy", "test");
		try {
			login.login();
		} catch (LoginException e) {
			// OK, caught some login exception
			return;
		}
		// Logout in case we were able to login - and only then.
		// We MUST NOT(!!!) logout when the login failed! This would corrupt our login-stack (cascaded authentication!). Marco.
		login.logout();
		fail("Could login with wrong credentials");
	}

	public void testIncorrectLogin_wrongPassword_onlyLogin()
	throws Exception
	{
		JFireLogin login = new JFireLogin("chezfrancois.jfire.org", "francois", "text");
		try {
			login.login();
		} catch (LoginException e) {
			// OK, caught some login exception
			return;
		}
		// Logout in case we were able to login - and only then.
		// We MUST NOT(!!!) logout when the login failed! This would corrupt our login-stack (cascaded authentication!). Marco.
		login.logout();
		fail("Could login with wrong credentials");
	}

	public void testIncorrectLogin_wrongPassword_onlyLogin_2()
	throws Exception
	{
		JFireLogin login = new JFireLogin("chezfrancois.jfire.org", "francois", "xyz");
		try {
			login.login();
		} catch (LoginException e) {
			// OK, caught some login exception
			return;
		}
		// Logout in case we were able to login - and only then.
		// We MUST NOT(!!!) logout when the login failed! This would corrupt our login-stack (cascaded authentication!). Marco.
		login.logout();
		fail("Could login with wrong credentials");
	}


	public void testIncorrectLogin_wrongUserName_createBean()
	throws Exception
	{
		LoginData loginData = new LoginData("chezfrancois.jfire.org", "francoiz", "test");
		loginData.setDefaultValues();
		try {
			JFireSecurityManagerRemote ejb = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, loginData.getInitialContextProperties());
			ejb.ping("test");
		} catch (Exception e) {
			if (ExceptionUtils.indexOfThrowable(e, LoginException.class) < 0)
				throw e; // different error - not a LoginException

			// OK, caught some login exception
			return;
		}
		fail("Could login with wrong credentials");
	}

	public void testIncorrectLogin_wrongPassword_createBean()
	throws Exception
	{
		LoginData loginData = new LoginData("chezfrancois.jfire.org", "francois", "text");
		loginData.setDefaultValues();
		try {
			JFireSecurityManagerRemote ejb = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, loginData.getInitialContextProperties());
			ejb.ping("test");
		} catch (Exception e) {
			if (ExceptionUtils.indexOfThrowable(e, LoginException.class) < 0)
				throw e; // different error - not a LoginException

			// OK, caught some login exception
			return;
		}
		fail("Could login with wrong credentials");
	}
}
