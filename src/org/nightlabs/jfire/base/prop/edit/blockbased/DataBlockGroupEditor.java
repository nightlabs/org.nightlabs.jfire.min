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
import org.nightlabs.jfire.base.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.prop.DataBlockGroup;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.exception.DataBlockNotFoundException;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class DataBlockGroupEditor 
extends XComposite 
implements DataBlockEditorChangedListener
{
	
	private static final Logger LOGGER = Logger.getLogger(DataBlockGroupEditor.class);
	
	private DataBlockGroup blockGroup;
	
	private IStruct struct;
	
	/**
	 * @param parent
	 * @param style
	 */
	public DataBlockGroupEditor(
			IStruct struct,
			DataBlockGroup blockGroup,
			Composite parent 
	) {
		super(parent, SWT.NONE);		
		this.blockGroup = blockGroup;
		
		wrapperComposite = new XComposite(this, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA);		
		
		createDataBlockEditors(struct, wrapperComposite);
	}
	
	XComposite wrapperComposite;
	
	private List propDataBlockEditors = new LinkedList();
	
	public void refresh(IStruct struct, DataBlockGroup blockGroup) {
		this.blockGroup = blockGroup;
		createDataBlockEditors(struct, wrapperComposite);
		for (int i=0; i<propDataBlockEditors.size(); i++){
			DataBlockEditor dataBlockEditor = (DataBlockEditor)propDataBlockEditors.get(i);
			try {
				dataBlockEditor.refresh(struct, blockGroup.getDataBlock(i));
			} catch (DataBlockNotFoundException e) {
				IllegalStateException ill = new IllegalStateException("No no datablock found on pos "+i);
				ill.initCause(e);
				throw ill;
			}
		}
	}
	
	
	protected void createDataBlockEditors(IStruct struct, Composite wrapperComp) {
		if (propDataBlockEditors.size() != blockGroup.getDataBlocks().size()) {
			int i = 0;;
			int j = 0;
			for (i=0; i<blockGroup.getDataBlocks().size(); i++){
				if (propDataBlockEditors.size() <= i) {
					try {
						DataBlockEditor blockEditor = DataBlockEditorFactoryRegistry.sharedInstance().getPropDataBlockEditor(
								struct,
								blockGroup.getDataBlock(0),
								wrapperComp,
								SWT.NONE,
								2
						);
						blockEditor.addPropDataBlockEditorChangedListener(this);
						propDataBlockEditors.add(blockEditor);
					} catch (DataBlockNotFoundException e) {
						LOGGER.error("Could not find DataBlock (idx = 0) for "+blockGroup.getStructBlockKey());
					} catch (EPProcessorException e1) {
						LOGGER.error("Caught EPProcessorException when trying to findnot find DataBlock (idx = 0) for "+blockGroup.getStructBlockKey(), e1);
					}
				}
				j = i;
			}				
			for (int k=propDataBlockEditors.size()-1; k>j; k--) {
				DataBlockEditor dataBlockEditor = (DataBlockEditor)propDataBlockEditors.get(k);
				dataBlockEditor.dispose();
				propDataBlockEditors.remove(k);
			}
		}
		
	}
	
	
	private ScrolledForm owner = null;
	
	public void setOwner(ScrolledForm owner) {
		this.owner = owner;
	}
	
	private Collection changeListener = new LinkedList();	
	public synchronized void addPropDataBlockEditorChangedListener(DataBlockEditorChangedListener listener) {
		changeListener.add(listener);
	}
	public synchronized void removePropDataBlockEditorChangedListener(DataBlockEditorChangedListener listener) {
		changeListener.add(listener);
	}
	protected synchronized void notifyChangeListeners(DataBlockEditor dataBlockEditor, DataFieldEditor dataFieldEditor) {
		for (Iterator it = changeListener.iterator(); it.hasNext(); ) {
			DataBlockEditorChangedListener listener = (DataBlockEditorChangedListener)it.next();
			listener.propDataBlockEditorChanged(dataBlockEditor,dataFieldEditor);
		}
	}
	/**
	 * @see org.nightlabs.jfire.base.prop.edit.blockbased.DataBlockEditorChangedListener#propDataBlockEditorChanged(org.nightlabs.jfire.base.admin.widgets.prop.edit.DataBlockEditor, org.nightlabs.jfire.base.admin.widgets.prop.edit.AbstractPropDataFieldEditor)
	 */
	public void propDataBlockEditorChanged(DataBlockEditor dataBlockEditor, DataFieldEditor dataFieldEditor) {
		notifyChangeListeners(dataBlockEditor,dataFieldEditor);
	}
	
	public void updateProp() {
		for (Iterator it = propDataBlockEditors.iterator(); it.hasNext(); ) {
			DataBlockEditor blockEditor = (DataBlockEditor)it.next();
			blockEditor.updateProperty();
		}
	}
	
	public IStruct getStruct() {
		return struct;
	}
	
}
