/**
 * 
 */
package org.nightlabs.jfire.testsuite;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.jdo.PersistenceManager;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.runner.BaseTestRunner;

import org.apache.log4j.Logger;
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
	
	/**
	 * Runs the given JFire {@link TestSuite} and notifies the
	 * registered {@link JFireTestListener}s of the status.
	 * 
	 * @param suite The suite to run.
	 * @param pm The PersistenceManager that can be passed to the test suites.
	 */
	public void run(TestSuite suite, PersistenceManager pm) {
		String skipReason;
		Throwable checkError = null;
		try {
			skipReason = suite.canRunTests(pm);
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
				test.run(result);
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
