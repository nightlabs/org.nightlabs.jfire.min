/*
 * Created on Jul 29, 2005
 */
package org.nightlabs.ipanema.base.jdo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.nightlabs.ipanema.base.jdo.cache.Cache;
import org.nightlabs.util.Utils;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public abstract class JDOObjectProvider
{
	private Cache cache = Cache.sharedInstance();

	private boolean inRetrieveJDOObject_StringArray = false;
	private boolean inRetrieveJDOObject_Set = false;
	private boolean inRetrieveJDOObjects_StringArray_StringArray = false;
	private boolean inRetrieveJDOObjects_Set_Set = false;
	private boolean inRetrieveJDOObjects_Set_StringArray = false;

	public JDOObjectProvider()
	{
	}

	/**
	 * Decide whether you want to use <tt>String[]</tt> or <tt>Set</tt>
	 * and then throw an <tt>UnsupportedOperationException</tt> in the
	 * other method.
	 */
	protected Object retrieveJDOObject(
			String scope, Object objectID, String[] fetchGroups)
	throws Exception
	{
		if (inRetrieveJDOObject_StringArray)
			throw new IllegalStateException("You have to override at least one of the retrieveJDOObject(...) methods!");

		inRetrieveJDOObject_StringArray = true;
		try {
			Set x = new HashSet();
			for(int i = 0; i < fetchGroups.length; i++)
				x.add(fetchGroups[i]);
			return retrieveJDOObject(scope, objectID, x);
		} finally {
			inRetrieveJDOObject_StringArray = false;
		}
	}

	/**
	 * @see #retrieveJDOObject(String, Object, String[])
	 */
	protected Object retrieveJDOObject(
			String scope, Object objectID, Set fetchGroups)
	throws Exception
	{
		if (inRetrieveJDOObject_Set)
			throw new IllegalStateException("You have to override at least one of the retrieveJDOObject(...) methods!");

		inRetrieveJDOObject_Set = true;
		try {
			return retrieveJDOObject(scope, objectID, (String[])Utils.collection2TypedArray(fetchGroups, String.class));
		} finally {
			inRetrieveJDOObject_Set = false;
		}
	}

	protected Collection retrieveJDOObjects(
			String scope, Object[] objectIDs, String[] fetchGroups)
	throws Exception
	{
		if(inRetrieveJDOObjects_StringArray_StringArray)
			throw new IllegalStateException("You have to override at least one of the retrieveJDOObjects(...) methods!");

		inRetrieveJDOObjects_StringArray_StringArray = true;
		try {
			return retrieveJDOObjects(scope, Utils.array2HashSet(objectIDs), Utils.array2HashSet(fetchGroups));
		} finally {
			inRetrieveJDOObjects_StringArray_StringArray = false;
		}
	}

	protected Collection retrieveJDOObjects(
			String scope, Set objectIDs, Set fetchGroups)
	throws Exception
	{
		if (inRetrieveJDOObjects_Set_Set)
			throw new IllegalStateException("You have to override at least one of retrieveJDOObjects(String, Object[], String[]) and retrieveJDOObjects(String, Set, Set)");

		inRetrieveJDOObjects_Set_Set = true;
		try {
			return retrieveJDOObjects(scope, objectIDs, (String[])Utils.collection2TypedArray(fetchGroups, String.class));
		} finally {
			inRetrieveJDOObjects_Set_Set = false;
		}
	}

	protected Collection retrieveJDOObjects(
			String scope, Set objectIDs, String[] fetchGroups)
	throws Exception
	{
		if (inRetrieveJDOObjects_Set_StringArray)
			throw new IllegalStateException("You have to override at least one of retrieveJDOObjects(String, Object[], String[]) and retrieveJDOObjects(String, Set, Set)");

		inRetrieveJDOObjects_Set_StringArray = true;
		try {
			return retrieveJDOObjects(scope, (new ArrayList(objectIDs)).toArray(), fetchGroups);
		} finally {
			inRetrieveJDOObjects_Set_StringArray = false;
		}
	}

	protected synchronized Object getJDOObject(
			String scope, Object objectID, Set fetchGroups)
	{
		return getJDOObject(scope, objectID, (String[])Utils.collection2TypedArray(fetchGroups, String.class));
	}

	protected synchronized Object getJDOObject(
			String scope, Object objectID, String[] fetchGroups)
	{
		try {
			Object res = cache.get(scope, objectID, fetchGroups);
			if (res == null) {
				res = retrieveJDOObject(scope, objectID, fetchGroups);
				cache.put(scope, res, fetchGroups);
			}
			return res;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	protected synchronized Collection getJDOObjects(String scope, Object[] objectIDs, String[] fetchGroups)
	{
		return getJDOObjects(scope, Utils.array2HashSet(objectIDs), Utils.array2HashSet(fetchGroups));
	}

	protected synchronized Collection getJDOObjects(String scope, Collection objectIDs, String[] fetchGroups)
	{
		return getJDOObjects(scope, objectIDs, Utils.array2HashSet(fetchGroups));
	}

	protected synchronized Collection getJDOObjects(String scope, Collection objectIDs, Set fetchGroups)
	{
		if (!(objectIDs instanceof Set))
			objectIDs = new HashSet(objectIDs);

		try 
		{
			ArrayList objects = new ArrayList();
			Set fetchObjectIDs = new HashSet();
			for(Iterator it = objectIDs.iterator(); it.hasNext(); )
			{
				Object objectID = it.next();
				Object res = cache.get(scope, objectID, fetchGroups);
				if(res != null)
					objects.add(res);
				else
					fetchObjectIDs.add(objectID);
			}

			if (fetchObjectIDs.size() > 0) 
			{
				Collection fetchedObjects = retrieveJDOObjects(scope, fetchObjectIDs, fetchGroups);
				cache.putAll(scope, fetchedObjects, fetchGroups);
				objects.addAll(fetchedObjects);
			}
			return objects;
		} 
		catch (Exception x) 
		{
			throw new RuntimeException(x);
		}
	}
	
	protected Cache getCache() {
		return cache;
	}

}
