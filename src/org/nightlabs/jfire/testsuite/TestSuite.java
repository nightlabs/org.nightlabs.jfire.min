/**
 * 
 */
package org.nightlabs.jfire.testsuite;

import javax.jdo.PersistenceManager;

import junit.framework.TestCase;

/**
 * A JFire {@link TestSuite} is a {@link junit.framework.TestSuite} that
 * might decide not to be run under certain circumstances. (See {@link #canRunTests(PersistenceManager)})
 * <p>
 * Additionally a JFire {@link TestSuite} knows {@link Status} that reflects
 * whether the suite was run or skipped.
 * </p>
 * <p>
 * Only the overridden constructor {@link #TestSuite(Class[])} is used and should be
 * implemented by subclasses.
 * </p>
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public abstract class TestSuite extends junit.framework.TestSuite {

	/**
	 * The status of a {@link TestSuite}.
	 */
	public enum Status {
		/**
		 * The TestSuite was started.
		 */
		START,
		/**
		 * The TestSuite finished.
		 */
		END,
		/**
		 * The TestSuite NOT run, it decided to be skipped.
		 */
		SKIP
	}

	public TestSuite(Class<? extends TestCase>... classes) {
		super(classes);
	}
	
	/**
	 * Lets the {@link TestSuite} check whether its evironment is suitable for the execution of the suite.
	 * <p>
	 * The suites are passed a PersistenceManager here, so they can access the datastore to check
	 * their prerequisites.
	 * </p>
	 * @param pm The persistenceManager to use.
	 * @return Whether this suite should be run or skipped.
	 */
	public abstract boolean canRunTests(PersistenceManager pm);
}
