package org.nightlabs.jfire.jdo.notification;

import java.util.Collection;
import java.util.EventObject;

import org.nightlabs.jfire.jdo.cache.DirtyObjectID;

public class JDOLifecycleRemoteEvent
		extends EventObject
{
	private static final long serialVersionUID = 1L;

	public JDOLifecycleRemoteEvent(Object source, Collection<DirtyObjectID> dirtyObjectIDs)
	{
		super(source);
		this.dirtyObjectIDs = dirtyObjectIDs;
	}

	private Collection<DirtyObjectID> dirtyObjectIDs;

	public Collection<DirtyObjectID> getDirtyObjectIDs()
	{
		return dirtyObjectIDs;
	}
}
