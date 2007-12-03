/**
 * 
 */
package org.nightlabs.jfire.testsuite.base;

import javax.jdo.PersistenceManager;

import junit.framework.TestCase;

import org.nightlabs.jfire.testsuite.TestSuite;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class JFireBaseTestSuite extends TestSuite
{

//	public JFireBaseTestSuite() {
//	}

	/**
	 * @param classes
	 */
	public JFireBaseTestSuite(Class<? extends TestCase>... classes) {
		super(classes);
		setName("JFireBase Testsuite");
	}
	/**
	 * {@inheritDoc}
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
