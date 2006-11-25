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
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class PropStructFieldSearchItemEditorHelper implements
		PropSearchFilterItemEditorHelper {

	
	protected AbstractStructField personStructField;
	
	
	protected PropStructFieldSearchItemEditorHelper() {
		super();
	}	
	
	/**
	 * Constructs a new StructFieldSearchItemEditorHelper
	 * and calls {@link #init(AbstractPersonStructField)}.
	 * 
	 * @param personStructField 
	 */
	public PropStructFieldSearchItemEditorHelper(AbstractStructField _personStructField) {
		super();
		init(_personStructField);
	}
	
	
	public void init(AbstractStructField personStructField) {
		this.personStructField = personStructField;
	}

	/**
	 * @see org.nightlabs.jfire.base.prop.search.PropSearchFilterItemEditorHelper#getControl(org.eclipse.swt.widgets.Composite)
	 */
	public abstract Control getControl(Composite parent);

	/**
	 * @see org.nightlabs.jfire.base.prop.search.PropSearchFilterItemEditorHelper#getSearchFilterItem()
	 */
	public abstract SearchFilterItem getSearchFilterItem();

	/**
	 * @see org.nightlabs.jfire.base.prop.search.PropSearchFilterItemEditorHelper#newInstance()
	 */
	public PropSearchFilterItemEditorHelper newInstance() {
		PropStructFieldSearchItemEditorHelper result;
		try {
			result = (PropStructFieldSearchItemEditorHelper)this.getClass().newInstance();
		} catch (Throwable t) {
			IllegalStateException ill = new IllegalStateException("Error instatiating new StructFieldSearchItemEditorHelper "+this);
			ill.initCause(t);
			throw ill;
		}
		result.init(this.personStructField);
		return result;
	}

	/**
	 * @see org.nightlabs.jfire.base.prop.search.PropSearchFilterItemEditorHelper#getDisplayName()
	 */
	public String getDisplayName() {
		return personStructField.getStructBlockID()+": "+personStructField.getStructFieldKey();
	}

}
