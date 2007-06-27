/**
 * 
 */
package org.nightlabs.jfire.testsuite;

import java.util.Properties;

import junit.framework.TestListener;

import org.nightlabs.jfire.testsuite.TestSuite.Status;

/**
 * A special JUnit test listener used within the JFireTestSuite.
 * <p>
 * JFireTestListener are registered in the JFireTestSuite properties file (jfireTestSuite.properties).
 * Each listener must have an entry as follows:
 * <pre>
 * listener.MyListenerName.class=my.fully.qualified.Class
 * </pre>
 * All properties prefixed with <code>listener.MyListenerName</code> will then be passed
 * to the listener as its configuration.
 * </p>
 * <p>
 * A listener is intantiated and used once per test run (i.e. the run of all {@link TestSuite}s).
 * It is instantiated by its default constructor, so make sure it has one.
 * </p>
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface JFireTestListener extends TestListener {

	/**
	 * Lets the Listener configure itself from the given Properties.
	 * <p>
	 * The properties will be a subset
	 * </p>
	 * @param config This listeners configuration.
	 */
	void configure(Properties config);

	/**
	 * Nofifies the listener that the test run has started.
	 * 
	 * @throws Exception When something went wrong.
	 */
	void startTestRun() throws Exception;
	
	/**
	 * Notifies the listener of the status change of the given {@link TestSuite}.
	 * This can be one of the value of {@link Status}.
	 * 
	 * @param suite The {@link TestSuite} whose status changed.
	 * @param status The new status of the suite.
	 * @throws Exception When something went wrong.
	 */
	void testSuiteStatus(TestSuite suite, TestSuite.Status status) throws Exception;
	
	/**
	 * Notifies the listnener that the start of the testsuite was skipped, because
	 * {@link TestSuite#canRunTests(javax.jdo.PersistenceManager)} threw the given
	 * exception.
	 * 
	 * @param suite The suite whose check for prerequisites failed.
	 * @param t The Throwable that was thrown.
	 */
	void addSuiteStartError(TestSuite suite, Throwable t);

	/**
	 * Notifies the listener that the test run has ended.
	 * 
	 * @throws Exception When something went wrong.
	 */
	void endTestRun() throws Exception;
}
