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

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditorFactoryRegistry;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditorNotFoundException;
import org.nightlabs.jfire.prop.AbstractDataField;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.IStruct;

/**
 * A Composite presenting all fields a propertySet has within a DataBlock to
 * the user for editing.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class GenericDataBlockEditor extends AbstractDataBlockEditor {
	
	private static Logger LOGGER = Logger.getLogger(GenericDataBlockEditor.class);

	/**
	 * Assumes to have a parent with GridLayout.
	 * Adds its controls to the parent.
	 * 
	 * @param parent Should be a ExpandableDataBlockGroupEditor
	 * @param style SWT-style for the container-Composite
	 * @param columnHint A hint for the column count the Editor should use
	 */	
	public GenericDataBlockEditor(
		IStruct struct,
		DataBlock dataBlock,
		Composite parent, 
		int style,
		int columnHint
	) {
		super(struct, dataBlock, parent, style);
		
		// set grid data for this
		GridData thisData = new GridData(GridData.FILL_HORIZONTAL);
		thisData.grabExcessHorizontalSpace = true;
//		thisData.grabExcessVerticalSpace = true;
		this.setLayoutData(thisData);
		
		GridLayout thisLayout = new GridLayout();
		thisLayout.numColumns = columnHint;
		setLayout(thisLayout);		
		createFieldEditors();
	}
	
	
	public void createFieldEditors() {
		for (Iterator<AbstractDataField> it = getOrderedPropDataFieldsIterator(); it.hasNext(); ) {
			AbstractDataField dataField = it.next();
			if (!hasFieldEditorFor(dataField)) {
				DataFieldEditor<AbstractDataField> fieldEditor;
				try {
					fieldEditor = DataFieldEditorFactoryRegistry.sharedInstance().getNewEditorInstance(
							getStruct(), ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE, 
							null, dataField
						);
				} catch (DataFieldEditorNotFoundException e) {
					// could not find editor for class log the error
					LOGGER.error("Editor not found for one field, continuing",e); //$NON-NLS-1$
					continue;
				}
				addFieldEditor(dataField, fieldEditor,true);
				// have an editor, store it
//				fieldEditors.put(dataFieldKey,fieldEditor);
				// wrap the editor in a Composite to make it easier to layout
				XComposite wrapperComp = new XComposite(this, SWT.PUSH, XComposite.LayoutMode.TIGHT_WRAPPER);
				((GridLayout)wrapperComp.getLayout()).verticalSpacing = 5;
				// add the field editor
				fieldEditor.createControl(wrapperComp);
//				fieldEditor.addDataFieldEditorChangedListener(this);
			}
			
			DataFieldEditor fieldEditor = getFieldEditor(dataField);
			if (getStruct() != null)
				fieldEditor.setData(getStruct(), dataField);
			fieldEditor.refresh();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.prop.edit.blockbased.AbstractDataBlockEditor#refresh(org.nightlabs.jfire.prop.IStruct, org.nightlabs.jfire.prop.DataBlock)
	 */
	@Override
	public void refresh(IStruct struct, DataBlock dataBlock) {
		if (dataBlock == null) 
			throw new IllegalStateException("Parameter dataBlock must not be null");		 //$NON-NLS-1$
		this.dataBlock = dataBlock;
		setStruct(struct);
		createFieldEditors();
	}
	
}
