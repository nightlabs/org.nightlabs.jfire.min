/*
 * Created on Sep 15, 2005
 */
package org.nightlabs.ipanema.jdo.cache;

import java.util.Collection;


public interface LocalDirtyListener
{
	/**
	 * This method is executed synchronously by
	 * {@link CacheManagerFactory#addDirtyObjectIDs(Collection)}.
	 * This means, it happens during the JDO-commit.
	 * @param objectIDs The JDO object ids of those objects that have been changed.
	 */
	void notifyDirtyObjectIDs(Collection objectIDs);
}
