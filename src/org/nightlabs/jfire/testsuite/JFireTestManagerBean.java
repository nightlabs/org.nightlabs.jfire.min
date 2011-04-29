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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.FetchPlan;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.spi.PersistenceCapable;

import junit.framework.Test;
import junit.framework.TestResult;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.asyncinvoke.AsyncInvoke;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.testsuite.id.TestCaseObjectsMapID;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.util.CollectionUtil;

/**
 * @ejb.bean name="jfire/ejb/JFireTestSuite/JFireTestManager"
 *					 jndi-name="jfire/ejb/JFireTestSuite/JFireTestManager"
 *					 type="Stateless"
 *					 transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class JFireTestManagerBean
	extends BaseSessionBeanImpl
	implements JFireTestManagerRemote, JFireTestManagerLocal
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(JFireTestManagerBean.class);

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.testsuite.JFireTestManagerRemote#logMemoryState(org.nightlabs.jfire.timer.id.TaskID)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void logMemoryState(final TaskID taskID)
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.testsuite.JFireTestManagerRemote#initialiseTestSystem()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void initialiseTestSystem()
	throws Exception
	{
	}

	private static Map<String, Integer> organisationID2testSuiteRunningCounter = new HashMap<String, Integer>();

	protected static int getTestSuiteRunningCounter(final String organisationID)
	{
		synchronized (organisationID2testSuiteRunningCounter) {
			Integer counter = organisationID2testSuiteRunningCounter.get(organisationID);
			if (counter == null)
				return 0;

			return counter.intValue();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.testsuite.JFireTestManagerLocal#evaluateCanRunTestsInNestedTransaction(org.nightlabs.jfire.testsuite.TestSuite)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
	public String evaluateCanRunTestsInNestedTransaction(final TestSuite testSuite)
	throws Exception
	{
		logger.info("evaluateCanRunTestsInNestedTransaction: " + testSuite.toString());
		PersistenceManager pm = createPersistenceManager();
		try {
			return testSuite.canRunTests(pm);
		} finally {
			pm.close();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
	public String evaluateCanRunTestInNestedTransaction(TestCase testCase) 
	throws Exception
	{
		logger.info("evaluateCanRunTestInNestedTransaction: " + testCase.toString());
		PersistenceManager pm = createPersistenceManager();
		try {
			return testCase.canRunTest(pm);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.testsuite.JFireTestManagerLocal#runTestInNestedTransaction(junit.framework.Test, junit.framework.TestResult)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
	public void runTestInNestedTransaction(final Test test, final TestResult result)
	throws Exception
	{
		logger.info("runTestInNestedTransaction: " + test.toString());
		test.run(result);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.testsuite.JFireTestManagerLocal#runTestInNestedTransaction_setUp(org.nightlabs.jfire.testsuite.TestCase)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
	public void runTestInNestedTransaction_setUp(final org.nightlabs.jfire.testsuite.TestCase test)
	throws Exception
	{
		test.setUp();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.testsuite.JFireTestManagerLocal#runTestInNestedTransaction_tearDown(org.nightlabs.jfire.testsuite.TestCase)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
	public void runTestInNestedTransaction_tearDown(final org.nightlabs.jfire.testsuite.TestCase test)
	throws Exception
	{
		test.tearDown();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
	public void runTestInNestedTransaction_cleanUpAfterClass(final org.nightlabs.jfire.testsuite.TestCase test) throws Exception {
		test.cleanUpAfterClass();
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
	public void runTestInNestedTransaction_setUpBeforeClass(final org.nightlabs.jfire.testsuite.TestCase test) throws Exception {
		test.setUpBeforeClass();
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.testsuite.JFireTestManagerLocal#runTestInNestedTransaction_runTest(org.nightlabs.jfire.testsuite.TestCase)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
	public void runTestInNestedTransaction_runTest(final org.nightlabs.jfire.testsuite.TestCase test)
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

	@Override
	public boolean isJDOObjectExisting(ObjectID objectID) {
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.refreshAll();
			pm.getObjectById(objectID);
			return true;
		} catch (JDOObjectNotFoundException e) {
			return false;
		}
		finally {
			pm.close();
		}		
	}
	
	@Override
	public Object getObject(ObjectID objectID)
	{
		PersistenceManager pm = createPersistenceManager();
		try
		{
			return pm.getObjectById(objectID);
		}
		catch (JDOObjectNotFoundException e)
		{
			return null;
		}
		finally
		{
			pm.close();
		}		
	}
	
	@Override
	public void storeObject(Object object)
	{
		if (! PersistenceCapable.class.isInstance(object))
			throw new IllegalArgumentException(
					"Given object does not implement PersistenceCapable! givenType=" + object.getClass().getName());
				
		PersistenceManager pm = createPersistenceManager();
		try
		{
			pm.makePersistent(object);
		}
		finally
		{
			pm.close();
		}		
	}
	
	@RolesAllowed("_Guest_")
	@Override
	public Set<TestCaseObjectsMapID> getTestCaseObjectsMapIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(TestCaseObjectsMap.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return CollectionUtil.createHashSetFromCollection( q.execute() );
		} finally {
			pm.close();
		}
	}
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public List<TestCaseObjectsMap> getTestCaseObjectsMaps(Collection<TestCaseObjectsMapID> testCaseObjectsMapIDs, String[] fetchGroups, int maxFetchDepth)
		
	{
	PersistenceManager pm = createPersistenceManager();
	try {
		return NLJDOHelper.getDetachedObjectList(pm, testCaseObjectsMapIDs, TestCaseObjectsMap.class, fetchGroups, maxFetchDepth);
	} finally {
		pm.close();
	}
	}
	

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public TestCaseObjectsMap storeTestCaseObjectsMap(
			TestCaseObjectsMap testCaseObjectsMap, Boolean get, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, testCaseObjectsMap, get, fetchGroups, maxFetchDepth);
		}
		finally {
			pm.close();
		}
	}
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public void deleteTestCaseObjectsMap(
			TestCaseObjectsMapID testCaseObjectIDsID) {
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
			pm.getExtent(TestCaseObjectsMap.class, true);
			TestCaseObjectsMap objectsMap = (TestCaseObjectsMap) pm.getObjectById(testCaseObjectIDsID);
			pm.deletePersistent(objectsMap);
			pm.flush();
		}
		finally {
			pm.close();
		}		
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public String runTestAsync(Class<?> testClass) throws Exception {
		String identifier = ObjectIDUtil.makeValidIDString(null, true);
		JFireTestSuiteNotificationManager notificationManager = JFireTestSuiteNotificationManager.getNotificationManager();
		notificationManager.startNotification(identifier, testClass);
		AsyncInvoke.exec(new JFireRemoteTestRunInvocation(identifier, testClass), true);
		return identifier;
	}
	
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("_Guest_")
	public List<JFireTestSuiteNotification> getTestNotifications(String identifier) {
		return JFireTestSuiteNotificationManager.getNotificationManager().popNotifications(identifier);
	}
	
}
