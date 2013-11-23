package org.nightlabs.jfire.testsuite.internal;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.nightlabs.jfire.testsuite.DefaultTestListener;
import org.nightlabs.jfire.testsuite.TestSuite;

import junit.framework.Test;


/**
 * Created and populated by {@link DefaultTestListener} for all {@link TestSuite}s that are run.
 */
public class TestCaseResult {
	private TestSuiteResult testSuiteResult;
	private Class<? extends Test> testCaseClass;
	private Date startTime;
	private Date endTime;
	private boolean hasFailures;
	private List<TestResult> testResults = new LinkedList<TestResult>();
	/**
	 * Returns the endTime of this DefaultTestListener.TestCaseResult.
	 * @return the endTime.
	 */
	public Date getEndTime() {
		return endTime;
	}
	/**
	 * Sets the endTime of this DefaultTestListener.TestCaseResult.
	 * @param endTime the endTime to set.
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	/**
	 * Returns the hasFailures of this DefaultTestListener.TestCaseResult.
	 * @return the hasFailures.
	 */
	public boolean isHasFailures() {
		return hasFailures;
	}
	/**
	 * Sets the hasFailures of this DefaultTestListener.TestCaseResult.
	 * @param hasFailures the hasFailures to set.
	 */
	public void setHasFailures(boolean hasFailures) {
		this.hasFailures = hasFailures;
		if (hasFailures) {
			if (testSuiteResult != null)
				testSuiteResult.setHasFailures(true);
		}
	}
	/**
	 * Returns the startTime of this DefaultTestListener.TestCaseResult.
	 * @return the startTime.
	 */
	public Date getStartTime() {
		return startTime;
	}
	/**
	 * Sets the startTime of this DefaultTestListener.TestCaseResult.
	 * @param startTime the startTime to set.
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	/**
	 * Returns the testCaseClass of this DefaultTestListener.TestCaseResult.
	 * @return the testCaseClass.
	 */
	public Class<? extends Test> getTestCaseClass() {
		return testCaseClass;
	}
	/**
	 * Sets the testCaseClass of this DefaultTestListener.TestCaseResult.
	 * @param testCaseClass the testCaseClass to set.
	 */
	public void setTestCaseClass(Class<? extends Test> testCaseClass) {
		this.testCaseClass = testCaseClass;
	}
	/**
	 * Returns the testResults of this DefaultTestListener.TestCaseResult.
	 * @return the testResults.
	 */
	public List<TestResult> getTestResults() {
		return testResults;
	}
	/**
	 * Sets the testResults of this DefaultTestListener.TestCaseResult.
	 * @param testResults the testResults to set.
	 */
	public void setTestResults(List<TestResult> testResults) {
		this.testResults = testResults;
	}
	/**
	 * Returns the testSuiteResult of this DefaultTestListener.TestCaseResult.
	 * @return the testSuiteResult.
	 */
	public TestSuiteResult getTestSuiteResult() {
		return testSuiteResult;
	}
	/**
	 * Sets the testSuiteResult of this DefaultTestListener.TestCaseResult.
	 * @param testSuiteResult the testSuiteResult to set.
	 */
	public void setTestSuiteResult(TestSuiteResult testSuiteResult) {
		this.testSuiteResult = testSuiteResult;
	}

}