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

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.util.Util;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Key;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import org.nightlabs.jfire.security.id.RoleID;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author nick
 * @author marco schulze - marco at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.security.id.RoleID"
 *		detachable="true"
 *		table="JFireBase_Role"
 *
 * @jdo.create-objectid-class field-order="roleID"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="Role.roleGroups" fields="roleGroups"
 */
@PersistenceCapable(
	objectIdClass=RoleID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Role")
@FetchGroups(
	@FetchGroup(
		name=Role.FETCH_GROUP_ROLE_GROUPS,
		members=@Persistent(name="roleGroups"))
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Role implements Serializable
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_ROLE_GROUPS = "Role.roleGroups";

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(Role.class);

	/**
	 * @jdo.field persistence-modifier="persistent" primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=100)
	private String roleID;


	/**
	 * key: String roleGroupID<br/>
	 * value: RoleGroup roleGroup
	 * <br/><br/>
	 * User (m) - (n) RoleGroup
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="RoleGroup"
	 *		table="JFireBase_Role_roleGroups"
	 *		null-value="exception"
	 *
	 * @jdo.key mapped-by="roleGroupID"
	 *
	 * @jdo.join
	 *
	 * @!jdo.map-vendor-extension vendor-name="jpox" key="key-field" value="roleGroupID"
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_Role_roleGroups",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Key(mappedBy="roleGroupID")
	private Map<String, RoleGroup> roleGroups = new HashMap<String, RoleGroup>();


	public Role() { }

	public Role(String _roleID)
	{
		if (!ObjectIDUtil.isValidIDString(_roleID))
			throw new IllegalArgumentException("roleID \""+_roleID+"\" is not a valid id!");
		this.roleID = _roleID;
	}

	protected static String EMPTYSTRING = "";


	/**
	 * @return Returns the roleID.
	 */
	public String getRoleID()
	{
		return roleID;
	}

	/**
	 * @return Returns the roleGroups.
	 */
	public Collection<RoleGroup> getRoleGroups() {
		return Collections.unmodifiableCollection(roleGroups.values());
	}

	/**
	 * This method is executed by RoleGroup.addRole(...).
	 *
	 * @param roleGroup
	 */
	protected void _addRoleGroup(RoleGroup roleGroup)
	{
		String roleGroupID = roleGroup.getRoleGroupID();
		if (roleGroups.containsKey(roleGroupID)) {
			logger.warn("_addRoleGroup(\""+roleGroupID+"\"): roleGroup was already added.");
			return;
		}
		roleGroups.put(roleGroupID, roleGroup);
	}

	/**
	 * This method is executed by RoleGroup.removeRole(...).
	 * @param roleGroup
	 */
	protected void _removeRoleGroup(RoleGroup roleGroup)
	{
		if (roleGroups.remove(roleGroup.getRoleGroupID()) == null)
			logger.warn("_removeRoleGroup(\""+roleGroup.getRoleGroupID()+"\"): roleGroup was not existing.");
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + roleID + ']';
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((roleID == null) ? 0 : roleID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final Role other = (Role) obj;
		return Util.equals(this.roleID, other.roleID);
	}

}
