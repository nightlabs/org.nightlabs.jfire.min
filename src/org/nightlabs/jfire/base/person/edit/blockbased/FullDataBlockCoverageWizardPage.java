/*
 * Created 	on Mar 31, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.person.edit.blockbased;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.nightlabs.base.wizard.WizardHopPage;
import org.nightlabs.jfire.person.Person;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class FullDataBlockCoverageWizardPage extends WizardHopPage {

	protected String editorScope;
	protected FullDataBlockCoverageComposite fullDataBlockCoverageComposite;
	protected Person person;
	
	/**
	 * @param pageName
	 * @param title
	 */
	public FullDataBlockCoverageWizardPage(String pageName, String title, String editorScope, Person person) {
		super(pageName, title);
		this.editorScope = editorScope;
		this.person = person;
	}

	/**
	 * @see org.nightlabs.base.wizard.DynamicPathWizardPage#createPageContents(org.eclipse.swt.widgets.Composite)
	 */
	public Control createPageContents(Composite parent) {
		fullDataBlockCoverageComposite = new FullDataBlockCoverageComposite(parent, SWT.NONE, editorScope, person);
		return fullDataBlockCoverageComposite;
	}

	public boolean isPageComplete() {
		return super.isPageComplete();
	}
	
	public void updatePerson() {
		if (fullDataBlockCoverageComposite != null)
			fullDataBlockCoverageComposite.updatePerson();
	}	

}
