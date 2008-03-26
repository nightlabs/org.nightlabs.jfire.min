package org.nightlabs.jfire.query.store;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.QueryCollection;
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
 * 	name="AbstractQueryStore.owner"
 * 	fields="owner"
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public abstract class AbstractQueryStore<R, Q extends AbstractSearchQuery<? extends R>>
	implements Serializable
{
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
	private UserID ownerID;
	
	/**
	 * @jdo.field
	 * 	persistence-modifier="persistent"
	 */
	private User owner;
	
	/**
	 * @jdo.field
	 * 	persistence-modifier="persistent"
	 * 	default-fetch-group="true"
	 */
	private QueryStoreName name;
	
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
	
	/**
	 * FetchGroup name for the Owner-FetchGroup.
	 */
	public static final String FETCH_GROUP_OWNER = "AbstractQueryStore.owner";
	
	/**
	 * Sets the QueryCollection to persist in the datastore.
	 * @param queries the QueryCollection to persist in the datastore.
	 */
	public void setQuerieCollection(QueryCollection<R, Q> queries)
	{
		this.deSerialisedQueries = queries;
	}
	
	/**
	 * Returns the QueryCollection, which might need to deserialise the serialised QueryCollection. 
	 * @return the managed QueryCollection.
	 */
	@SuppressWarnings("unchecked")
	public QueryCollection<R, Q> getQueryCollection()
	{
		if (deSerialisedQueries == null)
		{
			final ByteArrayInputStream inputStream = new ByteArrayInputStream(serialisedQueries);
			final XMLDecoder decoder = new XMLDecoder(inputStream);
			deSerialisedQueries = (QueryCollection<R, Q>) decoder.readObject();
		}
		
		return deSerialisedQueries;
	}
	
	/**
	 * This is only called by the DAO in order to prohibit the serialisation of the QueryCollection
	 * when calling {@link #setQuerieCollection(QueryCollection)}. <br />
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
			final XMLEncoder encoder = new XMLEncoder(outStream);
			encoder.writeObject(deSerialisedQueries);
			final byte[] serialisedForm = outStream.toByteArray();
			
			serialisedQueries = serialisedForm;
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
	 * @return the ownerID
	 */
	public UserID getOwnerID()
	{
		return ownerID;
	}

	/**
	 * @return the owner
	 */
	public User getOwner()
	{
		return owner;
	}
	
}
