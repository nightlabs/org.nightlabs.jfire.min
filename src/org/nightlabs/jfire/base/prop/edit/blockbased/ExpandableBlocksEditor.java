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

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.nightlabs.jfire.base.prop.edit.PropertySetEditor;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.jfire.prop.DataBlockGroup;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.progress.ProgressMonitor;

/**
 * A PropertySetEditor based on PropStructBlocks/PropDataBlocks.
 * This will present one expandable Composite with all PropDataFields
 * for all StructBlock/DataBlock it gets assosiated with.<br/>
 * Control wich blocks a editor displays by associating a list of blocks
 * to a editor-domain.
 * 
 * @see org.nightlabs.jfire.base.prop.edit.blockbased.AbstractDataBlockEditor
 * @see org.nightlabs.jfire.base.prop.edit.blockbased.EditorStructBlockRegistry
 * @see org.nightlabs.jfire.base.prop.edit.PropertySetEditor
 *  
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class ExpandableBlocksEditor implements PropertySetEditor { // extends ScrolledComposite {
	public static final String EDITORTYPE_BLOCK_BASED_EXPANDABLE = "block-based-expandable"; //$NON-NLS-1$

	public ExpandableBlocksEditor() {
		this(null);
	}
	
	public ExpandableBlocksEditor(PropertySet prop) {
		this.prop = prop;
		structBlockRegistry = new EditorStructBlockRegistry(prop.getStructLocalLinkClass(), prop.getStructLocalScope());
	}

	
	private FormToolkit toolkit = null;
	
	private PropertySet prop;
	private EditorStructBlockRegistry structBlockRegistry;
	
	/**
	 * Sets the current propertySet of this editor.
	 * If refresh is true {@link #refreshForm(DataBlockEditorChangedListener)} 
	 * is called.
	 * @param refresh
	 * @param propertySet
	 */
	public void setPropertySet(PropertySet prop, boolean refresh) {
		this.prop = prop;
		structBlockRegistry = new EditorStructBlockRegistry(prop.getStructLocalLinkClass(), prop.getStructLocalScope());
		if (refresh)
			refreshControl();		
	}
	
	/**
	 * Will only set the propertySet, no changes to the UI will be made.
	 * @param propertySet
	 */
	public void setPropertySet(PropertySet prop) {		
		setPropertySet(prop, false);
	}
	/**
	 * Returns the propertySet.
	 * @return
	 */
	public PropertySet getProp() {
		return prop;
	}

	private ScrolledForm form = null;
	/**
	 * Returns the {@link ScrolledForm}. 
	 * With {@link ScrolledForm#getBody()} you can access the Composite.
	 * @return
	 */
	public ScrolledForm getForm() {
		return form;
	}
	
	/**
	 * Holds the GroupEditors.
	 */
	private Map<String, ExpandableDataBlockGroupEditor> groupEditors = new HashMap<String, ExpandableDataBlockGroupEditor>();
	
	
	public Map<String, ExpandableDataBlockGroupEditor> getGroupEditors() {
		return groupEditors;
	}
	
	protected IStruct getPropStructure(ProgressMonitor monitor) {
		if (prop.isExploded())
			return prop.getStructure();
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.base.prop.edit.blockbased.ExpandableBlocksEditor.getPropStructure.monitor.taskName"), 1); //$NON-NLS-1$
		IStruct structure = StructLocalDAO.sharedInstance().getStructLocal(
				prop.getStructLocalLinkClass(), prop.getStructLocalScope(), monitor
		);
		monitor.worked(1);
		return structure;
	}
	
	
