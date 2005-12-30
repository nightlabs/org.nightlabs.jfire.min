/*
 * Created 	on Aug 11, 2005
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.config;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;

import org.nightlabs.ipanema.base.login.Login;
import org.nightlabs.ipanema.config.WorkstationConfigSetup;
import org.nightlabs.ipanema.workstation.id.WorkstationID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public abstract class AbstractWorkstationConfigModulePreferencePage extends
		AbstractConfigModulePreferencePage {

	/**
	 * 
	 */
	public AbstractWorkstationConfigModulePreferencePage() {
		super();
	}

	/**
	 * @param title
	 */
	public AbstractWorkstationConfigModulePreferencePage(String title) {
		super(title);
	}

	/**
	 * @param title
	 * @param image
	 */
	public AbstractWorkstationConfigModulePreferencePage(String title,
			ImageDescriptor image) {
		super(title, image);
	}

	/**
	 * Sets the current ConfigID to the workstation of currently logged in user.
	 *  
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		try {
			Login login = Login.getLogin();
			setCurrentConfigID(WorkstationConfigSetup.getWorkstationConfigID(WorkstationID.create(login.getOrganisationID(), login.getWorkstationID())), false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		super.init(workbench);
	}
	
}
