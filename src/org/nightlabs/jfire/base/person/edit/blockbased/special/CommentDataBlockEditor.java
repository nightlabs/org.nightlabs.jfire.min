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

package org.nightlabs.jfire.base.person.edit.blockbased.special;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditorChangeListener;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditorFactory;
import org.nightlabs.jfire.base.prop.edit.blockbased.DataBlockEditor;
import org.nightlabs.jfire.base.prop.edit.blockbased.DataBlockEditorFactory;
import org.nightlabs.jfire.base.prop.edit.blockbased.ExpandableBlocksEditor;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.AbstractDataField;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.TextDataField;
import org.nightlabs.jfire.prop.exception.DataFieldNotFoundException;
import org.nightlabs.jfire.prop.id.StructBlockID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class CommentDataBlockEditor extends DataBlockEditor
	implements 
		ModifyListener,
		DataFieldEditor
{
	
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(CommentDataBlockEditor.class);

	private Text textComment;
	private Label labelTitle;
	private Composite wrapper;
	
	/**
	 * @param dataBlock
	 * @param parent
	 * @param style
	 */
	public CommentDataBlockEditor(IStruct struct, DataBlock dataBlock, Composite parent, int style) {
		super(struct, dataBlock, parent, style);
		setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout thisLayout = new GridLayout();
		thisLayout.horizontalSpacing = 2;
		thisLayout.verticalSpacing = 2;
		this.setLayout(thisLayout);
		refresh(struct, dataBlock);
	}

	/**
	 * @see org.nightlabs.jfire.base.person.edit.blockbased.DataBlockEditor#refresh(org.nightlabs.jfire.base.person.DataBlock)
	 */
	public void refresh(IStruct struct, DataBlock block) {
		this.dataBlock = block;
		try {
			commentData = (TextDataField)dataBlock.getDataField(PersonStruct.COMMENT_COMMENT);
			refresh();
		} catch (DataFieldNotFoundException e) {
			logger.error("DataField not found. ",e);
			commentData = null;
		}
	}

	
	private boolean refreshing = false;
	/**
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	public void modifyText(ModifyEvent evt) {
		setChanged(true);
	}

	/**
	 * @see org.nightlabs.jfire.base.person.edit.DataFieldEditor#getTargetDataType()
	 */
	public Class getTargetDataType() {
		return TextDataField.class;
	}

	/**
	 * @see org.nightlabs.jfire.base.person.edit.DataFieldEditor#getEditorType()
	 */
	public String getEditorType() {
		return ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE;
	}

	/**
	 * @see org.nightlabs.jfire.base.person.edit.DataFieldEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(Composite parent) {
		if (textComment == null) {			
			labelTitle = new Label(parent,SWT.NONE);
			labelTitle.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			textComment = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
			GridData commentLData = new GridData();
			commentLData.grabExcessHorizontalSpace = true;
			commentLData.grabExcessVerticalSpace = true;
			commentLData.horizontalAlignment = GridData.FILL;
			commentLData.verticalAlignment = GridData.FILL;
			textComment.setLayoutData(commentLData);
		}
		return this;
	}

	public DataFieldEditor getNewEditorInstance(IStruct struct, AbstractDataField data) {
		return new CommentDataBlockEditor(struct, dataBlock,getParent(),getStyle());
	}

	private TextDataField commentData;

	/**
	 * @see org.nightlabs.jfire.base.person.edit.DataFieldEditor#refresh()
	 */
	public void refresh() {
		refreshing = true;
		try {
			createControl(this);
			if (commentData != null) {
				if (commentData.getText() == null)
					textComment.setText("");
				else
					textComment.setText(commentData.getText());
			}
		} finally {
			refreshing = false;
		}
	}

	private List fieldEditorChangeListener = new LinkedList();
	
	/**
	 * @see org.nightlabs.jfire.base.person.edit.DataFieldEditor#addDataFieldEditorChangedListener(org.nightlabs.jfire.base.person.edit.DataFieldEditorChangeListener)
	 */
	public void addDataFieldEditorChangedListener(DataFieldEditorChangeListener listener) {
		fieldEditorChangeListener.add(listener);
	}

	/**
	 * @see org.nightlabs.jfire.base.person.edit.DataFieldEditor#removeDataFieldEditorChangedListener(org.nightlabs.jfire.base.person.edit.DataFieldEditorChangeListener)
	 */
	public void removeDataFieldEditorChangedListener(DataFieldEditorChangeListener listener) {
		fieldEditorChangeListener.remove(listener);
	}

	
	private boolean changed = false;
	/**
	 * @see org.nightlabs.jfire.base.person.edit.DataFieldEditor#setChanged(boolean)
	 */	
	public void setChanged(boolean changed) {
		this.changed = changed;
		if (!refreshing)
			notifyChangeListeners(this);
	}
	
	protected synchronized void notifyChangeListeners(DataFieldEditor dataFieldEditor) {
		if (refreshing)
			return;
		// first notify fieldEditorChangeListener
		for (Iterator iter = fieldEditorChangeListener.iterator(); iter.hasNext();) {
			DataFieldEditorChangeListener listener = (DataFieldEditorChangeListener) iter.next();
			listener.dataFieldEditorChanged(this);
		}
		// then blockEditorChangeListener
		super.notifyChangeListeners(dataFieldEditor);
	}

	/**
	 * @see org.nightlabs.jfire.base.person.edit.DataFieldEditor#isChanged()
	 */
	public boolean isChanged() {
		return changed;
	}

	
	public static class Factory implements DataBlockEditorFactory {
		/**
		 * @see org.nightlabs.jfire.base.person.edit.blockbased.DataBlockEditorFactory#getProviderStructBlockID()
		 */
		public StructBlockID getProviderStructBlockID() {
			return PersonStruct.COMMENT;
		}
		
		/**
		 * @see org.nightlabs.jfire.base.person.edit.blockbased.DataBlockEditorFactory#createDataBlockEditor(org.nightlabs.jfire.base.person.DataBlock, org.eclipse.swt.widgets.Composite, int)
		 */
		public DataBlockEditor createPropDataBlockEditor(IStruct struct, DataBlock dataBlock, Composite parent, int style) {
			return new CommentDataBlockEditor(struct, dataBlock,parent,style);
		}
	}


	public Control getControl() {
		return this;
	}

	protected DataFieldEditorFactory factory;
	
	public void setPropDataFieldEditorFactory(DataFieldEditorFactory factory) {
		this.factory = factory;
	}

	public DataFieldEditorFactory getPropDataFieldEditorFactory() {
		return factory;
	}

	public void setData(IStruct struct, AbstractDataField data) {
		commentData = (TextDataField)data;
		refresh();
	}

}
