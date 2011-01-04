package org.nightlabs.jfire.testsuite;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.junit.internal.runners.JUnit38ClassRunner;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.login.JFireLogin;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerUtil;
import org.nightlabs.jfire.testsuite.JFireTestSuiteNotification.NotificationType;

/**
 * A JUnit3 runner for tests that should be run on a JFire server. Annotate the
 * test with the {@link RunWith}-annotation and point to this class if you want
 * your test-code to be run inside the server.
 * <p>
 * This runner checks whether it is running inside the JFire-server or within a
 * separate test-suite. If it is running on the server it simply delegates to
 * its super-class to run the test. If it is running in the client it will
 * notify the JFire-server to run the test-class. It order to run the test on
 * the server, the server has to be running, of course, and the test-code needs
 * to be deployed on that server.
 * </p>
 * <p>
 * The runner accesses the server and authenticates as the user defined either
 * by the <code>JFireTestRunner.properties</code> or
 * <code>JFireTestRunner-default.properties</code> files.
 * </p>
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class JFireJUnit3RemoteTestRunner extends JUnit38ClassRunner {

	/**
	 * Own TestSuite that is created for the Test. It knows its Test-classes. 
	 * Note: A Test is a Class an a method to run in that class.
	 */
	private static class MyTestSuite extends TestSuite {
		public MyTestSuite(final Class<? extends TestCase> theClass) {
			super(theClass);
		}
		
		/** Remembers all Tests added with {@link #addTest(Test)} */
		private List<Test> suiteTests;
		
		/**
		 * Overridden to give access to all added tests later.
		 */
		@Override
		public void addTest(Test test) {
			super.addTest(test);
			if (suiteTests == null) {
				suiteTests = new ArrayList<Test>();
			}
			suiteTests.add(test);
		}
		
		/**
		 * @return All Tests added to this suite.
		 */
		public List<Test> getSuiteTests() {
			return suiteTests;
		}
	}
	
	/** Gives access to the class to run. Set in constructor, but not accessible from super-class */
	private Class<?> myTestClass;
	/** Gives access to the TestSuite of this runner. Set in constructor, but not accessible from super-class */
	private MyTestSuite myTestSuite;
	
	/**
	 * {@inheritDoc}
	 */
	public JFireJUnit3RemoteTestRunner(Class<?> klass)
			throws InitializationError {
		this(createTestSuite(klass));
		myTestClass = klass;
	}
	
	/**
	 * Creates a {@link MyTestSuite}.
	 * 
	 * @param klass class.
	 * @return A new {@link MyTestSuite}.
	 */
	private static TestSuite createTestSuite(Class<?> klass) {
		return new MyTestSuite(klass.asSubclass(TestCase.class));
	}

	/**
	 * {@inheritDoc}
	 */
	public JFireJUnit3RemoteTestRunner(Test test)
	throws InitializationError {
		super(test);
		myTestSuite = (MyTestSuite) test;
	}

	/**
	 * Checks if the runner is in server-environment, if so it simply calls
	 * super.run(notifier). If the runner is in client-environment it will login
	 * to the JFire-server
	 */
	@Override
	public void run(RunNotifier notifier) {
		if (checkServerEnvironment()) {
			// We're running inside the server, normally run the test 
			super.run(notifier);
		} else {
			// We're running inside the test-suite, outside the server
			// First login to the server
			JFireLogin login = JFireRemoteTestRunnerLogin.getLogin();
			try {
				login.login();
			} catch (LoginException e) {
				notifier.fireTestFailure(new Failure(getDescription(), e));
				return;
			}
			
			try {
				JFireTestManagerRemote testManager = JFireEjb3Factory.getRemoteBean(JFireTestManagerRemote.class, login.getInitialContextProperties());
				
				// Prepare the notification to the local test-runner
				Map<String, TestCase> suiteTests = new HashMap<String, TestCase>();
				TestResult result= new TestResult();
				result.addListener(this.workaroundCreateAdaptingListener(notifier));
				
				for (Test test : myTestSuite.getSuiteTests()) {
					if (test instanceof TestCase) {
						TestCase testCase = (TestCase) test;
						suiteTests.put(testCase.getName(), (TestCase) test);
					}
				}
				
				// Start the asynchronous test-run on the server
				String notificationIdentifier = testManager.runTestAsync(myTestClass);
				
				// Poll notifications
				List<JFireTestSuiteNotification> testNotifications = testManager.getTestNotifications(notificationIdentifier);
				while (testNotifications != null) {
					boolean haveNotificationEnded = false;
					for (JFireTestSuiteNotification notification : testNotifications) {
						if (notification.getNotificationType() == NotificationType.notificationEnded) {
							haveNotificationEnded = true;
						}
						if (notification.getTestMethod() != null) {
							TestCase testCase = suiteTests.get(notification.getTestMethod());
							if (testCase != null) {
								switch (notification.getNotificationType()) {
								case testStarted: result.startTest(testCase); break;
								case testEnded: result.endTest(testCase); break;
								case testFailure: result.addError(testCase, notification.getFailure()); break;
								}
							}
						}
					}
					if (haveNotificationEnded) {
						break;
					}
					testNotifications = testManager.getTestNotifications(notificationIdentifier);
				}
			} catch (Throwable e) {
				notifier.fireTestFailure(new Failure(getDescription(), e));
				return;
			} finally {
				try {
					login.logout();
				} catch (LoginException e) {
					notifier.fireTestFailure(new Failure(getDescription(), e));
					return;
				}
			}
		}
	}

	/**
	 * Checks whether this runner is currently running inside the JFire-server
	 * or outside.
	 * 
	 * @return <code>true</code> if the code is executed inside the
	 *         JFire-server, <code>false</code> otherwise.
	 */
	private boolean checkServerEnvironment() {
		JFireServerManager jfsm = null;
		try {
			jfsm = JFireServerManagerUtil.getJFireServerManager();
		} catch (Throwable t) {
			return false;
		}
		return jfsm != null;
	}
	
	private TestListener workaroundCreateAdaptingListener(RunNotifier notifier) {
		try {
			Method declaredMethod = JUnit38ClassRunner.class.getDeclaredMethod("createAdaptingListener", RunNotifier.class);
			Object invokeObj = this;
			if (Modifier.isStatic(declaredMethod.getModifiers())) {
				invokeObj = null;
			}
			return (TestListener) declaredMethod.invoke(invokeObj, notifier);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
