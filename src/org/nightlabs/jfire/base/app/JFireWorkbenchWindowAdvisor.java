/*
 * Created 	on Sep 4, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.app;

import org.apache.log4j.Logger;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;

import org.nightlabs.base.part.PartVisibilityTracker;
import org.nightlabs.rcp.splash.SplashHandlingWorkbenchWindowAdvisor;

public class JFireWorkbenchWindowAdvisor extends
		SplashHandlingWorkbenchWindowAdvisor 
	{
	
	protected Logger LOGGER = Logger.getLogger(JFireWorkbenchWindowAdvisor.class);

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
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#postWindowClose()
	 */
	public void postWindowClose() {
		super.postWindowClose();
//    JFireActionBuilder builder = (JFireActionBuilder) configurer.getData(BUILDER_KEY);
//    if (builder != null) {
//        builder.dispose();
//    }
	}



	/**
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#postWindowCreate()
	 */
	public void postWindowCreate() {
		super.postWindowCreate();
		PartVisibilityTracker.sharedInstance().initialize();
		LOGGER.debug("Initialized part-visibibity-tracker");
	}



	/**
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#preWindowOpen()
	 */
	public void preWindowOpen() {
		super.preWindowOpen();
		// TODO: dont forget JobErrorNotificationManager
//		PlatformUI.getWorkbench().getProgressService().setJobErrorNotificationManager(new JobErrorNotificationManager());
	}
	
	
	
	

}
