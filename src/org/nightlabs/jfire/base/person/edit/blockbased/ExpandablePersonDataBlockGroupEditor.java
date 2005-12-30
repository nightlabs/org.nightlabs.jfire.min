/*
 * Created 	on Nov 29, 2004
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.person.edit.blockbased;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import org.nightlabs.jfire.base.person.PersonStructProvider;
import org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor;
import org.nightlabs.jfire.person.PersonDataBlockGroup;
import org.nightlabs.jfire.person.PersonStruct;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class ExpandablePersonDataBlockGroupEditor 
extends ExpandableComposite
implements PersonDataBlockEditorChangedListener
{
	
	private static final Logger LOGGER = Logger.getLogger(ExpandablePersonDataBlockGroupEditor.class);
	
	private PersonDataBlockGroupEditor blockGroupEditor;
	
	/**
	 * @param parent
	 * @param style
	 */
	public ExpandablePersonDataBlockGroupEditor(
			PersonDataBlockGroup blockGroup,
			Composite parent 
	) {
		super(parent, SWT.NONE);
		blockGroupEditor = new PersonDataBlockGroupEditor(blockGroup, this);
		
		GridLayout thisLayout = new GridLayout();
		thisLayout.verticalSpacing = 0;
		thisLayout.horizontalSpacing= 0;
		setLayout(thisLayout);
		
		//		TableWrapData thisLayoutData = new TableWrapData();
		GridData thisLayoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		setLayoutData(thisLayoutData);
		
		setClient(blockGroupEditor);
		
		PersonStruct structure = PersonStructProvider.getPersonStructure();
		if (blockGroup.getPersonStructBlock(structure).getBlockName().getText() == null)
			setText(blockGroup.getPersonStructBlock(structure).getPersonStructBlockID());
		else
			setText(blockGroup.getPersonStructBlock(structure).getBlockName().getText());
		
		addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				if (ExpandablePersonDataBlockGroupEditor.this.owner != null)
					ExpandablePersonDataBlockGroupEditor.this.owner.reflow(true);
			}
		});
		
	}

	public void personDataBlockEditorChanged(PersonDataBlockEditor dataBlockEditor, PersonDataFieldEditor dataFieldEditor) {
		blockGroupEditor.personDataBlockEditorChanged(dataBlockEditor, dataFieldEditor);
	}
	
	public void updatePerson() {
		blockGroupEditor.updatePerson();
	}
	
	public synchronized void addPersonDataBlockEditorChangedListener(PersonDataBlockEditorChangedListener listener) {
		blockGroupEditor.addPersonDataBlockEditorChangedListener(listener);
	}
	public synchronized void removePersonDataBlockEditorChangedListener(PersonDataBlockEditorChangedListener listener) {
		blockGroupEditor.removePersonDataBlockEditorChangedListener(listener);
	}
	
	public void refresh(PersonDataBlockGroup blockGroup) {
	}	
	
	private ScrolledForm owner = null;
	
	public void setOwner(ScrolledForm owner) {
		this.owner = owner;
	}
	
}
