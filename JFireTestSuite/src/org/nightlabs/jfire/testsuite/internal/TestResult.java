package org.nightlabs.jfire.testsuite.internal;

import java.util.Date;

import org.nightlabs.jfire.testsuite.DefaultTestListener;
import org.nightlabs.jfire.testsuite.TestSuite;

/**
 * Created and populated by {@link DefaultTestListener} for all {@link TestSuite}s that are run.
 */
public class TestResult {
	private TestCaseResult testCaseResult;
	private String testName;
	private Date startTime;
	private Date endTime;
	private boolean success = true;
	private Throwable error;

	/**
	 * Returns the endTime of this DefaultTestListener.TestResult.
	 * @return the endTime.
	 */
	public Date getEndTime() {
		return endTime;
	}
	/**
	 * Sets the endTime of this DefaultTestListener.TestResult.
	 * @param endTime the endTime to set.
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	/**
	 * Returns the error of this DefaultTestListener.TestResult.
	 * @return the error.
	 */
	public Throwable getError() {
		return error;
	}
	/**
	 * Sets the error of this DefaultTestListener.TestResult.
	 * @param error the error to set.
	 */
	public void setError(Throwable error) {
		this.error = error;
	}
	/**
	 * Returns the startTime of this DefaultTestListener.TestResult.
	 * @return the startTime.
	 */
	public Date getStartTime() {
		return startTime;
	}
	/**
	 * Sets the startTime of this DefaultTestListener.TestResult.
	 * @param startTime the startTime to set.
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	/**
	 * Returns the success of this DefaultTestListener.TestResult.
	 * @return the success.
	 */
	public boolean isSuccess() {
		return success;
	}
	/**
	 * Sets the success of this DefaultTestListener.TestResult.
	 * @param success the success to set.
	 */
	public void setSuccess(boolean success) {
		this.success = success;
		if (!success) {
			if (testCaseResult != null)
				testCaseResult.setHasFailures(true);
		}
	}
	/**
	 * Returns the test of this DefaultTestListener.TestResult.
	 * @return the test.
	 */
	public String getTestName() {
		return testName;
	}
	/**
	 * Sets the test of this DefaultTestListener.TestResult.
	 * @param test the test to set.
	 */
	public void setTestName(String testName) {
		this.testName = testName;
	}
	/**
	 * Returns the testCaseResult of this DefaultTestListener.TestResult.
	 * @return the testCaseResult.
	 */
	public TestCaseResult getTestCaseResult() {
		return testCaseResult;
	}
	/**
	 * Sets the testCaseResult of this DefaultTestListener.TestResult.
	 * @param testCaseResult the testCaseResult to set.
	 */
	public void setTestCaseResult(TestCaseResult testCaseResult) {
		this.testCaseResult = testCaseResult;
	}

}