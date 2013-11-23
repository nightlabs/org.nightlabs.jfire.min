package org.nightlabs.jfire.query.store;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.query.store.id.QueryStoreID;
import org.nightlabs.jfire.security.id.UserID;

/**
 *
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 *
 * @ejb.bean
 * 	name="jfire/ejb/JFireQueryStore/QueryStoreManager"
 *	jndi-name="jfire/ejb/JFireQueryStore/QueryStoreManager"
 *	type="Stateless"
 *  transaction-type="Container"
 *
 * @ejb.util generate="physical"
 *
 * @ejb.transaction	type="Required"
 *
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class QueryStoreManagerBean
extends BaseSessionBeanImpl
implements QueryStoreManagerRemote
{
	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The logger used in this class.
	 */
	private static final Logger logger = Logger.getLogger(QueryStoreManagerBean.class);

	/**
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	public QueryStore getQueryStore(QueryStoreID storeID, String[] fetchGroups,
		int maxFetchDepth)
	{
		return getQueryStores(Collections.singleton(storeID), fetchGroups, maxFetchDepth).iterator().next();
	}

	/**
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	public Collection<BaseQueryStore> getQueryStores(
		Set<QueryStoreID> storeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try
		{
			Collection<BaseQueryStore> stores = NLJDOHelper.getDetachedObjectList(pm, storeIDs, BaseQueryStore.class,
				fetchGroups, maxFetchDepth);

			if (stores == null)
				return null;

//			for (BaseQueryStore<?, ?> store : stores)
//			{
				// TODO: Authority check!
//				store.getAuthority().getUserRef(SecurityReflector.getUserDescriptor().getCompleteUserID()).
//			}

			return stores;
		}
		finally
		{
			pm.close();
		}
	}

	/**
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	public Collection<QueryStoreID> getQueryStoreIDs(Class<?> resultType, UserID ownerID,
		boolean allPublicAsWell, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try
		{
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
			{
				pm.getFetchPlan().setGroups(fetchGroups);
			}

			return BaseQueryStore.getQueryStoreIDs(pm, resultType, ownerID, allPublicAsWell);
		}
		finally
		{
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.query.store.QueryStoreManagerRemote#storeQueryCollection(org.nightlabs.jfire.query.store.QueryStore, java.lang.String[], int, boolean)
	 */
	public QueryStore storeQueryCollection(QueryStore queryStore, String[] fetchGroups,
		int maxFetchDepth, boolean get)
	{
		PersistenceManager pm = createPersistenceManager();
		try
		{
			return NLJDOHelper.storeJDO(pm, queryStore, get, fetchGroups, maxFetchDepth);
		}
		finally
		{
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.query.store.QueryStoreManagerRemote#removeQueryStore(org.nightlabs.jfire.query.store.QueryStore)
	 */
	public boolean removeQueryStore(QueryStore queryStore)
	{
		PersistenceManager pm = createPersistenceManager();
		// TODO: Authority check!
//		queryStore.getAuthority()
		try
		{
			pm.deletePersistent(queryStore);
			return true;
		}
		catch (JDOUserException userEx)
		{
			logger.warn("This istance is managed by another PersitenceManager!", userEx);
			return false;
		}
		finally
		{
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.query.store.QueryStoreManagerRemote#initialise()
	 */
	public void initialise() throws Exception
	{
		PersistenceManager pm = createPersistenceManager();;
		try {
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireQueryStoreEAR.MODULE_NAME);
			if (moduleMetaData != null)
				return;

			// create QueryStore tables.
			pm.getExtent(BaseQueryStore.class);

			// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
			moduleMetaData = pm.makePersistent(
					ModuleMetaData.createModuleMetaDataFromManifest(JFireQueryStoreEAR.MODULE_NAME, JFireQueryStoreEAR.class)
			);
		} finally {
			pm.close();
		}
	}

	/**
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	public QueryStoreID getDefaultQueryStoreID(Class<?> resultType, UserID ownerID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null) {
				pm.getFetchPlan().setGroups(fetchGroups);
			}
			return BaseQueryStore.getDefaultQueryStoreID(pm, resultType, ownerID);
		}
		finally {
			pm.close();
		}
	}
}
