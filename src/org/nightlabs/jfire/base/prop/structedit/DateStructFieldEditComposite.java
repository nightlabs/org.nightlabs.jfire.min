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
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.jfire.prop.structfield.DateStructField;
import org.nightlabs.l10n.DateFormatter;

class DateStructFieldEditComposite extends XComposite {
	private DateStructField dateField;
	private XComboComposite<String> dateFormatCombo;
	private Label exampleLabel;
	private XComposite comp;
	private DateStructFieldEditor dateStructFieldEditor;

	private void updateExampleLabelText() {
		exampleLabel.setText(
				String.format(Messages.getString("org.nightlabs.jfire.base.prop.structedit.DateStructFieldEditComposite.exampleLabel.text"), //$NON-NLS-1$
						new Object[] { dateField == null ? "" : DateFormatter.formatDate(new Date(), dateField.getDateTimeEditFlags()) })); //$NON-NLS-1$
		exampleLabel.pack();
		exampleLabel.getParent().layout();
	}

	public DateStructFieldEditComposite(Composite parent, int style, DateStructFieldEditor _dateStructFieldEditor) {
		super(parent, style | SWT.NONE, LayoutMode.LEFT_RIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);

		this.dateStructFieldEditor = _dateStructFieldEditor;

		dateFormatCombo = new XComboComposite<String>(this, XComboComposite.getDefaultWidgetStyle(this), (String) null);

		comp = new XComposite(this, SWT.NONE, LayoutMode.LEFT_RIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL);
		exampleLabel = new Label(comp, SWT.NONE);
		updateExampleLabelText();

		dateFormatCombo.setInput( Arrays.asList(DateFormatter.FLAG_NAMES) );

		dateFormatCombo.addSelectionListener(new SelectionListener() {
			private void selectionChanged(SelectionEvent event) {
				int selectionIndex = dateFormatCombo.getSelectionIndex();
				dateField.setDateTimeEditFlags(DateFormatter.FLAGS[selectionIndex]);
				dateStructFieldEditor.setChanged();

				updateExampleLabelText();
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
	 * @param field The {@link DateStructField} to be displayed. Can be null.
	 */
	public void setField(DateStructField field) {
//		if (field == null) // this is bad practice, imho. Either throw an exception or set it and support it. I'll support it now ;-) Marco.
//			return;

		dateField = field;

		if (dateField == null)
			dateFormatCombo.setSelection(-1);
		else {
			int index = 0;
			while (index < DateFormatter.FLAGS.length && DateFormatter.FLAGS[index] != dateField.getDateTimeEditFlags()) {
				index++;
			}
			dateFormatCombo.setSelection(index);
		}

		updateExampleLabelText();
	}
}