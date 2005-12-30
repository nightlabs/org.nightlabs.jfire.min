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

package org.nightlabs.jfire.base.login.action;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchWindow;

import org.nightlabs.jfire.base.JFireBasePlugin;
import org.nightlabs.jfire.base.login.Login;

/**
 * @author Alexander Bieber
 */
public class LoginAction extends LSDWorkbenchWindowActionDelegate {
	private static final Logger LOGGER = Logger.getLogger(LoginAction.class);

	private static ImageDescriptor loginIcon = null;
	private static ImageDescriptor logoutIcon = null;
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		super.init(window);
		if (loginIcon == null) {
			loginIcon = ImageDescriptor.createFromURL(JFireBasePlugin.getDefault().getBundle().getEntry(JFireBasePlugin.getResourceString("actions.login.icons.login")));
		}
		if (logoutIcon == null) {
			logoutIcon = ImageDescriptor.createFromURL(JFireBasePlugin.getDefault().getBundle().getEntry(JFireBasePlugin.getResourceString("actions.login.icons.logout")));
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		try {			
			Login login = Login.getLogin(false);
			if (Login.isLoggedIn()) 
				login.logout();
			else
				Login.getLogin();
			
		} catch (LoginException e) {
			LOGGER.error("Login failed",e);
		}
	}
	
	public void loginStateChanged(int loginState, IAction action) {
		switch (loginState) {
			case Login.LOGINSTATE_LOGGED_IN:
				action.setImageDescriptor(logoutIcon);
				action.setToolTipText(JFireBasePlugin.getResourceString("actions.login.tooltip.logout"));
				break;
			case Login.LOGINSTATE_LOGGED_OUT:
				action.setImageDescriptor(loginIcon);
				action.setToolTipText("You are working offline, click to login");
			break;				
			case Login.LOGINSTATE_OFFLINE:
				action.setImageDescriptor(loginIcon);
				action.setToolTipText(JFireBasePlugin.getResourceString("actions.login.tooltip.login"));
				break;				
		}
	}
}
