/*
 * Created 	on Jun 17, 2004
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.person.edit.blockbased;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nightlabs.ipanema.base.person.PersonStructProvider;
import org.nightlabs.ipanema.base.person.edit.PersonEditor;
import org.nightlabs.ipanema.base.person.preferences.PersonStructOrderConfigModule;
import org.nightlabs.ipanema.person.Person;
import org.nightlabs.ipanema.person.PersonDataBlockGroup;
import org.nightlabs.ipanema.person.PersonStruct;
import org.nightlabs.ipanema.person.id.PersonStructBlockID;

/**
 * @see org.nightlabs.ipanema.base.person.edit.blockbased.PersonDataBlockEditor
 * @see org.nightlabs.ipanema.base.person.edit.blockbased.PersonEditorStructBlockRegistry
 * @see org.nightlabs.ipanema.base.person.edit.PersonEditor
 *  
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class AbstractBlockBasedPersonEditor implements PersonEditor { // extends ScrolledComposite {
	
	public AbstractBlockBasedPersonEditor() {
		this (null);
	}
	
	public AbstractBlockBasedPersonEditor(Person person) {
		this.person = person;
	}
	
	
	protected Person person;
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
	
	
	/**
	 * Refreshes the UI-Representation of the given Person.
	 * 
	 * @param changeListener
	 */
	public abstract void refreshControl();
	
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
}
