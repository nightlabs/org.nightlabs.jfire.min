package org.nightlabs.jfire.testsuite.jdo;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.testsuite.JFireTestSuite;
import org.nightlabs.jfire.testsuite.TestCase;
import org.nightlabs.jfire.testsuite.login.JFireTestLogin;

/**
 * This test case tests some basic functionality of the JDO implementation.
 */

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

	@Test
	public void testCreateArrayListFromQueryResult() throws Exception
	{
		// REV Alex: What is this testing?!?
		// REV Marco: This was IMHO written by me a long time ago because of a bug in DataNucleus when
		// creating certain collections from query results.
		LoginData loginData = JFireTestLogin.getUserLoginData(JFireTestLogin.USER_QUALIFIER_SERVER_ADMIN);
		JFireEjb3Factory.getRemoteBean(JDOTestRemote.class, loginData.getInitialContextProperties()).createArrayListFromQueryResult();
	}

	@Test
	public void testCreateHashSetFromQueryResult() throws Exception
	{
		// REV Alex: What is this testing?!?
		// REV Marco: This was IMHO written by me a long time ago because of a bug in DataNucleus when
		// creating certain collections from query results.
		LoginData loginData = JFireTestLogin.getUserLoginData(JFireTestLogin.USER_QUALIFIER_SERVER_ADMIN);
		JFireEjb3Factory.getRemoteBean(JDOTestRemote.class, loginData.getInitialContextProperties()).createHashSetFromQueryResult();
	}
}
