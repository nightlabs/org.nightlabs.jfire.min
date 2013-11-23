package org.nightlabs.jfire.jdo.notification;

import java.io.Serializable;
import java.util.Collection;

import org.nightlabs.jdo.ObjectID;

public interface TreeNodeMultiParentResolver
extends Serializable
{
	/**
	 * Called (on server and client!) to resolve the
	 * parent ObjectIDs of the given JDO object.
	 *
	 * @param jdoObject The object the parent should be resolved for.
	 * @return the ObjectIDs of the parents of the given object. Can be <code>null</code> (which means the same as an empty collection).
	 */
	Collection<ObjectID> getParentObjectIDs(Object jdoObject);
}
