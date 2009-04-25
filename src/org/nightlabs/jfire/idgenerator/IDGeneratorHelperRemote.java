package org.nightlabs.jfire.idgenerator;

import javax.ejb.Remote;

@Remote
public interface IDGeneratorHelperRemote
{
	String ping(String message);

//	/**
//	 * This method is called by the {@link IDGeneratorServer}. It should be declared in <code>IDGeneratorHelperLocal</code>
//	 * (a local EJB interface),
//	 * but handling local EJB references is even less portable across JavaEE servers, if dependency injection is not possible
//	 * (like in the case of IDGeneratorServer). Thus, I moved the method here and deleted the local EJB interface.
//	 * <p>
//	 * <b>Warning:</b> You should not use this method directly, but instead call {@link IDGenerator#nextIDs(String, int)}!!! This
//	 * method is only used internally.
//	 * </p>
//	 *
//	 * @param namespace The namespace (within the scope of the current organisation) within which unique IDs need to be generated.
//	 * @param currentCacheSize The current number of cached IDs.
//	 * @param minCacheSize The minimum number of IDs that must be available in the cache after the generated ones are added.
//	 */
//	long[] serverNextIDs(String namespace, int currentCacheSize, int minCacheSize);

	/**
	 * This method is called by the client side {@link IDGenerator} implementation.
	 * <p>
	 * <b>Warning:</b> You should not use this method directly, but instead call {@link IDGenerator#nextIDs(String, int)}!!! This
	 * method is only used internally.
	 * </p>
	 *
	 * @param namespace The namespace (within the scope of the current organisation) within which unique IDs need to be generated.
	 * @param currentCacheSize The current number of cached IDs.
	 * @param minCacheSize The minimum number of IDs that must be available in the cache after the generated ones are added.
	 */
	long[] clientNextIDs(String namespace, int currentCacheSize, int minCacheSize);
}