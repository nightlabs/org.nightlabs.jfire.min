/**
 * 
 */
package org.nightlabs.jfire.testsuite.base;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.nightlabs.jfire.testsuite.JFireTestSuite;

/**
 * A simple TestCase for demonstration.
 * With the annotation it is linked to
 * the JFireBaseTestSuite, that will check
 * if JFireBase is deployed.
 */
@JFireTestSuite(JFireBaseTestSuite.class)
public class UserTest extends TestCase
{
	Logger logger = Logger.getLogger(UserTest.class);

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

	/**
	 * This method is invoked by the JUnit run,
	 * as its name starts with test!
	 */
	public void testCreateUser() {
		logger.info("testCreateUser: begin");
		// TODO implement!
		// It does not do very much, though
		logger.info("testCreateUser: end");
	}
	
	/**
	 * This method is invoked by the JUnit run,
	 * as it is annotated with the Test annotation.
	 */
	@Test
	public void listUsers() {
		// if fails, however ;-)
		// fail("Well, somewhere is an error.");

		logger.info("listUsers: begin");
		// TODO implement!
		logger.info("listUsers: end");
	}
}
