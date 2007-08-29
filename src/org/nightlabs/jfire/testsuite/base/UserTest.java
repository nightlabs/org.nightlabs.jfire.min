/**
 * 
 */
package org.nightlabs.jfire.testsuite.base;

import junit.framework.TestCase;

import org.junit.Test;
import org.nightlabs.jfire.testsuite.JFireTestSuite;

/**
 * A simple TestCase for demonstration.
 * With the annotation it is linked to 
 * the JFireBaseTestSuite, that will check
 * if JFireBase is deployed.
 */
@JFireTestSuite(JFireBaseTestSuite.class)
public class UserTest extends TestCase {

	/**
	 * 
	 */
	public UserTest() {
	}

	/**
	 * This method is invoked by the JUnit run, 
	 * as its name starts with test!
	 */
	public void testCreateUser() {
		// It does not do very much, though
	}
	
	/**
	 * This method is invoked by the JUnit run,
	 * as it is annotated with the Test annotation.
	 */
	@Test
	public void testListUser() {
		// if fails, however ;-)
		// fail("Well, somewhere is an error.");
	}
}
