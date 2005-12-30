/* ************************************************************************** *
 * Copyright (C) 2004 NightLabs GmbH, Marco Schulze                           *
 * All rights reserved.                                                       *
 * http://www.NightLabs.de                                                    *
 *                                                                            *
 * This program and the accompanying materials are free software; you can re- *
 * distribute it and/or modify it under the terms of the GNU General Public   *
 * License as published by the Free Software Foundation; either ver 2 of the  *
 * License, or any later version.                                             *
 *                                                                            *
 * This module is distributed in the hope that it will be useful, but WITHOUT *
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FIT- *
 * NESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more *
 * details.                                                                   *
 *                                                                            *
 * You should have received a copy of the GNU General Public License along    *
 * with this module; if not, write to the Free Software Foundation, Inc.:     *
 *    59 Temple Place, Suite 330                                              *
 *    Boston MA 02111-1307                                                    *
 *    USA                                                                     *
 *                                                                            *
 * Or get it online:                                                          *
 *    http://www.opensource.org/licenses/gpl-license.php                      *
 *                                                                            *
 * In case, you want to use this module or parts of it in a proprietary pro-  *
 * ject, you can purchase it under the NightLabs Commercial License. Please   *
 * contact NightLabs GmbH under info AT nightlabs DOT com for more infos or   *
 * visit http://www.NightLabs.com                                             *
 * ************************************************************************** */

/*
 * Created on 17.06.2004
 */
package org.nightlabs.jfire.servermanager.config;

import java.io.Serializable;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.security.Role;
import org.nightlabs.jfire.security.id.RoleID;

import org.nightlabs.config.InitException;
import org.nightlabs.config.Initializable;
import org.nightlabs.jdo.ObjectIDUtil;

/**
 * @author marco
 */
public class RoleCf implements Serializable, Initializable
{
	public static final Logger LOGGER = Logger.getLogger(RoleCf.class);
	
	private String roleID = null;
//	private String description = null;

	public RoleCf() { }
	public RoleCf(String _roleID) // , String _description)
		throws InitException
	{
		if (!ObjectIDUtil.isValidIDString(_roleID))
			throw new IllegalArgumentException("roleID \""+_roleID+"\" is not a valid id!");
		this.roleID = _roleID;
//		this.description = _description;
		init();
	}
	
	/**
	 * @return Returns the roleID.
	 */
	public String getRoleID() {
		return roleID;
	}
	/**
	 * @param roleID The roleID to set.
	 */
	public void setRoleID(String _roleID) {
		if (!ObjectIDUtil.isValidIDString(_roleID))
			throw new IllegalArgumentException("roleID \""+_roleID+"\" is not a valid id!");
		this.roleID = _roleID;
	}

//	/**
//	 * @return Returns the description.
//	 */
//	public String getDescription() {
//		return description;
//	}
//	/**
//	 * @param description The description to set.
//	 */
//	public void setDescription(String description) {
//		this.description = description;
//	}

	public Role createRole(PersistenceManager pm)
	{
		// Initialize meta data.
		LOGGER.debug("createRole(...): before: pm.getExtent(Role.class, true);");
		pm.getExtent(Role.class, true);
		
		// Fetch/create Role instance.
		Role role;
		try {
			LOGGER.debug("createRole(...): before: role = (Role)pm.getObjectById(RoleID.create(getRoleID()), true);");
			role = (Role)pm.getObjectById(RoleID.create(getRoleID()), true);
			LOGGER.debug("createRole(...): after (role exists): role = (Role)pm.getObjectById(RoleID.create(getRoleID()), true);");
		} catch (JDOObjectNotFoundException x) {
			LOGGER.debug("createRole(...): after (role does NOT exist): role = (Role)pm.getObjectById(RoleID.create(getRoleID()), true);");
			role = new Role(getRoleID());
			LOGGER.debug("createRole(...): before: pm.makePersistent(role);");
			pm.makePersistent(role);
			LOGGER.debug("createRole(...): after: pm.makePersistent(role);");
		}
		return role;
	}

	/**
	 * @see org.nightlabs.config.Initializable#init()
	 */
	public void init() throws InitException
	{
		if (roleID == null) 
			throw new NullPointerException("roleID must never be null!");
		
		if ("".equals(roleID))
			throw new InitException("roleID must never be empty!");
		
//		if (description == null)
//			description = "";
	}
}
