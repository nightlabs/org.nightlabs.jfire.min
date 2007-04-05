package org.nightlabs.jfire.base.worklock;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.annotation.Implement;
import org.nightlabs.base.dialog.CenteredDialog;
import org.nightlabs.base.util.RCPUtil;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.worklock.AcquireWorklockResult;
import org.nightlabs.jfire.worklock.Worklock;
import org.nightlabs.jfire.worklock.WorklockType;
import org.nightlabs.jfire.worklock.id.WorklockID;
import org.nightlabs.jfire.worklock.id.WorklockTypeID;

import sun.security.krb5.internal.ac;

public class WorklockMan
{
	private static WorklockMan sharedInstance = null;
	private WorklockDAO worklockDAO = WorklockDAO.sharedInstance();

	public static WorklockMan sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (WorklockMan.class) {
				if (sharedInstance == null)
					sharedInstance = new WorklockMan();
			}
		}
		return sharedInstance;
	}

//	private Thread worklockRefreshThread = new Thread() {
//		@Override
//		public void run()
//		{
//			// TODO track what needs to be refreshed and pop up a warn dialog
//		}
//	};

	private class WorklockRefreshJob extends Job
	{
		private WorklockID worklockID;
		private Worklock worklock;

		public WorklockRefreshJob(Worklock worklock)
		{
			super("Refresh Worklocks");
			this.worklock = worklock;
			this.worklockID = (WorklockID) JDOHelper.getObjectId(worklock);
		}

		public WorklockID getWorklockID()
		{
			return worklockID;
		}
		public Worklock getWorklock()
		{
			return worklock;
		}

		@Implement
		protected IStatus run(IProgressMonitor monitor)
		{
			Worklock newWorklock = WorklockDAO.sharedInstance().getWorklock(worklockID, FETCH_GROUPS_WORKLOCK, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
			if (!newWorklock.getLastUseDT().equals(worklock.getLastUseDT())) {
				// the lock has been refreshed
				synchronized (worklockID2Job) {
					if (worklockID2Job.get(worklockID) != this)
						return Status.CANCEL_STATUS; // a new job has been created and this one can be removed

					createJob(newWorklock);
					return Status.CANCEL_STATUS;
				} // synchronized (worklockID2Job) {
			} // if (!worklock.equals(worklock.getLastUseDT())) {

			Display.getDefault().asyncExec(new Runnable()
			{
				public void run()
				{
					if (worklockAboutToExpireDialog == null) {
						worklockAboutToExpireDialog = new WorklockAboutToExpireDialog(RCPUtil.getActiveWorkbenchShell());
						worklockAboutToExpireDialog.setBlockOnOpen(false);
						worklockAboutToExpireDialog.open();
					}

					// add the Worklock to the dialog TODO implement
				}
			});

			return Status.OK_STATUS;
		}

		public void scheduleWithWorklockTypeDelay()
		{
			WorklockType worklockType = worklock.getWorklockType();
			long expiryTimestamp = worklock.getLastUseDT().getTime() + worklockType.getWorklockExpiryMSec();
			// TODO we should somehow handle time differences between client and server... is this UTC here?
			long delay = expiryTimestamp - System.currentTimeMillis();
			long reduction = delay / 4;
			reduction = Math.max(reduction, 3L * 60L * 1000L); // 3 minutes before TODO should be configurable!
			delay = delay - reduction;
			if (delay < 0) delay = 0;
			schedule(delay);
		}
	}

	private WorklockAboutToExpireDialog worklockAboutToExpireDialog = null;

	private Map<WorklockID, WorklockRefreshJob> worklockID2Job = new HashMap<WorklockID, WorklockRefreshJob>();

	public WorklockMan()
	{
//		worklockRefreshThread.start();
	}

	private static final String[] FETCH_GROUPS_WORKLOCK = {
		FetchPlan.DEFAULT, Worklock.FETCH_GROUP_LOCK_OWNER_USER, Worklock.FETCH_GROUP_WORKLOCK_TYPE
	};

	private void createJob(Worklock worklock)
	{
		WorklockRefreshJob job = new WorklockRefreshJob(worklock);
		WorklockID worklockID = job.getWorklockID();
		WorklockRefreshJob oldJob;
		synchronized (worklockID2Job) {
			oldJob = worklockID2Job.put(worklockID, job);
		}
		if (oldJob != null)
			oldJob.cancel();

		job.scheduleWithWorklockTypeDelay();
	}

	// TODO this method should be optimized so that it can be called in a Text's ModifyListener and only performs an RMI call with a delay (and not more than 1 / minute).
	public void acquireWorklock(WorklockTypeID worklockTypeID, ObjectID objectID, Shell parentShell)
	{
		AcquireWorklockResult acquireWorklockResult = worklockDAO.acquireWorklock(worklockTypeID, objectID, FETCH_GROUPS_WORKLOCK, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		// TODO if we only refresh (i.e. we have only a lock) we shouldn't open a dialog!
		if (acquireWorklockResult.getWorklockCount() > 1) { // there's another lock => we show a warning
			WorklockCollisionWarningDialog dialog = new WorklockCollisionWarningDialog(parentShell, acquireWorklockResult);
			dialog.open();
		}

		createJob(acquireWorklockResult.getWorklock());
	}

	public void releaseWorklock(ObjectID objectID)
	{
		worklockDAO.releaseWorklock(objectID);
	}
}
