/**
 * 
 */
package org.nightlabs.jfire.base.j2ee;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.composite.CComboComposite;
import org.nightlabs.base.job.Job;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.j2ee.monitor.J2EEServerMonitorManager;
import org.nightlabs.jfire.j2ee.monitor.J2EEServerMonitorManagerUtil;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.CollectionUtil;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class JMSQeuesCombo extends CComboComposite<String> {

	/**
	 * @param types
	 * @param parent
	 * @param style
	 * @param caption
	 */
	public JMSQeuesCombo(Composite parent, int style, String caption) {
		super(CollectionUtil.array2ArrayList(new String[] {"Loading queues"}), parent, style, caption);
		Job loadJob = new Job("Loading queues") {
			@Override
			protected IStatus run(ProgressMonitor monitor) throws Exception {
				Login login = Login.getLogin();
				J2EEServerMonitorManager jsmm = J2EEServerMonitorManagerUtil.getHome(login.getInitialContextProperties()).create();
				final Collection<String> queues = jsmm.listQueues();
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						setInput(new ArrayList<String>(queues));
					}
				});
				return Status.OK_STATUS;				
			}
		};
		loadJob.schedule();
	}
}
