package org.nightlabs.jfire.base.prop.structedit;

import java.util.Arrays;
import java.util.Date;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
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
	private DateStructField dateField;
	private ComboComposite<String> dateFormatCombo;
	private Label exampleLabel;
	private XComposite comp;
	private DateStructFieldEditor dateStructFieldEditor;
	
	public DateStructFieldEditComposite(Composite parent, int style, DateStructFieldEditor editor) {
		super(parent, style | SWT.NONE, LayoutMode.LEFT_RIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);
		
		this.dateStructFieldEditor = editor;
		
		dateFormatCombo = new ComboComposite<String>(this, SWT.NONE);
		System.out.println(dateFormatCombo.getGridData().widthHint);
		comp = new XComposite(this, SWT.NONE, LayoutMode.LEFT_RIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);
		exampleLabel = new Label(comp, SWT.NONE);
		exampleLabel.setText("Preview:  ");
		
		dateFormatCombo.setInput(Arrays.asList(DateFormatter.FLAG_NAMES));
		
		dateFormatCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				int selectionIndex = dateFormatCombo.getSelectionIndex();
				dateField.setDateTimeEditFlags(DateFormatter.FLAGS[selectionIndex]);
				dateStructFieldEditor.setChanged();
				
				exampleLabel.setText("Preview:  " + DateFormatter.formatDate(new Date(), DateFormatter.FLAGS[selectionIndex]));
				exampleLabel.pack();
				exampleLabel.getParent().layout();
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
		exampleLabel.setText("Preview:  " + DateFormatter.formatDate(new Date(), dateField.getDateTimeEditFlags()));
		exampleLabel.pack();
		exampleLabel.getParent().layout();			
	}
}