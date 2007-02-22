package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jfire.prop.StructField;

public class DefaultStructFieldEditor extends AbstractStructFieldEditor<StructField> {

	public static class DefaultStructFieldEditorFactory extends AbstractStructFieldEditorFactory {
		public String getStructFieldEditorClass() {
			return DefaultStructFieldEditor.class.getName();
		}
	}
	
	@Override
	protected Composite createSpecialComposite(Composite parent, int style) {
		return null;
	}

	@Override
	protected void setSpecialData(StructField field) {		
	}
}
