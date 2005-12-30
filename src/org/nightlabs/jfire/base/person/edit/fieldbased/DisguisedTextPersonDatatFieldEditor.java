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

package org.nightlabs.jfire.base.person.edit.fieldbased;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.nightlabs.base.composite.DisguisedText;
import org.nightlabs.base.composite.DisguisedText.LabeledDisguisedText;
import org.nightlabs.jfire.base.person.edit.AbstractPersonDataFieldEditor;
import org.nightlabs.jfire.base.person.edit.AbstractPersonDataFieldEditorFactory;
import org.nightlabs.jfire.person.AbstractPersonDataField;
import org.nightlabs.jfire.person.TextPersonDataField;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class DisguisedTextPersonDatatFieldEditor extends AbstractPersonDataFieldEditor {
	
	public static class Factory extends AbstractPersonDataFieldEditorFactory {

		public Class getTargetPersonDataFieldType() {
			return TextPersonDataField.class;
		}

		public String getEditorType() {
			return DisguisedPersonEditor.EDITORTYPE_FIELD_BASED_DISGUISED;
		}

		public Class getPersonDataFieldEditorClass() {
			return DisguisedTextPersonDatatFieldEditor.class;
		}
		
	}

	/**
	 * 
	 */
	public DisguisedTextPersonDatatFieldEditor() {
		super();
	}

	private DisguisedTextEditorComposite composite;
	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(Composite parent) {
		composite = new DisguisedTextEditorComposite(parent,this);
		return composite;
	}

	private TextPersonDataField data;
	/**
	 * @see org.nightlabs.jfire.base.person.edit.AbstractPersonDataFieldEditor#doSetData(org.nightlabs.jfire.base.person.AbstractPersonDataField)
	 */
	public void doSetData(AbstractPersonDataField _data) {
		if (! (_data instanceof TextPersonDataField))
			throw new IllegalArgumentException("Argument data should be of type "+TextPersonDataField.class.getName()+" but was "+data.getClass().getName());
		this.data = (TextPersonDataField)_data;
		setChanged(false);
//		refreshComposite();
	}
	
	public void refreshComposite() {
		if (composite != null)
			composite.refresh();
	}
	
	public TextPersonDataField getData() {
		return data;
	}

	/**
	 * @see org.nightlabs.jfire.base.person.edit.AbstractPersonDataFieldEditor#doRefresh(org.nightlabs.jfire.base.person.AbstractPersonDataField)
	 */
	public void doRefresh() {
		refreshComposite();
	}

	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#updatePerson()
	 */
	public void updatePerson() {
		data.setText(composite.getText());
	}
	
	protected class DisguisedTextEditorComposite extends Composite {
		
		private Text editorText;
		private Label editorLabel;
		private DisguisedTextPersonDatatFieldEditor editor;
		
		private FocusListener focusListener = new FocusAdapter() {
			public void focusLost(FocusEvent arg0) {
				editorText.setSelection(0,0);
			}
		};
		
		public DisguisedTextEditorComposite(Composite parent, DisguisedTextPersonDatatFieldEditor editor) {
			super(parent,SWT.NONE);
			this.editor = editor;
			this.setSize(0,0);
			GridData gd = new GridData();
			gd.widthHint = 0;
			gd.heightHint = 0;
			this.setLayoutData(gd);
//			LabeledDisguisedText ldt = DisguisedText.createLabeledText(getPersonStructField().getFieldName().getText(),parent);
			// TODO: Reactivate above line
			LabeledDisguisedText ldt = DisguisedText.createLabeledText(editor.getData().getPersonStructFieldID(),parent);
			editorLabel = ldt.getLabelControl();
			editorText = ldt.getTextControl(); 
			editorText.addFocusListener(focusListener);
		}
		
		public String getText() {
			return editorText.getText(); 
		}
		
		public void refresh() {
			String editorText = editor.getData().getText();
			if (editorText != null)
				this.editorText.setText(editorText);
			else
				this.editorText.setText("");
		}
		
		public void dispose() {
			editorLabel.dispose();
			editorText.dispose();
			super.dispose();
		}
	}

	/**
	 * @see org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor#getControl()
	 */
	public Control getControl() {
		return composite;
	}

}
