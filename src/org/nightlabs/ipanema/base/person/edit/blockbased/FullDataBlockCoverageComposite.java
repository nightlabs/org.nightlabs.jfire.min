/*
 * Created 	on Jan 25, 2005
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.person.edit.blockbased;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.nightlabs.base.composite.XComposite;
import org.nightlabs.ipanema.base.person.edit.PersonEditor;
import org.nightlabs.ipanema.person.Person;
import org.nightlabs.ipanema.person.id.PersonStructBlockID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class FullDataBlockCoverageComposite extends Composite {


	private int numColumns;
	/**
	 */
	public FullDataBlockCoverageComposite(Composite parent, int style, String editorScope, Person person) {
		super(parent, style);
		this.numColumns = 1;
		PersonStructBlockID[] fullCoverageBlockIDs = PersonEditorStructBlockRegistry.sharedInstance().computeRemainingBlockKeys(editorScope);
		createPersonEditors();
		List[] splitBlockIDs = new List[numColumns];
		for (int i=0; i<numColumns; i++) {
			splitBlockIDs[i] = new ArrayList();
		}
		for (int i=0; i<fullCoverageBlockIDs.length; i++){
			splitBlockIDs[i % numColumns].add(fullCoverageBlockIDs[i]);
		}
		
		GridLayout thisLayout = new GridLayout();
		thisLayout.numColumns = numColumns;
		thisLayout.makeColumnsEqualWidth = true;
		this.setLayout(thisLayout);
		
		this.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		for (int i=0; i<numColumns; i++) {
			XComposite wrapper = new XComposite(this,SWT.BORDER, XComposite.LAYOUT_MODE_TIGHT_WRAPPER);				
			BlockBasedPersonEditor personEditor = (BlockBasedPersonEditor)personEditors.get(i);
			personEditor.setPerson(person);
			personEditor.setEditorDomain(editorScope,"#FullDatBlockCoverageComposite"+i);
			personEditor.setEditorPersonStructBlockList(splitBlockIDs[i]);
			Control personEditorControl = personEditor.createControl(wrapper,true);
			GridData editorControlGD = new GridData(GridData.FILL_BOTH);
			personEditorControl.setLayoutData(editorControlGD);
		}
			
	}
	
	private List personEditors = new LinkedList();
	
	private void createPersonEditors() {
		personEditors.clear();
		for (int i=0; i<numColumns; i++) {
			personEditors.add(new BlockBasedPersonEditor());
		}
	}
	
	public void updatePerson() {
		for (Iterator iter = personEditors.iterator(); iter.hasNext();) {
			PersonEditor editor = (PersonEditor) iter.next();
			editor.updatePerson();
		}
	}
	
	
}
