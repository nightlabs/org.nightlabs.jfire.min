package org.nightlabs.jfire.testsuite.workstation;


import javax.jdo.PersistenceManager;

import junit.framework.TestCase;

import org.nightlabs.jfire.testsuite.JFireTestSuite;
import org.nightlabs.jfire.testsuite.TestSuite;


/**
*
* @author Fitas Amine - fitas [at] nightlabs [dot] de
*
*/
public class JFireBaseWorkstationTestSuite extends TestSuite{
	
	public JFireBaseWorkstationTestSuite(Class<? extends TestCase>... classes) {
		super(classes);
		setName("JFireBaseWorkstation Testsuite");
	}
	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.testsuite.TestSuite#canRunTests(PersistenceManager)
	 */
	@Override
	public String canRunTests(PersistenceManager pm) throws Exception {
		String className = "org.nightlabs.jfire.workstation.Workstation";
		try {
			Class.forName(className);
		} catch (ClassNotFoundException x) {
			return "The module JFireBase seems not to be installed (Class \"" + className + "\" could not be found)!";
		}
		return null;
	}
	
}
