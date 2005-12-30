/*
 * Created on May 7, 2005
 *
 */
package org.nightlabs.jfire.test.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import org.nightlabs.j2ee.InitialContextProvider;

import junit.framework.TestCase;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 *
 */
public class JFireTestCase extends TestCase implements InitialContextProvider
{
	protected JFireLoginContext loginContext;
	protected Properties initialContextProperties;
	protected InitialContext initialContext;
	
	public JFireTestCase(String name) 
	{
		super(name);
		loadProperties();
		JFireSecurityConfiguration.declareConfiguration();
		init();
	}

	private void init()
	{
		try 
		{
			loginContext = new JFireLoginContext("jfire", new LoginCallbackHandler(this));
			loginContext.setCredentials(
					System.getProperty("org.nightlabs.jfire.test.userid"),
					System.getProperty("org.nightlabs.jfire.test.organisationid"),
					System.getProperty("org.nightlabs.jfire.test.password")
			);
		}
		catch (LoginException e) 
		{
			fail("Error creating login context");
		}
	}

	public Hashtable getInitialContextProperties() 
		throws LoginException 
	{
    if (initialContextProperties == null)
    {
    	initialContextProperties = new Properties();
			initialContextProperties.put(InitialContext.INITIAL_CONTEXT_FACTORY, System.getProperty("org.nightlabs.jfire.test.initialcontextfactory"));
    	initialContextProperties.put(InitialContext.PROVIDER_URL, System.getProperty("org.nightlabs.jfire.test.serverurl"));
    	initialContextProperties.put(InitialContext.SECURITY_PRINCIPAL, loginContext.getUsername());
    	initialContextProperties.put(InitialContext.SECURITY_CREDENTIALS, loginContext.getPassword());
    	initialContextProperties.put(InitialContext.SECURITY_PROTOCOL, "jfire");
		}
		return initialContextProperties;
	}


	
	public InitialContext getInitialContext() 
		throws LoginException, NamingException 
	{
		if (initialContext == null)
			initialContext = new InitialContext(getInitialContextProperties());
		return initialContext;
	}

	public JFireLoginContext getLoginContext() {
		return loginContext;
	}

	protected void loadProperties()
	{
		// if properties defined by system use them
    if((System.getProperty("org.nightlabs.jfire.test.initialcontextfactory") != null) &&
    	 (System.getProperty("org.nightlabs.jfire.test.serverurl") != null) &&
    	 (System.getProperty("org.nightlabs.jfire.test.userid") != null) &&
    	 (System.getProperty("org.nightlabs.jfire.test.organisationid") != null) &&
    	 (System.getProperty("org.nightlabs.jfire.test.password") != null))
    	return;

    Properties props = new Properties();
    InputStream is = null;
    String conf = System.getProperty("org.nightlabs.jfire.test.config");
    if(conf == null)
    	fail("org.nightlabs.jfire.test.config not defined");
    is = JFireTestCase.class.getResourceAsStream(conf);
    if(is == null)
    	fail("could not open properties: " + conf);
    try 
		{
    	props.load(is);
    	for( Iterator it = props.entrySet().iterator(); it.hasNext(); )
    	{
    		Map.Entry entry = (Map.Entry) it.next();
    		System.setProperty((String)entry.getKey(),(String)entry.getValue());
    	}
		} 
    catch (IOException e) 
		{
    	e.printStackTrace();
		}
	}
}
