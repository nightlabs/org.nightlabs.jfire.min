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

package org.nightlabs.jfire.base.person.edit.blockbased;

import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonDataBlock;
import org.nightlabs.jfire.person.id.PersonStructBlockID;

/**
 * A WizardPage to define values for one PersonDataBlock.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonDataBlockWizardPage extends PersonCompoundDataBlockWizardPage {

	private PersonStructBlockID structBlockID;
		
	
	/**
	 * Creates a new PersonDataBlockWizardPage for the 
	 * StructBlock identified by the dataBlockID 
	 */
	public PersonDataBlockWizardPage(
		String pageName, 
		String title, 
		Person person,
		PersonStructBlockID structBlockID
	) {
		super(pageName,title,person,new PersonStructBlockID[]{structBlockID});
		this.structBlockID = structBlockID;
	}
	
	
	/**
	 * Retruns the PersonDataBlockEditor created by
	 * {@link #createPersonDataBlockEditors()}, thus null
	 * before a call to this method.
	 * 
	 * @return
	 */
	public PersonDataBlockEditor getPersonDataBlockEditor() {
		return super.getPersonDataBlockEditor(structBlockID);
	}
	
	/**
	 * Returns the PersonsStructBlockID this WizardPage is 
	 * associated to.
	 * 
	 * @return
	 */
	public PersonStructBlockID getStructBlockID() {
		return structBlockID;
	}
	
	/**
	 * Returns the personDataBlock within the given
	 * Person this Page is associated with.
	 * 
	 * @return
	 */
	public PersonDataBlock getPersonDataBlock() {
		return super.getPersonDataBlock(structBlockID);
	}	
}
