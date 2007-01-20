package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.jfire.prop.structfield.DateStructField;

public class DateStructFieldEditor extends AbstractStructFieldEditor<DateStructField> {
	public static class DateStructFieldEditorFactory extends AbstractStructFieldEditorFactory {
		public String getStructFieldEditorClass() {
			return DateStructFieldEditor.class.getName();
		}
	}

	private DateStructField dateField;
	private DateStructFieldEditComposite comp;

	@Override
	protected Composite createSpecialComposite(Composite parent, int style) {
		comp = new DateStructFieldEditComposite(parent, style, this);		
		return comp;
	}

	@Override
	protected void setSpecialData(DateStructField field) {
		dateField = field;
		comp.setField(field);
	}
}

class DateStructFieldEditComposite extends XComposite {
	private DateStructFieldEditor editor;
	private DateStructField dateField;

	public DateStructFieldEditComposite(Composite parent, int style, DateStructFieldEditor editor) {
		super(parent, style, LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 2);
		this.editor = editor;
		
		// TODO display some data to let the user define the appearance of the DateTimeEdit control in 
		// the DateDataFieldEditor
	}

	/**
	 * Sets the currently display field.
	 * 
	 * @param field The {@link DateStructField} to be displayed.
	 */
	public void setField(DateStructField field) {
		dateField = field;

		if (dateField == null)
			return;
	}
}