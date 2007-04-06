package org.nightlabs.jfire.base.worklock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.annotation.Implement;
import org.nightlabs.base.util.RCPUtil;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.worklock.AcquireWorklockResult;
import org.nightlabs.jfire.worklock.ReleaseReason;
import org.nightlabs.jfire.worklock.Worklock;
import org.nightlabs.jfire.worklock.WorklockType;
import org.nightlabs.jfire.worklock.id.WorklockID;
import org.nightlabs.jfire.worklock.id.WorklockTypeID;

public class WorklockMan
{
	private static final Logger logger = Logger.getLogger(WorklockMan.class);

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

	protected void softReleaseWorklockOnUserInactivity(Collection<WorklockCarrier> worklockCarriers)
	{
		List<ObjectID> objectIDs = new ArrayList<ObjectID>(worklockCarriers.size());
		for (WorklockCarrier worklockCarrier : worklockCarriers) {
			boolean canReleaseWorklock = true;
			WorklockCallback callbackListener = worklockCarrier.getWorklockCallbackListener();
			if (callbackListener != null)
				canReleaseWorklock = callbackListener.canReleaseWorklock(worklockCarrier);

			if (canReleaseWorklock)	
				objectIDs.add(worklockCarrier.getWorklock().getLockedObjectID());
		}

		releaseWorklockWithJob(objectIDs, ReleaseReason.userInactivity);
	}

	private class WorklockRefreshJob extends Job
	{
		private WorklockCarrier worklockCarrier;
		private WorklockID worklockID;
		private Worklock worklock;
		private WorklockType worklockType;
		private WorklockTypeID worklockTypeID;

		public WorklockRefreshJob(WorklockCarrier worklockCarrier)
		{
			super("Refresh Worklocks");
			setWorklockCarrier(worklockCarrier);
		}

		private void setWorklockCarrier(WorklockCarrier worklockCarrier)
		{
			this.worklockCarrier = worklockCarrier;
			this.worklock = worklockCarrier.getWorklock();
			this.worklockID = (WorklockID) JDOHelper.getObjectId(worklock);
			this.worklockType = worklock.getWorklockType();
			this.worklockTypeID = (WorklockTypeID) JDOHelper.getObjectId(worklockType);
		}

		public WorklockCarrier getWorklockCarrier()
		{
			return worklockCarrier;
		}
		public WorklockID getWorklockID()
		{
			return worklockID;
		}
		public Worklock getWorklock()
		{
			return worklock;
		}

		private void handleExpiredUserActivityIfNecessary(IProgressMonitor monitor)
		{
			long now = System.currentTimeMillis();
			if (now >= expiryUserInactivityTimestamp) {
				boolean userInactivityReachedLimit;
				synchronized (worklockCarrierMutex) {
					userInactivityReachedLimit = worklockCarrier.getLastUserActivityDT().getTime() + worklockType.getWorklockExpiryUserInactivityMSec() < now;
					if (userInactivityReachedLimit)
						worklockCarrier.setLastUserActivityDT();
				}
				if (userInactivityReachedLimit) {
					Display.getDefault().asyncExec(new Runnable()
					{
						public void run()
						{
							boolean canPopupDialog = true;
							WorklockCallback callbackListener = worklockCarrier.getWorklockCallbackListener();
							if (callbackListener != null)
								canPopupDialog = callbackListener.canPopupDialog(worklockCarrier);

							if (!canPopupDialog) {
								ArrayList<WorklockCarrier> carriers = new ArrayList<WorklockCarrier>(1);
								carriers.add(worklockCarrier);
								softReleaseWorklockOnUserInactivity(carriers);
							}
							else {
								if (worklockAboutToExpireDueToUserInactivityDialog == null) {
									worklockAboutToExpireDueToUserInactivityDialog = new WorklockAboutToExpireDueToUserInactivityDialog(WorklockMan.this, RCPUtil.getActiveWorkbenchShell());
									worklockAboutToExpireDueToUserInactivityDialog.setBlockOnOpen(false);
									worklockAboutToExpireDueToUserInactivityDialog.open();
								}

								// add the Worklock to the dialog
								worklockAboutToExpireDueToUserInactivityDialog.addWorklockCarrier(worklockCarrier);
							}
						}
					});
				}
			} // if (now >= expiryUserInactivityTimestamp) {
		}

