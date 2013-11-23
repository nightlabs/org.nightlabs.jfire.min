package org.nightlabs.jfire.serverupdate.base;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import org.nightlabs.version.Version;

class UpdateStep
{
	private String moduleID;
	private Version fromVersion;
	private Version toVersion;

	private SortedSet<UpdateProcedure> updateProcedures = new TreeSet<UpdateProcedure>();

	public UpdateStep(UpdateProcedure updateProcedure)
	{
		this.moduleID = updateProcedure.getModuleID();
		this.fromVersion = updateProcedure.getFromVersion();
		this.toVersion = updateProcedure.getToVersion();
		updateProcedures.add(updateProcedure);
	}

	public String getModuleID() {
		return moduleID;
	}
	public Version getFromVersion() {
		return fromVersion;
	}
	public Version getToVersion() {
		return toVersion;
	}

	public SortedSet<UpdateProcedure> getUpdateProcedures() {
		return Collections.unmodifiableSortedSet(updateProcedures);
	}

	/**
	 * Add an {@link UpdateProcedure}.
	 *
	 * @param updateProcedure the instance to be added - must not be <code>null</code>.
	 * @throws InconsistentUpdateProceduresException if the {@link UpdateProcedure}'s <code>fromVersion</code> or <code>toVersion</code> do
	 * not match the values in this <code>UpdateStep</code>.
	 */
	public boolean addUpdateProcedure(UpdateProcedure updateProcedure)
	throws InconsistentUpdateProceduresException
	{
		if (updateProcedure == null)
			throw new IllegalArgumentException("updateProcedure == null");

		if (!this.fromVersion.equals(updateProcedure.getFromVersion()))
			throw new InconsistentUpdateProceduresException(
					InconsistentUpdateProceduresException.Reason.fromVersionMismatch,
					"The 'fromVersion' (" + this.fromVersion + ") of this UpdateStep does not match the 'fromVersion' of this UpdateProcedure: " + updateProcedure
			);

		if (!this.toVersion.equals(updateProcedure.getToVersion()))
			throw new InconsistentUpdateProceduresException(
					InconsistentUpdateProceduresException.Reason.toVersionMismatch,
					"The 'toVersion' (" + this.toVersion + ") of this UpdateStep does not match the 'toVersion' of this UpdateProcedure: " + updateProcedure
			);

		return updateProcedures.add(updateProcedure);
	}

	public boolean removeUpdateProcedure(UpdateProcedure updateProcedure) {
		return updateProcedures.remove(updateProcedure);
	}
}
