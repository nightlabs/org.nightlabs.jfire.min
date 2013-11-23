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

package org.nightlabs.jfire.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.StoreCallback;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Util;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;

/**
 * Except the one Authority which represents the organistation itself
 * (see {@link Authority#AUTHORITY_ID_ORGANISATION}), authorities are used
 * for special purposes. For example, there might exist an authority responsible
 * for an Event. This is only able to control, who is allowed to sell or to refund
 * and NOT whether new users can be created in the system configuration.
 * <p>
 * Because this Authority has a specialized purpose, there is only a subset
 * of access-rights (bundled in {@link org.nightlabs.jfire.security.RoleGroup}s)
 * available to be assigned. Which ones are available, is controlled by
 * the <code>AuthorityType</code>.
 * </p>
 * <p>
 * This means, an <code>AuthorityType</code> is declared by the developer of a module
 * (and not by the user). Developers should use the
 * <a href="https://www.jfire.org/modules/phpwiki/index.php/ACL%20jfire-security.xml">jfire-security.xml</a>
 * descriptor files to create an <code>AuthorityType</code>. An <code>AuthorityType</code> should
 * <b>not</b> be created manually!
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.security.id.AuthorityTypeID"
 *		detachable="true"
 *		table="JFireBase_AuthorityType"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="authorityTypeID"
 *
 * @jdo.fetch-group name="AuthorityType.roleGroups" fields="roleGroups"
 * @jdo.fetch-group name="AuthorityType.name" fields="name"
 * @jdo.fetch-group name="AuthorityType.description" fields="description"
 */
