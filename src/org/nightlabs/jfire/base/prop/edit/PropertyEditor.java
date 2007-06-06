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
import org.nightlabs.jfire.base.prop.edit.blockbased.DataBlockEditorChangedListener;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;

/**
 * A interface for common methods of all PropEditors, either
 * block or field based.<br/>
 * Common useage is:<br/>
 * <ul>
 * 	<li>Create a the Editor with parameterless constructor or one specific to the type your using.</li>
 *	<li>Link the editor to a propSet by using {@link #setProp(Property)</li>
 *	<li>Create the UI representation by calling {@link #createControl(Composite, DataBlockEditorChangedListener, boolean)}</li>
 *	<li>If not done with {@link #createControl(Composite, DataBlockEditorChangedListener, boolean)} set the field values by
 * 		calling {@link #refreshControl(DataBlockEditorChangedListener)}.
 *	</li>
 *  <li>To update the propSet with the values of the editor call {@link #updatePropertySet()}</li>
 * </ul>
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public interface PropertyEditor {
	
	/**
	 * Link the editor to a Property.
	 * @param propSet
	 */
	public void setPropertySet(PropertySet propSet, IStruct propStruct);
	
	/**
	 * Link the editor to a Property and refresh the Control
	 * if refresh is true.
	 * 
	 * @param propSet
	 * @param refresh
	 */
	public void setPropertySet(PropertySet propSet, IStruct propStruct, boolean refresh);
	
	/**
	 * Create the UI representation of the PropertyEditor and associate
	 * the passed changeListener with the fields. If refresh is true
	 * refresh the UI representation. This should be done synchronously
	 * on the GUI-Thread to avoid InvalidThreadAccessExceptions.
	 * 
	 * @param parent
	 * @param refresh
	 * @return
	 */
	public Control createControl(Composite parent, boolean refresh);
	
	public void disposeControl();
	
	/**
	 * Refresh the UI representation.
	 * Implementors should refresh on the GUI-Thread to avoid 
	 * InvalidThreadAccessExceptions.
	 * 
	 * @param changeListener
	 */
	public void refreshControl();
	
	/**
	 * Set the values from the editor to the Property it
	 * is associated with.
	 *
	 */
	public void updatePropertySet();
}
