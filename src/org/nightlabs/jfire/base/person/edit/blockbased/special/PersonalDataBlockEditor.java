/*
 * Created 	on Jan 21, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.person.edit.blockbased.special;

import org.apache.log4j.Logger;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor;
import org.nightlabs.jfire.base.person.edit.PersonDataFieldEditorFactoryRegistry;
import org.nightlabs.jfire.base.person.edit.PersonDataFieldEditorNotFoundException;
import org.nightlabs.jfire.base.person.edit.blockbased.ExpandableBlocksPersonEditor;
import org.nightlabs.jfire.base.person.edit.blockbased.PersonDataBlockEditor;
import org.nightlabs.jfire.base.person.edit.blockbased.PersonDataBlockEditorFactory;
import org.nightlabs.jfire.person.AbstractPersonDataField;
import org.nightlabs.jfire.person.PersonDataBlock;
import org.nightlabs.jfire.person.PersonDataFieldNotFoundException;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.person.id.PersonStructBlockID;
import org.nightlabs.jfire.person.id.PersonStructFieldID;

/**
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class PersonalDataBlockEditor extends PersonDataBlockEditor {

	public static final Logger LOGGER = Logger.getLogger(PersonalDataBlockEditor.class);
	
	public PersonalDataBlockEditor(PersonDataBlock dataBlock, Composite parent, int style) {		
		super(dataBlock, parent, style);
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
		addPersonalDataFieldEditor(PersonStruct.PERSONALDATA_NAME,3);
		addPersonalDataFieldEditor(PersonStruct.PERSONALDATA_FIRSTNAME,3);
		addPersonalDataFieldEditor(PersonStruct.PERSONALDATA_COMPANY,3);
		addPersonalDataFieldEditor(PersonStruct.PERSONALDATA_SALUTATION,1);
		addPersonalDataFieldEditor(PersonStruct.PERSONALDATA_TITLE,1);
		addPersonalDataFieldEditor(PersonStruct.PERSONALDATA_DATEOFBIRTH,1);
	}

	private void addPersonalDataFieldEditor(PersonStructFieldID fieldID, int horizontalSpan) 
	{
	 
		AbstractPersonDataField field = null;
		try {
			field = dataBlock.getPersonDataField(fieldID);
		} catch (PersonDataFieldNotFoundException e) {
			LOGGER.error("addPersonalDataFieldEditor(PersonStructFieldID fieldID) PersonDataField not found for fieldID continuing: "+fieldID.toString(),e);
		}
		PersonDataFieldEditor editor = null;
		if (!hasFieldEditorFor(field)) { 
			try {
				editor = PersonDataFieldEditorFactoryRegistry.sharedInstance().getNewEditorInstance(
					field,
					ExpandableBlocksPersonEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE
				);
			} catch (PersonDataFieldEditorNotFoundException e1) {
				LOGGER.error("addPersonalDataFieldEditor(PersonStructFieldID fieldID) PersonDataFieldEditor not found for fieldID continuing: "+fieldID.toString(),e1);
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
		editor.setData(field);
		editor.refresh();
	}

	/**
	 * @see org.nightlabs.jfire.base.person.edit.blockbased.PersonDataBlockEditor#refresh(org.nightlabs.jfire.base.person.PersonDataBlock)
	 */
	public void refresh(PersonDataBlock block) {
		this.dataBlock = block;
		createFieldEditors();
	}


	public static class Factory implements PersonDataBlockEditorFactory {  
		/**
		 * @see org.nightlabs.jfire.base.person.edit.blockbased.PersonDataBlockEditorFactory#getProviderStructBlockID()
		 */
		public PersonStructBlockID getProviderStructBlockID() {
			return PersonStruct.PERSONALDATA;
		}
		
		/**
		 * @see org.nightlabs.jfire.base.person.edit.blockbased.PersonDataBlockEditorFactory#createPersonDataBlockEditor(org.nightlabs.jfire.base.person.PersonDataBlock, org.eclipse.swt.widgets.Composite, int)
		 */
		public PersonDataBlockEditor createPersonDataBlockEditor(PersonDataBlock dataBlock, Composite parent, int style) {
			return new PersonalDataBlockEditor(dataBlock,parent,style);
		}
	}
}
