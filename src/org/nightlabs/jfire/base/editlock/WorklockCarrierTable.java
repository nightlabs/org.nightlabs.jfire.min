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
import org.nightlabs.l10n.DateFormatter;

public class WorklockCarrierTable
extends AbstractTableComposite
{
	private static class WorklockCarrierLabelProvider extends TableLabelProvider
	{
		@Implement
		public String getColumnText(Object element, int columnIndex)
		{
			if (!(element instanceof WorklockCarrier)) {
				if (columnIndex == 0)
					return String.valueOf(element);

				return "";
			}

			WorklockCarrier worklockCarrier = (WorklockCarrier) element;

			switch (columnIndex) {
				case 0:
					return worklockCarrier.getWorklock().getDescription();
				case 1:
					return DateFormatter.formatDateShortTimeHMS(worklockCarrier.getWorklock().getCreateDT(), false);
				case 2:
					return DateFormatter.formatDateShortTimeHMS(worklockCarrier.getLastUserActivityDT(), false);
				default:
					return "";
			}
		}
	}

	public WorklockCarrierTable(Composite parent, int style)
	{
		super(parent, style);
	}

	@Implement
	protected void createTableColumns(TableViewer tableViewer, Table table)
	{
		TableColumn tc;

		tc = new TableColumn(table, SWT.LEFT);
		tc.setText("Description");

		tc = new TableColumn(table, SWT.LEFT);
		tc.setText("Created");

		tc = new TableColumn(table, SWT.LEFT);
		tc.setText("Last User Activity");

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
		tableViewer.setLabelProvider(new WorklockCarrierLabelProvider());
	}
}
