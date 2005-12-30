/*
 * Created 	on Jan 9, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.person.edit;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.nightlabs.jfire.person.Person;

/**
 * A interface for common methods of all PersonEditors, either
 * block or field based.<br/>
 * Common useage is:<br/>
 * <ul>
 * 	<li>Create a the Editor with parameterless constructor or one specific to the type your using.</li>
 *	<li>Link the editor to a person by using {@link #setPerson(Person)</li>
 *	<li>Create the UI representation by calling {@link #createControl(Composite, PersonDataBlockEditorChangedListener, boolean)}</li>
 *	<li>If not done with {@link #createControl(Composite, PersonDataBlockEditorChangedListener, boolean)} set the field values by
 * 		calling {@link #refreshControl(PersonDataBlockEditorChangedListener)}.
 *	</li>
 *  <li>To update the person with the values of the editor call {@link #updatePeson()}</li>
 * </ul>
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public interface PersonEditor {
	
	/**
	 * Link the editor to a Person.
	 * @param person
	 */
	public void setPerson(Person person);
	/**
	 * Link the editor to a Person and refresh the Control
	 * if refresh is true.
	 * 
	 * @param person
	 * @param refresh
	 */
	public void setPerson(Person person, boolean refresh);
	
	/**
	 * Create the UI representation of the PersonEditor and associate
	 * the passed changeListener with the fields. If refresh is true
	 * refresh the UI representation. This should be done synchronously
	 * on the GUI-Thread to avoid InvalidThreadAccessExceptions.
	 * 
	 * @param parent
	 * @param refresh
	 * @return
	 */
	public Control createControl(Composite parent, boolean refresh);
	
	public void disposeControl();
	
	/**
	 * Refresh the UI representation.
	 * Implementors should refresh on the GUI-Thread to avoid 
	 * InvalidThreadAccessExceptions.
	 * 
	 * @param changeListener
	 */
	public void refreshControl();
	
	/**
	 * Set the values from the editor to the Person it
	 * is associated with.
	 *
	 */
	public void updatePerson();
}
