package org.nightlabs.jfire.testsuite.language;

import javax.jdo.PersistenceManager;
import junit.framework.TestCase;
import org.nightlabs.jfire.testsuite.TestSuite;


/**
*
* @author Fitas Amine - fitas [at] nightlabs [dot] de
*
*/
public class JFireBaseLanguageTestSuite extends TestSuite{

	public JFireBaseLanguageTestSuite(Class<? extends TestCase>... classes) {
		super(classes);
		setName("JFireBaseLanguageTestSuite Testsuite");
	}
	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.testsuite.TestSuite#canRunTests(PersistenceManager)
	 */
	@Override
	public String canRunTests(PersistenceManager pm) throws Exception {
		String className = "org.nightlabs.jfire.language.Language";
		try {
			Class.forName(className);
		} catch (ClassNotFoundException x) {
			return "The module JFireBase seems not to be installed (Class \"" + className + "\" could not be found)!";
		}
		return null;
	}
}
