package org.nightlabs.jfire.base.editlock;

import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.nightlabs.annotation.Implement;
import org.nightlabs.base.table.AbstractTableComposite;
import org.nightlabs.base.table.TableContentProvider;
import org.nightlabs.base.table.TableLabelProvider;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.l10n.DateFormatter;

public class EditLockCarrierTable
extends AbstractTableComposite
{
	private static class EditLockCarrierLabelProvider extends TableLabelProvider
	{
		@Implement
		public String getColumnText(Object element, int columnIndex)
		{
			if (!(element instanceof EditLockCarrier)) {
				if (columnIndex == 0)
					return String.valueOf(element);

				return ""; //$NON-NLS-1$
			}

			EditLockCarrier editLockCarrier = (EditLockCarrier) element;

			switch (columnIndex) {
				case 0:
					return editLockCarrier.getEditLock().getDescription();
				case 1:
					return DateFormatter.formatDateShortTimeHMS(editLockCarrier.getEditLock().getCreateDT(), false);
				case 2:
					return DateFormatter.formatDateShortTimeHMS(editLockCarrier.getLastUserActivityDT(), false);
				default:
					return ""; //$NON-NLS-1$
			}
		}
	}

	public EditLockCarrierTable(Composite parent, int style)
	{
		super(parent, style);
	}

	@Implement
	protected void createTableColumns(TableViewer tableViewer, Table table)
	{
		TableColumn tc;

		tc = new TableColumn(table, SWT.LEFT);
		tc.setText(Messages.getString("editlock.EditLockCarrierTable.description")); //$NON-NLS-1$

		tc = new TableColumn(table, SWT.LEFT);
		tc.setText(Messages.getString("editlock.EditLockCarrierTable.created")); //$NON-NLS-1$

		tc = new TableColumn(table, SWT.LEFT);
		tc.setText(Messages.getString("editlock.EditLockCarrierTable.lastActivity")); //$NON-NLS-1$

		TableLayout tl = new TableLayout();
		tl.addColumnData(new ColumnWeightData(1));
		tl.addColumnData(new ColumnPixelData(120));
		tl.addColumnData(new ColumnPixelData(120));
		table.setLayout(tl);
	}

	@Implement
	protected void setTableProvider(TableViewer tableViewer)
	{
		tableViewer.setContentProvider(new TableContentProvider());
		tableViewer.setLabelProvider(new EditLockCarrierLabelProvider());
	}
}
