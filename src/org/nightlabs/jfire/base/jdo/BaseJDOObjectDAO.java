/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.jdo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;
import org.nightlabs.util.CollectionUtil;

/**
 * JDO object retrieval through the JFire client cache.
 * <p>
 * Inherit this class with a JDO object id class and
 * JDO object class as generic parameters to provide
 * an accessor object for this kind of JDO object.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @param <JDOObjectID> The type of {@link ObjectID} of the JDO object this DAO can provide
 * @param <JDOObject> The type of JDO object this DAO can provide
 */
public abstract class BaseJDOObjectDAO<JDOObjectID extends ObjectID, JDOObject>
{
	/**
	 * The cache shared instance.
	 */
	private Cache cache = Cache.sharedInstance();

	/**
	 * Default constructor.
	 */
	public BaseJDOObjectDAO()
	{
	}

	/**
	 * Retrieve a JDO object from the server. This method will be called by
	 * {@link #getJDOObjects(String, Collection, Set, int, IProgressMonitor)}
	 * for all objects that are not already in the cache.
	 * <p>
	 * Subclassers may override this method to provide a specialized way to
	 * retrieve a single JDO object from the server. The given implementation
	 * works by calling {@link #retrieveJDOObjects(Set, Set, int, IProgressMonitor)}
	 * for the single object.
	 * </p>
	 * <p>
	 * <b>Important: Never call this method directly!</b> Always call
	 * {@link #getJDOObject(String, Collection, String[], int, ProgressMonitor)} or one of the
	 * <code>getJDOObjects(...)</code> methods!
	 * </p>
	 *
	 * @param objectID Wich object to get
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. After downloading the
	 * 					object, <code>monitor.worked(1)</code> should be called.
	 * @return All requested and existing JDO objects.
	 * @throws Exception in case of an error
	 */
	protected JDOObject retrieveJDOObject(JDOObjectID objectID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	throws Exception
	{
		Set<JDOObjectID> objectIDs = new HashSet<JDOObjectID>(1);
		objectIDs.add(objectID);
		Collection<? extends JDOObject> objects = retrieveJDOObjects(objectIDs, fetchGroups, maxFetchDepth, monitor);
		if(objects == null || objects.isEmpty())
			return null;
		return objects.iterator().next();
	}

	/**
	 * Retrieve JDO objects from the server. This method will be called by
	 * {@link #getJDOObjects(String, Collection, Set, int, IProgressMonitor)}
	 * for all objects that are not already in the cache.
	 * <p>
	 * <b>Important: Never call this method directly!</b> Always call
	 * {@link #getJDOObjects(String, Collection, String[], int, ProgressMonitor)} or one of the other
	 * <code>getJDOObjects(...)</code> methods!
	 * </p>
	 *
	 * @param objectIDs Which objects to get
	 * @param fetchGroups Which fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> should be called.
	 * @return All requested and existing JDO objects.
	 * @throws Exception in case of an error
	 */
	protected abstract Collection<? extends JDOObject> retrieveJDOObjects(Set<JDOObjectID> objectIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	throws Exception;

	/**
	 * Get a JDO object from the cache or the server.
	 * Object download for an uncached object is delegated by calling
	 * {@link #retrieveJDOObject(Object, Set, int, IProgressMonitor)
	 * for the uncached object.
	 *
	 * @param scope The cache scope to use
	 * @param objectID Wich object to get
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. For every cached
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return All requested and existing JDO objects.
	 * @throws Exception in case of an error
	 */
	@SuppressWarnings("unchecked")
	protected synchronized JDOObject getJDOObject(String scope, JDOObjectID objectID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			JDOObject res = (JDOObject)cache.get(scope, objectID, fetchGroups, maxFetchDepth);
			if (res == null) {
				res = retrieveJDOObject(objectID, fetchGroups, maxFetchDepth, monitor);
				if (res != null)
					cache.put(scope, res, fetchGroups, maxFetchDepth);
			}
			return res;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	/**
	 * Get JDO objects from the cache or the server.
	 * Object download for uncached objects is delegated by calling
	 * {@link #retrieveJDOObjects(Set, Set, int, IProgressMonitor)}
	 * for all uncached objects.
	 * <p>
	 * Note that this will maintain the order of the objects given if you
	 * pass a {@link List} as objectIDs.
	 *
	 * @param scope The cache scope to use
	 * @param objectIDs Wich objects to get
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. For every cached
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return All requested and existing JDO objects.
	 * @throws Exception in case of an error
	 */
	@SuppressWarnings("unchecked")
	protected synchronized List<JDOObject> getJDOObjects(String scope, Collection<JDOObjectID> objectIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		// ratio retrieve x to post-process y with y = 2, i.e. ratio x : 2
		int tickRatio_retrieve_to_postProcess = 15;
		int monitorRestTicks = objectIDs.size() * (tickRatio_retrieve_to_postProcess + 2);
		monitor.beginTask(String.format("Getting %s objects through cache.", objectIDs.size()), monitorRestTicks);
		try {

			if (objectIDs == null || objectIDs.isEmpty()) {
				return new ArrayList<JDOObject>(0);
			}
			ArrayList<JDOObject> objects = new ArrayList<JDOObject>(objectIDs.size());

			List<JDOObjectID> listetIDs = new ArrayList<JDOObjectID>(objectIDs);
			Map<JDOObjectID, Integer> notInCache = new HashMap<JDOObjectID, Integer>();

			// search the cache for the wanted Objects
			for (int i=0; i < listetIDs.size(); i++) {
				JDOObject cachedObject = (JDOObject) cache.get(scope, listetIDs.get(i), fetchGroups, maxFetchDepth);
				if (cachedObject != null) {
					objects.add(cachedObject);
					monitor.worked(tickRatio_retrieve_to_postProcess);
					monitorRestTicks -= tickRatio_retrieve_to_postProcess;
				}
				else {
					notInCache.put(listetIDs.get(i), i); // if not in cache save (objectID, position)
					objects.add(null); // fill the result array, so that we're later able to replace the
					// JDOObject at the correct position
				}
			}

			if (monitorRestTicks < (2 * objectIDs.size()))
				throw new IllegalStateException("monitorRestTicks < (2 * objectIDs.size())");

			if (notInCache.isEmpty())
				return objects;

			// fetch all missing objects from datastore
			Collection<? extends JDOObject> fetchedObjects;
			int localTix = monitorRestTicks - (2 * objectIDs.size());
			monitorRestTicks -= localTix;
			ProgressMonitor subMonitor = new SubProgressMonitor(monitor, localTix);
			try {
				fetchedObjects = chunkedRetrieveJDOObjects(
//						new HashSet<JDOObjectID>(notInCache.keySet()), // workaround for hashset.keyset != serializable
						notInCache.keySet(), // the above is not necessary with the chunked version of the retrieve method.
						fetchGroups, maxFetchDepth, subMonitor
				);
			} finally {
				// done can be called multiple times and even if there was no beginTask (having no effect in both cases)
				subMonitor.done();
			}

			// put remaining objects in correct position of the result list
			int index;
			for(Iterator<? extends JDOObject> it = fetchedObjects.iterator(); it.hasNext(); ) {
				JDOObject freshObject = it.next();
				if (freshObject == null) {
					it.remove();
					continue;
				}
				JDOObjectID freshObjectID = (JDOObjectID) JDOHelper.getObjectId(freshObject);
				if (freshObjectID == null)
					throw new IllegalStateException("It seems like the Objects returned from the Bean are not detached, since one of their IDs is null!");

				index = notInCache.get(freshObjectID);
				objects.set(index, freshObject);
				monitor.worked(1);
				--monitorRestTicks;
			}

			// Note: The server may not return all wanted Objects, if e.g. they are of a different organisation.
			// 			 These objects are discarded! Hence, we're deleting the "null"-Objects of the list.
			if (notInCache.size() != fetchedObjects.size()) {
				for (Iterator<JDOObject> it = objects.iterator(); it.hasNext();) {
					if (it.next() == null)
						it.remove();
				}
			}

			localTix = monitorRestTicks - objectIDs.size();
			monitorRestTicks -= localTix;

			if (monitorRestTicks < objectIDs.size())
				throw new IllegalStateException("monitorRestTicks < objectIDs.size()");

			monitor.worked(localTix); // localTix is pretty often 0, but that is a valid parameter.

			Cache.sharedInstance().putAll(scope, fetchedObjects, fetchGroups, maxFetchDepth);
			objects.trimToSize();

			monitor.worked(monitorRestTicks);

			return objects;
		} finally {
			monitor.done();
		}
	}

	private List<JDOObject> chunkedRetrieveJDOObjects(Set<JDOObjectID> objectIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		// How many chunks should be used? Every chunk will cost an additional round-trip to the server, but provide higher granularity
		// of the process indicator.
		int maxChunkCount = 100;

		// How many objects should be in every chunk at least? A chunk will only be smaller, if there are less objectIDs to process left.
		int minChunkSize = 20;

		int chunkSize = (objectIDs.size() / maxChunkCount) + 1;
		if (chunkSize < minChunkSize)
			chunkSize = minChunkSize;

		List<JDOObject> result = new ArrayList<JDOObject>(objectIDs.size());

		String monitorName = String.format("Retrieving %s objects from server.", objectIDs.size());
		monitor.beginTask(monitorName, objectIDs.size());
		try {
			Set<JDOObjectID> chunkObjectIDs = new HashSet<JDOObjectID>();
			for (Iterator<JDOObjectID> it = objectIDs.iterator(); it.hasNext(); ) {
				chunkObjectIDs.add(it.next());

				if (chunkObjectIDs.size() >= chunkSize || !it.hasNext()) {
					// We double-wrap the SubProgressMonitors, because we must ensure that beginTask(...) and done() is called,
					// but we don't know if the implementor of retrieveJDOObjects(...) does it and we can't check.
					ProgressMonitor subMonitor = new SubProgressMonitor(monitor, chunkObjectIDs.size()); // sub-monitor 1 - definitely started and stopped
					try {
						subMonitor.beginTask(monitorName, 1);
						Collection<? extends JDOObject> retrievedJDOObjects = retrieveJDOObjects(
								chunkObjectIDs,
								fetchGroups, maxFetchDepth, new SubProgressMonitor(subMonitor, 1) // sub-monitor 2 - maybe totally unused
						);
						result.addAll(retrievedJDOObjects);
						chunkObjectIDs.clear();
					} catch (Exception e) {
						if (subMonitor != null)
							subMonitor.setCanceled(true);

						if (e instanceof RuntimeException)
							throw (RuntimeException)e;
						else
							throw new RuntimeException(e);
					} finally {
						subMonitor.done();
					}
				}
			}
		} finally {
			monitor.done();
		}

		return result;
	}

	/**
	 * Get JDO objects from the cache or the server.
	 * Object download for uncached objects is delegated by calling
	 * {@link #retrieveJDOObjects(Set, Set, int, IProgressMonitor)}
	 * for all uncached objects.
	 * <p>
	 * This is a convenience method that calls
	 * {@link #getJDOObjects(String, Collection, Set, int, IProgressMonitor)}.
	 *
	 * @param scope The cache scope to use
	 * @param objectIDs Wich objects to get
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. For every cached
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return All requested and existing JDO objects.
	 * @throws Exception in case of an error
	 */
	protected synchronized List<JDOObject> getJDOObjects(String scope, Collection<JDOObjectID> objectIDs, Set<String> fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(
				scope,
				objectIDs,
				fetchGroups.toArray(new String[0]),
				maxFetchDepth,
				monitor);
	}

	/**
	 * Get JDO objects from the cache or the server.
	 * Object download for uncached objects is delegated by calling
	 * {@link #retrieveJDOObjects(Set, Set, int, IProgressMonitor)}
	 * for all uncached objects.
	 * <p>
	 * This is a convenience method that calls
	 * {@link #getJDOObjects(String, Collection, Set, int, IProgressMonitor)}.
	 *
	 * @param scope The cache scope to use
	 * @param objectIDs Wich objects to get
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. For every cached
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return All requested and existing JDO objects.
	 * @throws Exception in case of an error
	 */
	protected synchronized List<JDOObject> getJDOObjects(String scope, JDOObjectID[] objectIDs, Set<String> fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(
				scope,
				CollectionUtil.array2ArrayList(objectIDs),
				fetchGroups,
				maxFetchDepth,
				monitor);
	}

	/**
	 * Get JDO objects from the cache or the server.
	 * Object download for uncached objects is delegated by calling
	 * {@link #retrieveJDOObjects(Set, Set, int, IProgressMonitor)}
	 * for all uncached objects.
	 * <p>
	 * This is a convenience method that calls
	 * {@link #getJDOObjects(String, Collection, Set, int, IProgressMonitor)}.
	 *
	 * @param scope The cache scope to use
	 * @param objectIDs Wich objects to get
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. For every cached
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return All requested and existing JDO objects.
	 * @throws Exception in case of an error
	 */
	protected synchronized List<JDOObject> getJDOObjects(String scope, JDOObjectID[] objectIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(
				scope,
				CollectionUtil.array2ArrayList(objectIDs),
				CollectionUtil.array2HashSet(fetchGroups),
				maxFetchDepth,
				monitor);
	}

	/**
	 * Get the cache shared instance.
	 * @return The cache shared instance.
	 */
	protected Cache getCache()
	{
		return cache;
	}
}
