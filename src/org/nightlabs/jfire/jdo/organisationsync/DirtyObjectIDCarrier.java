/*
 * Created on Sep 16, 2005
 */
package org.nightlabs.jfire.jdo.organisationsync;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class DirtyObjectIDCarrier
implements Serializable
{
	private String context;

	private Set objectIDs;

	public DirtyObjectIDCarrier(String context)
	{
		this.context = context;
		this.objectIDs = new HashSet();
	}

	public String getContext()
	{
		return context;
	}
	public Set getObjectIDs()
	{
		return objectIDs;
	}
	public void addObjectID(Object objectID)
	{
		objectIDs.add(objectID);
	}
}
