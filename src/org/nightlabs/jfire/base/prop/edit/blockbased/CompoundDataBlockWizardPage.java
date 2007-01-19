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
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.Property;
import org.nightlabs.jfire.prop.exception.DataNotFoundException;
import org.nightlabs.jfire.prop.id.StructBlockID;

/**
 * A WizardPage to define values of PropDataFields for a
 * set of PropDataBlocks.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class CompoundDataBlockWizardPage extends WizardHopPage {

	private Property prop;
	private Map propDataBlockEditors = new HashMap();
	private Map propDataBlocks = new HashMap();
	private StructBlockID[] structBlockIDs;
	private int propDataBlockEditorColumnHint = 2;
	private IStruct struct;
		
	
	XComposite wrapperComp;
	
	private static StructBlockID[] getArrayFromList(List structBlockIDs) {
		StructBlockID[] blockIDs = new StructBlockID[structBlockIDs.size()];
		int i = 0;
		for (Iterator iter = structBlockIDs.iterator(); iter.hasNext();) {
			StructBlockID structBlockID = (StructBlockID) iter.next();
			blockIDs[i] = structBlockID;
			i++;
		}
		return blockIDs;
	}
	
	public CompoundDataBlockWizardPage ( 
		String pageName, 
		String title,
		Property prop,
		IStruct struct,
		List structBlockIDs
	) {
		this(pageName, title, struct, prop, getArrayFromList(structBlockIDs));
	}

	/**
	 * Creates a new CompoundDataBlockWizardPage for the 
	 * StructBlock identified by the dataBlockID 
	 */
	public CompoundDataBlockWizardPage (
		String pageName, 
		String title, 
		IStruct struct,
		Property prop,
		StructBlockID[] structBlockIDs
	) {
		super(pageName);
		this.setTitle(title);
		if (prop == null)
			throw new IllegalArgumentException("Parameter prop must not be null");
		this.struct = struct;
		this.prop = prop;
		this.structBlockIDs = structBlockIDs;
		for (int i = 0; i < structBlockIDs.length; i++) {
			try {
				propDataBlocks.put(structBlockIDs[i],prop.getDataBlockGroup(structBlockIDs[i]).getDataBlock(0));
			} catch (DataNotFoundException e) {
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
		wrapperComp = new XComposite(parent, SWT.NONE, XComposite.LayoutMode.TIGHT_WRAPPER);
		setControl(wrapperComp);
	}
	
	/**
	 * Creates a composite with the DataBlockEditor according
	 * to the StructBlockID passed to the constructor.
	 */
	protected void createPropDataBlockEditors() {
		propDataBlockEditors.clear();
		for (int i = 0; i < structBlockIDs.length; i++) {
			DataBlock dataBlock = (DataBlock)propDataBlocks.get(structBlockIDs[i]);
			try {
				DataBlockEditor editor = 
					DataBlockEditorFactoryRegistry.sharedInstance().getPropDataBlockEditor(
								struct,
								dataBlock,
								wrapperComp,
								SWT.NONE,
								getPropDataBlockEditorColumnHint()
							);
				
				editor.refresh(struct, dataBlock);
				propDataBlockEditors.put(
					structBlockIDs[i],
					editor
				);
			} catch (EPProcessorException e) {
				ExceptionHandlerRegistry.asyncHandleException(e);
			}			
		}
	}
	
	/**
	 * Returns the prop passed in the constructor.
	 * 
	 * @return
	 */
	public Property getProp() {
		return prop;
	}
	
	/**
	 * Retruns one of the PropDataBlockEditors created by
	 * {@link #createPropDataBlockEditors()}, thus null
	 * before a call to this method. Null can be returned
	 * as well when a StructBlockID is passed here
	 * that was not in the List the WizardPage was constructed with.
	 * 
	 * @return
	 */
	public DataBlockEditor getPropDataBlockEditor(StructBlockID structBlockID) {
		return (DataBlockEditor)propDataBlockEditors.get(structBlockID);
	}
	
	/**
	 * Returns the propDataBlock within the given
	 * Property this Page is associated with.
	 * 
	 * @return
	 */
	public DataBlock getPropDataBlock(StructBlockID structBlockID) {
		return (DataBlock)propDataBlocks.get(structBlockID);
	}	
	
	/**
	 * Get the hint for the column count of the
	 * DataBlockEditor. Default is 2.
	 * @return
	 */
	public int getPropDataBlockEditorColumnHint() {
		return propDataBlockEditorColumnHint;
	}
	/**
	 * Set the hint for the column count of the
	 * DataBlockEditor. Default is 2.
	 * 
	 * @param propDataBlockEditorColumnHint
	 */
	public void setPropDataBlockEditorColumnHint(
			int propDataBlockEditorColumnHint) {
		this.propDataBlockEditorColumnHint = propDataBlockEditorColumnHint;
	}
	
  /**
   * Set all values to the prop.
   */
  public void updateProp() {
  	for (Iterator iter = propDataBlockEditors.values().iterator(); iter.hasNext();) {
			DataBlockEditor editor = (DataBlockEditor) iter.next();
			editor.updateProperty();
		}
  }

	/**
	 * This implementation of createControl 
	 * calls {@link #createWrapper(Composite)} and {@link #createPropDataBlockEditors()}.
	 * Subclasses can override and call this method themselves.
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createPageContents(Composite parent) {
		createWrapper(parent);
		createPropDataBlockEditors();
		return wrapperComp;
	}
	
	public XComposite getWrapperComp() {
		return wrapperComp;
	}
}
