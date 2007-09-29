package org.nightlabs.jfire.base.prop.edit.blockbased;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.composite.AbstractListComposite;
import org.nightlabs.base.ui.composite.XComboComposite;
import org.nightlabs.jfire.base.prop.edit.AbstractInlineDataFieldComposite;
import org.nightlabs.jfire.prop.datafield.SelectionDataField;
import org.nightlabs.jfire.prop.exception.StructFieldValueNotFoundException;
import org.nightlabs.jfire.prop.structfield.SelectionStructField;
import org.nightlabs.jfire.prop.structfield.StructFieldValue;

public class SelectionDataFieldComposite extends AbstractInlineDataFieldComposite<SelectionDataFieldEditor, SelectionDataField> {

//	private Label fieldName;
	private XComboComposite<StructFieldValue> fieldValueCombo;
//	private SelectionDataFieldEditor editor;
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
		super(parent, style, editor);
		if (!(parent.getLayout() instanceof GridLayout))
			throw new IllegalArgumentException("Parent should have a GridLayout!"); //$NON-NLS-1$
		
//		this.editor = editor;
		
//		GridLayout layout = new GridLayout();
//		setLayout(layout);
//		layout.horizontalSpacing = 0;
//		layout.verticalSpacing = 0;
//		layout.marginHeight = 0;
//		layout.marginWidth = 0;
//		setLayout(createLayout());
		
//		GridData gridData = new GridData(GridData.FILL_BOTH);
//		setLayoutData(gridData);
		
//		fieldName = new Label(this, SWT.NONE);
//		GridData nameData = new GridData(GridData.FILL_HORIZONTAL);
//		nameData.grabExcessHorizontalSpace = true;
//		fieldName.setLayoutData(nameData);
		
		LabelProvider labelProvider = new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof StructFieldValue) {
					StructFieldValue value = (StructFieldValue) element;
					return value.getValueName().getText();					
				}
				return ""; //$NON-NLS-1$
			}
		};
		
		fieldValueCombo = new XComboComposite<StructFieldValue>(
				this, 
				AbstractListComposite.getDefaultWidgetStyle(this),
				(String) null,
				labelProvider,
				LayoutMode.TIGHT_WRAPPER
//				, LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.NONE
				);
		
//		SelectionStructField field = (SelectionStructField) editor.getStructField();
//		refresh();
//		fieldValueCombo.setInput(field.getStructFieldValues());
		
		GridData textData = new GridData(GridData.FILL_HORIZONTAL);
		textData.grabExcessHorizontalSpace = true;
		fieldValueCombo.setLayoutData(textData);
		this.modifyListener = modListener;
		fieldValueCombo.addModifyListener(modifyListener);
	}

	/**
	 * @see org.nightlabs.jfire.base.prop.edit.AbstractInlineDataFieldComposite#refresh()
	 */
	public void _refresh() {
		SelectionStructField field = (SelectionStructField) getEditor().getStructField();
//		fieldName.setText(field.getName().getText());
		fieldValueCombo.setInput( field.getStructFieldValues() );
		if (getEditor().getDataField().getStructFieldValueID() != null) {
			try {
				fieldValueCombo.selectElement(field.getStructFieldValue(getEditor().getDataField().getStructFieldValueID()));
			} catch (StructFieldValueNotFoundException e) {
				fieldValueCombo.selectElementByIndex(-1);
				throw new RuntimeException("Could not find the referenced structFieldValue with id "+getEditor().getDataField().getStructFieldValueID()); //$NON-NLS-1$
			}
		} else {
			fieldValueCombo.selectElementByIndex(-1);
		}
	}
	
	public XComboComposite<StructFieldValue> getFieldValueCombo() {
		return fieldValueCombo;
	}
	
	public void dispose() {
		fieldValueCombo.removeModifyListener(modifyListener);
		super.dispose();
	}
}
