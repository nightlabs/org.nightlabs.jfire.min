package org.nightlabs.jfire.base.app;

import java.io.File;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import org.nightlabs.base.app.AbstractApplication;
import org.nightlabs.base.app.AbstractWorkbenchAdvisor;
import org.nightlabs.base.exceptionhandler.ExceptionHandlerRegistry;
import org.nightlabs.base.exceptionhandler.ExceptionHandlingWorkbenchAdvisor;
import org.nightlabs.classsharing.extensionpoint.globalpublish.EPClassLoaderDelegate;
import org.nightlabs.jfire.base.JFireWelcomePerspective;
import org.nightlabs.jfire.base.login.JFireLoginHandler;
import org.nightlabs.jfire.base.login.JFireSecurityConfiguration;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.login.WorkOfflineException;
import org.nightlabs.jfire.classloader.JFireRCDLDelegate;

/**
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de> 
 * 
 */
public class JFireWorkbenchAdvisor 
//extends SplashHandlingWorkbenchAdvisor 
extends AbstractWorkbenchAdvisor
{
	public static Logger LOGGER = Logger.getLogger(JFireWorkbenchAdvisor.class);
    
	/**
	 * Constructs a new <code>JFireWorkbenchAdvisor</code>.<br/>
	 * Registeres {@link EPClassLoaderDelegate} to the parent ClassLoader.<br/>
	 * Initializes logging, so all logging should be done after 
	 * the WorkbenchAdvisor is created.<br/>
	 * At last initializes the config.
	 * @see ExceptionHandlingWorkbenchAdvisor
	 * @see org.nightlabs.rcp.splash.SplashHandlingWorkbenchAdvisor
	 */
	public JFireWorkbenchAdvisor(Display display) 
	{
		super();
		try {
			initLogin();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void initLogin() throws LoginException, WorkOfflineException
	{
		// create log directory if not existent
		JFireApplication.getLogDir();
		JFireRCDLDelegate.createSharedInstance(Login.getLogin(false), new File(JFireApplication.getRootDir(), "classloader.cache"));
		initializeLoginModule();
	}
	   	    
	/**
	 * By now JFireWelcomePerspective is initial perspective
	 * @see org.eclipse.ui.application.WorkbenchAdvisor
	 */
	public String getInitialWindowPerspectiveId() {
		return JFireWelcomePerspective.ID_PERSPECTIVE;
	}
			
	protected void initializeLoginModule(){
		LOGGER.debug("#initializeLoginModule: Declaring Configuration");
		JFireSecurityConfiguration.declareConfiguration();
		
		LOGGER.debug("#initializeLoginModule: Setting LoginHandler");
		try {
			Login.getLogin(false).setLoginHandler(new JFireLoginHandler());
		} catch (LoginException e) {
			throw new RuntimeException("How the hell could this happen?!", e);
		}
	}
	
	public void eventLoopException(Throwable exception) {
		if (!ExceptionHandlerRegistry.syncHandleException(exception))
			super.eventLoopException(exception);
	}

	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		return new JFireWorkbenchWindowAdvisor(configurer);
	}

	protected AbstractApplication initApplication() {
		return new JFireApplication();
	}	
		
}
