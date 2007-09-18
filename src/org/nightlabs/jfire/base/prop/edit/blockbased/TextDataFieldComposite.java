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

import java.util.Locale;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.jfire.base.prop.edit.AbstractDataFieldEditor;
import org.nightlabs.jfire.base.prop.edit.AbstractInlineDataFieldComposite;
import org.nightlabs.jfire.prop.AbstractDataField;
import org.nightlabs.jfire.prop.datafield.II18nTextDataField;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class TextDataFieldComposite<DataFieldType extends AbstractDataField & II18nTextDataField> extends AbstractInlineDataFieldComposite<AbstractDataFieldEditor<DataFieldType>, DataFieldType> {

//	private Label fieldName;
	private Text fieldText;
//	private LabeledText fieldText;
//	private AbstractDataFieldEditor<DataFieldType> editor;
	private ModifyListener modifyListener;
	
	
	public TextDataFieldComposite(AbstractDataFieldEditor<DataFieldType> editor, Composite parent, int style, ModifyListener modListener, GridLayout gl) {
		super(parent, style, editor);
		if (!(parent.getLayout() instanceof GridLayout))
			throw new IllegalArgumentException("Parent should have a GridLayout!"); //$NON-NLS-1$
		
//		this.editor = editor; 
//		
//		Layout layout = createLayout();
//		setLayout(layout);
//		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
//		setLayoutData(gridData);
//		
//		fieldName = new Label(this, SWT.NONE);
//		fieldName.setLayoutData(createLabelLayoutData());
		
		fieldText = new Text(this, getTextBorderStyle());
//		fieldText.setEditable(true);
		fieldText.setEnabled(true);
		fieldText.setLayoutData(createTextLayoutData());
		this.modifyListener = modListener;
		fieldText.addModifyListener(modifyListener);
		
		if (gl != null)
			setLayout(gl);
		
//		fieldText = new LabeledText(this, "");
	}
	
	/**
	 * Assumes to have a parent composite with GridLaout and
	 * adds it own GridData.
	 * @param editor
	 * @param parent
	 * @param style
	 */
	public TextDataFieldComposite(AbstractDataFieldEditor<DataFieldType> editor, Composite parent, int style, ModifyListener modListener) {
		this(editor, parent, style, modListener, null);
	}
	
	protected Object createLabelLayoutData() {
		GridData nameData = new GridData(GridData.FILL_HORIZONTAL);
		nameData.grabExcessHorizontalSpace = true;
		return nameData;
	}

	protected Object createTextLayoutData() {
		GridData textData = new GridData(GridData.FILL_HORIZONTAL);
		textData.grabExcessHorizontalSpace = true;
		return textData;
	}
	
	protected int getTextBorderStyle() {
		return getBorderStyle();
	}
	
	/**
	 * @see org.nightlabs.jfire.base.prop.edit.AbstractInlineDataFieldComposite#refresh()
	 */
	public void _refresh() {
//		StructField field = getEditor().getStructField();
//		fieldText.setCaption(field.getName().getText());
//		if (editor.getDataField().getText() == null)
//			fieldText.setText("");
//		else
//			fieldText.setText(editor.getDataField().getText());
		
//		fieldName.setText(field.getName().getText());
		if (getEditor().getDataField().getText(Locale.getDefault()) == null)
			fieldText.setText(""); //$NON-NLS-1$
		else
			fieldText.setText(getEditor().getDataField().getText(Locale.getDefault()));
		
		// TODO set the text fields maximum line count to the one given by the struct field 
		// ((TextStructField)editor.getDataField().getStructField()).getLineCount();
	}
	
	public String getText() {
		return fieldText.getText();
	}
	
	public void dispose() {
		fieldText.removeModifyListener(modifyListener);
		super.dispose();
	}
}
