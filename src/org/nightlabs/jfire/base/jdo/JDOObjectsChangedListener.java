package org.nightlabs.jfire.base.jdo;

public interface JDOObjectsChangedListener<JDOObjectID, JDOObject>
{
	void onJDOObjectsChanged(JDOObjectsChangedEvent<JDOObjectID, JDOObject> event);
}
