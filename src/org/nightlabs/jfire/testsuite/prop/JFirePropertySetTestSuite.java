/**
 * 
 */
package org.nightlabs.jfire.testsuite.prop;

import javax.jdo.PersistenceManager;

import junit.framework.TestCase;

import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.testsuite.TestSuite;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class JFirePropertySetTestSuite extends TestSuite {

	/**
	 * @param classes
	 */
	public JFirePropertySetTestSuite(Class<? extends TestCase>... classes) {
		super(classes);
	}

	/** {@inheritDoc}
	 */
	@Override
	public String canRunTests(PersistenceManager pm) throws Exception {
		PropertySetTestStruct.getTestStruct(SecurityReflector.getUserDescriptor().getOrganisationID(), pm);
		pm.flush();
		return null;
	}

}
