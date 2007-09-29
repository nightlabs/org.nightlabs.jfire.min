/**
 * 
 */
package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.jfire.prop.structfield.DateStructField;
import org.nightlabs.jfire.prop.structfield.NumberStructField;

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
		super(parent, style, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);
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
			numberField.setSpinnerMax((int) (numberField.getSpinnerMax() * factor));
			numberField.setSpinnerMin((int) (numberField.getSpinnerMin() * factor));
		} else {
			numberField.setSpinnerMin((int) (numberField.getSpinnerMin() * factor));
			numberField.setSpinnerMax((int) (numberField.getSpinnerMax() * factor));
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
		new Label(comp, SWT.NONE).setText(Messages.getString("org.nightlabs.jfire.base.prop.structedit.NumberStructFieldEditComposite.digitsLabel.text")); //$NON-NLS-1$
		digitSpinner = new Spinner(comp, getBorderStyle());
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
				editor.setChanged();
				rearrange();
			}
		});
		
		new Label(comp, SWT.NONE).setText(Messages.getString("org.nightlabs.jfire.base.prop.structedit.NumberStructFieldEditComposite.boundedLabel.text")); //$NON-NLS-1$
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
				editor.setChanged();
			}
		});
		
		boundsComp = new XComposite(comp, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 4);
		
		new Label(boundsComp, SWT.NONE).setText(Messages.getString("org.nightlabs.jfire.base.prop.structedit.NumberStructFieldEditComposite.minimumLabel.text")); //$NON-NLS-1$
		minSpinner = new Spinner(boundsComp, SWT.BORDER);
		new Label(boundsComp, SWT.NONE).setText(Messages.getString("org.nightlabs.jfire.base.prop.structedit.NumberStructFieldEditComposite.maximumLabel.text")); //$NON-NLS-1$
		maxSpinner = new Spinner(boundsComp, SWT.BORDER);
		minSpinner.setLayoutData(new GridData());
		((GridData) minSpinner.getLayoutData()).widthHint = 80;
		maxSpinner.setLayoutData(new GridData());
		((GridData) maxSpinner.getLayoutData()).widthHint = 80;
		
//		minSpinner.addModifyListener(new ModifyListener() {
//			public void modifyText(ModifyEvent e) {
//				if (ignoreModify)
//					return;
//				
//				setSpinnerBounds();
//				numberField.setSpinnerMin(minSpinner.getSelection());
//				rearrange();
//			}
//		});
		
		minSpinner.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {}
			public void focusLost(FocusEvent e) {
				if (ignoreModify)
					return;
				
				setSpinnerBounds();
				numberField.setSpinnerMin(minSpinner.getSelection());
				editor.setChanged();
				rearrange();
			}
		});
		
//		maxSpinner.addModifyListener(new ModifyListener() {
//			public void modifyText(ModifyEvent e) {
//				if (ignoreModify)
//					return;
//				
//				setSpinnerBounds();
//				numberField.setSpinnerMax(maxSpinner.getSelection());
//				rearrange();
//			}
//		});
		
		maxSpinner.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {}
			public void focusLost(FocusEvent e) {
				if (ignoreModify)
					return;
				
				setSpinnerBounds();
				numberField.setSpinnerMax(maxSpinner.getSelection());
				editor.setChanged();
				rearrange();
			}
		});
		
		boundsComp.setVisible(false);
		
		return comp;
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
		minSpinner.setMinimum(0);
		maxSpinner.setMaximum(Integer.MAX_VALUE);
		maxSpinner.setMinimum(0);
		minSpinner.setSelection(numberField.getSpinnerMin());
		maxSpinner.setSelection(numberField.getSpinnerMax());
		
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