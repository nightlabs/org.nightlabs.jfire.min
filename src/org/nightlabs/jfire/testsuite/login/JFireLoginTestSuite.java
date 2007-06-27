/**
 * 
 */
package org.nightlabs.jfire.testsuite.login;

import javax.jdo.PersistenceManager;

import junit.framework.TestCase;

import org.nightlabs.jfire.testsuite.TestSuite;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class JFireLoginTestSuite extends TestSuite {

	/**
	 * @param classes
	 */
	public JFireLoginTestSuite(Class<? extends TestCase>... classes) {
		super(classes);
		setName("Test JFire Login");
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation checks whether "francois@chezfrancois.jfire.org" is there.
	 * </p>
	 * @see org.nightlabs.jfire.testsuite.TestSuite#canRunTests(PersistenceManager)
	 */
	@Override
	public boolean canRunTests(PersistenceManager pm) {
		try {
			Class.forName("org.nightlabs.jfire.security.User");
		} catch (ClassNotFoundException x) {			
			return false;
		}
		return JFireLoginTestHelper.checkUser(pm);
	}

}
