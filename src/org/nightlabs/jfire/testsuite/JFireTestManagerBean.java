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
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.PersistenceManager;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.testsuite.login.JFireTestLogin;
import org.nightlabs.jfire.testsuite.prop.PropertySetTestStruct;
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
	/**
	 * 
	 */
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
	 * This method is called by the datastore initialisation mechanism.
	 * It initializes the users needed for Test logins and other prerequisites for the Test system.
	 * 
	 * @throws Exception When something went wrong. 
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	public void initialiseTestSystem()
	throws Exception 
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			JFireTestLogin.checkCreateLoginsAndRegisterInAuthorities(pm);
			PropertySetTestStruct.getTestStruct(getOrganisationID(), pm);
		} finally {
			pm.close();
		}
	}

	/**
	 * This method is called by the datastore initialisation mechanism.
	 * It runs all {@link TestSuite}s.
	 * 
	 * @throws Exception When something went wrong. 
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	public void initialise()
	throws Exception 
	{
		runAllTestSuites();
	}

	/**
	 * Runs all TestSuits and TestCases found in the classpath under org.nightlabs.jfire.testsuite.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void runAllTestSuites()
	throws SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ModuleException, IOException {
//		PersistenceManager pm = getPersistenceManager();
//		try {
			List<TestSuite> runSuites = createTestSuites(null);
			runTestSuiteInstances(runSuites);
//		} finally {
//			pm.close();
//		}
	}

	/**
	 * Runs TestCases found in the classpath under org.nightlabs.jfire.testsuite that belong to the given TestSuites..
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void runTestSuites(List<Class<? extends TestSuite>> testSuitesClasses)
	throws SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ModuleException, IOException {
//		PersistenceManager pm = getPersistenceManager();
//		try {
			List<TestSuite> runSuites = createTestSuites(testSuitesClasses);
			runTestSuiteInstances(runSuites);
//		} finally {
//			pm.close();
//		}
	}

	private static void runTestSuiteInstances(List<TestSuite> testSuites) throws ModuleException, IOException {
		List<JFireTestListener> listeners = getTestListeners();

		// Run the suites
		for (JFireTestListener listener : listeners) {
			try {
				listener.startTestRun();
			} catch (Exception e) {
				logger.error("Error notifing JFireTestListener!", e);
				continue;
			}
		}
		for (TestSuite suite : testSuites) {
			JFireTestRunner runner = new JFireTestRunner();
			for (JFireTestListener listener : listeners) {
				runner.addListener(listener);
			}
			runner.run(suite);
		}
		for (JFireTestListener listener : listeners) {
			try {
				listener.endTestRun();
			} catch (Exception e) {
				logger.error("Error notifing JFireTestListener!", e);
				continue;
			}
		}
	}

	/**
	 * Executes {@link TestSuite#canRunTests(PersistenceManager)} within a nested transaction. It is invoked by
	 * {@link JFireTestRunner#run(TestSuite, PersistenceManager)}
	 * <p>
	 * This only works if the JavaEE container passes the references, because {@link junit.framework.TestSuite}
	 * is not {@link Serializable}.
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
	 * are {@link Serializable}, the {@link TestResult} is not a return value, but the data is directly written into it and
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
