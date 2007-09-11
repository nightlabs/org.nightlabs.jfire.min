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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditor;
import org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditorFactory;
import org.nightlabs.jfire.base.prop.edit.fieldbased.FieldBasedEditor;
import org.nightlabs.jfire.prop.datafield.TextDataField;

/**
 * Represents an editor for {@link TextDataField} within a
 * block based ExpandableBlocksEditor.
 *  
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class TextDataFieldEditor extends AbstractDataFieldEditor<TextDataField> {
	
	public TextDataFieldEditor() {		
	}

	public static class Factory extends AbstractDataFieldEditorFactory<TextDataField> {
		/**
		 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditorFactory#getPropDataFieldType()
		 */
		public Class<TextDataField> getPropDataFieldType() {
			return TextDataField.class;
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public String[] getEditorTypes() {
			return new String[] {ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE, FieldBasedEditor.EDITORTYPE_FIELD_BASED};
		}
		/**
		 * @see org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditorFactory#getDataFieldEditorClass()
		 */
		public Class<TextDataFieldEditor> getDataFieldEditorClass() {
			return TextDataFieldEditor.class;
		}
	}
	
	/**
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#getEditorType()
	 */
	public String getEditorType() {
		return ExpandableBlocksEditor.EDITORTYPE_BLOCK_BASED_EXPANDABLE;
	}	
	
	private TextDataFieldComposite<TextDataField> composite;
	/**
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(Composite parent) {
		if (composite == null) {
			composite = new TextDataFieldComposite<TextDataField>(this, parent, SWT.NONE, this);
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

	/**
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#updatePropertySet()
	 */
	public void updatePropertySet() 
	{
//		Display.getDefault().syncExec(new Runnable(){
//			public void run() {
				getDataField().setText(composite.getText());	
//			}
//		});
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
