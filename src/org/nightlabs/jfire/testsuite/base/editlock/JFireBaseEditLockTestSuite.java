package org.nightlabs.jfire.testsuite.base.editlock;

import javax.jdo.PersistenceManager;

import junit.framework.TestCase;
import org.nightlabs.jfire.testsuite.TestSuite;

public class JFireBaseEditLockTestSuite extends TestSuite{

	public JFireBaseEditLockTestSuite(Class<? extends TestCase>... classes) {
		super(classes);
		setName("JFireBaseEditLock Testsuite");
	}
	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.testsuite.TestSuite#canRunTests(PersistenceManager)
	 */
	@Override
	public String canRunTests(PersistenceManager pm) throws Exception {
		String className = "org.nightlabs.jfire.editlock.EditLock";
		try {
			Class.forName(className);
		} catch (ClassNotFoundException x) {
			return "The module JFireBase seems not to be installed (Class \"" + className + "\" could not be found)!";
		}
		return null;
	}
	

}
