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
