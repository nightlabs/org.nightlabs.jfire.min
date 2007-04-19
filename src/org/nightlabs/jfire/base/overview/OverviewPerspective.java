package org.nightlabs.jfire.base.overview;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.nightlabs.base.util.RCPUtil;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class OverviewPerspective
implements IPerspectiveFactory 
{

	public void createInitialLayout(IPageLayout layout) 
	{
		layout.setEditorAreaVisible(true);
		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, 0.25f,	IPageLayout.ID_EDITOR_AREA);
		left.addView(OverviewView.VIEW_ID);
		RCPUtil.addAllPerspectiveShortcuts(layout);
		layout.addShowViewShortcut(OverviewView.VIEW_ID);		
	}
	
}
