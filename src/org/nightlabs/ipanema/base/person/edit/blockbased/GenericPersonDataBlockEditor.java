/*
 * Created 	on Nov 29, 2004
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.person.edit.blockbased;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import org.nightlabs.base.composite.XComposite;
import org.nightlabs.ipanema.base.person.edit.PersonDataFieldEditor;
import org.nightlabs.ipanema.base.person.edit.PersonDataFieldEditorFactoryRegistry;
import org.nightlabs.ipanema.base.person.edit.PersonDataFieldEditorNotFoundException;
import org.nightlabs.ipanema.base.person.preferences.PersonStructOrderConfigModule;
import org.nightlabs.ipanema.person.AbstractPersonDataField;
import org.nightlabs.ipanema.person.PersonDataBlock;

/**
 * A Composite presenting all fields a person has within a PersonDataBlock to
 * the user for editing.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class GenericPersonDataBlockEditor extends PersonDataBlockEditor {
	
	private static Logger LOGGER = Logger.getLogger(GenericPersonDataBlockEditor.class);

	/**
	 * Assumes to have a parent with GridLayout.
	 * Adds its controls to the parent.
	 * 
	 * @param parent Should be a ExpandablePersonDataBlockGroupEditor
	 * @param style SWT-style for the container-Composite
	 * @param columnHint A hint for the column count the Editor should use
	 */	
	public GenericPersonDataBlockEditor(
		PersonDataBlock dataBlock,
		Composite parent, 
		int style,
		int columnHint
	) {
		super(dataBlock, parent, style);
		
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
		for (Iterator it = getOrderedPersonDataFieldsIterator(); it.hasNext(); ) {
			AbstractPersonDataField dataField = (AbstractPersonDataField)it.next();
			if (!hasFieldEditorFor(dataField)) {
				PersonDataFieldEditor fieldEditor;
				try {
					fieldEditor = PersonDataFieldEditorFactoryRegistry.sharedInstance().getNewEditorInstance(dataField,ExpandableBlocksPersonEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE);
				} catch (PersonDataFieldEditorNotFoundException e) {
					// could not find editor for class log the error
					LOGGER.error("Editor not found for one field, continuing",e);
					continue;
				}
				addFieldEditor(dataField, fieldEditor,true);
				// have an editor, store it
//				fieldEditors.put(dataFieldKey,fieldEditor);
				// wrap the editor in a Composite to make it easier to layout
				XComposite wrapperComp = new XComposite(this, SWT.PUSH, XComposite.LAYOUT_MODE_TIGHT_WRAPPER);
				((GridLayout)wrapperComp.getLayout()).verticalSpacing = 5;
				// add the field editor
				fieldEditor.createControl(wrapperComp);
//				fieldEditor.addDataFieldEditorChangedListener(this);
			}
			else {
				PersonDataFieldEditor fieldEditor = getFieldEditor(dataField);
				fieldEditor.setData(dataField);
				fieldEditor.refresh();
			}
		}
	}
	
	public void refresh(PersonDataBlock dataBlock) {
		if (dataBlock == null) 
			throw new IllegalStateException("Parameter dataBlock must not be null");
		this.dataBlock = dataBlock;
		createFieldEditors();
	}
	
	public Map getStructFieldDisplayOrder() {
		return PersonStructOrderConfigModule.sharedInstance().structFieldDisplayOrder();
	}
	
	public Iterator getOrderedPersonDataFieldsIterator() {
		List result = new LinkedList();
		Map structFieldOrder = getStructFieldDisplayOrder();
		for (Iterator it = dataBlock.getPersonDataFields().iterator(); it.hasNext(); ) {
			AbstractPersonDataField dataField = (AbstractPersonDataField)it.next();
			if (structFieldOrder.containsKey(dataField.getStructFieldPK())) {
				Integer index = (Integer)structFieldOrder.get(dataField.getStructFieldPK());
				dataField.setPriority(index.intValue());
			}
			result.add(dataField);
		}
		Collections.sort(result);
		return result.iterator();
	}
	
}
