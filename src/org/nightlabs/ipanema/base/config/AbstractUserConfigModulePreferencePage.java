/*
 * Created 	on Aug 11, 2005
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.config;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;

import org.nightlabs.ipanema.base.login.Login;
import org.nightlabs.ipanema.config.UserConfigSetup;
import org.nightlabs.ipanema.security.id.UserID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public abstract class AbstractUserConfigModulePreferencePage extends
		AbstractConfigModulePreferencePage {

	/**
	 * 
	 */
	public AbstractUserConfigModulePreferencePage() {
		super();
	}

	/**
	 * @param title
	 */
	public AbstractUserConfigModulePreferencePage(String title) {
		super(title);
	}

	/**
	 * @param title
	 * @param image
	 */
	public AbstractUserConfigModulePreferencePage(String title,
			ImageDescriptor image) {
		super(title, image);
	}

	/**
	 * Sets the current ConfigID to the user currently logged in user.
	 *  
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		try {
			Login login = Login.getLogin();
			setCurrentConfigID(UserConfigSetup.getUserConfigID(UserID.create(login.getOrganisationID(), login.getUserID())), false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		super.init(workbench);
	}
	
}
