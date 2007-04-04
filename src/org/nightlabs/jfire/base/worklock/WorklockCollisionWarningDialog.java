package org.nightlabs.jfire.base.worklock;

import java.util.List;

import javax.jdo.FetchPlan;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.dialog.CenteredDialog;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.worklock.AcquireWorklockResult;
import org.nightlabs.jfire.worklock.Worklock;

public class WorklockCollisionWarningDialog
extends CenteredDialog
{
	private AcquireWorklockResult acquireWorklockResult;

	private WorklockTable worklockTable;

	public WorklockCollisionWarningDialog(Shell parentShell, AcquireWorklockResult acquireWorklockResult)
	{
		super(parentShell);
		this.acquireWorklockResult = acquireWorklockResult;
	}

	private static final String[] FETCH_GROUPS_WORKLOCK = {
		FetchPlan.DEFAULT, Worklock.FETCH_GROUP_LOCK_OWNER
	};

	@Override
	protected Control createContents(Composite parent)
	{
		Composite page = (Composite) super.createContents(parent);

		new Label(page, SWT.WRAP).setText("The object that you are about to edit is currently edited by someone else, too. This might cause collisions and changes to get lost.");

		worklockTable = new WorklockTable(page, SWT.NONE);
		worklockTable.setInput(new String[] {"Loading data..."});

		Job job = new Job("Loading Worklocks") {
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				final List<Worklock> worklocks = WorklockDAO.sharedInstance().getWorklocks(
						acquireWorklockResult.getWorklock().getLockedObjectID(),
						FETCH_GROUPS_WORKLOCK,
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
						monitor);

				worklocks.remove(acquireWorklockResult.getWorklock()); // remove our own lock

				Display.getDefault().asyncExec(new Runnable()
				{
					public void run()
					{
						if (worklockTable.isDisposed())
							return;

						if (worklocks.isEmpty()) { // in the mean time (while opening the dialog) the other worklock was released - close the dialog immediately
							close();
							return;
						}

						worklockTable.setInput(worklocks);
					}
				});

				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.SHORT);
		job.schedule();

		return page;
	}
}
