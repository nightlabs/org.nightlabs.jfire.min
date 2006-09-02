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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.cache.Cache;
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
	 * works by calling {@link #retrieveJDOObjects(Collection, Set, int, IProgressMonitor)}
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
		Collection<JDOObjectID> objectIDs = new ArrayList<JDOObjectID>(1);
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
	protected abstract Collection<JDOObject> retrieveJDOObjects(Collection<JDOObjectID> objectIDs, String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor)
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
	protected synchronized JDOObject getJDOObject(String scope, JDOObjectID objectID, String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor)
	{
		try {
			JDOObject res = (JDOObject)cache.get(scope, objectID, fetchGroups, maxFetchDepth);
			if (res == null) {
				res = retrieveJDOObject(objectID, fetchGroups, maxFetchDepth, monitor);
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
	 * {@link #retrieveJDOObjects(Collection, Set, int, IProgressMonitor)}
	 * for all uncached objects.
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
	protected synchronized List<JDOObject> getJDOObjects(String scope, Collection<JDOObjectID> objectIDs, String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor)
	{
		try	{
			ArrayList<JDOObject> objects = new ArrayList<JDOObject>();
			
			if(objectIDs.size() == 1) {
				objects.add(getJDOObject(scope, objectIDs.iterator().next(), fetchGroups, maxFetchDepth, monitor));
				return objects;
			}
			
			Set<JDOObjectID> fetchObjectIDs = new HashSet<JDOObjectID>();
			for (JDOObjectID objectID : objectIDs) {
				JDOObject res = (JDOObject)cache.get(scope, objectID, fetchGroups, maxFetchDepth);
				if(res != null) {
					objects.add(res);
					monitor.worked(1);
				}
				else
					fetchObjectIDs.add(objectID);
			}

			if (fetchObjectIDs.size() > 0) {
				if(fetchObjectIDs.size() == 1) {
					JDOObject fetchedObject = retrieveJDOObject(fetchObjectIDs.iterator().next(), fetchGroups, maxFetchDepth, monitor);
					cache.put(scope, fetchedObject, fetchGroups, maxFetchDepth);
					objects.add(fetchedObject);
					monitor.worked(1);
				} else {
					Collection<JDOObject> fetchedObjects = retrieveJDOObjects(fetchObjectIDs, fetchGroups, maxFetchDepth, monitor);
					cache.putAll(scope, fetchedObjects, fetchGroups, maxFetchDepth);
					objects.addAll(fetchedObjects);
					monitor.worked(fetchedObjects.size());
				}
			}
			return objects;
			
		} catch (Exception x)	{
			throw new RuntimeException(x);
		}
	}

	/**
	 * Get JDO objects from the cache or the server.
	 * Object download for uncached objects is delegated by calling 
	 * {@link #retrieveJDOObjects(Collection, Set, int, IProgressMonitor)}
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
	 * {@link #retrieveJDOObjects(Collection, Set, int, IProgressMonitor)}
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
	 * {@link #retrieveJDOObjects(Collection, Set, int, IProgressMonitor)}
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
