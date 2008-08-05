package org.nightlabs.jfire.query.store;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;

/**
 * The interface defining the minimum standard for all QueryStores that contain a query
 * configuration.
 *
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public interface QueryStore
{
	/**
	 * Sets the QueryCollection to persist in the datastore.
	 * @param queries the QueryCollection to persist in the datastore.
	 */
	void setQueryCollection(QueryCollection<?> queries);

	/**
	 * Returns the QueryCollection, which might need to deserialise the serialised QueryCollection.
	 * @return the managed QueryCollection.
	 */
	QueryCollection<?> getQueryCollection();

	/**
	 * This is only called by the DAO in order to prohibit the serialisation of the QueryCollection
	 * when calling {@link #setQueryCollection(QueryCollection)}. <br />
	 * <p><b>Note:</b> This has to be called from the outside BEFORE subclasses are send away!! This
	 * 	is usually done in the DAOs.
	 * </p>
	 */
	void serialiseCollection();

	/**
	 * @return the organisationID
	 */
	String getOrganisationID();

	/**
	 * @return the queryStoreID
	 */
	long getQueryStoreID();

	/**
	 * @return the name
	 */
	I18nText getName();

	/**
	 * @return the internationalised description of the stored QueryCollection.
	 */
	I18nText getDescription();

	/**
	 * @return the UserID of my creator.
	 */
	UserID getOwnerID();

	/**
	 * @return the owner
	 */
	User getOwner();

	/**
	 * @param authority the authority to set
	 */
	void setAuthority(Authority authority);

	/**
	 * @return the resultClassName
	 */
	String getResultClassName();

	/**
	 * Return whether the stored query configuration is publicly available.
	 * @return whether the stored query configuration is publicly available.
	 */
	boolean isPubliclyAvailable();

	/**
	 * @param publiclyAvailable the publiclyAvailable to set
	 */
	void setPubliclyAvailable(boolean publiclyAvailable);

	/**
	 * Return whether this queryStore is the defaultQuery for
	 * the user and the resultClass.
	 *
	 * @return the defaultQuery
	 */
	boolean isDefaultQuery();

	/**
	 * Sets the defaultQuery.
	 * @param defaultQuery the defaultQuery to set
	 */
	void setDefaultQuery(boolean defaultQuery);
}