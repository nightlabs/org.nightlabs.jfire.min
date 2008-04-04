package org.nightlabs.jfire.query.store;

import java.beans.DefaultPersistenceDelegate;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;

/**
 * @jdo.persistence-capable
 *	identity-type="application"
 *	objectid-class="org.nightlabs.jfire.query.store.id.QueryStoreID"
 *	detachable="true"
 *	table="JFireQueryStore_AbstractQueryStore"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, queryStoreID, ownerID"
 *
 * @jdo.fetch-group
 * 	name="BaseQueryStore.owner"
 * 	fields="owner"
 * 
 * FIXME: narf argh asldkfjsdalkfjlwakjr!!!
 * @jdo.query name="getQueryStoreIDsByResultType"
 * 	query="SELECT JDOHelper.getObjectId(this)
 * 				 WHERE this.resultClassName == :givenClassName"
 *
 *  TODO: Braucht man glaube ich nicht.
 * 				 PARAMETERS String givenClassName
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class BaseQueryStore<R, Q extends AbstractSearchQuery<? extends R>>
	implements Serializable
{
	/**
	 * FetchGroup name for the Owner-FetchGroup.
	 */
	public static final String FETCH_GROUP_OWNER = "BaseQueryStore.owner";
	
	/**
	 * The name of the query that returns all QueryStores with the given return type. 
	 */
	public static final String QUERY_STORES_BY_RESULT_TYPE = "getQueryStoreIDsByResultType";
	
	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private long queryStoreID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private String ownerID;
	
	/**
	 * @jdo.field
	 * 	persistence-modifier="persistent"
	 * 	null-value="exception"
	 */
	private User owner;
	
	/**
	 * @jdo.field
	 * 	persistence-modifier="persistent"
	 * 	default-fetch-group="true"
	 */
	private QueryStoreName name;
	
	/**
	 * @jdo.field
	 * 	persistence-modifier="persistent"
	 */
	private Authority authority;
	
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient QueryCollection<R, Q> deSerialisedQueries;
	
	/**
	 * @jdo.field
	 * 	persistence-modifier="persistent"
	 * 	default-fetch-group="true"
	 */
	private byte[] serialisedQueries;
	
//	/**
//	 * @jdo.field
//	 * 	persistence-modifier="persistent"
//	 * 	dependent-element="true"
//	 */
//	private Set<String> scopes = new HashSet<String>();
	
	/**
	 * The fully qualified classname of the result type of the stored QueryCollection.
	 * 
	 * @jdo.field
	 *	persistence-modifier="persistent"
	 */
	private String resultClassName;
	
	/**
	 * Sets the QueryCollection to persist in the datastore.
	 * @param queries the QueryCollection to persist in the datastore.
	 */
	public void setQueryCollection(QueryCollection<R, Q> queries)
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
	
	public BaseQueryStore(User owner, long queryStoreID, QueryCollection<R, Q> queryCollection)
	{
		assert owner != null;
		
		this.owner = owner;
		this.ownerID = owner.getUserID();
		this.organisationID = owner.getOrganisationID();
		this.queryStoreID = queryStoreID;
		this.name = new QueryStoreName(this);
		setQueryCollection(queryCollection);
	}
	
	/**
	 * Returns the QueryCollection, which might need to deserialise the serialised QueryCollection. 
	 * @return the managed QueryCollection.
	 */
	@SuppressWarnings("unchecked")
	public QueryCollection<R, Q> getQueryCollection()
	{
		if (deSerialisedQueries != null)
			return deSerialisedQueries;
		
		if (serialisedQueries == null || serialisedQueries.length == 0)
			return null;
		
		if (deSerialisedQueries == null)
		{
			final ByteArrayInputStream inputStream = new ByteArrayInputStream(serialisedQueries);
//			final DeflaterInputStream zippedStream = new DeflaterInputStream(inputStream);
			final XMLDecoder decoder = new XMLDecoder(inputStream);
			deSerialisedQueries = (QueryCollection<R, Q>) decoder.readObject();
			decoder.close();	
		}
		
		return deSerialisedQueries;
	}
	
	/**
	 * This is only called by the DAO in order to prohibit the serialisation of the QueryCollection
	 * when calling {@link #setQueryCollection(QueryCollection)}. <br />
	 * <p><b>Note:</b> This has to be called from the outside BEFORE subclasses are send away!! This
	 * 	is usually done in the DAOs.
	 * </p>
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
//			DeflaterOutputStream zippedStream = new DeflaterOutputStream(outStream);
			final XMLEncoder encoder = new XMLEncoder(outStream);
			encoder.setPersistenceDelegate(
				QueryCollection.class, new DefaultPersistenceDelegate(new String[] { "resultClassName" })
				);
			encoder.writeObject(deSerialisedQueries);
			encoder.close();
			serialisedQueries = outStream.toByteArray();
		}
	}

	/**
	 * @return the organisationID
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}

	/**
	 * @return the queryStoreID
	 */
	public long getQueryStoreID()
	{
		return queryStoreID;
	}

	/**
	 * @return the name
	 */
	public I18nText getName()
	{
		return name;
	}

	/**
	 * @return the UserID of my creator.
	 */
	public UserID getOwnerID()
	{
		return UserID.create(organisationID, ownerID);
	}

	/**
	 * @return the owner
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

	/**
	 * @param authority the authority to set
	 */
	public void setAuthority(Authority authority)
	{
		this.authority = authority;
	}

	/**
	 * @return the resultClassName
	 */
	public String getResultClassName()
	{
		return resultClassName;
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
		
		final BaseQueryStore<?,?> other = (BaseQueryStore<?,?>) obj;
		
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
	
}
