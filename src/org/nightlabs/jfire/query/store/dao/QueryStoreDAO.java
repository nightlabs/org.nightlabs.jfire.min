package org.nightlabs.jfire.query.store.dao;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.query.store.BaseQueryStore;
import org.nightlabs.jfire.query.store.QueryStore;
import org.nightlabs.jfire.query.store.QueryStoreManagerRemote;
import org.nightlabs.jfire.query.store.id.QueryStoreID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * Simple DAO providing access to QueryStores as to fetch, store and remove them.
 *
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class QueryStoreDAO
	extends BaseJDOObjectDAO<QueryStoreID, BaseQueryStore>
{
	protected QueryStoreDAO() {}

	private static volatile QueryStoreDAO sharedInstance = null;

	/**
	 * @return The shared Instance of this class.
	 */
	public static QueryStoreDAO sharedInstance()
	{
		if (sharedInstance == null)
		{
			synchronized (QueryStoreDAO.class)
			{
				if (sharedInstance == null) sharedInstance = new QueryStoreDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	protected Collection<BaseQueryStore> retrieveJDOObjects(Set<QueryStoreID> objectIDs,
		String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) throws Exception
	{
		monitor.beginTask("Fetching QueryStores...", objectIDs.size());
		QueryStoreManagerRemote qsm = JFireEjb3Factory.getRemoteBean(QueryStoreManagerRemote.class, SecurityReflector.getInitialContextProperties());

		try
		{
			Collection<BaseQueryStore> result = qsm.getQueryStores(objectIDs, fetchGroups,
				maxFetchDepth);

			monitor.worked(objectIDs.size());
			monitor.done();
			return result;
		} catch (Exception e)
		{
			monitor.setCanceled(true);
			if (e instanceof RuntimeException)
			{
				throw (RuntimeException) e;
			}

			throw new RuntimeException(e);
		}
	}

	public QueryStore getQueryStore(QueryStoreID storeID,
		String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObject(null, storeID, fetchGroups, maxFetchDepth, monitor);
	}

	public Collection<BaseQueryStore> getQueryStores(Set<QueryStoreID> storeIDs,
		String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, storeIDs, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Returns all QueryStores created by the currently active user and all marked as publicly
	 * available if <code>allPublicAsWell == true</code>.
	 *
	 * @param resultType
	 *          the return type of the query collection.
	 * @param allPublicAsWell
	 *          Whether all QueryStores marked as publicly available shall be retrieved as well.
	 * @param fetchGroups
	 *          the fetch groups to use.
	 * @param maxFetchDepth
	 *          the maximum fetch depth.
	 * @param monitor
	 *          the progress monitor to use.
	 * @return all QueryStores created by the currently active user and all marked as publicly
	 *         available if <code>allPublicAsWell == true</code>.
	 */
	public Collection<BaseQueryStore> getQueryStoresByReturnType(Class<?> resultType,
		boolean allPublicAsWell, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getQueryStoresByReturnType(resultType, SecurityReflector.getUserDescriptor()
			.getUserObjectID(), allPublicAsWell, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Returns all QueryStores created by the given user and all marked as publicly available if
	 * <code>allPublicAsWell == true</code>.
	 *
	 * @param resultType
	 *          the return type of the query collection.
	 * @param selectedOwner
	 *          the owner of the queries to search for.
	 * @param fetchGroups
	 *          the fetch groups to use.
	 * @param maxFetchDepth
	 *          the maximum fetch depth.
	 * @param monitor
	 *          the progress monitor to use.
	 * @return all QueryStores created by the given user and all marked as publicly available if
	 *         <code>allPublicAsWell == true</code>.
	 */
	public Collection<BaseQueryStore> getQueryStoresByReturnType(Class<?> resultType,
		UserID selectedOwner, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getQueryStoresByReturnType(resultType, selectedOwner, false, fetchGroups, maxFetchDepth,
			monitor);
	}

	/**
	 * Returns all QueryStores created by the given user and all marked as publicly available if
	 * <code>allPublicAsWell == true</code> of the given result type.
	 *
	 * @param resultType
	 *          the return type of the query collection.
	 * @param selectedOwner
	 *          the owner of the queries to search for.
	 * @param allPublicAsWell
	 *          Whether all QueryStores marked as publicly available shall be retrieved as well.
	 * @param fetchGroups
	 *          the fetch groups to use.
	 * @param maxFetchDepth
	 *          the maximum fetch depth.
	 * @param monitor
	 *          the progress monitor to use.
	 * @return all QueryStores created by the given user and all marked as publicly available if
	 *         <code>allPublicAsWell == true</code> of the given result type.
	 */
	public Collection<BaseQueryStore> getQueryStoresByReturnType(Class<?> resultType,
		UserID selectedOwner, boolean allPublicAsWell, String[] fetchGroups, int maxFetchDepth,
		ProgressMonitor monitor)
	{
		try
		{
			Properties initialContext = SecurityReflector.getInitialContextProperties();
			QueryStoreManagerRemote qsm = JFireEjb3Factory.getRemoteBean(QueryStoreManagerRemote.class, initialContext);
			// QueryStoreManager qsm = QueryStoreManagerUtil.getHome(
			// SecurityReflector.getInitialContextProperties()).create();

			Collection<QueryStoreID> storedQueryCollections = qsm.getQueryStoreIDs(resultType,
				selectedOwner, allPublicAsWell, fetchGroups, maxFetchDepth);

			return getJDOObjects(null, storedQueryCollections, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e)
		{
			monitor.setCanceled(true);
			if (e instanceof RuntimeException)
			{
				throw (RuntimeException) e;
			}

			throw new RuntimeException(e);
		}
	}

	/**
	 * Stores the given QueryStore and detaches the newly persitet store with the given fetch groups
	 * and maximum fetch depth if <code>get == true</code>.
	 *
	 * @param queryStore
	 *          the QueryStore to persist.
	 * @param fetchGroups
	 *          the fetch groups to use for detaching.
	 * @param maxFetchDepth
	 *          the maximum fetch depth for detaching.
	 * @param get
	 *          whether the given QueryStore shall be detached and returned after being made
	 *          persistent.
	 * @param monitor
	 *          the progress monitor to use.
	 * @return the upToDate version of the given QueryStore after it has been made persistent if
	 *         <code>get == true</code>, <code>null</code> otherwise.
	 */
	public QueryStore storeQueryStore(QueryStore queryStore,
		String[] fetchGroups, int maxFetchDepth, boolean get, ProgressMonitor monitor)
	{
		try
		{
			monitor.beginTask("Saving QueryStore", 10);
			QueryStoreManagerRemote qsm = JFireEjb3Factory.getRemoteBean(QueryStoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
			queryStore.serialiseCollection();
			monitor.worked(1);

			QueryStore store = qsm.storeQueryCollection(queryStore, fetchGroups, maxFetchDepth, get);
			monitor.worked(8);

			if (store != null)
			{
				getCache().put(null, store, fetchGroups, maxFetchDepth);
			}
			monitor.worked(1);
			monitor.done();

			return store;
		} catch (Exception e)
		{
			monitor.setCanceled(true);
			if (e instanceof RuntimeException) throw (RuntimeException) e;

			throw new RuntimeException(e);
		}
	}

	/**
	 * Removes/Deletes the QueryStore.
	 *
	 * @param queryStore the QueryStore to remove
	 * @param monitor the {@link ProgressMonitor} to show the progress
	 * @return true if the queryStore has been removed and false otherwise
	 */
	public boolean removeQueryStore(QueryStore queryStore, ProgressMonitor monitor)
	{
		try
		{
			monitor.beginTask("Removing QueryStore", 3);
			QueryStoreManagerRemote qsm = JFireEjb3Factory.getRemoteBean(QueryStoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
			monitor.worked(1);

			boolean removed = qsm.removeQueryStore(queryStore);
			monitor.worked(2);
			monitor.done();

			return removed;
		}
		catch (Exception e)
		{
			monitor.setCanceled(true);
			if (e instanceof RuntimeException) throw (RuntimeException) e;

			throw new RuntimeException(e);
		}
	}

	/**
	 *
	 * @param resultType
	 *          the return type of the query collection.
	 * @param ownerID
	 *          the owner of the queries to search for.
   * @param fetchGroups
	 *          the fetch groups to use.
	 * @param maxFetchDepth
	 *          the maximum fetch depth.
	 * @param monitor
	 *          the progress monitor to use.
	 * @return the {@link BaseQueryStore} which is the default QueryStore of the
	 * 	given user with the given resultClass
	 */
	public QueryStore getDefaultQueryStore(Class<?> resultType, UserID ownerID,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			monitor.beginTask("Fetching default QueryStore", 3);
			QueryStoreManagerRemote qsm = JFireEjb3Factory.getRemoteBean(QueryStoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
			monitor.worked(1);
			QueryStoreID defaultQueryStoreID = qsm.getDefaultQueryStoreID(resultType, ownerID, fetchGroups, maxFetchDepth);
			monitor.worked(2);
			QueryStore queryStore = null;
			if (defaultQueryStoreID != null) {
				queryStore = getQueryStore(defaultQueryStoreID, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 1));
			}
			monitor.done();
			return queryStore;
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
