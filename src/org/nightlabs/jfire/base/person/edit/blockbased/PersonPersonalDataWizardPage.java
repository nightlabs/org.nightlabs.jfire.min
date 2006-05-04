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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.nightlabs.base.composite.LabeledText;
import org.nightlabs.base.exceptionhandler.ExceptionHandlerRegistry;
import org.nightlabs.base.resource.SharedImages;
import org.nightlabs.jfire.base.JFireBasePlugin;
import org.nightlabs.jfire.base.person.PersonStructProvider;
import org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.person.TextPersonDataField;

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
	 * @see org.nightlabs.jfire.base.person.edit.blockbased.PersonDataBlockEditorChangedListener#personDataBlockEditorChanged(org.nightlabs.jfire.base.person.edit.PersonDataBlockEditor, org.nightlabs.jfire.base.person.edit.AbstractPersonDataFieldEditor)
	 */
	public void personDataBlockEditorChanged(PersonDataBlockEditor dataBlockEditor, PersonDataFieldEditor dataFieldEditor) {
		pageChanged();
	}
	
	
	/**
	 * Overrides and additionally sets displayName and autoCreateDisplayName.
	 * 
	 * @see org.nightlabs.jfire.base.person.edit.blockbased.PersonCompoundDataBlockWizardPage#updatePerson()
	 */
	public void updatePerson() {
		super.updatePerson();
		getPerson().setAutoGenerateDisplayName(autoCreateDisplayName.getSelection());
		getPerson().setPersonDisplayName(displayName.getTextControl().getText(), PersonStructProvider.getPersonStructure());
	}
	
	
}
