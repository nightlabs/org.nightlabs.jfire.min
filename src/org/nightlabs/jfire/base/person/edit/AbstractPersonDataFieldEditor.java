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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.nightlabs.jfire.base.person.PersonStructProvider;
import org.nightlabs.jfire.person.AbstractPersonDataField;
import org.nightlabs.jfire.person.AbstractPersonStructField;

/**
 * Abstract base class for all {@link PersonDataFieldEditor}s with implementations
 * for the listener stuff and other common things for all field editors.<br/>
 * This class as well already implements ModifyListener so it can be used as
 * listener for Text Widgets. 
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class AbstractPersonDataFieldEditor implements PersonDataFieldEditor, ModifyListener {

	/**
	 * Default constructor does nothing.
	 */
	public AbstractPersonDataFieldEditor() { }
	
	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public abstract Control createControl(Composite parent);

	private AbstractPersonDataField _data;
	
	/**
	 * Not intendet to be overridden.<br/>
	 * Sublclasses should set their data in {@link #doSetData(AbstractPersonDataField)}.
	 * 
	 * @see #doSetData(AbstractPersonDataField)
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#setData(org.nightlabs.jfire.base.person.AbstractPersonDataField)
	 */
	public void setData(AbstractPersonDataField data) {
		refreshing = true;
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
	 *@see PersonDataFieldEditor#setData(AbstractPersonDataField)  
	 */
	public abstract void doSetData(AbstractPersonDataField data);
	
	/**
	 * Subclasses should perfom refreshing <b>here<b> and not override
	 * {@link #refresh(AbstractPersonDataField)}
	 * 
	 * @param data
	 */
	public abstract void doRefresh();
	
	private boolean refreshing = false;
	
	/**
	 * Not intendeted to be overridden.
	 * 
	 * @see #doRefresh(AbstractPersonDataField) 
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
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#addDataFieldEditorChangedListener(org.nightlabs.jfire.base.person.edit.DataFieldEditorChangeListener)
	 */
	public synchronized void addDataFieldEditorChangedListener(DataFieldEditorChangeListener listener) {
		changeListener.add(listener);
	}
	/**
	 * 
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#removeDataFieldEditorChangedListener(org.nightlabs.jfire.base.person.edit.DataFieldEditorChangeListener)
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
	 * 
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#setChanged(boolean)
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
	 * 
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#isChanged()
	 */
	public boolean isChanged() {
		return changed;
	}
	
	/**
	 * Returns the PersonStructField this editor is
	 * associated with.
	 * 
	 * @return
	 */
	public AbstractPersonStructField getPersonStructField() {
		try {
			return PersonStructProvider.getPersonStructure().getPersonStructField(
				_data.getPersonStructBlockOrganisationID(),
				_data.getPersonStructBlockID(),
				_data.getPersonStructFieldOrganisationID(),
				_data.getPersonStructFieldID()
			);
		} catch (Exception e) {
			IllegalStateException ill = new IllegalStateException("Caught exception while accessing PresonStructFactory.");
			ill.initCause(e);
			throw ill;
		}
	}
	
	public void modifyText(ModifyEvent arg0) {
		setChanged(true);
	}

	protected PersonDataFieldEditorFactory factory;
	
	/**
	 * 
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#getPersonDataFieldEditorFactory()
	 */
	public PersonDataFieldEditorFactory getPersonDataFieldEditorFactory() {
		return factory;
	}

	/**
	 * 
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#setPersonDataFieldEditorFactory(org.nightlabs.jfire.base.person.edit.PersonDataFieldEditorFactory)
	 */
	public void setPersonDataFieldEditorFactory(PersonDataFieldEditorFactory factory) {
		this.factory = factory;
	}
	
	

}
