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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.nightlabs.jfire.base.person.PersonStructProvider;
import org.nightlabs.jfire.base.person.edit.PersonEditor;
import org.nightlabs.jfire.base.person.preferences.PersonStructOrderConfigModule;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonDataBlockGroup;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.person.id.PersonStructBlockID;

/**
 * A PersonEditor based onon PersonStructBlocks/PersonDataBlocks.
 * This will present one expandable Composite with all PersonDataFields
 * for all PersonStructBlock/PersonDataBlock it gets assosiated with.<br/>
 * Control wich blocks a editor displays by associating a list of blocks
 * to a editor-domain.
 * 
 * @see org.nightlabs.jfire.base.person.edit.blockbased.PersonDataBlockEditor
 * @see org.nightlabs.jfire.base.person.edit.blockbased.PersonEditorStructBlockRegistry
 * @see org.nightlabs.jfire.base.person.edit.PersonEditor
 *  
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class ExpandableBlocksPersonEditor implements PersonEditor { // extends ScrolledComposite {
	public static final String EDITORTYPE_BLOCK_BASED_EXPANDABLE = "block-based-expandable";
	
	public ExpandableBlocksPersonEditor() {
		this (null);
	}
	
	public ExpandableBlocksPersonEditor(Person person) {
		this.person = person;
	}

	
	private FormToolkit toolkit = null;
	
	private Person person;
	/**
	 * Sets the current person of thiss editor.
	 * If refresh is true {@link #refreshForm(PersonDataBlockEditorChangedListener)} 
	 * is called.
	 * @param person
	 * @param refresh
	 */
	public void setPerson(Person person, boolean refresh) {
		this.person = person;
		if (refresh)
			refreshControl();
	}
	/**
	 * Will only set the person, no changes to the UI will be made.
	 * @param person
	 */
	public void setPerson(Person person) {
		setPerson(person,false);
	}
	/**
	 * Returns the person.
	 * @return
	 */
	public Person getPerson() {
		return person;
	}

	/**
	 * Returns a cached version of the {@link PersonStruct}.
	 * @return
	 */
	protected PersonStruct getPersonStructure() {
		return PersonStructProvider.getPersonStructure();
	}
	
	
	private ScrolledForm form = null;
	/**
	 * Returns the {@link ScrolledForm}. 
	 * With {@link ScrolledForm#getBody()} you can access the Composite.
	 * @return
	 */
	public ScrolledForm getForm() {
		return form;
	}
	
	/**
	 * Holds the GroupEditors.
	 */
	private Map groupEditors = new HashMap();
	
	
	public Map getGroupEditors() {
		return groupEditors;
	}
	
