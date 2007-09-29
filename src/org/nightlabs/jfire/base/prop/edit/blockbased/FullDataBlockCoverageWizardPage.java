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
import org.nightlabs.base.ui.wizard.WizardHopPage;
import org.nightlabs.jfire.prop.PropertySet;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class FullDataBlockCoverageWizardPage extends WizardHopPage {

	protected FullDataBlockCoverageComposite fullDataBlockCoverageComposite;
	protected PropertySet prop;
	protected EditorStructBlockRegistry editorStructBlockRegistry;
	
	/**
	 * @param pageName
	 * @param title
	 */
	public FullDataBlockCoverageWizardPage(
			String pageName, String title, PropertySet propSet, EditorStructBlockRegistry editorStructBlockRegistry
			
	) {
		super(pageName, title);
		this.prop = propSet;
		this.editorStructBlockRegistry = editorStructBlockRegistry;
	}

	/**
	 * @see org.nightlabs.base.ui.wizard.DynamicPathWizardPage#createPageContents(org.eclipse.swt.widgets.Composite)
	 */
	public Control createPageContents(Composite parent) {
		fullDataBlockCoverageComposite = new FullDataBlockCoverageComposite(parent, SWT.NONE, prop, editorStructBlockRegistry);
		return fullDataBlockCoverageComposite;
	}

	public boolean isPageComplete() {
		return super.isPageComplete();
	}
	
	/**
	 * See {@link FullDataBlockCoverageComposite#updatePropertySet()}
	 */
	public void updatePropertySet() {
		if (fullDataBlockCoverageComposite != null)
			fullDataBlockCoverageComposite.updatePropertySet();
	}	

	/**
	 * See {@link FullDataBlockCoverageComposite#refresh(PropertySet)}
	 */
	public void refresh(PropertySet propertySet) {
		if (fullDataBlockCoverageComposite != null)
			fullDataBlockCoverageComposite.refresh(propertySet);
	}
	
	@Override
	public void onShow() {
		super.onShow();
		refresh(prop);
	}
	
	@Override
	public void onHide() {
		super.onHide();
		updatePropertySet();
	}
}
