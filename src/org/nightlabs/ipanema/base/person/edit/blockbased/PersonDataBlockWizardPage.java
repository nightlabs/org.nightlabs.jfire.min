/*
 * Created 	on Jan 5, 2005
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.person.edit.blockbased;

import org.nightlabs.ipanema.person.Person;
import org.nightlabs.ipanema.person.PersonDataBlock;
import org.nightlabs.ipanema.person.id.PersonStructBlockID;

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
