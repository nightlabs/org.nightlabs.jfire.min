/**
 * 
 */
package org.nightlabs.jfire.testsuite.base;

import junit.framework.TestCase;

import org.nightlabs.jfire.testsuite.TestSuite;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class JFireBaseTestSuite extends TestSuite {

	/**
	 * 
	 */
	public JFireBaseTestSuite() {
	}

	/**
	 * @param classes
	 */
	public JFireBaseTestSuite(Class<? extends TestCase>... classes) {
		super(classes);
		setName("JFireBase Testsuite");
	}
	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.testsuite.TestSuite#canRunTests()
	 */
	@Override
	public boolean canRunTests() {
		try {
			Class.forName("org.nightlabs.jfire.security.User");
		} catch (ClassNotFoundException x) {			
			return false;
		}
		return true;
	}

}
