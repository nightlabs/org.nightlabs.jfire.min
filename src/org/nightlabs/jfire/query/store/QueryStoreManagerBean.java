package org.nightlabs.jfire.query.store;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.query.store.id.QueryStoreID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.version.MalformedVersionException;

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
public abstract class QueryStoreManagerBean
	extends BaseSessionBeanImpl
	implements SessionBean
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
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public BaseQueryStore getQueryStore(QueryStoreID storeID, String[] fetchGroups,
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
	public Collection<BaseQueryStore> getQueryStores(
		Set<QueryStoreID> storeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
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
	public Collection<QueryStoreID> getQueryStoreIDs(Class<?> resultType, UserID ownerID, 
		boolean allPublicAsWell, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
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
	
	/**
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public BaseQueryStore storeQueryCollection(BaseQueryStore queryStore, String[] fetchGroups, 
		int maxFetchDepth, boolean get)
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
	
	/**
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public boolean removeQueryStore(BaseQueryStore queryStore)
	{
		PersistenceManager pm = getPersistenceManager();
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
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	public void initialise()
	{
		PersistenceManager pm = getPersistenceManager();;
		try
		{
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, "JFireQueryStore");
			if (moduleMetaData != null)
				return;

			// create QueryStore tables. 
			pm.getExtent(BaseQueryStore.class);
			
			// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
			moduleMetaData = new ModuleMetaData("JFireQueryStore", "0.9.3-0-beta", "0.9.3-0-beta");
			pm.makePersistent(moduleMetaData);
		} catch (MalformedVersionException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			pm.close();
		}
	}
}
