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
	
	/**
	 * Returns the {@link Person} associated with this editor.
	 * Implementations should perform an update of the person
	 * before returning it.
	 * @see #updatePerson();
	 * @return The {@link Person} associated with this editor.
	 */
	public Person getPerson();
}
