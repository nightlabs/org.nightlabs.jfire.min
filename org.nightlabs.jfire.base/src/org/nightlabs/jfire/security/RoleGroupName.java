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

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Join;
import org.nightlabs.jfire.security.id.RoleGroupNameID;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *    objectid-class="org.nightlabs.jfire.security.id.RoleGroupNameID"
 *		detachable="true"
 *		table="JFireBase_RoleGroupName"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.create-objectid-class field-order="roleGroupID"
 *
 * @jdo.fetch-group name="RoleGroup.name" fields="roleGroup"
 */
@PersistenceCapable(
	objectIdClass=RoleGroupNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_RoleGroupName")
@FetchGroups(
	@FetchGroup(
		name="RoleGroup.name",
		members=@Persistent(name="roleGroup"))
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class RoleGroupName extends I18nText
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

  /**
   * @jdo.field primary-key="true"
   * @jdo.column length="100"
   */
  @PrimaryKey
  @Column(length=100)
  private String roleGroupID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private RoleGroup roleGroup;
	
	
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected RoleGroupName() {
	}

	public RoleGroupName(RoleGroup roleGroup)
  {
    this.roleGroupID = roleGroup.getRoleGroupID();
    this.roleGroup = roleGroup;
	}

	/**
	 * key: String languageID<br/>
	 * value: String name
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		default-fetch-group="true"
	 *		table="JFireBase_RoleGroupName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_RoleGroupName_names",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, String> names = new HashMap<String, String>();
	
	/* (non-Javadoc)
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map<String, String> getI18nMap()
	{
		return names;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID)
  {
		return roleGroupID;
	}

	public String getRoleGroupID()
  {
		return roleGroupID;
	}

	/**
	 * Get the roleGroup.
	 * @return the roleGroup
	 */
	public RoleGroup getRoleGroup()
	{
		return roleGroup;
	}
	
}
