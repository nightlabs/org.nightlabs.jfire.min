package org.nightlabs.jfire.testsuite;

import java.io.Serializable;

/**
 * Used internall to notify a client of a test-event of a server-side-run test.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class JFireTestSuiteNotification implements Serializable {

	private static final long serialVersionUID = 20101208L;

	public enum NotificationType {
		notificationStarted,
		testStarted,
		testFailure,
		testEnded,
		notificationEnded
	}
	
	private Class<?> testClass;
	
	private String testMethod;
	
	private NotificationType notificationType;
	
	private Throwable failure;
	
	public JFireTestSuiteNotification() {
	}

	public JFireTestSuiteNotification(Class<?> testClass, String testMethod,
			NotificationType notificationType, Throwable failure) {
		super();
		this.testClass = testClass;
		this.testMethod = testMethod;
		this.notificationType = notificationType;
		this.failure = failure;
	}

	public Class<?> getTestClass() {
		return testClass;
	}

	public void setTestClass(Class<?> testClass) {
		this.testClass = testClass;
	}

	public String getTestMethod() {
		return testMethod;
	}

	public void setTestMethod(String testMethod) {
		this.testMethod = testMethod;
	}

	public NotificationType getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(NotificationType notificationType) {
		this.notificationType = notificationType;
	}

	public Throwable getFailure() {
		return failure;
	}

	public void setFailure(Throwable failure) {
		this.failure = failure;
	}

	public static JFireTestSuiteNotification testStarted(Class<?> testClass, String method) {
		return new JFireTestSuiteNotification(testClass, method, NotificationType.testStarted, null);
	}
	
	public static JFireTestSuiteNotification testEnded(Class<?> testClass, String method) {
		return new JFireTestSuiteNotification(testClass, method, NotificationType.testEnded, null);
	}
	
	public static JFireTestSuiteNotification testFailure(Class<?> testClass, String method, Throwable failure) {
		return new JFireTestSuiteNotification(testClass, method, NotificationType.testFailure, failure);
	}
}
