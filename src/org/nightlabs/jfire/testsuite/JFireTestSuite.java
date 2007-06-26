/**
 * 
 */
package org.nightlabs.jfire.testsuite;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Annotation to link a TestCase to a test suite.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface JFireTestSuite {
	Class<? extends TestSuite> value(); 
}
