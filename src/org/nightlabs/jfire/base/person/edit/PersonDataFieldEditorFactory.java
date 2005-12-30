/*
 * Created 	on Mar 20, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.person.edit;

import org.nightlabs.jfire.person.AbstractPersonDataField;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public interface PersonDataFieldEditorFactory {
	/**
	 * Should return a subclass of {@link AbstractPersonDataField}
	 * @return
	 */
	public Class getTargetPersonDataFieldType();

	/**
	 * Should return one static final type from {@link PersonDataFieldEditorFactoryRegistry}.
	 * @return
	 */
	public String getEditorType();

	/**
	 * Should return a new Instace of the editor for the supplied data.
	 * @param data
	 * @return
	 */
	public PersonDataFieldEditor createPersonDataFieldEditor(AbstractPersonDataField data, boolean setData);

	
}
