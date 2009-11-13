package org.nightlabs.jfire.testsuite.internal;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.nightlabs.jfire.testsuite.DefaultTestListener;
import org.nightlabs.jfire.testsuite.TestSuite;
import org.nightlabs.jfire.testsuite.TestSuite.Status;

/**
 * Created and populated by {@link DefaultTestListener} for all {@link TestSuite}s that are run.
 */
public class TestSuiteResult {
	private TestSuite suite;
	private Status status;
	private Date startTime;
	private Date endTime;
	private boolean hasFailures;
	private Throwable suiteStartError;
	private List<TestCaseResult> testCaseResults = new LinkedList<TestCaseResult>();
	/**
	 * Returns the endTime of this DefaultTestListener.TestSuiteResult.
	 * @return the endTime.
	 */
	public Date getEndTime() {
		return endTime;
	}
	/**
	 * Sets the endTime of this DefaultTestListener.TestSuiteResult.
	 * @param endTime the endTime to set.
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	/**
	 * Returns the hasFailures of this DefaultTestListener.TestSuiteResult.
	 * @return the hasFailures.
	 */
	public boolean isHasFailures() {
		return hasFailures;
	}
	/**
	 * Sets the hasFailures of this DefaultTestListener.TestSuiteResult.
	 * @param hasFailures the hasFailures to set.
	 */
	public void setHasFailures(boolean hasFailures) {
		this.hasFailures = hasFailures;
	}
	/**
	 * Returns the startTime of this DefaultTestListener.TestSuiteResult.
	 * @return the startTime.
	 */
	public Date getStartTime() {
		return startTime;
	}
	/**
	 * Sets the startTime of this DefaultTestListener.TestSuiteResult.
	 * @param startTime the startTime to set.
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	/**
	 * Returns the status of this DefaultTestListener.TestSuiteResult.
	 * @return the status.
	 */
	public Status getStatus() {
		return status;
	}
	/**
	 * Sets the status of this DefaultTestListener.TestSuiteResult.
	 * @param status the status to set.
	 */
	public void setStatus(Status status) {
		this.status = status;
	}
	/**
	 * Returns the suite of this DefaultTestListener.TestSuiteResult.
	 * @return the suite.
	 */
	public TestSuite getSuite() {
		return suite;
	}
	/**
	 * Sets the suite of this DefaultTestListener.TestSuiteResult.
	 * @param suite the suite to set.
	 */
	public void setSuite(TestSuite suite) {
		this.suite = suite;
	}
	/**
	 * Returns the testCaseResults of this DefaultTestListener.TestSuiteResult.
	 * @return the testCaseResults.
	 */
	public List<TestCaseResult> getTestCaseResults() {
		return testCaseResults;
	}
	/**
	 * Sets the testCaseResults of this DefaultTestListener.TestSuiteResult.
	 * @param testCaseResults the testCaseResults to set.
	 */
	public void setTestCaseResults(List<TestCaseResult> testCaseResults) {
		this.testCaseResults = testCaseResults;
	}
	/**
	 * Returns the suiteStartError of this DefaultTestListener.TestSuiteResult.
	 * @return the suiteStartError.
	 */
	public Throwable getSuiteStartError() {
		return suiteStartError;
	}
	/**
	 * Sets the suiteStartError of this DefaultTestListener.TestSuiteResult.
	 * @param suiteStartError the suiteStartError to set.
	 */
	public void setSuiteStartError(Throwable suiteStartError) {
		this.suiteStartError = suiteStartError;
	}
}