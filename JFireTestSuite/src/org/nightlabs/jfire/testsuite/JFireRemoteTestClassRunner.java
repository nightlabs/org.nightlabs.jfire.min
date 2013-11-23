package org.nightlabs.jfire.testsuite;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.login.JFireLogin;
import org.nightlabs.jfire.base.login.JFireSecurityConfiguration;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerUtil;

/**   
 * TODO: This class is not yet finished, it should be changed to the async notification mechanism of {@link JFireJUnit3RemoteTestRunner}
 * 
 * @author alex
 */
public class JFireRemoteTestClassRunner extends BlockJUnit4ClassRunner {

	public JFireRemoteTestClassRunner(Class<?> klass)
			throws InitializationError {
		super(klass);
	}
	
	@Override
	public void run(RunNotifier notifier) {
		if (checkServerEnvironment()) {
			super.run(notifier);
		} else {
			System.err.println("Now running " + getTestClass().getJavaClass());
			JFireSecurityConfiguration.declareConfiguration();
			LoginData loginData = new LoginData("chezfrancois.jfire.org", "francois", "test");
			loginData.setDefaultValues();
			JFireLogin login = new JFireLogin(loginData);
			try {
				login.login();
			} catch (LoginException e) {
				notifier.fireTestFailure(new Failure(getDescription(), e));
				return;
			}
			Map<String, Throwable> testResults = null;
			try {
				JFireTestManagerRemote testManager = JFireEjb3Factory.getRemoteBean(JFireTestManagerRemote.class, login.getInitialContextProperties());
//				testResults = testManager.runTestAsync(getTestClass().getJavaClass());
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
			for (FrameworkMethod method : getChildren()) {
				Description description= describeChild(method);
				EachTestNotifier childNotifier = new EachTestNotifier(notifier, description);
				childNotifier.fireTestStarted();
				if (testResults != null && testResults.containsKey(method.getName())) {
					Throwable failure = testResults.get(method.getName());
					if (failure != null) {
						childNotifier.addFailure(failure);
					} else {
						childNotifier.fireTestFinished();
					}
				} else {
					childNotifier.fireTestFinished();
				}
			}
		}
	}
		
	private boolean checkServerEnvironment() {
		JFireServerManager jfsm = null;
		try {
			jfsm = JFireServerManagerUtil.getJFireServerManager();
		} catch (Throwable t) {
			return false;
		}
		return jfsm != null;
	}
}
