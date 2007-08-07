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
import org.nightlabs.jfire.prop.id.StructBlockID;

/**
 * @see org.nightlabs.jfire.base.prop.edit.blockbased.AbstractDataBlockEditor
 * @see org.nightlabs.jfire.base.prop.edit.blockbased.EditorStructBlockRegistry
 * @see org.nightlabs.jfire.base.prop.edit.PropertySetEditor
 *  
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class AbstractBlockBasedEditor implements PropertySetEditor { // extends ScrolledComposite {
	
	public AbstractBlockBasedEditor() {
		this (null, null);
	}
	
	public AbstractBlockBasedEditor(PropertySet prop, IStruct propStruct) {
		this.propStruct = propStruct;
		this.propSet = prop;
		if (propStruct != null) {
			String scope = StructLocal.DEFAULT_SCOPE;
			if (propStruct instanceof StructLocal)
				scope = ((StructLocal)propStruct).getScope();
			structBlockRegistry = new EditorStructBlockRegistry(propStruct.getLinkClass(), scope);
		}
	}
	
	
	protected PropertySet propSet;
	protected IStruct propStruct;
	protected EditorStructBlockRegistry structBlockRegistry;
	
	/**
	 * Sets the current propSet of this editor.
	 * If refresh is true {@link #refreshForm(DataBlockEditorChangedListener)} 
	 * is called.
	 * @param propSet
	 * @param refresh
	 */
	public void setPropertySet(PropertySet propSet, IStruct propStruct, boolean refresh) {
		this.propSet = propSet;
		this.propStruct = propStruct;		
		propStruct.explodeProperty(propSet);
		String scope = StructLocal.DEFAULT_SCOPE;
		if (propStruct instanceof StructLocal)
			scope = ((StructLocal)propStruct).getScope();
		structBlockRegistry = new EditorStructBlockRegistry(propStruct.getLinkClass(), scope);
		if (refresh)
			refreshControl();
	}
	
	/**
	 * Will only set the propSet, no changes to the UI will be made.
	 * @param propSet
	 */
	public void setPropertySet(PropertySet propSet, IStruct propStruct) {		
		setPropertySet(propSet, propStruct, false);		
	}
	/**
	 * Returns the propSet.
	 * @return
	 */
	public PropertySet getPropertySet() {
		return propSet;
	}

	/**
	 * Returns a version of the {@link Struct}.
	 * @return
	 */
	protected IStruct getPropStructure() {
		return propStruct;
	}
	
	
	/**
	 * Refreshes the UI-Representation of the given Property.
	 * 
	 * @param changeListener
	 */
	public abstract void refreshControl();
	
	private String editorScope;
	private String editorName;
	
	/**
	 * Set the scope and the name of the editor.
	 * This can be used by to limit the PropStructBlocks
	 * a editor shows by registering it in the {@link EditorStructBlockRegistry}
	 * and calling this function with the appropriate values.<br/>
	 * Default will be all PropStructBlocks.
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
	 * registeres structBlocks to display in {@link PropE}
	 * @param editorScope
	 * @param editorName
	 * @param propStructBlockKeys
	 */
	public void setEditorDomain(String editorScope, String editorName, StructBlockID[] propStructBlockKeys) {
		setEditorDomain(editorScope,editorName);
		structBlockRegistry.addEditorStructBlocks(editorName,propStructBlockKeys);
	}
	
	private List domainPropStructBlocks;
	
	protected boolean shouldDisplayStructBlock(DataBlockGroup blockGroup) {
		// default is all PropStructBlocks
		if (domainPropStructBlocks == null)
			return true;
		else
			return domainPropStructBlocks.contains(StructBlockID.create(blockGroup.getStructBlockOrganisationID(),blockGroup.getStructBlockID()));
	}
	
	protected void buildDomainDataBlockGroups() {
		if (domainPropStructBlocks == null) {
			if ((editorScope != null ) && (editorName != null)) {
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
	public void setEditorPropStructBlockList(List structBlockList) {
		if (structBlockList != null && structBlockList.size() > 0)
		{
			domainPropStructBlocks = structBlockList;
		}
		else
		{
			domainPropStructBlocks = null;
		}
	}
	
	
	protected Iterator getDataBlockGroupsIterator() {
		buildDomainDataBlockGroups();
		return propSet.getDataBlockGroups().iterator();
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
	
		int allStructBlockCount = getPropStructure().getStructBlocks().size();
		List<DataBlockGroup> result = new LinkedList<DataBlockGroup>();
		IStruct struct = propSet.getStructure();
		if (struct == null)
			throw new IllegalStateException("The PropertySet was not exploded yet");
		Map<String, Integer> structBlockOrder = getStructBlockDisplayOrder(struct);
		
		int unmentionedCount = 0;
		// all datablocks of this propSet
		for (Iterator<DataBlockGroup> it = propSet.getDataBlockGroups().iterator(); it.hasNext(); ) {
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
