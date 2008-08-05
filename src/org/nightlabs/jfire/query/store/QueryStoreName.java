package org.nightlabs.jfire.query.store;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * The internationalised Name of a QueryStore.
 *
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 *
 * @jdo.persistence-capable
 *	identity-type="application"
 *	objectid-class="org.nightlabs.jfire.query.store.id.QueryStoreNameID"
 *	detachable="true"
 *	table="JFireQueryStore_QueryStoreName"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, queryStoreID"
 */
public class QueryStoreName
	extends I18nText
{
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
	 * @jdo.field persistence-modifier="persistent"
	 */
	private QueryStore queryStore;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		default-fetch-group="true"
	 *		null-value="exception"
	 */
	protected Map<String, String> names = new HashMap<String, String>();

	/**
	 * Only for JDO!
	 */
	@Deprecated
	public QueryStoreName()
	{}

	public QueryStoreName(QueryStore queryStore)
	{
		assert queryStore != null;
		this.queryStoreID = queryStore.getQueryStoreID();
		this.organisationID = queryStore.getOrganisationID();
		this.queryStore = queryStore;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID)
	{
		return String.valueOf(queryStoreID);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map<String, String> getI18nMap()
	{
		return names;
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

}
