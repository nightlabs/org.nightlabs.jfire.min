package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.jfire.prop.structfield.RegexStructField;

public class RegexStructFieldEditor extends AbstractStructFieldEditor<RegexStructField> {
	public static class RegexStructFieldEditorFactory extends AbstractStructFieldEditorFactory {
		public String getStructFieldEditorClass() {
			return RegexStructFieldEditor.class.getName();
		}
	}

	private RegexStructField regexField;
	private RegexStructFieldEditComposite comp;

	public boolean validateInput() {
		return regexField.validateData(comp.regexTextField.getText());
	}

	public String getErrorMessage() {
		return regexField.getValidationError();
	}

	@Override
	protected Composite createSpecialComposite(Composite parent, int style) {
		comp = new RegexStructFieldEditComposite(parent, style, this);		
		return comp;
	}

	@Override
	protected void setSpecialData(RegexStructField field) {
		regexField = field;
		comp.setField(regexField);
		if (validateInput())
			comp.saveData();
	}
	
	@Override
	public void saveData() {
		comp.saveData();
	}
	
	@Override
	public void restoreData() {
		comp.restoreData();
	}
}

class RegexStructFieldEditComposite extends XComposite {
	private RegexStructFieldEditor editor;
	private RegexStructField regexField;
	protected Text regexTextField;
	private String regexOrig;

	public RegexStructFieldEditComposite(Composite parent, int style, RegexStructFieldEditor editor) {
		super(parent, style, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA);
		this.editor = editor;

		new Label(this, SWT.NONE).setText("Regular expression:");
		regexTextField = new Text(this, getBorderStyle());
		regexTextField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		regexTextField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (regexField.validateRegex(regexTextField.getText()))
					regexField.setRegex(regexTextField.getText());
				updateErrorLabel();
			}
		});
	}
	
	private void updateErrorLabel() {
		regexField.validateData(regexTextField.getText());
		editor.setErrorMessage(regexField.getValidationError());
	}

	protected void saveData() {
		if (regexField == null)
			return;
		
		regexOrig = regexField.getRegex();		
	}
	
	protected void restoreData() {
		regexField.setRegex(regexOrig);
	}

	/**
	 * Sets the currently display field.
	 * 
	 * @param field The {@link RegexStructField} to be displayed.
	 */
	public void setField(RegexStructField field) {
		
		regexField = field;
		if (regexField == null) {
			this.setEnabled(false);
			return;
		}		
		this.setEnabled(true);

		String patternStr = field.getRegex() == null ? "" : field.getRegex();		
		regexTextField.setText(patternStr);
	}
}