package org.nightlabs.jfire.base.jdo;

public interface JDOObjectsChangedListener<JDOObject>
{
	void onJDOObjectsChanged(JDOObjectsChangedEvent<JDOObject> event);
}
