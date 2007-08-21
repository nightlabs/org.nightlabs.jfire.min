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

import org.apache.log4j.Logger;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.nightlabs.base.action.ContributionItemSetRegistry;
import org.nightlabs.base.editor.Editor2PerspectiveRegistry;
import org.nightlabs.base.extensionpoint.EPProcessorException;
import org.nightlabs.base.part.PartVisibilityTracker;
import org.nightlabs.base.util.RCPUtil;
import org.nightlabs.jfire.base.login.LoginStateStatusLineContribution;
import org.nightlabs.jfire.base.resource.Messages;

public class JFireWorkbenchWindowAdvisor 
//extends SplashHandlingWorkbenchWindowAdvisor 
extends WorkbenchWindowAdvisor
{	
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(JFireWorkbenchWindowAdvisor.class);

	public JFireWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
		configurer.setShowPerspectiveBar(true);
		configurer.setShowMenuBar(true);
		configurer.setShowCoolBar(true);
		configurer.setShowProgressIndicator(true);
	}

	/**
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#createActionBarAdvisor(org.eclipse.ui.application.IActionBarConfigurer)
	 */
	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		return new JFireActionBuilder(configurer);
	}

	/**
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#postWindowCreate()
	 */
	public void postWindowCreate() {
		super.postWindowCreate();
		PartVisibilityTracker.sharedInstance().initialize();
		logger.debug("Initialized part-visibibity-tracker"); //$NON-NLS-1$
		try {
			ContributionItemSetRegistry.sharedInstance().checkPerspectiveListenerAdded();
		} catch (EPProcessorException e) {
			logger.error("There occured an error getting the ContributionItemSetRegistry", e); //$NON-NLS-1$
		}

		// activates the editor2Perspective check
		Editor2PerspectiveRegistry.sharedInstance().activate();
	}

	@Override
	public void postWindowRestore() throws WorkbenchException {
		super.postWindowRestore();
		try {
			ContributionItemSetRegistry.sharedInstance().checkPerspectiveListenerAdded();
		} catch (EPProcessorException e) {
			logger.error("There occured an error getting the ContributionItemSetRegistry", e); //$NON-NLS-1$
		}		
	}

	/**
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#preWindowOpen()
	 */
	public void preWindowOpen() {
		super.preWindowOpen();
		// TODO: dont forget JobErrorNotificationManager
//		PlatformUI.getWorkbench().getProgressService().setJobErrorNotificationManager(new JobErrorNotificationManager());		
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Additionally adds IWindowTrims to the created Window for displaying the logged in user and current locale 
	 * </p>
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#createWindowContents(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	public void createWindowContents(Shell shell) {
		super.createWindowContents(shell);
		LocaleStatusLineContribution locale = new LocaleStatusLineContribution("Locale"); //$NON-NLS-1$
		LoginStateStatusLineContribution loginState = new LoginStateStatusLineContribution(Messages.getString("org.nightlabs.jfire.base.app.JFireActionBuilder.loginStatus")); //$NON-NLS-1$
		RCPUtil.addContributionItemTrim(shell, locale, StatusLineManager.class.getName());
		RCPUtil.addContributionItemTrim(shell, loginState, StatusLineManager.class.getName());
	}

}
