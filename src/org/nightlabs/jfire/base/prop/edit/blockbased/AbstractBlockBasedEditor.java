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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nightlabs.jfire.base.prop.edit.PropertySetEditor;
import org.nightlabs.jfire.prop.DataBlockGroup;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Abstract base for block based {@link PropertySetEditor}s.
 * It manages (holds) the {@link PropertySet} to edit and the StructBlocks
 * that should be visible when editing the propertySet. 
 * 
 * @see org.nightlabs.jfire.base.prop.edit.blockbased.AbstractDataBlockEditor
 * @see org.nightlabs.jfire.base.prop.edit.blockbased.EditorStructBlockRegistry
 * @see org.nightlabs.jfire.base.prop.edit.PropertySetEditor
 *  
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class AbstractBlockBasedEditor implements PropertySetEditor { // extends ScrolledComposite {
	
	protected PropertySet propertySet;
	protected EditorStructBlockRegistry structBlockRegistry;
		
	public AbstractBlockBasedEditor() {
		this (null, null);
	}
	
	/**
	 * Create a new {@link AbstractBlockBasedEditor} for the given 
	 * propertySet. 
	 * 
	 * @param prop
	 * @param propStruct
	 */
	public AbstractBlockBasedEditor(PropertySet prop, IStruct propStruct) {
		this.propertySet = prop;
		if (propStruct != null) {
			String scope = StructLocal.DEFAULT_SCOPE;
			if (propStruct instanceof StructLocal)
				scope = ((StructLocal)propStruct).getScope();
			structBlockRegistry = new EditorStructBlockRegistry(propStruct.getLinkClass(), scope);
		}
	}
	
	
	
	/**
	 * Sets the current propertySet of this editor.
	 * If refresh is true {@link #refreshForm(DataBlockEditorChangedListener)} 
	 * is called.
	 * @param refresh
	 * @param propertySet
	 */
	public void setPropertySet(PropertySet propSet, boolean refresh) {
		this.propertySet = propSet;
		if (refresh)
			refreshControl();
	}
	
	/**
	 * Will only set the propertySet, no changes to the UI will be made.
	 * @param propertySet
	 */
	public void setPropertySet(PropertySet propSet) {		
		setPropertySet(propSet, false);		
	}
	/**
	 * Returns the propertySet.
	 * @return
	 */
	public PropertySet getPropertySet() {
		return propertySet;
	}

	/**
	 * Returns a version of the {@link Struct}.
	 * @return
	 */
	protected IStruct getPropStructure(ProgressMonitor monitor) {
		if (propertySet.isExploded())
			return propertySet.getStructure();
		monitor.beginTask("Loading propertySet structure", 1);
		IStruct structure = StructLocalDAO.sharedInstance().getStructLocal(
				propertySet.getStructLocalLinkClass(), propertySet.getStructLocalScope(), monitor
		);
		monitor.worked(1);
		return structure;
	}
	
	
	/**
	 * Refreshes the UI-Representation of the given Property.
	 * 
	 * @param changeListener
	 */
	public abstract void refreshControl();
	
	private String editorName;
	
	/**
	 * Sets the editor domain for this editor and additionally
	 * registeres structBlocks to display in {@link PropE}
	 * @param editorScope
	 * @param editorName
	 * @param propStructBlockKeys
	 */
	public void setEditorDomain(String editorName, EditorStructBlockRegistry structBlockRegistry) {
		this.editorName = editorName;
		this.structBlockRegistry = structBlockRegistry;
	}
	
	private List<StructBlockID> domainPropStructBlocks;
	
	protected boolean shouldDisplayStructBlock(DataBlockGroup blockGroup) {
		// default is all PropStructBlocks
		if (domainPropStructBlocks == null)
			return true;
		else
			return domainPropStructBlocks.contains(StructBlockID.create(blockGroup.getStructBlockOrganisationID(),blockGroup.getStructBlockID()));
	}
	
	protected void buildDomainDataBlockGroups() {
		if (domainPropStructBlocks == null) {
			if (editorName != null && structBlockRegistry != null) {
				List structBlockList = structBlockRegistry.getEditorStructBlocks(editorName);
				if (!structBlockList.isEmpty())
					domainPropStructBlocks = structBlockList;
			}
		}
	}
	
	/**
	 * Shortcut to set the list of PropStructBlocks this editor should display. 
	 * After this was set to a non null value this editor 
	 * will not care about registrations in {@link EditorStructBlockRegistry}.
	 * 
	 * @param structBlockList
	 */	
	public void setEditorPropStructBlockList(List<StructBlockID> structBlockIDs) {
		if (structBlockIDs != null && structBlockIDs.size() > 0)
		{
			domainPropStructBlocks = structBlockIDs;
		}
		else
		{
			domainPropStructBlocks = null;
		}
	}
	
	
	protected Iterator getDataBlockGroupsIterator() {
		buildDomainDataBlockGroups();
		return propertySet.getDataBlockGroups().iterator();
	}
	
	public Map<String, Integer> getStructBlockDisplayOrder(IStruct struct) {
		//return AbstractPropStructOrderConfigModule.sharedInstance().structBlockDisplayOrder();
		List<StructBlock> structBlocks = struct.getStructBlocks();
		Map<String, Integer> result = new HashMap<String, Integer>();
		for (int i = 0; i < structBlocks.size(); i++) {
			result.put(structBlocks.get(i).getPrimaryKey(), i);
		}
		return result;
	}
	
	protected Iterator<DataBlockGroup> getOrderedDataBlockGroupsIterator() {
		buildDomainDataBlockGroups();
	
		IStruct struct = propertySet.getStructure();
		if (struct == null)
			throw new IllegalStateException("The PropertySet was not exploded yet");
		int allStructBlockCount = struct.getStructBlocks().size();
		List<DataBlockGroup> result = new LinkedList<DataBlockGroup>();
		Map<String, Integer> structBlockOrder = getStructBlockDisplayOrder(struct);
		
		int unmentionedCount = 0;
		// all datablocks of this propertySet
		for (Iterator<DataBlockGroup> it = propertySet.getDataBlockGroups().iterator(); it.hasNext(); ) {
			DataBlockGroup blockGroup = it.next();
			if (structBlockOrder.containsKey(blockGroup.getStructBlockKey())) {
				// block mentioned in structBlockOrder
				Integer index = structBlockOrder.get(blockGroup.getStructBlockKey());
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
