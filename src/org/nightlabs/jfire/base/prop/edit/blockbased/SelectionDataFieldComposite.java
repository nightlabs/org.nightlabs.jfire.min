package org.nightlabs.jfire.base.prop.edit.blockbased;

import java.util.ArrayList;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.composite.CComboComposite;
import org.nightlabs.jfire.base.prop.edit.AbstractDataFieldComposite;
import org.nightlabs.jfire.prop.exception.StructFieldValueNotFoundException;
import org.nightlabs.jfire.prop.structfield.SelectionStructField;
import org.nightlabs.jfire.prop.structfield.StructFieldValue;

public class SelectionDataFieldComposite extends AbstractDataFieldComposite {

	private Label fieldName;
	private CComboComposite<StructFieldValue> fieldValueCombo;
	private SelectionDataFieldEditor editor;
	private ModifyListener modifyListener;
	
	/**
	 * Assumes to have a parent composite with GridLaout and
	 * adds it own GridData.
	 * @param editor
	 * @param parent
	 * @param style
	 */
	public SelectionDataFieldComposite(final SelectionDataFieldEditor editor, Composite parent, int style,
			ModifyListener modListener) {
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
		
		fieldName = new Label(this, SWT.NONE);
		GridData nameData = new GridData(GridData.FILL_HORIZONTAL);
		nameData.grabExcessHorizontalSpace = true;
		fieldName.setLayoutData(nameData);
		
		LabelProvider labelProvider = new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof StructFieldValue) {
					StructFieldValue value = (StructFieldValue) element;
					return value.getValueName().getText(editor.getLanguage().getLanguageID());					
				}
				return "";
			}
		};
		
		fieldValueCombo = new CComboComposite<StructFieldValue>(
				new ArrayList<StructFieldValue>(0),
				labelProvider,
				this, 
				SWT.SINGLE,
				(String) null 
//				, LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.NONE
				);
		// TODO: this is a workaround for the problem of the FormToolkit painting the borders at the outside of the bounding box of the CComboComposite 
		GridLayout comboLayout = fieldValueCombo.getGridLayout();
		comboLayout.verticalSpacing = 2;
		comboLayout.marginWidth = 2;
		comboLayout.marginHeight = 2;
		
//		SelectionStructField field = (SelectionStructField) editor.getStructField();
		refresh();
//		fieldValueCombo.setInput(field.getStructFieldValues());
		
		GridData textData = new GridData(GridData.FILL_HORIZONTAL);
		textData.grabExcessHorizontalSpace = true;
		fieldValueCombo.setLayoutData(textData);
		this.modifyListener = modListener;
		fieldValueCombo.addModifyListener(modifyListener);
	}

	/**
	 * @see org.nightlabs.jfire.base.prop.edit.AbstractDataFieldComposite#refresh()
	 */
	public void refresh() {
		SelectionStructField field = (SelectionStructField) editor.getStructField();
		fieldName.setText(field.getName().getText(editor.getLanguage().getLanguageID()));
		fieldValueCombo.setItems(new ArrayList<StructFieldValue>(field.getStructFieldValues()));
		if (editor.getDataField().getStructFieldValueID() != null) {
			try {
				fieldValueCombo.selectElement(field.getStructFieldValue(editor.getDataField().getStructFieldValueID()));
			} catch (StructFieldValueNotFoundException e) {
				fieldValueCombo.select(-1);
				throw new RuntimeException("Could not find the referenced structFieldValue with id "+editor.getDataField().getStructFieldValueID());
			}
		} else {
			fieldValueCombo.select(-1);
		}
	}
	
	public CComboComposite<StructFieldValue> getFieldValueCombo() {
		return fieldValueCombo;
	}
	
	public void dispose() {
		fieldValueCombo.removeModifyListener(modifyListener);
		super.dispose();
	}
}
