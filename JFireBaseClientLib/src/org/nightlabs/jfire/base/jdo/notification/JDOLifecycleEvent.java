package org.nightlabs.jfire.base.jdo.notification;

import java.util.EventObject;
import java.util.SortedSet;

import org.nightlabs.jfire.jdo.notification.DirtyObjectID;

public class JDOLifecycleEvent
		extends EventObject
{
	private static final long serialVersionUID = 1L;

	public JDOLifecycleEvent(Object source, SortedSet<DirtyObjectID> dirtyObjectIDs)
	{
		super(source);
		this.dirtyObjectIDs = dirtyObjectIDs;
	}

	private SortedSet<DirtyObjectID> dirtyObjectIDs;

	public SortedSet<DirtyObjectID> getDirtyObjectIDs()
	{
		return dirtyObjectIDs;
	}
}
