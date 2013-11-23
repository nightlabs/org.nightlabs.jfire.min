package org.nightlabs.jfire.jboss.ejb3;


import java.security.GeneralSecurityException;

import org.jboss.ejb3.Container;
import org.jboss.ejb3.security.Ejb3AuthenticationInterceptor;
import org.jboss.logging.Logger;
import org.jboss.security.AuthenticationManager;


/**
 *
 * a custom Interceptor class which catches the general security exceptions not thrown by the default JBoss Ejb3AuthenticationInterceptor
 *  
 * @author Fitas Amine - fitas [at] nightlabs [dot] de
 */
public class JFireEjb3AuthenticationInterceptor extends Ejb3AuthenticationInterceptor
{ 
	private static final Logger logger = Logger.getLogger(JFireEjb3AuthenticationInterceptor.class);

	public JFireEjb3AuthenticationInterceptor(AuthenticationManager manager, Container container)
	{
		super(manager, container);
		logger.debug("JFireEjb3AuthenticationInterceptor has been initialized !!!");
	}

	@Override
	protected void handleGeneralSecurityException(GeneralSecurityException gse)
	{
		throw new SecurityException(gse.getMessage(), gse);
	}

}