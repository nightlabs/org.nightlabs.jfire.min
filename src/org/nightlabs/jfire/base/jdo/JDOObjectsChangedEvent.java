package org.nightlabs.jfire.base.jdo;

import java.util.Collection;
import java.util.EventObject;

public class JDOObjectsChangedEvent<JDOObject>
extends EventObject
{
	private static final long serialVersionUID = 1L;

	private Collection<JDOObject> loadedJDOObjects;
	private Collection<JDOObject> deletedJDOObjects;

	public JDOObjectsChangedEvent(Object source, Collection<JDOObject> loadedJDOObjects, Collection<JDOObject> deletedJDOObjects)
	{
		super(source);
		this.loadedJDOObjects = loadedJDOObjects;
		this.deletedJDOObjects = deletedJDOObjects;
	}

	public Collection<JDOObject> getDeletedJDOObjects()
	{
		return deletedJDOObjects;
	}
	public Collection<JDOObject> getLoadedJDOObjects()
	{
		return loadedJDOObjects;
	}
}
