package org.nightlabs.jfire.update.base;

import org.nightlabs.version.Version;

/**
 * @author Chairat
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class UpdateProcedure
implements Comparable<UpdateProcedure>
{
	private String moduleID = null;
	private Version fromVersion = null;
	private Version toVersion = null;

	/**
	 * Which module (see table NightLabsJDO_ModuleMetaData)
	 * is to be updated by the concrete {@link UpdateProcedure}-implementation.
	 *
	 * @return the module id.
	 */
	protected abstract String _getModuleID();

	/**
	 * Which module (see table NightLabsJDO_ModuleMetaData)
	 * is to be updated by the concrete {@link UpdateProcedure}-implementation.
	 * This method is called exactly once, thus implementors
	 * do not need to cache the {@link String} instance (which isn't necessary for a
	 * hard-coded <code>String</code> anyway, but might be for concatenated strings).
	 *
	 * @return the module id.
	 */
	public final String getModuleID()
	{
		if (moduleID == null)
			moduleID = _getModuleID();

		if (moduleID == null)
			throw new IllegalStateException("_getModuleID() returned null! Check your implementation in class " + this.getClass().getName() + "!!!");

		return moduleID;
	}

	/**
	 * From which version.
	 * @return the version from which to start the update.
	 */
	public final Version getFromVersion()
	{
		if (fromVersion == null)
			fromVersion = _getFromVersion();

		if (fromVersion == null)
			throw new IllegalStateException("_getFromVersion() returned null! Check your implementation in class " + this.getClass().getName() + "!!!");

		return fromVersion;
	}

	/**
	 * Get the version from which the update starts. This method is called exactly once, thus implementors
	 * do not need to cache the {@link Version} instance.
	 *
	 * @return the version from which to start the update.
	 */
	protected abstract Version _getFromVersion();

	/**
	 * To which version.
	 * @return the version to which this update-procedure updates.
	 */
	public final Version getToVersion()
	{
		if (toVersion == null)
			toVersion = _getToVersion();

		if (toVersion == null)
			throw new IllegalStateException("_getToVersion() returned null! Check your implementation in class " + this.getClass().getName() + "!!!");

		return toVersion;
	}

	/**
	 * Get the version to which the update leads. This method is called exactly once, thus implementors
	 * do not need to cache the {@link Version} instance.
	 *
	 * @return the version to which to update.
	 */
	protected abstract Version _getToVersion();

//	/**
//	 * which database server(s) it supports (usually there should be no
//	 * DB-server-specific code in an UpdateProcedure, but if the
//	 * DB-server-abstraction does not provide the required functionality,
//	 * this might become necessary – i.e. if this method is not overridden,
//	 * the default value ALL is assumed).
//	 * @return Collection of String
//	 */
//	protected boolean isDatabaseServerAgnostic() {
//		return true;
//	}

	public abstract void run() throws Exception;
	
	/**
	 * This UpdateContext provides access to the database (JDBC)
	 * as well as all other required information.
	 */
	private UpdateContext updateContext;
	public void setUpdateContext(UpdateContext updateContext) {
		this.updateContext = updateContext;
	}
	public UpdateContext getUpdateContext() {
		return updateContext;
	}

	@Override
	public int compareTo(UpdateProcedure o)
	{
		int result = this.getModuleID().compareTo(o.getModuleID());
		if (result != 0)
			return result;

		result = this.getFromVersion().compareTo(o.getFromVersion());
		if (result != 0)
			return result;

		result = this.getToVersion().compareTo(o.getToVersion()); // This is only necessary to fulfill the usual contract between compareTo and equals.
		if (result != 0)
			return result;

		result = this.getClass().getName().compareTo(o.getClass().getName());
		return result;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + moduleID + ',' + fromVersion + ',' + toVersion + ']';
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getModuleID().hashCode();
		result = prime * result + getFromVersion().hashCode();
		result = prime * result + getToVersion().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		UpdateProcedure other = (UpdateProcedure) obj;
		return (
				// Although this should always be the same, if the class is the same, we still check it for code beauty reasons ;-)
				// Actually, only this way, the contracts are cleanly fulfilled, even though in reality we should be able to rely
				// on the fact that the methods are statically implemented (i.e. they return the same for all instances).
				// Marco.
				this.getModuleID().equals(other.getModuleID()) &&
				this.getFromVersion().equals(other.getFromVersion()) &&
				this.getToVersion().equals(other.getToVersion())
		);
	}
}