		private void reacquireWorklockOnServerIfNecessary(IProgressMonitor monitor)
		{
			logger.info("reacquireWorklockOnServerIfNecessary: enter");
			if (System.currentTimeMillis() >= expiryClientLostTimestamp) { // it's time to refresh the server-side worklock
				logger.info("reacquireWorklockOnServerIfNecessary: reacquiring");
				AcquireWorklockResult acquireWorklockResult = worklockDAO.acquireWorklock(
						worklockTypeID, worklock.getLockedObjectID(), worklock.getDescription(),
						FETCH_GROUPS_WORKLOCK, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

				WorklockCarrier newWorklockCarrier = createWorklockCarrier(acquireWorklockResult.getWorklock(), worklockCarrier);
				setWorklockCarrier(newWorklockCarrier);
			}
		}

		@Implement
		protected IStatus run(IProgressMonitor monitor)
		{
			synchronized (worklockID2Job) {
				if (worklockID2Job.get(worklockID) != this) {
					logger.info("WorklockRefreshJob.run: job has been replaced - cancelling.");
					return Status.CANCEL_STATUS; // a new job has already been created - this job is out-of-charge now
				}
			}

			handleExpiredUserActivityIfNecessary(monitor);
			reacquireWorklockOnServerIfNecessary(monitor);

			scheduleWithWorklockTypeDelay();
			return Status.OK_STATUS;
		}

		public void scheduleWithWorklockTypeDelay()
		{
			WorklockType worklockType = worklock.getWorklockType();
			long now = System.currentTimeMillis();

			// Calculate when we need to wake-up in order to re-acquire the lock on the server in time.
			expiryClientLostTimestamp = worklock.getLastAcquireDT().getTime() + worklockType.getWorklockExpiryClientLostMSec();
			// TODO we should somehow handle time differences between client and server... As the time should be UTC (according to the javadoc of java.util.Date), this
			// should work fine if client + server are synchronized with an internet clock, but otherwise this causes problems. We need to change this code
			// somehow in order to take a wrong client-clock into account!
			long delayClientLost = expiryClientLostTimestamp - now;
//			long reduction = delayClientLost / 4;
//			reduction = Math.max(reduction, 1L * 60L * 1000L); // 1 minute before the server releases - is hopefully enough to react in time TODO should be configurable!
			long reduction = 2L * 60L * 1000L; // 2 minutes before the server releases - is hopefully enough to react in time TODO should be configurable!
			delayClientLost = delayClientLost - reduction;
			if (delayClientLost < 0) delayClientLost = 0;
			expiryClientLostTimestamp = now + delayClientLost;

			// Calculate when we need to wake-up in order to ask the user
			expiryUserInactivityTimestamp = worklockCarrier.getLastUserActivityDT().getTime() + worklockType.getWorklockExpiryUserInactivityMSec();
			// these times are managed within the client alone => no time-difference-problems
			long delayUserInactivity = expiryUserInactivityTimestamp - now;
			if (delayUserInactivity < 0) delayUserInactivity = 0;

			long delay = Math.min(delayClientLost, delayUserInactivity);
			logger.info("scheduleWithWorklockTypeDelay: delay="+delay+" delayClientLost="+delayClientLost + " delayUserInactivity="+delayUserInactivity);
			schedule(delay); // wake up as soon as some action is required
		}
	}

	private long expiryClientLostTimestamp;
	private long expiryUserInactivityTimestamp;

	private WorklockAboutToExpireDueToUserInactivityDialog worklockAboutToExpireDueToUserInactivityDialog = null;

	protected void onCloseWorklockAboutToExpireDueToUserInactivityDialog()
	{
		this.worklockAboutToExpireDueToUserInactivityDialog = null;
	}

	private Map<WorklockID, WorklockRefreshJob> worklockID2Job = new HashMap<WorklockID, WorklockRefreshJob>();

	private Map<ObjectID, WorklockCarrier> objectID2WorklockCarrier = new HashMap<ObjectID, WorklockCarrier>();
	private Map<WorklockID, WorklockCarrier> worklockID2WorklockCarrier = new HashMap<WorklockID, WorklockCarrier>();
	private Object worklockCarrierMutex = new Object();

	public WorklockMan()
	{
	}

