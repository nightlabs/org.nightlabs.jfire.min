package org.nightlabs.jfire.jdo.notification;

import java.io.Serializable;

import org.nightlabs.jdo.ObjectID;

public interface TreeNodeParentResolver
extends Serializable
{
	/**
	 * Called (on server and client!) to resolve the
	 * parent ObjectID of the given JDO object.
	 *  
	 * @param jdoObject The object the parent should be resolved for.
	 * @return The ObjectID of the parent of the given object.
	 */
	ObjectID getParentObjectID(Object jdoObject);
}
