package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.composite.XComposite.LayoutMode;
import org.nightlabs.jfire.prop.structfield.TextStructField;

public class TextStructFieldEditor extends AbstractStructFieldEditor<TextStructField> {
	public static class TextStructFieldEditorFactory extends AbstractStructFieldEditorFactory {
		public String getStructFieldEditorClass() {
			return TextStructFieldEditor.class.getName();
		}
	}

	private TextStructField textField;
	private Spinner lineCountSpinner;
	
	@Override
	protected Composite createSpecialComposite(Composite parent, int style) {
		XComposite comp = new XComposite(parent, style, LayoutMode.TIGHT_WRAPPER);
		comp.getGridLayout().numColumns = 3;		
		new Label(comp, SWT.NONE).setText("Line count: ");
		lineCountSpinner = new Spinner(comp, comp.getBorderStyle());
		lineCountSpinner.setMinimum(1);
		lineCountSpinner.setIncrement(1);
		lineCountSpinner.setSelection(1);
		
		lineCountSpinner.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (textField.validateLineCount(lineCountSpinner.getSelection()))
					textField.setLineCount(lineCountSpinner.getSelection());
			}			
		});
		return comp;
	}

	@Override
	protected void setSpecialData(TextStructField field) {
		this.textField = field;
		lineCountSpinner.setSelection(field.getLineCount());
	}
}
