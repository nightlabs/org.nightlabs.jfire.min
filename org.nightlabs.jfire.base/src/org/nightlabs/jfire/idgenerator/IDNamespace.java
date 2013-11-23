/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/
package org.nightlabs.jfire.idgenerator;

import java.io.Serializable;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.idgenerator.id.IDNamespaceID;
import org.nightlabs.util.Util;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * An instance of this class is automatically created and persisted by the {@link IDGenerator} (or better its
 * server-side implementation). When a new instance is created, the values for {@link #cacheSizeServer}
 * and {@link #cacheSizeClient} are loaded from the {@link IDNamespaceDefault} instance. If there is
 * no matching {@link IDNamespaceDefault} instance, it uses <code>cacheSizeServer = 50</code> and
 * <code>cacheSizeClient = 5</code>.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.idgenerator.id.IDNamespaceID"
 *		detachable="true"
 *		table="JFireBase_IDNamespace"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, namespaceID"
 */
@PersistenceCapable(
	objectIdClass=IDNamespaceID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_IDNamespace")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class IDNamespace
implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(IDNamespace.class);

	public static IDNamespace getIDNamespace(PersistenceManager pm, String organisationID, String namespaceID)
	{
		return getIDNamespace(pm, IDNamespaceID.create(organisationID, namespaceID));
	}

	public static IDNamespace getIDNamespace(PersistenceManager pm, IDNamespaceID idNamespaceID)
	{
		IDNamespace idNamespace = null;
		try {
			idNamespace = (IDNamespace) pm.getObjectById(idNamespaceID);
			idNamespace.getCacheSizeServer(); // workaround for JPOX bug - the JDOObjectNotFoundException doesn't occur always in the above line
		} catch (JDOObjectNotFoundException e) {
			idNamespace = new IDNamespace(
					idNamespaceID.organisationID,
					idNamespaceID.namespaceID,
					IDNamespaceDefault.getIDNamespaceDefault(pm, idNamespaceID.organisationID, idNamespaceID.namespaceID));
			idNamespace = pm.makePersistent(idNamespace);
		}
		return idNamespace;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IDNamespace() { }

	/**
	 * @param organisationID First part of the composite PK.
	 * @param namespaceID Second part of the composite PK.
	 * @param idNamespaceDefault Can be <code>null</code>.
	 */
	public IDNamespace(String organisationID, String namespaceID, IDNamespaceDefault idNamespaceDefault)
	{
		this.organisationID = organisationID;
		this.namespaceID = namespaceID;
		this.nextID = 1;
		if (idNamespaceDefault == null) {
			this.cacheSizeServer = 50;
			this.cacheSizeClient = 5;
		}
		else {
			this.cacheSizeServer = idNamespaceDefault.getCacheSizeServer();
			this.cacheSizeClient = idNamespaceDefault.getCacheSizeClient();
		}
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
	private String namespaceID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long nextID;

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

	public long getNextID()
	{
		return nextID;
	}

	public void setNextID(long nextID)
	{
		if (logger.isTraceEnabled()) {
			logger.trace("setNextID: old=" + ObjectIDUtil.longObjectIDFieldToString(this.nextID) + " new=" + ObjectIDUtil.longObjectIDFieldToString(nextID));
		}

		this.nextID = nextID;
	}

	public String getNamespaceID()
	{
		return namespaceID;
	}

	public String getOrganisationID()
	{
		return organisationID;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + ((namespaceID == null) ? 0 : namespaceID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final IDNamespace other = (IDNamespace) obj;

		return (
				Util.equals(this.organisationID, other.organisationID) &&
				Util.equals(this.namespaceID, other.namespaceID)
		);
	}

	@Override
	public String toString() {
		return (
				this.getClass().getName()
				+ '@' + Integer.toHexString(System.identityHashCode(this))
				+ '[' + organisationID + ',' + namespaceID
//				+ ',' + ObjectIDUtil.longObjectIDFieldToString(nextID) // was only added temporarily for debugging. Marco.
				+ ']'
		);
	}
}
