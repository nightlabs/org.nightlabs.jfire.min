package org.nightlabs.jfire.testsuite.jdo;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.nightlabs.j2ee.LoginData;
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
		LoginData loginData = JFireTestLogin.getUserLoginData(JFireTestLogin.USER_QUALIFIER_SERVER_ADMIN);
		JDOTestUtil.getHome(loginData.getInitialContextProperties()).create().createArrayListFromQueryResult();
	}

	public void testCreateHashSetFromQueryResult() throws Exception
	{
		LoginData loginData = JFireTestLogin.getUserLoginData(JFireTestLogin.USER_QUALIFIER_SERVER_ADMIN);
		JDOTestUtil.getHome(loginData.getInitialContextProperties()).create().createHashSetFromQueryResult();
	}
}
