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

package org.nightlabs.jfire.base.prop.edit.blockbased;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.exceptionhandler.ExceptionHandlerRegistry;
import org.nightlabs.base.wizard.WizardHopPage;
import org.nightlabs.jfire.prop.DataBlockGroup;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.exception.DataBlockGroupNotFoundException;
import org.nightlabs.jfire.prop.exception.DataNotFoundException;
import org.nightlabs.jfire.prop.id.StructBlockID;

/**
 * A WizardPage to define values of PropDataFields for a
 * set of PropDataBlocks.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class CompoundDataBlockWizardPage extends WizardHopPage {

	private PropertySet propSet;
	private Map<StructBlockID, DataBlockGroup> propDataBlockGroups = new HashMap<StructBlockID, DataBlockGroup>();
	private Map<StructBlockID, DataBlockGroupEditor> propDataBlockGroupEditors = new HashMap<StructBlockID, DataBlockGroupEditor>();
	private StructBlockID[] structBlockIDs;
	private int propDataBlockEditorColumnHint = 2;

	XComposite wrapperComp;

	public CompoundDataBlockWizardPage ( 
			String pageName, 
			String title,
			PropertySet prop,
			List<StructBlockID> structBlockIDs
	) {
		this(pageName, title, prop, structBlockIDs.toArray(new StructBlockID[structBlockIDs.size()]));
	}

	/**
	 * Creates a new CompoundDataBlockWizardPage for the 
	 * StructBlock identified by the dataBlockID 
	 */
	public CompoundDataBlockWizardPage (
			String pageName, 
			String title, 
			PropertySet propSet,
			StructBlockID[] structBlockIDs
	) {
		super(pageName);
		if (title != null)
			this.setTitle(title);
		if (propSet == null)
			throw new IllegalArgumentException("Parameter propertySet must not be null"); //$NON-NLS-1$
		this.propSet = propSet;
		this.structBlockIDs = structBlockIDs;
		for (int i = 0; i < structBlockIDs.length; i++) {
			try {
				propDataBlockGroups.put(structBlockIDs[i],propSet.getDataBlockGroup(structBlockIDs[i]));
			} catch (DataNotFoundException e) {
				ExceptionHandlerRegistry.syncHandleException(e);
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Creates the wrapper Composite.
	 * Has to be called when {@link #createControl(Composite)} is
	 * overridden.
	 * 
	 * @param parent
	 */
	protected void createWrapper(Composite parent) {
		wrapperComp = new XComposite(parent, SWT.NONE, XComposite.LayoutMode.TIGHT_WRAPPER);
		setControl(wrapperComp);
	}

	/**
	 * Creates a composite with the AbstractDataBlockEditor according
	 * to the StructBlockID passed to the constructor.
	 */
	protected void createPropDataBlockEditors() {
		propDataBlockGroupEditors.clear();
		for (int i = 0; i < structBlockIDs.length; i++) {
			DataBlockGroup dataBlockGroup = propDataBlockGroups.get(structBlockIDs[i]);
			DataBlockGroupEditor editor = new DataBlockGroupEditor(propSet.getStructure(), dataBlockGroup, wrapperComp); 
			editor.refresh(propSet.getStructure(), dataBlockGroup);
			propDataBlockGroupEditors.put(
					structBlockIDs[i],
					editor
			);
		}
	}

	/**
	 * Returns the propertySet passed in the constructor.
	 * 
	 * @return
	 */
	public PropertySet getPropertySet() {
		return propSet;
	}

	/**
	 * Returns one of the DataBlockGroupEditors created by
	 * {@link #createPropDataBlockEditors()}, thus null
	 * before a call to this method. Null can be returned
	 * as well when a StructBlockID is passed here
	 * that was not in the List the WizardPage was constructed with.
	 * 
	 * @return
	 */
	public DataBlockGroupEditor getDataBlockGroupEditor(StructBlockID structBlockID) {
		return propDataBlockGroupEditors.get(structBlockID);
	}

	/**
	 * Returns the propDataBlockGorup within the given
	 * Property this Page is associated with.
	 * 
	 * @return
	 */
	public DataBlockGroup getDataBlockGroup(StructBlockID structBlockID) {
		return propDataBlockGroups.get(structBlockID);
	}	

	/**
	 * Get the hint for the column count of the
	 * AbstractDataBlockEditor. Default is 2.
	 * @return
	 */
	public int getPropDataBlockEditorColumnHint() {
		return propDataBlockEditorColumnHint;
	}
	/**
	 * Set the hint for the column count of the
	 * AbstractDataBlockEditor. Default is 2.
	 * 
	 * @param propDataBlockEditorColumnHint
	 */
	public void setPropDataBlockEditorColumnHint(
			int propDataBlockEditorColumnHint) {
		this.propDataBlockEditorColumnHint = propDataBlockEditorColumnHint;
	}

	/**
	 * Set all values to the propertySet.
	 */
	public void updatePropertySet() {
		for (DataBlockGroupEditor editor : propDataBlockGroupEditors.values()) {
			editor.updatePropopertySet();
		}
	}
	
	public void refresh(PropertySet propertySet) {
		for (Map.Entry<StructBlockID, DataBlockGroupEditor> entry : propDataBlockGroupEditors.entrySet()) {
			try {
				DataBlockGroup blockGroup = propertySet.getDataBlockGroup(entry.getKey());
				propDataBlockGroups.put(entry.getKey(), blockGroup);
				entry.getValue().refresh(propertySet.getStructure(), blockGroup);
			} catch (DataBlockGroupNotFoundException e) {
				throw new RuntimeException(e);
			}
		}		
	}

	/**
	 * This implementation of createControl 
	 * calls {@link #createWrapper(Composite)} and {@link #createPropDataBlockEditors()}.
	 * Subclasses can override and call this method themselves.
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createPageContents(Composite parent) {
		createWrapper(parent);
		createPropDataBlockEditors();
		return wrapperComp;
	}

	public XComposite getWrapperComp() {
		return wrapperComp;
	}
	
	@Override
	public void onHide() {
		super.onHide();
		updatePropertySet();
	}
	
	@Override
	public void onShow() {		
		super.onShow();
		refresh(propSet);
	}
}
