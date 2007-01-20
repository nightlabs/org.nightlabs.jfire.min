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

	public RegexStructFieldEditor() {
	}

	public boolean validateInput() {
		return regexField.validateData(comp.pattern.getText(), comp.displayText.getText());
	}

	public String getValidationError() {
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
	protected Text pattern;
	protected Text displayText;
	private String patternOrig, displayTextOrig;

	public RegexStructFieldEditComposite(Composite parent, int style, RegexStructFieldEditor editor) {
		super(parent, style, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 2);
		this.editor = editor;

		Composite wrapper = new XComposite(this, SWT.NONE, LayoutMode.ORDINARY_WRAPPER,
				LayoutDataMode.GRID_DATA_HORIZONTAL, 2);
		new Label(wrapper, SWT.NONE).setText("Regular expression:");
		pattern = new Text(wrapper, SWT.BORDER);
		pattern.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(wrapper, SWT.NONE).setText("Display text:");
		displayText = new Text(wrapper, SWT.BORDER);
		displayText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		pattern.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (regexField.validatePattern(pattern.getText()))
					regexField.setPattern(pattern.getText());
				updateErrorLabel();
			}
		});

		displayText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (regexField.validateDisplayText(displayText.getText()))
					regexField.setDisplayText(displayText.getText());
				updateErrorLabel();
			}
		});
		new Label(wrapper, SWT.NONE);
	}
	
	private void updateErrorLabel() {
		regexField.validateData(pattern.getText(), displayText.getText());
		editor.setErrorMessage(regexField.getValidationError());
	}

	protected void saveData() {
		if (regexField == null)
			return;
		
		patternOrig = regexField.getPattern();
		displayTextOrig = regexField.getDisplayText();
	}
	
	protected void restoreData() {
		regexField.setPattern(patternOrig);
		regexField.setDisplayText(displayTextOrig);
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

		String patternStr = field.getPattern() == null ? "" : field.getPattern();
		String displayTextStr = field.getDisplayText() == null ? "" : field.getDisplayText();
		pattern.setText(patternStr);
		displayText.setText(displayTextStr);
	}
}