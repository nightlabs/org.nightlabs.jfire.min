package org.nightlabs.jfire.jdo.cache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;

public class LocalDirtyEvent
implements Serializable
{
	private static final long serialVersionUID = 2L;

	private UUID cacheManagerFactoryID;

//	private CacheSessionCoordinate cacheSessionCoordinate;

	private String organisationID;

	private String sessionID;

	private Map<JDOLifecycleState, Map<Object, DirtyObjectID>> dirtyObjectIDs;

	private Map<Object, Class<?>> objectID2ClassMap;

	private Map<String, String> properties;

	public LocalDirtyEvent(
			UUID cacheManagerFactoryID, String organisationID, String sessionID,
			Map<JDOLifecycleState, Map<Object, DirtyObjectID>> dirtyObjectIDs,
			Map<Object, Class<?>> objectID2ClassMap
	)
	{
		if (cacheManagerFactoryID == null)
			throw new IllegalArgumentException("cacheManagerFactoryID == null");

		if (organisationID == null)
			throw new IllegalArgumentException("organisationID == null");

		if (sessionID == null)
			throw new IllegalArgumentException("sessionID == null");

		if (dirtyObjectIDs == null)
			throw new IllegalArgumentException("dirtyObjectIDs == null");

		if (objectID2ClassMap == null)
			throw new IllegalArgumentException("objectID2ClassMap == null");

		this.cacheManagerFactoryID = cacheManagerFactoryID;
		this.organisationID = organisationID;
		this.sessionID = sessionID;
		this.dirtyObjectIDs = dirtyObjectIDs;
		this.objectID2ClassMap = objectID2ClassMap;
	}

	public UUID getCacheManagerFactoryID() {
		return cacheManagerFactoryID;
	}

//	public CacheSessionCoordinate getCacheSessionCoordinate() {
//		return cacheSessionCoordinate;
//	}

	public String getOrganisationID() {
		return organisationID;
	}

	public String getSessionID() {
		return sessionID;
	}

	public Map<JDOLifecycleState, Map<Object, DirtyObjectID>> getDirtyObjectIDs() {
		return dirtyObjectIDs;
	}

	public Map<Object, Class<?>> getObjectID2ClassMap() {
		return objectID2ClassMap;
	}

	public String getProperty(String key)
	{
		if (properties == null)
			return null;
		else
			return properties.get(key);
	}

	public void setProperty(String key, String value)
	{
		if (properties == null)
			properties = new HashMap<String, String>();

		if (value == null)
			properties.remove(key);
		else
			properties.put(key, value);
	}
}
