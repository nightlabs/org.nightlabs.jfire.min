/*
 * Created on Oct 7, 2005
 */
package org.nightlabs.jfire.jdo.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DirtyObjectID
implements Serializable
{
	private Object objectID;
	private Set sourceSessionIDs;

	/**
	 * @param objectID The jdo object id.
	 * @param causeSessionID The session which caused this object to be dirty.
	 */
	public DirtyObjectID(Object objectID, String causeSessionID)
	{
		this.objectID = objectID;
		this.sourceSessionIDs = new HashSet(1);
		this.sourceSessionIDs.add(causeSessionID);
	}

	public Set getSourceSessionIDs()
	{
		return sourceSessionIDs;
	}
	public void addSourceSessionID(String causeSessionID)
	{
		sourceSessionIDs.add(causeSessionID);
	}
	public void addSourceSessionIDs(Collection causeSessionIDs)
	{
		causeSessionIDs.addAll(causeSessionIDs);
	}
	public Object getObjectID()
	{
		return objectID;
	}
}
