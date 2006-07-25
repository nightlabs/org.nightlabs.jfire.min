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
