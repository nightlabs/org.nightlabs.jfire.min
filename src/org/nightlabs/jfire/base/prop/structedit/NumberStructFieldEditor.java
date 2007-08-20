package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jfire.prop.structfield.NumberStructField;

public class NumberStructFieldEditor extends AbstractStructFieldEditor<NumberStructField> {
	public static class NumberStructFieldEditorFactory extends AbstractStructFieldEditorFactory {
		public String getStructFieldEditorClass() {
			return NumberStructFieldEditor.class.getName();
		}
	}

	private NumberStructField numberField;
	private NumberStructFieldEditComposite comp;

	@Override
	protected Composite createSpecialComposite(Composite parent, int style) {
		comp = new NumberStructFieldEditComposite(parent, style, this);		
		return comp;
	}

	@Override
	protected void setSpecialData(NumberStructField field) {
		comp.setField(field);
		this.numberField = field;
	}

	protected NumberStructField getNumberField()
	{
		return numberField;
	}
}