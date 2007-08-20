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
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.eclipse.ui.PlatformUI;
import org.nightlabs.base.NLBasePlugin;
import org.nightlabs.config.Config;
import org.nightlabs.config.ConfigException;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.rcp.splash.SplashAlreadyTerminatedException;
import org.nightlabs.rcp.splash.SplashScreen;

/**
 * @see org.nightlabs.jfire.base.login.ILoginHandler
 * 
 * @author Alexander Bieber
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class JFireLoginHandler implements ILoginHandler {
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(JFireLoginHandler.class);

	private boolean autoLoginWithParams = true; // will be set false after it's done for the first time.

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
			String workstationID = null;

			if (autoLoginWithParams) {
				String[] args = NLBasePlugin.getDefault().getApplication().getArguments();
				for (int i = 0; i < args.length; i++) {
					String arg = args[i];
					String val = i + 1 < args.length ? args[i + 1] : null;

					if ("--login.userID".equals(arg)) //$NON-NLS-1$
						userID = val;
					else if ("--login.password".equals(arg)) //$NON-NLS-1$
						password = val;
					else if ("--login.organisationID".equals(arg)) //$NON-NLS-1$
						organisationID = val;
					else if ("--login.workstationID".equals(arg)) //$NON-NLS-1$
						workstationID = val;
					else if ("--login.initialContextFactory".equals(arg)) //$NON-NLS-1$
						initialContextFactory = val;
					else if ("--login.serverURL".equals(arg)) //$NON-NLS-1$
						serverURL = val;
				}

				autoLoginWithParams = false;
			}

			if (password != null) {
				LoginConfiguration latestConfig = loginConfigModule.getLatestLoginConfiguration();
				
				if (latestConfig == null) {
					latestConfig = new LoginConfiguration();
					latestConfig.init();
				}
				
//				if (workstationID != null)
//					loginConfigModule.setWorkstationID(workstationID);
				
				if (workstationID == null)
					workstationID = latestConfig.getWorkstationID();

				if (userID == null)
					userID = latestConfig.getUserID();
//				else
//					loginConfigModule.setUserID(userID);

				if (organisationID == null)
					organisationID = latestConfig.getOrganisationID();
//				else
//					loginConfigModule.setOrganisationID(organisationID);

				if (initialContextFactory == null)
					initialContextFactory = latestConfig.getInitialContextFactory();
//				else
//					loginConfigModule.setInitialContextFactory(initialContextFactory);

				if (serverURL == null)
					serverURL = latestConfig.getServerURL();
//				else
//					loginConfigModule.setServerURL(serverURL);
				
				loginConfigModule.setLatestLoginConfiguration(userID, workstationID, organisationID, serverURL, initialContextFactory, null, null);				

				// perform a test login
				loginContext.setCredentials(userID, organisationID, password);
				Login.AsyncLoginResult res = Login.testLogin(loginContext);
				if (res.isSuccess()) {
					BeanUtils.copyProperties(loginResult, res);
					return;
				}
				else if (res.isWasAuthenticationErr())
					throw new LoginException("Authentication error"); //$NON-NLS-1$
				else if (res.getException() != null)
					throw res.getException();
				else if ((res.getMessage() != null))
					throw new LoginException(res.getMessage());
				else
					throw new LoginException("Login failed and I have no idea, why!!!"); //$NON-NLS-1$
			}
		} catch (Throwable x) {
			// sth. went wrong => log and show normal login dialog
			logger.error("Could not login using the specified program arguments!", x); //$NON-NLS-1$
		}

		boolean loginDone = false;
		if (SplashScreen.waitForVisibleSplash()) { // isSplashVisible())
			try {
				handleSplashLogin(loginContext, loginConfigModule, loginResult);
				loginDone = true;
			} catch (Exception x) {
				// if it's a SplashAlreadyTerminatedException, we ignore it and silently switch to SWT login
				if (ExceptionUtils.indexOfThrowable(x, SplashAlreadyTerminatedException.class) < 0) {
					if (x instanceof LoginException)
						throw (LoginException)x;
					else if (x instanceof RuntimeException)
						throw (RuntimeException)x;
					else {
						LoginException n = new LoginException(x.getMessage());
						n.initCause(x);
						throw n;
					}
				}
			}
		}

		if (!loginDone)
			handleSWTLogin(loginContext, loginConfigModule, loginResult);
	}

	// TODO: should the creation and registration of login dialog be synchronized?? 
	protected void handleSWTLogin(JFireLoginContext loginContext, LoginConfigModule loginConfigModule, Login.AsyncLoginResult loginResult) throws LoginException {
		LoginDialog loginDialog = new LoginDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), loginResult, loginConfigModule, loginContext);
//		LoginDialog.registerSharedInstance(loginDialog);
//		try {
//			loginDialog.setLoginResult(loginResult);
//			loginDialog.setLoginModule(loginConfigModule);
//			loginDialog.setLoginContext(loginContext);

			// LoginDialog does all the work
			loginDialog.open();
//		}
//		finally {
//			LoginDialog.deregisterSharedInstance();
//		}
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
					SplashScreen.setSplashMessage(""); //$NON-NLS-1$
				}
			};
			if (EventQueue.isDispatchThread())
				runLogin.run();
			else
				SwingUtilities.invokeAndWait(runLogin);

		} catch (InterruptedException e) {
			LoginException x = new LoginException("Error in SplashLogin: "+e.getMessage()); //$NON-NLS-1$
			x.initCause(e);
			throw x;
		} catch (InvocationTargetException e) {
			LoginException x = new LoginException("Error in SplashLogin: "+e.getMessage()); //$NON-NLS-1$
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
				LoginException x = new LoginException("Caught InterruptedException while waiting for login: "+e.getMessage()); //$NON-NLS-1$
				x.initCause(e);
				throw x;
			}
			if(loginPanel.isWorkOffline()) {
				SplashScreen.setSplashMessage(Messages.getString("login.JFireLoginHandler.workOffline")); //$NON-NLS-1$
				loginResult.setWorkOffline(true);
				break;
			}
			
			boolean saveConfig = loginPanel.assignLoginValues();
			
			SplashScreen.setSplashMessage(Messages.getString("login.JFireLoginHandler.tryToLogin")); //$NON-NLS-1$
			Login.AsyncLoginResult testResult = Login.testLogin(loginContext);
			testResult.copyValuesTo(loginResult);
			loggedIn = testResult.isSuccess();
			if (loggedIn) {
				LoginConfigModule persistentLoginModule;
				try {
					persistentLoginModule = ((LoginConfigModule)Config.sharedInstance().createConfigModule(LoginConfigModule.class));
				} catch (ConfigException e) {
					throw new RuntimeException(e);
				}
				
				if (saveConfig)
					loginConfigModule.saveLatestConfiguration();
				
				try {
					BeanUtils.copyProperties(persistentLoginModule, loginConfigModule);
					persistentLoginModule.setChanged();
				} catch (Exception e) {
					logger.error("Saving config failed!", e); //$NON-NLS-1$
				}
				
				SplashScreen.setSplashMessage(Messages.getString("login.JFireLoginHandler.loginSuccessful")); //$NON-NLS-1$
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
							loginPanel.setErrMessage(Messages.getString("org.nightlabs.jfire.base.login.JFireLoginHandler.loginPanel.errMessage_authenticationFailed")); //$NON-NLS-1$
						}
						else if (loginResult.isWasCommunicationErr()) {
							loginPanel.setErrMessage(
									String.format(Messages.getString("org.nightlabs.jfire.base.login.JFireLoginHandler.loginPanel.errMessage_communicationError"), //$NON-NLS-1$
									new Object[] { loginResult.getException().getMessage() }
							));
						}
						else {
							String message = loginResult.getMessage();
							if (loginResult.getException() != null) {
								message += "\n"+loginResult.getException().getClass().getName()+": "+loginResult.getException().getLocalizedMessage(); //$NON-NLS-1$ //$NON-NLS-2$
								Throwable cause = loginResult.getException().getCause();
								while ( cause != null ) {
									message += "\n"+cause.getClass().getName()+": "+cause.getLocalizedMessage(); //$NON-NLS-1$ //$NON-NLS-2$
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
