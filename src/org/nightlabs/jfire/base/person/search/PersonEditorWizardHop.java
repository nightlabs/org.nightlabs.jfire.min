/**
 * 
 */
package org.nightlabs.jfire.base.person.search;

import org.nightlabs.base.wizard.WizardHop;
import org.nightlabs.jfire.person.Person;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class PersonEditorWizardHop extends WizardHop {

	/**
	 * The personal page
	 */
	private PersonEditorWizardPersonalPage personalPage;
	private PersonEditorWizardOtherPage otherPage;
	
	/**
	 * 
	 */
	public PersonEditorWizardHop() {
	}
	
	
	public void initialise(Person person) {
		if (personalPage == null) {
			personalPage = new PersonEditorWizardPersonalPage(person);
			setEntryPage(personalPage);
		} else
			personalPage.refresh(person);
		
		if (otherPage == null) {
			otherPage = new PersonEditorWizardOtherPage(person);
			addHopPage(otherPage);
		}
		else
			otherPage.refresh(person);
		
	}

	public void updatePerson() {
		
	}
}

