package org.nightlabs.jfire.query.store;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.listener.StoreCallback;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.query.store.dao.QueryStoreDAO;
import org.nightlabs.jfire.query.store.id.QueryStoreID;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.util.CollectionUtil;

import com.thoughtworks.xstream.XStream;

/**
 * I am a container for any number of {@link AbstractSearchQuery}s wrapped in a
 * {@link QueryCollection}.
 *
 * <p><b>Important:</b> When sending a QueryStore from a client to the server you have to make sure
 * 	{@link #serialiseCollection()} has been called before doing so, otherwise no query data will be
 * 	persisted! <br />
 * 	We don't serialise the collection every time it is set, since it may be changed afterwards which
 * 	would result in an additional serialisation or lost information. <br />
 * 	By using the {@link QueryStoreDAO} it is done implicitly when saving a QueryStore.
 * </p>
 *
 * @jdo.persistence-capable
 *	identity-type="application"
 *	objectid-class="org.nightlabs.jfire.query.store.id.QueryStoreID"
 *	detachable="true"
 *	table="JFireQueryStore_BaseQueryStore"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, queryStoreID, ownerID"
 *
 * @jdo.fetch-group
 * 	name="BaseQueryStore.owner"
 * 	fields="owner"
 *
 * @jdo.fetch-group
 * 	name="BaseQueryStore.name"
 * 	fields="name"
 *
 * @jdo.fetch-group
 * 	name="BaseQueryStore.description"
 * 	fields="description"
 *
 * @jdo.fetch-group
 * 	name="BaseQueryStore.authority"
 * 	fields="authority"
 *
 * @jdo.fetch-group
 * 	name="BaseQueryStore.serialisedQueries"
 * 	fields="serialisedQueries"
 *
 * @jdo.query name="getAllPublicQueryStoreIDsByResultType"
 * 	query="SELECT JDOHelper.getObjectId(this)
 * 				 WHERE this.resultClassName == :givenClassName
 * 							 && this.publiclyAvailable == true"
 *
 * @jdo.query name="getQueryStoreIDsOfOwnerByResultType"
 * 	query="SELECT JDOHelper.getObjectId(this)
 * 				 WHERE this.resultClassName == :givenClassName
 * 							 && this.organisationID == :givenOrganisationID
 * 							 && this.ownerID == :givenUserID"
 *
 * @jdo.query name="getDefaultQueryStoreIDOfOwnerWithResultType"
 * 	query="SELECT JDOHelper.getObjectId(this)
 * 				 WHERE this.resultClassName == :givenClassName
 * 							 && this.organisationID == :givenOrganisationID
 * 							 && this.ownerID == :givenUserID
 * 							 && this.defaultQuery == true"
 *
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
@PersistenceCapable(
	objectIdClass=QueryStoreID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireQueryStore_BaseQueryStore")
@FetchGroups({
	@FetchGroup(
		name=BaseQueryStore.FETCH_GROUP_OWNER,
		members=@Persistent(name="owner")),
	@FetchGroup(
		name=BaseQueryStore.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		name=BaseQueryStore.FETCH_GROUP_DESCRIPTION,
		members=@Persistent(name="description")),
	@FetchGroup(
		name=BaseQueryStore.FETCH_GROUP_AUTHORITY,
		members=@Persistent(name="authority")),
	@FetchGroup(
		name=BaseQueryStore.FETCH_GROUP_SERIALISED_QUERIES,
		members=@Persistent(name="serialisedQueries"))
})
@Queries({
	@javax.jdo.annotations.Query(
		name="getAllPublicQueryStoreIDsByResultType",
		value="SELECT JDOHelper.getObjectId(this) WHERE this.resultClassName == :givenClassName && this.publiclyAvailable == true"),
	@javax.jdo.annotations.Query(
		name="getQueryStoreIDsOfOwnerByResultType",
		value="SELECT JDOHelper.getObjectId(this) WHERE this.resultClassName == :givenClassName && this.organisationID == :givenOrganisationID && this.ownerID == :givenUserID"),
	@javax.jdo.annotations.Query(
		name="getDefaultQueryStoreIDOfOwnerWithResultType",
		value="SELECT JDOHelper.getObjectId(this) WHERE this.resultClassName == :givenClassName && this.organisationID == :givenOrganisationID && this.ownerID == :givenUserID && this.defaultQuery == true")
})
public class BaseQueryStore
	implements Serializable, StoreCallback, QueryStore
{
	/**
	 * FetchGroup name for the Owner-FetchGroup.
	 */
	public static final String FETCH_GROUP_OWNER = "BaseQueryStore.owner";

	/**
	 * FetchGroup name for the Name-FetchGroup.
	 */
	public static final String FETCH_GROUP_NAME = "BaseQueryStore.name";

	/**
	 * FetchGroup name for the Description-FetchGroup.
	 */
	public static final String FETCH_GROUP_DESCRIPTION = "BaseQueryStore.description";

	/**
	 * FetchGroup name for the Authority-FetchGroup.
	 */
	public static final String FETCH_GROUP_AUTHORITY = "BaseQueryStore.authority";

	/**
	 * FetchGroup name for the SerialisedQueries-FetchGroup.
	 */
	public static final String FETCH_GROUP_SERIALISED_QUERIES = "BaseQueryStore.serialisedQueries";

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 2L;

