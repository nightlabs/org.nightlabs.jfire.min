package org.nightlabs.jfire.testsuite;

import java.lang.reflect.Method;

import org.apache.commons.lang.exception.ExceptionUtils;
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
	// REV Marco: Why are these public??? They should be private!
	// And ARGGGG!!!! This is *not* thread-safe!!! You MUST write this in a thread-safe way!!!
	// And for the future *ALWAYS* think about multi-threading *BEFORE* you check-in such code!!!
	// Finding such bugs is very cumbersome!!! If you only want to test sth. quickly, then clearly
	// mark it as TODO tag and write that it needs to be made thread-safe!!!
	// But this should be a rare exception since it's always better and easier to make things
	// thread-safe from the beginning!
	public static boolean hasBeenInit = false;
	public static int testMethodsLeft = 0;


	public TestCase()
	{
	}


	public TestCase(String name)
	{
		super(name);
	}


	/**
	 * the method is called once upon initialization of each Testcase
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
		// REV Marco: *NOT* thread-safe!!! Make it thread-safe!!!
		if(!hasBeenInit)
		{
			// count the number of test methods in the current test case
			for (Method method : getClass().getMethods()) {
				if (method.getName().startsWith("test")) {
					testMethodsLeft++;
				}
			}
			// calls setup once at the beginning of a testcase cycle
			setUpBeforeClass();
			hasBeenInit = true;
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
		// REV Marco: *NOT* thread-safe!!! Make it thread-safe!!!
		// increment the counter of the test methods if zero is left then call up the clean up code
		if (--testMethodsLeft == 0) {
			// call cleanUp method
			cleanUpAfterClass();
	    	hasBeenInit = false;
		}

	}
}
