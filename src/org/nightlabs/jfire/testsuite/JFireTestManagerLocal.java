package org.nightlabs.jfire.testsuite;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.Local;

import junit.framework.Test;
import junit.framework.TestResult;

import org.nightlabs.ModuleException;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.testsuite.id.TestCaseObjectsMapID;
import org.nightlabs.jfire.timer.id.TaskID;

@Local
public interface JFireTestManagerLocal {

	void logMemoryState(TaskID taskID) throws Exception;

//	/**
//	 * Runs all TestSuits and TestCases found in the classpath under org.nightlabs.jfire.testsuite.
//	 * This method can be called by clients and is called by the {@link JFireTestRunnerInvocation} on every startup.
//	 */
//	void runAllTestSuites() throws SecurityException, IllegalArgumentException,
//			ClassNotFoundException, NoSuchMethodException,
//			InstantiationException, IllegalAccessException,
//			InvocationTargetException, ModuleException, IOException;
//
//	/**
//	 * Runs TestCases found in the classpath under org.nightlabs.jfire.testsuite that belong to the given TestSuites.
//	 */
//	void runTestSuites(List<Class<? extends TestSuite>> testSuitesClasses)
//			throws SecurityException, IllegalArgumentException,
//			ClassNotFoundException, NoSuchMethodException,
//			InstantiationException, IllegalAccessException,
//			InvocationTargetException, ModuleException, IOException;



	String evaluateCanRunTestsInNestedTransaction(TestSuite testSuite) throws Exception;

	void runTestInNestedTransaction(Test test, TestResult result) throws Exception;

	public void runTestInNestedTransaction_setUpBeforeClass(org.nightlabs.jfire.testsuite.TestCase test) throws Exception;

	public void runTestInNestedTransaction_cleanUpAfterClass(org.nightlabs.jfire.testsuite.TestCase test) throws Exception;
	
	public void runTestInNestedTransaction_setUp(org.nightlabs.jfire.testsuite.TestCase test) throws Exception;

	public void runTestInNestedTransaction_tearDown(org.nightlabs.jfire.testsuite.TestCase test) throws Exception;

	public void runTestInNestedTransaction_runTest(org.nightlabs.jfire.testsuite.TestCase test) throws Exception;
				
	public boolean isJDOObjectExisting(ObjectID objectID);
	
	public void deleteTestCaseObjectsMap(TestCaseObjectsMapID testCaseObjectsMapID);
	
	public Set<TestCaseObjectsMapID> getTestCaseObjectsMapIDs();
	
	public List<TestCaseObjectsMap> getTestCaseObjectsMaps(Collection<TestCaseObjectsMapID> testCaseObjectsMapIDs, String[] fetchGroups, int maxFetchDepth);

	public TestCaseObjectsMap storeTestCaseObjectsMap(TestCaseObjectsMap testCaseObjectsMap, Boolean get, String[] fetchGroups, int maxFetchDepth);	
}