package org.nightlabs.jfire.query.store;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.query.store.id.QueryStoreID;

/**
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 * 
 * @ejb.bean
 * 	name="jfire/ejb/JFireQueryStore/QueryStoreManager"
 *	jndi-name="jfire/ejb/JFireQueryStore/QueryStoreManager"
 *	type="Stateless"
 *	
 *
 * @ejb.util generate="physical"
 * 
 * @ejb.transaction	type="Required"
 * 
 */
public abstract class QueryStoreManagerBean
	extends BaseSessionBeanImpl
	implements SessionBean
{
	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	@Override
	public void setSessionContext(SessionContext sessionContext)
			throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	/**
	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#unsetSessionContext()
	 */
	@Override
	public void unsetSessionContext() {
		super.unsetSessionContext();
	}

	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate()
	throws CreateException
	{}

	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 *
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException
	{}
	
	/**
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public BaseQueryStore<?, ?> getQueryStore(QueryStoreID storeID, String[] fetchGroups,
		int maxFetchDepth)
	{
		return getQueryStores(Collections.singleton(storeID), fetchGroups, maxFetchDepth).iterator().next();
	}
	
	/**
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Collection<BaseQueryStore<?, ?>> getQueryStores(
		Set<QueryStoreID> storeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try
		{
			Collection<BaseQueryStore<?, ?>> stores = NLJDOHelper.getDetachedObjectList(pm, storeIDs, BaseQueryStore.class,
				fetchGroups, maxFetchDepth);
			
			if (stores == null)
				return null;
			
			for (BaseQueryStore<?, ?> store : stores)
			{
				// TODO: Authority check!
//				store.getAuthority().getUserRef(SecurityReflector.getUserDescriptor().getCompleteUserID()).
			}
			
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
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Collection<QueryStoreID> getQueryStoreIDs(Class<?> resultType,
		String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try
		{
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
			{
				pm.getFetchPlan().setGroups(fetchGroups);
			}
			
			Query query = 
				pm.newNamedQuery(BaseQueryStore.class, BaseQueryStore.QUERY_STORES_BY_RESULT_TYPE);
			
			return (Collection<QueryStoreID>) query.execute(resultType.getName());
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
	 * @ejb.transaction type="Required"
	 */
	public <R, Q extends AbstractJDOQuery<R>> BaseQueryStore<R, Q> storeQueryCollection(
		BaseQueryStore<R, Q> queryStore, String[] fetchGroups, int maxFetchDepth, boolean get)
	{
		PersistenceManager pm = getPersistenceManager();
		try
		{
			return NLJDOHelper.storeJDO(pm, queryStore, get, fetchGroups, maxFetchDepth);
		}
		finally
		{
			pm.close();
		}
	}
	
}
