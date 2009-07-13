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
import java.security.Principal;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.base.SimplePrincipal;
import org.nightlabs.jfire.security.id.RoleRefID;
import org.nightlabs.util.Util;


/**
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.security.id.RoleRefID"
 *		detachable="true"
 *		table="JFireBase_RoleRef"
 *
 * @jdo.create-objectid-class field-order="organisationID, authorityID, authorizedObjectID, roleID"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="RoleRef.authority" fields="authority"
 * @jdo.fetch-group name="RoleRef.authorizedObjectRef" fields="authorizedObjectRef"
 * @jdo.fetch-group name="RoleRef.role" fields="role"
 */
@PersistenceCapable(
	objectIdClass=RoleRefID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_RoleRef")
@FetchGroups({
	@FetchGroup(
		name=RoleRef.FETCH_GROUP_AUTHORITY,
		members=@Persistent(name="authority")),
	@FetchGroup(
		name=RoleRef.FETCH_GROUP_AUTHORIZED_OBJECT_REF,
		members=@Persistent(name="authorizedObjectRef")),
	@FetchGroup(
		name=RoleRef.FETCH_GROUP_ROLE,
		members=@Persistent(name="role"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class RoleRef implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_AUTHORITY = "RoleRef.authority";
	public static final String FETCH_GROUP_AUTHORIZED_OBJECT_REF = "RoleRef.authorizedObjectRef";
	public static final String FETCH_GROUP_ROLE = "RoleRef.role";

	/**
	 * This is the organisationID to which the user belongs. Within one organisation,
	 * all the users have their organisation's ID stored here, thus it's the same
	 * value for all of them. Even if the User object represents another organisation,
	 * this member is the organisationID to which the user logs in.
	 *
	 * @jdo.field persistence-modifier="persistent" primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field persistence-modifier="persistent" primary-key="true"
	 * @jdo.column length="50"
	 */
	@PrimaryKey
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=50)
	private String authorityID;

	/**
	 * @jdo.field persistence-modifier="persistent" primary-key="true"
	 * @jdo.column length="255"
	 */
	@PrimaryKey
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=255)
	private String authorizedObjectID;

	/**
	 * @jdo.field persistence-modifier="persistent" primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=100)
	private String roleID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Authority authority;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private AuthorizedObjectRef authorizedObjectRef;

	private Role role;

	private int referenceCount = 0;

	public RoleRef() { }

	public RoleRef(Authority _authority, AuthorizedObjectRef _authorizedObjectRef, Role _role)
	{
		if (_authority == null)
			throw new NullPointerException("authority must not be null!");
		if (_authorizedObjectRef == null)
			throw new NullPointerException("authorizedObjectRef must not be null!");
		if (_role == null)
			throw new NullPointerException("role must not be null!");

		this.authorityID = _authority.getAuthorityID();
		this.organisationID = _authorizedObjectRef.getOrganisationID();
		this.authorizedObjectID = _authorizedObjectRef.getAuthorizedObjectID();
		this.roleID = _role.getRoleID();
		this.authority = _authority;
		this.authorizedObjectRef = _authorizedObjectRef;
		this.role = _role;
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	/**
	 * @return Returns the authorizedObjectID.
	 */
	public String getAuthorizedObjectID() {
		return authorizedObjectID;
	}
	/**
	 * @return Returns the roleID.
	 */
	public String getRoleID() {
		return roleID;
	}

	/**
	 * @return Returns the referenceCount.
	 */
	public int getReferenceCount() {
		return referenceCount;
	}

	/**
	 * This method increments the reference count.
	 * @return Returns the new reference count after incrementation.
	 */
	public int incrementReferenceCount(int incRefCount)
	{
		int result = (referenceCount+=incRefCount);
		if (result < 0)
			throw new IllegalStateException("this.referenceCount < 0!!! this=" + this + " incRefCount=" + incRefCount);
		return result;
	}

	/**
	 * This method decrements the reference count.
	 * @return Returns the new reference count after decrementation.
	 */
	public int decrementReferenceCount(int decRefCount)
	{
		int result = (referenceCount-=decRefCount);
		if (result < 0)
			throw new IllegalStateException("this.referenceCount < 0!!! this=" + this + " decRefCount=" + decRefCount);
		return result;
	}

	/**
	 * @return Returns the user.
	 */
	public AuthorizedObjectRef getAuthorizedObjectRef() {
		return authorizedObjectRef;
	}

	/**
	 * @return Returns the role.
	 */
	public Role getRole() {
		return role;
	}

//	public static final int INCLUDE_NONE = 0;
////	public static final int INCLUDE_USER = 0x1;
////	public static final int INCLUDE_USER_PERSON = INCLUDE_USER | 0x2;
////	public static final int INCLUDE_USER_ROLEGROUPS = INCLUDE_USER | 0x4;
//	public static final int INCLUDE_ROLE = 0x8;
//	public static final int INCLUDE_ROLE_ROLEGROUPS = INCLUDE_ROLE | 0x10;
//
//	public void makeTransient(int includeMask)
//	{
//		PersistenceManager pm = ((PersistenceCapable)this).jdoGetPersistenceManager();
//		if (pm == null)
//			return;
//
//		pm.retrieve(this);
////		if ((includeMask & INCLUDE_USER) != 0)
////			getUser();
//
//		if ((includeMask & INCLUDE_ROLE) != 0)
//			getRole();
//
//		pm.makeTransient(this);
//
////		if ((includeMask & INCLUDE_USER) != 0) {
////			int userIncludeMask = 0;
////			if ((includeMask & INCLUDE_USER_PERSON) != 0)
////				userIncludeMask = userIncludeMask | User.INCLUDE_PERSON;
////			if ((includeMask & INCLUDE_USER_ROLEGROUPS) != 0)
////				userIncludeMask = userIncludeMask | User.INCLUDE_ROLEGROUPS;
////
////			this.user.makeTransient(userIncludeMask);
////		}
////		else
////			this.user = null;
//
//		if ((includeMask & INCLUDE_ROLE) != 0) {
//			int roleIncludeMask = 0;
//			if ((includeMask & INCLUDE_ROLE_ROLEGROUPS) != 0)
//				roleIncludeMask = roleIncludeMask | Role.INCLUDE_ROLEGROUPS;
//
//			this.role.makeTransient(roleIncludeMask);
//		}
//		else
//			this.role = null;
//	}
//
//	protected transient String thisString = null;

	@Override
	public String toString() {
		return (
				this.getClass().getName() + '@' +
				Integer.toHexString(System.identityHashCode(this)) +
				'[' + organisationID + ',' + authorityID + ',' + authorizedObjectID + ',' + roleID + ']'
		);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;

		if (!(obj instanceof RoleRef))
			return false;

		RoleRef other = (RoleRef) obj;
		return (
				Util.equals(this.organisationID, other.organisationID) &&
				Util.equals(this.authorityID, other.authorityID) &&
				Util.equals(this.authorizedObjectID, other.authorizedObjectID) &&
				Util.equals(this.roleID, other.roleID)
		);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authorityID == null) ? 0 : authorityID.hashCode());
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + ((roleID == null) ? 0 : roleID.hashCode());
		result = prime * result + ((authorizedObjectID == null) ? 0 : authorizedObjectID.hashCode());
		return result;
	}

	protected transient Principal rolePrincipal = null;
	public Principal getRolePrincipal()
	{
		if (rolePrincipal == null)
			rolePrincipal = new SimplePrincipal(roleID);

		return rolePrincipal;
	}
	/**
	 * @return Returns the authority.
	 */
	public Authority getAuthority() {
		return authority;
	}

}
