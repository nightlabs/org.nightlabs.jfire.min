/**
 * 
 */
package org.nightlabs.jfire.base.j2ee;

import java.util.Collection;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.job.Job;
import org.nightlabs.base.table.AbstractTableComposite;
import org.nightlabs.base.table.TableContentProvider;
import org.nightlabs.base.table.TableLabelProvider;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.j2ee.monitor.J2EEServerMonitorManager;
import org.nightlabs.jfire.j2ee.monitor.J2EEServerMonitorManagerUtil;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ListJMSQueuesComposite extends XComposite {

	public static class MessagesTable extends AbstractTableComposite<JMSMessageDescriptor> {

		public MessagesTable(Composite parent, int style) {
			super(parent, style);
		}

		public class LabelProvider extends TableLabelProvider {

			public String getColumnText(Object element, int colIdx) {
				if (element instanceof JMSMessageDescriptor) {
					JMSMessageDescriptor msg = (JMSMessageDescriptor) element;
					switch (colIdx) {
						case 0: return msg.getJMSMessageID();
//						case 1: return msg.getJMSType();
						case 1: return msg.getDescription();
					}
					return "";
				} else {
					if (colIdx == 0)
						return String.valueOf(element);
					return "";
				}
			}
			
		}
		
		@Override
		protected void createTableColumns(TableViewer tableViewer, Table table) {
			new TableColumn(table, SWT.LEFT).setText("ID");
//			new TableColumn(table, SWT.LEFT).setText("Type");
			new TableColumn(table, SWT.LEFT).setText("Description");
			TableLayout l = new TableLayout();
			l.addColumnData(new ColumnWeightData(1));
//			l.addColumnData(new ColumnWeightData(1));
			l.addColumnData(new ColumnWeightData(3));
			table.setLayout(l);
		}

		@Override
		protected void setTableProvider(TableViewer tableViewer) {
			tableViewer.setContentProvider(new TableContentProvider());
			tableViewer.setLabelProvider(new LabelProvider());
		}
	}
	
	private JMSQeuesCombo queuesCombo;
	private MessagesTable messagesTable;
	private XComposite summaryComposite;
	private Label summaryLabel;
	private boolean autoUpdate = false;
	private Timer updateTimer;
	
	/**
	 * @param parent
	 * @param style
	 */
	public ListJMSQueuesComposite(Composite parent, int style) {
		super(parent, style);
		queuesCombo = new JMSQeuesCombo(this, SWT.NONE, "Messages");
		queuesCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				String queueName = queuesCombo.getSelectedElement();
				loadQueueMessaged(queueName);
			}
		});
		messagesTable = new MessagesTable(this, SWT.NONE);
		summaryComposite = new XComposite(this, SWT.NONE);
		summaryComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		summaryLabel = new Label(summaryComposite, SWT.WRAP);
		summaryLabel.setLayoutData(new GridData(GridData.FILL_BOTH));
	}
	
	private void loadQueueMessaged(final String queueName) {
		Job loadJob = new Job("Loading Messages") {
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						messagesTable.setInput(new String[] {"loading ..."});
					}
				});
				J2EEServerMonitorManager manager = J2EEServerMonitorManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
				final Collection<JMSMessageDescriptor> messages = manager.listQueueMessages(queueName);
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						messagesTable.setInput(messages);
						summaryLabel.setText(String.format("%d messages in queue", messages.size()));
					}
				});
				return Status.OK_STATUS;
			}
		};
		loadJob.schedule();
	}
	
	private class UpdateTimerTask extends TimerTask {
		@Override
		public void run() {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					if (queuesCombo.getSelectedElement() != null) 
						loadQueueMessaged(queuesCombo.getSelectedElement());
				}
			});
		}
	}
	
	public void setAutoUpdate(boolean doAutoUpdate) {
		if (doAutoUpdate == autoUpdate)
			return;
		this.autoUpdate = doAutoUpdate;
		if (!doAutoUpdate) {
			if (updateTimer != null) {
				updateTimer.cancel();
				updateTimer = null;
			}
		} else {
			updateTimer = new Timer();
			updateTimer.scheduleAtFixedRate(new UpdateTimerTask(), new Date(), 10 * 1000);
		}
	}
	
	public boolean isAutoUpdate() {
		return autoUpdate;
	}

}
