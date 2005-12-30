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

import org.nightlabs.jfire.base.person.edit.DataFieldEditorChangeListener;
import org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor;
import org.nightlabs.jfire.base.person.edit.PersonDataFieldEditorFactory;
import org.nightlabs.jfire.base.person.edit.blockbased.ExpandableBlocksPersonEditor;
import org.nightlabs.jfire.base.person.edit.blockbased.PersonDataBlockEditor;
import org.nightlabs.jfire.base.person.edit.blockbased.PersonDataBlockEditorFactory;
import org.nightlabs.jfire.person.AbstractPersonDataField;
import org.nightlabs.jfire.person.PersonDataBlock;
import org.nightlabs.jfire.person.PersonDataFieldNotFoundException;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.person.TextPersonDataField;
import org.nightlabs.jfire.person.id.PersonStructBlockID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class CommentDataBlockEditor extends PersonDataBlockEditor
	implements 
		ModifyListener,
		PersonDataFieldEditor
{
	
	public static final Logger LOGGER = Logger.getLogger(CommentDataBlockEditor.class);

	private Text textComment;
	private Label labelTitle;
	private Composite wrapper;
	
	/**
	 * @param dataBlock
	 * @param parent
	 * @param style
	 */
	public CommentDataBlockEditor(PersonDataBlock dataBlock, Composite parent, int style) {
		super(dataBlock, parent, style);
		setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout thisLayout = new GridLayout();
		thisLayout.horizontalSpacing = 2;
		thisLayout.verticalSpacing = 2;
		this.setLayout(thisLayout);
		refresh(dataBlock);
	}

	/**
	 * @see org.nightlabs.jfire.base.person.edit.blockbased.PersonDataBlockEditor#refresh(org.nightlabs.jfire.base.person.PersonDataBlock)
	 */
	public void refresh(PersonDataBlock block) {
		this.dataBlock = block;
		try {
			commentData = (TextPersonDataField)dataBlock.getPersonDataField(PersonStruct.COMMENT_COMMENT);
			refresh();
		} catch (PersonDataFieldNotFoundException e) {
			LOGGER.error("PersonDataField not found. ",e);
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
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#getTargetPersonDataType()
	 */
	public Class getTargetPersonDataType() {
		return TextPersonDataField.class;
	}

	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#getEditorType()
	 */
	public String getEditorType() {
		return ExpandableBlocksPersonEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE;
	}

	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(Composite parent) {
		if (textComment == null) {			
			labelTitle = new Label(parent,SWT.NONE);
			labelTitle.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			textComment = new Text(parent,SWT.MULTI);
			GridData commentLData = new GridData();
			commentLData.grabExcessHorizontalSpace = true;
			commentLData.grabExcessVerticalSpace = true;
			commentLData.horizontalAlignment = GridData.FILL;
			commentLData.verticalAlignment = GridData.FILL;
			textComment.setLayoutData(commentLData);
		}
		return this;
	}

	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#getNewEditorInstance(org.nightlabs.jfire.base.person.AbstractPersonDataField)
	 */
	public PersonDataFieldEditor getNewEditorInstance(AbstractPersonDataField data) {
		return new CommentDataBlockEditor(dataBlock,getParent(),getStyle());
	}

	private TextPersonDataField commentData;
	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#setData(org.nightlabs.jfire.base.person.AbstractPersonDataField)
	 */
	public void setData(AbstractPersonDataField data) {
		commentData = (TextPersonDataField)data;
		refresh();
	}

	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#refresh()
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
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#addDataFieldEditorChangedListener(org.nightlabs.jfire.base.person.edit.DataFieldEditorChangeListener)
	 */
	public void addDataFieldEditorChangedListener(DataFieldEditorChangeListener listener) {
		fieldEditorChangeListener.add(listener);
	}

	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#removeDataFieldEditorChangedListener(org.nightlabs.jfire.base.person.edit.DataFieldEditorChangeListener)
	 */
	public void removeDataFieldEditorChangedListener(DataFieldEditorChangeListener listener) {
		fieldEditorChangeListener.remove(listener);
	}

	
	private boolean changed = false;
	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#setChanged(boolean)
	 */	
	public void setChanged(boolean changed) {
		this.changed = changed;
		if (!refreshing)
			notifyChangeListeners(this);
	}
	
	protected synchronized void notifyChangeListeners(PersonDataFieldEditor dataFieldEditor) {
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
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#isChanged()
	 */
	public boolean isChanged() {
		return changed;
	}

	
	public static class Factory implements PersonDataBlockEditorFactory {
		/**
		 * @see org.nightlabs.jfire.base.person.edit.blockbased.PersonDataBlockEditorFactory#getProviderStructBlockID()
		 */
		public PersonStructBlockID getProviderStructBlockID() {
			return PersonStruct.COMMENT;
		}
		
		/**
		 * @see org.nightlabs.jfire.base.person.edit.blockbased.PersonDataBlockEditorFactory#createPersonDataBlockEditor(org.nightlabs.jfire.base.person.PersonDataBlock, org.eclipse.swt.widgets.Composite, int)
		 */
		public PersonDataBlockEditor createPersonDataBlockEditor(PersonDataBlock dataBlock, Composite parent, int style) {
			return new CommentDataBlockEditor(dataBlock,parent,style);
		}
	}


	public Control getControl() {
		return this;
	}

	protected PersonDataFieldEditorFactory factory;
	
	public void setPersonDataFieldEditorFactory(PersonDataFieldEditorFactory factory) {
		this.factory = factory;
	}

	public PersonDataFieldEditorFactory getPersonDataFieldEditorFactory() {
		return factory;
	}

}
