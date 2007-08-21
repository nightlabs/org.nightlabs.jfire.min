/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.app;

import java.io.File;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.nightlabs.base.app.AbstractWorkbenchAdvisor;
import org.nightlabs.base.exceptionhandler.ExceptionHandlingWorkbenchAdvisor;
import org.nightlabs.jfire.base.JFireWelcomePerspective;
import org.nightlabs.jfire.base.j2ee.RemoteResourceFilterRegistry;
import org.nightlabs.jfire.base.login.JFireLoginHandler;
import org.nightlabs.jfire.base.login.JFireSecurityConfiguration;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.login.WorkOfflineException;

/**
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de> 
 * 
 */
public class JFireWorkbenchAdvisor 
//extends SplashHandlingWorkbenchAdvisor 
extends AbstractWorkbenchAdvisor
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(JFireWorkbenchAdvisor.class);
    
	/**
	 * Constructs a new <code>JFireWorkbenchAdvisor</code>.<br/>
	 * Registeres {@link ResourcePublishCLDelegate} to the parent ClassLoader.<br/>
	 * Initializes logging, so all logging should be done after 
	 * the WorkbenchAdvisor is created.<br/>
	 * At last initializes the config.
	 * @see ExceptionHandlingWorkbenchAdvisor
	 * @see org.nightlabs.rcp.splash.SplashHandlingWorkbenchAdvisor
	 */
	public JFireWorkbenchAdvisor(Display display) 
	{
		this(display, true);
	}
	
	public JFireWorkbenchAdvisor(Display display, boolean initLogin) 
	{
		try {
			if (initLogin)
				initLogin();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void initLogin() throws LoginException, WorkOfflineException
	{
		// create log directory if not existent
		JFireApplication.getLogDir();
		try {	
			org.nightlabs.jfire.classloader.JFireRCDLDelegate.
					createSharedInstance(Login.getLogin(false), new File(JFireApplication.getRootDir(), "classloader.cache")) //$NON-NLS-1$
					.setFilter(RemoteResourceFilterRegistry.sharedInstance());
		} catch (LoginException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			LoginException x = new LoginException(e.getMessage());
			x.initCause(e);
			throw x;
		}
		initializeLoginModule();
	}
	   	    
	/**
	 * By now JFireWelcomePerspective is initial perspective
	 * @see org.eclipse.ui.application.WorkbenchAdvisor
	 */
	public String getInitialWindowPerspectiveId() {
		return JFireWelcomePerspective.ID_PERSPECTIVE;
	}
			
	protected void initializeLoginModule()
	{
		logger.debug("#initializeLoginModule: Declaring Configuration"); //$NON-NLS-1$
		JFireSecurityConfiguration.declareConfiguration();		
		logger.debug("#initializeLoginModule: Setting LoginHandler"); //$NON-NLS-1$
		try {
			Login.getLogin(false).setLoginHandler(new JFireLoginHandler());
		} catch (LoginException e) {
			throw new RuntimeException("How the hell could this happen?!", e); //$NON-NLS-1$
		}
	}
	
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		return new JFireWorkbenchWindowAdvisor(configurer);
	}

}
