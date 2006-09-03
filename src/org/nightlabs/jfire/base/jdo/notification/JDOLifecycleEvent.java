package org.nightlabs.jfire.base.jdo.notification;

import java.util.Collection;
import java.util.EventObject;

import org.nightlabs.jfire.jdo.cache.DirtyObjectID;

public class JDOLifecycleEvent
		extends EventObject
{
	private static final long serialVersionUID = 1L;

	public JDOLifecycleEvent(Object source, Collection<DirtyObjectID> dirtyObjectIDs)
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
