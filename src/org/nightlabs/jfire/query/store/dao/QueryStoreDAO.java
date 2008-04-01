package org.nightlabs.jfire.query.store.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.query.store.BaseQueryStore;
import org.nightlabs.jfire.query.store.QueryStoreManager;
import org.nightlabs.jfire.query.store.QueryStoreManagerUtil;
import org.nightlabs.jfire.query.store.id.QueryStoreID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;


/**
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class QueryStoreDAO
	extends BaseJDOObjectDAO<QueryStoreID, BaseQueryStore<?, ?>>
{

	@Override
	protected Collection<BaseQueryStore<?, ?>> retrieveJDOObjects(Set<QueryStoreID> objectIDs,
		String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
		throws Exception
	{
		monitor.beginTask("Fetching QueryStores...", objectIDs.size());
		QueryStoreManager qsm = QueryStoreManagerUtil.getHome(
			SecurityReflector.getInitialContextProperties()).create();
		
		try
		{
			Collection<BaseQueryStore<?, ?>> result =
				qsm.getQueryStores(objectIDs, fetchGroups, maxFetchDepth);
			
			monitor.worked(objectIDs.size());
			monitor.done();
			return result;
		}
		catch (Exception e)
		{
			monitor.setCanceled(true);
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			
			throw new RuntimeException(e);
		}
	}

	public Collection<BaseQueryStore<?, ?>> getQueryStores(Set<QueryStoreID> storeIDs,
		String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, storeIDs, fetchGroups, maxFetchDepth, monitor);
	}
	
	public BaseQueryStore<?, ?> storeQueryStore(BaseQueryStore<?, ?> queryStore,
		String[] fetchGroups, int maxFetchDepth, boolean get, ProgressMonitor monitor)
	{
		QueryStoreManager qsm;
		try
		{
			monitor.beginTask("Saving QueryStore", 10);
			qsm = QueryStoreManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			monitor.worked(1);
			
			BaseQueryStore<?, ?> store = 
				qsm.storeQueryCollection(queryStore, fetchGroups, maxFetchDepth, get);
			monitor.worked(8);
			
			if (store != null)
			{
				getCache().put(null, store, fetchGroups, maxFetchDepth);
			}
			monitor.worked(1);
			monitor.done();
			
			return store;
		}
		catch (Exception e)
		{
			monitor.setCanceled(true);
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;
			
			throw new RuntimeException(e);
		}
	}
}
