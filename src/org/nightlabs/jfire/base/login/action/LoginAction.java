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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.nightlabs.base.exceptionhandler.ExceptionHandlerRegistry;
import org.nightlabs.base.resource.SharedImages;
import org.nightlabs.base.resource.SharedImages.ImageDimension;
import org.nightlabs.base.resource.SharedImages.ImageFormat;
import org.nightlabs.jfire.base.JFireBasePlugin;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.resource.Messages;

/**
 * @author Alexander Bieber
 * @author marco schulze - marco at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class LoginAction 
extends LSDWorkbenchWindowActionDelegate 
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(LoginAction.class);

	private static ImageDescriptor loginIcon_menu = null;
	private static ImageDescriptor logoutIcon_menu = null;

	private static ImageDescriptor loginIcon_toolbar = null;
	private static ImageDescriptor logoutIcon_toolbar = null;


	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		super.init(window);
		
		if (loginIcon_menu == null)
			loginIcon_menu = SharedImages.getSharedImageDescriptor(JFireBasePlugin.getDefault(), LoginAction.class, "Login", ImageDimension._16x16, ImageFormat.png); //$NON-NLS-1$

		if (logoutIcon_menu == null)
			logoutIcon_menu = SharedImages.getSharedImageDescriptor(JFireBasePlugin.getDefault(), LoginAction.class, "Logout", ImageDimension._16x16, ImageFormat.png); //$NON-NLS-1$

		if (loginIcon_toolbar == null)
			loginIcon_toolbar = SharedImages.getSharedImageDescriptor(JFireBasePlugin.getDefault(), LoginAction.class, "Login", ImageDimension._24x24, ImageFormat.png); //$NON-NLS-1$

		if (logoutIcon_toolbar == null)
			logoutIcon_toolbar = SharedImages.getSharedImageDescriptor(JFireBasePlugin.getDefault(), LoginAction.class, "Logout", ImageDimension._24x24, ImageFormat.png); //$NON-NLS-1$
		
		if (Login.isLoggedIn() && action != null) {
			loginStateChanged(Login.sharedInstance().getLoginState(), action);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		try {			
			Login login = Login.getLogin(false);
			if (Login.isLoggedIn())
				login.workOffline();
			else {
				try {
					Login.getLogin(false).setForceLogin(true);
					Login.getLogin();
				} catch (LoginException e) {
					if (Login.sharedInstance().getLoginState() != Login.LOGINSTATE_OFFLINE)
						ExceptionHandlerRegistry.asyncHandleException(e);
					logger.error("Login failed",e); //$NON-NLS-1$
				}
			}
		} catch (LoginException e) {
			ExceptionHandlerRegistry.asyncHandleException(e);
			logger.error("Login failed",e); //$NON-NLS-1$
		}
	}

	public void loginStateChanged(int loginState, IAction action)
	{
		super.loginStateChanged(loginState, action);
		
		ImageDescriptor loginIcon = null;
		ImageDescriptor logoutIcon = null;

		if (action.getId().endsWith("#menu")) { //$NON-NLS-1$
			loginIcon = loginIcon_menu;
			logoutIcon = logoutIcon_menu;
		}
		else if (action.getId().endsWith("#toolbar")) { //$NON-NLS-1$
			loginIcon = loginIcon_toolbar;
			logoutIcon = logoutIcon_toolbar;
		}
		else
			throw new IllegalStateException("This action.id does not end on #menu or #toolbar!"); //$NON-NLS-1$

		switch (loginState) {
			case Login.LOGINSTATE_LOGGED_IN:
				action.setImageDescriptor(logoutIcon);
				action.setToolTipText(Messages.getString("org.nightlabs.jfire.base.login.action.LoginAction.action.toolTipText_loggedIn")); //$NON-NLS-1$
				action.setHoverImageDescriptor(logoutIcon);
			break;
			case Login.LOGINSTATE_LOGGED_OUT:
				action.setImageDescriptor(loginIcon);
				action.setToolTipText(Messages.getString("org.nightlabs.jfire.base.login.action.LoginAction.action.toolTipText_loggedOut")); //$NON-NLS-1$
				action.setHoverImageDescriptor(loginIcon);
			break;
			case Login.LOGINSTATE_OFFLINE:
				action.setImageDescriptor(loginIcon);
				action.setToolTipText(Messages.getString("org.nightlabs.jfire.base.login.action.LoginAction.action.toolTipText_offline")); //$NON-NLS-1$
				action.setHoverImageDescriptor(loginIcon);
			break;
		}
	}
	
	private IAction action = null;
	public void selectionChanged(IAction action, ISelection selection) {
		this.action = action;
		super.selectionChanged(action, selection);
	}
}
