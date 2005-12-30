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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.exceptionhandler.ExceptionHandlerRegistry;
import org.nightlabs.base.extensionpoint.EPProcessorException;
import org.nightlabs.base.wizard.WizardHopPage;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonDataBlock;
import org.nightlabs.jfire.person.PersonDataNotFoundException;
import org.nightlabs.jfire.person.id.PersonStructBlockID;

/**
 * A WizardPage to define values of PersonDataFields for a
 * set of PersonDataBlocks.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonCompoundDataBlockWizardPage extends WizardHopPage {

	private Person person;
	private Map personDataBlockEditors = new HashMap();
	private Map personDataBlocks = new HashMap();
	private PersonStructBlockID[] structBlockIDs;
	private int personDataBlockEditorColumnHint = 2;
		
	
	XComposite wrapperComp;
	
	private static PersonStructBlockID[] getArrayFromList(List structBlockIDs) {
		PersonStructBlockID[] blockIDs = new PersonStructBlockID[structBlockIDs.size()];
		int i = 0;
		for (Iterator iter = structBlockIDs.iterator(); iter.hasNext();) {
			PersonStructBlockID structBlockID = (PersonStructBlockID) iter.next();
			blockIDs[i] = structBlockID;
			i++;
		}
		return blockIDs;
	}
	
	public PersonCompoundDataBlockWizardPage ( 
		String pageName, 
		String title,
		Person person,
		List structBlockIDs
	) {
		this(pageName, title, person, getArrayFromList(structBlockIDs));
	}

	/**
	 * Creates a new PersonCompoundDataBlockWizardPage for the 
	 * StructBlock identified by the dataBlockID 
	 */
	public PersonCompoundDataBlockWizardPage (
		String pageName, 
		String title, 
		Person person,
		PersonStructBlockID[] structBlockIDs
	) {
		super(pageName);
		this.setTitle(title);
		if (person == null)
			throw new IllegalArgumentException("Parameter person must not be null");
		this.person = person;
		this.structBlockIDs = structBlockIDs;
		for (int i = 0; i < structBlockIDs.length; i++) {
			try {
				personDataBlocks.put(structBlockIDs[i],person.getPersonDataBlockGroup(structBlockIDs[i]).getPersonDataBlock(0));
			} catch (PersonDataNotFoundException e) {
				ExceptionHandlerRegistry.syncHandleException(e);
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * Creates the wrapper Composite.
	 * Has to be called when {@link #createControl(Composite)} is
	 * overridden.
	 * 
	 * @param parent
	 */
	protected void createWrapper(Composite parent) {
		wrapperComp = new XComposite(parent, SWT.NONE, XComposite.LAYOUT_MODE_TIGHT_WRAPPER);
		setControl(wrapperComp);
	}
	
	/**
	 * Creates a composite with the PersonDataBlockEditor according
	 * to the PersonStructBlockID passed to the constructor.
	 */
	protected void createPersonDataBlockEditors() {
		personDataBlockEditors.clear();
		for (int i = 0; i < structBlockIDs.length; i++) {
			PersonDataBlock dataBlock = (PersonDataBlock)personDataBlocks.get(structBlockIDs[i]);
			try {
				PersonDataBlockEditor editor = 
					PersonDataBlockEditorFactoryRegistry.getSharedInstace().getPersonDataBlockEditor(
//						new PersonDataBlockEditor(
								dataBlock,
								wrapperComp,
								SWT.NONE,
								getPersonDataBlockEditorColumnHint()
							);
				
				editor.refresh(dataBlock);
				personDataBlockEditors.put(
					structBlockIDs[i],
					editor
				);
			} catch (EPProcessorException e) {
				ExceptionHandlerRegistry.asyncHandleException(e);
			}			
		}
	}
	
	/**
	 * Returns the person passed in the constructor.
	 * 
	 * @return
	 */
	public Person getPerson() {
		return person;
	}
	
	/**
	 * Retruns one of the PersonDataBlockEditors created by
	 * {@link #createPersonDataBlockEditors()}, thus null
	 * before a call to this method. Null can be returned
	 * as well when a PersonStructBlockID is passed here
	 * that was not in the List the WizardPage was constructed with.
	 * 
	 * @return
	 */
	public PersonDataBlockEditor getPersonDataBlockEditor(PersonStructBlockID structBlockID) {
		return (PersonDataBlockEditor)personDataBlockEditors.get(structBlockID);
	}
	
	/**
	 * Returns the personDataBlock within the given
	 * Person this Page is associated with.
	 * 
	 * @return
	 */
	public PersonDataBlock getPersonDataBlock(PersonStructBlockID structBlockID) {
		return (PersonDataBlock)personDataBlocks.get(structBlockID);
	}	
	
	/**
	 * Get the hint for the column count of the
	 * PersonDataBlockEditor. Default is 2.
	 * @return
	 */
	public int getPersonDataBlockEditorColumnHint() {
		return personDataBlockEditorColumnHint;
	}
	/**
	 * Set the hint for the column count of the
	 * PersonDataBlockEditor. Default is 2.
	 * 
	 * @param personDataBlockEditorColumnHint
	 */
	public void setPersonDataBlockEditorColumnHint(
			int personDataBlockEditorColumnHint) {
		this.personDataBlockEditorColumnHint = personDataBlockEditorColumnHint;
	}
	
  /**
   * Set all values to the person.
   */
  public void updatePerson() {
  	for (Iterator iter = personDataBlockEditors.values().iterator(); iter.hasNext();) {
			PersonDataBlockEditor editor = (PersonDataBlockEditor) iter.next();
			editor.updatePerson();
		}
  }

	/**
	 * This implementation of createControl 
	 * calls {@link #createWrapper(Composite)} and {@link #createPersonDataBlockEditors()}.
	 * Subclasses can override and call this method themselves.
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createPageContents(Composite parent) {
		createWrapper(parent);
		createPersonDataBlockEditors();
		return wrapperComp;
	}
}