@PersistenceCapable(
	objectIdClass=AuthorityTypeID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_AuthorityType")
@FetchGroups({
	@FetchGroup(
		name=AuthorityType.FETCH_GROUP_ROLE_GROUPS,
		members=@Persistent(name="roleGroups")),
	@FetchGroup(
		name=AuthorityType.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		name=AuthorityType.FETCH_GROUP_DESCRIPTION,
		members=@Persistent(name="description"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class AuthorityType
implements Serializable, SecuredObject, StoreCallback, AttachCallback
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_ROLE_GROUPS = "AuthorityType.roleGroups";
	public static final String FETCH_GROUP_NAME = "AuthorityType.name";
	public static final String FETCH_GROUP_DESCRIPTION = "AuthorityType.description";

	/**
	 * This <code>AuthorityType</code>
	 * specifies those Authorities which have the authorityID
	 * {@link Authority#AUTHORITY_ID_ORGANISATION}. They control the
	 * behaviour of the general J2EE access control (JAAS). The right-import (which scans
	 * all xml descriptors and generates {@link Role} and {@link RoleGroup} instances)
	 * adds all known <code>RoleGroup</code>s to this <code>AuthorityType</code>.
	 */
	public static final AuthorityTypeID AUTHORITY_TYPE_ID_ORGANISATION = AuthorityTypeID.create(
			AuthorityType.class.getName() + "#organisation"
	);

	/**
	 * This <code>AuthorityType</code>
	 * specifies those Authorities that are used to configure the access rights of the authority management
	 * itself.
	 */
	public static final AuthorityTypeID AUTHORITY_TYPE_ID_SELF = AuthorityTypeID.create(
			AuthorityType.class.getName() + "#self"
	);

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String authorityTypeID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String securingAuthorityID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String securingAuthorityTypeID;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.security.RoleGroup"
	 *		table="JFireBase_AuthorityType_roleGroups"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		table="JFireBase_AuthorityType_roleGroups",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<RoleGroup> roleGroups;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="authorityType"
	 */
	@Persistent(
		dependent="true",
		mappedBy="authorityType",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private AuthorityTypeName name;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="authorityType"
	 */
	@Persistent(
		dependent="true",
		mappedBy="authorityType",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private AuthorityTypeDescription description;

	/**
	 * @deprecated Only for JDO!
	 */
	protected AuthorityType() { }

	/**
	 * Creates an instance of <code>AuthorityType</code>.
	 * You should <b>not</b> call this constructor directly! Use a 
	 * <a href="https://www.jfire.org/modules/phpwiki/index.php/ACL%20jfire-security.xml">jfire-security.xml</a>
	 * descriptor file instead!
	 *
	 * @param authorityTypeID the object-id of the new <code>AuthorityType</code>.
	 */
	public AuthorityType(AuthorityTypeID authorityTypeID)
	{
		this(authorityTypeID.authorityTypeID);
	}
	/**
	 * Creates an instance of <code>AuthorityType</code>.
	 * You should <b>not</b> call this constructor directly! Use a 
	 * <a href="https://www.jfire.org/modules/phpwiki/index.php/ACL%20jfire-security.xml">jfire-security.xml</a>
	 * descriptor file instead!
	 *
	 * @param authorityTypeID the id of the new <code>AuthorityType</code>.
	 */
	public AuthorityType(String authorityTypeID)
	{
		this.authorityTypeID = authorityTypeID;
		this.roleGroups = new HashSet<RoleGroup>();
		this.securingAuthorityTypeID = AUTHORITY_TYPE_ID_SELF.toString();
		this.name = new AuthorityTypeName(this);
		this.description = new AuthorityTypeDescription(this);
	}

	public String getAuthorityTypeID()
	{
		return authorityTypeID;
	}

	public Set<RoleGroup> getRoleGroups()
	{
		return Collections.unmodifiableSet(roleGroups);
	}
	public void addRoleGroup(RoleGroup roleGroup)
	{
		if (!roleGroups.contains(roleGroup))
			roleGroups.add(roleGroup);
	}

	public void removeRoleGroup(RoleGroup roleGroup)
	{
		if (!roleGroups.contains(roleGroup))
			return;

		for (Authority authority : getAuthorities())
			authority.destroyRoleGroupRef(roleGroup);

		roleGroups.remove(roleGroup);
	}

	private Collection<Authority> getAuthorities()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of AuthorityType is currently not persistent! Cannot obtain PersistenceManager.");

		Query q = pm.newQuery(Authority.class);
		q.setFilter("this.authorityType == :authorityType");
		return CollectionUtil.castCollection((Collection<?>)q.execute(this));
	}

	@Override
	public AuthorityID getSecuringAuthorityID() {
		return (AuthorityID) ObjectIDUtil.createObjectID(securingAuthorityID);
	}
	@Override
	public void setSecuringAuthorityID(AuthorityID securingAuthorityID) {
		// TODO need to check this in a pre-store and pre-attach method
//		if (securingAuthorityID != null) {
//			// check if the AuthorityType is correct.
//			if (!securingAuthority.getAuthorityType().equals(securingAuthorityType))
//				throw new IllegalArgumentException("securingAuthority.authorityType does not match this.authorityType! securingAuthority: " + JDOHelper.getObjectId(securingAuthority) + " this: " + JDOHelper.getObjectId(this));
//		}

		this.securingAuthorityID = securingAuthorityID == null ? null : securingAuthorityID.toString();
	}

	public AuthorityTypeName getName() {
		return name;
	}
	public AuthorityTypeDescription getDescription() {
		return description;
	}

//	/**
//	 * Get the <code>Authority</code> which is responsible for this <code>AuthorityType</code>.
//	 * This method never returns <code>null</code>. It first checks by calling {@link #getSecuringAuthority()}
//	 * if there is an <code>Authority</code> assigned directly to this <code>AuthorityType</code>. If there is none,
//	 * it returns the organisation's global one by calling {@link Authority#getOrganisationAuthority(PersistenceManager)}.
//	 * <p>
//	 * This method can only be called when the object is attached to the datastore (i.e. within the server).
//	 * </p>
//	 *
//	 * @return the <code>Authority</code> which contains the access right configuration for this <code>Authority</code>. 
//	 */
//	public Authority resolveSecuringAuthority() {
//		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
//		if (pm == null)
//			throw new IllegalStateException("Could not obtain PersistenceManager from this instance of Authority!");
//
//		Authority authority = getSecuringAuthority();
//		if (authority != null)
//			return authority;
//
//		authority = Authority.getOrganisationAuthority(pm);
//		return authority;
//	}

	@Override
	public AuthorityTypeID getSecuringAuthorityTypeID() {
		// Return that AuthorityType that controls the Authority in the field 'securingAuthority'.
		// This is very likely different from 'this'. There is the rule that
		// this.securingAuthority.authorityType == this.securingAuthorityType. Therefore,
		// this method must return the AuthorityType containing the role grous for
		// "access rights management in general".
		// To make it clearer, this method must always return the same object as all
		// instances of Authority do in their implementation of this method.

		return (AuthorityTypeID) ObjectIDUtil.createObjectID(securingAuthorityTypeID);
	}

	@Override
	public void jdoPreStore() {
		// nothing to do - subclasses override this method, though - see Authority.getDummyAuthorityAllow()
	}

	@Override
	public void jdoPreAttach() {
		// nothing to do
	}
	@Override
	public void jdoPostAttach(Object o) {
		// nothing to do
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authorityTypeID == null) ? 0 : authorityTypeID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final AuthorityType other = (AuthorityType) obj;
		return Util.equals(this.authorityTypeID, other.authorityTypeID);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + authorityTypeID + ']';
	}
}
