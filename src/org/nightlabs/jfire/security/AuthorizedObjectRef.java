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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.jfire.security.id.RoleID;
import org.nightlabs.jfire.security.listener.SecurityChangeController;
import org.nightlabs.util.Util;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Value;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import org.nightlabs.jfire.security.id.AuthorizedObjectRefID;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Key;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;

/**
 * @author marco
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.security.id.AuthorizedObjectRefID"
 *		detachable="true"
 *		table="JFireBase_AuthorizedObjectRef"
 *
 * @jdo.create-objectid-class field-order="organisationID, authorityID, authorizedObjectID"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.fetch-group name="Authority.userRefs" fields="authority"
 *
 * @jdo.fetch-group name="AuthorizedObjectRef.authority" fields="authority"
 * @jdo.fetch-group name="AuthorizedObjectRef.authorizedObject" fields="authorizedObject, userLocal, userSecurityGroup"
 * @jdo.fetch-group name="AuthorizedObjectRef.roleGroupRefs" fields="roleGroupRefs"
 * @jdo.fetch-group name="AuthorizedObjectRef.roleRefs" fields="roleRefs"
 */
@PersistenceCapable(
	objectIdClass=AuthorizedObjectRefID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_AuthorizedObjectRef")
@FetchGroups({
	@FetchGroup(
		name="Authority.userRefs",
		members=@Persistent(name="authority")),
	@FetchGroup(
		name=AuthorizedObjectRef.FETCH_GROUP_AUTHORITY,
		members=@Persistent(name="authority")),
	@FetchGroup(
		name=AuthorizedObjectRef.FETCH_GROUP_AUTHORIZED_OBJECT,
		members={@Persistent(name="authorizedObject"), @Persistent(name="userLocal"), @Persistent(name="userSecurityGroup")}),
	@FetchGroup(
		name=AuthorizedObjectRef.FETCH_GROUP_ROLE_GROUP_REFS,
		members=@Persistent(name="roleGroupRefs")),
	@FetchGroup(
		name=AuthorizedObjectRef.FETCH_GROUP_ROLE_REFS,
		members=@Persistent(name="roleRefs"))
})
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class AuthorizedObjectRef implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AuthorizedObjectRef.class);

	public static final String FETCH_GROUP_AUTHORITY = "AuthorizedObjectRef.authority";
	public static final String FETCH_GROUP_AUTHORIZED_OBJECT = "AuthorizedObjectRef.authorizedObject";
	public static final String FETCH_GROUP_ROLE_GROUP_REFS = "AuthorizedObjectRef.roleGroupRefs";
	public static final String FETCH_GROUP_ROLE_REFS = "AuthorizedObjectRef.roleRefs";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	@PrimaryKey
	@Column(length=50)
	private String authorityID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="255"
	 */
	@PrimaryKey
	@Column(length=255)
	private String authorizedObjectID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Authority authority;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private IAuthorizedObject authorizedObject;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @deprecated TODO DATANUCLEUS WORKAROUND (this field should not exist)
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@SuppressWarnings("unused")
	@Deprecated
	private UserLocal userLocal;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @deprecated TODO DATANUCLEUS WORKAROUND (this field should not exist)
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@SuppressWarnings("unused")
	@Deprecated
	private UserSecurityGroup userSecurityGroup;

	/**
	 * This flag controls whether this AuthorizedObjectRef should be shown in lists etc.. If it's true, it
	 * means, the AuthorizedObjectRef has been created manually. If the AuthorizedObjectRef
	 * only exists, because the User belongs to a UserGroup, it is false and the AuthorizedObjectRef
	 * will be automatically deleted, if the last UserGroup containing this authorizedObjectRef gets
	 * removed from the authority.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean visible;

	/**
	 *  @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int userSecurityGroupReferenceCount = 0;

	/**
	 * key: String roleGroupID<br/>
	 * value: RoleGroupRef roleGroupRef
	 * <br/><br/>
	 * AuthorizedObjectRef (m) - (n) RoleGroupRef
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="RoleGroupRef"
	 *		table="JFireBase_AuthorizedObjectRef_roleGroupRefs"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_AuthorizedObjectRef_roleGroupRefs",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Map<String, RoleGroupRef> roleGroupRefs;

	/**
	 * key: String roleID<br/>
	 * value: RoleRef roleRef
	 * <br/><br/>
	 * AuthorizedObjectRef (1) - (n) RoleRef
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="RoleRef"
	 *		mapped-by="authorizedObjectRef"
	 *		dependent-value="true"
	 *
	 * @jdo.key mapped-by="roleID"
	 */
	@Persistent(
		mappedBy="authorizedObjectRef",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Key(mappedBy="roleID")
	@Value(dependent="true")
	private Map<String, RoleRef> roleRefs;

	private Date changeDT;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected AuthorizedObjectRef() { }

	public AuthorizedObjectRef(Authority _authority, AuthorizedObject _authorizedObject, boolean _visible)
	{
		this.authorizedObject = _authorizedObject;

		if (authorizedObject instanceof UserLocal)
			this.userLocal = (UserLocal) authorizedObject;
		else if (authorizedObject instanceof UserSecurityGroup)
			this.userSecurityGroup = (UserSecurityGroup) authorizedObject;

		this.organisationID = _authorizedObject.getOrganisationID();
		this.authorityID = _authority.getAuthorityID();
		Object authorizedObjectID = JDOHelper.getObjectId(_authorizedObject);
		if (authorizedObjectID == null)
			throw new IllegalArgumentException("JDOHelper.getObjectId(authorizedObject) returned null! Is this object persistent? " + _authorizedObject);
		this.authorizedObjectID = authorizedObjectID.toString();
		this.authority = _authority;
		this.visible = _visible;
		this.changeDT = new Date();

		roleGroupRefs = new HashMap<String, RoleGroupRef>();
		roleRefs = new HashMap<String, RoleRef>();
	}

	/**
	 * @return Returns the authorityID.
	 */
	public String getAuthorityID() {
		return authorityID;
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
	 * @return Returns the authority.
	 */
	public Authority getAuthority() {
		return authority;
	}
	/**
	 * @return Returns the authorizedObject.
	 */
	public AuthorizedObject getAuthorizedObject() {
		return (AuthorizedObject) authorizedObject;
	}

	/**
	 * @return Returns the visible.
	 */
	public boolean isVisible() {
		return visible;
	}
	/**
	 * @param visible The visible to set.
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * @return Returns the userSecurityGroupReferenceCount.
	 */
	public int getUserSecurityGroupReferenceCount() {
		return userSecurityGroupReferenceCount;
	}

	/**
	 * @return Returns the new userSecurityGroupReferenceCount.
	 */
	protected int incrementUserSecurityGroupReferenceCount(int inc) {
		int result = (userSecurityGroupReferenceCount += inc);
		if (result < 0)
			throw new IllegalStateException("userSecurityGroupReferenceCount is negative! userSecurityGroupReferenceCount=" + userSecurityGroupReferenceCount + " inc=" + inc);
		return result;
	}

	/**
	 * @return Returns the new userSecurityGroupReferenceCount.
	 */
	protected int decrementUserSecurityGroupReferenceCount(int dec) {
		int result = (userSecurityGroupReferenceCount -= dec);
		if (result < 0)
			throw new IllegalStateException("userSecurityGroupReferenceCount is negative! userSecurityGroupReferenceCount=" + userSecurityGroupReferenceCount + " dec=" + dec);
		return result;
	}

	/**
	 * @return Returns the changeDT.
	 */
	public Date getChangeDT() {
		return changeDT;
	}
	/**
	 * @param changeDT The changeDT to set.
	 */
	public void setChangeDT(Date changeDT) {
		this.changeDT = changeDT;
	}

	/**
	 * This method adds a roleGroup. It automatically generates a RoleGroupRef
	 * and adds all the necessary RoleRef instances
	 * for the direct joins to the roles.
	 *
	 * @param roleGroup
	 */
	public void addRoleGroupRef(RoleGroupRef roleGroupRef)
	{
		if (roleGroupRef == null)
			throw new IllegalArgumentException("roleGroupRef must not be null!");

		if (!Util.equals(this.authorityID, roleGroupRef.getAuthorityID()))
			throw new IllegalArgumentException("this.authorityID != roleGroupRef.authorityID");

		String roleGroupID = roleGroupRef.getRoleGroupID();
		if (roleGroupRefs.containsKey(roleGroupID))
			return;

		if (authority.getRoleGroupRef(roleGroupID) == null)
			throw new IllegalStateException("RoleGroup \""+roleGroupID+"\" not registered in authority \""+authorityID+"\"!");

		SecurityChangeController.getInstance().fireSecurityChangeEvent_pre_AuthorizedObjectRef_addRoleGroupRef(this, roleGroupRef);

		roleGroupRefs.put(roleGroupID, roleGroupRef);
		roleGroupRef._addAuthorizedObjectRef(this);

		RoleGroup roleGroup = roleGroupRef.getRoleGroup();
		for (Role role : roleGroup.getRoles()) {
			_addRole(role, 1);
		}
		changeDT = new Date();

		SecurityChangeController.getInstance().fireSecurityChangeEvent_post_AuthorizedObjectRef_addRoleGroupRef(this, roleGroupRef);
	}

	public void removeRoleGroupRef(RoleGroupRef roleGroupRef)
	{
		if (roleGroupRef == null)
			throw new IllegalArgumentException("roleGroup must not be null!");

		if (!authorityID.equals(roleGroupRef.getAuthorityID()))
			throw new IllegalArgumentException("authorizedObjectRef.authorityID != roleGroupRef.authorityID");

		removeRoleGroupRef(roleGroupRef.getRoleGroupID());
	}

	public void removeRoleGroupRef(RoleGroupID roleGroupID)
	{
		removeRoleGroupRef(roleGroupID.roleGroupID);
	}

	public void removeRoleGroupRef(String roleGroupID)
	{
		if (roleGroupID == null)
			throw new NullPointerException("roleGroupID must not be null!");

		RoleGroupRef roleGroupRef = roleGroupRefs.get(roleGroupID);

		if (roleGroupRef == null)
			return;

		SecurityChangeController.getInstance().fireSecurityChangeEvent_pre_AuthorizedObjectRef_removeRoleGroupRef(this, roleGroupRef);

		for (Role role : roleGroupRef.getRoleGroup().getRoles()) {
			_removeRole(role, 1);
		}

		roleGroupRefs.remove(roleGroupID);
		roleGroupRef._removeAuthorizedObjectRef(this);
		changeDT = new Date();

		SecurityChangeController.getInstance().fireSecurityChangeEvent_post_AuthorizedObjectRef_removeRoleGroupRef(this, roleGroupRef);
	}

	/**
	 * This method is called internally by addRoleGroup(...) and by
	 * RoleGroup.addRole(...). It increments the reference counter of
	 * the RoleRef and creates a RoleRef if not yet existing.
	 */
	protected void _addRole(Role role, int incRefCount)
	{
		if (role == null)
			throw new NullPointerException("role must not be null!");

		if (incRefCount < 1)
			throw new IllegalArgumentException("incRefCount must be >= 1");

		if (logger.isDebugEnabled())
			logger.debug("_addRole: this=" +  this + " role=" + role + " incRefCount=" + incRefCount);

		SecurityChangeController.getInstance().fireSecurityChangeEvent_pre_AuthorizedObjectRef_addRole(this, role, incRefCount);

		String roleID = role.getRoleID();
		RoleRef roleRef = roleRefs.get(roleID);
		if (roleRef == null) {
			roleRef = new RoleRef(authority, this, role);
			roleRefs.put(roleID, roleRef);
		}
		roleRef.incrementReferenceCount(incRefCount);
		changeDT = new Date();

		SecurityChangeController.getInstance().fireSecurityChangeEvent_post_AuthorizedObjectRef_addRole(this, role, incRefCount);
	}

	protected void _removeRole(Role role, int decRefCount)
	{
		String roleID = role.getRoleID();
		RoleRef roleRef = roleRefs.get(roleID);
		if (roleRef == null) {
			logger.warn("_removeRole(\""+roleID+"\"): The User \""+toString()+"\" does not contain a RoleRef for this role!");
			return;
		}

		if (decRefCount < 1)
			throw new IllegalArgumentException("decRefCount must be >= 1");

		SecurityChangeController.getInstance().fireSecurityChangeEvent_pre_AuthorizedObjectRef_removeRole(this, role, decRefCount);

		if (roleRef.decrementReferenceCount(decRefCount) <= 0) {
			if (roleRef.getReferenceCount() < 0)
				logger.warn("_removeRole(\""+roleID+"\"): referenceCount < 0!!! authorizedObject=\""+toString()+"\"!");

			roleRefs.remove(roleID);
		} // if (roleRef.decrementReferenceCount() <= 0) {
		changeDT = new Date();

		SecurityChangeController.getInstance().fireSecurityChangeEvent_post_AuthorizedObjectRef_removeRole(this, role, decRefCount);
	}

	public RoleGroupRef getRoleGroupRef(RoleGroup roleGroup)
	{
		RoleGroupRef roleGroupRef = roleGroupRefs.get(roleGroup.getRoleGroupID());
		return roleGroupRef;
	}

	public Collection<RoleGroupRef> getRoleGroupRefs()
	{
		return Collections.unmodifiableCollection(roleGroupRefs.values());
	}

	public RoleRef getRoleRef(RoleID roleID)
	{
		return roleRefs.get(roleID.roleID);
	}

	public Collection<RoleRef> getRoleRefs()
	{
		return Collections.unmodifiableCollection(roleRefs.values());
	}

	public boolean containsRoleRef(RoleID roleID)
	{
		return roleRefs.containsKey(roleID.roleID);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + authorityID + ',' + authorizedObjectID + ']';
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (obj instanceof User) {
			logger.error("User is in fact compared to AuthorizedObjectRef! Is that a bug or necessary? IMHO that's a bug! Marco. But an equals method should not throw an exception - it should simply return false.", new Exception());
		}

		if (!(obj instanceof AuthorizedObjectRef))
			return false;

		AuthorizedObjectRef other = (AuthorizedObjectRef)obj;
		return (
				Util.equals(this.organisationID, other.organisationID) &&
				Util.equals(this.authorityID, other.authorityID) &&
				Util.equals(this.authorizedObjectID, other.getAuthorizedObjectID())
		);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + ((authorityID == null) ? 0 : authorityID.hashCode());
		result = prime * result + ((authorizedObjectID == null) ? 0 : authorizedObjectID.hashCode());
		return result;
	}

}
