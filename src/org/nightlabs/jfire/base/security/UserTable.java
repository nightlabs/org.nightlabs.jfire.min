package org.nightlabs.jfire.base.security;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.nightlabs.base.layout.WeightedTableLayout;
import org.nightlabs.base.table.AbstractTableComposite;
import org.nightlabs.base.table.TableContentProvider;
import org.nightlabs.base.table.TableLabelProvider;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.jfire.security.User;
import org.nightlabs.l10n.DateFormatter;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class UserTable 
extends AbstractTableComposite<User>
{
	/**
	 * @param parent
	 * @param style
	 */
	public UserTable(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * @param parent
	 * @param style
	 * @param initTable
	 */
	public UserTable(Composite parent, int style, boolean initTable) {
		super(parent, style, initTable);
	}

	/**
	 * @param parent
	 * @param style
	 * @param initTable
	 * @param viewerStyle
	 */
	public UserTable(Composite parent, int style, boolean initTable,
			int viewerStyle) {
		super(parent, style, initTable, viewerStyle);
	}

	@Override
	protected void createTableColumns(TableViewer tableViewer, Table table) 
	{
		new TableColumn(table, SWT.LEFT).setText(Messages.getString("security.UserTable.userID")); //$NON-NLS-1$
		new TableColumn(table, SWT.LEFT).setText(Messages.getString("security.UserTable.name")); //$NON-NLS-1$
		new TableColumn(table, SWT.LEFT).setText(Messages.getString("security.UserTable.description")); //$NON-NLS-1$
		new TableColumn(table, SWT.LEFT).setText(Messages.getString("security.UserTable.userType")); //$NON-NLS-1$
		new TableColumn(table, SWT.LEFT).setText(Messages.getString("security.UserTable.changeDT")); //$NON-NLS-1$
		
		table.setLayout(new WeightedTableLayout(new int[] {10, 10, 10, 10, 10}));
	}

	@Override
	protected void setTableProvider(TableViewer tableViewer) {
		tableViewer.setContentProvider(new TableContentProvider());
		tableViewer.setLabelProvider(new UserTableLabelProvider());
	}

	class UserTableLabelProvider extends TableLabelProvider 
	{
		public UserTableLabelProvider() {
			super();
		}

		public String getColumnText(Object element, int columnIndex) 
		{
			if (element instanceof User) {
				User user = (User) element;
				switch (columnIndex) {
					case(0):
						return user.getUserID();
					case(1):
						return user.getName();
					case(2):
						return user.getDescription();
					case(3):
						return user.getUserType();
					case(4):
						return DateFormatter.formatDateShort(user.getChangeDT(), false);
				}
			}
			return null;
		}		
	}
	
}
