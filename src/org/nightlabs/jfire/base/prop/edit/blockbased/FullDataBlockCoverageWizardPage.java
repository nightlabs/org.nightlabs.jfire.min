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

package org.nightlabs.jfire.base.prop.edit.blockbased;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.base.wizard.WizardHopPage;
import org.nightlabs.jfire.prop.Property;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class FullDataBlockCoverageWizardPage extends WizardHopPage {

	protected String editorScope;
	protected FullDataBlockCoverageComposite fullDataBlockCoverageComposite;
	protected Property prop;
	
	/**
	 * @param pageName
	 * @param title
	 */
	public FullDataBlockCoverageWizardPage(String pageName, String title, String editorScope, Property prop) {
		super(pageName, title);
		this.editorScope = editorScope;
		this.prop = prop;
	}

	/**
	 * @see org.nightlabs.base.wizard.DynamicPathWizardPage#createPageContents(org.eclipse.swt.widgets.Composite)
	 */
	public Control createPageContents(Composite parent) {
		fullDataBlockCoverageComposite = new FullDataBlockCoverageComposite(parent, SWT.NONE, editorScope, prop);
		return fullDataBlockCoverageComposite;
	}

	public boolean isPageComplete() {
		return super.isPageComplete();
	}
	
	public void updateProp() {
		if (fullDataBlockCoverageComposite != null)
			fullDataBlockCoverageComposite.updateProp();
	}	

}
