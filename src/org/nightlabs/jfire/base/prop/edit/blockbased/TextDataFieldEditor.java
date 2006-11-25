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

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditor;
import org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditorFactory;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.prop.AbstractDataField;
import org.nightlabs.jfire.prop.TextDataField;

/**
 * Represents an editor for {@link TextDataField} within a
 * block based ExpandableBlocksEditor.
 *  
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class TextDataFieldEditor extends AbstractDataFieldEditor {
	
	public TextDataFieldEditor() {		
	}

	private static Logger LOGGER = Logger.getLogger(TextDataFieldEditor.class);
	
	public static class Factory extends AbstractDataFieldEditorFactory {
		/**
		 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditorFactory#getTargetPropDataFieldType()
		 */
		public Class getTargetPropDataFieldType() {
			return TextDataField.class;
		}
		/**
		 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditorFactory#getEditorType()
		 */
		public String getEditorType() {
			return ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE;
		}
		/**
		 * @see org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditorFactory#getPropDataFieldEditorClass()
		 */
		public Class getPropDataFieldEditorClass() {
			return TextDataFieldEditor.class;
		}
		
	}
	
	
	/**
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#getTargetPropDataType()
	 */
	public Class getTargetPropDataType() {
		return TextDataField.class;
	}

	/**
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#getEditorType()
	 */
	public String getEditorType() {
		return ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE;
	}

	
	private TextDataFieldComposite composite;
	/**
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(Composite parent) {
		if (composite == null) {
			composite = new TextDataFieldComposite(this,parent,SWT.NONE,this);
		}
		composite.refresh();
		return composite;
	}
	
	/**
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#getControl()
	 */
	public Control getControl() {
		return composite;
	}

	private TextDataField data;
	/**
	 * @see org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditor#setData(org.nightlabs.jfire.base.prop.AbstractDataField)
	 */
	public void doSetData(AbstractDataField data) {
		if (! (data instanceof TextDataField))
			throw new IllegalArgumentException("Argument data should be of type "+TextDataField.class.getName()+" but was "+data.getClass().getName());
		this.data = (TextDataField)data;
		setChanged(false);
		// TODO: refesh called twice ??
//		if (composite != null)
//			composite.refresh();
	}
	
	public TextDataField getData() {
		return data;
	}

	/**
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#updateProp()
	 */
	public void updateProp() {
		data.setText(composite.getText());
	}

	/**
	 * @see org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditor#doRefresh(org.nightlabs.jfire.base.prop.AbstractDataField)
	 */
	public void doRefresh(AbstractDataField data) {
		setData(getStruct(), data);
	}

	/**
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#disposeControl()
	 */
	public void disposeControl() {
		composite.dispose();
	}

	/**
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#refresh()
	 */
	public void doRefresh() {
		if (composite != null)
			composite.refresh();
	}

}
