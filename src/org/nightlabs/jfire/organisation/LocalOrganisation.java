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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;

import org.nightlabs.jfire.organisation.id.LocalOrganisationID;
import org.nightlabs.util.Util;

/**
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.organisation.id.LocalOrganisationID"
 *		detachable="true"
 *		table="JFireBase_LocalOrganisation"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.inheritance strategy="new-table"
 */
@PersistenceCapable(
	objectIdClass=LocalOrganisationID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_LocalOrganisation")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Queries({
	@javax.jdo.annotations.Query(name="LocalOrganisation", value="SELECT UNIQUE")
})
public class LocalOrganisation implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * This Map stores the passwords of this organisation which
	 * are used to authenticate at other organisations.<br/><br/>
	 * Note, that the passwords in here are encrypted by an easily
	 * decryptable algorithm, whose only purpose is to avoid
	 * a simple SQL SELECT from disclose the plain text passwords to
	 * normal users.
	 * <br/><br/>
	 * key: String otherOrganisationID<br/>
	 * value: String password
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireBase_LocalOrganisation_passwords"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_LocalOrganisation_passwords",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, String> passwords = new HashMap<String, String>();

	/**
	 * This Map stores all inter-organisation-registrations which are currently
	 * pending between this LocalOrganisation and the contained other organisations.
	 * <br/><br/>
	 * key: String organisationID<br/>
	 * value: RegistrationStatus pendingRegistration
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="RegistrationStatus"
	 *		table="JFireBase_LocalOrganisation_pendingRegistrations"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_LocalOrganisation_pendingRegistrations",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, RegistrationStatus> pendingRegistrations = new HashMap<String, RegistrationStatus>();

	public static LocalOrganisation getLocalOrganisation(PersistenceManager pm)
	{
		// Unfortunately, DataNucleus creates a new Query instance everytime 'pm.getExtent(...).iterator()' is called and it keeps
		// this Query instance until the PM is closed :-( Therefore, we now use a simple named query - it seems to exist only once per PM.
		// Marco. 2010-02-16
		Query q = pm.newNamedQuery(LocalOrganisation.class, "getLocalOrganisation");
		LocalOrganisation localOrganisation = (LocalOrganisation) q.execute();
		q.closeAll();
//		Iterator<?> it = pm.getExtent(LocalOrganisation.class).iterator();

		if (localOrganisation == null)
			throw new JDOObjectNotFoundException("LocalOrganisation undefined in datastore!");

//		localOrganisation = (LocalOrganisation)it.next();
//
//		if (it.hasNext())
//			throw new IllegalStateException("There are multiple instances of LocalOrganisation in the datastore!!!");

		return localOrganisation;
	}

	public LocalOrganisation() { }

	public LocalOrganisation(Organisation _organisation)
	{
		this.organisation = _organisation;
		this.organisationID = organisation.getOrganisationID();
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Organisation organisation;

	/**
	 * @return Returns the organisation.
	 */
	public Organisation getOrganisation() {
		return organisation;
	}
	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}

	protected String encrypt(String organisationID, String pw)
	{
		if (pw == null)
			return null;

		StringBuffer sb = new StringBuffer();
		// In case we'll change the encryption algorithm later,
		// we use a prefix to mark the current one.
		sb.append('i');
		int oidx = 0;
		for (int i = 0; i < pw.length(); ++i) {
			int c = pw.charAt(i);
			int k = organisationID.charAt(oidx++);
			if (oidx >= organisationID.length())
				oidx = 0;

			int x = c ^ k;
			sb.append(Integer.toHexString(x));
			sb.append('.');
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
	protected String decrypt(String organisationID, String pw)
	{
		if (pw == null)
			return null;

		StringBuffer res = new StringBuffer();

		char prefix = pw.charAt(0);
		pw = pw.substring(1);

		if (prefix == 'i') {
			StringTokenizer st = new StringTokenizer(pw, ".", false);
			int oidx = 0;
			while (st.hasMoreTokens()) {
				int x = Integer.valueOf(st.nextToken(), 16).intValue();
				int k = organisationID.charAt(oidx++);
				if (oidx >= organisationID.length())
					oidx = 0;

				int c = x ^ k;
				res.append((char)c);
			}
		}
		else
			throw new IllegalArgumentException("Password is encrypted with an unknown encryption mechanism!");

		return res.toString();
	}

	public void setPassword(String organisationID, String plainPassword)
	{
		if (organisationID == null)
			throw new NullPointerException("organisationID");

		if (plainPassword == null)
			throw new NullPointerException("plainPassword");

		passwords.put(organisationID, encrypt(organisationID, plainPassword));
	}
	public String getPassword(String organisationID)
	{
		return decrypt(organisationID, passwords.get(organisationID));
	}

	public void addPendingRegistration(RegistrationStatus registrationStatus)
	{
		String organisationID = registrationStatus.getOrganisationID();
		// It is probably better to ignore this. I think it's no problem
		// if a registration is performed multiple times. Here, we always store
		// the newest.
//		if (pendingRegistrations.containsKey(organisationID))
//			throw new IllegalStateException("There is already a registration pending for organisation \""+organisationID+"\"!");

		pendingRegistrations.put(organisationID, registrationStatus);
	}

	public Collection<RegistrationStatus> getPendingRegistrations()
	{
		return pendingRegistrations.values();
	}

	public RegistrationStatus getPendingRegistration(String organisationID)
	{
		return pendingRegistrations.get(organisationID);
	}

	public void removePendingRegistration(String organisationID)
	{
		pendingRegistrations.remove(organisationID);
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
		final LocalOrganisation other = (LocalOrganisation) obj;
		return Util.equals(this.organisationID, other.organisationID);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ']';
	}
}
