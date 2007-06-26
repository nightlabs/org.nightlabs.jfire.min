/**
 * 
 */
package org.nightlabs.jfire.testsuite.base;

import junit.framework.TestCase;

import org.nightlabs.jfire.testsuite.JFireTestSuite;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
@JFireTestSuite(JFireBaseTestSuite.class)
public class UserTest extends TestCase {

	/**
	 * 
	 */
	public UserTest() {
	}

	public void testCreateUser() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void testListUser() {
		fail("Hi developers, this is the first failing JFire TestCase ;-)");
	}
}
