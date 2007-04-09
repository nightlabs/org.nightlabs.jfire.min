package org.nightlabs.jfire.base.editlock;

import org.nightlabs.jfire.editlock.EditLock;

public interface EditLockCallback
{
	/**
	 * <p>
	 * After there was no user-activity for a certain time (i.e. the EditLock was not reacquired by a follow-up call to
	 * {@link EditLockMan#acquireEditLock(org.nightlabs.jfire.editLock.id.EditLockTypeID, org.nightlabs.jdo.ObjectID, String, EditLockCallback, org.eclipse.swt.widgets.Shell)}),
	 * this method will be called and then - if its result is <code>true</code> - shown in a dialog.
	 * </p>
	 * <p>
	 * If this method returns <code>true</code>, the EditLock will be shown in the
	 * {@link EditLockAboutToExpireDueToUserInactivityDialog}. If this dialog is currently open, the lock will simply
	 * be added to the already-visible table. Otherwise a new dialog will popup. If this method returns <code>false</code>,
	 * the dialog will not popup and it will be handled as if the user ignored the dialog =&gt; {@link #canReleaseEditLock(EditLockCarrier)} will
	 * be called next.
	 * </p>
	 *
	 * @return whether to popup the dialog.
	 */
	boolean canPopupDialog(EditLockCarrier editLockCarrier);

	/**
	 * After the expiring editLock was shown in the {@link EditLockAboutToExpireDueToUserInactivityDialog} and
	 * the user didn't react, it will be released. The same happens, if the method {@link #canPopupDialog(EditLockCarrier)}
	 * returned <code>false</code>, before.
	 * <p>
	 * Then this method needs to decide whether to release the EditLock. If it is not released, its {@link EditLockCarrier#setLastUserActivityDT()} method
	 * is called to simulate a new user activity.
	 * </p>
	 *
	 * @param editLockCarrier The {@link EditLockCarrier} referencing the {@link EditLock} that is about to be released.
	 * @return whether or not to release the {@link EditLock}.
	 */
	boolean canReleaseEditLock(EditLockCarrier editLockCarrier);
}