//	public void refreshControl() {
//		refreshControl(null);
//	}
	
	/**
	 * Refreshes the UI-Representation of the given Property.
	 * 
	 * @param changeListener
	 */
	public void refreshControl() {
		Display.getDefault().asyncExec( 
			new Runnable() {
				public void run() {
					if (!prop.isExploded())
						getPropStructure(new NullProgressMonitor()).explodePropertySet(prop);
					
					// get the ordered dataBlocks
					for (Iterator it = ExpandableBlocksEditor.this.getOrderedDataBlockGroupsIterator(); it.hasNext(); ) {
						DataBlockGroup blockGroup = (DataBlockGroup)it.next();
						if (shouldDisplayStructBlock(blockGroup)) {
							if (!groupEditors.containsKey(blockGroup.getStructBlockKey())) {
								ExpandableDataBlockGroupEditor groupEditor = new ExpandableDataBlockGroupEditor(prop.getStructure(), blockGroup, form.getBody());
								groupEditor.setOwner(form);
								if (ExpandableBlocksEditor.this.changeListener != null) 
									groupEditor.addPropDataBlockEditorChangedListener(ExpandableBlocksEditor.this.changeListener);
								groupEditors.put(blockGroup.getStructBlockKey(), groupEditor);
							}
							else {			
								ExpandableDataBlockGroupEditor groupEditor = (ExpandableDataBlockGroupEditor)groupEditors.get(blockGroup.getStructBlockKey());								
								groupEditor.refresh(blockGroup);
							}
						} // if (shouldDisplayStructBlock(blockGroup)) {
					}		
					form.reflow(true);
				}
			}
		);
	}
	
	private DataBlockEditorChangedListener changeListener;
	
	public ScrolledForm createForm(Composite parent, DataBlockEditorChangedListener changeListener, boolean refresh) {
		return (ScrolledForm)createControl(parent,changeListener,refresh);
	}
	/**
	 * Creates the Form. 
	 * If refresh is true {@link #refreshForm(DataBlockEditorChangedListener)}
	 * will be called. 
	 * 
	 * @param parent
	 * @param changeListener
	 * @param refresh
	 * @return
	 */
	public Control createControl(Composite parent, DataBlockEditorChangedListener changeListener, boolean refresh) {
		if (form != null)
			return form;
		if (toolkit == null)
			toolkit = new FormToolkit(parent.getDisplay());
		this.changeListener = changeListener;
		
		form = toolkit.createScrolledForm(parent);
		form.setBackground(parent.getBackground());
		form.setForeground(parent.getForeground());
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		form.getBody().setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		form.getBody().setLayoutData(gd);
		
		if (refresh)
			refreshControl();
		return form;
	}
	
	/**
	 * 
	 * @see org.nightlabs.jfire.base.prop.edit.PropertySetEditor#disposeControl()
	 */
	public void disposeControl() {
		if (form != null && !form.isDisposed()) {
			form.dispose();
			form = null;
		}
	}

	/**
	 * Will only create the Form. No propertySet data will be displayed
	 * 
	 * @param parent
	 * @param changeListener
	 * @return
	 */
	public ScrolledForm createForm(Composite parent, DataBlockEditorChangedListener changeListener) {
		return createForm(parent,changeListener,false);
	}
	
	/**
	 * Will create the form. No change listener will be set and 
	 * no propertySet data will be displayed.
	 * 
	 * @param parent
	 * @return
	 */
	public ScrolledForm createForm(Composite parent) {
		return createForm(parent, null);	
	}
	
	public ScrolledForm createForm(Composite parent, boolean refresh) {
		return createForm(parent, null, refresh);	
	}

	public Control createControl(Composite parent, boolean refresh) {
		return createForm(parent, refresh);	
	}

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
	 * Shortcut of setting the list of PropStructBlocks
	 * this editor should display. 
	 * After this was set to a non null value this editor 
	 * will not care about registrations in {@link EditorStructBlockRegistry}.
	 * 
	 * @param structBlockList
	 */	
	public void setEditorPropStructBlockList(List structBlockList) {
		if (structBlockList != null) {
			if (structBlockList.size() > 0)
				domainPropStructBlocks = structBlockList;
			else
				domainPropStructBlocks = null;
		} else {
			domainPropStructBlocks = null;
		}
	}
	
	
	protected Iterator<DataBlockGroup> getDataBlockGroupsIterator() {
		buildDomainDataBlockGroups();
		return prop.getDataBlockGroups().iterator();
	}
	
	public Map<String, Integer> getStructBlockDisplayOrder() {
		//return AbstractPropStructOrderConfigModule.sharedInstance().structBlockDisplayOrder();
		return new HashMap<String, Integer>();
	}
	
	protected Iterator<DataBlockGroup> getOrderedDataBlockGroupsIterator() {
		buildDomainDataBlockGroups();

		int allStructBlockCount = prop.getStructure().getStructBlocks().size();
		List<DataBlockGroup> result = new LinkedList<DataBlockGroup>();
		Map<String, Integer> structBlockOrder = getStructBlockDisplayOrder();
		
//		int maxIndex = 0;
		int unmentionedCount = 0;
		// all datablocks of this propertySet
		for (Iterator<DataBlockGroup> it = prop.getDataBlockGroups().iterator(); it.hasNext(); ) {
			DataBlockGroup blockGroup = (DataBlockGroup)it.next();
//			boolean orderedAdd = false;
			if (structBlockOrder.containsKey(blockGroup.getStructBlockKey())) {
				// block mentioned in structBlockOrder
				Integer index = (Integer)structBlockOrder.get(blockGroup.getStructBlockKey());
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

	public void updatePropertySet() {
		for (ExpandableDataBlockGroupEditor groupEditor : groupEditors.values()) {
			groupEditor.updateProp();
		}
	}
	
}
