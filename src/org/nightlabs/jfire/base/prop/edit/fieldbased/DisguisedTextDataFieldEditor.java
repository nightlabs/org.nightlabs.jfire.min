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

package org.nightlabs.jfire.base.prop.edit.fieldbased;

import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.composite.DisguisedText;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.composite.DisguisedText.LabeledDisguisedText;
import org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditor;
import org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditorFactory;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.datafield.TextDataField;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class DisguisedTextDataFieldEditor extends AbstractDataFieldEditor<TextDataField> {
	
	public static class Factory extends AbstractDataFieldEditorFactory<TextDataField> {

		public Class<TextDataField> getPropDataFieldType() {
			return TextDataField.class;
		}

		public String getEditorType() {
			return DisguisedEditor.EDITORTYPE_FIELD_BASED_DISGUISED;
		}

		public Class<DisguisedTextDataFieldEditor> getDataFieldEditorClass() {
			return DisguisedTextDataFieldEditor.class;
		}
	}

	public DisguisedTextDataFieldEditor() {		
	}

	private DisguisedTextEditorComposite composite;
	/**
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(Composite parent) {
		composite = new DisguisedTextEditorComposite(parent,this);
		return composite;
	}

	private TextDataField data;
	
	public void refreshComposite() {
		if (composite != null)
			composite.refresh();
	}
	
	public TextDataField getTextData() {
		if (getDataField() instanceof TextDataField)
			return getDataField();
		return null;
	}

	/**
	 * @see org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditor#doRefresh(org.nightlabs.jfire.base.prop.AbstractDataField)
	 */
	public void doRefresh() {
		refreshComposite();
	}

	/**
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#updatePropertySet()
	 */
	public void updatePropertySet() {
		data.setText(composite.getText());
	}
	
	protected class DisguisedTextEditorComposite extends XComposite {
		
		private Text editorText;
		private Label editorLabel;
		private DisguisedTextDataFieldEditor editor;
		
		private FocusListener focusListener = new FocusAdapter() {
			public void focusLost(FocusEvent arg0) {
				editorText.setSelection(0,0);
			}
		};
		
		public DisguisedTextEditorComposite(Composite parent, DisguisedTextDataFieldEditor editor) {
			super(parent, SWT.NONE);
			this.editor = editor;
			
//			LabeledDisguisedText ldt = DisguisedText.createLabeledText(getPropStructField().getFieldName().getText(),parent);
			// TODO: Reactivate above line
			StructField field = editor.getStructField();
			LabeledDisguisedText ldt = DisguisedText.createLabeledText(field.getName().getText(Locale.getDefault().getLanguage()),parent);
			editorLabel = ldt.getLabelControl();
			editorText = ldt.getTextControl(); 
			editorText.addFocusListener(focusListener);
		}
		
		public String getText() {
			return editorText.getText(); 
		}
		
		public void refresh() {
			String editorText = editor.getTextData().getText();
			if (editorText != null)
				this.editorText.setText(editorText);
			else
				this.editorText.setText(""); //$NON-NLS-1$
		}
		
		public void dispose() {
			editorLabel.dispose();
			editorText.dispose();
			super.dispose();
		}
	}

	/**
	 * @see org.nightlabs.jfire.base.prop.edit.DataFieldEditor#getControl()
	 */
	public Control getControl() {
		return composite;
	}

}
