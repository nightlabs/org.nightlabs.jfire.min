package org.nightlabs.jfire.base.worklock;

import org.nightlabs.jfire.worklock.Worklock;

public interface WorklockCallback
{
	/**
	 * <p>
	 * After there was no user-activity for a certain time (i.e. the Worklock was not reacquired by a follow-up call to
	 * {@link WorklockMan#acquireWorklock(org.nightlabs.jfire.worklock.id.WorklockTypeID, org.nightlabs.jdo.ObjectID, String, WorklockCallback, org.eclipse.swt.widgets.Shell)}),
	 * this method will be called and then - if its result is <code>true</code> - shown in a dialog.
	 * </p>
	 * <p>
	 * If this method returns <code>true</code>, the Worklock will be shown in the
	 * {@link WorklockAboutToExpireDueToUserInactivityDialog}. If this dialog is currently open, the lock will simply
	 * be added to the already-visible table. Otherwise a new dialog will popup. If this method returns <code>false</code>,
	 * the dialog will not popup and it will be handled as if the user ignored the dialog =&gt; {@link #canReleaseWorklock(WorklockCarrier)} will
	 * be called next.
	 * </p>
	 *
	 * @return whether to popup the dialog.
	 */
	boolean canPopupDialog(WorklockCarrier worklockCarrier);

	/**
	 * After the expiring worklock was shown in the {@link WorklockAboutToExpireDueToUserInactivityDialog} and
	 * the user didn't react, it will be released. The same happens, if the method {@link #canPopupDialog(WorklockCarrier)}
	 * returned <code>false</code>, before.
	 * <p>
	 * Then this method needs to decide whether to release the Worklock. If it is not released, its {@link WorklockCarrier#setLastUserActivityDT()} method
	 * is called to simulate a new user activity.
	 * </p>
	 *
	 * @param worklockCarrier The {@link WorklockCarrier} referencing the {@link Worklock} that is about to be released.
	 * @return whether or not to release the {@link Worklock}.
	 */
	boolean canReleaseWorklock(WorklockCarrier worklockCarrier);
}
