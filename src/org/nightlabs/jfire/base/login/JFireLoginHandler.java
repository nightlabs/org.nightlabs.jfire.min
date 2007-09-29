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

import javax.security.auth.login.LoginException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.NLBasePlugin;

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

		handleSWTLogin(loginContext, loginConfigModule, loginResult);
	}

	// TODO: should the creation and registration of login dialog be synchronized?? 
	protected void handleSWTLogin(JFireLoginContext loginContext, LoginConfigModule loginConfigModule, Login.AsyncLoginResult loginResult) 
	throws LoginException 
	{		
//		LoginDialog loginDialog = new LoginDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), loginResult, loginConfigModule, loginContext);
		LoginDialog loginDialog = new LoginDialog(Display.getDefault().getActiveShell(), loginResult, loginConfigModule, loginContext);
		// LoginDialog does all the work
		loginDialog.open();
	}
}
