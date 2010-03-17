package org.nightlabs.jfire.testsuite;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;


import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.JFireEjb3Factory;


/**
 * This implementation of {@link junit.framework.TestCase} ensures that
 * <code>setUp()</code> and <code>tearDown()</code> will be executed in
 * separate (nested) transactions before and after the actual test (which
 * is itself executed in a nested transaction). This way
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
public abstract class TestCase
extends junit.framework.TestCase
{

	// determines if method setUpBeforeClass() has been already called
	private static ThreadLocal<Boolean> hasBeenInit = new ThreadLocal<Boolean>(){
        protected synchronized Boolean initialValue() {
            return new Boolean(false);
        }
    };
    // counts the total number of test methods left to be executed
    // upon zero fires up the method cleanUpAfterClass()
	private static ThreadLocal<Integer> testMethodsLeft = new ThreadLocal<Integer>(){
         protected synchronized Integer initialValue() {
             return new Integer(0);
         }
     };
	// a useful local thread Map where it s possible to store ObjectIDs used in some testcases
	private static ThreadLocal<Map<String,ObjectID>> testCaseObjectIDsMap = new ThreadLocal<Map<String,ObjectID>>();

	
	public TestCase()
	{
	}


	public TestCase(String name)
	{
		super(name);
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
    
    public ObjectID getVariableObjectID(String key)
    {	
    	if (testCaseObjectIDsMap.get() == null) 
    		return null;
    	return testCaseObjectIDsMap.get().get(key);
    }

    public Collection<ObjectID> getVariableObjectIDs()
    {	
    	if (testCaseObjectIDsMap.get() == null) 
    		return null;
    	return Collections.unmodifiableCollection(testCaseObjectIDsMap.get().values());
    }

    
    public void addVariableObjectID(String key, ObjectID objectID)
    {
    	if (testCaseObjectIDsMap.get() != null) 
    	{
    		testCaseObjectIDsMap.get().put(key, objectID);
    	}
    }
    
	
	@Override
	public void runBare()
			throws Throwable
	{
		Throwable exception= null;
		JFireTestManagerLocal m = JFireEjb3Factory.getLocalBean(JFireTestManagerLocal.class);
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
			} catch (Throwable tearingDown) {
				if (exception == null) exception= tearingDown;
			}
		}
		if (exception != null) throw exception;
	}

	@Override
	protected void setUp()
			throws Exception
	{
		super.setUp();
		synchronized (this){
			if(!hasBeenInit.get())
			{
				// count the number of test methods in the current test case
				int methodCount = 0;
				for (Method method : getClass().getMethods()) {
					if (method.getName().startsWith("test")) {
						methodCount++;
					}
				}
				testMethodsLeft.set(methodCount);
				// setup the Object map IDs
				testCaseObjectIDsMap.set(new HashMap<String,ObjectID>());
				// calls setup once at the beginning of a test case cycle
				setUpBeforeClass();
				hasBeenInit.set(true);
			}
		}
	}

	
	@Override
	protected void runTest()
			throws Throwable
	{
		super.runTest();
	}

	@Override
	protected void tearDown()
	throws Exception
	{
		super.tearDown();
		synchronized (this) {
			testMethodsLeft.set(testMethodsLeft.get() - 1);
			if (testMethodsLeft.get() == 0) {
				// call cleanUp method
				cleanUpAfterClass();
				testCaseObjectIDsMap.get().clear();
				testCaseObjectIDsMap.remove();
				hasBeenInit.set(false);
			}
		}
	}
}
