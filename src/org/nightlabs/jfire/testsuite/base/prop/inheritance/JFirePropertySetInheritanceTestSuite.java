package org.nightlabs.jfire.testsuite.base.prop.inheritance;

import javax.jdo.PersistenceManager;

import junit.framework.TestCase;

import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.testsuite.TestSuite;

/**
 *
 * @author Frederik Loeser <!-- frederik [AT] nightlabs [DOT] de -->
 */
public class JFirePropertySetInheritanceTestSuite extends TestSuite {

	/**
	 * @param classes
	 */
	public JFirePropertySetInheritanceTestSuite(Class<? extends TestCase>... classes) {
		super(classes);
	}

	@Override
	public String canRunTests(PersistenceManager pm) throws Exception {
		PropertySetInheritanceTestStruct.getInheritanceTestStructure(SecurityReflector.getUserDescriptor().getOrganisationID(), pm);
		pm.flush();
		return null;
	}

}
