package org.nightlabs.jfire.testsuite;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.runner.RunWith;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.testsuite.TestSuite.Status;


/**
 * This class can be used as base class for JUnit3-TestCases for server-side tests.
 * <p>
 * This implementation of {@link junit.framework.TestCase} ensures that
 * <code>setUp()</code> and <code>tearDown()</code> will be executed in
 * separate (nested) transactions before and after the actual test (which
 * is itself executed in a nested transaction). This way
 * </p>
 * <p>
 * The JFire testsuite supports executing subclasses of the normal
 * {@link junit.framework.TestCase} as well as subclasses of this class.
 * So you do not necessarily need to subclass {@link TestCase}, if
 * you don't require its special adaptions.
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 * @author fitas - fitas at nightlabs dot de
 */
//@RunWith(JFireRemoteTestClassRunner.class)
@RunWith(JFireJUnit3RemoteTestRunner.class)
public abstract class TestCase
extends junit.framework.TestCase
{
    // counts the total number of test methods left to be executed
    // upon zero fires up the method cleanUpAfterClass()
	private static ThreadLocal<Integer> testMethodsLeft = new ThreadLocal<Integer>();
	// a useful local thread Map where it s possible to store ObjectIDs used in some testcases
	private static ThreadLocal<Map<String,Object>> testCaseContextObjectsMap = new ThreadLocal<Map<String,Object>>();
	private static ThreadLocal<String> skipReason = new ThreadLocal<String>();
	
	
	public TestCase()
	{
	}


	public TestCase(String name)
	{
		super(name);
	}


	
	protected String canRunTest(PersistenceManager pm) throws Exception
	{
		return null;	
	}
	
	/**
	 * the method is called once upon initialization of each Test case
	 * 
	 */
    protected void setUpBeforeClass() throws Exception
	{
	}

	/**
	 * the method is called once all test methods has been run
	 *
	 */
    protected void cleanUpAfterClass() throws Exception
	{
	}

    // used  for as an extension for names in testcase
    public static String getExtension(String string) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
        Date date = new Date();
        Integer rnd = (int)( Math.random()* 100000 );      
        return string.concat("-" + dateFormat.format(date)+ rnd.toString());
    }
    
    
    public Object getTestCaseContextObject(String key)
    {	
    	if (testCaseContextObjectsMap.get() == null) 
    		throw new IllegalStateException("the ContextObjectsMap has not been initialized !!");
    	return testCaseContextObjectsMap.get().get(key);
    }

    public int getTestCaseContextObjectSize()
    {	
     	if (testCaseContextObjectsMap.get() == null) 
    		throw new IllegalStateException("the ContextObjectsMap has not been initialized !!");
    	return testCaseContextObjectsMap.get().size();
    }
   
    public void setTestCaseContextObject(String key, Object object)
    {
    	if (testCaseContextObjectsMap.get() == null) 
    		throw new IllegalStateException("the ContextObjectsMap has not been initialized !!");
    	testCaseContextObjectsMap.get().put(key, object);
    }
    
	private boolean initSetUpBeforeClass()
	{
		if(testMethodsLeft.get() != null)
			return false;	
		// count the number of test methods in the current test case
		int methodCount = 0;
		for (Method method : getClass().getMethods()) {
			if (method.getName().startsWith("test")) {
				methodCount++;
			}
		}
		testMethodsLeft.set(methodCount);
		// setup the Object map IDs
		testCaseContextObjectsMap.set(new HashMap<String,Object>());
		return true;	
	}
	
	private boolean initCleanUpAfterClass()
	{
		testMethodsLeft.set(testMethodsLeft.get() - 1);
		if (testMethodsLeft.get() == 0) {
			testCaseContextObjectsMap.get().clear();
			testCaseContextObjectsMap.remove();
			testMethodsLeft.remove();
			return true;
		}
		return false;		
	}
	
	@Override
	public void runBare()
			throws Throwable
	{
		Throwable exception= null;
		JFireTestManagerLocal m = JFireEjb3Factory.getLocalBean(JFireTestManagerLocal.class);
		if (skipReason.get()!= null) {
			fail(skipReason.get());
		}
		if(initSetUpBeforeClass())
		{
			try {
				skipReason.set(m.evaluateCanRunTestInNestedTransaction(this));
			} catch (Exception e) {
				skipReason.set(e.getClass().getName() + ": " + e.getMessage());
			}
			if (skipReason.get()!= null) 
				fail(skipReason.get());
			m.runTestInNestedTransaction_setUpBeforeClass(this);
		}
		m.runTestInNestedTransaction_setUp(this);
		try {
			m.runTestInNestedTransaction_runTest(this);
		} catch (Throwable running) {
			if (ExceptionUtils.indexOfThrowable(running, InternalWrapperException.class) >= 0) {
				Throwable iwe = running;
				while (iwe != null && !(iwe instanceof InternalWrapperException))
					iwe = ExceptionUtils.getCause(iwe);

				if (iwe != null)
					running = iwe.getCause();
			}
			exception= running;
		}
		finally {
			try {
				m.runTestInNestedTransaction_tearDown(this);
				if(initCleanUpAfterClass())
					m.runTestInNestedTransaction_cleanUpAfterClass(this);
			} catch (Throwable tearingDown) {
				if (exception == null) exception= tearingDown;
			}
		}
		if (exception != null) throw exception;
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	
	@Override
	protected void runTest() throws Throwable
	{
		super.runTest();
	}

	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
}
