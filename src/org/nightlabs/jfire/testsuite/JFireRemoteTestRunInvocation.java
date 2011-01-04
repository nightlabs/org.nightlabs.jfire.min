/**
 * 
 */
package org.nightlabs.jfire.testsuite;

import java.io.Serializable;

import javax.jdo.PersistenceManager;
import javax.naming.InitialContext;
import javax.security.auth.login.LoginContext;

import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.base.AuthCallbackHandler;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter;

/**
 * This is the Invocation that is started by
 * {@link JFireTestManagerBean#runTestAsync(Class)}. It runs all tests of a
 * test-class an notifies the client using the identifier the invocation was created with.
 * <p>
 * This class is used internally and not intended for direct use.
 * </p>
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class JFireRemoteTestRunInvocation extends Invocation {

	private static final long serialVersionUID = 20101208L;
	
	private String identifier;
	private Class<?> testClass;
	
	/**
	 * 
	 */
	public JFireRemoteTestRunInvocation(String identifier, Class<?> testClass) {
		this.identifier = identifier;
		this.testClass = testClass;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.asyncinvoke.Invocation#invoke()
	 */
	@Override
	public Serializable invoke() throws Exception {
		PersistenceManager pm = createPersistenceManager();
		try {
			final JFireTestSuiteNotificationManager notificationManager = JFireTestSuiteNotificationManager.getNotificationManager();
			
			try {
				
				runAs(User.getUser(pm, getOrganisationID(), User.USER_ID_SYSTEM), new Runnable() {
					@Override
					public void run() {
						AllDefaultPossibilitiesBuilder builder = new AllDefaultPossibilitiesBuilder(false);
						Runner runner;
						try {
							runner = builder.runnerForClass(testClass);
						} catch (Throwable e) {
							throw new IllegalStateException("Could not create TestRunner for class " + testClass, e);
						}
						
						RunNotifier notifier = new RunNotifier();
						notifier.addFirstListener(new RunListener() {
							@Override
							public void testStarted(Description description) throws Exception {
								notificationManager.addNotification(
										identifier,
										JFireTestSuiteNotification.testStarted(testClass,description.getMethodName()));
							}
							@Override
							public void testFailure(Failure failure) throws Exception {
								notificationManager.addNotification(
										identifier,
										JFireTestSuiteNotification.testFailure(testClass, failure.getDescription().getMethodName(), failure.getException()));
							}
							@Override
							public void testFinished(Description description)
									throws Exception {
								notificationManager.addNotification(
										identifier,
										JFireTestSuiteNotification.testEnded(testClass,description.getMethodName()));
							}
						});
						runner.run(notifier);
						
						notificationManager.endNotification(identifier, testClass);
					}
				});
				
			} catch (Throwable t) {
				notificationManager.endNotification(identifier, testClass);
			}
			return null;
		} finally {
			pm.close();
		}
	}
	
	private void runAs(User user, Runnable runnable) throws Exception {
		LoginContext loginContext;
		InitialContext initialContext = new InitialContext();
		J2EEAdapter j2eeAdapter = (J2EEAdapter) initialContext.lookup(J2EEAdapter.JNDI_NAME);
		JFireServerManager ism = getJFireServerManager();
		try {
			loginContext = j2eeAdapter.createLoginContext(LoginData.DEFAULT_SECURITY_PROTOCOL, createAuthCallbackHandler(ism, user));
			loginContext.login();
			try {
				runnable.run();
			} finally {
				loginContext.logout();
			}

		} finally {
			ism.close();
		}
	}
	
	private AuthCallbackHandler createAuthCallbackHandler(JFireServerManager ism, User user) throws Exception {
		return new AuthCallbackHandler(ism,
				user.getOrganisationID(),
				user.getUserID(),
				ObjectIDUtil.makeValidIDString(null, true));
	}
	
}
