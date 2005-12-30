/*
 * Created 	on Nov 29, 2004
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.person.edit.blockbased;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.extensionpoint.EPProcessorException;
import org.nightlabs.ipanema.base.person.edit.PersonDataFieldEditor;
import org.nightlabs.ipanema.person.PersonDataBlockGroup;
import org.nightlabs.ipanema.person.PersonDataBlockNotFoundException;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonDataBlockGroupEditor 
extends XComposite 
implements PersonDataBlockEditorChangedListener
{
	
	private static final Logger LOGGER = Logger.getLogger(PersonDataBlockGroupEditor.class);
	
	private PersonDataBlockGroup blockGroup;
	
	/**
	 * @param parent
	 * @param style
	 */
	public PersonDataBlockGroupEditor(
			PersonDataBlockGroup blockGroup,
			Composite parent 
	) {
		super(parent, SWT.NONE);		
		this.blockGroup = blockGroup;
		
		wrapperComposite = new XComposite(this, SWT.NONE, XComposite.LAYOUT_MODE_TIGHT_WRAPPER);		
		
		createDataBlockEditors(wrapperComposite);
	}
	
	XComposite wrapperComposite;
	
	private List personDataBlockEditors = new LinkedList();
	
	public void refresh(PersonDataBlockGroup blockGroup) {
		this.blockGroup = blockGroup;
		createDataBlockEditors(wrapperComposite);
		for (int i=0; i<personDataBlockEditors.size(); i++){
			PersonDataBlockEditor dataBlockEditor = (PersonDataBlockEditor)personDataBlockEditors.get(i);
			try {
				dataBlockEditor.refresh(blockGroup.getPersonDataBlock(i));
			} catch (PersonDataBlockNotFoundException e) {
				IllegalStateException ill = new IllegalStateException("No no datablock found on pos "+i);
				ill.initCause(e);
				throw ill;
			}
		}
	}
	
	
	protected void createDataBlockEditors(Composite wrapperComp) {
		if (personDataBlockEditors.size() != blockGroup.getPersonDataBlocks().size()) {
			int i = 0;;
			int j = 0;
			for (i=0; i<blockGroup.getPersonDataBlocks().size(); i++){
				if (personDataBlockEditors.size() <= i) {
					try {
						PersonDataBlockEditor blockEditor = PersonDataBlockEditorFactoryRegistry.getSharedInstace().getPersonDataBlockEditor( 
//							new PersonDataBlockEditor(
								blockGroup.getPersonDataBlock(0),
								wrapperComp,
								SWT.NONE,
								2
						);
						blockEditor.addPersonDataBlockEditorChangedListener(this);
						personDataBlockEditors.add(blockEditor);
					} catch (PersonDataBlockNotFoundException e) {
						LOGGER.error("Could not find PersonDataBlock (idx = 0) for "+blockGroup.getStructBlockKey());
					} catch (EPProcessorException e1) {
						LOGGER.error("Caught EPProcessorException when trying to findnot find PersonDataBlock (idx = 0) for "+blockGroup.getStructBlockKey());
					}
				}
				j = i;
			}				
			for (int k=personDataBlockEditors.size()-1; k>j; k--) {
				PersonDataBlockEditor dataBlockEditor = (PersonDataBlockEditor)personDataBlockEditors.get(k);
				dataBlockEditor.dispose();
				personDataBlockEditors.remove(k);
			}
		}
		
	}
	
	
	private ScrolledForm owner = null;
	
	public void setOwner(ScrolledForm owner) {
		this.owner = owner;
	}
	
	private Collection changeListener = new LinkedList();	
	public synchronized void addPersonDataBlockEditorChangedListener(PersonDataBlockEditorChangedListener listener) {
		changeListener.add(listener);
	}
	public synchronized void removePersonDataBlockEditorChangedListener(PersonDataBlockEditorChangedListener listener) {
		changeListener.add(listener);
	}
	protected synchronized void notifyChangeListeners(PersonDataBlockEditor dataBlockEditor, PersonDataFieldEditor dataFieldEditor) {
		for (Iterator it = changeListener.iterator(); it.hasNext(); ) {
			PersonDataBlockEditorChangedListener listener = (PersonDataBlockEditorChangedListener)it.next();
			listener.personDataBlockEditorChanged(dataBlockEditor,dataFieldEditor);
		}
	}
	/**
	 * @see org.nightlabs.ipanema.base.person.edit.blockbased.PersonDataBlockEditorChangedListener#personDataBlockEditorChanged(org.nightlabs.ipanema.base.admin.widgets.person.edit.PersonDataBlockEditor, org.nightlabs.ipanema.base.admin.widgets.person.edit.AbstractPersonDataFieldEditor)
	 */
	public void personDataBlockEditorChanged(PersonDataBlockEditor dataBlockEditor, PersonDataFieldEditor dataFieldEditor) {
		notifyChangeListeners(dataBlockEditor,dataFieldEditor);
	}
	
	public void updatePerson() {
		for (Iterator it = personDataBlockEditors.iterator(); it.hasNext(); ) {
			PersonDataBlockEditor blockEditor = (PersonDataBlockEditor)it.next();
			blockEditor.updatePerson();
		}
	}
	
}
