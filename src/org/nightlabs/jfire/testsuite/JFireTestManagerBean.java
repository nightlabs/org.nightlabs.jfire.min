/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.testsuite;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jfire.asyncinvoke.AsyncInvoke;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.SecurityReflector.UserDescriptor;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.shutdownafterstartup.ShutdownControlHandle;
import org.nightlabs.jfire.testsuite.login.JFireTestLogin;
import org.nightlabs.jfire.testsuite.prop.PropertySetTestStruct;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.util.reflect.ReflectUtil;


/**
 * @ejb.bean name="jfire/ejb/JFireTestSuite/JFireTestManager"
 *					 jndi-name="jfire/ejb/JFireTestSuite/JFireTestManager"
 *					 type="Stateless"
 *					 transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
public abstract class JFireTestManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final long serialVersionUID = 1L;
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(JFireTestManagerBean.class);

	@Override
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	@Override
	public void unsetSessionContext() {
		super.unsetSessionContext();
	}

	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate() throws CreateException
	{
	}
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 *
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException { }

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	public void logMemoryState(TaskID taskID)
	throws Exception
	{
		logger.info("testSuiteTimerTask: Memory usage before gc:");
		logMemoryState();

		System.gc();

		logger.info("testSuiteTimerTask: Memory usage after gc:");
		logMemoryState();
	}

	private void logMemoryState()
	{
		Runtime runtime = Runtime.getRuntime();
		long maxMemory = runtime.maxMemory();
		long totalMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		long usedMemory = totalMemory - freeMemory;
		logger.info("logMemoryState: maxMemory = " + maxMemory / 1024 + " KB");
		logger.info("logMemoryState: totalMemory = " + totalMemory / 1024 + " KB");
		logger.info("logMemoryState: usedMemory = " + usedMemory / 1024 + " KB");
		logger.info("logMemoryState: freeMemory = " + freeMemory / 1024 + " KB");
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	public void runAllTestSuites(TaskID taskID)
	throws Exception
	{
		if (getTestSuiteRunningCounter(getOrganisationID()) > 0) {
			logger.info("runAllTestSuites: Tests are already running. Won't start another run for the timer task!");
			return;
		}

		runAllTestSuites();
	}

	/**
	 * This method is called by the datastore initialisation mechanism.
	 * It initializes the users needed for Test logins and other prerequisites for the Test system.
	 *
	 * @throws Exception When something went wrong.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @!ejb.permission unchecked="true"
	 * @ejb.transaction type="Required"
	 */
	public void initialiseTestSystem()
	throws Exception
	{
		// do not run tests on the root organisation
		if (hasRootOrganisation() && getRootOrganisationID().equals(getOrganisationID()))
			return;

		JFireServerManager jfsm = getJFireServerManager();
		try {
			PersistenceManager pm = getPersistenceManager();
			try {

				{
					TaskID taskID = TaskID.create(getOrganisationID(), Task.TASK_TYPE_ID_SYSTEM, "runAllTestSuites");
					try {
						pm.getObjectById(taskID);
					} catch (JDOObjectNotFoundException x) {
						Task task = new Task(
								taskID,
								User.getUser(pm, getPrincipal()),
								JFireTestManagerHome.JNDI_NAME,
								"runAllTestSuites"
						);

						task.getName().setText(Locale.ENGLISH.getLanguage(), "JFire test suite");
						task.getDescription().setText(Locale.ENGLISH.getLanguage(), "Run all tests.");

						task.getTimePatternSet().createTimePattern(
								"*", // year
								"*", // month
								"*", // day
								"*", // dayOfWeek
								"*", //  hour
								"/30" // minute
						);

						task.setEnabled(true);
						pm.makePersistent(task);
					}
				}

				{
					TaskID taskID = TaskID.create(getOrganisationID(), Task.TASK_TYPE_ID_SYSTEM, "logMemoryState");
					try {
						pm.getObjectById(taskID);
					} catch (JDOObjectNotFoundException x) {
						Task task = new Task(
								taskID,
								User.getUser(pm, getPrincipal()),
								JFireTestManagerHome.JNDI_NAME,
								"logMemoryState"
						);

						task.getName().setText(Locale.ENGLISH.getLanguage(), "Log memory state");
						task.getDescription().setText(Locale.ENGLISH.getLanguage(), "Logs the memory state, performs garbage collection, and logs again.");

						task.getTimePatternSet().createTimePattern(
								"*", // year
								"*", // month
								"*", // day
								"*", // dayOfWeek
								"*", //  hour
								"*" // minute
						);

						task.setEnabled(true);
						pm.makePersistent(task);
					}
				}


				JFireTestLogin.checkCreateLoginsAndRegisterInAuthorities(pm);
				PropertySetTestStruct.getTestStruct(getOrganisationID(), pm);

				boolean runOnStartup;
				String runOnStartupStr = System.getProperty(JFireTestManager.class.getName() + ".runOnStartup");
				if (runOnStartupStr == null || runOnStartupStr.isEmpty())
					runOnStartup = true;
				else
					runOnStartup = Boolean.parseBoolean(runOnStartupStr);

				if (runOnStartup) {
					ShutdownControlHandle shutdownControlHandle = jfsm.shutdownAfterStartup_createShutdownControlHandle();

					// This invocation will only be started after all organisation-inits have completed,
					// because no async-invocation is executed by the framework before completion of startup.
					JFireTestRunnerInvocation invocation = new JFireTestRunnerInvocation(shutdownControlHandle);
					AsyncInvoke.exec(invocation, true);
				} // if (runOnStartup) {

			} finally {
				pm.close();
			}
		} finally {
			jfsm.close();
		}
	}

	private static Map<String, Integer> organisationID2testSuiteRunningCounter = new HashMap<String, Integer>();

	protected static int getTestSuiteRunningCounter(String organisationID)
	{
		synchronized (organisationID2testSuiteRunningCounter) {
			Integer counter = organisationID2testSuiteRunningCounter.get(organisationID);
			if (counter == null)
				return 0;

			return counter.intValue();
		}
	}

	private static int incrementTestSuiteRunningCounter(String organisationID)
	{
		synchronized (organisationID2testSuiteRunningCounter) {
			Integer counter = organisationID2testSuiteRunningCounter.get(organisationID);
			if (counter == null)
				counter = 1;
			else
				counter = counter.intValue() + 1;

			organisationID2testSuiteRunningCounter.put(organisationID, counter);
			return counter.intValue();
		}
	}

	private static int decrementTestSuiteRunningCounter(String organisationID)
	{
		synchronized (organisationID2testSuiteRunningCounter) {
			Integer counter = organisationID2testSuiteRunningCounter.get(organisationID);
			if (counter == null)
				throw new IllegalStateException("No counter found! Cannot decrement! You're trying to decrement without having incremented first!");

			counter = counter.intValue() - 1;

			if (counter.intValue() < 0)
				throw new IllegalStateException("Counter became negative!");

			if (counter.intValue() == 0)
				organisationID2testSuiteRunningCounter.remove(organisationID);
			else
				organisationID2testSuiteRunningCounter.put(organisationID, counter);

			return counter.intValue();
		}
	}

	/**
	 * Runs all TestSuits and TestCases found in the classpath under org.nightlabs.jfire.testsuite.
	 * This method can be called by clients and is called by the {@link JFireTestRunnerInvocation} on every startup.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.permission unchecked="true"
	 */
	public void runAllTestSuites()
	throws SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ModuleException, IOException
	{
		List<TestSuite> runSuites = createTestSuites(null);
		runTestSuiteInstances(runSuites);
	}

	/**
	 * Runs TestCases found in the classpath under org.nightlabs.jfire.testsuite that belong to the given TestSuites..
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void runTestSuites(List<Class<? extends TestSuite>> testSuitesClasses)
	throws SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ModuleException, IOException
	{
		List<TestSuite> runSuites = createTestSuites(testSuitesClasses);
		runTestSuiteInstances(runSuites);
	}

	private static void runTestSuiteInstances(List<TestSuite> testSuites) throws ModuleException, IOException {
		List<JFireTestListener> listeners = getTestListeners();
		UserDescriptor userDescriptorOnStart = SecurityReflector.getUserDescriptor();
		if (userDescriptorOnStart == null)
			throw new IllegalStateException("SecurityReflector.getUserDescriptor() returned null!");

		incrementTestSuiteRunningCounter(userDescriptorOnStart.getOrganisationID());
		try {

			// Run the suites
			for (JFireTestListener listener : listeners) {
				try {
					listener.startTestRun();
				} catch (Exception e) {
					logger.error("Error notifying JFireTestListener!", e);
					continue;
				}
				UserDescriptor userDescriptorNow = SecurityReflector.getUserDescriptor();
				if (!userDescriptorOnStart.equals(userDescriptorNow))
					throw new IllegalStateException("SecurityReflector.getUserDescriptor() returned a different user now (after having called listener.startTestRun()) than at the beginning of this method! listener=" + listener + " start=" + userDescriptorOnStart + " now=" + userDescriptorNow);
			}
			for (TestSuite suite : testSuites) {
				JFireTestRunner runner = new JFireTestRunner();
				for (JFireTestListener listener : listeners) {
					runner.addListener(listener);
				}

				runner.run(suite);

				UserDescriptor userDescriptorNow = SecurityReflector.getUserDescriptor();
				if (!userDescriptorOnStart.equals(userDescriptorNow))
					throw new IllegalStateException("SecurityReflector.getUserDescriptor() returned a different user now (after having called runner.run(suite)) than at the beginning of this method! suite=" + suite + " start=" + userDescriptorOnStart + " now=" + userDescriptorNow);
			}
			for (JFireTestListener listener : listeners) {
				try {
					listener.endTestRun();
				} catch (Exception e) {
					logger.error("Error notifying JFireTestListener!", e);
					continue;
				}
				UserDescriptor userDescriptorNow = SecurityReflector.getUserDescriptor();
				if (!userDescriptorOnStart.equals(userDescriptorNow))
					throw new IllegalStateException("SecurityReflector.getUserDescriptor() returned a different user now (after having called listener.endTestRun()) than at the beginning of this method! listener=" + listener + " start=" + userDescriptorOnStart + " now=" + userDescriptorNow);
			}

		} finally {
			decrementTestSuiteRunningCounter(userDescriptorOnStart.getOrganisationID());
		}
	}

	/**
	 * Executes {@link TestSuite#canRunTests(PersistenceManager)} within a nested transaction. It is invoked by
	 * {@link JFireTestRunner#run(TestSuite, PersistenceManager)}
	 * <p>
	 * This only works if the JavaEE container passes the references, because {@link junit.framework.TestSuite}
	 * is not {@link java.io.Serializable}.
	 * </p>
	 *
	 * @ejb.interface-method view-type="local"
	 * @ejb.permission unchecked="true"
	 * @ejb.transaction type="RequiresNew"
	 */
	public String evaluateCanRunTestsInNestedTransaction(TestSuite testSuite)
	throws Exception
	{
		logger.info("evaluateCanRunTestsInNestedTransaction: " + testSuite.toString());
		PersistenceManager pm = getPersistenceManager();
		try {
			return testSuite.canRunTests(pm);
		} finally {
			pm.close();
		}
	}

	/**
	 * This only works if the JavaEE container passes the references, because neither {@link org.junit.Test} nor {@link TestResult}
	 * are {@link java.io.Serializable}, the {@link TestResult} is not a return value, but the data is directly written into it and
	 * there are listeners passed inside the parameter-objects.
	 *
	 * @param test The test to be run.
	 * @param result The result into which the test's execution result will be written.
	 *
	 * @ejb.interface-method view-type="local"
	 * @ejb.permission unchecked="true"
	 * @ejb.transaction type="RequiresNew"
	 */
	public void runTestInNestedTransaction(Test test, TestResult result)
	throws Exception
	{
		logger.info("runTestInNestedTransaction: " + test.toString());
		test.run(result);
	}

	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.permission unchecked="true"
	 * @ejb.transaction type="RequiresNew"
	 */
	public void runTestInNestedTransaction_setUp(org.nightlabs.jfire.testsuite.TestCase test)
	throws Exception
	{
		test.setUp();
	}

	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.permission unchecked="true"
	 * @ejb.transaction type="RequiresNew"
	 */
	public void runTestInNestedTransaction_tearDown(org.nightlabs.jfire.testsuite.TestCase test)
	throws Exception
	{
		test.tearDown();
	}

	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.permission unchecked="true"
	 * @ejb.transaction type="RequiresNew"
	 */
	public void runTestInNestedTransaction_runTest(org.nightlabs.jfire.testsuite.TestCase test)
	throws Exception
	{
		try {
			test.runTest();
		} catch (Exception x) {
			throw x;
		} catch (Throwable t) {
			throw new InternalWrapperException(t);
		}
	}

	private static List<JFireTestListener> getTestListeners() throws ModuleException, IOException {
		Properties mainProps = JFireTestSuiteEAR.getJFireTestSuiteProperties();
		Collection<Matcher> listenerMatches = JFireTestSuiteEAR.getPropertyKeyMatches(mainProps, Pattern.compile("(listener\\.(?:[^.]*?)\\.)class"));
		List<JFireTestListener> listeners = new LinkedList<JFireTestListener>();
		for (Matcher matcher : listenerMatches) {
			Properties listenerProps = JFireTestSuiteEAR.getProperties(mainProps, matcher.group(1));
			Class<?> clazz = null;
			try {
				clazz = Class.forName(listenerProps.getProperty("class"));
			} catch (Exception e) {
				logger.error("Could not relove listener class " + listenerProps.getProperty("class"), e);
				continue;
			}
			JFireTestListener listener = null;
			try {
				listener = (JFireTestListener) clazz.newInstance();
			} catch (Exception e) {
				logger.error("Could not instantiate test listener " + clazz.getName(), e);
				continue;
			}
			listener.configure(listenerProps);
			listeners.add(listener);
		}

		return listeners;
	}

	/**
	 * Searches the classpath for TestSuites and TestCases and creates all TestSuites and TestCases that belong to one of the
	 * TestSuite classes in <code>testSuiteClassFilter</code>. If <code>testSuiteClassFilter == null</code>, all encountered TestSuites
	 * and TestCases are created.
	 *
	 * @param testSuiteClassesFilter Restricts the created Test{Cases,Suites} to the given classes. Can be null to indicate no restriction, i.e. all
	 * 		encountered Test{Cases,Suites} will be created.
	 * @return A list of the created TestSuites.
	 * @throws ClassNotFoundException
	 */
	private static List<TestSuite> createTestSuites(List<Class<? extends TestSuite>> testSuiteClassesFilter) throws ClassNotFoundException {
		logger.debug("Scanning classpath for TestSuites and TestCases");
		Collection<Class<?>> classes = ReflectUtil.listClassesInPackage("org.nightlabs.jfire.testsuite", true);
		logger.debug("Found " + classes.size() + " classes");

		List<Class<? extends TestSuite>> testSuiteClasses = new LinkedList<Class<? extends TestSuite>>();

		Map<Class<? extends TestSuite>, List<Class<? extends TestCase>>> suites2TestCases = new HashMap<Class<? extends TestSuite>, List<Class<? extends TestCase>>>();
		for (Class<?> clazz : classes) {
			if (TestSuite.class.isAssignableFrom(clazz)) {
				Class<? extends TestSuite> suiteClass = (Class<? extends TestSuite>) clazz;
				testSuiteClasses.add(suiteClass);
			} else if (TestCase.class.isAssignableFrom(clazz)) {
				if ((clazz.getModifiers() & Modifier.ABSTRACT) != 0) // ignore abstract classes since they are base-classes and no test-cases themselves
					continue;

				Class<? extends TestCase> testCaseClass = (Class<? extends TestCase>) clazz;
				JFireTestSuite testSuiteAnnotation = clazz.getAnnotation(JFireTestSuite.class);
				Class<? extends TestSuite> suiteClass = DefaultTestSuite.class; // Default value, if not annotated.
				if (testSuiteAnnotation != null) {
					suiteClass = testSuiteAnnotation.value();
				}
				List<Class<? extends TestCase>> testCaseClasses = suites2TestCases.get(suiteClass);
				if (testCaseClasses == null) {
					testCaseClasses = new LinkedList<Class<? extends TestCase>>();
					suites2TestCases.put(suiteClass, testCaseClasses);
				}
				testCaseClasses.add(testCaseClass);
			}
		}

		// now iterate all registrations and find TestCases that have a Suite set that's not in the classpath
		for (Class<? extends TestSuite> suiteClass : suites2TestCases.keySet()) {
			if (!testSuiteClasses.contains(suiteClass)) {
				testSuiteClasses.add(suiteClass);
			}
		}

		// if a filter has been set, remove all test suites that are filtered out
		if (testSuiteClassesFilter != null) {
			testSuiteClasses.retainAll(testSuiteClassesFilter);
		}

		List<TestSuite> runSuites = new LinkedList<TestSuite>();
		for (Class<? extends TestSuite> clazz : testSuiteClasses) {
			if (suites2TestCases.containsKey(clazz)) {
				List<Class<? extends TestCase>> testCaseClasses = suites2TestCases.get(clazz);
				Constructor<?> c = null;
				try {
					c = clazz.getConstructor(new Class[] {Class[].class});
				} catch (Exception e) {
					logger.error("Could not find (Class<? extends TestCase> ... classes) constructor for TestSuite " + clazz.getName(), e);
					continue;
				}
				TestSuite testSuite = null;
				try {
					testSuite = (TestSuite) c.newInstance(new Object[] {testCaseClasses.toArray(new Class[testCaseClasses.size()])});
				} catch (Exception e) {
					logger.error("Could not instantiate TestSuite " + clazz.getName(), e);
					continue;
				}
				runSuites.add(testSuite);
			}
		}

		return runSuites;
	}
}
