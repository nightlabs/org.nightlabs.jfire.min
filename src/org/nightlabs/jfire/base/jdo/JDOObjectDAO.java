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

import org.eclipse.core.runtime.IProgressMonitor;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.base.resource.Messages;
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
 * 
 * @deprecated This is only for backward compatibility, all DAOs should be implemented in the server projects and
 * 		should extend {@link BaseJDOObjectDAO}.
 */
public abstract class JDOObjectDAO<JDOObjectID, JDOObject>
{

	/**
	 * The cache shared instance.
	 */
	private Cache cache = Cache.sharedInstance();

	/**
	 * Default constructor.
	 */
	public JDOObjectDAO()
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
	 * 
	 * @param objectID Wich object to get
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT} 
	 * @param monitor The progress monitor for this action. After downloading the
	 * 					object, <code>monitor.worked(1)</code> should be called.
	 * @return All requested and existing JDO objects.
	 * @throws Exception in case of an error
	 */
	protected JDOObject retrieveJDOObject(JDOObjectID objectID, String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor)
	throws Exception
	{
		Set<JDOObjectID> objectIDs = new HashSet<JDOObjectID>(1);
		objectIDs.add(objectID);
		Collection<JDOObject> objects = retrieveJDOObjects(objectIDs, fetchGroups, maxFetchDepth, monitor);
		if(objects == null || objects.isEmpty())
			return null;
		return objects.iterator().next();
	}
	
	/**
	 * Retrieve JDO objects from the server. This method will be called by
	 * {@link #getJDOObjects(String, Collection, Set, int, IProgressMonitor)}
	 * for all objects that are not already in the cache.
	 * 
	 * @param objectIDs Wich objects to get
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT} 
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> should be called.
	 * @return All requested and existing JDO objects.
	 * @throws Exception in case of an error
	 */
	protected abstract Collection<JDOObject> retrieveJDOObjects(Set<JDOObjectID> objectIDs, String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor)
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
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	protected synchronized JDOObject getJDOObject(String scope, JDOObjectID objectID, String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor)
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
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	protected synchronized List<JDOObject> getJDOObjects(String scope, Collection<JDOObjectID> objectIDs, String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor)
	{
		if (objectIDs == null || objectIDs.isEmpty())
			return new ArrayList<JDOObject>(1);
		
		ArrayList<JDOObject> objects = new ArrayList<JDOObject>(objectIDs.size());
		if (objectIDs.isEmpty()) {
			monitor.done();
			return objects;
		}
			
		monitor.beginTask(String.format(Messages.getString("jdo.JDOObjectDAO.gettingObjects"), objectIDs.size()), objectIDs.size()); //$NON-NLS-1$
//		List<JDOObject> fromCache = new ArrayList<JDOObject>();
		List<JDOObjectID> listetIDs = new ArrayList<JDOObjectID>(objectIDs);
//		Set<JDOObjectID> notInCache = new HashSet<JDOObjectID>();
		Map<JDOObjectID, Integer> notInCache = new HashMap<JDOObjectID, Integer>();
		
		// search the cache for the wanted Objects
		for (int i=0; i < listetIDs.size(); i++) {
			JDOObject cachedObject = (JDOObject) cache.get(scope, listetIDs.get(i), fetchGroups, maxFetchDepth);
			if (cachedObject != null) {
				objects.add(cachedObject);
				monitor.worked(1);
			}
			else {
				notInCache.put(listetIDs.get(i), i); // if not in cache save (objectID, position)
				objects.add(null); // fill the result array, so that we're later able to replace the 
													 // JDOObject at the correct position
			}
		}

		if (notInCache.isEmpty())
			return objects;

		// fetch all missing objects from datastore
		Collection<JDOObject> fetchedObjects;
		try { //                               workaround for hashset.keyset != serializable
			fetchedObjects = retrieveJDOObjects(new HashSet<JDOObjectID>(notInCache.keySet()), fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException("Error occured while fetching Objects from the data store!\n", e); //$NON-NLS-1$
		}
		
		// put remaining objects in correct position of the result list
		int index;
		for(JDOObject freshObject : fetchedObjects) {
			if (freshObject == null)
				continue;
			index = notInCache.get(JDOHelper.getObjectId(freshObject));
			objects.set(index, freshObject);
			monitor.worked(1);
		}

		// Note: The server may not return all wanted Objects, if e.g. they are of a different organisation.
		// 			 These objects are discarded! Hence, we're deleting the "null"-Objects of the list.
		if (notInCache.size() != fetchedObjects.size()) {
			for (Iterator<JDOObject> it = objects.iterator(); it.hasNext();) {
				if (it.next() == null)
					it.remove();
			}
		}
		
		monitor.done();
		objects.trimToSize();	
		return objects;		
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
	protected synchronized List<JDOObject> getJDOObjects(String scope, Collection<JDOObjectID> objectIDs, Set<String> fetchGroups, int maxFetchDepth, IProgressMonitor monitor)
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
	protected synchronized List<JDOObject> getJDOObjects(String scope, JDOObjectID[] objectIDs, Set<String> fetchGroups, int maxFetchDepth, IProgressMonitor monitor)
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
	protected synchronized List<JDOObject> getJDOObjects(String scope, JDOObjectID[] objectIDs, String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor)
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
