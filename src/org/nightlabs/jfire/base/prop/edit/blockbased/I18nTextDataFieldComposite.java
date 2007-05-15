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
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.language.I18nTextEditor;
import org.nightlabs.base.language.I18nTextEditor.EditMode;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.base.prop.edit.AbstractDataFieldComposite;
import org.nightlabs.jfire.prop.StructField;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class I18nTextDataFieldComposite extends AbstractDataFieldComposite {

	private Label fieldName;
	private I18nTextEditor i18nTextEditor;
	private I18nTextDataFieldEditor editor;
	private ModifyListener modifyListener;
	
	/**
	 * Assumes to have a parent composite with GridLaout and
	 * adds it own GridData.
	 * @param editor
	 * @param parent
	 * @param style
	 */
	public I18nTextDataFieldComposite(I18nTextDataFieldEditor editor, Composite parent, int style, ModifyListener modListener) {
		super(parent, style);
		if (!(parent.getLayout() instanceof GridLayout))
			throw new IllegalArgumentException("Parent should have a GridLayout!");
		
		this.editor = editor;
		
		GridLayout layout = new GridLayout();
		setLayout(layout);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		GridData gridData = new GridData(GridData.FILL_BOTH);
		setLayoutData(gridData);
		
		fieldName = new Label(this,SWT.PUSH);
		GridData nameData = new GridData(GridData.FILL_HORIZONTAL);
		nameData.grabExcessHorizontalSpace = true;
		fieldName.setLayoutData(nameData);
		
		i18nTextEditor = new I18nTextEditor(this);
		i18nTextEditor.setEditMode(EditMode.BUFFERED);
		GridData textData = new GridData(GridData.FILL_HORIZONTAL);
		textData.grabExcessHorizontalSpace = true;
		i18nTextEditor.setEditable(true);
		i18nTextEditor.setEnabled(true);
		i18nTextEditor.setLayoutData(textData);
		this.modifyListener = modListener;
		i18nTextEditor.addModifyListener(modifyListener);
	}

	/**
	 * @see org.nightlabs.jfire.base.prop.edit.AbstractDataFieldComposite#refresh()
	 */
	public void refresh() {
		StructField field = editor.getStructField();
		fieldName.setText(field.getName().getText());
		i18nTextEditor.getI18nText().copyFrom(editor.getDataField().getI18nText());
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
