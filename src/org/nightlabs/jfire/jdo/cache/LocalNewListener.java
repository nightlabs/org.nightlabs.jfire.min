package org.nightlabs.jfire.jdo.cache;

import java.util.Collection;

public interface LocalNewListener
{
	/**
	 * This method is executed synchronously by
	 * {@link CacheManagerFactory#addDirtyObjectIDs(Collection)}.
	 * This means, it happens during the JDO-commit.
	 * @param objectIDs The JDO object ids of those objects that have been newly added to the datastore.
	 */
	void notifyNewObjectIDs(Collection<Object> objectIDs);
}
