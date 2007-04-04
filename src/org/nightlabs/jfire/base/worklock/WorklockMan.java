package org.nightlabs.jfire.base.worklock;

import javax.jdo.FetchPlan;

import org.eclipse.swt.widgets.Shell;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.worklock.AcquireWorklockResult;
import org.nightlabs.jfire.worklock.Worklock;
import org.nightlabs.jfire.worklock.id.WorklockTypeID;

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

	private Thread worklockRefreshThread = new Thread() {
		@Override
		public void run()
		{
			// TODO track what needs to be refreshed and pop up a warn dialog
		}
	};

	public WorklockMan()
	{
		worklockRefreshThread.start();
	}

	private static final String[] FETCH_GROUPS_WORKLOCK = {
		FetchPlan.DEFAULT, Worklock.FETCH_GROUP_LOCK_OWNER
	};

	public void acquireWorklock(WorklockTypeID worklockTypeID, ObjectID objectID, Shell parentShell)
	{
		AcquireWorklockResult acquireWorklockResult = worklockDAO.acquireWorklock(worklockTypeID, objectID, FETCH_GROUPS_WORKLOCK, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		if (acquireWorklockResult.getWorklockCount() > 1) { // there's another lock => we show a warning
			WorklockCollisionWarningDialog dialog = new WorklockCollisionWarningDialog(parentShell, acquireWorklockResult);
			dialog.open();
		}
	}

	public void releaseWorklock(ObjectID objectID)
	{
		// TODO implement
	}
}
