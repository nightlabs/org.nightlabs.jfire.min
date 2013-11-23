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

package org.nightlabs.jfire.organisation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.jdo.JDOHelper;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.organisation.id.TemporaryOrganisationID;
import org.nightlabs.jfire.server.Server;
import org.nightlabs.util.Util;

/**
 * An instance of this class is persisted when an organisation initiates the cross-organisation-registration.
 * This is done via <code>org.nightlabs.jfire.organisation.OrganisationLinkerBean.requestRegistration(...)</code>.
 * <p>
 * Since this method is executed anonymously, it is essential that nothing is written to "real" tables.
 * If we persisted the organisation there, it would be possible to overwrite anonymously Server objects and thus
 * compromise the grant-organisation's data. Therefore we wrap it in a <code>TemporaryOrganisation</code> object
 * and this way store it as BLOB independent from real tables.
 * </p>
 * <p>
 * When the grant-organisation accepts the request, the <code>TemporaryOrganisation</code> object is deleted and
 * the organisation-object persisted to the real tables.
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *   	objectid-class="org.nightlabs.jfire.organisation.id.TemporaryOrganisationID"
 *		detachable="true"
 *		table="JFireBase_TemporaryOrganisation"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 */
@PersistenceCapable(
	objectIdClass=TemporaryOrganisationID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_TemporaryOrganisation")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class TemporaryOrganisation implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * This field is a cache for the data which is stored in serialized+zipped form in the
	 * field {@link #organisation}.
	 *
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient Organisation _organisation;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column sql-type="BLOB"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(sqlType="BLOB")
	private byte[] organisation;

	protected TemporaryOrganisation() { }

	public TemporaryOrganisation(Organisation organisation)
	{
		this.organisationID = organisation.getOrganisationID();
		if (!Organisation.isValidOrganisationID(this.organisationID))
			throw new IllegalArgumentException("organisation.getOrganisationID() is not a valid organisation-id!");

		// Ensure that the given Organisation object is detached and has
		// all fields available that are required.
		if (!JDOHelper.isDetached(organisation))
			throw new IllegalArgumentException("organisation is not detached!");

		Server server = organisation.getServer();
		if (server == null)
			throw new IllegalArgumentException("organisation.getServer() returned null!");

		if (server.getInitialContextURL(Server.PROTOCOL_JNP, false) == null)
			throw new IllegalArgumentException("organisation.getServer().getInitialContextURL(Server.PROTOCOL_JNP, false) returned null!");

		if (server.getInitialContextURL(Server.PROTOCOL_HTTPS, false) == null)
			throw new IllegalArgumentException("organisation.getServer().getInitialContextURL(Server.PROTOCOL_HTTPS, false) returned null!");

		if (server.getJ2eeServerType() == null)
			throw new IllegalArgumentException("organisation.getServer().getJ2eeServerType() returned null!");

		if (!Server.isValidServerID(server.getServerID()))
			throw new IllegalArgumentException("organisation.getServer().getServerID() is not a valid server-id!");

		if (server.getServerName() == null)
			throw new IllegalArgumentException("organisation.getServer().getServerName() returned null!");

		if (organisation.getPerson() == null)
			throw new IllegalArgumentException("organisation.getPerson() returned null!");

		if (organisation.getCreateDT() == null)
			throw new IllegalArgumentException("organisation.getCreateDT() returned null!");

		if (organisation.getChangeDT() == null)
			throw new IllegalArgumentException("organisation.getChangeDT() returned null!");

		try {
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			try {
				ObjectOutputStream objOut = new ObjectOutputStream(new DeflaterOutputStream(byteOut));
				try {
					objOut.writeObject(organisation);
				} finally {
					objOut.close(); // closes its inner streams as well - hence the deflater is flushed
				}
			} finally {
				byteOut.close(); // has no effect, but cleaner to anyway call it ;-)
			}

			this.organisation = byteOut.toByteArray();
		} catch (IOException x) {
			throw new RuntimeException(x); // should never happen when working in RAM
		}
	}

	public String getOrganisationID()
  {
		return organisationID;
	}

	public Organisation getOrganisation() {
		if (_organisation == null) {
			try {
				ObjectInputStream in = new ObjectInputStream(new InflaterInputStream(new ByteArrayInputStream(this.organisation)));
				try {
					_organisation = (Organisation) in.readObject();
				} finally {
					in.close();
				}
			} catch (ClassNotFoundException x) {
				throw new RuntimeException(x); // very unlikely that one of the classes cannot be found => RuntimeException
			} catch (IOException x) {
				throw new RuntimeException(x); // should never happen when working in RAM
			}
		}

		return _organisation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final TemporaryOrganisation other = (TemporaryOrganisation) obj;
		return Util.equals(this.organisationID, other.organisationID);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ']';
	}
}
