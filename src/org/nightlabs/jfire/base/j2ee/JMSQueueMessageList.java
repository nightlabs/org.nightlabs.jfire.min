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
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.base.ui.table.TableContentProvider;
import org.nightlabs.base.ui.table.TableLabelProvider;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.jfire.j2ee.monitor.J2EEServerMonitorManager;
import org.nightlabs.jfire.j2ee.monitor.J2EEServerMonitorManagerUtil;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class JMSQueueMessageList extends XComposite {

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
					return ""; //$NON-NLS-1$
				} else {
					if (colIdx == 0)
						return String.valueOf(element);
					return ""; //$NON-NLS-1$
				}
			}
			
		}
		
		@Override
		protected void createTableColumns(TableViewer tableViewer, Table table) {
			new TableColumn(table, SWT.LEFT).setText(Messages.getString("org.nightlabs.jfire.base.j2ee.JMSQueueMessageList.idTableColumn.text")); //$NON-NLS-1$
//			new TableColumn(table, SWT.LEFT).setText("Type");
			new TableColumn(table, SWT.LEFT).setText(Messages.getString("org.nightlabs.jfire.base.j2ee.JMSQueueMessageList.descriptionTableColumn.text")); //$NON-NLS-1$
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
	
	private JMSQueueCombo queuesCombo;
	private MessagesTable messagesTable;
	private XComposite summaryComposite;
	private Label summaryLabel;
	private boolean autoUpdate = false;
	private Timer updateTimer;
	
	/**
	 * @param parent
	 * @param style
	 */
	public JMSQueueMessageList(Composite parent, int style) {
		super(parent, style);
		queuesCombo = new JMSQueueCombo(this, SWT.NONE, Messages.getString("org.nightlabs.jfire.base.j2ee.JMSQueueMessageList.queueCombo.caption")); //$NON-NLS-1$
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
		Job loadJob = new Job(Messages.getString("org.nightlabs.jfire.base.j2ee.JMSQueueMessageList.loadJob.name")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						messagesTable.setInput(new String[] {Messages.getString("org.nightlabs.jfire.base.j2ee.JMSQueueMessageList.messagesTable.input_loading")}); //$NON-NLS-1$
					}
				});
				J2EEServerMonitorManager manager = J2EEServerMonitorManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
				final Collection<JMSMessageDescriptor> messages = manager.listQueueMessages(queueName);
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						messagesTable.setInput(messages);
						summaryLabel.setText(String.format(Messages.getString("org.nightlabs.jfire.base.j2ee.JMSQueueMessageList.summaryLabel.text"), messages.size())); //$NON-NLS-1$
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
