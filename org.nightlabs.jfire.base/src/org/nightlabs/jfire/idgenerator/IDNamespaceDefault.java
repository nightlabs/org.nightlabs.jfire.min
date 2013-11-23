package org.nightlabs.jfire.idgenerator;

import java.io.Serializable;
import java.util.regex.Pattern;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.idgenerator.id.IDNamespaceDefaultID;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.idgenerator.id.IDNamespaceDefaultID"
 *		detachable="true"
 *		table="JFireBase_IDNamespaceDefault"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, namespaceIDCategory"
 */
@PersistenceCapable(
	objectIdClass=IDNamespaceDefaultID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_IDNamespaceDefault")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class IDNamespaceDefault
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static Pattern removeLastCategorySegmentPattern = null;

	public static IDNamespaceDefault getIDNamespaceDefault(PersistenceManager pm, String organisationID, String namespaceID)
	{
		IDNamespaceDefault res = null;
		String namespaceIDCategory = namespaceID;
		while (res == null) {
			IDNamespaceDefaultID oid = IDNamespaceDefaultID.create(organisationID, namespaceIDCategory);
			try {
				res = (IDNamespaceDefault) pm.getObjectById(oid);
				res.getCacheSizeServer(); // WORKAROUND for JPOX
			} catch (JDOObjectNotFoundException x) {
				res = null; // in case the workaround caused the exception
			}

			if (res != null)
				return res;

			// There is no default for the current category. Hence, we will cut everything from the end of the prefix
			// until we reach the next separator (and we'll remove the separator, too).
			if (removeLastCategorySegmentPattern == null)
				removeLastCategorySegmentPattern = Pattern.compile("[.#/\\-][^.#/\\-]*$");

			String s = removeLastCategorySegmentPattern.matcher(namespaceIDCategory).replaceAll("");
			if (namespaceIDCategory.equals(s)) // the last segment that can be removed has already been removed => no change anymore => return
				return res; // res is null here (equivalent to return null)

			namespaceIDCategory = s;
		}
		return res;
	}

	public static IDNamespaceDefault createIDNamespaceDefault(PersistenceManager pm, String organisationID, Class<?> clazz)
	{
		return createIDNamespaceDefault(pm, organisationID, clazz.getName());
	}

	public static IDNamespaceDefault createIDNamespaceDefault(PersistenceManager pm, String organisationID, String namespaceIDCategory)
	{
		IDNamespaceDefault res;
		IDNamespaceDefaultID oid = IDNamespaceDefaultID.create(organisationID, namespaceIDCategory);
		try {
			res = (IDNamespaceDefault) pm.getObjectById(oid);
			res.getCacheSizeServer(); // WORKAROUND for JPOX
		} catch (JDOObjectNotFoundException x) {
			res = new IDNamespaceDefault(organisationID, namespaceIDCategory);
			res = pm.makePersistent(res);
		}
		return res;
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
	 * @jdo.column length="255"
	 */
	@PrimaryKey
	@Column(length=255)
	private String namespaceIDCategory;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int cacheSizeServer;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int cacheSizeClient;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IDNamespaceDefault()
	{
	}

	public IDNamespaceDefault(String organisationID, String namespaceIDCategory)
	{
		this.organisationID = organisationID;
		this.namespaceIDCategory = namespaceIDCategory;
		this.cacheSizeServer = 50;
		this.cacheSizeClient = 5;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getNamespaceIDCategory()
	{
		return namespaceIDCategory;
	}
	public int getCacheSizeServer()
	{
		return cacheSizeServer;
	}
	public void setCacheSizeServer(int cacheSize)
	{
		if (cacheSize < 0)
			throw new IllegalArgumentException("cacheSizeServer cannot be less than 0!");

		this.cacheSizeServer = cacheSize;
	}
	public int getCacheSizeClient()
	{
		return cacheSizeClient;
	}
	public void setCacheSizeClient(int cacheSize)
	{
		if (cacheSize < 0)
			throw new IllegalArgumentException("cacheSizeClient cannot be less than 0!");

		this.cacheSizeClient = cacheSize;
	}
}
