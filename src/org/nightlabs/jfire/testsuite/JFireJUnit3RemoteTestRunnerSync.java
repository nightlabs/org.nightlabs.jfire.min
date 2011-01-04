package org.nightlabs.jfire.testsuite;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.junit.internal.runners.JUnit38ClassRunner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.login.JFireLogin;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerUtil;

/**
 * Sync version of {@link JFireJUnit3RemoteTestRunner}, to be deleted.
 * 
 * @author alex
 */
public class JFireJUnit3RemoteTestRunnerSync extends JUnit38ClassRunner {

	public JFireJUnit3RemoteTestRunnerSync(Class<?> klass) {
		super(klass);
		// TODO Auto-generated constructor stub
	}

//	private static class MyTestSuite extends TestSuite {
//		
//		public MyTestSuite(Class<?>... klass) {
//			super(klass);
//		}
//		
//		public MyTestSuite(final Class<? extends TestCase> theClass) {
//			super(theClass);
//		}
//		
//		private List<Test> suiteTests;
//		
//		@Override
//		public void addTest(Test test) {
//			super.addTest(test);
//			if (suiteTests == null) {
//				suiteTests = new ArrayList<Test>();
//			}
//			suiteTests.add(test);
//		}
//		public List<Test> getSuiteTests() {
//			return suiteTests;
//		}
//	}
//	
//	private Class<?> myTestClass;
//	private MyTestSuite myTestSuite;
//	
//	public JFireJUnit3RemoteTestRunnerSync(Class<?> klass)
//			throws InitializationError {
//		this(createTestSuite(klass));
//		myTestClass = klass;
//	}
//
//	private static TestSuite createTestSuite(Class<?> klass) {
//		return new MyTestSuite(klass.asSubclass(TestCase.class));
//	}
//
//	public JFireJUnit3RemoteTestRunnerSync(Test test)
//	throws InitializationError {
//		super(test);
//		myTestSuite = (MyTestSuite) test;
//	}
//	
//	@Override
//	public void run(RunNotifier notifier) {
//		if (checkServerEnvironment()) {
//			super.run(notifier);
//		} else {
//			System.err.println("Now running " + myTestClass);
//			JFireLogin login = JFireRemoteTestRunnerLogin.getLogin();
//			try {
//				login.login();
//			} catch (LoginException e) {
//				notifier.fireTestFailure(new Failure(getDescription(), e));
//				return;
//			}
//			Map<String, Throwable> testResults = null;
//			try {
//				JFireTestManagerRemote testManager = JFireEjb3Factory.getRemoteBean(JFireTestManagerRemote.class, login.getInitialContextProperties());
//				testResults = testManager.runTest(myTestClass);
//			} catch (Throwable e) {
//				notifier.fireTestFailure(new Failure(getDescription(), e));
//				return;
//			} finally {
//				try {
//					login.logout();
//				} catch (LoginException e) {
//					notifier.fireTestFailure(new Failure(getDescription(), e));
//					return;
//				}
//			}
//			
//			List<Test> suiteTests = myTestSuite.getSuiteTests();
//			TestResult result= new TestResult();
//			result.addListener(createAdaptingListener(notifier));
//			
//			for (Test test : suiteTests) {
//				if (test instanceof TestCase) {
//					result.startTest(test);
//					TestCase testCase = (TestCase) test;
//					String methodName = testCase.getName();
//					if (testResults != null && testResults.containsKey(methodName)) {
//						Throwable failure = testResults.get(methodName);
//						if (failure != null) {
//							result.addError(testCase, failure);
//						} else {
//							result.endTest(testCase);
//						}
//					} else {
//						result.endTest(testCase);
//					}
//				}
//			}
//		}
//	}
//	
//	
//	private boolean checkServerEnvironment() {
//		JFireServerManager jfsm = null;
//		try {
//			jfsm = JFireServerManagerUtil.getJFireServerManager();
//		} catch (Throwable t) {
//			return false;
//		}
//		return jfsm != null;
//	}
}
