package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.jfire.prop.structfield.DateStructField;
import org.nightlabs.jfire.prop.structfield.SelectionStructField;

public class DateStructFieldEditor extends AbstractStructFieldEditor<DateStructField> {
	public static class SelectionStructFieldEditorFactory extends AbstractStructFieldEditorFactory {
		public String getStructFieldEditorClass() {
			return DateStructFieldEditor.class.getName();
		}
	}

	private DateStructField dateField;
	private DateStructFieldEditComposite comp;

	public void setData(DateStructField field) {
		dateField = field;
		comp.setField(dateField);
	}

	@Override
	protected Composite createSpecialComposite(Composite parent, int style) {
		comp = new DateStructFieldEditComposite(parent, style, this);		
		return comp;
	}

	public String getValidationError() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean validateInput() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void setSpecialData(DateStructField field) {
		// TODO Auto-generated method stub
	}

	public void reset() {
		// TODO Auto-generated method stub
		
	}
}

class DateStructFieldEditComposite extends XComposite {
	private DateStructFieldEditor editor;
	private DateStructField regexField;
	private boolean ignoreModify;

	public DateStructFieldEditComposite(Composite parent, int style, DateStructFieldEditor editor) {
		super(parent, style, LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 2);
		this.editor = editor;

		GridData data = new GridData();

		Composite wrapper = new XComposite(this, SWT.NONE, LayoutMode.LEFT_RIGHT_WRAPPER, LayoutDataMode.GRID_DATA);

		wrapper = new XComposite(this, SWT.NONE, LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 2);

	}

	/**
	 * Sets the currently display field.
	 * 
	 * @param field
	 *          The {@link SelectionStructField} to be displayed.
	 */
	public void setField(DateStructField field) {
		regexField = field;

		if (regexField == null)
			return;
	}
}