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

package org.nightlabs.jfire.base.login;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;

import javax.security.auth.login.LoginException;
import javax.swing.SwingUtilities;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.eclipse.ui.PlatformUI;
import org.nightlabs.base.NLBasePlugin;
import org.nightlabs.jfire.base.JFireBasePlugin;
import org.nightlabs.rcp.splash.SplashScreen;

/**
 * @see org.nightlabs.jfire.base.login.ILoginHandler
 * 
 * @author Alexander Bieber
 */
public class JFireLoginHandler implements ILoginHandler {
	public static final Logger LOGGER = Logger.getLogger(JFireLoginHandler.class);

	/**
	 * Opens an instance of {@link LoginDialog}. 
	 * The dialog sets the loginResult and loginContext values.
	 * A login verification is performed to be sure the user can
	 * be identified by the credentials he specified.
	 * @see org.nightlabs.jfire.base.login.ILoginHandler#handleLogin(org.nightlabs.jfire.base.login.JFireLoginContext)
	 * @see LoginDialog
	 */
	public void handleLogin(JFireLoginContext loginContext, LoginConfigModule loginConfigModule, Login.AsyncLoginResult loginResult)
	throws LoginException
	{
		// if the user specified the necessary parameters and the login succeeds, we don't show any login dialog
		try {
			String userID = null;
			String password = null;
			String organisationID = null;
			String initialContextFactory = null;
			String serverURL = null;

			String[] args = NLBasePlugin.getDefault().getApplication().getArguments();
			for (int i = 0; i < args.length; i++) {
				String arg = args[i];
				String val = i + 1 < args.length ? args[i + 1] : null;

				if ("--login.userID".equals(arg))
					userID = val;
				else if ("--login.password".equals(arg))
					password = val;
				else if ("--login.organisationID".equals(arg))
					organisationID = val;
				else if ("--login.initialContextFactory".equals(arg))
					initialContextFactory = val;
				else if ("--login.serverURL".equals(arg))
					serverURL = val;
			}

			if (password != null) {
				if (userID == null)
					userID = loginConfigModule.getUserID();
				else
					loginConfigModule.setUserID(userID);

				if (organisationID == null)
					organisationID = loginConfigModule.getOrganisationID();
				else
					loginConfigModule.setOrganisationID(organisationID);
				
				if (initialContextFactory == null)
					initialContextFactory = loginConfigModule.getInitialContextFactory();
				else
					loginConfigModule.setInitialContextFactory(initialContextFactory);

				if (serverURL == null)
					serverURL = loginConfigModule.getServerURL();
				else
					loginConfigModule.setServerURL(serverURL);

				// perform a test login
				loginContext.setCredentials(userID, organisationID, password);
				Login.AsyncLoginResult res = Login.testLogin(loginContext);
				if (res.isSuccess()) {
					BeanUtils.copyProperties(loginResult, res);
					return;
				}
				else if (res.getException() != null)
					throw res.getException();
				else if ((res.getMessage() != null))
					throw new LoginException(res.getMessage());
				else
					throw new LoginException("Login failed and I have no idea, why!!!");
			}
		} catch (Throwable x) {
			// sth. went wrong => log and show normal login dialog
			LOGGER.error("Could not login using the specified program arguments!", x);
		}

		if (SplashScreen.waitForVisibleSplash()) // isSplashVisible())
			handleSplashLogin(loginContext, loginConfigModule, loginResult);
		else
			handleSWTLogin(loginContext, loginConfigModule, loginResult);
	}
	
	protected void handleSWTLogin(JFireLoginContext loginContext, LoginConfigModule loginConfigModule, Login.AsyncLoginResult loginResult) throws LoginException {
		LoginDialog loginDialog = new LoginDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		LoginDialog.registerSharedInstance(loginDialog);
		try {
			loginDialog.setLoginResult(loginResult);
			loginDialog.setLoginModule(loginConfigModule);
			loginDialog.setLoginContext(loginContext);
			
			// LoginDialog does all the work
			loginDialog.open();
		}
		finally {
			LoginDialog.deregisterSharedInstance();
		}
	}
	
	protected void handleSplashLogin(JFireLoginContext loginContext, LoginConfigModule loginConfigModule, final Login.AsyncLoginResult loginResult) throws LoginException 
	{
		final SplashLoginPanel loginPanel = new SplashLoginPanel(loginContext, loginConfigModule);
		loginPanel.doLayout();
//		Object mutex = new Object();
		try {
      Runnable runLogin =  new Runnable() {
				public void run() {
					SplashScreen.setSplashPanel(loginPanel);
					SplashScreen.setProgressIndeterminite(false);
					SplashScreen.setProgressMinMax(0,1);
					SplashScreen.setProgressValue(0);
					SplashScreen.setSplashMessage("");
				}
      };
      if (EventQueue.isDispatchThread())
        runLogin.run();
      else
        SwingUtilities.invokeAndWait(runLogin);
      
		} catch (InterruptedException e) {
			LoginException x = new LoginException("Error in SplashLogin: "+e.getMessage());
			x.initCause(e);
			throw x;
		} catch (InvocationTargetException e) {
			LoginException x = new LoginException("Error in SplashLogin: "+e.getMessage());
			x.initCause(e);
			throw x;
		}
		boolean loggedIn = false;
		int loginTries = 0;
		// Wait for the login
		while ((!loggedIn) && (loginTries < 3)) {
			try {
				synchronized (SplashScreen.getMutex()) {
					SplashScreen.getMutex().wait();
				}
			} catch (InterruptedException e) {
				LoginException x = new LoginException("Caught InterruptedException while waiting for login: "+e.getMessage());
				x.initCause(e);
				throw x;
			}
      if(loginPanel.isWorkOffline()) {
        SplashScreen.setSplashMessage("Work offline");
        break;
      }
			loginPanel.assignLoginValues();
			SplashScreen.setSplashMessage("Try to log in ...");
			Login.AsyncLoginResult testResult = Login.testLogin(loginContext);
			testResult.copyValuesTo(loginResult);
			loggedIn = testResult.isSuccess();
			if (loggedIn) {
				SplashScreen.setSplashMessage("Login successful");
				break;
			}
			loginTries++;
			
			SwingUtilities.invokeLater(new Runnable(){
				public void run() {
					if ((!loginResult.isWasAuthenticationErr()) && (loginResult.isSuccess()))
						return;
					else {
						// login failed
						if (loginResult.isWasAuthenticationErr()) {
							loginPanel.setErrMessage(JFireBasePlugin.getResourceString("login.error.authenticationFailed"));
						}
						else if (loginResult.isWasCommunicationErr()) {
							loginPanel.setErrMessage(JFireBasePlugin.getResourceString("login.error.communicatinError")+" "+loginResult.getException().getMessage());
						}
						else {
							String message = loginResult.getMessage();
							if (loginResult.getException() != null) {
								message += "\n"+loginResult.getException().getClass().getName()+": "+loginResult.getException().getLocalizedMessage();
								Throwable cause = loginResult.getException().getCause();
								while ( cause != null ) {
									message += "\n"+cause.getClass().getName()+": "+cause.getLocalizedMessage();
									cause = cause.getCause();
								}
								loginResult.getException().printStackTrace();
							}
							loginPanel.setErrMessage(message);
							
						}
					}
				}
			});
		}
		SplashScreen.resetSplashPanel();
	}

}
