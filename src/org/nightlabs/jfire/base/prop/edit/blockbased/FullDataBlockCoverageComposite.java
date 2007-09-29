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

package org.nightlabs.jfire.base.prop.edit.blockbased;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jfire.base.prop.edit.PropertySetEditor;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.id.StructBlockID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class FullDataBlockCoverageComposite extends Composite {


	private int numColumns;
	private EditorStructBlockRegistry structBlockRegistry;
	/**
	 */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public FullDataBlockCoverageComposite(
			Composite parent, int style, 
			PropertySet propertySet,
			EditorStructBlockRegistry structBlockRegistry
	) {
		super(parent, style);
		this.numColumns = 1;
		if (!(propertySet.getStructure() instanceof StructLocal))
			throw new IllegalArgumentException("The given propertySet was not exploded by a StructLocal"); //$NON-NLS-1$
		this.structBlockRegistry = structBlockRegistry;
		if (structBlockRegistry == null) {
			this.structBlockRegistry = new EditorStructBlockRegistry(propertySet.getStructLocalLinkClass(), propertySet.getStructLocalScope());
		}
		StructBlockID[] fullCoverageBlockIDs = this.structBlockRegistry.getUnassignedBlockKeyArray();
		createPropEditors();
		List<StructBlockID>[] splitBlockIDs = new List[numColumns];
		for (int i=0; i<numColumns; i++) {
			splitBlockIDs[i] = new ArrayList<StructBlockID>();
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
			XComposite wrapper = new XComposite(this,SWT.BORDER, XComposite.LayoutMode.TIGHT_WRAPPER);				
			BlockBasedEditor propEditor = (BlockBasedEditor)propEditors.get(i);
			propEditor.setPropertySet(propertySet);
//			propEditor.setEditorDomain(editorScope,"#FullDatBlockCoverageComposite"+i);
			propEditor.setEditorPropStructBlockList(splitBlockIDs[i]);
			Control propEditorControl = propEditor.createControl(wrapper,true);
			GridData editorControlGD = new GridData(GridData.FILL_BOTH);
			propEditorControl.setLayoutData(editorControlGD);
		}
	}
	
	private List<PropertySetEditor> propEditors = new LinkedList<PropertySetEditor>();
	
	private void createPropEditors() {
		propEditors.clear();
		for (int i=0; i<numColumns; i++) {
			propEditors.add(new BlockBasedEditor());
		}
	}
	
	/**
	 * Set the values from the editor to the PropertySet it
	 * is associated with.
	 */
	public void updatePropertySet() {
		for (PropertySetEditor editor : propEditors) {			
			editor.updatePropertySet();
		}
	}
	
	/**
	 * Link the Composite to a PropertySet and refresh the Control.
	 * 
	 * @param propertySet The PropertySet to link to.
	 */
	public void refresh(PropertySet propertySet) {
		for (PropertySetEditor editor : propEditors) {
			editor.setPropertySet(propertySet, true);
		}
	}
	
	
}