	private static final String[] FETCH_GROUPS_WORKLOCK = {
		FetchPlan.DEFAULT, Worklock.FETCH_GROUP_LOCK_OWNER_USER, Worklock.FETCH_GROUP_WORKLOCK_TYPE
	};

	private void createJob(WorklockCarrier worklockCarrier)
	{
		WorklockRefreshJob job = new WorklockRefreshJob(worklockCarrier);
		WorklockID worklockID = job.getWorklockID();
		WorklockRefreshJob oldJob;
		synchronized (worklockID2Job) {
			oldJob = worklockID2Job.put(worklockID, job);
		}
		if (oldJob != null)
			oldJob.cancel();

		job.scheduleWithWorklockTypeDelay();
	}

	private WorklockCarrier createWorklockCarrier(Worklock worklock, WorklockCarrier oldWorklockCarrier)
	{
		WorklockID worklockID = (WorklockID) JDOHelper.getObjectId(worklock);
		WorklockCarrier worklockCarrier = new WorklockCarrier(worklock, oldWorklockCarrier); // copy properties from the old carrier

		synchronized (worklockCarrierMutex) {
			objectID2WorklockCarrier.put(worklock.getLockedObjectID(), worklockCarrier);
			worklockID2WorklockCarrier.put(worklockID, worklockCarrier);
		}

		return worklockCarrier;
	}

	public void acquireWorklock(WorklockTypeID worklockTypeID, ObjectID objectID, String description, WorklockCallback worklockCallback, Shell parentShell)
	{
		WorklockCarrier oldWorklockCarrier;
		synchronized (worklockCarrierMutex) {
			oldWorklockCarrier = objectID2WorklockCarrier.get(objectID);
			if (oldWorklockCarrier != null) { // If we already manage a lock, we only set the new lastUserActivity
				oldWorklockCarrier.setLastUserActivityDT();
				oldWorklockCarrier.setWorklockCallbackListener(worklockCallback);
			}
		}
		if (oldWorklockCarrier == null) { // we only need to communicate with the server, if the object is not yet locked there. and we don't open a dialog when refreshing - only when new. 
			AcquireWorklockResult acquireWorklockResult = worklockDAO.acquireWorklock(
					worklockTypeID, objectID, description, FETCH_GROUPS_WORKLOCK, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

			WorklockCarrier newWorklockCarrier;
			synchronized (worklockCarrierMutex) {
				newWorklockCarrier = createWorklockCarrier(acquireWorklockResult.getWorklock(), null); // we ignore the old carrier and create a clean one
				newWorklockCarrier.setWorklockCallbackListener(worklockCallback);
			}

			if (acquireWorklockResult.getWorklockCount() > 1) { // there's another lock => we show a warning
				WorklockCollisionWarningDialog dialog = new WorklockCollisionWarningDialog(parentShell, acquireWorklockResult);
				dialog.open();
			}
			createJob(newWorklockCarrier);
		}
	}

	private void releaseWorklockWithJob(final List<ObjectID> objectIDs, final ReleaseReason releaseReason)
	{
		Job job = new Job("Release Worklock") {
			@Implement
			protected IStatus run(IProgressMonitor monitor)
			{
				releaseWorklocks(objectIDs, releaseReason, monitor);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	public void releaseWorklock(ObjectID objectID, IProgressMonitor monitor)
	{
		ArrayList<ObjectID> objectIDs = new ArrayList<ObjectID>(1);
		objectIDs.add(objectID);
		releaseWorklocks(objectIDs, ReleaseReason.normal, monitor);
	}

	private void releaseWorklocks(List<ObjectID> objectIDs, ReleaseReason releaseReason, IProgressMonitor monitor)
	{
		for (ObjectID objectID : objectIDs) {
			WorklockCarrier worklockCarrier;
			synchronized (worklockCarrierMutex) {
				worklockCarrier = objectID2WorklockCarrier.remove(objectID);
				if (worklockCarrier != null)
					worklockCarrier.setLastUserActivityDT();
			}

			if (worklockCarrier != null) {
				Job job;
				synchronized (worklockID2Job) {
					job = worklockID2Job.remove((WorklockID)JDOHelper.getObjectId(worklockCarrier.getWorklock()));
				}
				if (job != null)
					job.cancel();
			}

			worklockDAO.releaseWorklock(objectID, ReleaseReason.normal, monitor);
		}
	}
}
