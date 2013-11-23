package org.nightlabs.jfire.base.jdo.notification.queue;

import java.util.EventObject;
import java.util.List;

import org.nightlabs.jfire.jdo.notification.DirtyObjectID;

public class QueuedJDOLifecycleEvent
extends EventObject
{
	private static final long serialVersionUID = 1L;

	private List<DirtyObjectID> dirtyObjectIDs;

	public QueuedJDOLifecycleEvent(Object source, List<DirtyObjectID> dirtyObjectIDs)
	{
		super(source);
		this.dirtyObjectIDs = dirtyObjectIDs;
	}

	public List<DirtyObjectID> getDirtyObjectIDs()
	{
		return dirtyObjectIDs;
	}
}
