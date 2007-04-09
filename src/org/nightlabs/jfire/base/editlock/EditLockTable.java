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
import org.nightlabs.jfire.editlock.EditLock;
import org.nightlabs.l10n.DateFormatter;

public class EditLockTable
extends AbstractTableComposite
{
	private static class EditLockLabelProvider extends TableLabelProvider
	{
		@Implement
		public String getColumnText(Object element, int columnIndex)
		{
			if (!(element instanceof EditLock)) {
				if (columnIndex == 0)
					return String.valueOf(element);

				return "";
			}

			EditLock editLock = (EditLock) element;

			switch (columnIndex) {
				case 0:
					return editLock.getLockOwnerUser().getName();
				case 1:
					return DateFormatter.formatDateShortTimeHMS(editLock.getCreateDT(), false);
				case 2:
					return DateFormatter.formatDateShortTimeHMS(editLock.getLastAcquireDT(), false);
				default:
					return "";
			}
		}
	}

	public EditLockTable(Composite parent, int style)
	{
		super(parent, style);
	}

	@Implement
	protected void createTableColumns(TableViewer tableViewer, Table table)
	{
		TableColumn tc;

		tc = new TableColumn(table, SWT.LEFT);
		tc.setText("User");

		tc = new TableColumn(table, SWT.LEFT);
		tc.setText("Created");

		tc = new TableColumn(table, SWT.LEFT);
		tc.setText("Last Acquired");

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
		tableViewer.setLabelProvider(new EditLockLabelProvider());
	}
}
