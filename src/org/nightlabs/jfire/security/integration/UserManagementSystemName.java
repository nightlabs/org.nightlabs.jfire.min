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

package org.nightlabs.jfire.security.integration;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemNameID;

/**
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
@PersistenceCapable(
	objectIdClass=UserManagementSystemNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_UserManagementSystemName")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class UserManagementSystemName extends I18nText
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long userManagementSystemID;

	/**
	 * Named UserManagementSystem
	 */
	@Persistent(defaultFetchGroup="true")
	private UserManagementSystem userManagementSystem;


	public UserManagementSystemName(UserManagementSystem ums) {
		this.organisationID = ums.getOrganisationID();
		this.userManagementSystemID = ums.getUserManagementSystemID();
		this.userManagementSystem = ums;
	}

	@Join
	@Persistent(
		table="JFireBase_UserManagementSystemName_names",
		defaultFetchGroup="true")
	private Map<String, String> names = new HashMap<String, String>();

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
	protected String getFallBackValue(String languageID) {
		return userManagementSystemID+"@"+organisationID;
	}
	
	/**
	 * @return organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	
	/**
	 * 
	 * @return named UserManagementSystem
	 */
	public UserManagementSystem getUserManagementSystem() {
		return userManagementSystem;
	}

}
