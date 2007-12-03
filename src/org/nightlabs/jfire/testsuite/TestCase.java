package org.nightlabs.jfire.testsuite;

import org.apache.commons.lang.exception.ExceptionUtils;

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
 */
public abstract class TestCase
extends junit.framework.TestCase
{
	public TestCase()
	{
	}

	public TestCase(String name)
	{
		super(name);
	}

	@Override
	public void runBare()
			throws Throwable
	{
		Throwable exception= null;
		JFireTestManagerLocal m = JFireTestManagerUtil.getLocalHome().create();
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
	}
}
