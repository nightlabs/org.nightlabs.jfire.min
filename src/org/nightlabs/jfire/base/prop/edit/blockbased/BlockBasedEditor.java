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
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.composite.groupedcontent.GroupedContentComposite;
import org.nightlabs.base.ui.composite.groupedcontent.GroupedContentProvider;
import org.nightlabs.jfire.prop.DataBlockGroup;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @see org.nightlabs.jfire.base.prop.edit.blockbased.AbstractDataBlockEditor
 * @see org.nightlabs.jfire.base.prop.edit.blockbased.EditorStructBlockRegistry
 * @see org.nightlabs.jfire.base.prop.edit.PropertySetEditor
 *  
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class BlockBasedEditor extends AbstractBlockBasedEditor {
	
	public static final String EDITORTYPE_BLOCK_BASED = "block-based"; //$NON-NLS-1$
	
	private GroupedContentComposite groupedContentComposite;
	
	private class ContentProvider implements GroupedContentProvider {
		private DataBlockGroupEditor groupEditor;
		private DataBlockGroup blockGroup;
		private IStruct struct;
		
		public ContentProvider(DataBlockGroup blockGroup, IStruct struct) {
			this.blockGroup = blockGroup;
			this.struct = struct;
		}
		
		public Image getGroupIcon() {
			return null;
		}
		public String getGroupTitle() {
			//return blockGroup.getStructBlock(getPropStructure()).getID();
			return blockGroup.getStructBlock(struct).getName().getText();
		}
		public Composite createGroupContent(Composite parent) {
			groupEditor = new DataBlockGroupEditor(struct, blockGroup, parent);
			if (changeListener != null)
				groupEditor.addPropDataBlockEditorChangedListener(changeListener);
			return groupEditor;
		}
		public void refresh(DataBlockGroup blockGroup) {
			if (groupEditor != null) {
				groupEditor.refresh(struct, blockGroup);
			}
			this.blockGroup = blockGroup;
		}
		public void updateProp() {
			if (groupEditor != null) {
				groupEditor.updatePropopertySet();
			}
		}
	}
	
	public BlockBasedEditor() {
		super (null, null);
	}
	
	public BlockBasedEditor(PropertySet propSet, IStruct propStruct) {
		super(propSet, propStruct);
	}
	
	private Map<String, ContentProvider> groupContentProvider = new HashMap<String, ContentProvider>();
	
	/**
	 * Refreshes the UI-Representation of the given Property.
	 * 
	 * @param changeListener
	 */
	public void refreshControl() {
		Display.getDefault().asyncExec( 
			new Runnable() {
				public void run() {
					if (groupedContentComposite == null || groupedContentComposite.isDisposed())
						return;
					
					if (!propertySet.isExploded())
						getPropStructure(new NullProgressMonitor()).explodePropertySet(propertySet);
					
					// get the ordered dataBlocks
					for (Iterator<DataBlockGroup> it = BlockBasedEditor.this.getOrderedDataBlockGroupsIterator(); it.hasNext(); ) {
						DataBlockGroup blockGroup = it.next();
						if (shouldDisplayStructBlock(blockGroup)) {
							if (!groupContentProvider.containsKey(blockGroup.getStructBlockKey())) {
								ContentProvider contentProvider = new ContentProvider(blockGroup, propertySet.getStructure());
								groupContentProvider.put(blockGroup.getStructBlockKey(),contentProvider);
								groupedContentComposite.addGroupedContentProvider(contentProvider);
							}
							else {			
								ContentProvider contentProvider = (ContentProvider)groupContentProvider.get(blockGroup.getStructBlockKey());								
								contentProvider.refresh(blockGroup);
							}
						} // if (shouldDisplayStructBlock(blockGroup)) {
					}		
					groupedContentComposite.layout();
				}
			}
		);
	}

	private DataBlockEditorChangedListener changeListener;
	
	public Control createControl(Composite parent, DataBlockEditorChangedListener changeListener, boolean refresh) {
		this.changeListener = changeListener;
		return createControl(parent, refresh);
	}
	
	/**
	 * @param changeListener The changeListener to set.
	 */
	public void setChangeListener(
			DataBlockEditorChangedListener changeListener) {
		this.changeListener = changeListener;
	}

	public Control createControl(Composite parent, boolean refresh) {
		if (groupedContentComposite == null) {
			groupedContentComposite = new GroupedContentComposite(parent, SWT.NONE, true);
			groupedContentComposite.setGroupTitle("propTail"); //$NON-NLS-1$
		}
		if (refresh)
			refreshControl();
		return groupedContentComposite;
	}

	public void disposeControl() {
		if (groupedContentComposite != null)
			if (!groupedContentComposite.isDisposed())
				groupedContentComposite.dispose();
		groupedContentComposite = null;
	}

	public void updatePropertySet() {
		for (ContentProvider contentProvider : groupContentProvider.values()) {
			contentProvider.updateProp();
		}
	}
	
}
