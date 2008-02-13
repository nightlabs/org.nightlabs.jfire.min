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
	public String canRunTests(PersistenceManager pm) throws Exception {
		String className = "org.nightlabs.jfire.security.User";
		try {
			Class.forName(className);
		} catch (ClassNotFoundException x) {
			return "The module JFireBase seems not to be installed (Class \"" + className + "\" could not be found)!";
		}
		return null;
	}

}
