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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.prop.AbstractDataField;
import org.nightlabs.jfire.prop.AbstractStructField;
import org.nightlabs.jfire.prop.IStruct;

/**
 * Abstract base class for all  {@link DataFieldEditor} s with implementations for the listener stuff and other
 * common things for all field editors.<br/>
 * This class as well already implements ModifyListener so it can be used as listener for Text Widgets. 
 * @author  Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class AbstractDataFieldEditor implements DataFieldEditor, ModifyListener
{
	AbstractStructField structField;
	private IStruct struct;
	
	public AbstractDataFieldEditor()
	{
//		this.refStruct = refStruct;
	}
	
	/**
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public abstract Control createControl(Composite parent);

	private AbstractDataField _data;
	
	/**
	 * Not intended to be overridden.<br/>
	 * Subclasses should set their data in {@link #doSetData(AbstractDataField)}.
	 * 
	 * @see #doSetData(AbstractDataField)
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#setData(org.nightlabs.jfire.base.prop.AbstractDataField)
	 */
	public void setData(IStruct struct, AbstractDataField data) {
		refreshing = true;
		this.struct = struct;
		structField = data.getStructField();
		try  {
			_data = data;
			doSetData(data);
		} finally {
			refreshing = false;
		}
	}  	
	
	/**
	 * Subclasses can do things when data changes here.
	 * 
	 * @see DataFieldEditor#setData(AbstractDataField)  
	 */
	public abstract void doSetData(AbstractDataField data);
	
	/**
	 * Subclasses should perfom refreshing <b>here<b> and not override
	 * {@link #refresh(AbstractDataField)}
	 * 
	 * @param data
	 */
	public abstract void doRefresh();
	
	private boolean refreshing = false;
	
	/**
	 * Not intended to be overridden.
	 * 
	 * @see #doRefresh(AbstractDataField) 
	 * @param data
	 */
	public void refresh() {
		refreshing = true;
		try {
			doRefresh();
		} finally {
			refreshing = false;
		}		
	}
	
	private Collection changeListener = new LinkedList();
	/**
	 * 
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#addDataFieldEditorChangedListener(org.nightlabs.jfire.base.prop.edit.DataFieldEditorChangeListener)
	 */
	public synchronized void addDataFieldEditorChangedListener(DataFieldEditorChangeListener listener) {
		changeListener.add(listener);
	}
	/**
	 * 
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#removeDataFieldEditorChangedListener(org.nightlabs.jfire.base.prop.edit.DataFieldEditorChangeListener)
	 */
	public synchronized void removeDataFieldEditorChangedListener(DataFieldEditorChangeListener listener) {
		changeListener.add(listener);
	}
	
	protected synchronized void notifyChangeListeners() {
		// TODO: Rewrite to noitfy listener asynchronously
		for (Iterator it = changeListener.iterator(); it.hasNext(); ) {
			DataFieldEditorChangeListener listener = (DataFieldEditorChangeListener)it.next();
			listener.dataFieldEditorChanged(this);
		}
	}
	
	private boolean changed;
	
	/**
	 * Sets the changed state of this editor.
	 * @see  org.nightlabs.jfire.base.prop.edit.DataFieldEditor#setChanged(boolean)
	 * @uml.property  name="changed"
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
		if (!refreshing) {
			if (changed) {
				notifyChangeListeners();
			}
		}
	}
	
	/**
	 * Checks if this editors value has changed.
	 * @see  org.nightlabs.jfire.base.prop.edit.DataFieldEditor#isChanged()
	 * @uml.property  name="changed"
	 */
	public boolean isChanged() {
		return changed;
	}
	
	/**
	 * Returns the PropStructField this editor is associated with.
	 * 
	 * @return
	 */
	public AbstractStructField getStructField() {
		if (structField == null) {
			if (_data != null) {
				try {
					structField = struct.getStructField(
							_data.getStructBlockOrganisationID(), _data.getStructBlockID(),
							_data.getStructFieldOrganisationID(), _data.getStructFieldID()
						);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		if (structField == null)
			throw new IllegalStateException("The StructField can only be retrieved if the Editor has already been assigned a DataField.");
		
		return structField;
	}
	
	public void modifyText(ModifyEvent arg0) {
		setChanged(true);
	}

	protected DataFieldEditorFactory factory;
	
	/**
	 * 
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#getPropDataFieldEditorFactory()
	 */
	public DataFieldEditorFactory getPropDataFieldEditorFactory() {
		return factory;
	}

	/**
	 * 
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#setPropDataFieldEditorFactory(org.nightlabs.jfire.base.prop.edit.DataFieldEditorFactory)
	 */
	public void setPropDataFieldEditorFactory(DataFieldEditorFactory factory) {
		this.factory = factory;
	}
	
	protected IStruct getStruct() {
		return struct;
	}
}