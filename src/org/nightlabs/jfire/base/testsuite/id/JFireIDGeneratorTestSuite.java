package org.nightlabs.jfire.base.testsuite.id;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.testsuite.TestSuite;

import junit.framework.TestCase;

public class JFireIDGeneratorTestSuite extends TestSuite{
	
//	public JFireBaseTestSuite() {
//	}

	/**
	 * @param classes
	 */
	public JFireIDGeneratorTestSuite(Class<? extends TestCase>... classes) {
		super(classes);
		setName("JFireIDGeneratorTestSuite Testsuite");
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
