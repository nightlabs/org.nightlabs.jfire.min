/**
 * 
 */
package org.nightlabs.jfire.base.j2ee;

import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.composite.XComboComposite;
import org.nightlabs.base.ui.job.Job;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.jfire.j2ee.monitor.J2EEServerMonitorManager;
import org.nightlabs.jfire.j2ee.monitor.J2EEServerMonitorManagerUtil;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class JMSQueueCombo extends XComboComposite<String> {

	public JMSQueueCombo(Composite parent, int comboStyle, String caption) {
		super(parent, comboStyle, caption);
		Job loadJob = new Job(Messages.getString("org.nightlabs.jfire.base.j2ee.JMSQueueCombo.loadJob.name")) { //$NON-NLS-1$
			@SuppressWarnings("unchecked") //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				Login login = Login.getLogin();
				J2EEServerMonitorManager jsmm = J2EEServerMonitorManagerUtil.getHome(login.getInitialContextProperties()).create();
				final Collection<String> queues = jsmm.listQueues();
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						setInput(queues);
					}
				});
				return Status.OK_STATUS;				
			}
		};
		loadJob.schedule();
	}
}
