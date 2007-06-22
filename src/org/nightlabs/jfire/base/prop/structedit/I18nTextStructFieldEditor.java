package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.jfire.prop.structfield.I18nTextStructField;

public class I18nTextStructFieldEditor extends AbstractStructFieldEditor<I18nTextStructField> {
	
	public static class I18nTextStructFieldEditorFactory extends AbstractStructFieldEditorFactory {
		public String getStructFieldEditorClass() {
			return I18nTextStructFieldEditor.class.getName();
		}
	}

	private I18nTextStructField textField;
	private Spinner lineCountSpinner;
	
	@Override
	protected Composite createSpecialComposite(Composite parent, int style) {
		XComposite comp = new XComposite(parent, style);
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
	protected void setSpecialData(I18nTextStructField field) {
		this.textField = field;
		lineCountSpinner.setSelection(field.getLineCount());
	}
}
