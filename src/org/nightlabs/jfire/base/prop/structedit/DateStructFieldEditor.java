package org.nightlabs.jfire.base.prop.structedit;

import java.util.Arrays;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.composite.XComboComposite;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.jfire.prop.structfield.DateStructField;
import org.nightlabs.l10n.DateFormatter;

public class DateStructFieldEditor extends AbstractStructFieldEditor<DateStructField> {
	public static class DateStructFieldEditorFactory extends AbstractStructFieldEditorFactory {
		public String getStructFieldEditorClass() {
			return DateStructFieldEditor.class.getName();
		}
	}

	private DateStructFieldEditComposite comp;

	@Override
	protected Composite createSpecialComposite(Composite parent, int style) {
		comp = new DateStructFieldEditComposite(parent, style, this);
		return comp;
	}

	@Override
	protected void setSpecialData(DateStructField field) {
		comp.setField(field);
	}
}

class DateStructFieldEditComposite extends XComposite {
	private DateStructField dateField;
	private XComboComposite<String> dateFormatCombo;
	private Label exampleLabel;
	private XComposite comp;
	private DateStructFieldEditor dateStructFieldEditor;
	
	public DateStructFieldEditComposite(Composite parent, int style, DateStructFieldEditor editor) {
		super(parent, style | SWT.NONE, LayoutMode.LEFT_RIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);
		
		this.dateStructFieldEditor = editor;
		
		dateFormatCombo = new XComboComposite<String>(this, XComboComposite.getDefaultWidgetStyle(this), (String) null);
		
		comp = new XComposite(this, SWT.NONE, LayoutMode.LEFT_RIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);
		exampleLabel = new Label(comp, SWT.NONE);
		exampleLabel.setText("Preview:  ");
		
		dateFormatCombo.setInput( Arrays.asList(DateFormatter.FLAG_NAMES) );
		
		dateFormatCombo.addSelectionListener(new SelectionListener() {
			private void selectionChanged(SelectionEvent event) {
				int selectionIndex = dateFormatCombo.getSelectionIndex();
				dateField.setDateTimeEditFlags(DateFormatter.FLAGS[selectionIndex]);
				dateStructFieldEditor.setChanged();
				
				exampleLabel.setText("Preview:  " + DateFormatter.formatDate(new Date(), DateFormatter.FLAGS[selectionIndex]));
				exampleLabel.pack();
				exampleLabel.getParent().layout();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				selectionChanged(e);
			}

			public void widgetSelected(SelectionEvent e) {
				selectionChanged(e);
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