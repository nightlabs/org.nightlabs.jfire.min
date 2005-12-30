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
import org.nightlabs.jfire.base.person.edit.PersonEditor;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.id.PersonStructBlockID;

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
