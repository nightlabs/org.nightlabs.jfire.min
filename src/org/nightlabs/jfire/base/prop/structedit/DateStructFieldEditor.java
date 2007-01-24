package org.nightlabs.jfire.base.prop.structedit;

import java.util.Arrays;
import java.util.Date;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.composite.ComboComposite;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.jfire.prop.structfield.DateStructField;
import org.nightlabs.l10n.DateFormatter;

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
		comp = new DateStructFieldEditComposite(parent, style);
		return comp;
	}

	@Override
	protected void setSpecialData(DateStructField field) {
		dateField = field;
		comp.setField(field);
	}
}

class DateStructFieldEditComposite extends XComposite {
	private DateStructField dateField;
	private ComboComposite<String> dateFormatCombo;
	private Label exampleLabel;
	
	public DateStructFieldEditComposite(Composite parent, int style) {
		super(parent, style | SWT.NONE, LayoutMode.LEFT_RIGHT_WRAPPER, LayoutDataMode.NONE);
		
		dateFormatCombo = new ComboComposite<String>(this, SWT.NONE);
		System.out.println(dateFormatCombo.getGridData().widthHint);
		new Label(this, SWT.NONE);
		exampleLabel = new Label(this, SWT.NONE);
		exampleLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		dateFormatCombo.setInput(Arrays.asList(DateFormatter.FLAG_NAMES));
		
		dateFormatCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				int selectionIndex = dateFormatCombo.getSelectionIndex();
				dateField.setDateTimeEditFlags(DateFormatter.FLAGS[selectionIndex]);
				
				exampleLabel.setText("Preview:   " + DateFormatter.formatDate(new Date(), DateFormatter.FLAGS[selectionIndex]));
			}
		});
	}

	/**
	 * Sets the currently display field.
	 * 
	 * @param field The {@link DateStructField} to be displayed.
	 */
	public void setField(DateStructField field) {
		if (field == null)
			return;
		
		dateField = field;
		int index = 0;
		
		while (index < DateFormatter.FLAGS.length && DateFormatter.FLAGS[index] != dateField.getDateTimeEditFlags()) {
			index++;
		}
		
		dateFormatCombo.setSelection(index);
		exampleLabel.setText("Preview:   " + DateFormatter.formatDate(new Date(), DateFormatter.FLAGS[index]));
		exampleLabel.pack();
	}
}