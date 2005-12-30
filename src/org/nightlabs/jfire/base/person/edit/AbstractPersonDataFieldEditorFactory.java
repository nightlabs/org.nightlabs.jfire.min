/*
 * Created 	on Nov 26, 2004
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.person.edit;

import org.nightlabs.jfire.person.AbstractPersonDataField;

/**
 * Abstract base class for all {@link PersonDataFieldEditorFactory}s 
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class AbstractPersonDataFieldEditorFactory implements PersonDataFieldEditorFactory {

	/**
	 * Default constructor does nothing.
	 */
	public AbstractPersonDataFieldEditorFactory() { }
	
	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#getTargetPersonDataType()
	 */
	public abstract Class getTargetPersonDataFieldType();

	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#getEditorType()
	 */
	public abstract String getEditorType();
	
	public abstract Class getPersonDataFieldEditorClass();

	/**
	 * Default implementation instatiates a new instance of getEditorClass.getNewInstance()
	 * invokes setData(data) and returnes the new instance.
	 * 
	 */
	public PersonDataFieldEditor createPersonDataFieldEditor(AbstractPersonDataField data, boolean setData) {
		PersonDataFieldEditor editor;
		try {
			editor = (PersonDataFieldEditor)getPersonDataFieldEditorClass().newInstance();
		} catch (Throwable t) {
			IllegalStateException ill = new IllegalStateException("Error instantiating "+getPersonDataFieldEditorClass().getName());
			ill.initCause(t);
			throw ill;
		}
		if (setData)
			editor.setData(data);
		return editor;
	}
	
}
