/**
 * 
 */
package org.nightlabs.jfire.testsuite.jdo;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.nightlabs.jfire.base.login.JFireLogin;
import org.nightlabs.jfire.testsuite.JFireTestSuite;
import org.nightlabs.jfire.testsuite.base.JFireBaseTestSuite;
import org.nightlabs.jfire.testsuite.login.JFireTestLogin;

/**
 * This test case tests some basic functionality of the JDO implementation.
 */
@JFireTestSuite(JFireBaseTestSuite.class)
public class JDOTestCase extends TestCase
{
	Logger logger = Logger.getLogger(JDOTestCase.class);

	@Override
	protected void setUp() throws Exception
	{
		logger.info("setUp: invoked");
	}

	@Override
	protected void tearDown()
			throws Exception
	{
		logger.info("tearDown: invoked");
	}

	public void testCreateArrayListFromQueryResult() throws Exception
	{
		JFireLogin login = JFireTestLogin.getUserLogin(JFireTestLogin.USER_QUALIFIER_SERVER_ADMIN);
		login.login();
		try {
			JDOTestUtil.getHome().create().createArrayListFromQueryResult();
		} finally {
			login.logout();
		}
	}

	@Test
	public void testCreateHashSetFromQueryResult() throws Exception
	{
		JFireLogin login = JFireTestLogin.getUserLogin(JFireTestLogin.USER_QUALIFIER_SERVER_ADMIN);
		login.login();
		try {
			JDOTestUtil.getHome().create().createHashSetFromQueryResult();
		} finally {
			login.logout();
		}
	}
}
