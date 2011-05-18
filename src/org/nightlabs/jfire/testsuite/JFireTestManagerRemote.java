package org.nightlabs.jfire.testsuite;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.testsuite.id.TestCaseObjectsMapID;
import org.nightlabs.jfire.timer.id.TaskID;

@Remote
public interface JFireTestManagerRemote {

	void logMemoryState(TaskID taskID) throws Exception;

//	void runAllTestSuites(TaskID taskID) throws Exception;

	/**
	 * This method is called by the datastore initialisation mechanism.
	 * It initializes the users needed for Test logins and other prerequisites for the Test system.
	 *
	 * @throws Exception When something went wrong.
	 */
	void initialiseTestSystem() throws Exception;

//	/**
//	 * Runs all TestSuits and TestCases found in the classpath under org.nightlabs.jfire.testsuite.
//	 * This method can be called by clients and is called by the {@link JFireTestRunnerInvocation} on every startup.
//	 */
//	void runAllTestSuites() throws SecurityException, IllegalArgumentException,
//			ClassNotFoundException, NoSuchMethodException,
//			InstantiationException, IllegalAccessException,
//			InvocationTargetException, ModuleException, IOException;

//	/**
//	 * Runs TestCases found in the classpath under org.nightlabs.jfire.testsuite that belong to the given TestSuites.
//	 */
//	void runTestSuites(List<Class<? extends TestSuite>> testSuitesClasses)
//			throws SecurityException, IllegalArgumentException,
//			ClassNotFoundException, NoSuchMethodException,
//			InstantiationException, IllegalAccessException,
//			InvocationTargetException, ModuleException, IOException;

	/**
	 * very useful method to check if a particular JDO Object exist in the data store.
	 */
	boolean isJDOObjectExisting(ObjectID objectID);
	
	Object getObject(ObjectID objectID);
	
	void storeObject(Object object);
	
	/**
	 * Deletes the object corresponding to the given objectID within a new transaction.
	 * 
	 * @param objectID corresponds to the object to delete.
	 */
	void deleteObject(ObjectID objectID);
	
	TestCaseObjectsMap storeTestCaseObjectsMap(TestCaseObjectsMap testCaseObjectsMap, Boolean get, String[] fetchGroups, int maxFetchDepth);	

	Set<TestCaseObjectsMapID> getTestCaseObjectsMapIDs();
	
	List<TestCaseObjectsMap> getTestCaseObjectsMaps(Collection<TestCaseObjectsMapID> testCaseObjectsMapIDs, String[] fetchGroups, int maxFetchDepth);
	
	void deleteTestCaseObjectsMap(TestCaseObjectsMapID testCaseObjectsMapID);
	
	String runTestAsync(Class<?> testClass) throws Exception;
	
	List<JFireTestSuiteNotification> getTestNotifications(String identifier);
}