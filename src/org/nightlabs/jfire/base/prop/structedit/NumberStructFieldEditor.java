package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.jfire.prop.structfield.DateStructField;
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
}

class NumberStructFieldEditComposite extends XComposite {
	private NumberStructFieldEditor editor;
	private NumberStructField numberField;
	private Button boundedCheckbox;
	private Spinner minSpinner;
	private Spinner maxSpinner;
	private Spinner digitSpinner;
	private boolean ignoreModify;
	private Composite comp;
	private Composite boundsComp;

	public NumberStructFieldEditComposite(Composite parent, int style, NumberStructFieldEditor editor) {
		super(parent, style, LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);
		this.editor = editor;
		comp = createSpinnerComposite(this, true);
	}
	
	private void convertSpinner(Spinner spinner, int newDigits) {
		ignoreModify = true;
		int oldDigits = numberField.getDigits();
		double factor = Math.pow(10, newDigits - oldDigits);
		int value = spinner.getSelection();
		int increment = spinner.getIncrement();
		int newIncrement = (int) (increment * factor);
		int newValue = (int) (value * factor);
		
		spinner.setMaximum(Integer.MAX_VALUE);
		spinner.setMinimum(0);
			
		spinner.setIncrement(newIncrement);
		spinner.setDigits(newDigits);
		
		spinner.setSelection(newValue);
		ignoreModify = false;
	}
	
	private void convertNumberField(int newDigits) {
		int oldDigits = numberField.getDigits();
		double factor = Math.pow(10, newDigits - oldDigits);
		if (factor > 1) {
			numberField.setMax((int) (numberField.getMax() * factor));
			numberField.setMin((int) (numberField.getMin() * factor));
		} else {
			numberField.setMin((int) (numberField.getMin() * factor));
			numberField.setMax((int) (numberField.getMax() * factor));
		}
		numberField.setDigits(newDigits);
	}
	
	private void rearrange() {
		minSpinner.pack();
		maxSpinner.pack();
		boundsComp.layout();
	}
	
	private void setSpinnerBounds() {
		minSpinner.setMaximum(Math.max(minSpinner.getMinimum()+1, maxSpinner.getSelection()-1));
		maxSpinner.setMinimum(Math.min(maxSpinner.getMaximum()-1, minSpinner.getSelection()+1));
	}
	
	private Composite createSpinnerComposite(Composite parent, boolean integer) {
		
		comp = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 5);
		new Label(comp, SWT.NONE).setText("Digits: ");
		digitSpinner = new Spinner(comp, SWT.BORDER);
		digitSpinner.setMinimum(0);
		digitSpinner.setMaximum(5);
		digitSpinner.setSelection(0);
		digitSpinner.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (ignoreModify)
					return;
				
				int oldDigits = numberField.getDigits();
				int newDigits = digitSpinner.getSelection();

				if (newDigits == oldDigits)
					return;
				
				convertSpinner(maxSpinner, newDigits);
				convertSpinner(minSpinner, newDigits);				
				convertNumberField(newDigits);
				
				setSpinnerBounds();
				
				rearrange();
			}
		});
		
		new Label(comp, SWT.NONE).setText("   Bounded: ");
		boundedCheckbox = new Button(comp, SWT.CHECK);
		boundedCheckbox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				if (boundedCheckbox.getSelection()) {
					numberField.setBounded(true);
					boundsComp.setVisible(true);
				} else {
					numberField.setBounded(false);
					boundsComp.setVisible(false);
				}
			}
		});
		
		boundsComp = new XComposite(comp, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 4);
		
		new Label(boundsComp, SWT.NONE).setText(" Minimum: ");
		minSpinner = new Spinner(boundsComp, SWT.BORDER);
		new Label(boundsComp, SWT.NONE).setText("Maximum: ");
		maxSpinner = new Spinner(boundsComp, SWT.BORDER);
		
		minSpinner.setSelection(0);
		minSpinner.setMinimum(0);
		minSpinner.setDigits(0);
		
		maxSpinner.setSelection(10);
		maxSpinner.setMaximum(Integer.MAX_VALUE);
		maxSpinner.setDigits(0);
		
		setSpinnerBounds();
		
		minSpinner.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (ignoreModify)
					return;
				
				setSpinnerBounds();
				numberField.setMin(minSpinner.getSelection());
				rearrange();
				System.out.println(numberField);
			}
		});
		
		maxSpinner.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (ignoreModify)
					return;
				
				setSpinnerBounds();
				numberField.setMax(maxSpinner.getSelection());
				rearrange();
				System.out.println(numberField);
			}
		});
		
		boundsComp.setVisible(false);
		
		return comp;
	}
	
	private String getSpinnerInfo() {
		String bla = "MaxSpinner: selection = "+maxSpinner.getSelection()+" max = " + maxSpinner.getMaximum() + " min = " + maxSpinner.getMinimum() + " digits = " + maxSpinner.getDigits();
		bla += "\nMinSpinner: selection = "+minSpinner.getSelection()+" max = " + minSpinner.getMaximum() + " min = " + minSpinner.getMinimum() + " digits = " + minSpinner.getDigits();
		bla += "\nNumberField: " + numberField;
		return bla;
	}
	
	/**
	 * Sets the currently display field.
	 * 
	 * @param field The {@link DateStructField} to be displayed.
	 */
	public void setField(NumberStructField field) {
		if (field == null)
			return;
		
		numberField = field;
		ignoreModify = true;
		
		minSpinner.setMaximum(Integer.MAX_VALUE);
		maxSpinner.setMinimum(0);
		minSpinner.setSelection(numberField.getMin());
		maxSpinner.setSelection(numberField.getMax());
		
		setSpinnerBounds();
		
		if (!numberField.isInteger()) {
			digitSpinner.setSelection(numberField.getDigits());
			minSpinner.setDigits(numberField.getDigits());
			minSpinner.setIncrement((int) Math.pow(10, numberField.getDigits()));
			maxSpinner.setDigits(numberField.getDigits());
			maxSpinner.setIncrement((int) Math.pow(10, numberField.getDigits()));
		}
		ignoreModify = false;
		
		boundedCheckbox.setSelection(numberField.isBounded());
		boundsComp.setVisible(numberField.isBounded());
		
		rearrange();
	}
}