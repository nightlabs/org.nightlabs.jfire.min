package org.nightlabs.jfire.idgenerator;

import javax.ejb.Local;

@Local
public interface IDGeneratorHelperLocal
{
	/**
	 * This method is called by the {@link IDGeneratorServer} and not visible to a remote client.
	 * Warning: You should not use this method, but instead call {@link IDGenerator#nextIDs(String, int)}!!! This
	 * method is only used internally.
	 *
	 * @param namespace The namespace (within the scope of the current organisation) within which unique IDs need to be generated.
	 * @param currentCacheSize The current number of cached IDs.
	 * @param minCacheSize The minimum number of IDs that must be available in the cache after the generated ones are added.
	 */
	long[] serverNextIDs(String namespace, int currentCacheSize, int minCacheSize);
}