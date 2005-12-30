/*
 * Created on Jul 27, 2005
 */
package org.nightlabs.jfire.jdo.cache;

import java.io.Serializable;

/**
 * This is a descriptor for a client which desires to be notified about
 * the change of a certain JDO object.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ChangeListenerDescriptor
implements Serializable
{
	private String sessionID;
	private Serializable objectID;

	public ChangeListenerDescriptor(String sessionID, Object objectID)
	{
		if (sessionID == null)
			throw new NullPointerException("sessionID");

		if (objectID == null)
			throw new NullPointerException("objectID");

		this.sessionID = sessionID;
		this.objectID = (Serializable)objectID;
	}

	/**
	 * @return Returns the sessionID.
	 */
	public String getSessionID()
	{
		return sessionID;
	}

	/**
	 * @return Returns the objectID.
	 */
	public Object getObjectID()
	{
		return objectID;
	}

	private int _hashCode = 0;

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		if (_hashCode == 0)
			_hashCode = sessionID.hashCode() ^ objectID.hashCode();

		return _hashCode;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;

		if (!(obj instanceof ChangeListenerDescriptor))
			return false;

		ChangeListenerDescriptor other = (ChangeListenerDescriptor)obj;
		return
				this.sessionID.equals(other.sessionID)
				&&
				this.objectID.equals(other.objectID);
	}
}
