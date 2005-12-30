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

package org.nightlabs.jfire.base.person.edit;

import org.nightlabs.jfire.person.AbstractPersonDataField;

/**
 * Abstract base class for all {@link PersonDataFieldEditorFactory}s 
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class AbstractPersonDataFieldEditorFactory implements PersonDataFieldEditorFactory {

	/**
	 * Default constructor does nothing.
	 */
	public AbstractPersonDataFieldEditorFactory() { }
	
	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#getTargetPersonDataType()
	 */
	public abstract Class getTargetPersonDataFieldType();

	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#getEditorType()
	 */
	public abstract String getEditorType();
	
	public abstract Class getPersonDataFieldEditorClass();

	/**
	 * Default implementation instatiates a new instance of getEditorClass.getNewInstance()
	 * invokes setData(data) and returnes the new instance.
	 * 
	 */
	public PersonDataFieldEditor createPersonDataFieldEditor(AbstractPersonDataField data, boolean setData) {
		PersonDataFieldEditor editor;
		try {
			editor = (PersonDataFieldEditor)getPersonDataFieldEditorClass().newInstance();
		} catch (Throwable t) {
			IllegalStateException ill = new IllegalStateException("Error instantiating "+getPersonDataFieldEditorClass().getName());
			ill.initCause(t);
			throw ill;
		}
		if (setData)
			editor.setData(data);
		return editor;
	}
	
}
