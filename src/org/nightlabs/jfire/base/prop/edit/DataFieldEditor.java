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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.prop.AbstractDataField;
import org.nightlabs.jfire.prop.IStruct;


/**
 * @author  Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public interface DataFieldEditor<F extends AbstractDataField> {
	
	/**
	 * @param factory
	 */
	public void setPropDataFieldEditorFactory(DataFieldEditorFactory factory);
	public DataFieldEditorFactory getPropDataFieldEditorFactory();
	
	/**
	 * Here a data field editor should add its
	 * control to a parent composite.<br/>
	 * The Composite returned should be a singelton
	 * and be updated with data changes.
	 * No data-display will be made here. See {@link #setData(AbstractDataField)}.
	 * 
	 * @param parent
	 * @return
	 */	
	public Control createControl(Composite parent);
	/**
	 * Should return the Control created in {@link #createControl(Composite)}.
	 * @return the Control created in {@link #createControl(Composite)}.
	 */
	public Control getControl();
	
	/**
	 * Editors should refresh their data during this method.
	 * The composite should be refreshed as well.
	 * @param data
	 */
	public void setData(IStruct struct, F data);
	
	public void refresh();
	
	/**
	 * updates the property
	 * This method should be Threadsave
	 */
	public void updateProperty();
	
	public void addDataFieldEditorChangedListener(DataFieldEditorChangeListener listener);
	public void removeDataFieldEditorChangedListener(DataFieldEditorChangeListener listener);
	public void setChanged(boolean changed);
	public boolean isChanged();
	
}
