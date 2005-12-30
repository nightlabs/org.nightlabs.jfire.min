/*
 * Created 	on Nov 26, 2004
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.person.edit;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.nightlabs.jfire.person.AbstractPersonDataField;


/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public interface PersonDataFieldEditor {
	
	public void setPersonDataFieldEditorFactory(PersonDataFieldEditorFactory factory);
	public PersonDataFieldEditorFactory getPersonDataFieldEditorFactory();
	
	/**
	 * Here a data field editor should add its
	 * control to a parent composite.<br/>
	 * The Composite returned should be a singelton
	 * and be updated with data changes.
	 * No data-display will be made here. See {@link #setData(AbstractPersonDataField)}.
	 * 
	 * @param parent
	 * @return
	 */	
	public Control createControl(Composite parent);
	/**
	 * Should return the Control created in {@link #createControl(Composite)}.
	 * @return the Control created in {@link #createControl(Composite)}.
	 */
	public Control getControl();
	
	/**
	 * Editors should refresh their data during this method.
	 * The composite should be refreshed as well.
	 * @param data
	 */
	public void setData(AbstractPersonDataField data);
	
	public void refresh();
	
	public void updatePerson();
	
	
	public void addDataFieldEditorChangedListener(DataFieldEditorChangeListener listener);
	public void removeDataFieldEditorChangedListener(DataFieldEditorChangeListener listener);
	public void setChanged(boolean changed);
	public boolean isChanged();
	
}
