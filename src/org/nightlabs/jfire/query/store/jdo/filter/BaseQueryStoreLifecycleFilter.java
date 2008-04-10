package org.nightlabs.jfire.query.store.jdo.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleRemoteEvent;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.SimpleLifecycleListenerFilter;
import org.nightlabs.jfire.query.store.BaseQueryStore;
import org.nightlabs.jfire.security.id.UserID;

/**
 * Serverside IJDOLifecycleListenerFilter that filters QueryStores according to a given owner
 * and result type. All stores that match this result type and are either publicly available or
 * are owner by the given User are returned.
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class BaseQueryStoreLifecycleFilter
	extends SimpleLifecycleListenerFilter
{
	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The result type class name of the QueryCollection, for which all
	 * QueryStoreIDs shall be returned.
	 */
	private String resultTypeClassName;
	
	/**
	 * The owner for whom shall be filtered.
	 */
	private UserID ownerID;

	/**
	 * @param candidateClass
	 * @param includeSubclasses
	 * @param lifecycleStates
	 */
	public BaseQueryStoreLifecycleFilter(UserID ownerID, Class<?> resultType, boolean includeSubclasses,
		JDOLifecycleState... lifecycleStates)
	{
		super(BaseQueryStore.class, includeSubclasses, lifecycleStates);
		assert resultType != null;
		this.resultTypeClassName = resultType.getName();
		this.ownerID = ownerID;
	}

	@Override
	public Collection<DirtyObjectID> filter(JDOLifecycleRemoteEvent event)
	{
		Collection<DirtyObjectID> filteredIDs = super.filter(event);
		if (filteredIDs == null)
			return null;
		
		List<DirtyObjectID> dirtyObjectIDs = new ArrayList<DirtyObjectID>( filteredIDs );
		Set<DirtyObjectID> matchingIDs = new HashSet<DirtyObjectID>();
		final PersistenceManager pm = event.getPersistenceManager();
		
		for (DirtyObjectID dirtyObjectID : dirtyObjectIDs)
		{
			BaseQueryStore<?, ?> store = (BaseQueryStore<?, ?>)
				pm.getObjectById(dirtyObjectID.getObjectID());
			
			if (resultTypeClassName.equals(store.getResultClassName()))
			{
				// Skip the store if a user is set and the current store is neither public nor the owner set
				// is its owner.
				if (!store.isPubliclyAvailable() && ownerID != null && ! ownerID.equals(store.getOwnerID()))
					continue;
				
				matchingIDs.add(dirtyObjectID);
			}
		}
		
		return matchingIDs;
	}
}
