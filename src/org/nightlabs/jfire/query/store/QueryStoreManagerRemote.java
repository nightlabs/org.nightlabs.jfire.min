package org.nightlabs.jfire.query.store;

import java.util.Collection;
import java.util.Set;

import javax.ejb.Remote;

import org.nightlabs.jfire.query.store.id.QueryStoreID;
import org.nightlabs.jfire.security.id.UserID;

@Remote
public interface QueryStoreManagerRemote {

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	String ping(String message);

	/**
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	QueryStore getQueryStore(QueryStoreID storeID, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	Collection<BaseQueryStore> getQueryStores(Set<QueryStoreID> storeIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	Collection<QueryStoreID> getQueryStoreIDs(Class<?> resultType,
			UserID ownerID, boolean allPublicAsWell, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	QueryStore storeQueryCollection(QueryStore queryStore,
			String[] fetchGroups, int maxFetchDepth, boolean get);

	/**
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	boolean removeQueryStore(QueryStore queryStore);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 * @throws Exception TODO
	 */
	void initialise() throws Exception;

	/**
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	QueryStoreID getDefaultQueryStoreID(Class<?> resultType, UserID ownerID,
			String[] fetchGroups, int maxFetchDepth);

}