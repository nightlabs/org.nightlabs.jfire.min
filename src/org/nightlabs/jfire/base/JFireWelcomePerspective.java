package org.nightlabs.jfire.base;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.internal.Workbench;

import org.nightlabs.base.util.RCPUtil;

/**
 * The perspective factory for the RCP Browser Example's perspective.
 * 
 * @since 3.0
 */
public class JFireWelcomePerspective implements IPerspectiveFactory {

	
    public static final String ID_PERSPECTIVE = "org.nightlabs.jfire.base.perspective.JFireWelcomePerspective";
    
	/**
	 * Creates the initial layout of the JFireWelcomePerspective.<br/>
	 * By now nothing no view is in the Perspective
	 * 
	 */
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
		layout.addView(
			JFireWelcomeView.ID_VIEW,
      IPageLayout.TOP,
      IPageLayout.RATIO_MAX,
      IPageLayout.ID_EDITOR_AREA
		);
		
		
		RCPUtil.addAllPerspectiveShortcuts(layout);
		
		layout.addShowViewShortcut(JFireWelcomeView.ID_VIEW);    
	}
}
