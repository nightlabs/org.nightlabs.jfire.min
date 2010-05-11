package org.nightlabs.jfire.testsuite.base.config;

import javax.jdo.PersistenceManager;

import junit.framework.TestCase;

import org.nightlabs.jfire.testsuite.TestSuite;

/**
*
* @author Fitas Amine - fitas [at] nightlabs [dot] de
*
*/
public class JFireBaseConfigTestSuite  extends TestSuite{

	public JFireBaseConfigTestSuite(Class<? extends TestCase>... classes) {
		super(classes);
		setName("JFireBaseConfig Testsuite");
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
