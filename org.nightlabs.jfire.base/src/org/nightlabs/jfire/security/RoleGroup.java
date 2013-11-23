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
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.listener.DeleteCallback;

import org.apache.log4j.Logger;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.security.id.RoleID;
import org.nightlabs.util.Util;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.FetchGroup;
import org.nightlabs.jfire.security.id.RoleGroupID;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Key;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;


/**
 * @author marco
 *
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.security.id.RoleGroupID"
 *		detachable="true"
 *		table="JFireBase_RoleGroup"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="roleGroupID"
 *
 * @jdo.fetch-group name="RoleGroup.this" fetch-groups="default" fields="name, description"
 *
 * @jdo.fetch-group name="RoleGroup.name" fields="name"
 * @jdo.fetch-group name="RoleGroup.description" fields="description"
 * @jdo.fetch-group name="RoleGroup.roles" fields="roles"
 * @jdo.fetch-group name="RoleGroup.roleGroupRefs" fields="roleGroupRefs"
 */
@PersistenceCapable(
	objectIdClass=RoleGroupID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_RoleGroup")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name=RoleGroup.FETCH_GROUP_THIS,
		members={@Persistent(name="name"), @Persistent(name="description")}),
	@FetchGroup(
		name=RoleGroup.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		name=RoleGroup.FETCH_GROUP_DESCRIPTION,
		members=@Persistent(name="description")),
	@FetchGroup(
		name=RoleGroup.FETCH_GROUP_ROLES,
		members=@Persistent(name="roles")),
	@FetchGroup(
		name=RoleGroup.FETCH_GROUP_ROLE_GROUP_REFS,
		members=@Persistent(name="roleGroupRefs"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class RoleGroup
implements Serializable, DeleteCallback
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(RoleGroup.class);

	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS = "RoleGroup.this";

	public static final String FETCH_GROUP_NAME = "RoleGroup.name";
	public static final String FETCH_GROUP_DESCRIPTION = "RoleGroup.description";
	public static final String FETCH_GROUP_ROLES = "RoleGroup.roles";
	public static final String FETCH_GROUP_ROLE_GROUP_REFS = "RoleGroup.roleGroupRefs";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String roleGroupID;

  /**
   * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="roleGroup"
   */
  @Persistent(
  	dependent="true",
  	mappedBy="roleGroup",
  	persistenceModifier=PersistenceModifier.PERSISTENT)
  private RoleGroupName name;

  /**
   * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="roleGroup"
   */
  @Persistent(
  	dependent="true",
  	mappedBy="roleGroup",
  	persistenceModifier=PersistenceModifier.PERSISTENT)
  private RoleGroupDescription description;

	/**
	 * key: String roleID<br/>
	 * value: Role role
	 * <br/><br/>
	 * RoleGroup (m) - (n) Role
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="Role"
	 *		table="JFireBase_RoleGroup_roles"
	 *		null-value="exception"
	 *
	 * @jdo.key mapped-by="roleID"
	 *
	 * @jdo.join
	 *
	 * @!jdo.map-vendor-extension vendor-name="jpox" key="key-field" value="roleID"
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_RoleGroup_roles",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Key(mappedBy="roleID")
	private Map<String, Role> roles = new HashMap<String, Role>();

	/**
	 * key: String authorityID<br/>
	 * value: RoleGroupRef roleGroupRef
	 * <br/><br/>
	 * RoleGroup (1) - (n) RoleGroupRef
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="RoleGroupRef"
	 *		mapped-by="roleGroup"
	 *		null-value="exception"
	 *
	 * @jdo.key mapped-by="authorityID"
	 *
	 * @!jdo.map-vendor-extension vendor-name="jpox" key="key-field" value="authorityID"
	 * @!jdo.map-vendor-extension vendor-name="jpox" key="owner-field" value="roleGroup"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		mappedBy="roleGroup",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Key(mappedBy="authorityID")
	private Map<String, RoleGroupRef> roleGroupRefs = new HashMap<String, RoleGroupRef>();

	public RoleGroup() { }

	public RoleGroup(String _roleGroupID)
	{
		if (!ObjectIDUtil.isValidIDString(_roleGroupID))
			throw new IllegalArgumentException("roleID \""+_roleGroupID+"\" is not a valid id!");
		this.roleGroupID = _roleGroupID;
    this.name = new RoleGroupName(this);
    this.description = new RoleGroupDescription(this);
	}

	/**
	 * @return Returns the roleGroupID.
	 */
	public String getRoleGroupID()
  {
		return roleGroupID;
	}

	/**
	 * @return Returns the name.
	 */
	public I18nText getName()
  {
		return name;
	}

	/**
	 * @return Returns the description.
	 */
	public I18nText getDescription()
  {
    return description;
	}

	/**
	 * @return Returns the roles.
	 */
	public Collection<Role> getRoles()
  {
		return Collections.unmodifiableCollection(roles.values());
	}

	/**
	 * @return Returns the roleGroupRefs.
	 */
	public Collection<RoleGroupRef> getRoleGroupRefs() {
		return roleGroupRefs.values();
	}

	/**
	 * Get the role for the given id.
	 * This method will return <code>null</code>, if there is no role with the given id.
	 * @param roleID the simple String-id of the role.
	 * @return the requested role or <code>null</code>.
	 */
	public Role getRole(String roleID)
	{
		return roles.get(roleID);
	}

	public void addRole(Role role)
	{
		String roleID = role.getRoleID();
		logger.debug("addRole(...): before: if (roles.containsKey(roleID))");
		if (roles.containsKey(roleID))
			return;
		logger.debug("addRole(...): before: roles.put(roleID, role);");
		roles.put(roleID, role);
		logger.debug("addRole(...): before: role._addRoleGroup(this);");
		role._addRoleGroup(this);

		// Add this role to all all roleGroupRefs
		logger.debug("addRole(...): before: for (Iterator it = getRoleGroupRefs().iterator(); it.hasNext(); ) {");
		for (RoleGroupRef roleGroupRef : getRoleGroupRefs()) {
			roleGroupRef._addRole(role);
		}
	}

	public void removeRole(Role role)
	{
		String roleID = role.getRoleID();
		if (!roles.containsKey(roleID))
			return;

		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm != null)
			pm.flush();

		// Remove this role from all roleGroupRefs
		for (RoleGroupRef roleGroupRef : getRoleGroupRefs()) {
			roleGroupRef._removeRole(role);
		}

		role._removeRoleGroup(this);
		roles.remove(role.getRoleID());

		if (pm != null)
			pm.flush();
	}

	public boolean containsRole(Role role)
	{
		return roles.containsKey(role.getRoleID());
	}

	public boolean containsRole(RoleID roleID)
	{
		return roles.containsKey(roleID.roleID);
	}

	protected void _addRoleGroupRef(RoleGroupRef roleGroupRef)
	{
		if (roleGroupRef == null)
			throw new NullPointerException("roleGroupRef must not be null!");

		if (!this.roleGroupID.equals(roleGroupRef.getRoleGroupID()))
			throw new IllegalArgumentException("roleGroupRef.roleGroupID invalid!");

		roleGroupRefs.put(roleGroupRef.getAuthorityID(), roleGroupRef);
	}

	protected void _removeRoleGroupRef(String authorityID)
	{
		roleGroupRefs.remove(authorityID);
	}

	public RoleGroupRef getRoleGroupRef(String authorityID)
	{
		return roleGroupRefs.get(authorityID);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((roleGroupID == null) ? 0 : roleGroupID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final RoleGroup other = (RoleGroup) obj;
		return Util.equals(this.roleGroupID, other.roleGroupID);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + roleGroupID + ']';
	}

	@Override
	public void jdoPreDelete() {
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("JDOHelper.getPersistenceManager(this) returned null in jdoPreDelete!");

		Query q1 = pm.newQuery(RoleGroupRef.class);
		q1.setFilter("this.roleGroup == :roleGroup");
		Collection<?> c1 = (Collection<?>) q1.execute(this);
		for (Object o1 : c1) {
			RoleGroupRef roleGroupRef = (RoleGroupRef) o1;
			roleGroupRef.getAuthority().destroyRoleGroupRef(roleGroupRef.getRoleGroup());
		}

		Query q2 = pm.newQuery(AuthorityType.class);
		q2.setFilter("this.roleGroups.contains(:roleGroup)");
		Collection<?> c2 = (Collection<?>) q2.execute(this);
		for (Object o2 : c2) {
			AuthorityType authorityType = (AuthorityType) o2;
			authorityType.removeRoleGroup(this);
		}
	}
}
