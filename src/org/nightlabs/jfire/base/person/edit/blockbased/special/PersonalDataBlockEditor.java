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

package org.nightlabs.jfire.base.person.edit.blockbased.special;

import org.apache.log4j.Logger;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditorFactoryRegistry;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditorNotFoundException;
import org.nightlabs.jfire.base.prop.edit.blockbased.DataBlockEditor;
import org.nightlabs.jfire.base.prop.edit.blockbased.DataBlockEditorFactory;
import org.nightlabs.jfire.base.prop.edit.blockbased.ExpandableBlocksEditor;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.AbstractDataField;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.exception.DataFieldNotFoundException;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class PersonalDataBlockEditor extends DataBlockEditor {

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(PersonalDataBlockEditor.class);
	
	public PersonalDataBlockEditor(IStruct struct, DataBlock dataBlock, Composite parent, int style) {		
		super(struct, dataBlock, parent, style);
		try {
			setLayoutData(new GridData(GridData.FILL_BOTH));
			GridLayout thisLayout = new GridLayout();
			thisLayout.horizontalSpacing = 2;
			thisLayout.numColumns = 3;
			thisLayout.verticalSpacing = 2;
			thisLayout.makeColumnsEqualWidth = true;
			this.setLayout(thisLayout);

			createFieldEditors();
			
			this.layout();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void createFieldEditors() {
		addDataFieldEditor(PersonStruct.PERSONALDATA_NAME,3);
		addDataFieldEditor(PersonStruct.PERSONALDATA_FIRSTNAME,3);
		addDataFieldEditor(PersonStruct.PERSONALDATA_COMPANY,3);
		addDataFieldEditor(PersonStruct.PERSONALDATA_SALUTATION,1);
		addDataFieldEditor(PersonStruct.PERSONALDATA_TITLE,1);
		addDataFieldEditor(PersonStruct.PERSONALDATA_DATEOFBIRTH,1);
		addDataFieldEditor(PersonStruct.PERSONALDATA_PHOTO,3);
	}

	private void addDataFieldEditor(StructFieldID fieldID, int horizontalSpan) 
	{
		AbstractDataField field = null;
		try {
			field = dataBlock.getDataField(fieldID);
		} catch (DataFieldNotFoundException e) {
			logger.error("addDataFieldEditor(StructFieldID fieldID) DataField not found for fieldID continuing: "+fieldID.toString(),e);
		}
		DataFieldEditor editor = null;
		if (!hasFieldEditorFor(field)) { 
			try {
				editor = DataFieldEditorFactoryRegistry.sharedInstance().getNewEditorInstance(
						getStruct(), ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE,
						"", // TODO: Context ?!?
						field					
				);
			} catch (DataFieldEditorNotFoundException e1) {
				logger.error("addPersonalDataFieldEditor(PersonStructFieldID fieldID) PersonDataFieldEditor not found for fieldID continuing: "+fieldID.toString(),e1);
			}
			Control editorControl = editor.createControl(this);
			GridData editorLData = new GridData();
			editorLData.horizontalSpan = horizontalSpan;
			editorLData.grabExcessHorizontalSpace = true;
			editorLData.horizontalAlignment = GridData.FILL;
			editorControl.setLayoutData(editorLData);
			addFieldEditor(field, editor);
		}
		else {
			editor = getFieldEditor(field);
		}
		editor.setData(getStruct(), field);
		editor.refresh();
	}

	@Override
	public void refresh(IStruct struct, DataBlock block) {
		this.dataBlock = block;
		createFieldEditors();
	}


	public static class Factory implements DataBlockEditorFactory {  
		/**
		 * @see org.nightlabs.jfire.base.person.edit.blockbased.PersonDataBlockEditorFactory#getProviderStructBlockID()
		 */
		public StructBlockID getProviderStructBlockID() {
			return PersonStruct.PERSONALDATA;
		}
		
		/**
		 * @see org.nightlabs.jfire.base.person.edit.blockbased.PersonDataBlockEditorFactory#createPersonDataBlockEditor(org.nightlabs.jfire.base.person.PersonDataBlock, org.eclipse.swt.widgets.Composite, int)
		 */
		public DataBlockEditor createPropDataBlockEditor(IStruct struct, DataBlock dataBlock, Composite parent, int style) {
			return new PersonalDataBlockEditor(struct, dataBlock, parent, style);
		}
	}


}
