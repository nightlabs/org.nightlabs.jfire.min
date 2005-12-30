/*
 * Created 	on Jan 5, 2005
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.person.edit.blockbased;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.nightlabs.base.composite.LabeledText;
import org.nightlabs.base.exceptionhandler.ExceptionHandlerRegistry;
import org.nightlabs.ipanema.base.JFireBasePlugin;
import org.nightlabs.ipanema.base.person.PersonStructProvider;
import org.nightlabs.ipanema.base.person.edit.PersonDataFieldEditor;
import org.nightlabs.ipanema.person.Person;
import org.nightlabs.ipanema.person.PersonStruct;
import org.nightlabs.ipanema.person.TextPersonDataField;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonPersonalDataWizardPage extends PersonDataBlockWizardPage implements PersonDataBlockEditorChangedListener{

	private LabeledText displayName;
	private Button autoCreateDisplayName;
	/**
	 * @param pageName
	 */
	public PersonPersonalDataWizardPage(String pageName, String title, Person person) {
		super(pageName,title,person,PersonStruct.PERSONALDATA);
	}

	/**
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createPageContents(Composite parent) {
		createWrapper(parent);
		
		displayName = new LabeledText(wrapperComp, JFireBasePlugin.getResourceString("person.edit.wizard.pages.PersonalDataPage.labels.displayName"));
		GridData displayNameGD = new GridData();
//		displayNameGD.grabExcessHorizontalSpace = true;
		displayNameGD.horizontalAlignment = GridData.FILL;
//		displayNameGD.widthHint = 200;
		displayNameGD.grabExcessHorizontalSpace = true;
		displayName.setLayoutData(displayNameGD);
		
		autoCreateDisplayName = new Button(wrapperComp, SWT.CHECK);
		GridData autoCreateDisplayNameGD = new GridData();
//		autoCreateDisplayNameGD.grabExcessHorizontalSpace = true;
//		autoCreateDisplayNameGD.horizontalAlignment = GridData.FILL;
		autoCreateDisplayName.setLayoutData(autoCreateDisplayNameGD);
		autoCreateDisplayName.setText(JFireBasePlugin.getResourceString("person.edit.wizard.pages.PersonalDataPage.labels.autoCreateDisplayName"));
		autoCreateDisplayName.addSelectionListener(
			new SelectionListener() {

				public void widgetSelected(SelectionEvent arg0) {
					displayName.getTextControl().setEnabled(!autoCreateDisplayName.getSelection());
				}
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
			}
		);
		autoCreateDisplayName.setSelection(true);
		displayName.getTextControl().setEnabled(false);
		
		Composite dummy = new Composite(wrapperComp,SWT.NONE);
		GridData dummyGD = new GridData();
		dummyGD.heightHint = 10;
		dummy.setLayoutData(dummyGD);
		
		setPersonDataBlockEditorColumnHint(1);
		createPersonDataBlockEditors();
		getPersonDataBlockEditor().addPersonDataBlockEditorChangedListener(this);
		pageChanged();
		return wrapperComp;
	}

	protected void pageChanged() {
		try {
			updatePerson();
			TextPersonDataField name = (TextPersonDataField)getPersonDataBlock().getPersonDataField(PersonStruct.PERSONALDATA_NAME);
			TextPersonDataField firstName = (TextPersonDataField)getPersonDataBlock().getPersonDataField(PersonStruct.PERSONALDATA_FIRSTNAME);
			displayName.getTextControl().setText(getPerson().getPersonDisplayName());

			if (firstName.isEmpty())
				updateStatus(JFireBasePlugin.getResourceString("person.edit.wizard.pages.PersonalDataPage.errormessage.specifyFirstName"));
			else if (name.isEmpty())
				updateStatus(JFireBasePlugin.getResourceString("person.edit.wizard.pages.PersonalDataPage.errormessage.specifyName"));
			else
				updateStatus(null);
		} catch (Throwable t) {
			ExceptionHandlerRegistry.syncHandleException(t);
		}
	}
	/**
	 * @see org.nightlabs.ipanema.base.person.edit.blockbased.PersonDataBlockEditorChangedListener#personDataBlockEditorChanged(org.nightlabs.ipanema.base.person.edit.PersonDataBlockEditor, org.nightlabs.ipanema.base.person.edit.AbstractPersonDataFieldEditor)
	 */
	public void personDataBlockEditorChanged(PersonDataBlockEditor dataBlockEditor, PersonDataFieldEditor dataFieldEditor) {
		pageChanged();
	}
	
	
	/**
	 * Overrides and additionally sets displayName and autoCreateDisplayName.
	 * 
	 * @see org.nightlabs.ipanema.base.person.edit.blockbased.PersonCompoundDataBlockWizardPage#updatePerson()
	 */
	public void updatePerson() {
		super.updatePerson();
		getPerson().setAutoGenerateDisplayName(autoCreateDisplayName.getSelection());
		getPerson().setPersonDisplayName(displayName.getTextControl().getText(), PersonStructProvider.getPersonStructure());
	}
	
	
}
