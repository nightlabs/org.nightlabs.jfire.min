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

package org.nightlabs.jfire.base.person.preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.composite.XComposite.LayoutDataMode;
import org.nightlabs.base.ui.composite.XComposite.LayoutMode;
import org.nightlabs.jfire.base.preferences.LSDPreferencePage;


/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonStructOrderPreferencePage 
extends LSDPreferencePage
{
	// TODO what is this wrapper composite good for???
	private XComposite wrapperComposite;
	private PersonStructOrderComposite structOrderComposite;
	
	public PersonStructOrderPreferencePage() {
		super();
		System.out.println("Constructor of PersonStructOrderPreferencePage called"); //$NON-NLS-1$
	}
	
  public void createPartContents(Composite parent) 
  {
  	createWrapperComposite(parent);
		structOrderComposite = new PersonStructOrderComposite(wrapperComposite,SWT.NONE);	
  }
  
  protected void createWrapperComposite(Composite parent) 
  {
  	if (wrapperComposite == null)
  		wrapperComposite = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.NONE); 	
  }

	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		
	}

	protected void performApply() {
		super.performApply();
		PersonStructOrderConfigModule.sharedInstance().setStructBlockDisplayOrder(
				structOrderComposite.getStructBlockOrder()
			);		
	}
}
