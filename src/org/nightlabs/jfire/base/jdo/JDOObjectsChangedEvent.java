package org.nightlabs.jfire.base.jdo;

import java.util.Collection;
import java.util.EventObject;
import java.util.Map;

public class JDOObjectsChangedEvent<JDOObjectID, JDOObject>
extends EventObject
{
	private static final long serialVersionUID = 1L;

	private Collection<JDOObject> loadedJDOObjects;
	private Map<JDOObjectID, JDOObject> ignoredJDOObjects;
	private Map<JDOObjectID, JDOObject> deletedJDOObjects;

	public JDOObjectsChangedEvent(
			Object source,
			Collection<JDOObject> loadedJDOObjects,
			Map<JDOObjectID, JDOObject> ignoredJDOObjects,
			Map<JDOObjectID, JDOObject> deletedJDOObjects)
	{
		super(source);
		this.loadedJDOObjects = loadedJDOObjects;
		this.ignoredJDOObjects = ignoredJDOObjects;
		this.deletedJDOObjects = deletedJDOObjects;
	}

	/**
	 * @return Returns all objects that have been loaded or reloaded (due to a modification on the server side). May be <code>null</code>.
	 */
	public Collection<JDOObject> getLoadedJDOObjects()
	{
		return loadedJDOObjects;
	}

	/**
	 * @return Returns all object-ids whose objects have not been reloaded after a modification. That means, these objects
	 *		have been filtered out (even though they still exist). May be <code>null</code>.
	 */
	public Map<JDOObjectID, JDOObject> getIgnoredJDOObjects()
	{
		return ignoredJDOObjects;
	}

	public Map<JDOObjectID, JDOObject> getDeletedJDOObjects()
	{
		return deletedJDOObjects;
	}
}
