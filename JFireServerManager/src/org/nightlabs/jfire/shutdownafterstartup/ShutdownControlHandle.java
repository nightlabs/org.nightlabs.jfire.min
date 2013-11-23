package org.nightlabs.jfire.shutdownafterstartup;

import java.io.Serializable;

import org.nightlabs.util.Util;

public class ShutdownControlHandle
implements Serializable
{
	private static final long serialVersionUID = 2L;

	private String shutdownAfterStartupManagerInstanceID;
	private int shutdownControlHandleID;

	public ShutdownControlHandle(String shutdownAfterStartupManagerInstanceID, int id) {
		this.shutdownAfterStartupManagerInstanceID = shutdownAfterStartupManagerInstanceID;
		this.shutdownControlHandleID = id;
	}

	public int getShutdownControlHandleID() {
		return shutdownControlHandleID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (shutdownAfterStartupManagerInstanceID == null ? 0 : shutdownAfterStartupManagerInstanceID.hashCode());
		result = prime * result + shutdownControlHandleID;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final ShutdownControlHandle other = (ShutdownControlHandle) obj;
		return (
				Util.equals(this.shutdownAfterStartupManagerInstanceID, other.shutdownAfterStartupManagerInstanceID) &&
				Util.equals(this.shutdownControlHandleID, other.shutdownControlHandleID)
		);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + System.identityHashCode(this) + '[' + shutdownAfterStartupManagerInstanceID + ',' + shutdownControlHandleID + ']';
	}
}