//	/**
//	 * This is the name of the member returned by {@link QueryCollection#getResultClassName()}.
//	 */
//	private static final String QUERYCOLLECTION_RESULTCLASS_NAME = "resultClassName";

	/**
	 * Returns all {@link QueryStoreID}s of the stores that conform to the given parameters.
	 *
	 * @param pm the {@link PersistenceManager} to use.
	 * @param resultClass the resultClass of the stored QueryCollection
	 * @param ownerID the owner of the QueryStore.
	 * @param allPublicAsWell whether all publicly available QueryStores shall be considered as well.
	 * @return all {@link QueryStoreID}s of the stores that conform to the given parameters.
	 */
	public static Set<QueryStoreID> getQueryStoreIDs(PersistenceManager pm,
		Class<?> resultClass, UserID ownerID, boolean allPublicAsWell)
	{
		assert pm != null;
		assert resultClass != null;
		assert ownerID != null;
		Query query = pm.newNamedQuery(BaseQueryStore.class, "getQueryStoreIDsOfOwnerByResultType");

		Collection<QueryStoreID> queryResult = CollectionUtil.castCollection(
				(Collection<?>)query.execute(resultClass.getName(), ownerID.organisationID, ownerID.userID)
		);

		Set<QueryStoreID> result = NLJDOHelper.getDetachedQueryResultAsSet(pm, queryResult);
		if (allPublicAsWell)
		{
			// create new Set, since the result of JDOHelper.getDetachedQueryResultAsSet(..) may return Collections.emptySet()
			result = new HashSet<QueryStoreID>(result);
			query = pm.newNamedQuery(BaseQueryStore.class, "getAllPublicQueryStoreIDsByResultType");
			queryResult = CollectionUtil.castCollection(
					(Collection<?>) query.execute(resultClass.getName())
			);
			Set<QueryStoreID> tmpIds = NLJDOHelper.getDetachedQueryResultAsSet(pm, queryResult);
			result.addAll(tmpIds);
		}
		return result;
	}

	/**
	 * Returns the {@link QueryStoreID} of the QueryStore which is the defaultQueryStore for
	 * the given resultClass and the given ownerID.
	 *
	 * @param pm the {@link PersistenceManager} to use.
	 * @param resultClass the resultClass of the stored QueryCollection
	 * @param ownerID the owner of the QueryStore.
	 * @return the {@link QueryStoreID} of the QueryStore which is the defaultQueryStore for
	 * the given resultClass and the given ownerID
	 */
	public static QueryStoreID getDefaultQueryStoreID(PersistenceManager pm,
			Class<?> resultClass, UserID ownerID)
	{
		return getDefaultQueryStoreID(pm, resultClass.getName(), ownerID.organisationID, ownerID.userID);
	}

	/**
	 * Returns the {@link QueryStoreID} of the QueryStore which is the defaultQueryStore for
	 * the given resultClassname and the given ownerID.
	 *
	 * @param pm the {@link PersistenceManager} to use.
	 * @param resultClassName the full qualified name of the resultClass of the stored QueryCollection
	 * @param ownerID the owner of the QueryStore.
	 * @return the {@link QueryStoreID} of the QueryStore which is the defaultQueryStore for
	 * the given resultClass and the given ownerID
	 */
	public static QueryStoreID getDefaultQueryStoreID(PersistenceManager pm,
			String resultClassName, String organisationID, String userID)
	{
		assert pm != null;
		assert resultClassName != null;
		assert organisationID != null;
		assert userID != null;
		Query query = pm.newNamedQuery(BaseQueryStore.class, "getDefaultQueryStoreIDOfOwnerWithResultType");
		Collection<QueryStoreID> queryResult = CollectionUtil.castCollection(
				(Collection<?>) query.execute(resultClassName, organisationID, userID)
		);
		Set<QueryStoreID> result = NLJDOHelper.getDetachedQueryResultAsSet(pm, queryResult);
		if (result == null || result.isEmpty()) {
			return null;
		}
		if (result.size() > 1) {
			throw new IllegalStateException("There exists more than one default query store for the resultClass "+resultClassName+" and the user "+UserID.create(organisationID, userID));
		}
		return result.iterator().next();
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long queryStoreID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private String ownerID;

	/**
	 * @jdo.field
	 * 	persistence-modifier="persistent"
	 * 	null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private User owner;

	/**
	 * @jdo.field
	 * 	persistence-modifier="persistent"
	 *  mapped-by="queryStore"
	 *  dependent="true"
	 * 	default-fetch-group="true"
	 */
	@Persistent(
		dependent="true",
		mappedBy="queryStore",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private QueryStoreName name;

	/**
	 * @jdo.field
	 * 	persistence-modifier="persistent"
	 *  mapped-by="queryStore"
	 *  dependent="true"
	 * 	default-fetch-group="true"
	 */
	@Persistent(
		dependent="true",
		mappedBy="queryStore",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private QueryStoreDescription description;

	/**
	 * @jdo.field
	 * 	persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Authority authority;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	// TODO it should be QueryCollection<?>, but the DataNucleus enhancer seems to get a problem with it ("Invalid element type: ?"). Hence, we temporarily omit the generic type.
	@SuppressWarnings("unchecked")
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient QueryCollection deSerialisedQueries;

	/**
	 * @jdo.field persistence-modifier="persistent" default-fetch-group="true"
	 * @jdo.column sql-type="BLOB"
	 */
	@Persistent(
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(sqlType="BLOB")
	private byte[] serialisedQueries;

	/**
	 * @jdo.field
	 * 	persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean publiclyAvailable;

	/**
	 * The fully qualified classname of the result type of the stored QueryCollection.
	 *
	 * @jdo.field
	 *	persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String resultClassName;

	/**
	 * Determines if this BaseQueryStore is the default QueryStore for
	 * the given user and the given resultClass.
	 *
	 * @jdo.field
	 *	persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean defaultQuery = false;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.query.store.QueryStore#setQueryCollection(org.nightlabs.jdo.query.QueryCollection)
	 */
	public void setQueryCollection(QueryCollection<?> queries)
	{
		this.deSerialisedQueries = queries;
		this.resultClassName = queries == null ? "" : queries.getResultClassName();
	}

	/**
	 * @deprecated only for JDO
	 */
	@Deprecated
	public BaseQueryStore()
	{
	}

	public BaseQueryStore(User owner, long queryStoreID, QueryCollection<?> queryCollection)
	{
		this(owner, queryStoreID, queryCollection, false);
	}

	public BaseQueryStore(User owner, long queryStoreID, QueryCollection<?> queryCollection,
		boolean publiclyAvailable)
	{
		assert owner != null;

		this.owner = owner;
		this.ownerID = owner.getUserID();
		this.organisationID = owner.getOrganisationID();
		this.queryStoreID = queryStoreID;
		this.publiclyAvailable = publiclyAvailable;
		this.name = new QueryStoreName(this);
		this.description = new QueryStoreDescription(this);
		setQueryCollection(queryCollection);
	}

//	/**
//	 * Returns the QueryCollection, which might need to deserialise the serialised QueryCollection.
//	 * @return the managed QueryCollection.
//	 */
//	@SuppressWarnings("unchecked")
//	public QueryCollection<?> getQueryCollection()
//	{
//		if (deSerialisedQueries != null)
//			return deSerialisedQueries;
//
//		if (serialisedQueries == null || serialisedQueries.length == 0)
//			return null;
//
//		if (deSerialisedQueries == null)
//		{
//			final ByteArrayInputStream inputStream = new ByteArrayInputStream(serialisedQueries);
//			final InflaterInputStream zippedStream = new InflaterInputStream(inputStream);
//			final XMLDecoder decoder = new XMLDecoder(zippedStream);
//			deSerialisedQueries = (QueryCollection<?>) decoder.readObject();
//			decoder.close();
//		}
//
//		return deSerialisedQueries;
//	}
//
//	/**
//	 * This is only called by the DAO in order to prohibit the serialisation of the QueryCollection
//	 * when calling {@link #setQueryCollection(QueryCollection)}. <br />
//	 * <p><b>Note:</b> This has to be called from the outside BEFORE subclasses are send away!! This
//	 * 	is usually done in the DAOs.
//	 * </p>
//	 */
//	public void serialiseCollection()
//	{
//		if (deSerialisedQueries == null || deSerialisedQueries == null)
//		{
//			serialisedQueries = null;
//		}
//		else
//		{
//			final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//			DeflaterOutputStream zippedStream = new DeflaterOutputStream(outStream);
//			final XMLEncoder encoder = new XMLEncoder(zippedStream);
//			encoder.setPersistenceDelegate(
//				QueryCollection.class,
//				new DefaultPersistenceDelegate(new String[] { QUERYCOLLECTION_RESULTCLASS_NAME })
//				);
//			encoder.writeObject(deSerialisedQueries);
//			encoder.close();
//			serialisedQueries = outStream.toByteArray();
//		}
//	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.query.store.QueryStore#getQueryCollection()
	 */
	public QueryCollection<?> getQueryCollection()
	{
		if (deSerialisedQueries != null)
			return deSerialisedQueries;

		if (serialisedQueries == null || serialisedQueries.length == 0)
			return null;

		if (deSerialisedQueries == null)
		{
			final ByteArrayInputStream inputStream = new ByteArrayInputStream(serialisedQueries);
			final InflaterInputStream zipStream = new InflaterInputStream(inputStream);
			try {
				XStream xStream = new XStream();
				deSerialisedQueries = (QueryCollection<?>) xStream.fromXML(zipStream);
				zipStream.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return deSerialisedQueries;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.query.store.QueryStore#serialiseCollection()
	 */
	public void serialiseCollection()
	{
		if (deSerialisedQueries == null || deSerialisedQueries == null)
		{
			serialisedQueries = null;
		}
		else
		{
			final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			DeflaterOutputStream zipStream = new DeflaterOutputStream(outStream);
			try {
				XStream xStream = new XStream();
				xStream.toXML(deSerialisedQueries, zipStream);
				zipStream.close();
				serialisedQueries = outStream.toByteArray();
				deSerialisedQueries = null;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.query.store.QueryStore#getOrganisationID()
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.query.store.QueryStore#getQueryStoreID()
	 */
	public long getQueryStoreID()
	{
		return queryStoreID;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.query.store.QueryStore#getName()
	 */
	public I18nText getName()
	{
		return name;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.query.store.QueryStore#getDescription()
	 */
	public I18nText getDescription()
	{
		return description;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.query.store.QueryStore#getOwnerID()
	 */
	public UserID getOwnerID()
	{
		return UserID.create(organisationID, ownerID);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.query.store.QueryStore#getOwner()
	 */
	public User getOwner()
	{
		return owner;
	}

	/**
	 * @return the authority
	 */
	public Authority getAuthority()
	{
		return authority;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.query.store.QueryStore#setAuthority(org.nightlabs.jfire.security.Authority)
	 */
	public void setAuthority(Authority authority)
	{
		this.authority = authority;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.query.store.QueryStore#getResultClassName()
	 */
	public String getResultClassName()
	{
		return resultClassName;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.query.store.QueryStore#isPubliclyAvailable()
	 */
	public boolean isPubliclyAvailable()
	{
		return publiclyAvailable;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.query.store.QueryStore#setPubliclyAvailable(boolean)
	 */
	public void setPubliclyAvailable(boolean publiclyAvailable)
	{
		this.publiclyAvailable = publiclyAvailable;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.query.store.QueryStore#isDefaultQuery()
	 */
	public boolean isDefaultQuery() {
		return defaultQuery;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.query.store.QueryStore#setDefaultQuery(boolean)
	 */
	public void setDefaultQuery(boolean defaultQuery)
	{
		// TODO check before if another defaultQuery exists for the user and
		// the resultClassanme and if yes throw an exception
		this.defaultQuery = defaultQuery;
		this.publiclyAvailable = false;

		getName().setText(Locale.ENGLISH.getLanguage(), "last changes");
		getName().setText(Locale.GERMAN.getLanguage(), "Letzte Änderungen");

		getDescription().setText(Locale.ENGLISH.getLanguage(), "Stores last changes which have been made by the user");
		getDescription().setText(Locale.GERMAN.getLanguage(), "Speichert die letzten Änderungen die vom Benutzer gemacht wurden");
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + ((ownerID == null) ? 0 : ownerID.hashCode());
		result = prime * result + (int) (queryStoreID ^ (queryStoreID >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass())
			return false;

		final BaseQueryStore other = (BaseQueryStore) obj;

		if (organisationID == null)
		{
			if (other.organisationID != null)
				return false;
		} else if (!organisationID.equals(other.organisationID))
			return false;
		if (ownerID == null)
		{
			if (other.ownerID != null)
				return false;
		} else if (!ownerID.equals(other.ownerID))
			return false;
		if (queryStoreID != other.queryStoreID)
			return false;

		return true;
	}

	@Override
	public void jdoPreStore()
	{
		if (defaultQuery) {
			PersistenceManager pm = JDOHelper.getPersistenceManager(this);
			if (!NLJDOHelper.exists(pm, this))
			{
				QueryStoreID defaultStoreId =
					getDefaultQueryStoreID(pm, resultClassName, organisationID, ownerID);

				if (defaultStoreId != null) {
					throw new IllegalArgumentException("There already exists a default queryStore for the " +
							"resultClass "+resultClassName+" and the user "+getOwnerID());
				}
			}
		}
	}

}
