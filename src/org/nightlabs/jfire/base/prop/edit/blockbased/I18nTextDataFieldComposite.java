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

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.language.I18nTextEditor;
import org.nightlabs.base.language.I18nTextEditorMultiLine;
import org.nightlabs.base.language.I18nTextEditor.EditMode;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.base.prop.edit.AbstractInlineDataFieldComposite;
import org.nightlabs.jfire.prop.datafield.I18nTextDataField;
import org.nightlabs.jfire.prop.structfield.I18nTextStructField;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class I18nTextDataFieldComposite extends AbstractInlineDataFieldComposite<I18nTextDataFieldEditor, I18nTextDataField> {

	private ModifyListener modifyListener;
	private I18nTextEditor i18nTextEditor;
	
	/**
	 * Assumes to have a parent composite with GridLaout and
	 * adds it own GridData.
	 * @param editor
	 * @param parent
	 * @param style
	 */
	public I18nTextDataFieldComposite(I18nTextDataFieldEditor editor, Composite parent, int style, ModifyListener modListener) {
		super(parent, style, editor);
		if (!(parent.getLayout() instanceof GridLayout))
			throw new IllegalArgumentException("Parent should have a GridLayout!"); //$NON-NLS-1$
		
//		GridLayout layout = new GridLayout();
//		setLayout(layout);
//		layout.horizontalSpacing = 2;
//		layout.verticalSpacing = 0;
//		layout.marginHeight = 2;
//		layout.marginWidth = 2;
//		GridData gridData = new GridData(GridData.FILL_BOTH);
//		setLayoutData(gridData);
//		
//		fieldName = new Label(this, SWT.NONE);
//		GridData nameData = new GridData(GridData.FILL_HORIZONTAL);
//		nameData.grabExcessHorizontalSpace = true;
//		fieldName.setLayoutData(nameData);
//
		createEditor(modListener);
	}
	
	private void createEditor(ModifyListener modListener) {
		I18nTextStructField field = (I18nTextStructField) getEditor().getStructField();		
		if (field.getLineCount() > 1)		
			i18nTextEditor = new I18nTextEditorMultiLine(this, null, null, field.getLineCount());
		else
			i18nTextEditor = new I18nTextEditor(this);
		
		i18nTextEditor.setI18nText(null, EditMode.BUFFERED);
		GridData textData = new GridData(GridData.FILL_HORIZONTAL);
		textData.grabExcessHorizontalSpace = true;
		i18nTextEditor.setEditable(true);
		if (i18nTextEditor instanceof Composite) {
			((Composite)i18nTextEditor).setEnabled(true);
			((Composite)i18nTextEditor).setLayoutData(textData);
		}
		this.modifyListener = modListener;
		i18nTextEditor.addModifyListener(modifyListener);
	}

	/**
	 * @see org.nightlabs.jfire.base.prop.edit.AbstractInlineDataFieldComposite#refresh()
	 */
	public void _refresh() {
		if (i18nTextEditor != null)
			i18nTextEditor.dispose();
		
		createEditor(modifyListener);
		
//		StructField field = getEditor().getStructField();
//		fieldName.setText(field.getName().getText());
		i18nTextEditor.getI18nText().copyFrom(getEditor().getDataField().getI18nText());
		i18nTextEditor.refresh();
		// TODO set the text fields maximum line count to the one given by the struct field 
		// ((TextStructField)editor.getDataField().getStructField()).getLineCount();
	}
	
	public void updateFieldText(I18nText fieldText) {
		i18nTextEditor.getI18nText().copyTo(fieldText);
	}
	
	public void dispose() {
		i18nTextEditor.removeModifyListener(modifyListener);
		super.dispose();
	}
}
