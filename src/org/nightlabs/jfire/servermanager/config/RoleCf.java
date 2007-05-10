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
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(RoleCf.class);
	
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
		logger.debug("createRole(...): before: pm.getExtent(Role.class, true);");
		pm.getExtent(Role.class, true);
		
		// Fetch/create Role instance.
		Role role;
		try {
			logger.debug("createRole(...): before: role = (Role)pm.getObjectById(RoleID.create(getRoleID()), true);");
			role = (Role)pm.getObjectById(RoleID.create(getRoleID()), true);
			logger.debug("createRole(...): after (role exists): role = (Role)pm.getObjectById(RoleID.create(getRoleID()), true);");
		} catch (JDOObjectNotFoundException x) {
			logger.debug("createRole(...): after (role does NOT exist): role = (Role)pm.getObjectById(RoleID.create(getRoleID()), true);");
			role = new Role(getRoleID());
			logger.debug("createRole(...): before: pm.makePersistent(role);");
			pm.makePersistent(role);
			logger.debug("createRole(...): after: pm.makePersistent(role);");
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
