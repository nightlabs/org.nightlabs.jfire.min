package org.nightlabs.jfire.testsuite.cascadedauthentication;

import javax.jdo.PersistenceManager;

import junit.framework.TestCase;

import org.nightlabs.jfire.testsuite.TestSuite;

public class CascadedAuthenticationTestSuite
extends TestSuite
{
	/**
	 * @param classes
	 */
	public CascadedAuthenticationTestSuite(Class<? extends TestCase>... classes) {
		super(classes);
		setName("Test JFire Cascaded Authentication (cross organisation communication)");
	}

	@Override
	public String canRunTests(PersistenceManager pm)
	throws Exception
	{
		String className = "org.nightlabs.jfire.security.User";
		try {
			Class.forName(className);
		} catch (ClassNotFoundException x) {			
			return "The module JFireBase seems not to be installed (Class \"" + className + "\" could not be found)!";
		}
		return null;
	}

}
