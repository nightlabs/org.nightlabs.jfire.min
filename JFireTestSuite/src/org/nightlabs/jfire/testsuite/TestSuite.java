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
 * Additionally a JFire {@link TestSuite} knows a {@link Status} that reflects
 * whether the suite was run or skipped.
 * </p>
 * <p>
 * Only the overridden constructor {@link #TestSuite(Class[])} is used and should be
 * implemented by subclasses.
 * </p>
 * <p>
 * <b>Important:</b> Extendors of this class must provide the constructor
 * <pre>
 * public MyTestSuite(Class&lt;? extends TestCase&gt;... classes) {
 *   super(classes);
 * }</pre>
 * in order to be able to be ran.
 * </p>
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public abstract class TestSuite extends junit.framework.TestSuite
{
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
		 * The TestSuite did NOT run, it decided to be skipped.
		 */
		SKIP
	}

	public TestSuite(Class<? extends TestCase>... classes) {
		super(classes);
	}

	/**
	 * Lets the {@link TestSuite} check whether its environment is suitable for the execution of the suite.
	 * <p>
	 * The suites are passed a PersistenceManager here, so they can access the datastore to check
	 * their prerequisites.
	 * </p>
	 * @param pm The persistenceManager to use.
	 * @return <code>null</code>, if everything is fine and this suite should be run or a <code>String</code> describing why it should be skipped.
	 * @throws Exception When something fails
	 */
	public abstract String canRunTests(PersistenceManager pm) throws Exception;

}
