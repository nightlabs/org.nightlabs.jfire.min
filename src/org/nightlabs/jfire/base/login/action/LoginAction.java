/*
 * Created 	on Nov 5, 2004
 * 					by Alexander Bieber
 *
 */
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
