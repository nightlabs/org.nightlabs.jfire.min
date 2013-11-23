package org.nightlabs.jfire.testsuite;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.nightlabs.jfire.base.login.JFireLogin;
import org.nightlabs.jfire.base.login.JFireSecurityConfiguration;

/**
 * This class provides the Client-Side {@link JFireLogin} the remotely run tests
 * use to authenticate and to run the tests. This is internally used by the
 * runners and not intended to be used directly.
 * 
 * @author alex
 */
public class JFireRemoteTestRunnerLogin
{
	private static final String PROP_FILE = "JFireTestRunner.properties";
	private static final String PROP_FILE_DEF = "JFireTestRunner-default.properties";


	/**
	 * 
	 */
	public JFireRemoteTestRunnerLogin() {
	}
	
	
	public static JFireLogin getLogin() {
		JFireSecurityConfiguration.declareConfiguration();
		URL propFile = JFireRemoteTestRunnerLogin.class.getResource(PROP_FILE);
		if (propFile == null) {
			propFile = JFireRemoteTestRunnerLogin.class.getResource(PROP_FILE_DEF);
		}
		Properties props = new Properties();
		try {
			InputStream in = propFile.openStream();
			try {
				props.load(in);
			} finally {
				in.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		JFireLogin login = new JFireLogin(props);
		login.getLoginData().setDefaultValues();
		return login;
	}

}
