/**
 * 
 */
package org.nightlabs.jfire.testsuite;

import javax.jdo.PersistenceManager;

import junit.framework.TestCase;

/**
 * The default {@link TestSuite} within all {@link TestCase}s that
 * are not linked to one are run in.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class DefaultTestSuite extends TestSuite {

	public DefaultTestSuite() {
	}

	public DefaultTestSuite(Class<? extends TestCase>... classes) {
		super(classes);
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.testsuite.TestSuite#canRunTests(PersistenceManager)
	 */
	@Override
	public boolean canRunTests(PersistenceManager pm) {
		return true;
	}

}