//	public void refreshControl() {
//		refreshControl(null);
//	}
	
	/**
	 * Refreshes the UI-Representation of the given Person.
	 * 
	 * @param changeListener
	 */
	public void refreshControl() {
		Display.getDefault().asyncExec( 
			new Runnable() {
				public void run() {
					getPersonStructure().explodePerson(person);
					
					// get the ordered dataBlocks
					for (Iterator it = ExpandableBlocksPersonEditor.this.getOrderedDataBlockGroupsIterator(); it.hasNext(); ) {
						PersonDataBlockGroup blockGroup = (PersonDataBlockGroup)it.next();
						if (shouldDisplayStructBlock(blockGroup)) {
							if (!groupEditors.containsKey(blockGroup.getStructBlockKey())) {
								ExpandablePersonDataBlockGroupEditor groupEditor = new ExpandablePersonDataBlockGroupEditor(blockGroup, form.getBody());
								groupEditor.setOwner(form);
								if (ExpandableBlocksPersonEditor.this.changeListener != null) 
									groupEditor.addPersonDataBlockEditorChangedListener(ExpandableBlocksPersonEditor.this.changeListener);
								groupEditors.put(blockGroup.getStructBlockKey(),groupEditor);
							}
							else {			
								ExpandablePersonDataBlockGroupEditor groupEditor = (ExpandablePersonDataBlockGroupEditor)groupEditors.get(blockGroup.getStructBlockKey());								
								groupEditor.refresh(blockGroup);
							}
						} // if (shouldDisplayStructBlock(blockGroup)) {
					}		
					form.reflow(true);
				}
			}
		);
	}
	
	private PersonDataBlockEditorChangedListener changeListener;
	
	public ScrolledForm createForm(Composite parent, PersonDataBlockEditorChangedListener changeListener, boolean refresh) {
		return (ScrolledForm)createControl(parent,changeListener,refresh);
	}
	/**
	 * Creates the Form. 
	 * If refresh is true {@link #refreshForm(PersonDataBlockEditorChangedListener)}
	 * will be called. 
	 * 
	 * @param parent
	 * @param changeListener
	 * @param refresh
	 * @return
	 */
	public Control createControl(Composite parent, PersonDataBlockEditorChangedListener changeListener, boolean refresh) {
		if (form != null)
			return form;
		if (toolkit == null)
			toolkit = new FormToolkit(parent.getDisplay());
		this.changeListener = changeListener;
		
		form = toolkit.createScrolledForm(parent);
		form.setBackground(parent.getBackground());
		form.setForeground(parent.getForeground());
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		form.getBody().setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		form.getBody().setLayoutData(gd);
		
		if (refresh)
			refreshControl();
		return form;
	}
	
	/**
	 * 
	 * @see org.nightlabs.jfire.base.person.edit.PersonEditor#disposeControl()
	 */
	public void disposeControl() {
		if (form != null && !form.isDisposed()) {
			form.dispose();
			form = null;
		}
	}

	/**
	 * Will only create the Form. No person data will be displayed
	 * 
	 * @param parent
	 * @param changeListener
	 * @return
	 */
	public ScrolledForm createForm(Composite parent, PersonDataBlockEditorChangedListener changeListener) {
		return createForm(parent,changeListener,false);
	}
	
	/**
	 * Will create the form. No change listener will be set and 
	 * no person data will be displayed.
	 * 
	 * @param parent
	 * @return
	 */
	public ScrolledForm createForm(Composite parent) {
		return createForm(parent, null);	
	}
	
	public ScrolledForm createForm(Composite parent, boolean refresh) {
		return createForm(parent, null, refresh);	
	}

	public Control createControl(Composite parent, boolean refresh) {
		return createForm(parent, refresh);	
	}

	private String editorScope;
	private String editorName;
	
	/**
	 * Set the scope and the name of the editor.
	 * This can be used by to limit the PersonStructBlocks
	 * a editor shows by registering it in the {@link PersonEditorStructBlockRegistry}
	 * and calling this function with the appropriate values.<br/>
	 * Default will be all PersonStructBlocks.
	 * 
	 * @param editorScope
	 * @param editorName
	 */
	public void setEditorDomain(String editorScope, String editorName) {
		this.editorScope = editorScope;
		this.editorName = editorName;
	}
	
	/**
	 * Sets the editor domain for this editor and additionally
	 * registeres structBlocks to display in {@link PersonE}
	 * @param editorScope
	 * @param editorName
	 * @param personStructBlockKeys
	 */
	public void setEditorDomain(String editorScope, String editorName, PersonStructBlockID[] personStructBlockKeys) {
		setEditorDomain(editorScope,editorName);
		PersonEditorStructBlockRegistry.sharedInstance().addEditorStructBlocks(editorScope,editorName,personStructBlockKeys);
	}
	
	private List domainPersonStructBlocks;
	
	protected boolean shouldDisplayStructBlock(PersonDataBlockGroup blockGroup) {
		// default is all PersonStructBlocks
		if (domainPersonStructBlocks == null)
			return true;
		else
			return domainPersonStructBlocks.contains(PersonStructBlockID.create(blockGroup.getPersonStructBlockOrganisationID(),blockGroup.getPersonStructBlockID()));
	}
	
	protected void buildDomainDataBlockGroups() {
		if (domainPersonStructBlocks == null) {
			if ((editorScope != null ) && (editorName != null)) {
				List structBlockList = PersonEditorStructBlockRegistry.sharedInstance().getEditorStructBlocks(editorScope,editorName);
				if (!structBlockList.isEmpty())
					domainPersonStructBlocks = structBlockList;
			}
		}
	}
	
	/**
	 * Shortcut of setting the list of PersonStructBlocks
	 * this editor should display. 
	 * After this was set to a non null value this editor 
	 * will not care about registrations in {@link PersonEditorStructBlockRegistry}.
	 * 
	 * @param structBlockList
	 */	
	public void setEditorPersonStructBlockList(List structBlockList) {
		if (structBlockList != null) {
			if (structBlockList.size() > 0)
				domainPersonStructBlocks = structBlockList;
			else
				domainPersonStructBlocks = null;
		} else {
			domainPersonStructBlocks = null;
		}
	}
	
	
	protected Iterator getDataBlockGroupsIterator() {
		buildDomainDataBlockGroups();
		return person.getPersonDataBlockGroups().iterator();
	}
	
	public Map getStructBlockDisplayOrder() {
		return PersonStructOrderConfigModule.sharedInstance().structBlockDisplayOrder();
	}
	
	protected Iterator getOrderedDataBlockGroupsIterator() {
		buildDomainDataBlockGroups();
	
		int allStructBlockCount = getPersonStructure().getPersonStructBlocks().size();
		List result = new LinkedList();
		Map structBlockOrder = getStructBlockDisplayOrder();
		
		int maxIndex = 0;
		int unmentionedCount = 0;
		// all datablocks of this person
		for (Iterator it = person.getPersonDataBlockGroups().iterator(); it.hasNext(); ) {
			PersonDataBlockGroup blockGroup = (PersonDataBlockGroup)it.next();
			boolean orderedAdd = false;
			if (structBlockOrder.containsKey(blockGroup.getStructBlockKey())) {
				// block mentioned in structBlockOrder
				Integer index = (Integer)structBlockOrder.get(blockGroup.getStructBlockKey());
				blockGroup.setPriority(index.intValue());
			}
			else {
				blockGroup.setPriority(allStructBlockCount + (unmentionedCount++));
			}
			result.add(blockGroup);
		}
		Collections.sort(result);
		return result.iterator();
	}

	public void updatePerson() {
		for (Iterator it = groupEditors.values().iterator(); it.hasNext(); ) {
			ExpandablePersonDataBlockGroupEditor groupEditor = (ExpandablePersonDataBlockGroupEditor)it.next();
			groupEditor.updatePerson();
		}
	}
	
}
