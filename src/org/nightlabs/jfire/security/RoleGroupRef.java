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
import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOHelper;

import org.apache.log4j.Logger;
import org.nightlabs.util.Util;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Key;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import org.nightlabs.jfire.security.id.RoleGroupRefID;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author marco
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.security.id.RoleGroupRefID"
 *		detachable="true"
 *		table="JFireBase_RoleGroupRef"
 *
 * @jdo.create-objectid-class field-order="organisationID, authorityID, roleGroupID"
 * 
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="Authority.roleGroupRefs" fields="authority"
 *
 * @jdo.fetch-group name="RoleGroupRef.authority" fields="authority"
 * @jdo.fetch-group name="RoleGroupRef.roleGroup" fields="roleGroup"
 * @jdo.fetch-group name="RoleGroupRef.authorizedObjectRefs" fields="authorizedObjectRefs"
 */
@PersistenceCapable(
	objectIdClass=RoleGroupRefID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_RoleGroupRef")
@FetchGroups({
	@FetchGroup(
		name="Authority.roleGroupRefs",
		members=@Persistent(name="authority")),
	@FetchGroup(
		name=RoleGroupRef.FETCH_GROUP_AUTHORITY,
		members=@Persistent(name="authority")),
	@FetchGroup(
		name=RoleGroupRef.FETCH_GROUP_ROLE_GROUP,
		members=@Persistent(name="roleGroup")),
	@FetchGroup(
		name=RoleGroupRef.FETCH_GROUP_USER_REFS,
		members=@Persistent(name="authorizedObjectRefs"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class RoleGroupRef implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(RoleGroupRef.class);

	public static final String FETCH_GROUP_AUTHORITY = "RoleGroupRef.authority";
	public static final String FETCH_GROUP_ROLE_GROUP = "RoleGroupRef.roleGroup";
	public static final String FETCH_GROUP_USER_REFS = "RoleGroupRef.authorizedObjectRefs";

	/**
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
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=100)
	private String roleGroupID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @!jdo.field-vendor-extension vendor-name="jpox" key="map-field" value="roleGroupRefs"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Authority authority;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @!jdo.field-vendor-extension vendor-name="jpox" key="map-field" value="roleGroupRefs"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private RoleGroup roleGroup;

	/**
	 * key: String authorizedObjectID<br/>
	 * value: AuthorizedObjectRef authorizedObjectRef
	 * <br/><br/>
	 * AuthorizedObjectRef (m) - (n) RoleGroupRef
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="AuthorizedObjectRef"
	 *		table="JFireBase_RoleGroupRef_authorizedObjectRefs"
	 *
	 * @jdo.key mapped-by="authorizedObjectID"
	 * @jdo.key-column length="255"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		table="JFireBase_RoleGroupRef_authorizedObjectRefs",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Key(mappedBy="authorizedObjectID")
	private Map<String, AuthorizedObjectRef> authorizedObjectRefs = new HashMap<String, AuthorizedObjectRef>();
	
	public RoleGroupRef() { }
	
	public RoleGroupRef(Authority _authority, RoleGroup _roleGroup)
	{
		if (_authority == null)
			throw new NullPointerException("authority must not be null!");
		
		if (_roleGroup == null)
			throw new NullPointerException("roleGroup must not be null!");

		this.organisationID = _authority.getOrganisationID();
		this.authorityID = _authority.getAuthorityID();
		this.roleGroupID = _roleGroup.getRoleGroupID();
		this.authority = _authority;
		this.roleGroup = _roleGroup;
	}

	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return Returns the authorityID.
	 */
	public String getAuthorityID() {
		return authorityID;
	}
	/**
	 * @return Returns the roleGroupID.
	 */
	public String getRoleGroupID() {
		return roleGroupID;
	}
	/**
	 * @return Returns the authority.
	 */
	public Authority getAuthority() {
		return authority;
	}
	/**
	 * @return Returns the roleGroup.
	 */
	public RoleGroup getRoleGroup() {
		return roleGroup;
	}
	
	/**
	 * This method is called by the User if this roleGroup is removed there.
	 * It does not update the RoleRefs, because User.removeRoleGroup(...) does
	 * that already.
	 *
	 * @param user
	 */
	protected void _removeAuthorizedObjectRef(AuthorizedObjectRef authorizedObjectRef)
	{
		if (authorizedObjectRefs.remove(authorizedObjectRef.getAuthorizedObjectID()) == null)
			logger.warn("_removeAuthorizedObjectRef: authorizedObjectRef did not exist in map! this=" + this + " authorizedObjectRef="+authorizedObjectRef);
	}

	/**
	 * This method is called by the AuthorizedObjectRef if this roleGroupRef is added there. It
	 * does not update the RoleRefs, because AuthorizedObjectRef.addRoleGroup(...) does that
	 * already.
	 *
	 * @param user
	 */
	protected void _addAuthorizedObjectRef(AuthorizedObjectRef authorizedObjectRef)
	{
		String authorizedObjectID = authorizedObjectRef.getAuthorizedObjectID();
		if (authorizedObjectRefs.containsKey(authorizedObjectID)) {
			logger.warn("_addAuthorizedObjectRef: authorizedObjectRef already exists in map! this=" + this + " authorizedObjectRef=" + authorizedObjectRef, new Exception("StackTrace"));
			return;
		}
		authorizedObjectRefs.put(authorizedObjectID, authorizedObjectRef);
	}

	/**
	 * This method makes sure, all authorizedObjectRefs get a direct link to the role via a roleRef.
	 *
	 * @param role
	 */
	protected void _addRole(Role role)
	{
		// Add this role to all UserRefs of this RoleGroupRef
		for (AuthorizedObjectRef authorizedObjectRef : getAuthorizedObjectRefs()) {
			authorizedObjectRef._addRole(role, 1);
		}
	}

	/**
	 * This method makes sure, all direct links to the role via a roleRef will be removed in all authorizedObjectRefs.
	 *
	 * @param role
	 */
	protected void _removeRole(Role role)
	{
		// Remove this role from all UserRefs of this RoleGroupRef
		for (AuthorizedObjectRef authorizedObjectRef : getAuthorizedObjectRefs()) {
			authorizedObjectRef._removeRole(role, 1);
		}
	}

	public Collection<AuthorizedObjectRef> getAuthorizedObjectRefs()
	{
		return Collections.unmodifiableCollection(authorizedObjectRefs.values());
	}

	public AuthorizedObjectRef getAuthorizedObjectRef(AuthorizedObject authorizedObject)
	{
		Object authorizedObjectID = JDOHelper.getObjectId(authorizedObject);
		if (authorizedObjectID == null)
			throw new IllegalArgumentException("JDOHelper.getObjectId(authorizedObject) returned null for authorizedObject=" + authorizedObject);

		AuthorizedObjectRef authorizedObjectRef = authorizedObjectRefs.get(authorizedObjectID.toString());
		return authorizedObjectRef;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authorityID == null) ? 0 : authorityID.hashCode());
		result = prime * result + ((roleGroupID == null) ? 0 : roleGroupID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final RoleGroupRef other = (RoleGroupRef) obj;

		return Util.equals(this.authorityID, other.authorityID) && Util.equals(this.roleGroupID, other.roleGroupID);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + authorityID + ',' + roleGroupID + ']';
	}
}
