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

package org.nightlabs.jfire.base.person.search;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.nightlabs.jfire.person.AbstractPersonStructField;
import org.nightlabs.jdo.search.SearchFilterItem;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class PersonStructFieldSearchItemEditorHelper implements
		PersonSearchFilterItemEditorHelper {

	
	protected AbstractPersonStructField personStructField;
	
	
	protected PersonStructFieldSearchItemEditorHelper() {
		super();
	}	
	
	/**
	 * Constructs a new PersonStructFieldSearchItemEditorHelper
	 * and calls {@link #init(AbstractPersonStructField)}.
	 * 
	 * @param personStructField 
	 */
	public PersonStructFieldSearchItemEditorHelper(AbstractPersonStructField _personStructField) {
		super();
		init(_personStructField);
	}
	
	
	public void init(AbstractPersonStructField personStructField) {
		this.personStructField = personStructField;
	}

	/**
	 * @see org.nightlabs.jfire.base.person.search.PersonSearchFilterItemEditorHelper#getControl(org.eclipse.swt.widgets.Composite)
	 */
	public abstract Control getControl(Composite parent);

	/**
	 * @see org.nightlabs.jfire.base.person.search.PersonSearchFilterItemEditorHelper#getSearchFilterItem()
	 */
	public abstract SearchFilterItem getSearchFilterItem();

	/**
	 * @see org.nightlabs.jfire.base.person.search.PersonSearchFilterItemEditorHelper#newInstance()
	 */
	public PersonSearchFilterItemEditorHelper newInstance() {
		PersonStructFieldSearchItemEditorHelper result;
		try {
			result = (PersonStructFieldSearchItemEditorHelper)this.getClass().newInstance();
		} catch (Throwable t) {
			IllegalStateException ill = new IllegalStateException("Error instatiating new PersonStructFieldSearchItemEditorHelper "+this);
			ill.initCause(t);
			throw ill;
		}
		result.init(this.personStructField);
		return result;
	}

	/**
	 * @see org.nightlabs.jfire.base.person.search.PersonSearchFilterItemEditorHelper#getDisplayName()
	 */
	public String getDisplayName() {
		return personStructField.getPersonStructBlockID()+": "+personStructField.getPersonStructFieldID();
	}

}
