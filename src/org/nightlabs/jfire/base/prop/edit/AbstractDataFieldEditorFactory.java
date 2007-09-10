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

package org.nightlabs.jfire.base.prop.edit;

import org.nightlabs.jfire.prop.AbstractDataField;
import org.nightlabs.jfire.prop.IStruct;

/**
 * Abstract base class for all {@link DataFieldEditorFactory}s 
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class AbstractDataFieldEditorFactory<F extends AbstractDataField> implements DataFieldEditorFactory<F> {

	/**
	 * Default constructor does nothing.
	 */
	public AbstractDataFieldEditorFactory() { }
	
	/**
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#getTargetPropDataType()
	 */
	public abstract Class<F> getPropDataFieldType();

	/**
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#getEditorType()
	 */
	public abstract String[] getEditorTypes();
	
	public abstract Class<? extends DataFieldEditor<F>> getDataFieldEditorClass();

	/**
	 * Default implementation instatiates a new instance of getEditorClass.getNewInstance()
	 * invokes setData(data) and returnes the new instance.
	 * 
	 */
	public DataFieldEditor<F> createPropDataFieldEditor(IStruct struct, F data, boolean setData) {
		DataFieldEditor<F> editor;
		try {
			editor = getDataFieldEditorClass().newInstance();
		} catch (Throwable t) {
			IllegalStateException ill = new IllegalStateException("Error instantiating "+getDataFieldEditorClass().getName()); //$NON-NLS-1$
			ill.initCause(t);
			throw ill;
		}
		if (setData)
			editor.setData(struct, data);
		return editor;
	}
	
}
