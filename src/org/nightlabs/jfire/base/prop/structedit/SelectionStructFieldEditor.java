package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.ListComposite;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.language.LanguageChangeEvent;
import org.nightlabs.base.ui.language.LanguageChangeListener;
import org.nightlabs.base.ui.language.LanguageChooser;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.jfire.prop.i18n.StructFieldValueName;
import org.nightlabs.jfire.prop.structfield.SelectionStructField;
import org.nightlabs.jfire.prop.structfield.StructFieldValue;
import org.nightlabs.language.LanguageCf;

public class SelectionStructFieldEditor extends AbstractStructFieldEditor<SelectionStructField> {
	public static class SelectionStructFieldEditorFactory extends AbstractStructFieldEditorFactory {
		public String getStructFieldEditorClass() {
			return SelectionStructFieldEditor.class.getName();
		}
	}

	private SelectionStructField selectionField;
	private SelectionStructFieldEditComposite comp;

	@Override
	protected Composite createSpecialComposite(Composite parent, int style) {
		comp = new SelectionStructFieldEditComposite(parent, style, this, getLanguageChooser());
		return comp;
	}

	@Override
	public void setSpecialData(SelectionStructField field) {
		selectionField = field;
		comp.setField(selectionField);
	}
}

class SelectionStructFieldEditComposite extends XComposite implements LanguageChangeListener {
	private SelectionStructFieldEditor editor;
	private ListComposite<StructFieldValueName> valueList;
	private SelectionStructField selectionField;
	private LanguageCf currLanguage;
	private Button addValueButton;
	private Button remValueButton;
	private Text valueText;
	private boolean ignoreModify;

	private class MyLabelProvider extends LabelProvider {
		public String getText(Object element) {
			if (element instanceof StructFieldValueName) {
				StructFieldValueName valueName = (StructFieldValueName) element;
				return valueName.getText(currLanguage.getLanguageID());
			}
			return ""; //$NON-NLS-1$
		}
	}

	public SelectionStructFieldEditComposite(Composite parent, int style, SelectionStructFieldEditor editor,
			LanguageChooser langChooser) {
		super(parent, style, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA, 2);
		getGridLayout().horizontalSpacing = 2;
		
		currLanguage = langChooser.getLanguage();
		langChooser.addLanguageChangeListener(this);
		this.editor = editor;

		GridData data = new GridData();

		XComposite wrapper = new XComposite(this, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA);
		new Label(wrapper, SWT.NONE).setText(Messages.getString("org.nightlabs.jfire.base.prop.structedit.SelectionStructFieldEditor.possibleValuesLabel.text")); //$NON-NLS-1$
		valueList = new ListComposite<StructFieldValueName>(wrapper, ListComposite.getDefaultWidgetStyle(this));
		valueList.setLabelProvider(new MyLabelProvider());
		valueList.setLayoutData(new GridData(GridData.FILL_BOTH));
		valueList.getList().addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				updateTextBox();
			}
		});
		wrapper.layout(true, true);

		wrapper = new XComposite(this, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 2);
		addValueButton = new Button(wrapper, SWT.NONE);
		addValueButton.setText(Messages.getString("org.nightlabs.jfire.base.prop.structedit.SelectionStructFieldEditor.addValueButton.text")); //$NON-NLS-1$
		data.widthHint = 30;
		addValueButton.setLayoutData(data);
		addValueButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				addNewValue();
			}
		});

		valueText = new Text(wrapper, getBorderStyle());
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		data.verticalSpan = 2;
		data.verticalAlignment = SWT.CENTER;
		valueText.setLayoutData(data);
		valueText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!ignoreModify)
					updateSelectedValueText();
			}
		});
		valueText.setEnabled(false);

		remValueButton = new Button(wrapper, SWT.NONE);
		remValueButton.setText(Messages.getString("org.nightlabs.jfire.base.prop.structedit.SelectionStructFieldEditor.removeValueButton.text")); //$NON-NLS-1$
		data = new GridData();
		data.widthHint = 30;
		remValueButton.setLayoutData(data);
		remValueButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				removeValue();
			}
		});
	}

	/**
	 * Updates the contents of the text box <code>valueText</code> with the
	 * currently in the <code>valueList</code> selected
	 * {@link StructFieldValueName}.
	 */
	private void updateTextBox() {
		StructFieldValueName valueName = valueList.getSelectedElement();
		if (valueName == null)
			return;

		ignoreModify = true;
		valueText.setText(valueName.getText(currLanguage.getLanguageID()));
		ignoreModify = false;
		valueText.setEnabled(true);

		valueText.setSelection(valueText.getText().length());
		valueText.setSelection(0, valueText.getText().length());
		valueText.setFocus();
	}

	/**
	 * Updates the text of the currently selected item in the
	 * <code>valueList</code> with the text in the text box
	 * <code>valueText</code>.
	 */
	private void updateSelectedValueText() {
		String newValueText = valueText.getText();
		StructFieldValueName valueName = valueList.getSelectedElement();
		valueName.setText(currLanguage.getLanguageID(), newValueText);
		valueList.refreshElement(valueName);
		editor.getStructEditor().setChanged(true);
	}

	/**
	 * Adds a new value to the list and also to the structure.
	 */
	private void addNewValue() {
		StructFieldValue value = selectionField.newStructFieldValue();
		StructFieldValueName valueName = value.getValueName();
		valueName.setText(currLanguage.getLanguageID(), Messages.getString("org.nightlabs.jfire.base.prop.structedit.SelectionStructFieldEditor.newValue.text")); //$NON-NLS-1$

		valueList.addElement(valueName);
		valueList.selectElement(valueName);
		updateTextBox();
		editor.getStructEditor().setChanged(true);
	}

	/**
	 * Removes the currently selected value from the list and also from the
	 * structure.
	 */
	private void removeValue() {
		StructFieldValue toRemove = valueList.removeSelected().getStructFieldValue();
		SelectionStructField field = toRemove.getStructField();
		field.removeStructFieldValue(toRemove);
		updateTextBox();
		editor.getStructEditor().setChanged(true);
	}

	/**
	 * Sets the currently display field.
	 * 
	 * @param field
	 *          The {@link SelectionStructField} to be displayed.
	 */
	public void setField(SelectionStructField field) {
		selectionField = field;

		if (selectionField == null)
			return;

		valueList.removeAll();

		for (StructFieldValue value : selectionField.getStructFieldValues())
			valueList.addElement(value.getValueName());
	}

	/**
	 * @see LanguageChangeListener#languageChanged(LanguageChangeEvent)
	 */
	public void languageChanged(LanguageChangeEvent event) {
		currLanguage = event.getNewLanguage();
		if (!valueList.isDisposed())
			valueList.refresh();
	}

}