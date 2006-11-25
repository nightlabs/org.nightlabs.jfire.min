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

package org.nightlabs.jfire.base.prop.search;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jdo.search.SearchFilterItem;
import org.nightlabs.jfire.prop.AbstractStructField;

/**
 * A concrete StructFieldSearchItemEditorHelper that
 * serves as a manager for other StructFieldSearchItemEditorHelper.
 * It searches for StructFieldSearchItemEditorHelper in
 * the PersonSearchFilterItemEditorHelperRegistry liked to
 * a class of PersonStructFields.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PropStructFieldSearchItemEditorManager extends
		PropStructFieldSearchItemEditorHelper {

	/**
	 * 
	 */
	public PropStructFieldSearchItemEditorManager() {
		super();
	}

	/**
	 * @param personStructField
	 */
	public PropStructFieldSearchItemEditorManager(
			AbstractStructField personStructField) {
		super(personStructField);
	}

	
	private PropSearchFilterItemEditorHelper helper;
	private Control helperControl;
	
	/**
	 * This searches for the right helper,
	 * gets and remembers a new instance of it
	 * and the Control it returned.
	 * 
	 * @see org.nightlabs.jfire.base.prop.search.PropSearchFilterItemEditorHelper#getControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control getControl(Composite parent) {
		if (helper != null)
			if (helperControl != null)
				return helperControl;
			
		PersonSearchFilterItemEditorHelperRegistry registry = PersonSearchFilterItemEditorHelperRegistry.sharedInstance();
		if (personStructField == null)			
			throw new IllegalStateException("Member personStructField is null. init(personStructField) might not have been called.");
		
		try {
			helper = registry.getEditorHelper(personStructField.getClass());
		} catch (PersonSearchFilterItemEditorHelperNotFoundException e) {
			IllegalStateException ill = new IllegalStateException("No helper found for class "+personStructField.getClass().getName());
			ill.initCause(e);
			throw ill;
		}
		helper = helper.newInstance();
		if (helper instanceof PropStructFieldSearchItemEditorHelper)
			((PropStructFieldSearchItemEditorHelper)helper).init(this.personStructField);
		helperControl = helper.getControl(parent); 
		return helperControl;
	}

	/**
	 * Delegates to the helper from the registry.
	 * @see #getControl(Composite)	
	 * @see org.nightlabs.jfire.base.prop.search.PropSearchFilterItemEditorHelper#getSearchFilterItem()
	 */
	public SearchFilterItem getSearchFilterItem() {
		if (helper == null)
			throw new IllegalStateException("SearchItemEditorHelper is null and can not be asked for the SearchFilterItem");
		
		return helper.getSearchFilterItem();
	}

	/**
	 * @see org.nightlabs.jfire.base.prop.search.PropSearchFilterItemEditorHelper#close()
	 */
	public void close() {
	}

}
