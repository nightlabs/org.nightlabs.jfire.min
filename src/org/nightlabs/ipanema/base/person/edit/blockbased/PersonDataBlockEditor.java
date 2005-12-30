/*
 * Created 	on Nov 29, 2004
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.person.edit.blockbased;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;

import org.nightlabs.ipanema.base.person.edit.DataFieldEditorChangeListener;
import org.nightlabs.ipanema.base.person.edit.PersonDataFieldEditor;
import org.nightlabs.ipanema.base.person.preferences.PersonStructOrderConfigModule;
import org.nightlabs.ipanema.person.AbstractPersonDataField;
import org.nightlabs.ipanema.person.PersonDataBlock;

/**
 * A Composite presenting all fields a person has within a PersonDataBlock to
 * the user for editing.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class PersonDataBlockEditor extends Composite implements DataFieldEditorChangeListener{
	
	private static Logger LOGGER = Logger.getLogger(PersonDataBlockEditor.class);
	
	protected PersonDataBlockEditor(PersonDataBlock dataBlock, Composite parent, int style) {
		super(parent,style);
		this.dataBlock = dataBlock;
	}
	
	public abstract void refresh(PersonDataBlock block);
	

	protected PersonDataBlock dataBlock;
	/**
	 * key: String AbstractPersonDataField.getPersonRelativePK<br/>
	 * value: PersonDataFieldEditor fieldEditor
	 * 
	 */
	private Map fieldEditors = new HashMap();
	
	
	protected void addFieldEditor(AbstractPersonDataField dataField, PersonDataFieldEditor fieldEditor) {
		addFieldEditor(dataField,fieldEditor, true);
	}
	
	protected void addFieldEditor(AbstractPersonDataField dataField, PersonDataFieldEditor fieldEditor, boolean addListener) {		
		fieldEditors.put(dataField.getPersonRelativePK(),fieldEditor);
		fieldEditor.addDataFieldEditorChangedListener(this);
	}
	
	protected PersonDataFieldEditor getFieldEditor(AbstractPersonDataField dataField) {
		return (PersonDataFieldEditor)fieldEditors.get(dataField.getPersonRelativePK());
	}
	
	protected boolean hasFieldEditorFor(AbstractPersonDataField dataField) {
		return fieldEditors.containsKey(dataField.getPersonRelativePK());
	}
	
	private Collection changeListener = new LinkedList();	
	public synchronized void addPersonDataBlockEditorChangedListener(PersonDataBlockEditorChangedListener listener) {
		changeListener.add(listener);
	}
	public synchronized void removePersonDataBlockEditorChangedListener(PersonDataBlockEditorChangedListener listener) {
		changeListener.add(listener);
	}
	protected synchronized void notifyChangeListeners(PersonDataFieldEditor dataFieldEditor) {
		for (Iterator it = changeListener.iterator(); it.hasNext(); ) {
			PersonDataBlockEditorChangedListener listener = (PersonDataBlockEditorChangedListener)it.next();
			listener.personDataBlockEditorChanged(this,dataFieldEditor);
		}
	}

	public Map getStructFieldDisplayOrder() {
		return PersonStructOrderConfigModule.sharedInstance().structFieldDisplayOrder();
	}
	
	/**
	 * @see org.nightlabs.ipanema.base.person.edit.DataFieldEditorChangeListener#dataFieldEditorChanged(org.nightlabs.ipanema.base.admin.widgets.person.edit.AbstractPersonDataFieldEditor)
	 */
	public void dataFieldEditorChanged(PersonDataFieldEditor editor) {
		notifyChangeListeners(editor);
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
	
	
	
	public void dispose() {
		Object[] editors = fieldEditors.values().toArray();
		fieldEditors.clear();
		for (int i=0; i<editors.length; i++) {
			PersonDataFieldEditor editor = (PersonDataFieldEditor)editors[i];
			editor.removeDataFieldEditorChangedListener(this);
		}
		super.dispose();
	}
	
	/**
	 * Default implementation of updatePerson() iterates through all
	 * PersonDataFieldEditor s added by {@link #addFieldEditor(AbstractPersonDataField, PersonDataFieldEditor)}
	 * and calls their updatePerson method.<br/>
	 * Implementors might override if no registered PersonDataFieldEditors are used.
	 */
	public void updatePerson() {
		for (Iterator it = fieldEditors.values().iterator(); it.hasNext(); ) {
			PersonDataFieldEditor fieldEditor = (PersonDataFieldEditor)it.next();
			fieldEditor.updatePerson();
		}
	}
	
}
