/**
 *
 */
package org.nightlabs.jfire.testsuite;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.runner.BaseTestRunner;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.UserDescriptor;
import org.nightlabs.jfire.testsuite.TestSuite.Status;

/**
 * Runner for JFire {@link TestSuite}s.
 * Accepts {@link JFireTestListener}s and notifies them of the
 * test suites {@link Status}.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class JFireTestRunner extends BaseTestRunner {

	/**
	 * Log4J Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(JFireTestRunner.class);

	/**
	 * The listener
	 */
	private List<JFireTestListener> testListeners = new LinkedList<JFireTestListener>();

	/**
	 *
	 */
	public JFireTestRunner() {
	}

	private void notifyTestSuiteStatus(TestSuite suite, TestSuite.Status status) {
		for (JFireTestListener listener : new ArrayList<JFireTestListener>(testListeners)) {
			try {
				listener.testSuiteStatus(suite, status);
			} catch (Exception e) {
				logger.error("Error notifying JFireTestListener!", e);
			}
		}
	}

	private void notifySuiteStartError(TestSuite suite, Throwable t) {
		for (JFireTestListener listener : new ArrayList<JFireTestListener>(testListeners)) {
			try {
				listener.addSuiteStartError(suite, t);
			} catch (Exception e) {
				logger.error("Error notifying JFireTestListener!", e);
			}
		}
	}

	/**
	 * Add a {@link JFireTestListener} to this runner.
	 * @param listener The listener to add.
	 */
	public void addListener(JFireTestListener listener) {
		synchronized (testListeners) {
			testListeners.add(listener);
		}
	}

	/**
	 * Removes a {@link JFireTestListener} from this runner.
	 * @param listener The listener to remove.
	 */
	public void removeListener(JFireTestListener listener) {
		synchronized (testListeners) {
			testListeners.remove(listener);
		}
	}

	private static class TestSuiteWithNestedTx extends junit.framework.TestSuite {

		/**
		 * This implementation of {@link #runTest(Test, TestResult)} delegates to an EJB which ensures a nested transaction.
		 * It is used by the {@link JFireTestRunner#run(TestSuite)} method to repackage all tests, since
		 * jUnit already repackages them once. Hence, subclassing this TestSuite doesn't help - the actual tests
		 * will still end up in an instance of {@link junit.framework.TestSuite} (not {@link TestSuite}). Hence,
		 * we repackage again and put them in an instance of this class.
		 */
		@Override
		public void runTest(Test test, TestResult result)
		{
			// TestResult is not serialisable. We pray that the container will pass the reference
			// *directly* to the *local* EJB without serialising/deserialising.

			UserDescriptor userDescriptorOnStart = SecurityReflector.getUserDescriptor();

			if (test instanceof TestCase) {
				// If it's a JFire-TestCase, the TestCase-implementation takes care about the transactions, so we directly
				// call the test's run method (done by super.runTest(...)).
				super.runTest(test, result);
			}
			else {
				// If it's an ordinary TestCase, we run it in a nested transaction here.
				try {
					JFireTestManagerLocal m = JFireEjb3Factory.getLocalBean(JFireTestManagerLocal.class);
					m.runTestInNestedTransaction(test, result);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			UserDescriptor userDescriptorNow = SecurityReflector.getUserDescriptor();
			if (!userDescriptorOnStart.equals(userDescriptorNow))
				throw new IllegalStateException("SecurityReflector.getUserDescriptor() returned a different user now (after running the test) than at the beginning of this method! test=" + test + " start=" + userDescriptorOnStart + " now=" + userDescriptorNow);
		}
	}

	/**
	 * Runs the given JFire {@link TestSuite} and notifies the
	 * registered {@link JFireTestListener}s of the status.
	 *
	 * @param suite The suite to run.
	 * @param pm The PersistenceManager that can be passed to the test suites.
	 */
	public void run(TestSuite suite) {
		String skipReason;
		Throwable checkError = null;
		try {
			JFireTestManagerLocal m = JFireEjb3Factory.getLocalBean(JFireTestManagerLocal.class);
			skipReason = m.evaluateCanRunTestsInNestedTransaction(suite);
//			skipReason = suite.canRunTests(pm);
		} catch (Exception e) {
			skipReason = e.getClass().getName() + ": " + e.getMessage();
			checkError = e;
		}
		if (skipReason != null) {
			notifyTestSuiteStatus(suite, Status.SKIP); // TODO we need to pass an Event object here in order to pass the skipReason
			if (checkError != null) {
				notifySuiteStartError(suite, checkError);
			}
			return;
		}
		notifyTestSuiteStatus(suite, Status.START);
		try {
			for (int i = 0; i < suite.testCount(); i++) {
				Test test = suite.testAt(i);
				TestResult result = new TestResult();
				for (JFireTestListener listener : new ArrayList<JFireTestListener>(testListeners)) {
					result.addListener(listener);
				}
//				test.run(result);
				// We cannot directly call it, because we want separate transactions => need to delegate to an EJB with transaction-tag "RequiresNew"
				// Here, test is normally an instance of TestSuite. If we directly passed this TestSuite to an EJB, it would execute all test-methods
				// in the same transaction - which we don't want.

				if (test instanceof junit.framework.TestSuite) { // this should always be the case, but just to play safe.
					junit.framework.TestSuite ts = (junit.framework.TestSuite) test;
					TestSuiteWithNestedTx testSuiteWithNestedTx = new TestSuiteWithNestedTx();
					testSuiteWithNestedTx.setName(ts.getName());
					for (Enumeration<Test> te = ts.tests(); te.hasMoreElements(); ) {
						Test t = te.nextElement();
						testSuiteWithNestedTx.addTest(t);
					}

					testSuiteWithNestedTx.run(result);
				}
				else {
					// TestResult is not serialisable. We pray that the container will pass the reference
					// *directly* to the *local* EJB without serialising/deserialising.
					try {
						JFireTestManagerLocal m = JFireEjb3Factory.getLocalBean(JFireTestManagerLocal.class);
						m.runTestInNestedTransaction(test, result);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		} finally {
			notifyTestSuiteStatus(suite, Status.END);
		}
	}

	/**
	 * {@inheritDoc}
	 * @see junit.runner.BaseTestRunner#runFailed(java.lang.String)
	 */
	@Override
	protected void runFailed(String message) {
	}

	/**
	 * {@inheritDoc}
	 * @see junit.runner.BaseTestRunner#testEnded(java.lang.String)
	 */
	@Override
	public void testEnded(String testName) {
	}

	/**
	 * {@inheritDoc}
	 * @see junit.runner.BaseTestRunner#testFailed(int, junit.framework.Test, java.lang.Throwable)
	 */
	@Override
	public void testFailed(int status, Test test, Throwable t) {
	}

	/**
	 * {@inheritDoc}
	 * @see junit.runner.BaseTestRunner#testStarted(java.lang.String)
	 */
	@Override
	public void testStarted(String testName) {
	}

}
