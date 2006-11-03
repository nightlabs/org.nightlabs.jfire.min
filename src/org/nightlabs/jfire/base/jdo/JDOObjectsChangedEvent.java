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
	 * @return Returns all object-ids (mapped to their jdo objects) whose objects have <b>not</b> been reloaded after a modification.
	 *		That means, these objects have been filtered out in
	 *		{@link ActiveJDOObjectController#retrieveJDOObjects(java.util.Set, org.eclipse.core.runtime.IProgressMonitor)}
	 *		(even though they still exist). Note, that for a given object-id, a <code>null</code> value
	 *		might be mapped as jdo object (i.e. map-value), if the jdo-object has not been fetched previously. May be <code>null</code>.
	 */
	public Map<JDOObjectID, JDOObject> getIgnoredJDOObjects()
	{
		return ignoredJDOObjects;
	}

	/**
	 * @return Returns object-ids (mapped to their jdo objects) of jdo objects that have been deleted. If the jdo object
	 *		has not been previously fetched, the mapped value will be <code>null</code> - otherwise the previously fetched
	 *		jdo object corresponding to the object id. May be <code>null</code>.
	 */
	public Map<JDOObjectID, JDOObject> getDeletedJDOObjects()
	{
		return deletedJDOObjects;
	}
}
